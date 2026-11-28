package net.claustra01.clayium.blockentity;

import java.util.List;
import net.claustra01.clayium.filter.ClayiumFilterMatcher;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Area Collector (Tier 7): pulls matching dropped {@link ItemEntity} stacks inside its linked {@link net.minecraft.world.phys.AABB}.
 */
public class AreaCollectorBlockEntity extends AreaMinerBlockEntity {
    public AreaCollectorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ClayiumBlockEntities.AREA_COLLECTOR.get(), pos, blockState);
        ensureTierInitialized();
    }

    @Override
    protected void initParams() {
        super.initParams();
        this.energyPerTick = 100L;
        this.progressPerTick = 100L;
        this.progressPerJob = 10L;
        this.maxJobCountInLoop = 1000;
    }

    @Override
    protected void initParamsByTier(int tier) {
        super.initParamsByTier(tier);
        this.energyPerTick = 100L;
        this.state = initState();
    }

    @Override
    protected int initState() {
        return STATE_REPEAT_START;
    }

    @Override
    protected void doWork() {
        if (this.level == null || this.axisAlignedBB == null) {
            return;
        }
        List<ItemEntity> entities = this.level.getEntitiesOfClass(
                ItemEntity.class,
                this.axisAlignedBB,
                e -> e.isAlive() && !e.getItem().isEmpty()
        );
        ItemStack filter = getStack(FILTER_HARVEST_SLOT);
        for (ItemEntity entity : entities) {
            ItemStack entityStack = entity.getItem();
            if (ClayiumFilterMatcher.isFilter(filter) && !ClayiumFilterMatcher.match(filter, entityStack)) {
                continue;
            }
            while (!entityStack.isEmpty() && this.progress >= this.progressPerJob) {
                ItemStack single = entityStack.copyWithCount(1);
                if (!insertSingleItem(single)) {
                    return;
                }
                this.progress -= this.progressPerJob;
                entityStack.shrink(1);
                if (entityStack.isEmpty()) {
                    entity.discard();
                    break;
                }
                entity.setItem(entityStack);
                if (this.state == STATE_ONCE_RUNNING) {
                    setState(STATE_STOPPED);
                    return;
                }
            }
        }
        this.progress = 0L;
    }

    private boolean insertSingleItem(ItemStack single) {
        for (int i = 0; i < this.slotNum; i++) {
            ItemStack existing = getStack(i);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, single)) {
                int max = Math.min(64, existing.getMaxStackSize());
                if (existing.getCount() < max) {
                    existing.grow(1);
                    markForSync();
                    return true;
                }
            }
        }
        for (int i = 0; i < this.slotNum; i++) {
            if (getStack(i).isEmpty()) {
                setStack(i, single);
                return true;
            }
        }
        return false;
    }
}
