package mods.clayium.core;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import java.lang.reflect.Field;
import java.util.Map.Entry;
import mods.clayium.block.CBlocks;
import mods.clayium.block.tile.TileAreaActivator;
import mods.clayium.block.tile.TileAreaCollector;
import mods.clayium.block.tile.TileAreaMiner;
import mods.clayium.block.tile.TileCAReactor;
import mods.clayium.block.tile.TileClayContainerInterface;
import mods.clayium.block.tile.TileClayEnergyLaser;
import mods.clayium.block.tile.TileClayLaserInterface;
import mods.clayium.block.tile.TileClayMarker;
import mods.clayium.block.tile.TileClayOpenPitMarker;
import mods.clayium.block.tile.TileLaserReflector;
import mods.clayium.block.tile.TileMetalChest;
import mods.clayium.block.tile.TileRedstoneInterface;
import mods.clayium.block.tile.TileStorageContainer;
import mods.clayium.block.tile.TileVacuumContainer;
import mods.clayium.entity.EntityClayBall;
import mods.clayium.entity.EntityTeleportBall;
import mods.clayium.item.CItems;
import mods.clayium.item.CMaterial;
import mods.clayium.item.CMaterials;
import mods.clayium.item.CShape;
import mods.clayium.item.ClaySteelPickaxe;
import mods.clayium.item.ClaySteelPickaxeClient;
import mods.clayium.item.ItemCapsule;
import mods.clayium.item.ItemDamaged;
import mods.clayium.item.gadget.GadgetLongArm;
import mods.clayium.misc.RenderFluidTab;
import mods.clayium.misc.TileEntityFluidTabRenderer;
import mods.clayium.misc.TileFluidTab;
import mods.clayium.network.ClaySteelPickaxePacket;
import mods.clayium.plugin.nei.LoadNEIConfig;
import mods.clayium.render.block.RenderClayContainer;
import mods.clayium.render.block.RenderLaserReflector;
import mods.clayium.render.block.RenderMetalChest;
import mods.clayium.render.block.RenderPANCable;
import mods.clayium.render.block.RenderQuartzCrucible;
import mods.clayium.render.item.ItemDamagedRenderer;
import mods.clayium.render.tile.AreaMachineRenderer;
import mods.clayium.render.tile.CAReactorRenderer;
import mods.clayium.render.tile.ClayLaserRenderer;
import mods.clayium.render.tile.ClayMarkerRenderer;
import mods.clayium.render.tile.InterfaceRenderer;
import mods.clayium.render.tile.MetalChestRenderer;
import mods.clayium.render.tile.StorageContainerRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovementInput;
import net.minecraft.world.World;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;

public class ClayiumClientProxy extends ClayiumCommonProxy {
    Field blockHitDelay = null;
    Field rightClickDelayTimer = null;
    float mode1velocity = 0.7F;
    float mode2acceleration = 0.9F;
    float mode2division = 1.1F;

    public ClayiumClientProxy() {
        super();
    }

