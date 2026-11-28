package net.claustra01.clayium.menu;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.WaterWheelBlockEntity;
import net.claustra01.clayium.registry.ClayiumMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Menu for Clay / Dense Clay Water Wheel: displays progress, durability/efficiency, and surrounding water count.
 */
public class WaterWheelMenu extends ClayiumBaseMenu {
    @Nullable
    private final WaterWheelBlockEntity wheel;
    private final ContainerData data;
    private int syncedProgress = 0;
    private int syncedEfficiency = 1000;
    private int syncedWaterCount = 0;

    public WaterWheelMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readBlockPos(), buf.readBlockPos());
    }

    public WaterWheelMenu(int containerId, Inventory playerInv, BlockPos targetPos, BlockPos interactionPos) {
        super(ClayiumMenus.WATER_WHEEL.get(), containerId, playerInv, interactionPos, null, null);
        BlockEntity be = playerInv.player.level().getBlockEntity(targetPos);
        this.wheel = be instanceof WaterWheelBlockEntity ww ? ww : null;
        initializeLayout();

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                if (wheel == null) {
                    return switch (index) {
                        case 0 -> syncedProgress;
                        case 1 -> syncedEfficiency;
                        case 2 -> syncedWaterCount;
                        default -> 0;
                    };
                }
                return switch (index) {
                    case 0 -> wheel.progress;
                    case 1 -> wheel.progressEfficiency;
                    case 2 -> wheel.countSurroundingFlowingWater();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> syncedProgress = value;
                    case 1 -> syncedEfficiency = value;
                    case 2 -> syncedWaterCount = value;
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
        addDataSlots(this.data);
    }

    @Override
    protected void addMachineSlots() {}

    public int getProgress() {
        return this.data.get(0);
    }

    public int getEfficiency() {
        return this.data.get(1);
    }

    public int getSurroundingWaterCount() {
        return this.data.get(2);
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.wheel != null && this.wheel.isRemoved()) {
            return false;
        }
        return super.stillValid(player);
    }
}
