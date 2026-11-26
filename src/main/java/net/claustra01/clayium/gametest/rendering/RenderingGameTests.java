package net.claustra01.clayium.gametest.rendering;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.machine.crucible.QuartzCrucibleBlockEntity;
import net.claustra01.clayium.machine.iface.InterfaceBlock;
import net.claustra01.clayium.machine.iface.InterfaceBlockEntity;
import net.claustra01.clayium.machine.iface.SynchronizedTarget;
import net.claustra01.clayium.pan.PanCableBlock;
import net.claustra01.clayium.registry.EnvironmentMachineRegistration;
import net.claustra01.clayium.registry.InterfaceRegistration;
import net.claustra01.clayium.registry.PanRegistration;
import net.claustra01.clayium.registry.StorageRegistration;
import net.claustra01.clayium.registry.AreaMachineRegistration;
import net.claustra01.clayium.registry.CaReactorRegistration;
import net.claustra01.clayium.multiblock.careactor.CaReactorCoreBlockEntity;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.claustra01.clayium.world.area.AreaMachineBlockEntity;
import net.claustra01.clayium.world.area.AreaMachineType;
import java.util.UUID;
import net.claustra01.clayium.world.area.AreaMachineBlock;
import net.claustra01.clayium.storage.MetalChestBlockEntity;
import net.claustra01.clayium.storage.StorageContainerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RenderingGameTests {
    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void panCableConnectionsRejectStoneAndRebuild(GameTestHelper helper) {
        BlockPos cablePos = new BlockPos(4, 4, 4);
        helper.setBlock(cablePos, PanRegistration.PAN_CABLE.get());
        for (Direction direction : Direction.values()) {
            helper.setBlock(cablePos.relative(direction), PanRegistration.PAN_CORE.get());
        }
        BlockState connected = helper.getBlockState(cablePos);
        for (Direction direction : Direction.values()) {
            require(connected.getValue(PanCableBlock.connectionProperty(direction)),
                    "PAN cable omitted its " + direction.getName() + " arm");
        }

        helper.setBlock(cablePos.east(), Blocks.AIR);
        BlockState disconnected = helper.getBlockState(cablePos);
        require(!disconnected.getValue(PanCableBlock.EAST), "Removed PAN neighbor left a stale east arm");
        require(disconnected.getValue(PanCableBlock.WEST), "Unrelated west arm was removed");

        helper.setBlock(cablePos.east(), Blocks.STONE);
        require(!helper.getBlockState(cablePos).getValue(PanCableBlock.EAST),
                "PAN cable connected to a non-PAN neighbor");
        helper.setBlock(cablePos, Blocks.AIR);
        helper.setBlock(cablePos, PanRegistration.PAN_CABLE.get());
        for (Direction direction : Direction.values()) {
            helper.setBlock(cablePos.relative(direction), Blocks.AIR);
            helper.setBlock(cablePos.relative(direction),
                    direction == Direction.EAST ? Blocks.STONE : PanRegistration.PAN_CORE.get());
        }
        BlockState rebuilt = helper.getBlockState(cablePos);
        for (Direction direction : Direction.values()) {
            require(rebuilt.getValue(PanCableBlock.connectionProperty(direction))
                            == disconnected.getValue(PanCableBlock.connectionProperty(direction)),
                    "PAN connection state was not rebuilt from neighbors");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void rendererDataSurvivesSaveReloadAndUpdateTags(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        BlockPos cruciblePos = new BlockPos(2, 2, 2);
        helper.setBlock(cruciblePos, EnvironmentMachineRegistration.QUARTZ_CRUCIBLE.get());
        QuartzCrucibleBlockEntity crucible = helper.getBlockEntity(cruciblePos);
        require(crucible.insertImpureSilicon(5) == 5, "Crucible setup rejected its visible contents");
        QuartzCrucibleBlockEntity crucibleDisplay = new QuartzCrucibleBlockEntity(
                crucible.getBlockPos(), crucible.getBlockState());
        crucibleDisplay.loadWithComponents(crucible.saveWithFullMetadata(registries), registries);
        require(crucibleDisplay.impureSilicon() == 5, "Crucible save omitted contents");
        crucibleDisplay.loadWithComponents(crucible.getUpdateTag(registries), registries);
        require(crucibleDisplay.impureSilicon() == 5
                        && crucibleDisplay.heatingTicks() == 5 * QuartzCrucibleBlockEntity.HEATING_TICKS_PER_INGOT,
                "Crucible update tag omitted content height or heating stage data");

        BlockPos storagePos = new BlockPos(4, 2, 2);
        helper.setBlock(storagePos, StorageRegistration.STORAGE_CONTAINER.get());
        StorageContainerBlockEntity storage = helper.getBlockEntity(storagePos);
        storage.itemHandler().insertItem(0, new ItemStack(Items.CLAY_BALL, 64), false);
        storage.itemHandler().insertItem(0, new ItemStack(Items.CLAY_BALL, 64), false);
        StorageContainerBlockEntity storageDisplay = new StorageContainerBlockEntity(
                storage.getBlockPos(), storage.getBlockState());
        storageDisplay.loadWithComponents(storage.saveWithFullMetadata(registries), registries);
        require(storageDisplay.amount() == 128L, "Storage save omitted count");
        storageDisplay.loadWithComponents(storage.getUpdateTag(registries), registries);
        require(storageDisplay.amount() == 128L && storageDisplay.storedItem().is(Items.CLAY_BALL),
                "Storage container update tag omitted its displayed item or long count");

        BlockPos chestPos = new BlockPos(6, 2, 2);
        helper.setBlock(chestPos, StorageRegistration.METAL_CHESTS.get("silicon_chest").block().get());
        MetalChestBlockEntity chest = helper.getBlockEntity(chestPos);
        chest.inventory().setStackInSlot(0, new ItemStack(Items.CLAY_BALL, 3));
        MetalChestBlockEntity chestReloaded = new MetalChestBlockEntity(chest.getBlockPos(), chest.getBlockState());
        chestReloaded.loadWithComponents(chest.saveWithFullMetadata(registries), registries);
        require(chestReloaded.inventory().getStackInSlot(0).getCount() == 3,
                "Metal chest save lost the contents used by its menu");
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        chest.startOpen(player);
        chest.triggerEvent(1, 1);
        for (int tick = 0; tick < 5; tick++) {
            MetalChestBlockEntity.lidAnimateTick(helper.getLevel(), chest.getBlockPos(), chest.getBlockState(), chest);
        }
        require(chest.openness(1.0F) > 0.0F, "Metal chest open event did not animate the lid");
        chest.stopOpen(player);
        chest.triggerEvent(1, 0);
        for (int tick = 0; tick < 10; tick++) {
            MetalChestBlockEntity.lidAnimateTick(helper.getLevel(), chest.getBlockPos(), chest.getBlockState(), chest);
        }
        require(chest.openness(1.0F) == 0.0F, "Metal chest close event left the lid visible as open");
        BlockPos areaPos = new BlockPos(8, 2, 2);
        helper.setBlock(areaPos, AreaMachineRegistration.MACHINES.get(AreaMachineType.MINER).block().get());
        AreaMachineBlockEntity area = helper.getBlockEntity(areaPos);
        area.setOwner(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        var areaSaved = area.saveWithFullMetadata(registries);
        areaSaved.getCompound("energy").putLong("clay_energy", 1_000_000L);
        area.loadWithComponents(areaSaved, registries);
        AreaMachineBlockEntity.serverTick(helper.getLevel(), area.getBlockPos(), area.getBlockState(), area);
        require(helper.getBlockState(areaPos).getValue(AreaMachineBlock.ACTIVE),
                "Fueled area miner did not publish its running BlockState");

        BlockPos corePos = new BlockPos(10, 2, 2);
        helper.setBlock(corePos, CaReactorRegistration.CORES.get(10).block().get());
        CaReactorCoreBlockEntity core = helper.getBlockEntity(corePos);
        var coreUpdate = core.getUpdateTag(registries);
        coreUpdate.putString("state", "RUNNING");
        CaReactorCoreBlockEntity coreDisplay = new CaReactorCoreBlockEntity(core.getBlockPos(), core.getBlockState());
        coreDisplay.loadWithComponents(coreUpdate, registries);
        require(coreDisplay.state() == ClayMachineBlockEntity.MachineState.RUNNING,
                "CA core update tag did not restore renderer running state");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void visualConnectionStatesAreServerAuthoritative(GameTestHelper helper) {
        BlockPos targetPos = new BlockPos(5, 2, 5);
        BlockPos interfacePos = new BlockPos(4, 2, 5);
        helper.setBlock(targetPos, Blocks.CHEST);
        var definition = InterfaceRegistration.INTERFACES.get("clay_interface_tier_5");
        helper.setBlock(interfacePos, definition.block().get());
        InterfaceBlockEntity blockEntity = helper.getBlockEntity(interfacePos);
        blockEntity.synchronize(new SynchronizedTarget(
                helper.getLevel().dimension(), helper.absolutePos(targetPos)), null);
        require(helper.getBlockState(interfacePos).getValue(InterfaceBlock.LINKED),
                "Synchronized interface did not expose its linked face state");
        require(!helper.getBlockState(interfacePos).getValue(InterfaceBlock.DISCONNECTED),
                "Connected interface exposed the disconnected face state");
        helper.setBlock(targetPos, Blocks.AIR);
        InterfaceBlockEntity.serverTick(
                helper.getLevel(), blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity);
        require(helper.getBlockState(interfacePos).getValue(InterfaceBlock.LINKED),
                "Interface forgot its synchronization when its target disappeared");
        require(helper.getBlockState(interfacePos).getValue(InterfaceBlock.DISCONNECTED),
                "Interface did not expose its disconnected face state after target removal");
        blockEntity.disconnect();
        require(!helper.getBlockState(interfacePos).getValue(InterfaceBlock.LINKED),
                "Disconnected interface retained its linked face state");
        require(!helper.getBlockState(interfacePos).getValue(InterfaceBlock.DISCONNECTED),
                "Unlinked interface retained its disconnected face state");
        helper.setBlock(targetPos, Blocks.CHEST);
        blockEntity.synchronize(new SynchronizedTarget(
                helper.getLevel().dimension(), helper.absolutePos(targetPos)), null);
        helper.setBlock(interfacePos, Blocks.AIR);
        require(helper.getBlockState(interfacePos).isAir()
                        && helper.getLevel().getBlockEntity(helper.absolutePos(interfacePos)) == null,
                "Removing a linked interface restored a block without its block entity");
        helper.succeed();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private RenderingGameTests() {
    }
}
