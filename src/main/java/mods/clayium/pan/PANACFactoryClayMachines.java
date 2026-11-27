package mods.clayium.pan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mods.clayium.block.ClayEnergyLaser;
import mods.clayium.block.laser.ClayLaser;
import mods.clayium.block.tile.TileCAInjector;
import mods.clayium.block.tile.TileCAReactor;
import mods.clayium.block.tile.TileClayEnergyLaser;
import mods.clayium.block.tile.TileClayMachines;
import mods.clayium.block.tile.TileClayReactor;
import mods.clayium.util.UtilBuilder;
import mods.clayium.util.crafting.IItemPattern;
import mods.clayium.util.crafting.Recipes;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PANACFactoryClayMachines implements IPANAdapterConversionFactory {
    public PANACFactoryClayMachines() {
        super();
    }

    public boolean match(World world, int x, int y, int z) {
        return UtilBuilder.safeGetTileEntity(world, x, y, z) instanceof TileClayMachines;
    }

    public IPANConversion getConversion(IPANAdapter adapter) {
        boolean flag = true;

        for (ItemStack item : adapter.getPatternItems()) {
            if (item != null) {
                flag = false;
                break;
            }
        }

        if (flag) {
            return null;
        } else {
            TileEntity te = UtilBuilder.safeGetTileEntity(
                adapter.getConnectedWorld(), adapter.getConnectedXCoord(), adapter.getConnectedYCoord(), adapter.getConnectedZCoord()
            );
            if (te instanceof TileClayMachines) {
                Recipes recipe = Recipes.GetRecipes(((TileClayMachines)te).getRecipeId());
                if (recipe != null) {
                    List<ItemStack> ingred = new ArrayList<>();

                    for (ItemStack itemx : adapter.getPatternItems()) {
                        if (itemx != null) {
                            ingred.add(itemx);
                        }
                    }

                    Recipes.RecipeCondition condition = recipe.getRecipeConditionFromInventory(
                        ingred.toArray(new ItemStack[0]), 0, ((TileClayMachines)te).getRecipeTier()
                    );
                    Recipes.RecipeResult result = condition != null ? recipe.getRecipeResult(condition) : null;
                    if (result != null) {
                        List<IItemPattern> patterns = new ArrayList<>();

                        for (Object material : condition.getMaterials()) {
                            patterns.add(Recipes.convert(material));
                        }

                        List<ItemStack> results = new ArrayList<>(Arrays.asList(result.getResults()));
                        long energy = (long)((float)result.getEnergy() * ((TileClayMachines)te).multConsumingEnergy);
                        long time = (long)((float)result.getTime() * ((TileClayMachines)te).multCraftTime);
                        if (te instanceof TileClayReactor) {
                            int b = 0;
                            int g = 0;
                            int r = 0;

                            for (ItemStack itemxx : adapter.getSubItems()) {
                                if (itemxx != null && itemxx.func_77973_b() instanceof ItemBlock) {
                                    Block block = ((ItemBlock)itemxx.func_77973_b()).field_150939_a;
                                    if (block instanceof ClayEnergyLaser) {
                                        switch (((ClayEnergyLaser)block).getTier(itemxx)) {
                                            case 7:
                                                b += itemxx.field_77994_a;
                                                break;
                                            case 8:
                                                g += itemxx.field_77994_a;
                                                break;
                                            case 9:
                                                r += itemxx.field_77994_a;
                                        }
                                    }
                                }
                            }

                            long le = (long)(ClayLaser.getEnergy(b, g, r) + 1.0);
                            if (--time <= 0L) {
                                time = 1L;
                            }

                            time = time / le + 1L;
                            energy += (long)(TileClayEnergyLaser.consumingEnergyBlue * b);
                            energy += (long)(TileClayEnergyLaser.consumingEnergyGreen * g);
                            energy += (long)(TileClayEnergyLaser.consumingEnergyRed * r);
                        }

                        if (te instanceof TileCAReactor) {
                            if (((TileCAReactor)te).isConstructed() && ((TileCAReactor)te).getResultPureAntimatter() != null) {
                                energy = (long)((double)energy * ((TileCAReactor)te).getConsumingEnergyTotalMultiplier());
                                time = (long)((double)time * ((TileCAReactor)te).getCraftTimeTotalMultiplier());
                                results.clear();
                                results.add(((TileCAReactor)te).getResultPureAntimatter());
                            } else {
                                results.clear();
                            }
                        }

                        if (te instanceof TileCAInjector) {
                            time = (long)((double)time * ((TileCAInjector)te).getCraftTimeMultiplier());
                        }

                        if (time <= 0L) {
                            time = 1L;
                        }

                        if (results.size() > 0) {
                            return new PANConversion(patterns.toArray(new IItemPattern[0]), results.toArray(new ItemStack[0]), (double)energy * (double)time);
                        }
                    }
                }
            }

            return null;
        }
    }
}
