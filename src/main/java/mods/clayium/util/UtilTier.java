package mods.clayium.util;

import java.util.HashMap;
import java.util.Map.Entry;
import mods.clayium.block.ClayMachines;
import mods.clayium.block.tile.TileClayContainer;
import mods.clayium.core.ClayiumCore;
import net.minecraft.block.Block;
import net.minecraftforge.common.config.Configuration;

public class UtilTier {
    public static UtilTier.TierManager tierSmelter;
    public static UtilTier.TierManager tierGeneric;
    public static UtilTier.TierManager tierCACondenser;
    public static UtilTier.TierManager tierMachineTransport;
    public static UtilTier.TierManager tierBufferTransport;
    public static final String multCraftTime = "multCraftTime";
    public static final String multConsumingEnergy = "multConsumingEnergy";
    public static final String autoInsertInterval = "autoInsertInterval";
    public static final String autoExtractInterval = "autoExtractInterval";
    public static final String maxAutoInsertDefault = "maxAutoInsertDefault";
    public static final String maxAutoExtractDefault = "maxAutoExtractDefault";

    public UtilTier() {
        super();
    }

    public static boolean canAutoTransfer(int tier) {
        return tier >= 3;
    }

    public static boolean canManufactualCraft(int tier) {
        return tier <= 2;
    }

    public static boolean acceptWaterWheel(int tier) {
        return tier == 2 || tier == 3;
    }

    public static boolean acceptEnergyClay(int tier) {
        return tier >= 4;
    }

