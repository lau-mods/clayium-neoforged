package net.claustra01.clayium.menu;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.AreaActivatorBlockEntity;
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
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Menu for Activator (T6) and Area Activator (T8).
 */
public class AreaActivatorMenu extends ClayiumBaseMenu implements IClayButtonMenu {
    public static final int BUTTON_WORK_ONCE = 0;
    public static final int BUTTON_STOP = 1;
    public static final int BUTTON_REPEAT = 2;
    public static final int BUTTON_CYCLE_BOX = 3;
    public static final int BUTTON_CYCLE_TARGET = 4;
    public static final int BUTTON_TOGGLE_RAYTRACE = 5;
    public static final int BUTTON_TOGGLE_SNEAK = 6;

    @Nullable
    private final AreaActivatorBlockEntity activator;
    private final Container container;
    private final ContainerData data;
    private final int[] synced = new int[13]; // 0..3 CE, 4..7 laser, 8 state, 9 boxState, 10 target, 11 rayTrace, 12 sneak

    public AreaActivatorMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readBlockPos(), buf.readBlockPos());
    }

    public AreaActivatorMenu(int containerId, Inventory playerInv, BlockPos targetPos, BlockPos interactionPos) {
        super(ClayiumMenus.AREA_ACTIVATOR.get(), containerId, playerInv, interactionPos, null, null);
        BlockEntity be = playerInv.player.level().getBlockEntity(targetPos);
        this.activator = be instanceof AreaActivatorBlockEntity act ? act : null;
        this.container = this.activator != null ? wrapContainer(this.activator) : new SimpleContainer(53);
        initializeLayout();

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                if (activator == null) {
                    return synced[index];
                }
                int[] buf = new int[4];
                if (index < 4) {
                    putLongIntoDataSlots(activator.getClayEnergy(), buf);
                    return buf[index];
                }
                if (index < 8) {
                    putLongIntoDataSlots(activator.lastLaserEnergy, buf);
                    return buf[index - 4];
                }
                return switch (index) {
                    case 8 -> activator.state;
                    case 9 -> activator.boxState;
                    case 10 -> activator.target;
                    case 11 -> activator.enableRayTrace ? 1 : 0;
                    case 12 -> activator.sneak ? 1 : 0;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                synced[index] = value;
            }

            @Override
            public int getCount() {
                return 13;
            }
        };
        addDataSlots(this.data);
    }

    @Override
    protected void configureLayout() {
        this.machineGuiSizeY = 90;
    }

    @Override
    protected void addMachineSlots() {
        int baseX = (this.machineGuiSizeX - 54) / 2; // 61
        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 3; i++) {
                addSlot(new ClaySlot(this.container, i + j * 3, baseX + i * 18, 18 + j * 18, SlotTextureKind.SMALL));
            }
        }
        addSlot(new ClayMemorySlot(this.container, AreaActivatorBlockEntity.FILTER_SLOT, 119, 18, SlotTextureKind.FILTER));
    }

    public long getClayEnergy() {
        if (this.activator != null && !this.level.isClientSide) {
            return this.activator.getClayEnergy();
        }
        return readLongFrom4Shorts(this.data.get(0), this.data.get(1), this.data.get(2), this.data.get(3));
    }

    public long getLaserEnergy() {
        if (this.activator != null && !this.level.isClientSide) {
            return this.activator.lastLaserEnergy;
        }
        return readLongFrom4Shorts(this.data.get(4), this.data.get(5), this.data.get(6), this.data.get(7));
    }

    public int getWorkState() {
        return this.data.get(8);
    }

    public int getBoxState() {
        return this.data.get(9);
    }

    public int getTargetMode() {
        return this.data.get(10);
    }

    public boolean isRayTraceEnabled() {
        return this.data.get(11) != 0;
    }

    public boolean isSneaking() {
        return this.data.get(12) != 0;
    }

    @Nullable
    public AreaActivatorBlockEntity getActivator() {
        return this.activator;
    }

    @Override
    public void handleGuiButton(ServerPlayer player, int buttonId) {
        if (this.activator == null || !stillValid(player)) {
            return;
        }
        switch (buttonId) {
            case BUTTON_WORK_ONCE -> this.activator.setState(AreaActivatorBlockEntity.STATE_ONCE_START);
            case BUTTON_STOP -> this.activator.setState(AreaActivatorBlockEntity.STATE_STOPPED);
            case BUTTON_REPEAT -> this.activator.setState(AreaActivatorBlockEntity.STATE_REPEAT_START);
            case BUTTON_CYCLE_BOX -> this.activator.cycleBoxState();
            case BUTTON_CYCLE_TARGET -> this.activator.cycleTarget();
            case BUTTON_TOGGLE_RAYTRACE -> this.activator.toggleRayTrace();
            case BUTTON_TOGGLE_SNEAK -> this.activator.toggleSneak();
        }
        broadcastChanges();
    }

    @Override
    protected boolean transferStackToMachineInventory(ItemStack stack) {
        return moveItemStackTo(stack, 0, 9, false);
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.activator != null && this.activator.isRemoved()) {
            return false;
        }
        return super.stillValid(player);
    }
}
