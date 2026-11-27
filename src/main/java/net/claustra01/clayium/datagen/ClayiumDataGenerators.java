package net.claustra01.clayium.datagen;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.claustra01.clayium.ClayiumMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = ClayiumMod.MOD_ID)
public final class ClayiumDataGenerators {
    private ClayiumDataGenerators() {}

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        ClayiumBlockTagsProvider blockTags = generator.addProvider(
                event.includeServer(),
                new ClayiumBlockTagsProvider(packOutput, lookupProvider, existingFileHelper)
        );
        generator.addProvider(
                event.includeServer(),
                new ClayiumItemTagsProvider(packOutput, lookupProvider, blockTags.contentsGetter(), existingFileHelper)
        );
        generator.addProvider(
                event.includeServer(),
                new ClayiumRecipeProvider(packOutput, lookupProvider)
        );
        generator.addProvider(
                event.includeServer(),
                new LootTableProvider(
                        packOutput,
                        Set.of(),
                        List.of(new LootTableProvider.SubProviderEntry(ClayiumLootTableProvider::new, LootContextParamSets.BLOCK)),
                        lookupProvider
                )
        );

        generator.addProvider(
                event.includeClient(),
                new ClayiumBlockStateProvider(packOutput, existingFileHelper)
        );
        generator.addProvider(
                event.includeClient(),
                new ClayiumItemModelProvider(packOutput, existingFileHelper)
        );
        generator.addProvider(event.includeClient(), new ClayiumLanguageProvider(packOutput, "en_us"));
        generator.addProvider(event.includeClient(), new ClayiumLanguageProvider(packOutput, "ja_jp"));
    }
}
