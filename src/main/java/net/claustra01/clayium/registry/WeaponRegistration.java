package net.claustra01.clayium.registry;

import java.util.List;
import net.claustra01.clayium.weapon.ClayBall;
import net.claustra01.clayium.weapon.ClayShooterItem;
import net.claustra01.clayium.weapon.ClayShooterSpecification;
import net.claustra01.clayium.weapon.TeleportBall;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class WeaponRegistration {
    public static final DeferredItem<ClayShooterItem> BASIC_CLAY_SHOOTER = registerShooter(
            ClayShooterSpecification.BASIC, false);
    public static final DeferredItem<ClayShooterItem> ADVANCED_CLAY_SHOOTER = registerShooter(
            ClayShooterSpecification.ADVANCED, false);
    public static final DeferredItem<ClayShooterItem> SNIPER_CLAY_SHOOTER = registerShooter(
            ClayShooterSpecification.SNIPER, false);
    public static final DeferredItem<ClayShooterItem> SNIPER_CLAY_SHOOTER_PRO = registerShooter(
            ClayShooterSpecification.SNIPER_PRO, false);
    public static final DeferredItem<ClayShooterItem> INSTANT_TELEPORTER = registerShooter(
            ClayShooterSpecification.TELEPORTER, true);

    public static final DeferredHolder<EntityType<?>, EntityType<ClayBall>> CLAY_BALL =
            ClayiumRegistries.ENTITY_TYPES.register(
                    "clay_ball",
                    () -> EntityType.Builder.<ClayBall>of(ClayBall::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("clay_ball"));
    public static final DeferredHolder<EntityType<?>, EntityType<TeleportBall>> TELEPORT_BALL =
            ClayiumRegistries.ENTITY_TYPES.register(
                    "teleport_ball",
                    () -> EntityType.Builder.<TeleportBall>of(TeleportBall::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("teleport_ball"));

    public static final List<DeferredItem<? extends Item>> ALL = List.of(
            BASIC_CLAY_SHOOTER,
            ADVANCED_CLAY_SHOOTER,
            SNIPER_CLAY_SHOOTER,
            SNIPER_CLAY_SHOOTER_PRO,
            INSTANT_TELEPORTER);

    private static DeferredItem<ClayShooterItem> registerShooter(
            ClayShooterSpecification specification, boolean teleporter) {
        return ClayiumRegistries.ITEMS.register(
                specification.id(),
                () -> new ClayShooterItem(
                        specification,
                        new Item.Properties().durability(specification.durability()).component(
                                FoundationDataComponentRegistration.TIER.get(), specification.tier()),
                        teleporter));
    }

    private WeaponRegistration() {
    }
}
