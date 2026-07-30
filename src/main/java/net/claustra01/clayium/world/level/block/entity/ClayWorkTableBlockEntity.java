/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.world.level.block.entity;

import javax.annotation.Nullable;
import net.claustra01.clayium.energy.ClayEnergyStorage;
import net.claustra01.clayium.machine.ClayiumMachineIds;
import net.claustra01.clayium.recipe.MachineRecipe;
import net.claustra01.clayium.recipe.MachineRecipeInput;
import net.claustra01.clayium.recipe.MachineRecipeLookup;
import net.claustra01.clayium.recipe.MachineRecipeTransaction;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Server-owned inventory and processing state for the Clay Work Table.
 *
 * <p>The Work Table is intentionally a distinct Clayium device. It does not
 * inherit the vanilla crafting table menu or recipe path.</p>
 */
public final class ClayWorkTableBlockEntity extends BaseContainerBlockEntity {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int SLOT_COUNT = 2;

    private static final String ACTIVE_RECIPE_KEY = "ActiveRecipe";
    private static final String PROGRESS_KEY = "Progress";

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final ClayEnergyStorage noEnergy = new ClayEnergyStorage(0, 0, 0);
    @Nullable
    private ResourceLocation activeRecipeId;
    private int progress;
    private int totalProgress;
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

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> totalProgress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = Math.max(0, value);
                case 1 -> totalProgress = Math.max(0, value);
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public ClayWorkTableBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumRegistries.CLAY_WORK_TABLE_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ClayWorkTableBlockEntity blockEntity) {
        blockEntity.tickServer(level);
    }

    public boolean startProcessing() {
        if (level == null || level.isClientSide || activeRecipeId != null) {
            return false;
        }
        return MachineRecipeLookup.find(level, ClayiumMachineIds.CLAY_WORK_TABLE, ClayTier.RAW, getItem(INPUT_SLOT))
                .filter(holder -> canProcess(holder.value()))
                .map(holder -> {
                    activeRecipeId = holder.id();
                    progress = 0;
                    totalProgress = holder.value().processingTimeTicks();
                    setChanged();
                    return true;
                })
                .orElse(false);
    }

    public IItemHandler externalItemHandler() {
        return externalItemHandler;
    }

    private void tickServer(Level level) {
        if (activeRecipeId == null) {
            return;
        }

        MachineRecipeInput input = new MachineRecipeInput(getItem(INPUT_SLOT));
        var recipe = level.getRecipeManager().getRecipeFor(
                ClayiumRecipes.MACHINE_RECIPE_TYPE.get(),
                input,
                level,
                activeRecipeId);
        if (recipe.isEmpty() || !isWorkTableRecipe(recipe.get())) {
            resetProcessing();
            return;
        }

        MachineRecipe machineRecipe = recipe.get().value();
        totalProgress = machineRecipe.processingTimeTicks();
        MachineRecipeTransaction.Result validation = MachineRecipeTransaction.execute(
                machineRecipe,
                getItem(INPUT_SLOT),
                getItem(OUTPUT_SLOT),
                ClayTier.RAW,
                noEnergy,
                true);
        if (!validation.successful()) {
            if (validation.failureReason() != MachineRecipeTransaction.FailureReason.OUTPUT_BLOCKED) {
                resetProcessing();
            }
            return;
        }

        if (++progress < totalProgress) {
            setChanged();
            return;
        }

        MachineRecipeTransaction.Result committed = MachineRecipeTransaction.execute(
                machineRecipe,
                getItem(INPUT_SLOT),
                getItem(OUTPUT_SLOT),
                ClayTier.RAW,
                noEnergy,
                false);
        if (committed.successful()) {
            items.set(INPUT_SLOT, committed.remainingInput());
            items.set(OUTPUT_SLOT, committed.resultingOutput());
        }
        resetProcessing();
    }

    private static boolean isWorkTableRecipe(RecipeHolder<MachineRecipe> holder) {
        return holder.value().machine().equals(ClayiumMachineIds.CLAY_WORK_TABLE);
    }

    private boolean canProcess(MachineRecipe recipe) {
        return MachineRecipeTransaction.execute(
                recipe,
                getItem(INPUT_SLOT),
                getItem(OUTPUT_SLOT),
                ClayTier.RAW,
                noEnergy,
                true).successful();
    }

    private void resetProcessing() {
        activeRecipeId = null;
        progress = 0;
        totalProgress = 0;
        setChanged();
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
            resetProcessing();
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
        progress = Math.max(0, tag.getInt(PROGRESS_KEY));
        totalProgress = 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        if (activeRecipeId != null) {
            tag.putString(ACTIVE_RECIPE_KEY, activeRecipeId.toString());
        }
        tag.putInt(PROGRESS_KEY, progress);
    }
}
