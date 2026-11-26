package net.claustra01.clayium.gametest.behavior;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.laser.LaserBeam;
import net.claustra01.clayium.laser.LaserIrradiation;
import net.claustra01.clayium.laser.LaserPath;
import net.claustra01.clayium.laser.ClayEnergyLaserBlock;
import net.claustra01.clayium.laser.ClayEnergyLaserBlockEntity;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.logistics.filter.FilterCopyValidator;
import net.claustra01.clayium.logistics.filter.FilterDuplicatorItem;
import net.claustra01.clayium.logistics.filter.ItemFilterSettings;
import net.claustra01.clayium.machine.crucible.QuartzCrucibleBlockEntity;
import net.claustra01.clayium.registry.AreaMachineRegistration;
import net.claustra01.clayium.registry.EnvironmentMachineRegistration;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.claustra01.clayium.registry.LaserRegistration;
import net.claustra01.clayium.registry.ClayTreeRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.WorkbenchRegistration;
import net.claustra01.clayium.tool.ClayCraftingToolItem;
import net.claustra01.clayium.world.area.AreaBounds;
import net.claustra01.clayium.world.area.AreaMachineBlockEntity;
import net.claustra01.clayium.world.area.AreaMachineType;
import net.claustra01.clayium.workbench.ClayWorkTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class OriginalBehaviorGameTests {
    private static final UUID OWNER = UUID.fromString("dd2faee6-8450-4dbc-ae51-dd987a71b921");

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void craftingToolsReturnDamageAndRawClayExactlyOnce(GameTestHelper helper) {
        verifyTool(WorkbenchRegistration.CLAY_ROLLING_PIN.toStack(), 60, 4);
        verifyTool(WorkbenchRegistration.CLAY_SLICER.toStack(), 60, 3);
        verifyTool(WorkbenchRegistration.CLAY_SPATULA.toStack(), 36, 2);

        verifyTableTool(helper, new BlockPos(2, 2, 2), WorkbenchRegistration.CLAY_ROLLING_PIN.toStack(),
                3, new ItemStack(Items.CLAY), 4, 60, 4);
        verifyTableTool(helper, new BlockPos(4, 2, 2), WorkbenchRegistration.CLAY_SLICER.toStack(),
                4, new ItemStack(MaterialRegistration.MATERIAL_ITEMS.get("clay_plate").get()), 2, 60, 3);
        verifyTableTool(helper, new BlockPos(6, 2, 2), WorkbenchRegistration.CLAY_SPATULA.toStack(),
                5, new ItemStack(MaterialRegistration.MATERIAL_ITEMS.get("clay_plate").get()), 4, 36, 2);
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 100)
    public static void areaFiltersAndActivatorPreservePriorityProtectionAndInteraction(GameTestHelper helper) {
        AreaMachineBlockEntity miner = placeMachine(helper, new BlockPos(2, 2, 2), AreaMachineType.MINER);
        ItemStack redstoneFilter = filterFor(new ItemStack(Items.REDSTONE_ORE));
        miner.inventory().setStackInSlot(AreaMachineBlockEntity.HARVEST_FILTER_SLOT, redstoneFilter.copy());
        miner.inventory().setStackInSlot(AreaMachineBlockEntity.FORTUNE_FILTER_SLOT, redstoneFilter.copy());
        miner.inventory().setStackInSlot(AreaMachineBlockEntity.SILK_TOUCH_FILTER_SLOT, redstoneFilter.copy());
        BlockPos ore = new BlockPos(3, 2, 2);
        helper.setBlock(ore, Blocks.REDSTONE_ORE);
        miner.configureArea(new AreaBounds(helper.absolutePos(ore), helper.absolutePos(ore)));
        require(miner.runOperationsNow(1) == 1 && outputCount(miner, Items.REDSTONE_ORE) == 1
                        && outputCount(miner, Items.REDSTONE) == 0,
                "Silk Touch did not override Fortune for the independently matching filter");
        miner.inventory().setStackInSlot(AreaMachineBlockEntity.SILK_TOUCH_FILTER_SLOT, ItemStack.EMPTY);
        BlockPos fortuneOre = new BlockPos(5, 2, 2);
        helper.setBlock(fortuneOre, Blocks.REDSTONE_ORE);
        miner.configureArea(new AreaBounds(helper.absolutePos(fortuneOre), helper.absolutePos(fortuneOre)));
        require(miner.runOperationsNow(1) == 1 && outputCount(miner, Items.REDSTONE) >= 4,
                "Fortune-only mining did not produce redstone drops");
        miner.inventory().setStackInSlot(AreaMachineBlockEntity.HARVEST_FILTER_SLOT,
                filterFor(new ItemStack(Items.STONE)));
        miner.inventory().setStackInSlot(AreaMachineBlockEntity.FORTUNE_FILTER_SLOT, ItemStack.EMPTY);
        BlockPos normalStone = new BlockPos(6, 2, 2);
        helper.setBlock(normalStone, Blocks.STONE);
        miner.configureArea(new AreaBounds(helper.absolutePos(normalStone), helper.absolutePos(normalStone)));
        require(miner.runOperationsNow(1) == 1 && outputCount(miner, Items.COBBLESTONE) == 1,
                "The ordinary harvest filter did not produce a normal drop");

        BlockPos protectedOre = new BlockPos(4, 2, 2);
        helper.setBlock(protectedOre, Blocks.REDSTONE_ORE);
        miner.configureArea(new AreaBounds(helper.absolutePos(protectedOre), helper.absolutePos(protectedOre)));
        Consumer<BlockEvent.BreakEvent> deny = event -> {
            if (event.getPos().equals(helper.absolutePos(protectedOre))) event.setCanceled(true);
        };
        NeoForge.EVENT_BUS.addListener(BlockEvent.BreakEvent.class, deny);
        try {
            require(miner.runOperationsNow(1) == 0 && helper.getBlockState(protectedOre).is(Blocks.REDSTONE_ORE),
                    "The three-filter miner bypassed a server protection event");
        } finally {
            NeoForge.EVENT_BUS.unregister(deny);
        }

        AreaMachineBlockEntity activator = placeMachine(helper, new BlockPos(2, 2, 6), AreaMachineType.ACTIVATOR);
        BlockPos entityCell = new BlockPos(3, 2, 6);
        activator.configureArea(new AreaBounds(helper.absolutePos(entityCell), helper.absolutePos(entityCell)));
        activator.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND_HELMET));
        activator.applyControl(net.claustra01.clayium.world.area.AreaGuiHost.BUTTON_TARGET);
        ArmorStand stand = new ArmorStand(helper.getLevel(),
                helper.absolutePos(entityCell).getX() + 0.5D, helper.absolutePos(entityCell).getY(),
                helper.absolutePos(entityCell).getZ() + 0.5D);
        helper.getLevel().addFreshEntity(stand);
        require(activator.runOperationsNow(1) == 1 && stand.getItemBySlot(
                        net.minecraft.world.entity.EquipmentSlot.HEAD).is(Items.DIAMOND_HELMET),
                "Entity interaction was not delegated by the area activator");
        require(activator.inventory().getStackInSlot(0).isEmpty(),
                "The activator did not consume the helmet equipped on the entity");
        BlockPos deniedCell = new BlockPos(4, 2, 6);
        activator.configureArea(new AreaBounds(helper.absolutePos(deniedCell), helper.absolutePos(deniedCell)));
        activator.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND_HELMET));
        ArmorStand deniedStand = new ArmorStand(helper.getLevel(),
                helper.absolutePos(deniedCell).getX() + 0.5D, helper.absolutePos(deniedCell).getY(),
                helper.absolutePos(deniedCell).getZ() + 0.5D);
        helper.getLevel().addFreshEntity(deniedStand);
        Consumer<PlayerInteractEvent.EntityInteractSpecific> denyEntity = event -> {
            if (event.getTarget() == deniedStand) event.setCanceled(true);
        };
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.EntityInteractSpecific.class, denyEntity);
        try {
            activator.runOperationsNow(1);
            require(deniedStand.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).isEmpty()
                            && activator.inventory().getStackInSlot(0).is(Items.DIAMOND_HELMET),
                    "The activator bypassed EntityInteractSpecific cancellation");
        } finally {
            NeoForge.EVENT_BUS.unregister(denyEntity);
        }
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        activator.applyControl(net.claustra01.clayium.world.area.AreaGuiHost.BUTTON_RAY_TRACE);
        activator.applyControl(net.claustra01.clayium.world.area.AreaGuiHost.BUTTON_SNEAK);
        // Original itemDirectionMemory: remember a ray trace on another block, then apply it.
        ItemStack card = WorkbenchRegistration.DIRECTION_MEMORY_CARD.toStack();
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, card);
        BlockPos rememberedBlock = new BlockPos(6, 2, 6);
        helper.setBlock(rememberedBlock, Blocks.STONE);
        useDirectionCard(player, helper.absolutePos(rememberedBlock), Direction.UP, "remember");
        var remembered = card.get(LogisticsRegistration.RAY_TRACE_MEMORY.get());
        require(remembered != null,
                "The direction memory card did not remember a ray trace");
        useDirectionCard(player, activator.getBlockPos(), Direction.NORTH, "apply");
        require(remembered.equals(activator.rayTraceMemory()),
                "The direction memory card did not replace the activator ray trace through item interaction");
        CompoundTag saved = activator.saveWithFullMetadata(helper.getLevel().registryAccess());
        AreaMachineBlockEntity loaded = new AreaMachineBlockEntity(activator.getBlockPos(), activator.getBlockState());
        loaded.loadWithComponents(saved, helper.getLevel().registryAccess());
        require(loaded.activatorRayTrace() && loaded.activatorSneaking() && loaded.rayTraceMemory() != null,
                "Ray trace memory or sneak settings did not survive NBT");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 200)
    public static void laserTransmitsConvertsAccumulatesAndHonorsProtection(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos source = helper.absolutePos(new BlockPos(2, 2, 2));
        helper.setBlock(new BlockPos(2, 2, 2), LaserRegistration.LASERS.get(10).block().get()
                .defaultBlockState().setValue(ClayEnergyLaserBlock.FACING, Direction.EAST));
        ClayEnergyLaserBlockEntity laser = helper.getBlockEntity(new BlockPos(2, 2, 2));
        laser.energyStorage().add(ClayEnergy.ofInternal(80_000_000L));
        BlockPos glass = source.east();
        BlockPos ore = source.east(2);
        level.setBlockAndUpdate(glass, Blocks.GLASS.defaultBlockState());
        level.setBlockAndUpdate(ore, Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState());
        LaserPath path = LaserPath.find(level, source, Direction.EAST);
        ClayEnergyLaserBlockEntity.serverTick(level, source, laser.getBlockState(), laser);
        require(laser.energyStorage().stored().internalValue() == 40_000_000L,
                "The placed laser did not consume its original CE per tick");
        require(path.target().equals(ore) && path.length() == 2,
                "Glass was not transparent to the original 32-block laser path");
        BlockPos lastReachable = source.above(31);
        BlockPos beyondRange = source.above(32);
        for (int distance = 1; distance <= 32; distance++) {
            level.setBlockAndUpdate(source.above(distance), Blocks.AIR.defaultBlockState());
        }
        level.setBlockAndUpdate(lastReachable, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(beyondRange, Blocks.STONE.defaultBlockState());
        require(LaserPath.find(level, source, Direction.UP).target().equals(lastReachable),
                "A target at distance 31 was not reachable");
        level.removeBlock(lastReachable, false);
        require(LaserPath.find(level, source, Direction.UP).target() == null,
                "A target at distance 32 was reached outside the laser limit");
        LaserIrradiation irradiation = new LaserIrradiation();
        LaserBeam strong = new LaserBeam(1_000L, 1_000L, 1_000L);
        for (int tick = 0; tick <= LaserIrradiation.HISTORY_LENGTH; tick++) {
            irradiation.irradiate(level, source, path, strong);
        }
        require(level.getBlockState(ore).is(Blocks.DIAMOND_BLOCK),
                "A deepslate ore did not use its stone-ore laser conversion");
        Block[][] conversions = {
                {Blocks.COAL_ORE, Blocks.COAL_BLOCK}, {Blocks.IRON_ORE, Blocks.IRON_BLOCK},
                {Blocks.GOLD_ORE, Blocks.GOLD_BLOCK}, {Blocks.DIAMOND_ORE, Blocks.DIAMOND_BLOCK},
                {Blocks.REDSTONE_ORE, Blocks.REDSTONE_BLOCK}, {Blocks.LAPIS_ORE, Blocks.LAPIS_BLOCK},
                {Blocks.EMERALD_ORE, Blocks.EMERALD_BLOCK},
                {Blocks.DEEPSLATE_COAL_ORE, Blocks.COAL_BLOCK},
                {Blocks.DEEPSLATE_IRON_ORE, Blocks.IRON_BLOCK},
                {Blocks.DEEPSLATE_GOLD_ORE, Blocks.GOLD_BLOCK},
                {Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DIAMOND_BLOCK},
                {Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.REDSTONE_BLOCK},
                {Blocks.DEEPSLATE_LAPIS_ORE, Blocks.LAPIS_BLOCK},
                {Blocks.DEEPSLATE_EMERALD_ORE, Blocks.EMERALD_BLOCK}
        };
        for (Block[] conversion : conversions) {
            level.setBlockAndUpdate(ore, conversion[0].defaultBlockState());
            LaserPath orePath = new LaserPath(2, ore, level.getBlockState(ore));
            for (int tick = 0; tick <= LaserIrradiation.HISTORY_LENGTH; tick++) {
                irradiation.irradiate(level, source, orePath, strong);
            }
            require(level.getBlockState(ore).is(conversion[1]), "A stone or deepslate ore did not convert");
        }
        CompoundTag accumulated = new CompoundTag();
        irradiation.save(accumulated);
        require(accumulated.getLongArray("history").length == LaserIrradiation.HISTORY_LENGTH,
                "The laser did not save ten energy samples");
        BlockPos changedTarget = source.north(3);
        level.setBlockAndUpdate(changedTarget, Blocks.STONE.defaultBlockState());
        irradiation.irradiate(level, source,
                new LaserPath(3, changedTarget, level.getBlockState(changedTarget)), strong);
        CompoundTag reset = new CompoundTag();
        irradiation.save(reset);
        require(reset.getLong("total") == 0L && reset.getLongArray("history")[0] == 0L,
                "Changing laser target or distance retained the previous energy history");
        AreaMachineBlockEntity receiver = placeMachine(helper, new BlockPos(2, 2, 5), AreaMachineType.MINER);
        LaserPath receiverPath = new LaserPath(3, receiver.getBlockPos(), receiver.getBlockState());
        require(receiverPath.deliver(level, strong, Direction.SOUTH, irradiation, source),
                "The laser receiver did not take priority over irradiation");
        CompoundTag received = new CompoundTag();
        irradiation.save(received);
        require(received.getLong("total") == 0L && level.getBlockState(receiver.getBlockPos())
                .is(receiver.getBlockState().getBlock()), "Irradiation changed a laser receiver");

        BlockPos bedrock = source.south(2);
        level.setBlockAndUpdate(bedrock, Blocks.BEDROCK.defaultBlockState());
        LaserPath bedrockPath = new LaserPath(2, bedrock, level.getBlockState(bedrock));
        for (int tick = 0; tick <= LaserIrradiation.HISTORY_LENGTH; tick++) {
            irradiation.irradiate(level, source, bedrockPath, strong);
        }
        require(level.getBlockState(bedrock).is(Blocks.BEDROCK), "Laser destroyed an unbreakable block");

        BlockPos sapling = source.south(3);
        level.setBlockAndUpdate(sapling, Blocks.OAK_SAPLING.defaultBlockState());
        LaserBeam staged = findBeamInRange(300L, 999L);
        LaserPath saplingPath = new LaserPath(3, sapling, level.getBlockState(sapling));
        for (int tick = 0; tick <= LaserIrradiation.HISTORY_LENGTH; tick++) {
            irradiation.irradiate(level, source, saplingPath, staged);
        }
        require(level.getBlockState(sapling).getValue(SaplingBlock.STAGE) == 1,
                "The verified 300-999 sapling stage threshold was not applied");
        BlockPos treeSource = source.south(5);
        level.setBlockAndUpdate(treeSource.below(), Blocks.DIRT.defaultBlockState());
        level.setBlockAndUpdate(treeSource, Blocks.OAK_SAPLING.defaultBlockState());
        LaserPath treePath = new LaserPath(5, treeSource, level.getBlockState(treeSource));
        for (int tick = 0; tick <= LaserIrradiation.HISTORY_LENGTH; tick++) {
            irradiation.irradiate(level, source, treePath, strong);
        }
        require(level.getBlockState(treeSource).is(ClayTreeRegistration.CLAY_TREE_SAPLING.get()),
                "The 300,000-energy clay-tree conversion did not occur");

        BlockPos protectedStone = source.south(4);
        level.setBlockAndUpdate(protectedStone, Blocks.STONE.defaultBlockState());
        LaserPath protectedPath = new LaserPath(4, protectedStone, level.getBlockState(protectedStone));
        Consumer<BlockEvent.BreakEvent> deny = event -> {
            if (event.getPos().equals(protectedStone)) event.setCanceled(true);
        };
        NeoForge.EVENT_BUS.addListener(BlockEvent.BreakEvent.class, deny);
        try {
            for (int tick = 0; tick <= LaserIrradiation.HISTORY_LENGTH; tick++) {
                irradiation.irradiate(level, source, protectedPath, strong);
            }
            require(level.getBlockState(protectedStone).is(Blocks.STONE),
                    "Laser bypassed the block break protection event");
        } finally {
            NeoForge.EVENT_BUS.unregister(deny);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void crucibleAndFilterCopiesConsumeValidateAndPersistExactly(GameTestHelper helper) {
        BlockPos position = new BlockPos(2, 2, 2);
        helper.setBlock(position, EnvironmentMachineRegistration.QUARTZ_CRUCIBLE.get());
        QuartzCrucibleBlockEntity crucible = (QuartzCrucibleBlockEntity)helper.getBlockEntity(position);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new ItemStack(MaterialRegistration.MATERIAL_ITEMS.get("impure_silicon_ingot").get(), 2));
        helper.useBlock(position, player);
        require(crucible.impureSilicon() == 2 && player.getMainHandItem().isEmpty(),
                "Right-click did not consume the offered silicon exactly once");
        ItemEntity dropped = new ItemEntity(helper.getLevel(), helper.absolutePos(position).getX() + 0.5D,
                helper.absolutePos(position).getY() + 0.75D, helper.absolutePos(position).getZ() + 0.5D,
                new ItemStack(MaterialRegistration.MATERIAL_ITEMS.get("impure_silicon_ingot").get(), 10));
        helper.getLevel().addFreshEntity(dropped);
        crucible.absorbDroppedItem(dropped);
        require(crucible.impureSilicon() == 3 && dropped.getItem().getCount() == 9,
                "One collision must insert one ingot");
        for (int collision = 0; collision < 6; collision++) {
            crucible.absorbDroppedItem(dropped);
        }
        require(crucible.impureSilicon() == 8 && dropped.isAlive() && dropped.getItem().getCount() == 4,
                "Dropped silicon was not partially consumed exactly once");
        CompoundTag heated = crucible.saveWithFullMetadata(helper.getLevel().registryAccess());
        heated.putInt("heating_ticks", 1);
        crucible.loadWithComponents(heated, helper.getLevel().registryAccess());
        crucible.tickServer();
        ItemEntity string = new ItemEntity(helper.getLevel(), helper.absolutePos(position).getX() + 0.5D,
                helper.absolutePos(position).getY() + 0.75D, helper.absolutePos(position).getZ() + 0.5D,
                new ItemStack(Items.STRING, 2));
        helper.getLevel().addFreshEntity(string);
        crucible.absorbDroppedItem(string);
        crucible.absorbDroppedItem(string);
        require(crucible.impureSilicon() == 0 && string.getItem().getCount() == 1,
                "A heated crucible consumed string more than once");

        ItemStack source = LogisticsRegistration.ITEM_FILTER.toStack();
        ItemFilterSettings settings = ItemFilterSettings.EMPTY.withItem(0, new ItemStack(Items.DIAMOND));
        source.set(LogisticsRegistration.ITEM_FILTER_SETTINGS.get(), settings);
        ItemStack duplicator = LogisticsRegistration.FILTER_DUPLICATOR.toStack();
        ItemStack destination = LogisticsRegistration.ITEM_FILTER.toStack();
        require(FilterCopyValidator.copy(source, duplicator)
                        && FilterCopyValidator.copy(duplicator, destination),
                "A valid compound filter could not be copied");
        destination.set(LogisticsRegistration.ITEM_FILTER_SETTINGS.get(),
                destination.get(LogisticsRegistration.ITEM_FILTER_SETTINGS.get())
                        .withMode(ItemFilterSettings.Mode.BLACKLIST));
        require(source.get(LogisticsRegistration.ITEM_FILTER_SETTINGS.get()).mode()
                        == ItemFilterSettings.Mode.WHITELIST,
                "Editing the copy changed its source filter");
        ItemStack operatedDuplicator = LogisticsRegistration.FILTER_DUPLICATOR.toStack();
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, operatedDuplicator);
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, source);
        FilterDuplicatorItem duplicatorItem = (FilterDuplicatorItem)operatedDuplicator.getItem();
        duplicatorItem.use(helper.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
        require(operatedDuplicator.getOrDefault(LogisticsRegistration.FILTER_COPY.get(), false),
                "Using the duplicator did not store copy state");
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, destination);
        duplicatorItem.use(helper.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
        require(destination.get(LogisticsRegistration.ITEM_FILTER_SETTINGS.get()).mode()
                        == ItemFilterSettings.Mode.WHITELIST
                        && !destination.getOrDefault(LogisticsRegistration.FILTER_COPY.get(), false),
                "Using the duplicator did not restore settings without marking the filter as a copy");
        player.setShiftKeyDown(true);
        duplicatorItem.use(helper.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
        require(!operatedDuplicator.getOrDefault(LogisticsRegistration.FILTER_COPY.get(), false),
                "Sneaking did not clear the duplicator copy state");

        // IItemWithFilterSize: 100 remembered entries in total, nested entries included.
        ItemStack nested = LogisticsRegistration.ITEM_FILTER.toStack();
        nested.set(LogisticsRegistration.ITEM_FILTER_SETTINGS.get(),
                ItemFilterSettings.EMPTY.withItem(0, new ItemStack(Items.DIRT)));
        for (int depth = 0; depth < 20; depth++) {
            ItemStack outer = LogisticsRegistration.ITEM_FILTER.toStack();
            ItemFilterSettings outerSettings = ItemFilterSettings.EMPTY;
            for (int slot = 0; slot < ItemFilterSettings.SIZE; slot++) {
                outerSettings = outerSettings.withItem(slot, nested);
            }
            outer.set(LogisticsRegistration.ITEM_FILTER_SETTINGS.get(), outerSettings);
            nested = outer;
        }
        require(!FilterCopyValidator.copy(nested, LogisticsRegistration.FILTER_DUPLICATOR.toStack()),
                "The duplicator accepted more than the original filter-size limit");
        require(!net.claustra01.clayium.logistics.filter.ItemFilter.accepts(nested, new ItemStack(Items.DIRT)),
                "An oversized nested filter was evaluated instead of being rejected");
        ItemStack editable = LogisticsRegistration.ITEM_FILTER.toStack();
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, editable);
        var filterMenu = new net.claustra01.clayium.logistics.filter.FilterMenu(0, player.getInventory());
        filterMenu.setCarried(nested);
        filterMenu.clicked(0, 0, net.minecraft.world.inventory.ClickType.PICKUP, player);
        require(editable.getOrDefault(LogisticsRegistration.ITEM_FILTER_SETTINGS.get(), ItemFilterSettings.EMPTY)
                        .item(0).isEmpty(),
                "The filter GUI accepted more than the original filter-size limit");

        CompoundTag encoded = (CompoundTag)duplicator.save(helper.getLevel().registryAccess());
        ItemStack loaded = ItemStack.parse(helper.getLevel().registryAccess(), encoded).orElseThrow();
        require(loaded.getOrDefault(LogisticsRegistration.FILTER_COPY.get(), false)
                        && loaded.get(LogisticsRegistration.ITEM_FILTER_SETTINGS.get()).entryCount() == 1,
                "Filter copy state did not survive item-stack NBT");
        helper.succeed();
    }

    private static void verifyTool(ItemStack stack, int durability, int clayBalls) {
        require(stack.getMaxDamage() == durability && stack.getMaxStackSize() == 1,
                "Crafting tool durability or stack size differs from hotfix2");
        ClayCraftingToolItem item = (ClayCraftingToolItem)stack.getItem();
        for (int use = 1; use <= durability; use++) {
            stack = item.getCraftingRemainingItem(stack);
            if (use < durability) {
                require(stack.is(item) && stack.getDamageValue() == use,
                        "Crafting did not consume exactly one durability");
            } else {
                require(stack.is(Items.CLAY_BALL) && stack.getCount() == clayBalls,
                        "Broken crafting tool returned the wrong raw clay amount");
            }
        }
    }

    private static void verifyTableTool(GameTestHelper helper, BlockPos position, ItemStack tool,
            int action, ItemStack input, int presses, int durability, int clayBalls) {
        helper.setBlock(position, WorkbenchRegistration.CLAY_WORK_TABLE.get());
        ClayWorkTableBlockEntity table = helper.getBlockEntity(position);
        table.inventory().setStackInSlot(ClayWorkTableBlockEntity.TOOL_SLOT, tool);
        for (int craft = 1; craft <= durability; craft++) {
            table.inventory().setStackInSlot(ClayWorkTableBlockEntity.INPUT_SLOT, input.copy());
            for (int press = 0; press < presses; press++) {
                require(table.press(action), "The work-table operation was rejected");
            }
            if (craft < durability) {
                require(table.inventory().getStackInSlot(ClayWorkTableBlockEntity.TOOL_SLOT)
                        .getDamageValue() == craft, "Work-table tool damage skipped or doubled");
            } else {
                require(table.inventory().getStackInSlot(ClayWorkTableBlockEntity.TOOL_SLOT).isEmpty(),
                        "Broken work-table tool remained in its slot");
                require(table.inventory().getStackInSlot(ClayWorkTableBlockEntity.SECOND_OUTPUT_SLOT)
                        .is(Items.CLAY_BALL)
                        && table.inventory().getStackInSlot(ClayWorkTableBlockEntity.SECOND_OUTPUT_SLOT)
                                .getCount() == clayBalls, "Broken tool returned the wrong clay count");
            }
            table.inventory().setStackInSlot(ClayWorkTableBlockEntity.FIRST_OUTPUT_SLOT, ItemStack.EMPTY);
            table.inventory().setStackInSlot(ClayWorkTableBlockEntity.SECOND_OUTPUT_SLOT, ItemStack.EMPTY);
        }
    }

    private static ItemStack filterFor(ItemStack example) {
        ItemStack filter = LogisticsRegistration.ITEM_FILTER.toStack();
        filter.set(LogisticsRegistration.ITEM_FILTER_SETTINGS.get(), ItemFilterSettings.EMPTY.withItem(0, example));
        return filter;
    }

    private static AreaMachineBlockEntity placeMachine(
            GameTestHelper helper, BlockPos relativePosition, AreaMachineType type) {
        helper.setBlock(relativePosition, AreaMachineRegistration.MACHINES.get(type).block().get());
        AreaMachineBlockEntity machine = (AreaMachineBlockEntity)helper.getBlockEntity(relativePosition);
        machine.setOwner(OWNER);
        return machine;
    }

    private static int outputCount(AreaMachineBlockEntity machine, net.minecraft.world.item.Item item) {
        int count = 0;
        for (int slot = 0; slot < machine.outputSlots(); slot++) {
            ItemStack stack = machine.inventory().getStackInSlot(slot);
            if (stack.is(item)) count += stack.getCount();
        }
        return count;
    }

    private static LaserBeam findBeamInRange(long minimum, long maximum) {
        for (long blue = 1; blue < 1_000; blue++) {
            LaserBeam beam = new LaserBeam(blue, 0L, 0L);
            long energy = (long)beam.energy();
            if (energy >= minimum && energy <= maximum) return beam;
        }
        throw new GameTestAssertException("No laser beam matched the sapling stage threshold");
    }

    private static void useDirectionCard(
            net.minecraft.world.entity.player.Player player, BlockPos position, Direction side, String action) {
        var hit = new net.minecraft.world.phys.BlockHitResult(
                net.minecraft.world.phys.Vec3.atCenterOf(position), side, position, false);
        var context = new net.minecraft.world.item.context.UseOnContext(
                player, net.minecraft.world.InteractionHand.MAIN_HAND, hit);
        require(player.getMainHandItem().onItemUseFirst(context).consumesAction(),
                "The direction memory card did not consume its " + action + " interaction");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new GameTestAssertException(message);
    }

    private OriginalBehaviorGameTests() {
    }
}
