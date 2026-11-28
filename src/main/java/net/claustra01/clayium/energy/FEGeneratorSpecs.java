package net.claustra01.clayium.energy;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.claustra01.clayium.ClayiumConfig;
import net.claustra01.clayium.block.FEGeneratorBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specification definitions and config parser for Clayium CE-FE Converters,
 * preserving 1.7.10 {@code ClayRFGenerator} parameters and unit scaling (1 CE = 100,000 internal units).
 */
public final class FEGeneratorSpecs {
    private static final Logger LOGGER = LoggerFactory.getLogger(FEGeneratorSpecs.class);
    public static final long CE_UNITS_PER_CE = 100_000L;

    public record Spec(
            String configBlockName,
            String registryPath,
            int tier,
            long ceConsumptionPerTick,
            int feProductionPerTick,
            int feOutputPerTick,
            int feStorageSize,
            double overclockExponent) {}

    private static final List<String> DEFAULT_DEFINITIONS = List.of(
            "BasicRFGenerator;rfgenerator:4:0.001:10:10:10000:1",
            "AdvancedRFGenerator;rfgenerator:5:0.01:30:30:30000:1",
            "PrecisionRFGenerator;rfgenerator:6:0.1:90:90:90000:1",
            "ClaySteelRFGenerator;rfgenerator:7:1:270:270:270000:1",
            "ClayiumRFGenerator;rfgenerator:8:10:810:810:810000:1",
            "UltimateRFGenerator;rfgenerator:9:100:2430:2430:2430000:1",
            "AntimatterRFGenerator;rfgenerator:10:1000:7290:7290:7290000:1",
            "PureAntimatterRFGenerator;rfgenerator:11:10000:21870:21870:21870000:1",
            "OECRFGenerator;rfgenerator:12:100000:65610:65610:65610000:1",
            "OPARFGenerator;rfgenerator:13:1000000:196830:196830:196830000:1"
    );

    private static final Map<String, String> REGISTRY_TO_CONFIG_NAME;
    private static final Map<String, String> CONFIG_TO_REGISTRY_PATH;

    static {
        Map<String, String> r2c = new LinkedHashMap<>();
        r2c.put("basic_rf_generator", "BasicRFGenerator");
        r2c.put("advanced_rf_generator", "AdvancedRFGenerator");
        r2c.put("precision_rf_generator", "PrecisionRFGenerator");
        r2c.put("clay_steel_rf_generator", "ClaySteelRFGenerator");
        r2c.put("clayium_rf_generator", "ClayiumRFGenerator");
        r2c.put("ultimate_rf_generator", "UltimateRFGenerator");
        r2c.put("antimatter_rf_generator", "AntimatterRFGenerator");
        r2c.put("pure_antimatter_rf_generator", "PureAntimatterRFGenerator");
        r2c.put("oec_rf_generator", "OECRFGenerator");
        r2c.put("opa_rf_generator", "OPARFGenerator");
        REGISTRY_TO_CONFIG_NAME = Collections.unmodifiableMap(r2c);

        Map<String, String> c2r = new LinkedHashMap<>();
        for (var entry : r2c.entrySet()) {
            c2r.put(entry.getValue(), entry.getKey());
        }
        CONFIG_TO_REGISTRY_PATH = Collections.unmodifiableMap(c2r);
    }

    private FEGeneratorSpecs() {}

    public static Map<String, String> registryToConfigNames() {
        return REGISTRY_TO_CONFIG_NAME;
    }

    @Nullable
    public static String configNameForRegistryPath(String registryPath) {
        return REGISTRY_TO_CONFIG_NAME.get(registryPath);
    }

    @Nullable
    public static String registryPathForConfigName(String configName) {
        return CONFIG_TO_REGISTRY_PATH.get(configName);
    }

