package net.claustra01.clayium.datagen.trade;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.TradeRegistration;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class TradeBlockStateProvider extends BlockStateProvider {
    public TradeBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        var model = models().getBuilder("auto_trader")
                .texture("side", ClayiumMod.id("block/autotraderside"))
                .texture("top", ClayiumMod.id("block/autotradertop"))
                .texture("particle", "#side")
                .element().from(0, 0, 0).to(16, 16, 16)
                .face(Direction.DOWN).texture("#side").cullface(Direction.DOWN).end()
                .face(Direction.UP).texture("#top").cullface(Direction.UP).end()
                .face(Direction.NORTH).texture("#side").cullface(Direction.NORTH).end()
                .face(Direction.SOUTH).texture("#side").cullface(Direction.SOUTH).end()
                .face(Direction.WEST).texture("#side").cullface(Direction.WEST).end()
                .face(Direction.EAST).texture("#side").cullface(Direction.EAST).end()
                .end();
        simpleBlockWithItem(TradeRegistration.AUTO_TRADER.get(), model);
    }

    @Override
    public String getName() {
        return "Auto Trader Block State";
    }
}
