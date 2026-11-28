package net.claustra01.clayium.blockentity;

import javax.annotation.Nullable;
import net.claustra01.clayium.block.ClayMarkerBlock;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Clay Marker BlockEntity: scans axis-aligned markers up to 63 blocks away, forms area bounds
 * (including Open Pit / Ground Leveling / Prism Y-extent overrides), and drops linked markers
 * at the origin when absorbed by an Area Machine.
 */
public class ClayMarkerBlockEntity extends BlockEntity implements IAxisAlignedBBProvider {
    public static final int MAX_RANGE = 64;

    public int state = 0;
    @Nullable
    public AABB axisAlignedBB;
    public int offsetX = 0;
    public int offsetY = 0;
    public int offsetZ = 0;

    public ClayMarkerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ClayiumBlockEntities.CLAY_MARKER.get(), pos, blockState);
    }

    @Override
    public boolean hasAxisAlignedBB() {
        return this.axisAlignedBB != null && this.state >= 2;
    }

    @Override
    public AABB getAxisAlignedBB() {
        return this.axisAlignedBB;
    }

    public int getBoxAppearance() {
        return Math.max(0, this.state - 2);
    }

    public void activate() {
        if (this.level == null) {
            return;
        }
        int foundX = 0;
        int foundY = 0;
        int foundZ = 0;

        for (int d = 1; d < MAX_RANGE; d++) {
            if (foundX == 0 && isMarkerAt(this.worldPosition.offset(d, 0, 0))) {
                foundX = d;
            } else if (foundX == 0 && isMarkerAt(this.worldPosition.offset(-d, 0, 0))) {
                foundX = -d;
            }
            if (foundY == 0 && isMarkerAt(this.worldPosition.offset(0, d, 0))) {
                foundY = d;
            } else if (foundY == 0 && isMarkerAt(this.worldPosition.offset(0, -d, 0))) {
                foundY = -d;
            }
            if (foundZ == 0 && isMarkerAt(this.worldPosition.offset(0, 0, d))) {
                foundZ = d;
            } else if (foundZ == 0 && isMarkerAt(this.worldPosition.offset(0, 0, -d))) {
                foundZ = -d;
            }
        }

        if (foundX == 0 && foundY == 0 && foundZ == 0 && this.state != 1) {
            this.axisAlignedBB = null;
            this.offsetX = 0;
            this.offsetY = 0;
            this.offsetZ = 0;
            this.state = 1;
        } else {
            this.offsetX = foundX;
            this.offsetY = foundY;
            this.offsetZ = foundZ;
            int minX = Math.min(this.worldPosition.getX(), this.worldPosition.getX() + foundX);
            int minY = Math.min(this.worldPosition.getY(), this.worldPosition.getY() + foundY);
            int minZ = Math.min(this.worldPosition.getZ(), this.worldPosition.getZ() + foundZ);
            int maxX = Math.max(this.worldPosition.getX(), this.worldPosition.getX() + foundX) + 1;
            int maxY = Math.max(this.worldPosition.getY(), this.worldPosition.getY() + foundY) + 1;
            int maxZ = Math.max(this.worldPosition.getZ(), this.worldPosition.getZ() + foundZ) + 1;

            String path = BuiltInRegistries.BLOCK.getKey(getBlockState().getBlock()).getPath();
            if ("clay_open_pit_marker".equals(path)) {
                minY = this.level.getMinBuildHeight() + 1;
            } else if ("clay_ground_leveling_marker".equals(path)) {
                maxY = this.level.getMaxBuildHeight();
            } else if ("clay_prism_marker".equals(path)) {
                minY = this.level.getMinBuildHeight() + 1;
                maxY = this.level.getMaxBuildHeight();
            }

            this.axisAlignedBB = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
            if (this.state <= 1) {
                this.state = 3;
            } else {
                this.state++;
                if (this.state >= 5) {
                    this.state = 2;
                }
            }
        }
        markForSync();
    }

    private boolean isMarkerAt(BlockPos pos) {
        return this.level != null && this.level.getBlockState(pos).getBlock() instanceof ClayMarkerBlock;
    }

    @Override
    public void setAxisAlignedBBToMachine() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        consumeMarkerAt(this.worldPosition.offset(this.offsetX, 0, 0));
        consumeMarkerAt(this.worldPosition.offset(0, this.offsetY, 0));
        consumeMarkerAt(this.worldPosition.offset(0, 0, this.offsetZ));
        consumeMarkerAt(this.worldPosition);
    }

    private void consumeMarkerAt(BlockPos markerPos) {
        if (this.level == null) {
            return;
        }
        BlockState stateAt = this.level.getBlockState(markerPos);
        if (stateAt.getBlock() instanceof ClayMarkerBlock) {
            ItemStack drop = new ItemStack(stateAt.getBlock());
            this.level.removeBlock(markerPos, false);
            this.level.addFreshEntity(new ItemEntity(
                    this.level,
                    this.worldPosition.getX() + 0.5,
                    this.worldPosition.getY() + 0.5,
                    this.worldPosition.getZ() + 0.5,
                    drop
            ));
        }
    }

    public void markForSync() {
        setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putByte("State", (byte) this.state);
        tag.putInt("OffsetX", this.offsetX);
        tag.putInt("OffsetY", this.offsetY);
        tag.putInt("OffsetZ", this.offsetZ);
        tag.putBoolean("hasAABB", this.axisAlignedBB != null);
        if (this.axisAlignedBB != null) {
            tag.putDouble("AABBMinX", this.axisAlignedBB.minX);
            tag.putDouble("AABBMinY", this.axisAlignedBB.minY);
            tag.putDouble("AABBMinZ", this.axisAlignedBB.minZ);
            tag.putDouble("AABBMaxX", this.axisAlignedBB.maxX);
            tag.putDouble("AABBMaxY", this.axisAlignedBB.maxY);
            tag.putDouble("AABBMaxZ", this.axisAlignedBB.maxZ);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.state = tag.getByte("State");
        this.offsetX = tag.getInt("OffsetX");
        this.offsetY = tag.getInt("OffsetY");
        this.offsetZ = tag.getInt("OffsetZ");
        if (tag.getBoolean("hasAABB")) {
            this.axisAlignedBB = new AABB(
                    tag.getDouble("AABBMinX"),
                    tag.getDouble("AABBMinY"),
                    tag.getDouble("AABBMinZ"),
                    tag.getDouble("AABBMaxX"),
                    tag.getDouble("AABBMaxY"),
                    tag.getDouble("AABBMaxZ")
            );
        } else {
            this.axisAlignedBB = null;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
