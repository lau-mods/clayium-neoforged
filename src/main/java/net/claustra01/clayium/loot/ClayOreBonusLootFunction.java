package net.claustra01.clayium.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.claustra01.clayium.ClayiumConfig;
import net.claustra01.clayium.registry.ClayiumItems;
import net.claustra01.clayium.registry.ClayiumLootFunctions;
import net.claustra01.clayium.util.ProgressionRate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Applies original ClayOre drop formula: ProgressionRate-scaled base (4 + rand(5)*rand(4))
 * with effective Fortune (fortune+1)*3 for Clay Shovel and (fortune+1)*4 for Clay Pickaxe.
 */
public class ClayOreBonusLootFunction extends LootItemConditionalFunction {
    public static final MapCodec<ClayOreBonusLootFunction> CODEC = RecordCodecBuilder.mapCodec(
            instance -> commonFields(instance).apply(instance, ClayOreBonusLootFunction::new)
    );

    public ClayOreBonusLootFunction(List<LootItemCondition> predicates) {
        super(predicates);
    }

    public static Builder<?> builder() {
        return simpleBuilder(ClayOreBonusLootFunction::new);
    }

    @Override
    public LootItemFunctionType<ClayOreBonusLootFunction> getType() {
        return ClayiumLootFunctions.CLAY_ORE_BONUS.get();
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        Holder<Enchantment> fortuneHolder = context.getLevel()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.FORTUNE);
        int fortune = tool != null ? EnchantmentHelper.getItemEnchantmentLevel(fortuneHolder, tool) : 0;
        boolean shovel = tool != null && tool.is(ClayiumItems.byPath("clay_shovel").get());
        boolean pickaxe = tool != null && tool.is(ClayiumItems.byPath("clay_pickaxe").get());
        double rate = ClayiumConfig.PROGRESSION_RATE.get();
        int count = computeDropCount(context.getRandom(), fortune, shovel, pickaxe, rate);
        stack.setCount(count);
        return stack;
    }

    public static int computeDropCount(
            RandomSource random,
            int fortune,
            boolean isClayShovel,
            boolean isClayPickaxe,
            double progressionRate) {
        int effectiveFortune = fortune;
        if (isClayShovel) {
            effectiveFortune = (fortune + 1) * 3;
        } else if (isClayPickaxe) {
            effectiveFortune = (fortune + 1) * 4;
        }
        int base = ProgressionRate.multiplyI(4 + random.nextInt(5) * random.nextInt(4), progressionRate);
        if (effectiveFortune > 0) {
            int bonus = Math.max(0, random.nextInt(effectiveFortune + 2) - 1);
            return base * (bonus + 1);
        }
        return base;
    }
}
