package net.claustra01.clayium.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.lang.reflect.Field;
import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.block.ITieredBlock;
import net.claustra01.clayium.gadget.ClayiumGadgetHandler;
import net.claustra01.clayium.gadget.ClayiumGadgetHandler.GadgetScanResult;
import net.claustra01.clayium.item.ClayShooterItem;
import net.claustra01.clayium.item.ClaySteelToolHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

/**
 * Client-only gameplay event hooks for Gadgets (Overclock / Flight physics),
 * Clay Steel Tool highlights, and Clay Shooter FOV zoom.
 */
@EventBusSubscriber(modid = ClayiumMod.MOD_ID, value = Dist.CLIENT)
public final class ClayiumClientGameplayEvents {
    private static final float MODE1_VELOCITY = 0.7F;
    private static final float MODE2_ACCELERATION = 0.9F;
    private static final float MODE2_DIVISION = 1.1F;

    private static Field destroyDelayField;
    private static Field rightClickDelayField;
    private static boolean delayFieldsLookedUp = false;

    private ClayiumClientGameplayEvents() {}

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        GadgetScanResult scan = ClayiumGadgetHandler.scanGadgets(player);

        int delay = computeOverclockDelay(player, scan);
        if (delay <= 3) {
            applyClientOverclockDelay(mc, delay);
        }

