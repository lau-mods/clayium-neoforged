package net.claustra01.clayium.machine.autocrafter;

import java.util.List;

public record AutoCrafterSpecification(
        int tier,
        int intervalTicks,
        int craftsPerCycle,
        long energyPerCraft) {
    public static final List<AutoCrafterSpecification> VALUES = List.of(
            new AutoCrafterSpecification(5, 20, 1, 0L),
            new AutoCrafterSpecification(6, 1, 1, 10L),
            new AutoCrafterSpecification(7, 1, 16, 40L),
            new AutoCrafterSpecification(8, 1, 64, 160L),
            new AutoCrafterSpecification(9, 1, 256, 640L));
}
