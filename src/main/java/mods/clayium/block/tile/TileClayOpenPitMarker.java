package mods.clayium.block.tile;

public class TileClayOpenPitMarker extends TileClayMarker {
    public TileClayOpenPitMarker() {
        super();
    }

    public void activate() {
        super.activate();
        if (this.aabb != null) {
            this.aabb.field_72338_b = 1.0;
        }
    }

    public static class TileClayGroundLevelingMarker extends TileClayMarker {
        public TileClayGroundLevelingMarker() {
            super();
        }

        public void activate() {
            super.activate();
            if (this.aabb != null) {
                this.aabb.field_72337_e = 255.0;
            }
        }
    }

    public static class TileClayPrismMarker extends TileClayMarker {
        public TileClayPrismMarker() {
            super();
        }

        public void activate() {
            super.activate();
            if (this.aabb != null) {
                this.aabb.field_72338_b = 1.0;
                this.aabb.field_72337_e = 255.0;
            }
        }
    }
}
