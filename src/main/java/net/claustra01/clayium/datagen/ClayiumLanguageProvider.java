package net.claustra01.clayium.datagen;

import java.util.Map;
import net.claustra01.clayium.ClayiumMod;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ClayiumLanguageProvider extends LanguageProvider {
    private final String locale;

    public ClayiumLanguageProvider(PackOutput output, String locale) {
        super(output, ClayiumMod.MOD_ID, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        Map<String, String> map = "ja_jp".equals(this.locale)
                ? ClayiumTranslations.jaJp()
                : ClayiumTranslations.enUs();
        map.forEach(this::add);
    }
}
