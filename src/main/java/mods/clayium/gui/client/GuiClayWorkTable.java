package mods.clayium.gui.client;

import codechicken.nei.recipe.GuiCraftingRecipe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.List;
import mods.clayium.block.tile.TileClayWorkTable;
import mods.clayium.core.ClayiumCore;
import mods.clayium.gui.GuiPictureButton;
import mods.clayium.gui.container.ContainerClayWorkTable;
import mods.clayium.network.GuiButtonPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiClayWorkTable extends GuiContainer {
    public static final ResourceLocation TEXTURE = new ResourceLocation("clayium", "textures/gui/clayworktable.png");
    private TileClayWorkTable tileClayWorkTable;

    public GuiClayWorkTable(int x, int y, int z) {
        super(new ContainerClayWorkTable(x, y, z));
    }

    public GuiClayWorkTable(InventoryPlayer invPlayer, TileClayWorkTable tile) {
        super(new ContainerClayWorkTable(invPlayer, tile));
        this.tileClayWorkTable = tile;
    }

    protected void func_146979_b(int mouseX, int mouseZ) {
        super.func_146979_b(mouseX, mouseZ);
        String string = this.tileClayWorkTable.func_145818_k_()
            ? this.tileClayWorkTable.func_145825_b()
            : I18n.func_135052_a(this.tileClayWorkTable.func_145825_b(), new Object[0]);
        this.field_146289_q.func_78276_b(string, 6, 6, 4210752);
        this.field_146289_q.func_78276_b(I18n.func_135052_a("container.inventory", new Object[0]), 8, this.field_147000_g - 94, 4210752);
    }

    protected void func_146976_a(float partialTick, int mouseX, int mouseZ) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.field_146297_k.func_110434_K().func_110577_a(TEXTURE);
        int k = (this.field_146294_l - this.field_146999_f) / 2;
        int l = (this.field_146295_m - this.field_147000_g) / 2;
        this.func_73729_b(k, l, 0, 0, this.field_146999_f, this.field_147000_g);
        if (this.tileClayWorkTable.isBurning()) {
            int i1 = 6;
            this.func_73729_b(k + 56, l + 36 + 12 - i1, 176, 12 - i1, 14, i1 + 2);
        }

        int i1 = this.tileClayWorkTable.getCookProgressScaled(80);
        this.func_73729_b(k + 48, l + 29, 176, 0, i1, 16);

        for (int i = 0; i < 6; i++) {
            ((GuiPictureButton)this.field_146292_n.get(i)).field_146124_l = this.tileClayWorkTable.canPushButton(i + 1) != 0;
        }
    }

    public boolean func_73868_f() {
        return false;
    }

    public void func_73866_w_() {
        super.func_73866_w_();
        int k = (this.field_146294_l - this.field_146999_f) / 2;
        int l = (this.field_146295_m - this.field_147000_g) / 2;
        this.field_146292_n.add(new GuiPictureButton(1, k + 40, l + 52, 16, 16, TEXTURE, 176, 32));
        this.field_146292_n.add(new GuiPictureButton(2, k + 56, l + 52, 16, 16, TEXTURE, 192, 32));
        this.field_146292_n.add(new GuiPictureButton(3, k + 72, l + 52, 16, 16, TEXTURE, 208, 32));
        this.field_146292_n.add(new GuiPictureButton(4, k + 88, l + 52, 16, 16, TEXTURE, 224, 32));
        this.field_146292_n.add(new GuiPictureButton(5, k + 104, l + 52, 16, 16, TEXTURE, 240, 32));
        this.field_146292_n.add(new GuiPictureButton(6, k + 120, l + 52, 16, 16, TEXTURE, 176, 80));
    }

    protected void func_146284_a(GuiButton guibutton) {
        switch (guibutton.field_146127_k) {
            case 1:
            case 2:
            default:
                if (guibutton.field_146124_l) {
                    ClayiumCore.packetDispatcher
                        .sendToServer(
                            new GuiButtonPacket(
                                this.tileClayWorkTable.field_145851_c,
                                this.tileClayWorkTable.field_145848_d,
                                this.tileClayWorkTable.field_145849_e,
                                guibutton.field_146127_k
                            )
                        );
                    this.field_146297_k.field_71442_b.func_78756_a(this.field_147002_h.field_75152_c, guibutton.field_146127_k);
                }
        }
    }

    public void func_73864_a(int mousex, int mousey, int button) {
        if (ClayiumCore.IntegrationID.NEI.loaded()) {
            int offsetX = (this.field_146294_l - this.field_146999_f) / 2;
            int offsetY = (this.field_146295_m - this.field_147000_g) / 2;
            int progressBarPosX = 48;
            int progressBarPosY = 33;
            int progressBarSizeX = 80;
            int progressBarSizeY = 12;
            if (offsetX + progressBarPosX <= mousex
                && offsetY + progressBarPosY <= mousey
                && offsetX + progressBarPosX + progressBarSizeX > mousex
                && offsetY + progressBarPosY + progressBarSizeY > mousey) {
                GuiCraftingRecipe.openRecipeGui("ClayWorkTable", new Object[0]);
                return;
            }
        }

        super.func_73864_a(mousex, mousey, button);
    }

    public void func_73863_a(int mousex, int mousey, float p_73863_3_) {
        super.func_73863_a(mousex, mousey, p_73863_3_);
        if (ClayiumCore.IntegrationID.NEI.loaded()) {
            int offsetX = (this.field_146294_l - this.field_146999_f) / 2;
            int offsetY = (this.field_146295_m - this.field_147000_g) / 2;
            int progressBarPosX = 48;
            int progressBarPosY = 33;
            int progressBarSizeX = 80;
            int progressBarSizeY = 12;
            if (offsetX + progressBarPosX <= mousex
                && offsetY + progressBarPosY <= mousey
                && offsetX + progressBarPosX + progressBarSizeX > mousex
                && offsetY + progressBarPosY + progressBarSizeY > mousey) {
                List list = new ArrayList();
                list.add("Recipes");
                this.func_146283_a(list, mousex, mousey);
            }
        }
    }
}
