package net.claustra01.clayium.gametest.storage;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.StorageRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.storage.StorageContainerBlock;
import net.claustra01.clayium.storage.StorageContainerMenu;
import net.claustra01.clayium.storage.VoidContainerMenu;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.claustra01.clayium.storage.MetalChestBlockEntity;
import net.claustra01.clayium.storage.MetalChestMenu;
import net.claustra01.clayium.storage.MetalChestType;
import net.claustra01.clayium.storage.StorageContainerBlockEntity;
import net.claustra01.clayium.storage.VoidContainerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class StorageGameTests {
    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void storageCapacityExpansionOverflowPersistenceAndVoidAreExact(GameTestHelper helper) {
        BlockPos storagePos = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlockAndUpdate(storagePos, StorageRegistration.STORAGE_CONTAINER.get().defaultBlockState());
        StorageContainerBlockEntity storage = (StorageContainerBlockEntity)helper.getLevel().getBlockEntity(storagePos);
        require(storage != null, "Storage container block entity was not created");

        for (int insertion = 0; insertion < 1_024; insertion++) {
            ItemStack remainder = storage.itemHandler().insertItem(0, new ItemStack(Items.CLAY_BALL, 64), false);
            require(remainder.isEmpty(), "The basic container rejected an item below 65,536");
        }
        require(storage.amount() == StorageContainerBlockEntity.BASIC_CAPACITY,
                "The basic container did not hold exactly 65,536 items");
        ItemStack basicOverflow = storage.itemHandler().insertItem(0, new ItemStack(Items.CLAY_BALL, 64), false);
        require(basicOverflow.getCount() == 64 && storage.amount() == 65_536L,
                "The basic capacity boundary lost items or overflowed");

        CompoundTag basicSaved = storage.saveWithFullMetadata(helper.getLevel().registryAccess());
        StorageContainerBlockEntity basicReloaded = new StorageContainerBlockEntity(storagePos, storage.getBlockState());
        basicReloaded.loadWithComponents(basicSaved, helper.getLevel().registryAccess());
        require(basicReloaded.amount() == 65_536L && !basicReloaded.expanded(),
                "Basic contents changed across save and load");
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack clayCore = new ItemStack(MaterialRegistration.COMPONENTS.get("clay_core").get());
        player.setItemInHand(InteractionHand.MAIN_HAND, clayCore);
        storage.getBlockState().useItemOn(clayCore, helper.getLevel(), player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(storagePos), Direction.UP, storagePos, false));
        require(clayCore.isEmpty() && helper.getLevel().getBlockState(storagePos).getValue(StorageContainerBlock.EXPANDED),
                "Clay Core was not consumed or expanded state was not set");
        require(storage.expanded()
                        && storage.capacity() == StorageContainerBlockEntity.EXPANDED_CAPACITY,
                "Clay Core did not expand the storage container");
        StorageContainerMenu storageMenu = new StorageContainerMenu(1, player.getInventory(), storage,
                new net.minecraft.world.inventory.SimpleContainerData(9));
        ItemStack menuOffer = new ItemStack(Items.CLAY_BALL, 64);
        storageMenu.slots.get(0).safeInsert(menuOffer);
        require(menuOffer.isEmpty() && storage.amount() == 65_600L,
                "Storage GUI insertion did not reach the long count");
        storageMenu.setCarried(new ItemStack(Items.CLAY_BALL, 8));
        storageMenu.clicked(0, 0, ClickType.PICKUP, player);
        require(storageMenu.getCarried().isEmpty() && storage.amount() == 65_608L,
                "Clicking a partly filled storage slot lost items");
        player.getInventory().setItem(9, new ItemStack(Items.CLAY_BALL, 8));
        storageMenu.quickMoveStack(player, 1);
        require(storage.amount() == 65_616L && player.getInventory().getItem(9).isEmpty(),
                "Shift insertion did not update the stored amount");
        ItemStack menuTaken = storageMenu.slots.get(0).safeTake(16, 16, player);
        require(menuTaken.getCount() == 16 && storage.amount() == 65_600L,
                "Storage GUI extraction did not lower the long count");
        storage.itemHandler().insertItem(0, menuTaken, false);
        storageMenu.quickMoveStack(player, 0);
        require(storage.amount() == 65_552L && player.getInventory().countItem(Items.CLAY_BALL) == 64,
                "Shift extraction duplicated or lost a normal stack");
        storageMenu.quickMoveStack(player, storageMenu.slots.size() - 1);
        require(storage.amount() == 65_616L && player.getInventory().countItem(Items.CLAY_BALL) == 0,
                "Shift reinsertion did not conserve the normal stack");
        CompoundTag nearlyFull = storage.saveWithFullMetadata(helper.getLevel().registryAccess());
        nearlyFull.putLong("amount", Integer.MAX_VALUE - 32L);
        StorageContainerBlockEntity loaded = new StorageContainerBlockEntity(storagePos, storage.getBlockState());
        loaded.loadWithComponents(nearlyFull, helper.getLevel().registryAccess());
        ItemStack finalRemainder = loaded.itemHandler().insertItem(0, new ItemStack(Items.CLAY_BALL, 64), false);
        require(finalRemainder.getCount() == 32 && loaded.amount() == Integer.MAX_VALUE,
                "Expanded insertion did not stop exactly at 2,147,483,647");
        ItemStack overflow = loaded.itemHandler().insertItem(0, new ItemStack(Items.CLAY_BALL, 64), false);
        require(overflow.getCount() == 64 && loaded.amount() == Integer.MAX_VALUE,
                "Expanded storage wrapped its integer boundary");
        ItemStack extracted = loaded.itemHandler().extractItem(0, 64, false);
        require(extracted.getCount() == 64 && loaded.amount() == Integer.MAX_VALUE - 64L,
                "The external item handler did not split a normal stack from long storage");
        loaded.itemHandler().insertItem(0, extracted, false);
        CompoundTag saved = loaded.saveWithFullMetadata(helper.getLevel().registryAccess());
        StorageContainerBlockEntity restarted = new StorageContainerBlockEntity(storagePos, storage.getBlockState());
        restarted.loadWithComponents(saved, helper.getLevel().registryAccess());
        require(restarted.amount() == Integer.MAX_VALUE && restarted.expanded()
                        && restarted.storedItem().is(Items.CLAY_BALL),
                "Expanded contents changed across a save and restart");

        BlockPos voidPos = helper.absolutePos(new BlockPos(2, 1, 1));
        helper.getLevel().setBlockAndUpdate(voidPos, StorageRegistration.VOID_CONTAINER.get().defaultBlockState());
        VoidContainerBlockEntity voidContainer = (VoidContainerBlockEntity)helper.getLevel().getBlockEntity(voidPos);
        require(voidContainer != null, "Void container block entity was not created");
        ItemStack offered = new ItemStack(Items.DIAMOND, 23);
        require(voidContainer.itemHandler().insertItem(0, offered, true).isEmpty()
                        && offered.getCount() == 23,
                "Simulating a void insertion mutated the caller's stack");
        require(voidContainer.itemHandler().insertItem(0, offered, false).isEmpty()
                        && voidContainer.itemHandler().getStackInSlot(0).isEmpty(),
                "The void container did not discard an explicitly inserted stack");
        VoidContainerMenu voidMenu = new VoidContainerMenu(2, player.getInventory(), voidContainer);
        ItemStack voidOffer = new ItemStack(Items.DIAMOND, 17);
        voidMenu.slots.get(0).safeInsert(voidOffer);
        require(voidOffer.isEmpty() && voidMenu.slots.get(0).getItem().isEmpty(),
                "Void GUI did not discard the explicitly inserted items");
        voidMenu.setCarried(new ItemStack(Items.EMERALD, 8));
        voidMenu.clicked(0, 0, ClickType.PICKUP, player);
        require(voidMenu.getCarried().isEmpty(), "Clicking the void slot did not discard the offered stack");
        require(voidContainer.itemHandler().extractItem(0, 64, false).isEmpty(),
                "The void container produced an item that was never inserted");
        helper.succeed();
    }

    private static final String GR08 = "silicon:9:5:1 aluminium:9:6:1 clay_steel:9:8:1 clayium:13:8:1 ultimate_alloy:13:8:3 magnesium:9:6:1 sodium:9:6:1 lithium:9:7:1 zirconium:9:7:1 zinc:9:6:1 manganese:9:6:1 calcium:9:6:1 potassium:9:6:1 nickel:9:6:1 beryllium:10:8:1 lead:9:6:1 hafnium:9:8:1 chrome:13:8:1 titanium:13:8:1 strontium:10:8:1 barium:10:8:1 az91_d_alloy:13:8:1 zk60_a_alloy:13:8:1 rubidium:13:3:1 caesium:13:3:1 francium:13:4:1 radium:13:4:1 actinium:13:5:1 thorium:13:5:1 protactinium:13:6:1 uranium:13:6:2 neptunium:13:6:3 plutonium:13:6:4 americium:13:6:5 curium:13:6:6 lanthanum:13:2:2 cerium:13:2:4 praseodymium:13:2:6 neodymium:13:2:8 promethium:13:4:8 samarium:13:6:8 europium:13:8:8 vanadium:4:8:1 cobalt:11:6:1 palladium:11:8:1 platinum:13:8:2 iridium:13:8:3 osmium:13:8:4 rhenium:13:8:5 tantalum:10:8:1 tungsten:13:8:1 molybdenum:13:8:2 antimony:9:6:1 bismuth:9:6:1 brass:9:6:1 electrum:13:8:1 invar:9:8:1";

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void storageContentsSurviveBreakingAndPlacement(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos, StorageRegistration.STORAGE_CONTAINER.get().defaultBlockState());
        StorageContainerBlockEntity storage = (StorageContainerBlockEntity)helper.getLevel().getBlockEntity(pos);
        storage.itemHandler().insertItem(0, new ItemStack(Items.DIAMOND, 37), false);
        storage.applyClayCore();
        ItemStack drop = Block.getDrops(storage.getBlockState(), helper.getLevel(), pos, storage).getFirst();
        helper.getLevel().removeBlock(pos, false);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, drop);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos.below()), Direction.UP, pos.below(), false);
        drop.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        StorageContainerBlockEntity replaced = (StorageContainerBlockEntity)helper.getLevel().getBlockEntity(pos);
        require(replaced != null && replaced.amount() == 37 && replaced.expanded()
                        && replaced.storedItem().is(Items.DIAMOND)
                        && helper.getLevel().getBlockState(pos).getValue(StorageContainerBlock.EXPANDED),
                "Mining and replacing the container lost its contents or Clay Core expansion");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void allFiftySevenMetalChestsMatchTheCapacityLedger(GameTestHelper helper) {
        require(MetalChestType.ALL.size() == 57 && StorageRegistration.METAL_CHESTS.size() == 57,
                "The metal chest registry does not contain all 57 materials");
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        var ledger = new java.util.HashMap<String, int[]>();
        for (String entry : GR08.split(" ")) {
            String[] columns = entry.split(":");
            ledger.put(columns[0] + "_chest", new int[]{Integer.parseInt(columns[1]),
                    Integer.parseInt(columns[2]), Integer.parseInt(columns[3])});
        }
        require(ledger.size() == 57, "GR-08 fixture does not have 57 entries");
        for (MetalChestType type : MetalChestType.ALL) {
            int[] dimensions = ledger.get(type.id());
            require(dimensions != null && type.columns() == dimensions[0]
                            && type.rows() == dimensions[1] && type.pages() == dimensions[2],
                    type.id() + " differs from the independent GR-08 ledger");
            var definition = StorageRegistration.METAL_CHESTS.get(type.id());
            require(definition != null, "Missing registered metal chest " + type.id());
            MetalChestBlockEntity chest = new MetalChestBlockEntity(
                    helper.absolutePos(new BlockPos(1, 1, 1)), definition.block().get().defaultBlockState());
            require(chest.inventory().getSlots() == type.columns() * type.rows() * type.pages(),
                    type.id() + " does not expose its complete capacity through IItemHandler");
            MetalChestMenu menu = new MetalChestMenu(1, player.getInventory(), chest);
            for (int page = 0; page < type.pages(); page++) {
                require(menu.clickMenuButton(player, page), type.id() + " rejected page " + page);
                for (int slot = 0; slot < type.slots(); slot++) {
                    require(menu.slots.get(slot).isActive() == (slot / type.pageSize() == page),
                            type.id() + " page " + page + " exposed the wrong slot " + slot);
                }
                int first = page * type.pageSize();
                int last = first + type.pageSize() - 1;
                ItemStack firstOffer = new ItemStack(Items.IRON_INGOT, page + 1);
                ItemStack lastOffer = new ItemStack(Items.GOLD_INGOT, page + 1);
                menu.slots.get(first).safeInsert(firstOffer);
                menu.slots.get(last).safeInsert(lastOffer);
                require(firstOffer.isEmpty() && lastOffer.isEmpty(), type.id() + " page insertion failed");
                ItemStack taken = menu.slots.get(last).safeTake(1, 1, player);
                require(taken.getCount() == 1 && chest.inventory().getStackInSlot(last).getCount() == page,
                        type.id() + " page extraction failed");
                menu.slots.get(last).safeInsert(taken);
                player.getInventory().setItem(9, new ItemStack(Items.CLAY_BALL));
                require(!menu.quickMoveStack(player, type.slots()).isEmpty()
                                && chest.inventory().getStackInSlot(first + 1).is(Items.CLAY_BALL),
                        type.id() + " shift insertion ignored the active page");
            }
            require(!menu.clickMenuButton(player, type.pages()), type.id() + " accepted an invalid page");
            CompoundTag saved = chest.saveWithFullMetadata(helper.getLevel().registryAccess());
            MetalChestBlockEntity loaded = new MetalChestBlockEntity(chest.getBlockPos(), chest.getBlockState());
            loaded.loadWithComponents(saved, helper.getLevel().registryAccess());
            for (int page = 0; page < type.pages(); page++) {
                require(loaded.inventory().getStackInSlot(page * type.pageSize()).getCount() == page + 1
                                && loaded.inventory().getStackInSlot((page + 1) * type.pageSize() - 1).getCount() == page + 1,
                        type.id() + " did not preserve all page boundaries");
            }
        }
        MetalChestType europium = MetalChestType.ALL.stream()
                .filter(type -> type.materialId().equals("europium")).findFirst().orElseThrow();
        require(europium.slots() == 832 && europium.columns() == 13 && europium.rows() == 8 && europium.pages() == 8,
                "The Europium chest is not 13x8x8 = 832 slots");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void metalChestMenusKeepIndependentServerAuthoritativePages(GameTestHelper helper) {
        MetalChestType europium = MetalChestType.ALL.stream()
                .filter(type -> type.materialId().equals("europium")).findFirst().orElseThrow();
        var definition = StorageRegistration.METAL_CHESTS.get(europium.id());
        MetalChestBlockEntity chest = new MetalChestBlockEntity(
                helper.absolutePos(new BlockPos(1, 1, 1)), definition.block().get().defaultBlockState());
        var firstPlayer = helper.makeMockPlayer(GameType.CREATIVE);
        var secondPlayer = helper.makeMockPlayer(GameType.CREATIVE);
        MetalChestMenu first = new MetalChestMenu(1, firstPlayer.getInventory(), chest);
        MetalChestMenu second = new MetalChestMenu(2, secondPlayer.getInventory(), chest);
        require(first.clickMenuButton(firstPlayer, 1) && second.clickMenuButton(secondPlayer, 7),
                "A valid server page selection was rejected");
        require(first.page() == 1 && second.page() == 7,
                "One client changed another client's selected page");
        ItemStack diamond = new ItemStack(Items.DIAMOND, 17);
        ItemStack emerald = new ItemStack(Items.EMERALD, 31);
        first.slots.get(europium.pageSize()).safeInsert(diamond);
        second.slots.get(europium.slots() - 1).safeInsert(emerald);
        require(diamond.isEmpty() && emerald.isEmpty(), "Concurrent menu insertion failed");
        second.clickMenuButton(secondPlayer, 1);
        require(second.slots.get(europium.pageSize()).getItem().getCount() == 17,
                "Second menu did not see first menu's item");
        first.clickMenuButton(firstPlayer, 7);
        require(first.slots.get(europium.slots() - 1).getItem().getCount() == 31,
                "First menu did not see second menu's item");
        require(chest.inventory().getStackInSlot(europium.pageSize()).getCount() == 17
                        && chest.inventory().getStackInSlot(europium.slots() - 1).getCount() == 31,
                "Concurrent page operations did not share one authoritative inventory");
        helper.succeed();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new GameTestAssertException(message);
    }

    private StorageGameTests() {
    }
}
