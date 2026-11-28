package net.claustra01.clayium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.netty.buffer.Unpooled;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.claustra01.clayium.block.ClayContainerBlock;
import net.claustra01.clayium.blockentity.AbstractClayMachineBlockEntity;
import net.claustra01.clayium.blockentity.AutoTraderBlockEntity;
import net.claustra01.clayium.blockentity.ChemicalReactorBlockEntity;
import net.claustra01.clayium.blockentity.ClayContainerIoIcons;
import net.claustra01.clayium.blockentity.ClayInterfaceBlockEntity;
import net.claustra01.clayium.blockentity.ClayWorkTableBlockEntity;
import net.claustra01.clayium.blockentity.FluidTranslatorBlockEntity;
import net.claustra01.clayium.blockentity.LaserInterfaceBlockEntity;
import net.claustra01.clayium.blockentity.MetalChestBlockEntity;
import net.claustra01.clayium.blockentity.MultitrackBufferBlockEntity;
import net.claustra01.clayium.blockentity.RedstoneInterfaceBlockEntity;
import net.claustra01.clayium.blockentity.SimpleClayMachineBlockEntity;
import net.claustra01.clayium.blockentity.StorageContainerBlockEntity;
import net.claustra01.clayium.blockentity.VacuumContainerBlockEntity;
import net.claustra01.clayium.filter.ClayiumFilterMatcher;
import net.claustra01.clayium.menu.ClayMemorySlot;
import net.claustra01.clayium.menu.ClayResultSlot;
import net.claustra01.clayium.menu.ClayStorageSlot;
import net.claustra01.clayium.menu.ClayiumBaseMenu;
import net.claustra01.clayium.menu.SlotTextureKind;
import net.claustra01.clayium.network.AutoTraderOffersPayload;
import net.claustra01.clayium.network.FilterTextPayload;
import net.claustra01.clayium.network.GuiButtonPayload;
import net.claustra01.clayium.recipe.ClayMachineRecipe;
import net.claustra01.clayium.recipe.ClayWorkTableRecipe;
import net.claustra01.clayium.recipe.CountedIngredient;
import net.claustra01.clayium.registry.ClayiumBlocks;
import net.claustra01.clayium.registry.ClayiumItems;
import net.claustra01.clayium.registry.ClayiumMenus;
import net.claustra01.clayium.util.ClayDirection.RelativeSide;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class Step10GuiAndNetworkTest {
    private static RegistryAccess.Frozen registries;

    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    @Test
    void all15MenuTypesAreRegisteredAndDistinct() {
        assertEquals(15, ClayiumMenus.REGISTRY.getEntries().size());
        Set<MenuType<?>> types = new HashSet<>();
        for (var entry : ClayiumMenus.REGISTRY.getEntries()) {
            MenuType<?> type = entry.get();
            assertNotNull(type);
            assertTrue(types.add(type), "Duplicate MenuType: " + entry.getId());
        }
        assertEquals(15, types.size());
    }

    @Test
    void customPacketPayloadsAnd64BitLongDataSplitRoundTripCleanly() {
        // 64-bit CE lossless DataSlot split
        long[] samples = {0L, 1L, 123_456_789_012_345L, -42L, Long.MAX_VALUE};
        for (long sample : samples) {
            int[] shorts = new int[4];
            ClayiumBaseMenu.putLongIntoDataSlots(sample, shorts);
            long reconstructed = ClayiumBaseMenu.readLongFrom4Shorts(shorts[0], shorts[1], shorts[2], shorts[3]);
            assertEquals(sample, reconstructed);
        }

        // GuiButtonPayload codec
        GuiButtonPayload btn = new GuiButtonPayload(42, 7);
        FriendlyByteBuf buf1 = new FriendlyByteBuf(Unpooled.buffer());
        GuiButtonPayload.STREAM_CODEC.encode(buf1, btn);
        GuiButtonPayload decodedBtn = GuiButtonPayload.STREAM_CODEC.decode(buf1);
        assertEquals(btn, decodedBtn);

        // FilterTextPayload codec
        FilterTextPayload filter = new FilterTextPayload(9, ".*ingot.*");
        FriendlyByteBuf buf2 = new FriendlyByteBuf(Unpooled.buffer());
        FilterTextPayload.STREAM_CODEC.encode(buf2, filter);
        FilterTextPayload decodedFilter = FilterTextPayload.STREAM_CODEC.decode(buf2);
        assertEquals(filter, decodedFilter);

        // AutoTraderOffersPayload codec
        MerchantOffers offers = new MerchantOffers();
        offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 3), new ItemStack(Items.DIAMOND, 1), 16, 5, 0.05F));
        AutoTraderOffersPayload traderPayload = new AutoTraderOffersPayload(5, 0, offers);
        RegistryFriendlyByteBuf buf3 = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries, ConnectionType.NEOFORGE);
        AutoTraderOffersPayload.STREAM_CODEC.encode(buf3, traderPayload);
        AutoTraderOffersPayload decodedTrader = AutoTraderOffersPayload.STREAM_CODEC.decode(buf3);
        assertEquals(5, decodedTrader.containerId());
        assertEquals(0, decodedTrader.currentRecipeIndex());
        assertEquals(1, decodedTrader.offers().size());
        assertEquals(Items.DIAMOND, decodedTrader.offers().get(0).getResult().getItem());
    }

    @Test
    void customSlotsHandleMemoryPhantomCopyAndStorageBulkOperations() {
        SimpleContainer container = new SimpleContainer(2);
        ClayMemorySlot memorySlot = new ClayMemorySlot(container, 0, 0, 0, SlotTextureKind.FILTER);
        assertTrue(memorySlot.isPhantomSlot());
        assertFalse(memorySlot.mayPlace(new ItemStack(Items.DIAMOND)));
        assertFalse(memorySlot.mayPickup(null));

        ItemStack copiedWhitelist = ClayiumFilterMatcher.withCopyFlag(
                new ItemStack(ClayiumItems.byPath("whitelist_item_filter").get()),
                true
        );
        assertTrue(ClayiumFilterMatcher.isCopyMode(copiedWhitelist));
        memorySlot.handlePhantomClick(copiedWhitelist);
        assertEquals(1, container.getItem(0).getCount());
        assertFalse(ClayiumFilterMatcher.isCopyMode(container.getItem(0)));
        memorySlot.handlePhantomClick(ItemStack.EMPTY);
        assertTrue(container.getItem(0).isEmpty());

        ClayResultSlot resultSlot = new ClayResultSlot(container, 1, 0, 0);
        assertFalse(resultSlot.mayPlace(new ItemStack(Items.IRON_INGOT)));
        assertEquals(SlotTextureKind.LARGE, resultSlot.getTextureKind());

        StorageContainerBlockEntity sc = assertInstanceOf(
                StorageContainerBlockEntity.class,
                createBe("storage_container")
        );
        assertFalse(sc.isVacuumContainer());
        Container sink = new SimpleContainer(1);
        ClayStorageSlot storageSlot = new ClayStorageSlot(sink, sc, 0, 0, 0);
        assertTrue(storageSlot.mayPlace(new ItemStack(Items.COBBLESTONE)));
        ItemStack leftover = storageSlot.safeInsert(new ItemStack(Items.COBBLESTONE, 64), 64);
        assertTrue(leftover.isEmpty());
        assertEquals(64, sc.getStoredCount());
        sc.insertIntoStorage(new ItemStack(Items.COBBLESTONE, 100), false);
        assertEquals(164, sc.getStoredCount());
        sc.setClientSyncedCount(500);
        sc.setClientSyncedMaxSize(Integer.MAX_VALUE);
        assertEquals(500, sc.getStoredCount());
        assertEquals(Integer.MAX_VALUE, sc.getMaxStorageSize());

        VacuumContainerBlockEntity vc = assertInstanceOf(
                VacuumContainerBlockEntity.class,
                createBe("vacuum_container")
        );
        assertTrue(vc.isVacuumContainer());
    }

    @Test
    void ioIconsAndInterfaceSyncNbtMatchOriginalSpecifications() {
        AbstractClayMachineBlockEntity bending = assertInstanceOf(
                AbstractClayMachineBlockEntity.class,
                createBe("clay_bending_machine")
        );
        assertEquals("import", ClayContainerIoIcons.getInsertIconName(bending, RelativeSide.UP, false));
        assertEquals("import_p", ClayContainerIoIcons.getInsertIconName(bending, RelativeSide.UP, true));
        assertEquals("import_energy", ClayContainerIoIcons.getInsertIconName(bending, RelativeSide.BACK, false));
        assertEquals("import_energy_p", ClayContainerIoIcons.getInsertIconName(bending, RelativeSide.BACK, true));
        assertEquals("export", ClayContainerIoIcons.getExtractIconName(bending, RelativeSide.DOWN, false));
        assertEquals("export_p", ClayContainerIoIcons.getExtractIconName(bending, RelativeSide.DOWN, true));
        assertNull(ClayContainerIoIcons.getFilterIconName(bending, RelativeSide.UP));
        bending.filters.set(RelativeSide.UP.id(), new ItemStack(ClayiumItems.byPath("whitelist_item_filter").get()));
        assertEquals("filter", ClayContainerIoIcons.getFilterIconName(bending, RelativeSide.UP));

        ChemicalReactorBlockEntity chem = assertInstanceOf(
                ChemicalReactorBlockEntity.class,
                createBe("basic_chemical_reactor")
        );
        assertEquals(4, ClayContainerIoIcons.getInsertIcons(chem).length);
        assertEquals("import_1", ClayContainerIoIcons.getInsertIcons(chem)[0]);
        assertEquals("import_energy", ClayContainerIoIcons.getInsertIcons(chem)[3]);
        assertEquals(3, ClayContainerIoIcons.getExtractIcons(chem).length);
        assertEquals("export_1", ClayContainerIoIcons.getExtractIcons(chem)[0]);

        FluidTranslatorBlockEntity ft = assertInstanceOf(
                FluidTranslatorBlockEntity.class,
                createBe("basic_fluid_translator")
        );
        assertEquals("import_l", ClayContainerIoIcons.getInsertIcons(ft)[0]);
        assertEquals("export_l", ClayContainerIoIcons.getExtractIcons(ft)[0]);

        MultitrackBufferBlockEntity mb = assertInstanceOf(
                MultitrackBufferBlockEntity.class,
                createBe("ultimate_multitrack_buffer")
        );
        assertEquals("import_m0", ClayContainerIoIcons.getInsertIcons(mb)[0]);
        assertEquals("export_m6", ClayContainerIoIcons.getExtractIcons(mb)[6]);

        AutoTraderBlockEntity trader = assertInstanceOf(AutoTraderBlockEntity.class, createBe("auto_trader"));
        assertEquals(4, ClayContainerIoIcons.getInsertIcons(trader).length);
        assertEquals("import_energy", ClayContainerIoIcons.getInsertIcons(trader)[3]);

        // Every referenced IO icon resolves to an existing block texture resource on classpath
        Set<String> allStems = new HashSet<>(List.of(
                "import", "import_1", "import_2", "import_energy", "import_l",
                "import_m0", "import_m1", "import_m2", "import_m3", "import_m4", "import_m5", "import_m6",
                "export", "export_1", "export_2", "export_l", "export_rf",
                "export_m0", "export_m1", "export_m2", "export_m3", "export_m4", "export_m5", "export_m6",
                "filter"
        ));
        for (String stem : allStems) {
            ResourceLocation texPath = ClayiumMod.id("textures/block/" + stem + ".png");
            assertNotNull(
                    Step10GuiAndNetworkTest.class.getResource("/assets/" + texPath.getNamespace() + "/" + texPath.getPath()),
                    "Missing texture for " + stem
            );
            if (!"filter".equals(stem)) {
                ResourceLocation pipePath = ClayiumMod.id("textures/block/" + stem + "_p.png");
                assertNotNull(
                        Step10GuiAndNetworkTest.class.getResource("/assets/" + pipePath.getNamespace() + "/" + pipePath.getPath()),
                        "Missing pipe texture for " + stem
                );
            }
        }

        // Interface Synced client update tag persistence
        RedstoneInterfaceBlockEntity ri = assertInstanceOf(
                RedstoneInterfaceBlockEntity.class,
                createBe("advanced_redstone_interface")
        );
        CompoundTag riUpdate = ri.getUpdateTag(registries);
        assertTrue(riUpdate.contains("Synced"));
        ClayInterfaceBlockEntity ci = assertInstanceOf(
                ClayInterfaceBlockEntity.class,
                createBe("advanced_clay_interface")
        );
        assertTrue(ci.getUpdateTag(registries).contains("Synced"));
        LaserInterfaceBlockEntity li = assertInstanceOf(
                LaserInterfaceBlockEntity.class,
                createBe("clay_steel_clay_laser_interface")
        );
        assertTrue(li.getUpdateTag(registries).contains("AcceptCoordChanger"));
        assertTrue(li.getUpdateTag(registries).contains("Synced"));
    }

    @Test
    void chestRenderShapeAndManualMachineAndWorkTableInteractionsWork() {
        Block chestBlock = ClayiumBlocks.byPath("aluminium_chest").get();
        assertEquals(RenderShape.ENTITYBLOCK_ANIMATED, chestBlock.defaultBlockState().getRenderShape());

        MetalChestBlockEntity chest = assertInstanceOf(MetalChestBlockEntity.class, createBe("aluminium_chest"));
        assertTrue(chest.triggerEvent(1, 2));
        assertEquals(2, chest.numPlayersUsing);
        assertTrue(chest.triggerEvent(1, 0));
        assertEquals(0, chest.numPlayersUsing);

        ClayWorkTableBlockEntity wt = assertInstanceOf(ClayWorkTableBlockEntity.class, createBe("clay_work_table"));
        wt.setOverrideRecipesForTest(List.of(new ClayWorkTableRecipe(
                new CountedIngredient(Ingredient.of(Items.CLAY_BALL), 1),
                3,
                new ItemStack(ClayiumItems.byPath("clay_plate").get(), 1),
                ItemStack.EMPTY,
                1
        )));
        wt.setStack(0, new ItemStack(Items.CLAY_BALL, 1));
        wt.setStack(1, new ItemStack(ClayiumItems.byPath("clay_rolling_pin").get()));
        assertEquals(1, wt.canPushButton(3));
        wt.pushButton(3);
        assertEquals(ClayiumItems.byPath("clay_plate").get(), wt.getStack(2).getItem());

        SimpleClayMachineBlockEntity sm = assertInstanceOf(SimpleClayMachineBlockEntity.class, createBe("clay_bending_machine"));
        assertEquals("bending_machine", sm.getRecipeCategoryId());
        sm.setOverrideRecipesForTest(List.of(new ClayMachineRecipe(
                "bending_machine",
                List.of(new CountedIngredient(Ingredient.of(ClayiumItems.byPath("clay_plate").get()), 1)),
                List.of(new ItemStack(Items.CLAY_BALL, 1)),
                1,
                10L,
                20L,
                0
        )));
        sm.setStack(0, new ItemStack(ClayiumItems.byPath("clay_plate").get(), 1));
        assertEquals(1, sm.canPushButton());
        sm.pushButton();
        assertEquals(5L, sm.getClayEnergy());

        // Filter GUI kinds
        assertInstanceOf(ClayContainerBlock.class, ClayiumBlocks.byPath("clay_bending_machine").get());
    }

    private static BlockEntity createBe(String path) {
        Block block = ClayiumBlocks.byPath(path).get();
        return ((EntityBlock) block).newBlockEntity(BlockPos.ZERO, block.defaultBlockState());
    }
}
