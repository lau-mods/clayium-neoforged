package net.claustra01.clayium.gametest.creative;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.energy.ClayEnergyStorage;
import net.claustra01.clayium.logistics.BufferBlockEntity;
import net.claustra01.clayium.logistics.creative.CreativeEnergySourceBlockEntity;
import net.claustra01.clayium.material.CompressionTier;
import net.claustra01.clayium.registry.CreativeSourceRegistration;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CreativeEnergySourceGameTests {
    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void replenishesSixtyFourFuelAndTransfersEveryTick(GameTestHelper helper) {
        BlockPos sourcePosition = new BlockPos(1, 1, 1);
        helper.setBlock(sourcePosition, CreativeSourceRegistration.CREATIVE_ENERGY_SOURCE.get());
        CreativeEnergySourceBlockEntity source = helper.getBlockEntity(sourcePosition);

        BlockPos inputPosition = sourcePosition.relative(Direction.WEST);
        helper.setBlock(inputPosition, LogisticsRegistration.BUFFERS.get(13).block().get());
        BufferBlockEntity input = helper.getBlockEntity(inputPosition);
        setOnlyFace(input, Direction.EAST, BufferBlockEntity.FaceMode.DISABLED);
        input.inventory().setStackInSlot(0, new ItemStack(Items.DIRT, 64));
        ItemStack offered = new ItemStack(Items.DIRT);
        require(ItemStack.matches(source.itemHandler(Direction.WEST).insertItem(0, offered, false), offered),
                "The creative source accepted a foreign item through its input capability");

        helper.runAfterDelay(2, () -> {
            require(fuelCount(source) == 64, "The creative source did not replenish 64 fuel items");
            require(input.inventory().getStackInSlot(0).getCount() == 64,
                    "The creative source pulled a foreign item from an input face");
            source.inventory().extractItem(0, 19, false);
        });
        helper.runAfterDelay(3, () -> {
            require(fuelCount(source) == 64, "The creative source did not replenish the missing fuel items");
            BlockPos targetPosition = sourcePosition.relative(Direction.EAST);
            helper.setBlock(targetPosition, LogisticsRegistration.BUFFERS.get(13).block().get());
            BufferBlockEntity target = helper.getBlockEntity(targetPosition);
            setOnlyFace(source, Direction.EAST, BufferBlockEntity.FaceMode.OUTPUT);
            setOnlyFace(target, Direction.WEST, BufferBlockEntity.FaceMode.INPUT);
        });
        for (int tick = 4; tick <= 6; tick++) {
            int checkTick = tick;
            helper.runAfterDelay(checkTick, () -> {
                BufferBlockEntity target = helper.getBlockEntity(sourcePosition.relative(Direction.EAST));
                require(fuelCount(target) == 64,
                        "The source did not export 64 fuel items on tick " + checkTick);
                clear(target);
                if (checkTick == 6) {
                    helper.succeed();
                }
            });
        }
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void suppliedItemsAreCeFuelWithoutFeOrSurvivalRecipe(GameTestHelper helper) {
        BlockPos position = new BlockPos(1, 1, 1);
        helper.setBlock(position, CreativeSourceRegistration.CREATIVE_ENERGY_SOURCE.get());
        CreativeEnergySourceBlockEntity source = helper.getBlockEntity(position);
        setOnlyFace(source, Direction.EAST, BufferBlockEntity.FaceMode.DISABLED);
        helper.runAfterDelay(2, () -> {
            ItemStack fuel = source.inventory().extractItem(0, 1, false);
            CompressionTier fuelTier = CompressionTier.OCTUPLE_COMPRESSED_ENERGETIC_CLAY;
            require(fuel.is(MaterialRegistration.COMPRESSION_BLOCKS.get(fuelTier.id()).get().asItem()),
                    "The source supplied a different item instead of octuple energetic clay");
            ClayEnergyStorage storage = new ClayEnergyStorage();
            require(storage.consume(ClayEnergy.ofInternal(1L), fuel),
                    "The supplied item was not accepted by normal CE fuel handling");
            require(fuel.isEmpty()
                            && storage.stored().internalValue()
                                    == ClayEnergy.ofCe(fuelTier.clayEnergy()).internalValue() - 1L,
                    "The supplied fuel had the wrong CE value");

            for (Direction direction : Direction.values()) {
                require(helper.getLevel().getCapability(
                                Capabilities.EnergyStorage.BLOCK, source.getBlockPos(), direction) == null,
                        "The creative source exposed FE on " + direction.getName());
            }
            require(helper.getLevel().getCapability(
                            Capabilities.EnergyStorage.BLOCK, source.getBlockPos(), null) == null,
                    "The creative source exposed unsided FE");

            Item sourceItem = CreativeSourceRegistration.CREATIVE_ENERGY_SOURCE_ITEM.get();
            require(helper.getLevel().getRecipeManager().getRecipes().stream().noneMatch(holder ->
                            holder.value().getResultItem(helper.getLevel().registryAccess()).is(sourceItem)),
                    "A survival recipe produces the creative-only energy source");
            helper.succeed();
        });
    }

    private static int fuelCount(BufferBlockEntity buffer) {
        Item fuel = MaterialRegistration.COMPRESSION_BLOCKS
                .get(CompressionTier.OCTUPLE_COMPRESSED_ENERGETIC_CLAY.id()).get().asItem();
        int count = 0;
        for (int slot = 0; slot < buffer.inventory().getSlots(); slot++) {
            if (buffer.inventory().getStackInSlot(slot).is(fuel)) {
                count += buffer.inventory().getStackInSlot(slot).getCount();
            }
        }
        return count;
    }

    private static void clear(BufferBlockEntity buffer) {
        for (int slot = 0; slot < buffer.inventory().getSlots(); slot++) {
            buffer.inventory().setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    private static void setOnlyFace(
            BufferBlockEntity buffer, Direction selected, BufferBlockEntity.FaceMode selectedMode) {
        for (Direction direction : Direction.values()) {
            buffer.setFaceMode(direction,
                    direction == selected ? selectedMode : BufferBlockEntity.FaceMode.DISABLED);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private CreativeEnergySourceGameTests() {
    }
}
