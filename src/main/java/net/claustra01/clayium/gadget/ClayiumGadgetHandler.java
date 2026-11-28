package net.claustra01.clayium.gadget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.component.ClayiumDataComponentTypes.FilterConfigData;
import net.claustra01.clayium.filter.ClayiumFilterMatcher;
import net.claustra01.clayium.item.GadgetHolderItem;
import net.claustra01.clayium.item.GadgetItem;
import net.claustra01.clayium.item.GadgetItem.GadgetFamily;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Applies Clayium Gadget Holder passive/active effects (Health, Long Arm, Flight, Auto Eat, Repeatedly Attack)
 * identically to original {@code Gadget*} and {@code ItemGadgetHolder} behavior.
 */
@EventBusSubscriber(modid = ClayiumMod.MOD_ID)
public final class ClayiumGadgetHandler {
    public static final ResourceLocation HEALTH_MODIFIER_ID = ClayiumMod.id("gadget_health");
    public static final ResourceLocation LONG_ARM_MODIFIER_ID = ClayiumMod.id("gadget_long_arm");

    private static final Map<FlightKey, Integer> PREVIOUS_FLIGHT_LEVEL = new ConcurrentHashMap<>();

    private record FlightKey(UUID playerId, boolean clientSide) {}

    public record GadgetScanResult(
            int overclockLevel,
            int flightLevel,
            int healthLevel,
            int autoEatLevel,
            int repeatedlyAttackLevel,
            int longArmLevel,
            List<ItemStack> autoEatStacks) {
        public static final GadgetScanResult EMPTY = new GadgetScanResult(-1, -1, -1, -1, -1, -1, List.of());
    }

    private ClayiumGadgetHandler() {}

    public static GadgetScanResult scanGadgets(@Nullable Player player) {
        if (player == null) {
            return GadgetScanResult.EMPTY;
        }
        int overclock = -1;
        int flight = -1;
        int health = -1;
        int autoEat = -1;
        int repAttack = -1;
        int longArm = -1;
        List<ItemStack> autoEatStacks = null;

        Inventory inv = player.getInventory();
        for (ItemStack holderStack : inv.items) {
            if (holderStack.getItem() instanceof GadgetHolderItem) {
                ItemContainerContents contents = holderStack.get(ClayiumDataComponents.GADGET_INVENTORY.get());
                if (contents == null) {
                    continue;
                }
                for (ItemStack sub : contents.nonEmptyItems()) {
                    if (!(sub.getItem() instanceof GadgetItem gadget)) {
                        continue;
                    }
                    int lvl = gadget.getLevel();
                    switch (gadget.getFamily()) {
                        case OVERCLOCK -> overclock = Math.max(overclock, lvl);
                        case FLIGHT -> flight = Math.max(flight, lvl);
                        case HEALTH -> health = Math.max(health, lvl);
                        case AUTO_EAT -> {
                            autoEat = Math.max(autoEat, lvl);
                            if (autoEatStacks == null) {
                                autoEatStacks = new ArrayList<>();
                            }
                            autoEatStacks.add(sub);
                        }
                        case REPEATEDLY_ATTACK -> repAttack = Math.max(repAttack, lvl);
                        case LONG_ARM -> longArm = Math.max(longArm, lvl);
                        case BLANK -> {}
                    }
                }
            }
        }
        for (ItemStack holderStack : inv.offhand) {
            if (holderStack.getItem() instanceof GadgetHolderItem) {
                ItemContainerContents contents = holderStack.get(ClayiumDataComponents.GADGET_INVENTORY.get());
                if (contents == null) {
                    continue;
                }
                for (ItemStack sub : contents.nonEmptyItems()) {
                    if (!(sub.getItem() instanceof GadgetItem gadget)) {
                        continue;
                    }
                    int lvl = gadget.getLevel();
                    switch (gadget.getFamily()) {
                        case OVERCLOCK -> overclock = Math.max(overclock, lvl);
                        case FLIGHT -> flight = Math.max(flight, lvl);
                        case HEALTH -> health = Math.max(health, lvl);
                        case AUTO_EAT -> {
                            autoEat = Math.max(autoEat, lvl);
                            if (autoEatStacks == null) {
                                autoEatStacks = new ArrayList<>();
                            }
                            autoEatStacks.add(sub);
                        }
                        case REPEATEDLY_ATTACK -> repAttack = Math.max(repAttack, lvl);
                        case LONG_ARM -> longArm = Math.max(longArm, lvl);
                        case BLANK -> {}
                    }
                }
            }
        }
        for (ItemStack holderStack : inv.armor) {
            if (holderStack.getItem() instanceof GadgetHolderItem) {
                ItemContainerContents contents = holderStack.get(ClayiumDataComponents.GADGET_INVENTORY.get());
                if (contents == null) {
                    continue;
                }
                for (ItemStack sub : contents.nonEmptyItems()) {
                    if (!(sub.getItem() instanceof GadgetItem gadget)) {
                        continue;
                    }
                    int lvl = gadget.getLevel();
                    switch (gadget.getFamily()) {
                        case OVERCLOCK -> overclock = Math.max(overclock, lvl);
                        case FLIGHT -> flight = Math.max(flight, lvl);
                        case HEALTH -> health = Math.max(health, lvl);
                        case AUTO_EAT -> {
                            autoEat = Math.max(autoEat, lvl);
                            if (autoEatStacks == null) {
                                autoEatStacks = new ArrayList<>();
                            }
                            autoEatStacks.add(sub);
                        }
                        case REPEATEDLY_ATTACK -> repAttack = Math.max(repAttack, lvl);
                        case LONG_ARM -> longArm = Math.max(longArm, lvl);
                        case BLANK -> {}
                    }
                }
            }
        }

        return new GadgetScanResult(
                overclock,
                flight,
                health,
                autoEat,
                repAttack,
                longArm,
                autoEatStacks != null ? List.copyOf(autoEatStacks) : List.of()
        );
    }

