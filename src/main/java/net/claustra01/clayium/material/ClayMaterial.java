package net.claustra01.clayium.material;

import java.util.List;

public record ClayMaterial(String id, List<Integer> colors) {
    public ClayMaterial {
        colors = List.copyOf(colors);
        if (colors.size() != 3) {
            throw new IllegalArgumentException("A material needs three original render-layer colors");
        }
    }

    public int color(int tintIndex) {
        return tintIndex >= 0 && tintIndex < colors.size() ? colors.get(tintIndex) : 0xFFFFFFFF;
    }
}
