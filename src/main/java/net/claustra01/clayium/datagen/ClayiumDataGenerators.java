package net.claustra01.clayium.datagen;

import java.util.List;
import java.util.Set;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.datagen.gadget.GadgetItemModelProvider;
import net.claustra01.clayium.datagen.material.MaterialBlockLoot;
import net.claustra01.clayium.datagen.material.MaterialBlockStateProvider;
import net.claustra01.clayium.datagen.material.MaterialItemModelProvider;
import net.claustra01.clayium.datagen.material.MaterialItemTagProvider;
import net.claustra01.clayium.datagen.material.MaterialLanguageProvider;
import net.claustra01.clayium.datagen.machine.MachineBlockLoot;
import net.claustra01.clayium.datagen.machine.MachineBlockStateProvider;
import net.claustra01.clayium.datagen.worldgen.WorldgenBlockLoot;
import net.claustra01.clayium.datagen.worldgen.WorldgenBlockStateProvider;
import net.claustra01.clayium.datagen.worldgen.WorldgenDataProvider;
import net.claustra01.clayium.datagen.recipe.MachineRecipeProvider;
import net.claustra01.clayium.datagen.weapon.WeaponItemModelProvider;
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
        generator.addProvider(event.includeClient(), new MaterialBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new MachineBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new WorldgenBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new MaterialItemModelProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new GadgetItemModelProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new WeaponItemModelProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new MaterialLanguageProvider(output, "en_us"));
        generator.addProvider(event.includeClient(), new MaterialLanguageProvider(output, "ja_jp"));

        FoundationBlockTagProvider blockTags = new FoundationBlockTagProvider(
                output, event.getLookupProvider(), event.getExistingFileHelper());
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(
                event.includeServer(),
                new MaterialItemTagProvider(
                        output,
                        event.getLookupProvider(),
                        blockTags.contentsGetter(),
                        event.getExistingFileHelper()));
        generator.addProvider(
                event.includeServer(),
                new LootTableProvider(
                        output,
                        Set.of(),
                        List.of(
                                new LootTableProvider.SubProviderEntry(FoundationBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(MaterialBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(MachineBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(WorldgenBlockLoot::new, LootContextParamSets.BLOCK)),
                        event.getLookupProvider()));
        generator.addProvider(event.includeServer(), WorldgenDataProvider.create(event));
        generator.addProvider(event.includeServer(), new MachineRecipeProvider(output, event.getLookupProvider()));
    }

    private ClayiumDataGenerators() {
    }
}
