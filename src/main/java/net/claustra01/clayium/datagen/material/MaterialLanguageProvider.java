package net.claustra01.clayium.datagen.material;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.datagen.LegacyTranslations;
import net.claustra01.clayium.datagen.machine.MachineLanguageEntries;
import net.claustra01.clayium.datagen.gadget.GadgetTranslations;
import net.claustra01.clayium.datagen.weapon.WeaponTranslations;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public final class MaterialLanguageProvider extends LanguageProvider {
    private final String locale;

    public MaterialLanguageProvider(PackOutput output, String locale) {
        super(output, ClayiumMod.MOD_ID, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        Map<String, String> foundation = locale.equals("ja_jp")
                ? LegacyTranslations.JAPANESE : LegacyTranslations.ENGLISH;
        foundation.forEach(this::add);
        GadgetTranslations.forLocale(locale).forEach(this::add);
        WeaponTranslations.forLocale(locale).forEach(this::add);
        try (var stream = MaterialLanguageProvider.class.getClassLoader()
                .getResourceAsStream("data/clayium/original_material_translations.tsv")) {
            if (stream == null) {
                throw new IllegalStateException("Original material translations are missing");
            }
            try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                reader.lines().forEach(line -> {
                    String[] columns = line.split("\t", -1);
                    if (columns.length != 3) {
                        throw new IllegalStateException("Invalid original material translation: " + line);
                    }
                    if (!foundation.containsKey(columns[0])) {
                        add(columns[0], columns[locale.equals("ja_jp") ? 2 : 1]);
                    }
                });
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read original material translations", exception);
        }
        add("tooltip.clayium.ce_value", locale.equals("ja_jp") ? "燃料価値: %1$s CE" : "Fuel value: %1$s CE");
        add("tooltip.clayium.clay_units", locale.equals("ja_jp") ? "粘土換算: %1$s" : "Clay equivalent: %1$s");
        MachineLanguageEntries.add(locale, this::add);
    }
}
