package net.claustra01.clayium.gadget;

public enum GadgetType {
    TEMPLATE("clay_gadget_template", "gadget_blank", Category.NONE, 0),
    OVERCLOCK_I("clay_gadget_overclock_i", "gadget_antimatteroverclocker", Category.OVERCLOCK, 1),
    OVERCLOCK_II("clay_gadget_overclock_ii", "gadget_pureantimatteroverclocker", Category.OVERCLOCK, 2),
    OVERCLOCK_III("clay_gadget_overclock_iii", "gadget_oecoverclocker", Category.OVERCLOCK, 3),
    OVERCLOCK_IV("clay_gadget_overclock_iv", "gadget_opaoverclocker", Category.OVERCLOCK, 4),
    FLIGHT("clay_gadget_flight", "gadget_flight0", Category.FLIGHT, 1),
    FREE_FLIGHT("clay_gadget_free_flight", "gadget_flight1", Category.FLIGHT, 2),
    SUPERSONIC_FLIGHT("clay_gadget_supersonic_flight", "gadget_flight2", Category.FLIGHT, 3),
    EXTRA_HEALTH_I("clay_gadget_extra_health_i", "gadget_health0", Category.HEALTH, 1),
    EXTRA_HEALTH_II("clay_gadget_extra_health_ii", "gadget_health1", Category.HEALTH, 2),
    EXTRA_HEALTH_III("clay_gadget_extra_health_iii", "gadget_health2", Category.HEALTH, 3),
    ECONOMICAL_AUTO_EAT("clay_gadget_economical_auto_eat", "gadget_autoeat0", Category.AUTO_EAT, 1),
    AUTO_EAT("clay_gadget_auto_eat", "gadget_autoeat1", Category.AUTO_EAT, 2),
    REPEATED_ATTACK("clay_gadget_repeatedly_attack", "gadget_repattack", Category.REPEATED_ATTACK, 1),
    LONG_ARM("clay_gadget_long_arm", "gadget_longarm0", Category.LONG_ARM, 1),
    LONGER_ARM("clay_gadget_longer_arm", "gadget_longarm1", Category.LONG_ARM, 2),
    LONGEST_ARM("clay_gadget_longest_arm", "gadget_longarm2", Category.LONG_ARM, 3);

    private final String id;
    private final String texture;
    private final Category category;
    private final int rank;

    GadgetType(String id, String texture, Category category, int rank) {
        this.id = id;
        this.texture = texture;
        this.category = category;
        this.rank = rank;
    }

    public String id() {
        return id;
    }

    public String texture() {
        return texture;
    }

    public Category category() {
        return category;
    }

    public int rank() {
        return rank;
    }

    public enum Category {
        NONE,
        OVERCLOCK,
        FLIGHT,
        HEALTH,
        AUTO_EAT,
        REPEATED_ATTACK,
        LONG_ARM
    }
}
