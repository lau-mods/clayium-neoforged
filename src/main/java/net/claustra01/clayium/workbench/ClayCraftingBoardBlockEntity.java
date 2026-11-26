package net.claustra01.clayium.workbench;

import javax.annotation.Nullable;
import net.claustra01.clayium.registry.WorkbenchRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

public final class ClayCraftingBoardBlockEntity extends BlockEntity implements MenuProvider {
    public ClayCraftingBoardBlockEntity(BlockPos pos, BlockState state) {
        super(WorkbenchRegistration.CLAY_CRAFTING_BOARD_BLOCK_ENTITY.get(), pos, state);
    }

    @Nullable
    public IItemHandler adjacentInventory() {
        if (level == null) {
            return null;
        }
        for (Direction direction : Direction.values()) {
            BlockPos adjacent = worldPosition.relative(direction);
            IItemHandler handler = level.getCapability(
                    Capabilities.ItemHandler.BLOCK, adjacent, direction.getOpposite());
            if (handler != null && handler.getSlots() > 0) {
                return handler;
            }
        }
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.clayium_neoforged.clay_crafting_board");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ClayCraftingBoardMenu(containerId, inventory, this);
    }

}
