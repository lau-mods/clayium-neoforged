package net.claustra01.clayium.gametest.claytree;

import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.laser.LaserBeam;
import net.claustra01.clayium.machine.iface.InterfaceCapabilities;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.ClayTreeRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.world.claytree.ClayTreeLaserConversion;
import net.claustra01.clayium.world.claytree.ClayTreeSaplingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.MangrovePropaguleBlock;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ClayTreeGameTests {
    @GameTest(template = "empty", batch = "clayium_clay_tree_threshold")
    public static void laserThresholdTagsAndNaturalGenerationMatchClayTreeRules(GameTestHelper helper) {
        LaserBeam weakBeam = new LaserBeam(3, 3, 3);
        LaserBeam strongBeam = new LaserBeam(4, 4, 4);
        require(weakBeam.energy() < ClayTreeLaserConversion.REQUIRED_ENERGY
                        && strongBeam.energy() >= ClayTreeLaserConversion.REQUIRED_ENERGY,
                "The test beams do not bracket the Clay Tree threshold");
        var sourceTag = helper.getLevel().registryAccess().registryOrThrow(Registries.BLOCK)
                .getTagOrEmpty(ClayTreeLaserConversion.LASER_TREE_SOURCES);
        int sourceCount = 0;
        for (var source : sourceTag) {
            sourceCount++;
            BlockPos position = new BlockPos(1, 2, 1);
            helper.setBlock(position.below(), Blocks.DIRT);
            helper.setBlock(position, source.value());
            require(helper.getBlockState(position).is(source.value()),
                    source.value() + " could not be prepared for irradiation");
            var receiver = helper.getLevel().getCapability(
                    InterfaceCapabilities.LASER_RECEIVER, helper.absolutePos(position), null);
            require(receiver != null && receiver.receiveLaser(strongBeam),
                    "A tagged source rejected the strong laser beam");
            require(helper.getBlockState(position).is(ClayTreeRegistration.CLAY_TREE_SAPLING.get()),
                    source.value() + " did not become a Clay Tree Sapling above 1000 energy");
        }
        require(sourceCount >= 10, "The vanilla Clay Tree source tag is incomplete");

        BlockPos weak = new BlockPos(1, 2, 3);
        helper.setBlock(weak.below(), Blocks.DIRT);
        helper.setBlock(weak, Blocks.OAK_SAPLING);
        var weakReceiver = helper.getLevel().getCapability(
                InterfaceCapabilities.LASER_RECEIVER, helper.absolutePos(weak), null);
        require(weakReceiver != null && weakReceiver.receiveLaser(weakBeam),
                "An eligible source did not accept sub-threshold irradiation");
        require(helper.getBlockState(weak).is(Blocks.OAK_SAPLING),
                "A sub-threshold laser beam converted a source sapling");

        BlockPos invalid = new BlockPos(2, 2, 3);
        helper.setBlock(invalid, Blocks.BAMBOO);
        require(helper.getLevel().getCapability(
                        InterfaceCapabilities.LASER_RECEIVER, helper.absolutePos(invalid), null) == null,
                "An ineligible block exposed a Clay Tree laser receiver");
        require(helper.getBlockState(invalid).is(Blocks.BAMBOO),
                "An ineligible block was replaced by a Clay Tree Sapling");
        require(helper.getLevel().getCapability(
                        InterfaceCapabilities.LASER_RECEIVER, helper.absolutePos(new BlockPos(1, 2, 1)), null) == null,
                "Clay Tree saplings must not receive conversion beams");

        BlockPos hanging = new BlockPos(3, 2, 3);
        helper.setBlock(hanging.above(), Blocks.MANGROVE_LEAVES);
        helper.setBlock(hanging, Blocks.MANGROVE_PROPAGULE.defaultBlockState()
                .setValue(MangrovePropaguleBlock.HANGING, true));
        var hangingReceiver = helper.getLevel().getCapability(
                InterfaceCapabilities.LASER_RECEIVER, helper.absolutePos(hanging), null);
        require(hangingReceiver != null && !hangingReceiver.receiveLaser(strongBeam),
                "A hanging propagule was accepted at an invalid sapling position");
        require(helper.getBlockState(hanging).is(Blocks.MANGROVE_PROPAGULE),
                "A hanging propagule was replaced by an unsupported sapling");

        var access = helper.getLevel().registryAccess();
        require(access.registryOrThrow(Registries.CONFIGURED_FEATURE).containsKey(ClayTreeSaplingBlock.CLAY_TREE.location()),
                "The planted Clay Tree configured feature is missing");
        var clayTree = access.registryOrThrow(Registries.CONFIGURED_FEATURE)
                .getOrThrow(ClayTreeSaplingBlock.CLAY_TREE);
        for (var placed : access.registryOrThrow(Registries.PLACED_FEATURE)) {
            require(!placesClayTree(placed, clayTree), "A placed feature generates Clay Trees");
        }
        for (var biome : access.registryOrThrow(Registries.BIOME)) {
            for (var step : biome.getGenerationSettings().features()) {
                for (Holder<PlacedFeature> placed : step) {
                    require(!placesClayTree(placed.value(), clayTree), "A biome generates Clay Trees");
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "clayium_clay_tree_growth", timeoutTicks = 200)
    public static void plantedTreeGrowthDropsAndOriginalProcessingRouteAreComplete(GameTestHelper helper) {
        BlockPos saplingPosition = new BlockPos(2, 66, 2);
        helper.setBlock(saplingPosition.below(), Blocks.DIRT);
        helper.setBlock(
                saplingPosition,
                ClayTreeRegistration.CLAY_TREE_SAPLING.get().defaultBlockState().setValue(SaplingBlock.STAGE, 1));
        ClayTreeSaplingBlock sapling = ClayTreeRegistration.CLAY_TREE_SAPLING.get();
        sapling.advanceTree(
                helper.getLevel(), helper.absolutePos(saplingPosition), helper.getBlockState(saplingPosition),
                RandomSource.create(32L));

        int height = 0;
        while (helper.getBlockState(saplingPosition.above(height)).is(ClayTreeRegistration.CLAY_TREE_LOG.get())) {
            height++;
        }
        require(height >= 5 && height <= 7, "The Clay Tree trunk height is outside 5-7 blocks: " + height);
        require(!helper.getBlockState(saplingPosition).is(ClayTreeRegistration.CLAY_TREE_SAPLING.get()),
                "The Clay Tree sapling remained after growth");
        int logs = 0;
        int leaves = 0;
        for (BlockPos position : BlockPos.betweenClosed(
                saplingPosition.offset(-3, 0, -3), saplingPosition.offset(3, 10, 3))) {
            if (helper.getBlockState(position).is(ClayTreeRegistration.CLAY_TREE_LOG.get())) {
                require(position.getX() == saplingPosition.getX()
                                && position.getZ() == saplingPosition.getZ()
                                && position.getY() >= saplingPosition.getY()
                                && position.getY() < saplingPosition.getY() + height,
                        "The Clay Tree trunk is not one straight contiguous column");
                logs++;
            } else if (helper.getBlockState(position).is(ClayTreeRegistration.CLAY_TREE_LEAF.get())) {
                leaves++;
            }
        }
        require(logs == height, "The Clay Tree generated extra or missing logs");
        require(leaves > 20, "The original compact leaf crown was not generated");
        int[] radii = {1, 1, 2, 2};
        for (int layer = 0; layer < radii.length; layer++) {
            BlockPos center = saplingPosition.above(height - layer);
            int radius = radii[layer];
            for (int distance = 1; distance <= radius; distance++) {
                for (BlockPos leaf : List.of(
                        center.offset(distance, 0, 0), center.offset(-distance, 0, 0),
                        center.offset(0, 0, distance), center.offset(0, 0, -distance))) {
                    require(helper.getBlockState(leaf).is(ClayTreeRegistration.CLAY_TREE_LEAF.get()),
                            "The Clay Tree crown is missing a leaf at " + leaf);
                }
            }
            if (layer < 2) {
                require(!helper.getBlockState(center.offset(2, 0, 0)).is(ClayTreeRegistration.CLAY_TREE_LEAF.get()),
                        "The Clay Tree upper leaf layer is wider than the original crown");
            }
        }

        List<ItemStack> logDrops = Block.getDrops(
                ClayTreeRegistration.CLAY_TREE_LOG.get().defaultBlockState(),
                helper.getLevel(), helper.absolutePos(saplingPosition), null, null, ItemStack.EMPTY);
        require(logDrops.size() == 1 && logDrops.getFirst().is(ClayTreeRegistration.CLAY_TREE_LOG_ITEM.get()),
                "Felling a Clay Tree log did not recover the log");
        List<ItemStack> shearedLeaves = Block.getDrops(
                ClayTreeRegistration.CLAY_TREE_LEAF.get().defaultBlockState(),
                helper.getLevel(), helper.absolutePos(saplingPosition), null, null, new ItemStack(Items.SHEARS));
        require(shearedLeaves.size() == 1 && shearedLeaves.getFirst().is(ClayTreeRegistration.CLAY_TREE_LEAF_ITEM.get()),
                "Shears did not preserve Clay Tree leaves");
        for (int attempt = 0; attempt < 256; attempt++) {
            List<ItemStack> drops = Block.getDrops(
                    ClayTreeRegistration.CLAY_TREE_LEAF.get().defaultBlockState(),
                    helper.getLevel(), helper.absolutePos(saplingPosition.offset(attempt, 0, 0)),
                    null, null, ItemStack.EMPTY);
            require(drops.stream().noneMatch(stack -> stack.is(ClayTreeRegistration.CLAY_TREE_SAPLING_ITEM.get())),
                    "Clay Tree leaves produced a sapling instead of the original clay dust drops");
            require(drops.stream().allMatch(stack -> stack.is(MaterialRegistration.MATERIAL_ITEMS.get("clay_dust").get())
                            || stack.is(MaterialRegistration.MATERIAL_ITEMS.get("dense_clay_dust").get())),
                    "Clay Tree leaves produced a non-original resource");
        }

        ClayiumMachineRecipe grinder = recipe(helper, "grinder/clay_tree_log");
        require(grinder.processingMethod() == ClayiumRecipeMethod.GRINDER
                        && grinder.requiredTier() == 5
                        && grinder.processingTicks() == 200
                        && grinder.clayEnergy() == 1_000L
                        && grinder.itemOutputs().size() == 1
                        && grinder.itemOutputs().getFirst().is(MaterialRegistration.MATERIAL_ITEMS.get("organic_clay").get()),
                "The original organic clay grinding route is incomplete");
        ClayiumMachineRecipe centrifuge = recipe(helper, "centrifuge/clay_tree_log");
        int[] amounts = {16, 5, 3, 1};
        String[] outputs = {"advanced_industrial_clay_dust", "manganese_dust", "lithium_dust", "zirconium_dust"};
        require(centrifuge.processingMethod() == ClayiumRecipeMethod.CENTRIFUGE
                        && centrifuge.requiredTier() == 6
                        && centrifuge.processingTicks() == 400
                        && centrifuge.clayEnergy() == 10_000L
                        && centrifuge.itemOutputs().size() == 4,
                "The original Clay Tree centrifuge recipe settings are incomplete");
        for (int index = 0; index < outputs.length; index++) {
            ItemStack output = centrifuge.itemOutputs().get(index);
            require(output.is(MaterialRegistration.MATERIAL_ITEMS.get(outputs[index]).get())
                            && output.getCount() == amounts[index],
                    "The Clay Tree centrifuge lost the " + outputs[index] + " output");
        }
        helper.succeed();
    }

    private static ClayiumMachineRecipe recipe(GameTestHelper helper, String id) {
        return helper.getLevel().getRecipeManager().byKey(ClayiumMod.id(id))
                .map(RecipeHolder::value)
                .filter(ClayiumMachineRecipe.class::isInstance)
                .map(ClayiumMachineRecipe.class::cast)
                .orElseThrow(() -> new GameTestAssertException("Missing Clay Tree recipe " + id));
    }

    private static boolean placesClayTree(PlacedFeature placed, ConfiguredFeature<?, ?> clayTree) {
        return placed.getFeatures().anyMatch(feature -> {
            if (feature == clayTree) {
                return true;
            }
            if (feature.config() instanceof TreeConfiguration tree) {
                return tree.trunkProvider.getState(RandomSource.create(), BlockPos.ZERO)
                                .is(ClayTreeRegistration.CLAY_TREE_LOG.get())
                        || tree.foliageProvider.getState(RandomSource.create(), BlockPos.ZERO)
                                .is(ClayTreeRegistration.CLAY_TREE_LEAF.get());
            }
            return false;
        });
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private ClayTreeGameTests() {
    }
}
