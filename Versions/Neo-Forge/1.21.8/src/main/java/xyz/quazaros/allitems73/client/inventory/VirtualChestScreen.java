package xyz.quazaros.allitems73.client.inventory;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import xyz.quazaros.allitems73.items.item;
import xyz.quazaros.allitems73.main;

import java.util.ArrayList;
import java.util.List;

import static xyz.quazaros.allitems73.client.events.onClickEvent.onInventoryKeyPressed;

public class VirtualChestScreen extends Screen {
    private static final int VISIBLE_ROWS = 5;
    private static final int COLUMNS = 9;

    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/creative_inventory/tab_items.png");

    private static final int BACKGROUND_WIDTH = 195;
    private static final int BACKGROUND_HEIGHT = 136;
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_OFFSET_X = 9;
    private static final int SLOT_OFFSET_Y = 18;

    private int guiLeft;
    private int guiTop;

    public final NonNullList<ItemStack> stacks =
            NonNullList.withSize(main.getItemList().getSize(), ItemStack.EMPTY);

    private boolean filtered;

    private List<ClientTooltipComponent> pendingTooltip;
    private int pendingTooltipX;
    private int pendingTooltipY;

    public VirtualChestScreen(boolean filtered) {
        // Determine the base string and append suffix if the mod is loaded
        super(Component.literal(
                (!filtered ? "All Items Inventory" : "All Items Inventory - Filtered") +
                        (net.neoforged.fml.ModList.get().isLoaded("allitemsclient73") ? " (S)" : "")
        ));

        this.filtered = filtered;
    }

    @Override
    protected void init() {
        this.guiLeft = (this.width - BACKGROUND_WIDTH) / 2;
        this.guiTop = (this.height - BACKGROUND_HEIGHT) / 2;

        if (!filtered) {
            for (int i = 0; i < stacks.size(); i++) {
                stacks.set(i, main.getItemList().items.get(i).item_stack);
            }
        } else {
            ArrayList<item> filteredItemList = main.getItemList().getFilteredList();
            for (int i = 0; i < Math.min(stacks.size(), filteredItemList.size()); i++) {
                stacks.set(i, filteredItemList.get(i).item_stack);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        pendingTooltip = null;

        renderBackgroundTexture(guiGraphics);
        drawTitle(guiGraphics);
        renderSlotsAndItems(guiGraphics);
        renderScrollbar(guiGraphics);
        renderProgress(guiGraphics, mouseX, mouseY);
        renderFilter(guiGraphics, mouseX, mouseY);
        renderLeaderboard(guiGraphics, mouseX, mouseY);
        renderHoveredTooltip(guiGraphics, mouseX, mouseY);

        if (pendingTooltip != null) {
            guiGraphics.renderTooltip(
                    this.font,
                    pendingTooltip,
                    pendingTooltipX,
                    pendingTooltipY,
                    DefaultTooltipPositioner.INSTANCE,
                    null
            );
        }
    }

    private void renderBackgroundTexture(GuiGraphics guiGraphics) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, guiLeft, guiTop, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, 256, 256);
    }

    private void renderSlotsAndItems(GuiGraphics guiGraphics) {
        for (int visRow = 0; visRow < VISIBLE_ROWS; visRow++) {
            int row = visRow + scrollOffsetRow;
            for (int col = 0; col < COLUMNS; col++) {
                int index = row * COLUMNS + col;
                if (index < 0 || index >= stacks.size()) continue;

                ItemStack stack = stacks.get(index);
                int x = guiLeft + SLOT_OFFSET_X + col * SLOT_SIZE;
                int y = guiTop + SLOT_OFFSET_Y + visRow * SLOT_SIZE;

                if (!stack.isEmpty()) {
                    guiGraphics.renderItem(stack, x, y);
                }
            }
        }
    }

    private void drawTitle(GuiGraphics guiGraphics) {
        guiGraphics.drawString(this.font, this.title, guiLeft + 8, guiTop + 6, 0xFF404040, false);
    }

    private void renderHoveredTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int slotIndex = getSlotIndexAt(mouseX, mouseY);
        if (slotIndex < 0 || slotIndex >= stacks.size()) return;

        ItemStack stack = stacks.get(slotIndex);
        if (stack.isEmpty()) return;

        item tempItem = main.getItemList().get(stack.getItemHolder().getRegisteredName());

        Component c = Component.literal(tempItem.item_display_name)
                .withStyle(tempItem.is_found ? ChatFormatting.GREEN : ChatFormatting.RED);

        List<ClientTooltipComponent> lines = new ArrayList<>();
        lines.add(new ClientTextTooltip(c.getVisualOrderText()));

        if (tempItem.is_found) {
            c = Component.literal("By: " + tempItem.item_founder).withStyle(ChatFormatting.AQUA);
            lines.add(new ClientTextTooltip(c.getVisualOrderText()));
            c = Component.literal("At: " + tempItem.item_time).withStyle(ChatFormatting.AQUA);
            lines.add(new ClientTextTooltip(c.getVisualOrderText()));
        }

