package net.claustra01.clayium.client.screen.pan;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.claustra01.clayium.client.screen.legacy.LegacyButton;
import net.claustra01.clayium.client.screen.legacy.LegacyGuiGraphics;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.pan.PanBlock;
import net.claustra01.clayium.pan.PanBlockEntity;
import net.claustra01.clayium.pan.PanMenu;
import net.claustra01.clayium.pan.PanScreenPayloads;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public final class PanScreen extends AbstractContainerScreen<PanMenu> {
    private static final int TABLE_X = 11;
    private static final int TABLE_Y = 18;
    private static final int TABLE_WIDTH = 144;
    private static final int TABLE_HEIGHT = 112;
    private static final int SLIDER_WIDTH = 10;
    private static final int ROWS = 7;

    private LegacyButton previous;
    private LegacyButton next;
    private EditBox search;
    private int sortMode;
    private boolean reverse;
    private boolean detail;
    private int scrollRow;
    private boolean draggingSlider;

    public PanScreen(PanMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = switch (menu.kind()) {
            case CORE -> 238;
            case ADAPTER -> 190;
            case DUPLICATOR -> 166;
            case CABLE -> 185;
        };
        inventoryLabelY = switch (menu.kind()) {
            case CORE -> 145;
            case ADAPTER -> 97;
            case DUPLICATOR -> 72;
            case CABLE -> 94;
        };
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (menu.kind() == PanBlock.Kind.CORE) {
            renderCoreContents(graphics);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        if (menu.kind() == PanBlock.Kind.ADAPTER) {
            if (menu.adapterPages() > 1) {
                previous = addRenderableWidget(new LegacyButton(leftPos + 4, topPos + 36, 16, 16,
                        Component.literal("<"), () -> changeAdapterPage(menu.adapterPage() - 1)));
                next = addRenderableWidget(new LegacyButton(leftPos + 156, topPos + 36, 16, 16,
                        Component.literal(">"), () -> changeAdapterPage(menu.adapterPage() + 1)));
            }
            updateAdapterButtons();
            return;
        }
        if (menu.kind() != PanBlock.Kind.CORE) {
            return;
        }
        addRenderableWidget(new LegacyButton(leftPos + 90, topPos + 5, 12, 12,
                Component.literal("I"), () -> sortBy(0)));
        addRenderableWidget(new LegacyButton(leftPos + 106, topPos + 5, 12, 12,
                Component.literal("M"), () -> sortBy(1)));
        addRenderableWidget(new LegacyButton(leftPos + 122, topPos + 5, 12, 12,
                Component.literal("C"), () -> sortBy(2)));
        addRenderableWidget(new LegacyButton(leftPos + 140, topPos + 5, 30, 12,
                Component.literal("Dump"), this::dumpMatches));
        addRenderableWidget(new LegacyButton(leftPos + 72, topPos + 133, 12, 12,
                Component.literal("D"), () -> {
                    detail = !detail;
                    scrollRow = 0;
                }));
        search = addRenderableWidget(new EditBox(font, leftPos + 88, topPos + 133, 77, 12,
                Component.translatable("gui.clayium.pan.search")));
        search.setMaxLength(128);
        search.setBordered(true);
        search.setResponder(value -> scrollRow = 0);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateAdapterButtons();
        scrollRow = Math.clamp(scrollRow, 0, maxScrollRow());
    }

    private void updateAdapterButtons() {
        if (menu.kind() != PanBlock.Kind.ADAPTER || previous == null) {
            return;
        }
        previous.active = menu.adapterPage() > 0;
        next.active = menu.adapterPage() + 1 < menu.adapterPages();
    }

    private void sortBy(int requested) {
        if (sortMode == requested) {
            reverse = !reverse;
        } else {
            sortMode = requested;
            reverse = false;
        }
        scrollRow = 0;
    }

    private void dumpMatches() {
        if (minecraft == null || minecraft.player == null) {
            return;
        }
        for (PanScreenPayloads.Candidate candidate : filteredCandidates()) {
            ItemStack stack = stack(candidate);
            minecraft.player.displayClientMessage(Component.literal(stack.getHoverName().getString()
                    + " [" + format(candidate.cost()) + " : " + format(candidate.consumption()) + "]"), false);
        }
    }

    private void changeAdapterPage(int page) {
        if (page >= 0 && page < menu.adapterPages() && menu.setAdapterPage(page)) {
            PacketDistributor.sendToServer(new PanScreenPayloads.AdapterPage(menu.position(), page));
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        int inventoryY = switch (menu.kind()) {
            case CORE -> 144;
            case ADAPTER -> 96;
            case DUPLICATOR -> 72;
            case CABLE -> 93;
        };
        graphics.blit(LegacyGuiGraphics.PLAYER_INVENTORY, leftPos, topPos + inventoryY,
                0, 0, 176, 90, 256, 256);
        if (menu.kind() == PanBlock.Kind.DUPLICATOR) {
            LegacyGuiGraphics.slot(graphics, leftPos + 32, topPos + 35, LegacyGuiGraphics.SlotTexture.IMPORT_1);
            LegacyGuiGraphics.slot(graphics, leftPos + 50, topPos + 35, LegacyGuiGraphics.SlotTexture.IMPORT_2);
            LegacyGuiGraphics.largeSlot(graphics, leftPos + 116, topPos + 35);
            LegacyGuiGraphics.progress(graphics, leftPos + 76, topPos + 35,
                    menu.progress(), menu.requiredProgress());
        } else if (menu.kind() == PanBlock.Kind.CORE) {
            graphics.fill(leftPos + TABLE_X - 1, topPos + TABLE_Y - 1,
                    leftPos + TABLE_X + TABLE_WIDTH + SLIDER_WIDTH + 1,
                    topPos + TABLE_Y + TABLE_HEIGHT + 1, 0xFF001E00);
            renderSlider(graphics);
        } else if (menu.kind() == PanBlock.Kind.ADAPTER) {
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 3; column++) {
                    LegacyGuiGraphics.slot(graphics, leftPos + 32 + column * 18, topPos + 18 + row * 18);
                    LegacyGuiGraphics.slot(graphics, leftPos + 92 + column * 18, topPos + 18 + row * 18);
                }
            }
            for (int slot = 0; slot < PanBlockEntity.ADAPTER_PAGE_SIZE; slot++) {
                LegacyGuiGraphics.slot(graphics, leftPos + 8 + slot * 18, topPos + 74);
            }
        }
    }

    private void renderSlider(GuiGraphics graphics) {
        int x = leftPos + TABLE_X + TABLE_WIDTH;
        int y = topPos + TABLE_Y;
        graphics.fill(x, y, x + SLIDER_WIDTH, y + TABLE_HEIGHT, 0xFF303030);
        int travel = TABLE_HEIGHT - 8;
        int maximum = maxScrollRow();
        int thumbY = y + (maximum == 0 ? 0 : scrollRow * travel / maximum);
        graphics.fill(x + 1, thumbY, x + SLIDER_WIDTH - 1, thumbY + 8, 0xFFC0C0C0);
        graphics.fill(x + 2, thumbY + 1, x + SLIDER_WIDTH - 2, thumbY + 7, 0xFF707070);
    }

    private void renderCoreContents(GuiGraphics graphics) {
        List<PanScreenPayloads.Candidate> candidates = filteredCandidates();
        int columns = detail ? 1 : 9;
        int start = scrollRow * columns;
        int end = Math.min(candidates.size(), start + ROWS * columns);
        for (int index = start; index < end; index++) {
            int visible = index - start;
            int column = visible % columns;
            int row = visible / columns;
            int x = leftPos + TABLE_X + column * 16;
            int y = topPos + TABLE_Y + row * 16;
            PanScreenPayloads.Candidate candidate = candidates.get(index);
            if (candidate.prohibited()) {
                graphics.fill(x, y, x + 16, y + 16, 0x78C0001E);
            }
            graphics.renderFakeItem(stack(candidate), x, y);
            if (detail) {
                graphics.drawString(font, format(candidate.cost()), x + 24, y + 4, 0xFFDCDCDC, false);
                graphics.drawString(font, format(candidate.consumption()), x + 88, y + 4, 0xFFF0F0B0, false);
            }
        }
    }

    private List<PanScreenPayloads.Candidate> filteredCandidates() {
        Pattern pattern;
        try {
            String expression = search == null ? "" : search.getValue();
            pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        } catch (PatternSyntaxException ignored) {
            return List.of();
        }
        List<PanScreenPayloads.Candidate> candidates = new ArrayList<>();
        for (PanScreenPayloads.Candidate candidate : menu.candidates()) {
            if (pattern.matcher(stack(candidate).getHoverName().getString()).find()) {
                candidates.add(candidate);
            }
        }
        Comparator<PanScreenPayloads.Candidate> comparator = switch (sortMode) {
            case 1 -> Comparator.comparingLong(PanScreenPayloads.Candidate::cost)
                    .thenComparing(candidate -> candidate.item().toString());
            case 2 -> Comparator.comparingLong(PanScreenPayloads.Candidate::consumption)
                    .thenComparing(candidate -> candidate.item().toString());
            default -> Comparator.comparing(candidate -> candidate.item().toString());
        };
        candidates.sort(reverse ? comparator.reversed() : comparator);
        return candidates;
    }

    private int maxScrollRow() {
        int columns = detail ? 1 : 9;
        int rows = (filteredCandidates().size() + columns - 1) / columns;
        return Math.max(0, rows - ROWS);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (menu.kind() == PanBlock.Kind.CORE && scrollY != 0.0D
                && mouseX >= leftPos + TABLE_X && mouseX < leftPos + TABLE_X + TABLE_WIDTH + SLIDER_WIDTH
                && mouseY >= topPos + TABLE_Y && mouseY < topPos + TABLE_Y + TABLE_HEIGHT) {
            scrollRow = Math.clamp(scrollRow + (scrollY < 0.0D ? 1 : -1), 0, maxScrollRow());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (menu.kind() == PanBlock.Kind.ADAPTER && button == 0 && hoveredSlot != null
                && hoveredSlot.index < 2 * PanBlockEntity.ADAPTER_PAGE_SIZE) {
            if (hoveredSlot.index < PanBlockEntity.ADAPTER_PAGE_SIZE) {
                PacketDistributor.sendToServer(new PanScreenPayloads.AdapterPattern(
                        menu.position(), menu.adapterPage(), hoveredSlot.index, menu.getCarried().isEmpty()));
            }
            return true;
        }
        if (menu.kind() == PanBlock.Kind.CORE) {
            if (button == 0 && sliderAt(mouseX, mouseY)) {
                draggingSlider = true;
                moveSlider(mouseY);
                return true;
            }
            PanScreenPayloads.Candidate candidate = candidateAt(mouseX, mouseY);
            if (button == 1 && candidate != null) {
                PacketDistributor.sendToServer(
                        new PanScreenPayloads.ToggleProhibited(menu.position(), candidate.item()));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingSlider = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (menu.kind() == PanBlock.Kind.CORE && button == 0 && draggingSlider) {
            moveSlider(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private boolean sliderAt(double mouseX, double mouseY) {
        int x = leftPos + TABLE_X + TABLE_WIDTH;
        return mouseX >= x && mouseX < x + SLIDER_WIDTH
                && mouseY >= topPos + TABLE_Y && mouseY < topPos + TABLE_Y + TABLE_HEIGHT;
    }

    private void moveSlider(double mouseY) {
        int maximum = maxScrollRow();
        int travel = TABLE_HEIGHT - 8;
        int row = (int)Math.round((mouseY - topPos - TABLE_Y - 4) * maximum / Math.max(1.0D, travel));
        scrollRow = Math.clamp(row, 0, maximum);
    }

    private PanScreenPayloads.Candidate candidateAt(double mouseX, double mouseY) {
        if (mouseX < leftPos + TABLE_X || mouseX >= leftPos + TABLE_X + TABLE_WIDTH
                || mouseY < topPos + TABLE_Y || mouseY >= topPos + TABLE_Y + TABLE_HEIGHT) {
            return null;
        }
        int columns = detail ? 1 : 9;
        int column = (int)(mouseX - leftPos - TABLE_X) / 16;
        int row = (int)(mouseY - topPos - TABLE_Y) / 16;
        if (column >= columns) {
            return null;
        }
        List<PanScreenPayloads.Candidate> candidates = filteredCandidates();
        int index = (scrollRow + row) * columns + column;
        return index >= 0 && index < candidates.size() ? candidates.get(index) : null;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, menu.kind() == PanBlock.Kind.DUPLICATOR ? 6 : 7, 6,
                LegacyGuiGraphics.TEXT_COLOR, false);
        if (menu.kind() == PanBlock.Kind.CORE) {
            drawFitted(graphics, Component.translatable("gui.clayium.pan.energy", format(menu.requiredEnergy())),
                    4, 132, 64, LegacyGuiGraphics.TEXT_COLOR);
        } else if (menu.kind() == PanBlock.Kind.ADAPTER) {
            if (menu.adapterPages() > 1) {
                String page = (menu.adapterPage() + 1) + "/" + menu.adapterPages();
                graphics.drawString(font, page, 169 - font.width(page), 64,
                        LegacyGuiGraphics.TEXT_COLOR, false);
            }
            drawFitted(graphics, Component.translatable("gui.clayium.pan.network",
                    menu.nodes(), menu.adapters()), 7, 64, 120, 0x405060);
        } else {
            // GuiClayEnergyTemp footer.
            graphics.drawString(font, Component.translatable("gui.clayium.machine.energy",
                    net.claustra01.clayium.energy.ClayEnergy.ofInternal(menu.storedEnergy()).formatGui()),
                    4, 72 - 12, LegacyGuiGraphics.TEXT_COLOR, false);
        }
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY,
                LegacyGuiGraphics.TEXT_COLOR, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        if (menu.kind() != PanBlock.Kind.CORE) {
            return;
        }
        PanScreenPayloads.Candidate candidate = candidateAt(mouseX, mouseY);
        if (candidate != null) {
            ItemStack stack = stack(candidate);
            graphics.renderTooltip(font, List.of(
                    stack.getHoverName(),
                    Component.translatable("gui.clayium.pan.cost", format(candidate.cost())),
                    Component.translatable("gui.clayium.pan.consumption", format(candidate.consumption())),
                    Component.translatable(candidate.prohibited()
                            ? "gui.clayium.pan.prohibited" : "gui.clayium.pan.permitted")),
                    java.util.Optional.empty(), mouseX, mouseY);
        }
    }

    private static ItemStack stack(PanScreenPayloads.Candidate candidate) {
        return new ItemStack(BuiltInRegistries.ITEM.get(candidate.item()));
    }

    private static String format(long internal) {
        return ClayEnergy.ofInternal(internal).format();
    }

    private void drawFitted(GuiGraphics graphics, Component label, int x, int y, int width, int color) {
        float scale = Math.min(1.0F, (float)width / Math.max(1, font.width(label)));
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(font, label, 0, 0, color, false);
        graphics.pose().popPose();
    }
}
