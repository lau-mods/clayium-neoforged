package net.claustra01.clayium.gametest.environment;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.machine.crucible.QuartzCrucibleBlockEntity;
import net.claustra01.clayium.machine.crucible.QuartzCrucibleBlock;
import net.claustra01.clayium.machine.crucible.QuartzCrucibleStage;
import net.claustra01.clayium.registry.EnvironmentMachineRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class QuartzCrucibleGameTests {
    private static final BlockPos CRUCIBLE_POS = new BlockPos(2, 2, 2);

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 200)
    public static void quartzCrucibleRequiresHeatingAndOneStringWithoutDuplication(GameTestHelper helper) {
        helper.setBlock(CRUCIBLE_POS, EnvironmentMachineRegistration.QUARTZ_CRUCIBLE.get());
        QuartzCrucibleBlockEntity crucible = (QuartzCrucibleBlockEntity)helper.getBlockEntity(CRUCIBLE_POS);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack impure = new ItemStack(MaterialRegistration.MATERIAL_ITEMS.get("impure_silicon_ingot").get(), 12);
        player.setItemInHand(InteractionHand.MAIN_HAND, impure);
        helper.useBlock(CRUCIBLE_POS, player);
        require(impure.getCount() == 4 && crucible.impureSilicon() == 8
                        && crucible.heatingTicks() == 8 * QuartzCrucibleBlockEntity.HEATING_TICKS_PER_INGOT,
                "Inserting twelve ingots did not leave four in hand and heat eight");
        require(helper.getBlockState(CRUCIBLE_POS).getValue(QuartzCrucibleBlock.STAGE) == QuartzCrucibleStage.HEATING,
                "Inserting silicon did not expose the heating stage to clients");

        ItemStack string = new ItemStack(Items.STRING, 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, string);
        helper.useBlock(CRUCIBLE_POS, player);
        require(string.getCount() == 2 && crucible.impureSilicon() == 8,
                "String was consumed before the batch finished heating");
        for (int tick = 0; tick < 2_400; tick++) {
            crucible.tickServer();
        }
        CompoundTag saved = crucible.saveWithFullMetadata(helper.getLevel().registryAccess());
        QuartzCrucibleBlockEntity loaded = new QuartzCrucibleBlockEntity(
                helper.absolutePos(CRUCIBLE_POS), crucible.getBlockState());
        loaded.loadWithComponents(saved, helper.getLevel().registryAccess());
        require(loaded.impureSilicon() == 8 && loaded.heatingTicks() == 2_400,
                "The halfway crucible stage did not survive save and load");
        helper.useBlock(CRUCIBLE_POS, player);
        require(string.getCount() == 2, "Reloading bypassed the remaining heating stage");
        for (int tick = 0; tick < 2_400; tick++) {
            crucible.tickServer();
        }
        require(crucible.isReady(), "The crucible did not become ready after heating");
        require(helper.getBlockState(CRUCIBLE_POS).getValue(QuartzCrucibleBlock.STAGE) == QuartzCrucibleStage.READY,
                "The ready stage was not exposed to clients");
        helper.useBlock(CRUCIBLE_POS, player);
        require(string.getCount() == 1 && crucible.impureSilicon() == 0,
                "Drawing ready silicon did not consume exactly one string");
        require(helper.getBlockState(CRUCIBLE_POS).getValue(QuartzCrucibleBlock.STAGE) == QuartzCrucibleStage.EMPTY,
                "Drawing silicon did not restore the empty stage");
        helper.useBlock(CRUCIBLE_POS, player);
        require(string.getCount() == 1, "The finished batch could be drawn again");
        require(countDrops(helper, CRUCIBLE_POS, "silicon_ingot") == 8,
                "Eight impure ingots did not yield exactly eight silicon ingots");

        BlockPos second = CRUCIBLE_POS.east(3);
        helper.setBlock(second, EnvironmentMachineRegistration.QUARTZ_CRUCIBLE.get());
        var other = (QuartzCrucibleBlockEntity)helper.getBlockEntity(second);
        ItemStack secondImpure = new ItemStack(MaterialRegistration.MATERIAL_ITEMS.get("impure_silicon_ingot").get(), 3);
        player.setItemInHand(InteractionHand.MAIN_HAND, secondImpure);
        helper.useBlock(second, player);
        require(secondImpure.isEmpty() && other.impureSilicon() == 3,
                "The second crucible did not accept three impure ingots");
        other.tickServer();
        helper.setBlock(second, Blocks.AIR);
        require(countDrops(helper, second, "impure_silicon_ingot") == 3
                        && countDrops(helper, second, "silicon_ingot") == 0,
                "Breaking a heating crucible did not return exactly its impure contents");
        helper.succeed();
    }

    private static int countDrops(GameTestHelper helper, BlockPos position, String id) {
        return helper.getLevel().getEntitiesOfClass(
                        ItemEntity.class, new AABB(helper.absolutePos(position)).inflate(1.0D)).stream()
                .map(ItemEntity::getItem)
                .filter(stack -> stack.is(MaterialRegistration.MATERIAL_ITEMS.get(id).get()))
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private QuartzCrucibleGameTests() {
    }
}
