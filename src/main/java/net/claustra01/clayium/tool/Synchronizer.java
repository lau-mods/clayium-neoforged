package net.claustra01.clayium.tool;

import net.claustra01.clayium.machine.iface.InterfaceBlockEntity;
import net.claustra01.clayium.machine.iface.SynchronizedTarget;
import net.claustra01.clayium.registry.InterfaceRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public final class Synchronizer extends Item {
    public Synchronizer(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return useOn(context);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide) {
            return context.getLevel().getBlockEntity(context.getClickedPos()) == null
                    ? InteractionResult.PASS : InteractionResult.SUCCESS;
        }
        if (!(context.getPlayer() instanceof ServerPlayer player)
                || !player.mayInteract(player.serverLevel(), context.getClickedPos())) {
            return InteractionResult.FAIL;
        }
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof InterfaceBlockEntity blockEntity) {
            SynchronizedTarget target = context.getItemInHand().get(InterfaceRegistration.SYNCHRONIZED_TARGET.get());
            if (target == null) {
                player.displayClientMessage(Component.translatable("message.clayium.synchronizer.empty"), true);
                return InteractionResult.FAIL;
            }
            var targetLevel = player.getServer().getLevel(target.dimension());
            if (targetLevel == null || !targetLevel.isLoaded(target.pos())
                    || targetLevel.getBlockEntity(target.pos()) == null
                    || !player.mayInteract(targetLevel, target.pos())) {
                player.displayClientMessage(Component.translatable("message.clayium.synchronizer.invalid"), true);
                return InteractionResult.FAIL;
            }
            blockEntity.synchronize(target, player.getUUID());
            player.displayClientMessage(Component.translatable("message.clayium.synchronizer.connected"), true);
            return InteractionResult.SUCCESS;
        }
        if (context.getLevel().getBlockEntity(context.getClickedPos()) == null) {
            return InteractionResult.PASS;
        }
        context.getItemInHand().set(
                InterfaceRegistration.SYNCHRONIZED_TARGET.get(),
                SynchronizedTarget.at(context.getLevel(), context.getClickedPos()));
        player.displayClientMessage(Component.translatable("message.clayium.synchronizer.recorded"), true);
        return InteractionResult.SUCCESS;
    }
}
