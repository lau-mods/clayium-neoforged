package net.claustra01.clayium.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.claustra01.clayium.component.ClayiumDataComponentTypes.FilterConfigData;
import net.claustra01.clayium.item.FilterItem;
import net.claustra01.clayium.item.FilterItem.FilterKind;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Evaluates all 12 original Clayium item/block filter modes (whitelist, blacklist, fuzzy,
 * tag/OreDict regex, display name, translation key, registry ID, mod ID, damage/metadata,
 * block state, harvestable crop, and duplicator/nested filter recursion).
 */
public final class ClayiumFilterMatcher {
    public static final int MAX_FILTER_SIZE = 100;
    private static final int MAX_DEPTH = 16;

    private ClayiumFilterMatcher() {}

    public static boolean isFilter(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof FilterItem;
    }

    public static boolean isCopyMode(ItemStack stack) {
        if (!isFilter(stack)) {
            return false;
        }
        FilterItem filter = (FilterItem) stack.getItem();
        if (filter.getFilterKind() == FilterKind.DUPLICATOR) {
            return true;
        }
        FilterConfigData data = stack.get(ClayiumDataComponents.FILTER_CONFIG.get());
        return data != null && data.copy();
    }

    public static ItemStack withCopyFlag(ItemStack stack, boolean copy) {
        if (!isFilter(stack)) {
            return stack;
        }
        FilterItem filter = (FilterItem) stack.getItem();
        if (filter.getFilterKind() == FilterKind.DUPLICATOR) {
            return stack;
        }
        FilterConfigData current = getConfig(stack);
        stack.set(
                ClayiumDataComponents.FILTER_CONFIG.get(),
                new FilterConfigData(current.pattern(), current.regex(), current.slots(), copy)
        );
        return stack;
    }

    public static FilterConfigData getConfig(ItemStack stack) {
        FilterConfigData data = stack.get(ClayiumDataComponents.FILTER_CONFIG.get());
        return data != null ? data : new FilterConfigData("", false, ItemContainerContents.EMPTY, false);
    }

    public static int getFilterSize(ItemStack stack) {
        return getFilterSize(stack, 0);
    }

    private static int getFilterSize(ItemStack stack, int depth) {
        if (!isFilter(stack) || depth >= MAX_DEPTH) {
            return 0;
        }
        FilterItem item = (FilterItem) stack.getItem();
        FilterKind kind = item.getFilterKind();
        if (kind != FilterKind.WHITELIST && kind != FilterKind.BLACKLIST && kind != FilterKind.FUZZY) {
            return 1;
        }
        FilterConfigData data = getConfig(stack);
        int size = 0;
        for (ItemStack slotStack : data.slots().nonEmptyItems()) {
            size++;
            if (isFilter(slotStack)) {
                size += getFilterSize(slotStack, depth + 1);
            }
        }
        return size;
    }

    public static boolean match(ItemStack filterStack, ItemStack candidate) {
        return match(filterStack, candidate, 0);
    }

    private static boolean match(ItemStack filterStack, ItemStack candidate, int depth) {
        if (!isFilter(filterStack) || depth >= MAX_DEPTH) {
            return false;
        }
        FilterKind kind = ((FilterItem) filterStack.getItem()).getFilterKind();
        FilterConfigData data = getConfig(filterStack);

        return switch (kind) {
            case DUPLICATOR -> false;
            case WHITELIST -> matchContainer(data.slots(), candidate, false, depth);
            case BLACKLIST -> !matchContainer(data.slots(), candidate, false, depth);
            case FUZZY -> matchContainer(data.slots(), candidate, true, depth);
            case TAG -> matchTagPattern(data.pattern(), candidate);
            case DISPLAY_NAME -> matchRegex(data.pattern(), candidate.isEmpty() ? "" : candidate.getHoverName().getString());
            case TRANSLATION_KEY -> matchRegex(data.pattern(), candidate.isEmpty() ? "" : candidate.getDescriptionId());
            case REGISTRY_ID -> matchRegex(
                    data.pattern(),
                    candidate.isEmpty() ? "" : BuiltInRegistries.ITEM.getKey(candidate.getItem()).toString()
            );
            case MOD_ID -> matchModId(data.pattern(), candidate);
            case DAMAGE_VALUE, BLOCK_STATE -> !candidate.isEmpty()
                    && matchExactRegex(data.pattern(), String.valueOf(candidate.getDamageValue()));
            case HARVESTABLE_BLOCK -> false;
        };
    }

