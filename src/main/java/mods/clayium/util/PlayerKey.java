package mods.clayium.util;

import java.util.HashMap;
import java.util.Map.Entry;

public class PlayerKey {
    private HashMap<Integer, Integer> mouse = new HashMap<>();
    private HashMap<PlayerKey.KeyType, Integer> keys = new HashMap<>();

    public PlayerKey() {
        super();
    }

    private Entry<Integer, Integer> getMouseEntry(int input) {
        for (Entry<Integer, Integer> entry : this.mouse.entrySet()) {
            if (entry.getKey() == input) {
                return entry;
            }
        }

        return null;
    }

    private Entry<PlayerKey.KeyType, Integer> getKeyEntry(PlayerKey.KeyType key) {
        for (Entry<PlayerKey.KeyType, Integer> entry : this.keys.entrySet()) {
            if (entry.getKey() == key) {
                return entry;
            }
        }

        return null;
    }

    public int getMouseLength(int input) {
        Entry<Integer, Integer> entry = this.getMouseEntry(input);
        return entry == null ? Integer.MIN_VALUE : entry.getValue();
    }

    public void setMouseInput(int input, boolean event) {
        Entry<Integer, Integer> entry = this.getMouseEntry(input);
        if (entry == null & event) {
            this.mouse.put(input, 0);
        }

        if (entry != null & !event) {
            this.mouse.remove(entry.getKey());
        }
    }

    public int getKeyLength(PlayerKey.KeyType key) {
        Entry<PlayerKey.KeyType, Integer> entry = this.getKeyEntry(key);
        return entry == null ? Integer.MIN_VALUE : entry.getValue();
    }

    public void setKeyInput(PlayerKey.KeyType key, boolean event) {
        Entry<PlayerKey.KeyType, Integer> entry = this.getKeyEntry(key);
        if (entry == null & event) {
            this.keys.put(key, 0);
        }

        if (entry != null & !event) {
            this.keys.remove(entry.getKey());
        }
    }

    public void update() {
        for (Entry<Integer, Integer> entry : this.mouse.entrySet()) {
            entry.setValue(entry.getValue() + 1);
        }

        for (Entry<PlayerKey.KeyType, Integer> entry : this.keys.entrySet()) {
            entry.setValue(entry.getValue() + 1);
        }
    }

    public static enum KeyType {
        SPRINT(0),
        USE_ITEM(1);

        private int id = -1;

        private KeyType(int id) {
            this.id = id;
        }

        public static PlayerKey.KeyType getKeyFromId(int id) {
            for (PlayerKey.KeyType key : values()) {
                if (key.id == id) {
                    return key;
                }
            }

            return null;
        }

        public int getId() {
            return this.id;
        }
    }
}
