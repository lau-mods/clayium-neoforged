package net.claustra01.clayium.menu;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.AreaMinerBlockEntity;
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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Menu for Miner (T6), Area Collector (T7), Area Miner (T8), Advanced Area Miner (T9), and Area Replacer (T10).
 */
public class AreaMinerMenu extends ClayiumBaseMenu implements IClayButtonMenu {
    public static final int BUTTON_WORK_ONCE = 0;
    public static final int BUTTON_STOP = 1;
    public static final int BUTTON_REPEAT = 2;
    public static final int BUTTON_CYCLE_BOX = 3;

    @Nullable
    private final AreaMinerBlockEntity miner;
    private final Container container;
    private final int tier;
    private final boolean replaceMode;
    private final ContainerData data;
    private final int[] synced = new int[10]; // 0..3 CE, 4..7 laser, 8 state, 9 boxState

    public AreaMinerMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readBlockPos(), buf.readBlockPos());
    }

    public AreaMinerMenu(int containerId, Inventory playerInv, BlockPos targetPos, BlockPos interactionPos) {
        super(ClayiumMenus.AREA_MINER.get(), containerId, playerInv, interactionPos, null, null);
        BlockEntity be = playerInv.player.level().getBlockEntity(targetPos);
        this.miner = be instanceof AreaMinerBlockEntity m ? m : null;
        this.container = this.miner != null ? wrapContainer(this.miner) : new SimpleContainer(40);
        this.tier = this.miner != null ? this.miner.getTier() : 8;
        this.replaceMode = this.miner != null && this.miner.replaceMode;
        initializeLayout();

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                if (miner == null) {
                    return synced[index];
                }
                int[] buf = new int[4];
                if (index < 4) {
                    putLongIntoDataSlots(miner.getClayEnergy(), buf);
                    return buf[index];
                }
                if (index < 8) {
                    putLongIntoDataSlots(miner.lastLaserEnergy, buf);
                    return buf[index - 4];
                }
                return switch (index) {
                    case 8 -> miner.state;
                    case 9 -> miner.boxState;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                synced[index] = value;
            }

            @Override
            public int getCount() {
                return 10;
            }
        };
        addDataSlots(this.data);
    }

    @Override
    protected void configureLayout() {
        if (this.replaceMode) {
            this.machineGuiSizeY = 110;
        } else {
            this.machineGuiSizeY = 90;
        }
    }

    @Override
    protected void addMachineSlots() {
        if (this.replaceMode) {
            for (int j = 0; j < 2; j++) {
                for (int i = 0; i < 4; i++) {
                    addSlot(new ClayResultSlot(this.container, i + j * 4, 36 + i * 18, 18 + j * 18, SlotTextureKind.SMALL));
                }
            }
            for (int j = 0; j < 2; j++) {
                for (int i = 0; i < 4; i++) {
                    addSlot(new ClaySlot(this.container, 8 + i + j * 4, 36 + i * 18, 56 + j * 18, SlotTextureKind.SMALL));
                }
            }
            addSlot(new ClayMemorySlot(this.container, AreaMinerBlockEntity.FILTER_HARVEST_SLOT, 110, 18, SlotTextureKind.FILTER));
            addSlot(new ClayMemorySlot(this.container, AreaMinerBlockEntity.FILTER_FORTUNE_SLOT, 110, 36, SlotTextureKind.FILTER));
            addSlot(new ClayMemorySlot(this.container, AreaMinerBlockEntity.FILTER_SILKTOUCH_SLOT, 110, 54, SlotTextureKind.FILTER));
        } else if (this.tier >= 9) {
            int baseX = (this.machineGuiSizeX - 32 - 54) / 2; // 45
            for (int j = 0; j < 3; j++) {
                for (int i = 0; i < 3; i++) {
                    addSlot(new ClayResultSlot(this.container, i + j * 3, baseX + i * 18, 18 + j * 18, SlotTextureKind.SMALL));
                }
            }
            addSlot(new ClayMemorySlot(this.container, AreaMinerBlockEntity.FILTER_HARVEST_SLOT, 103, 18, SlotTextureKind.FILTER));
            addSlot(new ClayMemorySlot(this.container, AreaMinerBlockEntity.FILTER_FORTUNE_SLOT, 103, 36, SlotTextureKind.FILTER));
            addSlot(new ClayMemorySlot(this.container, AreaMinerBlockEntity.FILTER_SILKTOUCH_SLOT, 103, 54, SlotTextureKind.FILTER));
        } else {
            int baseX = (this.machineGuiSizeX - 54) / 2; // 61
            for (int j = 0; j < 3; j++) {
                for (int i = 0; i < 3; i++) {
                    addSlot(new ClayResultSlot(this.container, i + j * 3, baseX + i * 18, 18 + j * 18, SlotTextureKind.SMALL));
                }
            }
            addSlot(new ClayMemorySlot(this.container, AreaMinerBlockEntity.FILTER_HARVEST_SLOT, 119, 18, SlotTextureKind.FILTER));
        }
    }

    public int getTier() {
        return this.tier;
    }

    public boolean isReplaceMode() {
        return this.replaceMode;
    }

    public long getClayEnergy() {
        if (this.miner != null && !this.level.isClientSide) {
            return this.miner.getClayEnergy();
        }
        return readLongFrom4Shorts(
                this.data.get(0),
                this.data.get(1),
                this.data.get(2),
                this.data.get(3)
        );
    }

    public long getLaserEnergy() {
        if (this.miner != null && !this.level.isClientSide) {
            return this.miner.lastLaserEnergy;
        }
        return readLongFrom4Shorts(
                this.data.get(4),
                this.data.get(5),
                this.data.get(6),
                this.data.get(7)
        );
    }

    public int getWorkState() {
        return this.data.get(8);
    }

    public int getBoxState() {
        return this.data.get(9);
    }

    @Nullable
    public AreaMinerBlockEntity getMiner() {
        return this.miner;
    }

    @Override
    public void handleGuiButton(ServerPlayer player, int buttonId) {
        if (this.miner == null || !stillValid(player)) {
            return;
        }
        switch (buttonId) {
            case BUTTON_WORK_ONCE -> this.miner.setState(AreaMinerBlockEntity.STATE_ONCE_START);
            case BUTTON_STOP -> this.miner.setState(AreaMinerBlockEntity.STATE_STOPPED);
            case BUTTON_REPEAT -> this.miner.setState(AreaMinerBlockEntity.STATE_REPEAT_START);
            case BUTTON_CYCLE_BOX -> this.miner.cycleBoxState();
        }
        broadcastChanges();
    }

    @Override
    protected boolean canTransferToMachineInventory(ItemStack stack) {
        return this.replaceMode && stack.getItem() instanceof BlockItem;
    }

    @Override
    protected boolean transferStackToMachineInventory(ItemStack stack) {
        if (!this.replaceMode) {
            return false;
        }
        return moveItemStackTo(stack, 8, 16, false);
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.miner != null && this.miner.isRemoved()) {
            return false;
        }
        return super.stillValid(player);
    }
}
