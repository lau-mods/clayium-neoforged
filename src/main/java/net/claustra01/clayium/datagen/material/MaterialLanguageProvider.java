package net.claustra01.clayium.datagen.material;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.datagen.LegacyTranslations;
import net.claustra01.clayium.datagen.autocrafter.AutoCrafterLanguageEntries;
import net.claustra01.clayium.datagen.environment.EnvironmentLanguageEntries;
import net.claustra01.clayium.datagen.machine.MachineLanguageEntries;
import net.claustra01.clayium.datagen.logistics.LogisticsLanguageEntries;
import net.claustra01.clayium.datagen.gadget.GadgetTranslations;
import net.claustra01.clayium.datagen.fluid.FluidLanguageEntries;
import net.claustra01.clayium.datagen.energyconverter.EnergyConverterLanguageEntries;
import net.claustra01.clayium.datagen.clayproduction.ClayProductionLanguageEntries;
import net.claustra01.clayium.datagen.chunkloader.ChunkLoaderLanguageEntries;
import net.claustra01.clayium.datagen.creative.CreativeSourceLanguageEntries;
import net.claustra01.clayium.datagen.weapon.WeaponTranslations;
import net.claustra01.clayium.datagen.waterwheel.WaterWheelLanguageEntries;
import net.claustra01.clayium.datagen.multitrack.MultitrackLanguageEntries;
import net.claustra01.clayium.datagen.storage.StorageLanguageEntries;
import net.claustra01.clayium.datagen.iface.InterfaceLanguageEntries;
import net.claustra01.clayium.datagen.laser.LaserLanguageEntries;
import net.claustra01.clayium.datagen.trade.TradeLanguageEntries;
import net.claustra01.clayium.datagen.miner.MinerLanguageEntries;
import net.claustra01.clayium.datagen.area.AreaLanguageEntries;
import net.claustra01.clayium.datagen.multiblock.MultiblockLanguageEntries;
import net.claustra01.clayium.datagen.claytree.ClayTreeLanguageEntries;
import net.claustra01.clayium.datagen.ca.CaLanguageEntries;
import net.claustra01.clayium.datagen.careactor.CaReactorLanguageEntries;
import net.claustra01.clayium.datagen.advancedtool.AdvancedToolLanguageEntries;
import net.claustra01.clayium.datagen.pan.PanLanguageEntries;
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
        WaterWheelLanguageEntries.add(locale, this::add);
        EnvironmentLanguageEntries.forLocale(locale).forEach((id, name) -> add("block.clayium." + id, name));
        ClayProductionLanguageEntries.add(locale, this::add);
        EnergyConverterLanguageEntries.add(locale, this::add);
        FluidLanguageEntries.add(locale, this::add);
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
        AutoCrafterLanguageEntries.add(locale, this::add);
        MinerLanguageEntries.add(locale, this::add);
        AreaLanguageEntries.add(locale, this::add);
        ChunkLoaderLanguageEntries.add(locale, this::add);
        LogisticsLanguageEntries.add(locale, this::add);
        CreativeSourceLanguageEntries.add(locale, this::add);
        MultitrackLanguageEntries.add(locale, this::add);
        StorageLanguageEntries.add(locale, this::add);
        InterfaceLanguageEntries.add(locale, this::add);
        LaserLanguageEntries.add(locale, this::add);
        MultiblockLanguageEntries.forLocale(locale).forEach(this::add);
        TradeLanguageEntries.add(locale, this::add);
        ClayTreeLanguageEntries.add(locale, this::add);
        CaLanguageEntries.add(locale, this::add);
        CaReactorLanguageEntries.add(locale, this::add);
        PanLanguageEntries.add(locale, this::add);
        AdvancedToolLanguageEntries.add(locale, this::add);
    }
}