    @SubscribeEvent
    public static void onPlayerTickPre(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        GadgetScanResult scan = scanGadgets(player);

        applyHealth(player, scan.healthLevel());
        applyLongArm(player, scan.longArmLevel());
        applyFlight(player, scan.flightLevel());

        if (!player.level().isClientSide && scan.autoEatLevel() >= 0 && player.canEat(false)) {
            for (ItemStack eatStack : scan.autoEatStacks()) {
                if (eatStack.getItem() instanceof GadgetItem g && player.canEat(false)) {
                    tryAutoEat(player, eatStack, g.getLevel());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        PREVIOUS_FLIGHT_LEVEL.remove(new FlightKey(id, false));
        PREVIOUS_FLIGHT_LEVEL.remove(new FlightKey(id, true));
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        Entity attacker = event.getSource().getDirectEntity();
        if (attacker instanceof Player player && scanGadgets(player).repeatedlyAttackLevel() >= 0) {
            event.getEntity().invulnerableTime = 0;
        }
    }

    public static double healthBonusForLevel(int level) {
        if (level < 0) {
            return 0.0;
        }
        return 20.0 * (double) (level + 1) * (double) (level + 1);
    }

    public static double longArmBonusForLevel(int level) {
        return switch (level) {
            case 0 -> 3.0;
            case 1 -> 7.0;
            case 2 -> 20.0;
            default -> level > 2 ? 20.0 : 0.0;
        };
    }

    public static float computeDampenedFallDistance(float currentFallDistance, double deltaY) {
        if (deltaY >= 0.0) {
            return 0.0F;
        }
        int t = (int) (-deltaY / 0.05);
        return Math.min(currentFallDistance, (float) (t * (t - 1)) * 0.025F);
    }

    private static void applyHealth(Player player, int healthLevel) {
        AttributeInstance attr = player.getAttribute(Attributes.MAX_HEALTH);
        if (attr == null) {
            return;
        }
        double expected = healthBonusForLevel(healthLevel);
        AttributeModifier existing = attr.getModifier(HEALTH_MODIFIER_ID);
        if (healthLevel >= 0) {
            if (existing == null || Double.compare(existing.amount(), expected) != 0) {
                attr.removeModifier(HEALTH_MODIFIER_ID);
                attr.addTransientModifier(new AttributeModifier(HEALTH_MODIFIER_ID, expected, AttributeModifier.Operation.ADD_VALUE));
            }
        } else if (existing != null) {
            attr.removeModifier(HEALTH_MODIFIER_ID);
            if (!player.level().isClientSide && player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
        }
    }

    private static void applyLongArm(Player player, int longArmLevel) {
        double expected = longArmBonusForLevel(longArmLevel);
        applyAddAttribute(player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE), LONG_ARM_MODIFIER_ID, expected, longArmLevel >= 0);
        applyAddAttribute(player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE), LONG_ARM_MODIFIER_ID, expected, longArmLevel >= 0);
    }

    private static void applyAddAttribute(
            @Nullable AttributeInstance attr,
            ResourceLocation id,
            double amount,
            boolean active) {
        if (attr == null) {
            return;
        }
        AttributeModifier existing = attr.getModifier(id);
        if (active) {
            if (existing == null || Double.compare(existing.amount(), amount) != 0) {
                attr.removeModifier(id);
                attr.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
            }
        } else if (existing != null) {
            attr.removeModifier(id);
        }
    }

    private static void applyFlight(Player player, int flightLevel) {
        FlightKey key = new FlightKey(player.getUUID(), player.level().isClientSide);
        int oldMode = PREVIOUS_FLIGHT_LEVEL.getOrDefault(key, -1);

        if (flightLevel >= 0) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                if (!player.level().isClientSide) {
                    player.onUpdateAbilities();
                }
            }
            Vec3 motion = player.getDeltaMovement();
            player.fallDistance = computeDampenedFallDistance(player.fallDistance, motion.y);
        } else if (oldMode != -1 && !player.isCreative() && !player.isSpectator()) {
            if (player.getAbilities().mayfly || player.getAbilities().flying) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                if (!player.level().isClientSide) {
                    player.onUpdateAbilities();
                }
            }
        }
        PREVIOUS_FLIGHT_LEVEL.put(key, flightLevel);
    }

    public static boolean matchesAutoEatFilter(ItemStack autoEatStack, ItemStack candidate) {
        if (candidate.isEmpty()) {
            return false;
        }
        FilterConfigData cfg = autoEatStack.get(ClayiumDataComponents.FILTER_CONFIG.get());
        if (cfg == null) {
            return true;
        }
        boolean hasAny = false;
        for (ItemStack filter : cfg.slots().nonEmptyItems()) {
            hasAny = true;
            if (ClayiumFilterMatcher.isFilter(filter)) {
                if (ClayiumFilterMatcher.match(filter, candidate)) {
                    return true;
                }
            } else if (ItemStack.isSameItem(filter, candidate)) {
                return true;
            }
        }
        return !hasAny;
    }

    /**
     * Evaluates and consumes the highest-efficiency matching food item if now is optimal timing
     * (level 0 = zero-waste timing, level 1 = eager timing whenever hungry).
     *
     * @return {@code true} if an item was eaten.
     */
    public static boolean tryAutoEat(Player player, ItemStack autoEatStack, int modeLevel) {
        FoodData foodData = player.getFoodData();
        int pHeal = foodData.getFoodLevel();
        float pSat = foodData.getSaturationLevel();

        List<ItemStack> items = player.getInventory().items;
        int bestIndex = selectBestFoodSlot(player, items, autoEatStack, modeLevel, pHeal, pSat);
        if (bestIndex < 0) {
            return false;
        }
        ItemStack food = items.get(bestIndex);
        ItemStack remaining = food.finishUsingItem(player.level(), player);
        player.getInventory().setItem(bestIndex, remaining);
        player.getInventory().setChanged();
        return true;
    }

    /**
     * Pure selection helper matching original {@code GadgetAutoEat.update} scoring and zero-waste check.
     */
    public static int selectBestFoodSlot(
            @Nullable Player player,
            List<ItemStack> items,
            ItemStack autoEatStack,
            int modeLevel,
            int pHeal,
            float pSat) {
        boolean bestTiming = true;
        float bestRate = 0.0F;
        int bestIndex = -1;

        for (int i = 0; i < items.size(); i++) {
            ItemStack cand = items.get(i);
            if (cand.isEmpty() || !matchesAutoEatFilter(autoEatStack, cand)) {
                continue;
            }
            FoodProperties props = cand.getFoodProperties(player);
            if (props == null) {
                continue;
            }
            int heal = props.nutrition();
            float sat = props.saturation();
            if ((float) heal + sat <= 0.0F) {
                continue;
            }

            float val0 = (float) Math.min(heal, 20 - pHeal) + Math.min(sat, (float) Math.min(pHeal + heal, 20) - pSat);
            boolean best0 = true;

            if (modeLevel == 0) {
                int tHeal = pHeal;
                float tSat = pSat;
                while (tSat > 0.0F || tHeal > 0) {
                    if (tSat > 0.0F) {
                        tSat--;
                    }
                    if (tSat <= 0.0F) {
                        tSat = 0.0F;
                        tHeal--;
                    }
                    float val1 = (float) Math.min(heal, 20 - tHeal)
                            + Math.min(sat, (float) Math.min(tHeal + heal, 20) - tSat);
                    if (val1 > val0) {
                        val0 = val1;
                        best0 = false;
                    }
                }
            }

            float rate = val0 / ((float) heal + sat);
            if (bestRate < rate) {
                bestRate = rate;
                bestIndex = i;
                bestTiming = best0;
            }
        }

        return (bestIndex >= 0 && bestTiming) ? bestIndex : -1;
    }
}
