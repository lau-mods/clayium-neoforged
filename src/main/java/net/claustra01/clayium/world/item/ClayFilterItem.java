/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.world.item;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.data.FilterSettings;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.inventory.ItemFilterMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

/** One of the distinct filter Items from Clayium 1.7.10. */
public final class ClayFilterItem extends Item {
    public enum Kind {
        DUPLICATOR(false, false),
        WHITELIST(true, false),
        BLACKLIST(true, false),
        FUZZY(true, false),
        ITEM_TAG(false, true),
        ITEM_NAME(false, true),
        TRANSLATION_KEY(false, true),
        UNIQUE_ID(false, true),
        MOD_ID(false, true),
        ITEM_DAMAGE(false, true),
        BLOCK_STATE(false, true),
        BLOCK_HARVESTABLE(false, false);

        private final boolean listEditor;
        private final boolean stringEditor;

        Kind(boolean listEditor, boolean stringEditor) {
            this.listEditor = listEditor;
            this.stringEditor = stringEditor;
        }
    }

    private final Kind kind;

    public ClayFilterItem(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public Component getName(ItemStack stack) {
        Component name = super.getName(stack);
        return isCopy(stack)
                ? Component.translatable("item.clayium_neoforged.filter_copy_name", name)
                : name;
    }

    public boolean isCopy(ItemStack stack) {
        return kind == Kind.DUPLICATOR
                || stack.getOrDefault(ClayiumDataComponents.FILTER_SETTINGS.get(), FilterSettings.DEFAULT).copy();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockEntity(context.getClickedPos())
                instanceof net.claustra01.clayium.logistics.ConfigurableItemDevice device)) {
            return InteractionResult.PASS;
        }
        if (!context.getLevel().isClientSide) {
            ItemStack held = context.getItemInHand();
            Player player = context.getPlayer();
            if (isCopy(held)) {
                ItemStack installed = device.filter(context.getClickedFace());
                if (!installed.isEmpty() && player != null) {
                    player.setItemInHand(context.getHand(), asCopied(installed));
                    player.displayClientMessage(
                            Component.translatable("message.clayium_neoforged.filter_copied", installed.getHoverName()),
                            true);
                }
            } else {
                device.setFilter(context.getClickedFace(), held);
                if (player != null) {
                    player.displayClientMessage(
                            Component.translatable("message.clayium_neoforged.filter_applied", held.getHoverName()),
                            true);
                }
            }
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        FilterSettings settings =
                stack.getOrDefault(ClayiumDataComponents.FILTER_SETTINGS.get(), FilterSettings.DEFAULT);
        if (player.isShiftKeyDown() && settings.copy() && kind != Kind.DUPLICATOR) {
            if (!level.isClientSide) {
                stack.set(ClayiumDataComponents.FILTER_SETTINGS.get(), settings.withCopy(false));
                stack.remove(DataComponents.CUSTOM_MODEL_DATA);
                player.displayClientMessage(Component.translatable("message.clayium_neoforged.filter_copy_cleared"), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        if ((kind.listEditor || kind.stringEditor) && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(
                    new SimpleMenuProvider(
                            (id, inventory, ignored) -> new ItemFilterMenu(id, inventory),
                            stack.getHoverName()));
        }
        return kind.listEditor || kind.stringEditor
                ? InteractionResultHolder.sidedSuccess(stack, level.isClientSide)
                : InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        FilterSettings settings =
                stack.getOrDefault(ClayiumDataComponents.FILTER_SETTINGS.get(), FilterSettings.DEFAULT);
        if (!settings.pattern().isEmpty()) {
            tooltip.add(Component.literal(settings.pattern()).withStyle(ChatFormatting.GRAY));
        }
        for (ItemStack entry : settings.entries()) {
            if (!entry.isEmpty()) {
                tooltip.add(Component.literal(" " + entry.getHoverName().getString()).withStyle(ChatFormatting.GRAY));
            }
        }
        if (isCopy(stack)) {
            tooltip.add(Component.translatable("tooltip.clayium_neoforged.filter_copy").withStyle(ChatFormatting.AQUA));
        }
    }

    public static boolean isFilter(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ClayFilterItem;
    }

    public static int nestedSize(ItemStack stack, Set<ItemStack> visited) {
        if (!isFilter(stack) || visited.size() >= FilterSettings.MAX_NESTED_FILTER_SIZE) {
            return 0;
        }
        visited.add(stack);
        FilterSettings data =
                stack.getOrDefault(ClayiumDataComponents.FILTER_SETTINGS.get(), FilterSettings.DEFAULT);
        int size = 1;
        for (ItemStack entry : data.entries()) {
            size += isFilter(entry) ? nestedSize(entry, visited) : entry.isEmpty() ? 0 : 1;
            if (size >= FilterSettings.MAX_NESTED_FILTER_SIZE) {
                return size;
            }
        }
        return size;
    }

    public static boolean matches(ItemStack filter, ItemStack candidate) {
        return matches(filter, candidate, new HashSet<>(), 0);
    }

    private static boolean matches(ItemStack filter, ItemStack candidate, Set<ItemStack> visited, int depth) {
        if (!(filter.getItem() instanceof ClayFilterItem item)
                || depth >= FilterSettings.MAX_NESTED_FILTER_SIZE
                || !visited.add(filter)) {
            return false;
        }
        FilterSettings data =
                filter.getOrDefault(ClayiumDataComponents.FILTER_SETTINGS.get(), FilterSettings.DEFAULT);
        return switch (item.kind) {
            case DUPLICATOR, BLOCK_HARVESTABLE -> false;
            case WHITELIST -> listMatches(data.entries(), candidate, false, visited, depth);
            case BLACKLIST -> !listMatches(data.entries(), candidate, false, visited, depth);
            case FUZZY -> listMatches(data.entries(), candidate, true, visited, depth);
            case ITEM_TAG -> regex(data.pattern(), tagStrings(candidate));
            case ITEM_NAME -> regex(data.pattern(), List.of(candidate.getHoverName().getString()));
            case TRANSLATION_KEY -> regex(data.pattern(), List.of(candidate.getDescriptionId()));
            case UNIQUE_ID -> regex(
                    data.pattern(), List.of(BuiltInRegistries.ITEM.getKey(candidate.getItem()).toString()));
            case MOD_ID -> regex(
                    data.pattern(), List.of(BuiltInRegistries.ITEM.getKey(candidate.getItem()).getNamespace()));
            case ITEM_DAMAGE -> exactRegex(data.pattern(), Integer.toString(candidate.getDamageValue()));
            case BLOCK_STATE -> false;
        };
    }

    private static boolean listMatches(
            List<ItemStack> entries,
            ItemStack candidate,
            boolean fuzzy,
            Set<ItemStack> visited,
            int depth) {
        for (ItemStack entry : entries) {
            if (entry.isEmpty()) {
                continue;
            }
            if (isFilter(entry) && matches(entry, candidate, visited, depth + 1)) {
                return true;
            }
            if (fuzzy ? fuzzyEqual(entry, candidate) : ItemStack.isSameItemSameComponents(entry, candidate)) {
                return true;
            }
        }
        return false;
    }

    private static boolean fuzzyEqual(ItemStack expected, ItemStack actual) {
        if (expected.getItem() == actual.getItem()
                && (expected.isDamageableItem() || expected.getDamageValue() == actual.getDamageValue())) {
            return true;
        }
        Set<TagKey<Item>> tags = new HashSet<>();
        expected.getTags().forEach(tags::add);
        return actual.getTags().anyMatch(tags::contains);
    }

    private static List<String> tagStrings(ItemStack stack) {
        return stack.getTags().map(tag -> tag.location().toString()).toList();
    }

    private static boolean regex(String expression, List<String> values) {
        try {
            Pattern pattern = Pattern.compile(expression);
            return values.stream().anyMatch(value -> pattern.matcher(value).find());
        } catch (PatternSyntaxException exception) {
            Clayium.LOGGER.error("Illegal Clayium filter pattern: {}", expression, exception);
            return false;
        }
    }

    private static boolean exactRegex(String expression, String value) {
        return regex("^(?:" + expression + ")$", List.of(value));
    }

    public static boolean matchesBlock(ItemStack filter, Level level, BlockPos pos) {
        if (!(filter.getItem() instanceof ClayFilterItem item)) {
            return false;
        }
        if (item.kind == Kind.BLOCK_HARVESTABLE) {
            BlockState state = level.getBlockState(pos);
            return state.getBlock() instanceof BonemealableBlock growable
                    && !growable.isValidBonemealTarget(level, pos, state);
        }
        if (item.kind == Kind.BLOCK_STATE) {
            String pattern = filter.getOrDefault(
                    ClayiumDataComponents.FILTER_SETTINGS.get(), FilterSettings.DEFAULT).pattern();
            String properties = level.getBlockState(pos).getValues().entrySet().stream()
                    .sorted(java.util.Comparator.comparing(entry -> entry.getKey().getName()))
                    .map(entry -> entry.getKey().getName() + "=" + entry.getValue())
                    .collect(java.util.stream.Collectors.joining(","));
            return exactRegex(pattern, properties);
        }
        return matches(filter, new ItemStack(level.getBlockState(pos).getBlock()));
    }

    public static ItemStack asCopied(ItemStack source) {
        ItemStack copy = source.copyWithCount(1);
        FilterSettings data =
                copy.getOrDefault(ClayiumDataComponents.FILTER_SETTINGS.get(), FilterSettings.DEFAULT);
        copy.set(ClayiumDataComponents.FILTER_SETTINGS.get(), data.withCopy(true));
        copy.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(1));
        return copy;
    }
}
