package net.claustra01.clayium.blockentity;

import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.transfer.ClayiumTransfer;
import net.claustra01.clayium.util.ClayDirection;
import net.claustra01.clayium.util.ClayDirection.RelativeSide;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Clay Distributor (T7..T9): receives items from BACK and distributes them 1-by-1 in round-robin
 * across enabled output faces while alternating input/output slot colonies cleanly.
 */
public class DistributorBlockEntity extends TieredClayContainerBlockEntity {
    public int inventoryX = 0;
    public int inventoryY = 0;
    public int inventoryColonyX = 0;
    public int inventoryColonyY = 0;
    public int startSide = 0;
    public int autoInsertColony = 0;
    public int autoExtractColony = 0;
    public boolean autoInsertDelayFlag = false;

    public DistributorBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.DISTRIBUTOR.get(), pos, state, 48);
        ensureTierInitialized();
    }

    @Override
    protected void initParams() {
        super.initParams();
        this.insertRoutes = new int[]{-1, -1, -1, 0, -1, -1};
        this.extractRoutes = new int[]{0, 0, 0, -1, 0, 0};
        this.autoInsert = true;
        this.autoExtract = true;
        this.clayEnergySlot = -1;
    }

    @Override
    protected void initParamsByTier(int tier) {
        switch (tier) {
            case 7 -> {
                this.inventoryX = 2;
                this.inventoryY = 2;
                this.maxAutoInsertDefault = 64;
                this.maxAutoExtractDefault = 64;
                this.inventoryColonyX = 2;
                this.inventoryColonyY = 2;
                this.autoExtractInterval = 1;
                this.autoInsertInterval = 1;
            }
            case 8 -> {
                this.inventoryX = 2;
                this.inventoryY = 2;
                this.maxAutoInsertDefault = 128;
                this.maxAutoExtractDefault = 128;
                this.inventoryColonyX = 3;
                this.inventoryColonyY = 2;
                this.autoExtractInterval = 1;
                this.autoInsertInterval = 1;
            }
            case 9 -> {
                this.inventoryX = 2;
                this.inventoryY = 2;
                this.maxAutoInsertDefault = 512;
                this.maxAutoExtractDefault = 512;
                this.inventoryColonyX = 4;
                this.inventoryColonyY = 3;
                this.autoExtractInterval = 1;
                this.autoInsertInterval = 1;
            }
            default -> {
                this.inventoryX = 1;
                this.inventoryY = 1;
                this.maxAutoInsertDefault = 1;
                this.maxAutoExtractDefault = 1;
                this.inventoryColonyX = 1;
                this.inventoryColonyY = 1;
                this.autoExtractInterval = 8;
                this.autoInsertInterval = 8;
            }
        }

        int slotNum = this.inventoryX * this.inventoryY * this.inventoryColonyX * this.inventoryColonyY;
        int[] slots = new int[slotNum];
        int[] slotsReversed = new int[slotNum];
        for (int i = 0; i < slotNum; i++) {
            slots[i] = i;
            slotsReversed[i] = slotNum - i - 1;
        }
        this.listSlotsInsert.clear();
        this.listSlotsExtract.clear();
        this.listSlotsInsert.add(slots);
        this.listSlotsExtract.add(slotsReversed);
        this.slotsDrop = slots;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (stack.isEmpty() || this.listSlotsInsert.isEmpty()) {
            return false;
        }
        for (int validSlot : this.listSlotsInsert.getFirst()) {
            if (validSlot == slot) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void serverTick() {
        if (this.level != null && !this.level.isClientSide) {
            int colonies = Math.max(1, this.inventoryColonyX * this.inventoryColonyY);
            if (this.autoExtractColony >= colonies) {
                this.autoExtractColony = 0;
            }
            if (this.autoInsertColony >= colonies) {
                this.autoInsertColony = 0;
            }

            int colonySize = Math.max(1, this.inventoryX * this.inventoryY);
            boolean syncNeeded = false;

            if (this.autoExtractColony != this.autoInsertColony || !this.autoInsertDelayFlag) {
                int baseOffset = colonySize * this.autoExtractColony;
                boolean hasItem = false;
                for (int i = baseOffset; i < baseOffset + colonySize && i < this.items.size(); i++) {
                    if (!this.items.get(i).isEmpty()) {
                        hasItem = true;
                        break;
                    }
                }
                if (hasItem) {
                    this.autoExtractColony = (this.autoExtractColony + 1) % colonies;
                    if (this.autoExtractColony == this.autoInsertColony) {
                        this.autoInsertDelayFlag = true;
                    }
                    syncNeeded = true;
                }
                int[] activeExtractInColony = new int[colonySize];
                for (int i = 0; i < colonySize; i++) {
                    activeExtractInColony[i] = i + colonySize * this.autoExtractColony;
                }
                this.listSlotsInsert.clear();
                this.listSlotsInsert.add(activeExtractInColony);
            }

            if (this.autoExtractColony != this.autoInsertColony || this.autoInsertDelayFlag) {
                int baseOffset = colonySize * this.autoInsertColony;
                boolean empty = true;
                for (int i = baseOffset; i < baseOffset + colonySize && i < this.items.size(); i++) {
                    if (!this.items.get(i).isEmpty()) {
                        empty = false;
                        break;
                    }
                }
                if (empty) {
                    if (++this.autoInsertColony >= colonies) {
                        this.autoInsertColony = 0;
                        this.autoInsertDelayFlag = false;
                    }
                    syncNeeded = true;
                }
            }

            int[] activeOutSlots = new int[colonySize];
            for (int i = 0; i < colonySize; i++) {
                activeOutSlots[i] = i + colonySize * this.autoInsertColony;
            }
            this.listSlotsExtract.clear();
            this.listSlotsExtract.add(activeOutSlots);

            if (this.autoExtractColony == this.autoInsertColony && this.autoInsertDelayFlag) {
                this.listSlotsInsert.clear();
                this.listSlotsInsert.add(new int[0]);
            }
            if (syncNeeded) {
                markForSync();
            }
        }
        super.serverTick();
    }

    public boolean isCrowded() {
        return this.autoInsertDelayFlag && this.autoExtractColony == this.autoInsertColony;
    }

    @Override
    public void doAutoInsert() {
        markForSync();
        for (int i = 0; i < 6; i++) {
            if (this.extractRoutes[i] >= this.listSlotsExtract.size()) {
                this.extractRoutes[i] = ROUTE_DISABLED;
            }
        }
        Direction front = getFrontDirection();
        int max = this.maxAutoInsertDefault;
        int prevMax = max + 1;

        while (prevMax > max && max > 0) {
            prevMax = max;
            int nextStart = this.startSide;
            for (int i = 0; i < 6 && max >= 1; i++) {
                int side = (this.startSide + i) % 6;
                int route = this.extractRoutes[side];
                if (route >= 0 && route < this.listSlotsExtract.size()) {
                    Direction worldDir = ClayDirection.sideToDirection(front, RelativeSide.byId(side));
                    if (ClayiumTransfer.insert(this, this.listSlotsExtract.get(route), worldDir, 1) == 0) {
                        max--;
                        nextStart = (side + 1) % 6;
                    }
                }
            }
            this.startSide = nextStart;
        }
    }

    @Override
    public boolean acceptEnergyClay() {
        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putShort("StartSide", (short) this.startSide);
        tag.putShort("AutoInsertColony", (short) this.autoInsertColony);
        tag.putShort("AutoExtractColony", (short) this.autoExtractColony);
        tag.putBoolean("AutoInsertDelayFlag", this.autoInsertDelayFlag);
        tag.putShort("InventoryX", (short) this.inventoryX);
        tag.putShort("InventoryY", (short) this.inventoryY);
        tag.putShort("InventoryColonyX", (short) this.inventoryColonyX);
        tag.putShort("InventoryColonyY", (short) this.inventoryColonyY);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.startSide = tag.getShort("StartSide");
        this.autoInsertColony = tag.getShort("AutoInsertColony");
        this.autoExtractColony = tag.getShort("AutoExtractColony");
        this.autoInsertDelayFlag = tag.getBoolean("AutoInsertDelayFlag");
        if (tag.contains("InventoryX")) {
            this.inventoryX = tag.getShort("InventoryX");
        }
        if (tag.contains("InventoryY")) {
            this.inventoryY = tag.getShort("InventoryY");
        }
        if (tag.contains("InventoryColonyX")) {
            this.inventoryColonyX = tag.getShort("InventoryColonyX");
        }
        if (tag.contains("InventoryColonyY")) {
            this.inventoryColonyY = tag.getShort("InventoryColonyY");
        }
    }
}
