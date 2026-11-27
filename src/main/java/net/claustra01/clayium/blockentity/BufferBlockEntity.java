package net.claustra01.clayium.blockentity;

import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.util.ClayDirection.RelativeSide;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tiered Buffer (T4..T13): auto-extracts from BACK by default and auto-inserts into configured outputs.
 */
public class BufferBlockEntity extends TieredClayContainerBlockEntity {
    public int inventoryX = 0;
    public int inventoryY = 0;

    public BufferBlockEntity(BlockPos pos, BlockState state) {
        this(ClayiumBlockEntities.BUFFER.get(), pos, state);
    }

    protected BufferBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 54);
        ensureTierInitialized();
    }

    @Override
    protected void initParams() {
        super.initParams();
        this.insertRoutes = new int[]{-1, -1, -1, 0, -1, -1};
        this.extractRoutes = new int[]{-1, -1, -1, -1, -1, -1};
        this.autoInsert = true;
        this.autoExtract = true;
        this.clayEnergySlot = -1;
    }

    @Override
    protected void initParamsByTier(int tier) {
        setDefaultTransportationParamsByTier(tier, ParamMode.BUFFER);
        switch (tier) {
            case 4 -> {
                this.inventoryY = 1;
                this.inventoryX = 2;
            }
            case 5 -> {
                this.inventoryY = 2;
                this.inventoryX = 3;
            }
            case 6 -> {
                this.inventoryY = 3;
                this.inventoryX = 4;
            }
            case 7 -> {
                this.inventoryY = 4;
                this.inventoryX = 5;
            }
            case 8 -> {
                this.inventoryY = 4;
                this.inventoryX = 9;
            }
            case 9, 10, 11, 12, 13 -> {
                this.inventoryY = 6;
                this.inventoryX = 9;
            }
            default -> {
                this.inventoryX = 1;
                this.inventoryY = 1;
            }
        }
        int slotNum = this.inventoryX * this.inventoryY;
        int[] insertSlots = new int[slotNum];
        int[] extractSlots = new int[slotNum];
        for (int i = 0; i < slotNum; i++) {
            insertSlots[i] = i;
            extractSlots[i] = slotNum - i - 1;
        }
        this.listSlotsInsert.clear();
        this.listSlotsExtract.clear();
        this.listSlotsInsert.add(insertSlots);
        this.listSlotsExtract.add(extractSlots);
        this.slotsDrop = insertSlots;
    }

    @Override
    public int getInventorySize() {
        return this.inventoryX > 0 && this.inventoryY > 0 ? this.inventoryX * this.inventoryY : super.getInventorySize();
    }

    @Override
    public boolean canInsertToSide(int slot, ItemStack stack, RelativeSide relSide) {
        if (slot < 0 || slot >= getInventorySize() || stack.isEmpty()) {
            return false;
        }
        if (this.insertRoutes[relSide.id()] == ROUTE_DISABLED) {
            // Original TileClayBuffer allows external pipes/hoppers to insert into accessible buffer slots even on unrouted sides if filter passes
            return super.canInsertToSide(slot, stack, relSide);
        }
        return super.canInsertToSide(slot, stack, relSide);
    }

    @Override
    public boolean canExtractItemUnsafe(int slot, ItemStack stack, int route) {
        return slot >= 0 && slot < getInventorySize();
    }

    @Override
    public boolean canInsertItemUnsafe(int slot, ItemStack stack, int route) {
        return slot >= 0 && slot < getInventorySize();
    }

    @Override
    public boolean acceptEnergyClay() {
        return false;
    }
}
