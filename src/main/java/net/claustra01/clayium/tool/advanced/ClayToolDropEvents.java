package net.claustra01.clayium.tool.advanced;

import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.AdvancedToolRegistration;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

@EventBusSubscriber(modid = ClayiumMod.MOD_ID)
public final class ClayToolDropEvents {
    @SubscribeEvent
    public static void increaseClayOreDrops(BlockDropsEvent event) {
        if (!ClayToolProperties.isOrdinaryClayOre(event.getState())) {
            return;
        }
        ItemStack tool = event.getTool();
        int factor;
        if (tool.is(AdvancedToolRegistration.CLAY_PICKAXE.get())) {
            factor = 4;
        } else if (tool.is(AdvancedToolRegistration.CLAY_SHOVEL.get())) {
            factor = 3;
        } else {
            return;
        }
        List<ItemEntity> clayDrops = event.getDrops().stream()
                .filter(drop -> drop.getItem().is(Items.CLAY_BALL))
                .toList();
        if (clayDrops.isEmpty()) {
            return;
        }
        var enchantments = event.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        int fortune = tool.getEnchantmentLevel(enchantments.getOrThrow(Enchantments.FORTUNE));
        clayDrops.getFirst().getItem().setCount(correctedDropCount(event.getLevel().random, fortune, factor));
        clayDrops.stream().skip(1).forEach(event.getDrops()::remove);
    }

    public static int correctedDropCount(RandomSource random, int fortune, int factor) {
        int baseCount = 4 + random.nextInt(5) * random.nextInt(4);
        int effectiveFortune = (fortune + 1) * factor;
        int bonus = Math.max(0, random.nextInt(effectiveFortune + 2) - 1);
        return baseCount * (bonus + 1);
    }

    private ClayToolDropEvents() {
    }
}
