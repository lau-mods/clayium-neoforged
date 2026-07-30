/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Stable IDs for the original Clay Work Table's six manual operations. */
public enum ClayWorkTableOperation {
    FORM("form", 1),
    CUT("cut", 2),
    ROLL("roll", 3),
    SLICE("slice", 4),
    PUNCH("punch", 5),
    DIVIDE("divide", 6);

    public static final Codec<ClayWorkTableOperation> CODEC =
            Codec.STRING.comapFlatMap(ClayWorkTableOperation::decode, ClayWorkTableOperation::id);
    public static final StreamCodec<io.netty.buffer.ByteBuf, ClayWorkTableOperation> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(ClayWorkTableOperation::byIdOrForm, ClayWorkTableOperation::id);
    private static final Map<String, ClayWorkTableOperation> BY_ID = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(ClayWorkTableOperation::id, Function.identity()));

    private final String id;
    private final int buttonId;

    ClayWorkTableOperation(String id, int buttonId) {
        this.id = id;
        this.buttonId = buttonId;
    }

    public String id() {
        return id;
    }

    public int buttonId() {
        return buttonId;
    }

    public String translationKey() {
        return "clayium_neoforged.clay_work_table.operation." + id;
    }

    public static Optional<ClayWorkTableOperation> byButtonId(int buttonId) {
        return Arrays.stream(values()).filter(operation -> operation.buttonId == buttonId).findFirst();
    }

    public static Optional<ClayWorkTableOperation> byId(String id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    private static ClayWorkTableOperation byIdOrForm(String id) {
        return BY_ID.getOrDefault(id, FORM);
    }

    private static DataResult<ClayWorkTableOperation> decode(String id) {
        ClayWorkTableOperation operation = BY_ID.get(id);
        return operation == null
                ? DataResult.error(() -> "Unknown Clay Work Table operation: " + id)
                : DataResult.success(operation);
    }
}
