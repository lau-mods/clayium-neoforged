package mods.clayium.block.tile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import mods.clayium.block.CBlocks;
import mods.clayium.item.CMaterial;
import mods.clayium.item.CMaterials;
import mods.clayium.item.CShape;
import mods.clayium.pan.IPANComponent;
import mods.clayium.pan.IPANConversion;
import mods.clayium.pan.IPANConversionProvider;
import mods.clayium.pan.UtilPAN;
import mods.clayium.util.UtilBuilder;
import mods.clayium.util.UtilItemStack;
import mods.clayium.util.UtilLocale;
import mods.clayium.util.crafting.IItemPattern;
import mods.clayium.util.crafting.WeightedList;
import mods.clayium.util.crafting.WeightedResult;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class TilePANCore extends TileClayContainer {
    public static int refreshRate = 200;
    protected static Random random = new Random();
    protected int refreshPhase = 0;
    protected long lastTotalWorldTime = -1L;
    protected Set<TilePANCore.ItemStackWithEnergy> conversionItemSet;
    protected Set<TilePANCore.ItemStackWithEnergy> ingredientItemSet;
    protected Set<TilePANCore.ItemStackWithEnergy> prohibitedItemSet;
    public List<IPANConversion> conversionList;
    private Set<TilePANCore.Vec> conductors;
    private int depth;
    private int num;
    public static int maxDepth = 1000;
    public static int maxNum = 10000;

    public TilePANCore() {
        super();
        this.insertRoutes = new int[]{-1, -1, -1, 0, -1, -1};
        this.extractRoutes = new int[]{-1, -1, -1, -1, -1, -1};
        this.autoInsert = true;
        this.autoExtract = true;
        this.containerItemStacks = new ItemStack[1];
        this.maxAutoInsertDefault = this.maxAutoExtractDefault = 64;
        this.autoExtractInterval = this.autoInsertInterval = 1;
        this.listSlotsInsert.clear();
        this.listSlotsExtract.clear();
        this.listSlotsInsert.add(new int[]{0});
        this.refreshPhase = random.nextInt(refreshRate);
    }

    public Set<TilePANCore.ItemStackWithEnergy> getConversionItemSet() {
        return this.conversionItemSet;
    }

    public Set<TilePANCore.ItemStackWithEnergy> getIngredientItemSet() {
        return this.ingredientItemSet;
    }

    public Set<TilePANCore.ItemStackWithEnergy> getProhibitedItemSet() {
        return this.prohibitedItemSet;
    }

    public void func_145845_h() {
        super.func_145845_h();
        if (this.lastTotalWorldTime != -1L) {
            this.refreshPhase = this.refreshPhase - (int)(this.field_145850_b.func_82737_E() - this.lastTotalWorldTime);
            if (this.refreshPhase > refreshRate) {
                this.refreshPhase = refreshRate;
            }
        }

        if (this.field_145850_b != null) {
            this.lastTotalWorldTime = this.field_145850_b.func_82737_E();
        }

        if (this.refreshPhase <= 0) {
            this.refreshPhase = refreshRate;
            this.refreshNetwork();
            if (this.getConversionItemSet() == null) {
                this.refreshItemSet();
            }
        }
    }

    public void refreshNetwork() {
        if (!this.field_145850_b.field_72995_K) {
            TilePANCore.Vec v = TilePANCore.Vec.get(
                this.field_145851_c, this.field_145848_d, this.field_145849_e, this.field_145850_b.field_73011_w.field_76574_g
            );
            this.conductors = new TreeSet<>(new TilePANCore.DistanceComparator(v));
            this.depth = 0;
            this.num = 0;
            this.conversionList = new ArrayList<>();
            this.search(v);

            for (TilePANCore.Vec conductor : this.conductors) {
                TileEntity te = getTileEntity(conductor);
                if (te instanceof TilePANCore) {
                }

                if (te instanceof IPANConversionProvider) {
                    IPANConversion[] conversions = ((IPANConversionProvider)te).getConversion();
                    if (conversions != null) {
                        this.conversionList.addAll(Arrays.asList(conversions));
                    }
                }

                if (te instanceof IPANComponent) {
                    ((IPANComponent)te).setPANCore(this, refreshRate * 2);
                }
            }
        }
    }

    public void refreshItemSet() {
        if (!this.field_145850_b.field_72995_K) {
            Set<TilePANCore.ItemStackWithEnergy> clays = new TreeSet<>(new TilePANCore.ItemStackComparator());
            double d0 = 1.0;
            clays.add(new TilePANCore.ItemStackWithEnergy(new ItemStack(Blocks.field_150435_aG), d0, d0));

            for (int i = 0; i < 13; i++) {
                d0 *= 10.0;
                clays.add(new TilePANCore.ItemStackWithEnergy(new ItemStack(CBlocks.blockCompressedClay, 1, i), d0, d0));
            }

            Set<TilePANCore.ItemStackWithEnergy> basematerials = new TreeSet<>(new TilePANCore.ItemStackComparator());
            d0 = (double)(TileCobblestoneGenerator.progressMax * TileSaltExtractor.energyPerWork / 6);
            basematerials.add(new TilePANCore.ItemStackWithEnergy(CMaterials.get(CMaterials.SALT, CMaterials.DUST), d0, d0));
            basematerials.add(new TilePANCore.ItemStackWithEnergy(CMaterials.get(CMaterials.ANTIMATTER, CMaterials.GEM), 100000.0, 100000.0));
            Set<TilePANCore.ItemStackWithEnergy> prohibiteds = new TreeSet<>(new TilePANCore.ItemStackComparator());

            for (CMaterial material : new CMaterial[]{
                CMaterials.CLAY,
                CMaterials.DENSE_CLAY,
                CMaterials.IND_CLAY,
                CMaterials.ADVIND_CLAY,
                CMaterials.ANTIMATTER,
                CMaterials.OCTUPLE_CLAY,
                CMaterials.COMPRESSED_PURE_ANTIMATTER[0],
                CMaterials.COMPRESSED_PURE_ANTIMATTER[1],
                CMaterials.COMPRESSED_PURE_ANTIMATTER[2],
                CMaterials.COMPRESSED_PURE_ANTIMATTER[3],
                CMaterials.COMPRESSED_PURE_ANTIMATTER[4],
                CMaterials.COMPRESSED_PURE_ANTIMATTER[5],
                CMaterials.COMPRESSED_PURE_ANTIMATTER[6],
                CMaterials.COMPRESSED_PURE_ANTIMATTER[7],
                CMaterials.COMPRESSED_PURE_ANTIMATTER[8]
            }) {
                for (CShape shape : new CShape[]{
                    CMaterials.PLATE,
                    CMaterials.STICK,
                    CMaterials.SHORT_STICK,
                    CMaterials.RING,
                    CMaterials.SMALL_RING,
                    CMaterials.GEAR,
                    CMaterials.BLADE,
                    CMaterials.NEEDLE,
                    CMaterials.DISC,
                    CMaterials.SMALL_DISC,
                    CMaterials.CYLINDER,
                    CMaterials.PIPE,
                    CMaterials.LARGE_BALL,
                    CMaterials.LARGE_PLATE,
                    CMaterials.GRINDING_HEAD,
                    CMaterials.BEARING,
                    CMaterials.SPINDLE,
                    CMaterials.CUTTING_HEAD,
                    CMaterials.WATER_WHEEL,
                    CMaterials.BLOCK,
                    CMaterials.BALL,
                    CMaterials.DUST,
                    CMaterials.INGOT,
                    CMaterials.GEM
                }) {
                    if (CMaterials.exist(material, shape)) {
                        prohibiteds.add(new TilePANCore.ItemStackWithEnergy(CMaterials.get(material, shape), 0.0, 0.0));
                    }
                }
            }

            Set<TilePANCore.ItemStackWithEnergy> impuredusts = new TreeSet<>(new TilePANCore.ItemStackComparator());
            WeightedList<ItemStack> recipeResults = TileChemicalMetalSeparator.results;
            double base = (double)(TileChemicalMetalSeparator.baseConsumingEnergy * TileChemicalMetalSeparator.baseCraftTime + 1000);

            for (WeightedResult<ItemStack> result : recipeResults) {
                double rate = (double)result.weight / (double)recipeResults.getWeightSum();
                impuredusts.add(new TilePANCore.ItemStackWithEnergy(result.result, base / rate, base / rate));
            }

            Set<TilePANCore.ItemStackWithEnergy> list = new TreeSet<>(new TilePANCore.ItemStackComparator());
            list.add(new TilePANCore.ItemStackWithEnergy(null, 0.0, 0.0));
            list.add(new TilePANCore.ItemStackWithEnergy(new ItemStack(Blocks.field_150347_e), 1.0, 1.0));
            list.add(new TilePANCore.ItemStackWithEnergy(new ItemStack(Blocks.field_150364_r), 1.0, 1.0));
            list.addAll(clays);
            list.addAll(impuredusts);
            list.addAll(basematerials);
            boolean flag = true;

            while (flag) {
                flag = false;
                int i = 0;

                label80:
                while (i < this.conversionList.size()) {
                    double cost = 0.0;
                    double consumption = 0.0;
                    IItemPattern[] patterns = this.conversionList.get(i).getPatterns();

                    for (IItemPattern pattern : patterns) {
                        if (pattern != null) {
                            double mincost = 0.0;
                            double minconsumption = Double.MAX_VALUE;

                            for (TilePANCore.ItemStackWithEnergy ie : list) {
                                int stackSize = 1;
                                if (pattern.match(ie.itemstack, false)
                                    && minconsumption > ie.consumption * (double)(stackSize = pattern.getStackSize(ie.itemstack))) {
                                    minconsumption = ie.consumption * (double)stackSize;
                                    mincost = ie.cost * (double)stackSize;
                                }
                            }

                            if (minconsumption == Double.MAX_VALUE) {
                                i++;
                                continue label80;
                            }

                            cost += mincost;
                            consumption += minconsumption;
                        }
                    }

                    ItemStack[] results = this.conversionList.get(i).getResults();
                    consumption += this.conversionList.get(i).getEnergy();

                    label94:
                    for (ItemStack result : results) {
                        for (TilePANCore.ItemStackWithEnergy l : list) {
                            if (UtilItemStack.areTypeEqual(result, l.itemstack)) {
                                continue label94;
                            }
                        }

                        ItemStack r = result.func_77946_l();
                        r.field_77994_a = 1;
                        list.add(
                            new TilePANCore.ItemStackWithEnergy(
                                r, cost / (double)Math.max(result.field_77994_a, 1), consumption / (double)Math.max(result.field_77994_a, 1)
                            )
                        );
                    }

                    this.conversionList.remove(i);
                    flag = true;
                    break;
                }
            }

            Set<TilePANCore.ItemStackWithEnergy> list2 = new TreeSet<>(new TilePANCore.ItemStackComparator());
            list2.addAll(list);
            list.removeAll(clays);
            prohibiteds.addAll(clays);
            list.removeAll(prohibiteds);
            this.prohibitedItemSet = prohibiteds;
            this.ingredientItemSet = list2;
            this.conversionItemSet = list;
        }
    }

    public void debug(EntityPlayer player) {
        if (!player.field_70170_p.field_72995_K) {
            this.refreshNetwork();
            this.refreshItemSet();

            for (TilePANCore.ItemStackWithEnergy var3 : this.conversionItemSet) {
                ;
            }
        }
    }

    public void search(TilePANCore.Vec v) {
        this.depth++;
        this.num++;
        this.conductors.add(v);
        if (this.depth < maxDepth && this.num < maxNum) {
            for (TilePANCore.Vec u : getConnection(v)) {
                if (!this.conductors.contains(u)) {
                    this.search(u);
                }
            }
        }

        this.depth--;
    }

    public static TilePANCore.Vec[] getConnection(TilePANCore.Vec v) {
        List<TilePANCore.Vec> neighbors = new ArrayList<>(Arrays.asList(getNeighbor(v)));
        List<TilePANCore.Vec> res = new ArrayList<>();

        for (TilePANCore.Vec neighbor : neighbors) {
            if (isConductor(neighbor)) {
                res.add(neighbor);
            }
        }

        return res.toArray(new TilePANCore.Vec[0]);
    }

    public static boolean isConductor(TilePANCore.Vec v) {
        return UtilPAN.isPANConductor(DimensionManager.getWorld(v.d), v.x, v.y, v.z);
    }

    public static Block getBlock(TilePANCore.Vec v) {
        World world = DimensionManager.getWorld(v.d);
        return world == null ? null : world.func_147439_a(v.x, v.y, v.z);
    }

    public static TileEntity getTileEntity(TilePANCore.Vec v) {
        World world = DimensionManager.getWorld(v.d);
        return world == null ? null : UtilBuilder.safeGetTileEntity(world, v.x, v.y, v.z);
    }

    public static TilePANCore.Vec[] getNeighbor(TilePANCore.Vec v) {
        return new TilePANCore.Vec[]{
            TilePANCore.Vec.get(v.x + 1, v.y, v.z, v.d),
            TilePANCore.Vec.get(v.x - 1, v.y, v.z, v.d),
            TilePANCore.Vec.get(v.x, v.y + 1, v.z, v.d),
            TilePANCore.Vec.get(v.x, v.y - 1, v.z, v.d),
            TilePANCore.Vec.get(v.x, v.y, v.z + 1, v.d),
            TilePANCore.Vec.get(v.x, v.y, v.z - 1, v.d)
        };
    }

    public void func_70295_k_() {
    }

    public void func_70305_f() {
    }

    public static class DistanceComparator implements Comparator<TilePANCore.Vec> {
        protected TilePANCore.Vec center;

        public DistanceComparator(TilePANCore.Vec center) {
            super();
            this.center = center;
        }

        public int compare(TilePANCore.Vec o1, TilePANCore.Vec o2) {
            double d1 = this.dimDistance(o1);
            double d2 = this.dimDistance(o2);
            if (d1 < d2) {
                return -1;
            } else if (d1 > d2) {
                return 1;
            } else {
                d1 = this.coordDistance(o1);
                d2 = this.coordDistance(o2);
                if (d1 < d2) {
                    return -1;
                } else if (d1 > d2) {
                    return 1;
                } else if (o1.x < o2.x) {
                    return -1;
                } else if (o1.x > o2.x) {
                    return 1;
                } else if (o1.z < o2.z) {
                    return -1;
                } else if (o1.z > o2.z) {
                    return 1;
                } else if (o1.y < o2.y) {
                    return -1;
                } else {
                    return o1.y > o2.y ? 1 : 0;
                }
            }
        }

        protected double dimDistance(TilePANCore.Vec o1) {
            return o1.d >= this.center.d ? (double)(o1.d - this.center.d) : (double)(this.center.d - o1.d) - 0.5;
        }

        protected double coordDistance(TilePANCore.Vec o1) {
            double dx = (double)o1.x - (double)this.center.x;
            double dy = (double)o1.y - (double)this.center.y;
            double dz = (double)o1.z - (double)this.center.z;
            return dx * dx + dy * dy + dz * dz;
        }
    }

    public static class ItemStackComparator implements Comparator<TilePANCore.ItemStackWithEnergy> {
        public ItemStackComparator() {
            super();
        }

        public int compare(TilePANCore.ItemStackWithEnergy o1, TilePANCore.ItemStackWithEnergy o2) {
            ItemStack s1 = o1.itemstack;
            ItemStack s2 = o2.itemstack;
            if (s1 == null && s2 == null) {
                return 0;
            } else if (s1 == null) {
                return -1;
            } else if (s2 == null) {
                return 1;
            } else {
                int i1 = Item.func_150891_b(s1.func_77973_b());
                int i2 = Item.func_150891_b(s2.func_77973_b());
                if (i1 < i2) {
                    return -1;
                } else if (i1 > i2) {
                    return 1;
                } else if (s1.func_77960_j() < s2.func_77960_j()) {
                    return -1;
                } else {
                    return s1.func_77960_j() > s2.func_77960_j() ? 1 : 0;
                }
            }
        }
    }

    public static class ItemStackWithEnergy {
        public double cost;
        public double consumption;
        public ItemStack itemstack;

        public ItemStackWithEnergy(ItemStack itemstack, double cost, double consumption) {
            super();
            this.cost = cost;
            this.consumption = consumption;
            this.itemstack = itemstack != null ? itemstack.func_77946_l() : null;
        }

        public String toString() {
            return "[ "
                + (this.itemstack == null ? "null" : this.itemstack.func_82833_r())
                + " : "
                + UtilLocale.ClayEnergyNumeral(this.cost)
                + "CE : "
                + UtilLocale.ClayEnergyNumeral(this.consumption)
                + "CE ]";
        }
    }

    public static class Vec {
        public int x;
        public int y;
        public int z;
        public int d;

        private Vec(int x, int y, int z, int d) {
            super();
            this.x = x;
            this.y = y;
            this.z = z;
            this.d = d;
        }

        public static TilePANCore.Vec get(int x, int y, int z, int d) {
            return new TilePANCore.Vec(x, y, z, d);
        }

        public boolean equals(Object object) {
            return object instanceof TilePANCore.Vec
                ? this.x == ((TilePANCore.Vec)object).x
                    && this.y == ((TilePANCore.Vec)object).y
                    && this.z == ((TilePANCore.Vec)object).z
                    && this.d == ((TilePANCore.Vec)object).d
                : false;
        }

        public int hashCode() {
            return this.x + 23059 * this.y + 668243 * this.z + 9118819 * this.d;
        }

        public String toString() {
            return "(" + this.x + "," + this.y + "," + this.z + ";" + this.d + ")";
        }
    }
}
