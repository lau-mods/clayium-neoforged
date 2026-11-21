package net.claustra01.clayium.gametest.miner;

import java.util.UUID;
import java.util.function.Consumer;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.machine.miner.BasicOperationBlock;
import net.claustra01.clayium.machine.miner.BasicOperationBlockEntity;
import net.claustra01.clayium.registry.MinerRegistration;
import net.claustra01.clayium.registry.WorldgenRegistration;
import net.claustra01.clayium.world.operation.BlockOperationResult;
import net.claustra01.clayium.world.operation.BlockOperationService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MinerGameTests {
    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 100)
    public static void blockBreakerUsesLootProtectionAndLoadedChunks(GameTestHelper helper) {
        UUID owner = UUID.fromString("7f50dc2c-f61b-4aaa-950b-2a8e1039be29");
        BlockPos machinePosition = new BlockPos(2, 2, 2);
        BasicOperationBlockEntity breaker = placeMachine(
                helper, machinePosition, MinerRegistration.BLOCK_BREAKER.get(), owner);
        BlockPos target = helper.absolutePos(machinePosition.relative(Direction.EAST));

        breaker.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND_PICKAXE));
        helper.getLevel().setBlockAndUpdate(target, Blocks.STONE.defaultBlockState());
        require(breaker.runOperationNow() == BlockOperationResult.SUCCESS, "The breaker did not mine stone");
        require(helper.getLevel().getBlockState(target).isAir(), "The mined stone remained in the world");
        require(outputCount(breaker, Items.COBBLESTONE) == 1, "Stone did not use its current loot table");

        Holder<Enchantment> fortune = helper.getLevel().registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
        Holder<Enchantment> silkTouch = helper.getLevel().registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
        int normalDrops = mineClayOre(helper, breaker, target, null, 12);
        int fortuneDrops = mineClayOre(helper, breaker, target, fortune, 12);
        require(fortuneDrops > normalDrops, "Fortune did not increase the block breaker's aggregate ore drops");

        clearOutputs(breaker);
        ItemStack silkTool = new ItemStack(Items.DIAMOND_PICKAXE);
        silkTool.enchant(silkTouch, 1);
        breaker.inventory().setStackInSlot(0, silkTool);
        helper.getLevel().setBlockAndUpdate(target, WorldgenRegistration.CLAY_ORE.get().defaultBlockState());
        require(breaker.runOperationNow() == BlockOperationResult.SUCCESS, "Silk Touch mining failed");
        require(outputCount(breaker, WorldgenRegistration.CLAY_ORE.get().asItem()) == 1,
                "Silk Touch did not preserve clay ore");

        clearOutputs(breaker);
        ItemStack protectedTool = new ItemStack(Items.DIAMOND_PICKAXE);
        breaker.inventory().setStackInSlot(0, protectedTool);
        helper.getLevel().setBlockAndUpdate(target, Blocks.STONE.defaultBlockState());
        Consumer<BlockEvent.BreakEvent> protect = event -> {
            if (event.getPos().equals(target) && event.getPlayer().getUUID().equals(owner)) {
                event.setCanceled(true);
            }
        };
        NeoForge.EVENT_BUS.addListener(BlockEvent.BreakEvent.class, protect);
        try {
            require(breaker.runOperationNow() == BlockOperationResult.DENIED,
                    "A cancelled BreakEvent was ignored");
        } finally {
            NeoForge.EVENT_BUS.unregister(protect);
        }
        require(helper.getLevel().getBlockState(target).is(Blocks.STONE), "A protected block was removed");
        require(protectedTool.getDamageValue() == 0 && outputCount(breaker, Items.COBBLESTONE) == 0,
                "A denied break damaged the tool or produced output");

        helper.getLevel().setBlockAndUpdate(target, Blocks.BEDROCK.defaultBlockState());
        ItemStack bedrockTool = new ItemStack(Items.DIAMOND_PICKAXE);
        breaker.inventory().setStackInSlot(0, bedrockTool);
        require(breaker.runOperationNow() == BlockOperationResult.DENIED,
                "The breaker removed an unbreakable block");
        require(helper.getLevel().getBlockState(target).is(Blocks.BEDROCK)
                        && breaker.inventory().getStackInSlot(0).getDamageValue() == 0
                        && outputCount(breaker, Items.COBBLESTONE) == 0,
                "A denied bedrock break changed the tool or outputs");

        for (int slot = BasicOperationBlockEntity.OUTPUT_START;
                slot < BasicOperationBlockEntity.OUTPUT_START + BasicOperationBlockEntity.OUTPUT_SLOTS;
                slot++) {
            breaker.inventory().setStackInSlot(slot, new ItemStack(Items.DIAMOND_BLOCK, 64));
        }
        helper.getLevel().setBlockAndUpdate(target, Blocks.STONE.defaultBlockState());
        int[] dropEvents = {0};
        Consumer<BlockDropsEvent> observeDrops = event -> {
            if (event.getPos().equals(target)) {
                dropEvents[0]++;
            }
        };
        NeoForge.EVENT_BUS.addListener(BlockDropsEvent.class, observeDrops);
        try {
            require(breaker.runOperationNow() == BlockOperationResult.NO_OUTPUT_SPACE,
                    "A full output did not stop mining");
        } finally {
            NeoForge.EVENT_BUS.unregister(observeDrops);
        }
        require(dropEvents[0] == 0 && helper.getLevel().getBlockState(target).is(Blocks.STONE)
                        && breaker.inventory().getStackInSlot(0).getDamageValue() == 0,
                "A rejected break posted drops or changed the world or tool");

        BlockPos unloaded = new BlockPos(29_999_000, 64, 29_999_000);
        int chunkX = unloaded.getX() >> 4;
        int chunkZ = unloaded.getZ() >> 4;
        require(helper.getLevel().getChunkSource().getChunkNow(chunkX, chunkZ) == null,
                "The unloaded-chunk precondition was not met");
        require(BlockOperationService.breakBlock(
                        helper.getLevel(), breaker.getBlockPos(), unloaded, owner,
                        new ItemStack(Items.DIAMOND_PICKAXE), stack -> { }, drops -> true, drops -> { })
                        == BlockOperationResult.UNLOADED,
                "An unloaded target was not rejected");
        require(helper.getLevel().getChunkSource().getChunkNow(chunkX, chunkZ) == null,
                "The block operation force-loaded its target chunk");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 100)
    public static void activatorMatchesPlayerUseAndDeniedActionsHaveNoSideEffects(GameTestHelper helper) {
        UUID owner = UUID.fromString("282c1a73-2aaf-4788-9fae-cf3c26573475");
        BlockPos machinePosition = new BlockPos(2, 2, 2);
        BasicOperationBlockEntity activator = placeMachine(
                helper, machinePosition, MinerRegistration.ACTIVATOR.get(), owner);
        BlockPos target = helper.absolutePos(machinePosition.relative(Direction.EAST));
        BlockPos placed = target.relative(Direction.EAST);

        helper.getLevel().setBlockAndUpdate(target, Blocks.SHORT_GRASS.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(placed, Blocks.AIR.defaultBlockState());
        activator.inventory().setStackInSlot(0, new ItemStack(Items.STONE, 2));
        require(activator.runOperationNow() == BlockOperationResult.SUCCESS, "Block placement was not delegated");
        require(helper.getLevel().getBlockState(target).is(Blocks.STONE)
                        && activator.inventory().getStackInSlot(0).getCount() == 1,
                "Delegated placement did not consume exactly one block");

        helper.getLevel().setBlockAndUpdate(target, Blocks.SHORT_GRASS.defaultBlockState());
        activator.inventory().setStackInSlot(0, new ItemStack(Items.FURNACE));
        require(activator.runOperationNow() == BlockOperationResult.SUCCESS
                        && helper.getLevel().getBlockState(target).is(Blocks.FURNACE)
                        && helper.getLevel().getBlockState(target).getValue(BlockStateProperties.HORIZONTAL_FACING)
                                == Direction.WEST,
                "A directional block did not face the operator");

        clearOutputs(activator);
        helper.getLevel().setBlockAndUpdate(target, Blocks.OAK_LOG.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(placed, Blocks.AIR.defaultBlockState());
        activator.inventory().setStackInSlot(0, new ItemStack(Items.IRON_AXE));
        require(activator.runOperationNow() == BlockOperationResult.SUCCESS, "Tool use was not delegated");
        require(helper.getLevel().getBlockState(target).is(Blocks.STRIPPED_OAK_LOG),
                "The delegated axe use did not change the block state");
        require(activator.inventory().getStackInSlot(0).getDamageValue() == 1,
                "The delegated axe use did not apply vanilla durability");

        clearOutputs(activator);
        helper.getLevel().setBlockAndUpdate(target, Blocks.CAULDRON.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(placed, Blocks.AIR.defaultBlockState());
        activator.inventory().setStackInSlot(0, new ItemStack(Items.WATER_BUCKET));
        require(activator.runOperationNow() == BlockOperationResult.SUCCESS, "Bucket use was not delegated");
        require(helper.getLevel().getBlockState(target).is(Blocks.WATER_CAULDRON)
                        && outputCount(activator, Items.BUCKET) == 1,
                "The delegated bucket use did not fill the cauldron and return its container");

        clearOutputs(activator);
        helper.getLevel().setBlockAndUpdate(target, Blocks.OAK_LOG.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(placed, Blocks.AIR.defaultBlockState());
        ItemStack deniedTool = new ItemStack(Items.IRON_AXE);
        activator.inventory().setStackInSlot(0, deniedTool);
        Consumer<PlayerInteractEvent.RightClickBlock> deny = event -> {
            if (event.getPos().equals(target) && event.getEntity().getUUID().equals(owner)) {
                event.setCancellationResult(InteractionResult.FAIL);
                event.setCanceled(true);
            }
        };
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.RightClickBlock.class, deny);
        try {
            require(activator.runOperationNow() == BlockOperationResult.DENIED,
                    "A cancelled RightClickBlock event was ignored");
        } finally {
            NeoForge.EVENT_BUS.unregister(deny);
        }
        require(helper.getLevel().getBlockState(target).is(Blocks.OAK_LOG)
                        && helper.getLevel().getBlockState(placed).isAir(),
                "A denied activation changed the world");
        require(deniedTool.getDamageValue() == 0
                        && activator.inventory().getStackInSlot(0).is(Items.IRON_AXE),
                "A denied activation consumed or damaged its item");
        helper.succeed();
    }

    private static BasicOperationBlockEntity placeMachine(
            GameTestHelper helper,
            BlockPos relativePosition,
            BasicOperationBlock block,
            UUID owner) {
        helper.setBlock(relativePosition, block.defaultBlockState()
                .setValue(BasicOperationBlock.FACING, Direction.EAST));
        BasicOperationBlockEntity operation = (BasicOperationBlockEntity)helper.getBlockEntity(relativePosition);
        operation.setOwner(owner);
        return operation;
    }

    private static int mineClayOre(
            GameTestHelper helper,
            BasicOperationBlockEntity breaker,
            BlockPos target,
            Holder<Enchantment> enchantment,
            int attempts) {
        int total = 0;
        for (int attempt = 0; attempt < attempts; attempt++) {
            clearOutputs(breaker);
            ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
            if (enchantment != null) {
                tool.enchant(enchantment, 3);
            }
            breaker.inventory().setStackInSlot(0, tool);
            helper.getLevel().setBlockAndUpdate(target, WorldgenRegistration.CLAY_ORE.get().defaultBlockState());
            require(breaker.runOperationNow() == BlockOperationResult.SUCCESS, "Clay ore mining failed");
            total += outputCount(breaker, Items.CLAY_BALL);
        }
        return total;
    }

    private static int outputCount(BasicOperationBlockEntity operation, Item item) {
        int count = 0;
        for (int slot = BasicOperationBlockEntity.OUTPUT_START;
                slot < BasicOperationBlockEntity.OUTPUT_START + BasicOperationBlockEntity.OUTPUT_SLOTS;
                slot++) {
            ItemStack stack = operation.inventory().getStackInSlot(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void clearOutputs(BasicOperationBlockEntity operation) {
        for (int slot = BasicOperationBlockEntity.OUTPUT_START;
                slot < BasicOperationBlockEntity.OUTPUT_START + BasicOperationBlockEntity.OUTPUT_SLOTS;
                slot++) {
            operation.inventory().setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private MinerGameTests() {
    }
}
