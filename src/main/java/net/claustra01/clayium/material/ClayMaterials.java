package net.claustra01.clayium.material;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class ClayMaterials {
    public static final List<MaterialVariant> VARIANTS = readVariants();
    public static final Map<String, ClayMaterial> MATERIALS = VARIANTS.stream()
            .map(MaterialVariant::material)
            .distinct()
            .collect(Collectors.toUnmodifiableMap(ClayMaterial::id, material -> material));
    public static final Map<String, MaterialVariant> VARIANTS_BY_ID = VARIANTS.stream()
            .collect(Collectors.toUnmodifiableMap(MaterialVariant::id, variant -> variant));

    private static List<MaterialVariant> readVariants() {
        Map<String, ClayMaterial> materials = new LinkedHashMap<>();
        Map<String, OriginalProperties> properties = readProperties();
        try (var stream = ClayMaterials.class.getClassLoader()
                        .getResourceAsStream("data/clayium/original_varieties.tsv")) {
            if (stream == null) {
                throw new IllegalStateException("Original material ledger is missing");
            }
            try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                return reader.lines()
                        .map(line -> line.split("\t", -1))
                        .filter(columns -> columns.length == 3 && columns[0].equals("material"))
                        .map(columns -> {
                            String[] legacy = columns[1].split(":", -1);
                            if (legacy.length != 3 || !legacy[0].equals("CMaterials")) {
                                throw new IllegalStateException("Invalid material ledger entry: " + columns[1]);
                            }
                            String materialId = legacy[1].toLowerCase(Locale.ROOT);
                            OriginalProperties original = properties.get(legacy[1] + ":" + legacy[2]);
                            if (original == null) {
                                throw new IllegalStateException("Missing original properties: " + columns[1]);
                            }
                            ClayMaterial material = materials.computeIfAbsent(
                                    materialId, id -> new ClayMaterial(id, original.colors()));
                            return new MaterialVariant(
                                    material, MaterialShape.valueOf(legacy[2]), columns[2], original.tier());
                        })
                        .toList();
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read original material ledger", exception);
        }
    }

    private static Map<String, OriginalProperties> readProperties() {
        Map<String, OriginalProperties> properties = new LinkedHashMap<>();
        try (var stream = ClayMaterials.class.getClassLoader()
                .getResourceAsStream("data/clayium/original_material_properties.tsv")) {
            if (stream == null) {
                throw new IllegalStateException("Original material properties are missing");
            }
            try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                reader.lines().forEach(line -> {
                    String[] columns = line.split("\t", -1);
                    if (columns.length != 6) {
                        throw new IllegalStateException("Invalid material properties: " + line);
                    }
                    String key = columns[0] + ":" + columns[1];
                    var value = new OriginalProperties(Integer.parseInt(columns[2]), List.of(
                            0xFF000000 | Integer.parseInt(columns[3]),
                            0xFF000000 | Integer.parseInt(columns[4]),
                            0xFF000000 | Integer.parseInt(columns[5])));
                    if (properties.put(key, value) != null) {
                        throw new IllegalStateException("Duplicate material properties: " + key);
                    }
                });
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read original material properties", exception);
        }
        return properties;
    }

    private record OriginalProperties(int tier, List<Integer> colors) {
    }

    private ClayMaterials() {
    }
}
