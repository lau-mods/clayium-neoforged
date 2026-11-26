package net.claustra01.clayium.datagen.trade;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.TradeRegistration;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class TradeBlockStateProvider extends BlockStateProvider {
    public TradeBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        var model = models().withExistingParent("auto_trader", modLoc("block/hull_with_overlays"))
                .renderType("minecraft:cutout")
                .texture("hull", modLoc("block/az91dhull"))
                .texture("side", modLoc("block/autotraderside"))
                .texture("front", "#side")
                .texture("top", modLoc("block/autotradertop"));
        simpleBlockWithItem(TradeRegistration.AUTO_TRADER.get(), model);
    }

    @Override
    public String getName() {
        return "Auto Trader Block State";
    }
}
