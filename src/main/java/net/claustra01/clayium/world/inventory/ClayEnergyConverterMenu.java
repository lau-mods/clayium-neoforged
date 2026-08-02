/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.inventory;

import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.level.block.entity.ClayEnergyConverterBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

public final class ClayEnergyConverterMenu extends AbstractDedicatedMachineMenu {
    private final int tier;
    public ClayEnergyConverterMenu(int id, Inventory inventory, RegistryFriendlyByteBuf data) { this(id, inventory,
            inventory.player.level().getBlockEntity(data.readBlockPos()) instanceof ClayEnergyConverterBlockEntity converter ? converter : new SimpleContainer(1), new SimpleContainerData(10)); }
    public ClayEnergyConverterMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(ClayiumRegistries.CLAY_ENERGY_CONVERTER_MENU.get(), id, inventory, container, data, 112);
        tier = container instanceof ClayEnergyConverterBlockEntity value ? value.tierIndex() : 4;
        addSlot(new EnergySlot(container, 0, 80, 25)); finishLayout(inventory);
    }
    public int tier() { return tier; }
    public int fe() { return data.get(4); }
    public int capacity() { return data.get(5); }
    public int production() { return data.get(6); }
    public int output() { return data.get(7); }
    public long energyPerTick() {
        return Integer.toUnsignedLong(data.get(8)) | Integer.toUnsignedLong(data.get(9)) << 32;
    }
}