    public static void setTierManagers() {
        tierGeneric = UtilTier.TierManager.getMachineTierManager("MachineBase_Crafting");
        tierGeneric.get("multCraftTime").put(Integer.valueOf(5), Float.valueOf(0.25F));
        tierGeneric.get("multCraftTime").put(Integer.valueOf(6), Float.valueOf(0.0625F));
        tierGeneric.get("multCraftTime").put(Integer.valueOf(10), Float.valueOf(0.01F));
        tierGeneric.get("multConsumingEnergy").put(Integer.valueOf(5), Float.valueOf(5.0F));
        tierGeneric.get("multConsumingEnergy").put(Integer.valueOf(6), Float.valueOf(25.0F));
        tierGeneric.get("multConsumingEnergy").put(Integer.valueOf(10), Float.valueOf(250.0F));
        tierSmelter = UtilTier.TierManager.getMachineTierManager("MachineSmelter_Crafting");
        tierSmelter.get("multCraftTime").put(Integer.valueOf(4), Float.valueOf(2.0F));
        tierSmelter.get("multCraftTime").put(Integer.valueOf(5), Float.valueOf(0.5F));
        tierSmelter.get("multCraftTime").put(Integer.valueOf(6), Float.valueOf(0.125F));
        tierSmelter.get("multCraftTime").put(Integer.valueOf(7), Float.valueOf(0.03F));
        tierSmelter.get("multCraftTime").put(Integer.valueOf(8), Float.valueOf(0.01F));
        tierSmelter.get("multCraftTime").put(Integer.valueOf(9), Float.valueOf(0.0025F));
        tierSmelter.get("multConsumingEnergy").put(Integer.valueOf(4), Float.valueOf(1.0F));
        tierSmelter.get("multConsumingEnergy").put(Integer.valueOf(5), Float.valueOf(14.0F));
        tierSmelter.get("multConsumingEnergy").put(Integer.valueOf(6), Float.valueOf(200.0F));
        tierSmelter.get("multConsumingEnergy").put(Integer.valueOf(7), Float.valueOf(2800.0F));
        tierSmelter.get("multConsumingEnergy").put(Integer.valueOf(8), Float.valueOf(40000.0F));
        tierSmelter.get("multConsumingEnergy").put(Integer.valueOf(9), Float.valueOf(560000.0F));
        tierCACondenser = UtilTier.TierManager.getMachineTierManager("MachineCACondenser_Crafting");
        tierCACondenser.get("multCraftTime").put(Integer.valueOf(10), Float.valueOf(0.1F));
        tierCACondenser.get("multConsumingEnergy").put(Integer.valueOf(10), Float.valueOf(10.0F));
        tierCACondenser.get("multCraftTime").put(Integer.valueOf(11), Float.valueOf(0.01F));
        tierCACondenser.get("multConsumingEnergy").put(Integer.valueOf(11), Float.valueOf(100.0F));
        tierMachineTransport = UtilTier.TierManager.getTransportTierManager("MachineBase_Transport");
        tierBufferTransport = UtilTier.TierManager.getTransportTierManager("MachineBuffer_Transport");

        for (int tier = 0; tier <= 13; tier++) {
            if (tier <= 4) {
                tierMachineTransport.get("autoExtractInterval").put(Integer.valueOf(tier), Integer.valueOf(20));
                tierMachineTransport.get("autoInsertInterval").put(Integer.valueOf(tier), Integer.valueOf(20));
                tierMachineTransport.get("maxAutoExtractDefault").put(Integer.valueOf(tier), Integer.valueOf(8));
                tierMachineTransport.get("maxAutoInsertDefault").put(Integer.valueOf(tier), Integer.valueOf(8));
            } else if (tier == 5) {
                tierMachineTransport.get("autoExtractInterval").put(Integer.valueOf(tier), Integer.valueOf(2));
                tierMachineTransport.get("autoInsertInterval").put(Integer.valueOf(tier), Integer.valueOf(2));
                tierMachineTransport.get("maxAutoExtractDefault").put(Integer.valueOf(tier), Integer.valueOf(16));
                tierMachineTransport.get("maxAutoInsertDefault").put(Integer.valueOf(tier), Integer.valueOf(16));
            } else if (tier >= 6) {
                tierMachineTransport.get("autoExtractInterval").put(Integer.valueOf(tier), Integer.valueOf(1));
                tierMachineTransport.get("autoInsertInterval").put(Integer.valueOf(tier), Integer.valueOf(1));
                tierMachineTransport.get("maxAutoExtractDefault").put(Integer.valueOf(tier), Integer.valueOf(64));
                tierMachineTransport.get("maxAutoInsertDefault").put(Integer.valueOf(tier), Integer.valueOf(64));
            }

            if (tier <= 4) {
                tierBufferTransport.get("autoExtractInterval").put(Integer.valueOf(tier), Integer.valueOf(8));
                tierBufferTransport.get("autoInsertInterval").put(Integer.valueOf(tier), Integer.valueOf(8));
                tierBufferTransport.get("maxAutoExtractDefault").put(Integer.valueOf(tier), Integer.valueOf(1));
                tierBufferTransport.get("maxAutoInsertDefault").put(Integer.valueOf(tier), Integer.valueOf(1));
            }

            if (tier >= 7) {
                tierBufferTransport.get("autoExtractInterval").put(Integer.valueOf(tier), Integer.valueOf(1));
                tierBufferTransport.get("autoInsertInterval").put(Integer.valueOf(tier), Integer.valueOf(1));
            }

            switch (tier) {
                case 5:
                    tierBufferTransport.get("autoExtractInterval").put(Integer.valueOf(tier), Integer.valueOf(4));
                    tierBufferTransport.get("autoInsertInterval").put(Integer.valueOf(tier), Integer.valueOf(4));
                    tierBufferTransport.get("maxAutoExtractDefault").put(Integer.valueOf(tier), Integer.valueOf(4));
                    tierBufferTransport.get("maxAutoInsertDefault").put(Integer.valueOf(tier), Integer.valueOf(4));
                    break;
                case 6:
                    tierBufferTransport.get("autoExtractInterval").put(Integer.valueOf(tier), Integer.valueOf(2));
                    tierBufferTransport.get("autoInsertInterval").put(Integer.valueOf(tier), Integer.valueOf(2));
                    tierBufferTransport.get("maxAutoExtractDefault").put(Integer.valueOf(tier), Integer.valueOf(16));
                    tierBufferTransport.get("maxAutoInsertDefault").put(Integer.valueOf(tier), Integer.valueOf(16));
                    break;
                case 7:
                    tierBufferTransport.get("maxAutoExtractDefault").put(Integer.valueOf(tier), Integer.valueOf(64));
                    tierBufferTransport.get("maxAutoInsertDefault").put(Integer.valueOf(tier), Integer.valueOf(64));
                    break;
                case 8:
                    tierBufferTransport.get("maxAutoExtractDefault").put(Integer.valueOf(tier), Integer.valueOf(128));
                    tierBufferTransport.get("maxAutoInsertDefault").put(Integer.valueOf(tier), Integer.valueOf(128));
                    break;
                case 9:
                    tierBufferTransport.get("maxAutoExtractDefault").put(Integer.valueOf(tier), Integer.valueOf(192));
                    tierBufferTransport.get("maxAutoInsertDefault").put(Integer.valueOf(tier), Integer.valueOf(192));
                    break;
                case 10:
                    tierBufferTransport.get("maxAutoExtractDefault").put(Integer.valueOf(tier), Integer.valueOf(256));
                    tierBufferTransport.get("maxAutoInsertDefault").put(Integer.valueOf(tier), Integer.valueOf(256));
                    break;
                case 11:
                    tierBufferTransport.get("maxAutoExtractDefault").put(Integer.valueOf(tier), Integer.valueOf(512));
                    tierBufferTransport.get("maxAutoInsertDefault").put(Integer.valueOf(tier), Integer.valueOf(512));
                    break;
                case 12:
                    tierBufferTransport.get("maxAutoExtractDefault").put(Integer.valueOf(tier), Integer.valueOf(1024));
                    tierBufferTransport.get("maxAutoInsertDefault").put(Integer.valueOf(tier), Integer.valueOf(1024));
                    break;
                case 13:
                    tierBufferTransport.get("maxAutoExtractDefault").put(Integer.valueOf(tier), Integer.valueOf(6400));
                    tierBufferTransport.get("maxAutoInsertDefault").put(Integer.valueOf(tier), Integer.valueOf(6400));
            }
        }
    }

