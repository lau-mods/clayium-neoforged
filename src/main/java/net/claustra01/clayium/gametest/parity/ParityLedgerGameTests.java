package net.claustra01.clayium.gametest.parity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.OriginalRegistryCatalog;
import net.claustra01.clayium.registry.StorageRegistration;
import net.claustra01.clayium.storage.MetalChestType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestRegistry;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ParityLedgerGameTests {
    private static final Map<String, Integer> EXPECTED_SCOPE_COUNTS = Map.ofEntries(
            Map.entry("reference", 1),
            Map.entry("registered_variety", 992),
            Map.entry("survival_recipe", 672),
            Map.entry("original_recipe_semantic", 4),
            Map.entry("gui", 20),
            Map.entry("gui_asset", 19),
            Map.entry("renderer", 25),
            Map.entry("original_png", 416),
            Map.entry("machine_family", 33),
            Map.entry("metal_chest", 57),
            Map.entry("configuration", 1),
            Map.entry("approved_exclusion", 1));
    private static final Set<String> ALLOWED_DECISIONS = Set.of(
            "同等", "明示的に変更済み", "欠落", "未確認");

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void parityLedgerCoversIndependentOriginalInventories(GameTestHelper helper) {
        List<LedgerRow> rows = readLedger();
        Map<String, Integer> actualCounts = new HashMap<>();
        Set<String> ids = new HashSet<>();
        Set<String> registeredTests = GameTestRegistry.getAllTestFunctions().stream()
                .map(function -> function.testName().toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        for (LedgerRow row : rows) {
            require(ids.add(row.unitId()), "Duplicate parity unit: " + row.unitId());
            actualCounts.merge(row.scope(), 1, Integer::sum);
            require(ALLOWED_DECISIONS.contains(row.decision()),
                    "Unknown parity decision for " + row.unitId() + ": " + row.decision());
            require(!row.originalEvidence().isBlank() && !row.originalEvidence().equals("-"),
                    "Missing original evidence for " + row.unitId());
            require(!row.portEvidence().isBlank() && !row.portEvidence().equals("-"),
                    "Missing port evidence for " + row.unitId());
            require(!row.testEvidence().isBlank() && !row.testEvidence().equals("-"),
                    "Missing test evidence for " + row.unitId());
            for (String evidence : row.testEvidence().split(";")) {
                if (evidence.matches("[A-Za-z][A-Za-z0-9]+")) {
                    String expected = evidence.toLowerCase(Locale.ROOT);
                    require(registeredTests.stream().anyMatch(name -> name.equals(expected)
                                    || name.endsWith("." + expected) || name.endsWith(":" + expected)),
                            "Unknown GameTest evidence for " + row.unitId() + ": " + evidence);
                }
            }
            if (row.scope().equals("registered_variety") && row.decision().equals("同等")) {
                String[] target = row.portEvidence().split(":", 2);
                require(target.length == 2, "Malformed registered-variety target: " + row.portEvidence());
                var id = target[1].contains(":")
                        ? net.minecraft.resources.ResourceLocation.parse(target[1])
                        : ClayiumMod.id(target[1]);
                if (target[0].equals("block")) {
                    require(BuiltInRegistries.BLOCK.containsKey(id) && BuiltInRegistries.ITEM.containsKey(id),
                            "Original block variety has no registered block/item target: " + row.unitId());
                } else {
                    require((target[0].equals("item") || target[0].equals("material"))
                                    && BuiltInRegistries.ITEM.containsKey(id),
                            "Original item variety has no registered item target: " + row.unitId());
                }
            }
        }
        require(actualCounts.equals(EXPECTED_SCOPE_COUNTS),
                "Parity scope inventory differs: " + actualCounts);
        require(rows.size() == EXPECTED_SCOPE_COUNTS.values().stream().mapToInt(Integer::intValue).sum(),
                "Parity ledger row count differs from the independent inventories");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void parityLedgerHasNoMissingOrUnverifiedEntries(GameTestHelper helper) {
        List<String> unresolved = readLedger().stream()
                .filter(row -> row.decision().equals("欠落") || row.decision().equals("未確認"))
                .map(row -> row.unitId() + "=" + row.decision())
                .toList();
        require(unresolved.isEmpty(), "Unresolved parity units remain: count=" + unresolved.size()
                + ", first=" + unresolved.stream().limit(8).toList());
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void allTierMachineAndChestLedgersCloseWithoutExternalDependencies(GameTestHelper helper) {
        require(OriginalRegistryCatalog.MACHINE_FAMILIES.size() == 33,
                "The machine-family inventory no longer contains 33 families");
        int variants = 0;
        Set<String> machineIds = new HashSet<>();
        for (OriginalRegistryCatalog.MachineFamily family : OriginalRegistryCatalog.MACHINE_FAMILIES) {
            for (int tier : family.tiers()) {
                variants++;
                String id = OriginalRegistryCatalog.machineId(family, tier);
                if (!OriginalRegistryCatalog.isRegisteredMachine(family, tier)) {
                    require(!BuiltInRegistries.BLOCK.containsKey(ClayiumMod.id(id)),
                            "An explicitly excluded machine is still registered: " + id);
                    continue;
                }
                require(machineIds.add(id), "Duplicate machine variant in the catalog: " + id);
                require(BuiltInRegistries.BLOCK.containsKey(ClayiumMod.id(id)),
                        "Catalog machine block is not registered: " + id);
                require(BuiltInRegistries.ITEM.containsKey(ClayiumMod.id(id)),
                        "Catalog machine item is not registered: " + id);
            }
        }
        require(variants == 168, "The machine-family inventory no longer contains 168 variants");
        require(MetalChestType.ALL.size() == 57 && StorageRegistration.METAL_CHESTS.size() == 57,
                "The metal-chest inventory no longer contains 57 variants");
        for (MetalChestType chest : MetalChestType.ALL) {
            require(StorageRegistration.METAL_CHESTS.containsKey(chest.id()),
                    "Metal chest is absent from registration: " + chest.id());
            require(BuiltInRegistries.BLOCK.containsKey(ClayiumMod.id(chest.id()))
                            && BuiltInRegistries.ITEM.containsKey(ClayiumMod.id(chest.id())),
                    "Metal chest lacks a block or item registration: " + chest.id());
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void removedLegacyFeaturesRemainAbsentAndCeToFeIsInitiallyReachable(GameTestHelper helper) {
        List<String> excludedIds = new ArrayList<>(OriginalRegistryCatalog.EXCLUDED_ENTRIES.stream()
                .map(OriginalRegistryCatalog.ExcludedEntry::legacyId).toList());
        excludedIds.addAll(List.of("utility_mode", "packet_sending_rate", "legacy_fluid_id"));
        for (String excluded : excludedIds) {
            require(!BuiltInRegistries.BLOCK.containsKey(ClayiumMod.id(excluded)),
                    "Excluded legacy block is registered: " + excluded);
            require(!BuiltInRegistries.ITEM.containsKey(ClayiumMod.id(excluded)),
                    "Excluded legacy item is registered: " + excluded);
        }
        var converter = helper.getLevel().getRecipeManager()
                .byKey(ClayiumMod.id("energy_converter/ce_rf_converter")).orElseThrow();
        require(converter.value().getResultItem(helper.getLevel().registryAccess())
                        .is(BuiltInRegistries.ITEM.get(ClayiumMod.id("ce_rf_converter"))),
                "The initial CE-to-FE converter recipe has the wrong output");
        helper.succeed();
    }

    private static List<LedgerRow> readLedger() {
        try (var stream = ParityLedgerGameTests.class.getResourceAsStream(
                        "/data/clayium_neoforged/parity_ledger.tsv")) {
            if (stream == null) throw new GameTestAssertException("The M44 parity ledger is absent");
            try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String header = reader.readLine();
                require(header != null && header.split("\\t", -1).length == 9,
                        "The parity ledger header is malformed");
                List<LedgerRow> rows = new ArrayList<>();
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    String[] columns = line.split("\\t", -1);
                    require(columns.length == 9, "Malformed parity row: " + line);
                    rows.add(new LedgerRow(
                            columns[0], columns[1], columns[2], columns[3], columns[5], columns[6]));
                }
                return rows;
            }
        } catch (IOException exception) {
            throw new GameTestAssertException("Could not read the M44 parity ledger: " + exception.getMessage());
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new GameTestAssertException(message);
    }

    private record LedgerRow(
            String unitId,
            String scope,
            String originalEvidence,
            String portEvidence,
            String testEvidence,
            String decision) {
    }

    private ParityLedgerGameTests() {
    }
}
