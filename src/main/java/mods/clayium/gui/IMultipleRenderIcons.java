package mods.clayium.gui;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

public interface IMultipleRenderIcons {
    int getRenderPasses();

    IIcon getIconFromPass(int var1);

    int getColorFromPass(int var1);

    void registerIcons(IIconRegister var1);
}
