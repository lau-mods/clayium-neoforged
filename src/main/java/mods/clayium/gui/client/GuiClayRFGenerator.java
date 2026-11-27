package mods.clayium.gui.client;

import mods.clayium.block.tile.TileClayRFGenerator;
import mods.clayium.gui.container.ContainerTemp;
import mods.clayium.util.UtilLocale;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;

public class GuiClayRFGenerator extends GuiClayEnergyTemp {
    public GuiClayRFGenerator(ContainerTemp container, TileClayRFGenerator tile, Block block) {
        super(container, tile, block);
    }

    protected void func_146979_b(int mouseX, int mouseZ) {
        super.func_146979_b(mouseX, mouseZ);
        ContainerTemp container = (ContainerTemp)this.field_147002_h;
        TileClayRFGenerator tileg = (TileClayRFGenerator)this.tile;
        this.field_146289_q
            .func_78276_b(
                I18n.func_135052_a(
                    "gui.RFGenerator.storage", new Object[]{UtilLocale.rfNumeral((long)tileg.rfStored), UtilLocale.rfNumeral((long)tileg.maxRfStored)}
                ),
                12,
                20,
                4210752
            );
        this.field_146289_q
            .func_78276_b(
                I18n.func_135052_a(
                    "gui.RFGenerator.convertRate",
                    new Object[]{UtilLocale.ClayEnergyNumeral(tileg.ceConsumptionPerTick), UtilLocale.rfNumeral((long)tileg.rfProductionPerTick)}
                ),
                12,
                40,
                4210752
            );
        this.field_146289_q
            .func_78276_b(I18n.func_135052_a("gui.RFGenerator.output", new Object[]{UtilLocale.rfNumeral((long)tileg.rfOutputPerTick)}), 12, 52, 4210752);
        if (tileg.overclockExponent == 1.0) {
            this.field_146289_q
                .func_78276_b(
                    I18n.func_135052_a("gui.RFGenerator.overclock", new Object[]{UtilLocale.CAResonanceNumeral(tileg.overclockTotalFactor)}), 12, 64, 4210752
                );
        } else {
            this.field_146289_q
                .func_78276_b(
                    I18n.func_135052_a(
                        "gui.RFGenerator.overclockExponen",
                        new Object[]{
                            UtilLocale.CAResonanceNumeral(tileg.overclockTotalFactor),
                            UtilLocale.CAResonanceNumeral(tileg.overclockExponent),
                            UtilLocale.CAResonanceNumeral(Math.pow(tileg.overclockTotalFactor, tileg.overclockExponent))
                        }
                    ),
                    12,
                    64,
                    4210752
                );
        }
    }
}
