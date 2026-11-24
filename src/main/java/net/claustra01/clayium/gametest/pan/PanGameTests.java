package net.claustra01.clayium.gametest.pan;

import java.util.List;
import java.util.Map;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.pan.PanBlockEntity;
import net.claustra01.clayium.pan.PanNetwork;
import net.claustra01.clayium.pan.PanRecipeClosure;
import net.claustra01.clayium.pan.PanMenu;
import net.claustra01.clayium.pan.PanScreenPayloads;
import net.claustra01.clayium.recipe.ClayiumRecipesReloadedEvent;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.PanRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PanGameTests {
    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 500)
    public static void networkTraversalIsIterativeBoundedAndInvalidated(GameTestHelper helper) {
        BlockPos corePos = new BlockPos(3, 3, 3);
        helper.setBlock(corePos, PanRegistration.PAN_CORE.get());
        helper.setBlock(corePos.east(), PanRegistration.PAN_CABLE.get());
        helper.setBlock(corePos.east().south(), PanRegistration.PAN_CABLE.get());
        helper.setBlock(corePos.south(), PanRegistration.PAN_CABLE.get());
        helper.setBlock(corePos.east(2), PanRegistration.DEFINITIONS.get("pan_adapter_tier_10").block().get());
        PanBlockEntity core = helper.getBlockEntity(corePos);
        helper.runAfterDelay(10, () -> require(core.nodeCount() == 5 && core.adapterCount() == 1,
                "Real PAN scan did not traverse the cable cycle and adapter once"));
        helper.runAfterDelay(12, () -> {
            helper.setBlock(corePos.east(), net.minecraft.world.level.block.Blocks.AIR);
            helper.setBlock(corePos.south(), net.minecraft.world.level.block.Blocks.AIR);
        });
        helper.runAfterDelay(20, () -> require(core.adapterCount() == 0,
                "Removing a cable did not invalidate the connected core"));
        helper.runAfterDelay(30, () -> {
            var saved = core.saveWithFullMetadata(helper.getLevel().registryAccess());
            core.loadWithComponents(saved, helper.getLevel().registryAccess());
            require(core.nodeCount() == 0, "Loaded PAN state retained a stale scan");
        });
        helper.runAfterDelay(40, () -> require(core.nodeCount() == 1,
                "Loaded PAN core did not rescan its network"));
        helper.runAfterDelay(245, () -> require(core.nodeCount() == 1
                        && core.lastScanTick() >= helper.getLevel().getGameTime() - 20,
                "The 200-tick rescan did not revisit the bounded component"));
        helper.runAfterDelay(246, helper::succeed);
    }

    @GameTest(template = "empty", batch = "clayium_pan_limits", timeoutTicks = 200)
    public static void realNetworkScanStopsAtDepthAndNodeLimits(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos lineOrigin = helper.absolutePos(new BlockPos(1_024, 70, 1_024));
        for (int chunkX = lineOrigin.getX() >> 4; chunkX <= (lineOrigin.getX() + 1_099) >> 4; chunkX++) {
            level.setChunkForced(chunkX, lineOrigin.getZ() >> 4, true);
        }
        for (int x = 0; x < 1_100; x++) {
            level.setBlock(lineOrigin.offset(x, 0, 0), PanRegistration.PAN_CABLE.get().defaultBlockState(), 2);
        }
        BlockPos denseOrigin = helper.absolutePos(new BlockPos(2_048, 70, 2_048));
        for (int chunkX = denseOrigin.getX() >> 4; chunkX <= (denseOrigin.getX() + 15) >> 4; chunkX++) {
            for (int chunkZ = denseOrigin.getZ() >> 4; chunkZ <= (denseOrigin.getZ() + 15) >> 4; chunkZ++) {
                level.setChunkForced(chunkX, chunkZ, true);
            }
        }
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 40; y++) {
                for (int z = 0; z < 16; z++) {
                    level.setBlock(denseOrigin.offset(x, y, z),
                            PanRegistration.PAN_CABLE.get().defaultBlockState(), 2);
                }
            }
        }
        PanNetwork.Scan line = PanNetwork.begin(level, lineOrigin);
        PanNetwork.Scan dense = PanNetwork.begin(level, denseOrigin);
        helper.succeedWhen(() -> {
            boolean lineDone = line.advance(PanNetwork.NODES_PER_TICK);
            boolean denseDone = dense.advance(PanNetwork.NODES_PER_TICK);
            require(line.nodeCount() <= 1_001 && dense.nodeCount() <= 10_000,
                    "A real PAN scan crossed its depth or node limit");
            if (!lineDone || !denseDone) {
                throw new GameTestAssertException("PAN scans are still within their per-tick budget");
            }
            require(line.nodeCount() == 1_001 && dense.nodeCount() == 10_000,
                    "PAN scan did not reach the exact original boundaries: line=" + line.nodeCount()
                            + " dense=" + dense.nodeCount());
            for (int chunkX = lineOrigin.getX() >> 4; chunkX <= (lineOrigin.getX() + 1_099) >> 4; chunkX++) {
                level.setChunkForced(chunkX, lineOrigin.getZ() >> 4, false);
            }
            for (int chunkX = denseOrigin.getX() >> 4; chunkX <= (denseOrigin.getX() + 15) >> 4; chunkX++) {
                for (int chunkZ = denseOrigin.getZ() >> 4; chunkZ <= (denseOrigin.getZ() + 15) >> 4; chunkZ++) {
                    level.setChunkForced(chunkX, chunkZ, false);
                }
            }
        });
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void recipeClosureRejectsCyclesFreeRecipesAndUnknownComponents(GameTestHelper helper) {
        ResourceLocation raw = ClayiumMod.id("pan_test_raw");
        ResourceLocation intermediate = ClayiumMod.id("pan_test_intermediate");
        ResourceLocation product = ClayiumMod.id("pan_test_product");
        ResourceLocation unreachable = ClayiumMod.id("pan_test_unreachable");
        List<PanRecipeClosure.Edge> recipes = List.of(
                new PanRecipeClosure.Edge(List.of(new PanRecipeClosure.Input(raw, 2)), intermediate, 1),
                new PanRecipeClosure.Edge(List.of(new PanRecipeClosure.Input(intermediate, 3)), product, 2),
                new PanRecipeClosure.Edge(List.of(new PanRecipeClosure.Input(product, 1)), intermediate, 1),
                new PanRecipeClosure.Edge(List.of(), unreachable, 64));
        PanRecipeClosure.Result first = PanRecipeClosure.calculate(Map.of(raw, 5L), recipes);
        require(first.costs().get(intermediate) == 10L && first.costs().get(product) == 15L,
                "Multi-stage PAN costs are not the positive fixed point");
        require(!first.costs().containsKey(unreachable)
                        && first.consumption().get(product) == 15L,
                "A cycle or free recipe created an unconditional candidate");

        PanRecipeClosure.Result reloaded = PanRecipeClosure.calculate(Map.of(raw, 5L),
                List.of(new PanRecipeClosure.Edge(List.of(new PanRecipeClosure.Input(raw, 1)), product, 5)));
        require(reloaded.costs().get(product) == 1L && !reloaded.costs().containsKey(intermediate),
                "Replacing datapack recipes left stale PAN closure entries");
        ItemStack componentStack = new ItemStack(Items.CLAY_BALL);
        componentStack.set(DataComponents.CUSTOM_NAME, Component.literal("unknown"));
        require(!PanRecipeClosure.isPlain(componentStack),
                "Unknown item components were accepted for PAN duplication");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "clayium_pan_reload", timeoutTicks = 400)
    public static void connectedAdapterRebuildsClosureAfterRecipeReload(GameTestHelper helper) {
        BlockPos corePos = new BlockPos(3, 3, 3);
        helper.setBlock(corePos, PanRegistration.PAN_CORE.get());
        helper.setBlock(corePos.east(), PanRegistration.DEFINITIONS.get("pan_adapter_tier_10").block().get());
        helper.setBlock(corePos.east(2), net.minecraft.world.level.block.Blocks.CRAFTING_TABLE);
        PanBlockEntity core = helper.getBlockEntity(corePos);
        PanBlockEntity adapter = helper.getBlockEntity(corePos.east());
        require(adapter.setAdapterPattern(0, 0, new ItemStack(Items.OAK_LOG)),
                "The adapter rejected its selected crafting pattern");
        var manager = helper.getLevel().getRecipeManager();
        List<RecipeHolder<?>> original = List.copyOf(manager.getRecipes());
        helper.runAfterDelay(100, () -> {
            require(core.closure().costs().containsKey(id(Items.OAK_PLANKS)),
                    "Connected crafting table did not make planks reachable from the base log");
            require(!core.closure().costs().containsKey(id(Items.STICK)),
                    "The adapter published an unselected crafting-table recipe");
            manager.replaceRecipes(original.stream().filter(holder ->
                    holder.value().getType() != RecipeType.CRAFTING).toList());
            NeoForge.EVENT_BUS.post(new ClayiumRecipesReloadedEvent(manager));
        });
        helper.runAfterDelay(200, () -> {
            require(!core.closure().costs().containsKey(id(Items.OAK_PLANKS)),
                    "Removed crafting recipe remained in the published PAN closure");
            manager.replaceRecipes(original);
            NeoForge.EVENT_BUS.post(new ClayiumRecipesReloadedEvent(manager));
        });
        helper.runAfterDelay(300, () -> require(core.closure().costs().containsKey(id(Items.OAK_PLANKS)),
                "Restored recipe did not return after a recipe reload"));
        helper.runAfterDelay(301, helper::succeed);
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 400)
    public static void duplicatorConsumesOneAntimatterAndExactCoreEnergyWhileKeepingSample(GameTestHelper helper) {
        BlockPos corePos = new BlockPos(3, 3, 3);
        BlockPos adapterPos = corePos.east(2);
        BlockPos duplicatorPos = corePos.west();
        helper.setBlock(corePos, PanRegistration.PAN_CORE.get());
        helper.setBlock(corePos.east(), PanRegistration.PAN_CABLE.get());
        helper.setBlock(adapterPos, PanRegistration.DEFINITIONS.get("pan_adapter_tier_10").block().get());
        helper.setBlock(adapterPos.east(), net.minecraft.world.level.block.Blocks.CRAFTING_TABLE);
        helper.setBlock(duplicatorPos, PanRegistration.DEFINITIONS.get("pan_duplicator_tier_13").block().get());
        PanBlockEntity core = helper.getBlockEntity(corePos);
        PanBlockEntity adapter = helper.getBlockEntity(adapterPos);
        require(adapter.setAdapterPattern(0, 0, new ItemStack(Items.OAK_LOG)),
                "The adapter rejected its selected crafting pattern");
        PanBlockEntity duplicator = helper.getBlockEntity(duplicatorPos);
        helper.runAfterDelay(100, () -> {
            require(core.adapterCount() == 1 && core.closure().costs().containsKey(id(Items.OAK_PLANKS)),
                    "The adapter did not collect a recipe-derived candidate");
            duplicator.inventory().setStackInSlot(PanBlockEntity.SAMPLE_SLOT, new ItemStack(Items.OAK_PLANKS));
            duplicator.inventory().setStackInSlot(PanBlockEntity.ANTIMATTER_SLOT,
                    new ItemStack(MaterialRegistration.MATERIAL_ITEMS.get("antimatter").get(), 2));
            duplicator.receiveEnergy(core.closure().consumption(new ItemStack(Items.OAK_PLANKS)) - 1);
        });
        helper.runAfterDelay(110, () -> {
            require(duplicator.inventory().getStackInSlot(PanBlockEntity.OUTPUT_SLOT).isEmpty()
                            && duplicator.inventory().getStackInSlot(PanBlockEntity.ANTIMATTER_SLOT).getCount() == 2,
                    "Insufficient CE consumed antimatter or produced output");
            duplicator.receiveEnergy(1);
        });
        helper.runAfterDelay(120, () -> {
            require(duplicator.inventory().getStackInSlot(PanBlockEntity.SAMPLE_SLOT).getCount() == 1
                            && duplicator.inventory().getStackInSlot(PanBlockEntity.ANTIMATTER_SLOT).getCount() == 1
                            && duplicator.inventory().getStackInSlot(PanBlockEntity.OUTPUT_SLOT).is(Items.OAK_PLANKS)
                            && duplicator.energy().stored().internalValue() == 0L,
                    "Duplication did not retain the sample or consume one antimatter and exact CE");
            duplicator.inventory().setStackInSlot(PanBlockEntity.OUTPUT_SLOT, ItemStack.EMPTY);
            ItemStack named = new ItemStack(Items.OAK_PLANKS);
            named.set(DataComponents.CUSTOM_NAME, Component.literal("unknown"));
            duplicator.inventory().setStackInSlot(PanBlockEntity.SAMPLE_SLOT, named);
            duplicator.receiveEnergy(1_000_000L);
        });
        helper.runAfterDelay(130, () -> require(duplicator.inventory().getStackInSlot(PanBlockEntity.OUTPUT_SLOT).isEmpty(),
                "Unknown DataComponents were duplicated"));
        helper.runAfterDelay(131, helper::succeed);
    }

    @GameTest(template = "empty", batch = "clayium_pan_tiers", timeoutTicks = 1_300)
    public static void allPanTiersDuplicateAtOriginalCeRates(GameTestHelper helper) {
        long[] originalTicksForOneHundredMillion = {1_000L, 200L, 40L, 1_000L, 1_000L,
                1_000L, 4L, 1_000L, 1_000L, 1_000L};
        for (int tier = 4; tier <= 13; tier++) {
            var definition = PanRegistration.DEFINITIONS.get("pan_duplicator_tier_" + tier);
            require(definition != null && definition.tier() == tier,
                    "Missing PAN duplicator tier " + tier);
            long ticks = PanBlockEntity.processingTicks(tier, 100_000_000L);
            require(ticks == originalTicksForOneHundredMillion[tier - 4],
                    "PAN duplicator rank does not follow the original CE per tick at tier " + tier);
        }
        for (int tier = 10; tier <= 13; tier++) {
            require(PanRegistration.DEFINITIONS.containsKey("pan_adapter_tier_" + tier),
                    "Missing PAN adapter tier " + tier);
        }
        require(PanRegistration.DEFINITIONS.size() == 16,
                "PAN core, cable, four adapters and ten duplicators are not all registered");
        BlockPos corePos = new BlockPos(3, 3, 3);
        helper.setBlock(corePos, PanRegistration.PAN_CORE.get());
        for (int x = 4; x <= 14; x++) {
            helper.setBlock(new BlockPos(x, 3, 3), PanRegistration.PAN_CABLE.get());
        }
        helper.setBlock(new BlockPos(14, 3, 4),
                PanRegistration.DEFINITIONS.get("pan_adapter_tier_10").block().get());
        helper.setBlock(new BlockPos(14, 3, 5),
                net.claustra01.clayium.registry.MachineRegistration.MACHINES
                        .get("chemical_metal_separator").block().get());
        PanBlockEntity adapter = helper.getBlockEntity(new BlockPos(14, 3, 4));
        require(adapter.setAdapterPattern(0, 0,
                        new ItemStack(MaterialRegistration.MATERIAL_ITEMS.get("industrial_clay_dust").get())),
                "PAN adapter rejected the selected chemical separator input");
        PanBlockEntity core = helper.getBlockEntity(corePos);
        for (int tier = 4; tier <= 13; tier++) {
            helper.setBlock(new BlockPos(tier, 3, 2),
                    PanRegistration.DEFINITIONS.get("pan_duplicator_tier_" + tier).block().get());
        }
        helper.runAfterDelay(200, () -> {
            ItemStack sample = new ItemStack(MaterialRegistration.MATERIAL_ITEMS.get("impure_copper_dust").get());
            long consumption = core.closure().consumption(sample);
            require(consumption == 98_088_000L && core.adapterCount() == 1,
                    "Connected core did not publish the original copper separator cost");
            for (int tier = 4; tier <= 13; tier++) {
                PanBlockEntity duplicator = helper.getBlockEntity(new BlockPos(tier, 3, 2));
                duplicator.inventory().setStackInSlot(PanBlockEntity.SAMPLE_SLOT, sample.copy());
                duplicator.inventory().setStackInSlot(PanBlockEntity.ANTIMATTER_SLOT,
                        new ItemStack(MaterialRegistration.MATERIAL_ITEMS.get("antimatter").get()));
                duplicator.receiveEnergy(consumption);
            }
        });
        for (int tier = 4; tier <= 13; tier++) {
            int checkedTier = tier;
            long ticks = PanBlockEntity.processingTicks(tier, 98_088_000L);
            helper.runAfterDelay((int)(200 + ticks - 2), () -> {
                PanBlockEntity duplicator = helper.getBlockEntity(new BlockPos(checkedTier, 3, 2));
                require(duplicator.inventory().getStackInSlot(PanBlockEntity.OUTPUT_SLOT).isEmpty(),
                        "PAN duplicator finished before its original rank duration at tier " + checkedTier);
            });
            helper.runAfterDelay((int)(200 + ticks + 2), () -> {
                PanBlockEntity duplicator = helper.getBlockEntity(new BlockPos(checkedTier, 3, 2));
                require(duplicator.inventory().getStackInSlot(PanBlockEntity.OUTPUT_SLOT)
                                .is(MaterialRegistration.MATERIAL_ITEMS.get("impure_copper_dust").get())
                                && duplicator.inventory().getStackInSlot(PanBlockEntity.SAMPLE_SLOT).getCount() == 1,
                        "PAN duplicator did not finish its original rank duration at tier " + checkedTier);
            });
        }
        helper.runAfterDelay(1_190, helper::succeed);
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void adapterPagesAreServerAuthoritativeGhostConversionsAndPersistSubItems(GameTestHelper helper) {
        BlockPos adapterPos = new BlockPos(3, 3, 3);
        helper.setBlock(adapterPos, PanRegistration.DEFINITIONS.get("pan_adapter_tier_11").block().get());
        helper.setBlock(adapterPos.east(), net.minecraft.world.level.block.Blocks.CRAFTING_TABLE);
        PanBlockEntity adapter = helper.getBlockEntity(adapterPos);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.moveTo(adapter.getBlockPos().getX() + 0.5D, adapter.getBlockPos().getY() + 0.5D,
                adapter.getBlockPos().getZ() + 0.5D);
        PanMenu menu = new PanMenu(17, player.getInventory(), adapter, new SimpleContainerData(12));
        player.containerMenu = menu;

        require(menu.adapterPages() == 2 && menu.adapterPage() == 0 && menu.machineSlots() == 27,
                "Tier 11 PAN Adapter did not expose two pages and 9+9+9 machine slots");
        require(menu.slots.get(0).x == 32 && menu.slots.get(0).y == 18
                        && menu.slots.get(8).x == 68 && menu.slots.get(8).y == 54
                        && menu.slots.get(9).x == 92 && menu.slots.get(9).y == 18
                        && menu.slots.get(18).x == 8 && menu.slots.get(18).y == 74,
                "PAN Adapter slot geometry differs from the original 3x3/3x3/9 layout");

        menu.setCarried(new ItemStack(Items.OAK_LOG, 16));
        PanScreenPayloads.apply(player, new PanScreenPayloads.AdapterPattern(adapter.getBlockPos(), 0, 0, false));
        require(menu.getCarried().getCount() == 16 && adapter.adapterPattern(0, 0).getCount() == 1,
                "Setting an adapter memory cell consumed the cursor or retained its count");
        PanScreenPayloads.apply(player, new PanScreenPayloads.AdapterPage(adapter.getBlockPos(), 1));
        require(menu.adapterPage() == 1, "The server rejected a valid adapter page");
        menu.setCarried(new ItemStack(Items.OAK_PLANKS, 32));
        for (int slot : new int[]{0, 1, 3, 4}) {
            PanScreenPayloads.apply(player,
                    new PanScreenPayloads.AdapterPattern(adapter.getBlockPos(), 1, slot, false));
        }
        PanScreenPayloads.apply(player, new PanScreenPayloads.AdapterPage(adapter.getBlockPos(), 2));
        require(menu.adapterPage() == 1, "The server accepted an out-of-range adapter page");
        ItemStack named = new ItemStack(Items.DIAMOND);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("component-bearing"));
        menu.setCarried(named);
        PanScreenPayloads.apply(player, new PanScreenPayloads.AdapterPattern(adapter.getBlockPos(), 1, 8, false));
        require(adapter.adapterPattern(1, 8).isEmpty(),
                "The server accepted a component-bearing PAN pattern");

        adapter.refreshAdapterResults();
        require(adapter.adapterResult(0, 0).is(Items.OAK_PLANKS)
                        && adapter.adapterResult(1, 0).is(Items.CRAFTING_TABLE),
                "Page-specific PAN patterns did not produce their selected conversion results");
        require(menu.slots.get(0).safeTake(1, 1, player).isEmpty()
                        && menu.slots.get(9).safeTake(1, 1, player).isEmpty(),
                "PAN pattern or result ghost slots were extractable");

        adapter.adapterSubItems().setStackInSlot(0, new ItemStack(Items.REDSTONE, 9));
        CompoundTag saved = adapter.saveWithFullMetadata(helper.getLevel().registryAccess());
        PanBlockEntity loaded = new PanBlockEntity(helper.absolutePos(adapterPos), adapter.getBlockState());
        loaded.loadWithComponents(saved, helper.getLevel().registryAccess());
        require(loaded.adapterPattern(0, 0).is(Items.OAK_LOG)
                        && loaded.adapterPattern(1, 4).is(Items.OAK_PLANKS)
                        && loaded.adapterSubItems().getStackInSlot(0).getCount() == 9,
                "PAN patterns or real sub-item inventory changed across save and load");
        var dropped = adapter.dropAdapterSubItems();
        require(dropped.getItem(0).getCount() == 9 && adapter.adapterSubItems().getStackInSlot(0).isEmpty(),
                "Breaking a PAN Adapter did not conserve and clear its real sub-item inventory");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void adapterShiftMoveUsesOnlyRealInventoryAndConservesItems(GameTestHelper helper) {
        BlockPos adapterPos = new BlockPos(3, 3, 3);
        helper.setBlock(adapterPos, PanRegistration.DEFINITIONS.get("pan_adapter_tier_10").block().get());
        helper.setBlock(adapterPos.east(), net.minecraft.world.level.block.Blocks.CRAFTING_TABLE);
        PanBlockEntity adapter = helper.getBlockEntity(adapterPos);
        require(adapter.setAdapterPattern(0, 0, new ItemStack(Items.OAK_LOG)),
                "The adapter rejected the shift-move test pattern");
        adapter.refreshAdapterResults();
        ItemStack patternBefore = adapter.adapterPattern(0, 0).copy();
        ItemStack resultBefore = adapter.adapterResult(0, 0).copy();
        require(resultBefore.is(Items.OAK_PLANKS),
                "The shift-move test did not establish a non-empty result ghost slot");

        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.moveTo(adapter.getBlockPos().getX() + 0.5D, adapter.getBlockPos().getY() + 0.5D,
                adapter.getBlockPos().getZ() + 0.5D);
        PanMenu menu = new PanMenu(18, player.getInventory(), adapter, new SimpleContainerData(12));
        player.containerMenu = menu;
        adapter.adapterSubItems().setStackInSlot(0, new ItemStack(Items.REDSTONE, 9));

        require(menu.quickMoveStack(player, 0).isEmpty()
                        && menu.quickMoveStack(player, PanBlockEntity.ADAPTER_PAGE_SIZE).isEmpty()
                        && ItemStack.isSameItemSameComponents(patternBefore, adapter.adapterPattern(0, 0))
                        && patternBefore.getCount() == adapter.adapterPattern(0, 0).getCount()
                        && ItemStack.isSameItemSameComponents(resultBefore, adapter.adapterResult(0, 0))
                        && resultBefore.getCount() == adapter.adapterResult(0, 0).getCount(),
                "Shift-moving changed or extracted a PAN Adapter ghost slot");

        ItemStack movedToPlayer = menu.quickMoveStack(player, PanBlockEntity.ADAPTER_PAGE_SIZE * 2);
        require(movedToPlayer.is(Items.REDSTONE) && movedToPlayer.getCount() == 9
                        && adapter.adapterSubItems().getStackInSlot(0).isEmpty()
                        && playerItemCount(player, Items.REDSTONE) == 9,
                "Shift-moving did not transfer the real adapter inventory to the player exactly once");
        int playerMenuSlot = -1;
        for (int index = menu.machineSlots(); index < menu.slots.size(); index++) {
            if (menu.slots.get(index).getItem().is(Items.REDSTONE)) {
                playerMenuSlot = index;
                break;
            }
        }
        require(playerMenuSlot >= menu.machineSlots(),
                "The shifted adapter stack was not present in a player menu slot");
        ItemStack movedToAdapter = menu.quickMoveStack(player, playerMenuSlot);
        require(movedToAdapter.is(Items.REDSTONE) && movedToAdapter.getCount() == 9
                        && playerItemCount(player, Items.REDSTONE) == 0
                        && adapter.adapterSubItems().getStackInSlot(0).is(Items.REDSTONE)
                        && adapter.adapterSubItems().getStackInSlot(0).getCount() == 9,
                "Shift-moving did not return the player stack to the real adapter inventory exactly once");
        require(ItemStack.isSameItemSameComponents(patternBefore, adapter.adapterPattern(0, 0))
                        && patternBefore.getCount() == adapter.adapterPattern(0, 0).getCount()
                        && ItemStack.isSameItemSameComponents(resultBefore, adapter.adapterResult(0, 0))
                        && resultBefore.getCount() == adapter.adapterResult(0, 0).getCount(),
                "Real-inventory shift movement changed a PAN Adapter ghost slot");
        helper.succeed();
    }

    private static int playerItemCount(net.minecraft.world.entity.player.Player player,
            net.minecraft.world.item.Item item) {
        int count = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static ResourceLocation id(net.minecraft.world.item.Item item) {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private PanGameTests() {
    }
}