    public static List<? extends String> getConfiguredDefinitions() {
        try {
            List<? extends String> configured = ClayiumConfig.RF_GENERATOR_DEFINITIONS.get();
            if (configured != null && !configured.isEmpty()) {
                return configured;
            }
        } catch (IllegalStateException ignored) {
            // Config spec not loaded yet (e.g. unit tests or early bootstrap)
        }
        return DEFAULT_DEFINITIONS;
    }

    @Nullable
    public static Spec parseDefinition(@Nullable String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        int semicolon = line.indexOf(';');
        if (semicolon <= 0 || semicolon >= line.length() - 1) {
            return null;
        }
        String configName = line.substring(0, semicolon).trim();
        String payload = line.substring(semicolon + 1).trim();
        String[] parts = payload.split(":", -1);
        if (parts.length < 7) {
            return null;
        }

        int tier;
        long cePerTick;
        int feProd;
        int feOutput;
        int feStorage;
        double ocExp;

        try {
            tier = Integer.parseInt(parts[1].trim());
            cePerTick = parseCeConsumptionUnits(parts[2].trim());
            feProd = Math.max(0, Integer.parseInt(parts[3].trim()));
            feOutput = Math.max(0, Integer.parseInt(parts[4].trim()));
            feStorage = Math.max(0, Integer.parseInt(parts[5].trim()));
            ocExp = Double.parseDouble(parts[6].trim());
        } catch (NumberFormatException ex) {
            LOGGER.warn("Failed to parse FE generator definition '{}'", line, ex);
            return null;
        }

        if (!Double.isFinite(ocExp)) {
            ocExp = 1.0;
        }

        String registryPath = CONFIG_TO_REGISTRY_PATH.getOrDefault(configName, "");
        return new Spec(configName, registryPath, tier, cePerTick, feProd, feOutput, feStorage, ocExp);
    }

    public static long parseCeConsumptionUnits(String raw) {
        try {
            long ce = Long.parseLong(raw);
            if (ce <= 0L) {
                return 0L;
            }
            try {
                return Math.multiplyExact(ce, CE_UNITS_PER_CE);
            } catch (ArithmeticException overflow) {
                return Long.MAX_VALUE;
            }
        } catch (NumberFormatException ignored) {
            double ce = Double.parseDouble(raw);
            if (Double.isNaN(ce) || ce <= 0.0) {
                return 0L;
            }
            if (Double.isInfinite(ce)) {
                return Long.MAX_VALUE;
            }
            double units = ce * (double) CE_UNITS_PER_CE;
            if (units >= (double) Long.MAX_VALUE) {
                return Long.MAX_VALUE;
            }
            return Math.max(0L, Math.round(units));
        }
    }

    @Nullable
    public static Spec byConfigName(@Nullable String configName) {
        if (configName == null || configName.isEmpty()) {
            return null;
        }
        for (String line : getConfiguredDefinitions()) {
            Spec spec = parseDefinition(line);
            if (spec != null && configName.equals(spec.configBlockName())) {
                return spec;
            }
        }
        for (String line : DEFAULT_DEFINITIONS) {
            Spec spec = parseDefinition(line);
            if (spec != null && configName.equals(spec.configBlockName())) {
                return spec;
            }
        }
        return null;
    }

    @Nullable
    public static Spec byRegistryPath(@Nullable String registryPath) {
        if (registryPath == null || registryPath.isEmpty()) {
            return null;
        }
        String configName = REGISTRY_TO_CONFIG_NAME.get(registryPath);
        return configName != null ? byConfigName(configName) : null;
    }

    @Nullable
    public static Spec forBlock(@Nullable Block block) {
        if (block == null) {
            return null;
        }
        if (block instanceof FEGeneratorBlock feGen) {
            Spec spec = byConfigName(feGen.getConfigBlockName());
            if (spec != null) {
                return spec;
            }
        }
        return byRegistryPath(BuiltInRegistries.BLOCK.getKey(block).getPath());
    }
}
