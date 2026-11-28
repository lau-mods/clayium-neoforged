package net.claustra01.clayium.pan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.claustra01.clayium.block.ClayContainerBlock;
import net.claustra01.clayium.blockentity.AbstractClayMachineBlockEntity;
import net.claustra01.clayium.blockentity.CAInjectorBlockEntity;
import net.claustra01.clayium.blockentity.CAReactorBlockEntity;
import net.claustra01.clayium.blockentity.PANDuplicatorBlockEntity;
import net.claustra01.clayium.blockentity.ReactorBlockEntity;
import net.claustra01.clayium.laser.ClayLaser;
import net.claustra01.clayium.recipe.ClayMachineRecipe;
import net.claustra01.clayium.recipe.ClayMachineRecipeLookup;
import net.claustra01.clayium.recipe.ClayMachineRecipeLookup.PermutationMatch;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * PAN Adapter conversion factory for adjacent Clayium processing machines.
 * Mirrors 1.7.10 {@code PANACFactoryClayMachines} energy, craft-time, Laser, CA Reactor, and CA Injector scaling.
 */
public class PANClayMachinesFactory implements IPANAdapterConversionFactory {
    @Override
    public boolean matches(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof AbstractClayMachineBlockEntity machine
                && !(machine instanceof PANDuplicatorBlockEntity)
                && !machine.getRecipeCategoryId().isEmpty();
    }

    @Override
    @Nullable
    public PANConversion getConversion(IPANAdapter adapter) {
        Level level = adapter.getConnectedLevel();
        if (level == null) {
            return null;
        }
        BlockEntity be = level.getBlockEntity(adapter.getConnectedPos());
        if (!(be instanceof AbstractClayMachineBlockEntity machine) || machine instanceof PANDuplicatorBlockEntity) {
            return null;
        }
        String categoryId = machine.getRecipeCategoryId();
        if (categoryId == null || categoryId.isEmpty()) {
            return null;
        }

        List<ItemStack> ingredients = new ArrayList<>();
        for (ItemStack item : adapter.getPatternItems()) {
            if (!item.isEmpty()) {
                ingredients.add(item);
            }
        }
        if (ingredients.isEmpty()) {
            return null;
        }

        Collection<ClayMachineRecipe> recipes = machine.getAvailableRecipes();
        ClayMachineRecipe matched = findMatchingRecipe(recipes, ingredients, machine.getRecipeTier());
        if (matched == null) {
            return null;
        }

        List<ItemStack> results = new ArrayList<>();
        for (ItemStack res : matched.results()) {
            if (!res.isEmpty()) {
                results.add(res.copy());
            }
        }
        long energy = (long) ((float) matched.energyPerTick() * machine.multConsumingEnergy);
        long time = (long) ((float) matched.durationTicks() * machine.multCraftTime);

        if (machine instanceof ReactorBlockEntity) {
            int b = 0;
            int g = 0;
            int r = 0;
            for (ItemStack sub : adapter.getSubItems()) {
                if (!sub.isEmpty() && sub.getItem() instanceof BlockItem blockItem
                        && blockItem.getBlock() instanceof ClayContainerBlock cc
                        && "laser".equals(cc.getCategoryKey())) {
                    switch (cc.getTier()) {
                        case 7 -> b += sub.getCount();
                        case 8 -> g += sub.getCount();
                        case 9 -> r += sub.getCount();
                        default -> {}
                    }
                }
            }
            long le = (long) (ClayLaser.getEnergy(b, g, r) + 1.0);
            if (--time <= 0L) {
                time = 1L;
            }
            time = time / le + 1L;
            energy += 40_000L * (long) b + 400_000L * (long) g + 4_000_000L * (long) r;
        }

        if (machine instanceof CAReactorBlockEntity caReactor) {
            ItemStack pureAntimatter = caReactor.getResultPureAntimatter();
            if (caReactor.constructed && !pureAntimatter.isEmpty()) {
                energy = (long) ((double) energy * caReactor.getConsumingEnergyTotalMultiplier());
                time = (long) ((double) time * caReactor.getCraftTimeTotalMultiplier());
                results.clear();
                results.add(pureAntimatter);
            } else {
                results.clear();
            }
        }

        if (machine instanceof CAInjectorBlockEntity caInjector) {
            time = (long) ((double) time * caInjector.getCraftTimeMultiplier());
        }

        if (time <= 0L) {
            time = 1L;
        }
        if (results.isEmpty()) {
            return null;
        }
        return new PANConversion(matched.inputs(), results, (double) energy * (double) time);
    }

    @Nullable
    private static ClayMachineRecipe findMatchingRecipe(
            Collection<ClayMachineRecipe> recipes,
            List<ItemStack> ingredients,
            int recipeTier) {
        if (ingredients.size() == 1) {
            return ClayMachineRecipeLookup.findBestSingle(recipes, ingredients.getFirst(), recipeTier);
        }
        if (ingredients.size() == 2) {
            PermutationMatch match = ClayMachineRecipeLookup.findBestTwoInput(
                    recipes,
                    ingredients.get(0),
                    ingredients.get(1),
                    recipeTier,
                    r -> true
            );
            return match != null ? match.recipe() : null;
        }
        return null;
    }
}
