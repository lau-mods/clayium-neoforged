package net.claustra01.clayium.menu;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.AutoTraderBlockEntity;
import net.claustra01.clayium.network.AutoTraderOffersPayload;
import net.claustra01.clayium.network.IClayButtonMenu;
import net.claustra01.clayium.registry.ClayiumMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Menu for Auto Trader (Tier 8): 2 trade input slots, 1 output slot, CE readout,
 * recipe index selection buttons, and server-to-client {@link MerchantOffers} synchronization.
 */
public class AutoTraderMenu extends ClayiumBaseMenu implements IClayButtonMenu {
    public static final int BUTTON_NEXT_RECIPE = 1;
    public static final int BUTTON_PREV_RECIPE = 2;

    @Nullable
    private final AutoTraderBlockEntity trader;
    private final Container container;
    private final ContainerData data;
    private final int[] energyShorts = new int[4];
    private int clientRecipeIndex = 0;
    private MerchantOffers clientOffers = new MerchantOffers();
    @Nullable
    private final ServerPlayer serverViewer;
    private int lastSentHash = Integer.MIN_VALUE;

    public AutoTraderMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readBlockPos(), buf.readBlockPos());
    }

    public AutoTraderMenu(int containerId, Inventory playerInv, BlockPos targetPos, BlockPos interactionPos) {
        super(ClayiumMenus.AUTO_TRADER.get(), containerId, playerInv, interactionPos, null, null);
        BlockEntity be = playerInv.player.level().getBlockEntity(targetPos);
        this.trader = be instanceof AutoTraderBlockEntity at ? at : null;
        this.container = this.trader != null ? wrapContainer(this.trader) : new SimpleContainer(4);
        this.serverViewer = playerInv.player instanceof ServerPlayer sp ? sp : null;
        initializeLayout();

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                if (trader == null) {
                    return index < 4 ? energyShorts[index] : clientRecipeIndex;
                }
                if (index < 4) {
                    int[] buf = new int[4];
                    putLongIntoDataSlots(trader.getClayEnergy(), buf);
                    return buf[index];
                }
                return trader.currentRecipeIndex;
            }

            @Override
            public void set(int index, int value) {
                if (index < 4) {
                    energyShorts[index] = value;
                } else {
                    clientRecipeIndex = value;
                }
            }

            @Override
            public int getCount() {
                return 5;
            }
        };
        addDataSlots(this.data);
    }

    @Override
    protected void configureLayout() {
        this.machineGuiSizeY += 16;
    }

    @Override
    protected void addMachineSlots() {
        addSlot(new ClaySlot(this.container, 0, 36, 53, SlotTextureKind.IMPORT_1));
        addSlot(new ClaySlot(this.container, 1, 62, 53, SlotTextureKind.IMPORT_2));
        addSlot(new ClayResultSlot(this.container, 2, 120, 53, SlotTextureKind.LARGE));
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        syncOffersToViewer(false);
    }

    public void syncOffersToViewer(boolean force) {
        if (this.serverViewer == null || this.trader == null) {
            return;
        }
        MerchantOffers offers = this.trader.merchantOffers != null ? this.trader.merchantOffers : new MerchantOffers();
        int idx = this.trader.currentRecipeIndex;
        int hash = idx * 31 + offers.size() * 97;
        for (int i = 0; i < offers.size(); i++) {
            hash = hash * 31 + offers.get(i).getUses() + (offers.get(i).isOutOfStock() ? 1000 : 0);
        }
        if (force || hash != this.lastSentHash) {
            this.lastSentHash = hash;
            PacketDistributor.sendToPlayer(this.serverViewer, new AutoTraderOffersPayload(this.containerId, idx, offers));
        }
    }

    public void applyClientOffers(int currentIndex, MerchantOffers offers) {
        this.clientRecipeIndex = currentIndex;
        this.clientOffers = offers != null ? offers : new MerchantOffers();
    }

    public MerchantOffers getOffers() {
        if (this.trader != null && !this.level.isClientSide && this.trader.merchantOffers != null) {
            return this.trader.merchantOffers;
        }
        return this.clientOffers;
    }

    public int getCurrentRecipeIndex() {
        if (this.trader != null && !this.level.isClientSide) {
            return this.trader.currentRecipeIndex;
        }
        return this.clientRecipeIndex;
    }

    public long getClayEnergy() {
        if (this.trader != null && !this.level.isClientSide) {
            return this.trader.getClayEnergy();
        }
        return readLongFrom4Shorts(
                this.data.get(0),
                this.data.get(1),
                this.data.get(2),
                this.data.get(3)
        );
    }

    @Override
    public void handleGuiButton(ServerPlayer player, int buttonId) {
        if (this.trader == null || !stillValid(player)) {
            return;
        }
        if (buttonId == BUTTON_NEXT_RECIPE) {
            int max = this.trader.merchantOffers != null ? Math.max(0, this.trader.merchantOffers.size() - 1) : 0;
            this.trader.setCurrentRecipeIndex(Math.min(max, this.trader.currentRecipeIndex + 1));
            syncOffersToViewer(true);
            broadcastChanges();
        } else if (buttonId == BUTTON_PREV_RECIPE) {
            this.trader.setCurrentRecipeIndex(Math.max(0, this.trader.currentRecipeIndex - 1));
            syncOffersToViewer(true);
            broadcastChanges();
        }
    }

    @Override
    protected boolean transferStackToMachineInventory(ItemStack stack) {
        return moveItemStackTo(stack, 0, 2, false);
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.trader != null && this.trader.isRemoved()) {
            return false;
        }
        return super.stillValid(player);
    }
}
