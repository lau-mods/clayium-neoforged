package net.claustra01.clayium.gadget;

import net.claustra01.clayium.ClayiumMod;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = ClayiumMod.MOD_ID)
public final class GadgetEvents {
    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide()) {
            GadgetEffects.update(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        GadgetEffects.update(event.getEntity());
    }

    @SubscribeEvent
    public static void changeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        GadgetEffects.update(event.getEntity());
    }

    @SubscribeEvent
    public static void attackEntity(AttackEntityEvent event) {
        if (event.getTarget() instanceof LivingEntity target
                && GadgetEffects.isActive(event.getEntity(), GadgetType.REPEATED_ATTACK)) {
            target.invulnerableTime = 0;
        }
    }

    @SubscribeEvent
    public static void projectileImpact(ProjectileImpactEvent event) {
        if (event.getRayTraceResult() instanceof EntityHitResult hit
                && hit.getEntity() instanceof LivingEntity target
                && event.getProjectile().getOwner() instanceof Player player
                && GadgetEffects.isActive(player, GadgetType.REPEATED_ATTACK)) {
            target.invulnerableTime = 0;
        }
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        event.getEntity().getData(net.claustra01.clayium.registry.GadgetRegistration.PLAYER_STATE)
                .supersonicSpeed(false);
        GadgetEffects.update(event.getEntity());
    }

    private GadgetEvents() {
    }
}
