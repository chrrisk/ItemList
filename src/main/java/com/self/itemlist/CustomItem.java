package com.self.itemlist;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.List;
import java.util.ArrayList;

public class CustomItem {
    private String id;
    private String name;
    private String material;
    private List<String> lore;
    private String description;
    private String obtain;
    private String category;
    private List<CraftingRecipe> recipes;

    public CustomItem(String id, String name, String material, List<String> lore, String description, String obtain, String category) {
        this.id = id;
        this.name = name;
        this.material = material;
        this.lore = lore != null ? lore : new ArrayList<>();
        this.description = description != null ? description : "";
        this.obtain = obtain != null && !obtain.isEmpty() ? obtain : "No information available";
        this.category = category != null ? category : "Misc";
        this.recipes = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMaterial() {
        return material;
    }

    public List<String> getLore() {
        return lore;
    }

    public String getDescription() {
        return description;
    }

    public String getObtain() {
        return obtain;
    }

    public String getCategory() {
        return category;
    }

    public List<CraftingRecipe> getRecipes() {
        return recipes;
    }

    public void addRecipe(CraftingRecipe recipe) {
        this.recipes.add(recipe);
    }

    public ItemStack toItemStack() {
        try {
            Identifier itemId = Identifier.tryParse(material);
            if (itemId != null && Registries.ITEM.containsId(itemId)) {
                ItemStack stack = new ItemStack(Registries.ITEM.get(itemId));
                stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME, Text.literal(name));
                return stack;
            }
        } catch (Exception e) {
            ItemList.LOGGER.error("Failed to create ItemStack for: " + material, e);
        }
        return new ItemStack(Items.BARRIER);
    }

    public boolean matchesSearch(String query) {
        if (query == null || query.isEmpty()) {
            return true;
        }

        String lowerQuery = query.toLowerCase();

        if (name.toLowerCase().contains(lowerQuery)) {
            return true;
        }

        for (String loreLine : lore) {
            if (loreLine.toLowerCase().contains(lowerQuery)) {
                return true;
            }
        }

        return false;
    }

    public static class CraftingRecipe {
        private RecipeIngredient[][] pattern;
        private int rows;
        private int cols;
        private int outputCount;

        public CraftingRecipe(RecipeIngredient[][] pattern) {
            this(pattern, 1);
        }

        public CraftingRecipe(RecipeIngredient[][] pattern, int outputCount) {
            this.pattern = pattern;
            this.rows = pattern.length;
            this.cols = pattern.length > 0 ? pattern[0].length : 0;
            this.outputCount = outputCount;
        }

        public RecipeIngredient[][] getPattern() {
            return pattern;
        }

        public int getRows() {
            return rows;
        }

        public int getCols() {
            return cols;
        }

        public int getOutputCount() {
            return outputCount;
        }
    }

    public static class RecipeIngredient {
        private String material;
        private int count;
        private boolean isCustomItem;

        public RecipeIngredient(String material, int count) {
            this.material = material;
            this.count = count;
            // Check if it's a custom item ID (doesn't start with "minecraft:")
            this.isCustomItem = !material.startsWith("minecraft:");
        }

        public String getMaterial() {
            return material;
        }

        public int getCount() {
            return count;
        }

        public boolean isCustomItem() {
            return isCustomItem;
        }

        public ItemStack toItemStack() {
            try {
                if (isCustomItem) {
                    // Look up custom item by ID
                    CustomItem customItem = ItemRegistry.getItemById(material);
                    if (customItem != null) {
                        ItemStack stack = customItem.toItemStack();
                        stack.setCount(count);
                        return stack;
                    }
                } else {
                    Identifier itemId = Identifier.tryParse(material);
                    if (itemId != null && Registries.ITEM.containsId(itemId)) {
                        ItemStack stack = new ItemStack(Registries.ITEM.get(itemId), count);
                        return stack;
                    }
                }
            } catch (Exception e) {
                ItemList.LOGGER.error("Failed to create ItemStack for recipe ingredient: " + material, e);
            }
            return new ItemStack(Items.BARRIER, count);
        }
    }
}