package mods.clayium.gui;

import cpw.mods.fml.client.config.GuiButtonExt;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class GuiPictureButton extends GuiButtonExt {
    protected ITexture[] textures;

    public GuiPictureButton(int id, int xPos, int yPos, int width, int height, String displaystring, ResourceLocation _buttonTextures, int _rXPos, int _rYPos) {
        super(id, xPos, yPos, width, height, displaystring);
        this.setTexture(_buttonTextures, _rXPos, _rYPos);
    }

    public GuiPictureButton(int id, int xPos, int yPos, int width, int height, ResourceLocation _buttonTextures, int _rXPos, int _rYPos) {
        this(id, xPos, yPos, width, height, "", _buttonTextures, _rXPos, _rYPos);
    }

    public void func_146112_a(Minecraft mc, int mouseX, int mouseY) {
        if (this.field_146125_m) {
            this.field_146123_n = mouseX >= this.field_146128_h
                && mouseY >= this.field_146129_i
                && mouseX < this.field_146128_h + this.field_146120_f
                && mouseY < this.field_146129_i + this.field_146121_g;
            int k = this.func_146114_a(this.field_146123_n);
            if (this.textures != null && k >= 0 && k < this.textures.length && this.textures[k] != null) {
                this.textures[k].draw(this, this.field_146128_h, this.field_146129_i);
            }

            this.func_146119_b(mc, mouseX, mouseY);
            int color = 14737632;
            if (this.packedFGColour != 0) {
                color = this.packedFGColour;
            } else if (!this.field_146124_l) {
                color = 10526880;
            } else if (this.field_146123_n) {
                color = 16777120;
            }

            String buttonText = this.field_146126_j;
            int strWidth = mc.field_71466_p.func_78256_a(buttonText);
            int ellipsisWidth = mc.field_71466_p.func_78256_a("...");
            if (strWidth > this.field_146120_f - 6 && strWidth > ellipsisWidth) {
                buttonText = mc.field_71466_p.func_78269_a(buttonText, this.field_146120_f - 6 - ellipsisWidth).trim() + "...";
            }

            this.func_73732_a(
                mc.field_71466_p, buttonText, this.field_146128_h + this.field_146120_f / 2, this.field_146129_i + (this.field_146121_g - 8) / 2, color
            );
        }
    }

    public void setTexture(ITexture tex, int hoverState) {
        if (this.textures == null) {
            this.textures = new ITexture[3];
        }

        if (hoverState >= 0 && hoverState < this.textures.length) {
            this.textures[hoverState] = tex;
        }
    }

    public void setTexture(ResourceLocation _buttonTextures, int _rXPos, int _rYPos, int hoverState) {
        this.setTexture(new RectangleTexture(_buttonTextures, this.field_146120_f, this.field_146121_g, _rXPos, _rYPos), hoverState);
    }

    public void setTexture(ResourceLocation _buttonTextures, int _rXPos, int _rYPos) {
        for (int i = 0; i < 3; i++) {
            this.setTexture(_buttonTextures, _rXPos, _rYPos + this.field_146121_g * i, i);
        }
    }
}
