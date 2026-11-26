package net.claustra01.clayium.gadget;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class GadgetPlayerState {
    public static final Codec<GadgetPlayerState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("supersonic_speed", false).forGetter(GadgetPlayerState::supersonicSpeed),
            Codec.FLOAT.optionalFieldOf("previous_flight_speed", 0.05F).forGetter(GadgetPlayerState::previousFlightSpeed)
    ).apply(instance, GadgetPlayerState::new));

    private boolean supersonicSpeed;
    private float previousFlightSpeed;

    public GadgetPlayerState() {
        this(false, 0.05F);
    }

    private GadgetPlayerState(boolean supersonicSpeed, float previousFlightSpeed) {
        this.supersonicSpeed = supersonicSpeed;
        this.previousFlightSpeed = previousFlightSpeed;
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