    public void registerTileEntity() {
        if (!ClayiumCore.cfgUtilityMode) {
            RenderingRegistry.registerBlockHandler(new RenderFluidTab());
            RenderingRegistry.registerBlockHandler(new RenderClayContainer());
            RenderingRegistry.registerBlockHandler(new RenderQuartzCrucible());
            RenderingRegistry.registerBlockHandler(new RenderLaserReflector());
            RenderingRegistry.registerBlockHandler(new RenderPANCable());
            RenderingRegistry.registerBlockHandler(new RenderMetalChest());
            ClientRegistry.bindTileEntitySpecialRenderer(TileFluidTab.class, new TileEntityFluidTabRenderer());
            ClientRegistry.bindTileEntitySpecialRenderer(TileClayEnergyLaser.class, new ClayLaserRenderer());
            ClientRegistry.bindTileEntitySpecialRenderer(TileLaserReflector.class, new ClayLaserRenderer());
            ClientRegistry.bindTileEntitySpecialRenderer(TileStorageContainer.class, new StorageContainerRenderer());
            ClientRegistry.bindTileEntitySpecialRenderer(TileVacuumContainer.class, new StorageContainerRenderer());
            ClientRegistry.bindTileEntitySpecialRenderer(TileClayMarker.class, new ClayMarkerRenderer());
            ClientRegistry.bindTileEntitySpecialRenderer(TileClayOpenPitMarker.class, new ClayMarkerRenderer());
            ClientRegistry.bindTileEntitySpecialRenderer(TileAreaMiner.class, new AreaMachineRenderer());
            ClientRegistry.bindTileEntitySpecialRenderer(TileAreaCollector.class, new AreaMachineRenderer());
            ClientRegistry.bindTileEntitySpecialRenderer(TileAreaActivator.class, new AreaMachineRenderer());
            ClientRegistry.bindTileEntitySpecialRenderer(TileCAReactor.class, new CAReactorRenderer());
            ClientRegistry.bindTileEntitySpecialRenderer(TileMetalChest.class, MetalChestRenderer.INSTANCE);
            ClientRegistry.bindTileEntitySpecialRenderer(TileClayContainerInterface.class, new InterfaceRenderer());
            ClientRegistry.bindTileEntitySpecialRenderer(TileClayLaserInterface.class, new InterfaceRenderer());
            ClientRegistry.bindTileEntitySpecialRenderer(TileRedstoneInterface.class, new InterfaceRenderer());
        } else {
            RenderingRegistry.registerBlockHandler(new RenderClayContainer());
        }
    }

    public void registerRenderer() {
        RenderingRegistry.registerEntityRenderingHandler(EntityClayBall.class, new RenderSnowball(Items.field_151119_aD));
        RenderingRegistry.registerEntityRenderingHandler(EntityTeleportBall.class, new RenderSnowball(Items.field_151079_bi));
        IItemRenderer renderer = new ItemDamagedRenderer();

        for (Entry<CMaterial, ItemDamaged> entry : CMaterials.materialMap.entrySet()) {
            MinecraftForgeClient.registerItemRenderer(entry.getValue(), renderer);
        }

        for (Entry<CShape, ItemDamaged> entry : CMaterials.shapeMap.entrySet()) {
            MinecraftForgeClient.registerItemRenderer(entry.getValue(), renderer);
        }

        if (ClayiumCore.cfgEnableFluidCapsule) {
            for (ItemCapsule item : CItems.itemsCapsule) {
                MinecraftForgeClient.registerItemRenderer(item, renderer);
            }
        }
    }

    public int getRenderID() {
        return RenderingRegistry.getNextAvailableRenderId();
    }

    public World getClientWorld() {
        return FMLClientHandler.instance().getClient().field_71441_e;
    }

    public void LoadNEI() {
        if (ClayiumCore.IntegrationID.NEI.loaded()) {
            try {
                LoadNEIConfig.load();
            } catch (Exception var2) {
                var2.printStackTrace(System.err);
            }
        }
    }

    public ClaySteelPickaxe newClaySteelPickaxe() {
        return new ClaySteelPickaxeClient();
    }

    public int getHittingSide(EntityPlayer player) {
        return player.func_130014_f_().field_72995_K && player == Minecraft.func_71410_x().field_71439_g ? this.sideHit() : super.getHittingSide(player);
    }

    public void updateHittingSide(EntityPlayer player) {
        if (player.field_70170_p.field_72995_K && this.getClientPlayer() == player) {
            ClayiumCore.packetDispatcher.sendToServer(new ClaySteelPickaxePacket(this.sideHit()));
        }
    }

    private int sideHit() {
        return Minecraft.func_71410_x().field_71451_h.func_70614_a(9999.0, 0.0F).field_72310_e;
    }

    public EntityPlayer getClientPlayer() {
        return Minecraft.func_71410_x().field_71439_g;
    }

    public boolean renderAsPipingMode() {
        EntityPlayer player = this.getClientPlayer();
        ItemStack item;
        return player != null && (item = player.func_71045_bC()) != null && item.func_77973_b() == CItems.itemClayPipingTools;
    }

