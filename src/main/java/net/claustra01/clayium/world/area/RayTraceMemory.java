package net.claustra01.clayium.world.area;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public record RayTraceMemory(Vec3 relativeEye, Vec3 look, Direction hitFace) {
    public static final com.mojang.serialization.Codec<RayTraceMemory> CODEC =
            com.mojang.serialization.codecs.RecordCodecBuilder.create(instance -> instance.group(
                    Vec3.CODEC.fieldOf("eye").forGetter(RayTraceMemory::relativeEye),
                    Vec3.CODEC.fieldOf("look").forGetter(RayTraceMemory::look),
                    Direction.CODEC.fieldOf("face").forGetter(RayTraceMemory::hitFace))
                    .apply(instance, RayTraceMemory::new));

    public static RayTraceMemory lookingFrom(Player player, BlockPos machinePosition, Direction hitFace) {
        return new RayTraceMemory(
                player.getEyePosition().subtract(Vec3.atLowerCornerOf(machinePosition)),
                player.getLookAngle().normalize(),
                hitFace);
    }

    public static RayTraceMemory standard(Direction direction) {
        Vec3 look = Vec3.atLowerCornerOf(direction.getNormal()).normalize();
        return new RayTraceMemory(new Vec3(0.5D, 0.5D, 0.5D).subtract(look.scale(0.499D)), look,
                direction.getOpposite());
    }

    public Vec3 startAt(BlockPos target) {
        return Vec3.atLowerCornerOf(target).add(relativeEye);
    }

    public BlockHitResult directHit(BlockPos target) {
        Vec3 location = Vec3.atCenterOf(target).add(
                hitFace.getStepX() * 0.5D, hitFace.getStepY() * 0.5D, hitFace.getStepZ() * 0.5D);
        return new BlockHitResult(location, hitFace, target, false);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("eye_x", relativeEye.x);
        tag.putDouble("eye_y", relativeEye.y);
        tag.putDouble("eye_z", relativeEye.z);
        tag.putDouble("look_x", look.x);
        tag.putDouble("look_y", look.y);
        tag.putDouble("look_z", look.z);
        tag.putString("hit_face", hitFace.getName());
        return tag;
    }

    public static RayTraceMemory load(CompoundTag tag) {
        Direction face = Direction.byName(tag.getString("hit_face"));
        return new RayTraceMemory(
                new Vec3(tag.getDouble("eye_x"), tag.getDouble("eye_y"), tag.getDouble("eye_z")),
                new Vec3(tag.getDouble("look_x"), tag.getDouble("look_y"), tag.getDouble("look_z")).normalize(),
                face == null ? Direction.UP : face);
    }
}
