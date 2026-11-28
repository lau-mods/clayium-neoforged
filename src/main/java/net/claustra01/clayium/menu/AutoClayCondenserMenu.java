package net.claustra01.clayium.menu;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.AutoClayCondenserBlockEntity;
import net.claustra01.clayium.registry.ClayiumMenus;
import net.claustra01.clayium.util.CompressedClayHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Menu for Auto Clay Condenser MK1 / MK2: 5x4 compressed clay inventory + tier cap memory slot (index 21).
 */
public class AutoClayCondenserMenu extends ClayiumBaseMenu {
    private static final int COLS = 5;
    private static final int ROWS = 4;

    @Nullable
    private final AutoClayCondenserBlockEntity condenser;
    private final Container container;

    public AutoClayCondenserMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readBlockPos(), buf.readBlockPos());
    }

    public AutoClayCondenserMenu(int containerId, Inventory playerInv, BlockPos targetPos, BlockPos interactionPos) {
        super(ClayiumMenus.AUTO_CLAY_CONDENSER.get(), containerId, playerInv, interactionPos, null, null);
        BlockEntity be = playerInv.player.level().getBlockEntity(targetPos);
        this.condenser = be instanceof AutoClayCondenserBlockEntity ac ? ac : null;
        this.container = this.condenser != null ? wrapContainer(this.condenser) : new SimpleContainer(22);
        initializeLayout();
    }

    @Override
    protected void configureLayout() {
        this.machineGuiSizeY = ROWS * 18 + 32;
    }

    @Override
    protected void addMachineSlots() {
        int baseX = (this.machineGuiSizeX - 18 * COLS) / 2;
        for (int j = 0; j < ROWS; j++) {
            for (int i = 0; i < COLS; i++) {
                addSlot(new ClaySlot(this.container, i + j * COLS, i * 18 + baseX, j * 18 + 18, SlotTextureKind.SMALL) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return CompressedClayHelper.getTierOfCompressedClay(stack, true) >= 0;
                    }
                });
            }
        }
        addSlot(new ClayMemorySlot(this.container, 21, 108 + baseX, 18, SlotTextureKind.ENERGY));
    }

    @Override
    protected boolean canTransferToMachineInventory(ItemStack stack) {
        return CompressedClayHelper.getTierOfCompressedClay(stack, true) >= 0;
    }

    @Override
    protected boolean transferStackToMachineInventory(ItemStack stack) {
        return moveItemStackTo(stack, 0, COLS * ROWS, false);
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.condenser != null && this.condenser.isRemoved()) {
            return false;
        }
        return super.stillValid(player);
    }
}
