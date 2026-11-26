package net.claustra01.clayium.gadget;

import net.claustra01.clayium.registry.GadgetRegistration;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public final class GadgetHolderMenu extends AbstractContainerMenu {
    private static final int GADGET_COLUMNS = 5;
    public static final int MACHINE_HEIGHT = 54;
    private final Inventory playerInventory;
    private final int holderSlot;
    private final SimpleContainer gadgets = new SimpleContainer(GadgetHolderItem.SLOT_COUNT);

    public static GadgetHolderMenu client(int containerId, Inventory inventory, RegistryFriendlyByteBuf data) {
        return new GadgetHolderMenu(containerId, inventory, data.readVarInt());
    }

    public GadgetHolderMenu(int containerId, Inventory playerInventory, int holderSlot) {
        super(GadgetRegistration.GADGET_HOLDER_MENU.get(), containerId);
        this.playerInventory = playerInventory;
        this.holderSlot = holderSlot;
        ItemStack holder = holder();
        holder.getOrDefault(GadgetRegistration.GADGETS.get(), ItemContainerContents.EMPTY).copyInto(gadgets.getItems());
        gadgets.addListener(this::slotsChanged);
        for (int index = 0; index < GadgetHolderItem.SLOT_COUNT; index++) {
            int x = index % GADGET_COLUMNS * 18 + (176 - 18 * GADGET_COLUMNS) / 2;
            int y = index / GADGET_COLUMNS * 18 + 18;
            addSlot(new Slot(gadgets, index, x, y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    if (!(stack.getItem() instanceof GadgetItem gadget) || gadget.type() == GadgetType.TEMPLATE) {
                        return false;
                    }
                    return gadgets.getItems().stream().noneMatch(existing -> ItemStack.isSameItem(existing, stack));
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addPlayerSlot(playerInventory, column + row * 9 + 9, 8 + column * 18, MACHINE_HEIGHT + 12 + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            addPlayerSlot(playerInventory, column, 8 + column * 18, MACHINE_HEIGHT + 70);
        }
    }

    private void addPlayerSlot(Inventory inventory, int index, int x, int y) {
        addSlot(new Slot(inventory, index, x, y) {
            @Override
            public boolean mayPickup(Player player) {
                return index != holderSlot;
            }
        });
    }

    @Override
    public void slotsChanged(net.minecraft.world.Container container) {
        super.slotsChanged(container);
        if (container == gadgets && !playerInventory.player.level().isClientSide()) {
            holder().set(GadgetRegistration.GADGETS.get(), ItemContainerContents.fromItems(gadgets.getItems()));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return holder().is(GadgetRegistration.GADGET_HOLDER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < GadgetHolderItem.SLOT_COUNT) {
            if (!moveItemStackTo(stack, GadgetHolderItem.SLOT_COUNT, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, GadgetHolderItem.SLOT_COUNT, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, stack);
        return original;
    }

    private ItemStack holder() {
        return playerInventory.getItem(holderSlot);
    }
}
