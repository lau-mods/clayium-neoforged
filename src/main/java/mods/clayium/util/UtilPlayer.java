package mods.clayium.util;

import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.UUID;
import javax.crypto.SecretKey;
import mods.clayium.core.ClayiumCore;
import mods.clayium.item.ClayShooter;
import mods.clayium.item.ItemGadgetHolder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C0CPacketInput;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import net.minecraft.network.play.client.C11PacketEnchantItem;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import net.minecraft.network.play.client.C13PacketPlayerAbilities;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.network.play.client.C15PacketClientSettings;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.FakePlayerFactory;

public class UtilPlayer {
    public static UtilPlayer INSTANCE = new UtilPlayer();
    public static HashMap<EntityPlayer, HashMap<String, Object>> playerInstantDataMap = new HashMap<>();
    public static HashMap<EntityPlayer, HashMap<String, Object>> playerInstantDataMapClient = new HashMap<>();
    private static EntityPlayer defaultPlayer = null;

    public UtilPlayer() {
        super();
    }

    @SubscribeEvent
    public void PlayerTickEventSubscriber(PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase == Phase.START) {
            UtilAdvancedTools.INSTANCE.playerTick(player);
            UtilKeyInput.INSTANCE.playerTick(player);
            this.clayShooterTick(player);
            if (player == getClientPlayer()) {
                ClayiumCore.proxy.clientPlayerTick(player);
            }

            ItemGadgetHolder.updateGadget(event.player, event.player.field_70170_p.field_72995_K);
            ItemGadgetHolder.clearGadgetList(event.player, event.player.field_70170_p.field_72995_K);
        }
    }

    public void clayShooterTick(EntityPlayer player) {
        ItemStack itemstack = player.func_71045_bC();
        if (itemstack != null && itemstack.func_77973_b() instanceof ClayShooter) {
            ClayShooter item = (ClayShooter)itemstack.func_77973_b();
            int cooldownTime = item.getCooldownTime(itemstack, player);
            float shootingRate = item.getShootingRate(itemstack, player);
            float cooldown = (Float)getPlayerInstantDataWithSafety(player, "ClayShooterCoolDown", new Float(0.0F));
            if (UtilKeyInput.getKeyLength(player, PlayerKey.KeyType.USE_ITEM) < 0) {
                if (cooldown >= 0.0F) {
                    cooldown = -1.0F;
                } else {
                    cooldown--;
                }
            } else {
                if (UtilKeyInput.getKeyLength(player, PlayerKey.KeyType.USE_ITEM) == 1 && cooldown <= -((float)cooldownTime)) {
                    cooldown = 1.0F;
                }

                if (cooldown >= 0.0F) {
                    while (cooldown >= 1.0F) {
                        cooldown--;
                        if (!item.isCharger(itemstack, player) && !player.field_70170_p.field_72995_K) {
                            item.shootBullet(itemstack, player);
                        }
                    }

                    cooldown += shootingRate;
                }
            }

            setPlayerInstantData(player, "ClayShooterCoolDown", new Float(cooldown));
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void FOVUpdateEventSubscriber(FOVUpdateEvent event) {
        ItemStack stack = event.entity.func_71011_bu();
        if (event.entity.func_71039_bw() && stack.func_77973_b() instanceof ClayShooter) {
            ClayShooter item = (ClayShooter)stack.func_77973_b();
            if (item.isCharger(stack, event.entity)) {
                int time = item.getChargeTime(stack, event.entity);
                int i = event.entity.func_71057_bx();
                if (i > time) {
                    i = time;
                }

                float f1 = (float)i / (float)time;
                if (f1 > 1.0F) {
                    f1 = 1.0F;
                } else {
                    f1 *= f1;
                }

                float t = item.getInitialVelocity(stack, event.entity) * (float)item.getLifespan(stack, event.entity);
                float u = (float)i / (float)time;
                float k = t * u - 10.0F;
                if (k < 0.0F) {
                    k = 0.0F;
                }

                k = k + 3600.0F / (k + 60.0F) - 60.0F;
                float m = 2.5F / (k + 2.5F);
                event.newfov = event.fov * m + (1.0F - m) * 0.1F;
            }
        }
    }

    public static HashMap<EntityPlayer, HashMap<String, Object>> getPlayerInstantDataMap(boolean isRemote) {
        return isRemote ? playerInstantDataMapClient : playerInstantDataMap;
    }

    public static Object getPlayerInstantDataWithSafety(EntityPlayer player, String key, Object def) {
        Object res = getPlayerInstantData(player, key);
        if (res == null) {
            setPlayerInstantData(player, key, def);
            res = def;
        }

        return res;
    }

    public static Object getPlayerInstantData(EntityPlayer player, String key) {
        return !dataMapContainsKey(player) ? null : (!dataMapGet(player).containsKey(key) ? null : dataMapGet(player).get(key));
    }

    public static void setPlayerInstantData(EntityPlayer player, String key, Object value) {
        if (!dataMapContainsKey(player)) {
            dataMapPut(player, new HashMap<>());
        }

        dataMapGet(player).put(key, value);
    }

    public static boolean isRemote(EntityPlayer player) {
        return player.field_70170_p.field_72995_K;
    }

    public static EntityPlayer getClientPlayer() {
        return ClayiumCore.proxy.getClientPlayer();
    }

    public static boolean dataMapContainsKey(EntityPlayer player) {
        return getPlayerInstantDataMap(isRemote(player)).containsKey(player);
    }

    public static HashMap<String, Object> dataMapPut(EntityPlayer player, HashMap<String, Object> value) {
        return getPlayerInstantDataMap(isRemote(player)).put(player, value);
    }

    public static HashMap<String, Object> dataMapGet(EntityPlayer player) {
        return getPlayerInstantDataMap(isRemote(player)).get(player);
    }

    public static EntityPlayer getFakePlayer(String id, World world, double x, double y, double z) {
        return (EntityPlayer)setWorldAndPosition(getFakePlayer(id), world, x, y, z);
    }

    public static EntityPlayer getFakePlayer(String id, World world, int x, int y, int z) {
        return getFakePlayer(id, world, (double)x + 0.5, (double)y + 0.5, (double)z + 0.5);
    }

    public static EntityPlayer getFakePlayerWithItem(String id, ItemStack itemstack) {
        EntityPlayer player = getFakePlayer(id);
        player.field_71071_by.func_70299_a(0, itemstack);
        player.field_71071_by.field_70461_c = 0;
        return player;
    }

    public static Entity setWorldAndPosition(Entity entity, World world, double x, double y, double z) {
        if (entity != null) {
            entity.func_70029_a(world);
            entity.func_70107_b(x, y, z);
        }

        return entity;
    }

    public static EntityPlayer getFakePlayer(String id) {
        EntityPlayerMP ret = null;
        if (id != null || defaultPlayer == null) {
            ret = FakePlayerFactory.get(DimensionManager.getWorld(0), getGameProfile(id));
            ret.field_71135_a = new UtilPlayer.NetHandlerPlayServerFakePlayer(ret);
            ret.eyeHeight = 0.0F;
        }

        if (id == null) {
            if (ret != null) {
                defaultPlayer = ret;
            }

            return defaultPlayer;
        } else {
            return ret;
        }
    }

    public static GameProfile getGameProfile(String id) {
        if (id == null) {
            id = "[Clayium]";
        }

        return new GameProfile(UUID.nameUUIDFromBytes(id.getBytes(Charsets.UTF_8)), id);
    }

    public static class NetHandlerPlayServerFakePlayer extends NetHandlerPlayServer {
        public NetHandlerPlayServerFakePlayer(EntityPlayerMP player) {
            super(FMLCommonHandler.instance().getMinecraftServerInstance(), new UtilPlayer.NetworkManagerFakePlayer(), player);
        }

        public void func_147233_a() {
        }

        public void func_147360_c(String p_147360_1_) {
        }

        public void func_147358_a(C0CPacketInput p_147358_1_) {
        }

        public void func_147347_a(C03PacketPlayer p_147347_1_) {
        }

        public void func_147364_a(double p_147364_1_, double p_147364_3_, double p_147364_5_, float p_147364_7_, float p_147364_8_) {
        }

        public void func_147345_a(C07PacketPlayerDigging p_147345_1_) {
        }

        public void func_147346_a(C08PacketPlayerBlockPlacement p_147346_1_) {
        }

        public void func_147231_a(IChatComponent p_147231_1_) {
        }

        public void func_147359_a(Packet p_147359_1_) {
        }

        public void func_147355_a(C09PacketHeldItemChange p_147355_1_) {
        }

        public void func_147354_a(C01PacketChatMessage p_147354_1_) {
        }

        public void func_147350_a(C0APacketAnimation p_147350_1_) {
        }

        public void func_147357_a(C0BPacketEntityAction p_147357_1_) {
        }

        public void func_147340_a(C02PacketUseEntity p_147340_1_) {
        }

        public void func_147342_a(C16PacketClientStatus p_147342_1_) {
        }

        public void func_147356_a(C0DPacketCloseWindow p_147356_1_) {
        }

        public void func_147351_a(C0EPacketClickWindow p_147351_1_) {
        }

        public void func_147338_a(C11PacketEnchantItem p_147338_1_) {
        }

        public void func_147344_a(C10PacketCreativeInventoryAction p_147344_1_) {
        }

        public void func_147339_a(C0FPacketConfirmTransaction p_147339_1_) {
        }

        public void func_147343_a(C12PacketUpdateSign p_147343_1_) {
        }

        public void func_147353_a(C00PacketKeepAlive p_147353_1_) {
        }

        public void func_147348_a(C13PacketPlayerAbilities p_147348_1_) {
        }

        public void func_147341_a(C14PacketTabComplete p_147341_1_) {
        }

        public void func_147352_a(C15PacketClientSettings p_147352_1_) {
        }

        public void func_147349_a(C17PacketCustomPayload p_147349_1_) {
        }

        public void func_147232_a(EnumConnectionState p_147232_1_, EnumConnectionState p_147232_2_) {
        }
    }

    public static class NetworkManagerFakePlayer extends NetworkManager {
        public NetworkManagerFakePlayer() {
            super(false);
        }

        public void channelActive(ChannelHandlerContext p_channelActive_1_) throws Exception {
        }

        public void func_150723_a(EnumConnectionState p_150723_1_) {
        }

        public void channelInactive(ChannelHandlerContext p_channelInactive_1_) {
        }

        public void exceptionCaught(ChannelHandlerContext p_exceptionCaught_1_, Throwable p_exceptionCaught_2_) {
        }

        protected void channelRead0(ChannelHandlerContext p_channelRead0_1_, Packet p_channelRead0_2_) {
        }

        public void func_150719_a(INetHandler p_150719_1_) {
        }

        public void func_150725_a(Packet p_150725_1_, GenericFutureListener... p_150725_2_) {
        }

        public void func_74428_b() {
        }

        public SocketAddress func_74430_c() {
            return null;
        }

        public void func_150718_a(IChatComponent p_150718_1_) {
        }

        public boolean func_150731_c() {
            return false;
        }

        public void func_150727_a(SecretKey p_150727_1_) {
        }

        public boolean func_150724_d() {
            return false;
        }

        public INetHandler func_150729_e() {
            return null;
        }

        public IChatComponent func_150730_f() {
            return null;
        }

        public void func_150721_g() {
        }

        protected void channelRead0(ChannelHandlerContext p_channelRead0_1_, Object p_channelRead0_2_) {
        }

        public Channel channel() {
            return null;
        }
    }
}
