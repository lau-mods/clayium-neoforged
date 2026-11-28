package net.claustra01.clayium.menu;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.StorageContainerBlockEntity;
import net.claustra01.clayium.registry.ClayiumMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Menu for Storage Container and Vacuum Container backed by virtual bulk storage slots.
 */
public class StorageContainerMenu extends ClayiumBaseMenu {
    @Nullable
    private final StorageContainerBlockEntity storage;
    private final boolean vacuumMode;
    private final ContainerData data;
    private int syncedStoredCount = 0;
    private int syncedMaxStorageSize = 1024;

    public StorageContainerMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readBlockPos(), buf.readBlockPos(), buf.readBoolean());
    }

    public StorageContainerMenu(
            int containerId,
            Inventory playerInv,
            BlockPos targetPos,
            BlockPos interactionPos,
            boolean vacuumMode) {
        super(ClayiumMenus.STORAGE_CONTAINER.get(), containerId, playerInv, interactionPos, null, null);
        BlockEntity be = playerInv.player.level().getBlockEntity(targetPos);
        this.storage = be instanceof StorageContainerBlockEntity sc ? sc : null;
        this.vacuumMode = vacuumMode || (this.storage != null && this.storage.isVacuumContainer());

        initializeLayout();

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> storage != null ? storage.getStoredCount() : syncedStoredCount;
                    case 1 -> storage != null ? storage.getMaxStorageSize() : syncedMaxStorageSize;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> {
                        syncedStoredCount = value;
                        if (storage != null) {
                            storage.setClientSyncedCount(value);
                        }
                    }
                    case 1 -> {
                        syncedMaxStorageSize = value;
                        if (storage != null) {
                            storage.setClientSyncedMaxSize(value);
                        }
                    }
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
        addDataSlots(this.data);
    }

    @Override
    protected void addMachineSlots() {
        Container virtual = new VirtualStorageContainer();
        if (this.vacuumMode) {
            if (this.storage != null) {
                addSlot(new ClayStorageSlot(virtual, this.storage, 0, (this.machineGuiSizeX - 16) / 2, 35));
            } else {
                addSlot(new ClaySlot(virtual, 0, (this.machineGuiSizeX - 16) / 2, 35, SlotTextureKind.LARGE));
            }
            addSlot(new ClayMemorySlot(virtual, 2, 108 + (this.machineGuiSizeX - 108) / 2, 18, SlotTextureKind.SMALL));
        } else {
            if (this.storage != null) {
                addSlot(new ClayStorageSlot(virtual, this.storage, 0, 44, 35));
            } else {
                addSlot(new ClaySlot(virtual, 0, 44, 35, SlotTextureKind.LARGE));
            }
            addSlot(new ClayResultSlot(virtual, 1, 116, 35, SlotTextureKind.LARGE) {
                @Override
                public ItemStack remove(int amount) {
                    return storage != null ? storage.extractFromStorage(amount, false) : ItemStack.EMPTY;
                }
            });
            addSlot(new ClayMemorySlot(virtual, 2, 108 + (this.machineGuiSizeX - 108) / 2, 18, SlotTextureKind.SMALL));
        }
    }

    public int getStoredCount() {
        return this.data.get(0);
    }

    public int getMaxStorageSize() {
        return this.data.get(1);
    }

    public boolean isVacuumMode() {
        return this.vacuumMode;
    }

    @Override
    protected boolean canTransferToMachineInventory(ItemStack stack) {
        return this.storage != null && this.storage.acceptsItem(stack);
    }

    @Override
    protected boolean transferStackToMachineInventory(ItemStack stack) {
        if (this.storage == null || stack.isEmpty()) {
            return false;
        }
        int moved = this.storage.insertIntoStorage(stack, false);
        if (moved > 0) {
            stack.shrink(moved);
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (!this.vacuumMode && index == 1 && this.storage != null) {
            ItemStack preview = this.storage.extractFromStorage(64, true);
            if (preview.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack toMove = preview.copy();
            int before = toMove.getCount();
            if (!moveItemStackToPlayerInventory(toMove, true)) {
                return ItemStack.EMPTY;
            }
            int moved = before - toMove.getCount();
            if (moved <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack extracted = this.storage.extractFromStorage(moved, false);
            return extracted;
        }
        return super.quickMoveStack(player, index);
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.storage != null && this.storage.isRemoved()) {
            return false;
        }
        return super.stillValid(player);
    }

    private class VirtualStorageContainer implements Container {
        @Override
        public int getContainerSize() {
            return 3;
        }

        @Override
        public boolean isEmpty() {
            return storage == null || (storage.getStoredCount() <= 0 && storage.getFilterTemplate().isEmpty());
        }

        @Override
        public ItemStack getItem(int slot) {
            if (storage == null) {
                return ItemStack.EMPTY;
            }
            return switch (slot) {
                case 0 -> ItemStack.EMPTY;
                case 1 -> {
                    if (vacuumMode || storage.getStoredCount() <= 0) {
                        yield ItemStack.EMPTY;
                    }
                    ItemStack tpl = storage.getStoredTemplate();
                    yield tpl.isEmpty() ? ItemStack.EMPTY : tpl.copyWithCount(Math.min(storage.getStoredCount(), tpl.getMaxStackSize()));
                }
                case 2 -> storage.getFilterTemplate();
                default -> ItemStack.EMPTY;
            };
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            if (storage == null || amount <= 0 || slot != 1 || vacuumMode) {
                return ItemStack.EMPTY;
            }
            return storage.extractFromStorage(amount, false);
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return removeItem(slot, 64);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (storage == null) {
                return;
            }
            if (slot == 0) {
                if (!stack.isEmpty()) {
                    storage.insertIntoStorage(stack, false);
                }
            } else if (slot == 1) {
                int current = Math.min(storage.getStoredCount(), storage.getStoredTemplate().getMaxStackSize());
                int next = stack.isEmpty() ? 0 : stack.getCount();
                if (next < current) {
                    storage.extractFromStorage(current - next, false);
                }
            } else if (slot == 2) {
                storage.setFilterTemplate(stack);
            }
        }

        @Override
        public void setChanged() {
            if (storage != null) {
                storage.markForSync();
            }
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {}
    }
}
