package net.claustra01.clayium.workbench;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeInput;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.RecipeRegistration;
import net.claustra01.clayium.registry.WorkbenchRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class ClayWorkTableBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INPUT_SLOT = 0;
    public static final int TOOL_SLOT = 1;
    public static final int FIRST_OUTPUT_SLOT = 2;
    public static final int SECOND_OUTPUT_SLOT = 3;
    private static final int RESERVED_INPUT_SLOT = 4;

    private final ItemStackHandler inventory = new ItemStackHandler(5) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == INPUT_SLOT || slot == TOOL_SLOT;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == TOOL_SLOT ? 1 : super.getSlotLimit(slot);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? progress : (int)Math.min(activeRecipeTicks(), Integer.MAX_VALUE);
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                progress = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };
    @Nullable private ResourceLocation activeRecipe;
    private int activeAction;
    private int progress;

    public ClayWorkTableBlockEntity(BlockPos pos, BlockState state) {
        super(WorkbenchRegistration.CLAY_WORK_TABLE_BLOCK_ENTITY.get(), pos, state);
    }

    public ItemStackHandler inventory() {
        return inventory;
    }

    public ContainerData menuData() {
        return menuData;
    }

    public boolean press(int action) {
        if (activeRecipe != null && findActiveRecipe(activeAction).isEmpty()) {
            ItemStack reserved = inventory.getStackInSlot(RESERVED_INPUT_SLOT);
            ItemStack input = inventory.getStackInSlot(INPUT_SLOT);
            if (input.isEmpty()) {
                inventory.setStackInSlot(INPUT_SLOT, reserved);
                inventory.setStackInSlot(RESERVED_INPUT_SLOT, ItemStack.EMPTY);
                clearActiveRecipe();
            } else if (ItemStack.isSameItemSameComponents(input, reserved)
                    && input.getCount() + reserved.getCount() <= input.getMaxStackSize()) {
                input.grow(reserved.getCount());
                inventory.setStackInSlot(RESERVED_INPUT_SLOT, ItemStack.EMPTY);
                clearActiveRecipe();
            } else {
                return false;
            }
        }
        Optional<RecipeHolder<ClayiumMachineRecipe>> recipe = activeRecipe == null
                ? findRecipe(inventory.getStackInSlot(INPUT_SLOT), action)
                : findActiveRecipe(action);
        if (recipe.isEmpty() || !hasRequiredTool(action)) {
            return false;
        }

        ClayiumMachineRecipe value = recipe.get().value();
        if (activeRecipe == null) {
            if (!canStoreOutputs(value.itemOutputs())) {
                return false;
            }
            ItemStack reserved = inventory.extractItem(
                    INPUT_SLOT, value.itemInputs().getFirst().count(), false);
            inventory.setStackInSlot(RESERVED_INPUT_SLOT, reserved);
            activeRecipe = recipe.get().id();
            activeAction = action;
            progress = 0;
        }

        if (progress < value.processingTicks()) {
            progress++;
        }
        if (progress < value.processingTicks()) {
            setChanged();
            return true;
        }
        if (!canStoreOutputs(value.itemOutputs())) {
            setChanged();
            return false;
        }

        for (ItemStack output : value.itemOutputs()) {
            insertOutput(output.copy());
        }
        inventory.setStackInSlot(RESERVED_INPUT_SLOT, ItemStack.EMPTY);
        damageTool(action);
        clearActiveRecipe();
        return true;
    }

    private Optional<RecipeHolder<ClayiumMachineRecipe>> findRecipe(ItemStack input, int action) {
        if (input.isEmpty()) {
            return Optional.empty();
        }
        ClayiumRecipeInput recipeInput = new ClayiumRecipeInput(
                List.of(input), List.<FluidStack>of(), action, 0L, worldPosition);
        return allWorkTableRecipes().stream()
                .filter(holder -> holder.value().requiredTier() == action)
                .filter(holder -> holder.value().matches(recipeInput, level))
                .findFirst();
    }

    private Optional<RecipeHolder<ClayiumMachineRecipe>> findActiveRecipe(int action) {
        if (action != activeAction) {
            return Optional.empty();
        }
        return level.getRecipeManager().byKey(activeRecipe)
                .filter(holder -> holder.value() instanceof ClayiumMachineRecipe)
                .map(holder -> new RecipeHolder<>(holder.id(), (ClayiumMachineRecipe) holder.value()))
                .filter(holder -> holder.value().processingMethod() == ClayiumRecipeMethod.CLAY_WORK_TABLE)
                .filter(holder -> holder.value().requiredTier() == action)
                .filter(holder -> holder.value().matches(new ClayiumRecipeInput(
                        List.of(inventory.getStackInSlot(RESERVED_INPUT_SLOT)), List.of(), action, 0L,
                        worldPosition), level));
    }

    private List<RecipeHolder<ClayiumMachineRecipe>> allWorkTableRecipes() {
        return level.getRecipeManager().getAllRecipesFor(
                RecipeRegistration.type(ClayiumRecipeMethod.CLAY_WORK_TABLE));
    }

    private long activeRecipeTicks() {
        if (level == null || activeRecipe == null) {
            return 0;
        }
        return level.getRecipeManager().byKey(activeRecipe).stream()
                .filter(holder -> holder.value() instanceof ClayiumMachineRecipe)
                .map(holder -> (ClayiumMachineRecipe) holder.value())
                .map(ClayiumMachineRecipe::processingTicks)
                .findFirst()
                .orElse(0L);
    }

    private boolean hasRequiredTool(int action) {
        ItemStack stack = inventory.getStackInSlot(TOOL_SLOT);
        return switch (action) {
            case 1, 2 -> true;
            case 3 -> stack.is(WorkbenchRegistration.CLAY_ROLLING_PIN.get());
            case 4, 6 -> stack.is(WorkbenchRegistration.CLAY_SLICER.get())
                    || stack.is(WorkbenchRegistration.CLAY_SPATULA.get());
            case 5 -> stack.is(WorkbenchRegistration.CLAY_SPATULA.get());
            default -> false;
        };
    }

    private void damageTool(int action) {
        if (action <= 2) {
            return;
        }
        ItemStack tool = inventory.getStackInSlot(TOOL_SLOT);
        tool.hurtAndBreak(1, (ServerLevel) level, null, item -> {});
    }

    private boolean canStoreOutputs(List<ItemStack> outputs) {
        ItemStackHandler simulation = new ItemStackHandler(2);
        simulation.setStackInSlot(0, inventory.getStackInSlot(FIRST_OUTPUT_SLOT).copy());
        simulation.setStackInSlot(1, inventory.getStackInSlot(SECOND_OUTPUT_SLOT).copy());
        for (ItemStack output : outputs) {
            ItemStack remaining = simulation.insertItem(0, output.copy(), false);
            remaining = simulation.insertItem(1, remaining, false);
            if (!remaining.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void insertOutput(ItemStack output) {
        ItemStack remaining = mergeOutput(FIRST_OUTPUT_SLOT, output);
        mergeOutput(SECOND_OUTPUT_SLOT, remaining);
    }

    private ItemStack mergeOutput(int slot, ItemStack output) {
        if (output.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack stored = inventory.getStackInSlot(slot);
        if (stored.isEmpty()) {
            inventory.setStackInSlot(slot, output);
            return ItemStack.EMPTY;
        }
        if (!ItemStack.isSameItemSameComponents(stored, output)) {
            return output;
        }
        int inserted = Math.min(output.getCount(), stored.getMaxStackSize() - stored.getCount());
        stored.grow(inserted);
        output.shrink(inserted);
        return output;
    }

    private void clearActiveRecipe() {
        activeRecipe = null;
        activeAction = 0;
        progress = 0;
        setChanged();
    }

    public void dropContents() {
        if (level == null) {
            return;
        }
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            Containers.dropItemStack(
                    level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                    inventory.getStackInSlot(slot));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        if (activeRecipe != null) {
            tag.putString("active_recipe", activeRecipe.toString());
        }
        tag.putInt("active_action", activeAction);
        tag.putInt("progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        activeRecipe = tag.contains("active_recipe") ? ResourceLocation.parse(tag.getString("active_recipe")) : null;
        activeAction = tag.getInt("active_action");
        progress = tag.getInt("progress");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.clayium.clay_work_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ClayWorkTableMenu(containerId, playerInventory, this);
    }
}
