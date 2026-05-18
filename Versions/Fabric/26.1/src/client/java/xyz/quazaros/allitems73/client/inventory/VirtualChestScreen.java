package xyz.quazaros.allitems73.client.inventory;

import com.mojang.realmsclient.util.TextRenderingUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.NonNullList;

import xyz.quazaros.allitems73.items.item;
import xyz.quazaros.allitems73.main;

import java.util.ArrayList;
import java.util.List;

import static xyz.quazaros.allitems73.client.events.onClickEvent.onInventoryKeyPressed;

public class VirtualChestScreen extends Screen {
    private static final int VISIBLE_ROWS = 5;
    private static final int COLUMNS = 9;

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/creative_inventory/tab_items.png");

    private static final int BACKGROUND_WIDTH = 195;
    private static final int BACKGROUND_HEIGHT = 136;
    private static final int TEX_WIDTH = 256;
    private static final int TEX_HEIGHT = 256;
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_OFFSET_X = 9;
    private static final int SLOT_OFFSET_Y = 18;

    private int guiLeft;
    private int guiTop;

    public NonNullList<ItemStack> stacks = NonNullList.create();

    private boolean filtered;

    public VirtualChestScreen(boolean filtered) {
        super(
            Component.literal(
                !filtered ?
                    "All Items Inventory" + (FabricLoader.getInstance().isModLoaded("allitemsclient73") ? " (S)" : "") :
                    "All Items Inventory - Filtered" + (FabricLoader.getInstance().isModLoaded("allitemsclient73") ? " (S)" : "")
            )
        );
        this.filtered = filtered;
    }

    @Override
    public void init() {
        this.guiLeft = (this.width - BACKGROUND_WIDTH) / 2;
        this.guiTop = (this.height - BACKGROUND_HEIGHT) / 2;

        int totalItems = !filtered ? main.getItemList().getSize() : main.getItemList().getFilteredList().size();
        this.stacks = NonNullList.withSize(totalItems, ItemStack.EMPTY);

        if (!filtered) {
            for (int i = 0; i < stacks.size(); i++) {
                stacks.set(i, main.getItemList().items.get(i).item_stack);
            }
        } else {
            ArrayList<item> filteredItemList = main.getItemList().getFilteredList();
            for (int i = 0; i < stacks.size(); i++) {
                stacks.set(i, filteredItemList.get(i).item_stack);
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {

        renderBackgroundTexture(context);
        drawTitle(context);
        renderSlotsAndItems(context);
        renderHoveredTooltip(context, mouseX, mouseY);
        renderScrollbar(context);
        renderProgress(context, mouseX, mouseY);
        renderFilter(context, mouseX, mouseY);
        renderLeaderboard(context, mouseX, mouseY);
        renderLeaderboard(context, mouseX, mouseY);

        super.extractRenderState(context, mouseX, mouseY, deltaTicks);
    }

    private void renderBackgroundTexture(GuiGraphicsExtractor context) {
        context.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                guiLeft, guiTop,
                0.0f, 0.0f,
                BACKGROUND_WIDTH, BACKGROUND_HEIGHT,
                TEX_WIDTH, TEX_HEIGHT
        );
    }

    private void renderSlotsAndItems(GuiGraphicsExtractor context) {
        for (int visRow = 0; visRow < VISIBLE_ROWS; visRow++) {
            int row = visRow + scrollOffsetRow;
            for (int col = 0; col < COLUMNS; col++) {
                int index = row * COLUMNS + col;
                if (index < 0 || index >= stacks.size()) {
                    continue;
                }
                ItemStack stack = stacks.get(index);
                int x = guiLeft + SLOT_OFFSET_X + col * SLOT_SIZE;
                int y = guiTop  + SLOT_OFFSET_Y + visRow * SLOT_SIZE;
                if (!stack.isEmpty()) {
                    context.item(stack, x, y);
                }
            }
        }
    }

    private void drawTitle(GuiGraphicsExtractor context) {
        int titleX = guiLeft + 8;
        int titleY = guiTop + 6;
        context.text(
                this.font,
                this.title,
                titleX,
                titleY,
                0xFF404040,
                false
        );
    }

    private void renderHoveredTooltip(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        int slotIndex = getSlotIndexAt(mouseX, mouseY);
        if (slotIndex < 0) return;
        if (slotIndex >= stacks.size()) return;

        ItemStack stack = stacks.get(slotIndex);
        if (stack.isEmpty()) return;

        item tempItem = main.getItemList().get(stack.getItem().toString());

        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(tempItem.item_display_name).withStyle(tempItem.is_found ? ChatFormatting.GREEN : ChatFormatting.RED));

        if (tempItem.is_found) {
            lines.add(Component.literal("By: " + tempItem.item_founder).withStyle(ChatFormatting.AQUA));
            lines.add(Component.literal("At: " + tempItem.item_time).withStyle(ChatFormatting.AQUA));
        }

        context.setComponentTooltipForNextFrame(this.font, lines, mouseX, mouseY);
    }

    private int getSlotIndexAt(int mouseX, int mouseY) {
        int gridLeft = guiLeft + SLOT_OFFSET_X;
        int gridTop  = guiTop  + SLOT_OFFSET_Y;
        int gridRight  = gridLeft + COLUMNS * SLOT_SIZE;
        int gridBottom = gridTop  + VISIBLE_ROWS * SLOT_SIZE; // or ROWS if no scroll
        if (mouseX < gridLeft || mouseX >= gridRight || mouseY < gridTop || mouseY >= gridBottom) {
            return -1;
        }
        int col = (mouseX - gridLeft) / SLOT_SIZE;
        int visRow = (mouseY - gridTop) / SLOT_SIZE;
        // If you have scrolling, convert visible row -> actual row
        int row = visRow + scrollOffsetRow; // if you use scrollOffsetRow
        int index = row * COLUMNS + col;
        // Bounds check against your stacks list
        if (index < 0 || index >= stacks.size()) {
            return -1;
        }
        return index;
    }

    private void renderProgress(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        ItemStack progressStack = new ItemStack(Items.DIAMOND);

        int x = guiLeft + SLOT_OFFSET_X + SLOT_SIZE * 2;
        int y = guiTop  + SLOT_OFFSET_Y + VISIBLE_ROWS * SLOT_SIZE + 4;

        context.item(progressStack, x, y);

        int size = 16;
        if (mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal("Progress: " + main.getItemList().getProgString()).withStyle(ChatFormatting.AQUA));
            context.setComponentTooltipForNextFrame(this.font, lines, mouseX, mouseY);
        }
    }

