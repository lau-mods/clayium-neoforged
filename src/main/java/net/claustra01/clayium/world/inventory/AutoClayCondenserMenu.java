/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.inventory;

import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.level.block.entity.AutoClayCondenserBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public final class AutoClayCondenserMenu extends AbstractDedicatedMachineMenu {
    public AutoClayCondenserMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, clientContainer(inventory, buffer), new SimpleContainerData(4));
    }
    public AutoClayCondenserMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(ClayiumRegistries.AUTO_CLAY_CONDENSER_MENU.get(), id, inventory, container, data, 104);
        for (int row = 0; row < 4; row++) for (int column = 0; column < 5; column++) {
            addSlot(new RestrictedSlot(container, column + row * 5, 43 + column * 18, 18 + row * 18));
        }
        addSlot(new GhostSlot(container, AutoClayCondenserBlockEntity.LIMIT_SLOT, 151, 18));
        finishLayout(inventory);
    }
    @Override public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId == AutoClayCondenserBlockEntity.STORAGE_END) {
            ItemStack carried = getCarried();
            ItemStack value = carried.isEmpty() ? ItemStack.EMPTY : carried.copyWithCount(1);
            if (!value.isEmpty() && AutoClayCondenserBlockEntity.clayLevel(value) < 0) return;
            container.setItem(AutoClayCondenserBlockEntity.LIMIT_SLOT, value);
            container.setChanged();
            broadcastChanges();
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }
    public long energyPerTick() { return 0; }
    private static Container clientContainer(Inventory inventory, RegistryFriendlyByteBuf buffer) {
        var pos = buffer.readBlockPos();
        return inventory.player.level().getBlockEntity(pos) instanceof AutoClayCondenserBlockEntity machine
                ? machine : new SimpleContainer(AutoClayCondenserBlockEntity.SLOT_COUNT);
    }
}
