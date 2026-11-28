package net.claustra01.clayium.client.screen;

import net.claustra01.clayium.menu.ItemFilterStringMenu;
import net.claustra01.clayium.network.FilterTextPayload;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * Screen for String/Pattern-based item filters sending {@link FilterTextPayload} updates on edit.
 */
public class ItemFilterStringScreen extends ClayiumBaseScreen<ItemFilterStringMenu> {
    private EditBox filterBox;

    public ItemFilterStringScreen(ItemFilterStringMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.filterBox = new EditBox(
                this.font,
                this.leftPos + 12,
                this.topPos + 18,
                this.imageWidth - 24,
                12,
                Component.empty()
        );
        this.filterBox.setTextColor(0xFFFFFFFF);
        this.filterBox.setTextColorUneditable(0xFFFFFFFF);
        this.filterBox.setBordered(true);
        this.filterBox.setMaxLength(128);
        this.filterBox.setValue(this.menu.getFilterString());
        this.filterBox.setResponder(text -> PacketDistributor.sendToServer(
                new FilterTextPayload(this.menu.containerId, text)
        ));
        this.filterBox.setFocused(true);
        addRenderableWidget(this.filterBox);
        setInitialFocus(this.filterBox);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (this.filterBox != null && this.filterBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.filterBox != null && this.filterBox.canConsumeInput()) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
