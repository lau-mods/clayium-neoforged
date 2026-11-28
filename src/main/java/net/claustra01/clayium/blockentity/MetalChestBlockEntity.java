package net.claustra01.clayium.blockentity;

import net.claustra01.clayium.block.MetalChestBlock;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tiered Metal Chest (up to 832 slots / 8 pages). Supports manual route assignment when configured with tools.
 */
public class MetalChestBlockEntity extends ClayContainerBlockEntity {
    private final int cols;
    private final int rows;
    private final int pages;
    public float lidAngle = 0.0F;
    public float prevLidAngle = 0.0F;
    public int numPlayersUsing = 0;
    private int ticksSinceSync = 0;

    public MetalChestBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.METAL_CHEST.get(), pos, state, computeSlotCount(state));
        if (state.getBlock() instanceof MetalChestBlock chest) {
            this.cols = chest.getCols();
            this.rows = chest.getRows();
            this.pages = chest.getPages();
        } else {
            this.cols = 9;
            this.rows = 6;
            this.pages = 1;
        }
        int total = this.cols * this.rows * this.pages;
        int[] allSlots = new int[total];
        for (int i = 0; i < total; i++) {
            allSlots[i] = i;
        }
        this.listSlotsInsert.add(allSlots);
        this.listSlotsExtract.add(allSlots);
        this.slotsDrop = allSlots;
    }

    private static int computeSlotCount(BlockState state) {
        if (state.getBlock() instanceof MetalChestBlock chest) {
            return Math.max(1, chest.getTotalSlots());
        }
        return 54;
    }

    public int getCols() {
        return this.cols;
    }

    public int getRows() {
        return this.rows;
    }

    public int getPages() {
        return this.pages;
    }

    @Override
    public boolean canExtractItemUnsafe(int slot, ItemStack stack, int route) {
        return route == 0;
    }

    @Override
    public boolean canInsertItemUnsafe(int slot, ItemStack stack, int route) {
        return route == 0;
    }

    @Override
    public boolean acceptEnergyClay() {
        return false;
    }

    public void startOpen(Player player) {
        if (this.level == null) {
            return;
        }
        if (this.numPlayersUsing < 0) {
            this.numPlayersUsing = 0;
        }
        this.numPlayersUsing++;
        this.level.blockEvent(this.worldPosition, getBlockState().getBlock(), 1, this.numPlayersUsing);
    }

    public void stopOpen(Player player) {
        if (this.level == null) {
            return;
        }
        this.numPlayersUsing = Math.max(0, this.numPlayersUsing - 1);
        this.level.blockEvent(this.worldPosition, getBlockState().getBlock(), 1, this.numPlayersUsing);
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            this.numPlayersUsing = type;
            return true;
        }
        return super.triggerEvent(id, type);
    }

    @Override
    public void serverTick() {
        super.serverTick();
        tickLidAnimation();
    }

    public static void clientTickStatic(Level level, BlockPos pos, BlockState state, MetalChestBlockEntity be) {
        be.tickLidAnimation();
    }

    private void tickLidAnimation() {
        if (this.level == null) {
            return;
        }
        this.ticksSinceSync++;
        if (!this.level.isClientSide && this.numPlayersUsing != 0 && (this.ticksSinceSync + this.worldPosition.getX() + this.worldPosition.getY() + this.worldPosition.getZ()) % 200 == 0) {
            this.level.blockEvent(this.worldPosition, getBlockState().getBlock(), 1, this.numPlayersUsing);
        }

        this.prevLidAngle = this.lidAngle;
        float step = 0.1F;
        if (this.numPlayersUsing > 0 && this.lidAngle == 0.0F) {
            this.level.playSound(
                    null,
                    this.worldPosition.getX() + 0.5,
                    this.worldPosition.getY() + 0.5,
                    this.worldPosition.getZ() + 0.5,
                    SoundEvents.CHEST_OPEN,
                    SoundSource.BLOCKS,
                    0.5F,
                    this.level.random.nextFloat() * 0.1F + 0.9F
            );
        }
        if (this.numPlayersUsing == 0 && this.lidAngle > 0.0F || this.numPlayersUsing > 0 && this.lidAngle < 1.0F) {
            float prev = this.lidAngle;
            if (this.numPlayersUsing > 0) {
                this.lidAngle += step;
            } else {
                this.lidAngle -= step;
            }
            if (this.lidAngle > 1.0F) {
                this.lidAngle = 1.0F;
            }
            if (this.lidAngle < 0.5F && prev >= 0.5F) {
                this.level.playSound(
                        null,
                        this.worldPosition.getX() + 0.5,
                        this.worldPosition.getY() + 0.5,
                        this.worldPosition.getZ() + 0.5,
                        SoundEvents.CHEST_CLOSE,
                        SoundSource.BLOCKS,
                        0.5F,
                        this.level.random.nextFloat() * 0.1F + 0.9F
                );
            }
            if (this.lidAngle < 0.0F) {
                this.lidAngle = 0.0F;
            }
        }
    }
}
