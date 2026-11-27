package net.claustra01.clayium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import net.claustra01.clayium.block.CompressedClayBlock;
import net.claustra01.clayium.block.SiliconeColoredBlock;
import net.claustra01.clayium.item.CraftingToolItem;
import net.claustra01.clayium.material.CMaterials;
import net.claustra01.clayium.material.CShape;
import net.claustra01.clayium.registry.ClayiumBlocks;
import net.claustra01.clayium.registry.ClayiumItems;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RegistryParityTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void registeredBlocksAndItemsMatchOriginalAudit1To1() throws IOException {
        Path projectDir = Path.of(System.getProperty("clayium.projectDir", "."));
        Path blocksJson = projectDir.resolve(".tmp/audit/original_blocks.json");
        Path itemsJson = projectDir.resolve(".tmp/audit/original_items.json");

        JsonArray blocksArr = JsonParser.parseString(Files.readString(blocksJson)).getAsJsonArray();
        Set<String> expectedBlocks = new LinkedHashSet<>();
        for (JsonElement el : blocksArr) {
            expectedBlocks.add(el.getAsJsonObject().get("new_id").getAsString());
        }

        Set<String> actualBlocks = ClayiumBlocks.REGISTRY.getEntries().stream()
                .map(h -> h.getId().toString())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        assertEquals(427, expectedBlocks.size());
        assertEquals(expectedBlocks, actualBlocks, "Registered blocks must match original_blocks.json 1:1");

        JsonObject itemsObj = JsonParser.parseString(Files.readString(itemsJson)).getAsJsonObject();
        Set<String> expectedStandaloneItems = new LinkedHashSet<>();
        for (JsonElement el : itemsObj.getAsJsonArray("items")) {
            JsonObject obj = el.getAsJsonObject();
            if (obj.get("standalone").getAsBoolean()) {
                expectedStandaloneItems.add(obj.get("new_id").getAsString());
            }
        }

        Set<String> actualStandaloneItems = ClayiumItems.standaloneItems().values().stream()
                .map(h -> h.getId().toString())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        assertEquals(309, expectedStandaloneItems.size());
        assertEquals(expectedStandaloneItems, actualStandaloneItems, "Standalone items must match original_items.json 1:1");
        assertEquals(427 + 309, ClayiumItems.REGISTRY.getEntries().size(), "Total items must be 427 BlockItems + 309 standalone items");

        assertNotNull(ClayiumItems.byPath("clay_plate"));
        assertNotNull(ClayiumItems.byPath("clay_wheel"));
        assertNotNull(ClayiumBlocks.byPath("clay_water_wheel"));
    }

    @Test
    void compressedClayCeSiliconeColorsAndVanillaBindingsMatchOriginal() {
        assertEquals(0L, CompressedClayBlock.clayEnergyForTier(3));
        assertEquals(100_000L, CompressedClayBlock.clayEnergyForTier(4));
        assertEquals(10_000_000_000_000L, CompressedClayBlock.clayEnergyForTier(12));

        assertEquals(Items.IRON_INGOT, CMaterials.getItem(CMaterials.IRON, CShape.INGOT));
        assertEquals(Items.GOLD_INGOT, CMaterials.getItem(CMaterials.GOLD, CShape.INGOT));

        int whiteTint = SiliconeColoredBlock.computeTintRgb(DyeColor.WHITE);
        assertEquals(0xFFFFFFFF, FastColor.ARGB32.opaque(whiteTint));
        int blackTint = SiliconeColoredBlock.computeTintRgb(DyeColor.BLACK);
        assertEquals(0xFF525252, FastColor.ARGB32.opaque(blackTint));
    }

    @Test
    void craftingToolExhaustionThresholdMatchesOriginal() {
        assertFalse(CraftingToolItem.isExhausted(0, 60));
        assertFalse(CraftingToolItem.isExhausted(59, 60));
        assertTrue(CraftingToolItem.isExhausted(60, 60));
        assertTrue(CraftingToolItem.isExhausted(36, 36));
    }
}
