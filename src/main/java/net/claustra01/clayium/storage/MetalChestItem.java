package net.claustra01.clayium.storage;

import java.util.function.Consumer;
import net.claustra01.clayium.client.screen.storage.MetalChestItemRenderer;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public final class MetalChestItem extends BlockItem {
    public MetalChestItem(MetalChestBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return MetalChestItemRenderer.INSTANCE;
            }
        });
    }
}
