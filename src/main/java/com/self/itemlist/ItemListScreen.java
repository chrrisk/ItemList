package com.self.itemlist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ItemListScreen {
    private static boolean isVisible = false;
    private static int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 128; // 8 columns x 16 rows
    private static final int COLUMNS = 8;
    private static final int ROWS = 16;
    private static final int SLOT_SIZE = 18;

    private static List<CustomItem> filteredItems = new ArrayList<>();
    private static CustomItem selectedItem = null;
    private static CustomItem hoveredItem = null;
    private static TextFieldWidget searchField = null;
    private static String searchQuery = "";

    // Recipe ingredient click detection
    private static int recipeGridX = 0;
    private static int recipeGridY = 0;
    private static CustomItem.RecipeIngredient[][] currentRecipePattern = null;

    public static void onScreenOpened(Screen screen) {
        if (screen instanceof HandledScreen) {
            isVisible = true;
            currentPage = 0;
            selectedItem = null;
            hoveredItem = null;
            updateFilteredItems();
            ItemList.LOGGER.info("ItemList screen opened. Total items available: {}", filteredItems.size());
        } else {
            isVisible = false;
            searchField = null;
        }
    }

    public static void render(DrawContext context, int mouseX, int mouseY, float delta, Screen screen) {
        if (!isVisible || !(screen instanceof HandledScreen)) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // Calculate panel position (right side)
        int panelWidth = COLUMNS * SLOT_SIZE + 20;
        int panelHeight = ROWS * SLOT_SIZE + 40; // Reduced to make room for search at bottom
        int panelX = screenWidth - panelWidth - 5;
        int panelY = (screenHeight - panelHeight - 25) / 2; // Adjusted for search bar

        // Draw background panel
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xC0000000);
        context.drawBorder(panelX, panelY, panelWidth, panelHeight, 0xFF8B8B8B);

        // Draw title
        context.drawText(client.textRenderer, Text.literal("Item List"), panelX + 10, panelY + 8, 0xFFFFFF, true);

        // Calculate item grid position
        int gridX = panelX + 10;
        int gridY = panelY + 20;

        // Reset hovered item
        hoveredItem = null;

        // Render items
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filteredItems.size());

        for (int i = startIndex; i < endIndex; i++) {
            int index = i - startIndex;
            int col = index % COLUMNS;
            int row = index / COLUMNS;

            int x = gridX + col * SLOT_SIZE;
            int y = gridY + row * SLOT_SIZE;

            // Draw slot background
            context.fill(x, y, x + 16, y + 16, 0x8B000000);

            // Check if hovered
            boolean isHovered = mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16;
            if (isHovered) {
                context.fill(x, y, x + 16, y + 16, 0x80FFFFFF);
                hoveredItem = filteredItems.get(i);
            }

            // Render item
            CustomItem item = filteredItems.get(i);
            ItemStack stack = item.toItemStack();
            context.drawItem(stack, x, y);
        }

        // Draw search bar at the bottom
        int searchBarY = panelY + panelHeight + 5;

        // Initialize search field if needed
        if (searchField == null) {
            searchField = new TextFieldWidget(client.textRenderer, panelX + 10, searchBarY, panelWidth - 90, 16, Text.literal("Search"));
            searchField.setMaxLength(100);
            searchField.setText(searchQuery);
            searchField.setPlaceholder(Text.literal("Search items..."));
            searchField.setEditable(true);
            searchField.setFocusUnlocked(true);
            searchField.setDrawsBackground(true);
            searchField.setChangedListener(text -> {
                searchQuery = text;
                currentPage = 0;
                updateFilteredItems();
                ItemList.LOGGER.info("Search query changed to: '{}'", text);
            });
        } else {
            searchField.setX(panelX + 10);
            searchField.setY(searchBarY);
            searchField.setWidth(panelWidth - 90);
        }

        // Draw search bar background with focus indicator
        int searchBgColor = searchField != null && searchField.isFocused() ? 0xC0003300 : 0xC0000000;
        context.fill(panelX, searchBarY - 2, panelX + panelWidth, searchBarY + 18, searchBgColor);
        context.drawBorder(panelX, searchBarY - 2, panelWidth, 20, searchField != null && searchField.isFocused() ? 0xFF00FF00 : 0xFF8B8B8B);

        searchField.render(context, mouseX, mouseY, delta);

        // Draw page navigation buttons
        int totalPages = (int) Math.ceil((double) filteredItems.size() / ITEMS_PER_PAGE);
        int buttonY = searchBarY;
        int buttonWidth = 15;
        int buttonHeight = 16;

        // Previous page button (<)
        int prevButtonX = panelX + panelWidth - 75;
        boolean prevHovered = mouseX >= prevButtonX && mouseX < prevButtonX + buttonWidth &&
                mouseY >= buttonY && mouseY < buttonY + buttonHeight;
        context.fill(prevButtonX, buttonY, prevButtonX + buttonWidth, buttonY + buttonHeight,
                prevHovered ? 0xFF555555 : 0xFF333333);
        context.drawBorder(prevButtonX, buttonY, buttonWidth, buttonHeight, 0xFF8B8B8B);
        context.drawText(client.textRenderer, Text.literal("<"), prevButtonX + 4, buttonY + 4,
                currentPage > 0 ? 0xFFFFFF : 0x888888, false);

        // Page number display
        String pageText = (currentPage + 1) + "/" + Math.max(1, totalPages);
        int pageTextWidth = client.textRenderer.getWidth(pageText);
        int pageTextX = prevButtonX + buttonWidth + 5;
        context.drawText(client.textRenderer, Text.literal(pageText),
                pageTextX, buttonY + 4, 0xFFFFFF, true);

        // Next page button (>)
        int nextButtonX = pageTextX + pageTextWidth + 5;
        boolean nextHovered = mouseX >= nextButtonX && mouseX < nextButtonX + buttonWidth &&
                mouseY >= buttonY && mouseY < buttonY + buttonHeight;
        context.fill(nextButtonX, buttonY, nextButtonX + buttonWidth, buttonY + buttonHeight,
                nextHovered ? 0xFF555555 : 0xFF333333);
        context.drawBorder(nextButtonX, buttonY, buttonWidth, buttonHeight, 0xFF8B8B8B);
        context.drawText(client.textRenderer, Text.literal(">"), nextButtonX + 4, buttonY + 4,
                currentPage < totalPages - 1 ? 0xFFFFFF : 0x888888, false);

        // Render clicked item details overlay on the left
        if (selectedItem != null) {
            renderItemDetailsOverlay(context, client, selectedItem, screenWidth, screenHeight, mouseX, mouseY);
        }

        // Render hover tooltip last (on top of everything) - ALWAYS show when hovering
        if (hoveredItem != null) {
            renderHoverTooltip(context, client, hoveredItem, mouseX, mouseY);
        }
    }

    private static void renderHoverTooltip(DrawContext context, MinecraftClient client, CustomItem item, int mouseX, int mouseY) {
        List<Text> tooltip = buildCustomItemTooltip(client, item);
        context.drawTooltip(client.textRenderer, tooltip, mouseX, mouseY);
    }

    private static void renderItemDetailsOverlay(DrawContext context, MinecraftClient client, CustomItem item, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        HandledScreen<?> handledScreen = (HandledScreen<?>) client.currentScreen;

        // Get inventory position
        int inventoryX = (screenWidth - 176) / 2;
        int inventoryY = (screenHeight - 166) / 2;

        // Calculate overlay position (left side of inventory, moved 20 pixels further left)
        int overlayWidth = 160;
        int overlayHeight = 200;
        int overlayX = inventoryX - overlayWidth - 25; // Added 20 pixels
        int overlayY = inventoryY;

        // Draw semi-transparent background
        context.fill(overlayX, overlayY, overlayX + overlayWidth, overlayY + overlayHeight, 0xE0000000);
        context.drawBorder(overlayX, overlayY, overlayWidth, overlayHeight, 0xFFFFAA00);

        int contentX = overlayX + 8;
        int contentY = overlayY + 8;

        // Draw item name
        context.drawText(client.textRenderer, Text.literal("Recipe"), contentX, contentY, 0xFFFFAA00, true);
        contentY += 15;

        List<Text> wrappedName = wrapText(client, item.getName(), overlayWidth - 16);
        for (Text line : wrappedName) {
            context.drawText(client.textRenderer, line, contentX, contentY, 0xFFFFFF, false);
            contentY += 10;
        }

        contentY += 5;

        // Draw crafting grid placeholder
        int gridSize = 54; // 3x3 grid with 18px slots
        int gridX = contentX + (overlayWidth - 16 - gridSize) / 2;
        int gridY = contentY;

        ItemStack hoveredRecipeStack = ItemStack.EMPTY;
        List<Text> hoveredRecipeTooltip = null;

        // Store grid position for click detection
        recipeGridX = gridX;
        recipeGridY = gridY;

        // Check if item has a recipe
        boolean hasRecipe = !item.getRecipes().isEmpty();

        // Store current recipe pattern for click detection
        currentRecipePattern = hasRecipe ? item.getRecipes().get(0).getPattern() : null;

        // Draw 3x3 crafting grid
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotX = gridX + col * 18;
                int slotY = gridY + row * 18;
                context.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF3F3F3F);
                context.drawBorder(slotX, slotY, 16, 16, 0xFF8B8B8B);

                // Render recipe ingredients if available
                if (hasRecipe) {
                    CustomItem.CraftingRecipe recipe = item.getRecipes().get(0);
                    CustomItem.RecipeIngredient[][] pattern = recipe.getPattern();

                    if (pattern[row][col] != null) {
                        ItemStack ingredientStack = pattern[row][col].toItemStack();
                        context.drawItem(ingredientStack, slotX, slotY);

                        // Highlight if it's a custom item (clickable)
                        if (pattern[row][col].isCustomItem()) {
                            // Draw subtle blue border to indicate it's clickable
                            context.drawBorder(slotX, slotY, 16, 16, 0x8800AAFF);
                        }

                        // Draw count if > 1 (AFTER the item so it's on top)
                        if (pattern[row][col].getCount() > 1) {
                            String countText = String.valueOf(pattern[row][col].getCount());
                            // Draw with shadow for better visibility
                            context.getMatrices().push();
                            context.getMatrices().translate(0, 0, 200);
                            context.drawText(client.textRenderer, Text.literal(countText),
                                    slotX + 17 - client.textRenderer.getWidth(countText),
                                    slotY + 9, 0xFFFFFF, true);
                            context.getMatrices().pop();
                        }

                        boolean slotHovered = mouseX >= slotX && mouseX < slotX + 16 && mouseY >= slotY && mouseY < slotY + 16;
                        if (slotHovered) {
                            hoveredRecipeStack = ingredientStack;
                            hoveredRecipeTooltip = null;
                            if (pattern[row][col].isCustomItem()) {
                                CustomItem customIngredient = ItemRegistry.getItemById(pattern[row][col].getMaterial());
                                if (customIngredient != null) {
                                    hoveredRecipeTooltip = buildCustomItemTooltip(client, customIngredient);
                                }
                            }
                        }
                    }
                }
            }
        }

        // Draw crafting table between the grid and result
        int craftingTableX = gridX + gridSize + 12;
        int craftingTableY = gridY + 19; // Center vertically with the grid

        // Draw crafting table slot
        context.fill(craftingTableX, craftingTableY, craftingTableX + 16, craftingTableY + 16, 0xFF3F3F3F);
        context.drawBorder(craftingTableX, craftingTableY, 16, 16, 0xFF8B8B8B);

        // Draw crafting table item
        ItemStack craftingTableStack = new ItemStack(net.minecraft.item.Items.CRAFTING_TABLE);
        context.drawItem(craftingTableStack, craftingTableX, craftingTableY);

        // Draw result slot to the right of crafting table
        int resultX = craftingTableX + 24;
        int resultY = craftingTableY;
        context.fill(resultX, resultY, resultX + 16, resultY + 16, 0xFF3F3F3F);
        context.drawBorder(resultX, resultY, 16, 16, 0xFFFFAA00);

        // Draw result item
        ItemStack resultStack = item.toItemStack();
        context.drawItem(resultStack, resultX, resultY);
        boolean resultHovered = mouseX >= resultX && mouseX < resultX + 16 && mouseY >= resultY && mouseY < resultY + 16;
        if (resultHovered) {
            hoveredRecipeStack = resultStack;
            hoveredRecipeTooltip = buildCustomItemTooltip(client, item);
        }

        contentY = gridY + gridSize + 16;

        // Draw "How to Obtain" section
        context.drawText(client.textRenderer, Text.literal("How to Obtain:"), contentX, contentY, 0xFFFFAA00, true);
        contentY += 12;

        // Draw obtain text from item data
        String obtainText = item.getObtain();
        if (obtainText == null || obtainText.isEmpty()) {
            obtainText = "No information available";
        }
        List<Text> wrappedObtain = wrapText(client, obtainText, overlayWidth - 16);
        for (Text line : wrappedObtain) {
            context.drawText(client.textRenderer, line, contentX, contentY, 0xAAAAAA, false);
            contentY += 10;
        }

        // Add close instruction
        contentY = overlayY + overlayHeight - 15;
        context.drawText(client.textRenderer, Text.literal("Click again to close"), contentX, contentY, 0x888888, false);

        if (hoveredRecipeTooltip != null && !hoveredRecipeTooltip.isEmpty()) {
            context.drawTooltip(client.textRenderer, hoveredRecipeTooltip, mouseX, mouseY);
        } else if (!hoveredRecipeStack.isEmpty()) {
            context.drawItemTooltip(client.textRenderer, hoveredRecipeStack, mouseX, mouseY);
        }
    }

    private static List<Text> buildCustomItemTooltip(MinecraftClient client, CustomItem item) {
        List<Text> tooltip = new ArrayList<>();
        tooltip.add(Text.literal(item.getName()));

        if (!item.getDescription().isEmpty()) {
            tooltip.add(Text.literal(""));
            tooltip.addAll(wrapText(client, item.getDescription(), 200));
        }

        if (!item.getLore().isEmpty()) {
            tooltip.add(Text.literal(""));
            for (String loreLine : item.getLore()) {
                tooltip.add(Text.literal(loreLine));
            }
        }

        return tooltip;
    }

    private static List<Text> wrapText(MinecraftClient client, String text, int maxWidth) {
        List<Text> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
            if (client.textRenderer.getWidth(testLine) <= maxWidth) {
                currentLine = new StringBuilder(testLine);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(Text.literal(currentLine.toString()));
                }
                currentLine = new StringBuilder(word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(Text.literal(currentLine.toString()));
        }

        return lines;
    }

    public static boolean mouseClicked(double mouseX, double mouseY, int button, Screen screen) {
        if (!isVisible || !(screen instanceof HandledScreen)) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int panelWidth = COLUMNS * SLOT_SIZE + 20;
        int panelHeight = ROWS * SLOT_SIZE + 40;
        int panelX = screenWidth - panelWidth - 5;
        int panelY = (screenHeight - panelHeight - 25) / 2;

        // Calculate search bar position
        int searchBarY = panelY + panelHeight + 5;
        int searchBarX = panelX + 10;
        int searchBarWidth = panelWidth - 90;

        // Check if clicked on search bar area - prioritize this!
        if (mouseX >= searchBarX && mouseX < searchBarX + searchBarWidth &&
                mouseY >= searchBarY && mouseY < searchBarY + 16) {
            if (searchField != null) {
                searchField.setFocused(true);
                searchField.mouseClicked(mouseX, mouseY, button);
                ItemList.LOGGER.info("Search field clicked and focused");
                return true;
            }
        }

        // Unfocus search field if clicked elsewhere
        if (searchField != null && searchField.isFocused()) {
            boolean clickedOnSearch = searchField.mouseClicked(mouseX, mouseY, button);
            if (!clickedOnSearch) {
                searchField.setFocused(false);
            }
        }

        // Check page navigation buttons
        int buttonY = searchBarY;
        int buttonWidth = 15;
        int buttonHeight = 16;
        int totalPages = (int) Math.ceil((double) filteredItems.size() / ITEMS_PER_PAGE);

        // Previous button
        int prevButtonX = panelX + panelWidth - 75;
        if (mouseX >= prevButtonX && mouseX < prevButtonX + buttonWidth &&
                mouseY >= buttonY && mouseY < buttonY + buttonHeight) {
            if (currentPage > 0) {
                currentPage--;
                return true;
            }
        }

        // Next button
        String pageText = (currentPage + 1) + "/" + Math.max(1, totalPages);
        int pageTextWidth = client.textRenderer.getWidth(pageText);
        int pageTextX = prevButtonX + buttonWidth + 5;
        int nextButtonX = pageTextX + pageTextWidth + 5;
        if (mouseX >= nextButtonX && mouseX < nextButtonX + buttonWidth &&
                mouseY >= buttonY && mouseY < buttonY + buttonHeight) {
            if (currentPage < totalPages - 1) {
                currentPage++;
                return true;
            }
        }

        int gridX = panelX + 10;
        int gridY = panelY + 20;

        // Check if clicked on an item
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filteredItems.size());

        for (int i = startIndex; i < endIndex; i++) {
            int index = i - startIndex;
            int col = index % COLUMNS;
            int row = index / COLUMNS;

            int x = gridX + col * SLOT_SIZE;
            int y = gridY + row * SLOT_SIZE;

            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                CustomItem clickedItem = filteredItems.get(i);
                // Toggle selection
                if (selectedItem == clickedItem) {
                    selectedItem = null;
                } else {
                    selectedItem = clickedItem;
                }
                return true;
            }
        }

        // Check if clicked outside overlay to close it
        if (selectedItem != null) {
            int inventoryX = (screenWidth - 176) / 2;
            int inventoryY = (screenHeight - 166) / 2;
            int overlayWidth = 160;
            int overlayHeight = 200;
            int overlayX = inventoryX - overlayWidth - 25;
            int overlayY = inventoryY;

            // Check if clicked on a recipe ingredient (if it's a custom item)
            if (currentRecipePattern != null) {
                for (int row = 0; row < 3; row++) {
                    for (int col = 0; col < 3; col++) {
                        if (currentRecipePattern[row][col] != null &&
                                currentRecipePattern[row][col].isCustomItem()) {

                            int slotX = recipeGridX + col * 18;
                            int slotY = recipeGridY + row * 18;

                            if (mouseX >= slotX && mouseX < slotX + 16 &&
                                    mouseY >= slotY && mouseY < slotY + 16) {

                                // Look up the custom item
                                String itemId = currentRecipePattern[row][col].getMaterial();
                                CustomItem customItem = ItemRegistry.getItemById(itemId);

                                if (customItem != null) {
                                    selectedItem = customItem;
                                    ItemList.LOGGER.info("Clicked on recipe ingredient: {}", itemId);
                                    return true;
                                }
                            }
                        }
                    }
                }
            }

            // If clicked outside the overlay, close it
            if (mouseX < overlayX || mouseX > overlayX + overlayWidth ||
                    mouseY < overlayY || mouseY > overlayY + overlayHeight) {
                selectedItem = null;
                return true;
            }
        }

        return false;
    }

    public static boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isVisible) {
            return false;
        }

        // Handle search field input FIRST with highest priority
        if (searchField != null && searchField.isFocused()) {
            // Let the search field handle the key input
            if (searchField.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }

            // Handle text input manually for letters, numbers, and special characters
            if (keyCode != GLFW.GLFW_KEY_ESCAPE && keyCode != GLFW.GLFW_KEY_TAB) {
                // Allow all printable characters to pass through
                return true;
            }
        }

        // Close overlay with ESC
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && selectedItem != null) {
            selectedItem = null;
            return true;
        }

        // Page navigation (only if search field is not focused)
        if (searchField == null || !searchField.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
                int totalPages = (int) Math.ceil((double) filteredItems.size() / ITEMS_PER_PAGE);
                if (currentPage < totalPages - 1) {
                    currentPage++;
                    return true;
                }
            } else if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_PAGE_UP) {
                if (currentPage > 0) {
                    currentPage--;
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean charTyped(char chr, int modifiers) {
        if (!isVisible) {
            return false;
        }

        // Allow search field to receive character input when focused
        if (searchField != null && searchField.isFocused()) {
            boolean handled = searchField.charTyped(chr, modifiers);
            ItemList.LOGGER.info("Character typed: '{}', handled: {}", chr, handled);
            return handled;
        }

        return false;
    }

    private static void updateFilteredItems() {
        filteredItems = ItemRegistry.searchItems(searchQuery);
        ItemList.LOGGER.info("Updated filtered items. Query: '{}', Results: {}", searchQuery, filteredItems.size());
    }
}
