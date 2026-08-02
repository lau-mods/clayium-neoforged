/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.inventory;

import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.level.block.entity.ClayEnergyLaserBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

public final class ClayEnergyLaserMenu extends AbstractDedicatedMachineMenu {
    private final int tier;
    private final long energyPerTick;
    public ClayEnergyLaserMenu(int id, Inventory inventory, RegistryFriendlyByteBuf data) {
        this(id, inventory, inventory.player.level().getBlockEntity(data.readBlockPos()) instanceof ClayEnergyLaserBlockEntity laser
                ? laser : new SimpleContainer(1), new SimpleContainerData(4));
    }
    public ClayEnergyLaserMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(ClayiumRegistries.CLAY_ENERGY_LASER_MENU.get(), id, inventory, container, data, 76);
        ClayEnergyLaserBlockEntity laser = container instanceof ClayEnergyLaserBlockEntity value ? value : null;
        tier = laser == null ? 7 : laser.tierIndex();
        energyPerTick = laser == null ? 40_000L : laser.energyPerTick();
        addSlot(new EnergySlot(container, 0, 80, 31));
        finishLayout(inventory);
    }
    public int tier() { return tier; }
    public long energyPerTick() { return energyPerTick; }
}
