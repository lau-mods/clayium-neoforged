package net.claustra01.clayium.datagen;

import java.util.Map;
import net.claustra01.clayium.ClayiumMod;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public final class FoundationLanguageProvider extends LanguageProvider {
    private final String locale;

    public FoundationLanguageProvider(PackOutput output, String locale) {
        super(output, ClayiumMod.MOD_ID, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        Map<String, String> translations = locale.equals("ja_jp")
                ? LegacyTranslations.JAPANESE : LegacyTranslations.ENGLISH;
        translations.forEach(this::add);
    }
}
