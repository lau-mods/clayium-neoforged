package mods.clayium.gui;

import net.minecraft.client.gui.Gui;

public interface ITexture {
    int getSizeX();

    int getSizeY();

    void draw(Gui var1, int var2, int var3);
}
