package net.claustra01.clayium.item;

import java.util.List;
import javax.annotation.Nullable;
import net.claustra01.clayium.material.CMaterial;
import net.claustra01.clayium.material.CShape;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Material x Shape item carrying optional tier and layered tint vs dedicated texture metadata.
 */
public class ClayMaterialItem extends Item implements ITieredItem {
    private final CMaterial material;
    private final CShape shape;
    private final int tier;
    private final boolean layered;
    private final String iconName;

    public ClayMaterialItem(
            Properties properties,
            CMaterial material,
            CShape shape,
            @Nullable Integer tier,
            boolean layered,
            String iconName) {
        super(properties);
        this.material = material;
        this.shape = shape;
        this.tier = tier != null ? tier : -1;
        this.layered = layered;
        this.iconName = iconName;
    }

    public CMaterial getMaterial() {
        return this.material;
    }

    public CShape getShape() {
        return this.shape;
    }

    public boolean isLayered() {
        return this.layered;
    }

    public String getIconName() {
        return this.iconName;
    }

    @Override
    public int getTier(ItemStack stack) {
        return this.tier;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (this.tier >= 0) {
            tooltip.add(Component.translatable("gui.Common.tier", this.tier).withStyle(ChatFormatting.GRAY));
        }
    }
}
