package net.claustra01.clayium.registry;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.entity.ClayBallEntity;
import net.claustra01.clayium.entity.TeleportBallEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ClayiumEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY =
            DeferredRegister.create(Registries.ENTITY_TYPE, ClayiumMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<ClayBallEntity>> CLAY_BALL =
            REGISTRY.register("clay_ball", () -> EntityType.Builder.<ClayBallEntity>of(ClayBallEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("clay_ball"));

    public static final DeferredHolder<EntityType<?>, EntityType<TeleportBallEntity>> TELEPORT_BALL =
            REGISTRY.register("teleport_ball", () -> EntityType.Builder.<TeleportBallEntity>of(TeleportBallEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("teleport_ball"));

    private ClayiumEntities() {}

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}