    public static boolean matchBlock(ItemStack filterStack, Level level, BlockPos pos) {
        return matchBlock(filterStack, level, pos, 0);
    }

    private static boolean matchBlock(ItemStack filterStack, Level level, BlockPos pos, int depth) {
        if (!isFilter(filterStack) || level == null || depth >= MAX_DEPTH) {
            return false;
        }
        FilterKind kind = ((FilterItem) filterStack.getItem()).getFilterKind();
        FilterConfigData data = getConfig(filterStack);
        BlockState state = level.getBlockState(pos);
        ItemStack blockStack = new ItemStack(state.getBlock().asItem());

        return switch (kind) {
            case DUPLICATOR -> false;
            case HARVESTABLE_BLOCK -> state.getBlock() instanceof BonemealableBlock growable
                    && !growable.isValidBonemealTarget(level, pos, state);
            case BLOCK_STATE -> matchExactRegex(data.pattern(), state.toString())
                    || matchExactRegex(data.pattern(), state.getBlock().getStateDefinition().getPossibleStates().indexOf(state) + "");
            case WHITELIST -> matchBlockContainer(data.slots(), level, pos, blockStack, false, depth);
            case BLACKLIST -> !matchBlockContainer(data.slots(), level, pos, blockStack, false, depth);
            case FUZZY -> matchBlockContainer(data.slots(), level, pos, blockStack, true, depth);
            default -> match(filterStack, blockStack, depth);
        };
    }

