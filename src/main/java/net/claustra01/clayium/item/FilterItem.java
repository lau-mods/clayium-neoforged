package net.claustra01.clayium.item;

import java.util.List;
import net.claustra01.clayium.filter.ClayiumFilterMatcher;
import net.claustra01.clayium.menu.ItemFilterStringMenu;
import net.claustra01.clayium.menu.ItemFilterWhitelistMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Item filter items (Duplicator, Whitelist, Blacklist, Fuzzy, Tag, Name, Translation,
 * Registry, ModId, Damage, BlockState, Harvestable).
 */
public class FilterItem extends SimpleTieredItem {
    public enum FilterKind {
        DUPLICATOR,
        WHITELIST,
        BLACKLIST,
        FUZZY,
        TAG,
        DISPLAY_NAME,
        TRANSLATION_KEY,
        REGISTRY_ID,
        MOD_ID,
        DAMAGE_VALUE,
        BLOCK_STATE,
        HARVESTABLE_BLOCK
    }

    private final FilterKind filterKind;

    public FilterItem(int tier, String iconName, FilterKind filterKind) {
        super(new Properties().stacksTo(1), tier, iconName);
        this.filterKind = filterKind;
    }

    public FilterKind getFilterKind() {
        return this.filterKind;
    }

    @Override
    public Component getName(ItemStack stack) {
        Component base = super.getName(stack);
        if (this.filterKind != FilterKind.DUPLICATOR && ClayiumFilterMatcher.isCopyMode(stack)) {
            return Component.empty().append(base).append(Component.literal(" (Copy)"));
        }
        return base;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.filterKind != FilterKind.DUPLICATOR && player.isShiftKeyDown() && ClayiumFilterMatcher.isCopyMode(stack)) {
            if (!level.isClientSide) {
                ClayiumFilterMatcher.withCopyFlag(stack, false);
                player.sendSystemMessage(Component.literal("Cleared the copy flag."));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        if (isWhitelistGuiKind()) {
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
        if (isStringGuiKind()) {
            if (!level.isClientSide) {
                int heldSlot = ItemFilterStringMenu.resolveHeldSlotIndex(player.getInventory(), hand);
                player.openMenu(
                        new SimpleMenuProvider(
                                (id, inv, p) -> new ItemFilterStringMenu(id, inv, heldSlot),
                                stack.getHoverName()),
                        buf -> buf.writeVarInt(heldSlot));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }

    public boolean isWhitelistGuiKind() {
        return this.filterKind == FilterKind.WHITELIST
                || this.filterKind == FilterKind.BLACKLIST
                || this.filterKind == FilterKind.FUZZY;
    }

    public boolean isStringGuiKind() {
        return this.filterKind == FilterKind.TAG
                || this.filterKind == FilterKind.DISPLAY_NAME
                || this.filterKind == FilterKind.TRANSLATION_KEY
                || this.filterKind == FilterKind.REGISTRY_ID
                || this.filterKind == FilterKind.MOD_ID
                || this.filterKind == FilterKind.DAMAGE_VALUE
                || this.filterKind == FilterKind.BLOCK_STATE;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        ClayiumFilterMatcher.appendFilterTooltip(stack, tooltip, 0);
    }
}
