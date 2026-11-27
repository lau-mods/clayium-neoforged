package net.claustra01.clayium.item;

import java.util.List;
import net.claustra01.clayium.block.ClayContainerBlock;
import net.claustra01.clayium.block.CompressedClayBlock;
import net.claustra01.clayium.block.ITieredBlock;
import net.claustra01.clayium.block.MetalChestBlock;
import net.claustra01.clayium.block.StorageContainerBlock;
import net.claustra01.clayium.block.UpgradeBlock;
import net.claustra01.clayium.component.ClayiumDataComponentTypes.StorageContentData;
import net.claustra01.clayium.energy.IClayEnergy;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.util.ClayiumFormatting;
import net.claustra01.clayium.util.ClayiumTooltips;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

/**
 * BlockItem for Clayium blocks displaying localized shift-expandable tooltips, Tier,
 * upgrade factors, laser consumption, Metal Chest / Storage Container capacity, and Compressed Clay CE.
 */
public class TieredBlockItem extends BlockItem implements ITieredItem, IClayEnergy {
    public TieredBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public int getTier(ItemStack stack) {
        return getBlock() instanceof ITieredBlock tiered ? tiered.getTier(stack) : -1;
    }

    @Override
    public long getClayEnergy(ItemStack stack) {
        return getBlock() instanceof CompressedClayBlock compressed ? compressed.getClayEnergyValue() : 0L;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        Block block = getBlock();
        String tooltipPrefix = ClayiumTooltips.resolveBlockTooltipPrefix(block, block.getDescriptionId());
        ClayiumTooltips.appendLocalizedTooltip(tooltip, tooltipPrefix, flag);

        int tier = getTier(stack);
        if (tier >= 0) {
            tooltip.add(Component.translatable("gui.Common.tier", tier).withStyle(ChatFormatting.GRAY));
        }

        Language lang = Language.getInstance();
        if (block instanceof UpgradeBlock upgrade) {
            if (upgrade.getKind() == UpgradeBlock.UpgradeKind.RESONATOR && lang.has("tooltip.Resonator.resonance")) {
                tooltip.add(ClayiumTooltips.formatLocalizedLine("tooltip.Resonator.resonance", upgrade.getFactor()));
            } else if (upgrade.getKind() == UpgradeBlock.UpgradeKind.OVERCLOCKER && lang.has("tooltip.Overclocker.overclockFactor")) {
                tooltip.add(ClayiumTooltips.formatLocalizedLine("tooltip.Overclocker.overclockFactor", upgrade.getFactor()));
            }
        } else if (block instanceof ClayContainerBlock cc && "energylaser".equals(cc.getOverlayTexture())) {
            long cePerTick = laserEnergyConsumptionForTier(tier);
            if (cePerTick > 0L && lang.has("tooltip.ClayEnergyLaser.energyConsumption")) {
                tooltip.add(ClayiumTooltips.formatLocalizedLine(
                        "tooltip.ClayEnergyLaser.energyConsumption",
                        ClayiumFormatting.formatClayEnergy(cePerTick)
                ));
            }
        } else if (block instanceof MetalChestBlock chest) {
            tooltip.add(Component.translatable("tooltip.MetalChest.capacity1", chest.getCols() * chest.getRows())
                    .withStyle(ChatFormatting.GRAY));
            if (chest.getPages() > 1) {
                tooltip.add(Component.translatable("tooltip.MetalChest.capacity2", chest.getPages())
                        .withStyle(ChatFormatting.GRAY));
            }
        } else if (block instanceof StorageContainerBlock) {
            StorageContentData data = stack.get(ClayiumDataComponents.STORAGE_CONTENT.get());
            if (data != null && data.item() != Items.AIR && data.count() > 0L) {
                tooltip.add(new ItemStack(data.item()).getHoverName().copy().withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal(data.count() + "/" + Math.max(data.maxStorage(), StorageContentData.DEFAULT_MAX_STORAGE))
                        .withStyle(ChatFormatting.GRAY));
            } else if (StorageContainerBlock.isUpgradedStack(stack)) {
                tooltip.add(Component.literal("0/" + StorageContainerBlock.getMaxStorage(stack)).withStyle(ChatFormatting.GRAY));
            }
        }

        long ce = getClayEnergy(stack);
        if (ce > 0L) {
            tooltip.add(Component.literal(ClayiumFormatting.formatClayEnergy(ce) + "CE").withStyle(ChatFormatting.YELLOW));
        }
    }

    public static long laserEnergyConsumptionForTier(int tier) {
        return switch (tier) {
            case 7 -> 40_000L;
            case 8 -> 400_000L;
            case 9 -> 4_000_000L;
            case 10 -> 40_000_000L;
            default -> 0L;
        };
    }
}
