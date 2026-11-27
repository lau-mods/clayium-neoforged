package mods.clayium.item;

import mods.clayium.entity.EntityTeleportBall;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class InstantTeleporter extends ClayShooter {
    public InstantTeleporter(
        int maxDamage,
        String unlocalizedName,
        String textureName,
        int bulletLifespan,
        float bulletInitialVelocity,
        float bulletDiffusion,
        int bulletDamage,
        float bulletShootingRate,
        int bulletCooldownTime,
        int chargeTime
    ) {
        super(
            maxDamage,
            unlocalizedName,
            textureName,
            bulletLifespan,
            bulletInitialVelocity,
            bulletDiffusion,
            bulletDamage,
            bulletShootingRate,
            bulletCooldownTime,
            chargeTime
        );
    }

    public InstantTeleporter(
        int maxDamage,
        String unlocalizedName,
        String textureName,
        int bulletLifespan,
        float bulletInitialVelocity,
        float bulletDiffusion,
        int bulletDamage,
        int bulletShootingFrame,
        int chargeTime
    ) {
        super(maxDamage, unlocalizedName, textureName, bulletLifespan, bulletInitialVelocity, bulletDiffusion, bulletDamage, bulletShootingFrame, chargeTime);
    }

    public void spawnEntity(ItemStack stack, EntityPlayer player, float per, boolean critical) {
        if (!player.field_70170_p.field_72995_K) {
            player.field_70170_p
                .func_72838_d(
                    new EntityTeleportBall(
                        player.field_70170_p,
                        player,
                        this.getLifespan(stack, player),
                        this.getInitialVelocity(stack, player) * per,
                        this.getDiffusion(stack, player),
                        (int)((float)this.getDamage(stack, player) * per),
                        1,
                        critical
                    )
                );
        }
    }
}
