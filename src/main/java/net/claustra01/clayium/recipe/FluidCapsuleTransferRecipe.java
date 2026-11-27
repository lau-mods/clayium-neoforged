package net.claustra01.clayium.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.claustra01.clayium.item.FluidCapsuleItem;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.registry.ClayiumItems;
import net.claustra01.clayium.registry.ClayiumRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

/**
 * Dynamic fluid capsule compression/decompression recipe chain: 1 <-> 5 <-> 25 <-> 125 <-> 1000 mB,
 * preserving matching fluid identity via FLUID_CONTENT DataComponent.
 */
public class FluidCapsuleTransferRecipe extends CustomRecipe {
    private static final String[] CHAIN_IDS = {
            "fluid_capsule_1",
            "fluid_capsule_5",
            "fluid_capsule_25",
            "fluid_capsule_125",
            "fluid_capsule_1000"
    };
    private static final int[] RATIOS = {5, 5, 5, 8};

    public FluidCapsuleTransferRecipe(CraftingBookCategory category) {
        super(category);
    }

    private static int indexOfCapsule(Item item) {
        for (int i = 0; i < CHAIN_IDS.length; i++) {
            if (ClayiumItems.byPath(CHAIN_IDS[i]).get() == item) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !assemble(input, level.registryAccess()).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        List<ItemStack> nonEmpty = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            ItemStack s = input.getItem(i);
            if (!s.isEmpty()) {
                nonEmpty.add(s);
            }
        }
        if (nonEmpty.isEmpty()) {
            return ItemStack.EMPTY;
        }

        Item firstItem = nonEmpty.getFirst().getItem();
        int idx = indexOfCapsule(firstItem);
        if (idx < 0 || !(firstItem instanceof FluidCapsuleItem firstCapsule)) {
            return ItemStack.EMPTY;
        }
        SimpleFluidContent content = nonEmpty.getFirst().get(ClayiumDataComponents.FLUID_CONTENT.get());

        for (int i = 1; i < nonEmpty.size(); i++) {
            ItemStack s = nonEmpty.get(i);
            if (s.getItem() != firstItem) {
                return ItemStack.EMPTY;
            }
            if (!Objects.equals(content, s.get(ClayiumDataComponents.FLUID_CONTENT.get()))) {
                return ItemStack.EMPTY;
            }
        }

        int count = nonEmpty.size();
        // Decompress 1 larger capsule -> N smaller capsules
        if (count == 1 && idx > 0) {
            Item smaller = ClayiumItems.byPath(CHAIN_IDS[idx - 1]).get();
            ItemStack out = new ItemStack(smaller, RATIOS[idx - 1]);
            copyScaledContent(content, out, ((FluidCapsuleItem) smaller).getCapacityMb());
            return out;
        }
        // Compress N smaller capsules -> 1 larger capsule
        if (idx < RATIOS.length && count == RATIOS[idx]) {
            Item larger = ClayiumItems.byPath(CHAIN_IDS[idx + 1]).get();
            ItemStack out = new ItemStack(larger, 1);
            copyScaledContent(content, out, ((FluidCapsuleItem) larger).getCapacityMb());
            return out;
        }
        return ItemStack.EMPTY;
    }

    private static void copyScaledContent(SimpleFluidContent source, ItemStack dest, int newAmountMb) {
        if (source == null || source.isEmpty()) {
            return;
        }
        var copy = source.copy();
        copy.setAmount(newAmountMb);
        dest.set(ClayiumDataComponents.FLUID_CONTENT.get(), SimpleFluidContent.copyOf(copy));
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ClayiumRecipeSerializers.FLUID_CAPSULE_TRANSFER.get();
    }
}
