package net.claustra01.clayium.material;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * Complete catalog of Clayium materials ported from 1.7.10 CMaterials.
 */
public final class CMaterials {
    private static final Map<String, CMaterial> BY_NAME = new LinkedHashMap<>();
    private static final Map<CMaterial, Map<CShape, Supplier<Item>>> ITEM_MAP = new LinkedHashMap<>();

    public static final CMaterial CLAY = reg("clay", "Clay", "clay", "Clay", 512, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial DENSE_CLAY = reg("dense_clay", "DenseClay", "denseclay", "DenseClay", 513, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial IND_CLAY = reg("industrial_clay", "IndustrialClay", "indclay", "IndustrialClay", 515, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ADVIND_CLAY = reg("advanced_industrial_clay", "AdvancedIndustrialClay", "advindclay", "AdvancedIndustrialClay", 516, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ENG_CLAY = reg("energized_clay", "EnergizedClay", "engclay", "EnergizedClay", 768, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial IMPURE_SILICON = reg("impure_silicon", "ImpureSilicon", "impuresilicon", "ImpureSilicon", 142, new int[]{151, 143, 152}, new int[]{83, 55, 100}, new int[]{169, 165, 165}, 1.0F);
    public static final CMaterial SILICON = reg("silicon", "Silicon", "silicon", "Silicon", 14, new int[]{40, 28, 40}, new int[]{6, 4, 6}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial IRON = reg("iron", "Iron", "iron", "Iron", 26, new int[]{216, 216, 216}, new int[]{53, 53, 53}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial GOLD = reg("gold", "Gold", "gold", "Gold", 79, new int[]{255, 255, 10}, new int[]{60, 60, 0}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ALUMINIUM = reg("aluminium", "Aluminium", "alminium", "Aluminum", 13, new int[]{190, 200, 202}, new int[]{31, 33, 33}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial MAGNESIUM = reg("magnesium", "Magnesium", "magnesium", "Magnesium", 12, new int[]{150, 210, 150}, new int[]{120, 120, 120}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial SODIUM = reg("sodium", "Sodium", "sodium", "Sodium", 11, new int[]{170, 170, 222}, new int[]{120, 120, 120}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial LITHIUM = reg("lithium", "Lithium", "lithium", "Lithium", 3, new int[]{210, 210, 150}, new int[]{120, 120, 120}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ZIRCONIUM = reg("zirconium", "Zirconium", "zirconium", "Zirconium", 40, new int[]{190, 170, 122}, new int[]{120, 120, 120}, new int[]{255, 255, 244}, 1.0F);
    public static final CMaterial ZINC = reg("zinc", "Zinc", "zinc", "Zinc", 30, new int[]{230, 170, 170}, new int[]{120, 120, 120}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial MANGANESE = reg("manganese", "Manganese", "manganese", "Manganese", 25, new int[]{190, 240, 240}, new int[]{31, 40, 40}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial CALCIUM = reg("calcium", "Calcium", "calcium", "Calcium", 20, new int[]{240, 240, 240}, new int[]{40, 40, 40}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial POTASSIUM = reg("potassium", "Potassium", "potassium", "Potassium", 19, new int[]{240, 240, 190}, new int[]{40, 40, 31}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial NICKEL = reg("nickel", "Nickel", "nickel", "Nickel", 28, new int[]{210, 210, 240}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial BERYLLIUM = reg("beryllium", "Beryllium", "beryllium", "Beryllium", 4, new int[]{210, 240, 210}, new int[]{35, 40, 35}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial LEAD = reg("lead", "Lead", "lead", "Lead", 82, new int[]{190, 240, 210}, new int[]{31, 40, 35}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial HAFNIUM = reg("hafnium", "Hafnium", "hafnium", "Hafnium", 72, new int[]{240, 210, 170}, new int[]{40, 35, 28}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial CHROME = reg("chrome", "Chrome", "chrome", "Chrome", 24, new int[]{240, 210, 210}, new int[]{40, 35, 35}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial TITANIUM = reg("titanium", "Titanium", "titanium", "Titanium", 22, new int[]{210, 240, 240}, new int[]{35, 40, 40}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial STRONTIUM = reg("strontium", "Strontium", "strontium", "Strontium", 38, new int[]{210, 170, 242}, new int[]{35, 28, 40}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial BARIUM = reg("barium", "Barium", "barium", "Barium", 56, new int[]{150, 80, 120}, new int[]{120, 20, 80}, new int[]{255, 160, 240}, 1.0F);
    public static final CMaterial COPPER = reg("copper", "Copper", "copper", "Copper", 29, new int[]{160, 90, 10}, new int[]{26, 15, 1}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ZINCALMINIUM_ALLOY = reg("zincalminium", "Zincalminium", "zincalminium", "Zincalminium", 1344, new int[]{240, 190, 220}, new int[]{160, 0, 0}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial AZ91D_ALLOY = reg("az91d", "AZ91D", "az91d", "AZ91D", 1312, new int[]{130, 140, 135}, new int[]{10, 40, 10}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ZINCONIUM_ALLOY = reg("zinconium", "Zinconium", "zinconium", "Zinconium", 1345, new int[]{230, 170, 140}, new int[]{120, 0, 0}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ZK60A_ALLOY = reg("zk60a", "ZK60A", "zk60a", "ZK60A", 1313, new int[]{75, 85, 80}, new int[]{10, 40, 10}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial IMPURE_ALUMINIUM = reg("impure_aluminium", "ImpureAluminium", "alminium", "ImpureAluminium", 141, new int[]{190, 200, 202}, new int[]{120, 120, 60}, new int[]{220, 220, 220}, 1.0F);
    public static final CMaterial IMPURE_MAGNESIUM = reg("impure_magnesium", "ImpureMagnesium", "impuremagnesium", "ImpureMagnesium", 140, new int[]{150, 220, 150}, new int[]{120, 120, 60}, new int[]{220, 220, 220}, 1.0F);
    public static final CMaterial IMPURE_SODIUM = reg("impure_sodium", "ImpureSodium", "impuresodium", "ImpureSodium", 139, new int[]{170, 170, 230}, new int[]{120, 120, 60}, new int[]{220, 220, 220}, 1.0F);
    public static final CMaterial IMPURE_LITHIUM = reg("impure_lithium", "ImpureLithium", "impurelithium", "ImpureLithium", 131, new int[]{220, 220, 150}, new int[]{120, 120, 60}, new int[]{220, 220, 220}, 1.0F);
    public static final CMaterial IMPURE_ZIRCONIUM = reg("impure_zirconium", "ImpureZirconium", "impurezirconium", "ImpureZirconium", 168, new int[]{190, 170, 122}, new int[]{120, 120, 60}, new int[]{220, 220, 220}, 1.0F);
    public static final CMaterial IMPURE_ZINC = reg("impure_zinc", "ImpureZinc", "impurezinc", "ImpureZinc", 158, new int[]{230, 170, 170}, new int[]{120, 120, 60}, new int[]{220, 220, 220}, 1.0F);
    public static final CMaterial IMPURE_MANGANESE = reg("impure_manganese", "ImpureManganese", "impuremanganese", "ImpureManganese", 153, new int[]{190, 240, 240}, new int[]{120, 120, 60}, new int[]{220, 220, 220}, 1.0F);
    public static final CMaterial IMPURE_CALCIUM = reg("impure_calcium", "ImpureCalcium", "impurecalcium", "ImpureCalcium", 148, new int[]{240, 240, 240}, new int[]{120, 120, 60}, new int[]{220, 220, 220}, 1.0F);
    public static final CMaterial IMPURE_POTASSIUM = reg("impure_potassium", "ImpurePotassium", "impurepotassium", "ImpurePotassium", 147, new int[]{240, 240, 190}, new int[]{120, 120, 60}, new int[]{220, 220, 220}, 1.0F);
    public static final CMaterial IMPURE_NICKEL = reg("impure_nickel", "ImpureNickel", "impurenickel", "ImpureNickel", 156, new int[]{210, 210, 240}, new int[]{120, 120, 60}, new int[]{220, 220, 220}, 1.0F);
    public static final CMaterial IMPURE_IRON = reg("impure_iron", "ImpureIron", "impureiron", "ImpureIron", 154, new int[]{216, 216, 216}, new int[]{120, 120, 60}, new int[]{220, 220, 220}, 1.0F);
    public static final CMaterial IMPURE_BERYLLIUM = reg("impure_beryllium", "ImpureBeryllium", "impureberyllium", "ImpureBeryllium", 132, new int[]{210, 240, 210}, new int[]{120, 120, 60}, new int[]{220, 220, 220}, 1.0F);
    public static final CMaterial IMPURE_LEAD = reg("impure_lead", "ImpureLead", "impurelead", "ImpureLead", 210, new int[]{190, 240, 210}, new int[]{120, 120, 60}, new int[]{220, 220, 220}, 1.0F);
    public static final CMaterial IMPURE_HAFNIUM = reg("impure_hafnium", "ImpureHafnium", "impurehafnium", "ImpureHafnium", 200, new int[]{240, 210, 170}, new int[]{120, 120, 60}, new int[]{220, 220, 220}, 1.0F);
    public static final CMaterial IMPURE_CHROME = reg("impure_chrome", "ImpureChrome", "impurechrome", "ImpureChrome", 152, new int[]{240, 210, 210}, new int[]{120, 120, 60}, new int[]{220, 220, 220}, 1.0F);
    public static final CMaterial IMPURE_TITANIUM = reg("impure_titanium", "ImpureTitanium", "impuretitanium", "ImpureTitanium", 150, new int[]{210, 240, 240}, new int[]{120, 120, 60}, new int[]{220, 220, 220}, 1.0F);
    public static final CMaterial IMPURE_STRONTIUM = reg("impure_strontium", "ImpureStrontium", "impurestrontium", "ImpureStrontium", 166, new int[]{210, 170, 242}, new int[]{120, 120, 60}, new int[]{220, 220, 220}, 1.0F);
    public static final CMaterial IMPURE_BARIUM = reg("impure_barium", "ImpureBarium", "impurebarium", "ImpureBarium", 184, new int[]{150, 80, 120}, new int[]{120, 120, 60}, new int[]{220, 220, 220}, 1.0F);
    public static final CMaterial IMPURE_COPPER = reg("impure_copper", "ImpureCopper", "impurecopper", "ImpureCopper", 157, new int[]{160, 90, 10}, new int[]{120, 120, 60}, new int[]{220, 220, 220}, 1.0F);
    public static final CMaterial IMPURE_OSMIUM = reg("impure_osmium", "ImpureOsmium", "impureosmium", "ImpureOsmium", 204, new int[]{70, 70, 150}, new int[]{120, 120, 60}, new int[]{220, 220, 220}, 1.0F);
    public static final CMaterial SALT = reg("salt", "Salt", "salt", "Salt", 1024, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial CAL_CLAY = reg("calcareous_clay", "CalcareousClay", "calclay", "CalcareousClay", 769, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial CALCIUM_CHLORIDE = reg("calcium_chloride", "CalciumChloride", "calciumchloride", "CalciumChloride", 1025, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial SODIUM_CARBONATE = reg("sodium_carbonate", "SodiumCarbonate", "sodiumcarbonate", "SodiumCarbonate", 1026, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial QUARTZ = reg("quartz", "Quartz", "quartz", "Quartz", 1027, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial SILICONE = reg("silicone", "Silicone", "silicone", "Silicone", 1028, new int[]{210, 210, 210}, new int[]{180, 180, 180}, new int[]{240, 240, 240}, 0.2F);
    public static final CMaterial EXC_CLAY = reg("excited_clay", "ExcitedClay", "excclay", "ExcitedClay", 770, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial CLAY_STEEL = reg("clay_steel", "ClaySteel", "claysteel", "ClaySteel", 256, new int[]{136, 144, 173}, new int[]{22, 24, 28}, new int[]{255, 255, 255}, 3.0F);
    public static final CMaterial CLAYIUM = reg("clayium", "Clayium", "clayium", "Clayium", 257, new int[]{90, 240, 210}, new int[]{63, 72, 85}, new int[]{255, 205, 200}, 6.0F);
    public static final CMaterial ULTIMATE_ALLOY = reg("ultimate_alloy", "UltimateAlloy", "ultimatealloy", "UltimateAlloy", 258, new int[]{85, 205, 85}, new int[]{14, 34, 14}, new int[]{245, 160, 255}, 9.0F);
    public static final CMaterial ORG_CLAY = reg("organic_clay", "OrganicClay", "orgclay", "OrganicClay", 771, new int[]{136, 144, 173}, new int[]{106, 44, 43}, new int[]{146, 164, 183}, 1.0F);
    public static final CMaterial ANTIMATTER = reg("antimatter", "Antimatter", "antimatter", "Antimatter", 800, new int[]{0, 0, 235}, new int[]{0, 0, 0}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial PURE_ANTIMATTER = reg("pure_antimatter", "PureAntimatter", "pureantimatter", "PureAntimatter", 801, new int[]{255, 50, 255}, new int[]{0, 0, 0}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial OCTUPLE_CLAY = reg("octuple_energetic_clay", "OctupleEnergeticClay", "octupleenergeticclay", "OctupleEnergeticClay", 525, new int[]{255, 255, 0}, new int[]{140, 140, 140}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial IMPURE_ULTIMATE_ALLOY = reg("impure_ultimate_alloy", "ImpureUltimateAlloy", "impureultimatealloy", "ImpureUltimateAlloy", 386, new int[]{85, 205, 85}, new int[]{245, 255, 255}, new int[]{245, 160, 255}, 9.0F);
    public static final CMaterial IMPURE_REDSTONE = reg("impure_redstone", "ImpureRedstone", "impureredstone", "ImpureRedstone", 897, new int[]{151, 70, 70}, new int[]{25, 11, 11}, new int[]{255, 140, 140}, 1.0F);
    public static final CMaterial IMPURE_GLOWSTONE = reg("impure_glowstone", "ImpureGlowstone", "impureglowstone", "ImpureGlowstone", 898, new int[]{151, 151, 70}, new int[]{25, 25, 11}, new int[]{255, 255, 140}, 1.0F);
    public static final CMaterial RUBIDIUM = reg("rubidium", "Rubidium", "rubidium", "Rubidium", 37, new int[]{245, 245, 245}, new int[]{235, 0, 0}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial CAESIUM = reg("caesium", "Caesium", "caesium", "Caesium", 55, new int[]{245, 245, 245}, new int[]{150, 150, 0}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial FRANCIUM = reg("francium", "Francium", "francium", "Francium", 87, new int[]{245, 245, 245}, new int[]{0, 235, 0}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial RADIUM = reg("radium", "Radium", "radium", "Radium", 88, new int[]{245, 245, 245}, new int[]{0, 150, 150}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ACTINIUM = reg("actinium", "Actinium", "actinium", "Actinium", 89, new int[]{245, 245, 245}, new int[]{0, 0, 235}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial THORIUM = reg("thorium", "Thorium", "thorium", "Thorium", 90, new int[]{50, 50, 50}, new int[]{8, 8, 8}, new int[]{200, 50, 50}, 1.0F);
    public static final CMaterial PROTACTINIUM = reg("protactinium", "Protactinium", "protactinium", "Protactinium", 91, new int[]{50, 50, 50}, new int[]{8, 8, 8}, new int[]{50, 50, 100}, 1.0F);
    public static final CMaterial URANIUM = reg("uranium", "Uranium", "uranium", "Uranium", 92, new int[]{50, 255, 50}, new int[]{50, 155, 50}, new int[]{50, 255, 50}, 1.0F);
    public static final CMaterial NEPTUNIUM = reg("neptunium", "Neptunium", "neptunium", "Neptunium", 93, new int[]{50, 50, 255}, new int[]{50, 50, 155}, new int[]{50, 50, 255}, 1.0F);
    public static final CMaterial PLUTONIUM = reg("plutonium", "Plutonium", "plutonium", "Plutonium", 94, new int[]{255, 50, 50}, new int[]{155, 50, 50}, new int[]{255, 50, 50}, 1.0F);
    public static final CMaterial AMERICIUM = reg("americium", "Americium", "americium", "Americium", 95, new int[]{235, 235, 235}, new int[]{155, 155, 155}, new int[]{235, 235, 235}, 1.0F);
    public static final CMaterial CURIUM = reg("curium", "Curium", "curium", "Curium", 96, new int[]{255, 255, 255}, new int[]{155, 155, 155}, new int[]{244, 244, 244}, 1.0F);
    public static final CMaterial LANTHANUM = reg("lanthanum", "Lanthanum", "lanthanum", "Lanthanum", 57, new int[]{145, 145, 145}, new int[]{235, 0, 0}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial CERIUM = reg("cerium", "Cerium", "cerium", "Cerium", 58, new int[]{145, 145, 145}, new int[]{150, 150, 0}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial PRASEODYMIUM = reg("praseodymium", "Praseodymium", "praseodymium", "Praseodymium", 59, new int[]{145, 145, 145}, new int[]{0, 235, 0}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial NEODYMIUM = reg("neodymium", "Neodymium", "neodymium", "Neodymium", 60, new int[]{145, 145, 145}, new int[]{0, 150, 150}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial PROMETHIUM = reg("promethium", "Promethium", "promethium", "Promethium", 61, new int[]{145, 145, 145}, new int[]{0, 0, 235}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial SAMARIUM = reg("samarium", "Samarium", "samarium", "Samarium", 62, new int[]{145, 145, 145}, new int[]{150, 0, 150}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial EUROPIUM = reg("europium", "Europium", "europium", "Europium", 63, new int[]{145, 145, 145}, new int[]{55, 55, 55}, new int[]{145, 145, 145}, 1.0F);
    public static final CMaterial VANADIUM = reg("vanadium", "Vanadium", "vanadium", "Vanadium", 23, new int[]{60, 120, 120}, new int[]{10, 20, 20}, new int[]{120, 240, 240}, 1.0F);
    public static final CMaterial COBALT = reg("cobalt", "Cobalt", "cobalt", "Cobalt", 27, new int[]{30, 30, 230}, new int[]{5, 5, 38}, new int[]{60, 60, 255}, 1.0F);
    public static final CMaterial PALLADIUM = reg("palladium", "Palladium", "palladium", "Palladium", 46, new int[]{151, 70, 70}, new int[]{25, 11, 11}, new int[]{255, 140, 140}, 1.0F);
    public static final CMaterial SILVER = reg("silver", "Silver", "silver", "Silver", 47, new int[]{230, 230, 245}, new int[]{120, 120, 140}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial PLATINUM = reg("platinum", "Platinum", "platinum", "Platinum", 78, new int[]{245, 245, 230}, new int[]{140, 140, 120}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial IRIDIUM = reg("iridium", "Iridium", "iridium", "Iridium", 77, new int[]{240, 240, 240}, new int[]{210, 210, 210}, new int[]{235, 235, 235}, 1.0F);
    public static final CMaterial OSMIUM = reg("osmium", "Osmium", "osmium", "Osmium", 76, new int[]{70, 70, 150}, new int[]{11, 11, 25}, new int[]{140, 140, 255}, 1.0F);
    public static final CMaterial RHENIUM = reg("rhenium", "Rhenium", "rhenium", "Rhenium", 75, new int[]{70, 70, 150}, new int[]{11, 11, 25}, new int[]{50, 50, 90}, 1.0F);
    public static final CMaterial TANTALUM = reg("tantalum", "Tantalum", "tantalum", "Tantalum", 73, new int[]{240, 210, 170}, new int[]{40, 35, 28}, new int[]{240, 210, 150}, 1.0F);
    public static final CMaterial TUNGSTEN = reg("tungsten", "Tungsten", "tungsten", "Tungsten", 74, new int[]{30, 30, 30}, new int[]{5, 5, 5}, new int[]{60, 60, 60}, 1.0F);
    public static final CMaterial MOLYBDENUM = reg("molybdenum", "Molybdenum", "molybdenum", "Molybdenum", 42, new int[]{130, 160, 130}, new int[]{21, 26, 21}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial TIN = reg("tin", "Tin", "tin", "Tin", 50, new int[]{230, 230, 240}, new int[]{0, 0, 0}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ANTIMONY = reg("antimony", "Antimony", "antimony", "Antimony", 51, new int[]{70, 70, 70}, new int[]{11, 11, 11}, new int[]{140, 140, 140}, 1.0F);
    public static final CMaterial BISMUTH = reg("bismuth", "Bismuth", "bismuth", "Bismuth", 83, new int[]{70, 120, 70}, new int[]{11, 20, 11}, new int[]{140, 240, 140}, 1.0F);
    public static final CMaterial BRONZE = reg("bronze", "Bronze", "bronze", "Bronze", 1280, new int[]{250, 150, 40}, new int[]{0, 0, 0}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial BRASS = reg("brass", "Brass", "brass", "Brass", 1281, new int[]{190, 170, 20}, new int[]{0, 0, 0}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ELECTRUM = reg("electrum", "Electrum", "electrum", "Electrum", 1283, new int[]{230, 230, 155}, new int[]{120, 120, 70}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial INVAR = reg("invar", "Invar", "invar", "Invar", 1284, new int[]{170, 170, 80}, new int[]{140, 140, 70}, new int[]{180, 180, 80}, 1.0F);
    public static final CMaterial STEEL = reg("steel", "Steel", "steel", "Steel", 1536, new int[]{90, 90, 110}, new int[]{0, 0, 0}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial OBSIDIAN = reg("obsidian", "Obsidian", "obsidian", "Obsidian", 896, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial REDSTONE = reg("redstone", "Redstone", "redstone", "Redstone", 899, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial GLOWSTONE = reg("glowstone", "Glowstone", "glowstone", "Glowstone", 900, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ENDER_PEARL = reg("ender_pearl", "EnderPearl", "enderpearl", "EnderPearl", 901, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial COAL = reg("coal", "Coal", "coal", "Coal", 1792, new int[]{20, 20, 20}, new int[]{3, 3, 3}, new int[]{50, 50, 80}, 1.0F);
    public static final CMaterial CHARCOAL = reg("charcoal", "Charcoal", "charcoal", "Charcoal", 1793, new int[]{20, 20, 20}, new int[]{3, 3, 3}, new int[]{80, 50, 50}, 1.0F);
    public static final CMaterial LAPIS = reg("lapis", "Lapis", "lapis", "Lapis", 1824, new int[]{60, 100, 190}, new int[]{10, 43, 122}, new int[]{90, 130, 226}, 1.0F);
    public static final CMaterial DIAMOND = reg("diamond", "Diamond", "diamond", "Diamond", 1856, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial EMERALD = reg("emerald", "Emerald", "emerald", "Emerald", 1857, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial STONE = reg("stone", "Stone", "stone", "Stone", 2048, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial CARBON = reg("carbon", "Carbon", "carbon", "Carbon", 6, new int[]{10, 10, 10}, new int[]{1, 1, 1}, new int[]{30, 30, 30}, 1.0F);
    public static final CMaterial PHOSPHORUS = reg("phosphorus", "Phosphorus", "phosphorus", "Phosphorus", 15, new int[]{180, 180, 25}, new int[]{155, 155, 0}, new int[]{205, 205, 50}, 1.0F);
    public static final CMaterial SULFUR = reg("sulfur", "Sulfur", "sulfur", "Sulfur", 16, new int[]{230, 230, 0}, new int[]{205, 205, 0}, new int[]{255, 255, 0}, 1.0F);
    public static final CMaterial PLASTIC = reg("plastic", "Plastic", "plastic", "Plastic", 1032, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial CINNABAR = reg("cinnabar", "Cinnabar", "cinnabar", "Cinnabar", 1040, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial SALTPETER = reg("saltpeter", "Saltpeter", "saltpeter", "Saltpeter", 1041, new int[]{222, 220, 220}, new int[]{190, 200, 210}, new int[]{255, 240, 230}, 1.0F);
    public static final CMaterial RUBY = reg("ruby", "Ruby", "ruby", "Ruby", 1858, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial SAPPHIRE = reg("sapphire", "Sapphire", "sapphire", "Sapphire", 1859, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial PERIDOT = reg("peridot", "Peridot", "peridot", "Peridot", 1860, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial AMBER = reg("amber", "Amber", "amber", "Amber", 1861, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial AMETHYST = reg("amethyst", "Amethyst", "amethyst", "Amethyst", 1862, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial REDSTONE_ALLOY = reg("redstone_alloy", "RedstoneAlloy", "redstonealloy", "RedstoneAlloy", 1408, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial CONDUCTIVE_IRON = reg("conductive_iron", "ConductiveIron", "conductiveiron", "ConductiveIron", 1409, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ENERGETIC_ALLOY = reg("energetic_alloy", "EnergeticAlloy", "energeticalloy", "EnergeticAlloy", 1410, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ELECTRICAL_STEEL = reg("electrical_steel", "ElectricalSteel", "electricalsteel", "ElectricalSteel", 1411, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial DARK_STEEL = reg("dark_steel", "DarkSteel", "darksteel", "DarkSteel", 1412, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial PHASED_IRON = reg("phased_iron", "PhasedIron", "phasediron", "PhasedIron", 1413, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial PHASED_GOLD = reg("phased_gold", "PhasedGold", "phasedgold", "PhasedGold", 1414, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial SOULARIUM = reg("soularium", "Soularium", "soularium", "Soularium", 1415, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial SIGNALUM = reg("signalum", "Signalum", "signalum", "Signalum", 1424, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial LUMIUM = reg("lumium", "Lumium", "lumium", "Lumium", 1425, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ENDERIUM = reg("enderium", "Enderium", "enderium", "Enderium", 1426, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ELECTRUM_FLUX = reg("electrum_flux", "ElectrumFlux", "electrumflux", "ElectrumFlux", 1428, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial CRYSTAL_FLUX = reg("crystal_flux", "CrystalFlux", "crystalflux", "CrystalFlux", 2016, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial APATITE = reg("apatite", "Apatite", "apatite", "Apatite", 1044, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial CERTUS_QUARTZ = reg("certus_quartz", "CertusQuartz", "certusquartz", "CertusQuartz", 1048, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial FLUIX = reg("fluix", "Fluix", "fluix", "Fluix", 1049, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ARDITE = reg("ardite", "Ardite", "ardite", "Ardite", 320, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ALUMINUM_BRASS = reg("aluminum_brass", "AluminumBrass", "aluminumbrass", "AluminumBrass", 1432, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial PIG_IRON = reg("pig_iron", "PigIron", "pigiron", "PigIron", 1433, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ALUMITE = reg("alumite", "Alumite", "alumite", "Alumite", 1434, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial MANYULLYN = reg("manyullyn", "Manyullyn", "manyullyn", "Manyullyn", 1435, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial FAIRY = reg("fairy", "Fairy", "fairy", "Fairy", 1440, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial POKEFENNIUM = reg("pokefennium", "Pokefennium", "pokefennium", "Pokefennium", 1441, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial RED_AURUM = reg("red_aurum", "Red_aurum", "red_aurum", "Red_aurum", 1442, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial DRULLOY = reg("drulloy", "Drulloy", "drulloy", "Drulloy", 1443, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial RED_ALLOY = reg("red_alloy", "RedAlloy", "redalloy", "RedAlloy", 1448, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ELECTROTINE = reg("electrotine", "Electrotine", "electrotine", "Electrotine", 1060, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ELECTROTINE_ALLOY = reg("electrotine_alloy", "ElectrotineAlloy", "electrotinealloy", "ElectrotineAlloy", 1449, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial REFINED_GLOWSTONE = reg("refined_glowstone", "RefinedGlowstone", "refinedglowstone", "RefinedGlowstone", 1600, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial REFINED_OBSIDIAN = reg("refined_obsidian", "RefinedObsidian", "refinedobsidian", "RefinedObsidian", 1601, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial UNSTABLE = reg("unstable", "Unstable", "unstable", "Unstable", 1728, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial HSLA = reg("hsla", "HSLA", "hsla", "HSLA", 1608, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial YELLORIUM = reg("yellorium", "Yellorium", "yellorium", "Yellorium", 324, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial CYANITE = reg("cyanite", "Cyanite", "cyanite", "Cyanite", 325, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial BLUTONIUM = reg("blutonium", "Blutonium", "blutonium", "Blutonium", 326, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial LUDICRITE = reg("ludicrite", "Ludicrite", "ludicrite", "Ludicrite", 327, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial GRAPHITE = reg("graphite", "Graphite", "graphite", "Graphite", 1056, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial METEORIC_IRON = reg("meteoric_iron", "MeteoricIron", "meteoriciron", "MeteoricIron", 336, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial DESH = reg("desh", "Desh", "desh", "Desh", 337, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial IRON_COMPRESSED = reg("iron_compressed", "IronCompressed", "ironcompressed", "IronCompressed", 1604, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial FZ_DARK_IRON = reg("fz_dark_iron", "FzDarkIron", "fzdarkiron", "FzDarkIron", 332, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial THAUMIUM = reg("thaumium", "Thaumium", "thaumium", "Thaumium", 1664, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial VOID = reg("void", "Void", "void", "Void", 1665, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial MANASTEEL = reg("manasteel", "Manasteel", "manasteel", "Manasteel", 1668, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial TERRASTEEL = reg("terrasteel", "Terrasteel", "terrasteel", "Terrasteel", 1669, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ELVEN_ELEMENTIUM = reg("elven_elementium", "ElvenElementium", "elvenelementium", "ElvenElementium", 1670, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial TOPAZ = reg("manasteel", "Manasteel", "manasteel", "Manasteel", 1863, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial MALACHITE = reg("malachite", "Malachite", "malachite", "Malachite", 1864, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial TANZANITE = reg("tanzanite", "Tanzanite", "tanzanite", "Tanzanite", 1865, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial HEE_ENDIUM = reg("hee_endium", "HeeEndium", "heeendium", "HeeEndium", 1696, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial DILITHIUM = reg("dilithium", "Dilithium", "dilithium", "Dilithium", 1880, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial FORCICIUM = reg("forcicium", "Forcicium", "forcicium", "Forcicium", 1884, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial GALLIUM = reg("gallium", "Gallium", "gallium", "Gallium", 31, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial YTTRIUM = reg("yttrium", "Yttrium", "yttrium", "Yttrium", 39, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial NIOBIUM = reg("niobium", "Niobium", "niobium", "Niobium", 41, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial URANIUM_235 = reg("uranium235", "Uranium235", "uranium235", "Uranium235", 288, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial PLUTONIUM_241 = reg("plutonium241", "Plutonium241", "plutonium241", "Plutonium241", 289, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial NAQUADAH = reg("naquadah", "Naquadah", "naquadah", "Naquadah", 296, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial NAQUADAH_ENRICHED = reg("naquadah_enriched", "NaquadahEnriched", "naquadahenriched", "NaquadahEnriched", 297, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial NAQUADRIA = reg("naquadria", "Naquadria", "naquadria", "Naquadria", 298, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial NEUTRONIUM = reg("neutronium", "Neutronium", "neutronium", "Neutronium", 299, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial NIKOLITE = reg("nikolite", "Nikolite", "nikolite", "Nikolite", 1064, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial QUARTZITE = reg("quartzite", "Quartzite", "quartzite", "Quartzite", 1088, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial MONAZITE = reg("monazite", "Monazite", "monazite", "Monazite", 1089, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial NITER = reg("niter", "Niter", "niter", "Niter", 1090, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial TUNGSTEN_STEEL = reg("tungsten_steel", "TungstenSteel", "tungstensteel", "TungstenSteel", 1472, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial CUPRONICKEL = reg("cupronickel", "Cupronickel", "cupronickel", "Cupronickel", 1473, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial NICHROME = reg("nichrome", "Nichrome", "nichrome", "Nichrome", 1474, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial KANTHAL = reg("kanthal", "Kanthal", "kanthal", "Kanthal", 1475, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial STAINLESS_STEEL = reg("stainless_steel", "StainlessSteel", "stainlesssteel", "StainlessSteel", 1476, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial COBALT_BRASS = reg("cobalt_brass", "CobaltBrass", "cobaltbrass", "CobaltBrass", 1477, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial MAGNALIUM = reg("magnalium", "Magnalium", "magnalium", "Magnalium", 1478, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial SOLDERING_ALLOY = reg("soldering_alloy", "SolderingAlloy", "solderingalloy", "SolderingAlloy", 1479, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial BATTERY_ALLOY = reg("battery_alloy", "BatteryAlloy", "batteryalloy", "BatteryAlloy", 1480, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial VANADIUM_GALLIUM = reg("vanadium_gallium", "VanadiumGallium", "vanadiumgallium", "VanadiumGallium", 1481, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial YTTRIUM_BARIUM_CUPRATE = reg("yttrium_barium_cuprate", "YttriumBariumCuprate", "yttriumbariumcuprate", "YttriumBariumCuprate", 1482, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial NIOBIUM_TITANIUM = reg("niobium_titanium", "NiobiumTitanium", "niobiumtitanium", "NiobiumTitanium", 1483, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ULTIMET = reg("ultimet", "Ultimet", "ultimet", "Ultimet", 1484, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial TIN_ALLOY = reg("tin_alloy", "TinAlloy", "tinalloy", "TinAlloy", 1485, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial BLUE_ALLOY = reg("blue_alloy", "BlueAlloy", "bluealloy", "BlueAlloy", 1486, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial WROUGHT_IRON = reg("wrought_iron", "WroughtIron", "wroughtiron", "WroughtIron", 1552, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ANNEALED_COPPER = reg("annealed_copper", "AnnealedCopper", "annealedcopper", "AnnealedCopper", 1553, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial IRON_MAGNETIC = reg("iron_magnetic", "IronMagnetic", "ironmagnetic", "IronMagnetic", 1568, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial STEEL_MAGNETIC = reg("steel_magnetic", "SteelMagnetic", "steelmagnetic", "SteelMagnetic", 1569, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial NEODYMIUM_MAGNETIC = reg("neodymium_magnetic", "NeodymiumMagnetic", "neodymiummagnetic", "NeodymiumMagnetic", 1570, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial LIGNITE = reg("lignite", "Lignite", "lignite", "Lignite", 1808, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial LAZURITE = reg("lazurite", "Lazurite", "lazurite", "Lazurite", 1840, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial SODALITE = reg("sodalite", "Sodalite", "sodalite", "Sodalite", 1841, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial GREEN_SAPPHIRE = reg("green_saphire", "GreenSaphire", "greensaphire", "GreenSaphire", 1888, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial GARNET_RED = reg("garnet_red", "GarnetRed", "garnetred", "GarnetRed", 1892, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial GARNET_YELLOW = reg("garnet_yellow", "GarnetYellow", "garnetyellow", "GarnetYellow", 1893, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial OPAL = reg("opal", "Opal", "opal", "Opal", 1896, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial JASPER = reg("jasper", "Jasper", "jasper", "Jasper", 1897, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial BLUE_TOPAZ = reg("blue_topaz", "BlueTopaz", "bluetopaz", "BlueTopaz", 1920, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial FORCE = reg("force", "Force", "force", "Force", 1984, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial FORCILLIUM = reg("forcillium", "Forcillium", "forcillium", "Forcillium", 1988, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial GLASS = reg("glass", "Glass", "glass", "Glass", 2176, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial PROMETHEUM = reg("prometheum", "Prometheum", "prometheum", "Prometheum", 352, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial DEEP_IRON = reg("deep_iron", "DeepIron", "deepiron", "DeepIron", 353, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial INFUSCOLIUM = reg("infuscolium", "Infuscolium", "infuscolium", "Infuscolium", 354, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial OURECLASE = reg("oureclase", "Oureclase", "oureclase", "Oureclase", 355, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial AREDRITE = reg("aredrite", "Aredrite", "aredrite", "Aredrite", 356, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ASTRAL_SILVER = reg("astral_silver", "AstralSilver", "astralsilver", "AstralSilver", 357, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial CARMOT = reg("carmot", "Carmot", "carmot", "Carmot", 358, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial MITHRIL = reg("mithril", "Mithril", "mithril", "Mithril", 359, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial RUBRACIUM = reg("rubracium", "Rubracium", "rubracium", "Rubracium", 360, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ORICHALCUM = reg("orichalcum", "Orichalcum", "orichalcum", "Orichalcum", 361, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ADAMANTINE = reg("adamantine", "Adamantine", "adamantine", "Adamantine", 362, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ATLARUS = reg("atlarus", "Atlarus", "atlarus", "Atlarus", 363, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial IGNATIUS = reg("ignatius", "Ignatius", "ignatius", "Ignatius", 368, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial SHADOW_IRON = reg("shadow_iron", "ShadowIron", "shadowiron", "ShadowIron", 369, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial LEMURITE = reg("lemurite", "Lemurite", "lemurite", "Lemurite", 370, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial MIDASIUM = reg("midasium", "Midasium", "midasium", "Midasium", 371, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial VYROXERES = reg("vyroxeres", "Vyroxeres", "vyroxeres", "Vyroxeres", 372, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial CERUCLASE = reg("ceruclase", "Ceruclase", "ceruclase", "Ceruclase", 373, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ALDUORITE = reg("alduorite", "Alduorite", "alduorite", "Alduorite", 374, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial KALENDRITE = reg("kalendrite", "Kalendrite", "kalendrite", "Kalendrite", 375, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial VULCANITE = reg("vulcanite", "Vulcanite", "vulcanite", "Vulcanite", 376, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial SANGUINITE = reg("sanguinite", "Sanguinite", "sanguinite", "Sanguinite", 377, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial EXIMITE = reg("eximite", "Eximite", "eximite", "Eximite", 380, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial MEUTOITE = reg("meutoite", "Meutoite", "meutoite", "Meutoite", 381, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial HEPATIZON = reg("hepatizon", "Hepatizon", "hepatizon", "Hepatizon", 1504, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial DAMASCUS_STEEL = reg("damascus_steel", "DamascusSteel", "damascussteel", "DamascusSteel", 1505, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ANGMALLEN = reg("angmallen", "Angmallen", "angmallen", "Angmallen", 1506, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial BLACK_STEEL = reg("black_steel", "BlackSteel", "blacksteel", "BlackSteel", 1508, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial QUICKSILVER = reg("quick_silver", "QuickSilver", "quicksilver", "QuickSilver", 1509, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial HADEROTH = reg("haderoth", "Haderoth", "haderoth", "Haderoth", 1510, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial CELENEGIL = reg("celenegil", "Celenegil", "celenegil", "Celenegil", 1511, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial TARTARITE = reg("tartarite", "Tartarite", "tartarite", "Tartarite", 1512, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial SHADOW_STEEL = reg("shadow_steel", "ShadowSteel", "shadowsteel", "ShadowSteel", 1516, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial INOLASHITE = reg("inolashite", "Inolashite", "inolashite", "Inolashite", 1517, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial AMORDRINE = reg("amordrine", "Amordrine", "amordrine", "Amordrine", 1518, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial DESICHALKOS = reg("desichalkos", "Desichalkos", "desichalkos", "Desichalkos", 1519, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial NINJA = reg("ninja", "Ninja", "ninja", "Ninja", 1468, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial YELLOWSTONE = reg("yellowstone", "Yellowstone", "yellowstone", "Yellowstone", 1068, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial BLUESTONE = reg("bluestone", "Bluestone", "bluestone", "Bluestone", 1069, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial ALUMINIUM_OD = reg("aluminium", "Aluminium", "aluminium", "Aluminium", 4096, new int[]{140, 140, 140}, new int[]{25, 25, 25}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial COMPRESSED_PURE_ANTIMATTER_1 = reg("pure_antimatter1", "PureAntimatter1", "pureantimatter1", "PureAntimatter1", 802, new int[]{196, 35, 181}, new int[]{25, 25, 0}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial COMPRESSED_PURE_ANTIMATTER_2 = reg("pure_antimatter2", "PureAntimatter2", "pureantimatter2", "PureAntimatter2", 803, new int[]{142, 23, 119}, new int[]{50, 50, 0}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial COMPRESSED_PURE_ANTIMATTER_3 = reg("pure_antimatter3", "PureAntimatter3", "pureantimatter3", "PureAntimatter3", 804, new int[]{94, 13, 69}, new int[]{75, 75, 0}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial COMPRESSED_PURE_ANTIMATTER_4 = reg("pure_antimatter4", "PureAntimatter4", "pureantimatter4", "PureAntimatter4", 805, new int[]{50, 6, 31}, new int[]{100, 100, 0}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial COMPRESSED_PURE_ANTIMATTER_5 = reg("pure_antimatter5", "PureAntimatter5", "pureantimatter5", "PureAntimatter5", 806, new int[]{82, 8, 41}, new int[]{125, 125, 0}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial COMPRESSED_PURE_ANTIMATTER_6 = reg("pure_antimatter6", "PureAntimatter6", "pureantimatter6", "PureAntimatter6", 807, new int[]{110, 7, 39}, new int[]{150, 150, 0}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial COMPRESSED_PURE_ANTIMATTER_7 = reg("pure_antimatter7", "PureAntimatter7", "pureantimatter7", "PureAntimatter7", 808, new int[]{132, 5, 25}, new int[]{175, 175, 0}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial COMPRESSED_PURE_ANTIMATTER_8 = reg("octuple_pure_antimatter", "OctuplePureAntimatter", "octuplepureantimatter", "OctuplePureAntimatter", 809, new int[]{150, 0, 0}, new int[]{200, 200, 0}, new int[]{255, 255, 255}, 1.0F);
    public static final CMaterial OCTUPLE_PURE_ANTIMATTER = COMPRESSED_PURE_ANTIMATTER_8;

    private CMaterials() {}

    private static CMaterial reg(
            String idPrefix,
            String name,
            String iconName,
            String odName,
            int meta,
            int[] base,
            int[] dark,
            int[] light,
            float hardness) {
        CMaterial mat = new CMaterial(idPrefix, name, iconName, odName, meta, base, dark, light, hardness);
        BY_NAME.putIfAbsent(name, mat);
        return mat;
    }

    public static Map<String, CMaterial> byName() {
        return Collections.unmodifiableMap(BY_NAME);
    }

    public static void bind(CMaterial material, CShape shape, Supplier<? extends Item> itemSupplier) {
        ITEM_MAP.computeIfAbsent(material, k -> new LinkedHashMap<>()).put(shape, itemSupplier::get);
    }

    public static void bindDefaults() {
        bind(IRON, CShape.INGOT, () -> Items.IRON_INGOT);
        bind(GOLD, CShape.INGOT, () -> Items.GOLD_INGOT);
        bind(CLAY, CShape.BLOCK, () -> Blocks.CLAY.asItem());
        bind(CLAY, CShape.BALL, () -> Items.CLAY_BALL);
        bind(DENSE_CLAY, CShape.BALL, () -> Items.CLAY_BALL);
    }

    public static Item getItem(CMaterial material, CShape shape) {
        Map<CShape, Supplier<Item>> byShape = ITEM_MAP.get(material);
        if (byShape == null) {
            return Items.AIR;
        }
        Supplier<Item> supplier = byShape.get(shape);
        return supplier != null ? supplier.get() : Items.AIR;
    }
}
