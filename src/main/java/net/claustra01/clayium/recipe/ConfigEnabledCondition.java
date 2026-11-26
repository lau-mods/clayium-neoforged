package net.claustra01.clayium.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.claustra01.clayium.ClayiumConfig;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.neoforged.neoforge.common.conditions.ICondition;

/** Datapack condition for original recipe switches loaded from the startup common configuration. */
public record ConfigEnabledCondition(Option option) implements ICondition {
    public static final MapCodec<ConfigEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(Codec.STRING.fieldOf("option")
                    .xmap(Option::byName, Option::serializedName)
                    .forGetter(ConfigEnabledCondition::option))
                    .apply(instance, ConfigEnabledCondition::new));

    @Override
    public boolean test(IContext context) {
        return option.enabled();
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return ClayiumRegistries.CONFIG_ENABLED_CONDITION.get();
    }

    public enum Option {
        HARDCORE_ALUMINIUM,
        HARDCORE_OSMIUM,
        INJECTOR_INTERFACE;

        public boolean enabled() {
            return switch (this) {
                case HARDCORE_ALUMINIUM -> ClayiumConfig.hardcoreAluminium();
                case HARDCORE_OSMIUM -> ClayiumConfig.hardcoreOsmium();
                case INJECTOR_INTERFACE -> ClayiumConfig.enableInjectorRecipeOfInterface();
            };
        }

        public String serializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static Option byName(String value) {
            return valueOf(value.toUpperCase(Locale.ROOT));
        }
    }
}
