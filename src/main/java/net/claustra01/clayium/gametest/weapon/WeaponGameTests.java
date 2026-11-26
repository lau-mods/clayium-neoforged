package net.claustra01.clayium.gametest.weapon;

import java.util.List;
import java.util.function.Consumer;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.WeaponRegistration;
import net.claustra01.clayium.weapon.ClayBall;
import net.claustra01.clayium.weapon.ClayShooterItem;
import net.claustra01.clayium.weapon.ClayShooterSpecification;
import net.claustra01.clayium.weapon.TeleportBall;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WeaponGameTests {
    private static final double EPSILON = 0.000_01D;

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void fourShootersPreserveOriginalPerformanceAndCharge(GameTestHelper helper) {
        List<ClayShooterSpecification> specifications = List.of(
                ClayShooterSpecification.BASIC,
                ClayShooterSpecification.ADVANCED,
                ClayShooterSpecification.SNIPER,
                ClayShooterSpecification.SNIPER_PRO);
        int[] durability = {10_000, 1_000, 8_000, 2_500};
        int[] lifespan = {2, 4, 2, 3};
        float[] velocity = {1.2F, 4.5F, 15.0F, 30.0F};
        float[] diffusion = {25.0F, 3.0F, 0.0F, 0.0F};
        int[] damage = {4, 7, 25, 100};
        int[] charge = {0, 0, 20, 40};
        int[] cooldown = {2, 4, 2, 2};
        List<ClayShooterItem> items = List.of(
                WeaponRegistration.BASIC_CLAY_SHOOTER.get(),
                WeaponRegistration.ADVANCED_CLAY_SHOOTER.get(),
                WeaponRegistration.SNIPER_CLAY_SHOOTER.get(),
                WeaponRegistration.SNIPER_CLAY_SHOOTER_PRO.get());
        for (int index = 0; index < specifications.size(); index++) {
            ClayShooterSpecification specification = specifications.get(index);
            require(specification.durability() == durability[index], "Wrong durability at shooter " + index);
            require(specification.straightFlightTicks() == lifespan[index], "Wrong lifespan at shooter " + index);
            requireClose(specification.initialVelocity(), velocity[index], "initial velocity " + index);
            requireClose(specification.diffusion(), diffusion[index], "diffusion " + index);
            require(specification.damage() == damage[index], "Wrong damage at shooter " + index);
            require(specification.chargeTicks() == charge[index], "Wrong charge at shooter " + index);
            require(specification.cooldownTicks() == cooldown[index], "Wrong cooldown at shooter " + index);
            require(items.get(index).getDefaultInstance().getMaxDamage() == durability[index],
                    "Registered durability differs at shooter " + index);
            require(items.get(index).specification() == specification, "Registered shooter uses the wrong specification");
        }
        for (int index = 0; index < items.size(); index++) {
            ClayShooterSpecification specification = specifications.get(index);
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.setPos(helper.absolutePos(new BlockPos(1, 5 + index, 1)).getCenter());
            player.setYRot(0.0F);
            player.setXRot(0.0F);
            ItemStack stack = new ItemStack(items.get(index));
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            items.get(index).use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
            if (specification.chargeTicks() > 0) {
                require(helper.getLevel().getEntities(WeaponRegistration.CLAY_BALL.get(),
                        entity -> entity.getOwner() == player).isEmpty(), "Uncharged sniper fired on use");
                items.get(index).releaseUsing(stack, helper.getLevel(), player,
                        72_000 - specification.chargeTicks());
            }
            ClayBall projectile = helper.getLevel().getEntities(WeaponRegistration.CLAY_BALL.get(),
                    entity -> entity.getOwner() == player).stream().findFirst()
                    .orElseThrow(() -> new GameTestAssertException("Shooter did not fire"));
            require(Math.abs(projectile.getDeltaMovement().length() - velocity[index])
                    < 0.15D + velocity[index] * diffusion[index] * 0.017_227_5D * 4.0D,
                    "Wrong projectile speed at shooter " + index + ": " + projectile.getDeltaMovement().length());
            require(Math.abs(projectile.getDeltaMovement().normalize().dot(new Vec3(0, 0, 1)))
                    >= Math.cos(Math.toRadians(diffusion[index] * 3.0D)), "Spread exceeded bound " + index);
            requireClose(projectile.damage(), damage[index], "projectile damage " + index);
            require(projectile.straightFlightTicks() == lifespan[index], "Wrong flight duration " + index);
            require(stack.getDamageValue() == 1 + charge[index], "Wrong durability cost " + index);
            require(player.getCooldowns().isOnCooldown(items.get(index)), "Missing cooldown " + index);
        }
        for (ClayShooterItem item : List.of(items.get(2), items.get(3))) {
            ClayShooterSpecification specification = item.specification();
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.setPos(helper.absolutePos(new BlockPos(1, 10, 1)).getCenter());
            ItemStack stack = new ItemStack(item);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            item.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
            item.releaseUsing(stack, helper.getLevel(), player, 72_000 - specification.chargeTicks() / 2);
            ClayBall projectile = helper.getLevel().getEntities(WeaponRegistration.CLAY_BALL.get(),
                    entity -> entity.getOwner() == player).stream().findFirst()
                    .orElseThrow(() -> new GameTestAssertException("Partial charge did not fire"));
            double power = 5.0D / 12.0D;
            require(Math.abs(projectile.getDeltaMovement().length() - specification.initialVelocity() * power) < 0.15D,
                    "Partial charge speed is wrong");
            requireClose(projectile.damage(), (int) (specification.damage() * (float) power), "partial damage");
            require(stack.getDamageValue() == 1 + (int) (specification.chargeTicks() * (float) power),
                    "Partial charge durability is wrong");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 40)
    public static void clayBallCollisionDamageAndRemovalAreServerAuthoritative(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        BlockPos targetPosition = helper.absolutePos(new BlockPos(1, 3, 1));
        Cow target = EntityType.COW.create(level);
        require(target != null, "Could not create the target");
        target.setNoAi(true);
        target.setPos(targetPosition.getX() + 0.5D, targetPosition.getY(), targetPosition.getZ() + 0.5D);
        level.addFreshEntity(target);
        float initialHealth = target.getHealth();

        ClayBall damagingBall = new ClayBall(level, owner, ClayShooterSpecification.BASIC, 1.0F, false);
        damagingBall.setPos(target.getX() - 1.5D, target.getEyeY(), target.getZ());
        damagingBall.setDeltaMovement(1.0D, 0.0D, 0.0D);
        level.addFreshEntity(damagingBall);

        BlockPos wall = targetPosition.offset(4, 0, 0);
        level.setBlock(wall, Blocks.STONE.defaultBlockState(), 3);
        ClayBall collidingBall = new ClayBall(level, owner, ClayShooterSpecification.ADVANCED, 1.0F, false);
        collidingBall.setPos(wall.getX() - 1.0D, wall.getY() + 0.5D, wall.getZ() + 0.5D);
        collidingBall.setDeltaMovement(1.0D, 0.0D, 0.0D);
        level.addFreshEntity(collidingBall);

        helper.runAfterDelay(4, () -> {
            requireClose(target.getHealth(), initialHealth - 4.0F, "server projectile damage");
            require(damagingBall.isRemoved(), "Entity-hit projectile remained alive");
            require(collidingBall.isRemoved(), "Block-hit projectile remained alive");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 40)
    public static void teleportBallAcceptsOnlySafeAuthorizedDestinations(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(3, 3, 3));
        level.setBlock(origin.below(), Blocks.STONE.defaultBlockState(), 3);
        player.setPos(Vec3.atBottomCenterOf(origin));
        BlockPos safeBlock = helper.absolutePos(new BlockPos(1, 3, 1));
        level.setBlock(safeBlock.below(), Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(safeBlock, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(safeBlock.above(), Blocks.AIR.defaultBlockState(), 3);
        Vec3 safeTarget = Vec3.atBottomCenterOf(safeBlock);
        require(TeleportBall.isSafeDestination(player, level, safeTarget), "Open destination was rejected");
        TeleportBall teleportBall = new TeleportBall(
                level, player, ClayShooterSpecification.TELEPORTER, 1.0F, true);
        teleportBall.setPos(safeTarget.add(0.0D, 2.0D, 0.0D));
        teleportBall.setDeltaMovement(0.0D, -1.0D, 0.0D);
        level.addFreshEntity(teleportBall);

        helper.runAfterDelay(4, () -> {
            require(teleportBall.isRemoved(), "Impact did not remove the teleport projectile");
            require(player.position().distanceTo(safeTarget) <= 0.02D,
                    "Projectile impact did not synchronize the player position");

            Vec3 positionAfterSafeTeleport = player.position();
            BlockPos blockedDestination = safeBlock.offset(3, 0, 0);
            level.setBlock(blockedDestination, Blocks.STONE.defaultBlockState(), 3);
            require(!TeleportBall.tryTeleport(player, level, Vec3.atBottomCenterOf(blockedDestination)),
                    "Blocked destination was accepted");
            require(player.position().equals(positionAfterSafeTeleport), "Blocked teleport moved the player");
            require(!TeleportBall.tryTeleport(player, level,
                            new Vec3(safeTarget.x, level.getMaxBuildHeight(), safeTarget.z)),
                    "Out-of-build-height destination was accepted");

            Consumer<EntityTeleportEvent> denyThisPlayer = event -> {
                if (event.getEntity() == player) {
                    event.setCanceled(true);
                }
            };
            NeoForge.EVENT_BUS.addListener(EntityTeleportEvent.class, denyThisPlayer);
            try {
                require(!TeleportBall.tryTeleport(player, level, safeTarget), "Canceled permission event was ignored");
            } finally {
                NeoForge.EVENT_BUS.unregister(denyThisPlayer);
            }
            require(player.position().equals(positionAfterSafeTeleport), "Canceled teleport moved the player");
            Consumer<EntityTeleportEvent> redirectIntoBlock = event -> {
                if (event.getEntity() == player) {
                    Vec3 blocked = Vec3.atBottomCenterOf(blockedDestination);
                    event.setTargetX(blocked.x);
                    event.setTargetY(blocked.y);
                    event.setTargetZ(blocked.z);
                }
            };
            NeoForge.EVENT_BUS.addListener(EntityTeleportEvent.class, redirectIntoBlock);
            TeleportBall blockedBall = new TeleportBall(level, player,
                    ClayShooterSpecification.TELEPORTER, 1.0F, true);
            blockedBall.setPos(safeTarget.add(0.0D, 2.5D, 0.0D));
            blockedBall.setDeltaMovement(0.0D, -1.0D, 0.0D);
            level.addFreshEntity(blockedBall);
            helper.runAfterDelay(8, () -> {
                NeoForge.EVENT_BUS.unregister(redirectIntoBlock);
                require(blockedBall.isRemoved(), "Blocked impact did not remove the projectile");
                require(player.position().equals(positionAfterSafeTeleport), "Blocked impact moved the player");
                helper.succeed();
            });
        });
    }

    @GameTest(template = "empty", batch = "clayium_weapon_border", timeoutTicks = 30)
    public static void teleportBallChecksSideImpactBorderAndDimension(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setNoGravity(true);
        BlockPos wall = helper.absolutePos(new BlockPos(5, 5, 5));
        level.setBlock(wall, Blocks.STONE.defaultBlockState(), 3);
        player.setPos(wall.getX() - 4.0D, wall.getY(), wall.getZ() + 0.5D);
        TeleportBall sideBall = new TeleportBall(level, player,
                ClayShooterSpecification.TELEPORTER, 1.0F, true);
        sideBall.setPos(wall.getX() - 1.5D, wall.getY() + 0.5D, wall.getZ() + 0.5D);
        sideBall.setDeltaMovement(1.0D, 0.0D, 0.0D);
        level.addFreshEntity(sideBall);
        helper.runAfterDelay(4, () -> {
            require(sideBall.isRemoved(), "Side impact did not remove projectile");
            require(Math.abs(player.getX() - (wall.getX() - player.getBbWidth() * 0.5D - 0.01D)) < 0.1D,
                    "Side impact did not use the wall-face offset");

            double oldCenterX = level.getWorldBorder().getCenterX();
            double oldCenterZ = level.getWorldBorder().getCenterZ();
            double oldSize = level.getWorldBorder().getSize();
            level.getWorldBorder().setCenter(wall.getX(), wall.getZ());
            level.getWorldBorder().setSize(4.0D);
            BlockPos outsideWall = wall.offset(5, 0, 0);
            level.setBlock(outsideWall, Blocks.STONE.defaultBlockState(), 3);
            Vec3 beforeBorderShot = player.position();
            TeleportBall borderBall = new TeleportBall(level, player,
                    ClayShooterSpecification.TELEPORTER, 1.0F, true);
            borderBall.setPos(outsideWall.getX() - 1.5D, outsideWall.getY() + 0.5D, outsideWall.getZ() + 0.5D);
            borderBall.setDeltaMovement(1.0D, 0.0D, 0.0D);
            level.addFreshEntity(borderBall);
            helper.runAfterDelay(8, () -> {
                level.getWorldBorder().setCenter(oldCenterX, oldCenterZ);
                level.getWorldBorder().setSize(oldSize);
                require(borderBall.isRemoved(), "Border impact did not remove projectile");
                require(Math.abs(player.getX() - beforeBorderShot.x) < 0.01D
                        && Math.abs(player.getZ() - beforeBorderShot.z) < 0.01D,
                        "Outside-border impact moved its owner");

                ServerLevel nether = level.getServer().getLevel(Level.NETHER);
                require(nether != null, "Nether level is unavailable");
                player.teleportTo(nether, 0.5D, 100.0D, 0.5D, java.util.Set.of(), 0.0F, 0.0F);
                Vec3 netherPosition = player.position();
                TeleportBall otherDimensionBall = new TeleportBall(level, player,
                        ClayShooterSpecification.TELEPORTER, 1.0F, true);
                otherDimensionBall.setPos(wall.getX() - 1.5D, wall.getY() + 0.5D, wall.getZ() + 0.5D);
                otherDimensionBall.setDeltaMovement(1.0D, 0.0D, 0.0D);
                level.addFreshEntity(otherDimensionBall);
                helper.runAfterDelay(12, () -> {
                    require(otherDimensionBall.isRemoved(), "Cross-dimension impact did not remove projectile");
                    require(player.level() == nether
                            && Math.abs(player.getX() - netherPosition.x) < 0.01D
                            && Math.abs(player.getZ() - netherPosition.z) < 0.01D,
                            "Cross-dimension impact moved its owner");
                    helper.succeed();
                });
            });
        });
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private static void requireClose(double actual, double expected, String label) {
        require(Math.abs(actual - expected) <= EPSILON,
                label + " expected " + expected + " but was " + actual);
    }

    private WeaponGameTests() {
    }
}
