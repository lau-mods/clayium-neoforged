/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block.entity;

import net.claustra01.clayium.registry.ClayiumRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.claustra01.clayium.world.level.block.QuartzCrucibleBlock;

public final class QuartzCrucibleBlockEntity extends BlockEntity {
    public static final int MAX_INGOTS = 9;
    public static final int TICKS_PER_INGOT = 600;
    private int ingotCount;
    private int heatingTicks;

    public QuartzCrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumRegistries.QUARTZ_CRUCIBLE_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, QuartzCrucibleBlockEntity crucible) {
        if(state.getValue(QuartzCrucibleBlock.FILL)!=crucible.ingotCount){
            level.setBlock(pos,state.setValue(QuartzCrucibleBlock.FILL,crucible.ingotCount),3);
        }
        if (crucible.ingotCount > 0 && crucible.heatingTicks < crucible.ingotCount * TICKS_PER_INGOT) {
            crucible.heatingTicks++;
            crucible.setChanged();
        }
    }

    public void accept(Entity entity) {
        if (!(entity instanceof ItemEntity itemEntity) || entity.getY() - worldPosition.getY() >= 0.2D) return;
        ItemStack stack = itemEntity.getItem();
        if (stack.is(ClayiumRegistries.PHASE6_ITEMS.get("impure_silicon_ingot").get()) && ingotCount < MAX_INGOTS) {
            stack.shrink(1);
            ingotCount++;
            level.setBlock(worldPosition,getBlockState().setValue(QuartzCrucibleBlock.FILL,ingotCount),3);
            changedAndSync();
        } else if (stack.is(Items.STRING) && ingotCount > 0 && heatingTicks >= ingotCount * TICKS_PER_INGOT) {
            stack.shrink(1);
            ItemStack result = new ItemStack(ClayiumRegistries.PHASE6_ITEMS.get("silicon_ingot").get(), ingotCount);
            level.addFreshEntity(new ItemEntity(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.3D,
                    worldPosition.getZ() + 0.5D, result));
            ingotCount = 0;
            heatingTicks = 0;
            level.setBlock(worldPosition,getBlockState().setValue(QuartzCrucibleBlock.FILL,0),3);
            changedAndSync();
        }
        if (stack.isEmpty()) itemEntity.discard();
    }

    private void changedAndSync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public int ingotCount() { return ingotCount; }

    @Override public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("IngotCount", ingotCount);
        tag.putInt("HeatingTicks", heatingTicks);
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ingotCount = Math.clamp(tag.getInt("IngotCount"), 0, MAX_INGOTS);
        heatingTicks = Math.clamp(tag.getInt("HeatingTicks"), 0, ingotCount * TICKS_PER_INGOT);
    }
}
