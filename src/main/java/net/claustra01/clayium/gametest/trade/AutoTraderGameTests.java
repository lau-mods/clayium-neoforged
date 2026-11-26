package net.claustra01.clayium.gametest.trade;

import java.util.Optional;
import io.netty.buffer.Unpooled;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.machine.trade.AutoTraderBlockEntity;
import net.claustra01.clayium.machine.trade.AutoTraderMenu;
import net.claustra01.clayium.machine.trade.TradeSelectPayload;
import net.claustra01.clayium.machine.trade.TradeSnapshotPayload;
import net.claustra01.clayium.registry.TradeRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.connection.ConnectionType;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AutoTraderGameTests {
    private static final BlockPos MACHINE_POS = new BlockPos(3, 2, 3);

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void selectedTradeUsesDiscountedCostsCeOutputUsesAndVillagerExperience(GameTestHelper helper) {
        AutoTraderBlockEntity autoTrader = placeAutoTrader(helper);
        Villager villager = villager(helper);
        MerchantOffer offer = new MerchantOffer(
                new ItemCost(Items.EMERALD, 5),
                Optional.of(new ItemCost(Items.BOOK, 2)),
                new ItemStack(Items.DIAMOND, 2),
                8,
                4,
                0.0F);
        offer.setSpecialPriceDiff(-2);
        villager.getOffers().clear();
        villager.getOffers().add(offer);

        autoTrader.inventory().setStackInSlot(AutoTraderBlockEntity.INPUT_A_SLOT, new ItemStack(Items.EMERALD, 4));
        autoTrader.inventory().setStackInSlot(AutoTraderBlockEntity.INPUT_B_SLOT, new ItemStack(Items.BOOK, 3));
        autoTrader.energyStorage().add(ClayEnergy.ofInternal(AutoTraderBlockEntity.ENERGY_PER_TRADE * 2));
        long energyBefore = autoTrader.energyStorage().stored().internalValue();

        require(autoTrader.tryTrade(), "A funded discounted two-input trade did not execute");
        require(autoTrader.inventory().getStackInSlot(AutoTraderBlockEntity.INPUT_A_SLOT).getCount() == 1,
                "The trader did not consume the current discounted first cost of three emeralds");
        require(autoTrader.inventory().getStackInSlot(AutoTraderBlockEntity.INPUT_B_SLOT).getCount() == 1,
                "The trader did not consume the exact second cost");
        ItemStack output = autoTrader.inventory().getStackInSlot(AutoTraderBlockEntity.OUTPUT_SLOT);
        require(output.is(Items.DIAMOND) && output.getCount() == 2,
                "The selected offer result was not inserted exactly once");
        require(autoTrader.energyStorage().stored().internalValue()
                        == energyBefore - AutoTraderBlockEntity.ENERGY_PER_TRADE,
                "The auto trader consumed the wrong amount of Clay Energy");
        require(offer.getUses() == 1, "MerchantOffer uses were not updated through notifyTrade");
        require(villager.getVillagerXp() == offer.getXp(), "Villager experience was not rewarded by the trade");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void failedConditionsAreAtomicAndSelectionPayloadIsServerValidated(GameTestHelper helper) {
        AutoTraderBlockEntity autoTrader = placeAutoTrader(helper);
        autoTrader.inventory().setStackInSlot(AutoTraderBlockEntity.INPUT_A_SLOT, new ItemStack(Items.EMERALD, 16));
        autoTrader.energyStorage().add(ClayEnergy.ofInternal(AutoTraderBlockEntity.ENERGY_PER_TRADE * 8));

        Snapshot before = snapshot(autoTrader, 0);
        require(!autoTrader.tryTrade() && autoTrader.state() == AutoTraderBlockEntity.State.NO_MERCHANT,
                "The trader did not stop when its merchant was absent");
        require(before.equals(snapshot(autoTrader, 0)), "No-merchant failure changed inventory or CE");

        Villager villager = villager(helper);
        MerchantOffer first = offer(Items.EMERALD, 2, Items.BREAD, 1, 1);
        first.setToOutOfStock();
        MerchantOffer second = offer(Items.EMERALD, 4, Items.APPLE, 2, 8);
        villager.getOffers().clear();
        villager.getOffers().add(first);
        villager.getOffers().add(second);
        before = snapshot(autoTrader, first.getUses());
        require(!autoTrader.tryTrade() && autoTrader.state() == AutoTraderBlockEntity.State.OUT_OF_STOCK,
                "An exhausted offer was executed");
        require(before.equals(snapshot(autoTrader, first.getUses())), "Exhausted-offer failure changed state");

        autoTrader.selectOffer(1);
        autoTrader.inventory().setStackInSlot(AutoTraderBlockEntity.OUTPUT_SLOT, new ItemStack(Items.DIRT, 64));
        before = snapshot(autoTrader, second.getUses());
        require(!autoTrader.tryTrade() && autoTrader.state() == AutoTraderBlockEntity.State.OUTPUT_FULL,
                "An incompatible full output did not stop trading");
        require(before.equals(snapshot(autoTrader, second.getUses())), "Full-output failure changed state");

        autoTrader.inventory().setStackInSlot(AutoTraderBlockEntity.OUTPUT_SLOT, ItemStack.EMPTY);
        autoTrader.inventory().setStackInSlot(AutoTraderBlockEntity.INPUT_A_SLOT, new ItemStack(Items.EMERALD, 3));
        before = snapshot(autoTrader, second.getUses());
        require(!autoTrader.tryTrade() && autoTrader.state() == AutoTraderBlockEntity.State.MISSING_INPUT,
                "Insufficient materials did not stop trading");
        require(before.equals(snapshot(autoTrader, second.getUses())), "Missing-material failure changed state");

        autoTrader.inventory().setStackInSlot(AutoTraderBlockEntity.INPUT_A_SLOT, new ItemStack(Items.EMERALD, 4));
        autoTrader.energyStorage().consume(autoTrader.energyStorage().stored());
        before = snapshot(autoTrader, second.getUses());
        require(!autoTrader.tryTrade() && autoTrader.state() == AutoTraderBlockEntity.State.WAITING_FOR_ENERGY,
                "Insufficient CE did not stop trading");
        require(before.equals(snapshot(autoTrader, second.getUses())), "CE failure changed inventory, CE, or uses");

        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        player.setPos(helper.absolutePos(MACHINE_POS).getCenter());
        autoTrader.energyStorage().add(ClayEnergy.ofInternal(AutoTraderBlockEntity.ENERGY_PER_TRADE));
        villager.setTradingPlayer(player);
        before = snapshot(autoTrader, second.getUses());
        require(!autoTrader.tryTrade() && autoTrader.state() == AutoTraderBlockEntity.State.MERCHANT_BUSY,
                "A merchant locked by a player was traded with concurrently");
        require(before.equals(snapshot(autoTrader, second.getUses())), "Merchant-lock failure changed state");
        villager.setTradingPlayer(null);

        ItemStack namedResult = new ItemStack(Items.GOLDEN_APPLE);
        namedResult.set(DataComponents.CUSTOM_NAME, Component.literal("Selected trade result"));
        MerchantOffer discounted = new MerchantOffer(new ItemCost(Items.EMERALD, 5),
                Optional.of(new ItemCost(Items.BOOK, 1)), namedResult, 8, 2, 0.0F);
        discounted.setSpecialPriceDiff(-2);
        villager.getOffers().add(discounted);
        AutoTraderMenu menu = (AutoTraderMenu)autoTrader.createMenu(29, player.getInventory(), player);
        player.containerMenu = menu;
        TradeSelectPayload.apply(player, new TradeSelectPayload(autoTrader.getBlockPos(), 2));
        require(autoTrader.selectedOffer() == 2, "A valid selected-offer payload was rejected");
        TradeSelectPayload.apply(player, new TradeSelectPayload(autoTrader.getBlockPos(), 9));
        require(autoTrader.selectedOffer() == 2, "An out-of-range offer payload changed the selection");
        TradeSelectPayload.apply(player, new TradeSelectPayload(autoTrader.getBlockPos().above(), 1));
        require(autoTrader.selectedOffer() == 2, "A payload for another block changed the selection");
        player.setPos(autoTrader.getBlockPos().getCenter().add(16.0D, 0.0D, 0.0D));
        TradeSelectPayload.apply(player, new TradeSelectPayload(autoTrader.getBlockPos(), 1));
        require(autoTrader.selectedOffer() == 2, "A distant player changed the selected offer");
        player.setPos(autoTrader.getBlockPos().getCenter());
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(
                Unpooled.buffer(), helper.getLevel().registryAccess(), ConnectionType.OTHER);
        try {
            TradeSnapshotPayload.STREAM_CODEC.encode(buffer, menu.snapshot());
            menu.applySnapshot(TradeSnapshotPayload.STREAM_CODEC.decode(buffer));
        } finally {
            buffer.release();
        }
        require(menu.offerCount() == 3 && menu.selectedOffer() == 2
                        && menu.selectedCostA().is(Items.EMERALD) && menu.selectedCostA().getCount() == 3
                        && menu.selectedCostB().is(Items.BOOK)
                        && namedResult.getHoverName().equals(menu.selectedResult().getHoverName())
                        && menu.energyPerTrade() == AutoTraderBlockEntity.ENERGY_PER_TRADE,
                "The client snapshot lost the selected offer, discounted costs, components, or long CE value");
        for (int index = 3; index < 12; index++) {
            villager.getOffers().add(offer(Items.EMERALD, 1, Items.BREAD, 1, 8));
        }
        autoTrader.selectOffer(11);
        menu.applySnapshot(menu.snapshot());
        require(menu.offerCount() == 12 && menu.selectedOffer() == 11,
                "The auto trader imposed a non-original ten-offer limit");
        player.containerMenu = player.inventoryMenu;
        while (villager.getOffers().size() > 3) {
            villager.getOffers().remove(villager.getOffers().size() - 1);
        }
        autoTrader.selectOffer(2);
        villager.getOffers().remove(2);
        before = snapshot(autoTrader, second.getUses());
        require(!autoTrader.tryTrade() && autoTrader.state() == AutoTraderBlockEntity.State.NO_OFFERS
                        && autoTrader.selectedOffer() == 2,
                "A removed selected offer changed selection or executed another trade");
        require(before.equals(snapshot(autoTrader, second.getUses())),
                "A removed selected offer changed inventory, CE, or trade uses");
        helper.succeed();
    }

    private static MerchantOffer offer(
            net.minecraft.world.item.Item input, int inputCount,
            net.minecraft.world.item.Item output, int outputCount, int maximumUses) {
        return new MerchantOffer(
                new ItemCost(input, inputCount), new ItemStack(output, outputCount), maximumUses, 2, 0.0F);
    }

    private static AutoTraderBlockEntity placeAutoTrader(GameTestHelper helper) {
        helper.setBlock(MACHINE_POS, TradeRegistration.AUTO_TRADER.get());
        require(helper.getBlockEntity(MACHINE_POS) instanceof AutoTraderBlockEntity,
                "Auto trader did not create its block entity");
        return helper.getBlockEntity(MACHINE_POS);
    }

    private static Villager villager(GameTestHelper helper) {
        Villager villager = helper.spawnWithNoFreeWill(EntityType.VILLAGER, MACHINE_POS.above());
        villager.setVillagerXp(0);
        return villager;
    }

    private static Snapshot snapshot(AutoTraderBlockEntity autoTrader, int uses) {
        ItemStack inputA = autoTrader.inventory().getStackInSlot(AutoTraderBlockEntity.INPUT_A_SLOT);
        ItemStack inputB = autoTrader.inventory().getStackInSlot(AutoTraderBlockEntity.INPUT_B_SLOT);
        ItemStack output = autoTrader.inventory().getStackInSlot(AutoTraderBlockEntity.OUTPUT_SLOT);
        ItemStack fuel = autoTrader.inventory().getStackInSlot(AutoTraderBlockEntity.FUEL_SLOT);
        return new Snapshot(
                inputA.getItem(), inputA.getCount(), inputB.getItem(), inputB.getCount(),
                output.getItem(), output.getCount(), fuel.getItem(), fuel.getCount(),
                autoTrader.energyStorage().stored().internalValue(),
                uses);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private record Snapshot(
            net.minecraft.world.item.Item inputA, int inputACount,
            net.minecraft.world.item.Item inputB, int inputBCount,
            net.minecraft.world.item.Item output, int outputCount,
            net.minecraft.world.item.Item fuel, int fuelCount,
            long energy,
            int uses) {
    }

    private AutoTraderGameTests() {
    }
}
