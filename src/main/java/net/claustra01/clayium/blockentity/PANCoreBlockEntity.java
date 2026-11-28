package net.claustra01.clayium.blockentity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.claustra01.clayium.block.CompressedClayBlock;
import net.claustra01.clayium.material.CMaterial;
import net.claustra01.clayium.material.CMaterials;
import net.claustra01.clayium.material.CShape;
import net.claustra01.clayium.pan.IPANComponent;
import net.claustra01.clayium.pan.IPANConversionProvider;
import net.claustra01.clayium.pan.PANConversion;
import net.claustra01.clayium.pan.UtilPAN;
import net.claustra01.clayium.recipe.ChemicalMetalSeparatorRecipe;
import net.claustra01.clayium.recipe.ChemicalMetalSeparatorRecipe.WeightedOutput;
import net.claustra01.clayium.recipe.CountedIngredient;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.registry.ClayiumBlocks;
import net.claustra01.clayium.registry.ClayiumItems;
import net.claustra01.clayium.registry.ClayiumRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * PAN Core (Tier 11): periodically scans connected {@code IPANConductor} graphs,
 * resolves transitive conversion trees from base materials/ores, computes minimum CE cost
 * and duplication CE consumption, and exposes duplicatable items to connected {@link IPANComponent}s.
 */
public class PANCoreBlockEntity extends ClayContainerBlockEntity implements IPANCoreAccess {
    public static final int REFRESH_RATE = 200;
    public static final int MAX_DEPTH = 1000;
    public static final int MAX_NUM = 10_000;

    public record PANItemCost(ItemStack stack, double cost, double consumption) {
        public PANItemCost {
            stack = stack.copyWithCount(1);
        }

        public boolean matchesType(ItemStack other) {
            return ItemStack.isSameItemSameComponents(this.stack, other);
        }
    }

    private static final CMaterial[] PROHIBITED_MATERIALS = {
            CMaterials.CLAY,
            CMaterials.DENSE_CLAY,
            CMaterials.IND_CLAY,
            CMaterials.ADVIND_CLAY,
            CMaterials.ANTIMATTER,
            CMaterials.OCTUPLE_CLAY,
            CMaterials.PURE_ANTIMATTER,
            CMaterials.COMPRESSED_PURE_ANTIMATTER_1,
            CMaterials.COMPRESSED_PURE_ANTIMATTER_2,
            CMaterials.COMPRESSED_PURE_ANTIMATTER_3,
            CMaterials.COMPRESSED_PURE_ANTIMATTER_4,
            CMaterials.COMPRESSED_PURE_ANTIMATTER_5,
            CMaterials.COMPRESSED_PURE_ANTIMATTER_6,
            CMaterials.COMPRESSED_PURE_ANTIMATTER_7,
            CMaterials.COMPRESSED_PURE_ANTIMATTER_8
    };

    private static final CShape[] PROHIBITED_SHAPES = {
            CShape.PLATE, CShape.STICK, CShape.SHORT_STICK, CShape.RING,
            CShape.SMALL_RING, CShape.GEAR, CShape.BLADE, CShape.NEEDLE,
            CShape.DISC, CShape.SMALL_DISC, CShape.CYLINDER, CShape.PIPE,
            CShape.LARGE_BALL, CShape.LARGE_PLATE, CShape.GRINDING_HEAD, CShape.BEARING,
            CShape.SPINDLE, CShape.CUTTING_HEAD, CShape.WATER_WHEEL, CShape.BLOCK,
            CShape.BALL, CShape.DUST, CShape.INGOT, CShape.GEM
    };

    private int refreshPhase;
    private long lastWorldTime = -1L;

    @Nullable
    private List<PANItemCost> conversionItemSet;
    @Nullable
    private List<PANItemCost> ingredientItemSet;
    @Nullable
    private List<PANItemCost> prohibitedItemSet;
    private List<PANConversion> conversionList = new ArrayList<>();
    private final Set<BlockPos> conductors = new HashSet<>();
    private int searchDepth;

