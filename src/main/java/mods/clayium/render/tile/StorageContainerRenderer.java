package mods.clayium.render.tile;

import mods.clayium.block.tile.TileStorageContainer;
import mods.clayium.core.ClayiumCore;
import mods.clayium.util.UtilDirection;
import mods.clayium.util.UtilLocale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;

public class StorageContainerRenderer extends TileEntitySpecialRenderer {
    public StorageContainerRenderer() {
        super();
    }

    public void func_147500_a(TileEntity tile, double x, double y, double z, float partialTicktime) {
        if (tile != null && tile instanceof TileStorageContainer) {
            ItemStack itemstack = ((TileStorageContainer)tile).containerItemStacks[0];
            int stackSize = 0;
            if (itemstack == null) {
                itemstack = ((TileStorageContainer)tile).containerItemStacks[1];
            } else {
                stackSize = itemstack.field_77994_a;
            }

            if (itemstack != null) {
                GL11.glPushMatrix();
                GL11.glTranslatef((float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F);
                UtilDirection direction = UtilDirection.getOrientation(((TileStorageContainer)tile).getFrontDirection());
                switch (direction) {
                    case NORTH:
                        GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                        break;
                    case SOUTH:
                        GL11.glRotatef(0.0F, 0.0F, 1.0F, 0.0F);
                        break;
                    case WEST:
                        GL11.glRotatef(270.0F, 0.0F, 1.0F, 0.0F);
                        break;
                    case EAST:
                        GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
                }

                if (MinecraftForgeClient.getRenderPass() == 0) {
                    EntityItem entityitem = new EntityItem(tile.func_145831_w(), 0.0, 0.0, 0.0, itemstack.func_77946_l());
                    entityitem.func_92059_d().field_77994_a = 1;
                    entityitem.field_70290_d = 0.0F;
                    GL11.glPushMatrix();
                    GL11.glTranslatef(0.0F, -0.03F, 0.51F);
                    RenderItem.field_82407_g = true;
                    RenderManager.field_78727_a.func_147940_a(entityitem, 0.0, 0.0, 0.0, 0.0F, 0.0F);
                    RenderItem.field_82407_g = false;
                    GL11.glPopMatrix();
                }

                if (MinecraftForgeClient.getRenderPass() == 1) {
                    String s = String.valueOf(UtilLocale.StackSizeNumeral((long)stackSize));
                    FontRenderer fontrenderer = RenderManager.field_78727_a.func_78716_a();
                    GL11.glPushMatrix();
                    GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glTranslatef(0.0F, -0.15F, -0.55F);
                    GL11.glNormal3f(0.0F, 1.0F, 0.0F);
                    float f = 1.6F;
                    float f1 = 0.016666668F * f;
                    GL11.glScalef(-f1, -f1, f1);
                    GL11.glDisable(2896);
                    GL11.glTranslatef(0.0F, 0.0F, -0.01F);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    fontrenderer.func_78276_b(s, -fontrenderer.func_78256_a(s) / 2, 0, -603979776);
                    MovingObjectPosition p = Minecraft.func_71410_x().field_71451_h.func_70614_a(9999.0, 0.0F);
                    boolean b = p.field_72313_a == MovingObjectType.BLOCK
                        && p.field_72311_b == tile.field_145851_c
                        && p.field_72312_c == tile.field_145848_d
                        && p.field_72309_d == tile.field_145849_e;
                    if (ClayiumCore.proxy.renderAsPipingMode() || b) {
                        GL11.glTranslatef(0.0F, -20.0F, -5.0F);
                        f1 = 0.5F;
                        GL11.glScalef(f1, f1, f1);
                        s = itemstack.func_82833_r();
                        int i = fontrenderer.func_78256_a(s);
                        float f2 = i > 64 ? 64.0F / (float)i : 1.0F;
                        GL11.glScalef(f2, 1.0F, 1.0F);
                        GL11.glEnable(3042);
                        GL11.glBlendFunc(770, 771);
                        GL11.glEnable(32823);
                        GL11.glPolygonOffset(0.1F, 1.0F);
                        Tessellator tessellator = Tessellator.field_78398_a;
                        GL11.glDisable(3553);
                        tessellator.func_78382_b();
                        i /= 2;
                        tessellator.func_78369_a(0.0F, 0.0F, 0.0F, 0.5F);
                        tessellator.func_78377_a((double)(-i - 1), -1.0, 0.0);
                        tessellator.func_78377_a((double)(-i - 1), 8.0, 0.0);
                        tessellator.func_78377_a((double)(i + 1), 8.0, 0.0);
                        tessellator.func_78377_a((double)(i + 1), -1.0, 0.0);
                        tessellator.func_78381_a();
                        GL11.glDisable(32823);
                        GL11.glEnable(3553);
                        fontrenderer.func_78276_b(s, -fontrenderer.func_78256_a(s) / 2, 0, -1);
                    }

                    GL11.glEnable(2896);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    GL11.glPopMatrix();
                }

                GL11.glPopMatrix();
            }
        }
    }
}
