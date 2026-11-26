package net.claustra01.clayium.gadget;

import java.util.List;
import net.claustra01.clayium.item.FoundationItem;
import net.claustra01.clayium.registry.GadgetRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

public final class GadgetHolderItem extends FoundationItem {
    /** Original GUI 23: ContainerInItemStack(player, 5, 2, "Items"). */
    public static final int SLOT_COUNT = 10;

    public GadgetHolderItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack holder = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            int holderSlot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : Inventory.SLOT_OFFHAND;
            serverPlayer.openMenu(
                    new SimpleMenuProvider(
                            (containerId, inventory, ignored) -> new GadgetHolderMenu(containerId, inventory, holderSlot),
                            holder.getHoverName()),
                    buffer -> buffer.writeVarInt(holderSlot));
        }
        return InteractionResultHolder.sidedSuccess(holder, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        List<ItemStack> gadgets = gadgets(stack);
        if (gadgets.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.clayium.gadget_holder.empty").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.clayium.gadget_holder.configured", gadgets.size())
                    .withStyle(ChatFormatting.AQUA));
            for (ItemStack gadget : gadgets) {
                tooltip.add(Component.literal(" • ").append(gadget.getHoverName()).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    public static List<ItemStack> gadgets(ItemStack holder) {
        ItemContainerContents contents = holder.getOrDefault(
                GadgetRegistration.GADGETS.get(), ItemContainerContents.EMPTY);
        return contents.nonEmptyStream().limit(SLOT_COUNT).toList();
    }

    public static void setGadgets(ItemStack holder, List<ItemStack> gadgets) {
        holder.set(GadgetRegistration.GADGETS.get(), ItemContainerContents.fromItems(
                gadgets.stream().limit(SLOT_COUNT).map(stack -> stack.copyWithCount(1)).toList()));
    }
}