    public void clientPlayerTick(EntityPlayer player) {
        boolean flag = false;
        int s = 5;

        for (int i = 0; i < 9; i++) {
            ItemStack item = player.field_71071_by.field_70462_a[i];
            if (item != null && item.func_77973_b() instanceof ItemBlock && ((ItemBlock)item.func_77973_b()).field_150939_a == CBlocks.blockOverclocker) {
                flag |= true;
                switch (item.func_77960_j()) {
                    case 10:
                        s = Math.min(s, 3);
                        break;
                    case 11:
                        s = Math.min(s, 2);
                        break;
                    case 12:
                        s = Math.min(s, 1);
                        break;
                    case 13:
                        s = Math.min(s, 0);
                }
            }
        }

        if (flag) {
            this.overclockPlayer(s);
        }
    }

    public void overclockPlayer(int delay) {
        if (this.blockHitDelay == null) {
            Class classPlayerController = null;

            try {
                classPlayerController = Class.forName("net.minecraft.client.multiplayer.PlayerControllerMP");
            } catch (ClassNotFoundException var24) {
                try {
                    classPlayerController = Class.forName("net.minecraft.bje");
                } catch (ClassNotFoundException var23) {
                    ClayiumCore.logger.catching(var23);
                }
            }

            if (classPlayerController != null) {
                try {
                    this.blockHitDelay = classPlayerController.getDeclaredField("field_78781_i");
                } catch (NoSuchFieldException var21) {
                    ClayiumCore.logger.info(var21.getMessage());
                } catch (SecurityException var22) {
                    ClayiumCore.logger.catching(var22);
                }

                if (this.blockHitDelay == null) {
                    try {
                        this.blockHitDelay = classPlayerController.getDeclaredField("blockHitDelay");
                    } catch (NoSuchFieldException var19) {
                        ClayiumCore.logger.info(var19.getMessage());
                    } catch (SecurityException var20) {
                        ClayiumCore.logger.catching(var20);
                    }
                }

                if (this.blockHitDelay == null) {
                    try {
                        this.blockHitDelay = classPlayerController.getDeclaredField("i");
                    } catch (NoSuchFieldException var17) {
                        ClayiumCore.logger.info(var17.getMessage());
                    } catch (SecurityException var18) {
                        ClayiumCore.logger.catching(var18);
                    }
                }

                if (this.blockHitDelay != null) {
                    this.blockHitDelay.setAccessible(true);
                }
            }
        }

        if (this.rightClickDelayTimer == null) {
            Class classMinecraft = null;

            try {
                classMinecraft = Class.forName("net.minecraft.client.Minecraft");
            } catch (ClassNotFoundException var16) {
                try {
                    classMinecraft = Class.forName("net.minecraft.bao");
                } catch (ClassNotFoundException var15) {
                    ClayiumCore.logger.catching(var15);
                }
            }

            if (classMinecraft != null) {
                try {
                    this.rightClickDelayTimer = classMinecraft.getDeclaredField("field_71467_ac");
                } catch (NoSuchFieldException var13) {
                    ClayiumCore.logger.info(var13.getMessage());
                } catch (SecurityException var14) {
                    ClayiumCore.logger.catching(var14);
                }

                if (this.rightClickDelayTimer == null) {
                    try {
                        this.rightClickDelayTimer = classMinecraft.getDeclaredField("rightClickDelayTimer");
                    } catch (NoSuchFieldException var11) {
                        ClayiumCore.logger.info(var11.getMessage());
                    } catch (SecurityException var12) {
                        ClayiumCore.logger.catching(var12);
                    }
                }

                if (this.rightClickDelayTimer == null) {
                    try {
                        this.rightClickDelayTimer = classMinecraft.getDeclaredField("ad");
                    } catch (NoSuchFieldException var9) {
                        ClayiumCore.logger.info(var9.getMessage());
                    } catch (SecurityException var10) {
                        ClayiumCore.logger.catching(var10);
                    }
                }

                if (this.rightClickDelayTimer != null) {
                    this.rightClickDelayTimer.setAccessible(true);
                }
            }
        }

        if (this.blockHitDelay != null) {
            try {
                int i = this.blockHitDelay.getInt(Minecraft.func_71410_x().field_71442_b);
                if (i >= delay) {
                    this.blockHitDelay.setInt(Minecraft.func_71410_x().field_71442_b, delay);
                }
            } catch (IllegalArgumentException var7) {
                ClayiumCore.logger.catching(var7);
            } catch (IllegalAccessException var8) {
                ClayiumCore.logger.catching(var8);
            }
        }

        if (this.rightClickDelayTimer != null) {
            try {
                int i = this.rightClickDelayTimer.getInt(Minecraft.func_71410_x());
                if (i >= delay + 1) {
                    this.rightClickDelayTimer.setInt(Minecraft.func_71410_x(), delay + 1);
                }
            } catch (IllegalArgumentException var5) {
                ClayiumCore.logger.catching(var5);
            } catch (IllegalAccessException var6) {
                ClayiumCore.logger.catching(var6);
            }
        }
    }

