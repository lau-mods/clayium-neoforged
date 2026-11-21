package net.claustra01.clayium.gametest.iface;

import java.util.Set;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.claustra01.clayium.machine.iface.InterfaceBlockEntity;
import net.claustra01.clayium.machine.iface.InterfaceCapabilities;
import net.claustra01.clayium.machine.iface.LaserReceiver;
import net.claustra01.clayium.registry.FoundationDataComponentRegistration;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import java.util.Map;
import java.util.HashMap;
import net.claustra01.clayium.machine.iface.InterfaceType;
import net.claustra01.clayium.machine.iface.SynchronizedTarget;
import net.claustra01.clayium.machine.series.ProcessingSeries;
import net.claustra01.clayium.registry.InterfaceRegistration;
import net.claustra01.clayium.registry.MachineRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class InterfaceGameTests {
    private static final Map<BlockPos, LaserReceiver> LASER_RECEIVERS = new HashMap<>();

    public static void registerTestCapability(RegisterCapabilitiesEvent event) {
        event.registerBlock(InterfaceCapabilities.LASER_RECEIVER,
                (level, pos, state, blockEntity, side) -> LASER_RECEIVERS.get(pos), Blocks.CHEST);
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void allInterfacesSynchronizeOnlyToValidatedPersistentTargets(GameTestHelper helper) {
        require(InterfaceRegistration.INTERFACES.size() == 25, "The three interface series do not contain 25 blocks");
        assertTiers(InterfaceType.ITEM, Set.of(5, 6, 7, 8, 9, 10, 11, 12, 13));
        assertTiers(InterfaceType.REDSTONE, Set.of(5, 6, 7, 8, 9, 10, 11, 12, 13));
        assertTiers(InterfaceType.LASER, Set.of(7, 8, 9, 10, 11, 12, 13));

        BlockPos targetPosition = new BlockPos(2, 2, 2);
        BlockPos otherPosition = new BlockPos(2, 2, 4);
        helper.setBlock(targetPosition, Blocks.CHEST);
        helper.setBlock(otherPosition, Blocks.CHEST);
        InterfaceBlockEntity itemInterface = place(helper, InterfaceType.ITEM, 5, new BlockPos(1, 2, 2));
        ItemStack synchronizer = new ItemStack(InterfaceRegistration.SYNCHRONIZER.get());
        var player = helper.makeMockServerPlayerInLevel();
        helper.getLevel().getServer().getPlayerList().op(player.getGameProfile());
        player.setItemInHand(InteractionHand.MAIN_HAND, synchronizer);
        useSynchronizer(helper, player, targetPosition);
        SynchronizedTarget recorded = synchronizer.get(InterfaceRegistration.SYNCHRONIZED_TARGET.get());
        require(recorded != null && recorded.pos().equals(helper.absolutePos(targetPosition)),
                "The synchronizer did not record the clicked target");
        useSynchronizer(helper, player, new BlockPos(1, 2, 2));
        require(recorded.equals(itemInterface.target()) && player.getUUID().equals(itemInterface.owner()),
                "The synchronizer did not apply its target and owner to the interface");
        IItemHandler itemHandler = helper.getLevel().getCapability(
                Capabilities.ItemHandler.BLOCK, itemInterface.getBlockPos(), Direction.WEST);
        require(itemHandler != null && itemHandler.getSlots() == 27,
                "The item interface did not proxy the target ItemHandler");
        require(itemHandler.insertItem(0, new ItemStack(Items.DIAMOND, 7), false).isEmpty(),
                "The target ItemHandler rejected a valid insertion");
        ChestBlockEntity targetChest = helper.getBlockEntity(targetPosition);
        ChestBlockEntity otherChest = helper.getBlockEntity(otherPosition);
        require(targetChest.getItem(0).getCount() == 7 && otherChest.getItem(0).isEmpty(),
                "The item interface touched a block other than its synchronized target");

        CompoundTag saved = itemInterface.saveWithFullMetadata(helper.getLevel().registryAccess());
        InterfaceBlockEntity loaded = new InterfaceBlockEntity(itemInterface.getBlockPos(), itemInterface.getBlockState());
        loaded.loadWithComponents(saved, helper.getLevel().registryAccess());
        require(loaded.target() != null
                        && loaded.target().dimension().equals(helper.getLevel().dimension())
                        && loaded.target().pos().equals(helper.absolutePos(targetPosition)),
                "The synchronized dimension and position did not survive save and load");

        helper.setBlock(targetPosition, Blocks.AIR);
        require(itemHandler.getSlots() == 0
                        && itemHandler.insertItem(0, new ItemStack(Items.CLAY_BALL), false).getCount() == 1,
                "A removed synchronized target remained accessible through a stale capability");
        BlockPos unloaded = new BlockPos(16_000, 64, 16_000);
        require(!helper.getLevel().hasChunk(unloaded.getX() >> 4, unloaded.getZ() >> 4),
                "The unloaded target chunk was unexpectedly loaded");
        itemInterface.synchronize(new SynchronizedTarget(helper.getLevel().dimension(), unloaded), null);
        require(itemHandler.getSlots() == 0, "An unloaded target was accessed or force-loaded");
        require(!helper.getLevel().hasChunk(unloaded.getX() >> 4, unloaded.getZ() >> 4),
                "An interface access loaded the target chunk");
        itemInterface.loadWithComponents(saved, helper.getLevel().registryAccess());
        helper.setBlock(targetPosition, Blocks.CHEST);
        require(itemHandler.getSlots() == 27, "The restored interface did not reconnect to a reloaded target");

        BlockPos machinePosition = new BlockPos(5, 2, 2);
        helper.setBlock(machinePosition, MachineRegistration.MACHINES.values().stream()
                .filter(definition -> definition.series() == ProcessingSeries.GRINDER && definition.tier() == 5)
                .findFirst().orElseThrow().block().get());
        ClayMachineBlockEntity machine = helper.getBlockEntity(machinePosition);
        BlockPos otherMachinePosition = new BlockPos(5, 2, 4);
        helper.setBlock(otherMachinePosition, machine.getBlockState());
        ClayMachineBlockEntity otherMachine = helper.getBlockEntity(otherMachinePosition);
        machine.inventory().setStackInSlot(0, new ItemStack(Items.IRON_INGOT));
        machine.receiveClayEnergy(ClayEnergy.ofInternal(100_000L));
        machine.tickServer(helper.getLevel().random);
        long progressBeforeRelay = machine.progress();
        require(machine.activeRecipe() != null, "The target machine did not start its recipe");

        InterfaceBlockEntity redstoneInterface = place(helper, InterfaceType.REDSTONE, 5, new BlockPos(4, 2, 2));
        redstoneInterface.synchronize(SynchronizedTarget.at(helper.getLevel(), helper.absolutePos(machinePosition)), null);
        helper.setBlock(new BlockPos(4, 2, 1), Blocks.REDSTONE_BLOCK);
        InterfaceBlockEntity.serverTick(
                helper.getLevel(), redstoneInterface.getBlockPos(), redstoneInterface.getBlockState(), redstoneInterface);
        machine.tickServer(helper.getLevel().random);
        require(machine.isRemoteRedstonePowered() && machine.progress() == progressBeforeRelay
                        && !otherMachine.isRemoteRedstonePowered(),
                "Redstone control did not stop only the synchronized machine");
        helper.setBlock(new BlockPos(4, 2, 1), Blocks.AIR);
        InterfaceBlockEntity.serverTick(
                helper.getLevel(), redstoneInterface.getBlockPos(), redstoneInterface.getBlockState(), redstoneInterface);
        require(!machine.isRemoteRedstonePowered(), "Redstone control remained after the signal ended");

        BlockPos laserTarget = new BlockPos(7, 2, 2);
        BlockPos otherLaserTarget = new BlockPos(7, 2, 4);
        helper.setBlock(laserTarget, Blocks.CHEST);
        helper.setBlock(otherLaserTarget, Blocks.CHEST);
        long[] received = {0L, 0L, 0L};
        long[] otherReceived = {0L, 0L, 0L};
        LASER_RECEIVERS.put(helper.absolutePos(laserTarget), (blue, green, red) -> {
            received[0] += blue;
            received[1] += green;
            received[2] += red;
            return true;
        });
        LASER_RECEIVERS.put(helper.absolutePos(otherLaserTarget), (blue, green, red) -> {
            otherReceived[0] += blue;
            return true;
        });
        helper.getLevel().invalidateCapabilities(helper.absolutePos(laserTarget));
        InterfaceBlockEntity laserInterface = place(helper, InterfaceType.LASER, 7, new BlockPos(6, 2, 2));
        laserInterface.synchronize(SynchronizedTarget.at(helper.getLevel(), helper.absolutePos(laserTarget)), null);
        require(laserInterface.receiveLaser(3L, 5L, 7L)
                        && received[0] == 3L && received[1] == 5L && received[2] == 7L
                        && otherReceived[0] == 0L,
                "Laser components were not relayed only to the synchronized receiver");
        helper.setBlock(laserTarget, Blocks.AIR);
        require(!laserInterface.receiveLaser(1L, 1L, 1L),
                "A removed laser receiver remained accessible");

        recorded = SynchronizedTarget.at(helper.getLevel(), helper.absolutePos(machinePosition));
        synchronizer.set(InterfaceRegistration.SYNCHRONIZED_TARGET.get(), recorded);
        require(recorded.equals(synchronizer.get(InterfaceRegistration.SYNCHRONIZED_TARGET.get())),
                "The synchronizer did not retain its target data component");
        helper.succeed();
    }

    private static InterfaceBlockEntity place(
            GameTestHelper helper, InterfaceType type, int tier, BlockPos relativePosition) {
        var definition = InterfaceRegistration.INTERFACES.values().stream()
                .filter(value -> value.type() == type && value.tier() == tier)
                .findFirst().orElseThrow();
        helper.setBlock(relativePosition, definition.block().get());
        return helper.getBlockEntity(relativePosition);
    }

    private static void useSynchronizer(GameTestHelper helper, Player player, BlockPos relativePosition) {
        BlockPos position = helper.absolutePos(relativePosition);
        player.setPos(Vec3.atCenterOf(position));
        BlockHitResult hit = new BlockHitResult(
                Vec3.atCenterOf(position), Direction.NORTH, position, false);
        UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND, hit);
        require(player.getMainHandItem().onItemUseFirst(context).consumesAction(),
                "The synchronizer did not handle the first-use interaction");
    }

    private static void assertTiers(InterfaceType type, Set<Integer> expected) {
        Set<Integer> actual = InterfaceRegistration.INTERFACES.values().stream()
                .filter(definition -> definition.type() == type)
                .map(InterfaceRegistration.InterfaceDefinition::tier)
                .collect(java.util.stream.Collectors.toSet());
        require(actual.equals(expected), type + " registered the wrong tiers: " + actual);
        InterfaceRegistration.INTERFACES.values().stream()
                .filter(definition -> definition.type() == type)
                .forEach(definition -> require(
                        definition.block().get().tier() == definition.tier()
                                && new ItemStack(definition.item().get()).get(
                                        FoundationDataComponentRegistration.TIER.get()) == definition.tier(),
                        definition.id() + " has an incorrect block or item tier"));
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private InterfaceGameTests() {
    }
}