    private void renderLeaderboard(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        ItemStack progressStack = new ItemStack(Items.OAK_HANGING_SIGN);

        int x = guiLeft + SLOT_OFFSET_X + SLOT_SIZE * 4;
        int y = guiTop  + SLOT_OFFSET_Y + VISIBLE_ROWS * SLOT_SIZE + 4;

        context.item(progressStack, x, y);

        int size = 16;
        if (mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size) {
            List<Component> lines = new ArrayList<>();
            List<String> raw = main.getItemList().getLeaderboard();
            for (int i = 0; i < raw.size(); i++) {
                String s = raw.get(i);
                if (i == 0) lines.add(Component.literal(s).withStyle(ChatFormatting.AQUA));
                else lines.add(Component.literal((i) + ". " + s).withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            context.setComponentTooltipForNextFrame(this.font, lines, mouseX, mouseY);
        }
    }

    private void renderFilter(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        ItemStack progressStack = new ItemStack(Items.HOPPER);

        int x = guiLeft + SLOT_OFFSET_X + SLOT_SIZE * 6;
        int y = guiTop  + SLOT_OFFSET_Y + VISIBLE_ROWS * SLOT_SIZE + 4;

        context.item(progressStack, x, y);

        int size = 16;
        if (mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal("Filter").withStyle(ChatFormatting.AQUA));
            context.setComponentTooltipForNextFrame(this.font, lines, mouseX, mouseY);
        }
    }

    private boolean isOverFilter(double mouseX, double mouseY) {
        int x = guiLeft + SLOT_OFFSET_X + SLOT_SIZE * 6;
        int y = guiTop  + SLOT_OFFSET_Y + VISIBLE_ROWS * SLOT_SIZE + 4;
        return mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y && mouseY < y + SLOT_SIZE;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (click.button() == 0 && canScroll() && isOverScrollbar(click.x(), click.y())) {
            scrolling = true;
            updateScrollFromMouse(click.y());
            return true;
        } else if (click.button() == 0 && isOverFilter(click.x(), click.y())) {
            onInventoryKeyPressed(Minecraft.getInstance(), !filtered);
        }
        return super.mouseClicked(click, doubled);
    }

    /////////////////////
    // Scrollbar Stuff //
    /////////////////////

    private float scrollPosition = 0.0f;
    private int scrollOffsetRow = 0;
    private boolean scrolling = false;

    private static final int SCROLLBAR_X = 175;
    private static final int SCROLLBAR_Y = 18;
    private static final int SCROLLBAR_HEIGHT = 110;
    private static final int SCROLLBAR_WIDTH = 12;
    private static final int KNOB_HEIGHT = 15;

    private static final int KNOB_TEX_HEIGHT = 15;
    private static final int KNOB_TEX_WIDTH = 12;

    private static final Identifier KNOB_TEXTURE =
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/container/creative_inventory/scroller.png");
    private static final Identifier KNOB_TEXTURE_DISABLES =
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/container/creative_inventory/scroller_disabled.png");

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!canScroll()) {
            return false;
        }