    public void updateFlightStatus(int mode) {
        EntityClientPlayerMP player = (EntityClientPlayerMP)this.getClientPlayer();
        if (mode != -1 && mode >= 1 && player.field_71075_bZ.field_75100_b) {
            MovementInput mi = player.field_71158_b;
            GameSettings settings = Minecraft.func_71410_x().field_71474_y;
            float sin = MathHelper.func_76126_a(player.field_70177_z * (float) Math.PI / 180.0F);
            float cos = MathHelper.func_76134_b(player.field_70177_z * (float) Math.PI / 180.0F);
            mi.field_78900_b = (float)(player.field_70179_y * (double)cos - player.field_70159_w * (double)sin);
            mi.field_78902_a = (float)(player.field_70179_y * (double)sin + player.field_70159_w * (double)cos);
            if (mi.field_78899_d) {
                if (mode >= 2) {
                    player.field_70181_x = player.field_70181_x - (double)this.mode2acceleration;
                    player.field_70181_x = player.field_70181_x / (double)this.mode2division;
                } else {
                    player.field_70181_x = (double)(-this.mode1velocity);
                }
            }

            if (mi.field_78901_c) {
                if (mode >= 2) {
                    player.field_70181_x = player.field_70181_x + (double)this.mode2acceleration;
                    player.field_70181_x = player.field_70181_x / (double)this.mode2division;
                } else {
                    player.field_70181_x = (double)this.mode1velocity;
                }
            }

            if (mi.field_78901_c == mi.field_78899_d) {
                player.field_70181_x = 0.0;
            }

            if (settings.field_74351_w.func_151470_d()) {
                if (mode >= 2) {
                    mi.field_78900_b = mi.field_78900_b + this.mode2acceleration;
                    mi.field_78900_b = mi.field_78900_b / this.mode2division;
                } else {
                    mi.field_78900_b = this.mode1velocity;
                }
            }

            if (settings.field_74368_y.func_151470_d()) {
                if (mode >= 2) {
                    mi.field_78900_b = mi.field_78900_b - this.mode2acceleration;
                    mi.field_78900_b = mi.field_78900_b / this.mode2division;
                } else {
                    mi.field_78900_b = -this.mode1velocity;
                }
            }

            if (!settings.field_74351_w.func_151470_d() && !settings.field_74368_y.func_151470_d()) {
                mi.field_78900_b = 0.0F;
            }

            if (settings.field_74370_x.func_151470_d()) {
                if (mode >= 2) {
                    mi.field_78902_a = mi.field_78902_a + this.mode2acceleration;
                    mi.field_78902_a = mi.field_78902_a / this.mode2division;
                } else {
                    mi.field_78902_a = this.mode1velocity;
                }
            }

            if (settings.field_74366_z.func_151470_d()) {
                if (mode >= 2) {
                    mi.field_78902_a = mi.field_78902_a - this.mode2acceleration;
                    mi.field_78902_a = mi.field_78902_a / this.mode2division;
                } else {
                    mi.field_78902_a = -this.mode1velocity;
                }
            }

            if (!settings.field_74370_x.func_151470_d() && !settings.field_74366_z.func_151470_d()) {
                mi.field_78902_a = 0.0F;
            }

            player.field_70159_w = (double)(mi.field_78902_a * cos - mi.field_78900_b * sin);
            player.field_70179_y = (double)(mi.field_78900_b * cos + mi.field_78902_a * sin);
        }
    }

    public float hookBlockReachDistance(float distance) {
        return GadgetLongArm.hookBlockReachDistance(distance, this.getClientPlayer());
    }

    public boolean isCreative(EntityPlayer player) {
        return player == this.getClientPlayer() ? Minecraft.func_71410_x().field_71442_b.func_78758_h() : super.isCreative(player);
    }
}
