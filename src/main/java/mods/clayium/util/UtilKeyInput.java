package mods.clayium.util;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.HashMap;
import mods.clayium.core.ClayiumCore;
import mods.clayium.network.KeyInputEventPacket;
import mods.clayium.network.MouseClickEventPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.MouseEvent;

public class UtilKeyInput {
    public static UtilKeyInput INSTANCE = new UtilKeyInput();
    @SideOnly(Side.CLIENT)
    public static volatile HashMap<KeyBinding, PlayerKey.KeyType> keyBindingMap;
    public static HashMap<EntityPlayer, PlayerKey> playerKeyMapClient = new HashMap<>();
    public static HashMap<EntityPlayer, PlayerKey> playerKeyMap = new HashMap<>();

    public UtilKeyInput() {
        super();
    }

    public void playerTick(EntityPlayer player) {
        if (!keyMapContainsKey(player)) {
            keyMapPut(player, new PlayerKey());
        }

        keyMapGet(player).update();
    }

    @Deprecated
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void MouseEventSubscriber(MouseEvent event) {
        if (event.button != -1) {
            setMouseInput(UtilPlayer.getClientPlayer(), event.button, event.buttonstate);
            ClayiumCore.packetDispatcher.sendToServer(new MouseClickEventPacket(event.button, event.buttonstate));
        }
    }

    @Deprecated
    @SideOnly(Side.CLIENT)
    public void sendMouseEventToServer(int button, boolean buttonstate) {
        EntityPlayer player = UtilPlayer.getClientPlayer();
        if (button != -1 && player != null) {
            boolean oldstate = getMouseLength(player, button) >= 0;
            if (oldstate != buttonstate) {
                setMouseInput(player, button, buttonstate);
                ClayiumCore.packetDispatcher.sendToServer(new MouseClickEventPacket(button, buttonstate));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void ClientTickEventSubscriber(ClientTickEvent event) {
        if (event.phase == Phase.END) {
            if (Minecraft.func_71410_x().field_71462_r != null) {
                this.sendMouseEventToServer(0, false);
                this.sendMouseEventToServer(1, false);
            }

            this.sendAllKeyEventToServer();
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void KeyEventSubscriber(KeyInputEvent event) {
    }

    @SideOnly(Side.CLIENT)
    public synchronized void sendAllKeyEventToServer() {
        if (keyBindingMap == null) {
            keyBindingMap = new HashMap<>();
            keyBindingMap.put(Minecraft.func_71410_x().field_71474_y.field_151444_V, PlayerKey.KeyType.SPRINT);
            keyBindingMap.put(Minecraft.func_71410_x().field_71474_y.field_74313_G, PlayerKey.KeyType.USE_ITEM);
        }

        for (KeyBinding key : keyBindingMap.keySet()) {
            this.sendKeyEventToServer(key);
        }
    }

    @SideOnly(Side.CLIENT)
    public void sendKeyEventToServer(PlayerKey.KeyType key, boolean buttonstate) {
        if (key != null) {
            EntityPlayer player = UtilPlayer.getClientPlayer();
            if (player != null) {
                boolean oldstate = getKeyLength(player, key) >= 0;
                if (oldstate != buttonstate) {
                    setKeyInput(player, key, buttonstate);
                    ClayiumCore.packetDispatcher.sendToServer(new KeyInputEventPacket(key, buttonstate));
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void sendKeyEventToServer(KeyBinding key) {
        this.sendKeyEventToServer(keyBindingMap.get(key), key.func_151470_d());
    }

    public static PlayerKey keyMapGet(EntityPlayer player) {
        return UtilPlayer.isRemote(player) ? playerKeyMapClient.get(player) : playerKeyMap.get(player);
    }

    public static PlayerKey keyMapPut(EntityPlayer player, PlayerKey value) {
        return UtilPlayer.isRemote(player) ? playerKeyMapClient.put(player, value) : playerKeyMap.put(player, value);
    }

    public static boolean keyMapContainsKey(EntityPlayer player) {
        return UtilPlayer.isRemote(player) ? playerKeyMapClient.containsKey(player) : playerKeyMap.containsKey(player);
    }

    public static int getKeyLength(EntityPlayer player, PlayerKey.KeyType key) {
        if (!keyMapContainsKey(player)) {
            keyMapPut(player, new PlayerKey());
        }

        return keyMapGet(player).getKeyLength(key);
    }

    public static void setKeyInput(EntityPlayer player, PlayerKey.KeyType key, boolean buttonstate) {
        if (!keyMapContainsKey(player)) {
            keyMapPut(player, new PlayerKey());
        }

        keyMapGet(player).setKeyInput(key, buttonstate);
    }

    @Deprecated
    public static int getMouseLength(EntityPlayer player, int button) {
        if (!keyMapContainsKey(player)) {
            keyMapPut(player, new PlayerKey());
        }

        return keyMapGet(player).getMouseLength(button);
    }

    @Deprecated
    public static void setMouseInput(EntityPlayer player, int button, boolean buttonstate) {
        if (!keyMapContainsKey(player)) {
            keyMapPut(player, new PlayerKey());
        }

        keyMapGet(player).setMouseInput(button, buttonstate);
    }
}
