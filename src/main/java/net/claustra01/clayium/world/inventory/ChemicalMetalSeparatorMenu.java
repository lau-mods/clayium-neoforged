/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.inventory;

import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.level.block.entity.ChemicalMetalSeparatorBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

public final class ChemicalMetalSeparatorMenu extends AbstractDedicatedMachineMenu {
    public ChemicalMetalSeparatorMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, clientContainer(inventory, buffer), new SimpleContainerData(4));
    }
    public ChemicalMetalSeparatorMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(ClayiumRegistries.CHEMICAL_METAL_SEPARATOR_MENU.get(), id, inventory, container, data, 96);
        addSlot(new RestrictedSlot(container, ChemicalMetalSeparatorBlockEntity.INPUT_SLOT, 25, 44));
        for (int row = 0; row < 4; row++) for (int column = 0; column < 4; column++) {
            addSlot(new OutputSlot(container, ChemicalMetalSeparatorBlockEntity.OUTPUT_START + row * 4 + column,
                    85 + column * 18, 17 + row * 18));
        }
        addSlot(new EnergySlot(container, ChemicalMetalSeparatorBlockEntity.ENERGY_SLOT, -12, 80));
        finishLayout(inventory);
    }
    public long energyPerTick() { return 5_000; }
    private static Container clientContainer(Inventory inventory, RegistryFriendlyByteBuf buffer) {
        var pos = buffer.readBlockPos();
        return inventory.player.level().getBlockEntity(pos) instanceof ChemicalMetalSeparatorBlockEntity machine
                ? machine : new SimpleContainer(ChemicalMetalSeparatorBlockEntity.SLOT_COUNT);
    }
}
