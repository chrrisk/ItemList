package com.self.itemlist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
    private static CustomItem hoveredItem = null;
    private static TextFieldWidget searchField = null;
    private static String searchQuery = "";
    private static List<String> filters = new ArrayList<>();
    private static java.util.Set<String> selectedFilters = new java.util.HashSet<>();

    // Filter item mappings
    private static final java.util.Map<String, String> filterItems = new java.util.HashMap<>();
    static {
        filterItems.put("All", "minecraft:chest");
        filterItems.put("Materials", "minecraft:diamond");
        filterItems.put("Weapons", "minecraft:diamond_sword");
        filterItems.put("Food", "minecraft:apple");
        filterItems.put("Armor", "minecraft:diamond_boots");
        filterItems.put("Misc", "minecraft:barrier");
        filterItems.put("Sticks", "minecraft:stick");
        filterItems.put("Tools", "minecraft:diamond_pickaxe");
        filterItems.put("Blocks", "minecraft:stone");
        filterItems.put("Potions", "minecraft:potion");
        filterItems.put("Enchantments", "minecraft:enchanted_book");
        filterItems.put("Redstone", "minecraft:redstone");
        filterItems.put("Nether", "minecraft:nether_brick");
        filterItems.put("End", "minecraft:end_crystal");
        filterItems.put("Farming", "minecraft:diamond_hoe");
        filterItems.put("Woodcutting", "minecraft:diamond_axe");
        filterItems.put("Cooking", "minecraft:bowl");
        filterItems.put("Artisan", "minecraft:anvil");
        filterItems.put("Fishing", "minecraft:fishing_rod");
        filterItems.put("MobDrops", "minecraft:rotten_flesh");
        filterItems.put("Ammo", "minecraft:arrow");
        filterItems.put("Augments", "minecraft:player_head");
        filterItems.put("Melee", "minecraft:diamond_sword");
        filterItems.put("Ranger", "minecraft:bow");
        filterItems.put("Mage", "minecraft:blaze_rod");
    }



    public static void onScreenOpened(Screen screen) {
        if (screen instanceof HandledScreen) {
            isVisible = true;
            currentPage = 0;
            hoveredItem = null;
            selectedFilters.clear();
            selectedFilters.add("All");
            updateFilters();
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

        // Calculate filter rows and total height needed
        int buttonSize = 18;
        int buttonSpacing = 2;
        int filtersPerRow = (panelWidth - 20) / (buttonSize + buttonSpacing);
        int filterRows = (int) Math.ceil((double) filters.size() / filtersPerRow);
        int filterHeight = filterRows * (buttonSize + buttonSpacing) - buttonSpacing;

        int gridHeight = ROWS * SLOT_SIZE;
        int panelX = screenWidth - panelWidth - 5;
        int panelY = (screenHeight - (25 + gridHeight + 10 + filterHeight + 10 + 20) - 25) / 2; // Adjusted for search bar
        int panelHeight = 25 + gridHeight + 10 + filterHeight + 10 + 20; // title + grid + spacing + filters + spacing + search bar
        int filterBarY = panelY + 25 + gridHeight + 10;
        int searchBarY = panelY + 25 + gridHeight + 10 + filterHeight + 10;

        // Draw background panel
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xC0000000);
        context.drawBorder(panelX, panelY, panelWidth, panelHeight, 0xFF8B8B8B);

        // Draw title
        context.drawText(client.textRenderer, Text.literal("Item List"), panelX + 10, panelY + 8, 0xFFFFFF, true);

        // Draw separator line below title
        int separatorY = panelY + 18;
        context.fill(panelX + 5, separatorY, panelX + panelWidth - 5, separatorY + 1, 0xFF555555);

        // Calculate item grid position
        int gridX = panelX + 10;
        int gridY = panelY + 25;

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

        // Draw filter buttons above search bar
        int currentX = panelX + 10;
        int currentY = filterBarY;

        for (String filter : filters) {
            if (currentX + buttonSize > panelX + panelWidth - 10) {
                // Start new row
                currentX = panelX + 10;
                currentY += buttonSize + buttonSpacing;
            }

            boolean isSelected = selectedFilters.contains(filter);
            boolean isHovered = mouseX >= currentX && mouseX < currentX + buttonSize &&
                    mouseY >= currentY && mouseY < currentY + buttonSize;

            int buttonColor = isSelected ? 0xFF00AA00 : (isHovered ? 0xFF555555 : 0xFF333333);
            context.fill(currentX, currentY, currentX + buttonSize, currentY + buttonSize, buttonColor);
            context.drawBorder(currentX, currentY, buttonSize, buttonSize, 0xFF8B8B8B);

            // Draw filter item
            String itemId = filterItems.get(filter);
            if (itemId != null) {
                try {
                    net.minecraft.util.Identifier identifier = net.minecraft.util.Identifier.tryParse(itemId);
                    if (identifier != null && net.minecraft.registry.Registries.ITEM.containsId(identifier)) {
                        net.minecraft.item.ItemStack stack = new net.minecraft.item.ItemStack(net.minecraft.registry.Registries.ITEM.get(identifier));
                        context.drawItem(stack, currentX + 1, currentY + 1);
                    }
                } catch (Exception e) {
                    // Fallback to text if item fails
                    context.drawText(client.textRenderer, Text.literal(filter.substring(0, 1)), currentX + 6, currentY + 5, 0xFFFFFF, false);
                }
            }

            // Show tooltip on hover
            if (isHovered) {
                context.drawTooltip(client.textRenderer, java.util.List.of(Text.literal(filter)), mouseX, mouseY);
            }

            currentX += buttonSize + buttonSpacing;
        }

        // Adjust panel height if filter buttons extend beyond
        int adjustedFilterHeight = currentY + buttonSize - filterBarY;
        if (adjustedFilterHeight > 20) { // If more than one row
            // Note: For simplicity, assuming panel height is sufficient; in a real implementation, might need to adjust panelY or height
        }

        // Draw page navigation buttons
        int totalPages = (int) Math.ceil((double) filteredItems.size() / ITEMS_PER_PAGE);
        int navButtonY = searchBarY;
        int navButtonWidth = 15;
        int navButtonHeight = 16;

        // Previous page button (<)
        int prevButtonX = panelX + panelWidth - 75;
        boolean prevHovered = mouseX >= prevButtonX && mouseX < prevButtonX + navButtonWidth &&
                mouseY >= navButtonY && mouseY < navButtonY + navButtonHeight;
        context.fill(prevButtonX, navButtonY, prevButtonX + navButtonWidth, navButtonY + navButtonHeight,
                prevHovered ? 0xFF555555 : 0xFF333333);
        context.drawBorder(prevButtonX, navButtonY, navButtonWidth, navButtonHeight, 0xFF8B8B8B);
        context.drawText(client.textRenderer, Text.literal("<"), prevButtonX + 4, navButtonY + 4,
                currentPage > 0 ? 0xFFFFFF : 0x888888, false);

        // Page number display
        String pageText = (currentPage + 1) + "/" + Math.max(1, totalPages);
        int pageTextWidth = client.textRenderer.getWidth(pageText);
        int pageTextX = prevButtonX + navButtonWidth + 5;
        context.drawText(client.textRenderer, Text.literal(pageText),
                pageTextX, navButtonY + 4, 0xFFFFFF, true);

        // Next page button (>)
        int nextButtonX = pageTextX + pageTextWidth + 5;
        boolean nextHovered = mouseX >= nextButtonX && mouseX < nextButtonX + navButtonWidth &&
                mouseY >= navButtonY && mouseY < navButtonY + navButtonHeight;
        context.fill(nextButtonX, navButtonY, nextButtonX + navButtonWidth, navButtonY + navButtonHeight,
                nextHovered ? 0xFF555555 : 0xFF333333);
        context.drawBorder(nextButtonX, navButtonY, navButtonWidth, navButtonHeight, 0xFF8B8B8B);
        context.drawText(client.textRenderer, Text.literal(">"), nextButtonX + 4, navButtonY + 4,
                currentPage < totalPages - 1 ? 0xFFFFFF : 0x888888, false);

        // No longer render overlay - replaced with RecipeEditorScreen

        // Render items again on top to ensure they appear above the overlay
        for (int i = startIndex; i < endIndex; i++) {
            int index = i - startIndex;
            int col = index % COLUMNS;
            int row = index / COLUMNS;

            int x = gridX + col * SLOT_SIZE;
            int y = gridY + row * SLOT_SIZE;

            // Check if hovered and draw highlight
            boolean isHovered = mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16;
            if (isHovered) {
                context.fill(x, y, x + 16, y + 16, 0x80FFFFFF);
            }

            // Render item again on top
            CustomItem item = filteredItems.get(i);
            ItemStack stack = item.toItemStack();
            ItemStack displayStack = stack.copy();
            displayStack.setCount(1);
            context.drawItem(displayStack, x, y);

            // Draw count if > 1
            if (stack.getCount() > 1) {
                context.drawText(client.textRenderer, Text.literal(String.valueOf(stack.getCount())),
                        x + 18, y + 9, 0xFFFFFF, true);
            }
        }

        // Render standard tooltip for hovered item (rendered last to be on top of everything)
        if (hoveredItem != null) {
            renderHoverTooltip(context, client, hoveredItem, mouseX, mouseY);
        }
    }

    private static void renderHoverTooltip(DrawContext context, MinecraftClient client, CustomItem item, int mouseX, int mouseY) {
        List<Text> tooltip = buildCustomItemTooltip(client, item);
        context.drawTooltip(client.textRenderer, tooltip, mouseX, mouseY);
    }

    private static void renderInfoBox(DrawContext context, MinecraftClient client, CustomItem item, int mouseX, int mouseY) {
        // Calculate info box height based on lore
        int loreLines = item.getLore().size();
        int boxWidth = 200;
        int boxHeight = 60 + loreLines * 10; // Base 60 + 10 per lore line
        int boxX = mouseX - boxWidth - 10;
        int boxY = mouseY - boxHeight / 2;

        // If box would go off-screen, position to the right
        if (boxX < 0) {
            boxX = mouseX + 10;
        }

        // Ensure box stays on screen horizontally
        if (boxX + boxWidth > client.getWindow().getScaledWidth()) {
            boxX = client.getWindow().getScaledWidth() - boxWidth;
        }
        if (boxX < 0) {
            boxX = 0;
        }

        // Ensure box stays on screen vertically
        if (boxY < 0) {
            boxY = 0;
        } else if (boxY + boxHeight > client.getWindow().getScaledHeight()) {
            boxY = client.getWindow().getScaledHeight() - boxHeight;
        }

        // Draw background
        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xFF000000);
        context.drawBorder(boxX, boxY, boxWidth, boxHeight, 0xFF8B8B8B);

        // Draw item name
        context.drawText(client.textRenderer, Text.literal(item.getName()), boxX + 5, boxY + 5, 0xFFFFFF, true);

        // Draw category
        String category = item.getCategory() != null ? item.getCategory() : "Unknown";
        context.drawText(client.textRenderer, Text.literal("Category: " + category), boxX + 5, boxY + 20, 0xAAAAAA, false);

        // Draw description (truncated if too long)
        String desc = item.getDescription();
        if (desc.length() > 30) {
            desc = desc.substring(0, 27) + "...";
        }
        context.drawText(client.textRenderer, Text.literal("Desc: " + desc), boxX + 5, boxY + 35, 0xAAAAAA, false);

        // Draw lore
        int yOffset = boxY + 50;
        for (String loreLine : item.getLore()) {
            context.drawText(client.textRenderer, Text.literal(loreLine), boxX + 5, yOffset, 0xAAAAAA, false);
            yOffset += 10;
        }
    }

    private static void renderRecipeInfoBox(DrawContext context, MinecraftClient client, CustomItem item, Screen screen) {
        // Position the info box to the left of the recipe chest GUI
        int boxWidth = 200;
        int boxHeight = 120;

        // Calculate chest GUI position
        int containerX = (screen.width - 176) / 2; // Chest GUI width is 176
        int containerY = (screen.height - 222) / 2; // Chest GUI height is 222

        // Position box to the left of the chest GUI
        int boxX = containerX - boxWidth - 10;
        int boxY = containerY;

        // Clamp to screen edges
        if (boxX < 0) {
            boxX = 0;
        }
        if (boxY < 0) {
            boxY = 0;
        } else if (boxY + boxHeight > screen.height) {
            boxY = screen.height - boxHeight;
        }

        // Draw background
        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xFF000000);
        context.drawBorder(boxX, boxY, boxWidth, boxHeight, 0xFF8B8B8B);

        // Draw item name
        context.drawText(client.textRenderer, Text.literal(item.getName()), boxX + 5, boxY + 5, 0xFFFFFF, true);

        // Draw description
        String desc = item.getDescription();
        if (desc.length() > 40) {
            desc = desc.substring(0, 37) + "...";
        }
        context.drawText(client.textRenderer, Text.literal("Description: " + desc), boxX + 5, boxY + 20, 0xAAAAAA, false);

        // Draw lore
        int yOffset = boxY + 35;
        for (String loreLine : item.getLore()) {
            context.drawText(client.textRenderer, Text.literal(loreLine), boxX + 5, yOffset, 0xAAAAAA, false);
            yOffset += 10;
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
        // Handle clicks in recipe chest screens
        if (screen instanceof GenericContainerScreen && screen.getTitle().getString().startsWith("Recipe: ")) {
            return handleRecipeChestClick(mouseX, mouseY, button, screen);
        }

        if (!isVisible || !(screen instanceof HandledScreen)) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // Calculate panel position (right side)
        int panelWidth = COLUMNS * SLOT_SIZE + 20;

        // Calculate filter rows and total height needed
        int buttonSize = 18;
        int buttonSpacing = 2;
        int filtersPerRow = (panelWidth - 20) / (buttonSize + buttonSpacing);
        int filterRows = (int) Math.ceil((double) filters.size() / filtersPerRow);
        int filterHeight = filterRows * (buttonSize + buttonSpacing) - buttonSpacing;

        int gridHeight = ROWS * SLOT_SIZE;
        int panelX = screenWidth - panelWidth - 5;
        int panelY = (screenHeight - (25 + gridHeight + 10 + filterHeight + 10 + 20) - 25) / 2; // Adjusted for search bar
        int panelHeight = 25 + gridHeight + 10 + filterHeight + 10 + 20; // title + grid + spacing + filters + spacing + search bar
        int filterBarY = panelY + 25 + gridHeight + 10;
        int searchBarY = panelY + 25 + gridHeight + 10 + filterHeight + 10;

        // Calculate search bar position
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


        int currentX = panelX + 10;
        int currentY = filterBarY;

        for (String filter : filters) {
            if (currentX + buttonSize > panelX + panelWidth - 10) {
                // Start new row
                currentX = panelX + 10;
                currentY += buttonSize + buttonSpacing;
            }

            if (mouseX >= currentX && mouseX < currentX + buttonSize &&
                    mouseY >= currentY && mouseY < currentY + buttonSize) {
                if (filter.equals("All")) {
                    selectedFilters.clear();
                    selectedFilters.add("All");
                } else {
                    selectedFilters.remove("All");
                    if (selectedFilters.contains(filter)) {
                        selectedFilters.remove(filter);
                        if (selectedFilters.isEmpty()) {
                            selectedFilters.add("All");
                        }
                    } else {
                        selectedFilters.add(filter);
                    }
                }
                currentPage = 0;
                updateFilteredItems();
                ItemList.LOGGER.info("Filters changed to: {}", selectedFilters);
                return true;
            }

            currentX += buttonSize + buttonSpacing;
        }

        // Check page navigation buttons
        int navButtonY = searchBarY;
        int navButtonWidth = 15;
        int navButtonHeight = 16;
        int totalPages = (int) Math.ceil((double) filteredItems.size() / ITEMS_PER_PAGE);

        // Previous button
        int prevButtonX = panelX + panelWidth - 75;
        if (mouseX >= prevButtonX && mouseX < prevButtonX + navButtonWidth &&
                mouseY >= navButtonY && mouseY < navButtonY + navButtonHeight) {
            if (currentPage > 0) {
                currentPage--;
                return true;
            }
        }

        // Next button
        String pageText = (currentPage + 1) + "/" + Math.max(1, totalPages);
        int pageTextWidth = client.textRenderer.getWidth(pageText);
        int pageTextX = prevButtonX + navButtonWidth + 5;
        int nextButtonX = pageTextX + pageTextWidth + 5;
        if (mouseX >= nextButtonX && mouseX < nextButtonX + navButtonWidth &&
                mouseY >= navButtonY && mouseY < navButtonY + navButtonHeight) {
            if (currentPage < totalPages - 1) {
                currentPage++;
                return true;
            }
        }

        int gridX = panelX + 10;
        int gridY = panelY + 25;

        // Check for right-click in item list area to reset filters
        if (button == 1 && mouseX >= gridX && mouseX < gridX + COLUMNS * SLOT_SIZE &&
                mouseY >= gridY && mouseY < gridY + ROWS * SLOT_SIZE) {
            selectedFilters.clear();
            selectedFilters.add("All");
            currentPage = 0;
            updateFilteredItems();
            ItemList.LOGGER.info("Filters reset to 'All' via right-click");
            return true;
        }

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
                // Open chest with recipe items
                openRecipeChest(clickedItem, screen);
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
        if (!selectedFilters.contains("All")) {
            filteredItems = filteredItems.stream()
                    .filter(item -> {
                        if (item.getCategory() == null) return false;
                        java.util.Set<String> itemCategories = java.util.Arrays.stream(item.getCategory().split(","))
                                .map(String::trim)
                                .collect(java.util.stream.Collectors.toSet());
                        return selectedFilters.stream().allMatch(itemCategories::contains);
                    })
                    .collect(java.util.stream.Collectors.toList());
        }
        ItemList.LOGGER.info("Updated filtered items. Query: '{}', Filters: '{}', Results: {}", searchQuery, selectedFilters, filteredItems.size());
    }

    private static void updateFilters() {
        filters.clear();
        filters.add("All");
        java.util.Set<String> uniqueWords = new java.util.HashSet<>();
        for (CustomItem item : ItemRegistry.getAllItems()) {
            String category = item.getCategory();
            if (category != null && !category.isEmpty()) {
                String[] words = category.split(",");
                for (String word : words) {
                    String trimmed = word.trim();
                    if (!trimmed.isEmpty() && !uniqueWords.contains(trimmed)) {
                        uniqueWords.add(trimmed);
                        filters.add(trimmed);
                    }
                }
            }
        }
        ItemList.LOGGER.info("Updated filters: {}", filters);
    }

    private static void openRecipeChest(CustomItem item, Screen parent) {
        if (item.getRecipes().isEmpty()) {
            ItemList.LOGGER.info("No recipes available for item: {}", item.getName());
            return;
        }

        // Open the custom recipe viewer screen instead of a chest
        MinecraftClient.getInstance().setScreen(new RecipeViewerScreen(item, parent));
    }

    private static boolean handleRecipeChestClick(double mouseX, double mouseY, int button, Screen screen) {
        if (!(screen instanceof GenericContainerScreen)) {
            return false;
        }

        GenericContainerScreen containerScreen = (GenericContainerScreen) screen;
        // Get the current recipe item from the title
        String title = screen.getTitle().getString();
        if (!title.startsWith("Recipe: ")) {
            return false;
        }
        String itemName = title.substring("Recipe: ".length());
        CustomItem currentItem = ItemRegistry.getAllItems().stream()
            .filter(item -> item.getName().equals(itemName))
            .findFirst().orElse(null);
        if (currentItem == null || currentItem.getRecipes().isEmpty()) {
            return false;
        }

        CustomItem.CraftingRecipe recipe = currentItem.getRecipes().get(0);

        // Calculate slot positions (assuming standard chest layout)
        int containerX = (containerScreen.width - 176) / 2; // Chest GUI width is 176
        int containerY = (containerScreen.height - 222) / 2; // Chest GUI height is 222 for 6 rows, but we have 7? Wait, let's check

        // For a 7-row chest (GENERIC_9X6 is 9x6=54 slots, but we used 7 rows? Wait, inconsistency)
        // Actually, ScreenHandlerType.GENERIC_9X6 is 9 columns x 6 rows = 54 slots, but we passed 7 as rows? Wait, that might be wrong.
        // But assuming it's working, slots are in rows 0-6, columns 0-8

        // Ingredient slots are in rows 0-4, columns 0-2 (top-left 3x5 grid)
        for (int row = 0; row < recipe.getRows() && row < 5; row++) {
            for (int col = 0; col < recipe.getCols() && col < 3; col++) {
                CustomItem.RecipeIngredient ri = recipe.getPattern()[row][col];
                if (ri != null && ri.isCustomItem()) {
                    // Slot position: each slot is 18x18, with 2px spacing? Standard chest slots are at x+8 + col*18, y+18 + row*18 or similar
                    int slotX = containerX + 8 + col * 18;
                    int slotY = containerY + 18 + row * 18;

                    if (mouseX >= slotX && mouseX < slotX + 16 && mouseY >= slotY && mouseY < slotY + 16) {
                        // Clicked on this ingredient, open its recipe
                        CustomItem ingredient = ItemRegistry.getItemById(ri.getMaterial());
                        if (ingredient != null) {
                            openRecipeChest(ingredient, screen);
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
