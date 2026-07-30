/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.world.level.block.entity;

import java.util.Optional;
import javax.annotation.Nullable;
import net.claustra01.clayium.recipe.ClayWorkTableOperation;
import net.claustra01.clayium.recipe.ClayWorkTableRecipe;
import net.claustra01.clayium.recipe.MachineRecipeInput;
import net.claustra01.clayium.registry.ClayiumRecipes;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.tier.ClayTier;
import net.claustra01.clayium.world.inventory.ClayWorkTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Persistent state for the original-style, button-driven Clay Work Table.
 *
 * <p>Manual recipes progress once per validated button press. They never
 * advance from a server tick.</p>
 */
public final class ClayWorkTableBlockEntity extends BaseContainerBlockEntity {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int SLOT_COUNT = 2;

    private static final String ACTIVE_RECIPE_KEY = "ActiveRecipe";
    private static final String ACTIVE_OPERATION_KEY = "ActiveOperation";
    private static final String PROGRESS_KEY = "Progress";
    private static final String REQUIRED_ACTIONS_KEY = "RequiredActions";

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    @Nullable
    private ResourceLocation activeRecipeId;
    @Nullable
    private ClayWorkTableOperation activeOperation;
    private int progress;
    private int requiredActions;

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> requiredActions;
                case 2 -> activeOperation == null ? 0 : activeOperation.buttonId();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = Math.max(0, value);
                case 1 -> requiredActions = Math.max(0, value);
                case 2 -> activeOperation = ClayWorkTableOperation.byButtonId(value).orElse(null);
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    private final IItemHandler externalItemHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return SLOT_COUNT;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            validateSlot(slot);
            return getItem(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            validateSlot(slot);
            if (slot != INPUT_SLOT || stack.isEmpty()) {
                return stack;
            }
            ItemStack existing = getItem(INPUT_SLOT);
            if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, stack)) {
                return stack;
            }
            int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
            int accepted = Math.min(stack.getCount(), limit - existing.getCount());
            if (accepted <= 0) {
                return stack;
            }
            if (!simulate) {
                ItemStack next = existing.isEmpty() ? stack.copyWithCount(accepted) : existing.copy();
                if (!existing.isEmpty()) {
                    next.grow(accepted);
                }
                setItem(INPUT_SLOT, next);
            }
            return accepted == stack.getCount()
                    ? ItemStack.EMPTY
                    : stack.copyWithCount(stack.getCount() - accepted);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            validateSlot(slot);
            if (slot != OUTPUT_SLOT || amount <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack output = getItem(OUTPUT_SLOT);
            int extracted = Math.min(amount, output.getCount());
            if (extracted <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack result = output.copyWithCount(extracted);
            if (!simulate) {
                removeItem(OUTPUT_SLOT, extracted);
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            validateSlot(slot);
            return getMaxStackSize();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            validateSlot(slot);
            return slot == INPUT_SLOT && canPlaceItem(slot, stack);
        }

        private void validateSlot(int slot) {
            if (slot < 0 || slot >= SLOT_COUNT) {
                throw new IndexOutOfBoundsException("Clay Work Table slot: " + slot);
            }
        }
    };

    public ClayWorkTableBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumRegistries.CLAY_WORK_TABLE_BLOCK_ENTITY.get(), pos, state);
    }

    public boolean pushOperation(int buttonId) {
        if (level == null || level.isClientSide) {
            return false;
        }
        Optional<ClayWorkTableOperation> operation = ClayWorkTableOperation.byButtonId(buttonId);
        if (operation.isEmpty()) {
            return false;
        }

        RecipeHolder<ClayWorkTableRecipe> holder;
        if (activeRecipeId == null) {
            Optional<RecipeHolder<ClayWorkTableRecipe>> found = findRecipe(operation.get());
            if (found.isEmpty() || !canOutput(found.get().value().result())) {
                return false;
            }
            holder = found.get();
            activeRecipeId = holder.id();
            activeOperation = operation.get();
            progress = 0;
            requiredActions = holder.value().requiredActions();
        } else {
            Optional<RecipeHolder<ClayWorkTableRecipe>> resolved = resolveActiveRecipe();
            if (resolved.isEmpty()
                    || activeOperation != operation.get()
                    || !canOutput(resolved.get().value().result())) {
                return false;
            }
            holder = resolved.get();
            requiredActions = holder.value().requiredActions();
        }

        progress++;
        if (progress >= requiredActions) {
            complete(holder.value());
        }
        setChanged();
        return true;
    }

    private Optional<RecipeHolder<ClayWorkTableRecipe>> findRecipe(ClayWorkTableOperation operation) {
        MachineRecipeInput input = new MachineRecipeInput(getItem(INPUT_SLOT));
        return level.getRecipeManager()
                .getAllRecipesFor(ClayiumRecipes.CLAY_WORK_TABLE_RECIPE_TYPE.get())
                .stream()
                .filter(holder -> holder.value().operation() == operation)
                .filter(holder -> ClayTier.RAW.isAtLeast(holder.value().minimumTier()))
                .filter(holder -> holder.value().matches(input, level))
                .findFirst();
    }

    private Optional<RecipeHolder<ClayWorkTableRecipe>> resolveActiveRecipe() {
        if (activeRecipeId == null || level == null) {
            return Optional.empty();
        }
        return level.getRecipeManager().getRecipeFor(
                ClayiumRecipes.CLAY_WORK_TABLE_RECIPE_TYPE.get(),
                new MachineRecipeInput(getItem(INPUT_SLOT)),
                level,
                activeRecipeId);
    }

    private void complete(ClayWorkTableRecipe recipe) {
        ItemStack input = getItem(INPUT_SLOT);
        if (input.isEmpty() || !recipe.ingredient().test(input) || !canOutput(recipe.result())) {
            resetProgress();
            return;
        }

        ItemStack output = getItem(OUTPUT_SLOT);
        ItemStack result = recipe.result();
        if (output.isEmpty()) {
            items.set(OUTPUT_SLOT, result);
        } else {
            output.grow(result.getCount());
        }
        input.shrink(1);
        resetProgress();
    }

    private boolean canOutput(ItemStack result) {
        ItemStack output = getItem(OUTPUT_SLOT);
        return output.isEmpty()
                || ItemStack.isSameItemSameComponents(output, result)
                && result.getCount() <= output.getMaxStackSize() - output.getCount();
    }

    private void resetProgress() {
        activeRecipeId = null;
        activeOperation = null;
        progress = 0;
        requiredActions = 0;
    }

    public IItemHandler externalItemHandler() {
        return externalItemHandler;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.clayium_neoforged.clay_work_table");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new ClayWorkTableMenu(containerId, inventory, this, menuData);
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == INPUT_SLOT;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
        if (slot == INPUT_SLOT) {
            resetProgress();
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        activeRecipeId = tag.contains(ACTIVE_RECIPE_KEY)
                ? ResourceLocation.tryParse(tag.getString(ACTIVE_RECIPE_KEY))
                : null;
        activeOperation = tag.contains(ACTIVE_OPERATION_KEY)
                ? ClayWorkTableOperation.byId(tag.getString(ACTIVE_OPERATION_KEY)).orElse(null)
                : null;
        progress = Math.max(0, tag.getInt(PROGRESS_KEY));
        requiredActions = Math.max(0, tag.getInt(REQUIRED_ACTIONS_KEY));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        if (activeRecipeId != null) {
            tag.putString(ACTIVE_RECIPE_KEY, activeRecipeId.toString());
        }
        if (activeOperation != null) {
            tag.putString(ACTIVE_OPERATION_KEY, activeOperation.id());
        }
        tag.putInt(PROGRESS_KEY, progress);
        tag.putInt(REQUIRED_ACTIONS_KEY, requiredActions);
    }
}
