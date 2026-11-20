package net.claustra01.clayium.gametest.vanillacompat;

import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.datagen.tag.VanillaCompatTags;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.registry.MachineRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.WorkbenchRegistration;
import net.claustra01.clayium.workbench.ClayCraftingBoardMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class VanillaCompatGameTests {
    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void rawOresPreserveMetalBalanceAndVanillaDrops(GameTestHelper helper) {
        List<Metal> metals = List.of(
                new Metal("iron", Items.RAW_IRON, Blocks.RAW_IRON_BLOCK, Blocks.IRON_ORE,
                        Blocks.DEEPSLATE_IRON_ORE, Items.IRON_INGOT, material("iron_dust"), material("iron_ingot")),
                new Metal("gold", Items.RAW_GOLD, Blocks.RAW_GOLD_BLOCK, Blocks.GOLD_ORE,
                        Blocks.DEEPSLATE_GOLD_ORE, Items.GOLD_INGOT, material("gold_dust"), material("gold_ingot")),
                new Metal("copper", Items.RAW_COPPER, Blocks.RAW_COPPER_BLOCK, Blocks.COPPER_ORE,
                        Blocks.DEEPSLATE_COPPER_ORE, Items.COPPER_INGOT, material("copper_dust"), material("copper_ingot")));

        for (Metal metal : metals) {
            ClayiumMachineRecipe raw = machineRecipe(helper, "grinder/raw_" + metal.id());
            assertSingleInputOutput(raw, metal.raw(), metal.dust(), 1);
            process(helper, "grinder_tier_4", metal.raw(), metal.dust(), 1);
            ClayiumMachineRecipe rawBlock = machineRecipe(helper, "grinder/raw_" + metal.id() + "_block");
            assertSingleInputOutput(rawBlock, metal.rawBlock(), metal.dust(), 9);
            process(helper, "grinder_tier_4", metal.rawBlock(), metal.dust(), 9);
            ClayiumMachineRecipe ore = machineRecipe(helper, "grinder/" + metal.id() + "_ore");
            assertSingleInputOutput(ore, metal.stoneOre(), metal.dust(), 2);
            process(helper, "grinder_tier_4", metal.stoneOre(), metal.dust(), 2);
            process(helper, "grinder_tier_4", metal.deepslateOre(), metal.dust(), 2);
            require(ore.itemInputs().getFirst().ingredient().test(new ItemStack(metal.deepslateOre())),
                    "Deepslate " + metal.id() + " ore is not accepted by the ore recipe");
            process(helper, "smelter_tier_4", metal.raw(), metal.vanillaIngot(), 1);
            process(helper, "smelter_tier_4", metal.dust(), metal.clayiumIngot(), 1);

            var smelting = helper.getLevel().getRecipeManager()
                    .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(new ItemStack(metal.raw())), helper.getLevel())
                    .orElseThrow(() -> new GameTestAssertException("Raw " + metal.id() + " has no vanilla smelting recipe"));
            ItemStack vanillaResult = smelting.value().assemble(
                    new SingleRecipeInput(new ItemStack(metal.raw())), helper.getLevel().registryAccess());
            require(vanillaResult.is(metal.vanillaIngot()) && vanillaResult.getCount() == 1,
                    "Raw " + metal.id() + " is not one vanilla-ingot equivalent");

            var dustSmelting = helper.getLevel().getRecipeManager()
                    .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(new ItemStack(metal.dust())), helper.getLevel())
                    .orElseThrow(() -> new GameTestAssertException(metal.id() + " dust has no smelting recipe"));
            ItemStack dustResult = dustSmelting.value().assemble(
                    new SingleRecipeInput(new ItemStack(metal.dust())), helper.getLevel().registryAccess());
            require(dustResult.is(metal.clayiumIngot()) && dustResult.getCount() == 1,
                    "Grinding and smelting raw " + metal.id() + " changed its metal amount");
        }

        ItemStack silkTool = new ItemStack(Items.DIAMOND_PICKAXE);
        Holder<Enchantment> silkTouch = helper.getLevel().registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
        silkTool.enchant(silkTouch, 1);
        for (Metal metal : metals) {
            require(isSingleSelfDrop(helper, metal.stoneOre(), silkTool),
                    "Silk Touch no longer preserves stone " + metal.id() + " ore");
            require(isSingleSelfDrop(helper, metal.deepslateOre(), silkTool),
                    "Silk Touch no longer preserves deepslate " + metal.id() + " ore");
        }
        int copperDrops = 0;
        for (int attempt = 0; attempt < 32; attempt++) {
            copperDrops += Block.getDrops(
                            Blocks.COPPER_ORE.defaultBlockState(), helper.getLevel(),
                            helper.absolutePos(new BlockPos(attempt, 1, 0)), null, null,
                            new ItemStack(Items.DIAMOND_PICKAXE))
                    .stream().filter(stack -> stack.is(Items.RAW_COPPER)).mapToInt(ItemStack::getCount).sum();
        }
        require(copperDrops > 32, "Vanilla copper ore no longer keeps its multiple-raw-copper loot");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void modernWoodsUseTagsAndBambooIsNotASapling(GameTestHelper helper) {
        for (ItemLike log : List.of(Blocks.OAK_LOG, Blocks.CHERRY_LOG, Blocks.MANGROVE_LOG)) {
            require(new ItemStack(log).is(ItemTags.LOGS_THAT_BURN), log + " is not burnable log material");
            var charcoal = helper.getLevel().getRecipeManager().getRecipeFor(
                    RecipeType.SMELTING, new SingleRecipeInput(new ItemStack(log)), helper.getLevel())
                    .orElseThrow(() -> new GameTestAssertException(log + " has no charcoal smelting recipe"));
            require(charcoal.value().assemble(new SingleRecipeInput(new ItemStack(log)),
                    helper.getLevel().registryAccess()).is(Items.CHARCOAL),
                    log + " did not produce the carbon source used by Clayium");
        }
        ClayiumMachineRecipe silicon = machineRecipe(helper, "chemical_reactor/quartz_and_coal");
        require(silicon.itemInputs().get(1).ingredient().test(new ItemStack(Items.CHARCOAL)),
                "Clayium chemical processing rejected wood-derived charcoal");
        process(helper, "transformer_tier_8", Blocks.DARK_OAK_LOG, Blocks.CHERRY_LOG, 1);
        process(helper, "transformer_tier_8", Blocks.CHERRY_LOG, Blocks.MANGROVE_LOG, 1);
        process(helper, "transformer_tier_8", Blocks.DARK_OAK_LEAVES, Blocks.CHERRY_LEAVES, 1);
        process(helper, "transformer_tier_8", Blocks.MANGROVE_LEAVES, Blocks.AZALEA_LEAVES, 1);
        process(helper, "transformer_tier_8", Blocks.AZALEA_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES, 1);
        process(helper, "transformer_tier_8", Blocks.DARK_OAK_SAPLING, Blocks.CHERRY_SAPLING, 1);
        process(helper, "transformer_tier_8", Blocks.CHERRY_SAPLING, Blocks.MANGROVE_PROPAGULE, 1);
        process(helper, "transformer_tier_8", Blocks.MANGROVE_PROPAGULE, Blocks.AZALEA, 1);
        process(helper, "transformer_tier_8", Blocks.AZALEA, Blocks.FLOWERING_AZALEA, 1);

        BlockPos boardPosition = new BlockPos(1, 1, 1);
        helper.setBlock(boardPosition, WorkbenchRegistration.CLAY_CRAFTING_BOARD.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ClayCraftingBoardMenu board = new ClayCraftingBoardMenu(
                1, player.getInventory(), helper.getBlockEntity(boardPosition));
        for (int slot : List.of(1, 2, 3)) {
            board.slots.get(slot).set(new ItemStack(Items.CLAY_BALL));
        }
        for (int slot : List.of(4, 5, 6)) {
            board.slots.get(slot).set(new ItemStack(Items.BAMBOO_PLANKS));
        }
        require(board.slots.get(0).getItem().is(WorkbenchRegistration.CLAY_WORK_TABLE.get().asItem()),
                "Bamboo planks did not match the Clayium work table recipe");
        board.clicked(0, 0, ClickType.PICKUP, player);
        require(board.getCarried().is(WorkbenchRegistration.CLAY_WORK_TABLE.get().asItem()),
                "The board did not craft a work table from bamboo planks");

        for (ItemLike source : List.of(Blocks.OAK_SAPLING, Blocks.CHERRY_SAPLING,
                Blocks.MANGROVE_PROPAGULE, Blocks.AZALEA, Blocks.FLOWERING_AZALEA)) {
            require(new ItemStack(source).is(VanillaCompatTags.LASER_TREE_SOURCES),
                    source + " is absent from laser_tree_sources");
        }
        require(!new ItemStack(Items.BAMBOO).is(VanillaCompatTags.LASER_TREE_SOURCES),
                "Bamboo was treated as an ordinary tree sapling");
        require(new ItemStack(Items.BAMBOO_PLANKS).is(ItemTags.PLANKS),
                "Bamboo planks are not treated as planks");
        helper.succeed();
    }

    private static void process(GameTestHelper helper, String machineId, ItemLike input,
                                ItemLike output, int outputCount) {
        BlockPos position = new BlockPos(2, 1, 2);
        helper.setBlock(position, Blocks.AIR);
        helper.setBlock(position, MachineRegistration.MACHINES.get(machineId).block().get());
        ClayMachineBlockEntity machine = helper.getBlockEntity(position);
        machine.inventory().setStackInSlot(0, new ItemStack(input));
        machine.receiveClayEnergy(ClayEnergy.MAX);
        ClayMachineBlockEntity.serverTick(helper.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
        require(machine.activeRecipe() != null, machineId + " rejected " + input);
        for (int tick = 0; tick < 1000 && machine.activeRecipe() != null; tick++) {
            ClayMachineBlockEntity.serverTick(helper.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
        }
        ItemStack result = machine.inventory().getStackInSlot(machine.outputSlot());
        require(result.is(output.asItem()) && result.getCount() == outputCount,
                machineId + " produced the wrong result for " + input);
        require(machine.inventory().getStackInSlot(0).isEmpty()
                        && machine.inventory().getStackInSlot(machine.reservedSlot(0)).isEmpty(),
                machineId + " did not consume exactly one " + input);
    }

    private static ClayiumMachineRecipe machineRecipe(GameTestHelper helper, String id) {
        RecipeHolder<?> holder = helper.getLevel().getRecipeManager().byKey(ClayiumMod.id(id))
                .orElseThrow(() -> new GameTestAssertException("Missing recipe " + id));
        require(holder.value() instanceof ClayiumMachineRecipe, id + " is not a Clayium machine recipe");
        return (ClayiumMachineRecipe) holder.value();
    }

    private static void assertSingleInputOutput(
            ClayiumMachineRecipe recipe, ItemLike input, ItemLike output, int outputCount) {
        require(recipe.requiredTier() == 4
                        && recipe.itemInputs().size() == 1
                        && recipe.itemInputs().getFirst().count() == 1
                        && recipe.itemInputs().getFirst().ingredient().test(new ItemStack(input))
                        && recipe.itemOutputs().size() == 1
                        && recipe.itemOutputs().getFirst().is(output.asItem())
                        && recipe.itemOutputs().getFirst().getCount() == outputCount,
                "Wrong raw-metal balance in " + recipe);
    }

    private static boolean isSingleSelfDrop(GameTestHelper helper, Block block, ItemStack tool) {
        List<ItemStack> drops = Block.getDrops(
                block.defaultBlockState(), helper.getLevel(), helper.absolutePos(BlockPos.ZERO), null, null, tool);
        return drops.size() == 1 && drops.getFirst().is(block.asItem()) && drops.getFirst().getCount() == 1;
    }

    private static Item material(String id) {
        return MaterialRegistration.MATERIAL_ITEMS.get(id).get();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private record Metal(
            String id,
            Item raw,
            Block rawBlock,
            Block stoneOre,
            Block deepslateOre,
            Item vanillaIngot,
            Item dust,
            Item clayiumIngot) {
    }

    private VanillaCompatGameTests() {
    }
}
