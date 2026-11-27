package net.claustra01.clayium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.claustra01.clayium.block.ClayContainerBlock;
import net.claustra01.clayium.block.StorageContainerBlock;
import net.claustra01.clayium.blockentity.BufferBlockEntity;
import net.claustra01.clayium.blockentity.DistributorBlockEntity;
import net.claustra01.clayium.blockentity.FluidTranslatorBlockEntity;
import net.claustra01.clayium.blockentity.MetalChestBlockEntity;
import net.claustra01.clayium.blockentity.MultitrackBufferBlockEntity;
import net.claustra01.clayium.blockentity.RedstoneInterfaceBlockEntity;
import net.claustra01.clayium.blockentity.StorageContainerBlockEntity;
import net.claustra01.clayium.blockentity.VacuumContainerBlockEntity;
import net.claustra01.clayium.component.ClayiumDataComponentTypes.FilterConfigData;
import net.claustra01.clayium.component.ClayiumDataComponentTypes.StorageContentData;
import net.claustra01.clayium.filter.ClayiumFilterMatcher;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.registry.ClayiumBlocks;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.registry.ClayiumItems;
import net.claustra01.clayium.transfer.ClayiumFluidTransfer;
import net.claustra01.clayium.transfer.ClayiumTransfer;
import net.claustra01.clayium.util.ClayDirection.RelativeSide;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.IItemHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class Step08TransportAndStorageTest {
    private static RegistryAccess.Frozen registries;

    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    @Test
    void blockEntityTypesAndPipeModeStatesAreRegisteredCorrectly() {
        assertNotNull(ClayiumBlockEntities.BUFFER.get());
        assertNotNull(ClayiumBlockEntities.MULTITRACK_BUFFER.get());
        assertNotNull(ClayiumBlockEntities.FLUID_TRANSLATOR.get());
        assertNotNull(ClayiumBlockEntities.STORAGE_CONTAINER.get());
        assertNotNull(ClayiumBlockEntities.VACUUM_CONTAINER.get());
        assertNotNull(ClayiumBlockEntities.METAL_CHEST.get());
        assertNotNull(ClayiumBlockEntities.DISTRIBUTOR.get());
        assertNotNull(ClayiumBlockEntities.CLAY_INTERFACE.get());
        assertNotNull(ClayiumBlockEntities.REDSTONE_INTERFACE.get());

        ClayContainerBlock buffer = (ClayContainerBlock) ClayiumBlocks.byPath("basic_buffer").get();
        assertTrue(buffer.canBePipe());
        assertTrue(buffer.defaultBlockState().hasProperty(ClayContainerBlock.PIPE_MODE));
        assertFalse(buffer.defaultBlockState().getValue(ClayContainerBlock.PIPE_MODE));

        ClayContainerBlock chest = (ClayContainerBlock) ClayiumBlocks.byPath("silicon_chest").get();
        assertFalse(chest.canBePipe());
        assertFalse(chest.defaultBlockState().hasProperty(ClayContainerBlock.PIPE_MODE));

        ClayContainerBlock workTable = (ClayContainerBlock) ClayiumBlocks.byPath("clay_work_table").get();
        assertFalse(workTable.canBePipe());
        assertFalse(workTable.defaultBlockState().hasProperty(ClayContainerBlock.PIPE_MODE));
    }

    @Test
    void allItemFilterKindsAndCopyFlagsMatchOriginalSemantics() {
        ItemStack iron = new ItemStack(Items.IRON_INGOT);
        ItemStack clay = new ItemStack(Items.CLAY_BALL);

        ItemStack whitelist = new ItemStack(ClayiumItems.byPath("whitelist_item_filter").get());
        whitelist.set(
                ClayiumDataComponents.FILTER_CONFIG.get(),
                new FilterConfigData("", false, ItemContainerContents.fromItems(List.of(iron)))
        );
        assertTrue(ClayiumFilterMatcher.match(whitelist, iron));
        assertFalse(ClayiumFilterMatcher.match(whitelist, clay));

        ItemStack blacklist = new ItemStack(ClayiumItems.byPath("blacklist_item_filter").get());
        blacklist.set(
                ClayiumDataComponents.FILTER_CONFIG.get(),
                new FilterConfigData("", false, ItemContainerContents.fromItems(List.of(iron)))
        );
        assertFalse(ClayiumFilterMatcher.match(blacklist, iron));
        assertTrue(ClayiumFilterMatcher.match(blacklist, clay));

        // Nested blacklist inside whitelist + copy flag behavior
        ItemStack nested = new ItemStack(ClayiumItems.byPath("whitelist_item_filter").get());
        nested.set(
                ClayiumDataComponents.FILTER_CONFIG.get(),
                new FilterConfigData("", false, ItemContainerContents.fromItems(List.of(blacklist)))
        );
        assertTrue(ClayiumFilterMatcher.match(nested, clay));
        assertFalse(ClayiumFilterMatcher.match(nested, iron));

        ClayiumFilterMatcher.withCopyFlag(whitelist, true);
        assertTrue(ClayiumFilterMatcher.isCopyMode(whitelist));
        assertTrue(whitelist.getHoverName().getString().endsWith("(Copy)"));
        ClayiumFilterMatcher.withCopyFlag(whitelist, false);
        assertFalse(ClayiumFilterMatcher.isCopyMode(whitelist));

        ItemStack duplicator = new ItemStack(ClayiumItems.byPath("item_filter_duplicator").get());
        assertTrue(ClayiumFilterMatcher.isCopyMode(duplicator));
        assertFalse(ClayiumFilterMatcher.match(duplicator, iron));

        // Fuzzy filter matches damaged tools of the same item
        ItemStack damagedPick1 = new ItemStack(ClayiumItems.byPath("clay_pickaxe").get());
        damagedPick1.setDamageValue(50);
        ItemStack damagedPick2 = new ItemStack(ClayiumItems.byPath("clay_pickaxe").get());
        damagedPick2.setDamageValue(200);
        ItemStack fuzzy = new ItemStack(ClayiumItems.byPath("fuzzy_item_filter").get());
        fuzzy.set(
                ClayiumDataComponents.FILTER_CONFIG.get(),
                new FilterConfigData("", false, ItemContainerContents.fromItems(List.of(damagedPick1)))
        );
        assertTrue(ClayiumFilterMatcher.match(fuzzy, damagedPick2));

        // Tag OreDict alias mapping
        List<String> aliases = ClayiumFilterMatcher.tagNamesAndAliases(ItemTags.create(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("c", "ingots/copper")));
        assertTrue(aliases.contains("ingotCopper"));
        assertTrue(aliases.contains("c:ingots/copper"));

        // Name, Translation Key, Registry ID, Mod ID, Damage Value, Invalid Regex
        ItemStack displayName = new ItemStack(ClayiumItems.byPath("display_name_item_filter").get());
        displayName.set(ClayiumDataComponents.FILTER_CONFIG.get(), new FilterConfigData("Clay", true, ItemContainerContents.EMPTY));
        assertTrue(ClayiumFilterMatcher.match(displayName, clay));

        ItemStack translationKey = new ItemStack(ClayiumItems.byPath("translation_key_item_filter").get());
        translationKey.set(ClayiumDataComponents.FILTER_CONFIG.get(), new FilterConfigData("item\\.minecraft\\.clay_ball", true, ItemContainerContents.EMPTY));
        assertTrue(ClayiumFilterMatcher.match(translationKey, clay));

        ItemStack registryId = new ItemStack(ClayiumItems.byPath("registry_id_item_filter").get());
        registryId.set(ClayiumDataComponents.FILTER_CONFIG.get(), new FilterConfigData("^minecraft:iron_ingot$", true, ItemContainerContents.EMPTY));
        assertTrue(ClayiumFilterMatcher.match(registryId, iron));
        assertFalse(ClayiumFilterMatcher.match(registryId, clay));

        ItemStack modId = new ItemStack(ClayiumItems.byPath("mod_id_item_filter").get());
        modId.set(ClayiumDataComponents.FILTER_CONFIG.get(), new FilterConfigData("^clayium_neoforged$", true, ItemContainerContents.EMPTY));
        assertTrue(ClayiumFilterMatcher.match(modId, damagedPick1));
        assertFalse(ClayiumFilterMatcher.match(modId, iron));

        ItemStack invalidRegex = new ItemStack(ClayiumItems.byPath("registry_id_item_filter").get());
        invalidRegex.set(ClayiumDataComponents.FILTER_CONFIG.get(), new FilterConfigData("[unclosed", true, ItemContainerContents.EMPTY));
        assertFalse(ClayiumFilterMatcher.match(invalidRegex, iron));

        ItemStack dmgFilter = new ItemStack(ClayiumItems.byPath("damage_value_item_filter").get());
        dmgFilter.set(ClayiumDataComponents.FILTER_CONFIG.get(), new FilterConfigData("50", true, ItemContainerContents.EMPTY));
        assertTrue(ClayiumFilterMatcher.match(dmgFilter, damagedPick1));
        assertFalse(ClayiumFilterMatcher.match(dmgFilter, damagedPick2));

        // Remove custom name test check doesn't throw
        ItemStack renamed = clay.copy();
        renamed.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Special Clay"));
        assertTrue(ClayiumFilterMatcher.match(displayName, renamed));
    }

    @Test
    void bufferAndMultitrackBufferEnforceTierSizesRoutesAndTrackIsolation() {
        BufferBlockEntity buf4 = new BufferBlockEntity(
                BlockPos.ZERO,
                ClayiumBlocks.byPath("basic_buffer").get().defaultBlockState()
        );
        assertEquals(4, buf4.getTier());
        assertEquals(2, buf4.getInventorySize());
        assertEquals(8, buf4.autoInsertInterval);
        assertEquals(1, buf4.maxAutoInsertDefault);

        BufferBlockEntity buf13 = new BufferBlockEntity(
                BlockPos.ZERO,
                ClayiumBlocks.byPath("opa_buffer").get().defaultBlockState()
        );
        assertEquals(54, buf13.getInventorySize());
        assertEquals(1, buf13.autoInsertInterval);
        assertEquals(6400, buf13.maxAutoInsertDefault);

        // Side filter + extract route filtering + two-pass transfer
        buf4.extractRoutes[RelativeSide.FRONT.id()] = 0;
        buf4.insertRoutes[RelativeSide.BACK.id()] = 0;
        ItemStack ironFilter = new ItemStack(ClayiumItems.byPath("whitelist_item_filter").get());
        ironFilter.set(
                ClayiumDataComponents.FILTER_CONFIG.get(),
                new FilterConfigData("", false, ItemContainerContents.fromItems(List.of(new ItemStack(Items.IRON_INGOT))))
        );
        buf4.filters.set(RelativeSide.BACK.id(), ironFilter);

        IItemHandler backHandler = buf4.getItemHandler(Direction.SOUTH); // NORTH-facing block -> SOUTH = BACK
        assertFalse(backHandler.insertItem(0, new ItemStack(Items.CLAY_BALL, 4), false).isEmpty());
        assertTrue(backHandler.insertItem(0, new ItemStack(Items.IRON_INGOT, 4), false).isEmpty());

        BufferBlockEntity dest = new BufferBlockEntity(
                BlockPos.ZERO,
                ClayiumBlocks.byPath("basic_buffer").get().defaultBlockState()
        );
        int leftover = ClayiumTransfer.transfer(
                buf4.getItemHandler(Direction.NORTH),
                new int[]{0, 1},
                dest.getItemHandler(Direction.SOUTH),
                new int[]{0, 1},
                1
        );
        assertEquals(0, leftover);
        assertEquals(3, buf4.getStack(0).getCount());
        assertEquals(1, dest.getStack(0).getCount());

        // Multitrack Buffer track isolation & track filter
        MultitrackBufferBlockEntity mt = new MultitrackBufferBlockEntity(
                BlockPos.ZERO,
                ClayiumBlocks.byPath("clay_steel_multitrack_buffer").get().defaultBlockState()
        );
        assertEquals(5, mt.trackCount);
        assertEquals(4, mt.trackSize);
        mt.setStack(MultitrackBufferBlockEntity.FILTER_SLOT_START, new ItemStack(Items.GOLD_INGOT));

        IItemHandler track0 = mt.getItemHandlerForTrack(Direction.SOUTH, 0);
        IItemHandler track1 = mt.getItemHandlerForTrack(Direction.SOUTH, 1);
        // Track 0 rejects copper/iron because filter slot 54 locks it to gold
        assertFalse(track0.insertItem(0, new ItemStack(Items.IRON_INGOT), false).isEmpty());
        assertTrue(track0.insertItem(0, new ItemStack(Items.GOLD_INGOT), false).isEmpty());
        // Track 0 handler cannot insert into track 1 slot (index 4)
        assertFalse(track0.insertItem(4, new ItemStack(Items.GOLD_INGOT), false).isEmpty());
        // Track 1 has no filter so accepts iron into slot 4
        assertTrue(track1.insertItem(4, new ItemStack(Items.IRON_INGOT), false).isEmpty());
    }

    @Test
    void fluidTranslatorAndCapsuleCompactionRoundTripExactly() {
        NonNullList<ItemStack> slots = NonNullList.withSize(9, ItemStack.EMPTY);
        int[] indices = {0, 1, 2, 3, 4, 5, 6, 7, 8};

        int filled = ClayiumFluidTransfer.fillFluid(slots, indices, new FluidStack(Fluids.WATER, 1156), FluidAction.EXECUTE);
        assertEquals(1156, filled);
        // 1156 = 1000 + 125 + 25 + 5 + 1
        assertEquals(Fluids.WATER, ClayiumFluidTransfer.extractCapsuleFluid(slots.get(0)).getFluid());
        assertEquals(1000, ClayiumFluidTransfer.extractCapsuleFluid(slots.get(0)).getAmount());
        assertEquals(125, ClayiumFluidTransfer.extractCapsuleFluid(slots.get(1)).getAmount());
        assertEquals(25, ClayiumFluidTransfer.extractCapsuleFluid(slots.get(2)).getAmount());
        assertEquals(5, ClayiumFluidTransfer.extractCapsuleFluid(slots.get(3)).getAmount());
        assertEquals(1, ClayiumFluidTransfer.extractCapsuleFluid(slots.get(4)).getAmount());

        // Tier 4 Fluid Translator (2 slots) caps distinct capsule sizes at 1000 + 125 = 1125 mB
        FluidTranslatorBlockEntity ft4 = new FluidTranslatorBlockEntity(
                BlockPos.ZERO,
                ClayiumBlocks.byPath("basic_fluid_translator").get().defaultBlockState()
        );
        assertEquals(1125, ft4.getFluidHandler(Direction.NORTH).fill(new FluidStack(Fluids.WATER, 1131), FluidAction.EXECUTE));

        // Tier 6 Precision Fluid Translator (12 slots) fits full 1131 mB decomposition
        FluidTranslatorBlockEntity ft = new FluidTranslatorBlockEntity(
                BlockPos.ZERO,
                ClayiumBlocks.byPath("precision_fluid_translator").get().defaultBlockState()
        );
        IFluidHandler fh = ft.getFluidHandler(Direction.NORTH);
        assertEquals(1131, fh.fill(new FluidStack(Fluids.WATER, 1131), FluidAction.EXECUTE));
        FluidStack drained = fh.drain(131, FluidAction.EXECUTE);
        assertEquals(131, drained.getAmount());
        assertEquals(1000, fh.getFluidInTank(0).getAmount());

        CompoundTag tag = ft.saveCustomOnly(registries);
        FluidTranslatorBlockEntity reloaded = new FluidTranslatorBlockEntity(
                BlockPos.ZERO,
                ClayiumBlocks.byPath("precision_fluid_translator").get().defaultBlockState()
        );
        reloaded.loadCustomOnly(tag, registries);
        assertEquals(1000, reloaded.getFluidHandler(Direction.NORTH).getFluidInTank(0).getAmount());
    }

    @Test
    void storageContainerVacuumDistributorMetalChestAndRedstoneInterfacePreserveOriginalSpecs() {
        // Storage Container
        StorageContainerBlockEntity sc = new StorageContainerBlockEntity(
                BlockPos.ZERO,
                ClayiumBlocks.byPath("storage_container").get().defaultBlockState()
        );
        assertEquals(65536, sc.getMaxStorageSize());
        assertEquals(64000 + 1536, sc.insertIntoStorage(new ItemStack(Items.COBBLESTONE, 64), false)
                + sc.insertIntoStorage(new ItemStack(Items.COBBLESTONE, 65472), false));
        assertEquals(65536, sc.getStoredCount());
        assertEquals(0, sc.insertIntoStorage(new ItemStack(Items.COBBLESTONE, 1), false));
        assertEquals(0, sc.insertIntoStorage(new ItemStack(Items.DIRT, 1), false));

        StorageContentData data = sc.toStorageContentData();
        assertEquals(Items.COBBLESTONE, data.item());
        assertEquals(65536L, data.count());
        assertEquals(65536, data.maxStorage());

        CompoundTag scTag = sc.saveCustomOnly(registries);
        StorageContainerBlockEntity scReloaded = new StorageContainerBlockEntity(
                BlockPos.ZERO,
                ClayiumBlocks.byPath("storage_container").get().defaultBlockState()
        );
        scReloaded.loadCustomOnly(scTag, registries);
        assertEquals(65536, scReloaded.getStoredCount());
        assertEquals(Items.COBBLESTONE, scReloaded.getStoredTemplate().getItem());

        // Upgraded stack component restore
        ItemStack upgradedDrop = new ItemStack(ClayiumBlocks.byPath("storage_container").get());
        upgradedDrop.set(
                ClayiumDataComponents.STORAGE_CONTENT.get(),
                new StorageContentData(Items.DIAMOND, 100_000L, Integer.MAX_VALUE)
        );
        StorageContainerBlockEntity scUpgraded = new StorageContainerBlockEntity(
                BlockPos.ZERO,
                ClayiumBlocks.byPath("storage_container").get().defaultBlockState()
        );
        scUpgraded.applyStackComponentData(upgradedDrop);
        assertTrue(scUpgraded.isUpgraded());
        assertEquals(Integer.MAX_VALUE, scUpgraded.getMaxStorageSize());
        assertEquals(100_000, scUpgraded.getStoredCount());

        // Vacuum Container voids matching items
        VacuumContainerBlockEntity vc = new VacuumContainerBlockEntity(
                BlockPos.ZERO,
                ClayiumBlocks.byPath("vacuum_container").get().defaultBlockState()
        );
        vc.setFilterTemplate(new ItemStack(Items.GRAVEL));
        assertEquals(0, vc.insertIntoStorage(new ItemStack(Items.COBBLESTONE, 32), false));
        assertEquals(32, vc.insertIntoStorage(new ItemStack(Items.GRAVEL, 32), false));
        assertEquals(0, vc.getStoredCount());

        // Metal Chest (Europium: 13 cols x 8 rows x 8 pages = 832 slots)
        MetalChestBlockEntity euChest = new MetalChestBlockEntity(
                BlockPos.ZERO,
                ClayiumBlocks.byPath("europium_chest").get().defaultBlockState()
        );
        assertEquals(832, euChest.getInventorySize());
        assertFalse(euChest.autoInsert);
        assertFalse(euChest.autoExtract);

        // Distributor (T7=2x2 colonies, T8=3x2, T9=4x3)
        DistributorBlockEntity dist9 = new DistributorBlockEntity(
                BlockPos.ZERO,
                ClayiumBlocks.byPath("ultimate_distributor").get().defaultBlockState()
        );
        assertEquals(9, dist9.getTier());
        assertEquals(12, dist9.inventoryColonyX * dist9.inventoryColonyY);
        assertEquals(48, dist9.slotsDrop.length);
        assertEquals(512, dist9.maxAutoInsertDefault);

        // Redstone Interface cycles through 9 states & persists NBT
        RedstoneInterfaceBlockEntity ri = new RedstoneInterfaceBlockEntity(
                BlockPos.ZERO,
                ClayiumBlocks.byPath("advanced_redstone_interface").get().defaultBlockState()
        );
        assertEquals("None", ri.getControlState());
        ri.cycleControlState();
        assertEquals("Emit if idle", ri.getControlState());
        CompoundTag riTag = ri.saveCustomOnly(registries);
        RedstoneInterfaceBlockEntity ri2 = new RedstoneInterfaceBlockEntity(
                BlockPos.ZERO,
                ClayiumBlocks.byPath("advanced_redstone_interface").get().defaultBlockState()
        );
        ri2.loadCustomOnly(riTag, registries);
        assertEquals("Emit if idle", ri2.getControlState());
    }
}
