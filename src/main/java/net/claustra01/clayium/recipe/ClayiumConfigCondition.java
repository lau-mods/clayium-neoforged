package net.claustra01.clayium.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.claustra01.clayium.ClayiumConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * Config-gated NeoForge recipe condition mirroring 1.7.10 CRecipes boolean toggles
 * with safe default evaluation when config files have not yet loaded (e.g. during DataGen).
 */
public record ClayiumConfigCondition(String key, boolean expected) implements ICondition {
    public static final String ENABLE_FLUID_CAPSULE = "enable_fluid_capsule";
    public static final String ENABLE_RF_GENERATOR = "enable_rf_generator";
    public static final String HARDCORE_ALUMINIUM = "hardcore_aluminium";
    public static final String HARDCORE_OSMIUM = "hardcore_osmium";
    public static final String ENABLE_INJECTOR_RECIPE_OF_INTERFACE = "enable_injector_recipe_of_interface";

    public static final MapCodec<ClayiumConfigCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("key").forGetter(ClayiumConfigCondition::key),
            Codec.BOOL.optionalFieldOf("expected", true).forGetter(ClayiumConfigCondition::expected)
    ).apply(instance, ClayiumConfigCondition::new));

    public ClayiumConfigCondition(String key) {
        this(key, true);
    }

    @Override
    public boolean test(IContext context) {
        boolean actual = switch (this.key) {
            case ENABLE_FLUID_CAPSULE -> readBoolean(ClayiumConfig.ENABLE_FLUID_CAPSULE);
            case ENABLE_RF_GENERATOR -> readBoolean(ClayiumConfig.ENABLE_RF_GENERATOR);
            case HARDCORE_ALUMINIUM -> readBoolean(ClayiumConfig.HARDCORE_ALUMINIUM);
            case HARDCORE_OSMIUM -> readBoolean(ClayiumConfig.HARDCORE_OSMIUM);
            case ENABLE_INJECTOR_RECIPE_OF_INTERFACE -> readBoolean(ClayiumConfig.ENABLE_INJECTOR_RECIPE_OF_INTERFACE);
            default -> false;
        };
        return actual == this.expected;
    }

    private static boolean readBoolean(ModConfigSpec.BooleanValue value) {
        return ClayiumConfig.SPEC.isLoaded() ? value.get() : Boolean.TRUE.equals(value.getDefault());
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
