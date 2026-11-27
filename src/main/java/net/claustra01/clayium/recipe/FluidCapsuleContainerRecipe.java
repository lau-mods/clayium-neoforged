package net.claustra01.clayium.recipe;

import net.claustra01.clayium.item.FluidCapsuleItem;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.registry.ClayiumRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/**
 * Bidirectional fluid exchange between FluidCapsuleItem and generic NeoForge IFluidHandlerItem containers
 * (porting 1.7.10 ItemCapsule.RecipeCapsulePackaging and RecipeCapsuleUnpackaging).
 */
public class FluidCapsuleContainerRecipe extends CustomRecipe {
    public FluidCapsuleContainerRecipe(CraftingBookCategory category) {
        super(category);
    }

    private record Match(
            boolean packaging,
            int capsuleSlot,
            int containerSlot,
            FluidCapsuleItem capsule,
            ItemStack capsuleStack,
            ItemStack containerStack) {}

    private static Match findMatch(CraftingInput input) {
        int capsuleSlot = -1;
        int containerSlot = -1;
        ItemStack capsuleStack = ItemStack.EMPTY;
        ItemStack containerStack = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            ItemStack s = input.getItem(i);
            if (s.isEmpty()) {
                continue;
            }
            if (s.getItem() instanceof FluidCapsuleItem) {
                if (capsuleSlot >= 0) {
                    return null;
                }
                capsuleSlot = i;
                capsuleStack = s;
            } else {
                if (containerSlot >= 0) {
                    return null;
                }
                containerSlot = i;
                containerStack = s;
            }
        }
        if (capsuleSlot < 0 || containerSlot < 0) {
            return null;
        }
        FluidCapsuleItem capsule = (FluidCapsuleItem) capsuleStack.getItem();
        int cap = capsule.getCapacityMb();
        SimpleFluidContent content = capsuleStack.get(ClayiumDataComponents.FLUID_CONTENT.get());
        boolean capsuleEmpty = content == null || content.isEmpty();

        IFluidHandlerItem handler = FluidUtil.getFluidHandler(containerStack.copyWithCount(1)).orElse(null);
        if (handler == null) {
            return null;
        }

        if (capsuleEmpty) {
            // Packaging: drain exactly capsule capacity from filled external container
            FluidStack drained = handler.drain(cap, FluidAction.SIMULATE);
            if (drained.isEmpty() || drained.getAmount() != cap) {
                return null;
            }
            return new Match(true, capsuleSlot, containerSlot, capsule, capsuleStack, containerStack);
        } else {
            // Unpackaging: fill an empty exact-capacity container from filled capsule
            if (content.getAmount() != cap || handler.getTanks() <= 0) {
                return null;
            }
            if (handler.getTankCapacity(0) != cap || !handler.getFluidInTank(0).isEmpty()) {
                return null;
            }
            FluidStack toFill = content.copy();
            int accepted = handler.fill(toFill, FluidAction.SIMULATE);
            if (accepted != cap) {
                return null;
            }
            return new Match(false, capsuleSlot, containerSlot, capsule, capsuleStack, containerStack);
        }
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return findMatch(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        Match match = findMatch(input);
        if (match == null) {
            return ItemStack.EMPTY;
        }
        int cap = match.capsule().getCapacityMb();
        IFluidHandlerItem handler = FluidUtil.getFluidHandler(match.containerStack().copyWithCount(1)).orElse(null);
        if (handler == null) {
            return ItemStack.EMPTY;
        }
        if (match.packaging()) {
            FluidStack drained = handler.drain(cap, FluidAction.EXECUTE);
            if (drained.isEmpty() || drained.getAmount() != cap) {
                return ItemStack.EMPTY;
            }
            ItemStack out = new ItemStack(match.capsule(), 1);
            out.set(ClayiumDataComponents.FLUID_CONTENT.get(), SimpleFluidContent.copyOf(drained));
            return out;
        } else {
            SimpleFluidContent content = match.capsuleStack().get(ClayiumDataComponents.FLUID_CONTENT.get());
            if (content == null || content.isEmpty()) {
                return ItemStack.EMPTY;
            }
            int filled = handler.fill(content.copy(), FluidAction.EXECUTE);
            return filled == cap ? handler.getContainer() : ItemStack.EMPTY;
        }
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        Match match = findMatch(input);
        if (match == null || !match.packaging()) {
            return remaining;
        }
        IFluidHandlerItem handler = FluidUtil.getFluidHandler(match.containerStack().copyWithCount(1)).orElse(null);
        if (handler != null) {
            handler.drain(match.capsule().getCapacityMb(), FluidAction.EXECUTE);
            remaining.set(match.containerSlot(), handler.getContainer());
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ClayiumRecipeSerializers.FLUID_CAPSULE_CONTAINER.get();
    }
}
