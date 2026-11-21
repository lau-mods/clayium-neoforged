package net.claustra01.clayium.machine.trade;

import java.util.Comparator;
import javax.annotation.Nullable;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.energy.ClayEnergyStorage;
import net.claustra01.clayium.recipe.ClayiumRecipeEnergy;
import net.claustra01.clayium.registry.TradeRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class AutoTraderBlockEntity extends BlockEntity implements MenuProvider {
    public static final int TIER = 8;
    public static final int INPUT_A_SLOT = 0;
    public static final int INPUT_B_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int FUEL_SLOT = 3;
    public static final int MAX_VISIBLE_OFFERS = 10;
    public static final long ENERGY_PER_TRADE = ClayiumRecipeEnergy.e(1.0D, TIER);

    private final ItemStackHandler inventory = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case INPUT_A_SLOT, INPUT_B_SLOT -> true;
                case FUEL_SLOT -> ClayEnergyStorage.fuelValue(stack).internalValue() > 0L;
                default -> false;
            };
        }
    };
    private final ClayEnergyStorage energy = new ClayEnergyStorage();
    @Nullable
    private Merchant cachedMerchant;

    private int selectedOffer;
    private State state = State.NO_MERCHANT;

    public AutoTraderBlockEntity(BlockPos pos, BlockState state) {
        super(TradeRegistration.BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AutoTraderBlockEntity autoTrader) {
        if (level instanceof ServerLevel) {
            autoTrader.tryTrade();
        }
    }

    public boolean tryTrade() {
        Merchant merchant = merchant();
        cachedMerchant = merchant;
        if (merchant == null) {
            return stop(State.NO_MERCHANT);
        }
        if (merchant.getTradingPlayer() != null) {
            return stop(State.MERCHANT_BUSY);
        }
        if (merchant.getOffers().isEmpty()) {
            return stop(State.NO_OFFERS);
        }
        if (selectedOffer < 0 || selectedOffer >= Math.min(MAX_VISIBLE_OFFERS, merchant.getOffers().size())) {
            return stop(State.NO_OFFERS);
        }
        MerchantOffer offer = merchant.getOffers().get(selectedOffer);
        if (offer.isOutOfStock()) {
            return stop(State.OUT_OF_STOCK);
        }
        ItemStack result = offer.assemble();
        if (!canAcceptOutput(result)) {
            return stop(State.OUTPUT_FULL);
        }
        InputPlan inputs = inputPlan(offer);
        if (inputs == null) {
            return stop(State.MISSING_INPUT);
        }
        if (!canPower()) {
            return stop(State.WAITING_FOR_ENERGY);
        }

        if (!energy.consume(ClayEnergy.ofInternal(ENERGY_PER_TRADE), inventory.getStackInSlot(FUEL_SLOT))) {
            return stop(State.WAITING_FOR_ENERGY);
        }
        offer.take(
                inventory.getStackInSlot(inputs.costASlot()),
                inputs.costBSlot() < 0 ? ItemStack.EMPTY : inventory.getStackInSlot(inputs.costBSlot()));
        addOutput(result);
        merchant.notifyTrade(offer);
        state = State.TRADED;
        setChanged();
        return true;
    }

    @Nullable
    public Merchant merchant() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        AABB search = new AABB(
                worldPosition.getX() - 1.0D, worldPosition.getY() + 1.0D, worldPosition.getZ() - 1.0D,
                worldPosition.getX() + 2.0D, worldPosition.getY() + 5.0D, worldPosition.getZ() + 2.0D);
        return serverLevel.getEntitiesOfClass(Entity.class, search,
                        entity -> entity.isAlive() && entity instanceof Merchant)
                .stream()
                .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(
                        worldPosition.getX() + 0.5D, worldPosition.getY() + 1.0D, worldPosition.getZ() + 0.5D)))
                .map(entity -> (Merchant)entity)
                .orElse(null);
    }

    public MerchantOffer offer(int index) {
        Merchant merchant = cachedMerchant;
        return merchant != null && index >= 0 && index < merchant.getOffers().size()
                ? merchant.getOffers().get(index) : null;
    }

    public MerchantOffers visibleOffers() {
        MerchantOffers offers = new MerchantOffers();
        if (cachedMerchant != null) {
            for (int index = 0; index < offerCount(); index++) {
                offers.add(cachedMerchant.getOffers().get(index).copy());
            }
        }
        return offers;
    }

    public int offerCount() {
        Merchant merchant = cachedMerchant;
        return merchant == null ? 0 : Math.min(MAX_VISIBLE_OFFERS, merchant.getOffers().size());
    }

    public void selectOffer(int index) {
        Merchant merchant = merchant();
        if (merchant != null && index >= 0 && index < Math.min(MAX_VISIBLE_OFFERS, merchant.getOffers().size())) {
            cachedMerchant = merchant;
            selectedOffer = index;
            state = State.READY;
            setChanged();
        }
    }

    private InputPlan inputPlan(MerchantOffer offer) {
        ItemStack inputA = inventory.getStackInSlot(INPUT_A_SLOT);
        ItemStack inputB = inventory.getStackInSlot(INPUT_B_SLOT);
        if (offer.getCostB().isEmpty()) {
            if (offer.satisfiedBy(inputA, ItemStack.EMPTY)) {
                return new InputPlan(INPUT_A_SLOT, -1);
            }
            return offer.satisfiedBy(inputB, ItemStack.EMPTY)
                    ? new InputPlan(INPUT_B_SLOT, -1) : null;
        }
        if (offer.satisfiedBy(inputA, inputB)) {
            return new InputPlan(INPUT_A_SLOT, INPUT_B_SLOT);
        }
        return offer.satisfiedBy(inputB, inputA)
                ? new InputPlan(INPUT_B_SLOT, INPUT_A_SLOT) : null;
    }

    private boolean canAcceptOutput(ItemStack result) {
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        return !result.isEmpty() && result.getCount() <= result.getMaxStackSize()
                && (output.isEmpty() || ItemStack.isSameItemSameComponents(output, result)
                        && output.getCount() + result.getCount() <= output.getMaxStackSize());
    }

    private void addOutput(ItemStack result) {
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            inventory.setStackInSlot(OUTPUT_SLOT, result.copy());
        } else {
            output.grow(result.getCount());
        }
    }

    private boolean canPower() {
        return energy.stored().internalValue() >= ENERGY_PER_TRADE
                || ClayEnergyStorage.fuelValue(inventory.getStackInSlot(FUEL_SLOT)).internalValue()
                        >= ENERGY_PER_TRADE - energy.stored().internalValue();
    }

    private boolean stop(State stoppedState) {
        if (state != stoppedState) {
            state = stoppedState;
            setChanged();
        }
        return false;
    }

    public ItemStackHandler inventory() {
        return inventory;
    }

    public ClayEnergyStorage energyStorage() {
        return energy;
    }

    public int selectedOffer() {
        return selectedOffer;
    }

    public State state() {
        return state;
    }

    public IItemHandler itemHandler(@Nullable Direction direction) {
        return new TradeItemHandler(direction);
    }

    public Container dropInventory() {
        SimpleContainer dropped = new SimpleContainer(inventory.getSlots());
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            dropped.setItem(slot, inventory.getStackInSlot(slot).copy());
            inventory.setStackInSlot(slot, ItemStack.EMPTY);
        }
        return dropped;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AutoTraderMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.put("energy", energy.serializeNBT(registries));
        tag.putInt("selected_offer", selectedOffer);
        tag.putString("state", state.name());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        energy.deserializeNBT(registries, tag.getCompound("energy"));
        selectedOffer = Math.max(0, tag.getInt("selected_offer"));
        state = tag.contains("state") ? State.valueOf(tag.getString("state")) : State.NO_MERCHANT;
    }

    public enum State {
        READY,
        TRADED,
        NO_MERCHANT,
        MERCHANT_BUSY,
        NO_OFFERS,
        OUT_OF_STOCK,
        OUTPUT_FULL,
        MISSING_INPUT,
        WAITING_FOR_ENERGY
    }

    private record InputPlan(int costASlot, int costBSlot) {
    }

    private final class TradeItemHandler implements IItemHandler {
        @Nullable
        private final Direction direction;

        private TradeItemHandler(@Nullable Direction direction) {
            this.direction = direction;
        }

        @Override
        public int getSlots() {
            return direction == Direction.DOWN ? 1 : direction == null ? 4 : 3;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(mappedSlot(slot));
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            int mapped = mappedSlot(slot);
            return mapped == OUTPUT_SLOT ? stack : inventory.insertItem(mapped, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return mappedSlot(slot) == OUTPUT_SLOT
                    ? inventory.extractItem(OUTPUT_SLOT, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(mappedSlot(slot));
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            int mapped = mappedSlot(slot);
            return mapped != OUTPUT_SLOT && inventory.isItemValid(mapped, stack);
        }

        private int mappedSlot(int slot) {
            if (direction == Direction.DOWN) {
                return OUTPUT_SLOT;
            }
            if (direction == null) {
                return slot;
            }
            return switch (slot) {
                case 0 -> INPUT_A_SLOT;
                case 1 -> INPUT_B_SLOT;
                default -> FUEL_SLOT;
            };
        }
    }
}
