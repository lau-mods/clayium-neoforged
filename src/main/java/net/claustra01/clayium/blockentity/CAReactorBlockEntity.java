package net.claustra01.clayium.blockentity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.claustra01.clayium.energy.ClayEnergyUpgradeMath;
import net.claustra01.clayium.recipe.ClayMachineRecipe;
import net.claustra01.clayium.recipe.ClayMachineRecipeLookup;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.registry.ClayiumItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * CA Reactor Core (T10..T13 multiblock):
 * validates closed loop of up to 128 CA Reactor Coils, enclosed by >= 50 CA Reactor Hulls / Interfaces,
 * and compresses Antimatter into Pure Antimatter (rank 0..8) scaled by hull count and average rank.
 */
public class CAReactorBlockEntity extends AbstractMultiblockMachineBlockEntity {
    public static final int REACTOR_HULL_MIN_NUM = 50;
    public static final int MAX_COIL_LENGTH = 128;
    public static final double BASE_EFFICIENCY = 0.2;
    public static final double EFFICIENCY_PER_NUM = 1.02;
    public static final double EFFICIENCY_PER_RANK = 7.5;
    public static final double ENERGY_BASE = 1.01;

    private static final int[][] ORTHOGONAL_OFFSETS = {
            {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}
    };
    private static final int[][] COIL_WALK_OFFSETS = {
            {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1},
            {1, 1, 0}, {1, -1, 0}, {-1, 1, 0}, {-1, -1, 0},
            {1, 0, 1}, {1, 0, -1}, {-1, 0, 1}, {-1, 0, -1},
            {0, 1, 1}, {0, 1, -1}, {0, -1, 1}, {0, -1, -1}
    };

    private static final String[] PURE_ANTIMATTER_ITEM_PATHS = {
            "pure_antimatter",
            "pure_antimatter_1",
            "pure_antimatter_2",
            "pure_antimatter_3",
            "pure_antimatter_4",
            "pure_antimatter_5",
            "pure_antimatter_6",
            "pure_antimatter_7",
            "octuple_pure_antimatter"
    };

    public int reactorTier;
    public double reactorRank;
    public int reactorHullNum;
    public int reactorHullMaxRank;
    @Nullable
    public List<int[]> coilCoords;
    @Nullable
    public List<int[]> hullCoords;
    public String errorMessage;