        float dir = (float) verticalAmount;
        int maxRows = getMaxScrollRows();
        if (maxRows <= 0) return false;

        float step = 1.0f / maxRows;
        scrollPosition = clamp(scrollPosition - dir * step, 0.0f, 1.0f);
        updateScrollFromPosition();
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
        if (click.button() == 0 && scrolling && canScroll()) {
            updateScrollFromMouse(click.y());
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (click.button() == 0) {
            scrolling = false;
        }
        return super.mouseReleased(click);
    }

    private void renderScrollbar(GuiGraphicsExtractor context) {
        Identifier tempTexture;
        if (!canScroll()) {
            tempTexture = KNOB_TEXTURE_DISABLES;
        } else {
            tempTexture = KNOB_TEXTURE;
        }

        int x = guiLeft + SCROLLBAR_X;
        int y = guiTop  + SCROLLBAR_Y;

        context.fill(x, y, x + SCROLLBAR_WIDTH, y + SCROLLBAR_HEIGHT, 0x00202020);

        int trackHeight = SCROLLBAR_HEIGHT - KNOB_HEIGHT;
        int knobY = y + (int) (scrollPosition * trackHeight);

        context.blit(
                RenderPipelines.GUI_TEXTURED,
                tempTexture,
                x, knobY,
                KNOB_TEX_WIDTH,
                KNOB_TEX_HEIGHT,
                KNOB_TEX_WIDTH,
                KNOB_TEX_HEIGHT,
                KNOB_TEX_WIDTH,
                KNOB_TEX_HEIGHT
        );
    }

    private int getTotalRows() {
        return (int) Math.ceil(stacks.size() / (double) COLUMNS);
    }

    private int getMaxScrollRows() {
        return Math.max(0, getTotalRows() - VISIBLE_ROWS);
    }

    private boolean canScroll() {
        return getMaxScrollRows() > 0;
    }

    private void updateScrollFromPosition() {
        int max = getMaxScrollRows();
        if (max <= 0) {
            scrollOffsetRow = 0;
            scrollPosition = 0.0f;
            return;
        }
        scrollOffsetRow = Math.round(scrollPosition * max);
    }

    private static float clamp(float value, float min, float max) {
        return value < min ? min : (value > max ? max : value);
    }

    private boolean isOverScrollbar(double mouseX, double mouseY) {
        int x = guiLeft + SCROLLBAR_X;
        int y = guiTop  + SCROLLBAR_Y;
        return mouseX >= x && mouseX < x + SCROLLBAR_WIDTH
                && mouseY >= y && mouseY < y + SCROLLBAR_HEIGHT;
    }

    private void updateScrollFromMouse(double mouseY) {
        int y = guiTop + SCROLLBAR_Y;
        int trackHeight = SCROLLBAR_HEIGHT - KNOB_HEIGHT;
        float relative = (float) ((mouseY - y - KNOB_HEIGHT / 2.0) / trackHeight);
        scrollPosition = clamp(relative, 0.0f, 1.0f);
        updateScrollFromPosition();
    }
}