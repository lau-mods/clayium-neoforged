package net.claustra01.clayium.tool;

import net.claustra01.clayium.item.FoundationItem;
import net.claustra01.clayium.machine.ClayMachineBlock;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

/** Restores the original machine-side tool clicks using the current three-state face model. */
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
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof ClayMachineBlockEntity machine)) {
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
            case WRENCH -> rotate(context);
            case IO -> configureFace(machine, context.getClickedFace(), player.isShiftKeyDown());
            case PIPING -> player.isShiftKeyDown() && rotate(context);
            case MEMORY -> player.isShiftKeyDown()
                    ? save(context.getItemInHand(), machine)
                    : load(context.getItemInHand(), machine, context);
        };
        if (!changed) {
            return InteractionResult.PASS;
        }
        if (context.getItemInHand().isDamageableItem()) {
            context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean configureFace(
            ClayMachineBlockEntity machine, Direction direction, boolean extraction) {
        ClayMachineBlockEntity.FaceMode selected = extraction
                ? ClayMachineBlockEntity.FaceMode.OUTPUT
                : ClayMachineBlockEntity.FaceMode.INPUT;
        ClayMachineBlockEntity.FaceMode next = machine.faceMode(direction) == selected
                ? ClayMachineBlockEntity.FaceMode.DISABLED
                : selected;
        machine.setFaceMode(direction, next);
        return true;
    }

    private static boolean rotate(UseOnContext context) {
        Direction clicked = context.getClickedFace();
        if (!clicked.getAxis().isHorizontal()) {
            return false;
        }
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (!state.hasProperty(ClayMachineBlock.FACING)) {
            return false;
        }
        Direction current = state.getValue(ClayMachineBlock.FACING);
        Direction next = current == clicked ? clicked.getOpposite() : clicked;
        context.getLevel().setBlock(context.getClickedPos(), state.setValue(ClayMachineBlock.FACING, next), 3);
        return true;
    }

    private static boolean save(ItemStack card, ClayMachineBlockEntity machine) {
        CompoundTag root = card.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag memory = new CompoundTag();
        for (Direction direction : Direction.values()) {
            memory.putString(direction.getName(), machine.faceMode(direction).name());
        }
        BlockState state = machine.getBlockState();
        if (state.hasProperty(ClayMachineBlock.FACING)) {
            memory.putString("facing", state.getValue(ClayMachineBlock.FACING).getName());
        }
        root.put(MEMORY_TAG, memory);
        card.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        return true;
    }

    private static boolean load(
            ItemStack card, ClayMachineBlockEntity machine, UseOnContext context) {
        CompoundTag root = card.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!root.contains(MEMORY_TAG, CompoundTag.TAG_COMPOUND)) {
            return false;
        }
        CompoundTag memory = root.getCompound(MEMORY_TAG);
        for (Direction direction : Direction.values()) {
            ClayMachineBlockEntity.FaceMode faceMode = parseFaceMode(memory.getString(direction.getName()));
            if (faceMode != null) {
                machine.setFaceMode(direction, faceMode);
            }
        }
        Direction facing = Direction.byName(memory.getString("facing"));
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (facing != null && facing.getAxis().isHorizontal() && state.hasProperty(ClayMachineBlock.FACING)) {
            context.getLevel().setBlock(
                    context.getClickedPos(), state.setValue(ClayMachineBlock.FACING, facing), 3);
        }
        return true;
    }

    private static ClayMachineBlockEntity.FaceMode parseFaceMode(String name) {
        try {
            return ClayMachineBlockEntity.FaceMode.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public enum Mode {
        WRENCH,
        IO,
        PIPING,
        MEMORY
    }
}
