package net.claustra01.clayium.menu;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.AutoCrafterBlockEntity;
import net.claustra01.clayium.registry.ClayiumMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Menu for Auto Crafter (T5..T9): 3x3 pattern memory slots, 3x3 restricted inputs, 2x3 outputs, and synced CE.
 */
public class AutoCrafterMenu extends ClayiumBaseMenu {
    @Nullable
    private final AutoCrafterBlockEntity crafter;
    private final Container container;
    private final ContainerData energyData;
    private final int[] energyShorts = new int[4];

    public AutoCrafterMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readBlockPos(), buf.readBlockPos());
    }

    public AutoCrafterMenu(int containerId, Inventory playerInv, BlockPos targetPos, BlockPos interactionPos) {
        super(ClayiumMenus.AUTO_CRAFTER.get(), containerId, playerInv, interactionPos, null, null);
        BlockEntity be = playerInv.player.level().getBlockEntity(targetPos);
        this.crafter = be instanceof AutoCrafterBlockEntity ac ? ac : null;
        this.container = this.crafter != null ? wrapContainer(this.crafter) : new SimpleContainer(43);
        initializeLayout();

        this.energyData = new ContainerData() {
            @Override
            public int get(int index) {
                if (crafter == null) {
                    return energyShorts[index];
                }
                int[] buf = new int[4];
                putLongIntoDataSlots(crafter.getClayEnergy(), buf);
                return buf[index];
            }

            @Override
            public void set(int index, int value) {
                energyShorts[index] = value;
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
        addDataSlots(this.energyData);
    }

    @Override
    protected void configureLayout() {
        this.machineGuiSizeY = 84;
    }

    @Override
    protected void addMachineSlots() {
        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 3; i++) {
                addSlot(new ClaySlot(this.container, i + j * 3, i * 18 + (this.machineGuiSizeX - 54) / 2 + 1, j * 18 + 18, SlotTextureKind.SMALL)
                        .setRestricted());
            }
        }
        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 3; i++) {
                addSlot(new ClayMemorySlot(this.container, i + j * 3 + 15, i * 18 + 5, j * 18 + 18, SlotTextureKind.FILTER));
            }
        }
        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 2; i++) {
                addSlot(new ClayResultSlot(this.container, i + j * 2 + 9, i * 18 + (this.machineGuiSizeX - 36) - 5, j * 18 + 18, SlotTextureKind.SMALL));
            }
        }
    }

    public long getClayEnergy() {
        if (this.crafter != null && !this.level.isClientSide) {
            return this.crafter.getClayEnergy();
        }
        return readLongFrom4Shorts(
                this.energyData.get(0),
                this.energyData.get(1),
                this.energyData.get(2),
                this.energyData.get(3)
        );
    }

    @Nullable
    public AutoCrafterBlockEntity getCrafter() {
        return this.crafter;
    }

    @Override
    protected boolean canTransferToMachineInventory(ItemStack stack) {
        if (this.crafter == null) {
            return false;
        }
        for (int i = 0; i < 9; i++) {
            if (this.crafter.isItemValidForSlot(i, stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean transferStackToMachineInventory(ItemStack stack) {
        if (this.crafter == null) {
            return false;
        }
        boolean moved = false;
        for (int i = 0; i < 9; i++) {
            if (this.crafter.isItemValidForSlot(i, stack)) {
                moved |= moveItemStackTo(stack, i, i + 1, false);
            }
        }
        return moved;
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.crafter != null && this.crafter.isRemoved()) {
            return false;
        }
        return super.stillValid(player);
    }
}
