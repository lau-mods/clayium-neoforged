package net.claustra01.clayium.gametest.fluid;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.fluid.FluidFilterSettings;
import net.claustra01.clayium.fluid.FluidTransportBlockEntity;
import net.claustra01.clayium.fluid.FluidTransportSpecification;
import net.claustra01.clayium.registry.FluidRegistration;
import net.claustra01.clayium.registry.FoundationCreativeTabRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FluidTransportGameTests {
    private static final Map<BlockPos, TestTank> TEST_TANKS = new HashMap<>();

    public static void registerTestCapability(RegisterCapabilitiesEvent event) {
        event.registerBlock(
                Capabilities.FluidHandler.BLOCK,
                (level, pos, state, blockEntity, side) -> TEST_TANKS.get(pos),
                Blocks.IRON_BLOCK);
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void allFluidTiersTransferDirectlyAndConservePendingFluid(GameTestHelper helper) {
        int[] capacities = {
                4_000, 8_000, 16_000, 64_000, 128_000,
                256_000, 1_024_000, 4_096_000, 16_384_000, 65_536_000
        };
        int[] speeds = {250, 500, 1_000, 4_000, 8_000, 16_000, 64_000, 256_000, 1_024_000, 4_096_000};
        BlockPos machinePosition = new BlockPos(2, 2, 2);
        BlockPos receiverPosition = helper.absolutePos(machinePosition.relative(Direction.EAST));

        for (int tier = 4; tier <= 13; tier++) {
            FluidTransportSpecification specification = FluidTransportSpecification.TRANSLATORS.get(tier - 4);
            require(specification.tier() == tier
                            && specification.capacity() == capacities[tier - 4]
                            && specification.transferPerTick() == speeds[tier - 4],
                    "Tier " + tier + " differs from GR-06");
            FluidTransportBlockEntity transport = placed(helper, specification, machinePosition);
            require(transport.faceMode(Direction.DOWN) == FluidTransportBlockEntity.FaceMode.OUTPUT
                            && java.util.Arrays.stream(Direction.values())
                                    .filter(direction -> direction != Direction.DOWN)
                                    .allMatch(direction -> transport.faceMode(direction)
                                            == FluidTransportBlockEntity.FaceMode.INPUT),
                    "Tier " + tier + " has the wrong default face configuration");
            transport.setFaceMode(Direction.EAST, FluidTransportBlockEntity.FaceMode.OUTPUT);
            transport.tank().fill(new FluidStack(Fluids.WATER, specification.capacity()),
                    IFluidHandler.FluidAction.EXECUTE);
            TestTank receiver = new TestTank(specification.capacity() * 2);
            TEST_TANKS.put(receiverPosition, receiver);
            helper.getLevel().setBlockAndUpdate(receiverPosition, Blocks.IRON_BLOCK.defaultBlockState());
            helper.getLevel().invalidateCapabilities(receiverPosition);

            FluidTransportBlockEntity.serverTick(
                    helper.getLevel(), transport.getBlockPos(), transport.getBlockState(), transport);
            require(receiver.amount() == specification.transferPerTick(),
                    "Tier " + tier + " did not use its one-tick total transfer limit");
            require(transport.tank().getFluidAmount()
                            == specification.capacity() - specification.transferPerTick(),
                    "Tier " + tier + " lost or duplicated fluid");
            require(transport.tank().getCapacity() == specification.capacity(),
                    "Tier " + tier + " exposes the wrong tank capacity");
            IFluidHandler inputCapability = helper.getLevel().getCapability(
                    Capabilities.FluidHandler.BLOCK, transport.getBlockPos(), Direction.NORTH);
            IFluidHandler outputCapability = helper.getLevel().getCapability(
                    Capabilities.FluidHandler.BLOCK, transport.getBlockPos(), Direction.EAST);
            require(inputCapability != null && outputCapability != null
                            && inputCapability.fill(new FluidStack(Fluids.WATER, 1),
                                    IFluidHandler.FluidAction.SIMULATE) == 0
                            && outputCapability.fill(new FluidStack(Fluids.WATER, 1),
                                    IFluidHandler.FluidAction.SIMULATE) == 0,
                    "Tier " + tier + " does not expose its side-aware standard fluid capability");

            CompoundTag saved = transport.saveWithFullMetadata(helper.getLevel().registryAccess());
            FluidTransportBlockEntity loaded = new FluidTransportBlockEntity(
                    transport.getBlockPos(), transport.getBlockState());
            loaded.loadWithComponents(saved, helper.getLevel().registryAccess());
            require(loaded.tank().getFluidAmount() == transport.tank().getFluidAmount()
                            && loaded.tank().getFluid().is(Fluids.WATER),
                    "Tier " + tier + " did not save fluid type and amount");
        }

        FluidTransportSpecification tierFive = FluidTransportSpecification.TRANSLATORS.get(1);
        require(FluidTransportSpecification.TRANSFER_MACHINE.tier() == 5
                        && FluidTransportSpecification.TRANSFER_MACHINE.capacity() == 8_000
                        && FluidTransportSpecification.TRANSFER_MACHINE.transferPerTick() == 500,
                "The direct fluid transfer machine differs from its Tier 5 transport values");
        FluidTransportBlockEntity transferMachine = placed(
                helper, FluidTransportSpecification.TRANSFER_MACHINE, machinePosition);
        transferMachine.setFaceMode(Direction.EAST, FluidTransportBlockEntity.FaceMode.OUTPUT);
        transferMachine.tank().fill(new FluidStack(Fluids.WATER, 500), IFluidHandler.FluidAction.EXECUTE);
        TestTank transferReceiver = new TestTank(1_000);
        TEST_TANKS.put(receiverPosition, transferReceiver);
        helper.getLevel().invalidateCapabilities(receiverPosition);
        FluidTransportBlockEntity.serverTick(
                helper.getLevel(), transferMachine.getBlockPos(), transferMachine.getBlockState(), transferMachine);
        require(transferReceiver.amount() == 500 && transferMachine.tank().isEmpty(),
                "The FluidTransferMachine did not use the shared direct-fluid transport path");

        FluidTransportBlockEntity transport = placed(helper, tierFive, machinePosition);
        transport.setFaceMode(Direction.EAST, FluidTransportBlockEntity.FaceMode.OUTPUT);
        transport.tank().fill(new FluidStack(Fluids.WATER, 1_000), IFluidHandler.FluidAction.EXECUTE);
        TestTank changingReceiver = new TestTank(2_000);
        changingReceiver.executeLimit = 125;
        TEST_TANKS.put(receiverPosition, changingReceiver);
        helper.getLevel().invalidateCapabilities(receiverPosition);
        FluidTransportBlockEntity.serverTick(
                helper.getLevel(), transport.getBlockPos(), transport.getBlockState(), transport);
        require(changingReceiver.amount() == 125
                        && transport.tank().getFluidAmount() == 500
                        && transport.pending().getFluidAmount() == 375,
                "A reduced execute acceptance was not recovered into pending storage");
        require(changingReceiver.amount() + transport.tank().getFluidAmount()
                        + transport.pending().getFluidAmount() == 1_000,
                "Fluid was not conserved across sender, receiver, and pending storage");
        CompoundTag saved = transport.saveWithFullMetadata(helper.getLevel().registryAccess());
        FluidTransportBlockEntity loaded = new FluidTransportBlockEntity(
                transport.getBlockPos(), transport.getBlockState());
        loaded.loadWithComponents(saved, helper.getLevel().registryAccess());
        require(loaded.pending().getFluidAmount() == 375,
                "Pending fluid did not survive save and reload");

        FluidTransportBlockEntity limited = placed(helper, tierFive, new BlockPos(8, 2, 2));
        IFluidHandler input = helper.getLevel().getCapability(
                Capabilities.FluidHandler.BLOCK, limited.getBlockPos(), Direction.UP);
        require(input != null && input.fill(new FluidStack(Fluids.WATER, 2_000),
                        IFluidHandler.FluidAction.EXECUTE) == 500,
                "Capability fill exceeded the machine's per-tick limit");
        limited.setFaceMode(Direction.DOWN, FluidTransportBlockEntity.FaceMode.OUTPUT);
        IFluidHandler output = helper.getLevel().getCapability(
                Capabilities.FluidHandler.BLOCK, limited.getBlockPos(), Direction.DOWN);
        require(output != null && output.drain(2_000, IFluidHandler.FluidAction.EXECUTE).isEmpty(),
                "Capability drain exceeded the shared per-tick limit");
        FluidTransportBlockEntity draining = placed(helper, tierFive, new BlockPos(9, 2, 2));
        draining.tank().fill(new FluidStack(Fluids.WATER, 2_000), IFluidHandler.FluidAction.EXECUTE);
        IFluidHandler drainCapability = helper.getLevel().getCapability(
                Capabilities.FluidHandler.BLOCK, draining.getBlockPos(), Direction.DOWN);
        require(drainCapability != null && drainCapability.drain(2_000,
                        IFluidHandler.FluidAction.EXECUTE).getAmount() == 500,
                "Capability drain was not clamped to the machine's per-tick limit");

        TEST_TANKS.clear();
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void fluidFiltersFacesAndRestartPreserveExactAmounts(GameTestHelper helper) {
        FluidTransportSpecification specification = FluidTransportSpecification.TRANSLATORS.get(1);
        BlockPos machinePosition = new BlockPos(2, 2, 2);
        BlockPos sourcePosition = helper.absolutePos(machinePosition.relative(Direction.NORTH));
        FluidTransportBlockEntity transport = placed(helper, specification, machinePosition);
        transport.tank().fill(new FluidStack(Fluids.LAVA, 250), IFluidHandler.FluidAction.EXECUTE);
        transport.toggleFilterMode();
        transport.filterCurrentFluid();
        require(transport.filterInventory().getStackInSlot(0).isEmpty(),
                "Filter buttons created an item without an installed filter");
        ItemStack filter = FluidRegistration.FLUID_FILTER.toStack();
        filter.set(FluidRegistration.FLUID_FILTER_SETTINGS.get(), new FluidFilterSettings(
                Optional.of(BuiltInRegistries.FLUID.getKey(Fluids.WATER)), false));
        transport.filterInventory().setStackInSlot(0, filter);
        transport.setFaceMode(Direction.EAST, FluidTransportBlockEntity.FaceMode.OUTPUT);
        BlockPos receiverPosition = helper.absolutePos(machinePosition.relative(Direction.EAST));
        TestTank receiver = new TestTank(2_000);
        TEST_TANKS.put(receiverPosition, receiver);
        helper.getLevel().setBlockAndUpdate(receiverPosition, Blocks.IRON_BLOCK.defaultBlockState());
        helper.getLevel().invalidateCapabilities(receiverPosition);
        IFluidHandler filteredOutput = helper.getLevel().getCapability(
                Capabilities.FluidHandler.BLOCK, transport.getBlockPos(), Direction.EAST);
        require(filteredOutput != null
                        && filteredOutput.drain(250, IFluidHandler.FluidAction.SIMULATE).isEmpty(),
                "An output face ignored the installed fluid filter");
        FluidTransportBlockEntity.serverTick(
                helper.getLevel(), transport.getBlockPos(), transport.getBlockState(), transport);
        require(receiver.amount() == 0 && transport.tank().getFluidAmount() == 250,
                "Non-matching fluid was pushed through a filtered output");
        transport.tank().drain(250, IFluidHandler.FluidAction.EXECUTE);

        TestTank source = new TestTank(2_000);
        source.fluid = new FluidStack(Fluids.LAVA, 1_000);
        TEST_TANKS.put(sourcePosition, source);
        helper.getLevel().setBlockAndUpdate(sourcePosition, Blocks.IRON_BLOCK.defaultBlockState());
        helper.getLevel().invalidateCapabilities(sourcePosition);
        FluidTransportBlockEntity.serverTick(
                helper.getLevel(), transport.getBlockPos(), transport.getBlockState(), transport);
        require(transport.tank().isEmpty() && source.amount() == 1_000,
                "A fluid rejected by the filter was transferred");

        source.fluid = new FluidStack(Fluids.WATER, 1_000);
        FluidTransportBlockEntity.serverTick(
                helper.getLevel(), transport.getBlockPos(), transport.getBlockState(), transport);
        require(transport.tank().getFluidAmount() == 500 && source.amount() == 500,
                "The matching fluid did not move through an input face");

        transport.setFaceMode(Direction.SOUTH, FluidTransportBlockEntity.FaceMode.DISABLED);
        transport.setRedstoneCondition(FluidTransportBlockEntity.RedstoneCondition.UNPOWERED);
        CompoundTag beforeTransfer = transport.saveWithFullMetadata(helper.getLevel().registryAccess());
        FluidTransportBlockEntity transferring = new FluidTransportBlockEntity(
                transport.getBlockPos(), transport.getBlockState());
        transferring.loadWithComponents(beforeTransfer, helper.getLevel().registryAccess());
        transferring.setLevel(helper.getLevel());
        receiver.executeLimit = 125;
        FluidTransportBlockEntity.serverTick(helper.getLevel(), transferring.getBlockPos(),
                transferring.getBlockState(), transferring);
        require(receiver.amount() == 125 && transferring.pending().getFluidAmount() == 375
                        && transferring.tank().isEmpty(),
                "Reduced acceptance did not preserve the rejected matching fluid: receiver=" + receiver.amount()
                        + ", pending=" + transferring.pending().getFluidAmount()
                        + ", tank=" + transferring.tank().getFluidAmount());
        transferring.setFaceMode(Direction.NORTH, FluidTransportBlockEntity.FaceMode.BOTH);
        CompoundTag saved = transferring.saveWithFullMetadata(helper.getLevel().registryAccess());
        FluidTransportBlockEntity loaded = new FluidTransportBlockEntity(
                transport.getBlockPos(), transport.getBlockState());
        loaded.loadWithComponents(saved, helper.getLevel().registryAccess());
        loaded.setLevel(helper.getLevel());
        require(loaded.faceMode(Direction.NORTH) == FluidTransportBlockEntity.FaceMode.BOTH
                        && loaded.faceMode(Direction.SOUTH) == FluidTransportBlockEntity.FaceMode.DISABLED
                        && loaded.redstoneCondition() == FluidTransportBlockEntity.RedstoneCondition.UNPOWERED,
                "Face and redstone settings did not survive restart");
        require(loaded.pending().getFluidAmount() == 375
                        && loaded.filterSettings().fluid().orElseThrow().equals(
                                BuiltInRegistries.FLUID.getKey(Fluids.WATER)),
                "Pending fluid or the Data Component filter did not survive restart");
        source.fluid = new FluidStack(Fluids.LAVA, 500);
        receiver.executeLimit = Integer.MAX_VALUE;
        FluidTransportBlockEntity.serverTick(helper.getLevel(), loaded.getBlockPos(), loaded.getBlockState(), loaded);
        require(loaded.pending().isEmpty() && receiver.amount() == 500
                        && loaded.tank().getFluidAmount() + loaded.pending().getFluidAmount()
                                + receiver.amount() == 500,
                "Restored pending fluid was not resent without loss or duplication");

        FluidTransportBlockEntity noRoundTrip = placed(helper, specification, new BlockPos(6, 2, 2));
        noRoundTrip.setFaceMode(Direction.EAST, FluidTransportBlockEntity.FaceMode.BOTH);
        BlockPos roundTripPosition = helper.absolutePos(new BlockPos(7, 2, 2));
        TestTank roundTripReceiver = new TestTank(2_000);
        TEST_TANKS.put(roundTripPosition, roundTripReceiver);
        helper.getLevel().setBlockAndUpdate(roundTripPosition, Blocks.IRON_BLOCK.defaultBlockState());
        helper.getLevel().invalidateCapabilities(roundTripPosition);
        IFluidHandler input = helper.getLevel().getCapability(
                Capabilities.FluidHandler.BLOCK, noRoundTrip.getBlockPos(), Direction.UP);
        require(input != null && input.fill(new FluidStack(Fluids.WATER, 250),
                        IFluidHandler.FluidAction.EXECUTE) == 250,
                "The standard capability did not accept direct fluid input");
        FluidTransportBlockEntity.serverTick(
                helper.getLevel(), noRoundTrip.getBlockPos(), noRoundTrip.getBlockState(), noRoundTrip);
        require(roundTripReceiver.amount() == 250 && noRoundTrip.tank().isEmpty(),
                "An input on one face stopped output through another face");
        FluidTransportBlockEntity sameFace = placed(helper, specification, new BlockPos(9, 2, 2));
        sameFace.setFaceMode(Direction.EAST, FluidTransportBlockEntity.FaceMode.BOTH);
        BlockPos sameFacePosition = helper.absolutePos(new BlockPos(10, 2, 2));
        TestTank sameFaceReceiver = new TestTank(2_000);
        TEST_TANKS.put(sameFacePosition, sameFaceReceiver);
        helper.getLevel().setBlockAndUpdate(sameFacePosition, Blocks.IRON_BLOCK.defaultBlockState());
        helper.getLevel().invalidateCapabilities(sameFacePosition);
        sameFace.tank().fill(new FluidStack(Fluids.WATER, 250), IFluidHandler.FluidAction.EXECUTE);
        FluidTransportBlockEntity.serverTick(helper.getLevel(), sameFace.getBlockPos(),
                sameFace.getBlockState(), sameFace);
        require(sameFaceReceiver.amount() == 250 && sameFace.tank().isEmpty(),
                "A BOTH face pulled fluid back after pushing it in the same tick");
        TEST_TANKS.clear();
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void capsulesStayRemovedAndAllDirectFluidMachinesAreRegistered(GameTestHelper helper) {
        require(FluidRegistration.MACHINES.size() == 11,
                "The ten translators and one transfer machine are not all registered");
        BuiltInRegistries.ITEM.keySet().stream()
                .filter(id -> id.getNamespace().equals(ClayiumMod.MOD_ID))
                .forEach(id -> require(!id.getPath().contains("capsule"), "Removed item is registered: " + id));
        BuiltInRegistries.BLOCK.keySet().stream()
                .filter(id -> id.getNamespace().equals(ClayiumMod.MOD_ID))
                .forEach(id -> require(!id.getPath().contains("capsule"), "Removed block is registered: " + id));
        require(BuiltInRegistries.ITEM.containsKey(ClayiumMod.id("fluid_filter")),
                "The direct-fluid filter is missing");
        require(FluidTransportSpecification.VALUES.stream().allMatch(specification ->
                        BuiltInRegistries.BLOCK.containsKey(ClayiumMod.id(specification.id()))),
                "A direct-fluid machine registry ID is missing");
        require(helper.getLevel().getRecipeManager().getRecipes().stream().noneMatch(holder ->
                        holder.id().getPath().contains("capsule")
                        || holder.value().getIngredients().stream().flatMap(ingredient ->
                                java.util.Arrays.stream(ingredient.getItems())).anyMatch(
                                stack -> isCapsule(stack))
                        || isCapsule(holder.value().getResultItem(helper.getLevel().registryAccess()))),
                "A recipe for a removed fluid capsule is still loaded");
        CreativeModeTab tab = FoundationCreativeTabRegistration.MAIN.get();
        tab.buildContents(new CreativeModeTab.ItemDisplayParameters(
                FeatureFlags.DEFAULT_FLAGS, false, helper.getLevel().registryAccess()));
        require(tab.getDisplayItems().stream().noneMatch(FluidTransportGameTests::isCapsule),
                "A removed fluid capsule appears in the creative tab");
        helper.succeed();
    }

    private static boolean isCapsule(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id.getNamespace().equals(ClayiumMod.MOD_ID) && id.getPath().contains("capsule");
    }

    private static FluidTransportBlockEntity placed(
            GameTestHelper helper, FluidTransportSpecification specification, BlockPos position) {
        FluidRegistration.Definition definition = FluidRegistration.MACHINES.get(specification.id());
        helper.setBlock(position, definition.block().get());
        return helper.getBlockEntity(position);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private static final class TestTank implements IFluidHandler {
        private final int capacity;
        private FluidStack fluid = FluidStack.EMPTY;
        private int executeLimit = Integer.MAX_VALUE;

        private TestTank(int capacity) {
            this.capacity = capacity;
        }

        private int amount() {
            return fluid.getAmount();
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return fluid;
        }

        @Override
        public int getTankCapacity(int tank) {
            return capacity;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || !fluid.isEmpty()
                    && !FluidStack.isSameFluidSameComponents(resource, fluid)) {
                return 0;
            }
            int accepted = Math.min(resource.getAmount(), capacity - amount());
            if (action.execute()) {
                accepted = Math.min(accepted, executeLimit);
                if (accepted > 0) {
                    if (fluid.isEmpty()) {
                        fluid = resource.copyWithAmount(accepted);
                    } else {
                        fluid.grow(accepted);
                    }
                }
            }
            return accepted;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || fluid.isEmpty()
                    || !FluidStack.isSameFluidSameComponents(resource, fluid)) {
                return FluidStack.EMPTY;
            }
            return drain(resource.getAmount(), action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            int drainedAmount = Math.min(Math.max(0, maxDrain), amount());
            if (drainedAmount == 0) {
                return FluidStack.EMPTY;
            }
            FluidStack drained = fluid.copyWithAmount(drainedAmount);
            if (action.execute()) {
                fluid.shrink(drainedAmount);
                if (fluid.isEmpty()) {
                    fluid = FluidStack.EMPTY;
                }
            }
            return drained;
        }
    }

    private FluidTransportGameTests() {
    }
}
