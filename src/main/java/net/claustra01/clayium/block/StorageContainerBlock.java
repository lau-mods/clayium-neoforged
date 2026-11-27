package net.claustra01.clayium.block;

import net.claustra01.clayium.component.ClayiumDataComponentTypes.StorageContentData;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Storage Container (Tier 6, AZ91D hull base + translucent overlays, upgraded state adds storagecontainerex).
 */
public class StorageContainerBlock extends HorizontalClayContainerBlock {
    public static final BooleanProperty UPGRADED = BooleanProperty.create("upgraded");
    private static final int UPGRADED_THRESHOLD = 1 << 30;

    public StorageContainerBlock(Properties properties) {
        super(properties, 6, "az91dhull", "storagecontainer", "storage", null, OverlayMode.STORAGE_CONTAINER);
        this.registerDefaultState(this.defaultBlockState().setValue(UPGRADED, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(UPGRADED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean upgraded = isUpgradedStack(context.getItemInHand());
        return super.getStateForPlacement(context).setValue(UPGRADED, upgraded);
    }

    public static int getMaxStorage(ItemStack stack) {
        StorageContentData data = stack.get(ClayiumDataComponents.STORAGE_CONTENT.get());
        return data != null ? Math.max(data.maxStorage(), StorageContentData.DEFAULT_MAX_STORAGE) : StorageContentData.DEFAULT_MAX_STORAGE;
    }

    public static boolean isUpgradedStack(ItemStack stack) {
        return getMaxStorage(stack) >= UPGRADED_THRESHOLD;
    }
}
