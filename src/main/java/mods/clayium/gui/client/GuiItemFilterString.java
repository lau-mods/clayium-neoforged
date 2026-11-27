package mods.clayium.gui.client;

import mods.clayium.gui.container.ContainerItemFilterString;
import net.minecraft.client.gui.GuiTextField;

public class GuiItemFilterString extends GuiTemp {
    public GuiItemFilterString(ContainerItemFilterString container) {
        super(container);
    }

    public void addTextField() {
        int i = (this.field_146294_l - this.field_146999_f) / 2;
        int j = (this.field_146295_m - this.field_147000_g) / 2;
        GuiTextField textField = new GuiTextField(this.field_146289_q, i + 12, j + 18, this.field_146999_f - 24, 12);
        textField.func_146193_g(-1);
        textField.func_146204_h(-1);
        textField.func_146185_a(true);
        textField.func_146203_f(128);
        this.addTextFieldToList(textField);
    }

    protected void keyTyped(char p_73869_1_, int p_73869_2_, int id) {
        ((ContainerItemFilterString)this.field_147002_h).setFilterString(this.textFields.get(id).func_146179_b());
    }
}
