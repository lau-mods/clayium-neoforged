package net.claustra01.clayium.gametest.gadget;

import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.gadget.GadgetEffects;
import net.claustra01.clayium.gadget.GadgetEvents;
import net.claustra01.clayium.gadget.GadgetHolderItem;
import net.claustra01.clayium.gadget.GadgetHolderMenu;
import net.claustra01.clayium.gadget.GadgetType;
import net.claustra01.clayium.registry.GadgetRegistration;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class GadgetGameTests {
    private static final double EPSILON = 0.000_001D;

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void gadgetAttributesUseExactHealthReachAndFlightValues(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        requireClose(player.getMaxHealth(), 20.0D, "base maximum health");
        requireClose(player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE), 4.5D, "base block range");
        GadgetType[] health = {
                GadgetType.EXTRA_HEALTH_I, GadgetType.EXTRA_HEALTH_II, GadgetType.EXTRA_HEALTH_III
        };
        double[] healthValues = {40.0D, 100.0D, 200.0D};
        for (int index = 0; index < health.length; index++) {
            equip(player, health[index]);
            requireClose(player.getMaxHealth(), healthValues[index], "maximum health rank " + (index + 1));
        }

        GadgetType[] reach = {GadgetType.LONG_ARM, GadgetType.LONGER_ARM, GadgetType.LONGEST_ARM};
        double[] reachValues = {7.5D, 11.5D, 24.5D};
        for (int index = 0; index < reach.length; index++) {
            equip(player, reach[index]);
            requireClose(player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE), reachValues[index],
                    "block reach rank " + (index + 1));
        }

        for (GadgetType flight : List.of(
                GadgetType.FLIGHT, GadgetType.FREE_FLIGHT, GadgetType.SUPERSONIC_FLIGHT)) {
            equip(player, flight);
            require(player.getAttributeValue(NeoForgeMod.CREATIVE_FLIGHT) > 0.0D,
                    "Flight permission was not granted for " + flight.id());
            require(player.mayFly(), "Flight was not available for " + flight.id());
            requireClose(player.getAbilities().getFlyingSpeed(), flight == GadgetType.SUPERSONIC_FLIGHT
                    ? 0.2D : 0.05D, "flying speed for " + flight.id());
        }
        equip(player, GadgetType.FLIGHT);
        player.getAbilities().flying = true;
        player.tickCount = 80;
        float exhaustion = player.getFoodData().getExhaustionLevel();
        GadgetEffects.update(player);
        requireClose(player.getFoodData().getExhaustionLevel(), exhaustion + 1.0D,
                "ordinary flight exhaustion");
        equip(player, GadgetType.FREE_FLIGHT);
        exhaustion = player.getFoodData().getExhaustionLevel();
        GadgetEffects.update(player);
        requireClose(player.getFoodData().getExhaustionLevel(), exhaustion,
                "free flight exhaustion");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void activeGadgetsDriveEatingAttackAndOperationIntervals(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        GadgetType[] overclocks = {
                GadgetType.OVERCLOCK_I,
                GadgetType.OVERCLOCK_II,
                GadgetType.OVERCLOCK_III,
                GadgetType.OVERCLOCK_IV
        };
        require(GadgetEffects.operationInterval(player) == 0, "Base operation interval was changed");
        int[] expectedIntervals = {8, 4, 2, 1};
        for (int index = 0; index < overclocks.length; index++) {
            equip(player, overclocks[index]);
            require(GadgetEffects.operationInterval(player) == expectedIntervals[index],
                    "Overclock operation interval is wrong for " + overclocks[index].id());
        }

        require(!GadgetEffects.isActive(player, GadgetType.REPEATED_ATTACK),
                "Repeated attack was active without the gadget");
        equip(player, GadgetType.REPEATED_ATTACK);
        require(GadgetEffects.isActive(player, GadgetType.REPEATED_ATTACK),
                "Repeated attack did not become active");

        equip(player, GadgetType.ECONOMICAL_AUTO_EAT);
        player.getInventory().add(new ItemStack(Items.APPLE, 2));
        player.getFoodData().setFoodLevel(19);
        GadgetEffects.update(player);
        require(count(player, Items.APPLE) == 2, "Economical auto eat wasted food at one missing hunger point");
        player.getFoodData().setFoodLevel(16);
        GadgetEffects.update(player);
        require(count(player, Items.APPLE) == 1 && player.getFoodData().getFoodLevel() == 20,
                "Economical auto eat did not consume an exactly useful food");

        equip(player, GadgetType.AUTO_EAT);
        player.getInventory().add(new ItemStack(Items.APPLE));
        player.getInventory().add(new ItemStack(Items.PUFFERFISH));
        player.getFoodData().setFoodLevel(19);
        GadgetEffects.update(player);
        require(count(player, Items.APPLE) == 0 && count(player, Items.PUFFERFISH) == 1
                        && player.getFoodData().getFoodLevel() == 20,
                "Normal auto eat did not eat as soon as food was needed");

        equip(player, GadgetType.AUTO_EAT);
        ItemStack harmfulStew = new ItemStack(Items.SUSPICIOUS_STEW);
        harmfulStew.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, new SuspiciousStewEffects(
                List.of(new SuspiciousStewEffects.Entry(MobEffects.POISON, 100))));
        player.getInventory().add(harmfulStew);
        player.getFoodData().setFoodLevel(10);
        GadgetEffects.update(player);
        require(count(player, Items.SUSPICIOUS_STEW) == 1 && !player.hasEffect(MobEffects.POISON),
                "Auto eat consumed a harmful suspicious stew");

        equip(player, GadgetType.AUTO_EAT);
        player.getInventory().add(new ItemStack(Items.MUSHROOM_STEW));
        player.getFoodData().setFoodLevel(14);
        GadgetEffects.update(player);
        require(count(player, Items.BOWL) == 1, "Auto eat lost the stew bowl");

        require(GadgetRegistration.GADGETS_BY_TYPE.size() == 17,
                "The original gadget variants were not all registered");
        for (ResourceLocation id : BuiltInRegistries.ITEM.keySet()) {
            if (id.getNamespace().equals(ClayiumMod.MOD_ID)) {
                require(!id.getPath().contains("speed_gadget") && !id.getPath().contains("jump_gadget"),
                        "A non-original speed or jump gadget was registered: " + id);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void holderRemovalAndRepeatedEvaluationNeverStackModifiers(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack holder = holder(
                GadgetType.EXTRA_HEALTH_III,
                GadgetType.LONGEST_ARM,
                GadgetType.FREE_FLIGHT);
        player.getInventory().add(holder);
        GadgetEffects.update(player);
        GadgetEffects.update(player);
        requireClose(player.getMaxHealth(), 200.0D, "double-evaluated maximum health");
        requireClose(player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE), 24.5D,
                "double-evaluated block range");
        require(modifierCount(player, Attributes.MAX_HEALTH, GadgetEffects.HEALTH_MODIFIER_ID) == 1,
                "Maximum health modifier accumulated");
        require(modifierCount(player, Attributes.BLOCK_INTERACTION_RANGE, GadgetEffects.LONG_ARM_MODIFIER_ID) == 1,
                "Long-arm modifier accumulated");

        player.getInventory().clearContent();
        GadgetEffects.update(player);
        requireClose(player.getMaxHealth(), 20.0D, "maximum health after removing the holder");
        requireClose(player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE), 4.5D,
                "block range after removing the holder");
        require(player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT).getModifier(GadgetEffects.FLIGHT_MODIFIER_ID) == null,
                "Clayium flight remained after removing the holder");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void attachmentCloneAndForeignFlightPermissionSurviveLifecycleChanges(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack holder = holder(GadgetType.SUPERSONIC_FLIGHT, GadgetType.EXTRA_HEALTH_I);
        player.getInventory().add(holder);
        GadgetEffects.update(player);
        require(player.getData(GadgetRegistration.PLAYER_STATE).supersonicSpeed(),
                "Active gadget state was not stored in the player attachment");
        ItemStack equippedHolder = player.getInventory().items.stream()
                .filter(stack -> stack.is(GadgetRegistration.GADGET_HOLDER.get()))
                .findFirst()
                .orElseThrow();
        require(equippedHolder.copy().get(GadgetRegistration.GADGETS.get())
                        .equals(equippedHolder.get(GadgetRegistration.GADGETS.get())),
                "Holder gadgets did not survive an ItemStack copy");

        player.setHealth(35.0F);
        CompoundTag saved = player.saveWithoutId(new CompoundTag());
        GadgetEvents.login(new PlayerEvent.PlayerLoggedInEvent(player));
        GadgetEvents.changeDimension(new PlayerEvent.PlayerChangedDimensionEvent(
                player, net.minecraft.world.level.Level.OVERWORLD, net.minecraft.world.level.Level.NETHER));
        require(modifierCount(player, Attributes.MAX_HEALTH, GadgetEffects.HEALTH_MODIFIER_ID) == 1,
                "Relogin or dimension change duplicated a gadget modifier");
        requireClose(player.getHealth(), 35.0D, "health after relogin");
        requireClose(player.getAbilities().getFlyingSpeed(), 0.2D, "speed after relogin");
        Player loaded = helper.makeMockPlayer(GameType.SURVIVAL);
        loaded.load(saved);
        require(loaded.getData(GadgetRegistration.PLAYER_STATE).supersonicSpeed(),
                "Supersonic state was not restored from the player attachment");
        requireClose(loaded.getData(GadgetRegistration.PLAYER_STATE).previousFlightSpeed(), 0.05D,
                "saved previous flight speed");
        require(GadgetEffects.isActive(loaded, GadgetType.SUPERSONIC_FLIGHT),
                "Holder data component was not restored with the player inventory");
        GadgetEffects.update(loaded);
        requireClose(loaded.getHealth(), 35.0D, "loaded health");

        Player clone = helper.makeMockPlayer(GameType.SURVIVAL);
        clone.copyAttachmentsFrom(player, true);
        clone.getInventory().add(equippedHolder.copy());
        require(GadgetEffects.isActive(clone, GadgetType.SUPERSONIC_FLIGHT),
                "Cloned player did not retain the holder");
        GadgetEvents.clone(new PlayerEvent.Clone(clone, player, true));
        requireClose(clone.getAbilities().getFlyingSpeed(), 0.2D, "speed after death");
        require(modifierCount(clone, Attributes.MAX_HEALTH, GadgetEffects.HEALTH_MODIFIER_ID) == 1,
                "Health modifier was not restored after death");

        ResourceLocation foreignFlightId = ResourceLocation.fromNamespaceAndPath("test", "foreign_flight");
        player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT).addTransientModifier(new AttributeModifier(
                foreignFlightId, 1.0D, AttributeModifier.Operation.ADD_VALUE));
        player.getInventory().clearContent();
        GadgetEffects.update(player);
        require(player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT).getModifier(foreignFlightId) != null,
                "Removing a Clayium holder removed another mod's flight permission");

        player.getAbilities().mayfly = true;
        GadgetEvents.changeGameMode(new PlayerEvent.PlayerChangeGameModeEvent(
                player, GameType.SURVIVAL, GameType.CREATIVE));
        GadgetEffects.clear(player);
        require(player.getAbilities().mayfly,
                "Cleaning up Clayium gadgets removed creative-mode flight permission");
        requireClose(player.getAbilities().getFlyingSpeed(), 0.05D,
                "Supersonic flight speed was not restored after cleanup");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void holderMenuPersistsInstalledGadgets(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack holder = new ItemStack(GadgetRegistration.GADGET_HOLDER.get());
        player.getInventory().setItem(0, holder);
        GadgetHolderMenu menu = new GadgetHolderMenu(1, player.getInventory(), 0);
        menu.getSlot(0).set(new ItemStack(GadgetRegistration.GADGETS_BY_TYPE.get(GadgetType.EXTRA_HEALTH_I).get()));
        require(GadgetHolderItem.gadgets(holder).size() == 1, "Menu did not save the installed gadget");
        GadgetEffects.update(player);
        requireClose(player.getMaxHealth(), 40.0D, "health after using holder menu");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void creativeTransitionsPreserveVanillaFlight(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().add(holder(GadgetType.FLIGHT));
        GadgetEffects.update(player);
        require(player.mayFly(), "Gadget flight was unavailable in survival");
        player.setGameMode(GameType.CREATIVE);
        player.getInventory().clearContent();
        GadgetEffects.update(player);
        require(player.mayFly(), "Creative flight was removed with the holder");
        player.setGameMode(GameType.SURVIVAL);
        GadgetEffects.update(player);
        require(!player.mayFly(), "Gadget flight remained after returning to survival");
        helper.succeed();
    }

    private static void equip(Player player, GadgetType gadget) {
        GadgetEffects.clear(player);
        player.getInventory().clearContent();
        player.getInventory().add(holder(gadget));
        GadgetEffects.update(player);
    }

    private static ItemStack holder(GadgetType... gadgets) {
        ItemStack holder = new ItemStack(GadgetRegistration.GADGET_HOLDER.get());
        GadgetHolderItem.setGadgets(holder, java.util.Arrays.stream(gadgets)
                .map(gadget -> new ItemStack(GadgetRegistration.GADGETS_BY_TYPE.get(gadget).get()))
                .toList());
        return holder;
    }

    private static int count(Player player, net.minecraft.world.item.Item item) {
        return player.getInventory().items.stream()
                .filter(stack -> stack.is(item))
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    private static int modifierCount(
            Player player,
            net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
            ResourceLocation id) {
        return (int) player.getAttribute(attribute).getModifiers().stream()
                .filter(modifier -> modifier.id().equals(id))
                .count();
    }

    private static void requireClose(double actual, double expected, String description) {
        require(Math.abs(actual - expected) < EPSILON,
                "Wrong " + description + ": " + actual + " instead of " + expected);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private GadgetGameTests() {
    }
}
