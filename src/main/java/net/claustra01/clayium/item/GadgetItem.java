package net.claustra01.clayium.item;

import net.claustra01.clayium.menu.ItemFilterWhitelistMenu;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Individual gadget modules inserted into Clay Gadget Holder.
 */
public class GadgetItem extends SimpleTieredItem {
    public enum GadgetFamily {
        OVERCLOCK,
        FLIGHT,
        HEALTH,
        AUTO_EAT,
        REPEATEDLY_ATTACK,
        LONG_ARM,
        BLANK
    }

    private final GadgetFamily family;
    private final int level;

    public GadgetItem(int tier, String iconName, GadgetFamily family, int level) {
        super(new Properties().stacksTo(1), tier, iconName);
        this.family = family;
        this.level = level;
    }

    public GadgetFamily getFamily() {
        return this.family;
    }

    public int getLevel() {
        return this.level;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.family == GadgetFamily.AUTO_EAT) {
            if (!level.isClientSide) {
                int heldSlot = ItemFilterWhitelistMenu.resolveHeldSlotIndex(player.getInventory(), hand);
                player.openMenu(
                        new SimpleMenuProvider(
                                (id, inv, p) -> new ItemFilterWhitelistMenu(id, inv, heldSlot),
                                stack.getHoverName()),
                        buf -> buf.writeVarInt(heldSlot));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return super.use(level, player, hand);
    }
}