    public PANCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.PAN_CORE.get(), pos, state, 1);
        this.refreshPhase = Math.floorMod(pos.hashCode(), REFRESH_RATE);
    }

    @Override
    protected void initParams() {
        super.initParams();
        this.items = NonNullList.withSize(1, ItemStack.EMPTY);
        this.insertRoutes = new int[]{ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, 0, ROUTE_DISABLED, ROUTE_DISABLED};
        this.extractRoutes = new int[]{ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED};
        this.autoInsert = true;
        this.autoExtract = true;
        this.maxAutoInsertDefault = 64;
        this.maxAutoExtractDefault = 64;
        this.autoInsertInterval = 1;
        this.autoExtractInterval = 1;
        this.slotsDrop = new int[]{0};
        this.listSlotsInsert.clear();
        this.listSlotsExtract.clear();
        this.listSlotsInsert.add(new int[]{0});
    }

    @Override
    public boolean acceptEnergyClay() {
        return false;
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        long gameTime = this.level.getGameTime();
        if (this.lastWorldTime != -1L) {
            this.refreshPhase -= (int) (gameTime - this.lastWorldTime);
            if (this.refreshPhase > REFRESH_RATE) {
                this.refreshPhase = REFRESH_RATE;
            }
        }
        this.lastWorldTime = gameTime;

        if (this.refreshPhase <= 0) {
            this.refreshPhase = REFRESH_RATE;
            refreshNetwork();
            if (this.conversionItemSet == null) {
                refreshItemSet();
            }
        }
    }

    public void refreshNetwork() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        this.conductors.clear();
        this.searchDepth = 0;
        this.conversionList = new ArrayList<>();
        search(this.worldPosition);

        List<BlockPos> sortedConductors = new ArrayList<>(this.conductors);
        sortedConductors.sort(distanceComparator(this.worldPosition));

        for (BlockPos conductorPos : sortedConductors) {
            BlockEntity be = this.level.getBlockEntity(conductorPos);
            if (be instanceof IPANConversionProvider provider) {
                List<PANConversion> provided = provider.getConversions();
                if (provided != null) {
                    this.conversionList.addAll(provided);
                }
            }
            if (be instanceof IPANComponent component) {
                component.setPANCore(this, REFRESH_RATE * 2);
            }
        }
    }

    private void search(BlockPos pos) {
        this.searchDepth++;
        this.conductors.add(pos.immutable());
        if (this.searchDepth < MAX_DEPTH && this.conductors.size() < MAX_NUM) {
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.relative(dir);
                if (!this.conductors.contains(neighbor) && UtilPAN.isPANConductor(this.level, neighbor)) {
                    search(neighbor);
                }
            }
        }
        this.searchDepth--;
    }

    public void refreshItemSet() {
        if (this.level != null && this.level.isClientSide) {
            return;
        }
        List<ChemicalMetalSeparatorRecipe> sepRecipes = List.of();
        if (this.level != null) {
            List<RecipeHolder<ChemicalMetalSeparatorRecipe>> holders =
                    this.level.getRecipeManager().getAllRecipesFor(ClayiumRecipeTypes.CHEMICAL_METAL_SEPARATOR.get());
            List<ChemicalMetalSeparatorRecipe> list = new ArrayList<>(holders.size());
            for (RecipeHolder<ChemicalMetalSeparatorRecipe> holder : holders) {
                list.add(holder.value());
            }
            sepRecipes = list;
        }
        refreshItemSet(sepRecipes);
    }

    /**
     * Resolves PAN costs and prohibited exclusions using the provided Chemical Metal Separator recipes
     * plus the active {@link #conversionList} collected by {@link #refreshNetwork()} (or injected for testing).
     */
    public void refreshItemSet(List<ChemicalMetalSeparatorRecipe> separatorRecipes) {
        List<PANItemCost> clays = new ArrayList<>();
        addUniqueItemCost(clays, new ItemStack(Blocks.CLAY), 1.0, 1.0);
        for (var holder : ClayiumBlocks.all().values()) {
            if (holder.get() instanceof CompressedClayBlock compressedClay) {
                double energy = Math.pow(10.0, compressedClay.getTier() + 1);
                addUniqueItemCost(clays, new ItemStack(compressedClay), energy, energy);
            }
        }

        List<PANItemCost> baseMaterials = new ArrayList<>();
        double saltEnergy = (double) (CobblestoneGeneratorBlockEntity.PROGRESS_MAX * SaltExtractorBlockEntity.ENERGY_PER_WORK / 6);
        Item saltDust = CMaterials.getItem(CMaterials.SALT, CShape.DUST);
        if (saltDust != Items.AIR) {
            addUniqueItemCost(baseMaterials, new ItemStack(saltDust), saltEnergy, saltEnergy);
        }
        Item antimatterGem = ClayiumItems.byPath("antimatter").get();
        addUniqueItemCost(baseMaterials, new ItemStack(antimatterGem), 100_000.0, 100_000.0);

        List<PANItemCost> prohibiteds = new ArrayList<>();
        for (CMaterial material : PROHIBITED_MATERIALS) {
            for (CShape shape : PROHIBITED_SHAPES) {
                Item item = CMaterials.getItem(material, shape);
                if (item != Items.AIR) {
                    addUniqueItemCost(prohibiteds, new ItemStack(item), 0.0, 0.0);
                }
            }
        }

        List<PANItemCost> impureDusts = new ArrayList<>();
        for (ChemicalMetalSeparatorRecipe recipe : separatorRecipes) {
            int weightSum = recipe.totalWeight();
            if (weightSum <= 0) {
                continue;
            }
            double base = (double) (recipe.energyPerTick() * recipe.durationTicks() + 1000L);
            for (WeightedOutput out : recipe.outputs()) {
                if (!out.stack().isEmpty() && out.weight() > 0) {
                    double rate = (double) out.weight() / (double) weightSum;
                    addUniqueItemCost(impureDusts, out.stack(), base / rate, base / rate);
                }
            }
        }

        List<PANItemCost> list = new ArrayList<>();
        addUniqueItemCost(list, new ItemStack(Blocks.COBBLESTONE), 1.0, 1.0);
        addUniqueItemCost(list, new ItemStack(Blocks.OAK_LOG), 1.0, 1.0);
        for (PANItemCost c : clays) {
            addUniqueItemCost(list, c.stack(), c.cost(), c.consumption());
        }
        for (PANItemCost d : impureDusts) {
            addUniqueItemCost(list, d.stack(), d.cost(), d.consumption());
        }
        for (PANItemCost b : baseMaterials) {
            addUniqueItemCost(list, b.stack(), b.cost(), b.consumption());
        }

        List<PANConversion> pending = new ArrayList<>(this.conversionList);
        boolean progress = true;
        while (progress) {
            progress = false;
            for (int i = 0; i < pending.size(); i++) {
                PANConversion conv = pending.get(i);
                double cost = 0.0;
                double consumption = 0.0;
                boolean resolved = true;

                for (CountedIngredient req : conv.inputs()) {
                    if (req.ingredient().isEmpty()) {
                        continue;
                    }
                    double minCost = 0.0;
                    double minConsumption = Double.MAX_VALUE;
                    int reqCount = Math.max(1, req.count());
                    for (PANItemCost known : list) {
                        if (req.ingredient().test(known.stack())) {
                            double candConsumption = known.consumption() * (double) reqCount;
                            if (candConsumption < minConsumption) {
                                minConsumption = candConsumption;
                                minCost = known.cost() * (double) reqCount;
                            }
                        }
                    }
                    if (minConsumption == Double.MAX_VALUE) {
                        resolved = false;
                        break;
                    }
                    cost += minCost;
                    consumption += minConsumption;
                }

                if (!resolved) {
                    continue;
                }

                consumption += conv.energy();
                for (ItemStack result : conv.results()) {
                    if (!result.isEmpty() && !containsItemType(list, result)) {
                        double denom = (double) Math.max(1, result.getCount());
                        list.add(new PANItemCost(result.copyWithCount(1), cost / denom, consumption / denom));
                    }
                }
                pending.remove(i);
                progress = true;
                break;
            }
        }

        sortByRegistryOrder(list);

        List<PANItemCost> allIngredients = List.copyOf(list);
        List<PANItemCost> allProhibited = new ArrayList<>(prohibiteds);
        for (PANItemCost clay : clays) {
            addUniqueItemCost(allProhibited, clay.stack(), clay.cost(), clay.consumption());
        }
        sortByRegistryOrder(allProhibited);

        List<PANItemCost> duplicatable = new ArrayList<>();
        for (PANItemCost item : allIngredients) {
            if (!containsItemType(allProhibited, item.stack())) {
                duplicatable.add(item);
            }
        }

        this.ingredientItemSet = allIngredients;
        this.prohibitedItemSet = List.copyOf(allProhibited);
        this.conversionItemSet = List.copyOf(duplicatable);
    }

    public void setConversionListForTest(List<PANConversion> conversions) {
        this.conversionList = new ArrayList<>(conversions);
    }

    public List<PANItemCost> getConversionItemList() {
        return this.conversionItemSet != null ? this.conversionItemSet : List.of();
    }

    public List<PANItemCost> getIngredientItemList() {
        return this.ingredientItemSet != null ? this.ingredientItemSet : List.of();
    }

    public List<PANItemCost> getProhibitedItemList() {
        return this.prohibitedItemSet != null ? this.prohibitedItemSet : List.of();
    }

    public boolean isProhibited(ItemStack stack) {
        return this.prohibitedItemSet != null && containsItemType(this.prohibitedItemSet, stack);
    }

    @Override
    public Set<PANConversionEntry> getConversionEntries() {
        if (this.conversionItemSet == null) {
            return Set.of();
        }
        Set<PANConversionEntry> entries = new LinkedHashSet<>(this.conversionItemSet.size());
        for (PANItemCost item : this.conversionItemSet) {
            entries.add(new PANConversionEntry(item.stack(), item.consumption()));
        }
        return entries;
    }

    private static void addUniqueItemCost(List<PANItemCost> target, ItemStack stack, double cost, double consumption) {
        if (!stack.isEmpty() && !containsItemType(target, stack)) {
            target.add(new PANItemCost(stack.copyWithCount(1), cost, consumption));
        }
    }

    private static boolean containsItemType(List<PANItemCost> list, ItemStack candidate) {
        for (PANItemCost entry : list) {
            if (entry.matchesType(candidate)) {
                return true;
            }
        }
        return false;
    }

    private static void sortByRegistryOrder(List<PANItemCost> list) {
        list.sort(Comparator.comparingInt(e -> BuiltInRegistries.ITEM.getId(e.stack().getItem())));
    }

    private static Comparator<BlockPos> distanceComparator(BlockPos center) {
        return (a, b) -> {
            double da = center.distSqr(a);
            double db = center.distSqr(b);
            int cmp = Double.compare(da, db);
            if (cmp != 0) {
                return cmp;
            }
            cmp = Integer.compare(a.getX(), b.getX());
            if (cmp != 0) {
                return cmp;
            }
            cmp = Integer.compare(a.getZ(), b.getZ());
            if (cmp != 0) {
                return cmp;
            }
            return Integer.compare(a.getY(), b.getY());
        };
    }
}
