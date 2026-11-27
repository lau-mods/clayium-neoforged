package mods.clayium.util.crafting;

import cpw.mods.fml.common.registry.GameRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import mods.clayium.block.CBlocks;
import mods.clayium.block.ClayRFGenerator;
import mods.clayium.block.MetalChest;
import mods.clayium.block.StorageContainer;
import mods.clayium.block.tile.TileChemicalMetalSeparator;
import mods.clayium.core.ClayiumCore;
import mods.clayium.item.CItems;
import mods.clayium.item.CMaterial;
import mods.clayium.item.CMaterials;
import mods.clayium.item.CShape;
import mods.clayium.item.ItemCapsule;
import mods.clayium.util.UtilItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class CRecipes {
    public static SimpleMachinesRecipes recipeBendingMachine;
    public static SimpleMachinesRecipes recipeWireDrawingMachine;
    public static SimpleMachinesRecipes recipePipeDrawingMachine;
    public static SimpleMachinesRecipes recipeCuttingMachine;
    public static SimpleMachinesRecipes recipeLathe;
    public static SimpleMachinesRecipes recipeMillingMachine;
    public static SimpleMachinesRecipes recipeCondenser;
    public static SimpleMachinesRecipes recipeGrinder;
    public static SimpleMachinesRecipes recipeDecomposer;
    public static Recipes recipeAssembler;
    public static Recipes recipeInscriber;
    public static Recipes recipeCentrifuge;
    public static SimpleMachinesRecipes recipeEnergeticClayCondenser;
    public static Recipes recipeChemicalReactor;
    public static Recipes recipeAlloySmelter;
    public static Recipes recipeBlastFurnace;
    public static SimpleMachinesRecipes recipeElectrolysisReactor;
    public static Recipes recipeReactor;
    public static SimpleMachinesRecipes recipeTransformer;
    public static SimpleMachinesRecipes recipeCACondenser;
    public static Recipes recipeCAInjector;
    public static SimpleMachinesRecipes recipeCAReactor;
    public static SimpleMachinesRecipes recipeSmelter;
    public static SimpleMachinesRecipes recipeEnergeticClayDecomposer;
    public static Recipes recipeFluidTransferMachine;
    private static CMaterial[] cmats;
    private static CMaterial[] mats;
    private static ItemStack[] circuits;
    private static ItemStack[] machines;

    public CRecipes() {
        super();
    }

    public static ItemStack i(Block block) {
        return new ItemStack(block);
    }

    public static ItemStack i(Block block, int stacksize) {
        return new ItemStack(block, stacksize);
    }

    public static ItemStack i(Block block, int stacksize, int meta) {
        return new ItemStack(block, stacksize, meta);
    }

    public static ItemStack i(Item item) {
        return new ItemStack(item);
    }

    public static ItemStack i(Item item, int stacksize) {
        return new ItemStack(item, stacksize);
    }

    public static ItemStack i(Item item, int stacksize, int meta) {
        return new ItemStack(item, stacksize, meta);
    }

    public static ItemStack s(ItemStack itemstack, int stacksize) {
        ItemStack res = itemstack.func_77946_l();
        res.field_77994_a = stacksize;
        return res;
    }

    public static Object[] oo(Object... a) {
        return a;
    }

    public static ItemStack[] ii(ItemStack... a) {
        return a;
    }

    public static List<ItemStack> li(ItemStack... a) {
        return new ArrayList<>(Arrays.asList(ii(a)));
    }

    public static long e(int tier) {
        return e(1.0, tier);
    }

    public static long e(double factor, int tier) {
        return (long)(factor * 100.0 * Math.pow(10.0, (double)(tier - 4)));
    }

    public static void registerODChainRecipes(SimpleMachinesRecipes recipe, CMaterial[] materials, CShape shape, int[] stackSizes, int[] tiers, int time) {
        int matPos = 0;
        long energy = 0L;

        for (int i = 1; i < materials.length; i++) {
            energy += e(tiers[i]);
            if (CMaterials.existOD(materials[i], shape)) {
                int x = stackSizes[matPos];
                int y = stackSizes[i];
                if (x < y) {
                    int tmp = y;
                    y = x;
                    x = tmp;
                }

                int tmp;
                while ((tmp = x % y) != 0) {
                    x = y;
                    y = tmp;
                }

                recipe.addRecipe(
                    CMaterials.getOD(materials[matPos], shape, stackSizes[matPos] / y),
                    tiers[i],
                    CMaterials.getODExist(materials[i], shape, stackSizes[i] / y),
                    energy,
                    (long)time
                );
                matPos = i;
                energy = 0L;
            }
        }
    }

    public static void registerODChainRecipes(SimpleMachinesRecipes recipe, CMaterial[] materials, CShape shape, int[] tiers, int time) {
        int[] stackSizes = new int[materials.length];

        for (int i = 0; i < stackSizes.length; i++) {
            stackSizes[i] = 1;
        }

        registerODChainRecipes(recipe, materials, shape, stackSizes, tiers, time);
    }

    public static void registerStackChainRecipes(SimpleMachinesRecipes recipe, ItemStack[] itemstacks, int[] stackSizes, int[] tiers, int time) {
        int matPos = 0;
        long energy = 0L;

        for (int i = 1; i < itemstacks.length; i++) {
            energy += e(tiers[i]);
            if (itemstacks[i] != null) {
                int x = stackSizes[matPos];
                int y = stackSizes[i];
                if (x < y) {
                    int tmp = y;
                    y = x;
                    x = tmp;
                }

                int tmp;
                while ((tmp = x % y) != 0) {
                    x = y;
                    y = tmp;
                }

                recipe.addRecipe(s(itemstacks[matPos], stackSizes[matPos] / y), tiers[i], s(itemstacks[i], stackSizes[i] / y), energy, (long)time);
                matPos = i;
                energy = 0L;
            }
        }
    }

    public static void registerStackChainRecipes(SimpleMachinesRecipes recipe, ItemStack[] itemstacks, int[] tiers, int time) {
        int[] stackSizes = new int[itemstacks.length];

        for (int i = 0; i < stackSizes.length; i++) {
            stackSizes[i] = 1;
        }

        registerStackChainRecipes(recipe, itemstacks, stackSizes, tiers, time);
    }

    public static void register1to1Recipe(Recipes recipe, Object material, int tier, ItemStack result, long time) {
        if (result != null) {
            recipe.addRecipe(oo(material), 0, tier, ii(result), e(tier), time, true);
        }
    }

    public static void register2to1Recipe(Recipes recipe, Object material1, Object material2, int tier, ItemStack result, long time) {
        if (result != null) {
            recipe.addRecipe(oo(material1, material2), 0, tier, ii(result), e(tier), time, true);
        }
    }

    public static void initRecipes() {
        recipeBendingMachine = new SimpleMachinesRecipes("BendingMachine");
        recipeWireDrawingMachine = new SimpleMachinesRecipes("WireDrawingMachine");
        recipePipeDrawingMachine = new SimpleMachinesRecipes("PipeDrawingMachine");
        recipeCuttingMachine = new SimpleMachinesRecipes("CuttingMachine");
        recipeLathe = new SimpleMachinesRecipes("Lathe");
        recipeMillingMachine = new SimpleMachinesRecipes("MillingMachine");
        recipeCondenser = new SimpleMachinesRecipes("Condenser");
        recipeGrinder = new SimpleMachinesRecipes("Grinder");
        recipeDecomposer = new SimpleMachinesRecipes("Decomposer");
        recipeEnergeticClayCondenser = new SimpleMachinesRecipes("ECCondenser");
        recipeAssembler = new Recipes("Assembler");
        recipeInscriber = new Recipes("Inscriber");
        recipeCentrifuge = new Recipes("Centrifuge");
        recipeSmelter = new SmeltingRecipe("Smelter");
        recipeChemicalReactor = new Recipes("ChemicalReactor");
        recipeAlloySmelter = new Recipes("AlloySmelter");
        recipeBlastFurnace = new Recipes("BlastFurnace");
        recipeElectrolysisReactor = new SimpleMachinesRecipes("ElectrolysisReactor");
        recipeReactor = new Recipes("Reactor");
        recipeTransformer = new SimpleMachinesRecipes("MatterTransformer");
        recipeCACondenser = new SimpleMachinesRecipes("CACondenser");
        recipeCAInjector = new SimpleMachinesRecipes("CAInjector");
        recipeCAReactor = new SimpleMachinesRecipes("CAReactor");
        recipeEnergeticClayDecomposer = new SimpleMachinesRecipes("ECDecomposer");
        recipeFluidTransferMachine = new Recipes("FluidTransferMachine");
    }

    public static void registerRecipes() {
        if (!ClayiumCore.cfgUtilityMode) {
            cmats = new CMaterial[]{CMaterials.CLAY, CMaterials.CLAY, CMaterials.DENSE_CLAY, CMaterials.DENSE_CLAY, CMaterials.DENSE_CLAY};
            mats = new CMaterial[]{
                CMaterials.CLAY,
                CMaterials.CLAY,
                CMaterials.DENSE_CLAY,
                CMaterials.IND_CLAY,
                CMaterials.ADVIND_CLAY,
                CMaterials.IMPURE_SILICON,
                CMaterials.MAIN_ALUMINIUM,
                CMaterials.CLAY_STEEL,
                CMaterials.CLAYIUM,
                CMaterials.ULTIMATE_ALLOY,
                CMaterials.ANTIMATTER,
                CMaterials.PURE_ANTIMATTER,
                CMaterials.OCTUPLE_CLAY,
                CMaterials.OCTUPLE_PURE_ANTIMATTER
            };
            circuits = new ItemStack[]{
                CItems.itemMisc.get("ClayCircuit"),
                CItems.itemMisc.get("ClayCircuit"),
                CItems.itemMisc.get("ClayCircuit"),
                CItems.itemMisc.get("SimpleCircuit"),
                CItems.itemMisc.get("BasicCircuit"),
                CItems.itemMisc.get("AdvancedCircuit"),
                CItems.itemMisc.get("PrecisionCircuit"),
                CItems.itemMisc.get("IntegratedCircuit"),
                CItems.itemMisc.get("ClayCore"),
                CItems.itemMisc.get("ClayBrain"),
                CItems.itemMisc.get("ClaySpirit"),
                CItems.itemMisc.get("ClaySoul"),
                CItems.itemMisc.get("ClayAnima"),
                CItems.itemMisc.get("ClayPsyche")
            };
            machines = new ItemStack[16];
            machines[0] = new ItemStack(CBlocks.blockRawClayMachineHull, 1, 0);

            for (int i = 1; i < 16; i++) {
                machines[i] = new ItemStack(CBlocks.blockMachineHull, 1, i - 1);
            }

            registerMainMaterialRecipes();
            registerHullRecipes();
            registerMachineRecipes();
            registerMaterialRecipes();
            registerToolRecipes();
        } else {
            GameRegistry.addRecipe(i(CBlocks.blockCompressedClay, 1, 0), new Object[]{"###", "###", "###", '#', Blocks.field_150435_aG});
            GameRegistry.addShapelessRecipe(i(Blocks.field_150435_aG, 9), new Object[]{i(CBlocks.blockCompressedClay, 1, 0)});

            for (int i = 0; i <= 11; i++) {
                GameRegistry.addRecipe(i(CBlocks.blockCompressedClay, 1, i + 1), new Object[]{"###", "###", "###", '#', i(CBlocks.blockCompressedClay, 1, i)});
                GameRegistry.addShapelessRecipe(i(CBlocks.blockCompressedClay, 9, i), new Object[]{i(CBlocks.blockCompressedClay, 1, i + 1)});
            }

            GameRegistry.addShapelessRecipe(
                CMaterials.get(CMaterials.CLAY_STEEL, CMaterials.INGOT),
                new Object[]{i(CBlocks.blockCompressedClay, 1, 0), i(CBlocks.blockCompressedClay, 1, 0), i(Items.field_151042_j)}
            );
            GameRegistry.addRecipe(
                i(CBlocks.blockClayChunkLoader), new Object[]{"###", "# #", "###", '#', CMaterials.get(CMaterials.CLAY_STEEL, CMaterials.INGOT)}
            );
            GameRegistry.addRecipe(
                i(CItems.itemClaySteelPickaxe),
                new Object[]{"###", " | ", " | ", '#', CMaterials.get(CMaterials.CLAY_STEEL, CMaterials.INGOT), '|', Items.field_151055_y}
            );
            GameRegistry.addRecipe(
                i(CItems.itemClaySteelShovel),
                new Object[]{"#", "|", "|", '#', CMaterials.get(CMaterials.CLAY_STEEL, CMaterials.INGOT), '|', Items.field_151055_y}
            );
        }
    }

    public static void registerToolRecipes() {
        GameRegistry.addRecipe(
            CItems.itemRawClayCraftingTools.get("RollingPin"),
            new Object[]{"-=-", '-', CMaterials.get(CMaterials.CLAY, CMaterials.SHORT_STICK), '=', CMaterials.get(CMaterials.CLAY, CMaterials.CYLINDER)}
        );
        GameRegistry.addRecipe(
            CItems.itemRawClayCraftingTools.get("Spatula"),
            new Object[]{"-=", '-', CMaterials.get(CMaterials.CLAY, CMaterials.SHORT_STICK), '=', CMaterials.get(CMaterials.CLAY, CMaterials.BLADE)}
        );
        GameRegistry.addSmelting(CItems.itemRawClayCraftingTools.get("RollingPin"), i(CItems.itemClayRollingPin), 0.1F);
        GameRegistry.addSmelting(CItems.itemRawClayCraftingTools.get("Slicer"), i(CItems.itemClaySlicer), 0.1F);
        GameRegistry.addSmelting(CItems.itemRawClayCraftingTools.get("Spatula"), i(CItems.itemClaySpatula), 0.1F);
        recipeAssembler.addRecipe(
            oo(i(CItems.itemClayRollingPin, 1, 32767), i(CItems.itemClaySlicer, 1, 32767)), 0, 6, ii(CItems.itemClayPipingTools.get("IO")), e(6), 20L
        );
        recipeAssembler.addRecipe(
            oo(i(CItems.itemClaySpatula, 1, 32767), i(CItems.itemClayWrench)), 0, 6, ii(CItems.itemClayPipingTools.get("Piping")), e(6), 20L
        );
        recipeAssembler.addRecipe(oo(CItems.itemClayPipingTools.get("IO"), s(circuits[6], 2)), 0, 6, ii(CItems.itemClayPipingTools.get("Memory")), e(6), 20L);
        recipeAssembler.addRecipe(
            oo(CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.SPINDLE), s(circuits[6], 2)), 0, 6, ii(i(CItems.itemDirectionMemory)), e(6), 20L
        );
        GameRegistry.addRecipe(
            i(CItems.itemClayShovel),
            new Object[]{"#", "|", "|", '#', CMaterials.get(CMaterials.CLAY, CMaterials.PLATE), '|', CMaterials.get(CMaterials.CLAY, CMaterials.STICK)}
        );
        GameRegistry.addRecipe(
            i(CItems.itemClayPickaxe),
            new Object[]{
                "###", " | ", " | ", '#', CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.PLATE), '|', CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.STICK)
            }
        );
        GameRegistry.addRecipe(
            i(CItems.itemClayWrench),
            new Object[]{
                "# #",
                " o ",
                " | ",
                '#',
                CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.BLADE),
                '|',
                CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.STICK),
                'o',
                CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.SPINDLE)
            }
        );
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                i(CItems.itemClaySteelPickaxe),
                new Object[]{
                    "###",
                    " | ",
                    " | ",
                    '#',
                    CMaterials.getODName(CMaterials.CLAY_STEEL, CMaterials.INGOT),
                    '|',
                    CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.STICK)
                }
            )
        );
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                i(CItems.itemClaySteelShovel),
                new Object[]{
                    "#",
                    "|",
                    "|",
                    '#',
                    CMaterials.getODName(CMaterials.CLAY_STEEL, CMaterials.INGOT),
                    '|',
                    CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.STICK)
                }
            )
        );
        recipeAssembler.addRecipe(
            oo(CMaterials.get(CMaterials.AZ91D_ALLOY, CMaterials.PLATE, 3), CItems.itemMisc.get("SynchronousParts", 2)),
            0,
            6,
            ii(i(CItems.itemSynchronizer)),
            e(6),
            20L
        );
        recipeAssembler.addRecipe(oo(circuits[5], CMaterials.get(CMaterials.IND_CLAY, CMaterials.PLATE, 3)), 0, 4, ii(i(CItems.itemFilterWhitelist)), 8L, 20L);
        recipeAssembler.addRecipe(oo(circuits[6], CMaterials.get(CMaterials.IND_CLAY, CMaterials.PLATE, 3)), 0, 4, ii(i(CItems.itemFilterItemName)), 8L, 20L);
        recipeAssembler.addRecipe(oo(circuits[7], CMaterials.get(CMaterials.IND_CLAY, CMaterials.PLATE, 3)), 0, 4, ii(i(CItems.itemFilterFuzzy)), 8L, 20L);
        GameRegistry.addShapelessRecipe(i(CItems.itemFilterWhitelist), new Object[]{i(CItems.itemFilterBlacklist)});
        GameRegistry.addShapelessRecipe(i(CItems.itemFilterBlacklist), new Object[]{i(CItems.itemFilterWhitelist)});
        GameRegistry.addShapelessRecipe(i(CItems.itemFilterUnlocalizedName), new Object[]{i(CItems.itemFilterItemName)});
        GameRegistry.addShapelessRecipe(i(CItems.itemFilterOreDict), new Object[]{i(CItems.itemFilterUnlocalizedName)});
        GameRegistry.addShapelessRecipe(i(CItems.itemFilterUniqueId), new Object[]{i(CItems.itemFilterOreDict)});
        GameRegistry.addShapelessRecipe(i(CItems.itemFilterModId), new Object[]{i(CItems.itemFilterUniqueId)});
        GameRegistry.addShapelessRecipe(i(CItems.itemFilterItemDamage), new Object[]{i(CItems.itemFilterModId)});
        GameRegistry.addShapelessRecipe(i(CItems.itemFilterItemName), new Object[]{i(CItems.itemFilterItemDamage)});
        GameRegistry.addShapelessRecipe(i(CItems.itemFilterFuzzy), new Object[]{i(CItems.itemFilterFuzzy)});
        GameRegistry.addShapelessRecipe(
            i(CItems.itemFilterDuplicator), new Object[]{i(CItems.itemFilterWhitelist), i(CItems.itemFilterItemName), i(CItems.itemFilterFuzzy)}
        );
        GameRegistry.addShapelessRecipe(i(CItems.itemFilterBlockMetadata), new Object[]{i(CItems.itemFilterItemDamage), i(Blocks.field_150435_aG)});
        GameRegistry.addShapelessRecipe(i(CItems.itemFilterItemDamage), new Object[]{i(CItems.itemFilterBlockMetadata), i(Blocks.field_150435_aG)});
        if (ClayiumCore.cfgEnableFluidCapsule) {
            ItemCapsule.registerCompressionRecipe(
                new ItemCapsule[]{CItems.itemsCapsule[4], CItems.itemsCapsule[3], CItems.itemsCapsule[2], CItems.itemsCapsule[1], CItems.itemsCapsule[0]},
                new int[]{5, 5, 5, 8}
            );
            GameRegistry.addRecipe(i(CItems.itemsCapsule[0]), new Object[]{" # ", "# #", " # ", '#', i(CBlocks.blockCompressedClay, 1, 0)});
        }

        recipeAssembler.addRecipe(
            oo(i(Items.field_151116_aA, 4), CMaterials.get(CMaterials.AZ91D_ALLOY, CMaterials.PLATE, 8)), 0, 4, ii(i(CItems.itemGadgetHolder)), e(6), 120L
        );
        recipeAssembler.addRecipe(
            oo(CMaterials.get(CMaterials.IND_CLAY, CMaterials.PLATE, 8), CMaterials.get(CMaterials.AZ91D_ALLOY, CMaterials.PLATE, 4)),
            0,
            4,
            ii(CItems.itemGadget.get("Blank")),
            e(6),
            120L
        );
        recipeAssembler.addRecipe(
            oo(CItems.itemGadget.get("Blank"), CBlocks.blockOverclocker.get("antimatter")),
            0,
            10,
            ii(CItems.itemGadget.get("AntimatterOverclock")),
            e(10),
            120L
        );
        recipeAssembler.addRecipe(
            oo(CItems.itemGadget.get("AntimatterOverclock"), CBlocks.blockOverclocker.get("pureantimatter")),
            0,
            10,
            ii(CItems.itemGadget.get("PureAntimatterOverclock")),
            e(11),
            120L
        );
        recipeAssembler.addRecipe(
            oo(CItems.itemGadget.get("PureAntimatterOverclock"), CBlocks.blockOverclocker.get("oec")),
            0,
            10,
            ii(CItems.itemGadget.get("OECOverclock")),
            e(12),
            120L
        );
        recipeAssembler.addRecipe(
            oo(CItems.itemGadget.get("OECOverclock"), CBlocks.blockOverclocker.get("opa")), 0, 10, ii(CItems.itemGadget.get("OPAOverclock")), e(13), 120L
        );
        recipeAssembler.addRecipe(oo(CItems.itemGadget.get("Blank"), s(circuits[12], 16)), 0, 10, ii(CItems.itemGadget.get("Flight0")), e(12), 120L);
        recipeAssembler.addRecipe(oo(CItems.itemGadget.get("Flight0"), s(circuits[13], 16)), 0, 10, ii(CItems.itemGadget.get("Flight1")), e(13), 1200L);
        recipeAssembler.addRecipe(
            oo(CItems.itemGadget.get("Flight1"), CBlocks.blockOverclocker.get("opa", 16)), 0, 10, ii(CItems.itemGadget.get("Flight2")), e(14), 12000L
        );
        recipeAssembler.addRecipe(oo(CItems.itemGadget.get("Blank"), s(circuits[6], 4)), 0, 4, ii(CItems.itemGadget.get("Health0")), e(6), 120L);
        recipeAssembler.addRecipe(oo(CItems.itemGadget.get("Health0"), s(circuits[10], 4)), 0, 4, ii(CItems.itemGadget.get("Health1")), e(10), 120L);
        recipeAssembler.addRecipe(oo(CItems.itemGadget.get("Health1"), s(circuits[12], 4)), 0, 10, ii(CItems.itemGadget.get("Health2")), e(12), 120L);
        recipeAssembler.addRecipe(oo(CItems.itemGadget.get("Blank"), s(circuits[7], 2)), 0, 4, ii(CItems.itemGadget.get("AutoEat0")), e(7), 120L);
        GameRegistry.addRecipe(new ShapelessRecipesWithNBT(CItems.itemGadget.get("AutoEat0"), CItems.itemGadget.get("AutoEat1")));
        GameRegistry.addRecipe(new ShapelessRecipesWithNBT(CItems.itemGadget.get("AutoEat1"), CItems.itemGadget.get("AutoEat0")));
        recipeAssembler.addRecipe(
            oo(CItems.itemGadget.get("AntimatterOverclock"), s(circuits[10], 4)), 0, 10, ii(CItems.itemGadget.get("RepeatedlyAttack")), e(10), 120L
        );
        recipeAssembler.addRecipe(
            oo(CMaterials.get(CMaterials.AZ91D_ALLOY, CMaterials.INGOT, 16), circuits[6]), 0, 4, ii(CItems.itemMisc.get("Manipulator1")), e(4), 20L
        );
        recipeAssembler.addRecipe(oo(CItems.itemMisc.get("Manipulator1", 16), s(circuits[8], 6)), 0, 4, ii(CItems.itemMisc.get("Manipulator2")), e(8), 20L);
        recipeAssembler.addRecipe(oo(CItems.itemMisc.get("Manipulator2", 64), s(circuits[12], 6)), 0, 10, ii(CItems.itemMisc.get("Manipulator3")), e(12), 20L);
        recipeAssembler.addRecipe(
            oo(CItems.itemGadget.get("Blank"), CItems.itemMisc.get("Manipulator1")), 0, 4, ii(CItems.itemGadget.get("LongArm0")), e(4), 120L
        );
        recipeAssembler.addRecipe(
            oo(CItems.itemGadget.get("LongArm0"), CItems.itemMisc.get("Manipulator2")), 0, 4, ii(CItems.itemGadget.get("LongArm1")), e(8), 120L
        );
        recipeAssembler.addRecipe(
            oo(CItems.itemGadget.get("LongArm1"), CItems.itemMisc.get("Manipulator3")), 0, 10, ii(CItems.itemGadget.get("LongArm2")), e(12), 120L
        );
    }

    public static void registerMainMaterialRecipes() {
        GameRegistry.addRecipe(i(CBlocks.blockCompressedClay, 1, 0), new Object[]{"###", "###", "###", '#', Blocks.field_150435_aG});
        GameRegistry.addShapelessRecipe(i(Blocks.field_150435_aG, 9), new Object[]{i(CBlocks.blockCompressedClay, 1, 0)});
        GameRegistry.addRecipe(i(CBlocks.blockCompressedClay, 1, 1), new Object[]{"###", "###", "###", '#', i(CBlocks.blockCompressedClay, 1, 0)});
        GameRegistry.addShapelessRecipe(i(CBlocks.blockCompressedClay, 9, 0), new Object[]{i(CBlocks.blockCompressedClay, 1, 1)});
        recipeCondenser.addRecipe(i(Blocks.field_150435_aG, 9), i(CBlocks.blockCompressedClay, 1, 0), 1L, 4L);
        recipeCondenser.addRecipe(i(CBlocks.blockCompressedClay, 9, 0), i(CBlocks.blockCompressedClay, 1, 1), 1L, 4L);
        recipeCondenser.addRecipe(i(CBlocks.blockCompressedClay, 9, 1), i(CBlocks.blockCompressedClay, 1, 2), 10L, 4L);
        recipeCondenser.addRecipe(i(CBlocks.blockCompressedClay, 9, 2), i(CBlocks.blockCompressedClay, 1, 3), 100L, 4L);
        recipeCondenser.addRecipe(i(CBlocks.blockCompressedClay, 9, 3), 4, i(CBlocks.blockCompressedClay, 1, 4), 100L, 16L);
        recipeCondenser.addRecipe(i(CBlocks.blockCompressedClay, 9, 4), 4, i(CBlocks.blockCompressedClay, 1, 5), 1000L, 16L);
        recipeCondenser.addRecipe(i(CBlocks.blockCompressedClay, 9, 5), 4, i(CBlocks.blockCompressedClay, 1, 6), 10000L, 13L);
        recipeCondenser.addRecipe(i(CBlocks.blockCompressedClay, 9, 6), 5, i(CBlocks.blockCompressedClay, 1, 7), 100000L, 10L);
        recipeCondenser.addRecipe(i(CBlocks.blockCompressedClay, 9, 7), 5, i(CBlocks.blockCompressedClay, 1, 8), 1000000L, 8L);
        recipeCondenser.addRecipe(i(CBlocks.blockCompressedClay, 9, 8), 5, i(CBlocks.blockCompressedClay, 1, 9), 10000000L, 6L);
        recipeCondenser.addRecipe(i(CBlocks.blockCompressedClay, 9, 9), 5, i(CBlocks.blockCompressedClay, 1, 10), 100000000L, 4L);
        recipeCondenser.addRecipe(i(CBlocks.blockCompressedClay, 9, 10), 5, i(CBlocks.blockCompressedClay, 1, 11), 1000000000L, 3L);
        recipeCondenser.addRecipe(i(CBlocks.blockCompressedClay, 9, 11), 5, i(CBlocks.blockCompressedClay, 1, 12), 1000000000L, 25L);
        recipeEnergeticClayCondenser.addRecipe(i(CBlocks.blockCompressedClay, 9, 3), 3, i(CBlocks.blockCompressedClay, 1, 4), 1L, 16L);
        recipeEnergeticClayCondenser.addRecipe(i(CBlocks.blockCompressedClay, 9, 4), 3, i(CBlocks.blockCompressedClay, 1, 5), 10L, 32L);
        recipeEnergeticClayCondenser.addRecipe(i(CBlocks.blockCompressedClay, 9, 5), 3, i(CBlocks.blockCompressedClay, 1, 6), 100L, 64L);
        recipeEnergeticClayCondenser.addRecipe(i(CBlocks.blockCompressedClay, 9, 6), 4, i(CBlocks.blockCompressedClay, 1, 7), 1000L, 64L);
        recipeEnergeticClayCondenser.addRecipe(i(CBlocks.blockCompressedClay, 9, 7), 4, i(CBlocks.blockCompressedClay, 1, 8), 10000L, 64L);
        recipeEnergeticClayCondenser.addRecipe(i(CBlocks.blockCompressedClay, 9, 8), 4, i(CBlocks.blockCompressedClay, 1, 9), 100000L, 64L);
        recipeEnergeticClayCondenser.addRecipe(i(CBlocks.blockCompressedClay, 9, 9), 4, i(CBlocks.blockCompressedClay, 1, 10), 1000000L, 64L);
        recipeEnergeticClayCondenser.addRecipe(i(CBlocks.blockCompressedClay, 9, 10), 4, i(CBlocks.blockCompressedClay, 1, 11), 10000000L, 64L);
        recipeEnergeticClayCondenser.addRecipe(i(CBlocks.blockCompressedClay, 9, 11), 4, i(CBlocks.blockCompressedClay, 1, 12), 10000000L, 64L);
        recipeDecomposer.addRecipe(i(Blocks.field_150435_aG), CMaterials.get(CMaterials.CLAY, CMaterials.BALL, 4), 1L, 3L);
        recipeDecomposer.addRecipe(i(CBlocks.blockCompressedClay, 1, 0), i(Blocks.field_150435_aG, 9), 1L, 3L);
        recipeDecomposer.addRecipe(i(CBlocks.blockCompressedClay, 1, 1), i(CBlocks.blockCompressedClay, 9, 0), 1L, 3L);
        recipeDecomposer.addRecipe(i(CBlocks.blockCompressedClay, 1, 2), i(CBlocks.blockCompressedClay, 9, 1), 1L, 10L);
        recipeDecomposer.addRecipe(i(CBlocks.blockCompressedClay, 1, 3), i(CBlocks.blockCompressedClay, 9, 2), 1L, 20L);
        recipeEnergeticClayDecomposer.addRecipe(i(Blocks.field_150435_aG), 13, CMaterials.get(CMaterials.CLAY, CMaterials.BALL, 4), 1L, 0L);
        recipeEnergeticClayDecomposer.addRecipe(i(CBlocks.blockCompressedClay, 1, 0), 13, i(Blocks.field_150435_aG, 9), 1L, 0L);

        for (int i = 0; i < 12; i++) {
            recipeEnergeticClayDecomposer.addRecipe(i(CBlocks.blockCompressedClay, 1, i + 1), 13, i(CBlocks.blockCompressedClay, 9, i), 1L, 0L);
        }

        GameRegistry.addShapelessRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.LARGE_BALL),
            new Object[]{
                CMaterials.get(CMaterials.CLAY, CMaterials.BALL),
                CMaterials.get(CMaterials.CLAY, CMaterials.BALL),
                CMaterials.get(CMaterials.CLAY, CMaterials.BALL),
                CMaterials.get(CMaterials.CLAY, CMaterials.BALL),
                CMaterials.get(CMaterials.CLAY, CMaterials.BALL),
                CMaterials.get(CMaterials.CLAY, CMaterials.BALL),
                CMaterials.get(CMaterials.CLAY, CMaterials.BALL),
                CMaterials.get(CMaterials.CLAY, CMaterials.BALL)
            }
        );
        GameRegistry.addShapelessRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.SHORT_STICK, 2), new Object[]{CMaterials.get(CMaterials.CLAY, CMaterials.STICK)}
        );
        GameRegistry.addShapelessRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.SMALL_RING), new Object[]{CMaterials.get(CMaterials.CLAY, CMaterials.SHORT_STICK)}
        );
        GameRegistry.addShapelessRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.SHORT_STICK), new Object[]{CMaterials.get(CMaterials.CLAY, CMaterials.SMALL_RING)}
        );
        GameRegistry.addShapelessRecipe(CMaterials.get(CMaterials.CLAY, CMaterials.RING), new Object[]{CMaterials.get(CMaterials.CLAY, CMaterials.CYLINDER)});
        GameRegistry.addShapelessRecipe(CMaterials.get(CMaterials.CLAY, CMaterials.PIPE), new Object[]{CMaterials.get(CMaterials.CLAY, CMaterials.PLATE)});
        GameRegistry.addRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.LARGE_PLATE), new Object[]{"###", "###", "###", '#', CMaterials.get(CMaterials.CLAY, CMaterials.PLATE)}
        );

        for (CMaterial material : new CMaterial[]{CMaterials.CLAY, CMaterials.DENSE_CLAY}) {
            GameRegistry.addRecipe(
                CMaterials.get(material, CMaterials.GEAR),
                new Object[]{"iii", "ioi", "iii", 'i', CMaterials.get(material, CMaterials.SHORT_STICK), 'o', CMaterials.get(material, CMaterials.SMALL_RING)}
            );
            GameRegistry.addRecipe(
                CMaterials.get(material, CMaterials.CUTTING_HEAD),
                new Object[]{"iii", "ioi", "iii", 'i', CMaterials.get(material, CMaterials.BLADE), 'o', CMaterials.get(material, CMaterials.RING)}
            );
            GameRegistry.addRecipe(
                CMaterials.get(material, CMaterials.BEARING),
                new Object[]{"iii", "ioi", "iii", 'i', CMaterials.get(material, CMaterials.BALL), 'o', CMaterials.get(material, CMaterials.RING)}
            );
            GameRegistry.addRecipe(
                CMaterials.get(material, CMaterials.SPINDLE),
                new Object[]{
                    "0#0",
                    "ioO",
                    "0#0",
                    'i',
                    CMaterials.get(material, CMaterials.STICK),
                    'o',
                    CMaterials.get(material, CMaterials.BEARING),
                    'O',
                    CMaterials.get(material, CMaterials.RING),
                    '#',
                    CMaterials.get(material, CMaterials.PLATE),
                    '0',
                    CMaterials.get(material, CMaterials.SMALL_RING)
                }
            );
            GameRegistry.addRecipe(
                CMaterials.get(material, CMaterials.GRINDING_HEAD),
                new Object[]{"iii", "ioi", "iii", 'i', CMaterials.get(material, CMaterials.NEEDLE), 'o', CMaterials.get(material, CMaterials.RING)}
            );
            GameRegistry.addRecipe(
                CMaterials.get(material, CMaterials.WATER_WHEEL),
                new Object[]{"###", "#o#", "###", '#', CMaterials.get(material, CMaterials.PLATE), 'o', CMaterials.get(material, CMaterials.RING)}
            );
        }

        recipeBendingMachine.addRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.BALL),
            CMaterials.get(CMaterials.CLAY, CMaterials.SMALL_DISC),
            (long)ClayiumCore.divideByProgressionRateI(3)
        );
        recipeBendingMachine.addRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.LARGE_BALL),
            CMaterials.get(CMaterials.CLAY, CMaterials.DISC),
            (long)ClayiumCore.divideByProgressionRateI(2)
        );
        recipeBendingMachine.addRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.DISC),
            CItems.itemRawClayCraftingTools.getItemStack("Slicer"),
            (long)ClayiumCore.divideByProgressionRateI(1)
        );
        recipeBendingMachine.addRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.BLOCK), CMaterials.get(CMaterials.CLAY, CMaterials.PLATE), (long)ClayiumCore.divideByProgressionRateI(1)
        );
        recipeBendingMachine.addRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.PLATE, 4),
            CMaterials.get(CMaterials.CLAY, CMaterials.LARGE_PLATE),
            (long)ClayiumCore.divideByProgressionRateI(4)
        );
        recipeBendingMachine.addRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.CYLINDER),
            CMaterials.get(CMaterials.CLAY, CMaterials.BLADE, 2),
            (long)ClayiumCore.divideByProgressionRateI(4)
        );
        recipeBendingMachine.addRecipe(
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.BLOCK),
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.PLATE),
            (long)ClayiumCore.divideByProgressionRateI(4)
        );
        recipeBendingMachine.addRecipe(
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.PLATE, 4),
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.LARGE_PLATE),
            (long)ClayiumCore.divideByProgressionRateI(8)
        );
        recipeBendingMachine.addRecipe(
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.CYLINDER),
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.BLADE, 2),
            (long)ClayiumCore.divideByProgressionRateI(8)
        );
        recipeBendingMachine.addRecipe(
            CMaterials.get(CMaterials.IND_CLAY, CMaterials.BLOCK),
            2,
            CMaterials.get(CMaterials.IND_CLAY, CMaterials.PLATE),
            2L,
            (long)ClayiumCore.divideByProgressionRateI(4)
        );
        recipeBendingMachine.addRecipe(
            CMaterials.get(CMaterials.IND_CLAY, CMaterials.PLATE, 4),
            2,
            CMaterials.get(CMaterials.IND_CLAY, CMaterials.LARGE_PLATE),
            2L,
            (long)ClayiumCore.divideByProgressionRateI(8)
        );
        recipeBendingMachine.addRecipe(
            CMaterials.get(CMaterials.ADVIND_CLAY, CMaterials.BLOCK),
            2,
            CMaterials.get(CMaterials.ADVIND_CLAY, CMaterials.PLATE),
            4L,
            (long)ClayiumCore.divideByProgressionRateI(4)
        );
        recipeBendingMachine.addRecipe(
            CMaterials.get(CMaterials.ADVIND_CLAY, CMaterials.PLATE, 4),
            2,
            CMaterials.get(CMaterials.ADVIND_CLAY, CMaterials.LARGE_PLATE),
            4L,
            (long)ClayiumCore.divideByProgressionRateI(8)
        );
        recipeWireDrawingMachine.addRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.BALL), CMaterials.get(CMaterials.CLAY, CMaterials.STICK), (long)ClayiumCore.divideByProgressionRateI(1)
        );
        recipeWireDrawingMachine.addRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.CYLINDER),
            CMaterials.get(CMaterials.CLAY, CMaterials.STICK, 8),
            (long)ClayiumCore.divideByProgressionRateI(3)
        );
        recipeWireDrawingMachine.addRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.PIPE),
            CMaterials.get(CMaterials.CLAY, CMaterials.STICK, 4),
            (long)ClayiumCore.divideByProgressionRateI(2)
        );
        recipeWireDrawingMachine.addRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.SMALL_DISC),
            CMaterials.get(CMaterials.CLAY, CMaterials.STICK),
            (long)ClayiumCore.divideByProgressionRateI(1)
        );
        recipeWireDrawingMachine.addRecipe(
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.CYLINDER),
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.STICK, 8),
            (long)ClayiumCore.divideByProgressionRateI(6)
        );
        recipeWireDrawingMachine.addRecipe(
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.PIPE),
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.STICK, 4),
            (long)ClayiumCore.divideByProgressionRateI(4)
        );
        recipeWireDrawingMachine.addRecipe(
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.SMALL_DISC),
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.STICK),
            (long)ClayiumCore.divideByProgressionRateI(2)
        );
        recipePipeDrawingMachine.addRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.CYLINDER),
            CMaterials.get(CMaterials.CLAY, CMaterials.PIPE, 2),
            (long)ClayiumCore.divideByProgressionRateI(3)
        );
        recipePipeDrawingMachine.addRecipe(
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.CYLINDER),
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.PIPE, 2),
            (long)ClayiumCore.divideByProgressionRateI(6)
        );
        recipeCuttingMachine.addRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.LARGE_BALL),
            CMaterials.get(CMaterials.CLAY, CMaterials.DISC),
            (long)ClayiumCore.divideByProgressionRateI(2)
        );
        recipeCuttingMachine.addRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.CYLINDER),
            CMaterials.get(CMaterials.CLAY, CMaterials.SMALL_DISC, 8),
            (long)ClayiumCore.divideByProgressionRateI(2)
        );
        recipeCuttingMachine.addRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.LARGE_PLATE),
            CMaterials.get(CMaterials.CLAY, CMaterials.DISC, 2),
            (long)ClayiumCore.divideByProgressionRateI(3)
        );
        recipeCuttingMachine.addRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.PLATE),
            CMaterials.get(CMaterials.CLAY, CMaterials.SMALL_DISC, 4),
            (long)ClayiumCore.divideByProgressionRateI(3)
        );
        recipeCuttingMachine.addRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.STICK),
            CMaterials.get(CMaterials.CLAY, CMaterials.SHORT_STICK, 2),
            (long)ClayiumCore.divideByProgressionRateI(1)
        );
        recipeCuttingMachine.addRecipe(
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.CYLINDER),
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.SMALL_DISC, 8),
            (long)ClayiumCore.divideByProgressionRateI(4)
        );
        recipeCuttingMachine.addRecipe(
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.LARGE_PLATE),
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.DISC, 2),
            (long)ClayiumCore.divideByProgressionRateI(6)
        );
        recipeCuttingMachine.addRecipe(
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.PLATE),
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.SMALL_DISC, 4),
            (long)ClayiumCore.divideByProgressionRateI(6)
        );
        recipeCuttingMachine.addRecipe(
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.STICK),
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.SHORT_STICK, 2),
            (long)ClayiumCore.divideByProgressionRateI(2)
        );
        recipeLathe.addRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.BALL),
            CMaterials.get(CMaterials.CLAY, CMaterials.SHORT_STICK),
            (long)ClayiumCore.divideByProgressionRateI(1)
        );
        recipeLathe.addRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.LARGE_BALL),
            CMaterials.get(CMaterials.CLAY, CMaterials.CYLINDER),
            (long)ClayiumCore.divideByProgressionRateI(4)
        );
        recipeLathe.addRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.CYLINDER),
            CMaterials.get(CMaterials.CLAY, CMaterials.NEEDLE),
            (long)ClayiumCore.divideByProgressionRateI(3)
        );
        recipeLathe.addRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.NEEDLE),
            CMaterials.get(CMaterials.CLAY, CMaterials.STICK, 6),
            (long)ClayiumCore.divideByProgressionRateI(3)
        );
        recipeLathe.addRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.DISC), CMaterials.get(CMaterials.CLAY, CMaterials.RING), (long)ClayiumCore.divideByProgressionRateI(2)
        );
        recipeLathe.addRecipe(
            CMaterials.get(CMaterials.CLAY, CMaterials.SMALL_DISC),
            CMaterials.get(CMaterials.CLAY, CMaterials.SMALL_RING),
            (long)ClayiumCore.divideByProgressionRateI(1)
        );
        recipeLathe.addRecipe(
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.BLOCK, 2),
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.CYLINDER),
            (long)ClayiumCore.divideByProgressionRateI(4)
        );
        recipeLathe.addRecipe(
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.CYLINDER),
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.NEEDLE),
            (long)ClayiumCore.divideByProgressionRateI(6)
        );
        recipeLathe.addRecipe(
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.NEEDLE),
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.STICK, 6),
            (long)ClayiumCore.divideByProgressionRateI(6)
        );
        recipeLathe.addRecipe(
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.DISC),
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.RING),
            (long)ClayiumCore.divideByProgressionRateI(4)
        );
        recipeLathe.addRecipe(
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.SMALL_DISC),
            CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.SMALL_RING),
            (long)ClayiumCore.divideByProgressionRateI(2)
        );
        recipeAssembler.addRecipe(
            ii(CMaterials.get(CMaterials.CLAY, CMaterials.STICK, 5)),
            0,
            3,
            ii(CMaterials.get(CMaterials.CLAY, CMaterials.GEAR)),
            10L,
            (long)ClayiumCore.divideByProgressionRateI(20)
        );
        recipeAssembler.addRecipe(
            ii(CMaterials.get(CMaterials.CLAY, CMaterials.SHORT_STICK, 9)),
            0,
            3,
            ii(CMaterials.get(CMaterials.CLAY, CMaterials.GEAR)),
            10L,
            (long)ClayiumCore.divideByProgressionRateI(20)
        );
        recipeAssembler.addRecipe(
            ii(CMaterials.get(CMaterials.CLAY, CMaterials.LARGE_PLATE), CMaterials.get(CMaterials.CLAY, CMaterials.BALL, 8)),
            0,
            3,
            ii(CMaterials.get(CMaterials.CLAY, CMaterials.SPINDLE)),
            10L,
            (long)ClayiumCore.divideByProgressionRateI(20)
        );
        recipeAssembler.addRecipe(
            ii(CMaterials.get(CMaterials.CLAY, CMaterials.LARGE_PLATE), CMaterials.get(CMaterials.CLAY, CMaterials.BLOCK, 8)),
            0,
            3,
            ii(CMaterials.get(CMaterials.CLAY, CMaterials.GRINDING_HEAD)),
            10L,
            (long)ClayiumCore.divideByProgressionRateI(20)
        );
        recipeAssembler.addRecipe(
            ii(CMaterials.get(CMaterials.CLAY, CMaterials.LARGE_PLATE), CMaterials.get(CMaterials.CLAY, CMaterials.PLATE, 8)),
            0,
            3,
            ii(CMaterials.get(CMaterials.CLAY, CMaterials.CUTTING_HEAD)),
            10L,
            (long)ClayiumCore.divideByProgressionRateI(20)
        );
        recipeAssembler.addRecipe(
            ii(CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.STICK, 5)),
            0,
            3,
            ii(CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.GEAR)),
            10L,
            (long)ClayiumCore.divideByProgressionRateI(20)
        );
        recipeAssembler.addRecipe(
            ii(CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.SHORT_STICK, 9)),
            0,
            3,
            ii(CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.GEAR)),
            10L,
            (long)ClayiumCore.divideByProgressionRateI(20)
        );
        recipeAssembler.addRecipe(
            ii(CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.LARGE_PLATE), CMaterials.get(CMaterials.CLAY, CMaterials.BALL, 8)),
            0,
            3,
            ii(CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.SPINDLE)),
            100L,
            (long)ClayiumCore.divideByProgressionRateI(20)
        );
        recipeAssembler.addRecipe(
            ii(CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.LARGE_PLATE), CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.BLOCK, 8)),
            0,
            3,
            ii(CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.GRINDING_HEAD)),
            100L,
            (long)ClayiumCore.divideByProgressionRateI(20)
        );
        recipeAssembler.addRecipe(
            ii(CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.LARGE_PLATE), CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.PLATE, 8)),
            0,
            3,
            ii(CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.CUTTING_HEAD)),
            100L,
            (long)ClayiumCore.divideByProgressionRateI(20)
        );
        GameRegistry.addShapelessRecipe(CMaterials.get(CMaterials.CLAY, CMaterials.BALL, 3), new Object[]{CMaterials.get(CMaterials.CLAY, CMaterials.RING)});
        GameRegistry.addShapelessRecipe(CMaterials.get(CMaterials.CLAY, CMaterials.BALL, 3), new Object[]{CMaterials.get(CMaterials.CLAY, CMaterials.GEAR)});
        GameRegistry.addShapelessRecipe(CMaterials.get(CMaterials.CLAY, CMaterials.BALL, 3), new Object[]{CMaterials.get(CMaterials.CLAY, CMaterials.BLADE)});
        GameRegistry.addShapelessRecipe(CMaterials.get(CMaterials.CLAY, CMaterials.BALL, 5), new Object[]{CMaterials.get(CMaterials.CLAY, CMaterials.NEEDLE)});

        for (CMaterial material : new CMaterial[]{CMaterials.CLAY, CMaterials.DENSE_CLAY}) {
            int i = material == CMaterials.CLAY ? 1 : 4;
            recipeGrinder.addRecipe(CMaterials.get(material, CMaterials.PLATE), CMaterials.get(material, CMaterials.DUST), 1L, (long)(3 * i));
            recipeGrinder.addRecipe(CMaterials.get(material, CMaterials.STICK, 4), CMaterials.get(material, CMaterials.DUST), 1L, (long)(3 * i));
            recipeGrinder.addRecipe(CMaterials.get(material, CMaterials.SHORT_STICK, 8), CMaterials.get(material, CMaterials.DUST), 1L, (long)(3 * i));
            recipeGrinder.addRecipe(CMaterials.get(material, CMaterials.RING, 4), CMaterials.get(material, CMaterials.DUST, 5), 1L, (long)(15 * i));
            recipeGrinder.addRecipe(CMaterials.get(material, CMaterials.SMALL_RING, 8), CMaterials.get(material, CMaterials.DUST), 1L, (long)(3 * i));
            recipeGrinder.addRecipe(CMaterials.get(material, CMaterials.GEAR, 8), CMaterials.get(material, CMaterials.DUST, 9), 1L, (long)(27 * i));
            recipeGrinder.addRecipe(CMaterials.get(material, CMaterials.BLADE), CMaterials.get(material, CMaterials.DUST), 1L, (long)(3 * i));
            recipeGrinder.addRecipe(CMaterials.get(material, CMaterials.NEEDLE), CMaterials.get(material, CMaterials.DUST, 2), 1L, (long)(6 * i));
            recipeGrinder.addRecipe(CMaterials.get(material, CMaterials.DISC, 2), CMaterials.get(material, CMaterials.DUST, 3), 1L, (long)(9 * i));
            recipeGrinder.addRecipe(CMaterials.get(material, CMaterials.SMALL_DISC, 4), CMaterials.get(material, CMaterials.DUST), 1L, (long)(3 * i));
            recipeGrinder.addRecipe(CMaterials.get(material, CMaterials.CYLINDER), CMaterials.get(material, CMaterials.DUST, 2), 1L, (long)(6 * i));
            recipeGrinder.addRecipe(CMaterials.get(material, CMaterials.PIPE), CMaterials.get(material, CMaterials.DUST), 1L, (long)(3 * i));
            recipeGrinder.addRecipe(CMaterials.get(material, CMaterials.LARGE_PLATE), CMaterials.get(material, CMaterials.DUST, 4), 1L, (long)(12 * i));
            recipeGrinder.addRecipe(CMaterials.get(material, CMaterials.GRINDING_HEAD), CMaterials.get(material, CMaterials.DUST, 16), 1L, (long)(48 * i));
            recipeGrinder.addRecipe(CMaterials.get(material, CMaterials.BEARING, 4), CMaterials.get(material, CMaterials.DUST, 5), 1L, (long)(15 * i));
            recipeGrinder.addRecipe(CMaterials.get(material, CMaterials.SPINDLE), CMaterials.get(material, CMaterials.DUST, 4), 1L, (long)(12 * i));
            recipeGrinder.addRecipe(CMaterials.get(material, CMaterials.CUTTING_HEAD), CMaterials.get(material, CMaterials.DUST, 9), 1L, (long)(27 * i));
        }

        recipeDecomposer.addRecipe(
            CMaterials.get(CMaterials.IND_CLAY, CMaterials.DUST),
            CMaterials.get(CMaterials.ENG_CLAY, CMaterials.DUST, 3),
            1L,
            (long)ClayiumCore.divideByProgressionRateI(60)
        );
        recipeDecomposer.addRecipe(
            CMaterials.get(CMaterials.ADVIND_CLAY, CMaterials.DUST),
            4,
            CMaterials.get(CMaterials.ENG_CLAY, CMaterials.DUST, 28),
            1000L,
            (long)ClayiumCore.divideByProgressionRateI(60)
        );
        recipeCentrifuge.addRecipe(
            ii(CMaterials.get(CMaterials.CLAY, CMaterials.DUST, 9)),
            0,
            0,
            ii(CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.DUST)),
            4L,
            (long)ClayiumCore.divideByProgressionRateI(20)
        );
        recipeCentrifuge.addRecipe(
            ii(CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.DUST, 2)),
            0,
            0,
            ii(CMaterials.get(CMaterials.CLAY, CMaterials.DUST, 9), CMaterials.get(CMaterials.CAL_CLAY, CMaterials.DUST, 1)),
            4L,
            (long)ClayiumCore.divideByProgressionRateI(20)
        );
        recipeCentrifuge.addRecipe(
            ii(CMaterials.get(CMaterials.IND_CLAY, CMaterials.DUST, 2)),
            0,
            0,
            ii(
                CMaterials.get(CMaterials.ENG_CLAY, CMaterials.DUST, 12),
                CMaterials.get(CMaterials.CLAY, CMaterials.DUST, 8),
                CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.DUST, 8),
                CMaterials.get(CMaterials.IND_CLAY, CMaterials.DUST)
            ),
            4L,
            (long)ClayiumCore.divideByProgressionRateI(20)
        );
        recipeCentrifuge.addRecipe(
            ii(CMaterials.get(CMaterials.ADVIND_CLAY, CMaterials.DUST, 2)),
            0,
            4,
            ii(
                CMaterials.get(CMaterials.ENG_CLAY, CMaterials.DUST, 64),
                CMaterials.get(CMaterials.CLAY, CMaterials.DUST, 64),
                CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.DUST, 64),
                CMaterials.get(CMaterials.IND_CLAY, CMaterials.DUST, 12)
            ),
            10000L,
            (long)ClayiumCore.divideByProgressionRateI(12)
        );
        recipeChemicalReactor.addRecipe(
            oo(CMaterials.getOD(CMaterials.SALT, CMaterials.DUST, 2), CMaterials.get(CMaterials.CAL_CLAY, CMaterials.DUST)),
            0,
            0,
            ii(CMaterials.get(CMaterials.CALCIUM_CHLORIDE, CMaterials.DUST), CMaterials.get(CMaterials.SODIUM_CARBONATE, CMaterials.DUST)),
            e(5),
            120L
        );
        recipeChemicalReactor.addRecipe(
            ii(CMaterials.get(CMaterials.SODIUM_CARBONATE, CMaterials.DUST), CMaterials.get(CMaterials.CLAY, CMaterials.DUST)),
            0,
            0,
            ii(CMaterials.get(CMaterials.QUARTZ, CMaterials.DUST)),
            e(4),
            120L
        );
        recipeChemicalReactor.addRecipe(
            oo(CMaterials.getOD(CMaterials.QUARTZ, CMaterials.DUST), i(Items.field_151044_h)),
            0,
            0,
            ii(CMaterials.get(CMaterials.IMPURE_SILICON, CMaterials.INGOT)),
            e(4),
            120L
        );
        recipeChemicalReactor.addRecipe(
            oo(CMaterials.getOD(CMaterials.QUARTZ, CMaterials.DUST), i(Items.field_151044_h, 1, 1)),
            0,
            0,
            ii(CMaterials.get(CMaterials.IMPURE_SILICON, CMaterials.INGOT)),
            e(4),
            120L
        );
        recipeChemicalReactor.addRecipe(
            oo(CMaterials.getOD(CMaterials.SALT, CMaterials.DUST), CMaterials.get(CMaterials.IND_CLAY, CMaterials.DUST)),
            0,
            8,
            ii(CMaterials.get(CMaterials.QUARTZ, CMaterials.DUST), CMaterials.get(CMaterials.CALCIUM_CHLORIDE, CMaterials.DUST)),
            e(10.0, 8),
            1L
        );
        recipeChemicalReactor.addRecipe(
            oo(CMaterials.getOD(CMaterials.QUARTZ, CMaterials.DUST), CMaterials.get(CMaterials.IND_CLAY, CMaterials.DUST)),
            0,
            8,
            ii(CMaterials.get(CMaterials.IMPURE_SILICON, CMaterials.INGOT)),
            e(10.0, 8),
            1L
        );
        recipeBlastFurnace.addRecipe(
            ii(CMaterials.get(CMaterials.ADVIND_CLAY, CMaterials.DUST), CMaterials.get(CMaterials.IMPURE_SILICON, CMaterials.INGOT)),
            0,
            7,
            ii(CMaterials.get(CMaterials.SILICON, CMaterials.INGOT)),
            e(7),
            100L
        );
        recipeChemicalReactor.addRecipe(
            ii(CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.DUST)),
            0,
            5,
            ii(CMaterials.get(CMaterials.IMPURE_SILICON, CMaterials.DUST), CMaterials.get(CMaterials.MAIN_ALUMINIUM, CMaterials.DUST)),
            e(5),
            30L
        );
        recipeBlastFurnace.addRecipe(
            ii(CMaterials.get(CMaterials.IND_CLAY, CMaterials.DUST, 2), CMaterials.get(CMaterials.IMPURE_MANGANESE, CMaterials.DUST)),
            0,
            6,
            ii(CMaterials.get(CMaterials.CLAY_STEEL, CMaterials.INGOT, 2)),
            e(5.0, 6),
            200L
        );
        recipeBlastFurnace.addRecipe(
            ii(CMaterials.get(CMaterials.ADVIND_CLAY, CMaterials.DUST), CMaterials.get(CMaterials.IMPURE_MANGANESE, CMaterials.DUST)),
            0,
            7,
            ii(CMaterials.get(CMaterials.CLAY_STEEL, CMaterials.INGOT)),
            e(5.0, 7),
            5L
        );
        recipeBlastFurnace.addRecipe(
            oo(CMaterials.get(CMaterials.ADVIND_CLAY, CMaterials.DUST), CMaterials.getOD(CMaterials.MANGANESE, CMaterials.DUST)),
            0,
            8,
            ii(CMaterials.get(CMaterials.CLAY_STEEL, CMaterials.INGOT)),
            e(5.0, 8),
            1L
        );
        recipeReactor.addRecipe(
            oo(CMaterials.get(CMaterials.ADVIND_CLAY, CMaterials.DUST, 8), CMaterials.getOD(CMaterials.LITHIUM, CMaterials.DUST, 4)),
            0,
            7,
            ii(CMaterials.get(CMaterials.CLAYIUM, CMaterials.DUST, 8)),
            e(10.0, 7),
            50000L
        );
        recipeReactor.addRecipe(
            oo(CMaterials.get(CMaterials.ADVIND_CLAY, CMaterials.DUST, 8), CMaterials.getOD(CMaterials.HAFNIUM, CMaterials.DUST)),
            0,
            7,
            ii(CMaterials.get(CMaterials.CLAYIUM, CMaterials.DUST, 8)),
            e(10.0, 7),
            500000L
        );
        recipeReactor.addRecipe(
            oo(CMaterials.get(CMaterials.ADVIND_CLAY, CMaterials.DUST, 8), CMaterials.getOD(CMaterials.BARIUM, CMaterials.DUST)),
            0,
            7,
            ii(CMaterials.get(CMaterials.CLAYIUM, CMaterials.DUST, 8)),
            e(3.0, 7),
            5000000L
        );
        recipeReactor.addRecipe(
            oo(CMaterials.get(CMaterials.ADVIND_CLAY, CMaterials.DUST, 8), CMaterials.getOD(CMaterials.STRONTIUM, CMaterials.DUST)),
            0,
            7,
            ii(CMaterials.get(CMaterials.CLAYIUM, CMaterials.DUST, 8)),
            e(7),
            50000000L
        );
        recipeReactor.addRecipe(
            oo(CMaterials.get(CMaterials.ADVIND_CLAY, CMaterials.DUST), CMaterials.get(CMaterials.IMPURE_ULTIMATE_ALLOY, CMaterials.INGOT)),
            0,
            8,
            ii(CMaterials.get(CMaterials.ULTIMATE_ALLOY, CMaterials.INGOT)),
            e(10.0, 8),
            1000000000L
        );
        recipeReactor.addRecipe(
            oo(CMaterials.get(CMaterials.ENG_CLAY, CMaterials.DUST, 8), CMaterials.getOD(CMaterials.LITHIUM, CMaterials.DUST)),
            0,
            7,
            ii(CMaterials.get(CMaterials.EXC_CLAY, CMaterials.DUST, 4)),
            e(7),
            2000000L
        );
        recipeGrinder.addRecipe(i(CBlocks.blockClayTreeLog), 5, CMaterials.get(CMaterials.ORG_CLAY, CMaterials.DUST), e(5), 200L);
        recipeReactor.addRecipe(
            ii(CMaterials.get(CMaterials.ORG_CLAY, CMaterials.DUST), CMaterials.get(CMaterials.ADVIND_CLAY, CMaterials.DUST)),
            0,
            10,
            ii(CMaterials.get(CMaterials.ORG_CLAY, CMaterials.DUST, 2)),
            e(10),
            1000000000000L
        );
        recipeReactor.addRecipe(
            ii(CItems.itemMisc.get("ClaySoul"), CMaterials.get(CMaterials.ADVIND_CLAY, CMaterials.DUST)),
            0,
            11,
            ii(CMaterials.get(CMaterials.ORG_CLAY, CMaterials.DUST, 2)),
            e(11),
            100000000000000L
        );
        recipeReactor.addRecipe(
            oo(CMaterials.get(CMaterials.CLAYIUM, CMaterials.INGOT)),
            0,
            9,
            ii(CItems.itemMisc.get("AntimatterSeed")),
            e(9),
            ClayiumCore.divideByProgressionRateL(200000000000000L)
        );
        recipeCACondenser.addRecipe(
            CItems.itemMisc.get("AntimatterSeed"),
            CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM),
            e(2.5, 9),
            ClayiumCore.divideByProgressionRateL(2000L)
        );
        recipeCAInjector.addRecipe(
            ii(machines[10], CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM, 8)), 0, 10, ii(i(CBlocks.blockCACollector)), e(2.0, 10), 4000L
        );
        recipeCAReactor.addRecipe(
            CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM),
            10,
            CMaterials.get(CMaterials.PURE_ANTIMATTER, CMaterials.GEM),
            e(0.1, 10),
            (long)ClayiumCore.divideByProgressionRateI(300)
        );

        for (int i = 0; i < 8; i++) {
            recipeCondenser.addRecipe(
                CMaterials.get(CMaterials.COMPRESSED_PURE_ANTIMATTER[i], CMaterials.GEM, 9),
                10,
                CMaterials.get(CMaterials.COMPRESSED_PURE_ANTIMATTER[i + 1], CMaterials.GEM),
                e(9),
                6L
            );
            GameRegistry.addShapelessRecipe(
                CMaterials.get(CMaterials.COMPRESSED_PURE_ANTIMATTER[i], CMaterials.GEM, 9),
                new Object[]{CMaterials.get(CMaterials.COMPRESSED_PURE_ANTIMATTER[i + 1], CMaterials.GEM)}
            );
        }
    }

    public static void registerHullRecipes() {
        GameRegistry.addRecipe(
            i(CBlocks.blockRawClayMachineHull, 1, 0),
            new Object[]{
                "###", "#o#", "###", '#', CMaterials.get(CMaterials.CLAY, CMaterials.LARGE_PLATE), 'o', CMaterials.get(CMaterials.CLAY, CMaterials.GEAR)
            }
        );
        GameRegistry.addSmelting(i(CBlocks.blockRawClayMachineHull, 1, 0), i(CBlocks.blockMachineHull, 1, 0), 0.1F);
        GameRegistry.addRecipe(
            i(CBlocks.blockMachineHull, 1, 1),
            new Object[]{"###", "#C#", "###", '#', CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.LARGE_PLATE), 'C', CItems.itemMisc.get("ClayCircuit")}
        );
        GameRegistry.addRecipe(
            i(CBlocks.blockMachineHull, 1, 2),
            new Object[]{"###", "#C#", "###", '#', CMaterials.get(CMaterials.IND_CLAY, CMaterials.LARGE_PLATE), 'C', CItems.itemMisc.get("SimpleCircuit")}
        );
        GameRegistry.addRecipe(
            i(CBlocks.blockMachineHull, 1, 3),
            new Object[]{
                "#E#",
                "#C#",
                "###",
                '#',
                CMaterials.get(CMaterials.ADVIND_CLAY, CMaterials.LARGE_PLATE),
                'C',
                CItems.itemMisc.get("BasicCircuit"),
                'E',
                CItems.itemMisc.get("CEE")
            }
        );
        GameRegistry.addRecipe(
            i(CBlocks.blockMachineHull, 1, 4),
            new Object[]{
                "#E#",
                "*C*",
                "#*#",
                '#',
                CMaterials.get(CMaterials.IMPURE_SILICON, CMaterials.LARGE_PLATE),
                '*',
                CMaterials.get(CMaterials.SILICONE, CMaterials.LARGE_PLATE),
                'C',
                CItems.itemMisc.get("AdvancedCircuit"),
                'E',
                CItems.itemMisc.get("CEE")
            }
        );
        GameRegistry.addRecipe(
            i(CBlocks.blockMachineHull, 1, 5),
            new Object[]{
                "#E#",
                "#C#",
                "###",
                '#',
                CMaterials.get(CMaterials.MAIN_ALUMINIUM, CMaterials.LARGE_PLATE),
                'C',
                CItems.itemMisc.get("PrecisionCircuit"),
                'E',
                CItems.itemMisc.get("CEE")
            }
        );
        GameRegistry.addRecipe(
            i(CBlocks.blockMachineHull, 1, 6),
            new Object[]{
                "#E#",
                "#C#",
                "###",
                '#',
                CMaterials.get(CMaterials.CLAY_STEEL, CMaterials.LARGE_PLATE),
                'C',
                CItems.itemMisc.get("IntegratedCircuit"),
                'E',
                CItems.itemMisc.get("CEE")
            }
        );
        GameRegistry.addRecipe(
            i(CBlocks.blockMachineHull, 1, 7),
            new Object[]{
                "#E#",
                "#C#",
                "###",
                '#',
                CMaterials.get(CMaterials.CLAYIUM, CMaterials.LARGE_PLATE),
                'C',
                CItems.itemMisc.get("ClayCore"),
                'E',
                CItems.itemMisc.get("CEE")
            }
        );
        GameRegistry.addRecipe(
            i(CBlocks.blockMachineHull, 1, 8),
            new Object[]{
                "#E#",
                "#C#",
                "###",
                '#',
                CMaterials.get(CMaterials.ULTIMATE_ALLOY, CMaterials.LARGE_PLATE),
                'C',
                CItems.itemMisc.get("ClayBrain"),
                'E',
                CItems.itemMisc.get("CEE")
            }
        );
        GameRegistry.addRecipe(
            i(CBlocks.blockMachineHull, 1, 9),
            new Object[]{
                "#E#",
                "#C#",
                "###",
                '#',
                CMaterials.get(CMaterials.ANTIMATTER, CMaterials.LARGE_PLATE),
                'C',
                CItems.itemMisc.get("ClaySpirit"),
                'E',
                CItems.itemMisc.get("CEE")
            }
        );
        GameRegistry.addRecipe(
            i(CBlocks.blockMachineHull, 1, 10),
            new Object[]{
                "#E#",
                "#C#",
                "###",
                '#',
                CMaterials.get(CMaterials.PURE_ANTIMATTER, CMaterials.LARGE_PLATE),
                'C',
                CItems.itemMisc.get("ClaySoul"),
                'E',
                CItems.itemMisc.get("CEE")
            }
        );
        GameRegistry.addRecipe(
            i(CBlocks.blockMachineHull, 1, 11),
            new Object[]{
                "#E#",
                "#C#",
                "###",
                '#',
                CMaterials.get(CMaterials.OCTUPLE_CLAY, CMaterials.LARGE_PLATE),
                'C',
                CItems.itemMisc.get("ClayAnima"),
                'E',
                CItems.itemMisc.get("CEE")
            }
        );
        GameRegistry.addRecipe(
            i(CBlocks.blockMachineHull, 1, 12),
            new Object[]{
                "#E#",
                "#C#",
                "###",
                '#',
                CMaterials.get(CMaterials.OCTUPLE_PURE_ANTIMATTER, CMaterials.LARGE_PLATE),
                'C',
                CItems.itemMisc.get("ClayPsyche"),
                'E',
                CItems.itemMisc.get("CEE")
            }
        );
        recipeAssembler.addRecipe(
            ii(CMaterials.get(CMaterials.AZ91D_ALLOY, CMaterials.LARGE_PLATE), circuits[6]), 0, 4, ii(CBlocks.blockOthersHull.get("az91d")), e(6), 120L
        );
        GameRegistry.addRecipe(
            CBlocks.blockOthersHull.get("zk60a"),
            new Object[]{
                "###", "#C#", "###", '#', CMaterials.get(CMaterials.ZK60A_ALLOY, CMaterials.LARGE_PLATE), 'C', CItems.itemMisc.get("PrecisionCircuit")
            }
        );
        CMaterial[] reactorHullMats = new CMaterial[]{
            CMaterials.RUBIDIUM,
            CMaterials.CERIUM,
            CMaterials.TANTALUM,
            CMaterials.PRASEODYMIUM,
            CMaterials.PROTACTINIUM,
            CMaterials.NEPTUNIUM,
            CMaterials.PROMETHIUM,
            CMaterials.SAMARIUM,
            CMaterials.CURIUM,
            CMaterials.EUROPIUM
        };
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                i(CBlocks.blockCAReactorHull, 1, 0),
                oo(
                    "#I#",
                    "#H#",
                    "###",
                    '#',
                    CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM),
                    'H',
                    machines[10],
                    'I',
                    CMaterials.getODName(reactorHullMats[0], CMaterials.INGOT)
                )
            )
        );

        for (int i = 0; i <= 8; i++) {
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    i(CBlocks.blockCAReactorHull, 1, i + 1),
                    oo(
                        "#I#",
                        "#H#",
                        "###",
                        '#',
                        CMaterials.get(CMaterials.COMPRESSED_PURE_ANTIMATTER[i], CMaterials.GEM),
                        'H',
                        i(CBlocks.blockCAReactorHull, 1, i),
                        'I',
                        CMaterials.getODName(reactorHullMats[i + 1], CMaterials.INGOT)
                    )
                )
            );
        }

        GameRegistry.addRecipe(
            CItems.itemMisc.get("ClayCircuit"),
            new Object[]{
                "-*-",
                "o#o",
                "-*-",
                '-',
                CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.STICK),
                '*',
                CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.GEAR),
                'o',
                CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.RING),
                '#',
                CItems.itemMisc.get("ClayCircuitBoard")
            }
        );
        GameRegistry.addRecipe(
            CItems.itemMisc.get("SimpleCircuit"),
            new Object[]{"---", "-#-", "---", '-', CMaterials.get(CMaterials.ENG_CLAY, CMaterials.DUST), '#', CItems.itemMisc.get("ClayCircuitBoard")}
        );
        recipeMillingMachine.addRecipe(CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.PLATE), CItems.itemMisc.get("ClayCircuitBoard"), 32L);
        recipeMillingMachine.addRecipe(CMaterials.get(CMaterials.IND_CLAY, CMaterials.PLATE), CItems.itemMisc.get("ClayCircuitBoard"), 1L);
        recipeMillingMachine.addRecipe(CMaterials.get(CMaterials.ADVIND_CLAY, CMaterials.PLATE), 3, CItems.itemMisc.get("CEEBoard"), 2L, 32L);
        recipeAssembler.addRecipe(
            ii(CItems.itemMisc.get("CEECircuit"), CMaterials.get(CMaterials.IND_CLAY, CMaterials.PLATE, 3)), 0, 0, ii(CItems.itemMisc.get("CEE")), 8L, 20L
        );
        recipeInscriber.addRecipe(
            ii(CItems.itemMisc.get("CEEBoard"), CMaterials.get(CMaterials.ENG_CLAY, CMaterials.DUST, 32)), 0, 0, ii(CItems.itemMisc.get("CEECircuit")), 2L, 20L
        );
        recipeInscriber.addRecipe(
            ii(CItems.itemMisc.get("ClayCircuitBoard"), CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.DUST, 6)),
            0,
            0,
            ii(CItems.itemMisc.get("ClayCircuit")),
            2L,
            20L
        );
        recipeInscriber.addRecipe(
            ii(CItems.itemMisc.get("ClayCircuitBoard"), CMaterials.get(CMaterials.ENG_CLAY, CMaterials.DUST, 32)),
            0,
            0,
            ii(CItems.itemMisc.get("BasicCircuit")),
            2L,
            20L
        );
        recipeInscriber.addRecipe(
            ii(CMaterials.get(CMaterials.IMPURE_SILICON, CMaterials.PLATE), CMaterials.get(CMaterials.ENG_CLAY, CMaterials.DUST, 32)),
            0,
            0,
            ii(CItems.itemMisc.get("AdvancedCircuit")),
            100L,
            120L
        );
        recipeInscriber.addRecipe(
            ii(CMaterials.get(CMaterials.SILICON, CMaterials.PLATE), CMaterials.get(CMaterials.ENG_CLAY, CMaterials.DUST, 32)),
            0,
            0,
            ii(CItems.itemMisc.get("PrecisionCircuit")),
            1000L,
            120L
        );
        recipeAssembler.addRecipe(
            ii(CItems.itemMisc.get("PrecisionCircuit"), CMaterials.get(CMaterials.ENG_CLAY, CMaterials.DUST, 32)),
            0,
            6,
            ii(CItems.itemMisc.get("IntegratedCircuit")),
            10000L,
            1200L
        );
        recipeAssembler.addRecipe(
            oo(CItems.itemMisc.get("CEE"), CItems.itemMisc.get("IntegratedCircuit")), 0, 6, ii(CItems.itemMisc.get("LaserParts")), e(6), 20L
        );
        recipeAssembler.addRecipe(
            oo(CMaterials.get(CMaterials.BERYLLIUM, CMaterials.DUST, 8), CItems.itemMisc.get("IntegratedCircuit")),
            0,
            6,
            ii(CItems.itemMisc.get("SynchronousParts")),
            e(6),
            432000L
        );
        recipeReactor.addRecipe(
            oo(CMaterials.get(CMaterials.PURE_ANTIMATTER, CMaterials.GEM, 8), CItems.itemMisc.get("IntegratedCircuit")),
            0,
            11,
            ii(CItems.itemMisc.get("TeleportationParts")),
            e(11),
            10000000000000L
        );
        recipeReactor.addRecipe(
            oo(CItems.itemMisc.get("IntegratedCircuit", 6), CMaterials.get(CMaterials.EXC_CLAY, CMaterials.DUST)),
            0,
            7,
            ii(CItems.itemMisc.get("ClayCore")),
            e(10.0, 7),
            8000000L
        );
        recipeReactor.addRecipe(
            oo(CItems.itemMisc.get("ClayCore", 6), CMaterials.get(CMaterials.EXC_CLAY, CMaterials.DUST, 12)),
            0,
            8,
            ii(CItems.itemMisc.get("ClayBrain")),
            e(10.0, 8),
            4000000000L
        );
        recipeReactor.addRecipe(
            oo(CItems.itemMisc.get("ClayBrain", 6), CMaterials.get(CMaterials.EXC_CLAY, CMaterials.DUST, 32)),
            0,
            9,
            ii(CItems.itemMisc.get("ClaySpirit")),
            e(10.0, 9),
            10000000000000L
        );
        recipeReactor.addRecipe(
            oo(CItems.itemMisc.get("ClaySpirit", 6), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM, 4)),
            0,
            10,
            ii(CItems.itemMisc.get("ClaySoul")),
            e(10.0, 10),
            10000000000000L
        );
        recipeReactor.addRecipe(
            oo(CItems.itemMisc.get("ClaySoul", 6), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM, 16)),
            0,
            11,
            ii(CItems.itemMisc.get("ClayAnima")),
            e(30.0, 11),
            100000000000000L
        );
        recipeReactor.addRecipe(
            oo(CItems.itemMisc.get("ClayAnima", 6), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM, 64)),
            0,
            12,
            ii(CItems.itemMisc.get("ClayPsyche")),
            e(90.0, 12),
            1000000000000000L
        );
    }

    public static void registerMachineRecipes() {
        GameRegistry.addRecipe(i(CBlocks.blockClayWorkTable), new Object[]{"##", "##", '#', CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.BLOCK)});
        GameRegistry.addRecipe(i(CBlocks.blockClayCraftingTable), new Object[]{"###", '#', CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.BLOCK)});
        GameRegistry.addShapelessRecipe(CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.BLOCK, 3), new Object[]{i(CBlocks.blockClayCraftingTable)});

        for (int i = 1; i <= 4; i++) {
            GameRegistry.addRecipe(
                i(CBlocks.blocksBendingMachine[i]),
                new Object[]{
                    "o-*",
                    "P#P",
                    "o-*",
                    '#',
                    machines[i],
                    'o',
                    CMaterials.get(cmats[i], CMaterials.SPINDLE),
                    '-',
                    CMaterials.get(cmats[i], CMaterials.CYLINDER),
                    '*',
                    CMaterials.get(cmats[i], CMaterials.GEAR),
                    'P',
                    CMaterials.get(cmats[i], CMaterials.PLATE)
                }
            );
            GameRegistry.addRecipe(
                i(CBlocks.blocksWireDrawingMachine[i]),
                new Object[]{
                    "*o*",
                    "=#=",
                    "*o*",
                    '#',
                    machines[i],
                    'o',
                    CMaterials.get(cmats[i], CMaterials.SPINDLE),
                    '*',
                    CMaterials.get(cmats[i], CMaterials.GEAR),
                    '=',
                    CMaterials.get(cmats[i], CMaterials.PIPE)
                }
            );
            GameRegistry.addRecipe(
                i(CBlocks.blocksPipeDrawingMachine[i]),
                new Object[]{
                    "*o*",
                    "-#=",
                    "*o*",
                    '#',
                    machines[i],
                    'o',
                    CMaterials.get(cmats[i], CMaterials.SPINDLE),
                    '-',
                    CMaterials.get(cmats[i], CMaterials.CYLINDER),
                    '*',
                    CMaterials.get(cmats[i], CMaterials.GEAR),
                    '=',
                    CMaterials.get(cmats[i], CMaterials.PIPE)
                }
            );
            GameRegistry.addRecipe(
                i(CBlocks.blocksCuttingMachine[i]),
                new Object[]{
                    "P*P",
                    "o#|",
                    "P*P",
                    '#',
                    machines[i],
                    'o',
                    CMaterials.get(cmats[i], CMaterials.SPINDLE),
                    '*',
                    CMaterials.get(cmats[i], CMaterials.GEAR),
                    'P',
                    CMaterials.get(cmats[i], CMaterials.PLATE),
                    '|',
                    CMaterials.get(cmats[i], CMaterials.CUTTING_HEAD)
                }
            );
            GameRegistry.addRecipe(
                i(CBlocks.blocksLathe[i]),
                new Object[]{
                    "P*P",
                    "-#o",
                    "P*P",
                    '#',
                    machines[i],
                    'o',
                    CMaterials.get(cmats[i], CMaterials.SPINDLE),
                    '*',
                    CMaterials.get(cmats[i], CMaterials.GEAR),
                    'P',
                    CMaterials.get(cmats[i], CMaterials.PLATE),
                    '-',
                    CMaterials.get(cmats[i], CMaterials.STICK)
                }
            );
            GameRegistry.addRecipe(
                i(CBlocks.blocksCobblestoneGenerator[i]),
                new Object[]{
                    " * ", "=#=", " * ", '#', machines[i], '*', CMaterials.get(cmats[i], CMaterials.GEAR), '=', CMaterials.get(cmats[i], CMaterials.PIPE)
                }
            );
            if (i == 1) {
                GameRegistry.addRecipe(
                    i(CBlocks.blockElementalMillingMachine),
                    new Object[]{
                        "P0P",
                        "o#o",
                        "P*P",
                        '#',
                        machines[i],
                        'o',
                        CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.SPINDLE),
                        '*',
                        CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.GEAR),
                        'P',
                        CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.PLATE),
                        '0',
                        CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.CUTTING_HEAD)
                    }
                );
                GameRegistry.addShapelessRecipe(i(CBlocks.blockClayWaterWheel), new Object[]{machines[i], CMaterials.get(mats[i - 1], CMaterials.WATER_WHEEL)});
            }

            if (i >= 2) {
                GameRegistry.addRecipe(
                    i(CBlocks.blocksGrinder[i]),
                    new Object[]{
                        "P0P",
                        "o#o",
                        "P*P",
                        '#',
                        machines[i],
                        'o',
                        CMaterials.get(cmats[i], CMaterials.SPINDLE),
                        '*',
                        CMaterials.get(cmats[i], CMaterials.GEAR),
                        'P',
                        CMaterials.get(cmats[i], CMaterials.PLATE),
                        '0',
                        CMaterials.get(cmats[i], CMaterials.GRINDING_HEAD)
                    }
                );
                GameRegistry.addRecipe(
                    i(CBlocks.blocksDecomposer[i]),
                    new Object[]{
                        "*o*",
                        "C#C",
                        "*=*",
                        '#',
                        machines[i],
                        'o',
                        CMaterials.get(cmats[i], CMaterials.SPINDLE),
                        '*',
                        CMaterials.get(cmats[i], CMaterials.GEAR),
                        '=',
                        CMaterials.get(cmats[i], CMaterials.PIPE),
                        'C',
                        circuits[i]
                    }
                );
                GameRegistry.addRecipe(
                    i(CBlocks.blocksCondenser[i]),
                    new Object[]{
                        "*P*", "P#P", "*P*", '#', machines[i], '*', CMaterials.get(cmats[i], CMaterials.GEAR), 'P', CMaterials.get(cmats[i], CMaterials.PLATE)
                    }
                );
            }

            if (i == 2) {
                GameRegistry.addShapelessRecipe(
                    i(CBlocks.blockDenseClayWaterWheel), new Object[]{machines[i], CMaterials.get(cmats[i], CMaterials.WATER_WHEEL)}
                );
            }

            if (i >= 3) {
                GameRegistry.addRecipe(
                    i(CBlocks.blocksCentrifuge[i]),
                    new Object[]{
                        "*o*",
                        "o#o",
                        "*o*",
                        '#',
                        machines[i],
                        'o',
                        CMaterials.get(cmats[i], CMaterials.SPINDLE),
                        '*',
                        CMaterials.get(cmats[i], CMaterials.GEAR)
                    }
                );
                GameRegistry.addRecipe(
                    i(CBlocks.blocksInscriber[i]),
                    new Object[]{
                        "*o*",
                        "C#C",
                        "*C*",
                        '#',
                        machines[i],
                        'o',
                        CMaterials.get(cmats[i], CMaterials.SPINDLE),
                        '*',
                        CMaterials.get(cmats[i], CMaterials.GEAR),
                        'C',
                        circuits[i]
                    }
                );
                GameRegistry.addRecipe(
                    i(CBlocks.blocksAssembler[i]),
                    new Object[]{
                        "*C*",
                        "o#o",
                        "*C*",
                        '#',
                        machines[i],
                        'o',
                        CMaterials.get(cmats[i], CMaterials.SPINDLE),
                        '*',
                        CMaterials.get(cmats[i], CMaterials.GEAR),
                        'C',
                        circuits[i]
                    }
                );
                GameRegistry.addRecipe(
                    i(CBlocks.blocksMillingMachine[i]),
                    new Object[]{
                        "P0P",
                        "o#o",
                        "P*P",
                        '#',
                        machines[i],
                        'o',
                        CMaterials.get(cmats[i], CMaterials.SPINDLE),
                        '*',
                        CMaterials.get(cmats[i], CMaterials.GEAR),
                        'P',
                        CMaterials.get(cmats[i], CMaterials.PLATE),
                        '0',
                        CMaterials.get(cmats[i], CMaterials.CUTTING_HEAD)
                    }
                );
            }

            if (i == 3) {
                GameRegistry.addRecipe(
                    i(CBlocks.blockEnergeticClayCondenser),
                    new Object[]{
                        "P*P",
                        "E#E",
                        "PCP",
                        '#',
                        machines[i],
                        'P',
                        CMaterials.get(cmats[i], CMaterials.PLATE),
                        '*',
                        CMaterials.get(cmats[i], CMaterials.GEAR),
                        'C',
                        circuits[i + 1],
                        'E',
                        CItems.itemMisc.get("CEE")
                    }
                );
            }

            if (i == 4) {
                GameRegistry.addRecipe(
                    i(CBlocks.blockEnergeticClayCondenserMK2),
                    new Object[]{
                        "P*P",
                        "E#E",
                        "PCP",
                        '#',
                        machines[i],
                        'P',
                        CMaterials.get(cmats[i], CMaterials.PLATE),
                        '*',
                        CMaterials.get(cmats[i], CMaterials.GEAR),
                        'C',
                        circuits[i + 1],
                        'E',
                        CItems.itemMisc.get("CEE")
                    }
                );
            }
        }

        for (int i = 1; i <= 13; i++) {
            if (i == 1) {
                recipeAssembler.addRecipe(
                    ii(machines[i], CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.CUTTING_HEAD)),
                    0,
                    4,
                    ii(i(CBlocks.blockElementalMillingMachine)),
                    e(i),
                    120L
                );
            }

            if (i >= 1) {
                if (i <= 3) {
                    recipeAssembler.addRecipe(
                        ii(CMaterials.get(mats[i], CMaterials.LARGE_PLATE), circuits[3]), 0, 4, ii(i(CBlocks.blocksCobblestoneGenerator[i])), e(i), 40L
                    );
                }

                if (i <= 4) {
                    recipeAssembler.addRecipe(
                        ii(machines[i], CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.PLATE, 3)), 0, 4, ii(i(CBlocks.blocksBendingMachine[i])), e(i), 120L
                    );
                    recipeAssembler.addRecipe(
                        ii(machines[i], CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.PIPE, 2)),
                        0,
                        4,
                        ii(i(CBlocks.blocksWireDrawingMachine[i])),
                        e(i),
                        120L
                    );
                    recipeAssembler.addRecipe(
                        ii(machines[i], CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.CYLINDER, 2)),
                        0,
                        4,
                        ii(i(CBlocks.blocksPipeDrawingMachine[i])),
                        e(i),
                        120L
                    );
                    recipeAssembler.addRecipe(
                        ii(machines[i], CMaterials.get(CMaterials.CLAY, CMaterials.CUTTING_HEAD)), 0, 4, ii(i(CBlocks.blocksCuttingMachine[i])), e(i), 120L
                    );
                    recipeAssembler.addRecipe(
                        ii(machines[i], CMaterials.get(CMaterials.CLAY, CMaterials.SPINDLE)), 0, 4, ii(i(CBlocks.blocksLathe[i])), e(i), 120L
                    );
                }
            }

            if (i >= 2) {
                if (i <= 3) {
                    recipeAssembler.addRecipe(
                        oo(machines[i], CMaterials.get(mats[i], CMaterials.LARGE_PLATE)), 0, 4, ii(i(CBlocks.blocksCondenser[i])), e(i), 120L
                    );
                }

                if (i <= 4) {
                    recipeAssembler.addRecipe(
                        oo(machines[i], CMaterials.get(CMaterials.CLAY, CMaterials.GEAR, 4)), 0, 4, ii(i(CBlocks.blocksDecomposer[i])), e(i), 120L
                    );
                }

                if (i <= 6) {
                    recipeAssembler.addRecipe(
                        ii(machines[i], CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.GRINDING_HEAD, 1)), 0, 4, ii(i(CBlocks.blocksGrinder[i])), e(i), 120L
                    );
                }
            }

            if (i == 3) {
                recipeAssembler.addRecipe(ii(machines[i], CItems.itemMisc.get("CEE", 2)), 0, 4, ii(i(CBlocks.blockEnergeticClayCondenser)), e(i), 120L);
            }

            if (i >= 3) {
                if (i <= 4) {
                    recipeAssembler.addRecipe(
                        ii(machines[i], CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.CUTTING_HEAD)),
                        0,
                        4,
                        ii(i(CBlocks.blocksMillingMachine[i])),
                        e(i),
                        120L
                    );
                    recipeAssembler.addRecipe(
                        ii(machines[i], CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.GEAR, 4)), 0, 4, ii(i(CBlocks.blocksAssembler[i])), e(i), 40L
                    );
                    recipeAssembler.addRecipe(ii(i(CBlocks.blocksAssembler[i]), circuits[4]), 0, 4, ii(i(CBlocks.blocksInscriber[i])), e(i), 40L);
                }

                if (i <= 6) {
                    recipeAssembler.addRecipe(
                        ii(machines[i], CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.SPINDLE, Math.max(i - 4, 1))),
                        0,
                        4,
                        ii(i(CBlocks.blocksCentrifuge[i])),
                        e(i),
                        120L
                    );
                }
            }

            if (i == 4) {
                recipeAssembler.addRecipe(ii(machines[i], CItems.itemMisc.get("CEE", 2)), 0, 4, ii(i(CBlocks.blockEnergeticClayCondenserMK2)), e(i), 120L);
            }

            if (i >= 4) {
                recipeAssembler.addRecipe(ii(CMaterials.get(mats[i], CMaterials.PLATE), circuits[i]), 0, 4, ii(i(CBlocks.blocksBuffer[i], 16)), e(i), 40L);
                recipeAssembler.addRecipe(
                    ii(i(CBlocks.blocksBuffer[i], 6), CMaterials.get(mats[i], CMaterials.LARGE_PLATE)),
                    0,
                    4,
                    ii(i(CBlocks.blocksMultitrackBuffer[i])),
                    e(i),
                    40L
                );
                if (ClayiumCore.cfgEnableFluidCapsule) {
                    recipeAssembler.addRecipe(
                        ii(i(CBlocks.blocksBuffer[i]), i(CItems.itemsCapsule[0], 4)), 0, 4, ii(i(CBlocks.blocksFluidTranslator[i])), e(i), 40L
                    );
                }

                if (i <= 7) {
                    recipeAssembler.addRecipe(ii(i(CBlocks.blocksBuffer[i]), circuits[3]), 0, 4, ii(i(CBlocks.blocksCobblestoneGenerator[i])), e(i), 40L);
                    recipeAssembler.addRecipe(ii(i(CBlocks.blocksBuffer[i]), circuits[4]), 0, 4, ii(i(CBlocks.blocksSaltExtractor[i])), e(i), 40L);
                    recipeAssembler.addRecipe(ii(machines[i], s(circuits[3], i - 3)), 0, 4, ii(i(CBlocks.blocksSmelter[i])), e(i), 120L);
                    recipeAssembler.addRecipe(
                        ii(CMaterials.get(CMaterials.AZ91D_ALLOY, CMaterials.PLATE, 4), circuits[i]), 0, 4, ii(i(CItems.itemsClayShooter[i - 4])), e(i), 40L
                    );
                }

                if (i <= 5) {
                    recipeAssembler.addRecipe(oo(machines[i], i(CBlocks.blocksBuffer[i])), 0, 4, ii(i(CBlocks.blocksCondenser[i])), e(i), 120L);
                    recipeAssembler.addRecipe(ii(machines[i], s(circuits[4], i - 3)), 0, 4, ii(i(CBlocks.blocksChemicalReactor[i])), e(i), 120L);
                }
            }

            if (i >= 5) {
                recipeAssembler.addRecipe(ii(machines[i], i(CBlocks.blocksBuffer[6])), 0, 4, ii(i(CBlocks.blocksClayInterface[i])), e(i), 40L);
                recipeAssembler.addRecipe(
                    ii(i(CBlocks.blocksClayInterface[i]), CMaterials.get(CMaterials.ENG_CLAY, CMaterials.DUST, 16)),
                    0,
                    4,
                    ii(i(CBlocks.blocksRedstoneInterface[i])),
                    e(i),
                    40L
                );
                if (i <= 7) {
                    recipeAssembler.addRecipe(
                        ii(machines[i], CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.PLATE, (i - 4) * 3)),
                        0,
                        4,
                        ii(i(CBlocks.blocksBendingMachine[i])),
                        e(i),
                        120L
                    );
                }
            }

            if (i == 5) {
                recipeAssembler.addRecipe(
                    oo(machines[i], CMaterials.getOD(CMaterials.SILICON, CMaterials.PLATE, 8)), 0, 4, ii(i(CBlocks.blockSolarClayFabricatorMK1)), e(i), 120L
                );
                recipeAssembler.addRecipe(ii(i(CBlocks.blocksBuffer[i]), circuits[5]), 0, 4, ii(i(CBlocks.blockAutoClayCondenser)), e(i), 40L);
                recipeAssembler.addRecipe(ii(i(CBlocks.blocksAssembler[4]), machines[i]), 0, 4, ii(i(CBlocks.blocksAutoCrafter[i])), e(i), 40L);
                if (ClayiumCore.cfgEnableFluidCapsule) {
                    recipeAssembler.addRecipe(
                        ii(i(CBlocks.blocksFluidTranslator[i], 2), machines[i]), 0, 4, ii(i(CBlocks.blocksFluidTransferMachine[i])), e(i), 40L
                    );
                }
            }

            if (i >= 6 && i <= 9) {
                recipeAssembler.addRecipe(ii(i(CBlocks.blocksChemicalReactor[5]), circuits[i]), 0, 4, ii(i(CBlocks.blocksElectrolysisReactor[i])), e(i), 40L);
                recipeAssembler.addRecipe(ii(i(CBlocks.blocksAutoCrafter[i - 1]), machines[i]), 0, 4, ii(i(CBlocks.blocksAutoCrafter[i])), e(i), 40L);
            }

            if (i == 6) {
                recipeAssembler.addRecipe(
                    oo(machines[i], CMaterials.getOD(CMaterials.SILICON, CMaterials.PLATE, 16)), 0, 4, ii(i(CBlocks.blockSolarClayFabricatorMK2)), e(i), 120L
                );
                recipeAssembler.addRecipe(
                    ii(i(CBlocks.blocksSmelter[i]), i(CBlocks.blocksClayInterface[i])), 0, 4, ii(i(CBlocks.blockClayBlastFurnace)), e(i), 120L
                );
                recipeAssembler.addRecipe(
                    ii(i(CBlocks.blocksChemicalReactor[5]), i(CBlocks.blocksSmelter[i])), 0, 4, ii(i(CBlocks.blockChemicalMetalSeparator)), e(i), 40L
                );
                recipeAssembler.addRecipe(
                    ii(machines[i], CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.GEAR, 4)), 0, 4, ii(i(CBlocks.blocksAssembler[i])), e(i), 40L
                );
                recipeAssembler.addRecipe(ii(i(CBlocks.blocksAssembler[4]), circuits[i]), 0, 4, ii(i(CBlocks.blocksAssembler[6])), e(i), 40L);
                recipeAssembler.addRecipe(ii(i(CBlocks.blocksSmelter[i]), circuits[i]), 0, 4, ii(i(CBlocks.blocksAlloySmelter[i])), e(i), 40L);
            }

            if (i == 7) {
                recipeAssembler.addRecipe(
                    oo(machines[i], CMaterials.getOD(CMaterials.SILICON, CMaterials.PLATE, 16)),
                    0,
                    4,
                    ii(i(CBlocks.blockLithiumSolarClayFabricator)),
                    e(i),
                    120L
                );
                recipeAssembler.addRecipe(ii(i(CBlocks.blocksBuffer[i]), circuits[5]), 0, 4, ii(i(CBlocks.blockAutoClayCondenserMK2)), e(i), 40L);
                recipeAssembler.addRecipe(oo(machines[i], i(CBlocks.blocksClayLaserInterface[i])), 0, 6, ii(i(CBlocks.blockClayReactor)), e(i), 1200L);
            }

            if (i >= 7) {
                recipeAssembler.addRecipe(
                    oo(i(CBlocks.blocksBuffer[i]), CItems.itemMisc.get("LaserParts", 1)), 0, 6, ii(i(CBlocks.blocksClayLaserInterface[i])), e(i), 120L
                );
                if (i <= 9) {
                    recipeAssembler.addRecipe(oo(i(CBlocks.blocksBuffer[i]), machines[i]), 0, 6, ii(i(CBlocks.blocksDistributor[i])), e(i), 120L);
                    recipeAssembler.addRecipe(oo(i(CBlocks.blocksBuffer[i]), circuits[7]), 0, 6, ii(i(CBlocks.blocksDistributor[i])), e(i), 120L);
                    recipeAssembler.addRecipe(
                        oo(i(CBlocks.blockClayReactor), i(CBlocks.blocksElectrolysisReactor[i])), 0, 6, ii(i(CBlocks.blocksTransformer[i])), e(i), 120L
                    );
                }

                if (i <= 10) {
                    recipeAssembler.addRecipe(oo(machines[i], CItems.itemMisc.get("LaserParts", 4)), 0, 6, ii(i(CBlocks.blocksClayEnergyLaser[i])), e(i), 480L);
                }
            }

            if (i == 8) {
                recipeAssembler.addRecipe(ii(machines[i], i(CBlocks.blocksChemicalReactor[5])), 0, 4, ii(i(CBlocks.blocksChemicalReactor[i])), e(i), 480L);
            }

            if (i >= 8 && i <= 9) {
                recipeAssembler.addRecipe(ii(machines[i], i(CBlocks.blocksSmelter[i - 1], 16)), 0, 6, ii(i(CBlocks.blocksSmelter[i])), e(i), 2000L);
            }

            if (i == 9) {
                recipeAssembler.addRecipe(oo(machines[i], i(CBlocks.blocksBendingMachine[7], 4)), 0, 6, ii(i(CBlocks.blocksBendingMachine[i])), e(i), 480L);
            }

            if (i >= 9) {
                if (i == 9) {
                    recipeAssembler.addRecipe(oo(machines[i], i(CBlocks.blocksTransformer[i], 16)), 0, 6, ii(i(CBlocks.blocksCACondenser[i])), e(i), 480L);
                    recipeAssembler.addRecipe(oo(machines[i], i(CBlocks.blockClayReactor, 16)), 0, 6, ii(i(CBlocks.blocksCAInjector[i])), e(i), 480L);
                } else {
                    if (i <= 11) {
                        recipeAssembler.addRecipe(oo(machines[i], i(CBlocks.blocksTransformer[i], 16)), 0, 10, ii(i(CBlocks.blocksCACondenser[i])), e(i), 480L);
                    }

                    recipeAssembler.addRecipe(oo(machines[i], i(CBlocks.blocksCAInjector[i - 1], 2)), 0, 10, ii(i(CBlocks.blocksCAInjector[i])), e(i), 480L);
                }
            }

            if (i == 10) {
                recipeAssembler.addRecipe(ii(machines[i], i(CBlocks.blocksAssembler[6])), 0, 6, ii(i(CBlocks.blocksAssembler[i])), e(i), 40L);
            }

            if (i >= 10) {
                recipeAssembler.addRecipe(oo(machines[i], i(CBlocks.blockClayReactor, 16)), 0, 10, ii(i(CBlocks.blocksCAReactorCore[i])), e(i), 120L);
                if (i <= 12) {
                    recipeAssembler.addRecipe(
                        oo(i(CBlocks.blocksCAInjector[i]), i(CBlocks.blockClayReactor)), 0, 10, ii(i(CBlocks.blocksTransformer[i])), e(i), 120L
                    );
                }
            }

            if (i == 13) {
                recipeAssembler.addRecipe(
                    oo(machines[i], CBlocks.blockCAReactorCoil.get("opa")), 0, 10, ii(i(CBlocks.blockEnergeticClayDecomposer)), e(i), 120L
                );
            }
        }

        if (ClayiumCore.cfgEnableRFGenerator) {
            Map<String, Map<String, Object>> map = ClayRFGenerator.getConfigMap();
            Map<Integer, List<Block>> blockList = new HashMap<>();

            for (Entry<String, Map<String, Object>> entry : map.entrySet()) {
                Map<String, Object> config = entry.getValue();
                if (config != null && config.get("Tier") instanceof Number) {
                    int tier = ((Number)config.get("Tier")).intValue();
                    Block blockRFGenerator = CBlocks.blocksRFGenerator.get(entry.getKey());
                    List<Block> list = (List<Block>)(blockList.containsKey(tier) ? blockList.get(tier) : new ArrayList<>());
                    list.add(blockRFGenerator);
                    blockList.put(tier, list);
                }
            }

            for (Entry<Integer, List<Block>> entryx : blockList.entrySet()) {
                int tier = entryx.getKey();
                List<Block> list = entryx.getValue();

                for (int i = 0; i < list.size(); i++) {
                    Block block = tier >= 0 && tier < CBlocks.blocksRedstoneInterface.length && CBlocks.blocksRedstoneInterface[tier] != null
                        ? CBlocks.blocksRedstoneInterface[tier]
                        : CBlocks.blocksRedstoneInterface[5];
                    ItemStack prev = i == 0 ? machines[tier] : i(list.get(i - 1));
                    recipeAssembler.addRecipe(oo(prev, i(block, 1)), 0, 4, ii(i(list.get(i))), e(tier), 120L);
                }
            }
        }

        recipeAssembler.addRecipe(oo(CMaterials.getOD(CMaterials.QUARTZ, CMaterials.DUST, 16)), 0, 0, ii(i(CBlocks.blockQuartzCrucible)), 1000L, 20L);
        recipeBlastFurnace.addRecipe(
            oo(CMaterials.get(CMaterials.IND_CLAY, CMaterials.DUST), CMaterials.getOD(CMaterials.QUARTZ, CMaterials.DUST, 8)),
            0,
            7,
            ii(i(CBlocks.blockLaserReflector)),
            e(0.2, 7),
            100L
        );
        recipeReactor.addRecipe(ii(machines[8], i(CBlocks.blockLithiumSolarClayFabricator)), 0, 8, ii(i(CBlocks.blockClayFabricatorMK1)), e(3.0, 8), 100000000L);
        recipeReactor.addRecipe(
            ii(machines[9], i(CBlocks.blockLithiumSolarClayFabricator)), 0, 9, ii(i(CBlocks.blockClayFabricatorMK2)), e(3.0, 9), 100000000000L
        );
        recipeReactor.addRecipe(
            ii(i(CBlocks.blockClayFabricatorMK2, 64), CBlocks.blockOverclocker.get("opa", 16)),
            0,
            13,
            ii(i(CBlocks.blockClayFabricatorMK3)),
            e(10.0, 13),
            1000000000000000000L
        );
        recipeCAInjector.addRecipe(
            ii(machines[9], CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM, 8)), 0, 9, ii(CBlocks.blockResonator.get("antimatter")), e(2.0, 9), 4000L
        );
        recipeCAInjector.addRecipe(
            ii(CBlocks.blockResonator.get("antimatter", 16), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM, 64)),
            0,
            11,
            ii(CBlocks.blockResonator.get("pureantimatter")),
            e(2.0, 11),
            4000L
        );
        recipeCAInjector.addRecipe(
            ii(CBlocks.blockResonator.get("pureantimatter", 16), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM, 64)),
            0,
            12,
            ii(CBlocks.blockResonator.get("oec")),
            e(2.0, 12),
            4000L
        );
        recipeCAInjector.addRecipe(
            ii(CBlocks.blockResonator.get("oec", 16), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM, 64)),
            0,
            13,
            ii(CBlocks.blockResonator.get("opa")),
            e(2.0, 13),
            4000L
        );
        recipeCAInjector.addRecipe(
            ii(CBlocks.blockOverclocker.get("antimatter"), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM, 8)),
            0,
            9,
            ii(CBlocks.blockEnergyStorageUpgrade.get("antimatter")),
            e(2.0, 9),
            4000L
        );
        recipeCAInjector.addRecipe(
            ii(CBlocks.blockEnergyStorageUpgrade.get("antimatter", 16), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM, 64)),
            0,
            11,
            ii(CBlocks.blockEnergyStorageUpgrade.get("pureantimatter")),
            e(2.0, 11),
            4000L
        );
        recipeCAInjector.addRecipe(
            ii(CBlocks.blockEnergyStorageUpgrade.get("pureantimatter", 16), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM, 64)),
            0,
            12,
            ii(CBlocks.blockEnergyStorageUpgrade.get("oec")),
            e(2.0, 12),
            4000L
        );
        recipeCAInjector.addRecipe(
            ii(CBlocks.blockEnergyStorageUpgrade.get("oec", 16), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM, 64)),
            0,
            13,
            ii(CBlocks.blockEnergyStorageUpgrade.get("opa")),
            e(2.0, 13),
            4000L
        );
        recipeReactor.addRecipe(
            oo(s(machines[10], 1), CBlocks.blockResonator.get("antimatter", 8)),
            0,
            10,
            ii(CBlocks.blockOverclocker.get("antimatter")),
            e(5.0, 10),
            10000000000000L
        );
        recipeReactor.addRecipe(
            oo(s(machines[11], 4), CBlocks.blockResonator.get("pureantimatter", 16)),
            0,
            11,
            ii(CBlocks.blockOverclocker.get("pureantimatter")),
            e(5.0, 11),
            100000000000000L
        );
        recipeReactor.addRecipe(
            oo(s(machines[12], 16), CBlocks.blockResonator.get("oec", 32)), 0, 12, ii(CBlocks.blockOverclocker.get("oec")), e(5.0, 12), 1000000000000000L
        );
        recipeReactor.addRecipe(
            oo(s(machines[13], 64), CBlocks.blockResonator.get("opa", 64)), 0, 13, ii(CBlocks.blockOverclocker.get("opa")), e(5.0, 13), 1000000000000000L
        );
        recipeReactor.addRecipe(
            oo(CMaterials.get(mats[10], CMaterials.PLATE, 6), CMaterials.getOD(CMaterials.PLATINUM, CMaterials.INGOT)),
            0,
            10,
            ii(CBlocks.blockCAReactorCoil.get("antimatter")),
            e(10),
            10000000000000L
        );
        recipeReactor.addRecipe(
            oo(CMaterials.get(mats[11], CMaterials.PLATE, 6), CMaterials.getOD(CMaterials.IRIDIUM, CMaterials.INGOT, 4)),
            0,
            11,
            ii(CBlocks.blockCAReactorCoil.get("pureantimatter")),
            e(11),
            100000000000000L
        );
        recipeReactor.addRecipe(
            oo(CMaterials.get(mats[12], CMaterials.PLATE, 6), CMaterials.getOD(CMaterials.MAIN_OSMIUM, CMaterials.INGOT, 16)),
            0,
            12,
            ii(CBlocks.blockCAReactorCoil.get("oec")),
            e(12),
            1000000000000000L
        );
        recipeReactor.addRecipe(
            oo(CMaterials.get(mats[13], CMaterials.PLATE, 6), CMaterials.getOD(CMaterials.RHENIUM, CMaterials.INGOT, 64)),
            0,
            13,
            ii(CBlocks.blockCAReactorCoil.get("opa")),
            e(13),
            1000000000000000L
        );
        recipeAssembler.addRecipe(
            oo(CMaterials.get(CMaterials.PURE_ANTIMATTER, CMaterials.GEM, 3), new OreDictionaryStack("blockGlass", 2)),
            0,
            10,
            ii(i(CBlocks.blockPANCable, 12)),
            e(10),
            2L
        );
        recipeReactor.addRecipe(oo(i(CBlocks.blocksPANAdapter[10], 4), circuits[11]), 0, 11, ii(i(CBlocks.blockPANCore)), e(11), 100000000000000L);
        recipeAssembler.addRecipe(
            ii(CBlocks.blockResonator.get("antimatter"), i(CBlocks.blockPANCable, 6)), 0, 10, ii(i(CBlocks.blocksPANAdapter[10])), e(10), 60L
        );
        recipeAssembler.addRecipe(
            ii(CBlocks.blockResonator.get("pureantimatter"), i(CBlocks.blockPANCable, 6)), 0, 10, ii(i(CBlocks.blocksPANAdapter[11])), e(11), 60L
        );
        recipeAssembler.addRecipe(ii(CBlocks.blockResonator.get("oec"), i(CBlocks.blockPANCable, 6)), 0, 10, ii(i(CBlocks.blocksPANAdapter[12])), e(12), 60L);
        recipeAssembler.addRecipe(ii(CBlocks.blockResonator.get("opa"), i(CBlocks.blockPANCable, 6)), 0, 10, ii(i(CBlocks.blocksPANAdapter[13])), e(13), 60L);
        recipeAssembler.addRecipe(oo(machines[4], i(CBlocks.blockPANCable, 4)), 0, 10, ii(i(CBlocks.blocksPANDuplicator[4])), e(10), 20L);
        CMaterial[] panDuplicatorMats = new CMaterial[]{
            CMaterials.RUBIDIUM,
            CMaterials.LANTHANUM,
            CMaterials.CAESIUM,
            CMaterials.FRANCIUM,
            CMaterials.RADIUM,
            CMaterials.TANTALUM,
            CMaterials.BISMUTH,
            CMaterials.ACTINIUM,
            CMaterials.VANADIUM
        };

        for (int i = 0; i <= 8; i++) {
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    i(CBlocks.blocksPANDuplicator[i + 5]),
                    oo(
                        "#I#",
                        "DMD",
                        "#I#",
                        '#',
                        CMaterials.get(CMaterials.COMPRESSED_PURE_ANTIMATTER[i], CMaterials.GEM),
                        'D',
                        i(CBlocks.blocksPANDuplicator[i + 4]),
                        'I',
                        CMaterials.getODName(panDuplicatorMats[i], CMaterials.INGOT),
                        'M',
                        machines[i + 5]
                    )
                )
            );
        }

        recipeAssembler.addRecipe(
            ii(CBlocks.blockOthersHull.get("az91d", 4), i(CBlocks.blocksClayInterface[5])),
            0,
            4,
            ii(s(StorageContainer.expandStorage(i(CBlocks.blockStorageContainer), 65536), 4)),
            e(6),
            120L
        );
        recipeAssembler.addRecipe(
            ii(CBlocks.blockOthersHull.get("az91d", 4), i(CBlocks.blocksRedstoneInterface[5])), 0, 4, ii(i(CBlocks.blockVacuumContainer, 4)), e(6), 120L
        );
        recipeAssembler.addRecipe(ii(CBlocks.blockOthersHull.get("az91d"), s(circuits[8], 4)), 0, 6, ii(i(CBlocks.blockAutoTrader)), e(8), 120L);
        GameRegistry.addRecipe(
            new ShapelessRecipes(
                StorageContainer.expandStorage(i(CBlocks.blockStorageContainer), Integer.MAX_VALUE),
                li(StorageContainer.expandStorage(i(CBlocks.blockStorageContainer), 65536), s(circuits[8], 1))
            ) {
                public ItemStack func_77572_b(InventoryCrafting inv) {
                    for (int i = 0; i < inv.func_70302_i_(); i++) {
                        ItemStack item = inv.func_70301_a(i);
                        if (StorageContainer.getStorageSize(item) == 65536) {
                            ItemStack copy = item.func_77946_l();
                            copy.field_77994_a = 1;
                            return StorageContainer.expandStorage(copy, Integer.MAX_VALUE);
                        }
                    }

                    return super.func_77572_b(inv);
                }
            }
        );
        GameRegistry.addRecipe(
            i(CBlocks.blockClayChunkLoader), new Object[]{"PCP", "C#C", "PCP", '#', CBlocks.blockOthersHull.get("zk60a"), 'C', circuits[6], 'P', circuits[5]}
        );
        recipeAssembler.addRecipe(ii(i(Blocks.field_150435_aG), circuits[7]), 0, 6, ii(i(CBlocks.blockClayMarker)), e(7), 480L);
        recipeAssembler.addRecipe(ii(i(CBlocks.blockCompressedClay, 1, 0), circuits[8]), 0, 6, ii(i(CBlocks.blockClayOpenPitMarker)), e(8), 480L);
        recipeAssembler.addRecipe(ii(i(CBlocks.blockCompressedClay, 1, 1), circuits[8]), 0, 6, ii(i(CBlocks.blockClayGroundLevelingMarker)), e(8), 480L);
        recipeAssembler.addRecipe(
            ii(i(CBlocks.blockClayOpenPitMarker), i(CBlocks.blockClayGroundLevelingMarker)), 0, 6, ii(i(CBlocks.blockClayPrismMarker)), e(8), 480L
        );
        recipeAssembler.addRecipe(ii(CBlocks.blockOthersHull.get("zk60a"), s(circuits[7], 64)), 0, 6, ii(i(CBlocks.blockAreaCollector)), e(7), 6000L);
        recipeAssembler.addRecipe(ii(CBlocks.blockOthersHull.get("az91d"), circuits[6]), 0, 6, ii(i(CBlocks.blockMiner)), e(6), 60L);
        recipeAssembler.addRecipe(ii(CBlocks.blockOthersHull.get("zk60a"), s(circuits[8], 64)), 0, 6, ii(i(CBlocks.blockAreaMiner)), e(8), 6000L);
        recipeAssembler.addRecipe(ii(i(CBlocks.blockAreaMiner), s(circuits[9], 64)), 0, 6, ii(i(CBlocks.blockAdvancedAreaMiner)), e(9), 6000L);
        recipeAssembler.addRecipe(ii(i(CBlocks.blockAdvancedAreaMiner), s(circuits[10], 64)), 0, 6, ii(i(CBlocks.blockAreaReplacer)), e(10), 6000L);
        recipeAssembler.addRecipe(ii(CBlocks.blockOthersHull.get("az91d"), circuits[5]), 0, 6, ii(i(CBlocks.blockActivator)), e(6), 60L);
        recipeAssembler.addRecipe(ii(i(CBlocks.blockAreaCollector), circuits[8]), 0, 6, ii(i(CBlocks.blockAreaActivator)), e(8), 6000L);
        recipeAssembler.addRecipe(
            oo(i(CItems.itemsClayShooter[3]), CItems.itemMisc.get("TeleportationParts")), 0, 10, ii(i(CItems.itemInstantTeleporter)), e(11), 3000L
        );
        Block[] blocksEnergeticClayCondenser = new Block[16];
        blocksEnergeticClayCondenser[3] = CBlocks.blockEnergeticClayCondenser;
        blocksEnergeticClayCondenser[4] = CBlocks.blockEnergeticClayCondenserMK2;
        blocksEnergeticClayCondenser[5] = CBlocks.blockAutoClayCondenser;
        blocksEnergeticClayCondenser[7] = CBlocks.blockAutoClayCondenserMK2;
        Block[] blocksClayFabricator = new Block[16];
        blocksClayFabricator[5] = CBlocks.blockSolarClayFabricatorMK1;
        blocksClayFabricator[6] = CBlocks.blockSolarClayFabricatorMK2;
        blocksClayFabricator[7] = CBlocks.blockLithiumSolarClayFabricator;
        blocksClayFabricator[8] = CBlocks.blockClayFabricatorMK1;
        blocksClayFabricator[9] = CBlocks.blockClayFabricatorMK2;
        Block[] blocksClayWaterWheel = new Block[16];
        blocksClayWaterWheel[1] = CBlocks.blockClayWaterWheel;
        blocksClayWaterWheel[2] = CBlocks.blockDenseClayWaterWheel;
        Block[][] arrayblocklist = new Block[][]{
            CBlocks.blocksBendingMachine,
            CBlocks.blocksWireDrawingMachine,
            CBlocks.blocksPipeDrawingMachine,
            CBlocks.blocksCuttingMachine,
            CBlocks.blocksLathe,
            CBlocks.blocksCobblestoneGenerator,
            CBlocks.blocksCondenser,
            CBlocks.blocksGrinder,
            CBlocks.blocksDecomposer,
            CBlocks.blocksMillingMachine,
            CBlocks.blocksAssembler,
            CBlocks.blocksInscriber,
            CBlocks.blocksCentrifuge,
            CBlocks.blocksSaltExtractor,
            CBlocks.blocksSmelter,
            CBlocks.blocksBuffer,
            CBlocks.blocksChemicalReactor,
            CBlocks.blocksAlloySmelter,
            CBlocks.blocksElectrolysisReactor,
            CBlocks.blocksClayEnergyLaser,
            CBlocks.blocksDistributor,
            CBlocks.blocksTransformer,
            CBlocks.blocksAutoCrafter,
            CBlocks.blocksCACondenser,
            CBlocks.blocksCAInjector,
            blocksEnergeticClayCondenser,
            blocksClayFabricator,
            blocksClayWaterWheel
        };
        ArrayList<Block[]> blocklists = new ArrayList<>(Arrays.asList(arrayblocklist));
        if (ClayiumCore.cfgEnableInjectorRecipeOfInterface) {
            blocklists.addAll(Arrays.asList(CBlocks.blocksClayInterface, CBlocks.blocksRedstoneInterface, CBlocks.blocksClayLaserInterface));
        }

        for (Block[] blocks : blocklists) {
            int j = -1;
            int n = 0;

            for (int i = 0; i < blocks.length; i++) {
                n = (int)((double)n + Math.pow(1.3F, (double)i));
                if (n >= 64) {
                    n = 64;
                }

                if (blocks[i] != null) {
                    if (j != -1) {
                        recipeCAInjector.addRecipe(
                            oo(i(blocks[j]), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM, n)), 0, i, ii(i(blocks[i])), e(3.0, i), 4000L
                        );
                    }

                    j = i;
                    n = 0;
                }
            }
        }

        for (CMaterial material : MetalChest.getChestMaterialMap().keySet()) {
            CMaterial imaterial = material;
            if (material == CMaterials.OSMIUM) {
                imaterial = CMaterials.MAIN_OSMIUM;
            }

            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    i(CBlocks.blockMetalChest, 1, material.meta),
                    oo("###", "#*#", "###", '#', CMaterials.getODName(imaterial, CMaterials.INGOT), '*', i(Blocks.field_150486_ae, 1, 32767))
                )
            );
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    i(CBlocks.blockMetalChest, 1, material.meta),
                    oo("###", "#*#", "###", '#', CMaterials.getODName(imaterial, CMaterials.INGOT), '*', i(CBlocks.blockMetalChest, 1, 32767))
                )
            );
        }
    }

    public static void registerMaterialRecipes() {
        CShape shape = CMaterials.INGOT;

        for (CMaterial material : new CMaterial[]{
            CMaterials.IMPURE_SILICON,
            CMaterials.SILICONE,
            CMaterials.SILICON,
            CMaterials.ALUMINIUM,
            CMaterials.CLAY_STEEL,
            CMaterials.CLAYIUM,
            CMaterials.ULTIMATE_ALLOY
        }) {
            GameRegistry.addRecipe(
                new ShapelessOreRecipe(
                    CMaterials.get(material, CMaterials.BLOCK),
                    oo(
                        CMaterials.getODName(material, shape),
                        CMaterials.getODName(material, shape),
                        CMaterials.getODName(material, shape),
                        CMaterials.getODName(material, shape),
                        CMaterials.getODName(material, shape),
                        CMaterials.getODName(material, shape),
                        CMaterials.getODName(material, shape),
                        CMaterials.getODName(material, shape),
                        CMaterials.getODName(material, shape)
                    )
                )
            );
            GameRegistry.addRecipe(new ShapelessOreRecipe(CMaterials.get(material, shape, 9), oo(CMaterials.getODName(material, CMaterials.BLOCK))));
        }

        GameRegistry.addRecipe(
            new ShapelessOreRecipe(
                CMaterials.get(CMaterials.ALUMINIUM, CMaterials.BLOCK),
                oo(
                    CMaterials.getODName(CMaterials.ALUMINIUM_OD, shape),
                    CMaterials.getODName(CMaterials.ALUMINIUM_OD, shape),
                    CMaterials.getODName(CMaterials.ALUMINIUM_OD, shape),
                    CMaterials.getODName(CMaterials.ALUMINIUM_OD, shape),
                    CMaterials.getODName(CMaterials.ALUMINIUM_OD, shape),
                    CMaterials.getODName(CMaterials.ALUMINIUM_OD, shape),
                    CMaterials.getODName(CMaterials.ALUMINIUM_OD, shape),
                    CMaterials.getODName(CMaterials.ALUMINIUM_OD, shape),
                    CMaterials.getODName(CMaterials.ALUMINIUM_OD, shape)
                )
            )
        );
        GameRegistry.addRecipe(
            new ShapelessOreRecipe(CMaterials.get(CMaterials.ALUMINIUM, shape, 9), oo(CMaterials.getODName(CMaterials.ALUMINIUM_OD, CMaterials.BLOCK)))
        );
        shape = CMaterials.GEM;

        for (CMaterial material : new CMaterial[]{CMaterials.ANTIMATTER, CMaterials.PURE_ANTIMATTER, CMaterials.OCTUPLE_PURE_ANTIMATTER}) {
            GameRegistry.addRecipe(
                new ShapelessOreRecipe(
                    CMaterials.get(material, CMaterials.BLOCK),
                    oo(
                        CMaterials.getODName(material, shape),
                        CMaterials.getODName(material, shape),
                        CMaterials.getODName(material, shape),
                        CMaterials.getODName(material, shape),
                        CMaterials.getODName(material, shape),
                        CMaterials.getODName(material, shape),
                        CMaterials.getODName(material, shape),
                        CMaterials.getODName(material, shape),
                        CMaterials.getODName(material, shape)
                    )
                )
            );
            GameRegistry.addRecipe(new ShapelessOreRecipe(CMaterials.get(material, shape, 9), oo(CMaterials.getODName(material, CMaterials.BLOCK))));
        }

        GameRegistry.addRecipe(CBlocks.blockMaterial.get("OctupleEnergeticClay"), new Object[]{"###", "###", "###", '#', i(CBlocks.blockCompressedClay, 1, 12)});
        GameRegistry.addShapelessRecipe(i(CBlocks.blockCompressedClay, 9, 12), new Object[]{CBlocks.blockMaterial.get("OctupleEnergeticClay")});
        String[] dyes = new String[]{
            "Black",
            "Red",
            "Green",
            "Brown",
            "Blue",
            "Purple",
            "Cyan",
            "LightGray",
            "Gray",
            "Pink",
            "Lime",
            "Yellow",
            "LightBlue",
            "Magenta",
            "Orange",
            "White"
        };

        for (int i = 0; i < 16; i++) {
            String str = ItemDye.field_150921_b[BlockColored.func_150031_c(i)];
            str = Character.toTitleCase(str.charAt(0)) + str.substring(1);
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    i(CBlocks.blockSiliconeColored, 8, i),
                    oo("###", "#*#", "###", '#', CMaterials.getODName(CMaterials.SILICONE, CMaterials.BLOCK), '*', "dye" + dyes[15 - i])
                )
            );
        }

        recipeGrinder.addRecipe(CMaterials.get(CMaterials.CLAY, CMaterials.BLOCK), CMaterials.get(CMaterials.CLAY, CMaterials.DUST), 1L, 3L);
        recipeGrinder.addRecipe(CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.BLOCK), CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.DUST), 1L, 6L);
        recipeGrinder.addRecipe(CMaterials.get(CMaterials.IND_CLAY, CMaterials.BLOCK), CMaterials.get(CMaterials.IND_CLAY, CMaterials.DUST), 1L, 12L);
        recipeGrinder.addRecipe(CMaterials.get(CMaterials.ADVIND_CLAY, CMaterials.BLOCK), CMaterials.get(CMaterials.ADVIND_CLAY, CMaterials.DUST), 2L, 12L);
        recipeCondenser.addRecipe(CMaterials.get(CMaterials.CLAY, CMaterials.DUST), CMaterials.get(CMaterials.CLAY, CMaterials.BLOCK), 1L, 3L);
        recipeCondenser.addRecipe(CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.DUST), CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.BLOCK), 1L, 6L);
        CMaterial[] materials = new CMaterial[]{
            CMaterials.IMPURE_SILICON,
            CMaterials.SILICON,
            CMaterials.SILICONE,
            CMaterials.ALUMINIUM,
            CMaterials.CLAY_STEEL,
            CMaterials.CLAYIUM,
            CMaterials.ULTIMATE_ALLOY,
            CMaterials.AZ91D_ALLOY,
            CMaterials.ZK60A_ALLOY
        };

        for (CMaterial material : materials) {
            recipeBendingMachine.addRecipe(
                CMaterials.getOD(material, CMaterials.INGOT), 4, CMaterials.get(material, CMaterials.PLATE), e(4), (long)((int)(20.0F * material.hardness))
            );
            recipeBendingMachine.addRecipe(
                CMaterials.getOD(material, CMaterials.PLATE, 4),
                4,
                CMaterials.get(material, CMaterials.LARGE_PLATE),
                e(4),
                (long)((int)(40.0F * material.hardness))
            );
            recipeGrinder.addRecipe(
                CMaterials.getOD(material, CMaterials.INGOT), CMaterials.get(material, CMaterials.DUST), e(0.25, 4), (long)((int)(80.0F * material.hardness))
            );
            recipeGrinder.addRecipe(
                CMaterials.getOD(material, CMaterials.PLATE), CMaterials.get(material, CMaterials.DUST), e(0.25, 4), (long)((int)(80.0F * material.hardness))
            );
            recipeGrinder.addRecipe(
                CMaterials.get(material, CMaterials.LARGE_PLATE),
                CMaterials.get(material, CMaterials.DUST, 4),
                e(0.25, 4),
                (long)((int)(80.0F * material.hardness))
            );
        }

        recipeBendingMachine.addRecipe(
            CMaterials.getOD(CMaterials.ALUMINIUM_OD, CMaterials.INGOT),
            4,
            CMaterials.get(CMaterials.ALUMINIUM, CMaterials.PLATE),
            e(4),
            (long)((int)(20.0F * CMaterials.ALUMINIUM.hardness))
        );
        recipeBendingMachine.addRecipe(
            CMaterials.getOD(CMaterials.ALUMINIUM_OD, CMaterials.PLATE, 4),
            4,
            CMaterials.get(CMaterials.ALUMINIUM, CMaterials.LARGE_PLATE),
            e(4),
            (long)((int)(40.0F * CMaterials.ALUMINIUM.hardness))
        );
        recipeGrinder.addRecipe(
            CMaterials.getOD(CMaterials.ALUMINIUM_OD, CMaterials.INGOT),
            CMaterials.get(CMaterials.ALUMINIUM, CMaterials.DUST),
            e(0.25, 4),
            (long)((int)(80.0F * CMaterials.ALUMINIUM.hardness))
        );
        recipeGrinder.addRecipe(
            CMaterials.getOD(CMaterials.ALUMINIUM_OD, CMaterials.PLATE),
            CMaterials.get(CMaterials.ALUMINIUM, CMaterials.DUST),
            e(0.25, 4),
            (long)((int)(80.0F * CMaterials.ALUMINIUM.hardness))
        );
        if (ClayiumCore.cfgHardcoreAluminium) {
            recipeBendingMachine.addRecipe(
                CMaterials.get(CMaterials.IMPURE_ALUMINIUM, CMaterials.INGOT),
                4,
                CMaterials.get(CMaterials.IMPURE_ALUMINIUM, CMaterials.PLATE),
                e(4),
                (long)((int)(20.0F * CMaterials.IMPURE_ALUMINIUM.hardness))
            );
            recipeBendingMachine.addRecipe(
                CMaterials.get(CMaterials.IMPURE_ALUMINIUM, CMaterials.PLATE, 4),
                4,
                CMaterials.get(CMaterials.IMPURE_ALUMINIUM, CMaterials.LARGE_PLATE),
                e(4),
                (long)((int)(40.0F * CMaterials.IMPURE_ALUMINIUM.hardness))
            );
            recipeGrinder.addRecipe(
                CMaterials.get(CMaterials.IMPURE_ALUMINIUM, CMaterials.INGOT),
                CMaterials.get(CMaterials.IMPURE_ALUMINIUM, CMaterials.DUST),
                e(0.25, 4),
                (long)((int)(80.0F * CMaterials.IMPURE_ALUMINIUM.hardness))
            );
            recipeGrinder.addRecipe(
                CMaterials.get(CMaterials.IMPURE_ALUMINIUM, CMaterials.PLATE),
                CMaterials.get(CMaterials.IMPURE_ALUMINIUM, CMaterials.DUST),
                e(0.25, 4),
                (long)((int)(80.0F * CMaterials.IMPURE_ALUMINIUM.hardness))
            );
            recipeGrinder.addRecipe(
                CMaterials.get(CMaterials.IMPURE_ALUMINIUM, CMaterials.LARGE_PLATE),
                CMaterials.get(CMaterials.IMPURE_ALUMINIUM, CMaterials.DUST, 4),
                e(0.25, 4),
                (long)((int)(80.0F * CMaterials.IMPURE_ALUMINIUM.hardness))
            );
        }

        GameRegistry.addSmelting(CMaterials.get(CMaterials.IMPURE_SILICON, CMaterials.INGOT), CMaterials.get(CMaterials.SILICONE, CMaterials.INGOT), 0.0F);
        materials = new CMaterial[]{
            CMaterials.IRON,
            CMaterials.GOLD,
            CMaterials.MAGNESIUM,
            CMaterials.SODIUM,
            CMaterials.LITHIUM,
            CMaterials.ZIRCONIUM,
            CMaterials.ZINC,
            CMaterials.MANGANESE,
            CMaterials.CALCIUM,
            CMaterials.POTASSIUM,
            CMaterials.NICKEL,
            CMaterials.BERYLLIUM,
            CMaterials.LEAD,
            CMaterials.HAFNIUM,
            CMaterials.CHROME,
            CMaterials.TITANIUM,
            CMaterials.STRONTIUM,
            CMaterials.BARIUM,
            CMaterials.COPPER,
            CMaterials.ZINCALMINIUM_ALLOY,
            CMaterials.ZINCONIUM_ALLOY,
            CMaterials.BRONZE,
            CMaterials.BRASS,
            CMaterials.ELECTRUM,
            CMaterials.INVAR,
            CMaterials.STEEL
        };

        for (CMaterial material : materials) {
            recipeGrinder.addRecipe(
                CMaterials.getOD(material, CMaterials.INGOT), CMaterials.get(material, CMaterials.DUST), e(0.25, 4), (long)((int)(80.0F * material.hardness))
            );
            recipeGrinder.addRecipe(
                CMaterials.getOD(material, CMaterials.PLATE), CMaterials.get(material, CMaterials.DUST), e(0.25, 4), (long)((int)(80.0F * material.hardness))
            );
            if (CMaterials.existOD(material, CMaterials.PLATE)) {
                recipeBendingMachine.addRecipe(
                    CMaterials.getOD(material, CMaterials.INGOT),
                    CMaterials.getODExist(material, CMaterials.PLATE),
                    e(4),
                    (long)((int)(40.0F * material.hardness))
                );
            }
        }

        materials = new CMaterial[]{CMaterials.ZINCALMINIUM_ALLOY, CMaterials.ZINCONIUM_ALLOY};

        for (CMaterial materialx : materials) {
            recipeGrinder.addRecipe(
                CMaterials.get(materialx, CMaterials.INGOT), CMaterials.get(materialx, CMaterials.DUST), e(0.25, 4), (long)((int)(80.0F * materialx.hardness))
            );
        }

        materials = new CMaterial[]{
            CMaterials.RUBIDIUM,
            CMaterials.CAESIUM,
            CMaterials.FRANCIUM,
            CMaterials.RADIUM,
            CMaterials.ACTINIUM,
            CMaterials.THORIUM,
            CMaterials.PROTACTINIUM,
            CMaterials.URANIUM,
            CMaterials.NEPTUNIUM,
            CMaterials.PLUTONIUM,
            CMaterials.AMERICIUM,
            CMaterials.CURIUM,
            CMaterials.LANTHANUM,
            CMaterials.CERIUM,
            CMaterials.PRASEODYMIUM,
            CMaterials.NEODYMIUM,
            CMaterials.PROMETHIUM,
            CMaterials.SAMARIUM,
            CMaterials.EUROPIUM,
            CMaterials.VANADIUM,
            CMaterials.COBALT,
            CMaterials.PALLADIUM,
            CMaterials.SILVER,
            CMaterials.PLATINUM,
            CMaterials.IRIDIUM,
            CMaterials.OSMIUM,
            CMaterials.RHENIUM,
            CMaterials.TANTALUM,
            CMaterials.TUNGSTEN,
            CMaterials.MOLYBDENUM,
            CMaterials.TIN,
            CMaterials.ANTIMONY,
            CMaterials.BISMUTH,
            CMaterials.CARBON,
            CMaterials.GALLIUM,
            CMaterials.YTTRIUM,
            CMaterials.NIOBIUM,
            CMaterials.URANIUM_235,
            CMaterials.PLUTONIUM_241,
            CMaterials.NAQUADAH,
            CMaterials.NAQUADAH_ENRICHED,
            CMaterials.NAQUADRIA,
            CMaterials.NEUTRONIUM,
            CMaterials.ARDITE,
            CMaterials.YELLORIUM,
            CMaterials.CYANITE,
            CMaterials.BLUTONIUM,
            CMaterials.LUDICRITE,
            CMaterials.FZ_DARK_IRON,
            CMaterials.METEORIC_IRON,
            CMaterials.DESH,
            CMaterials.PROMETHEUM,
            CMaterials.DEEP_IRON,
            CMaterials.INFUSCOLIUM,
            CMaterials.OURECLASE,
            CMaterials.AREDRITE,
            CMaterials.ASTRAL_SILVER,
            CMaterials.CARMOT,
            CMaterials.MITHRIL,
            CMaterials.RUBRACIUM,
            CMaterials.ORICHALCUM,
            CMaterials.ADAMANTINE,
            CMaterials.ATLARUS,
            CMaterials.IGNATIUS,
            CMaterials.SHADOW_IRON,
            CMaterials.LEMURITE,
            CMaterials.MIDASIUM,
            CMaterials.VYROXERES,
            CMaterials.CERUCLASE,
            CMaterials.ALDUORITE,
            CMaterials.KALENDRITE,
            CMaterials.VULCANITE,
            CMaterials.SANGUINITE,
            CMaterials.EXIMITE,
            CMaterials.MEUTOITE,
            CMaterials.PLASTIC,
            CMaterials.GRAPHITE,
            CMaterials.REDSTONE_ALLOY,
            CMaterials.CONDUCTIVE_IRON,
            CMaterials.ENERGETIC_ALLOY,
            CMaterials.ELECTRICAL_STEEL,
            CMaterials.DARK_STEEL,
            CMaterials.PHASED_IRON,
            CMaterials.PHASED_GOLD,
            CMaterials.SOULARIUM,
            CMaterials.SIGNALUM,
            CMaterials.LUMIUM,
            CMaterials.ENDERIUM,
            CMaterials.ELECTRUM_FLUX,
            CMaterials.ALUMINUM_BRASS,
            CMaterials.PIG_IRON,
            CMaterials.ALUMITE,
            CMaterials.MANYULLYN,
            CMaterials.FAIRY,
            CMaterials.POKEFENNIUM,
            CMaterials.RED_AURUM,
            CMaterials.DRULLOY,
            CMaterials.RED_ALLOY,
            CMaterials.ELECTROTINE_ALLOY,
            CMaterials.TUNGSTEN_STEEL,
            CMaterials.CUPRONICKEL,
            CMaterials.NICHROME,
            CMaterials.KANTHAL,
            CMaterials.STAINLESS_STEEL,
            CMaterials.COBALT_BRASS,
            CMaterials.MAGNALIUM,
            CMaterials.SOLDERING_ALLOY,
            CMaterials.BATTERY_ALLOY,
            CMaterials.VANADIUM_GALLIUM,
            CMaterials.YTTRIUM_BARIUM_CUPRATE,
            CMaterials.ULTIMET,
            CMaterials.TIN_ALLOY,
            CMaterials.BLUE_ALLOY,
            CMaterials.HEPATIZON,
            CMaterials.DAMASCUS_STEEL,
            CMaterials.ANGMALLEN,
            CMaterials.BLACK_STEEL,
            CMaterials.QUICKSILVER,
            CMaterials.HADEROTH,
            CMaterials.CELENEGIL,
            CMaterials.TARTARITE,
            CMaterials.SHADOW_STEEL,
            CMaterials.INOLASHITE,
            CMaterials.AMORDRINE,
            CMaterials.DESICHALKOS,
            CMaterials.WROUGHT_IRON,
            CMaterials.ANNEALED_COPPER,
            CMaterials.IRON_MAGNETIC,
            CMaterials.STEEL_MAGNETIC,
            CMaterials.NEODYMIUM_MAGNETIC,
            CMaterials.REFINED_GLOWSTONE,
            CMaterials.REFINED_OBSIDIAN,
            CMaterials.IRON_COMPRESSED,
            CMaterials.THAUMIUM,
            CMaterials.VOID,
            CMaterials.MANASTEEL,
            CMaterials.TERRASTEEL,
            CMaterials.ELVEN_ELEMENTIUM,
            CMaterials.HEE_ENDIUM,
            CMaterials.UNSTABLE,
            CMaterials.NINJA
        };

        for (CMaterial materialx : materials) {
            if (CMaterials.existOD(materialx, CMaterials.DUST)) {
                recipeGrinder.addRecipe(
                    CMaterials.getOD(materialx, CMaterials.INGOT),
                    CMaterials.getODExist(materialx, CMaterials.DUST),
                    e(0.25, 4),
                    (long)((int)(80.0F * materialx.hardness))
                );
                recipeGrinder.addRecipe(
                    CMaterials.getOD(materialx, CMaterials.PLATE),
                    CMaterials.getODExist(materialx, CMaterials.DUST),
                    e(0.25, 4),
                    (long)((int)(80.0F * materialx.hardness))
                );
            }

            if (CMaterials.existOD(materialx, CMaterials.PLATE)) {
                recipeBendingMachine.addRecipe(
                    CMaterials.getOD(materialx, CMaterials.INGOT),
                    CMaterials.getODExist(materialx, CMaterials.PLATE),
                    e(4),
                    (long)((int)(40.0F * materialx.hardness))
                );
            }
        }

        materials = new CMaterial[]{CMaterials.ANTIMATTER, CMaterials.PURE_ANTIMATTER, CMaterials.OCTUPLE_CLAY, CMaterials.OCTUPLE_PURE_ANTIMATTER};
        CShape[] shapes = new CShape[]{CMaterials.GEM, CMaterials.GEM, CMaterials.BLOCK, CMaterials.GEM};

        for (int i = 0; i < materials.length; i++) {
            CMaterial materialx = materials[i];
            shape = shapes[i];
            recipeBendingMachine.addRecipe(
                CMaterials.get(materialx, shape), 9, CMaterials.get(materialx, CMaterials.PLATE), e(9 + i), (long)((int)(20.0F * materialx.hardness))
            );
            recipeBendingMachine.addRecipe(
                CMaterials.get(materialx, CMaterials.PLATE, 4),
                9,
                CMaterials.get(materialx, CMaterials.LARGE_PLATE),
                e(9 + i),
                (long)((int)(40.0F * materialx.hardness))
            );
            recipeGrinder.addRecipe(
                CMaterials.get(materialx, shape), 10, CMaterials.get(materialx, CMaterials.DUST), e(0.25, 9 + i), (long)((int)(80.0F * materialx.hardness))
            );
            recipeGrinder.addRecipe(
                CMaterials.get(materialx, CMaterials.PLATE),
                10,
                CMaterials.get(materialx, CMaterials.DUST),
                e(0.25, 9 + i),
                (long)((int)(80.0F * materialx.hardness))
            );
            recipeGrinder.addRecipe(
                CMaterials.get(materialx, CMaterials.LARGE_PLATE),
                10,
                CMaterials.get(materialx, CMaterials.DUST, 4),
                e(0.25, 9 + i),
                (long)((int)(80.0F * materialx.hardness))
            );
            recipeCondenser.addRecipe(
                CMaterials.get(materialx, CMaterials.DUST), 10, CMaterials.get(materialx, shape), e(0.25, 9 + i), (long)((int)(80.0F * materialx.hardness))
            );
        }

        materials = new CMaterial[]{
            CMaterials.IMPURE_SILICON,
            CMaterials.SILICON,
            CMaterials.SILICONE,
            CMaterials.IRON,
            CMaterials.GOLD,
            CMaterials.LEAD,
            CMaterials.COPPER,
            CMaterials.BRONZE,
            CMaterials.BRASS,
            CMaterials.ELECTRUM,
            CMaterials.INVAR
        };

        for (CMaterial materialx : materials) {
            GameRegistry.addSmelting(CMaterials.get(materialx, CMaterials.DUST), CMaterials.get(materialx, CMaterials.INGOT), 0.0F);
        }

        materials = new CMaterial[]{CMaterials.ALUMINIUM, CMaterials.SODIUM, CMaterials.ZINC, CMaterials.NICKEL, CMaterials.ALUMINIUM_OD};

        for (CMaterial materialx : materials) {
            recipeSmelter.addRecipe(CMaterials.getOD(materialx, CMaterials.DUST), 5, CMaterials.get(materialx, CMaterials.INGOT), e(0.5, 5), 200L);
        }

        if (ClayiumCore.cfgHardcoreAluminium) {
            recipeSmelter.addRecipe(
                CMaterials.get(CMaterials.IMPURE_ALUMINIUM, CMaterials.DUST), 5, CMaterials.get(CMaterials.IMPURE_ALUMINIUM, CMaterials.INGOT), e(0.5, 5), 200L
            );
        }

        materials = new CMaterial[]{
            CMaterials.MAGNESIUM,
            CMaterials.LITHIUM,
            CMaterials.ZIRCONIUM,
            CMaterials.ZINCALMINIUM_ALLOY,
            CMaterials.ZINCONIUM_ALLOY,
            CMaterials.AZ91D_ALLOY,
            CMaterials.ZK60A_ALLOY
        };

        for (CMaterial materialx : materials) {
            recipeSmelter.addRecipe(CMaterials.getOD(materialx, CMaterials.DUST), 6, CMaterials.get(materialx, CMaterials.INGOT), e(0.2, 6), 400L);
        }

        materials = new CMaterial[]{CMaterials.CALCIUM, CMaterials.POTASSIUM};

        for (CMaterial materialx : materials) {
            recipeBlastFurnace.addRecipe(oo(CMaterials.getOD(materialx, CMaterials.DUST)), 0, 5, ii(CMaterials.get(materialx, CMaterials.INGOT)), e(5), 500L);
        }

        materials = new CMaterial[]{CMaterials.BERYLLIUM, CMaterials.HAFNIUM, CMaterials.CLAY_STEEL, CMaterials.STEEL};

        for (CMaterial materialx : materials) {
            recipeBlastFurnace.addRecipe(oo(CMaterials.getOD(materialx, CMaterials.DUST)), 0, 6, ii(CMaterials.get(materialx, CMaterials.INGOT)), e(6), 500L);
        }

        materials = new CMaterial[]{CMaterials.MANGANESE, CMaterials.STRONTIUM, CMaterials.BARIUM, CMaterials.CLAYIUM};

        for (CMaterial materialx : materials) {
            recipeBlastFurnace.addRecipe(
                oo(CMaterials.getOD(materialx, CMaterials.DUST)), 0, 7, ii(CMaterials.get(materialx, CMaterials.INGOT)), e(2.0, 7), 1000L
            );
        }

        materials = new CMaterial[]{CMaterials.TITANIUM, CMaterials.ULTIMATE_ALLOY};

        for (CMaterial materialx : materials) {
            recipeBlastFurnace.addRecipe(
                oo(CMaterials.getOD(materialx, CMaterials.DUST)), 0, 8, ii(CMaterials.get(materialx, CMaterials.INGOT)), e(4.0, 8), 2000L
            );
        }

        materials = new CMaterial[]{CMaterials.CHROME};

        for (CMaterial materialx : materials) {
            recipeBlastFurnace.addRecipe(
                oo(CMaterials.getOD(materialx, CMaterials.DUST)), 0, 9, ii(CMaterials.get(materialx, CMaterials.INGOT)), e(4.0, 9), 2000L
            );
        }

        if (ClayiumCore.cfgHardcoreOsmium) {
            recipeBlastFurnace.addRecipe(
                oo(CMaterials.getOD(CMaterials.IMPURE_OSMIUM, CMaterials.INGOT)),
                0,
                11,
                ii(CMaterials.get(CMaterials.OSMIUM, CMaterials.INGOT)),
                e(4.0, 11),
                100L
            );
        }

        materials = new CMaterial[]{
            CMaterials.QUARTZ,
            CMaterials.LAPIS,
            CMaterials.DIAMOND,
            CMaterials.EMERALD,
            CMaterials.RUBY,
            CMaterials.SAPPHIRE,
            CMaterials.PERIDOT,
            CMaterials.AMBER,
            CMaterials.AMETHYST,
            CMaterials.APATITE,
            CMaterials.CRYSTAL_FLUX,
            CMaterials.MALACHITE,
            CMaterials.TANZANITE,
            CMaterials.TOPAZ,
            CMaterials.DILITHIUM,
            CMaterials.FORCICIUM,
            CMaterials.GREEN_SAPPHIRE,
            CMaterials.OPAL,
            CMaterials.JASPER,
            CMaterials.BLUE_TOPAZ,
            CMaterials.FORCILLIUM,
            CMaterials.MONAZITE,
            CMaterials.FORCE,
            CMaterials.QUARTZITE,
            CMaterials.LAZURITE,
            CMaterials.SODALITE,
            CMaterials.GARNET_RED,
            CMaterials.GARNET_YELLOW,
            CMaterials.NITER,
            CMaterials.PHOSPHORUS,
            CMaterials.LIGNITE,
            CMaterials.GLASS,
            CMaterials.IRIDIUM
        };

        for (CMaterial materialx : materials) {
            if (CMaterials.existOD(materialx, CMaterials.GEM)) {
                recipeReactor.addRecipe(
                    oo(CMaterials.getOD(materialx, CMaterials.DUST), CMaterials.get(CMaterials.IND_CLAY, CMaterials.DUST)),
                    0,
                    7,
                    ii(CMaterials.getODExist(materialx, CMaterials.GEM)),
                    e(7),
                    10000L
                );
            }
        }

        recipeReactor.addRecipe(
            oo(CMaterials.getOD(CMaterials.COAL, CMaterials.DUST), CMaterials.get(CMaterials.IND_CLAY, CMaterials.DUST)),
            0,
            7,
            ii(i(Items.field_151044_h)),
            e(7),
            10000L
        );
        recipeReactor.addRecipe(
            oo(CMaterials.getOD(CMaterials.CHARCOAL, CMaterials.DUST), CMaterials.get(CMaterials.IND_CLAY, CMaterials.DUST)),
            0,
            7,
            ii(i(Items.field_151044_h, 1, 1)),
            e(7),
            10000L
        );

        for (CMaterial materialxx : materials) {
            if (CMaterials.existOD(materialxx, CMaterials.DUST)) {
                recipeGrinder.addRecipe(
                    CMaterials.getOD(materialxx, CMaterials.GEM),
                    5,
                    CMaterials.getODExist(materialxx, CMaterials.DUST),
                    e(0.25, 5),
                    (long)((int)(80.0F * materialxx.hardness))
                );
            }
        }

        recipeGrinder.addRecipe(
            i(Items.field_151044_h), 5, CMaterials.get(CMaterials.COAL, CMaterials.DUST), e(0.25, 5), (long)((int)(80.0F * CMaterials.COAL.hardness))
        );
        recipeGrinder.addRecipe(
            i(Items.field_151044_h, 1, 1),
            5,
            CMaterials.get(CMaterials.CHARCOAL, CMaterials.DUST),
            e(0.25, 5),
            (long)((int)(80.0F * CMaterials.CHARCOAL.hardness))
        );
        materials = new CMaterial[]{CMaterials.CINNABAR, CMaterials.CERTUS_QUARTZ, CMaterials.FLUIX};

        for (CMaterial materialxxx : materials) {
            if (CMaterials.existOD(materialxxx, CMaterials.CRYSTAL)) {
                recipeReactor.addRecipe(
                    oo(CMaterials.getOD(materialxxx, CMaterials.DUST), CMaterials.get(CMaterials.IND_CLAY, CMaterials.DUST)),
                    0,
                    7,
                    ii(CMaterials.getODExist(materialxxx, CMaterials.CRYSTAL)),
                    e(7),
                    10000L
                );
            }
        }

        for (CMaterial materialxxxx : materials) {
            if (CMaterials.existOD(materialxxxx, CMaterials.DUST)) {
                recipeGrinder.addRecipe(
                    CMaterials.getOD(materialxxxx, CMaterials.CRYSTAL),
                    5,
                    CMaterials.getODExist(materialxxxx, CMaterials.DUST),
                    e(0.25, 5),
                    (long)((int)(80.0F * materialxxxx.hardness))
                );
            }
        }

        recipeGrinder.addRecipe(
            "oreClay",
            CItems.itemCompressedClayShard.get("1", ClayiumCore.multiplyProgressionRateStackSize(2)),
            1L,
            (long)ClayiumCore.divideByProgressionRateI(3)
        );
        recipeGrinder.addRecipe(
            "oreDenseClay",
            CItems.itemCompressedClayShard.get("2", ClayiumCore.multiplyProgressionRateStackSize(3)),
            1L,
            (long)ClayiumCore.divideByProgressionRateI(6)
        );
        recipeGrinder.addRecipe(
            "oreLargeDenseClay",
            CItems.itemCompressedClayShard.get("3", ClayiumCore.multiplyProgressionRateStackSize(5)),
            1L,
            (long)ClayiumCore.divideByProgressionRateI(9)
        );
        recipeCondenser.addRecipe(
            CItems.itemCompressedClayShard.get("1", 4), i(CBlocks.blockCompressedClay, 1, 1), 1L, (long)ClayiumCore.divideByProgressionRateI(3)
        );
        recipeCondenser.addRecipe(
            CItems.itemCompressedClayShard.get("2", 4), i(CBlocks.blockCompressedClay, 1, 2), 1L, (long)ClayiumCore.divideByProgressionRateI(6)
        );
        recipeCondenser.addRecipe(
            CItems.itemCompressedClayShard.get("3", 4), i(CBlocks.blockCompressedClay, 1, 3), 1L, (long)ClayiumCore.divideByProgressionRateI(9)
        );
        List<ItemStack> oreList = null;
        materials = new CMaterial[]{
            CMaterials.SILICON,
            CMaterials.ALUMINIUM,
            CMaterials.ALUMINIUM_OD,
            CMaterials.COAL,
            CMaterials.IRON,
            CMaterials.GOLD,
            CMaterials.MAGNESIUM,
            CMaterials.SODIUM,
            CMaterials.LITHIUM,
            CMaterials.ZIRCONIUM,
            CMaterials.ZINC,
            CMaterials.MANGANESE,
            CMaterials.CALCIUM,
            CMaterials.POTASSIUM,
            CMaterials.NICKEL,
            CMaterials.BERYLLIUM,
            CMaterials.LEAD,
            CMaterials.HAFNIUM,
            CMaterials.CHROME,
            CMaterials.TITANIUM,
            CMaterials.STRONTIUM,
            CMaterials.BARIUM,
            CMaterials.COPPER,
            CMaterials.RUBIDIUM,
            CMaterials.CAESIUM,
            CMaterials.FRANCIUM,
            CMaterials.RADIUM,
            CMaterials.ACTINIUM,
            CMaterials.THORIUM,
            CMaterials.PROTACTINIUM,
            CMaterials.URANIUM,
            CMaterials.NEPTUNIUM,
            CMaterials.PLUTONIUM,
            CMaterials.AMERICIUM,
            CMaterials.CURIUM,
            CMaterials.LANTHANUM,
            CMaterials.CERIUM,
            CMaterials.PRASEODYMIUM,
            CMaterials.NEODYMIUM,
            CMaterials.PROMETHIUM,
            CMaterials.SAMARIUM,
            CMaterials.EUROPIUM,
            CMaterials.VANADIUM,
            CMaterials.COBALT,
            CMaterials.PALLADIUM,
            CMaterials.SILVER,
            CMaterials.PLATINUM,
            CMaterials.IRIDIUM,
            CMaterials.OSMIUM,
            CMaterials.RHENIUM,
            CMaterials.TANTALUM,
            CMaterials.TUNGSTEN,
            CMaterials.MOLYBDENUM,
            CMaterials.TIN,
            CMaterials.ANTIMONY,
            CMaterials.BISMUTH,
            CMaterials.GALLIUM,
            CMaterials.YTTRIUM,
            CMaterials.NIOBIUM,
            CMaterials.NAQUADAH,
            CMaterials.NAQUADAH_ENRICHED,
            CMaterials.NAQUADRIA,
            CMaterials.ARDITE,
            CMaterials.YELLORIUM,
            CMaterials.FZ_DARK_IRON,
            CMaterials.METEORIC_IRON,
            CMaterials.DESH,
            CMaterials.GRAPHITE,
            CMaterials.PROMETHEUM,
            CMaterials.DEEP_IRON,
            CMaterials.INFUSCOLIUM,
            CMaterials.OURECLASE,
            CMaterials.AREDRITE,
            CMaterials.ASTRAL_SILVER,
            CMaterials.CARMOT,
            CMaterials.MITHRIL,
            CMaterials.RUBRACIUM,
            CMaterials.ORICHALCUM,
            CMaterials.ADAMANTINE,
            CMaterials.ATLARUS,
            CMaterials.IGNATIUS,
            CMaterials.SHADOW_IRON,
            CMaterials.LEMURITE,
            CMaterials.MIDASIUM,
            CMaterials.VYROXERES,
            CMaterials.CERUCLASE,
            CMaterials.ALDUORITE,
            CMaterials.KALENDRITE,
            CMaterials.VULCANITE,
            CMaterials.SANGUINITE,
            CMaterials.EXIMITE,
            CMaterials.MEUTOITE
        };

        for (CMaterial materialxxxxx : materials) {
            if (CMaterials.existOD(materialxxxxx, CMaterials.DUST)) {
                recipeGrinder.addRecipe("ore" + materialxxxxx.oreDictionaryName, 5, CMaterials.getODExist(materialxxxxx, CMaterials.DUST, 2), e(0.025, 5), 80L);
                recipeGrinder.addRecipe(
                    "oreRedgranite" + materialxxxxx.oreDictionaryName, 5, CMaterials.getODExist(materialxxxxx, CMaterials.DUST, 2), e(0.025, 5), 80L
                );
                recipeGrinder.addRecipe(
                    "oreBlackgranite" + materialxxxxx.oreDictionaryName, 5, CMaterials.getODExist(materialxxxxx, CMaterials.DUST, 2), e(0.025, 5), 80L
                );
                recipeGrinder.addRecipe(
                    "oreEndstone" + materialxxxxx.oreDictionaryName, 5, CMaterials.getODExist(materialxxxxx, CMaterials.DUST, 2), e(0.025, 5), 80L
                );
            }
        }

        for (CMaterial materialxxxxxx : new CMaterial[]{CMaterials.CERTUS_QUARTZ, CMaterials.CINNABAR}) {
            if (CMaterials.existOD(materialxxxxxx, CMaterials.CRYSTAL)) {
                recipeGrinder.addRecipe(
                    "ore" + materialxxxxxx.oreDictionaryName, 5, CMaterials.getODExist(materialxxxxxx, CMaterials.CRYSTAL, 2), e(0.025, 5), 80L
                );
                recipeGrinder.addRecipe(
                    "oreRedgranite" + materialxxxxxx.oreDictionaryName, 5, CMaterials.getODExist(materialxxxxxx, CMaterials.CRYSTAL, 2), e(0.025, 5), 80L
                );
                recipeGrinder.addRecipe(
                    "oreBlackgranite" + materialxxxxxx.oreDictionaryName, 5, CMaterials.getODExist(materialxxxxxx, CMaterials.CRYSTAL, 2), e(0.025, 5), 80L
                );
                recipeGrinder.addRecipe(
                    "oreEndstone" + materialxxxxxx.oreDictionaryName, 5, CMaterials.getODExist(materialxxxxxx, CMaterials.CRYSTAL, 2), e(0.025, 5), 80L
                );
            }
        }

        for (CMaterial materialxxxxxxx : new CMaterial[]{
            CMaterials.QUARTZ,
            CMaterials.DIAMOND,
            CMaterials.EMERALD,
            CMaterials.AMETHYST,
            CMaterials.AMBER,
            CMaterials.MALACHITE,
            CMaterials.PERIDOT,
            CMaterials.RUBY,
            CMaterials.SAPPHIRE,
            CMaterials.TANZANITE,
            CMaterials.TOPAZ,
            CMaterials.OPAL,
            CMaterials.JASPER,
            CMaterials.FORCILLIUM,
            CMaterials.FORCE,
            CMaterials.GARNET_RED,
            CMaterials.GARNET_YELLOW,
            CMaterials.NITER,
            CMaterials.LIGNITE,
            CMaterials.QUARTZITE,
            CMaterials.DILITHIUM,
            CMaterials.ORICHALCUM
        }) {
            if (CMaterials.existOD(materialxxxxxxx, CMaterials.GEM)) {
                recipeGrinder.addRecipe(
                    "ore" + materialxxxxxxx.oreDictionaryName, 5, CMaterials.getODExist(materialxxxxxxx, CMaterials.GEM, 2), e(0.025, 5), 80L
                );
                recipeGrinder.addRecipe(
                    "oreRedgranite" + materialxxxxxxx.oreDictionaryName, 5, CMaterials.getODExist(materialxxxxxxx, CMaterials.GEM, 2), e(0.025, 5), 80L
                );
                recipeGrinder.addRecipe(
                    "oreBlackgranite" + materialxxxxxxx.oreDictionaryName, 5, CMaterials.getODExist(materialxxxxxxx, CMaterials.GEM, 2), e(0.025, 5), 80L
                );
                recipeGrinder.addRecipe(
                    "oreEndstone" + materialxxxxxxx.oreDictionaryName, 5, CMaterials.getODExist(materialxxxxxxx, CMaterials.GEM, 2), e(0.025, 5), 80L
                );
            }
        }

        for (CMaterial materialxxxxxxxx : new CMaterial[]{CMaterials.MONAZITE, CMaterials.FORCICIUM}) {
            if (CMaterials.existOD(materialxxxxxxxx, CMaterials.GEM)) {
                recipeGrinder.addRecipe(
                    "ore" + materialxxxxxxxx.oreDictionaryName, 5, CMaterials.getODExist(materialxxxxxxxx, CMaterials.GEM, 4), e(0.025, 5), 80L
                );
                recipeGrinder.addRecipe(
                    "oreRedgranite" + materialxxxxxxxx.oreDictionaryName, 5, CMaterials.getODExist(materialxxxxxxxx, CMaterials.GEM, 4), e(0.025, 5), 80L
                );
                recipeGrinder.addRecipe(
                    "oreBlackgranite" + materialxxxxxxxx.oreDictionaryName, 5, CMaterials.getODExist(materialxxxxxxxx, CMaterials.GEM, 4), e(0.025, 5), 80L
                );
                recipeGrinder.addRecipe(
                    "oreEndstone" + materialxxxxxxxx.oreDictionaryName, 5, CMaterials.getODExist(materialxxxxxxxx, CMaterials.GEM, 4), e(0.025, 5), 80L
                );
            }
        }

        for (CMaterial materialxxxxxxxxx : new CMaterial[]{
            CMaterials.LAPIS, CMaterials.LAZURITE, CMaterials.SODALITE, CMaterials.PHOSPHORUS, CMaterials.APATITE
        }) {
            if (CMaterials.existOD(materialxxxxxxxxx, CMaterials.GEM)) {
                recipeGrinder.addRecipe(
                    "ore" + materialxxxxxxxxx.oreDictionaryName, 5, CMaterials.getODExist(materialxxxxxxxxx, CMaterials.GEM, 10), e(0.025, 5), 80L
                );
                recipeGrinder.addRecipe(
                    "oreRedgranite" + materialxxxxxxxxx.oreDictionaryName, 5, CMaterials.getODExist(materialxxxxxxxxx, CMaterials.GEM, 10), e(0.025, 5), 80L
                );
                recipeGrinder.addRecipe(
                    "oreBlackgranite" + materialxxxxxxxxx.oreDictionaryName, 5, CMaterials.getODExist(materialxxxxxxxxx, CMaterials.GEM, 10), e(0.025, 5), 80L
                );
                recipeGrinder.addRecipe(
                    "oreEndstone" + materialxxxxxxxxx.oreDictionaryName, 5, CMaterials.getODExist(materialxxxxxxxxx, CMaterials.GEM, 10), e(0.025, 5), 80L
                );
            }
        }

        for (CMaterial materialxxxxxxxxxx : new CMaterial[]{
            CMaterials.REDSTONE,
            CMaterials.NIKOLITE,
            CMaterials.ELECTROTINE,
            CMaterials.YELLOWSTONE,
            CMaterials.BLUESTONE,
            CMaterials.SULFUR,
            CMaterials.SALTPETER
        }) {
            if (CMaterials.existOD(materialxxxxxxxxxx, CMaterials.DUST)) {
                recipeGrinder.addRecipe(
                    "ore" + materialxxxxxxxxxx.oreDictionaryName, 5, CMaterials.getODExist(materialxxxxxxxxxx, CMaterials.DUST, 10), e(0.025, 5), 80L
                );
                recipeGrinder.addRecipe(
                    "oreRedgranite" + materialxxxxxxxxxx.oreDictionaryName, 5, CMaterials.getODExist(materialxxxxxxxxxx, CMaterials.DUST, 10), e(0.025, 5), 80L
                );
                recipeGrinder.addRecipe(
                    "oreBlackgranite" + materialxxxxxxxxxx.oreDictionaryName,
                    5,
                    CMaterials.getODExist(materialxxxxxxxxxx, CMaterials.DUST, 10),
                    e(0.025, 5),
                    80L
                );
                recipeGrinder.addRecipe(
                    "oreEndstone" + materialxxxxxxxxxx.oreDictionaryName, 5, CMaterials.getODExist(materialxxxxxxxxxx, CMaterials.DUST, 10), e(0.025, 5), 80L
                );
            }
        }

        recipeAlloySmelter.addRecipe(
            oo(CMaterials.getOD(CMaterials.ZINC, CMaterials.INGOT, 9), CMaterials.getOD(CMaterials.ALUMINIUM, CMaterials.INGOT)),
            0,
            6,
            ii(CMaterials.get(CMaterials.ZINCALMINIUM_ALLOY, CMaterials.INGOT, 10)),
            e(6),
            50L
        );
        recipeAlloySmelter.addRecipe(
            oo(CMaterials.getOD(CMaterials.ZINC, CMaterials.INGOT, 9), CMaterials.getOD(CMaterials.ALUMINIUM_OD, CMaterials.INGOT)),
            0,
            6,
            ii(CMaterials.get(CMaterials.ZINCALMINIUM_ALLOY, CMaterials.INGOT, 10)),
            e(6),
            50L
        );
        recipeAlloySmelter.addRecipe(
            oo(CMaterials.getOD(CMaterials.MAGNESIUM, CMaterials.INGOT, 9), CMaterials.get(CMaterials.ZINCALMINIUM_ALLOY, CMaterials.INGOT)),
            0,
            6,
            ii(CMaterials.get(CMaterials.AZ91D_ALLOY, CMaterials.INGOT, 10)),
            e(7),
            500L
        );
        recipeAlloySmelter.addRecipe(
            oo(CMaterials.getOD(CMaterials.ZINC, CMaterials.INGOT, 9), CMaterials.getOD(CMaterials.ZIRCONIUM, CMaterials.INGOT)),
            0,
            6,
            ii(CMaterials.get(CMaterials.ZINCONIUM_ALLOY, CMaterials.INGOT, 10)),
            e(3.0, 7),
            50L
        );
        recipeAlloySmelter.addRecipe(
            oo(CMaterials.getOD(CMaterials.MAGNESIUM, CMaterials.INGOT, 19), CMaterials.get(CMaterials.ZINCONIUM_ALLOY, CMaterials.INGOT)),
            0,
            6,
            ii(CMaterials.get(CMaterials.ZK60A_ALLOY, CMaterials.INGOT, 20)),
            e(3.0, 7),
            500L
        );
        shapes = new CShape[]{CMaterials.INGOT, CMaterials.DUST};

        for (CShape shape1 : shapes) {
            for (CShape shape2 : shapes) {
                recipeAlloySmelter.addRecipe(
                    oo(CMaterials.getOD(CMaterials.COPPER, shape1, 3), CMaterials.getOD(CMaterials.TIN, shape2)),
                    0,
                    0,
                    ii(CMaterials.get(CMaterials.BRONZE, CMaterials.INGOT, 4)),
                    e(4),
                    40L
                );
                recipeAlloySmelter.addRecipe(
                    oo(CMaterials.getOD(CMaterials.COPPER, shape1, 3), CMaterials.getOD(CMaterials.ZINC, shape2)),
                    0,
                    0,
                    ii(CMaterials.get(CMaterials.BRASS, CMaterials.INGOT, 4)),
                    e(4),
                    40L
                );
                recipeAlloySmelter.addRecipe(
                    oo(CMaterials.getOD(CMaterials.GOLD, shape1), CMaterials.getOD(CMaterials.SILVER, shape2)),
                    0,
                    0,
                    ii(CMaterials.get(CMaterials.ELECTRUM, CMaterials.INGOT, 2)),
                    e(4),
                    40L
                );
                recipeAlloySmelter.addRecipe(
                    oo(CMaterials.getOD(CMaterials.IRON, shape1, 2), CMaterials.getOD(CMaterials.NICKEL, shape2)),
                    0,
                    0,
                    ii(CMaterials.get(CMaterials.INVAR, CMaterials.INGOT, 3)),
                    e(4),
                    40L
                );
            }

            recipeBlastFurnace.addRecipe(
                oo(CMaterials.getOD(CMaterials.IRON, shape1), i(Items.field_151044_h, 2)),
                0,
                6,
                ii(CMaterials.get(CMaterials.STEEL, CMaterials.INGOT)),
                e(6),
                500L
            );
        }

        GameRegistry.addRecipe(
            new ShapelessOreRecipe(
                CMaterials.get(CMaterials.IMPURE_ULTIMATE_ALLOY, CMaterials.INGOT, 9),
                oo(
                    CMaterials.getODName(CMaterials.BARIUM, CMaterials.INGOT),
                    CMaterials.getODName(CMaterials.STRONTIUM, CMaterials.INGOT),
                    CMaterials.getODName(CMaterials.CALCIUM, CMaterials.INGOT),
                    CMaterials.getODName(CMaterials.CLAYIUM, CMaterials.INGOT),
                    CMaterials.getODName(CMaterials.ALUMINIUM, CMaterials.INGOT),
                    CMaterials.getODName(CMaterials.ALUMINIUM, CMaterials.INGOT),
                    CMaterials.getODName(CMaterials.ALUMINIUM, CMaterials.INGOT),
                    CMaterials.getODName(CMaterials.ALUMINIUM, CMaterials.INGOT),
                    CMaterials.getODName(CMaterials.ALUMINIUM, CMaterials.INGOT)
                )
            )
        );
        GameRegistry.addRecipe(
            new ShapelessOreRecipe(
                CMaterials.get(CMaterials.IMPURE_ULTIMATE_ALLOY, CMaterials.INGOT, 9),
                oo(
                    CMaterials.getODName(CMaterials.BARIUM, CMaterials.INGOT),
                    CMaterials.getODName(CMaterials.STRONTIUM, CMaterials.INGOT),
                    CMaterials.getODName(CMaterials.CALCIUM, CMaterials.INGOT),
                    CMaterials.getODName(CMaterials.CLAYIUM, CMaterials.INGOT),
                    CMaterials.getODName(CMaterials.ALUMINIUM_OD, CMaterials.INGOT),
                    CMaterials.getODName(CMaterials.ALUMINIUM_OD, CMaterials.INGOT),
                    CMaterials.getODName(CMaterials.ALUMINIUM_OD, CMaterials.INGOT),
                    CMaterials.getODName(CMaterials.ALUMINIUM_OD, CMaterials.INGOT),
                    CMaterials.getODName(CMaterials.ALUMINIUM_OD, CMaterials.INGOT)
                )
            )
        );
        recipeGrinder.addRecipe(i(Blocks.field_150348_b), i(Blocks.field_150347_e), 1L, 3L);
        recipeGrinder.addRecipe(
            i(Blocks.field_150347_e, ClayiumCore.multiplyProgressionRateStackSize(1)),
            i(Blocks.field_150351_n, ClayiumCore.multiplyProgressionRateStackSize(1)),
            1L,
            10L
        );
        recipeGrinder.addRecipe(
            i(Blocks.field_150347_e, ClayiumCore.multiplyProgressionRateStackSize(4)),
            i(Blocks.field_150351_n, ClayiumCore.multiplyProgressionRateStackSize(4)),
            1L,
            10L
        );
        recipeGrinder.addRecipe(
            i(Blocks.field_150347_e, ClayiumCore.multiplyProgressionRateStackSize(16)),
            i(Blocks.field_150351_n, ClayiumCore.multiplyProgressionRateStackSize(16)),
            1L,
            10L
        );
        recipeGrinder.addRecipe(
            i(Blocks.field_150347_e, ClayiumCore.multiplyProgressionRateStackSize(64)),
            i(Blocks.field_150351_n, ClayiumCore.multiplyProgressionRateStackSize(64)),
            1L,
            10L
        );
        recipeGrinder.addRecipe(i(Blocks.field_150351_n), i(Blocks.field_150354_m), 10L, 10L);
        recipeCentrifuge.addRecipe(
            ii(i(Blocks.field_150351_n, ClayiumCore.multiplyProgressionRateStackSize(1))),
            0,
            4,
            ii(CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.BLOCK, ClayiumCore.multiplyProgressionRateStackSize(1))),
            1L,
            2L
        );
        recipeCentrifuge.addRecipe(
            ii(i(Blocks.field_150351_n, ClayiumCore.multiplyProgressionRateStackSize(4))),
            0,
            4,
            ii(CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.BLOCK, ClayiumCore.multiplyProgressionRateStackSize(4))),
            2L,
            2L
        );
        recipeCentrifuge.addRecipe(
            ii(i(Blocks.field_150351_n, ClayiumCore.multiplyProgressionRateStackSize(16))),
            0,
            5,
            ii(CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.BLOCK, ClayiumCore.multiplyProgressionRateStackSize(16))),
            4L,
            1L
        );
        recipeCentrifuge.addRecipe(
            ii(i(Blocks.field_150351_n, ClayiumCore.multiplyProgressionRateStackSize(64))),
            0,
            6,
            ii(CMaterials.get(CMaterials.DENSE_CLAY, CMaterials.BLOCK, ClayiumCore.multiplyProgressionRateStackSize(64))),
            8L,
            1L
        );
        recipeReactor.addRecipe(
            ii(CMaterials.get(CMaterials.ORG_CLAY, CMaterials.DUST), i(Blocks.field_150351_n)), 0, 7, ii(i(Blocks.field_150346_d)), e(7), 100L
        );
        recipeCAInjector.addRecipe(
            ii(i(Blocks.field_150351_n), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM)), 0, 10, ii(i(Blocks.field_150346_d)), e(2.0, 10), 60L
        );
        recipeGrinder.addRecipe(i(Blocks.field_150322_A, 1, 32767), 3, i(Blocks.field_150354_m, 4), e(3), 100L);
        recipeCAInjector.addRecipe(
            ii(i(Blocks.field_150354_m), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM)), 0, 10, ii(i(Blocks.field_150354_m, 1, 1)), e(2.0, 10), 60L
        );
        recipeChemicalReactor.addRecipe(
            ii(CMaterials.get(CMaterials.ENG_CLAY, CMaterials.DUST)),
            0,
            5,
            ii(CMaterials.get(CMaterials.IMPURE_REDSTONE, CMaterials.DUST), CMaterials.get(CMaterials.IMPURE_GLOWSTONE, CMaterials.DUST)),
            e(5),
            10L
        );
        recipeReactor.addRecipe(oo(CMaterials.get(CMaterials.IMPURE_REDSTONE, CMaterials.DUST)), 0, 7, ii(i(Items.field_151137_ax)), e(0.1, 7), 2000L);
        recipeReactor.addRecipe(oo(CMaterials.get(CMaterials.IMPURE_GLOWSTONE, CMaterials.DUST)), 0, 7, ii(i(Items.field_151114_aO)), e(0.1, 7), 2000L);
        recipeCAInjector.addRecipe(
            oo(CMaterials.getOD(CMaterials.REDSTONE, CMaterials.DUST), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM)),
            0,
            10,
            ii(i(Blocks.field_150343_Z)),
            e(2.0, 10),
            60L
        );
        recipeCondenser.addRecipe(CMaterials.getOD(CMaterials.COAL, CMaterials.BLOCK, 8), 5, i(Items.field_151045_i), e(5), 100L);
        recipeReactor.addRecipe(
            oo(i(Items.field_151044_h, 64), CMaterials.get(CMaterials.IND_CLAY, CMaterials.DUST)), 0, 7, ii(i(Items.field_151045_i)), e(7), 10000L
        );
        recipeReactor.addRecipe(
            oo(CMaterials.get(CMaterials.ORG_CLAY, CMaterials.DUST), CMaterials.getOD(CMaterials.POTASSIUM, CMaterials.DUST)),
            0,
            8,
            ii(CMaterials.get(CMaterials.SALTPETER, CMaterials.DUST)),
            e(8),
            10000L
        );
        GameRegistry.addRecipe(
            new ShapelessOreRecipe(
                i(Items.field_151016_H, 2),
                oo(
                    CMaterials.getODName(CMaterials.SALTPETER, CMaterials.DUST),
                    CMaterials.getODName(CMaterials.SALTPETER, CMaterials.DUST),
                    CMaterials.getODName(CMaterials.SULFUR, CMaterials.DUST),
                    CMaterials.getODName(CMaterials.CHARCOAL, CMaterials.DUST)
                )
            )
        );
        recipeGrinder.addRecipe(i(Blocks.field_150325_L, 1, 32767), 5, i(Items.field_151007_F, 4), e(5), 100L);
        recipeGrinder.addRecipe(i(Blocks.field_150404_cg, 1, 32767), 5, i(Items.field_151007_F, 2), e(5), 100L);
        recipeGrinder.addRecipe(i(Items.field_151072_bj), 5, i(Items.field_151065_br, 5), e(5), 100L);
        recipeGrinder.addRecipe(i(Items.field_151103_aS), 5, i(Items.field_151100_aR, 5, 15), e(5), 100L);
        recipeCentrifuge.addRecipe(
            ii(i(CBlocks.blockClayTreeLog)),
            0,
            6,
            ii(
                CMaterials.get(CMaterials.ADVIND_CLAY, CMaterials.DUST, 16),
                CMaterials.get(CMaterials.MANGANESE, CMaterials.DUST, 5),
                CMaterials.get(CMaterials.LITHIUM, CMaterials.DUST, 3),
                CMaterials.get(CMaterials.ZIRCONIUM, CMaterials.DUST, 1)
            ),
            10000L,
            400L
        );
        recipeAssembler.addRecipe(ii(i(Blocks.field_150417_aV), i(Blocks.field_150395_bd)), 0, 6, ii(i(Blocks.field_150417_aV, 1, 1)), e(6), 20L);
        recipeReactor.addRecipe(
            oo(CMaterials.getOD(CMaterials.ULTIMATE_ALLOY, CMaterials.DUST, 4), i(Items.field_151069_bo)),
            0,
            11,
            ii(i(Items.field_151062_by)),
            e(11),
            100000000000000L
        );
        recipeReactor.addRecipe(ii(i(Items.field_151174_bG)), 0, 11, ii(i(Items.field_151170_bI)), e(9), 10000000000L);
        TileChemicalMetalSeparator.results.add(CMaterials.get(CMaterials.IMPURE_ALUMINIUM, CMaterials.DUST), 200);
        TileChemicalMetalSeparator.results.add(CMaterials.get(CMaterials.IMPURE_MAGNESIUM, CMaterials.DUST), 60);
        TileChemicalMetalSeparator.results.add(CMaterials.get(CMaterials.IMPURE_SODIUM, CMaterials.DUST), 40);
        TileChemicalMetalSeparator.results.add(CMaterials.get(CMaterials.IMPURE_LITHIUM, CMaterials.DUST), 7);
        TileChemicalMetalSeparator.results.add(CMaterials.get(CMaterials.IMPURE_ZIRCONIUM, CMaterials.DUST), 5);
        TileChemicalMetalSeparator.results.add(CMaterials.get(CMaterials.IMPURE_ZINC, CMaterials.DUST), 10);
        TileChemicalMetalSeparator.results.add(CMaterials.get(CMaterials.IMPURE_MANGANESE, CMaterials.DUST), 80);
        TileChemicalMetalSeparator.results.add(CMaterials.get(CMaterials.IMPURE_CALCIUM, CMaterials.DUST), 20);
        TileChemicalMetalSeparator.results.add(CMaterials.get(CMaterials.IMPURE_POTASSIUM, CMaterials.DUST), 15);
        TileChemicalMetalSeparator.results.add(CMaterials.get(CMaterials.IMPURE_NICKEL, CMaterials.DUST), 13);
        TileChemicalMetalSeparator.results.add(CMaterials.get(CMaterials.IMPURE_IRON, CMaterials.DUST), 9);
        TileChemicalMetalSeparator.results.add(CMaterials.get(CMaterials.IMPURE_BERYLLIUM, CMaterials.DUST), 8);
        TileChemicalMetalSeparator.results.add(CMaterials.get(CMaterials.IMPURE_LEAD, CMaterials.DUST), 6);
        TileChemicalMetalSeparator.results.add(CMaterials.get(CMaterials.IMPURE_HAFNIUM, CMaterials.DUST), 4);
        TileChemicalMetalSeparator.results.add(CMaterials.get(CMaterials.IMPURE_CHROME, CMaterials.DUST), 3);
        TileChemicalMetalSeparator.results.add(CMaterials.get(CMaterials.IMPURE_TITANIUM, CMaterials.DUST), 3);
        TileChemicalMetalSeparator.results.add(CMaterials.get(CMaterials.IMPURE_STRONTIUM, CMaterials.DUST), 2);
        TileChemicalMetalSeparator.results.add(CMaterials.get(CMaterials.IMPURE_BARIUM, CMaterials.DUST), 2);
        TileChemicalMetalSeparator.results.add(CMaterials.get(CMaterials.IMPURE_COPPER, CMaterials.DUST), 1);
        CMaterial[][] materialmaps = new CMaterial[][]{
            {CMaterials.IMPURE_ALUMINIUM, CMaterials.ALUMINIUM},
            {CMaterials.IMPURE_MAGNESIUM, CMaterials.MAGNESIUM},
            {CMaterials.IMPURE_SODIUM, CMaterials.SODIUM},
            {CMaterials.IMPURE_LITHIUM, CMaterials.LITHIUM},
            {CMaterials.IMPURE_ZIRCONIUM, CMaterials.ZIRCONIUM},
            {CMaterials.IMPURE_ZINC, CMaterials.ZINC}
        };

        for (CMaterial[] materialmap : materialmaps) {
            recipeElectrolysisReactor.addRecipe(CMaterials.get(materialmap[0], CMaterials.DUST), 6, CMaterials.get(materialmap[1], CMaterials.DUST), e(6), 100L);
        }

        materialmaps = new CMaterial[][]{
            {CMaterials.IMPURE_MANGANESE, CMaterials.MANGANESE},
            {CMaterials.IMPURE_POTASSIUM, CMaterials.POTASSIUM},
            {CMaterials.IMPURE_HAFNIUM, CMaterials.HAFNIUM},
            {CMaterials.IMPURE_STRONTIUM, CMaterials.STRONTIUM},
            {CMaterials.IMPURE_BARIUM, CMaterials.BARIUM},
            {CMaterials.IMPURE_CALCIUM, CMaterials.CALCIUM}
        };

        for (CMaterial[] materialmap : materialmaps) {
            recipeElectrolysisReactor.addRecipe(
                CMaterials.get(materialmap[0], CMaterials.DUST), 7, CMaterials.get(materialmap[1], CMaterials.DUST), e(10.0, 7), 300L
            );
        }

        materialmaps = new CMaterial[][]{
            {CMaterials.IMPURE_IRON, CMaterials.IRON}, {CMaterials.IMPURE_LEAD, CMaterials.LEAD}, {CMaterials.IMPURE_COPPER, CMaterials.COPPER}
        };

        for (CMaterial[] materialmap : materialmaps) {
            recipeElectrolysisReactor.addRecipe(
                CMaterials.get(materialmap[0], CMaterials.DUST), 8, CMaterials.get(materialmap[1], CMaterials.DUST), e(10.0, 8), 1000L
            );
        }

        materialmaps = new CMaterial[][]{
            {CMaterials.IMPURE_NICKEL, CMaterials.NICKEL},
            {CMaterials.IMPURE_BERYLLIUM, CMaterials.BERYLLIUM},
            {CMaterials.IMPURE_CHROME, CMaterials.CHROME},
            {CMaterials.IMPURE_TITANIUM, CMaterials.TITANIUM}
        };

        for (CMaterial[] materialmap : materialmaps) {
            recipeElectrolysisReactor.addRecipe(
                CMaterials.get(materialmap[0], CMaterials.DUST), 9, CMaterials.get(materialmap[1], CMaterials.DUST), e(10.0, 9), 3000L
            );
        }

        recipeCAInjector.addRecipe(
            ii(CMaterials.get(CMaterials.ORG_CLAY, CMaterials.DUST), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM)),
            0,
            10,
            ii(i(Items.field_151014_N)),
            e(2.0, 10),
            60L
        );
        recipeCAInjector.addRecipe(
            ii(i(Items.field_151014_N), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM)), 0, 10, ii(i(Blocks.field_150327_N)), e(2.0, 10), 60L
        );
        recipeCAInjector.addRecipe(
            ii(i(Blocks.field_150327_N), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM)), 0, 10, ii(i(Items.field_151034_e)), e(2.0, 10), 60L
        );
        recipeCAInjector.addRecipe(
            ii(i(Items.field_151034_e), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM)), 0, 10, ii(i(Items.field_151120_aE)), e(2.0, 10), 60L
        );
        recipeCAInjector.addRecipe(
            ii(i(Items.field_151120_aE), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM)), 0, 10, ii(i(Blocks.field_150345_g)), e(2.0, 10), 60L
        );
        recipeCAInjector.addRecipe(
            ii(i(Blocks.field_150345_g), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM)), 0, 10, ii(i(Blocks.field_150362_t)), e(2.0, 10), 60L
        );
        recipeCAInjector.addRecipe(
            ii(i(Blocks.field_150362_t), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM)), 0, 10, ii(i(Blocks.field_150364_r)), e(2.0, 10), 60L
        );
        if (CMaterials.existOD("itemRawRubber")) {
            recipeCAInjector.addRecipe(
                ii(i(Blocks.field_150364_r), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM)),
                0,
                10,
                ii(CMaterials.getODExist("itemRawRubber")),
                e(2.0, 10),
                60L
            );
        }

        recipeCAInjector.addRecipe(
            ii(i(Blocks.field_150349_c), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM)), 0, 10, ii(i(Blocks.field_150329_H, 1, 1)), e(2.0, 10), 60L
        );
        recipeCAInjector.addRecipe(
            ii(i(Blocks.field_150391_bh), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM)), 0, 10, ii(i(Blocks.field_150338_P, 1, 1)), e(2.0, 10), 60L
        );
        recipeReactor.addRecipe(
            oo(CMaterials.getOD(CMaterials.CARBON, CMaterials.DUST), CMaterials.get(CMaterials.ORG_CLAY, CMaterials.DUST)),
            0,
            11,
            ii(i(Items.field_151078_bh)),
            e(11),
            100000000000000L
        );
        recipeCAInjector.addRecipe(
            ii(i(Items.field_151078_bh), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM)), 0, 10, ii(i(Items.field_151116_aA)), e(2.0, 10), 60L
        );
        recipeCAInjector.addRecipe(
            ii(i(Items.field_151116_aA), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM)), 0, 10, ii(i(Items.field_151103_aS)), e(2.0, 10), 60L
        );
        recipeCAInjector.addRecipe(
            ii(i(Items.field_151103_aS), CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM)), 0, 10, ii(i(Items.field_151123_aH)), e(2.0, 10), 60L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.LITHIUM, CMaterials.INGOT), 7, CMaterials.get(CMaterials.SODIUM, CMaterials.INGOT), e(10.0, 7), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.SODIUM, CMaterials.INGOT), 7, CMaterials.get(CMaterials.POTASSIUM, CMaterials.INGOT), e(30.0, 7), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.POTASSIUM, CMaterials.INGOT), 8, CMaterials.get(CMaterials.RUBIDIUM, CMaterials.INGOT), e(10.0, 8), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.RUBIDIUM, CMaterials.INGOT), 8, CMaterials.get(CMaterials.CAESIUM, CMaterials.INGOT), e(20.0, 8), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.CAESIUM, CMaterials.INGOT), 8, CMaterials.get(CMaterials.FRANCIUM, CMaterials.INGOT), e(30.0, 8), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.FRANCIUM, CMaterials.INGOT), 8, CMaterials.get(CMaterials.RADIUM, CMaterials.INGOT), e(50.0, 8), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.RADIUM, CMaterials.INGOT), 9, CMaterials.get(CMaterials.ACTINIUM, CMaterials.INGOT), e(10.0, 9), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.ACTINIUM, CMaterials.INGOT), 9, CMaterials.get(CMaterials.THORIUM, CMaterials.INGOT), e(20.0, 9), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.THORIUM, CMaterials.INGOT), 9, CMaterials.get(CMaterials.PROTACTINIUM, CMaterials.INGOT), e(30.0, 9), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.PROTACTINIUM, CMaterials.INGOT), 9, CMaterials.get(CMaterials.URANIUM, CMaterials.INGOT), e(50.0, 9), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.URANIUM, CMaterials.INGOT), 9, CMaterials.get(CMaterials.NEPTUNIUM, CMaterials.INGOT), e(80.0, 9), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.NEPTUNIUM, CMaterials.INGOT), 10, CMaterials.get(CMaterials.PLUTONIUM, CMaterials.INGOT), e(20.0, 10), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.PLUTONIUM, CMaterials.INGOT), 11, CMaterials.get(CMaterials.AMERICIUM, CMaterials.INGOT), e(30.0, 11), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.AMERICIUM, CMaterials.INGOT), 12, CMaterials.get(CMaterials.CURIUM, CMaterials.INGOT), e(50.0, 12), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.BERYLLIUM, CMaterials.INGOT), 7, CMaterials.get(CMaterials.MAGNESIUM, CMaterials.INGOT), e(10.0, 7), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.MAGNESIUM, CMaterials.INGOT), 7, CMaterials.get(CMaterials.CALCIUM, CMaterials.INGOT), e(20.0, 7), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.CALCIUM, CMaterials.INGOT), 7, CMaterials.get(CMaterials.STRONTIUM, CMaterials.INGOT), e(30.0, 7), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.STRONTIUM, CMaterials.INGOT), 7, CMaterials.get(CMaterials.BARIUM, CMaterials.INGOT), e(50.0, 7), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.BARIUM, CMaterials.INGOT), 8, CMaterials.get(CMaterials.LANTHANUM, CMaterials.INGOT), e(10.0, 8), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.LANTHANUM, CMaterials.INGOT), 8, CMaterials.get(CMaterials.CERIUM, CMaterials.INGOT), e(30.0, 8), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.CERIUM, CMaterials.INGOT), 8, CMaterials.get(CMaterials.PRASEODYMIUM, CMaterials.INGOT), e(90.0, 8), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.PRASEODYMIUM, CMaterials.INGOT), 9, CMaterials.get(CMaterials.NEODYMIUM, CMaterials.INGOT), e(20.0, 9), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.NEODYMIUM, CMaterials.INGOT), 10, CMaterials.get(CMaterials.PROMETHIUM, CMaterials.INGOT), e(10.0, 10), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.PROMETHIUM, CMaterials.INGOT), 11, CMaterials.get(CMaterials.SAMARIUM, CMaterials.INGOT), e(20.0, 11), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.SAMARIUM, CMaterials.INGOT), 12, CMaterials.get(CMaterials.EUROPIUM, CMaterials.INGOT), e(60.0, 12), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.ZIRCONIUM, CMaterials.INGOT), 8, CMaterials.get(CMaterials.TITANIUM, CMaterials.INGOT), e(60.0, 8), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.TITANIUM, CMaterials.INGOT), 9, CMaterials.get(CMaterials.VANADIUM, CMaterials.INGOT), e(60.0, 9), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.MANGANESE, CMaterials.INGOT), 7, CMaterials.get(CMaterials.IRON, CMaterials.INGOT), e(90.0, 7), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.IRON, CMaterials.INGOT), 8, CMaterials.get(CMaterials.COBALT, CMaterials.INGOT), e(30.0, 8), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.COBALT, CMaterials.INGOT), 8, CMaterials.get(CMaterials.NICKEL, CMaterials.INGOT), e(90.0, 8), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.NICKEL, CMaterials.INGOT), 9, CMaterials.get(CMaterials.PALLADIUM, CMaterials.INGOT), e(40.0, 9), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.ZINC, CMaterials.INGOT), 8, CMaterials.get(CMaterials.COPPER, CMaterials.INGOT), e(20.0, 8), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.COPPER, CMaterials.INGOT), 9, CMaterials.get(CMaterials.SILVER, CMaterials.INGOT), e(10.0, 9), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.SILVER, CMaterials.INGOT), 9, CMaterials.get(CMaterials.GOLD, CMaterials.INGOT), e(50.0, 9), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.GOLD, CMaterials.INGOT), 10, CMaterials.get(CMaterials.PLATINUM, CMaterials.INGOT), e(30.0, 10), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.PLATINUM, CMaterials.INGOT), 11, CMaterials.get(CMaterials.IRIDIUM, CMaterials.INGOT), e(10.0, 11), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.IRIDIUM, CMaterials.INGOT), 11, CMaterials.get(CMaterials.MAIN_OSMIUM, CMaterials.INGOT), e(30.0, 11), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.MAIN_OSMIUM, CMaterials.INGOT), 12, CMaterials.get(CMaterials.RHENIUM, CMaterials.INGOT), e(10.0, 12), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.HAFNIUM, CMaterials.INGOT), 8, CMaterials.get(CMaterials.TANTALUM, CMaterials.INGOT), e(70.0, 8), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.TANTALUM, CMaterials.INGOT), 9, CMaterials.get(CMaterials.TUNGSTEN, CMaterials.INGOT), e(40.0, 9), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.TUNGSTEN, CMaterials.INGOT), 10, CMaterials.get(CMaterials.MOLYBDENUM, CMaterials.INGOT), e(20.0, 10), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.MOLYBDENUM, CMaterials.INGOT), 11, CMaterials.get(CMaterials.CHROME, CMaterials.INGOT), e(10.0, 11), 200L
        );
        recipeTransformer.addRecipe(CMaterials.getOD(CMaterials.LEAD, CMaterials.INGOT), 7, CMaterials.get(CMaterials.TIN, CMaterials.INGOT), e(50.0, 7), 200L);
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.TIN, CMaterials.INGOT), 8, CMaterials.get(CMaterials.ANTIMONY, CMaterials.INGOT), e(20.0, 8), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.ANTIMONY, CMaterials.INGOT), 9, CMaterials.get(CMaterials.BISMUTH, CMaterials.INGOT), e(10.0, 9), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.SILICON, CMaterials.DUST), 7, CMaterials.get(CMaterials.PHOSPHORUS, CMaterials.DUST), e(10.0, 7), 200L
        );
        recipeTransformer.addRecipe(
            CMaterials.getOD(CMaterials.PHOSPHORUS, CMaterials.DUST), 7, CMaterials.get(CMaterials.SULFUR, CMaterials.DUST), e(30.0, 7), 200L
        );
        registerODChainRecipes(
            recipeTransformer,
            new CMaterial[]{
                CMaterials.IND_CLAY,
                CMaterials.CARBON,
                CMaterials.GRAPHITE,
                CMaterials.CHARCOAL,
                CMaterials.COAL,
                CMaterials.LAPIS,
                CMaterials.LAZURITE,
                CMaterials.SODALITE,
                CMaterials.MONAZITE
            },
            CMaterials.DUST,
            new int[]{8, 8, 8, 4, 4, 4, 4, 4, 1},
            new int[]{0, 7, 8, 9, 10, 10, 10, 10, 11},
            200
        );
        registerODChainRecipes(
            recipeTransformer,
            new CMaterial[]{
                CMaterials.DIAMOND, CMaterials.AMBER, CMaterials.AMETHYST, CMaterials.PERIDOT, CMaterials.SAPPHIRE, CMaterials.RUBY, CMaterials.EMERALD
            },
            CMaterials.GEM,
            new int[]{0, 10, 10, 10, 10, 10, 11},
            200
        );
        registerStackChainRecipes(
            recipeTransformer, ii(i(Blocks.field_150347_e), i(Blocks.field_150424_aL), i(Blocks.field_150377_bs)), new int[]{0, 9, 11}, 20
        );
        registerStackChainRecipes(
            recipeTransformer, ii(i(Blocks.field_150417_aV), i(Blocks.field_150417_aV, 1, 2), i(Blocks.field_150417_aV, 1, 3)), new int[]{0, 8, 8}, 20
        );
        registerStackChainRecipes(
            recipeTransformer,
            ii(i(Blocks.field_150346_d), i(Blocks.field_150346_d, 1, 2), i(Blocks.field_150349_c), i(Blocks.field_150391_bh), i(Blocks.field_150425_aM)),
            new int[]{0, 7, 8, 9, 10},
            20
        );
        registerStackChainRecipes(
            recipeTransformer,
            ii(
                i(Blocks.field_150364_r),
                i(Blocks.field_150364_r, 1, 1),
                i(Blocks.field_150364_r, 1, 2),
                i(Blocks.field_150364_r, 1, 3),
                i(Blocks.field_150363_s),
                i(Blocks.field_150363_s, 1, 1)
            ),
            new int[]{0, 7, 7, 8, 8, 8},
            80
        );
        registerStackChainRecipes(
            recipeTransformer,
            ii(
                i(Blocks.field_150362_t),
                i(Blocks.field_150362_t, 1, 1),
                i(Blocks.field_150362_t, 1, 2),
                i(Blocks.field_150362_t, 1, 3),
                i(Blocks.field_150361_u),
                i(Blocks.field_150361_u, 1, 1)
            ),
            new int[]{0, 7, 7, 8, 8, 8},
            20
        );
        registerStackChainRecipes(
            recipeTransformer,
            ii(
                i(Blocks.field_150345_g),
                i(Blocks.field_150345_g, 1, 1),
                i(Blocks.field_150345_g, 1, 2),
                i(Blocks.field_150345_g, 1, 3),
                i(Blocks.field_150345_g, 1, 4),
                i(Blocks.field_150345_g, 1, 5)
            ),
            new int[]{0, 7, 7, 8, 8, 8},
            20
        );
        registerStackChainRecipes(
            recipeTransformer,
            ii(i(Items.field_151014_N), i(Items.field_151080_bb), i(Items.field_151081_bc), i(Items.field_151100_aR, 1, 3), i(Items.field_151075_bm)),
            new int[]{0, 8, 8, 8, 8},
            20
        );
        register2to1Recipe(recipeReactor, "dyeLime", i(Items.field_151100_aR, 1, 3), 8, CMaterials.getODExist("edamame"), 1000000000L);
        register2to1Recipe(recipeReactor, i(Items.field_151081_bc), i(Items.field_151100_aR, 1, 3), 8, CMaterials.getODExist("soybeans"), 1000000000L);
        register2to1Recipe(recipeReactor, i(Items.field_151081_bc), i(Items.field_151007_F, 3), 8, CMaterials.getODExist("seedCotton"), 1000000000L);
        register2to1Recipe(recipeReactor, i(Items.field_151014_N), i(Items.field_151100_aR, 1, 3), 10, CMaterials.getODExist("cropCoffee"), 100000000000000L);
        registerStackChainRecipes(recipeTransformer, ii(i(Items.field_151015_O), i(Items.field_151172_bF), i(Items.field_151174_bG)), new int[]{0, 8, 8}, 20);
        recipeReactor.addRecipe(ii(i(Items.field_151014_N)), 0, 10, ii(i(Items.field_151015_O)), e(8), 2000000000000000L);
        recipeReactor.addRecipe(ii(i(Items.field_151080_bb)), 0, 10, ii(i(Blocks.field_150423_aK)), e(8), 2000000000000000L);
        recipeReactor.addRecipe(ii(i(Items.field_151081_bc)), 0, 10, ii(i(Blocks.field_150440_ba)), e(8), 2000000000000000L);
        register1to1Recipe(recipeReactor, "seedCotton", 8, CMaterials.getODExist("cropCotton"), 2000000000000000L);
        recipeGrinder.addRecipe(i(Blocks.field_150440_ba), 5, i(Items.field_151127_ba, 9), e(5), 100L);
        if (CMaterials.existOD("flour")) {
            register1to1Recipe(recipeGrinder, i(Items.field_151015_O), 5, CMaterials.getODExist("flour"), 60L);
        } else if (CMaterials.existOD("itemFlour")) {
            register1to1Recipe(recipeGrinder, i(Items.field_151015_O), 5, CMaterials.getODExist("itemFlour"), 60L);
        } else if (CMaterials.existOD("dustFlour")) {
            register1to1Recipe(recipeGrinder, i(Items.field_151015_O), 5, CMaterials.getODExist("dustFlour"), 60L);
        }

        register2to1Recipe(recipeReactor, i(Items.field_151015_O), i(Items.field_151014_N), 8, CMaterials.getODExist("cropRice"), 1000000000L);
        register2to1Recipe(recipeReactor, i(Items.field_151015_O), i(Blocks.field_150329_H, 1, 1), 8, CMaterials.getODExist("cropStraw"), 1000000000L);
        register2to1Recipe(recipeReactor, i(Items.field_151034_e), "dyeRed", 8, CMaterials.getODExist("apricot"), 1000000000L);
        registerStackChainRecipes(
            recipeTransformer,
            ii(i(Blocks.field_150329_H, 1, 1), i(Blocks.field_150329_H, 1, 2), i(Blocks.field_150330_I), i(Blocks.field_150395_bd), i(Blocks.field_150392_bi)),
            new int[]{0, 7, 7, 8, 9},
            20
        );
        registerStackChainRecipes(
            recipeTransformer,
            ii(
                i(Blocks.field_150327_N),
                i(Blocks.field_150328_O),
                i(Blocks.field_150328_O, 1, 1),
                i(Blocks.field_150328_O, 1, 2),
                i(Blocks.field_150328_O, 1, 3),
                i(Blocks.field_150328_O, 1, 4),
                i(Blocks.field_150328_O, 1, 5),
                i(Blocks.field_150328_O, 1, 6),
                i(Blocks.field_150328_O, 1, 7),
                i(Blocks.field_150328_O, 1, 8),
                i(Blocks.field_150398_cm),
                i(Blocks.field_150398_cm, 1, 1),
                i(Blocks.field_150398_cm, 1, 2),
                i(Blocks.field_150398_cm, 1, 3),
                i(Blocks.field_150398_cm, 1, 4),
                i(Blocks.field_150398_cm, 1, 5)
            ),
            new int[]{0, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8},
            20
        );
        registerStackChainRecipes(recipeTransformer, ii(i(Items.field_151120_aE), i(Blocks.field_150434_aF)), new int[]{0, 8}, 20);
        register2to1Recipe(recipeReactor, i(Items.field_151120_aE), "logWood", 8, CMaterials.getODExist("bamboo"), 1000000000L);
        registerStackChainRecipes(
            recipeTransformer,
            ii(i(Items.field_151078_bh), i(Items.field_151147_al), i(Items.field_151082_bd), i(Items.field_151076_bf)),
            new int[]{64, 2, 2, 1},
            new int[]{0, 9, 9, 9},
            200
        );
        registerStackChainRecipes(
            recipeTransformer, ii(i(Items.field_151116_aA), i(Blocks.field_150325_L), i(Items.field_151008_G)), new int[]{1, 4, 16}, new int[]{0, 9, 9}, 80
        );
        recipeAssembler.addRecipe(ii(i(Items.field_151116_aA, 4), i(Items.field_151007_F, 16)), 0, 10, ii(i(Items.field_151141_av)), e(10), 6000L);
        recipeAssembler.addRecipe(ii(i(Items.field_151121_aF, 2), i(Items.field_151007_F, 4)), 0, 10, ii(i(Items.field_151057_cb)), e(10), 600L);
        registerStackChainRecipes(
            recipeTransformer,
            ii(i(Items.field_151103_aS), i(Items.field_151072_bj), i(Items.field_151079_bi), i(Items.field_151156_bN), i(Blocks.field_150380_bt)),
            new int[]{262144, 4096, 1024, 64, 1},
            new int[]{0, 9, 9, 12, 12},
            200
        );
        registerStackChainRecipes(
            recipeTransformer,
            ii(i(Items.field_151123_aH), i(Items.field_151110_aK), i(Items.field_151100_aR), i(Items.field_151070_bp), i(Items.field_151073_bk)),
            new int[]{0, 8, 8, 9, 10},
            100
        );
        registerStackChainRecipes(
            recipeTransformer,
            ii(
                i(Items.field_151144_bL, 1, 1),
                i(Items.field_151144_bL),
                i(Items.field_151144_bL, 1, 2),
                i(Items.field_151144_bL, 1, 4),
                i(Items.field_151144_bL, 1, 3)
            ),
            new int[]{0, 11, 11, 11, 12},
            1000
        );
        registerStackChainRecipes(
            recipeTransformer,
            ii(i(Blocks.field_150351_n), i(Items.field_151145_ak), CMaterials.getODExist(CMaterials.CINNABAR, CMaterials.CRYSTAL)),
            new int[]{0, 7, 10},
            1000
        );
        if (ClayiumCore.IntegrationID.EIO.enabled()) {
            register2to1Recipe(
                recipeAlloySmelter, i(Items.field_151137_ax), "itemSilicon", 6, CMaterials.getODExist(CMaterials.REDSTONE_ALLOY, CMaterials.INGOT), 100L
            );
            register2to1Recipe(
                recipeAlloySmelter,
                i(Items.field_151137_ax),
                CMaterials.getOD(CMaterials.IRON, CMaterials.INGOT),
                6,
                CMaterials.getODExist(CMaterials.CONDUCTIVE_IRON, CMaterials.INGOT),
                100L
            );
            register2to1Recipe(
                recipeAlloySmelter,
                i(Items.field_151137_ax),
                CMaterials.getOD(CMaterials.IRON, CMaterials.DUST),
                6,
                CMaterials.getODExist(CMaterials.CONDUCTIVE_IRON, CMaterials.INGOT),
                100L
            );
            register2to1Recipe(
                recipeReactor,
                i(Items.field_151137_ax),
                CMaterials.getOD(CMaterials.GOLD, CMaterials.INGOT),
                8,
                CMaterials.getODExist(CMaterials.ENERGETIC_ALLOY, CMaterials.INGOT),
                1000000000L
            );
            register2to1Recipe(
                recipeReactor,
                i(Items.field_151137_ax),
                CMaterials.getOD(CMaterials.GOLD, CMaterials.DUST),
                8,
                CMaterials.getODExist(CMaterials.ENERGETIC_ALLOY, CMaterials.INGOT),
                1000000000L
            );
            register2to1Recipe(
                recipeBlastFurnace,
                CMaterials.getOD(CMaterials.STEEL, CMaterials.INGOT),
                "itemSilicon",
                7,
                CMaterials.getODExist(CMaterials.ELECTRICAL_STEEL, CMaterials.INGOT),
                500L
            );
            register2to1Recipe(
                recipeBlastFurnace,
                CMaterials.getOD(CMaterials.STEEL, CMaterials.DUST),
                "itemSilicon",
                7,
                CMaterials.getODExist(CMaterials.ELECTRICAL_STEEL, CMaterials.INGOT),
                500L
            );
            register2to1Recipe(
                recipeBlastFurnace,
                CMaterials.getOD(CMaterials.STEEL, CMaterials.INGOT),
                i(Blocks.field_150343_Z),
                7,
                CMaterials.getODExist(CMaterials.DARK_STEEL, CMaterials.INGOT),
                500L
            );
            register2to1Recipe(
                recipeBlastFurnace,
                CMaterials.getOD(CMaterials.STEEL, CMaterials.DUST),
                i(Blocks.field_150343_Z),
                7,
                CMaterials.getODExist(CMaterials.DARK_STEEL, CMaterials.INGOT),
                500L
            );
            register2to1Recipe(
                recipeBlastFurnace,
                CMaterials.getOD(CMaterials.IRON, CMaterials.INGOT),
                i(Items.field_151079_bi),
                6,
                CMaterials.getODExist(CMaterials.PHASED_IRON, CMaterials.INGOT),
                500L
            );
            register2to1Recipe(
                recipeBlastFurnace,
                CMaterials.getOD(CMaterials.IRON, CMaterials.DUST),
                i(Items.field_151079_bi),
                6,
                CMaterials.getODExist(CMaterials.PHASED_IRON, CMaterials.INGOT),
                500L
            );
            register2to1Recipe(
                recipeBlastFurnace,
                CMaterials.getOD(CMaterials.ENERGETIC_ALLOY, CMaterials.INGOT),
                i(Items.field_151079_bi),
                6,
                CMaterials.getODExist(CMaterials.PHASED_GOLD, CMaterials.INGOT),
                500L
            );
            register2to1Recipe(
                recipeBlastFurnace,
                CMaterials.getOD(CMaterials.ENERGETIC_ALLOY, CMaterials.DUST),
                i(Items.field_151079_bi),
                6,
                CMaterials.getODExist(CMaterials.PHASED_GOLD, CMaterials.INGOT),
                500L
            );
            register2to1Recipe(
                recipeBlastFurnace,
                CMaterials.getOD(CMaterials.GOLD, CMaterials.INGOT),
                i(Blocks.field_150425_aM),
                6,
                CMaterials.getODExist(CMaterials.SOULARIUM, CMaterials.INGOT),
                500L
            );
            register2to1Recipe(
                recipeBlastFurnace,
                CMaterials.getOD(CMaterials.GOLD, CMaterials.DUST),
                i(Blocks.field_150425_aM),
                6,
                CMaterials.getODExist(CMaterials.SOULARIUM, CMaterials.INGOT),
                500L
            );
        }

        if (ClayiumCore.IntegrationID.TF.enabled()) {
            for (CShape shape1 : new CShape[]{CMaterials.INGOT, CMaterials.DUST}) {
                for (CShape shape2 : new CShape[]{CMaterials.INGOT, CMaterials.DUST}) {
                    register2to1Recipe(
                        recipeReactor,
                        CMaterials.getOD(CMaterials.COPPER, shape1, 3),
                        CMaterials.getOD(CMaterials.SILVER, shape2),
                        8,
                        CMaterials.getODExist(CMaterials.SIGNALUM, CMaterials.INGOT, 4),
                        1000000000L
                    );
                    register2to1Recipe(
                        recipeReactor,
                        CMaterials.getOD(CMaterials.TIN, shape1, 3),
                        CMaterials.getOD(CMaterials.SILVER, shape2),
                        8,
                        CMaterials.getODExist(CMaterials.LUMIUM, CMaterials.INGOT, 4),
                        1000000000L
                    );
                }

                register2to1Recipe(
                    recipeAlloySmelter,
                    CMaterials.getOD(CMaterials.ELECTRUM, shape1),
                    i(Items.field_151137_ax, 2),
                    6,
                    CMaterials.getODExist(CMaterials.ELECTRUM_FLUX, CMaterials.INGOT),
                    100L
                );
            }

            register2to1Recipe(
                recipeReactor, i(Items.field_151045_i), i(Items.field_151137_ax, 2), 7, CMaterials.getODExist(CMaterials.CRYSTAL_FLUX, CMaterials.GEM), 10000L
            );
        }

        if (ClayiumCore.IntegrationID.FFM.enabled()) {
            register2to1Recipe(
                recipeCAInjector,
                CMaterials.getOD(CMaterials.PHOSPHORUS, CMaterials.DUST),
                CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM),
                10,
                CMaterials.getODExist(CMaterials.APATITE, CMaterials.GEM),
                60L
            );
            register2to1Recipe(
                recipeCAInjector,
                CMaterials.getOD(CMaterials.PHOSPHORUS, CMaterials.GEM),
                CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM),
                10,
                CMaterials.getODExist(CMaterials.APATITE, CMaterials.GEM),
                60L
            );
        }

        if (ClayiumCore.IntegrationID.AE2.enabled()) {
            register1to1Recipe(recipeTransformer, i(Items.field_151128_bU), 10, CMaterials.getODExist(CMaterials.CERTUS_QUARTZ, CMaterials.CRYSTAL), 60L);
        }

        if (ClayiumCore.IntegrationID.TIC.enabled()) {
            register2to1Recipe(
                recipeCAInjector,
                CMaterials.getOD(CMaterials.COBALT, CMaterials.INGOT),
                CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM),
                10,
                CMaterials.getODExist(CMaterials.ARDITE, CMaterials.INGOT),
                60L
            );

            for (CShape shape1 : new CShape[]{CMaterials.INGOT, CMaterials.DUST}) {
                for (CShape shape2 : new CShape[]{CMaterials.INGOT, CMaterials.DUST}) {
                    register2to1Recipe(
                        recipeAlloySmelter,
                        CMaterials.getOD(CMaterials.ALUMINIUM, shape1, 3),
                        CMaterials.getOD(CMaterials.COPPER, shape2),
                        6,
                        CMaterials.getODExist(CMaterials.ALUMINUM_BRASS, CMaterials.INGOT, 4),
                        100L
                    );
                    register2to1Recipe(
                        recipeAlloySmelter,
                        CMaterials.getOD(CMaterials.ALUMINIUM_OD, shape1, 3),
                        CMaterials.getOD(CMaterials.COPPER, shape2),
                        6,
                        CMaterials.getODExist(CMaterials.ALUMINUM_BRASS, CMaterials.INGOT, 4),
                        100L
                    );
                    register2to1Recipe(
                        recipeReactor,
                        CMaterials.getOD(CMaterials.ALUMINIUM, shape1, 5),
                        CMaterials.getOD(CMaterials.DARK_STEEL, shape2, 2),
                        8,
                        CMaterials.getODExist(CMaterials.ALUMITE, CMaterials.INGOT, 3),
                        1000000000L
                    );
                    register2to1Recipe(
                        recipeReactor,
                        CMaterials.getOD(CMaterials.ALUMINIUM_OD, shape1, 5),
                        CMaterials.getOD(CMaterials.DARK_STEEL, shape2, 2),
                        8,
                        CMaterials.getODExist(CMaterials.ALUMITE, CMaterials.INGOT, 3),
                        1000000000L
                    );
                    register2to1Recipe(
                        recipeAlloySmelter,
                        CMaterials.getOD(CMaterials.ARDITE, shape1),
                        CMaterials.getOD(CMaterials.COBALT, shape2),
                        6,
                        CMaterials.getODExist(CMaterials.MANYULLYN, CMaterials.INGOT),
                        100L
                    );
                    register2to1Recipe(
                        recipeReactor,
                        CMaterials.getOD(CMaterials.IRON, shape1),
                        CMaterials.getOD(CMaterials.COBALT, shape2),
                        8,
                        CMaterials.getODExist(CMaterials.POKEFENNIUM, CMaterials.INGOT, 2),
                        1000000000L
                    );
                }

                register2to1Recipe(
                    recipeReactor,
                    CMaterials.getOD(CMaterials.IRON, shape1),
                    CMaterials.getOD(CMaterials.EMERALD, CMaterials.GEM, 9),
                    9,
                    CMaterials.getODExist(CMaterials.PIG_IRON, CMaterials.INGOT),
                    100000000000L
                );
                register2to1Recipe(
                    recipeReactor,
                    CMaterials.getOD(CMaterials.ARDITE, shape1),
                    i(Blocks.field_150343_Z),
                    8,
                    CMaterials.getODExist(CMaterials.FAIRY, CMaterials.INGOT, 2),
                    1000000000L
                );
            }
        }

        if (ClayiumCore.IntegrationID.PR_EX.enabled()) {
            for (CShape shape1 : new CShape[]{CMaterials.INGOT, CMaterials.DUST}) {
                if (!ClayiumCore.IntegrationID.GT.loaded() && !CMaterials.existOD(CMaterials.CONDUCTIVE_IRON, CMaterials.INGOT)) {
                    register2to1Recipe(
                        recipeAlloySmelter,
                        CMaterials.getOD(CMaterials.IRON, shape1),
                        i(Items.field_151137_ax, 8),
                        6,
                        CMaterials.getODExist(CMaterials.RED_ALLOY, CMaterials.INGOT),
                        100L
                    );
                } else {
                    register2to1Recipe(
                        recipeAlloySmelter,
                        CMaterials.getOD(CMaterials.COPPER, shape1),
                        i(Items.field_151137_ax, 4),
                        6,
                        CMaterials.getODExist(CMaterials.RED_ALLOY, CMaterials.INGOT),
                        100L
                    );
                }

                register2to1Recipe(
                    recipeAlloySmelter,
                    CMaterials.getOD(CMaterials.IRON, shape1),
                    CMaterials.getOD(CMaterials.ELECTROTINE, CMaterials.DUST, 8),
                    6,
                    CMaterials.getODExist(CMaterials.ELECTROTINE_ALLOY, CMaterials.INGOT),
                    100L
                );
            }
        }

        if (ClayiumCore.IntegrationID.MEK.enabled()) {
            for (CShape shape1 : new CShape[]{CMaterials.INGOT, CMaterials.DUST}) {
                register2to1Recipe(
                    recipeAlloySmelter,
                    CMaterials.getOD(CMaterials.OSMIUM, shape1),
                    i(Items.field_151114_aO),
                    6,
                    CMaterials.getODExist(CMaterials.REFINED_GLOWSTONE, CMaterials.INGOT),
                    100L
                );
            }

            for (CShape shape1 : new CShape[]{CMaterials.GEM, CMaterials.DUST}) {
                for (CShape shape2 : new CShape[]{CMaterials.BLOCK, CMaterials.DUST}) {
                    register2to1Recipe(
                        recipeReactor,
                        CMaterials.getOD(CMaterials.DIAMOND, shape1),
                        CMaterials.getOD(CMaterials.OBSIDIAN, shape2),
                        8,
                        CMaterials.getODExist(CMaterials.REFINED_OBSIDIAN, CMaterials.INGOT),
                        1000000000L
                    );
                }
            }
        }

        if (ClayiumCore.IntegrationID.BR.enabled() && CMaterials.existOD(CMaterials.YELLORIUM, CMaterials.INGOT)) {
            boolean flag = true;

            for (ItemStack uranium : OreDictionary.getOres(CMaterials.getODName(CMaterials.URANIUM, CMaterials.INGOT))) {
                for (ItemStack yellorium : OreDictionary.getOres(CMaterials.getODName(CMaterials.YELLORIUM, CMaterials.INGOT))) {
                    if (UtilItemStack.areTypeEqual(uranium, yellorium)) {
                        flag = false;
                    }
                }
            }

            if (flag) {
                register2to1Recipe(
                    recipeCAInjector,
                    CMaterials.getOD(CMaterials.URANIUM, CMaterials.INGOT),
                    CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM),
                    10,
                    CMaterials.getODExist(CMaterials.YELLORIUM, CMaterials.INGOT),
                    60L
                );
                registerODChainRecipes(
                    recipeTransformer,
                    new CMaterial[]{CMaterials.YELLORIUM, CMaterials.CYANITE, CMaterials.BLUTONIUM, CMaterials.LUDICRITE},
                    CMaterials.INGOT,
                    new int[]{16, 16, 8, 1},
                    new int[]{0, 7, 10, 12},
                    200
                );
            } else {
                boolean flag1 = true;

                for (ItemStack plutonium : OreDictionary.getOres(CMaterials.getODName(CMaterials.PLUTONIUM, CMaterials.INGOT))) {
                    for (ItemStack blutonium : OreDictionary.getOres(CMaterials.getODName(CMaterials.BLUTONIUM, CMaterials.INGOT))) {
                        if (UtilItemStack.areTypeEqual(plutonium, blutonium)) {
                            flag1 = false;
                        }
                    }
                }

                if (flag1) {
                    registerODChainRecipes(
                        recipeTransformer,
                        new CMaterial[]{CMaterials.CYANITE, CMaterials.BLUTONIUM, CMaterials.LUDICRITE},
                        CMaterials.INGOT,
                        new int[]{16, 8, 1},
                        new int[]{7, 10, 12},
                        200
                    );
                } else {
                    register2to1Recipe(
                        recipeCAInjector,
                        CMaterials.getOD(CMaterials.PLUTONIUM, CMaterials.INGOT, 8),
                        CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM, 64),
                        12,
                        CMaterials.getODExist(CMaterials.LUDICRITE, CMaterials.INGOT),
                        200L
                    );
                }
            }
        }

        if (ClayiumCore.IntegrationID.GC.enabled()) {
            register2to1Recipe(
                recipeCAInjector,
                CMaterials.getOD(CMaterials.NICKEL, CMaterials.INGOT),
                CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM),
                10,
                CMaterials.getODExist(CMaterials.METEORIC_IRON, CMaterials.INGOT),
                60L
            );
            registerODChainRecipes(recipeTransformer, new CMaterial[]{CMaterials.METEORIC_IRON, CMaterials.DESH}, CMaterials.INGOT, new int[]{0, 11}, 200);
        }

        if (ClayiumCore.IntegrationID.FZ.enabled()) {
            register2to1Recipe(
                recipeCAInjector,
                CMaterials.getOD(CMaterials.INVAR, CMaterials.INGOT),
                CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM),
                10,
                CMaterials.getODExist(CMaterials.FZ_DARK_IRON, CMaterials.INGOT),
                60L
            );
        }

        if (ClayiumCore.IntegrationID.MISC.enabled()) {
            register2to1Recipe(
                recipeCAInjector,
                CMaterials.getOD(CMaterials.EMERALD, CMaterials.GEM),
                CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM),
                10,
                CMaterials.getODExist(CMaterials.TOPAZ, CMaterials.GEM),
                60L
            );
            registerODChainRecipes(
                recipeTransformer, new CMaterial[]{CMaterials.TOPAZ, CMaterials.MALACHITE, CMaterials.TANZANITE}, CMaterials.GEM, new int[]{0, 10, 10}, 200
            );
            register2to1Recipe(
                recipeReactor,
                CMaterials.getOD(CMaterials.QUARTZ, CMaterials.GEM),
                CMaterials.getOD(CMaterials.LITHIUM, CMaterials.DUST),
                8,
                CMaterials.getODExist(CMaterials.DILITHIUM, CMaterials.GEM),
                1000000000L
            );
            register2to1Recipe(
                recipeReactor,
                CMaterials.getOD(CMaterials.QUARTZ, CMaterials.GEM),
                i(Items.field_151137_ax, 4),
                8,
                CMaterials.getODExist(CMaterials.FORCICIUM, CMaterials.GEM, 4),
                1000000000L
            );
        }

        if (ClayiumCore.IntegrationID.GT.enabled()) {
            registerODChainRecipes(recipeTransformer, new CMaterial[]{CMaterials.ALUMINIUM, CMaterials.GALLIUM}, CMaterials.INGOT, new int[]{0, 11}, 200);
            registerODChainRecipes(recipeTransformer, new CMaterial[]{CMaterials.ALUMINIUM_OD, CMaterials.GALLIUM}, CMaterials.INGOT, new int[]{0, 11}, 200);
            registerODChainRecipes(
                recipeTransformer, new CMaterial[]{CMaterials.VANADIUM, CMaterials.NIOBIUM, CMaterials.YTTRIUM}, CMaterials.INGOT, new int[]{0, 11, 11}, 200
            );
            registerODChainRecipes(
                recipeTransformer,
                new CMaterial[]{CMaterials.EUROPIUM, CMaterials.NAQUADAH, CMaterials.NAQUADAH_ENRICHED, CMaterials.NAQUADRIA},
                CMaterials.INGOT,
                new int[]{0, 12, 12, 12},
                200
            );
            registerODChainRecipes(recipeTransformer, new CMaterial[]{CMaterials.CURIUM, CMaterials.NEUTRONIUM}, CMaterials.INGOT, new int[]{0, 12}, 200);
            registerODChainRecipes(
                recipeTransformer, new CMaterial[]{CMaterials.REDSTONE, CMaterials.NIKOLITE, CMaterials.ELECTROTINE}, CMaterials.DUST, new int[]{0, 9, 9}, 200
            );
            register2to1Recipe(
                recipeCAInjector,
                CMaterials.getOD(CMaterials.QUARTZ, CMaterials.GEM),
                CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM),
                10,
                CMaterials.getODExist(CMaterials.QUARTZITE, CMaterials.GEM),
                60L
            );
            register2to1Recipe(
                recipeCAInjector,
                CMaterials.getOD(CMaterials.SALTPETER, CMaterials.DUST),
                CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM),
                10,
                CMaterials.getODExist(CMaterials.NITER, CMaterials.GEM),
                60L
            );

            for (CShape shape1 : new CShape[]{CMaterials.INGOT, CMaterials.DUST}) {
                for (CShape shape2 : new CShape[]{CMaterials.INGOT, CMaterials.DUST}) {
                    register2to1Recipe(
                        recipeBlastFurnace,
                        CMaterials.getOD(CMaterials.TUNGSTEN, shape1),
                        CMaterials.getOD(CMaterials.STEEL, shape2),
                        10,
                        CMaterials.getODExist(CMaterials.TUNGSTEN_STEEL, CMaterials.INGOT, 2),
                        1000L
                    );
                    register2to1Recipe(
                        recipeAlloySmelter,
                        CMaterials.getOD(CMaterials.COPPER, shape1),
                        CMaterials.getOD(CMaterials.NICKEL, shape2),
                        6,
                        CMaterials.getODExist(CMaterials.CUPRONICKEL, CMaterials.INGOT, 2),
                        100L
                    );
                    register2to1Recipe(
                        recipeBlastFurnace,
                        CMaterials.getOD(CMaterials.NICKEL, shape1, 4),
                        CMaterials.getOD(CMaterials.CHROME, shape2),
                        9,
                        CMaterials.getODExist(CMaterials.NICHROME, CMaterials.INGOT, 5),
                        1000L
                    );
                    register2to1Recipe(
                        recipeAlloySmelter,
                        CMaterials.getOD(CMaterials.ALUMINIUM, shape1, 2),
                        CMaterials.getOD(CMaterials.MAGNESIUM, shape2),
                        6,
                        CMaterials.getODExist(CMaterials.MAGNALIUM, CMaterials.INGOT, 3),
                        100L
                    );
                    register2to1Recipe(
                        recipeAlloySmelter,
                        CMaterials.getOD(CMaterials.ALUMINIUM_OD, shape1, 2),
                        CMaterials.getOD(CMaterials.MAGNESIUM, shape2),
                        6,
                        CMaterials.getODExist(CMaterials.MAGNALIUM, CMaterials.INGOT, 3),
                        100L
                    );
                    register2to1Recipe(
                        recipeAlloySmelter,
                        CMaterials.getOD(CMaterials.TIN, shape1, 9),
                        CMaterials.getOD(CMaterials.ANTIMONY, shape2),
                        6,
                        CMaterials.getODExist(CMaterials.SOLDERING_ALLOY, CMaterials.INGOT, 10),
                        100L
                    );
                    register2to1Recipe(
                        recipeAlloySmelter,
                        CMaterials.getOD(CMaterials.LEAD, shape1, 4),
                        CMaterials.getOD(CMaterials.ANTIMONY, shape2),
                        6,
                        CMaterials.getODExist(CMaterials.BATTERY_ALLOY, CMaterials.INGOT, 5),
                        100L
                    );
                    register2to1Recipe(
                        recipeBlastFurnace,
                        CMaterials.getOD(CMaterials.VANADIUM, shape1, 3),
                        CMaterials.getOD(CMaterials.GALLIUM, shape2),
                        10,
                        CMaterials.getODExist(CMaterials.VANADIUM_GALLIUM, CMaterials.INGOT, 4),
                        1000L
                    );
                    register2to1Recipe(
                        recipeBlastFurnace,
                        CMaterials.getOD(CMaterials.NIOBIUM, shape1),
                        CMaterials.getOD(CMaterials.TITANIUM, shape2),
                        9,
                        CMaterials.getODExist(CMaterials.NIOBIUM_TITANIUM, CMaterials.INGOT, 2),
                        1000L
                    );
                    register2to1Recipe(
                        recipeAlloySmelter,
                        CMaterials.getOD(CMaterials.TIN, shape1),
                        CMaterials.getOD(CMaterials.IRON, shape2),
                        6,
                        CMaterials.getODExist(CMaterials.TIN_ALLOY, CMaterials.INGOT, 2),
                        100L
                    );
                    register2to1Recipe(
                        recipeReactor,
                        CMaterials.getOD(CMaterials.STEEL, shape1),
                        CMaterials.getOD(CMaterials.IRON, shape2, 4),
                        8,
                        CMaterials.getODExist(CMaterials.WROUGHT_IRON, CMaterials.INGOT, 5),
                        1000000000L
                    );
                }

                register2to1Recipe(
                    recipeAlloySmelter,
                    CMaterials.getOD(CMaterials.SILVER, shape1),
                    CMaterials.getOD(CMaterials.NIKOLITE, CMaterials.DUST),
                    6,
                    CMaterials.getODExist(CMaterials.BLUE_ALLOY, CMaterials.INGOT, 2),
                    100L
                );
            }

            register1to1Recipe(
                recipeBlastFurnace,
                CMaterials.getOD(CMaterials.TUNGSTEN_STEEL, CMaterials.DUST),
                10,
                CMaterials.getODExist(CMaterials.TUNGSTEN_STEEL, CMaterials.INGOT),
                1000L
            );
            register1to1Recipe(
                recipeBlastFurnace,
                CMaterials.getOD(CMaterials.KANTHAL, CMaterials.DUST),
                7,
                CMaterials.getODExist(CMaterials.KANTHAL, CMaterials.INGOT),
                1000L
            );
            register1to1Recipe(
                recipeBlastFurnace,
                CMaterials.getOD(CMaterials.STAINLESS_STEEL, CMaterials.DUST),
                7,
                CMaterials.getODExist(CMaterials.STAINLESS_STEEL, CMaterials.INGOT),
                1000L
            );
            register1to1Recipe(
                recipeBlastFurnace,
                CMaterials.getOD(CMaterials.VANADIUM_GALLIUM, CMaterials.DUST),
                10,
                CMaterials.getODExist(CMaterials.VANADIUM_GALLIUM, CMaterials.INGOT),
                1000L
            );
            register1to1Recipe(
                recipeBlastFurnace,
                CMaterials.getOD(CMaterials.YTTRIUM_BARIUM_CUPRATE, CMaterials.DUST),
                6,
                CMaterials.getODExist(CMaterials.YTTRIUM_BARIUM_CUPRATE, CMaterials.INGOT),
                1000L
            );
            register1to1Recipe(
                recipeBlastFurnace,
                CMaterials.getOD(CMaterials.NIOBIUM_TITANIUM, CMaterials.DUST),
                9,
                CMaterials.getODExist(CMaterials.NIOBIUM_TITANIUM, CMaterials.INGOT),
                1000L
            );
            register1to1Recipe(
                recipeBlastFurnace,
                CMaterials.getOD(CMaterials.ULTIMET, CMaterials.DUST),
                9,
                CMaterials.getODExist(CMaterials.ULTIMET, CMaterials.INGOT),
                1000L
            );
            if (CMaterials.existOD(CMaterials.SAPPHIRE, CMaterials.GEM)) {
                for (CMaterial materialxxxxxxxxxxx : new CMaterial[]{
                    CMaterials.GREEN_SAPPHIRE,
                    CMaterials.OPAL,
                    CMaterials.JASPER,
                    CMaterials.GARNET_RED,
                    CMaterials.GARNET_YELLOW,
                    CMaterials.FORCE,
                    CMaterials.FORCILLIUM
                }) {
                    register2to1Recipe(
                        recipeCAInjector,
                        CMaterials.getOD(CMaterials.SAPPHIRE, CMaterials.GEM),
                        CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM),
                        10,
                        CMaterials.getODExist(materialxxxxxxxxxxx, CMaterials.INGOT),
                        60L
                    );
                    if (CMaterials.existOD(materialxxxxxxxxxxx, CMaterials.GEM)) {
                        break;
                    }
                }
            } else {
                for (CMaterial materialxxxxxxxxxxxx : new CMaterial[]{
                    CMaterials.GREEN_SAPPHIRE,
                    CMaterials.OPAL,
                    CMaterials.JASPER,
                    CMaterials.GARNET_RED,
                    CMaterials.GARNET_YELLOW,
                    CMaterials.FORCE,
                    CMaterials.FORCILLIUM
                }) {
                    register2to1Recipe(
                        recipeCAInjector,
                        CMaterials.getOD(CMaterials.DIAMOND, CMaterials.GEM),
                        CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM),
                        10,
                        CMaterials.getODExist(materialxxxxxxxxxxxx, CMaterials.INGOT),
                        60L
                    );
                    if (CMaterials.existOD(materialxxxxxxxxxxxx, CMaterials.GEM)) {
                        break;
                    }
                }
            }

            registerODChainRecipes(
                recipeTransformer,
                new CMaterial[]{
                    CMaterials.GREEN_SAPPHIRE,
                    CMaterials.OPAL,
                    CMaterials.JASPER,
                    CMaterials.GARNET_RED,
                    CMaterials.GARNET_YELLOW,
                    CMaterials.FORCE,
                    CMaterials.FORCILLIUM
                },
                CMaterials.GEM,
                new int[]{0, 8, 8, 9, 9, 11, 11},
                200
            );
            if (CMaterials.existOD(CMaterials.TOPAZ, CMaterials.GEM)) {
                register2to1Recipe(
                    recipeCAInjector,
                    CMaterials.getOD(CMaterials.TOPAZ, CMaterials.GEM),
                    CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM),
                    10,
                    CMaterials.getODExist(CMaterials.BLUE_TOPAZ, CMaterials.GEM),
                    60L
                );
            } else {
                register2to1Recipe(
                    recipeCAInjector,
                    CMaterials.getOD(CMaterials.EMERALD, CMaterials.GEM),
                    CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM),
                    10,
                    CMaterials.getODExist(CMaterials.BLUE_TOPAZ, CMaterials.GEM),
                    60L
                );
            }
        }

        if (ClayiumCore.IntegrationID.MET.enabled()) {
            for (CMaterial materialxxxxxxxxxxxxx : new CMaterial[]{
                CMaterials.PROMETHEUM,
                CMaterials.DEEP_IRON,
                CMaterials.INFUSCOLIUM,
                CMaterials.OURECLASE,
                CMaterials.AREDRITE,
                CMaterials.ASTRAL_SILVER,
                CMaterials.CARMOT,
                CMaterials.MITHRIL,
                CMaterials.RUBRACIUM,
                CMaterials.ORICHALCUM,
                CMaterials.ADAMANTINE,
                CMaterials.ATLARUS
            }) {
                register2to1Recipe(
                    recipeCAInjector,
                    CMaterials.getOD(CMaterials.PROMETHIUM, CMaterials.INGOT),
                    CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM),
                    10,
                    CMaterials.getODExist(materialxxxxxxxxxxxxx, CMaterials.INGOT),
                    60L
                );
                if (CMaterials.existOD(materialxxxxxxxxxxxxx, CMaterials.INGOT)) {
                    break;
                }
            }

            registerODChainRecipes(
                recipeTransformer,
                new CMaterial[]{
                    CMaterials.PROMETHEUM,
                    CMaterials.DEEP_IRON,
                    CMaterials.INFUSCOLIUM,
                    CMaterials.OURECLASE,
                    CMaterials.AREDRITE,
                    CMaterials.ASTRAL_SILVER,
                    CMaterials.CARMOT,
                    CMaterials.MITHRIL,
                    CMaterials.RUBRACIUM,
                    CMaterials.ORICHALCUM,
                    CMaterials.ADAMANTINE,
                    CMaterials.ATLARUS
                },
                CMaterials.INGOT,
                new int[]{0, 7, 7, 8, 8, 9, 9, 9, 10, 10, 11, 11},
                200
            );

            for (CMaterial materialxxxxxxxxxxxxxx : new CMaterial[]{
                CMaterials.IGNATIUS,
                CMaterials.SHADOW_IRON,
                CMaterials.LEMURITE,
                CMaterials.MIDASIUM,
                CMaterials.VYROXERES,
                CMaterials.CERUCLASE,
                CMaterials.ALDUORITE,
                CMaterials.KALENDRITE,
                CMaterials.VULCANITE,
                CMaterials.SANGUINITE
            }) {
                register2to1Recipe(
                    recipeCAInjector,
                    CMaterials.getOD(CMaterials.RUBIDIUM, CMaterials.INGOT),
                    CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM),
                    10,
                    CMaterials.getODExist(materialxxxxxxxxxxxxxx, CMaterials.INGOT),
                    60L
                );
                if (CMaterials.existOD(materialxxxxxxxxxxxxxx, CMaterials.INGOT)) {
                    break;
                }
            }

            registerODChainRecipes(
                recipeTransformer,
                new CMaterial[]{
                    CMaterials.IGNATIUS,
                    CMaterials.SHADOW_IRON,
                    CMaterials.LEMURITE,
                    CMaterials.MIDASIUM,
                    CMaterials.VYROXERES,
                    CMaterials.CERUCLASE,
                    CMaterials.ALDUORITE,
                    CMaterials.KALENDRITE,
                    CMaterials.VULCANITE,
                    CMaterials.SANGUINITE
                },
                CMaterials.INGOT,
                new int[]{0, 7, 7, 8, 8, 8, 8, 9, 10, 11},
                200
            );

            for (CMaterial materialxxxxxxxxxxxxxxx : new CMaterial[]{CMaterials.EXIMITE, CMaterials.MEUTOITE}) {
                register2to1Recipe(
                    recipeCAInjector,
                    CMaterials.getOD(CMaterials.PROTACTINIUM, CMaterials.INGOT),
                    CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM),
                    10,
                    CMaterials.getODExist(materialxxxxxxxxxxxxxxx, CMaterials.INGOT),
                    60L
                );
                if (CMaterials.existOD(materialxxxxxxxxxxxxxxx, CMaterials.INGOT)) {
                    break;
                }
            }

            registerODChainRecipes(recipeTransformer, new CMaterial[]{CMaterials.EXIMITE, CMaterials.MEUTOITE}, CMaterials.INGOT, new int[]{0, 9}, 200);

            for (CShape shape1 : new CShape[]{CMaterials.INGOT, CMaterials.DUST}) {
                for (CShape shape2 : new CShape[]{CMaterials.INGOT, CMaterials.DUST}) {
                    register2to1Recipe(
                        recipeAlloySmelter,
                        CMaterials.getOD(CMaterials.BRONZE, shape1),
                        CMaterials.getOD(CMaterials.GOLD, shape2),
                        6,
                        CMaterials.getODExist(CMaterials.HEPATIZON, CMaterials.INGOT, 2),
                        100L
                    );
                    register2to1Recipe(
                        recipeAlloySmelter,
                        CMaterials.getOD(CMaterials.BRONZE, shape1),
                        CMaterials.getOD(CMaterials.IRON, shape2),
                        6,
                        CMaterials.getODExist(CMaterials.DAMASCUS_STEEL, CMaterials.INGOT, 2),
                        100L
                    );
                    register2to1Recipe(
                        recipeAlloySmelter,
                        CMaterials.getOD(CMaterials.IRON, shape1),
                        CMaterials.getOD(CMaterials.GOLD, shape2),
                        6,
                        CMaterials.getODExist(CMaterials.ANGMALLEN, CMaterials.INGOT, 2),
                        100L
                    );
                    register2to1Recipe(
                        recipeAlloySmelter,
                        CMaterials.getOD(CMaterials.DEEP_IRON, shape1),
                        CMaterials.getOD(CMaterials.INFUSCOLIUM, shape2),
                        6,
                        CMaterials.getODExist(CMaterials.BLACK_STEEL, CMaterials.INGOT, 2),
                        100L
                    );
                    register2to1Recipe(
                        recipeAlloySmelter,
                        CMaterials.getOD(CMaterials.MITHRIL, shape1),
                        CMaterials.getOD(CMaterials.SILVER, shape2),
                        6,
                        CMaterials.getODExist(CMaterials.QUICKSILVER, CMaterials.INGOT, 2),
                        100L
                    );
                    register2to1Recipe(
                        recipeAlloySmelter,
                        CMaterials.getOD(CMaterials.MITHRIL, shape1),
                        CMaterials.getOD(CMaterials.RUBRACIUM, shape2),
                        6,
                        CMaterials.getODExist(CMaterials.HADEROTH, CMaterials.INGOT, 2),
                        100L
                    );
                    register2to1Recipe(
                        recipeAlloySmelter,
                        CMaterials.getOD(CMaterials.ORICHALCUM, shape1),
                        CMaterials.getOD(CMaterials.PLATINUM, shape2),
                        6,
                        CMaterials.getODExist(CMaterials.CELENEGIL, CMaterials.INGOT, 2),
                        100L
                    );
                    register2to1Recipe(
                        recipeAlloySmelter,
                        CMaterials.getOD(CMaterials.ADAMANTINE, shape1),
                        CMaterials.getOD(CMaterials.ATLARUS, shape2),
                        6,
                        CMaterials.getODExist(CMaterials.TARTARITE, CMaterials.INGOT, 2),
                        100L
                    );
                    register2to1Recipe(
                        recipeAlloySmelter,
                        CMaterials.getOD(CMaterials.SHADOW_IRON, shape1),
                        CMaterials.getOD(CMaterials.LEMURITE, shape2),
                        6,
                        CMaterials.getODExist(CMaterials.SHADOW_STEEL, CMaterials.INGOT, 2),
                        100L
                    );
                    register2to1Recipe(
                        recipeAlloySmelter,
                        CMaterials.getOD(CMaterials.ALDUORITE, shape1),
                        CMaterials.getOD(CMaterials.CERUCLASE, shape2),
                        6,
                        CMaterials.getODExist(CMaterials.INOLASHITE, CMaterials.INGOT, 2),
                        100L
                    );
                    register2to1Recipe(
                        recipeAlloySmelter,
                        CMaterials.getOD(CMaterials.KALENDRITE, shape1),
                        CMaterials.getOD(CMaterials.PLATINUM, shape2),
                        6,
                        CMaterials.getODExist(CMaterials.AMORDRINE, CMaterials.INGOT, 2),
                        100L
                    );
                    register2to1Recipe(
                        recipeAlloySmelter,
                        CMaterials.getOD(CMaterials.EXIMITE, shape1),
                        CMaterials.getOD(CMaterials.MEUTOITE, shape2),
                        6,
                        CMaterials.getODExist(CMaterials.DESICHALKOS, CMaterials.INGOT, 2),
                        100L
                    );
                }
            }
        }

        if (ClayiumCore.IntegrationID.SS2.enabled()) {
            for (CShape shape1 : new CShape[]{CMaterials.INGOT, CMaterials.DUST}) {
                for (CShape shape2 : new CShape[]{CMaterials.GEM, CMaterials.DUST}) {
                    register2to1Recipe(
                        recipeAlloySmelter,
                        CMaterials.getOD(CMaterials.DIAMOND, shape2),
                        CMaterials.getOD(CMaterials.MITHRIL, shape1),
                        6,
                        CMaterials.getODExist(CMaterials.NINJA, CMaterials.INGOT),
                        100L
                    );
                }

                register2to1Recipe(
                    recipeReactor,
                    CMaterials.getOD(CMaterials.DIAMOND, CMaterials.GEM),
                    CMaterials.getOD(CMaterials.PLATINUM, shape1),
                    11,
                    CMaterials.getODExist(CMaterials.ORICHALCUM, CMaterials.GEM),
                    1000000000000000L
                );
            }

            register2to1Recipe(
                recipeReactor,
                CMaterials.getOD(CMaterials.REDSTONE, CMaterials.DUST),
                "dyeYellow",
                9,
                CMaterials.getODExist(CMaterials.YELLOWSTONE, CMaterials.DUST),
                10000000000000L
            );
            register2to1Recipe(
                recipeReactor,
                CMaterials.getOD(CMaterials.REDSTONE, CMaterials.DUST),
                "dyeBlue",
                9,
                CMaterials.getODExist(CMaterials.BLUESTONE, CMaterials.DUST),
                10000000000000L
            );
            register2to1Recipe(
                recipeReactor,
                CMaterials.getOD(CMaterials.REDSTONE, CMaterials.DUST),
                CMaterials.getOD(CMaterials.YELLOWSTONE, CMaterials.DUST),
                8,
                CMaterials.getODExist(CMaterials.YELLOWSTONE, CMaterials.INGOT),
                1000000000L
            );
            register2to1Recipe(
                recipeReactor,
                CMaterials.getOD(CMaterials.REDSTONE, CMaterials.DUST),
                CMaterials.getOD(CMaterials.BLUESTONE, CMaterials.DUST),
                8,
                CMaterials.getODExist(CMaterials.BLUESTONE, CMaterials.INGOT),
                1000000000L
            );
        }

        if (ClayiumCore.IntegrationID.MAPLE.enabled()) {
            register1to1Recipe(recipeGrinder, "oreMapleDiamond", 5, CMaterials.getODExist("mapleDiamond", 2), 80L);
            register1to1Recipe(recipeGrinder, "oreDemantoidGarnet", 5, CMaterials.getODExist("demantoidGarnet", 2), 80L);
            register1to1Recipe(recipeGrinder, "oreMarble", 5, CMaterials.getODExist("marble", 2), 80L);
            register2to1Recipe(
                recipeReactor,
                CMaterials.getOD(CMaterials.DIAMOND, CMaterials.GEM),
                i(Items.field_151034_e),
                10,
                CMaterials.getODExist("mapleDiamond"),
                100000000000000L
            );
            register1to1Recipe(recipeTransformer, "mapleDiamond", 10, CMaterials.getODExist("demantoidGarnet"), 200L);
        }

        if (ClayiumCore.IntegrationID.TOFU.enabled()) {
            register2to1Recipe(recipeReactor, "soybeans", "nigari", 9, CMaterials.getODExist("tofuKinu", 4), 10000000000000L);
            register2to1Recipe(recipeReactor, "tofuKinu", "plankWood", 7, CMaterials.getODExist("tofuMomen"), 10000L);
            register2to1Recipe(recipeReactor, "tofuMomen", "cobblestone", 8, CMaterials.getODExist("tofuIshi"), 1000000000L);
            register2to1Recipe(
                recipeReactor, "tofuIshi", CMaterials.getOD(CMaterials.IRON, CMaterials.INGOT), 9, CMaterials.getODExist("tofuMetal"), 10000000000000L
            );
            register2to1Recipe(
                recipeReactor, "tofuMetal", CMaterials.getOD(CMaterials.DIAMOND, CMaterials.GEM), 11, CMaterials.getODExist("tofuDiamond"), 10000000000000000L
            );
            register2to1Recipe(
                recipeReactor, "tofuMomen", CMaterials.getOD(CMaterials.DIAMOND, CMaterials.GEM), 9, CMaterials.getODExist("tofuGem"), 10000000000000L
            );
        }
    }

    public static class RecipeConditions {
        public int tier = 0;
        public long time = 0L;
        public Recipes recipes = null;

        public RecipeConditions(Recipes recipes, int tier, long time) {
            super();
            this.recipes = recipes;
            this.tier = tier;
            this.time = time;
        }
    }
}