    private static boolean matchContainer(
            ItemContainerContents slots,
            ItemStack candidate,
            boolean fuzzy,
            int depth) {
        for (ItemStack entry : slots.nonEmptyItems()) {
            if (isFilter(entry) && match(entry, candidate, depth + 1)) {
                return true;
            }
            if (matchBetweenItemStacks(entry, candidate, fuzzy)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchBlockContainer(
            ItemContainerContents slots,
            Level level,
            BlockPos pos,
            ItemStack blockStack,
            boolean fuzzy,
            int depth) {
        for (ItemStack entry : slots.nonEmptyItems()) {
            if (isFilter(entry) && matchBlock(entry, level, pos, depth + 1)) {
                return true;
            }
            if (matchBetweenItemStacks(entry, blockStack, fuzzy)) {
                return true;
            }
        }
        return false;
    }

    public static boolean matchBetweenItemStacks(ItemStack filter, ItemStack candidate, boolean fuzzy) {
        if (filter.isEmpty()) {
            return candidate.isEmpty();
        }
        if (candidate.isEmpty()) {
            return false;
        }
        if (!fuzzy) {
            return ItemStack.isSameItemSameComponents(filter, candidate);
        }
        if (filter.is(candidate.getItem())) {
            if (filter.isDamageableItem() || candidate.isDamageableItem()
                    || filter.getDamageValue() == candidate.getDamageValue()) {
                return true;
            }
        }
        return candidate.getTags().anyMatch(filter::is);
    }

    private static boolean matchTagPattern(String patternStr, ItemStack candidate) {
        if (candidate.isEmpty() || patternStr == null || patternStr.isEmpty()) {
            return false;
        }
        Pattern compiled = compilePattern(patternStr);
        if (compiled == null) {
            return false;
        }
        return candidate.getTags().anyMatch(tag -> {
            for (String candidateName : tagNamesAndAliases(tag)) {
                if (compiled.matcher(candidateName).find()) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * Generates the raw tag path/id plus 1.7.10 OreDictionary camelCase aliases (e.g. {@code c:ingots/copper} ->
     * {@code ingotCopper}) so both modern tag paths and classic OreDict regexes match.
     */
    public static List<String> tagNamesAndAliases(TagKey<Item> tag) {
        List<String> out = new ArrayList<>(4);
        String full = tag.location().toString();
        String path = tag.location().getPath();
        out.add(full);
        out.add(path);
        int slash = path.indexOf('/');
        if (slash > 0 && slash + 1 < path.length()) {
            String folder = path.substring(0, slash);
            String mat = path.substring(slash + 1);
            String prefix = switch (folder) {
                case "ingots" -> "ingot";
                case "dusts" -> "dust";
                case "plates" -> "plate";
                case "large_plates" -> "largePlate";
                case "gears" -> "gear";
                case "rods", "sticks" -> "stick";
                case "nuggets" -> "nugget";
                case "gems" -> "gem";
                case "storage_blocks" -> "block";
                case "ores" -> "ore";
                case "circuits" -> "circuit";
                default -> folder;
            };
            out.add(prefix + toPascalCase(mat));
        }
        return out;
    }

    private static String toPascalCase(String snake) {
        StringBuilder sb = new StringBuilder(snake.length());
        boolean upper = true;
        for (int i = 0; i < snake.length(); i++) {
            char c = snake.charAt(i);
            if (c == '_' || c == '-') {
                upper = true;
            } else {
                sb.append(upper ? Character.toUpperCase(c) : Character.toLowerCase(c));
                upper = false;
            }
        }
        return sb.toString();
    }

    private static boolean matchModId(String patternStr, ItemStack candidate) {
        if (candidate.isEmpty()) {
            return false;
        }
        String modId = candidate.getItem() instanceof BlockItem blockItem
                ? BuiltInRegistries.BLOCK.getKey(blockItem.getBlock()).getNamespace()
                : BuiltInRegistries.ITEM.getKey(candidate.getItem()).getNamespace();
        return matchRegex(patternStr, modId);
    }

    private static boolean matchExactRegex(String patternStr, String target) {
        if (patternStr == null || patternStr.isEmpty()) {
            return false;
        }
        return matchRegex("^" + patternStr + "$", target);
    }

    private static boolean matchRegex(String patternStr, String target) {
        if (patternStr == null || patternStr.isEmpty() || target == null) {
            return false;
        }
        Pattern compiled = compilePattern(patternStr);
        return compiled != null && compiled.matcher(target).find();
    }

    private static Pattern compilePattern(String patternStr) {
        try {
            return Pattern.compile(patternStr);
        } catch (PatternSyntaxException e) {
            return null;
        }
    }

    public static void appendFilterTooltip(ItemStack filterStack, List<Component> tooltip, int indent) {
        if (!isFilter(filterStack) || tooltip.size() >= MAX_FILTER_SIZE || indent >= MAX_DEPTH) {
            return;
        }
        FilterKind kind = ((FilterItem) filterStack.getItem()).getFilterKind();
        FilterConfigData data = getConfig(filterStack);
        String pad = " ".repeat(Math.max(0, indent));
        if (kind == FilterKind.WHITELIST || kind == FilterKind.BLACKLIST || kind == FilterKind.FUZZY) {
            for (ItemStack entry : data.slots().nonEmptyItems()) {
                if (tooltip.size() >= MAX_FILTER_SIZE) {
                    break;
                }
                tooltip.add(Component.literal(pad + entry.getHoverName().getString()).withStyle(ChatFormatting.DARK_GRAY));
                if (isFilter(entry)) {
                    appendFilterTooltip(entry, tooltip, indent + 1);
                }
            }
        } else if (!data.pattern().isEmpty()) {
            tooltip.add(Component.literal(pad + data.pattern()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public static String describeSummary(ItemStack filterStack) {
        if (!isFilter(filterStack)) {
            return "";
        }
        FilterConfigData data = getConfig(filterStack);
        if (!data.pattern().isEmpty()) {
            return data.pattern();
        }
        List<String> names = new ArrayList<>();
        for (ItemStack s : data.slots().nonEmptyItems()) {
            names.add(s.getHoverName().getString());
            if (names.size() >= 3) {
                break;
            }
        }
        return String.join(", ", names).toLowerCase(Locale.ROOT);
    }
}
