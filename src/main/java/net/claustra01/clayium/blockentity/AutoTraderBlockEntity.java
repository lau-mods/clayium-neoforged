package net.claustra01.clayium.blockentity;

import java.util.List;
import javax.annotation.Nullable;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.transfer.ClayiumTransfer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Auto Trader (Tier 8): automatically trades with a {@link Merchant} entity standing above it using 10M CE per trade.
 */
public class AutoTraderBlockEntity extends TieredClayContainerBlockEntity {
    public static final long ENERGY_PER_TRADE = 10_000_000L;

    @Nullable
    public Merchant merchant;
    @Nullable
    public MerchantOffers merchantOffers;
    @Nullable
    public MerchantOffer currentOffer;
    public int currentRecipeIndex = 0;

    public AutoTraderBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.AUTO_TRADER.get(), pos, state, 4);
        ensureTierInitialized();
    }

    @Override
    protected void initParams() {
        this.items = NonNullList.withSize(4, ItemStack.EMPTY);
        this.clayEnergySlot = 3;
        this.listSlotsInsert.add(new int[]{0});
        this.listSlotsInsert.add(new int[]{1});
        this.listSlotsInsert.add(new int[]{0, 1});
        this.listSlotsInsert.add(new int[]{3});
        this.listSlotsExtract.add(new int[]{2});
        this.insertRoutes = new int[]{ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, 3, 0, 1};
        this.maxAutoExtract = new int[]{-1, -1, -1, 1};
        this.extractRoutes = new int[]{ROUTE_DISABLED, ROUTE_DISABLED, 0, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED};
        this.maxAutoInsert = new int[]{-1};
        this.slotsDrop = new int[]{0, 1, 2, 3};
        this.autoInsert = true;
        this.autoExtract = true;
    }

    @Override
    protected void initParamsByTier(int tier) {
        setDefaultTransportationParamsByTier(tier, ParamMode.MACHINE);
    }

    @Override
    public boolean acceptEnergyClay() {
        return true;
    }

    public void setCurrentRecipeIndex(int index) {
        this.currentRecipeIndex = Math.max(0, index);
        markForSync();
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (this.level == null || this.level.isClientSide) {
            return;
        }

        AABB scanBox = new AABB(
                this.worldPosition.getX(),
                this.worldPosition.getY() + 1,
                this.worldPosition.getZ(),
                this.worldPosition.getX() + 1,
                this.worldPosition.getY() + 3,
                this.worldPosition.getZ() + 1
        );
        List<Entity> merchants = this.level.getEntities((Entity) null, scanBox, e -> e.isAlive() && e instanceof Merchant);
        if (!merchants.isEmpty()) {
            this.merchant = (Merchant) merchants.getFirst();
            this.merchantOffers = this.merchant.getOffers();
            if (this.merchantOffers != null && !this.merchantOffers.isEmpty()) {
                this.currentRecipeIndex = Math.max(0, Math.min(this.currentRecipeIndex, this.merchantOffers.size() - 1));
                this.currentOffer = resolveMatchingOffer(this.merchantOffers.get(this.currentRecipeIndex));
            } else {
                this.currentOffer = null;
            }
        } else {
            this.merchant = null;
            this.merchantOffers = null;
            this.currentOffer = null;
        }

        if (this.merchant != null && this.currentOffer != null && !this.currentOffer.isOutOfStock()) {
            ItemStack result = this.currentOffer.getResult().copy();
            if (!result.isEmpty()
                    && ClayiumTransfer.canProduceItemStack(result, this.items, 2, 3, getInventoryStackLimit(2)) >= result.getCount()
                    && consumeClayEnergy(ENERGY_PER_TRADE)) {
                if (consumeOfferInputs(this.currentOffer)) {
                    ClayiumTransfer.produceItemStack(result, this.items, 2, 3, getInventoryStackLimit(2));
                    this.merchant.notifyTrade(this.currentOffer);
                    this.merchant.notifyTradeUpdated(result);
                    markForSync();
                }
            }
        }
    }

    @Nullable
    private MerchantOffer resolveMatchingOffer(MerchantOffer offer) {
        if (offer == null || offer.isOutOfStock()) {
            return null;
        }
        ItemStack in0 = getStack(0);
        ItemStack in1 = getStack(1);
        if (matchesOfferOrder(offer, in0, in1) || matchesOfferOrder(offer, in1, in0)) {
            return offer;
        }
        return null;
    }

    private static boolean matchesOfferOrder(MerchantOffer offer, ItemStack a, ItemStack b) {
        ItemStack costA = offer.getBaseCostA();
        ItemStack costB = offer.getCostB();
        if (costA.isEmpty() || a.isEmpty()) {
            return false;
        }
        if (!ItemStack.isSameItemSameComponents(a, costA) || a.getCount() < costA.getCount()) {
            return false;
        }
        if (costB.isEmpty()) {
            return true;
        }
        return !b.isEmpty() && ItemStack.isSameItemSameComponents(b, costB) && b.getCount() >= costB.getCount();
    }

    private boolean consumeOfferInputs(MerchantOffer offer) {
        ItemStack in0 = getStack(0);
        ItemStack in1 = getStack(1);
        if (matchesOfferOrder(offer, in0, in1)) {
            consumeCounts(0, offer.getBaseCostA().getCount(), 1, offer.getCostB().getCount());
            return true;
        }
        if (matchesOfferOrder(offer, in1, in0)) {
            consumeCounts(1, offer.getBaseCostA().getCount(), 0, offer.getCostB().getCount());
            return true;
        }
        return false;
    }

    private void consumeCounts(int slotA, int countA, int slotB, int countB) {
        ItemStack a = getStack(slotA);
        a.shrink(countA);
        if (a.isEmpty()) {
            setStack(slotA, ItemStack.EMPTY);
        }
        if (countB > 0) {
            ItemStack b = getStack(slotB);
            b.shrink(countB);
            if (b.isEmpty()) {
                setStack(slotB, ItemStack.EMPTY);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("RecipeIndex", this.currentRecipeIndex);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.currentRecipeIndex = tag.getInt("RecipeIndex");
    }
}
