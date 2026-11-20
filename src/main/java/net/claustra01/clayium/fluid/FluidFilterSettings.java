package net.claustra01.clayium.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;

public record FluidFilterSettings(Optional<ResourceLocation> fluid, boolean blacklist) {
    public static final FluidFilterSettings ALLOW_ALL = new FluidFilterSettings(Optional.empty(), false);
    public static final Codec<FluidFilterSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("fluid").forGetter(FluidFilterSettings::fluid),
            Codec.BOOL.optionalFieldOf("blacklist", false).forGetter(FluidFilterSettings::blacklist))
            .apply(instance, FluidFilterSettings::new));

    public boolean accepts(FluidStack stack) {
        if (stack.isEmpty() || fluid.isEmpty()) {
            return true;
        }
        boolean matches = stack.getFluid().builtInRegistryHolder().key().location().equals(fluid.get());
        return blacklist != matches;
    }

    public FluidFilterSettings toggled() {
        return new FluidFilterSettings(fluid, !blacklist);
    }
}
