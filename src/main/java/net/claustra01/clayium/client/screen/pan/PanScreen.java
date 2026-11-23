package net.claustra01.clayium.client.screen.pan;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import net.claustra01.clayium.client.screen.legacy.LegacyButton;
import net.claustra01.clayium.client.screen.legacy.LegacyGuiGraphics;
import net.claustra01.clayium.pan.PanBlock;
import net.claustra01.clayium.pan.PanBlockEntity;
import net.claustra01.clayium.pan.PanMenu;
import net.claustra01.clayium.pan.PanScreenPayloads;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public final class PanScreen extends AbstractContainerScreen<PanMenu> {
    private final List<LegacyButton> candidateButtons = new ArrayList<>();
    private LegacyButton previous;
    private LegacyButton next;
    private LegacyButton sort;
    private LegacyButton reverse;
    private LegacyButton prohibit;

    public PanScreen(PanMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = switch (menu.kind()) {
            case CORE -> 238;
            case ADAPTER -> 190;
            case DUPLICATOR -> 199;
            case CABLE -> 185;
        };
        inventoryLabelY = switch (menu.kind()) {
            case CORE -> 145;
            case ADAPTER -> 97;
            case DUPLICATOR -> 108;
            case CABLE -> 94;
        };
    }

    @Override
    protected void init() {
        super.init();
        if (menu.kind() != PanBlock.Kind.CORE) {
            return;
        }
        sort = addRenderableWidget(new LegacyButton(leftPos + 6, topPos + 17, 53, 12,
                Component.translatable("gui.clayium.pan.sort"),
                () -> send(PanScreenPayloads.SORT, (menu.candidateSort() + 1) % 3)));
        reverse = addRenderableWidget(new LegacyButton(leftPos + 61, topPos + 17, 36, 12,
                Component.empty(), () -> send(PanScreenPayloads.REVERSE, 0)));
        previous = addRenderableWidget(new LegacyButton(leftPos + 99, topPos + 17, 18, 12,
                Component.literal("<"), () -> send(PanScreenPayloads.PAGE, menu.candidatePage() - 1)));
        next = addRenderableWidget(new LegacyButton(leftPos + 119, topPos + 17, 18, 12,
                Component.literal(">"), () -> send(PanScreenPayloads.PAGE, menu.candidatePage() + 1)));
        prohibit = addRenderableWidget(new LegacyButton(leftPos + 139, topPos + 17, 31, 12,
                Component.translatable("gui.clayium.pan.prohibit"),
                () -> send(PanScreenPayloads.TOGGLE_PROHIBITED, 0)));
        for (int index = 0; index < PanBlockEntity.CANDIDATES_PER_PAGE; index++) {
            int candidate = index;
            candidateButtons.add(addRenderableWidget(new LegacyButton(
                    leftPos + 26, topPos + 32 + index * 15, 144, 14, Component.empty(),
                    () -> send(PanScreenPayloads.SELECT, candidate))));
        }
        updateButtons();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateButtons();
    }

    private void updateButtons() {
        if (previous == null) {
            return;
        }
        previous.active = menu.candidatePage() > 0;
        next.active = menu.candidatePage() + 1 < menu.candidatePages();
        sort.setMessage(Component.translatable("gui.clayium.pan.sort." + menu.candidateSort()));
        reverse.setMessage(Component.translatable(menu.candidateReverse()
                ? "gui.clayium.pan.descending" : "gui.clayium.pan.ascending"));
        List<PanScreenPayloads.Candidate> candidates = menu.visibleCandidates();
        boolean selected = false;
        for (int index = 0; index < candidateButtons.size(); index++) {
            LegacyButton button = candidateButtons.get(index);
            button.visible = index < candidates.size();
            if (!button.visible) {
                continue;
            }
            PanScreenPayloads.Candidate candidate = candidates.get(index);
            ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(candidate.item()));
            String marker = candidate.item().equals(menu.selectedCandidate()) ? "> " : "  ";
            String disabled = candidate.prohibited() ? " [X]" : "";
            String suffix = " " + format(candidate.consumption()) + disabled;
            int nameWidth = Math.max(0, button.getWidth() - 8 - font.width(marker + suffix));
            String name = stack.getHoverName().getString();
            if (font.width(name) > nameWidth) {
                name = font.plainSubstrByWidth(name, Math.max(0, nameWidth - font.width("…"))) + "…";
            }
            button.setMessage(Component.literal(marker + name + suffix));
            selected |= candidate.item().equals(menu.selectedCandidate());
        }
        prohibit.active = selected;
    }

    private void send(int action, int value) {
        PacketDistributor.sendToServer(new PanScreenPayloads.Control(menu.position(), action, value));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        LegacyGuiGraphics.background(graphics, leftPos, topPos, imageWidth, imageHeight);
        int inventoryY = switch (menu.kind()) {
            case CORE -> 144;
            case ADAPTER -> 96;
            case DUPLICATOR -> 107;
            case CABLE -> 93;
        };
        graphics.blit(LegacyGuiGraphics.PLAYER_INVENTORY, leftPos, topPos + inventoryY,
                0, 0, 176, 90, 256, 256);
        if (menu.kind() == PanBlock.Kind.DUPLICATOR) {
            for (int[] slot : new int[][] {{35, 35}, {35, 57}, {125, 35}, {35, 79}}) {
                LegacyGuiGraphics.slot(graphics, leftPos + slot[0], topPos + slot[1]);
            }
            LegacyGuiGraphics.progress(graphics, leftPos + 84, topPos + 36,
                    menu.progress(), menu.requiredProgress());
        } else if (menu.kind() == PanBlock.Kind.CORE) {
            List<PanScreenPayloads.Candidate> candidates = menu.visibleCandidates();
            for (int index = 0; index < candidates.size(); index++) {
                ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(candidates.get(index).item()));
                graphics.renderFakeItem(stack, leftPos + 8, topPos + 32 + index * 15);
            }
            int sliderY = menu.candidatePages() <= 1 ? 32
                    : 32 + menu.candidatePage() * 89 / (menu.candidatePages() - 1);
            LegacyGuiGraphics.slider(graphics, leftPos + 170, topPos + 32, topPos + sliderY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (menu.kind() == PanBlock.Kind.CORE && scrollY != 0.0D) {
            int page = menu.candidatePage() + (scrollY < 0.0D ? 1 : -1);
            if (page >= 0 && page < menu.candidatePages()) {
                send(PanScreenPayloads.PAGE, page);
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (menu.kind() == PanBlock.Kind.CORE && button == 0 && sliderAt(mouseX, mouseY)) {
            moveSlider(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (menu.kind() == PanBlock.Kind.CORE && button == 0 && sliderAt(mouseX, mouseY)) {
            moveSlider(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private boolean sliderAt(double mouseX, double mouseY) {
        return mouseX >= leftPos + 170 && mouseX < leftPos + 176
                && mouseY >= topPos + 32 && mouseY < topPos + 128;
    }

    private void moveSlider(double mouseY) {
        int page = (int)Math.round((mouseY - topPos - 32) * (menu.candidatePages() - 1) / 95.0D);
        page = Math.clamp(page, 0, menu.candidatePages() - 1);
        if (page != menu.candidatePage()) {
            send(PanScreenPayloads.PAGE, page);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 7, 6, LegacyGuiGraphics.TEXT_COLOR, false);
        if (menu.kind() == PanBlock.Kind.CORE) {
            String page = (menu.candidatePage() + 1) + "/" + menu.candidatePages();
            graphics.drawString(font, page, 118 - font.width(page) / 2, 7,
                    LegacyGuiGraphics.TEXT_COLOR, false);
            drawFitted(graphics, Component.translatable("gui.clayium.pan.network",
                    menu.nodes(), menu.adapters()), 7, 130, 162, 0x405060);
        } else if (menu.kind() == PanBlock.Kind.ADAPTER) {
            drawFitted(graphics, Component.translatable("gui.clayium.pan.network",
                    menu.nodes(), menu.adapters()), 7, 27, 162, 0x405060);
        } else {
            graphics.drawString(font, Component.translatable("gui.clayium.pan.tier", menu.tier()), 7, 18,
                    LegacyGuiGraphics.TEXT_COLOR, false);
            drawFitted(graphics, Component.translatable("gui.clayium.pan.network",
                    menu.nodes(), menu.adapters()), 70, 18, 99, 0x405060);
            drawFitted(graphics, Component.translatable(menu.sampleReachable()
                    ? "gui.clayium.pan.reachable" : "gui.clayium.pan.unreachable"),
                    70, 56, 99, LegacyGuiGraphics.TEXT_COLOR);
            drawFitted(graphics, Component.translatable("gui.clayium.pan.energy", format(menu.requiredEnergy())),
                    70, 68, 99, LegacyGuiGraphics.TEXT_COLOR);
            drawFitted(graphics, Component.translatable("gui.clayium.pan.progress",
                    menu.progress(), menu.requiredProgress()), 70, 80, 99, LegacyGuiGraphics.TEXT_COLOR);
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
        int relativeY = mouseY - topPos - 32;
        int index = relativeY < 0 ? -1 : relativeY / 15;
        List<PanScreenPayloads.Candidate> candidates = menu.visibleCandidates();
        if (index >= 0 && index < candidates.size() && mouseX >= leftPos + 6 && mouseX < leftPos + 170) {
            PanScreenPayloads.Candidate candidate = candidates.get(index);
            ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(candidate.item()));
            graphics.renderTooltip(font, List.of(
                    stack.getHoverName(),
                    Component.translatable("gui.clayium.pan.cost", candidate.cost()),
                    Component.translatable("gui.clayium.pan.consumption", format(candidate.consumption())),
                    Component.translatable(candidate.prohibited()
                            ? "gui.clayium.pan.prohibited" : "gui.clayium.pan.permitted")),
                    java.util.Optional.empty(), mouseX, mouseY);
        }
    }

    private static String format(long internal) {
        return BigDecimal.valueOf(internal, 5).stripTrailingZeros().toPlainString() + " CE";
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
