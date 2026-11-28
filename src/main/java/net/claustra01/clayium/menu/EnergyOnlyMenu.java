package net.claustra01.clayium.menu;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.ClayContainerBlockEntity;
import net.claustra01.clayium.registry.ClayiumMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Menu for CE-only blocks without interactive machine inventory slots
 * (Solar Clay Fabricator, Clay Fabricator, Salt Extractor, Creative CE Source).
 */
public class EnergyOnlyMenu extends ClayiumBaseMenu {
    @Nullable
    private final ClayContainerBlockEntity containerEntity;
    private final ContainerData energyData;
    private final int[] energyShorts = new int[4];

    public EnergyOnlyMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readBlockPos(), buf.readBlockPos());
    }

    public EnergyOnlyMenu(int containerId, Inventory playerInv, BlockPos targetPos, BlockPos interactionPos) {
        super(ClayiumMenus.ENERGY_ONLY.get(), containerId, playerInv, interactionPos, null, null);
        BlockEntity be = playerInv.player.level().getBlockEntity(targetPos);
        this.containerEntity = be instanceof ClayContainerBlockEntity cc ? cc : null;
        initializeLayout();

        this.energyData = new ContainerData() {
            @Override
            public int get(int index) {
                if (containerEntity == null) {
                    return energyShorts[index];
                }
                int[] buf = new int[4];
                putLongIntoDataSlots(containerEntity.getClayEnergy(), buf);
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
    protected void addMachineSlots() {}

    public long getClayEnergy() {
        if (this.containerEntity != null && !this.level.isClientSide) {
            return this.containerEntity.getClayEnergy();
        }
        return readLongFrom4Shorts(
                this.energyData.get(0),
                this.energyData.get(1),
                this.energyData.get(2),
                this.energyData.get(3)
        );
    }

    @Nullable
    public ClayContainerBlockEntity getContainerEntity() {
        return this.containerEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.containerEntity != null && this.containerEntity.isRemoved()) {
            return false;
        }
        return super.stillValid(player);
    }
}
