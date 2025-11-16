package com.self.itemlist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RecipeEditorScreen extends Screen {
    private final CustomItem item;
    private final Screen parent;
    private TextFieldWidget texturePathField;
    private ButtonWidget browseButton;
    private ButtonWidget saveButton;
    private ButtonWidget backButton;
    private List<String> pngFiles = new ArrayList<>();
    private int selectedPngIndex = -1;

    public RecipeEditorScreen(CustomItem item, Screen parent) {
        super(Text.literal("Recipe Editor - " + item.getName()));
        this.item = item;
        this.parent = parent;
        loadPngFiles();
    }

    private void loadPngFiles() {
        // Load PNG files from a textures directory (you can customize this path)
        File texturesDir = new File("config/itemlist/textures");
        if (texturesDir.exists() && texturesDir.isDirectory()) {
            File[] files = texturesDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
            if (files != null) {
                for (File file : files) {
                    pngFiles.add(file.getName());
                }
            }
        }
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Texture path field
        this.texturePathField = new TextFieldWidget(this.textRenderer, centerX - 100, centerY - 80, 150, 20, Text.literal("Texture Path"));
        this.texturePathField.setText(item.getMaterial().replace("minecraft:", ""));
        this.addSelectableChild(this.texturePathField);

        // Browse button
        this.browseButton = ButtonWidget.builder(Text.literal("Browse"), button -> openFileBrowser())
                .dimensions(centerX + 60, centerY - 80, 60, 20)
                .build();
        this.addDrawableChild(this.browseButton);

        // Save button
        this.saveButton = ButtonWidget.builder(Text.literal("Save"), button -> saveChanges())
                .dimensions(centerX - 50, centerY + 100, 60, 20)
                .build();
        this.addDrawableChild(this.saveButton);

        // Back button
        this.backButton = ButtonWidget.builder(Text.literal("Back"), button -> close())
                .dimensions(centerX + 20, centerY + 100, 60, 20)
                .build();
        this.addDrawableChild(this.backButton);
    }

    private void openFileBrowser() {
        // Simple file browser implementation
        // For now, just cycle through available PNG files
        if (!pngFiles.isEmpty()) {
            selectedPngIndex = (selectedPngIndex + 1) % pngFiles.size();
            texturePathField.setText(pngFiles.get(selectedPngIndex));
        }
    }

    private void saveChanges() {
        // Save the texture path change (this would need to be implemented properly)
        String newTexture = texturePathField.getText();
        ItemList.LOGGER.info("Saving texture change for {}: {}", item.getId(), newTexture);
        // Here you would update the item's material and save to file
        close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        // Draw title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);

        // Draw item info
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        context.drawText(this.textRenderer, Text.literal("Item: " + item.getName()), centerX - 100, centerY - 100, 0xFFFFFF, false);
        context.drawText(this.textRenderer, Text.literal("ID: " + item.getId()), centerX - 100, centerY - 90, 0xFFFFFF, false);

        // Draw recipe grid
        if (!item.getRecipes().isEmpty()) {
            CustomItem.CraftingRecipe recipe = item.getRecipes().get(0);
            int gridX = centerX - 100;
            int gridY = centerY - 50;

            for (int row = 0; row < recipe.getRows(); row++) {
                for (int col = 0; col < recipe.getCols(); col++) {
                    int slotX = gridX + col * 18;
                    int slotY = gridY + row * 18;

                    // Draw slot background
                    context.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF3F3F3F);
                    context.drawBorder(slotX, slotY, 16, 16, 0xFF8B8B8B);

                    // Draw ingredient
                    if (recipe.getPattern()[row][col] != null) {
                        ItemStack stack = recipe.getPattern()[row][col].toItemStack();
                        context.drawItem(stack, slotX, slotY);

                        // Draw count if > 1
                        if (recipe.getPattern()[row][col].getCount() > 1) {
                            context.drawText(this.textRenderer, Text.literal(String.valueOf(recipe.getPattern()[row][col].getCount())),
                                    slotX + 17 - this.textRenderer.getWidth(String.valueOf(recipe.getPattern()[row][col].getCount())),
                                    slotY + 9, 0xFFFFFF, true);
                        }
                    }
                }
            }

            // Draw result slot
            int resultX = gridX + recipe.getCols() * 18 + 24;
            int resultY = gridY + (recipe.getRows() * 18) / 2 - 8;
            context.fill(resultX, resultY, resultX + 16, resultY + 16, 0xFF3F3F3F);
            context.drawBorder(resultX, resultY, 16, 16, 0xFFFFAA00);

            ItemStack resultStack = item.toItemStack();
            context.drawItem(resultStack, resultX, resultY);
        }

        // Draw texture editing section
        context.drawText(this.textRenderer, Text.literal("Texture:"), centerX - 100, centerY - 85, 0xFFFFFF, false);
        texturePathField.render(context, mouseX, mouseY, delta);

        // Draw available PNG files
        if (!pngFiles.isEmpty()) {
            context.drawText(this.textRenderer, Text.literal("Available Textures:"), centerX - 100, centerY + 10, 0xFFFFFF, false);
            for (int i = 0; i < Math.min(pngFiles.size(), 10); i++) {
                int color = (i == selectedPngIndex) ? 0xFFFFAA00 : 0xAAAAAA;
                context.drawText(this.textRenderer, Text.literal(pngFiles.get(i)), centerX - 100, centerY + 25 + i * 10, color, false);
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers) || texturePathField.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return super.charTyped(chr, modifiers) || texturePathField.charTyped(chr, modifiers);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
