package com.self.itemlist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ItemRegistry {
    private static final List<CustomItem> ITEMS = new ArrayList<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void loadItems() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("itemlist");
        File itemsFile = configDir.resolve("items.json").toFile();

        ItemList.LOGGER.info("=== ItemList Loading ===");
        ItemList.LOGGER.info("Config directory: {}", configDir.toAbsolutePath());
        ItemList.LOGGER.info("Items file path: {}", itemsFile.getAbsolutePath());
        ItemList.LOGGER.info("Config dir exists: {}", Files.exists(configDir));
        ItemList.LOGGER.info("Items file exists: {}", itemsFile.exists());

        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
                ItemList.LOGGER.info("Created config directory: {}", configDir);
            }

            if (!itemsFile.exists()) {
                ItemList.LOGGER.info("items.json not found, creating default file...");
                createDefaultItemsFile(itemsFile);
                ItemList.LOGGER.info("Created items.json at: {}", itemsFile.getAbsolutePath());
            } else {
                ItemList.LOGGER.info("Found existing items.json, loading...");
            }

            loadItemsFromFile(itemsFile);
            ItemList.LOGGER.info("=== Successfully loaded {} items ===", ITEMS.size());

            // Log first few items for debugging
            if (!ITEMS.isEmpty()) {
                ItemList.LOGGER.info("Sample items loaded:");
                for (int i = 0; i < Math.min(3, ITEMS.size()); i++) {
                    CustomItem item = ITEMS.get(i);
                    ItemList.LOGGER.info("  - {} ({})", item.getName(), item.getId());
                }
            } else {
                ItemList.LOGGER.error("WARNING: No items were loaded!");
            }

        } catch (Exception e) {
            ItemList.LOGGER.error("CRITICAL: Failed to load items", e);
            e.printStackTrace();
        }
    }

    private static void createDefaultItemsFile(File file) throws IOException {
        JsonArray items = new JsonArray();

        items.add(buildEnchantedDiamondJson());
        items.add(buildEnchantedStickJson());
        items.add(buildEnchantedDiamondSwordJson());

        // Item 4: Magical Pickaxe
        JsonObject item2 = new JsonObject();
        item2.addProperty("id", "magical_pickaxe");
        item2.addProperty("name", "\u00A75Magical Pickaxe");
        item2.addProperty("material", "minecraft:diamond_pickaxe");
        JsonArray lore2 = new JsonArray();
        lore2.add("\u00A77Mining Speed: \u00A7a+200%");
        lore2.add("\u00A75\u00A7lEPIC");
        item2.add("lore", lore2);
        item2.addProperty("description", "Mines at incredible speed");
        item2.addProperty("obtain", "Craft using 3 Diamonds and 2 Sticks");
        JsonArray recipe2 = new JsonArray();
        recipe2.add(createRecipeRow("minecraft:diamond", "minecraft:diamond", "minecraft:diamond"));
        recipe2.add(createRecipeRow("", "minecraft:stick", ""));
        recipe2.add(createRecipeRow("", "minecraft:stick", ""));
        item2.add("recipe", recipe2);
        items.add(item2);

        // Item 5: Health Potion
        JsonObject item3 = new JsonObject();
        item3.addProperty("id", "health_potion");
        item3.addProperty("name", "\u00A7cHealth Potion");
        item3.addProperty("material", "minecraft:potion");
        JsonArray lore3 = new JsonArray();
        lore3.add("\u00A77Restores \u00A7c+200 HP");
        lore3.add("\u00A7a\u00A7lUNCOMMON");
        item3.add("lore", lore3);
        item3.addProperty("description", "Restores health instantly");
        item3.addProperty("obtain", "Brew or craft");
        JsonArray recipe3 = new JsonArray();
        recipe3.add(createRecipeRow("", "minecraft:glistering_melon_slice", ""));
        recipe3.add(createRecipeRow("minecraft:nether_wart", "minecraft:glass_bottle", "minecraft:glowstone_dust"));
        recipe3.add(createRecipeRow("", "minecraft:blaze_powder", ""));
        item3.add("recipe", recipe3);
        items.add(item3);

        // Item 6: Golden Apple
        JsonObject item4 = new JsonObject();
        item4.addProperty("id", "super_golden_apple");
        item4.addProperty("name", "\u00A76Super Golden Apple");
        item4.addProperty("material", "minecraft:golden_apple");
        JsonArray lore4 = new JsonArray();
        lore4.add("\u00A77Regeneration II");
        lore4.add("\u00A76\u00A7lLEGENDARY");
        item4.add("lore", lore4);
        item4.addProperty("description", "Provides powerful buffs");
        item4.addProperty("obtain", "Craft using Gold Blocks and Apple");
        JsonArray recipe4 = new JsonArray();
        recipe4.add(createRecipeRow("minecraft:gold_block", "minecraft:gold_block", "minecraft:gold_block"));
        recipe4.add(createRecipeRow("minecraft:gold_block", "minecraft:apple", "minecraft:gold_block"));
        recipe4.add(createRecipeRow("minecraft:gold_block", "minecraft:gold_block", "minecraft:gold_block"));
        item4.add("recipe", recipe4);
        items.add(item4);

        // Item 7: Speed Boots
        JsonObject item5 = new JsonObject();
        item5.addProperty("id", "speed_boots");
        item5.addProperty("name", "\u00A7bSpeed Boots");
        item5.addProperty("material", "minecraft:diamond_boots");
        JsonArray lore5 = new JsonArray();
        lore5.add("\u00A77Speed: \u00A7a+50%");
        lore5.add("\u00A79\u00A7lRARE");
        item5.add("lore", lore5);
        item5.addProperty("description", "Run faster with these boots");
        item5.addProperty("obtain", "Craft using Diamonds and Feathers");
        JsonArray recipe5 = new JsonArray();
        recipe5.add(createRecipeRow("", "", ""));
        recipe5.add(createRecipeRow("minecraft:diamond", "", "minecraft:diamond"));
        recipe5.add(createRecipeRow("minecraft:feather:2", "", "minecraft:feather:2"));
        item5.add("recipe", recipe5);
        items.add(item5);

        // Add more basic items without recipes
        for (int i = 8; i <= 150; i++) {
            JsonObject item = new JsonObject();
            item.addProperty("id", "custom_item_" + i);
            item.addProperty("name", "\u00A76Custom Item #" + i);
            item.addProperty("material", "minecraft:diamond");

            JsonArray lore = new JsonArray();
            lore.add("\u00A77This is item number " + i);
            lore.add("\u00A7aRarity: LEGENDARY");
            item.add("lore", lore);

            item.addProperty("description", "This is a custom item #" + i + " for testing purposes.");
            item.addProperty("obtain", "Craft using materials or purchase from NPC");

            items.add(item);
        }

        JsonObject root = new JsonObject();
        root.add("items", items);

        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(root, writer);
        }

        ItemList.LOGGER.info("Created default items.json with {} items", items.size());
    }

    private static JsonObject buildEnchantedDiamondJson() {
        JsonObject enchantedDiamond = new JsonObject();
        enchantedDiamond.addProperty("id", "enchanted_diamond");
        enchantedDiamond.addProperty("name", "\u00A7bEnchanted Diamond");
        enchantedDiamond.addProperty("material", "minecraft:diamond");
        JsonArray lore = new JsonArray();
        lore.add("\u00A77Condenses the power of nine diamonds.");
        lore.add("\u00A79\u00A7lRARE COMPONENT");
        enchantedDiamond.add("lore", lore);
        enchantedDiamond.addProperty("description", "Packed diamond dust that glows with latent energy.");
        enchantedDiamond.addProperty("obtain", "Combine 9 Diamonds in a crafting grid.");
        JsonArray recipe = new JsonArray();
        recipe.add(createRecipeRow("minecraft:diamond", "minecraft:diamond", "minecraft:diamond"));
        recipe.add(createRecipeRow("minecraft:diamond", "minecraft:diamond", "minecraft:diamond"));
        recipe.add(createRecipeRow("minecraft:diamond", "minecraft:diamond", "minecraft:diamond"));
        enchantedDiamond.add("recipe", recipe);
        return enchantedDiamond;
    }

    private static JsonObject buildEnchantedStickJson() {
        JsonObject enchantedStick = new JsonObject();
        enchantedStick.addProperty("id", "enchanted_stick");
        enchantedStick.addProperty("name", "\u00A76Enchanted Stick");
        enchantedStick.addProperty("material", "minecraft:stick");
        JsonArray lore = new JsonArray();
        lore.add("\u00A77Infused with blaze powder and ancient sap.");
        lore.add("\u00A7e\u00A7lEPIC COMPONENT");
        enchantedStick.add("lore", lore);
        enchantedStick.addProperty("description", "Perfect handle material for high-tier weapons.");
        enchantedStick.addProperty("obtain", "Empower sticks with blaze powder and more sticks.");
        JsonArray recipe = new JsonArray();
        recipe.add(createRecipeRow("", "minecraft:stick", ""));
        recipe.add(createRecipeRow("minecraft:stick", "minecraft:blaze_powder", "minecraft:stick"));
        recipe.add(createRecipeRow("", "minecraft:stick", ""));
        enchantedStick.add("recipe", recipe);
        return enchantedStick;
    }

    private static JsonObject buildEnchantedDiamondSwordJson() {
        JsonObject enchantedSword = new JsonObject();
        enchantedSword.addProperty("id", "enchanted_diamond_sword");
        enchantedSword.addProperty("name", "\u00A7bEnchanted Diamond Sword");
        enchantedSword.addProperty("material", "minecraft:diamond_sword");
        JsonArray lore = new JsonArray();
        lore.add("\u00A77Damage: \u00A7c+125");
        lore.add("\u00A79\u00A7lMYTHIC WEAPON");
        enchantedSword.add("lore", lore);
        enchantedSword.addProperty("description", "Channel two enchanted diamonds onto a steady handle.");
        enchantedSword.addProperty("obtain", "Craft using 2 Enchanted Diamonds and 1 Stick.");
        JsonArray recipe = new JsonArray();
        recipe.add(createRecipeRow("", "enchanted_diamond", ""));
        recipe.add(createRecipeRow("", "enchanted_diamond", ""));
        recipe.add(createRecipeRow("", "minecraft:stick", ""));
        enchantedSword.add("recipe", recipe);
        return enchantedSword;
    }

    private static JsonArray createRecipeRow(String slot1, String slot2, String slot3) {
        JsonArray row = new JsonArray();
        row.add(slot1);
        row.add(slot2);
        row.add(slot3);
        return row;
    }

    private static boolean ensureEssentialItems(JsonObject root, JsonArray itemsArray, File file) throws IOException {
        boolean mutated = false;

        if (!containsItem(itemsArray, "enchanted_diamond")) {
            itemsArray.add(buildEnchantedDiamondJson());
            mutated = true;
        }
        if (!containsItem(itemsArray, "enchanted_stick")) {
            itemsArray.add(buildEnchantedStickJson());
            mutated = true;
        }
        if (!containsItem(itemsArray, "enchanted_diamond_sword")) {
            itemsArray.add(buildEnchantedDiamondSwordJson());
            mutated = true;
        }

        if (mutated) {
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(root, writer);
            }
        }

        return mutated;
    }

    private static boolean containsItem(JsonArray itemsArray, String id) {
        for (JsonElement element : itemsArray) {
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                if (obj.has("id") && id.equals(obj.get("id").getAsString())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void loadItemsFromFile(File file) throws IOException {
        ItemList.LOGGER.info("Reading file: {}", file.getAbsolutePath());
        ItemList.LOGGER.info("File size: {} bytes", file.length());

        try (FileReader reader = new FileReader(file)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);

            if (root == null) {
                ItemList.LOGGER.error("PARSE ERROR: Failed to parse items.json - file is empty or invalid JSON");
                return;
            }

            ItemList.LOGGER.info("JSON parsed successfully");

            if (!root.has("items")) {
                ItemList.LOGGER.error("ERROR: items.json missing 'items' array. Root keys: {}", root.keySet());
                return;
            }

            JsonArray itemsArray = root.getAsJsonArray("items");
            boolean appendedDefaults = ensureEssentialItems(root, itemsArray, file);
            if (appendedDefaults) {
                ItemList.LOGGER.info("Appended missing default ItemList entries to {}", file.getName());
            }
            ItemList.LOGGER.info("Found items array with {} entries", itemsArray.size());

            ITEMS.clear();

            int successCount = 0;
            int failCount = 0;

            for (int idx = 0; idx < itemsArray.size(); idx++) {
                try {
                    JsonElement element = itemsArray.get(idx);
                    JsonObject obj = element.getAsJsonObject();

                    String id = obj.get("id").getAsString();
                    String name = obj.get("name").getAsString();
                    String material = obj.get("material").getAsString();

                    List<String> lore = new ArrayList<>();
                    if (obj.has("lore")) {
                        JsonArray loreArray = obj.getAsJsonArray("lore");
                        for (JsonElement loreElement : loreArray) {
                            lore.add(loreElement.getAsString());
                        }
                    }

                    String description = obj.has("description") ? obj.get("description").getAsString() : "";
                    String obtain = obj.has("obtain") ? obj.get("obtain").getAsString() : "No information available";

                    CustomItem item = new CustomItem(id, name, material, lore, description, obtain);

                    // Load recipe if present
                    if (obj.has("recipe")) {
                        JsonArray recipeArray = obj.getAsJsonArray("recipe");
                        if (recipeArray.size() == 3) {
                            CustomItem.RecipeIngredient[][] pattern = new CustomItem.RecipeIngredient[3][3];

                            for (int row = 0; row < 3; row++) {
                                JsonArray rowArray = recipeArray.get(row).getAsJsonArray();
                                for (int col = 0; col < 3; col++) {
                                    if (col < rowArray.size()) {
                                        JsonElement cell = rowArray.get(col);
                                        if (cell.isJsonNull() || cell.getAsString().isEmpty()) {
                                            pattern[row][col] = null;
                                        } else {
                                            String cellValue = cell.getAsString();
                                            // Parse format: "material:count" or just "material"
                                            // Handle minecraft:item_name vs minecraft:item_name:count
                                            String ingredientMaterial;
                                            int count = 1;

                                            // Check if there's a count at the end (last colon)
                                            int lastColon = cellValue.lastIndexOf(':');
                                            if (lastColon != -1 && lastColon < cellValue.length() - 1) {
                                                String possibleCount = cellValue.substring(lastColon + 1);
                                                try {
                                                    count = Integer.parseInt(possibleCount);
                                                    ingredientMaterial = cellValue.substring(0, lastColon);
                                                } catch (NumberFormatException e) {
                                                    // Not a count, it's part of the material name
                                                    ingredientMaterial = cellValue;
                                                    count = 1;
                                                }
                                            } else {
                                                ingredientMaterial = cellValue;
                                            }

                                            pattern[row][col] = new CustomItem.RecipeIngredient(ingredientMaterial, count);
                                        }
                                    } else {
                                        pattern[row][col] = null;
                                    }
                                }
                            }

                            item.addRecipe(new CustomItem.CraftingRecipe(pattern));
                        }
                    }

                    ITEMS.add(item);
                    successCount++;

                } catch (Exception e) {
                    failCount++;
                    ItemList.LOGGER.error("Failed to load item at index {}", idx, e);
                }
            }

            ItemList.LOGGER.info("Loaded {} items successfully, {} failed", successCount, failCount);
        } catch (Exception e) {
            ItemList.LOGGER.error("CRITICAL ERROR reading items.json", e);
            throw e;
        }
    }

    public static List<CustomItem> getAllItems() {
        return new ArrayList<>(ITEMS);
    }

    public static List<CustomItem> searchItems(String query) {
        if (query == null || query.isEmpty()) {
            return getAllItems();
        }

        List<CustomItem> results = new ArrayList<>();
        for (CustomItem item : ITEMS) {
            if (item.matchesSearch(query)) {
                results.add(item);
            }
        }
        return results;
    }

    public static CustomItem getItemById(String id) {
        for (CustomItem item : ITEMS) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }
}
