package net.claustra01.clayium.client.screen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.Nullable;
import net.claustra01.clayium.menu.PANCoreMenu;
import net.claustra01.clayium.network.PANCoreListPayload.Entry;
import net.claustra01.clayium.util.ClayiumFormatting;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

/**
 * Client screen for PAN Core: searchable, sortable, scrollable item ledger with cost/consumption
 * readout, prohibited item highlights, detail mode toggle, and chat dump button.
 */
public class PANCoreScreen extends ClayiumBaseScreen<PANCoreMenu> {
    private static final int TABLE_BG = 0xFF001800;
    private static final int SLIDER_BG = 0xFFA0E6A0;
    private static final int SLIDER_KNOB = 0xFF1E3C1E;
    private static final int PROHIBITED_OVERLAY = 0x78C81E1E;

    private final List<Entry> conversionList = new ArrayList<>();
    private List<ItemStack> prohibitedList = List.of();
    private int lastSyncRevision = -1;

    private int sortIndex = 0;
    private final boolean[] sortReversed = {false, false, false};

    private int sliderY = 0;
    private int sliderYMax = 0;
    private float sliderNormalizedPos = 0.0F;
    private boolean draggingSlider = false;

    private final int tableX = 11;
    private final int tableY = 18;
    private final int sliderWidth = 10;
    private final int marginR = 11;
    private final int marginB = 14;
    private final int knobLength = 10;
    private int tableWidth;
    private int tableHeight;
    private boolean detail = false;

    @Nullable
    private EditBox searchBox;
    @Nullable
    private Entry tooltipEntry = null;

    public PANCoreScreen(PANCoreMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.tableWidth = this.menu.machineGuiSizeX - this.tableX - this.sliderWidth - this.marginR;
        this.tableHeight = this.menu.machineGuiSizeY - this.tableY - this.marginB;

        addRenderableWidget(Button.builder(Component.literal("I"), b -> sortBy(0))
                .bounds(this.leftPos + this.menu.machineGuiSizeX - 80 - 6, this.topPos + 5, 12, 12)
                .build());
        addRenderableWidget(Button.builder(Component.literal("M"), b -> sortBy(1))
                .bounds(this.leftPos + this.menu.machineGuiSizeX - 64 - 6, this.topPos + 5, 12, 12)
                .build());
        addRenderableWidget(Button.builder(Component.literal("C"), b -> sortBy(2))
                .bounds(this.leftPos + this.menu.machineGuiSizeX - 48 - 6, this.topPos + 5, 12, 12)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Dump"), b -> dumpToChat())
                .bounds(this.leftPos + this.menu.machineGuiSizeX - 32 - 4, this.topPos + 5, 30, 12)
                .build());
        addRenderableWidget(Button.builder(Component.literal("D"), b -> this.detail = !this.detail)
                .bounds(this.leftPos + this.imageWidth / 2 - 16, this.topPos + this.tableY + this.tableHeight + 3, 12, 12)
                .build());

        this.searchBox = new EditBox(
                this.font,
                this.leftPos + this.imageWidth / 2,
                this.topPos + this.tableY + this.tableHeight + 3,
                this.imageWidth / 2 - this.marginR,
                12,
                Component.empty()
        );
        this.searchBox.setMaxLength(128);
        this.searchBox.setBordered(true);
        addRenderableWidget(this.searchBox);

        syncFromMenuIfNeeded();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        syncFromMenuIfNeeded();
    }

    private void syncFromMenuIfNeeded() {
        if (this.menu.getSyncRevision() != this.lastSyncRevision) {
            this.lastSyncRevision = this.menu.getSyncRevision();
            this.conversionList.clear();
            this.conversionList.addAll(this.menu.getSyncedIngredients());
            this.prohibitedList = this.menu.getSyncedProhibited();
            applyCurrentSort();
        }
    }

    private void sortBy(int index) {
        this.sortIndex = index;
        this.sortReversed[index] = !this.sortReversed[index];
        applyCurrentSort();
    }

    private void applyCurrentSort() {
        Comparator<Entry> base = switch (this.sortIndex) {
            case 1 -> Comparator.comparingDouble(Entry::cost);
            case 2 -> Comparator.comparingDouble(Entry::consumption);
            default -> Comparator.comparingInt(e -> BuiltInRegistries.ITEM.getId(e.stack().getItem()));
        };
        if (this.sortReversed[this.sortIndex]) {
            base = base.reversed();
        }
        this.conversionList.sort(base);
    }

