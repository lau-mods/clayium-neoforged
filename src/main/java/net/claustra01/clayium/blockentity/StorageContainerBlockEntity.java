package net.claustra01.clayium.blockentity;

import javax.annotation.Nullable;
import net.claustra01.clayium.block.StorageContainerBlock;
import net.claustra01.clayium.component.ClayiumDataComponentTypes.StorageContentData;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.registry.ClayiumItems;
import net.claustra01.clayium.util.ClayDirection.RelativeSide;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Storage Container (Tier 6): compresses up to {@code 65,536} (or {@code Integer.MAX_VALUE} when upgraded with
 * a Clay Core) of a single item type, exposing virtual insert/extract slots via {@link IItemHandler}.
 */
public class StorageContainerBlockEntity extends ClayContainerBlockEntity {
    public static final int EXTRACT_SLOT_COUNT = 4;
    public static final int INSERT_SLOT_COUNT = 4;
    public static final int VIRTUAL_SLOTS = EXTRACT_SLOT_COUNT + INSERT_SLOT_COUNT;
    public static final int FILTER_SLOT = 1;

    protected int storedCount = 0;
    protected int maxStorageSize = StorageContentData.DEFAULT_MAX_STORAGE;

    public StorageContainerBlockEntity(BlockPos pos, BlockState state) {
        this(ClayiumBlockEntities.STORAGE_CONTAINER.get(), pos, state);
    }

