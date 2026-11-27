package mods.clayium.util;

import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class UtilCoordinate {
    private static double d2rRate = Math.PI / 180.0;
    private static double r2dRate = 180.0 / Math.PI;

    public UtilCoordinate() {
        super();
    }

    public static Vec3 v3(double x, double y, double z) {
        return Vec3.func_72443_a(x, y, z);
    }

    public static UtilCoordinate.Vec3RYP v3RYP(double r, float yaw, float pitch) {
        return new UtilCoordinate.Vec3RYP(r, yaw, pitch);
    }

    public static UtilCoordinate.Vec3SYF v3SYF(double strafe, double y, double forward, float yaw) {
        return new UtilCoordinate.Vec3SYF(strafe, y, forward, yaw);
    }

    public static float d2r(float degree) {
        return degree * (float)d2rRate;
    }

    public static float r2d(float radian) {
        return radian * (float)r2dRate;
    }

    public static class Vec3RYP extends Vec3 {
        protected Vec3RYP(double r, float yaw, float pitch) {
            super(r, (double)yaw, (double)pitch);
        }

        public double getR() {
            return this.field_72450_a;
        }

        public float getYaw() {
            return (float)this.field_72448_b;
        }

        public float getPitch() {
            return (float)this.field_72449_c;
        }

        public void setR(double r) {
            this.field_72450_a = r;
        }

        public void setYaw(double yaw) {
            this.field_72448_b = yaw;
        }

        public void setPitch(double pitch) {
            this.field_72449_c = pitch;
        }

        public static UtilCoordinate.Vec3RYP fromXYZ(Vec3 vec3) {
            return UtilCoordinate.v3RYP(
                vec3.func_72433_c(),
                UtilCoordinate.r2d((float)Math.atan2(-vec3.field_72450_a, vec3.field_72449_c)),
                UtilCoordinate.r2d(
                    (float)(
                        -Math.atan2(
                            vec3.field_72448_b,
                            (double)MathHelper.func_76133_a(vec3.field_72450_a * vec3.field_72450_a + vec3.field_72449_c * vec3.field_72449_c)
                        )
                    )
                )
            );
        }

        public static Vec3 toXYZ(double r, float yaw, float pitch) {
            return UtilCoordinate.v3(
                -r * (double)MathHelper.func_76126_a(UtilCoordinate.d2r(yaw)) * (double)MathHelper.func_76134_b(UtilCoordinate.d2r(pitch)),
                -r * (double)MathHelper.func_76126_a(UtilCoordinate.d2r(pitch)),
                r * (double)MathHelper.func_76134_b(UtilCoordinate.d2r(yaw)) * (double)MathHelper.func_76134_b(UtilCoordinate.d2r(pitch))
            );
        }

        public Vec3 toXYZ() {
            return toXYZ(this.getR(), this.getYaw(), this.getPitch());
        }
    }

    public static class Vec3SYF extends Vec3 {
        private float yaw;
        private float yawr;

        public float getYaw() {
            return this.yaw;
        }

        public void setYaw(float yaw) {
            this.yaw = yaw;
            this.yawr = UtilCoordinate.d2r(yaw);
        }

        public void updateYaw(float newYaw) {
            float newYawr = UtilCoordinate.d2r(newYaw);
            float f = this.yawr - newYawr;
            float sin = MathHelper.func_76126_a(f);
            float cos = MathHelper.func_76134_b(f);
            double newStrafe = (double)cos * this.getStrafe() - (double)sin * this.getForward();
            this.setForward((double)sin * this.getStrafe() + (double)sin * this.getForward());
            this.setStrafe(newStrafe);
            this.yaw = newYaw;
            this.yawr = newYawr;
        }

        public double getStrafe() {
            return this.field_72450_a;
        }

        public double getY() {
            return this.field_72448_b;
        }

        public double getForward() {
            return this.field_72449_c;
        }

        public void setStrafe(double strafe) {
            this.field_72450_a = strafe;
        }

        public void setY(double y) {
            this.field_72448_b = y;
        }

        public void setForward(double forward) {
            this.field_72449_c = forward;
        }

        protected Vec3SYF(double strafe, double y, double forward, float yaw) {
            super(strafe, y, forward);
            this.setYaw(yaw);
        }

        public static UtilCoordinate.Vec3SYF fromXYZ(Vec3 vec3, float yaw) {
            float yawr = yaw * (float) Math.PI / 180.0F;
            float sin = MathHelper.func_76126_a(yawr);
            float cos = MathHelper.func_76134_b(yawr);
            return UtilCoordinate.v3SYF(
                vec3.field_72450_a * (double)cos + vec3.field_72449_c * (double)sin,
                vec3.field_72448_b,
                vec3.field_72449_c * (double)cos - vec3.field_72450_a * (double)sin,
                yaw
            );
        }

        public Vec3 toXYZ() {
            return toXYZ(this.getStrafe(), this.getY(), this.getForward(), this.getYaw());
        }

        public static Vec3 toXYZ(double strafe, double y, double forward, float yaw) {
            float yawr = yaw * (float) Math.PI / 180.0F;
            float sin = MathHelper.func_76126_a(yawr);
            float cos = MathHelper.func_76134_b(yawr);
            return UtilCoordinate.v3(strafe * (double)cos - forward * (double)sin, y, forward * (double)cos + strafe * (double)sin);
        }
    }
}