    public static void loadConfig(Configuration cfg) {
        tierSmelter.loadAllConfig(cfg);
        tierGeneric.loadAllConfig(cfg);
        tierCACondenser.loadAllConfig(cfg);
        tierMachineTransport.loadAllConfig(cfg);
        tierBufferTransport.loadAllConfig(cfg);
    }

    public static class TierManager extends HashMap<String, UtilTier.TieredStatus> {
        protected String configName;

        public static UtilTier.TierManager getMachineTierManager() {
            return getMachineTierManager("");
        }

        public static UtilTier.TierManager getMachineTierManager(String configName) {
            UtilTier.TierManager res = new UtilTier.TierManager(configName);
            res.put("multCraftTime", new UtilTier.TieredNumericStatus<>(1.0F, 0.0F, 1.0E9F));
            res.put("multConsumingEnergy", new UtilTier.TieredNumericStatus<>(1.0F, 0.0F, 1.0E9F));
            return res;
        }

        public static UtilTier.TierManager getTransportTierManager(String configName) {
            UtilTier.TierManager res = new UtilTier.TierManager(configName);
            res.put("autoInsertInterval", new UtilTier.TieredNumericStatus<>(20, 1, 999));
            res.put("autoExtractInterval", new UtilTier.TieredNumericStatus<>(20, 1, 999));
            res.put("maxAutoInsertDefault", new UtilTier.TieredNumericStatus<>(8, 1, 9999));
            res.put("maxAutoExtractDefault", new UtilTier.TieredNumericStatus<>(8, 1, 9999));
            return res;
        }

        public TierManager(String configName) {
            super();
            this.configName = configName;
        }

        public static void applyMachineTierManager(Block[] machines, UtilTier.TierManager manager) {
            for (int i = 0; i < machines.length; i++) {
                if (machines[i] instanceof ClayMachines) {
                    ((ClayMachines)machines[i]).multCraftTime = manager.getF("multCraftTime", i);
                    ((ClayMachines)machines[i]).multConsumingEnergy = manager.getF("multConsumingEnergy", i);
                }
            }
        }

