package net.claustra01.clayium.item;

import net.claustra01.clayium.menu.GadgetHolderMenu;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Wearable/inventory Clay Gadget Holder storing 10 gadget slots in {@code GADGET_INVENTORY}.
 */
public class GadgetHolderItem extends SimpleTieredItem {
    public GadgetHolderItem() {
        super(new Properties().stacksTo(1), null, "gadgetholder");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            int heldSlot = GadgetHolderMenu.resolveHeldSlotIndex(player.getInventory(), hand);
            player.openMenu(
                    new SimpleMenuProvider(
                            (id, inv, p) -> new GadgetHolderMenu(id, inv, heldSlot),
                            stack.getHoverName()),
                    buf -> buf.writeVarInt(heldSlot));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
