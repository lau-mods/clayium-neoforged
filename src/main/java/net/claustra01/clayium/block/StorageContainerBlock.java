package net.claustra01.clayium.block;

import java.util.List;
import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.StorageContainerBlockEntity;
import net.claustra01.clayium.component.ClayiumDataComponentTypes.StorageContentData;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * Storage Container (Tier 6, AZ91D hull base + translucent overlays, upgraded state adds storagecontainerex).
 * Preserves stored item, count, and max capacity on drop and placement.
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

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof StorageContainerBlockEntity sc) {
            sc.applyStackComponentData(stack);
        }
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        BlockEntity be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof StorageContainerBlockEntity sc) {
            StorageContentData data = sc.toStorageContentData();
            for (ItemStack drop : drops) {
                if (drop.is(this.asItem())) {
                    drop.set(ClayiumDataComponents.STORAGE_CONTENT.get(), data);
                }
            }
        }
        return drops;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StorageContainerBlockEntity(pos, state);
    }

    public static int getMaxStorage(ItemStack stack) {
        StorageContentData data = stack.get(ClayiumDataComponents.STORAGE_CONTENT.get());
        return data != null ? Math.max(data.maxStorage(), StorageContentData.DEFAULT_MAX_STORAGE) : StorageContentData.DEFAULT_MAX_STORAGE;
    }

    public static boolean isUpgradedStack(ItemStack stack) {
        return getMaxStorage(stack) >= UPGRADED_THRESHOLD;
    }
}
