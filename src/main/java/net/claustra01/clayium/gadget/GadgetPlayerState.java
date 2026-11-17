package net.claustra01.clayium.gadget;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class GadgetPlayerState {
    public static final Codec<GadgetPlayerState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("auto_eat_cooldown", 0).forGetter(GadgetPlayerState::autoEatCooldown),
            Codec.BOOL.optionalFieldOf("supersonic_speed", false).forGetter(GadgetPlayerState::supersonicSpeed),
            Codec.FLOAT.optionalFieldOf("previous_flight_speed", 0.05F).forGetter(GadgetPlayerState::previousFlightSpeed)
    ).apply(instance, GadgetPlayerState::new));

    private int autoEatCooldown;
    private boolean supersonicSpeed;
    private float previousFlightSpeed;

    public GadgetPlayerState() {
        this(0, false, 0.05F);
    }

    private GadgetPlayerState(int autoEatCooldown, boolean supersonicSpeed, float previousFlightSpeed) {
        this.autoEatCooldown = autoEatCooldown;
        this.supersonicSpeed = supersonicSpeed;
        this.previousFlightSpeed = previousFlightSpeed;
    }

    public int autoEatCooldown() {
        return autoEatCooldown;
    }

    public void autoEatCooldown(int autoEatCooldown) {
        this.autoEatCooldown = autoEatCooldown;
    }

    public boolean supersonicSpeed() {
        return supersonicSpeed;
    }

    public void supersonicSpeed(boolean supersonicSpeed) {
        this.supersonicSpeed = supersonicSpeed;
    }

    public float previousFlightSpeed() {
        return previousFlightSpeed;
    }

    public void previousFlightSpeed(float previousFlightSpeed) {
        this.previousFlightSpeed = previousFlightSpeed;
    }
}