        guiGraphics.renderTooltip(
                this.font,
                lines,
                mouseX,
                mouseY,
                DefaultTooltipPositioner.INSTANCE,
                null
        );
    }

    private int getSlotIndexAt(int mouseX, int mouseY) {
        int gridLeft = guiLeft + SLOT_OFFSET_X;
        int gridTop = guiTop + SLOT_OFFSET_Y;
        if (mouseX < gridLeft || mouseX >= gridLeft + COLUMNS * SLOT_SIZE ||
                mouseY < gridTop || mouseY >= gridTop + VISIBLE_ROWS * SLOT_SIZE) return -1;

        int col = (mouseX - gridLeft) / SLOT_SIZE;
        int visRow = (mouseY - gridTop) / SLOT_SIZE;
        int index = (visRow + scrollOffsetRow) * COLUMNS + col;

        return (index >= 0 && index < stacks.size()) ? index : -1;
    }

    private void renderProgress(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = guiLeft + SLOT_OFFSET_X + SLOT_SIZE * 2;
        int y = guiTop + SLOT_OFFSET_Y + VISIBLE_ROWS * SLOT_SIZE + 4;
        guiGraphics.renderItem(new ItemStack(Items.DIAMOND), x, y);

        Component c = Component.literal("Progress: " + main.getItemList().getProgString()).withStyle(ChatFormatting.AQUA);

        List<ClientTooltipComponent> lines = new ArrayList<>();
        lines.add(new ClientTextTooltip(c.getVisualOrderText()));

        if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
            pendingTooltip = lines;
            pendingTooltipX = mouseX;
            pendingTooltipY = mouseY;
        }
    }

    private void renderLeaderboard(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = guiLeft + SLOT_OFFSET_X + SLOT_SIZE * 4;
        int y = guiTop + SLOT_OFFSET_Y + VISIBLE_ROWS * SLOT_SIZE + 4;
        guiGraphics.renderItem(new ItemStack(Items.OAK_HANGING_SIGN), x, y);

        if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
            pendingTooltip = main.getItemList().getLeaderboard();
            pendingTooltipX = mouseX;
            pendingTooltipY = mouseY;
        }
    }

    private void renderFilter(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = guiLeft + SLOT_OFFSET_X + SLOT_SIZE * 6;
        int y = guiTop + SLOT_OFFSET_Y + VISIBLE_ROWS * SLOT_SIZE + 4;
        guiGraphics.renderItem(new ItemStack(Items.HOPPER), x, y);

        Component c = Component.literal("Filter").withStyle(ChatFormatting.AQUA);

        List<ClientTooltipComponent> lines = new java.util.ArrayList<>();
        lines.add(new ClientTextTooltip(c.getVisualOrderText()));

        if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
            pendingTooltip = lines;
            pendingTooltipX = mouseX;
            pendingTooltipY = mouseY;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (canScroll() && isOverScrollbar(mouseX, mouseY)) {
                scrolling = true;
                updateScrollFromMouse(mouseY);
                return true;
            }
            if (isOverFilter(mouseX, mouseY)) {
                onInventoryKeyPressed(Minecraft.getInstance(), !filtered);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isOverFilter(double mouseX, double mouseY) {
        int x = guiLeft + SLOT_OFFSET_X + SLOT_SIZE * 6;
        int y = guiTop + SLOT_OFFSET_Y + VISIBLE_ROWS * SLOT_SIZE + 4;
        return mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16;
    }

    // --- Scrolling Logic ---
    private float scrollPosition = 0.0f;
    private int scrollOffsetRow = 0;
    private boolean scrolling = false;

    private static final int SCROLLBAR_X = 175;
    private static final int SCROLLBAR_Y = 18;
    private static final int SCROLLBAR_HEIGHT = 110;
    private static final int SCROLLBAR_WIDTH = 12;
    private static final int KNOB_HEIGHT = 15;

    private static final ResourceLocation KNOB_TEXTURE =
            ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller");
    private static final ResourceLocation KNOB_TEXTURE_DISABLED =
            ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller_disabled");

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!canScroll()) return false;
        int maxRows = getMaxScrollRows();
        scrollPosition = clamp(scrollPosition - (float) scrollY / maxRows, 0.0f, 1.0f);
        updateScrollFromPosition();
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && scrolling && canScroll()) {
            updateScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) scrolling = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void renderScrollbar(GuiGraphics guiGraphics) {
        ResourceLocation tex = canScroll() ? KNOB_TEXTURE : KNOB_TEXTURE_DISABLED;
        int x = guiLeft + SCROLLBAR_X;
        int y = guiTop + SCROLLBAR_Y;
        int knobY = y + (int) (scrollPosition * (SCROLLBAR_HEIGHT - KNOB_HEIGHT));

        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, tex, x, knobY, SCROLLBAR_WIDTH, KNOB_HEIGHT);
    }

    private int getMaxScrollRows() { return Math.max(0, (int) Math.ceil(stacks.size() / 9.0) - VISIBLE_ROWS); }
    private boolean canScroll() { return getMaxScrollRows() > 0; }
    private void updateScrollFromPosition() { scrollOffsetRow = Math.round(scrollPosition * getMaxScrollRows()); }
    private float clamp(float v, float min, float max) { return Math.max(min, Math.min(max, v)); }
    private boolean isOverScrollbar(double mouseX, double mouseY) {
        return mouseX >= guiLeft + SCROLLBAR_X && mouseX < guiLeft + SCROLLBAR_X + SCROLLBAR_WIDTH &&
                mouseY >= guiTop + SCROLLBAR_Y && mouseY < guiTop + SCROLLBAR_Y + SCROLLBAR_HEIGHT;
    }
    private void updateScrollFromMouse(double mouseY) {
        scrollPosition = clamp((float) (mouseY - (guiTop + SCROLLBAR_Y) - KNOB_HEIGHT / 2.0) / (SCROLLBAR_HEIGHT - KNOB_HEIGHT), 0.0f, 1.0f);
        updateScrollFromPosition();
    }
}