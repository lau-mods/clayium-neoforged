package net.claustra01.clayium.item;

/**
 * Item filter items (Duplicator, Whitelist, Blacklist, Fuzzy, Tag, Name, Translation, Registry, ModId, Damage, BlockState, Harvestable).
 * Filter matching & GUI logic is wired in Step 08/10.
 */
public class FilterItem extends SimpleTieredItem {
    public enum FilterKind {
        DUPLICATOR,
        WHITELIST,
        BLACKLIST,
        FUZZY,
        TAG,
        DISPLAY_NAME,
        TRANSLATION_KEY,
        REGISTRY_ID,
        MOD_ID,
        DAMAGE_VALUE,
        BLOCK_STATE,
        HARVESTABLE_BLOCK
    }

    private final FilterKind filterKind;

    public FilterItem(int tier, String iconName, FilterKind filterKind) {
        super(new Properties().stacksTo(64), tier, iconName);
        this.filterKind = filterKind;
    }

    public FilterKind getFilterKind() {
        return this.filterKind;
    }
}
