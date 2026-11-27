package mods.clayium.gui.client;

import java.util.ArrayList;
import java.util.List;
import mods.clayium.core.ClayiumCore;
import mods.clayium.gui.SlotWithTexture;
import mods.clayium.gui.container.ContainerTemp;
import mods.clayium.network.GuiTextFieldPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class GuiTemp extends GuiContainer {
    protected static ResourceLocation PlayerInventoryTexture = new ResourceLocation("clayium", "textures/gui/playerinventory.png");
    protected static ResourceLocation DefaultBackTexture = new ResourceLocation("clayium", "textures/gui/back.png");
    protected static ResourceLocation ButtonTexture = new ResourceLocation("clayium", "textures/gui/button.png");
    private boolean initTextureSize = false;
    private int tlw;
    private int tlh;
    private int trw;
    private int trh;
    private int blw;
    private int blh;
    private int brw;
    private int brh;
    private int th;
    private int bh;
    private int lw;
    private int rw;
    protected static ResourceLocation DefaultTextureBack = new ResourceLocation("clayium", "textures/gui/gui_back.png");
    protected static ResourceLocation DefaultTextureTop = new ResourceLocation("clayium", "textures/gui/gui_t.png");
    protected static ResourceLocation DefaultTextureBottom = new ResourceLocation("clayium", "textures/gui/gui_b.png");
    protected static ResourceLocation DefaultTextureLeft = new ResourceLocation("clayium", "textures/gui/gui_l.png");
    protected static ResourceLocation DefaultTextureRight = new ResourceLocation("clayium", "textures/gui/gui_r.png");
    protected static ResourceLocation DefaultTextureTopLeft = new ResourceLocation("clayium", "textures/gui/gui_tl.png");
    protected static ResourceLocation DefaultTextureTopRight = new ResourceLocation("clayium", "textures/gui/gui_tr.png");
    protected static ResourceLocation DefaultTextureBottomLeft = new ResourceLocation("clayium", "textures/gui/gui_bl.png");
    protected static ResourceLocation DefaultTextureBottomRight = new ResourceLocation("clayium", "textures/gui/gui_br.png");
    protected static ResourceLocation DefaultTexturePlayer = new ResourceLocation("clayium", "textures/gui/gui_playerinventory.png");
    public ResourceLocation backTexture = null;
    public ResourceLocation overlayTexture = null;
    protected String guiTitle = "";
    protected List<GuiTextField> textFields = new ArrayList<>();
    protected boolean keyEvents = false;

    public GuiTemp(ContainerTemp container) {
        super(container);
        ContainerTemp containerTemp = (ContainerTemp)this.field_147002_h;
        this.field_146999_f = containerTemp.machineGuiSizeX;
        this.field_147000_g = containerTemp.machineGuiSizeY + 94;
        this.setGuiTitle(container.getInventoryName());
    }

    public void setGuiTitle(String guiTitle) {
        this.guiTitle = guiTitle;
    }

    protected void func_146979_b(int mouseX, int mouseZ) {
        super.func_146979_b(mouseX, mouseZ);
        ContainerTemp container = (ContainerTemp)this.field_147002_h;
        if (this.drawInventoryName()) {
            this.field_146289_q.func_78276_b(this.guiTitle, 6, 6, 4210752);
        }

        if (this.drawPlayerInventoryName()) {
            this.field_146289_q
                .func_78276_b(I18n.func_135052_a("container.inventory", new Object[0]), container.playerSlotOffsetX + 8, container.playerSlotOffsetY, 4210752);
        }
    }

    private int getTextureWidth() {
        return GL11.glGetTexLevelParameteri(3553, 0, 4096);
    }

    private int getTextureHeight() {
        return GL11.glGetTexLevelParameteri(3553, 0, 4097);
    }

    public void drawTexture(int p_146110_0_, int p_146110_1_, int p_146110_4_, int p_146110_5_) {
        func_146110_a(p_146110_0_, p_146110_1_, 0.0F, 0.0F, p_146110_4_, p_146110_5_, (float)this.getTextureWidth(), (float)this.getTextureHeight());
    }

    public boolean drawInventoryName() {
        return ((ContainerTemp)this.field_147002_h).drawInventoryName();
    }

    public boolean drawPlayerInventoryName() {
        return ((ContainerTemp)this.field_147002_h).drawInventoryName();
    }

    protected void func_146976_a(float partialTick, int mouseX, int mouseZ) {
        ContainerTemp container = (ContainerTemp)this.field_147002_h;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int offsetX = (this.field_146294_l - this.field_146999_f) / 2;
        int offsetY = (this.field_146295_m - this.field_147000_g) / 2;
        if (this.backTexture == null) {
            if (!this.initTextureSize) {
                this.field_146297_k.func_110434_K().func_110577_a(DefaultTextureTopLeft);
                this.tlw = this.getTextureWidth();
                this.tlh = this.getTextureHeight();
                this.field_146297_k.func_110434_K().func_110577_a(DefaultTextureTopRight);
                this.trw = this.getTextureWidth();
                this.trh = this.getTextureHeight();
                this.field_146297_k.func_110434_K().func_110577_a(DefaultTextureBottomLeft);
                this.blw = this.getTextureWidth();
                this.blh = this.getTextureHeight();
                this.field_146297_k.func_110434_K().func_110577_a(DefaultTextureBottomRight);
                this.brw = this.getTextureWidth();
                this.brh = this.getTextureHeight();
                this.field_146297_k.func_110434_K().func_110577_a(DefaultTextureTop);
                this.th = this.getTextureHeight();
                this.field_146297_k.func_110434_K().func_110577_a(DefaultTextureBottom);
                this.bh = this.getTextureHeight();
                this.field_146297_k.func_110434_K().func_110577_a(DefaultTextureLeft);
                this.lw = this.getTextureWidth();
                this.field_146297_k.func_110434_K().func_110577_a(DefaultTextureRight);
                this.rw = this.getTextureWidth();
                this.initTextureSize = true;
            }

            this.field_146297_k.func_110434_K().func_110577_a(DefaultTextureBack);
            this.drawTexture(offsetX + this.lw, offsetY + this.th, this.field_146999_f - this.lw - this.rw, this.field_147000_g - this.th - this.bh);
            this.field_146297_k.func_110434_K().func_110577_a(DefaultTextureTop);
            this.drawTexture(offsetX + this.tlw, offsetY, this.field_146999_f - this.tlw - this.trw, this.th);
            this.field_146297_k.func_110434_K().func_110577_a(DefaultTextureBottom);
            this.drawTexture(offsetX + this.blw, offsetY + this.field_147000_g - this.bh, this.field_146999_f - this.blw - this.brw, this.bh);
            this.field_146297_k.func_110434_K().func_110577_a(DefaultTextureLeft);
            this.drawTexture(offsetX, offsetY + this.tlh, this.lw, this.field_147000_g - this.tlh - this.blh);
            this.field_146297_k.func_110434_K().func_110577_a(DefaultTextureRight);
            this.drawTexture(offsetX + this.field_146999_f - this.rw, offsetY + this.trh, this.rw, this.field_147000_g - this.trh - this.brh);
            this.field_146297_k.func_110434_K().func_110577_a(DefaultTextureTopLeft);
            this.drawTexture(offsetX, offsetY, this.tlw, this.tlh);
            this.field_146297_k.func_110434_K().func_110577_a(DefaultTextureTopRight);
            this.drawTexture(offsetX + this.field_146999_f - this.trw, offsetY, this.trw, this.trh);
            this.field_146297_k.func_110434_K().func_110577_a(DefaultTextureBottomLeft);
            this.drawTexture(offsetX, offsetY + this.field_147000_g - this.blh, this.blw, this.blh);
            this.field_146297_k.func_110434_K().func_110577_a(DefaultTextureBottomRight);
            this.drawTexture(offsetX + this.field_146999_f - this.brw, offsetY + this.field_147000_g - this.brh, this.brw, this.brh);
            this.field_146297_k.func_110434_K().func_110577_a(DefaultTexturePlayer);
            this.drawTexture(offsetX + container.playerSlotOffsetX, offsetY + container.playerSlotOffsetY, 176, 94);
        } else {
            this.field_146297_k.func_110434_K().func_110577_a(this.backTexture == null ? DefaultBackTexture : this.backTexture);
            this.func_73729_b(offsetX, offsetY, 0, 0, container.machineGuiSizeX, container.machineGuiSizeY);
            this.field_146297_k.func_110434_K().func_110577_a(PlayerInventoryTexture);
            this.func_73729_b(offsetX + container.playerSlotOffsetX, offsetY + container.playerSlotOffsetY, 0, 0, 176, 94);
        }

        for (int i = 0; i < container.machineInventorySlots.size(); i++) {
            Slot slot = container.machineInventorySlots.get(i);
            if (slot instanceof SlotWithTexture) {
                ((SlotWithTexture)slot).draw(this, offsetX, offsetY);
            } else {
                ResourceLocation SlotTexture = new ResourceLocation("clayium", "textures/gui/slot.png");
                int slotSizeX = 18;
                int slotSizeY = 18;
                int slotTextureX = 0;
                int slotTextureY = 0;
                this.field_146297_k.func_110434_K().func_110577_a(SlotTexture);
                this.func_73729_b(
                    offsetX + slot.field_75223_e - (slotSizeX - 16) / 2,
                    offsetY + slot.field_75221_f - (slotSizeY - 16) / 2,
                    slotTextureX,
                    slotTextureY,
                    slotSizeX,
                    slotSizeY
                );
            }
        }

        if (this.overlayTexture != null) {
            this.field_146297_k.func_110434_K().func_110577_a(this.overlayTexture);
            this.func_73729_b(offsetX, offsetY, 0, 0, container.machineGuiSizeX, container.machineGuiSizeY);
        }
    }

    public void func_73863_a(int p_73863_1_, int p_73863_2_, float p_73863_3_) {
        super.func_73863_a(p_73863_1_, p_73863_2_, p_73863_3_);
        GL11.glDisable(2896);
        GL11.glDisable(3042);

        for (GuiTextField textField : this.textFields) {
            textField.func_146194_f();
        }
    }

    public void func_73866_w_() {
        super.func_73866_w_();
        this.addButton();
        this.addTextField();
        if (this.textFields != null && this.textFields.size() != 0) {
            this.keyEvents = true;
            Keyboard.enableRepeatEvents(true);
        }
    }

    public void addButton() {
    }

    public void addTextField() {
    }

    protected void keyTyped(char c, int key, int id) {
    }

    public void addTextFieldToList(GuiTextField textField) {
        String s = ((ContainerTemp)this.field_147002_h).getTextFieldString(this.field_146297_k.field_71439_g, this.textFields.size());
        textField.func_146180_a(s == null ? "" : s);
        this.textFields.add(textField);
    }

    public void func_146281_b() {
        super.func_146281_b();
        if (this.keyEvents) {
            Keyboard.enableRepeatEvents(false);
        }
    }

    protected void func_73869_a(char c, int key) {
        boolean flag = true;

        for (int i = 0; i < this.textFields.size(); i++) {
            GuiTextField textField = this.textFields.get(i);
            if (textField.func_146201_a(c, key)) {
                ClayiumCore.packetDispatcher.sendToServer(new GuiTextFieldPacket(textField.func_146179_b(), i));
                this.keyTyped(c, key, i);
                flag = false;
            }
        }

        if (flag) {
            super.func_73869_a(c, key);
        }
    }

    public void setTextFieldString(String string, int id, boolean sendToContainer) {
        if (id >= 0 && id < this.textFields.size()) {
            GuiTextField textField = this.textFields.get(id);
            textField.func_146180_a(string);
            if (sendToContainer) {
                ContainerTemp container = (ContainerTemp)this.field_147002_h;
                container.setTextFieldString(Minecraft.func_71410_x().field_71439_g, string, id);
            }
        }
    }

    protected void func_73864_a(int p_73864_1_, int p_73864_2_, int p_73864_3_) {
        super.func_73864_a(p_73864_1_, p_73864_2_, p_73864_3_);

        for (GuiTextField textField : this.textFields) {
            textField.func_146192_a(p_73864_1_, p_73864_2_, p_73864_3_);
        }
    }

    public boolean func_73868_f() {
        return false;
    }

    protected void func_146284_a(GuiButton guibutton) {
        this.field_146297_k.field_71442_b.func_78756_a(this.field_147002_h.field_75152_c, guibutton.field_146127_k);
        ((ContainerTemp)this.field_147002_h).pushClientButton(Minecraft.func_71410_x().field_71439_g, guibutton.field_146127_k);
    }
}
