package net.claustra01.clayium.gametest.environment;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.claustra01.clayium.machine.environment.CobblestoneGeneratorBlockEntity;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.registry.EnvironmentMachineRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class EnvironmentMachineGameTests {
    private static final BlockPos GENERATOR_POS = new BlockPos(2, 2, 2);
    private static final BlockPos EXTRACTOR_POS = new BlockPos(6, 2, 2);

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void environmentMachinesDistinguishWaterLavaSourcesAndAir(GameTestHelper helper) {
        CobblestoneGeneratorBlockEntity generator = placeGenerator(helper);
        helper.setBlock(GENERATOR_POS.east(), Blocks.WATER);
        helper.setBlock(GENERATOR_POS.west(), Blocks.LAVA);
        runGenerator(generator, generator.intervalTicks());
        require(generator.output().getStackInSlot(0).is(Items.COBBLESTONE)
                        && generator.output().getStackInSlot(0).getCount() == 1,
                "Horizontal water and lava did not produce one cobblestone");

        generator.output().setStackInSlot(0, ItemStack.EMPTY);
        runGenerator(generator, generator.intervalTicks() / 2);
        helper.setBlock(GENERATOR_POS.east(), Blocks.AIR);
        runGenerator(generator, generator.intervalTicks() * 2);
        require(generator.output().getStackInSlot(0).isEmpty(),
                "The cobblestone generator ran with lava and air");
        helper.setBlock(GENERATOR_POS.east(), Blocks.WATER);
        runGenerator(generator, generator.intervalTicks() / 2);
        require(generator.output().getStackInSlot(0).is(Items.COBBLESTONE),
                "The cobblestone generator discarded paused progress");

        generator.output().setStackInSlot(0, ItemStack.EMPTY);
        helper.setBlock(GENERATOR_POS.west(), Blocks.AIR);
        runGenerator(generator, generator.intervalTicks() * 2);
        require(generator.output().getStackInSlot(0).isEmpty(),
                "The cobblestone generator ran with water and air");
        helper.setBlock(GENERATOR_POS.above(), Blocks.LAVA);
        runGenerator(generator, generator.intervalTicks() * 2);
        require(generator.output().getStackInSlot(0).isEmpty(),
                "A vertical lava source incorrectly satisfied the original horizontal layout");

        ClayMachineBlockEntity extractor = placeExtractor(helper);
        ClayiumMachineRecipe recipe = saltRecipe(helper);
        require(recipe.environment().requiresAdjacentWaterSource()
                        && recipe.itemInputs().isEmpty()
                        && recipe.itemOutputs().getFirst()
                                .is(MaterialRegistration.MATERIAL_ITEMS.get("salt").get()),
                "Salt extraction is not represented by a source-water environmental recipe");
        extractor.receiveClayEnergy(ClayEnergy.ofInternal(10_000L));
        long dryEnergy = extractor.energyStorage().stored().internalValue();
        runExtractor(extractor, 50);
        require(extractor.inventory().getStackInSlot(extractor.outputSlot()).isEmpty()
                        && extractor.energyStorage().stored().internalValue() == dryEnergy,
                "A dry salt extractor produced salt or consumed CE");

        helper.setBlock(EXTRACTOR_POS.east(), Fluids.FLOWING_WATER.getFlowing(7, false).createLegacyBlock());
        runExtractor(extractor, 50);
        require(extractor.inventory().getStackInSlot(extractor.outputSlot()).isEmpty()
                        && extractor.energyStorage().stored().internalValue() == dryEnergy,
                "Flowing water incorrectly satisfied the salt extractor's source condition");

        helper.setBlock(EXTRACTOR_POS.east(), Blocks.LAVA);
        runExtractor(extractor, 50);
        require(extractor.inventory().getStackInSlot(extractor.outputSlot()).isEmpty()
                        && extractor.energyStorage().stored().internalValue() == dryEnergy,
                "Lava incorrectly produced salt or consumed CE");

        helper.setBlock(EXTRACTOR_POS.east(), Blocks.WATER);
        runExtractor(extractor, 21);
        require(extractor.inventory().getStackInSlot(extractor.outputSlot())
                        .is(recipe.itemOutputs().getFirst().getItem()),
                "A source-water salt extractor did not produce salt");
        require(extractor.energyStorage().stored().internalValue() == dryEnergy - 2_000L,
                "Salt extraction did not consume the recipe's exact 20 x 100 internal CE");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void waterloggedFluidStatesDriveEnvironmentMachines(GameTestHelper helper) {
        var waterlogged = Blocks.OAK_FENCE.defaultBlockState()
                .setValue(BlockStateProperties.WATERLOGGED, true);

        CobblestoneGeneratorBlockEntity generator = placeGenerator(helper);
        helper.setBlock(GENERATOR_POS.east(), waterlogged);
        helper.setBlock(GENERATOR_POS.west(), Blocks.LAVA);
        runGenerator(generator, generator.intervalTicks());
        require(generator.output().getStackInSlot(0).is(Items.COBBLESTONE),
                "A waterlogged block was excluded from cobblestone generation");

        ClayMachineBlockEntity extractor = placeExtractor(helper);
        helper.setBlock(EXTRACTOR_POS.east(), waterlogged);
        extractor.receiveClayEnergy(ClayEnergy.ofInternal(10_000L));
        runExtractor(extractor, 21);
        require(extractor.inventory().getStackInSlot(extractor.outputSlot()).is(saltRecipe(helper)
                        .itemOutputs().getFirst().getItem()),
                "A waterlogged source was excluded from salt extraction");
        require(extractor.energyStorage().stored().internalValue() == 8_000L,
                "Waterlogged-source salt extraction consumed the wrong CE amount");
        helper.succeed();
    }

    private static CobblestoneGeneratorBlockEntity placeGenerator(GameTestHelper helper) {
        var definition = EnvironmentMachineRegistration.COBBLESTONE_GENERATORS.getFirst();
        helper.setBlock(GENERATOR_POS, definition.block().get());
        var blockEntity = helper.getBlockEntity(GENERATOR_POS);
        require(blockEntity instanceof CobblestoneGeneratorBlockEntity,
                "Clay cobblestone generator did not create its block entity");
        return (CobblestoneGeneratorBlockEntity)blockEntity;
    }

    private static ClayMachineBlockEntity placeExtractor(GameTestHelper helper) {
        var definition = EnvironmentMachineRegistration.SALT_EXTRACTORS.get("salt_extractor_tier_4");
        require(definition != null, "Missing Tier 4 salt extractor");
        helper.setBlock(EXTRACTOR_POS, definition.block().get());
        var blockEntity = helper.getBlockEntity(EXTRACTOR_POS);
        require(blockEntity instanceof ClayMachineBlockEntity,
                "Salt extractor did not use the shared processing-machine block entity");
        return (ClayMachineBlockEntity)blockEntity;
    }

    private static ClayiumMachineRecipe saltRecipe(GameTestHelper helper) {
        return helper.getLevel().getRecipeManager().byKey(ClayiumMod.id("salt_extractor/salt"))
                .filter(holder -> holder.value() instanceof ClayiumMachineRecipe)
                .map(holder -> (ClayiumMachineRecipe)holder.value())
                .orElseThrow(() -> new GameTestAssertException("Missing salt extraction recipe"));
    }

    private static void runGenerator(CobblestoneGeneratorBlockEntity generator, int ticks) {
        for (int tick = 0; tick < ticks; tick++) {
            generator.tickServer();
        }
    }

    private static void runExtractor(ClayMachineBlockEntity extractor, int ticks) {
        RandomSource random = RandomSource.create(315L);
        for (int tick = 0; tick < ticks; tick++) {
            extractor.tickServer(random);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private EnvironmentMachineGameTests() {
    }
}
