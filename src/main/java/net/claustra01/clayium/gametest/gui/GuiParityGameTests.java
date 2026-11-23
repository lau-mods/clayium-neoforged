package net.claustra01.clayium.gametest.gui;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.claustra01.clayium.machine.ClayMachineMenu;
import net.claustra01.clayium.logistics.BufferBlockEntity;
import net.claustra01.clayium.logistics.BufferMenu;
import net.claustra01.clayium.logistics.filter.FilterMenu;
import net.claustra01.clayium.logistics.multitrack.MultitrackBufferBlockEntity;
import net.claustra01.clayium.logistics.multitrack.MultitrackBufferMenu;
import net.claustra01.clayium.logistics.distributor.DistributorBlockEntity;
import net.claustra01.clayium.logistics.distributor.DistributorMenu;
import net.claustra01.clayium.fluid.FluidTransportBlockEntity;
import net.claustra01.clayium.fluid.FluidTransportMenu;
import net.claustra01.clayium.machine.iface.InterfaceBlockEntity;
import net.claustra01.clayium.machine.iface.InterfaceMenu;
import net.claustra01.clayium.machine.clayproduction.ClayProductionBlockEntity;
import net.claustra01.clayium.machine.clayproduction.ClayProductionMenu;
import net.claustra01.clayium.machine.clayproduction.ClayProductionType;
import net.claustra01.clayium.energy.fe.EnergyConverterBlockEntity;
import net.claustra01.clayium.energy.fe.EnergyConverterMenu;
import net.claustra01.clayium.gadget.GadgetHolderMenu;
import net.claustra01.clayium.gadget.GadgetType;
import net.claustra01.clayium.machine.autocrafter.AutoCrafterBlockEntity;
import net.claustra01.clayium.machine.autocrafter.AutoCrafterMenu;
import net.claustra01.clayium.machine.waterwheel.WaterWheelBlockEntity;
import net.claustra01.clayium.machine.waterwheel.WaterWheelMenu;
import net.claustra01.clayium.machine.trade.AutoTraderBlockEntity;
import net.claustra01.clayium.machine.trade.AutoTraderMenu;
import net.claustra01.clayium.pan.PanBlockEntity;
import net.claustra01.clayium.pan.PanMenu;
import net.claustra01.clayium.pan.PanScreenPayloads;
import net.claustra01.clayium.registry.AreaMachineRegistration;
import net.claustra01.clayium.registry.AutoCrafterRegistration;
import net.claustra01.clayium.registry.EnergyConverterRegistration;
import net.claustra01.clayium.registry.GadgetRegistration;
import net.claustra01.clayium.registry.MachineRegistration;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.claustra01.clayium.registry.MultitrackRegistration;
import net.claustra01.clayium.registry.FluidRegistration;
import net.claustra01.clayium.registry.InterfaceRegistration;
import net.claustra01.clayium.registry.ClayProductionRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.PanRegistration;
import net.claustra01.clayium.registry.StorageRegistration;
import net.claustra01.clayium.registry.TradeRegistration;
import net.claustra01.clayium.registry.WaterWheelRegistration;
import net.claustra01.clayium.registry.WorkbenchRegistration;
import net.claustra01.clayium.storage.MetalChestBlockEntity;
import net.claustra01.clayium.storage.MetalChestMenu;
import net.claustra01.clayium.storage.StorageContainerBlockEntity;
import net.claustra01.clayium.storage.StorageContainerMenu;
import net.claustra01.clayium.storage.VoidContainerBlockEntity;
import net.claustra01.clayium.storage.VoidContainerMenu;
import net.claustra01.clayium.workbench.ClayWorkTableBlockEntity;
import net.claustra01.clayium.workbench.ClayWorkTableMenu;
import net.claustra01.clayium.workbench.ClayCraftingBoardBlockEntity;
import net.claustra01.clayium.workbench.ClayCraftingBoardMenu;
import net.claustra01.clayium.storage.MetalChestType;
import net.claustra01.clayium.world.area.AreaMachineBlockEntity;
import net.claustra01.clayium.world.area.AreaMachineMenu;
import net.claustra01.clayium.world.area.AreaMachineType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class GuiParityGameTests {
    private static final Map<String, String> ORIGINAL_GUI_HASHES = Map.ofEntries(
            Map.entry("autotrader.png", "721ecc027452a647d6f364a4b4a3fe3dc5b57307ed5855611fe9ef4895fe37a4"),
            Map.entry("back.png", "91bbae30893ada1943d641076850551ebc4087edcb6151bbef58860f15434622"),
            Map.entry("button.png", "04f523984da899df14f50122e4f00a35c08a4452d35da07668c016c1773eac1f"),
            Map.entry("claycraftingtable.png", "d409addcb1ee0be7ee13eebcb694525cb05df93d06479c8587921005dc6e8da0"),
            Map.entry("clayworktable.png", "60f7cdf4e09a8e3694416e87aef403fc899c729852df0355cce216009d7073b9"),
            Map.entry("gui_b.png", "40bd6db030fe696c65aacbf98684b3f33992f413b9d1d88db52936f4161cbdc7"),
            Map.entry("gui_back.png", "dd60803bc4754f70555d25c7c7e6e929292eee961412620cdd2be2c4579ec2a5"),
            Map.entry("gui_bl.png", "22c927b57b96527a5ac3d5fc488bff54311eecbbbc4ef20bb9d5bf50df5a18ba"),
            Map.entry("gui_br.png", "ab807bbf956b5ddff97b26ecb7f1dc67342956cb975e8b116946a2ae974e6106"),
            Map.entry("gui_l.png", "aff58cc107ad8214303a7a152fac7bc568912f5db3051c3baa4e70dac7202ffa"),
            Map.entry("gui_playerinventory.png", "1e2c4a4404705b880fb0b41054bc8fac14e017cf7fc3dc77d6564d18237e1960"),
            Map.entry("gui_r.png", "c98324ea07350e046653a2599ad7dc4227b783a16db0c06ed02827c8a93830d1"),
            Map.entry("gui_t.png", "74155576efc733a413eda108df409429769c7d39ef739e667396e0f2a4f86f72"),
            Map.entry("gui_tl.png", "53597b31a7a1f078d4c3882f56b876c64472d0c050cdc4ea600d07b069d39e3a"),
            Map.entry("gui_tr.png", "e43710999f4b4429b47af08b10687a2183d35e19d115f9b8f8be32b68f8913ad"),
            Map.entry("nei.png", "06d062e06f9845506e6ba3b8e1bb6da22ef94b4150941976f737f076911f67fd"),
            Map.entry("playerinventory.png", "e304c66e46684aed3c8139334dabd73723c46577cf74fc7730d1fd8409fe71fc"),
            Map.entry("progressbarfurnace.png", "b8f1a60c4aa87ef1ccb0218af9deadda64ec4c83c221783762da2532412b980e"),
            Map.entry("slot.png", "ddbc0bda245fe1895b68eeb181e78b0f21d9653541ea4adad05fcd9960a15ef4"));

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void legacyMenusExposeOriginalSlotRelationships(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.CREATIVE);

        BlockPos machinePos = new BlockPos(2, 2, 2);
        helper.setBlock(machinePos, MachineRegistration.MACHINES.get("grinder_tier_4").block().get());
        ClayMachineBlockEntity machine = helper.getBlockEntity(machinePos);
        ClayMachineMenu machineMenu = new ClayMachineMenu(1, player.getInventory(), machine,
                new net.minecraft.world.inventory.SimpleContainerData(22));
        slot(machineMenu.slots.get(0), 44, 35, "processing input");
        slot(machineMenu.slots.get(1), 116, 35, "processing output");
        slot(machineMenu.slots.get(3), 44, 57, "processing CE fuel");
        slot(machineMenu.slots.get(4), 8, 90, "processing player inventory");
        player.getInventory().setItem(9, new ItemStack(Items.COBBLESTONE));
        require(!machineMenu.quickMoveStack(player, 4).isEmpty()
                        && machine.inventory().getStackInSlot(0).is(Items.COBBLESTONE)
                        && player.getInventory().getItem(9).isEmpty(),
                "Processing shift transfer did not move exactly one input stack");

        BlockPos duplicatorPos = new BlockPos(4, 2, 2);
        helper.setBlock(duplicatorPos, PanRegistration.DEFINITIONS.get("pan_duplicator_tier_10").block().get());
        PanBlockEntity duplicator = helper.getBlockEntity(duplicatorPos);
        PanMenu duplicatorMenu = new PanMenu(2, player.getInventory(), duplicator,
                new net.minecraft.world.inventory.SimpleContainerData(12));
        slot(duplicatorMenu.slots.get(0), 35, 35, "PAN sample");
        slot(duplicatorMenu.slots.get(1), 35, 57, "PAN antimatter");
        slot(duplicatorMenu.slots.get(2), 125, 35, "PAN output");
        slot(duplicatorMenu.slots.get(3), 35, 79, "PAN fuel");
        slot(duplicatorMenu.slots.get(4), 8, 119, "PAN player inventory");
        player.getInventory().setItem(9, new ItemStack(Items.COBBLESTONE));
        require(!duplicatorMenu.quickMoveStack(player, 4).isEmpty()
                        && duplicator.inventory().getStackInSlot(PanBlockEntity.SAMPLE_SLOT).is(Items.COBBLESTONE)
                        && player.getInventory().getItem(9).isEmpty(),
                "PAN shift transfer did not reach the sample slot");

        BlockPos traderPos = new BlockPos(6, 2, 2);
        helper.setBlock(traderPos, TradeRegistration.AUTO_TRADER.get());
        AutoTraderBlockEntity trader = helper.getBlockEntity(traderPos);
        AutoTraderMenu traderMenu = new AutoTraderMenu(3, player.getInventory(), trader);
        slot(traderMenu.slots.get(0), 36, 53, "trade input A");
        slot(traderMenu.slots.get(1), 62, 53, "trade input B");
        slot(traderMenu.slots.get(2), 120, 53, "trade output");
        slot(traderMenu.slots.get(3), 8, 53, "trade fuel");
        slot(traderMenu.slots.get(4), 8, 116, "trade player inventory");

        BlockPos areaPos = new BlockPos(8, 2, 2);
        helper.setBlock(areaPos, AreaMachineRegistration.MACHINES.get(AreaMachineType.ACTIVATOR).block().get());
        AreaMachineBlockEntity area = helper.getBlockEntity(areaPos);
        AreaMachineMenu areaMenu = new AreaMachineMenu(4, player.getInventory(), area);
        slot(areaMenu.slots.get(0), 8, 35, "area operation");
        slot(areaMenu.slots.get(1), 8, 58, "area harvest filter");
        slot(areaMenu.slots.get(2), 26, 58, "area fortune filter");
        slot(areaMenu.slots.get(3), 44, 58, "area silk filter");
        slot(areaMenu.slots.get(AreaMachineBlockEntity.FUEL_SLOT + 1), 8, 133, "area player inventory");

        MetalChestType europium = MetalChestType.ALL.stream()
                .filter(type -> type.materialId().equals("europium")).findFirst().orElseThrow();
        var definition = StorageRegistration.METAL_CHESTS.get(europium.id());
        MetalChestBlockEntity chest = new MetalChestBlockEntity(helper.absolutePos(new BlockPos(1, 1, 1)),
                definition.block().get().defaultBlockState());
        MetalChestMenu chestMenu = new MetalChestMenu(5, player.getInventory(), chest);
        require(chestMenu.imageWidth() == 256 && chestMenu.imageHeight() == 236
                        && chestMenu.type().pages() == 8,
                "The 13-column original multipage window dimensions changed");
        slot(chestMenu.slots.get(0), 12, 6, "metal chest first slot");
        require(chestMenu.clickMenuButton(player, 7) && chestMenu.slots.get(728).isActive()
                        && !chestMenu.slots.get(0).isActive(),
                "The original multipage slot relationship is not server authoritative");
        player.getInventory().setItem(9, new ItemStack(Items.DIAMOND));
        require(!chestMenu.quickMoveStack(player, europium.slots()).isEmpty()
                        && chestMenu.slots.get(728).getItem().is(Items.DIAMOND)
                        && chestMenu.slots.get(0).getItem().isEmpty(),
                "Metal chest shift transfer left the selected page");
        BlockPos converterPos = new BlockPos(10, 2, 2);
        helper.setBlock(converterPos, EnergyConverterRegistration.CONVERTERS.get(4).block().get());
        EnergyConverterBlockEntity converter = helper.getBlockEntity(converterPos);
        EnergyConverterMenu converterMenu = new EnergyConverterMenu(6, player.getInventory(), converter,
                new net.minecraft.world.inventory.SimpleContainerData(12));
        slot(converterMenu.slots.get(0), 35, 53, "converter fuel");
        slot(converterMenu.slots.get(1), 8, 105, "converter player inventory");
        BlockPos topConverterPos = new BlockPos(11, 2, 2);
        helper.setBlock(topConverterPos, EnergyConverterRegistration.CONVERTERS.get(13).block().get());
        EnergyConverterBlockEntity topConverter = helper.getBlockEntity(topConverterPos);
        EnergyConverterMenu topConverterMenu = (EnergyConverterMenu)topConverter.createMenu(
                7, player.getInventory(), player);
        require(topConverterMenu.generatedFe() == 196_830 && topConverterMenu.outputLimit() == 196_830
                        && topConverterMenu.capacity() == 196_830_000,
                "High-tier FE values were truncated by menu synchronization");
        BlockPos wheelPos = new BlockPos(12, 2, 2);
        helper.setBlock(wheelPos, WaterWheelRegistration.CLAY.block().get());
        WaterWheelBlockEntity wheel = helper.getBlockEntity(wheelPos);
        WaterWheelMenu wheelMenu = new WaterWheelMenu(7, player.getInventory(), wheel,
                new net.minecraft.world.inventory.SimpleContainerData(3));
        require(wheelMenu.slots.size() == 36, "The original water wheel screen includes player inventory");
        slot(wheelMenu.slots.get(0), 8, 44, "water wheel player inventory");
        slot(wheelMenu.slots.get(27), 8, 102, "water wheel hotbar");
        BlockPos workTablePos = new BlockPos(14, 2, 2);
        helper.setBlock(workTablePos, WorkbenchRegistration.CLAY_WORK_TABLE.get());
        ClayWorkTableBlockEntity workTable = helper.getBlockEntity(workTablePos);
        ClayWorkTableMenu workMenu = new ClayWorkTableMenu(8, player.getInventory(), workTable);
        slot(workMenu.slots.get(0), 17, 30, "work table input");
        slot(workMenu.slots.get(1), 80, 17, "work table tool");
        slot(workMenu.slots.get(2), 143, 30, "work table first output");
        player.getInventory().setItem(9, new ItemStack(Items.CLAY_BALL));
        require(!workMenu.quickMoveStack(player, 4).isEmpty()
                        && workTable.inventory().getStackInSlot(ClayWorkTableBlockEntity.INPUT_SLOT).is(Items.CLAY_BALL)
                        && player.getInventory().getItem(9).isEmpty(),
                "Work table shift transfer did not reach its original input slot");
        BlockPos boardPos = new BlockPos(16, 2, 2);
        helper.setBlock(boardPos, WorkbenchRegistration.CLAY_CRAFTING_BOARD.get());
        ClayCraftingBoardBlockEntity board = helper.getBlockEntity(boardPos);
        ClayCraftingBoardMenu boardMenu = new ClayCraftingBoardMenu(9, player.getInventory(), board);
        slot(boardMenu.slots.get(0), 124, 35, "crafting board result");
        slot(boardMenu.slots.get(1), 30, 17, "crafting board pattern");
        int boardPlayerSlot = 10 + boardMenu.adjacentSlotCount();
        slot(boardMenu.slots.get(boardPlayerSlot), 8, 84, "standalone crafting board inventory");
        slot(boardMenu.slots.get(boardPlayerSlot + 27), 8, 142, "standalone crafting board hotbar");
        player.getInventory().setItem(9, new ItemStack(Items.COBBLESTONE));
        require(!boardMenu.quickMoveStack(player, boardPlayerSlot).isEmpty()
                        && boardMenu.slots.get(1).getItem().is(Items.COBBLESTONE)
                        && player.getInventory().getItem(9).isEmpty(),
                "Crafting board shift transfer missed its 3x3 pattern");
        player.getInventory().setItem(0, new ItemStack(GadgetRegistration.GADGET_HOLDER.get()));
        GadgetHolderMenu gadgetMenu = new GadgetHolderMenu(10, player.getInventory(), 0);
        slot(gadgetMenu.slots.get(0), 17, 29, "gadget holder first slot");
        slot(gadgetMenu.slots.get(16), 8, 91, "gadget holder player inventory");
        player.getInventory().setItem(9,
                new ItemStack(GadgetRegistration.GADGETS_BY_TYPE.get(GadgetType.FLIGHT).get()));
        require(!gadgetMenu.quickMoveStack(player, 16).isEmpty()
                        && gadgetMenu.slots.get(0).getItem().is(
                                GadgetRegistration.GADGETS_BY_TYPE.get(GadgetType.FLIGHT).get())
                        && player.getInventory().getItem(9).isEmpty(),
                "Gadget shift transfer did not reach the holder");
        BlockPos crafterPos = new BlockPos(18, 2, 2);
        helper.setBlock(crafterPos, AutoCrafterRegistration.AUTO_CRAFTERS.get(5).block().get());
        AutoCrafterBlockEntity crafter = helper.getBlockEntity(crafterPos);
        AutoCrafterMenu crafterMenu = new AutoCrafterMenu(11, player.getInventory(), crafter,
                new net.minecraft.world.inventory.SimpleContainerData(7));
        slot(crafterMenu.slots.get(0), 8, 34, "auto crafter first pattern slot");
        slot(crafterMenu.slots.get(9), 75, 52, "auto crafter result preview");
        int crafterPlayerSlot = crafterMenu.slots.size() - 36;
        player.getInventory().setItem(9, new ItemStack(Items.COBBLESTONE));
        require(!crafterMenu.quickMoveStack(player, crafterPlayerSlot).isEmpty()
                        && crafter.inventory().getStackInSlot(0).is(Items.COBBLESTONE)
                        && player.getInventory().getItem(9).isEmpty(),
                "Auto crafter shift transfer entered a ghost or output slot");
        BlockPos storagePos = new BlockPos(20, 2, 2);
        helper.setBlock(storagePos, StorageRegistration.STORAGE_CONTAINER.get());
        StorageContainerBlockEntity storage = helper.getBlockEntity(storagePos);
        StorageContainerMenu storageMenu = new StorageContainerMenu(12, player.getInventory(), storage,
                new net.minecraft.world.inventory.SimpleContainerData(9));
        slot(storageMenu.slots.get(0), 44, 35, "storage input");
        BlockPos voidPos = new BlockPos(22, 2, 2);
        helper.setBlock(voidPos, StorageRegistration.VOID_CONTAINER.get());
        VoidContainerBlockEntity voidContainer = helper.getBlockEntity(voidPos);
        VoidContainerMenu voidMenu = new VoidContainerMenu(13, player.getInventory(), voidContainer);
        slot(voidMenu.slots.get(0), 80, 35, "void input");
        BlockPos corePos = new BlockPos(24, 2, 2);
        helper.setBlock(corePos, PanRegistration.PAN_CORE.get());
        PanBlockEntity core = helper.getBlockEntity(corePos);
        PanMenu coreMenu = new PanMenu(14, player.getInventory(), core,
                new net.minecraft.world.inventory.SimpleContainerData(12));
        require(coreMenu.slots.size() == 36, "PAN core has no machine slots");
        slot(coreMenu.slots.get(0), 8, 156, "PAN core player inventory");
        slot(coreMenu.slots.get(27), 8, 214, "PAN core hotbar");
        BlockPos adapterPos = new BlockPos(25, 2, 2);
        helper.setBlock(adapterPos, PanRegistration.DEFINITIONS.get("pan_adapter_tier_10").block().get());
        PanBlockEntity adapter = helper.getBlockEntity(adapterPos);
        PanMenu adapterMenu = new PanMenu(22, player.getInventory(), adapter,
                new net.minecraft.world.inventory.SimpleContainerData(12));
        require(adapterMenu.slots.size() == 36, "PAN adapter status has no machine inventory");
        slot(adapterMenu.slots.get(0), 8, 108, "PAN adapter player inventory");
        FilterMenu filterMenu = new FilterMenu(15, player.getInventory());
        slot(filterMenu.slots.get(0), 8, 77, "filter example");
        slot(filterMenu.slots.get(1), 8, 118, "filter player inventory");
        BlockPos bufferPos = new BlockPos(26, 2, 2);
        helper.setBlock(bufferPos, LogisticsRegistration.BUFFERS.get(9).block().get());
        BufferBlockEntity buffer = helper.getBlockEntity(bufferPos);
        BufferMenu bufferMenu = new BufferMenu(16, player.getInventory(), buffer,
                new net.minecraft.world.inventory.SimpleContainerData(12));
        slot(bufferMenu.slots.get(0), 29, 69, "buffer first storage slot");
        slot(bufferMenu.slots.get(100), 7, 83, "buffer filter slot");
        slot(bufferMenu.slots.get(102), 29, 141, "buffer player inventory");
        player.getInventory().setItem(9, new ItemStack(Items.CLAY_BALL));
        require(!bufferMenu.quickMoveStack(player, 102).isEmpty()
                        && buffer.exampleInventory().getStackInSlot(0).is(Items.CLAY_BALL)
                        && player.getInventory().getItem(9).isEmpty(),
                "Buffer shift transfer did not reach its example slot");
        BlockPos multitrackPos = new BlockPos(28, 2, 2);
        helper.setBlock(multitrackPos, MultitrackRegistration.MULTITRACK_BUFFERS.get(9).block().get());
        MultitrackBufferBlockEntity multitrack = helper.getBlockEntity(multitrackPos);
        MultitrackBufferMenu multitrackMenu = new MultitrackBufferMenu(17, player.getInventory(), multitrack);
        slot(multitrackMenu.slots.get(0), 62, 61, "multitrack first item");
        slot(multitrackMenu.slots.get(9), 126, 79, "multitrack filter");
        int multitrackPlayerSlot = multitrack.trackCount()
                * (MultitrackBufferBlockEntity.SLOTS_PER_TRACK + 1);
        player.getInventory().setItem(9, new ItemStack(Items.COBBLESTONE));
        require(!multitrackMenu.quickMoveStack(player, multitrackPlayerSlot).isEmpty()
                        && multitrack.track(0).inventory().getStackInSlot(0).is(Items.COBBLESTONE)
                        && player.getInventory().getItem(9).isEmpty(),
                "Multitrack shift transfer crossed the selected track boundary");
        BlockPos distributorPos = new BlockPos(30, 2, 2);
        helper.setBlock(distributorPos, MultitrackRegistration.DISTRIBUTORS.get(7).block().get());
        DistributorBlockEntity distributor = helper.getBlockEntity(distributorPos);
        DistributorMenu distributorMenu = new DistributorMenu(18, player.getInventory(), distributor,
                new net.minecraft.world.inventory.SimpleContainerData(1));
        slot(distributorMenu.slots.get(0), 62, 54, "distributor first slot");
        int distributorPlayerSlot = distributorMenu.slots.size() - 36;
        player.getInventory().setItem(9, new ItemStack(Items.COBBLESTONE));
        require(!distributorMenu.quickMoveStack(player, distributorPlayerSlot).isEmpty()
                        && distributorMenu.slots.get(0).getItem().is(Items.COBBLESTONE)
                        && player.getInventory().getItem(9).isEmpty(),
                "Distributor shift transfer did not reach its inventory");
        BlockPos fluidPos = new BlockPos(32, 2, 2);
        helper.setBlock(fluidPos, FluidRegistration.MACHINES.get("fluid_translator_tier_8").block().get());
        FluidTransportBlockEntity fluid = helper.getBlockEntity(fluidPos);
        FluidTransportMenu fluidMenu = new FluidTransportMenu(19, player.getInventory(), fluid,
                new net.minecraft.world.inventory.SimpleContainerData(19));
        slot(fluidMenu.slots.get(0), 152, 54, "fluid filter");
        slot(fluidMenu.slots.get(1), 8, 113, "fluid player inventory");
        player.getInventory().setItem(9, new ItemStack(Items.COBBLESTONE));
        require(fluidMenu.quickMoveStack(player, 1).isEmpty()
                        && fluidMenu.slots.get(0).getItem().isEmpty()
                        && player.getInventory().getItem(9).is(Items.COBBLESTONE),
                "Fluid transport accepted a non-filter item through shift transfer");
        BlockPos interfacePos = new BlockPos(34, 2, 2);
        helper.setBlock(interfacePos, InterfaceRegistration.INTERFACES.get("clay_interface_tier_5").block().get());
        InterfaceBlockEntity interfaceEntity = helper.getBlockEntity(interfacePos);
        InterfaceMenu interfaceMenu = new InterfaceMenu(20, player.getInventory(), interfaceEntity,
                new net.minecraft.world.inventory.SimpleContainerData(2));
        require(interfaceMenu.slots.isEmpty(), "Interface status window has no inventory slots");
        BlockPos productionPos = new BlockPos(36, 2, 2);
        helper.setBlock(productionPos, ClayProductionRegistration.BLOCKS.get(
                ClayProductionType.ENERGETIC_CLAY_CONDENSER_MK_I).get());
        ClayProductionBlockEntity production = helper.getBlockEntity(productionPos);
        ClayProductionMenu productionMenu = new ClayProductionMenu(21, player.getInventory(), production,
                new net.minecraft.world.inventory.SimpleContainerData(7));
        slot(productionMenu.slots.get(0), 44, 46, "clay production input");
        slot(productionMenu.slots.get(1), 116, 46, "clay production output");
        player.getInventory().setItem(9, new ItemStack(Items.CLAY));
        int productionPlayerSlot = productionMenu.slots.size() - 36;
        require(!productionMenu.quickMoveStack(player, productionPlayerSlot).isEmpty()
                        && production.inventory().getStackInSlot(ClayProductionBlockEntity.INPUT_SLOT).is(Items.CLAY)
                        && player.getInventory().getItem(9).isEmpty(),
                "Clay production shift transfer missed its input slot");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 100)
    public static void panViewStateAndProhibitionSurviveSave(GameTestHelper helper) {
        BlockPos corePos = new BlockPos(3, 3, 3);
        helper.setBlock(corePos, PanRegistration.PAN_CORE.get());
        PanBlockEntity core = helper.getBlockEntity(corePos);
        helper.runAfterDelay(20, () -> {
            require(!core.visibleCandidates().isEmpty(), "PAN core did not publish its base candidates");
            core.applyScreenControl(PanScreenPayloads.SORT, 2);
            core.applyScreenControl(PanScreenPayloads.REVERSE, 0);
            require(core.candidatePages() > 1, "PAN save test requires multiple pages");
            core.applyScreenControl(PanScreenPayloads.PAGE, 1);
            core.applyScreenControl(PanScreenPayloads.SELECT, 0);
            core.applyScreenControl(PanScreenPayloads.TOGGLE_PROHIBITED, 0);
            require(core.selectedCandidate() != null && core.isProhibited(core.selectedCandidate()),
                    "PAN prohibition did not apply to the server-selected candidate");
            CompoundTag saved = core.saveWithFullMetadata(helper.getLevel().registryAccess());
            PanBlockEntity loaded = new PanBlockEntity(core.getBlockPos(), core.getBlockState());
            loaded.loadWithComponents(saved, helper.getLevel().registryAccess());
            require(loaded.candidateSort() == 2 && loaded.candidateReverse() && loaded.candidatePage() == 1
                            && core.selectedCandidate().equals(loaded.selectedCandidate())
                            && loaded.isProhibited(loaded.selectedCandidate()),
                    "PAN sort, reverse, selection, or prohibition was lost across NBT");
        });
        helper.runAfterDelay(21, helper::succeed);
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 100)
    public static void prohibitedPanSampleStopsConnectedDuplicatorUntilPermitted(GameTestHelper helper) {
        BlockPos corePos = new BlockPos(3, 3, 3);
        BlockPos duplicatorPos = new BlockPos(4, 3, 3);
        helper.setBlock(corePos, PanRegistration.PAN_CORE.get());
        helper.setBlock(duplicatorPos, PanRegistration.DEFINITIONS.get("pan_duplicator_tier_10").block().get());
        PanBlockEntity core = helper.getBlockEntity(corePos);
        PanBlockEntity duplicator = helper.getBlockEntity(duplicatorPos);
        helper.runAfterDelay(20, () -> {
            require(!core.visibleCandidates().isEmpty(), "PAN closure did not produce a duplicator sample");
            core.applyScreenControl(PanScreenPayloads.SORT, 1);
            core.applyScreenControl(PanScreenPayloads.SELECT, 0);
            ItemStack sample = new ItemStack(BuiltInRegistries.ITEM.get(core.selectedCandidate()));
            duplicator.inventory().setStackInSlot(PanBlockEntity.SAMPLE_SLOT, sample);
            duplicator.inventory().setStackInSlot(PanBlockEntity.ANTIMATTER_SLOT,
                    new ItemStack(MaterialRegistration.MATERIAL_ITEMS.get("antimatter").get()));
            duplicator.receiveEnergy(1_000_000_000L);
            core.applyScreenControl(PanScreenPayloads.TOGGLE_PROHIBITED, 0);
        });
        helper.runAfterDelay(40, () -> {
            var player = helper.makeMockPlayer(GameType.CREATIVE);
            PanMenu menu = (PanMenu)duplicator.createMenu(20, player.getInventory(), player);
            require(!menu.sampleReachable() && duplicator.inventory().getStackInSlot(PanBlockEntity.OUTPUT_SLOT).isEmpty(),
                    "Prohibited sample was displayed as reachable or duplicated");
            require(duplicator.saveWithFullMetadata(helper.getLevel().registryAccess()).getLong("progress") == 0L,
                    "Prohibited sample continued processing");
            core.applyScreenControl(PanScreenPayloads.TOGGLE_PROHIBITED, 0);
        });
        helper.runAfterDelay(60, () -> {
            var player = helper.makeMockPlayer(GameType.CREATIVE);
            PanMenu menu = (PanMenu)duplicator.createMenu(21, player.getInventory(), player);
            require(menu.sampleReachable(), "Permitted sample did not become reachable");
            CompoundTag state = duplicator.saveWithFullMetadata(helper.getLevel().registryAccess());
            require(state.getLong("progress") > 0L
                            || !duplicator.inventory().getStackInSlot(PanBlockEntity.OUTPUT_SLOT).isEmpty(),
                    "Duplicator did not resume after unprohibiting the sample");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 100)
    public static void guiPayloadsRejectWrongMenuPositionDistanceAndValues(GameTestHelper helper) {
        BlockPos corePos = new BlockPos(3, 3, 3);
        helper.setBlock(corePos, PanRegistration.PAN_CORE.get());
        PanBlockEntity core = helper.getBlockEntity(corePos);
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        player.moveTo(core.getBlockPos().getX() + 0.5D, core.getBlockPos().getY() + 0.5D,
                core.getBlockPos().getZ() + 0.5D);
        PanMenu menu = new PanMenu(9, player.getInventory(), core,
                new net.minecraft.world.inventory.SimpleContainerData(12));
        player.containerMenu = menu;
        helper.runAfterDelay(20, () -> {
            require(core.candidatePages() > 1, "PAN payload test requires multiple pages");
            core.applyScreenControl(PanScreenPayloads.PAGE, 1);
            player.containerMenu = player.inventoryMenu;
            PanScreenPayloads.apply(player, new PanScreenPayloads.Control(
                    core.getBlockPos(), PanScreenPayloads.SORT, 2));
            require(core.candidateSort() == 0, "A PAN control without its menu changed server state");
            BlockPos duplicatorPos = new BlockPos(5, 3, 3);
            helper.setBlock(duplicatorPos, PanRegistration.DEFINITIONS.get("pan_duplicator_tier_10").block().get());
            PanBlockEntity duplicator = helper.getBlockEntity(duplicatorPos);
            player.containerMenu = new PanMenu(10, player.getInventory(), duplicator,
                    new net.minecraft.world.inventory.SimpleContainerData(12));
            PanScreenPayloads.apply(player, new PanScreenPayloads.Control(
                    duplicatorPos, PanScreenPayloads.SORT, 2));
            require(duplicator.candidateSort() == 0, "A duplicator accepted a core-only control");
            player.containerMenu = menu;
            PanScreenPayloads.apply(player, new PanScreenPayloads.Control(
                    core.getBlockPos().east(), PanScreenPayloads.SORT, 2));
            PanScreenPayloads.apply(player, new PanScreenPayloads.Control(
                    core.getBlockPos(), PanScreenPayloads.SORT, 99));
            PanScreenPayloads.apply(player, new PanScreenPayloads.Control(
                    core.getBlockPos(), PanScreenPayloads.PAGE, 99));
            PanScreenPayloads.apply(player, new PanScreenPayloads.Control(
                    core.getBlockPos(), PanScreenPayloads.REVERSE, 99));
            PanScreenPayloads.apply(player, new PanScreenPayloads.Control(
                    core.getBlockPos(), PanScreenPayloads.SELECT, PanBlockEntity.CANDIDATES_PER_PAGE));
            require(core.candidateSort() == 0 && core.candidatePage() == 1
                            && core.selectedCandidate() == null && !core.candidateReverse(),
                    "A forged PAN position or value changed server state");
            PanScreenPayloads.apply(player, new PanScreenPayloads.Control(
                    core.getBlockPos(), PanScreenPayloads.SORT, 1));
            require(core.candidateSort() == 1, "A valid open-menu PAN operation was rejected");
            player.moveTo(core.getBlockPos().getX() + 20.0D, core.getBlockPos().getY(), core.getBlockPos().getZ());
            PanScreenPayloads.apply(player, new PanScreenPayloads.Control(
                    core.getBlockPos(), PanScreenPayloads.SORT, 2));
            require(core.candidateSort() == 1, "A remote PAN payload bypassed the distance check");
        });
        helper.runAfterDelay(21, helper::succeed);
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void originalGuiPngHashesRemainUnmodified(GameTestHelper helper) {
        for (var entry : ORIGINAL_GUI_HASHES.entrySet()) {
            String path = "/assets/clayium/textures/gui/" + entry.getKey();
            try (var input = ClayiumMod.class.getResourceAsStream(path)) {
                require(input != null, "Missing original GUI asset " + entry.getKey());
                String actual = HexFormat.of().formatHex(
                        MessageDigest.getInstance("SHA-256").digest(input.readAllBytes()));
                require(actual.equals(entry.getValue()),
                        "Original GUI asset changed: " + entry.getKey());
            } catch (IOException | NoSuchAlgorithmException exception) {
                throw new GameTestAssertException("Could not hash original GUI assets: " + exception.getMessage());
            }
        }
        helper.succeed();
    }

    private static void slot(Slot slot, int x, int y, String name) {
        require(slot.x == x && slot.y == y,
                name + " moved from " + x + "," + y + " to " + slot.x + "," + slot.y);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private GuiParityGameTests() {
    }
}
