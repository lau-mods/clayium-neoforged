package net.claustra01.clayium.datagen;

import java.util.List;
import java.util.Set;
import net.claustra01.clayium.ClayiumMod;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class ClayiumDataGenerators {
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        generator.addProvider(event.includeClient(), new FoundationBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new FoundationItemModelProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new FoundationLanguageProvider(output, "en_us"));
        generator.addProvider(event.includeClient(), new FoundationLanguageProvider(output, "ja_jp"));

        FoundationBlockTagProvider blockTags = new FoundationBlockTagProvider(
                output, event.getLookupProvider(), event.getExistingFileHelper());
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(
                event.includeServer(),
                new FoundationItemTagProvider(
                        output,
                        event.getLookupProvider(),
                        blockTags.contentsGetter(),
                        event.getExistingFileHelper()));
        generator.addProvider(
                event.includeServer(),
                new LootTableProvider(
                        output,
                        Set.of(),
                        List.of(new LootTableProvider.SubProviderEntry(FoundationBlockLoot::new, LootContextParamSets.BLOCK)),
                        event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new FoundationRecipeProvider(output, event.getLookupProvider()));
    }

    private ClayiumDataGenerators() {
    }
}