    public CAReactorBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.CA_REACTOR.get(), pos, state);
        ensureTierInitialized();
    }

    @Override
    protected void initParams() {
        this.reactorTier = 10;
        this.reactorRank = 0.0;
        this.reactorHullNum = 0;
        this.reactorHullMaxRank = 1;
        this.errorMessage = "";
        this.items = NonNullList.withSize(4, ItemStack.EMPTY);
        this.clayEnergySlot = 3;
        this.listSlotsInsert.clear();
        this.listSlotsExtract.clear();
        this.listSlotsInsert.add(new int[]{0});
        this.listSlotsInsert.add(new int[]{3});
        this.listSlotsExtract.add(new int[]{1});
        this.insertRoutes = new int[]{-1, 0, -1, 1, -1, -1};
        this.maxAutoExtract = new int[]{-1, 1};
        this.extractRoutes = new int[]{0, -1, -1, -1, -1, -1};
        this.maxAutoInsert = new int[]{-1};
        this.slotsDrop = new int[]{0, 1, 3};
        this.autoInsert = true;
        this.autoExtract = true;
    }

    @Override
    protected void initParamsByTier(int tier) {
        setDefaultTransportationParamsByTier(tier, ParamMode.MACHINE);
        this.recipeCategoryId = "ca_reactor";
        this.reactorTier = tier;
        this.reactorHullMaxRank = switch (tier) {
            case 10 -> 1;
            case 11 -> 5;
            default -> 9;
        };
    }

    @Override
    public boolean canOverclock() {
        return this.reactorTier >= 13;
    }

    public double getEfficiency() {
        return ClayEnergyUpgradeMath.caReactorEfficiency(this.reactorHullNum, this.reactorRank);
    }

    public int getPureAntimatterRank() {
        return Math.min(Math.max(0, (int) this.reactorRank), PURE_ANTIMATTER_ITEM_PATHS.length - 1);
    }

    public double getCraftTimePureAntimatterMultiplier() {
        return ClayEnergyUpgradeMath.caReactorPureAntimatterTimeMultiplier(getPureAntimatterRank());
    }

    public double getConsumingEnergyBaseMultiplier() {
        return ClayEnergyUpgradeMath.caReactorConsumingEnergyMultiplier(this.reactorHullNum, this.reactorRank);
    }

    public double getConsumingEnergyTotalMultiplier() {
        return getConsumingEnergyBaseMultiplier() * (double) this.multConsumingEnergy;
    }

    public double getCraftTimeTotalMultiplier() {
        return getCraftTimePureAntimatterMultiplier() / getEfficiency();
    }

    public ItemStack getResultPureAntimatter() {
        int rank = getPureAntimatterRank();
        Item item = ClayiumItems.byPath(PURE_ANTIMATTER_ITEM_PATHS[rank]).get();
        return new ItemStack(item, 1);
    }

    public static int resolveCoilTier(Block block) {
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        return switch (path) {
            case "antimatter_ca_reactor_coil" -> 10;
            case "pure_antimatter_ca_reactor_coil" -> 11;
            case "oec_ca_reactor_coil" -> 12;
            case "opa_ca_reactor_coil" -> 13;
            default -> -1;
        };
    }

    public static int resolveHullRank(Block block) {
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        if (!path.startsWith("ca_reactor_hull_rank_")) {
            return -1;
        }
        try {
            return Integer.parseInt(path.substring("ca_reactor_hull_rank_".length())) - 1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public boolean checkStructureConstructed() {
        this.errorMessage = "";
        this.reactorHullNum = 0;
        this.reactorRank = 0.0;
        boolean res = true;

        if (this.coilCoords == null || this.coilCoords.isEmpty()) {
            this.coilCoords = new ArrayList<>();
            int xx0 = 0;
            int yy0 = 0;
            int zz0 = 0;
            int xx1 = 0;
            int yy1 = 0;
            int zz1 = 0;
            int[][] currentOffsets = {{0, 0, 1}};
            int step = 0;

            while (true) {
                int[] nextOffset = null;
                int count = 0;
                for (int[] offset : currentOffsets) {
                    Block block = getRelativeBlock(xx1 + offset[0], yy1 + offset[1], zz1 + offset[2]);
                    if (block != null && resolveCoilTier(block) >= 0) {
                        if (xx0 != xx1 + offset[0] || yy0 != yy1 + offset[1] || zz0 != zz1 + offset[2]) {
                            nextOffset = offset;
                        }
                        count++;
                    }
                }
                if (count > 2 || nextOffset == null || step == MAX_COIL_LENGTH + 1) {
                    BlockPos rel = getRelativePos(xx1, yy1, zz1);
                    if (step == MAX_COIL_LENGTH + 1) {
                        this.errorMessage = "The number of coils must not be higher than " + MAX_COIL_LENGTH + ".";
                    } else {
                        this.errorMessage = "Coil [" + rel.getX() + "," + rel.getY() + "," + rel.getZ() + "] is invalid.";
                    }
                    res = false;
                    this.coilCoords = null;
                    break;
                }
                xx0 = xx1;
                yy0 = yy1;
                zz0 = zz1;
                xx1 += nextOffset[0];
                yy1 += nextOffset[1];
                zz1 += nextOffset[2];
                if (xx0 != 0 || yy0 != 0 || zz0 != 0) {
                    this.coilCoords.add(new int[]{xx0, yy0, zz0});
                }
                currentOffsets = COIL_WALK_OFFSETS;
                if (!this.coilCoords.isEmpty()
                        && this.coilCoords.get(0)[0] == xx1
                        && this.coilCoords.get(0)[1] == yy1
                        && this.coilCoords.get(0)[2] == zz1) {
                    break;
                }
                step++;
            }
        }

        if (this.coilCoords == null || this.coilCoords.isEmpty()) {
            return false;
        }

        int minTier = 99;
        for (int[] coilCoord : this.coilCoords) {
            Block block = getRelativeBlock(coilCoord[0], coilCoord[1], coilCoord[2]);
            int coilTier = block == null ? -1 : resolveCoilTier(block);
            if (coilTier < 0) {
                BlockPos rel = getRelativePos(coilCoord[0], coilCoord[1], coilCoord[2]);
                this.errorMessage = "Coil [" + rel.getX() + "," + rel.getY() + "," + rel.getZ() + "] is invalid.";
                this.coilCoords = null;
                return false;
            }
            minTier = Math.min(minTier, coilTier);
            if (minTier < this.reactorTier) {
                BlockPos rel = getRelativePos(coilCoord[0], coilCoord[1], coilCoord[2]);
                this.errorMessage = "Coil [" + rel.getX() + "," + rel.getY() + "," + rel.getZ() + "]'s tier is insufficient.";
                return false;
            }
        }

        if (this.hullCoords == null || this.hullCoords.isEmpty()) {
            this.hullCoords = new ArrayList<>();
            for (int[] coilCoord : this.coilCoords) {
                for (int[] offset : ORTHOGONAL_OFFSETS) {
                    int hx = coilCoord[0] + offset[0];
                    int hy = coilCoord[1] + offset[1];
                    int hz = coilCoord[2] + offset[2];
                    Block block = getRelativeBlock(hx, hy, hz);
                    BlockEntity be = getRelativeBlockEntity(hx, hy, hz);
                    if ((block != null && resolveHullRank(block) >= 0)
                            || be instanceof ClayInterfaceBlockEntity
                            || be instanceof RedstoneInterfaceBlockEntity) {
                        boolean seen = false;
                        for (int[] existing : this.hullCoords) {
                            if (existing[0] == hx && existing[1] == hy && existing[2] == hz) {
                                seen = true;
                                break;
                            }
                        }
                        if (!seen) {
                            this.hullCoords.add(new int[]{hx, hy, hz});
                        }
                    } else if ((block == null || resolveCoilTier(block) < 0) && (hx != 0 || hy != 0 || hz != 0)) {
                        BlockPos rel = getRelativePos(hx, hy, hz);
                        this.errorMessage = "Hull [" + rel.getX() + "," + rel.getY() + "," + rel.getZ() + "] is invalid.";
                        this.hullCoords = null;
                        return false;
                    }
                }
            }
        }

        if (this.hullCoords == null || this.hullCoords.isEmpty()) {
            return false;
        }

        int numberOfHulls = 0;
        int hullRankSum = 0;
        BlockPos corePos = getRelativePos(0, 0, 0);

        for (int[] hullCoord : this.hullCoords) {
            Block block = getRelativeBlock(hullCoord[0], hullCoord[1], hullCoord[2]);
            BlockEntity be = getRelativeBlockEntity(hullCoord[0], hullCoord[1], hullCoord[2]);
            int rank = block == null ? -1 : resolveHullRank(block);
            if (rank >= 0) {
                numberOfHulls++;
                hullRankSum += rank;
                if (rank > this.reactorHullMaxRank) {
                    BlockPos rel = getRelativePos(hullCoord[0], hullCoord[1], hullCoord[2]);
                    this.errorMessage = "Hull [" + rel.getX() + "," + rel.getY() + "," + rel.getZ() + "]'s tier is too high.";
                    res = false;
                    break;
                }
            } else if (be instanceof ClayInterfaceBlockEntity || be instanceof RedstoneInterfaceBlockEntity) {
                BlockPos ifacePos = getRelativePos(hullCoord[0], hullCoord[1], hullCoord[2]);
                int ifaceTier = be instanceof TieredClayContainerBlockEntity tiered ? tiered.getTier() : getRelativeBlockTier(hullCoord[0], hullCoord[1], hullCoord[2]);
                if (ifaceTier < this.reactorTier) {
                    this.errorMessage = "Interface [" + ifacePos.getX() + "," + ifacePos.getY() + "," + ifacePos.getZ() + "]'s tier is insufficient.";
                }
                ((ISynchronizedInterface) be).setCoreBlockOffset(
                        corePos.getX() - ifacePos.getX(),
                        corePos.getY() - ifacePos.getY(),
                        corePos.getZ() - ifacePos.getZ()
                );
            } else {
                BlockPos rel = getRelativePos(hullCoord[0], hullCoord[1], hullCoord[2]);
                this.errorMessage = "Hull [" + rel.getX() + "," + rel.getY() + "," + rel.getZ() + "] is invalid.";
                this.hullCoords = null;
                res = false;
                break;
            }
        }

        if (numberOfHulls < REACTOR_HULL_MIN_NUM && res) {
            this.errorMessage = "Reactor size(" + numberOfHulls + ") is less than minimum size(" + REACTOR_HULL_MIN_NUM + ").";
            res = false;
        }

        if (res && numberOfHulls > 0) {
            this.reactorHullNum = numberOfHulls;
            this.reactorRank = (double) hullRankSum / (double) numberOfHulls;
        }
        return res;
    }

    @Override
    protected void onDestruction() {
        super.onDestruction();
        this.machineCraftTime = 0L;
        setStack(2, ItemStack.EMPTY);
    }

    @Override
    public boolean canProceedCraftWhenConstructed() {
        if (!getStack(2).isEmpty()) {
            return true;
        }
        ItemStack antimatter = new ItemStack(ClayiumItems.byPath("antimatter").get());
        ItemStack in = getStack(0);
        if (in.isEmpty() || !ItemStack.isSameItemSameComponents(in, antimatter)) {
            return false;
        }
        return canFitOutputStack(1, getResultPureAntimatter());
    }

    @Override
    public void proceedCraft() {
        Collection<ClayMachineRecipe> recipes = getAvailableRecipes();
        if (getStack(2).isEmpty()) {
            ClayMachineRecipe recipe = ClayMachineRecipeLookup.findBestSingle(recipes, getStack(0), this.baseTier);
            long baseEnergy = recipe != null ? recipe.energyPerTick() : 10_000_000L;
            this.machineConsumingEnergy = (long) ((float) baseEnergy * this.multConsumingEnergy);
            this.machineConsumingEnergy = (long) ((double) this.machineConsumingEnergy * getConsumingEnergyBaseMultiplier());
        }

        if (consumeClayEnergy(this.machineConsumingEnergy)) {
            if (getStack(2).isEmpty()) {
                ClayMachineRecipe recipe = ClayMachineRecipeLookup.findBestSingle(recipes, getStack(0), this.baseTier);
                long baseTime = recipe != null ? recipe.durationTicks() : 300L;
                this.machineTimeToCraft = (long) ((float) (1000L * baseTime) * this.multCraftTime);
                this.machineTimeToCraft = (long) ((double) this.machineTimeToCraft * getCraftTimePureAntimatterMultiplier());
                setStack(2, getResultPureAntimatter());
                ItemStack in = getStack(0);
                in.shrink(1);
                if (in.isEmpty()) {
                    setStack(0, ItemStack.EMPTY);
                }
            }

            this.machineCraftTime += (long) (1000.0 * getEfficiency());
            this.doingWork = true;
            if (this.machineCraftTime >= this.machineTimeToCraft) {
                this.machineCraftTime = 0L;
                this.machineConsumingEnergy = 0L;
                addOutputStack(1, getStack(2));
                setStack(2, ItemStack.EMPTY);
                if (this.externalControlState > 0) {
                    this.externalControlState--;
                    if (this.externalControlState == 0) {
                        this.externalControlState = -1;
                    }
                }
                markForSync();
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putShort("ReactorTier", (short) this.reactorTier);
        tag.putDouble("ReactorRank", this.reactorRank);
        tag.putShort("ReactorHullNum", (short) this.reactorHullNum);
        tag.putBoolean("HasCoilCoords", this.coilCoords != null);
        if (this.coilCoords != null) {
            ListTag list = new ListTag();
            for (int[] c : this.coilCoords) {
                list.add(new IntArrayTag(c));
            }
            tag.put("CoilCoords", list);
        }
        tag.putBoolean("HasHullCoords", this.hullCoords != null);
        if (this.hullCoords != null) {
            ListTag list = new ListTag();
            for (int[] c : this.hullCoords) {
                list.add(new IntArrayTag(c));
            }
            tag.put("HullCoords", list);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("ReactorTier")) {
            this.reactorTier = tag.getShort("ReactorTier");
        }
        this.reactorRank = tag.getDouble("ReactorRank");
        this.reactorHullNum = tag.getShort("ReactorHullNum");
        if (tag.getBoolean("HasCoilCoords")) {
            ListTag list = tag.getList("CoilCoords", Tag.TAG_INT_ARRAY);
            this.coilCoords = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i++) {
                this.coilCoords.add(list.getIntArray(i));
            }
        } else {
            this.coilCoords = null;
        }
        if (tag.getBoolean("HasHullCoords")) {
            ListTag list = tag.getList("HullCoords", Tag.TAG_INT_ARRAY);
            this.hullCoords = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i++) {
                this.hullCoords.add(list.getIntArray(i));
            }
        } else {
            this.hullCoords = null;
        }
    }
}
