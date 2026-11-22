package net.claustra01.clayium.datagen;

import java.util.List;
import java.util.Set;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.datagen.gadget.GadgetItemModelProvider;
import net.claustra01.clayium.datagen.advancedtool.AdvancedToolItemModelProvider;
import net.claustra01.clayium.datagen.area.AreaBlockLoot;
import net.claustra01.clayium.datagen.area.AreaBlockStateProvider;
import net.claustra01.clayium.datagen.autocrafter.AutoCrafterBlockLoot;
import net.claustra01.clayium.datagen.autocrafter.AutoCrafterBlockStateProvider;
import net.claustra01.clayium.datagen.fluid.FluidBlockLoot;
import net.claustra01.clayium.datagen.fluid.FluidBlockStateProvider;
import net.claustra01.clayium.datagen.fluid.FluidItemModelProvider;
import net.claustra01.clayium.datagen.environment.EnvironmentBlockLoot;
import net.claustra01.clayium.datagen.environment.EnvironmentBlockStateProvider;
import net.claustra01.clayium.datagen.energyconverter.EnergyConverterBlockLoot;
import net.claustra01.clayium.datagen.energyconverter.EnergyConverterBlockStateProvider;
import net.claustra01.clayium.datagen.clayproduction.ClayProductionBlockLoot;
import net.claustra01.clayium.datagen.clayproduction.ClayProductionBlockStateProvider;
import net.claustra01.clayium.datagen.chunkloader.ChunkLoaderBlockLoot;
import net.claustra01.clayium.datagen.chunkloader.ChunkLoaderBlockStateProvider;
import net.claustra01.clayium.datagen.creative.CreativeSourceBlockLoot;
import net.claustra01.clayium.datagen.creative.CreativeSourceBlockStateProvider;
import net.claustra01.clayium.datagen.material.MaterialBlockLoot;
import net.claustra01.clayium.datagen.material.MaterialBlockStateProvider;
import net.claustra01.clayium.datagen.material.MaterialItemModelProvider;
import net.claustra01.clayium.datagen.material.MaterialItemTagProvider;
import net.claustra01.clayium.datagen.material.MaterialLanguageProvider;
import net.claustra01.clayium.datagen.miner.MinerBlockLoot;
import net.claustra01.clayium.datagen.miner.MinerBlockStateProvider;
import net.claustra01.clayium.datagen.machine.MachineBlockLoot;
import net.claustra01.clayium.datagen.machine.MachineBlockStateProvider;
import net.claustra01.clayium.datagen.machine.AssemblyMachineModels;
import net.claustra01.clayium.datagen.logistics.LogisticsBlockLoot;
import net.claustra01.clayium.datagen.logistics.LogisticsBlockStateProvider;
import net.claustra01.clayium.datagen.logistics.LogisticsItemModelProvider;
import net.claustra01.clayium.datagen.iface.InterfaceBlockLoot;
import net.claustra01.clayium.datagen.iface.InterfaceBlockStateProvider;
import net.claustra01.clayium.datagen.laser.LaserBlockLoot;
import net.claustra01.clayium.datagen.laser.LaserBlockStateProvider;
import net.claustra01.clayium.datagen.iface.InterfaceItemModelProvider;
import net.claustra01.clayium.datagen.multitrack.MultitrackBlockLoot;
import net.claustra01.clayium.datagen.multitrack.MultitrackBlockStateProvider;
import net.claustra01.clayium.datagen.storage.StorageBlockLoot;
import net.claustra01.clayium.datagen.storage.StorageBlockStateProvider;
import net.claustra01.clayium.datagen.storage.StorageItemModelProvider;
import net.claustra01.clayium.datagen.worldgen.WorldgenBlockLoot;
import net.claustra01.clayium.datagen.worldgen.WorldgenBlockStateProvider;
import net.claustra01.clayium.datagen.worldgen.WorldgenDataProvider;
import net.claustra01.clayium.datagen.claytree.ClayTreeBlockLoot;
import net.claustra01.clayium.datagen.claytree.ClayTreeBlockStateProvider;
import net.claustra01.clayium.datagen.waterwheel.WaterWheelBlockLoot;
import net.claustra01.clayium.datagen.waterwheel.WaterWheelBlockStateProvider;
import net.claustra01.clayium.datagen.recipe.MachineRecipeProvider;
import net.claustra01.clayium.datagen.progression.ProgressionRecipeProvider;
import net.claustra01.clayium.datagen.tag.VanillaCompatTags;
import net.claustra01.clayium.datagen.workbench.WorkbenchBlockLoot;
import net.claustra01.clayium.datagen.workbench.WorkbenchBlockStateProvider;
import net.claustra01.clayium.datagen.workbench.WorkbenchItemModelProvider;
import net.claustra01.clayium.datagen.weapon.WeaponItemModelProvider;
import net.claustra01.clayium.datagen.trade.TradeBlockLoot;
import net.claustra01.clayium.datagen.trade.TradeBlockStateProvider;
import net.claustra01.clayium.datagen.multiblock.MultiblockBlockLoot;
import net.claustra01.clayium.datagen.multiblock.MultiblockBlockStateProvider;
import net.claustra01.clayium.datagen.ca.CaBlockStateProvider;
import net.claustra01.clayium.datagen.ca.CaBlockLoot;
import net.claustra01.clayium.datagen.careactor.CaReactorBlockLoot;
import net.claustra01.clayium.datagen.careactor.CaReactorBlockStateProvider;
import net.claustra01.clayium.datagen.pan.PanBlockLoot;
import net.claustra01.clayium.datagen.pan.PanBlockStateProvider;
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
        generator.addProvider(event.includeClient(), new AutoCrafterBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new MinerBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new AreaBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new ChunkLoaderBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new EnergyConverterBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new FluidBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new AssemblyMachineModels(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new ClayProductionBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new LogisticsBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new InterfaceBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new LaserBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new CreativeSourceBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new MultitrackBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new StorageBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new WorldgenBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new ClayTreeBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new WaterWheelBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new EnvironmentBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new WorkbenchBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new TradeBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new MultiblockBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new CaBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new CaReactorBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new PanBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new MaterialItemModelProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new WorkbenchItemModelProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new GadgetItemModelProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new WeaponItemModelProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new AdvancedToolItemModelProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new LogisticsItemModelProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new StorageItemModelProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new InterfaceItemModelProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new FluidItemModelProvider(output, event.getExistingFileHelper()));
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
                new VanillaCompatTags(
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
                                new LootTableProvider.SubProviderEntry(WorldgenBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(ClayTreeBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(MaterialBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(MachineBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(AutoCrafterBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(MinerBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(AreaBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(ChunkLoaderBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(EnergyConverterBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(FluidBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(ClayProductionBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(LogisticsBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(InterfaceBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(LaserBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(CreativeSourceBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(MultitrackBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(StorageBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(WaterWheelBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(EnvironmentBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(WorkbenchBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(TradeBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(MultiblockBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(CaBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(CaReactorBlockLoot::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(PanBlockLoot::new, LootContextParamSets.BLOCK)),
                        event.getLookupProvider()));
        generator.addProvider(event.includeServer(), WorldgenDataProvider.create(event));
        generator.addProvider(event.includeServer(), new MachineRecipeProvider(output, event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new ProgressionRecipeProvider(output, event.getLookupProvider()));
    }

    private ClayiumDataGenerators() {
    }
}
