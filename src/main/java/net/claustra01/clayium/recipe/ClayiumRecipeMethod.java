package net.claustra01.clayium.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.claustra01.clayium.ClayiumMod;
import net.minecraft.resources.ResourceLocation;

public enum ClayiumRecipeMethod {
    BENDING_MACHINE("bending_machine"),
    WIRE_DRAWING_MACHINE("wire_drawing_machine"),
    PIPE_DRAWING_MACHINE("pipe_drawing_machine"),
    CUTTING_MACHINE("cutting_machine"),
    LATHE("lathe"),
    MILLING_MACHINE("milling_machine"),
    CONDENSER("condenser"),
    GRINDER("grinder"),
    DECOMPOSER("decomposer"),
    EC_CONDENSER("ec_condenser"),
    ASSEMBLER("assembler"),
    INSCRIBER("inscriber"),
    CENTRIFUGE("centrifuge"),
    SMELTER("smelter"),
    CHEMICAL_REACTOR("chemical_reactor"),
    ALLOY_SMELTER("alloy_smelter"),
    BLAST_FURNACE("blast_furnace"),
    ELECTROLYSIS_REACTOR("electrolysis_reactor"),
    REACTOR("reactor"),
    MATTER_TRANSFORMER("matter_transformer"),
    CA_CONDENSER("ca_condenser"),
    CA_INJECTOR("ca_injector"),
    CA_REACTOR("ca_reactor"),
    EC_DECOMPOSER("ec_decomposer"),
    FLUID_TRANSFER_MACHINE("fluid_transfer_machine");

    private static final Map<String, ClayiumRecipeMethod> BY_ID = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(ClayiumRecipeMethod::id, Function.identity()));
    public static final Codec<ClayiumRecipeMethod> CODEC = ResourceLocation.CODEC.comapFlatMap(
            location -> {
                ClayiumRecipeMethod method = location.getNamespace().equals(ClayiumMod.MOD_ID)
                        ? BY_ID.get(location.getPath())
                        : null;
                return method == null
                        ? DataResult.error(() -> "Unknown Clayium processing method: " + location)
                        : DataResult.success(method);
            },
            ClayiumRecipeMethod::location);

    private final String id;

    ClayiumRecipeMethod(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public ResourceLocation location() {
        return ClayiumMod.id(id);
    }

    public static ClayiumRecipeMethod byId(ResourceLocation id) {
        ClayiumRecipeMethod method = id.getNamespace().equals(ClayiumMod.MOD_ID) ? BY_ID.get(id.getPath()) : null;
        if (method == null) {
            throw new IllegalArgumentException("Unknown Clayium processing method: " + id);
        }
        return method;
    }
}
