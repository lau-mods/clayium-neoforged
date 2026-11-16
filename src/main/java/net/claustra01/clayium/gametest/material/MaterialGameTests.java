package net.claustra01.clayium.gametest.material;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.material.ClayMaterials;
import net.claustra01.clayium.material.CompressionTier;
import net.claustra01.clayium.material.MaterialAppearance;
import net.claustra01.clayium.material.MaterialShape;
import net.claustra01.clayium.registry.FoundationDataComponentRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.OriginalRegistryCatalog;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MaterialGameTests {
    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void compressionTiersPreserveOriginalValues(GameTestHelper helper) {
        require(CompressionTier.values().length == 13, "Exactly thirteen compression tiers are required");
        require(MaterialRegistration.COMPRESSION_BLOCKS.size() == 13, "A compression block is missing");
        Set<String> ids = new HashSet<>();
        long[] clayUnits = {9L, 81L, 729L, 6_561L, 59_049L, 531_441L, 4_782_969L,
                43_046_721L, 387_420_489L, 3_486_784_401L, 31_381_059_609L,
                282_429_536_481L, 2_541_865_828_329L};
        long[] clayEnergy = {0L, 0L, 0L, 0L, 1L, 10L, 100L, 1_000L, 10_000L,
                100_000L, 1_000_000L, 10_000_000L, 100_000_000L};
        for (CompressionTier compressionTier : CompressionTier.values()) {
            require(ids.add(compressionTier.id()), "Compression tier IDs are not distinct");
            require(compressionTier.clayUnits() == clayUnits[compressionTier.originalMetadata()], "Wrong 9:1 clay value at " + compressionTier.id());
            require(compressionTier.clayEnergy() == clayEnergy[compressionTier.originalMetadata()],
                    "Wrong original CE value at " + compressionTier.id());
            require(BuiltInRegistries.BLOCK.containsKey(ClayiumMod.id(compressionTier.id())),
                    "Compression block is not registered: " + compressionTier.id());
            require(BuiltInRegistries.ITEM.containsKey(ClayiumMod.id(compressionTier.id())),
                    "Compression item is not registered: " + compressionTier.id());
        }
        require(CompressionTier.OCTUPLE_COMPRESSED_ENERGETIC_CLAY.clayEnergy() == 100_000_000L,
                "The highest energetic clay must be worth 100,000,000 CE");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void compressionRecipesRoundTripNineToOne(GameTestHelper helper) {
        CompressionTier[] tiers = CompressionTier.values();
        for (int index = 0; index < tiers.length; index++) {
            CompressionTier upperTier = tiers[index];
            Item lower = index == 0
                    ? Items.CLAY
                    : MaterialRegistration.COMPRESSION_BLOCKS.get(tiers[index - 1].id()).get().asItem();
            Item upper = MaterialRegistration.COMPRESSION_BLOCKS.get(upperTier.id()).get().asItem();

            List<ItemStack> compressionGrid = new ArrayList<>();
            for (int slot = 0; slot < 9; slot++) {
                compressionGrid.add(new ItemStack(lower));
            }
            CraftingInput compressionInput = CraftingInput.of(3, 3, compressionGrid);
            ItemStack compressed = helper.getLevel().getRecipeManager()
                    .getRecipeFor(RecipeType.CRAFTING, compressionInput, helper.getLevel())
                    .orElseThrow(() -> new GameTestAssertException("Missing compression recipe: " + upperTier.id()))
                    .value()
                    .assemble(compressionInput, helper.getLevel().registryAccess());
            require(compressed.is(upper) && compressed.getCount() == 1,
                    "Nine lower-tier items did not create one " + upperTier.id());

            if (index > 0) {
                List<ItemStack> mixedGrid = new ArrayList<>(compressionGrid);
                mixedGrid.set(8, new ItemStack(tiers[index - 1].id().equals("dense_clay")
                        ? Items.CLAY : MaterialRegistration.COMPRESSION_BLOCKS.get(
                                tiers[index - 2].id()).get().asItem()));
                CraftingInput mixedInput = CraftingInput.of(3, 3, mixedGrid);
                require(helper.getLevel().getRecipeManager()
                        .getRecipeFor(RecipeType.CRAFTING, mixedInput, helper.getLevel()).isEmpty(),
                        "Mixed compression tiers unexpectedly crafted " + upperTier.id());
            }
            CraftingInput wrongInput = CraftingInput.of(3, 3,
                    java.util.Collections.nCopies(9, new ItemStack(upper)));
            var wrongRecipe = helper.getLevel().getRecipeManager()
                    .getRecipeFor(RecipeType.CRAFTING, wrongInput, helper.getLevel());
            require(wrongRecipe.isEmpty() || !wrongRecipe.get().value()
                    .assemble(wrongInput, helper.getLevel().registryAccess()).is(upper),
                    "Wrong compression tier crafted itself: " + upperTier.id());

            CraftingInput decompressionInput = CraftingInput.of(1, 1, List.of(new ItemStack(upper)));
            ItemStack decompressed = helper.getLevel().getRecipeManager()
                    .getRecipeFor(RecipeType.CRAFTING, decompressionInput, helper.getLevel())
                    .orElseThrow(() -> new GameTestAssertException("Missing decompression recipe: " + upperTier.id()))
                    .value()
                    .assemble(decompressionInput, helper.getLevel().registryAccess());
            require(decompressed.is(lower) && decompressed.getCount() == 9,
                    "Decompression did not restore nine lower-tier items from " + upperTier.id());
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void originalMaterialShapeLedgerIsExact(GameTestHelper helper) {
        List<OriginalRegistryCatalog.OriginalVariety> ledger = OriginalRegistryCatalog.ORIGINAL_VARIETIES.stream()
                .filter(variety -> variety.kind().equals("material"))
                .toList();
        require(ledger.size() == 245, "The original material-shape ledger must contain 245 combinations");
        require(ClayMaterials.VARIANTS.size() == ledger.size(), "A material-shape combination was added or removed");
        require(MaterialRegistration.MATERIAL_ITEMS.size() == ledger.size() - 1,
                "Every non-vanilla material-shape combination needs one registered item ID");

        Set<String> ledgerIds = new HashSet<>();
        for (OriginalRegistryCatalog.OriginalVariety variety : ledger) {
            require(ledgerIds.add(variety.namedId()), "Duplicate material ledger ID: " + variety.namedId());
        }
        require(ledgerIds.equals(ClayMaterials.VARIANTS_BY_ID.keySet()),
                "Registered material IDs differ from the original materialShapeMap ledger");
        Set<String> registeredIds = new HashSet<>(ledgerIds);
        registeredIds.remove("minecraft:clay");
        registeredIds.remove("minecraft:clay_ball");
        registeredIds.add("impure_osmium_ingot");
        require(registeredIds.equals(MaterialRegistration.MATERIAL_ITEMS.keySet()),
                "Registered items differ from the original material-shape ledger");
        for (String id : registeredIds) {
            require(BuiltInRegistries.ITEM.containsKey(ClayiumMod.id(id)), "Material item is absent: " + id);
        }
        for (OriginalRegistryCatalog.OriginalVariety variety : OriginalRegistryCatalog.ORIGINAL_VARIETIES) {
            if (variety.kind().equals("item") && variety.legacyId().matches(
                    "item\\.item(ClayParts|DenseClayParts|Plates|LargePlates|Dusts|Ingots|Gems)\\..*")) {
                require(registeredIds.contains(variety.namedId())
                        || MaterialRegistration.COMPONENTS.containsKey(variety.namedId()),
                        "Original material item ID is absent: " + variety.namedId());
            }
        }
        for (String id : MaterialRegistration.COMPONENTS.keySet()) {
            require(BuiltInRegistries.ITEM.containsKey(ClayiumMod.id(id)), "Named original component is absent: " + id);
        }
        for (OriginalRegistryCatalog.ExcludedEntry excluded : OriginalRegistryCatalog.EXCLUDED_ENTRIES) {
            if (excluded.legacyId().startsWith("capsule_")) {
                require(!BuiltInRegistries.ITEM.containsKey(ClayiumMod.id(excluded.legacyId())),
                        "An excluded fluid capsule was registered");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void materialNamesColorsAndTooltipsAreComplete(GameTestHelper helper) {
        JsonObject english = language("en_us");
        JsonObject japanese = language("ja_jp");
        for (var variant : ClayMaterials.VARIANTS) {
            if (variant.id().startsWith("minecraft:")) {
                continue;
            }
            String prefix = variant.shape() == MaterialShape.BLOCK ? "block" : "item";
            String key = prefix + ".clayium." + variant.id();
            require(english.has(key), "English material name is missing: " + key);
            require(japanese.has(key), "Japanese material name is missing: " + key);

            if (variant.id().startsWith("minecraft:")) {
                continue;
            }
            ItemStack stack = MaterialRegistration.MATERIAL_ITEMS.get(variant.id()).get().getDefaultInstance();
            Integer tier = stack.get(FoundationDataComponentRegistration.TIER.get());
            require(variant.tier() < 0 ? tier == null : tier != null && tier == variant.tier(),
                    "Material tier tooltip value differs: " + variant.id());
            List<Component> tooltip = new ArrayList<>();
            stack.getItem().appendHoverText(stack, Item.TooltipContext.EMPTY, tooltip, TooltipFlag.NORMAL);
            if (tier != null) {
                require(tooltip.contains(Component.translatable("tooltip.clayium.tier", tier)),
                        "Material tier tooltip is absent: " + variant.id());
            }
            if (MaterialAppearance.usesTint(variant)) {
                JsonObject model = jsonResource("assets/clayium/models/item/" + variant.id() + ".json");
                JsonObject textures = model.getAsJsonObject("textures");
                require(textures.has("layer0") && (variant.shape().name().equals("BALL")
                        || textures.has("layer1") && textures.has("layer2")),
                        "Original three-layer material model is incomplete: " + variant.id());
            }
        }
        for (String id : MaterialRegistration.COMPONENTS.keySet()) {
            require(english.has("item.clayium." + id) && japanese.has("item.clayium." + id),
                    "Named component translation is absent: " + id);
        }
        require(ClayMaterials.MATERIALS.get("copper").color(0) == 0xFFA05A0A,
                "Copper must use its original base color");
        require(ClayMaterials.MATERIALS.get("zinc").color(0) == 0xFFE6AAAA,
                "Zinc must use its original base color");
        require(ClayMaterials.MATERIALS.get("titanium").color(0) == 0xFFD2F0F0,
                "Titanium must use its original base color");
        require(ClayMaterials.MATERIALS.get("actinium").color(1) == 0xFF0000EB,
                "Actinium must use its original dark color");
        helper.succeed();
    }

    private static JsonObject language(String locale) {
        return jsonResource("assets/clayium/lang/" + locale + ".json");
    }

    private static JsonObject jsonResource(String path) {
        try (InputStream stream = MaterialGameTests.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                throw new GameTestAssertException("Missing generated resource: " + path);
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (IOException exception) {
            throw new GameTestAssertException("Unable to read generated resource: " + path);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private MaterialGameTests() {
    }
}
