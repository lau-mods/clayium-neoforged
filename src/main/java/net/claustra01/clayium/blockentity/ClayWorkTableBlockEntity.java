package net.claustra01.clayium.blockentity;

import java.util.Collection;
import javax.annotation.Nullable;
import net.claustra01.clayium.recipe.ClayWorkTableRecipe;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.registry.ClayiumItems;
import net.claustra01.clayium.registry.ClayiumRecipeTypes;
import net.claustra01.clayium.transfer.ClayiumTransfer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Clay Work Table (Tier 0): 5-slot manual clay kneading/shaping table with 6 action buttons
 * matching 1.7.10 {@code TileClayWorkTable}.
 * Slots: 0 = material, 1 = tool (rolling pin / slicer / spatula), 2 = primary output, 3 = secondary output, 4 = work buffer.
 */
public class ClayWorkTableBlockEntity extends BlockEntity {
    private static final int[] SLOTS_TOP = {0};
    private static final int[] SLOTS_BOTTOM = {2, 1};
    private static final int[] SLOTS_SIDES = {1};

    private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    public int cookTime = 0;
    public int timeToCook = 0;
    public int cookingMethod = 0;
    @Nullable
    private Collection<ClayWorkTableRecipe> overrideRecipesForTest;

    public ClayWorkTableBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.CLAY_WORK_TABLE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ClayWorkTableBlockEntity be) {
        if (level.isClientSide) {
            return;
        }
        be.tickAutoExtractToNeighbors(level, pos);
    }

    private void tickAutoExtractToNeighbors(Level level, BlockPos pos) {
        int[] fromSlots = {2, 3};
        IItemHandler source = getItemHandler(Direction.DOWN);
        for (Direction dir : Direction.values()) {
            if (getStack(2).isEmpty() && getStack(3).isEmpty()) {
                break;
            }
            IItemHandler target = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.relative(dir), dir.getOpposite());
            if (target == null) {
                continue;
            }
            int[] toSlots = new int[target.getSlots()];
            for (int i = 0; i < toSlots.length; i++) {
                toSlots[i] = i;
            }
            ClayiumTransfer.transfer(source, fromSlots, target, toSlots, 1);
        }
    }

    public void setOverrideRecipesForTest(@Nullable Collection<ClayWorkTableRecipe> recipes) {
        this.overrideRecipesForTest = recipes;
    }

    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    public ItemStack getStack(int slot) {
        return slot >= 0 && slot < this.items.size() ? this.items.get(slot) : ItemStack.EMPTY;
    }

    public void setStack(int slot, ItemStack stack) {
        if (slot >= 0 && slot < this.items.size()) {
            this.items.set(slot, stack);
            setChanged();
        }
    }

    @Nullable
    public ClayWorkTableRecipe findRecipe(ItemStack material, int buttonId) {
        if (material.isEmpty() || buttonId <= 0) {
            return null;
        }
        if (this.overrideRecipesForTest != null) {
            for (ClayWorkTableRecipe recipe : this.overrideRecipesForTest) {
                if (recipe.buttonId() == buttonId && recipe.input().test(material)) {
                    return recipe;
                }
            }
            return null;
        }
        if (this.level == null) {
            return null;
        }
        for (RecipeHolder<ClayWorkTableRecipe> holder :
                this.level.getRecipeManager().getAllRecipesFor(ClayiumRecipeTypes.CLAY_WORK_TABLE.get())) {
            ClayWorkTableRecipe recipe = holder.value();
            if (recipe.buttonId() == buttonId && recipe.input().test(material)) {
                return recipe;
            }
        }
        return null;
    }

    public boolean canKnead(ItemStack material, int buttonId) {
        ClayWorkTableRecipe recipe = findRecipe(material, buttonId);
        if (recipe == null || recipe.primaryResult().isEmpty()) {
            return false;
        }
        if (!canFitInto(2, recipe.primaryResult())) {
            return false;
        }
        if (!recipe.secondaryResult().isEmpty() && !canFitInto(3, recipe.secondaryResult())) {
            return false;
        }
        return true;
    }

    private boolean canFitInto(int slot, ItemStack result) {
        ItemStack existing = getStack(slot);
        if (existing.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(existing, result)) {
            return false;
        }
        return existing.getCount() + result.getCount() <= Math.min(64, existing.getMaxStackSize());
    }

    private void addInto(int slot, ItemStack result) {
        if (result.isEmpty()) {
            return;
        }
        ItemStack existing = getStack(slot);
        if (existing.isEmpty()) {
            setStack(slot, result.copy());
        } else if (ItemStack.isSameItemSameComponents(existing, result)) {
            existing.grow(result.getCount());
            setChanged();
        }
    }

    /**
     * @return {@code 0} if disabled, {@code 1} if press advances active/new craft, {@code 2} if press resets incompatible work buffer.
     */
    public int canPushButton(int buttonId) {
        if (buttonId < 1 || buttonId > 6) {
            return 0;
        }
        if (!ClayWorkTableRecipe.toolMatchesButton(buttonId, getStack(1))) {
            return 0;
        }
        if (this.cookingMethod != 0) {
            if (this.cookingMethod == buttonId && canKnead(getStack(4), buttonId)) {
                return 1;
            }
        }
        if (!canKnead(getStack(0), buttonId)) {
            return 0;
        }
        return this.cookingMethod == 0 ? 1 : 2;
    }

    public void pushButton(int buttonId) {
        int can = canPushButton(buttonId);
        if (can == 0) {
            return;
        }
        if (buttonId >= 3) {
            ItemStack tool = getStack(1);
            if (!tool.isEmpty() && tool.hasCraftingRemainingItem()) {
                setStack(1, tool.getCraftingRemainingItem());
            }
        }
        if (can == 1) {
            if (this.cookingMethod == 0) {
                ClayWorkTableRecipe recipe = findRecipe(getStack(0), buttonId);
                if (recipe == null) {
                    return;
                }
                this.timeToCook = recipe.clicks();
                this.cookingMethod = buttonId;
                ItemStack mat = getStack(0);
                ItemStack taken = mat.split(recipe.input().count());
                if (mat.isEmpty()) {
                    setStack(0, ItemStack.EMPTY);
                }
                setStack(4, taken);
            }
            this.cookTime++;
            if (this.cookTime >= this.timeToCook) {
                ClayWorkTableRecipe recipe = findRecipe(getStack(4), buttonId);
                this.cookTime = 0;
                this.cookingMethod = 0;
                if (recipe != null) {
                    addInto(2, recipe.primaryResult());
                    if (!recipe.secondaryResult().isEmpty()) {
                        addInto(3, recipe.secondaryResult());
                    }
                    ItemStack buf = getStack(4);
                    buf.shrink(recipe.input().count());
                    if (buf.isEmpty()) {
                        setStack(4, ItemStack.EMPTY);
                    }
                } else {
                    setStack(4, ItemStack.EMPTY);
                }
            }
        } else if (can == 2) {
            this.cookTime = 0;
            this.cookingMethod = 0;
            setStack(4, ItemStack.EMPTY);
        }
        setChanged();
    }

    public static boolean isItemTool(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        return item == ClayiumItems.byPath("clay_rolling_pin").get()
                || item == ClayiumItems.byPath("clay_slicer").get()
                || item == ClayiumItems.byPath("clay_spatula").get();
    }

    public IItemHandler getItemHandler(@Nullable Direction side) {
        return new WorkTableItemHandler(side);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putShort("CookTime", (short) this.cookTime);
        tag.putShort("TimeToCook", (short) this.timeToCook);
        tag.putShort("CookingMethod", (short) this.cookingMethod);
        ContainerHelper.saveAllItems(tag, this.items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(5, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, registries);
        this.cookTime = tag.getShort("CookTime");
        this.timeToCook = tag.getShort("TimeToCook");
        this.cookingMethod = tag.getShort("CookingMethod");
    }

    private final class WorkTableItemHandler implements IItemHandler {
        @Nullable
        private final Direction side;

        private WorkTableItemHandler(@Nullable Direction side) {
            this.side = side;
        }

        private int[] accessibleSlots() {
            if (this.side == null || this.side == Direction.DOWN) {
                return SLOTS_BOTTOM;
            }
            if (this.side == Direction.UP) {
                return SLOTS_TOP;
            }
            return SLOTS_SIDES;
        }

        private int mapSlot(int index) {
            int[] slots = accessibleSlots();
            return index >= 0 && index < slots.length ? slots[index] : -1;
        }

        @Override
        public int getSlots() {
            return accessibleSlots().length;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            int raw = mapSlot(slot);
            return raw >= 0 ? getStack(raw) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            int raw = mapSlot(slot);
            if (raw < 0 || stack.isEmpty() || !isItemValid(slot, stack)) {
                return stack;
            }
            ItemStack existing = getStack(raw);
            int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
            if (!existing.isEmpty()) {
                if (!ItemStack.isSameItemSameComponents(existing, stack)) {
                    return stack;
                }
                limit -= existing.getCount();
            }
            if (limit <= 0) {
                return stack;
            }
            int insertCount = Math.min(limit, stack.getCount());
            if (!simulate) {
                if (existing.isEmpty()) {
                    setStack(raw, stack.copyWithCount(insertCount));
                } else {
                    existing.grow(insertCount);
                    setChanged();
                }
            }
            return insertCount == stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - insertCount);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            int raw = mapSlot(slot);
            if (raw < 0 || amount <= 0) {
                return ItemStack.EMPTY;
            }
            if (this.side == Direction.DOWN && raw == 1) {
                return ItemStack.EMPTY;
            }
            ItemStack existing = getStack(raw);
            if (existing.isEmpty()) {
                return ItemStack.EMPTY;
            }
            int take = Math.min(amount, existing.getCount());
            ItemStack out = existing.copyWithCount(take);
            if (!simulate) {
                existing.shrink(take);
                if (existing.isEmpty()) {
                    setStack(raw, ItemStack.EMPTY);
                } else {
                    setChanged();
                }
            }
            return out;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            int raw = mapSlot(slot);
            if (raw == 0) {
                return true;
            }
            if (raw == 1) {
                return isItemTool(stack);
            }
            return false;
        }
    }
}
