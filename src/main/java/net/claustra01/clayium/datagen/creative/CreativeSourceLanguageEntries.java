package net.claustra01.clayium.datagen.creative;

import java.util.function.BiConsumer;
import net.claustra01.clayium.registry.CreativeSourceRegistration;

public final class CreativeSourceLanguageEntries {
    public static void add(String locale, BiConsumer<String, String> add) {
        add.accept(
                "block.clayium." + CreativeSourceRegistration.ID,
                locale.equals("ja_jp") ? "クリエイティブCE源" : "Creative CE Source");
    }

    private CreativeSourceLanguageEntries() {
    }
}
