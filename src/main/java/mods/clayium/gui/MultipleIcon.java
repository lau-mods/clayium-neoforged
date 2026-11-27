package mods.clayium.gui;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

public class MultipleIcon implements IMultipleRenderIcons {
    int[] colors;
    IIcon[] iicons;
    String[] iconStrings;
    public int pos;

    public MultipleIcon(int passes) {
        super();
        this.colors = new int[passes];
        this.iconStrings = new String[passes];
    }

    public MultipleIcon addIcon(String iconString) {
        return this.setIcon(this.pos, iconString);
    }

    public MultipleIcon setIcon(int i, String iconString) {
        return this.setIcon(i, iconString, 255, 255, 255);
    }

    public MultipleIcon addIcon(String iconString, int r, int g, int b) {
        return this.setIcon(this.pos, iconString, r, g, b);
    }

    public MultipleIcon setIcon(int i, String iconString, int r, int g, int b) {
        return this.setIcon(i, iconString, GenericMaterialIcon.rgb2int(r, g, b));
    }

    public MultipleIcon addIcon(String iconString, int color) {
        return this.setIcon(this.pos, iconString, color);
    }

    public MultipleIcon setIcon(int i, String iconString, int color) {
        this.setColor(i, color);
        this.iconStrings[i] = iconString;
        this.pos++;
        return this;
    }

    public MultipleIcon addIcons(String... iconStrings) {
        for (String string : iconStrings) {
            this.setIcon(this.pos, string);
        }

        return this;
    }

    public MultipleIcon setColor(int i, int r, int g, int b) {
        return this.setColor(i, GenericMaterialIcon.rgb2int(r, g, b));
    }

    public MultipleIcon setColor(int i, int color) {
        this.colors[i] = color;
        return this;
    }

    public int getRenderPasses() {
        return this.iicons.length;
    }

    public IIcon getIconFromPass(int pass) {
        return this.iicons[pass];
    }

    public int getColorFromPass(int pass) {
        return this.colors[pass];
    }

    public void registerIcons(IIconRegister iiconRegister) {
        this.iicons = new IIcon[this.iconStrings.length];

        for (int i = 0; i < this.iicons.length; i++) {
            this.iicons[i] = iiconRegister.func_94245_a("clayium:" + this.iconStrings[i]);
        }
    }
}
