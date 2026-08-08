/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.inventory;

import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.level.block.AbstractTieredIoMachineBlock;
import net.claustra01.clayium.world.level.block.entity.PanDuplicatorBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

public final class PanDuplicatorMenu extends AbstractDedicatedMachineMenu {
    private final int tier;
    public PanDuplicatorMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, clientData(inventory, buffer));
    }
    private PanDuplicatorMenu(int id, Inventory inventory, ClientData value) {
        this(id, inventory, value.container(), new SimpleContainerData(6), value.tier());
    }
    public PanDuplicatorMenu(int id, Inventory inventory, Container container, ContainerData data, int tier) {
        super(ClayiumRegistries.PAN_DUPLICATOR_MENU.get(), id, inventory, container, data, 72);
        this.tier = tier;
        checkContainerDataCount(data,6);
        addSlot(new RestrictedSlot(container, PanDuplicatorBlockEntity.INPUT_A, 32, 35));
        addSlot(new RestrictedSlot(container, PanDuplicatorBlockEntity.INPUT_B, 50, 35));
        addSlot(new OutputSlot(container, PanDuplicatorBlockEntity.OUTPUT, 116, 35));
        // CE fuel is intentionally automation-only for high-tier devices; it is supplied
        // through the configured energy route or a linked Clay Interface.
        finishLayout(inventory);
    }
    public int tier() { return tier; }
    public PanDuplicatorBlockEntity.WorkStatus workStatus(){return PanDuplicatorBlockEntity.WorkStatus.byOrdinal(data.get(4));}
    public boolean linked(){return data.get(5)!=0;}
    public long energyPerTick() { return 100_000L * powerOfTen(Math.max(0, tier - 5)); }
    private static long powerOfTen(int exponent) {
        long value = 1;
        for (int i = 0; i < exponent; i++) value = value > Long.MAX_VALUE / 10 ? Long.MAX_VALUE : value * 10;
        return value;
    }
    private static ClientData clientData(Inventory inventory, RegistryFriendlyByteBuf buffer) {
        var pos = buffer.readBlockPos();
        if (inventory.player.level().getBlockEntity(pos) instanceof PanDuplicatorBlockEntity value)
            return new ClientData(value, value.tierIndex());
        int tier = inventory.player.level().getBlockState(pos).getBlock() instanceof AbstractTieredIoMachineBlock block
                ? block.tier().progressionIndex() : 4;
        return new ClientData(new SimpleContainer(PanDuplicatorBlockEntity.SLOTS), tier);
    }
    private record ClientData(Container container, int tier) {}
}
