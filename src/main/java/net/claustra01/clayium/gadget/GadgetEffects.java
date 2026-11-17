package net.claustra01.clayium.gadget;

import java.util.EnumSet;
import java.util.Set;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.GadgetRegistration;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForgeMod;

public final class GadgetEffects {
    public static final ResourceLocation HEALTH_MODIFIER_ID = ClayiumMod.id("gadget_extra_health");
    public static final ResourceLocation LONG_ARM_MODIFIER_ID = ClayiumMod.id("gadget_long_arm");
    public static final ResourceLocation FLIGHT_MODIFIER_ID = ClayiumMod.id("gadget_flight");
    private static final int[] OVERCLOCK_INTERVALS = {0, 8, 4, 2, 1};
    private static final double[] HEALTH_BONUSES = {0.0D, 20.0D, 80.0D, 180.0D};
    private static final double[] LONG_ARM_BONUSES = {0.0D, 3.0D, 7.0D, 20.0D};
    private static final float SUPERSONIC_FLIGHT_SPEED = 0.2F;

    public static void update(Player player) {
        Set<GadgetType> active = activeGadgets(player);
        int healthRank = highestRank(active, GadgetType.Category.HEALTH);
        int longArmRank = highestRank(active, GadgetType.Category.LONG_ARM);
        int flightRank = highestRank(active, GadgetType.Category.FLIGHT);
        int autoEatRank = highestRank(active, GadgetType.Category.AUTO_EAT);

        applyModifier(player.getAttribute(Attributes.MAX_HEALTH), HEALTH_MODIFIER_ID, HEALTH_BONUSES[healthRank]);
        applyModifier(player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE), LONG_ARM_MODIFIER_ID,
                LONG_ARM_BONUSES[longArmRank]);
        applyModifier(player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT), FLIGHT_MODIFIER_ID,
                flightRank == 0 ? 0.0D : 1.0D);

        GadgetPlayerState state = player.getData(GadgetRegistration.PLAYER_STATE);
        updateFlightSpeed(player, state, flightRank == 3);
        if (flightRank == 1 && player.getAbilities().flying && player.tickCount % 80 == 0) {
            player.causeFoodExhaustion(1.0F);
        }
        updateAutoEat(player, state, autoEatRank);
    }

    public static void clear(Player player) {
        applyModifier(player.getAttribute(Attributes.MAX_HEALTH), HEALTH_MODIFIER_ID, 0.0D);
        applyModifier(player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE), LONG_ARM_MODIFIER_ID, 0.0D);
        applyModifier(player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT), FLIGHT_MODIFIER_ID, 0.0D);
        GadgetPlayerState state = player.getData(GadgetRegistration.PLAYER_STATE);
        updateFlightSpeed(player, state, false);
        state.autoEatCooldown(0);
    }

    public static Set<GadgetType> activeGadgets(Player player) {
        Set<GadgetType> active = EnumSet.noneOf(GadgetType.class);
        for (ItemStack stack : player.getInventory().items) {
            collect(stack, active);
        }
        for (ItemStack stack : player.getInventory().offhand) {
            collect(stack, active);
        }
        return active;
    }

    public static boolean isActive(Player player, GadgetType type) {
        return activeGadgets(player).contains(type);
    }

    public static int operationInterval(Player player) {
        return OVERCLOCK_INTERVALS[highestRank(activeGadgets(player), GadgetType.Category.OVERCLOCK)];
    }

    private static void collect(ItemStack stack, Set<GadgetType> active) {
        if (!stack.is(GadgetRegistration.GADGET_HOLDER.get())) {
            return;
        }
        for (ItemStack gadget : GadgetHolderItem.gadgets(stack)) {
            if (gadget.getItem() instanceof GadgetItem gadgetItem && gadgetItem.type() != GadgetType.TEMPLATE) {
                active.add(gadgetItem.type());
            }
        }
    }

    private static int highestRank(Set<GadgetType> gadgets, GadgetType.Category category) {
        return gadgets.stream()
                .filter(gadget -> gadget.category() == category)
                .mapToInt(GadgetType::rank)
                .max()
                .orElse(0);
    }

    private static void applyModifier(AttributeInstance attribute, ResourceLocation id, double amount) {
        if (attribute == null) {
            return;
        }
        AttributeModifier current = attribute.getModifier(id);
        if (amount == 0.0D) {
            if (current != null) {
                attribute.removeModifier(id);
            }
            return;
        }
        if (current != null && current.amount() == amount) {
            return;
        }
        if (current != null) {
            attribute.removeModifier(id);
        }
        if (id.equals(HEALTH_MODIFIER_ID)) {
            attribute.addPermanentModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        } else {
            attribute.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void updateFlightSpeed(Player player, GadgetPlayerState state, boolean supersonic) {
        if (supersonic && player.getAbilities().getFlyingSpeed() != SUPERSONIC_FLIGHT_SPEED) {
            if (!state.supersonicSpeed()) {
                state.previousFlightSpeed(player.getAbilities().getFlyingSpeed());
            }
            player.getAbilities().setFlyingSpeed(SUPERSONIC_FLIGHT_SPEED);
            state.supersonicSpeed(true);
            syncAbilities(player);
        } else if (!supersonic && state.supersonicSpeed()) {
            if (player.getAbilities().getFlyingSpeed() == SUPERSONIC_FLIGHT_SPEED) {
                player.getAbilities().setFlyingSpeed(state.previousFlightSpeed());
            }
            state.supersonicSpeed(false);
            syncAbilities(player);
        }
    }

    private static void syncAbilities(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.onUpdateAbilities();
        }
    }

    private static void updateAutoEat(Player player, GadgetPlayerState state, int rank) {
        if (state.autoEatCooldown() > 0) {
            state.autoEatCooldown(state.autoEatCooldown() - 1);
        }
        if (rank == 0 || !player.getFoodData().needsFood() || state.autoEatCooldown() > 0) {
            return;
        }
        int missingFood = 20 - player.getFoodData().getFoodLevel();
        int selected = -1;
        int selectedNutrition = rank == 1 ? -1 : Integer.MAX_VALUE;
        for (int slot = 0; slot < player.getInventory().items.size(); slot++) {
            ItemStack food = player.getInventory().items.get(slot);
            FoodProperties properties = food.get(DataComponents.FOOD);
            var stewEffects = food.get(DataComponents.SUSPICIOUS_STEW_EFFECTS);
            if (properties == null
                    || properties.effects().stream()
                            .anyMatch(effect -> !effect.effect().getEffect().value().isBeneficial())
                    || (stewEffects != null && stewEffects.effects().stream()
                            .anyMatch(effect -> !effect.effect().value().isBeneficial()))) {
                continue;
            }
            if (rank == 1) {
                if (properties.nutrition() <= missingFood && properties.nutrition() > selectedNutrition) {
                    selected = slot;
                    selectedNutrition = properties.nutrition();
                }
            } else if (properties.nutrition() < selectedNutrition) {
                selected = slot;
                selectedNutrition = properties.nutrition();
            }
        }
        if (selected < 0) {
            return;
        }
        ItemStack food = player.getInventory().items.get(selected);
        ItemStack result = food.finishUsingItem(player.level(), player);
        if (food.isEmpty()) {
            player.getInventory().items.set(selected, result);
        } else if (!result.isEmpty() && result != food) {
            player.getInventory().add(result);
        }
        state.autoEatCooldown(10);
    }

    private GadgetEffects() {
    }
}