        public static void applyTransportTierManager(TileClayContainer tile, int tier, UtilTier.TierManager manager) {
            tile.autoExtractInterval = manager.getI("autoExtractInterval", tier);
            tile.autoInsertInterval = manager.getI("autoInsertInterval", tier);
            tile.maxAutoExtractDefault = manager.getI("maxAutoExtractDefault", tier);
            tile.maxAutoInsertDefault = manager.getI("maxAutoInsertDefault", tier);
        }

        public Object get(String string, int tier) {
            return this.containsKey(string) ? this.get(string).get(tier) : null;
        }

        public float getF(String string, int tier) {
            Object res = this.get(string, tier);
            return res instanceof Float ? (Float)res : 0.0F;
        }

        public int getI(String string, int tier) {
            Object res = this.get(string, tier);
            return res instanceof Integer ? (Integer)res : 0;
        }

        public void loadAllConfig(Configuration cfg) {
            for (Entry<String, UtilTier.TieredStatus> entry : this.entrySet()) {
                entry.getValue().loadAllConfig(this.configName + "_" + entry.getKey(), cfg);
            }
        }
    }

    public static class TieredNumericStatus<T extends Number> extends UtilTier.TieredStatus<T> {
        public T min;
        public T max;

        public TieredNumericStatus(T defvalue) {
            super(defvalue);
        }

        public TieredNumericStatus(T defvalue, T min, T max) {
            this(defvalue);
            this.min = min;
            this.max = max;
        }

        public Object getConfig(String name, Object value, Configuration cfg) {
            if (value instanceof Integer) {
                int minValue = this.min instanceof Integer ? (Integer)this.min : Integer.MIN_VALUE;
                int maxValue = this.max instanceof Integer ? (Integer)this.max : Integer.MAX_VALUE;
                return cfg.getInt(name, this.configCategory, (Integer)value, minValue, maxValue, this.configComment);
            } else if (value instanceof Float) {
                float minValue = this.min instanceof Float ? (Float)this.min : Float.MIN_VALUE;
                float maxValue = this.max instanceof Float ? (Float)this.max : Float.MAX_VALUE;
                return cfg.getFloat(name, this.configCategory, (Float)value, minValue, maxValue, this.configComment);
            } else {
                return super.getConfig(name, value, cfg);
            }
        }
    }

    public static class TieredStatus<T> extends HashMap<Integer, T> {
        public T defvalue;
        public String configComment = "";
        public String configCategory = "tier balance";

        public TieredStatus(T defvalue) {
            super();
            this.defvalue = defvalue;
        }

        public T get(int tier) {
            return this.containsKey(Integer.valueOf(tier)) ? this.get(Integer.valueOf(tier)) : this.defvalue;
        }

        public Object getConfig(String name, Object value, Configuration cfg) {
            if (value instanceof Boolean) {
                return cfg.getBoolean(name, this.configCategory, (Boolean)value, this.configComment);
            } else {
                return value instanceof String ? cfg.getString(name, this.configCategory, (String)value, this.configComment) : null;
            }
        }

        public void loadAllConfig(String name, Configuration cfg) {
            for (Integer tier : this.keySet()) {
                int t = tier;
                String cstr = String.format(name + "_%02d", t);
                Object value = this.getConfig(cstr, this.get(t), cfg);

                try {
                    this.put(Integer.valueOf(t), (T)value);
                } catch (Exception var10) {
                    ClayiumCore.logger.error("Config Error @ " + cstr);
                    ClayiumCore.logger.catching(var10);
                }
            }

            if (this.defvalue != null) {
                String cstr = name + "_def";
                Object value = this.getConfig(cstr, this.defvalue, cfg);

                try {
                    this.defvalue = (T)value;
                } catch (Exception var9) {
                    ClayiumCore.logger.error("Config Error @ " + cstr);
                    ClayiumCore.logger.catching(var9);
                }
            }
        }
    }
}
