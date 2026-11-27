package net.claustra01.clayium.item;

import java.util.List;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

/**
 * Fluid capsule item (1000 / 125 / 25 / 5 / 1 mB) storing fluid via FLUID_CONTENT DataComponent.
 */
public class FluidCapsuleItem extends SimpleTieredItem {
    private final int capacityMb;

    public FluidCapsuleItem(int capacityMb, String iconName) {
        super(new Properties().stacksTo(64), null, iconName);
        this.capacityMb = capacityMb;
    }

    public int getCapacityMb() {
        return this.capacityMb;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        SimpleFluidContent content = stack.get(ClayiumDataComponents.FLUID_CONTENT.get());
        if (content != null && !content.isEmpty()) {
            FluidStack fluid = content.copy();
            tooltip.add(Component.empty()
                    .append(fluid.getHoverName())
                    .append(Component.literal(" (" + fluid.getAmount() + " / " + this.capacityMb + " mB)"))
                    .withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.literal("Empty (0 / " + this.capacityMb + " mB)").withStyle(ChatFormatting.GRAY));
        }
    }
}