        if (scan.flightLevel() >= 1 && player.getAbilities().flying) {
            applyClientFlightPhysics(mc, player, scan.flightLevel());
        }
    }

    public static int computeOverclockDelay(Player player, GadgetScanResult scan) {
        int delay = 5;
        if (scan.overclockLevel() >= 0) {
            delay = Math.min(delay, Math.max(0, 3 - scan.overclockLevel()));
        }
        Inventory inv = player.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ITieredBlock tiered) {
                String path = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock()).getPath();
                if (path.endsWith("_overclocker")) {
                    int tier = tiered.getTier();
                    if (tier >= 10 && tier <= 13) {
                        delay = Math.min(delay, 13 - tier);
                    }
                }
            }
        }
        return delay;
    }

    private static void applyClientOverclockDelay(Minecraft mc, int delay) {
        ensureDelayFields();
        MultiPlayerGameMode gameMode = mc.gameMode;
        if (gameMode != null && destroyDelayField != null) {
            try {
                int current = destroyDelayField.getInt(gameMode);
                if (current >= delay) {
                    destroyDelayField.setInt(gameMode, delay);
                }
            } catch (IllegalAccessException ignored) {
            }
        }
        if (rightClickDelayField != null) {
            try {
                int current = rightClickDelayField.getInt(mc);
                if (current >= delay + 1) {
                    rightClickDelayField.setInt(mc, delay + 1);
                }
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    private static void ensureDelayFields() {
        if (delayFieldsLookedUp) {
            return;
        }
        delayFieldsLookedUp = true;
        try {
            destroyDelayField = MultiPlayerGameMode.class.getDeclaredField("destroyDelay");
            destroyDelayField.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            ClayiumMod.LOGGER.debug("Unable to reflect MultiPlayerGameMode.destroyDelay", e);
        }
        try {
            rightClickDelayField = Minecraft.class.getDeclaredField("rightClickDelay");
            rightClickDelayField.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            ClayiumMod.LOGGER.debug("Unable to reflect Minecraft.rightClickDelay", e);
        }
    }

    private static void applyClientFlightPhysics(Minecraft mc, LocalPlayer player, int flightLevel) {
        Options options = mc.options;
        float sin = Mth.sin(player.getYRot() * Mth.DEG_TO_RAD);
        float cos = Mth.cos(player.getYRot() * Mth.DEG_TO_RAD);
        Vec3 delta = player.getDeltaMovement();

        double vy = delta.y;
        float forward = (float) (delta.z * cos - delta.x * sin);
        float strafe = (float) (delta.z * sin + delta.x * cos);

        boolean jumping = player.input.jumping;
        boolean sneaking = player.input.shiftKeyDown;

        if (sneaking) {
            if (flightLevel >= 2) {
                vy = (vy - MODE2_ACCELERATION) / MODE2_DIVISION;
            } else {
                vy = -MODE1_VELOCITY;
            }
        }
        if (jumping) {
            if (flightLevel >= 2) {
                vy = (vy + MODE2_ACCELERATION) / MODE2_DIVISION;
            } else {
                vy = MODE1_VELOCITY;
            }
        }
        if (jumping == sneaking) {
            vy = 0.0;
        }

        boolean upDown = options.keyUp.isDown();
        boolean downDown = options.keyDown.isDown();
        boolean leftDown = options.keyLeft.isDown();
        boolean rightDown = options.keyRight.isDown();

        if (upDown) {
            if (flightLevel >= 2) {
                forward = (forward + MODE2_ACCELERATION) / MODE2_DIVISION;
            } else {
                forward = MODE1_VELOCITY;
            }
        }
        if (downDown) {
            if (flightLevel >= 2) {
                forward = (forward - MODE2_ACCELERATION) / MODE2_DIVISION;
            } else {
                forward = -MODE1_VELOCITY;
            }
        }
        if (!upDown && !downDown) {
            forward = 0.0F;
        }

        if (leftDown) {
            if (flightLevel >= 2) {
                strafe = (strafe + MODE2_ACCELERATION) / MODE2_DIVISION;
            } else {
                strafe = MODE1_VELOCITY;
            }
        }
        if (rightDown) {
            if (flightLevel >= 2) {
                strafe = (strafe - MODE2_ACCELERATION) / MODE2_DIVISION;
            } else {
                strafe = -MODE1_VELOCITY;
            }
        }
        if (!leftDown && !rightDown) {
            strafe = 0.0F;
        }

        double vx = strafe * cos - forward * sin;
        double vz = forward * cos + strafe * sin;
        player.setDeltaMovement(vx, vy, vz);
    }

    @SubscribeEvent
    public static void onRenderBlockHighlight(RenderHighlightEvent.Block event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (!ClaySteelToolHelper.isClaySteelTool(held)) {
            return;
        }
        Level level = player.level();
        BlockPos origin = event.getTarget().getBlockPos();
        List<BlockPos> targets = ClaySteelToolHelper.getHarvestedPositions(held, origin, player, event.getTarget().getDirection());
        PoseStack poseStack = event.getPoseStack();
        VertexConsumer consumer = event.getMultiBufferSource().getBuffer(RenderType.lines());
        Vec3 camPos = event.getCamera().getPosition();

        for (BlockPos pos : targets) {
            if (pos.equals(origin)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (!state.isAir()) {
                AABB box = new AABB(pos).move(-camPos.x, -camPos.y, -camPos.z).inflate(0.002);
                LevelRenderer.renderLineBox(poseStack, consumer, box, 0.0F, 0.0F, 0.0F, 0.4F);
            }
        }
    }

    @SubscribeEvent
    public static void onComputeFovModifier(ComputeFovModifierEvent event) {
        Player player = event.getPlayer();
        if (!player.isUsingItem()) {
            return;
        }
        ItemStack stack = player.getUseItem();
        if (!(stack.getItem() instanceof ClayShooterItem shooter) || !shooter.isCharger()) {
            return;
        }
        int chargeTime = shooter.getChargeTime();
        int i = Math.min(player.getTicksUsingItem(), chargeTime);
        float t = shooter.getInitialVelocity() * (float) shooter.getLifespanTicks();
        float u = (float) i / (float) chargeTime;
        float k = Math.max(0.0F, t * u - 10.0F);
        k = k + 3600.0F / (k + 60.0F) - 60.0F;
        float m = 2.5F / (k + 2.5F);
        event.setNewFovModifier(event.getFovModifier() * m + (1.0F - m) * 0.1F);
    }
}
