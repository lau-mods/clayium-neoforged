package net.claustra01.clayium.machine.ca;

public final class CaMachineRules {
    public static int condenserOutput(int baseCount, double resonance) {
        double scaled = baseCount * (1.0D + Math.log(Math.max(1.0D, resonance)));
        return Math.max(baseCount, (int)scaled);
    }

    public static double injectorProgressMultiplier(int tier, double resonance) {
        double exponent = switch (tier) {
            case 9 -> 0.2D;
            case 10 -> 0.9D;
            case 11 -> 3.0D;
            default -> 1.0D;
        };
        return Math.pow(Math.max(1.0D, resonance), exponent);
    }

    private CaMachineRules() {
    }
}
