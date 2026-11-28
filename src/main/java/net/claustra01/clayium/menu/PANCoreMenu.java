package net.claustra01.clayium.menu;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.PANCoreBlockEntity;
import net.claustra01.clayium.blockentity.PANCoreBlockEntity.PANItemCost;
import net.claustra01.clayium.network.PANCoreListPayload;
import net.claustra01.clayium.registry.ClayiumMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Menu for PAN Core: hosts synced ingredient CE table and prohibited item exclusions.
 */
public class PANCoreMenu extends ClayiumBaseMenu {
    @Nullable
    private final PANCoreBlockEntity panCore;
    private List<PANCoreListPayload.Entry> syncedIngredients = List.of();
    private List<ItemStack> syncedProhibited = List.of();
    private int syncRevision = 0;

    public PANCoreMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readBlockPos(), buf.readBlockPos());
    }

    public PANCoreMenu(int containerId, Inventory playerInv, BlockPos targetPos, BlockPos interactionPos) {
        super(ClayiumMenus.PAN_CORE.get(), containerId, playerInv, interactionPos, null, null);
        BlockEntity be = playerInv.player.level().getBlockEntity(targetPos);
        this.panCore = be instanceof PANCoreBlockEntity core ? core : null;
        initializeLayout();
    }

    @Override
    protected void configureLayout() {
        this.machineGuiSizeY += 72;
    }

    @Override
    protected void addMachineSlots() {
        // PAN Core displays the synchronized item ledger directly without container slots.
    }

    @Override
    protected boolean canTransferToMachineInventory(ItemStack stack) {
        return false;
    }

    public void updateSyncedList(List<PANCoreListPayload.Entry> ingredients, List<ItemStack> prohibited) {
        this.syncedIngredients = List.copyOf(ingredients);
        this.syncedProhibited = prohibited.stream().map(ItemStack::copy).toList();
        this.syncRevision++;
    }

    public List<PANCoreListPayload.Entry> getSyncedIngredients() {
        return this.syncedIngredients;
    }

    public List<ItemStack> getSyncedProhibited() {
        return this.syncedProhibited;
    }

    public int getSyncRevision() {
        return this.syncRevision;
    }

    public PANCoreListPayload createSyncPayload() {
        if (this.panCore == null) {
            return new PANCoreListPayload(this.containerId, List.of(), List.of());
        }
        List<PANCoreListPayload.Entry> ingredients = new ArrayList<>();
        for (PANItemCost item : this.panCore.getIngredientItemList()) {
            ingredients.add(new PANCoreListPayload.Entry(item.stack(), item.cost(), item.consumption()));
        }
        List<ItemStack> prohibited = new ArrayList<>();
        for (PANItemCost item : this.panCore.getProhibitedItemList()) {
            prohibited.add(item.stack());
        }
        return new PANCoreListPayload(this.containerId, ingredients, prohibited);
    }
}
