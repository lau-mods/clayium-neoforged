package net.claustra01.clayium.gametest.advancedtool;

import java.util.Set;
import java.util.function.Consumer;
import net.claustra01.clayium.ClayiumConfig;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.AdvancedToolRegistration;
import net.claustra01.clayium.registry.WorldgenRegistration;
import net.claustra01.clayium.tool.advanced.AdvancedToolPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AdvancedToolGameTests {
    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 100)
    public static void threeByThreeMiningStaysInPlaneAndRespectsBreakProtection(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        ItemStack tool = new ItemStack(AdvancedToolRegistration.CLAY_STEEL_PICKAXE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, tool);
        BlockPos center = helper.absolutePos(new BlockPos(5, 5, 5));
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                helper.getLevel().setBlock(center.offset(x, y, 0), Blocks.STONE.defaultBlockState(), 3);
            }
        }
        BlockPos single = helper.absolutePos(new BlockPos(5, 5, 9));
        helper.getLevel().setBlock(single, Blocks.STONE.defaultBlockState(), 3);
        helper.getLevel().setBlock(single.above(), Blocks.STONE.defaultBlockState(), 3);
        player.setPos(single.getX() + 0.5D, single.getY(), single.getZ() - 3.0D);
        player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(single));
        require(player.gameMode.destroyBlock(single), "Single mode did not break its primary block");
        require(helper.getLevel().getBlockState(single.above()).is(Blocks.STONE),
                "Single mode broke an additional block");
        AdvancedToolRegistration.CLAY_STEEL_PICKAXE.get().useOn(new UseOnContext(
                player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(center), Direction.NORTH, center, false)));
        require(tool.getOrDefault(AdvancedToolRegistration.MODE.get(), 0) == 1,
                "Right-click did not switch to 3x3 mode");
        BlockPos protectedPosition = center.offset(1, 1, 0);
        BlockPos outside = center.offset(2, 0, 0);
        BlockPos behind = center.offset(0, 0, 1);
        BlockPos unsuitable = center.offset(-1, -1, 0);
        helper.getLevel().setBlock(outside, Blocks.STONE.defaultBlockState(), 3);
        helper.getLevel().setBlock(behind, Blocks.STONE.defaultBlockState(), 3);
        helper.getLevel().setBlock(unsuitable, Blocks.DIRT.defaultBlockState(), 3);
        player.setPos(center.getX() + 0.5D, center.getY(), center.getZ() - 3.0D);
        player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(center));
        Consumer<BlockEvent.BreakEvent> protect = event -> {
            if (event.getPos().equals(protectedPosition)) {
                event.setCanceled(true);
            }
        };
        NeoForge.EVENT_BUS.addListener(BlockEvent.BreakEvent.class, protect);
        try {
            require(player.gameMode.destroyBlock(center), "Area mode did not break its primary block");
        } finally {
            NeoForge.EVENT_BUS.unregister(protect);
        }
        require(helper.getLevel().getBlockState(center).isAir(), "Primary block survived area mining");
        int broken = 0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                BlockPos position = center.offset(x, y, 0);
                if (!position.equals(center) && helper.getLevel().getBlockState(position).isAir()) {
                    broken++;
                }
            }
        }
        require(broken == 6, "Area mining did not break exactly six eligible neighbours");
        require(helper.getLevel().getBlockState(protectedPosition).is(Blocks.STONE)
                        && helper.getLevel().getBlockState(unsuitable).is(Blocks.DIRT),
                "Protection or tool suitability was ignored");
        require(helper.getLevel().getBlockState(outside).is(Blocks.STONE)
                        && helper.getLevel().getBlockState(behind).is(Blocks.STONE),
                "Area mining escaped its plane");
        require(tool.getDamageValue() == 8, "Successful blocks did not each consume one durability");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 100)
    public static void rememberedClayShapeRotatesMinesAndSurvivesItemSerialization(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.setYRot(0.0F);
        BlockPos clayOrigin = helper.absolutePos(new BlockPos(4, 4, 4));
        Set<BlockPos> clayShape = Set.of(clayOrigin, clayOrigin.east(), clayOrigin.above(), clayOrigin.south());
        clayShape.forEach(position -> helper.getLevel().setBlock(position, Blocks.CLAY.defaultBlockState(), 3));
        ItemStack tool = new ItemStack(AdvancedToolRegistration.CLAY_STEEL_SHOVEL.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, tool);
        UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(clayOrigin), Direction.NORTH, clayOrigin, false));
        for (int mode = 1; mode <= 2; mode++) {
            AdvancedToolRegistration.CLAY_STEEL_SHOVEL.get().useOn(context);
            require(tool.getOrDefault(AdvancedToolRegistration.MODE.get(), -1) == mode,
                    "Use did not advance the shape mode");
        }
        AdvancedToolRegistration.CLAY_STEEL_SHOVEL.get().useOn(context);
        AdvancedToolPattern pattern = tool.getOrDefault(
                AdvancedToolRegistration.PATTERN.get(), AdvancedToolPattern.EMPTY);
        require(pattern.offsets().size() == 4, "Clay shape was not recorded");
        ItemStack loaded = ItemStack.parse(helper.getLevel().registryAccess(),
                        tool.save(helper.getLevel().registryAccess()))
                .orElseThrow(() -> new GameTestAssertException("Advanced tool did not deserialize"));
        require(loaded.getOrDefault(AdvancedToolRegistration.MODE.get(), -1) == 2
                        && loaded.getOrDefault(AdvancedToolRegistration.PATTERN.get(), AdvancedToolPattern.EMPTY).equals(pattern),
                "Remembered mode or shape did not survive saving");
        player.setItemInHand(InteractionHand.MAIN_HAND, loaded);
        BlockPos target = helper.absolutePos(new BlockPos(10, 5, 5));
        BlockPos protectedPosition = target.south();
        BlockPos behind = target.west();
        BlockPos above = target.above();
        for (BlockPos position : Set.of(target, protectedPosition, behind, above, target.north(), target.above(2))) {
            helper.getLevel().setBlock(position, Blocks.DIRT.defaultBlockState(), 3);
        }
        player.setPos(target.getX() + 3.0D, target.getY(), target.getZ() + 0.5D);
        player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(target));
        Consumer<BlockEvent.BreakEvent> protect = event -> {
            if (event.getPos().equals(protectedPosition)) {
                event.setCanceled(true);
            }
        };
        NeoForge.EVENT_BUS.addListener(BlockEvent.BreakEvent.class, protect);
        try {
            require(player.gameMode.destroyBlock(target), "Remembered mode did not mine the primary block");
        } finally {
            NeoForge.EVENT_BUS.unregister(protect);
        }
        require(helper.getLevel().getBlockState(target).isAir()
                        && helper.getLevel().getBlockState(behind).isAir()
                        && helper.getLevel().getBlockState(above).isAir(),
                "Rotated remembered shape did not mine its literal expected positions");
        require(helper.getLevel().getBlockState(protectedPosition).is(Blocks.DIRT)
                        && helper.getLevel().getBlockState(target.north()).is(Blocks.DIRT)
                        && helper.getLevel().getBlockState(target.above(2)).is(Blocks.DIRT),
                "Remembered mining crossed protection or pattern bounds");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void fourClayToolsKeepOriginalDurabilitySpeedOreHarvestAndConfig(GameTestHelper helper) {
        ItemStack pickaxe = new ItemStack(AdvancedToolRegistration.CLAY_PICKAXE.get());
        ItemStack shovel = new ItemStack(AdvancedToolRegistration.CLAY_SHOVEL.get());
        ItemStack steelPickaxe = new ItemStack(AdvancedToolRegistration.CLAY_STEEL_PICKAXE.get());
        ItemStack steelShovel = new ItemStack(AdvancedToolRegistration.CLAY_STEEL_SHOVEL.get());
        require(pickaxe.getMaxDamage() == 500 && shovel.getMaxDamage() == 500,
                "Clay tool durability differs from the original 500");
        require(steelPickaxe.getMaxDamage() == 10_000 && steelShovel.getMaxDamage() == 10_000,
                "Clay Steel tool durability differs from the original 10000");
        require(AdvancedToolRegistration.CLAY_PICKAXE.get().getDestroySpeed(
                        pickaxe, WorldgenRegistration.CLAY_ORE.get().defaultBlockState()) == 32.0F,
                "Clay Pickaxe did not use the original Clay Ore speed 32");
        require(AdvancedToolRegistration.CLAY_SHOVEL.get().getDestroySpeed(
                        shovel, WorldgenRegistration.CLAY_ORE.get().defaultBlockState()) == 12.0F,
                "Clay Shovel did not use the original Clay Ore speed 12");
        require(AdvancedToolRegistration.CLAY_SHOVEL.get().getDestroySpeed(
                        shovel, Blocks.CLAY.defaultBlockState()) == 32.0F,
                "Clay Shovel did not use the original clay-block speed 32");
        require(AdvancedToolRegistration.CLAY_STEEL_PICKAXE.get().getDestroySpeed(
                        steelPickaxe, Blocks.STONE.defaultBlockState()) == 48.0F,
                "Clay Steel Pickaxe did not mine at six times diamond speed");
        require(AdvancedToolRegistration.CLAY_PICKAXE.get().isCorrectToolForDrops(
                        pickaxe, WorldgenRegistration.LARGE_DENSE_CLAY_ORE.get().defaultBlockState())
                        && AdvancedToolRegistration.CLAY_SHOVEL.get().isCorrectToolForDrops(
                                shovel, WorldgenRegistration.LARGE_DENSE_CLAY_ORE.get().defaultBlockState()),
                "Basic Clay tools cannot harvest every original Clay Ore rank");
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        BlockPos orePosition = helper.absolutePos(new BlockPos(4, 4, 4));
        int[] observed = {-1};
        int[] unmodified = {-1};
        Consumer<BlockDropsEvent> seedDrops = event -> {
            if (event.getPos().equals(orePosition)) {
                unmodified[0] = event.getDrops().stream()
                        .filter(drop -> drop.getItem().is(Items.CLAY_BALL))
                        .mapToInt(drop -> drop.getItem().getCount()).sum();
                event.getLevel().random.setSeed(7L);
            }
        };
        Consumer<BlockDropsEvent> readDrops = event -> {
            if (event.getPos().equals(orePosition)) {
                observed[0] = event.getDrops().stream()
                        .filter(drop -> drop.getItem().is(Items.CLAY_BALL))
                        .mapToInt(drop -> drop.getItem().getCount()).sum();
            }
        };
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.HIGHEST,
                BlockDropsEvent.class, seedDrops);
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,
                BlockDropsEvent.class, readDrops);
        try {
            ItemStack[] tools = {pickaxe, shovel, steelPickaxe};
            for (int index = 0; index < tools.length; index++) {
                helper.getLevel().setBlock(orePosition, WorldgenRegistration.CLAY_ORE.get().defaultBlockState(), 3);
                player.setItemInHand(InteractionHand.MAIN_HAND, tools[index]);
                player.setPos(orePosition.getX() + 0.5D, orePosition.getY(), orePosition.getZ() - 3.0D);
                player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,
                        Vec3.atCenterOf(orePosition));
                observed[0] = -1;
                require(player.gameMode.destroyBlock(orePosition), "Clay Ore was not mined by a real player path");
                if (index == 0) {
                    require(observed[0] == 18, "Clay Pickaxe did not apply its exact fourfold correction");
                } else if (index == 1) {
                    require(observed[0] == 6, "Clay Shovel did not apply its exact threefold correction");
                } else {
                    require(observed[0] == unmodified[0] && observed[0] >= 4 && observed[0] <= 16,
                            "Clay Steel tool incorrectly applied the basic-tool correction");
                }
            }
            ItemStack silkPickaxe = new ItemStack(AdvancedToolRegistration.CLAY_PICKAXE.get());
            silkPickaxe.enchant(helper.getLevel().registryAccess()
                    .lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                    .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), 1);
            helper.getLevel().setBlock(orePosition, WorldgenRegistration.CLAY_ORE.get().defaultBlockState(), 3);
            player.setItemInHand(InteractionHand.MAIN_HAND, silkPickaxe);
            observed[0] = -1;
            require(player.gameMode.destroyBlock(orePosition), "Silk Touch Clay Ore was not mined");
            require(observed[0] == 0, "Silk Touch was changed into a Clay Ball drop");
        } finally {
            NeoForge.EVENT_BUS.unregister(seedDrops);
            NeoForge.EVENT_BUS.unregister(readDrops);
        }
        require(ClayiumConfig.CLAY_STEEL_PICKAXE_RANGE.getDefault() == 2,
                "ClaySteelPickaxeRange default is not 2");
        require(AdvancedToolRegistration.ALL.size() == 4,
                "The four original Clay tools were not registered");
        helper.succeed();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private AdvancedToolGameTests() {
    }
}
