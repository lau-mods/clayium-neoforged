package net.claustra01.clayium.blockentity;

import net.claustra01.clayium.block.MachineHullBlock;
import net.claustra01.clayium.block.UpgradeBlock;
import net.claustra01.clayium.block.UpgradeBlock.UpgradeKind;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Clay Blast Furnace (T6 multiblock, 3x2x3 volume around front controller):
 * requires all 17 casing/interface blocks at tier > 4 (tier >= 5) and computes effective {@code recipeTier}.
 */
public class BlastFurnaceBlockEntity extends AbstractMultiblockMachineBlockEntity {
    public int recipeTier;

    public BlastFurnaceBlockEntity(BlockPos pos, BlockState state) {
        this(ClayiumBlockEntities.BLAST_FURNACE.get(), pos, state);
        ensureTierInitialized();
    }

    protected BlastFurnaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void initParams() {
        this.recipeTier = 0;
        this.resultSlotNum = 2;
        this.items = NonNullList.withSize(7, ItemStack.EMPTY);
        this.clayEnergySlot = 6;
        this.listSlotsInsert.clear();
        this.listSlotsExtract.clear();
        this.listSlotsInsert.add(new int[]{0});
        this.listSlotsInsert.add(new int[]{1});
        this.listSlotsInsert.add(new int[]{0, 1});
        this.listSlotsInsert.add(new int[]{6});
        this.listSlotsExtract.add(new int[]{2});
        this.listSlotsExtract.add(new int[]{3});
        this.listSlotsExtract.add(new int[]{2, 3});
        this.insertRoutes = new int[]{-1, 2, -1, 3, -1, -1};
        this.maxAutoExtract = new int[]{64, 64, 64, 1};
        this.extractRoutes = new int[]{2, -1, -1, -1, -1, -1};
        this.maxAutoInsert = new int[]{64, 64, 64};
        this.autoExtractInterval = 1;
        this.autoInsertInterval = 1;
        this.slotsDrop = new int[]{0, 1, 2, 3, 6};
        this.autoInsert = true;
        this.autoExtract = true;
    }

    @Override
    protected void initParamsByTier(int tier) {
        // Intentionally preserve multiblock initParams transport intervals & limits (not overridden by UtilTier)
        this.recipeCategoryId = "blast_furnace";
    }

    @Override
    public int getRecipeTier() {
        return this.recipeTier;
    }

    @Override
    public boolean checkStructureConstructed() {
        int sum = 0;
        int num = 0;
        boolean valid = true;
        BlockPos corePos = getRelativePos(0, 0, 0);

        for (int yy = 0; yy < 2; yy++) {
            for (int xx = -1; xx < 2; xx++) {
                for (int zz = 0; zz < 3; zz++) {
                    if (xx == 0 && yy == 0 && zz == 0) {
                        continue;
                    }
                    num++;
                    int atier = getStructureBlockTier(xx, yy, zz);
                    if (atier <= 4) {
                        valid = false;
                    }
                    if (atier >= 0) {
                        sum += (int) Math.pow(2.0, 16 - atier);
                    } else {
                        sum += (int) Math.pow(2.0, 17);
                    }
                    BlockEntity be = getRelativeBlockEntity(xx, yy, zz);
                    if (be instanceof ClayInterfaceBlockEntity || be instanceof RedstoneInterfaceBlockEntity) {
                        BlockPos ifacePos = getRelativePos(xx, yy, zz);
                        ((ISynchronizedInterface) be).setCoreBlockOffset(
                                corePos.getX() - ifacePos.getX(),
                                corePos.getY() - ifacePos.getY(),
                                corePos.getZ() - ifacePos.getZ()
                        );
                    }
                }
            }
        }

        if (num > 0 && sum > 0) {
            this.recipeTier = Math.max((int) (16.0 - Math.floor(Math.log((double) (sum / num)) / Math.log(2.0) + 0.5)), 0);
        } else {
            this.recipeTier = 0;
        }
        return valid;
    }

    protected int getStructureBlockTier(int xx, int yy, int zz) {
        Block block = getRelativeBlock(xx, yy, zz);
        if (isStandardMachineHull(block)) {
            return ((MachineHullBlock) block).getTier();
        }
        if (block instanceof UpgradeBlock upgrade && upgrade.getKind() == UpgradeKind.OVERCLOCKER) {
            return upgrade.getTier();
        }
        BlockEntity be = getRelativeBlockEntity(xx, yy, zz);
        if (be instanceof ClayInterfaceBlockEntity iface) {
            return iface.getTier();
        }
        if (be instanceof RedstoneInterfaceBlockEntity) {
            return getRelativeBlockTier(xx, yy, zz);
        }
        return -1;
    }

    public static boolean isStandardMachineHull(Block block) {
        if (!(block instanceof MachineHullBlock)) {
            return false;
        }
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        return !path.equals("raw_clay_machine_hull") && !path.endsWith("_alloy_hull");
    }

    @Override
    protected void onDestruction() {
        super.onDestruction();
        this.machineCraftTime = 0L;
        setStack(4, ItemStack.EMPTY);
        setStack(5, ItemStack.EMPTY);
    }
}
