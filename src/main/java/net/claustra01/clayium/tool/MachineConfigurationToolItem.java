package net.claustra01.clayium.tool;

import net.claustra01.clayium.item.FoundationItem;
import net.claustra01.clayium.logistics.ConfigurableIoDevice;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;

/** Applies the original configuration-tool actions to every shared relative-side I/O device. */
public final class MachineConfigurationToolItem extends FoundationItem {
    private static final String MEMORY_TAG = "clayium_machine_configuration";

    private final Mode mode;

    public MachineConfigurationToolItem(Properties properties, Mode mode) {
        super(properties);
        this.mode = mode;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return useOn(context);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof ConfigurableIoDevice device)) {
            return InteractionResult.PASS;
        }
        if (context.getLevel().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(context.getPlayer() instanceof ServerPlayer player)
                || !player.mayInteract(player.serverLevel(), context.getClickedPos())) {
            return InteractionResult.FAIL;
        }

        boolean changed = switch (mode) {
            case WRENCH -> device.rotate(context.getClickedFace());
            case IO -> configureFace(device, context.getClickedFace(), player.isShiftKeyDown());
            case PIPING -> player.isShiftKeyDown()
                    ? device.rotate(context.getClickedFace()) : device.togglePipe();
            case MEMORY -> player.isShiftKeyDown()
                    ? save(context.getItemInHand(), device)
                    : load(context.getItemInHand(), device);
        };
        if (!changed) {
            return InteractionResult.PASS;
        }
        if (context.getItemInHand().isDamageableItem()) {
            context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean configureFace(ConfigurableIoDevice device, Direction direction, boolean extraction) {
        if (extraction) device.cycleExtractRoute(direction);
        else device.cycleInsertRoute(direction);
        return true;
    }

    private static boolean save(ItemStack card, ConfigurableIoDevice device) {
        CompoundTag root = card.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        root.put(MEMORY_TAG, device.saveIoMemory());
        card.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        return true;
    }

    private static boolean load(ItemStack card, ConfigurableIoDevice device) {
        CompoundTag root = card.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!root.contains(MEMORY_TAG, CompoundTag.TAG_COMPOUND)) {
            return false;
        }
        device.loadIoMemory(root.getCompound(MEMORY_TAG));
        return true;
    }

    public enum Mode {
        WRENCH,
        IO,
        PIPING,
        MEMORY
    }
}
