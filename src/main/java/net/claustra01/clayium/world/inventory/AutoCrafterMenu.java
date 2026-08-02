/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.inventory;

import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.level.block.AbstractTieredIoMachineBlock;
import net.claustra01.clayium.world.level.block.entity.AutoCrafterBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public final class AutoCrafterMenu extends AbstractDedicatedMachineMenu {
    private final int tier;
    public AutoCrafterMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, clientData(inventory, buffer));
    }
    private AutoCrafterMenu(int id, Inventory inventory, ClientData client) {
        this(id, inventory, client.container(), new SimpleContainerData(4), client.tier());
    }
    public AutoCrafterMenu(int id, Inventory inventory, Container container, ContainerData data, int tier) {
        super(ClayiumRegistries.AUTO_CRAFTER_MENU.get(), id, inventory, container, data, 84);
        this.tier = tier;
        for (int row = 0; row < 3; row++) for (int column = 0; column < 3; column++) {
            addSlot(new RestrictedSlot(container, column + row * 3, 62 + column * 18, 18 + row * 18));
            addSlot(new GhostSlot(container, AutoCrafterBlockEntity.PATTERN_START + column + row * 3,
                    5 + column * 18, 18 + row * 18));
        }
        for (int row = 0; row < 3; row++) for (int column = 0; column < 2; column++) {
            addSlot(new OutputSlot(container, AutoCrafterBlockEntity.OUTPUT_START + column + row * 2,
                    135 + column * 18, 18 + row * 18));
        }
        if (tier >= 6) addSlot(new EnergySlot(container, AutoCrafterBlockEntity.ENERGY_SLOT, -12, 68));
        finishLayout(inventory);
    }
    @Override public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 1 && slotId < 18 && (slotId & 1) == 1) {
            int pattern = AutoCrafterBlockEntity.PATTERN_START + slotId / 2;
            ItemStack carried = getCarried();
            container.setItem(pattern, carried.isEmpty() ? ItemStack.EMPTY : carried.copyWithCount(1));
            container.setChanged();
            broadcastChanges();
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }
    public int tier() { return tier; }
    public long energyPerTick() { return tier >= 6 ? 10 : 0; }
    private static ClientData clientData(Inventory inventory, RegistryFriendlyByteBuf buffer) {
        var pos = buffer.readBlockPos();
        if (inventory.player.level().getBlockEntity(pos) instanceof AutoCrafterBlockEntity machine) {
            return new ClientData(machine, machine.tierIndex());
        }
        int tier = inventory.player.level().getBlockState(pos).getBlock() instanceof AbstractTieredIoMachineBlock block
                ? block.tier().progressionIndex() : 5;
        return new ClientData(new SimpleContainer(AutoCrafterBlockEntity.SLOT_COUNT), tier);
    }
    private record ClientData(Container container, int tier) {}
}