    protected StorageContainerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        // items[0] = stored item prototype (count 1), items[1] = optional locked item filter template
        super(type, pos, state, 2);
    }

    @Override
    protected void initParams() {
        super.initParams();
        this.insertRoutes = new int[]{-1, 0, -1, -1, -1, -1};
        this.extractRoutes = new int[]{-1, -1, -1, -1, -1, -1};
        this.autoInsert = true;
        this.autoExtract = true;
        this.maxAutoInsertDefault = 64;
        this.maxAutoExtractDefault = 64;
        this.autoInsertInterval = 1;
        this.autoExtractInterval = 1;
        int[] slotsIn = new int[VIRTUAL_SLOTS];
        int[] slotsOut = new int[VIRTUAL_SLOTS];
        for (int i = 0; i < VIRTUAL_SLOTS; i++) {
            slotsIn[i] = i;
            slotsOut[i] = VIRTUAL_SLOTS - i - 1;
        }
        this.listSlotsInsert.add(slotsIn);
        this.listSlotsExtract.add(slotsOut);
        this.slotsDrop = new int[0];
    }

    public int getStoredCount() {
        return this.storedCount;
    }

    public int getMaxStorageSize() {
        return this.maxStorageSize;
    }

    public boolean isUpgraded() {
        return this.maxStorageSize >= (1 << 30);
    }

    public ItemStack getStoredTemplate() {
        return this.storedCount > 0 ? this.items.get(0) : ItemStack.EMPTY;
    }

    public ItemStack getFilterTemplate() {
        return this.items.get(FILTER_SLOT);
    }

    public void setFilterTemplate(ItemStack template) {
        this.items.set(FILTER_SLOT, template.isEmpty() ? ItemStack.EMPTY : template.copyWithCount(1));
        markForSync();
    }

    public boolean acceptsItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ItemStack current = getStoredTemplate();
        if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, stack)) {
            return false;
        }
        ItemStack lock = getFilterTemplate();
        return lock.isEmpty() || checkFilterSlot(stack, lock);
    }

    protected boolean checkFilterSlot(ItemStack candidate, ItemStack filter) {
        return ItemStack.isSameItemSameComponents(candidate, filter);
    }

    @Override
    public boolean canInsertToSide(int slot, ItemStack stack, RelativeSide relSide) {
        if (slot < 0 || slot >= VIRTUAL_SLOTS || !acceptsItem(stack)) {
            return false;
        }
        return canInsertItemUnsafe(slot, stack, this.insertRoutes[relSide.id()]);
    }

    @Override
    public boolean canExtractFromSide(int slot, ItemStack stack, RelativeSide relSide) {
        if (slot < 0 || slot >= VIRTUAL_SLOTS || stack.isEmpty()) {
            return false;
        }
        return canExtractItemUnsafe(slot, stack, this.extractRoutes[relSide.id()]);
    }

    @Override
    public boolean canInsertItemUnsafe(int slot, ItemStack stack, int route) {
        if (route < 0 || route >= this.listSlotsInsert.size()) {
            return false;
        }
        return this.storedCount < this.maxStorageSize && acceptsItem(stack);
    }

    @Override
    public boolean canExtractItemUnsafe(int slot, ItemStack stack, int route) {
        return route >= 0 && route < this.listSlotsExtract.size() && this.storedCount > 0;
    }

    public int insertIntoStorage(ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !acceptsItem(stack)) {
            return 0;
        }
        int remainingSpace = Math.max(0, this.maxStorageSize - this.storedCount);
        int toInsert = Math.min(stack.getCount(), remainingSpace);
        if (toInsert > 0 && !simulate) {
            if (this.items.get(0).isEmpty() || this.storedCount == 0) {
                this.items.set(0, stack.copyWithCount(1));
            }
            this.storedCount += toInsert;
            markForSync();
        }
        return toInsert;
    }

    public ItemStack extractFromStorage(int maxAmount, boolean simulate) {
        if (maxAmount <= 0 || this.storedCount <= 0 || this.items.get(0).isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack template = this.items.get(0);
        int taken = Math.min(maxAmount, Math.min(this.storedCount, template.getMaxStackSize()));
        ItemStack result = template.copyWithCount(taken);
        if (!simulate) {
            this.storedCount -= taken;
            if (this.storedCount <= 0) {
                this.storedCount = 0;
                this.items.set(0, ItemStack.EMPTY);
            }
            markForSync();
        }
        return result;
    }

    @Override
    public int getItemUseMode(ItemStack stack, Player player) {
        if (!stack.isEmpty() && stack.is(ClayiumItems.byPath("clay_core").get())) {
            return 99;
        }
        return super.getItemUseMode(stack, player);
    }

    @Override
    protected void useItemFromSide(ItemStack stack, Player player, RelativeSide relSide, int mode, Direction hitFace) {
        if (mode == 99) {
            if (this.maxStorageSize < Integer.MAX_VALUE) {
                this.maxStorageSize = Integer.MAX_VALUE;
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                syncUpgradedBlockState();
                markForInstantSync();
                player.sendSystemMessage(Component.literal("Upgraded Storage Container to 2147483647"));
            }
            return;
        }
        super.useItemFromSide(stack, player, relSide, mode, hitFace);
    }

    public void syncUpgradedBlockState() {
        if (this.level == null) {
            return;
        }
        BlockState state = getBlockState();
        if (state.hasProperty(StorageContainerBlock.UPGRADED)) {
            boolean shouldUpgrade = isUpgraded();
            if (state.getValue(StorageContainerBlock.UPGRADED) != shouldUpgrade) {
                this.level.setBlock(this.worldPosition, state.setValue(StorageContainerBlock.UPGRADED, shouldUpgrade), Block.UPDATE_ALL);
            }
        }
    }

    public StorageContentData toStorageContentData() {
        ItemStack template = getStoredTemplate();
        return new StorageContentData(
                template.isEmpty() ? Items.AIR : template.getItem(),
                this.storedCount,
                this.maxStorageSize
        );
    }

    public void applyStackComponentData(ItemStack stack) {
        StorageContentData data = stack.get(ClayiumDataComponents.STORAGE_CONTENT.get());
        if (data != null) {
            this.maxStorageSize = Math.max(data.maxStorage(), StorageContentData.DEFAULT_MAX_STORAGE);
            if (data.item() != Items.AIR && data.count() > 0L) {
                this.items.set(0, new ItemStack(data.item(), 1));
                this.storedCount = (int) Math.min(this.maxStorageSize, data.count());
            }
            syncUpgradedBlockState();
            markForSync();
        }
    }

    @Override
    public boolean acceptInterfaceSync() {
        return false;
    }

    @Override
    public boolean acceptEnergyClay() {
        return false;
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction side) {
        return new StorageItemHandler(side);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("ItemStackSize", this.storedCount);
        tag.putInt("MaxStorageSize", this.maxStorageSize);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.storedCount = Math.max(0, tag.getInt("ItemStackSize"));
        this.maxStorageSize = tag.contains("MaxStorageSize")
                ? Math.max(StorageContentData.DEFAULT_MAX_STORAGE, tag.getInt("MaxStorageSize"))
                : StorageContentData.DEFAULT_MAX_STORAGE;
        if (this.storedCount == 0 || this.items.get(0).isEmpty()) {
            this.storedCount = 0;
            this.items.set(0, ItemStack.EMPTY);
        }
    }

    private final class StorageItemHandler implements IItemHandler {
        @Nullable
        private final Direction side;

        StorageItemHandler(@Nullable Direction side) {
            this.side = side;
        }

        @Override
        public int getSlots() {
            return VIRTUAL_SLOTS;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot < 0 || slot >= VIRTUAL_SLOTS || StorageContainerBlockEntity.this.storedCount <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack template = getStoredTemplate();
            if (template.isEmpty()) {
                return ItemStack.EMPTY;
            }
            int maxStack = template.getMaxStackSize();
            if (slot < EXTRACT_SLOT_COUNT) {
                int remainingBefore = StorageContainerBlockEntity.this.storedCount - slot * maxStack;
                if (remainingBefore <= 0) {
                    return ItemStack.EMPTY;
                }
                return template.copyWithCount(Math.min(maxStack, remainingBefore));
            }
            int free = StorageContainerBlockEntity.this.maxStorageSize - StorageContainerBlockEntity.this.storedCount;
            int totalInsertCap = INSERT_SLOT_COUNT * maxStack;
            if (free >= totalInsertCap) {
                return ItemStack.EMPTY;
            }
            int filledInsert = totalInsertCap - free;
            int insertIdx = slot - EXTRACT_SLOT_COUNT;
            int amt = Math.min(maxStack, Math.max(0, filledInsert - insertIdx * maxStack));
            return amt <= 0 ? ItemStack.EMPTY : template.copyWithCount(amt);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty() || slot < 0 || slot >= VIRTUAL_SLOTS) {
                return stack;
            }
            if (this.side != null && !canInsertToSide(slot, stack, toRelativeSide(this.side))) {
                return stack;
            }
            int moved = insertIntoStorage(stack, simulate);
            return moved >= stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - moved);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount <= 0 || slot < 0 || slot >= VIRTUAL_SLOTS) {
                return ItemStack.EMPTY;
            }
            ItemStack current = getStoredTemplate();
            if (current.isEmpty()) {
                return ItemStack.EMPTY;
            }
            if (this.side != null && !canExtractFromSide(slot, current, toRelativeSide(this.side))) {
                return ItemStack.EMPTY;
            }
            return extractFromStorage(amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            ItemStack current = getStoredTemplate();
            return current.isEmpty() ? 64 : current.getMaxStackSize();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (this.side != null) {
                return canInsertToSide(slot, stack, toRelativeSide(this.side));
            }
            return acceptsItem(stack);
        }
    }
}
