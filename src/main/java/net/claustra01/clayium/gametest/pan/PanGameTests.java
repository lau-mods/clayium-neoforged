package net.claustra01.clayium.gametest.pan;

import java.util.List;
import java.util.Map;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.pan.PanBlockEntity;
import net.claustra01.clayium.pan.PanNetwork;
import net.claustra01.clayium.pan.PanRecipeClosure;
import net.claustra01.clayium.recipe.ClayiumRecipesReloadedEvent;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.PanRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
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
        var manager = helper.getLevel().getRecipeManager();
        List<RecipeHolder<?>> original = List.copyOf(manager.getRecipes());
        helper.runAfterDelay(100, () -> {
            require(core.closure().costs().containsKey(id(Items.OAK_PLANKS)),
                    "Connected crafting table did not make planks reachable from the base log");
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
        helper.setBlock(new BlockPos(14, 3, 5), net.minecraft.world.level.block.Blocks.CRAFTING_TABLE);
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
