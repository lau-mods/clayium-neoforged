package net.claustra01.clayium.machine.trade;

import net.claustra01.clayium.registry.TradeRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class AutoTraderMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final AutoTraderBlockEntity autoTrader;
    private final ServerPlayer viewer;
    private MerchantOffers offers = new MerchantOffers();
    private int selectedOffer;
    private AutoTraderBlockEntity.State state = AutoTraderBlockEntity.State.NO_MERCHANT;
    private long storedEnergy;
    private long energyPerTrade;
    private long lastSnapshotHash = Long.MIN_VALUE;
    private boolean snapshotApplied;

    public AutoTraderMenu(int containerId, Inventory inventory, BlockPos pos) {
        this(containerId, inventory, autoTraderAt(inventory, pos));
    }

    public AutoTraderMenu(
            int containerId, Inventory inventory, AutoTraderBlockEntity autoTrader) {
        super(TradeRegistration.MENU.get(), containerId);
        this.autoTrader = autoTrader;
        this.pos = autoTrader == null ? BlockPos.ZERO : autoTrader.getBlockPos();
        this.viewer = inventory.player instanceof ServerPlayer serverPlayer ? serverPlayer : null;
        if (autoTrader != null) {
            addSlot(new SlotItemHandler(autoTrader.inventory(), AutoTraderBlockEntity.INPUT_A_SLOT, 36, 53));
            addSlot(new SlotItemHandler(autoTrader.inventory(), AutoTraderBlockEntity.INPUT_B_SLOT, 62, 53));
            addSlot(new SlotItemHandler(autoTrader.inventory(), AutoTraderBlockEntity.OUTPUT_SLOT, 120, 53));
            addSlot(new SlotItemHandler(autoTrader.inventory(), AutoTraderBlockEntity.FUEL_SLOT, 8, 53));
        }
        addPlayerInventory(inventory);

    }

    private static AutoTraderBlockEntity autoTraderAt(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        return blockEntity instanceof AutoTraderBlockEntity autoTrader ? autoTrader : null;
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 116 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 174));
        }
    }

    public AutoTraderBlockEntity autoTrader() {
        return autoTrader;
    }

    public BlockPos pos() {
        return pos;
    }

    public TradeSnapshotPayload snapshot() {
        return new TradeSnapshotPayload(containerId, autoTrader.visibleOffers(), autoTrader.selectedOffer(),
                autoTrader.state(), autoTrader.energyStorage().stored().internalValue(),
                AutoTraderBlockEntity.ENERGY_PER_TRADE);
    }

    public void applySnapshot(TradeSnapshotPayload snapshot) {
        if (snapshot.containerId() != containerId) {
            return;
        }
        snapshotApplied = true;
        offers = snapshot.offers();
        selectedOffer = snapshot.selectedOffer();
        state = snapshot.state();
        storedEnergy = snapshot.storedEnergy();
        energyPerTrade = snapshot.energyPerTrade();
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (viewer == null || autoTrader == null) {
            return;
        }
        TradeSnapshotPayload snapshot = snapshot();
        long hash = snapshot.contentHash();
        if (hash != lastSnapshotHash) {
            lastSnapshotHash = hash;
            PacketDistributor.sendToPlayer(viewer, snapshot);
        }
    }

    public int selectedOffer() {
        return snapshotApplied || viewer == null ? selectedOffer : autoTrader.selectedOffer();
    }

    public int offerCount() {
        return snapshotApplied || viewer == null ? offers.size() : autoTrader.offerCount();
    }

    public AutoTraderBlockEntity.State state() {
        return snapshotApplied || viewer == null ? state : autoTrader.state();
    }

    public long storedEnergy() {
        return snapshotApplied || viewer == null ? storedEnergy : autoTrader.energyStorage().stored().internalValue();
    }

    private MerchantOffer selected() {
        int index = selectedOffer();
        return snapshotApplied || viewer == null ? index >= 0 && index < offers.size() ? offers.get(index) : null
                : autoTrader.offer(index);
    }

    public ItemStack selectedCostA() {
        MerchantOffer offer = selected();
        return offer == null ? ItemStack.EMPTY : offer.getCostA();
    }

    public ItemStack selectedCostB() {
        MerchantOffer offer = selected();
        return offer == null ? ItemStack.EMPTY : offer.getCostB();
    }

    public ItemStack selectedResult() {
        MerchantOffer offer = selected();
        return offer == null ? ItemStack.EMPTY : offer.getResult();
    }

    public int selectedUses() {
        MerchantOffer offer = selected();
        return offer == null ? 0 : offer.getUses();
    }

    public int selectedMaxUses() {
        MerchantOffer offer = selected();
        return offer == null ? 0 : offer.getMaxUses();
    }

    public int tier() {
        return AutoTraderBlockEntity.TIER;
    }

    public long energyPerTrade() {
        return snapshotApplied || viewer == null ? energyPerTrade : AutoTraderBlockEntity.ENERGY_PER_TRADE;
    }

    public ItemStack offerResult(int index) {
        MerchantOffer offer = snapshotApplied || viewer == null ? index >= 0 && index < offers.size() ? offers.get(index) : null
                : autoTrader.offer(index);
        return offer == null ? ItemStack.EMPTY : offer.getResult();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (autoTrader == null || index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem() || !slot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }
        ItemStack original = slot.getItem().copy();
        ItemStack moving = slot.getItem();
        if (index < 4) {
            if (!moveItemStackTo(moving, 4, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            boolean moved = autoTrader.inventory().isItemValid(AutoTraderBlockEntity.FUEL_SLOT, moving)
                    && moveItemStackTo(moving, 3, 4, false);
            if (!moved) {
                moved = moveItemStackTo(moving, 0, 2, false);
            }
            if (!moved) {
                return ItemStack.EMPTY;
            }
        }
        if (moving.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return autoTrader != null && !autoTrader.isRemoved()
                && player.level().getBlockEntity(pos) == autoTrader
                && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }
}
