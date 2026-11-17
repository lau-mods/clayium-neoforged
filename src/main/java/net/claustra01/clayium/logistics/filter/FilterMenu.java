package net.claustra01.clayium.logistics.filter;

import net.claustra01.clayium.registry.LogisticsRegistration;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public final class FilterMenu extends AbstractContainerMenu {
    private final Container example = new SimpleContainer(1);
    private final Inventory inventory;
    private ItemFilterRule.MatchType type = ItemFilterRule.MatchType.FUZZY;
    private int tagIndex;
    private final ContainerData data;

    public FilterMenu(int id, Inventory inventory) {
        super(LogisticsRegistration.FILTER_MENU.get(), id);
        this.inventory = inventory;
        this.data = inventory.player.level().isClientSide ? new SimpleContainerData(3) : new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> type.ordinal();
                    case 1 -> settings().mode().ordinal();
                    case 2 -> settings().rules().size();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
        addDataSlots(data);
        addSlot(new Slot(example, 0, 8, 77));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 118 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 176));
        }
    }

    public ItemStack filterStack() {
        return inventory.player.getMainHandItem();
    }

    public ItemFilterSettings settings() {
        return filterStack().getOrDefault(LogisticsRegistration.ITEM_FILTER_SETTINGS.get(), ItemFilterSettings.ALLOW_ALL);
    }

    public ItemFilterRule.MatchType typeSelected() {
        int ordinal = data.get(0);
        return ordinal >= 0 && ordinal < ItemFilterRule.MatchType.values().length
                ? ItemFilterRule.MatchType.values()[ordinal] : ItemFilterRule.MatchType.FUZZY;
    }

    public ItemFilterSettings.Mode modeSelected() {
        return data.get(1) == ItemFilterSettings.Mode.WHITELIST.ordinal()
                ? ItemFilterSettings.Mode.WHITELIST : ItemFilterSettings.Mode.BLACKLIST;
    }

    public int ruleCount() {
        return data.get(2);
    }

    public void apply(int action) {
        if (action == 0) {
            filterStack().set(LogisticsRegistration.ITEM_FILTER_SETTINGS.get(), settings().withMode(settings().mode().next()));
        } else if (action == 1) {
            type = type.next();
        } else if (action == 2) {
            tagIndex++;
        } else if (action == 3 && !example.getItem(0).isEmpty()) {
            ItemFilterRule rule = ItemFilterRule.fromExample(type, example.getItem(0));
            if (type == ItemFilterRule.MatchType.TAG) {
                var tags = example.getItem(0).getTags().map(tag -> tag.location().toString()).sorted().toList();
                if (tags.isEmpty()) {
                    return;
                }
                rule = new ItemFilterRule(type, tags.get(Math.floorMod(tagIndex, tags.size())), ItemStack.EMPTY);
            }
            if (type == ItemFilterRule.MatchType.NESTED && rule.reference().isEmpty()) {
                return;
            }
            filterStack().set(LogisticsRegistration.ITEM_FILTER_SETTINGS.get(), settings().withRule(rule));
        } else if (action == 4) {
            filterStack().set(LogisticsRegistration.ITEM_FILTER_SETTINGS.get(), settings().withoutLastRule());
        }
        broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        ItemStack stack = example.removeItemNoUpdate(0);
        if (!stack.isEmpty()) {
            player.getInventory().placeItemBackInInventory(stack);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return filterStack().is(LogisticsRegistration.ITEM_FILTER.get());
    }
}
