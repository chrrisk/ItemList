
package com.self.itemlist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class RecipeViewerScreen extends Screen {
    private final CustomItem item;
    private final Screen parent;
    private SimpleInventory inventory;
    private final int gridCols = 10;
    private final int gridRows = 7;
    private int outputCount = 1;

    public RecipeViewerScreen(CustomItem item, Screen parent) {
        super(Text.literal("Recipe: " + item.getName()));
        this.item = item;
        this.parent = parent;
        this.inventory = new SimpleInventory(70); // 10 rows x 7 columns = 70 slots

        // Set up the inventory with recipe items
        if (!item.getRecipes().isEmpty()) {
            CustomItem.CraftingRecipe recipe = item.getRecipes().get(0);

            // Place ingredients centered around column 3, row 4, moved up and left one (cols 1-3, rows 2-4)
            for (int row = 0; row < recipe.getRows() && row < 3; row++) {
                for (int col = 0; col < recipe.getCols() && col < 3; col++) {
                    if (recipe.getPattern()[row][col] != null) {
                        ItemStack ingredientStack = recipe.getPattern()[row][col].toItemStack();
                        inventory.setStack((row + 2) * gridCols + (col + 1), ingredientStack);
                    }
                }
            }

            // Place result on column 8, row 4 (0-based: row 3, col 7)
            ItemStack resultStack = item.toItemStack();
            outputCount = recipe.getOutputCount();
            resultStack.setCount(outputCount); // Show the actual output count
            inventory.setStack(3 * gridCols + 7, resultStack);

            // Add anvil at column 6, row 4 (0-based: row 3, col 5)
            inventory.setStack(3 * gridCols + 5, new ItemStack(Items.ANVIL));
        }
    }

    @Override
    protected void init() {
        // No back button needed
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Override to remove blur effect
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw solid background to remove blur
        context.fill(0, 0, this.width, this.height, 0xC0000000);

        // Draw title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);

        // Calculate inventory grid position (centered)
        int slotSize = 18;
        int gridWidth = gridCols * slotSize;
        int gridHeight = gridRows * slotSize;
        int gridX = (this.width - gridWidth) / 2;
        int gridY = (this.height - gridHeight) / 2;

        // Draw inventory background
        context.fill(gridX - 5, gridY - 5, gridX + gridWidth + 5, gridY + gridHeight + 5, 0xC0000000);
        context.drawBorder(gridX - 5, gridY - 5, gridWidth + 10, gridHeight + 10, 0xFF8B8B8B);

        // Draw grey stained glass pane border around the inventory, excluding the slot to the right of the result
        ItemStack glassPane = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                if ((row == 0 || row == gridRows - 1 || col == 0 || col == gridCols - 1) && !(col == gridCols - 2 && row == 3)) {
                    int slotX = gridX + col * slotSize;
                    int slotY = gridY + row * slotSize;
                    context.drawItem(glassPane, slotX, slotY);
                }
            }
        }

        // Fill all empty slots with gray stained glass panes, excluding border and the slot to the right of the result
        ItemStack grayGlassPane = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                int slotIndex = row * gridCols + col;
                if (inventory.getStack(slotIndex).isEmpty() && !(row == 0 || row == gridRows - 1 || col == 0 || col == gridCols - 1) && !(col == gridCols - 2 && row == 3)) {
                    int slotX = gridX + col * slotSize;
                    int slotY = gridY + row * slotSize;
                    context.drawItem(grayGlassPane, slotX, slotY);
                }
            }
        }



        // Render inventory slots
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                // Skip drawing the slot to the right of the result (col 8, row 3)
                if (col == gridCols - 2 && row == 3) {
                    continue;
                }
                int slotX = gridX + col * slotSize;
                int slotY = gridY + row * slotSize;

                // Draw slot background
                context.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF3F3F3F);
                context.drawBorder(slotX, slotY, 16, 16, 0xFF8B8B8B);
            }
        }

        // Render items
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                int slotX = gridX + col * slotSize;
                int slotY = gridY + row * slotSize;

                ItemStack stack = inventory.getStack(row * gridCols + col);
                if (!stack.isEmpty()) {
                    ItemStack displayStack = stack.copy();
                    displayStack.setCount(1);
                    context.drawItem(displayStack, slotX, slotY);
                }
            }
        }

        // Render counts on top
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                int slotX = gridX + col * slotSize;
                int slotY = gridY + row * slotSize;

                ItemStack stack = inventory.getStack(row * gridCols + col);
                if (!stack.isEmpty()) {
                    // Draw count for recipe output in typical Minecraft position
                    if (col == 7 && row == 3 && stack.getCount() > 1) {
                        String countText = String.valueOf(stack.getCount());
                        int textX = slotX + 16 - this.textRenderer.getWidth(countText);
                        int textY = slotY + 9;
                        // make sure the count renders above the item instead of beneath it
                        context.getMatrices().push();
                        context.getMatrices().translate(0.0f, 0.0f, 200.0f);
                        context.drawText(this.textRenderer, Text.literal(countText), textX, textY, 0xFFFFFF, false);
                        context.getMatrices().pop();
                    }
                }
            }
        }

        // Render info box to the left of the inventory
        renderRecipeInfoBox(context, this.item, gridX, gridY);

        // Render tooltips for hovered items
        renderTooltips(context, mouseX, mouseY);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderRecipeInfoBox(DrawContext context, CustomItem item, int gridX, int gridY) {
        // Position the info box to the left of the inventory grid
        int boxWidth = 200;
        int boxHeight = item.getRecipes().isEmpty() ? 160 : 120; // Increase height if no recipes to show obtain info

        // Position box to the left of the inventory
        int boxX = gridX - boxWidth - 10;
        int boxY = gridY;

        // Clamp to screen edges
        if (boxX < 0) {
            boxX = 0;
        }
        if (boxY < 0) {
            boxY = 0;
        } else if (boxY + boxHeight > this.height) {
            boxY = this.height - boxHeight;
        }

        // Draw background
        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xC0000000);
        context.drawBorder(boxX, boxY, boxWidth, boxHeight, 0xFF8B8B8B);

        // Draw item name
        context.drawText(this.textRenderer, Text.literal(item.getName()), boxX + 5, boxY + 5, 0xFFFFFF, true);

        // Draw description with wrapping
        int yOffset = boxY + 20;
        Text descText = Text.literal("Description: " + item.getDescription());
        context.drawTextWrapped(this.textRenderer, descText, boxX + 5, yOffset, boxWidth - 10, 0xAAAAAA);
        yOffset += this.textRenderer.getWrappedLinesHeight(descText, boxWidth - 10) + 5;

        // Draw lore with wrapping
        for (String loreLine : item.getLore()) {
            Text loreText = Text.literal(loreLine);
            context.drawTextWrapped(this.textRenderer, loreText, boxX + 5, yOffset, boxWidth - 10, 0xAAAAAA);
            yOffset += this.textRenderer.getWrappedLinesHeight(loreText, boxWidth - 10) + 5;
        }

        // If no recipes, show how to obtain
        if (item.getRecipes().isEmpty()) {
            Text obtainText = Text.literal("How to obtain: " + item.getObtain());
            context.drawTextWrapped(this.textRenderer, obtainText, boxX + 5, yOffset, boxWidth - 10, 0xAAAAAA);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle clicks on inventory slots
        int slotSize = 18;
        int gridWidth = gridCols * slotSize;
        int gridHeight = gridRows * slotSize;
        int gridX = (this.width - gridWidth) / 2;
        int gridY = (this.height - gridHeight) / 2;

        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                int slotX = gridX + col * slotSize;
                int slotY = gridY + row * slotSize;

                if (mouseX >= slotX && mouseX < slotX + 16 && mouseY >= slotY && mouseY < slotY + 16) {
                    ItemStack stack = inventory.getStack(row * gridCols + col);
                    if (!stack.isEmpty()) {
                        // Check if this is a custom item ingredient
                        if (!item.getRecipes().isEmpty()) {
                            CustomItem.CraftingRecipe recipe = item.getRecipes().get(0);
                            if (row >= 2 && row <= 4 && col >= 1 && col <= 3) {
                                int patRow = row - 2;
                                int patCol = col - 1;
                                if (patRow < recipe.getRows() && patCol < recipe.getCols() && recipe.getPattern()[patRow][patCol] != null) {
                                    CustomItem.RecipeIngredient ri = recipe.getPattern()[patRow][patCol];
                                    if (ri.isCustomItem()) {
                                        CustomItem ingredient = ItemRegistry.getItemById(ri.getMaterial());
                                        if (ingredient != null) {
                                            // Open recipe for this ingredient
                                            MinecraftClient.getInstance().setScreen(new RecipeViewerScreen(ingredient, this));
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void renderTooltips(DrawContext context, int mouseX, int mouseY) {
        // Calculate inventory grid position (centered)
        int slotSize = 18;
        int gridWidth = gridCols * slotSize;
        int gridHeight = gridRows * slotSize;
        int gridX = (this.width - gridWidth) / 2;
        int gridY = (this.height - gridHeight) / 2;

        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                int slotX = gridX + col * slotSize;
                int slotY = gridY + row * slotSize;

                if (mouseX >= slotX && mouseX < slotX + 16 && mouseY >= slotY && mouseY < slotY + 16) {
                    ItemStack stack = inventory.getStack(row * gridCols + col);
                    if (!stack.isEmpty()) {
                        List<Text> tooltip = new java.util.ArrayList<>();
                        if (!stack.getItem().equals(Items.ANVIL)) {
                            tooltip.add(Text.literal(stack.getName().getString()));
                            // Add additional info for custom items
                            for (CustomItem customItem : ItemRegistry.getAllItems()) {
                                if (customItem.toItemStack().getItem() == stack.getItem()) {
                                    // Add lore
                                    for (String loreLine : customItem.getLore()) {
                                        tooltip.add(Text.literal(loreLine));
                                    }
                                    // Add description
                                    if (!customItem.getDescription().isEmpty()) {
                                        tooltip.add(Text.literal("Description: " + customItem.getDescription()));
                                    }
                                    // Add obtain info
                                    if (!customItem.getObtain().isEmpty()) {
                                        tooltip.add(Text.literal("Obtain: " + customItem.getObtain()));
                                    }
                                    // Add category
                                    if (!customItem.getCategory().isEmpty()) {
                                        tooltip.add(Text.literal("Category: " + customItem.getCategory()));
                                    }
                                    break;
                                }
                            }
                        }
                        if (!tooltip.isEmpty()) {
                            context.drawTooltip(this.textRenderer, tooltip, mouseX, mouseY);
                        }
                        return; // Only show tooltip for the first hovered item
                    }
                }
            }
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