    private void dumpToChat() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        Pattern pattern = compileSearchPattern();
        for (Entry entry : this.conversionList) {
            if (!entry.stack().isEmpty() && matchesPattern(pattern, entry.stack())) {
                String msg = "[ " + entry.stack().getHoverName().getString()
                        + " : " + ClayiumFormatting.formatClayEnergy(entry.cost()) + "CE"
                        + " : " + ClayiumFormatting.formatClayEnergy(entry.consumption()) + "CE ]";
                this.minecraft.player.sendSystemMessage(Component.literal(msg));
            }
        }
    }

    @Nullable
    private Pattern compileSearchPattern() {
        String query = this.searchBox != null ? this.searchBox.getValue() : "";
        try {
            return Pattern.compile(query, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException e) {
            return null;
        }
    }

    private static boolean matchesPattern(@Nullable Pattern pattern, ItemStack stack) {
        return pattern != null && pattern.matcher(stack.getHoverName().getString()).find();
    }

    private boolean isProhibited(ItemStack stack) {
        for (ItemStack proh : this.prohibitedList) {
            if (ItemStack.isSameItemSameComponents(proh, stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTick, mouseX, mouseY);
        int x0 = this.leftPos + this.tableX;
        int y0 = this.topPos + this.tableY;
        guiGraphics.fill(x0 - 1, y0 - 1, x0 + this.tableWidth + this.sliderWidth + 1, y0 + this.tableHeight + 1, TABLE_BG);

        int sx = x0 + this.tableWidth;
        guiGraphics.fill(sx, y0, sx + this.sliderWidth, y0 + this.tableHeight, SLIDER_BG);
        int knobTop = y0 + (int) (this.sliderNormalizedPos * (float) (this.tableHeight - this.knobLength));
        guiGraphics.fill(sx + 1, knobTop + 1, sx + this.sliderWidth - 1, knobTop + this.knobLength - 1, SLIDER_KNOB);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        syncFromMenuIfNeeded();

        this.sliderY = Math.min((int) ((float) (this.sliderYMax + 1) * this.sliderNormalizedPos), this.sliderYMax);
        this.tooltipEntry = null;

        int ix = this.detail ? 1 : Math.max(this.tableWidth / 16, 1);
        int iy = Math.max(this.tableHeight / 16, 1);

        Pattern pattern = compileSearchPattern();
        int count = 0;
        for (Entry entry : this.conversionList) {
            if (entry.stack().isEmpty() || !matchesPattern(pattern, entry.stack())) {
                continue;
            }
            int px = count % ix;
            int py = count / ix;
            if (py >= this.sliderY && py < this.sliderY + iy) {
                int cx = this.tableX + px * 16;
                int cy = this.tableY + (py - this.sliderY) * 16;
                if (isProhibited(entry.stack())) {
                    guiGraphics.fill(cx, cy, cx + 16, cy + 16, PROHIBITED_OVERLAY);
                }
                guiGraphics.renderItem(entry.stack(), cx, cy);
                guiGraphics.renderItemDecorations(this.font, entry.stack(), cx, cy);

                int screenCx = this.leftPos + cx;
                int screenCy = this.topPos + cy;
                if (mouseX >= screenCx && mouseX < screenCx + 16 && mouseY >= screenCy && mouseY < screenCy + 16) {
                    this.tooltipEntry = entry;
                }

                if (this.detail) {
                    guiGraphics.drawString(
                            this.font,
                            ClayiumFormatting.formatClayEnergy(entry.cost()) + "CE",
                            cx + 24,
                            cy + 4,
                            0xDCDCDC,
                            false
                    );
                    guiGraphics.drawString(
                            this.font,
                            ClayiumFormatting.formatClayEnergy(entry.consumption()) + "CE",
                            cx + 24 + (this.tableWidth - cx - 24) / 2 + 6,
                            cy + 4,
                            0xF0F0F0,
                            false
                    );
                }
            }
            count++;
        }
        this.sliderYMax = Math.max((count - 1) / ix - iy + 1, 0);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);
        if (this.tooltipEntry != null && !this.tooltipEntry.stack().isEmpty()) {
            List<Component> lines = new ArrayList<>(getTooltipFromContainerItem(this.tooltipEntry.stack()));
            String ceLine = " [" + ClayiumFormatting.formatClayEnergy(this.tooltipEntry.cost())
                    + "CE : " + ClayiumFormatting.formatClayEnergy(this.tooltipEntry.consumption()) + "CE ]";
            lines.add(Math.min(1, lines.size()), Component.literal(ceLine).withStyle(ChatFormatting.WHITE));
            guiGraphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0.0) {
            this.sliderNormalizedPos -= 1.0F / (float) (this.sliderYMax + 1);
        } else if (scrollY < 0.0) {
            this.sliderNormalizedPos += 1.0F / (float) (this.sliderYMax + 1);
        }
        this.sliderNormalizedPos = Mth.clamp(this.sliderNormalizedPos, 0.0F, 1.0F);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isInsideSlider(mouseX, mouseY)) {
            this.draggingSlider = true;
            updateSliderFromMouseY(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.draggingSlider && button == 0) {
            updateSliderFromMouseY(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.draggingSlider = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.searchBox != null && this.searchBox.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.searchBox.setFocused(false);
                return true;
            }
            return this.searchBox.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean isInsideSlider(double mouseX, double mouseY) {
        int sx = this.leftPos + this.tableX + this.tableWidth;
        int sy = this.topPos + this.tableY;
        return mouseX >= sx && mouseX < sx + this.sliderWidth && mouseY >= sy && mouseY < sy + this.tableHeight;
    }

    private void updateSliderFromMouseY(double mouseY) {
        int sy = this.topPos + this.tableY;
        float pos = (float) (mouseY - (double) (sy + this.knobLength / 2)) / (float) (this.tableHeight - this.knobLength);
        this.sliderNormalizedPos = Mth.clamp(pos, 0.0F, 1.0F);
    }
}
