/*
 *  Copyright 2013 eccentric_nz.
 */
package me.eccentric_nz.TARDIS.recipes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import me.eccentric_nz.TARDIS.TARDIS;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author eccentric_nz
 */
public class TARDISShapedRecipe {

    private final TARDIS plugin;
    private final HashMap<String, ShapedRecipe> shapedRecipes;

    public TARDISShapedRecipe(TARDIS plugin) {
        this.plugin = plugin;
        this.shapedRecipes = new HashMap<String, ShapedRecipe>();
    }

    public void addShapedRecipes() {
        Set<String> shaped = plugin.getRecipesConfig().getConfigurationSection("shaped").getKeys(false);
        for (String s : shaped) {
            plugin.getServer().addRecipe(makeRecipe(s));
        }
    }

    private ShapedRecipe makeRecipe(String s) {
        /*
         shape: A-A,BBB,CDC
         ingredients:
         A: 1
         B: 2
         C: '5:2'
         D: 57
         result: 276
         amount: 1
         lore: "The vorpal blade\ngoes snicker-snack!"
         enchantment: FIRE_ASPECT
         strength: 3
         */
        String[] result_iddata = plugin.getRecipesConfig().getString("shaped." + s + ".result").split(":");
        Material mat = Material.valueOf(result_iddata[0]);
        int amount = plugin.getRecipesConfig().getInt("shaped." + s + ".amount");
        ItemStack is;
        if (result_iddata.length == 2) {
            short result_data = plugin.utils.parseShort(result_iddata[1]);
            is = new ItemStack(mat, amount, result_data);
        } else {
            is = new ItemStack(mat, amount);
        }
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(s);
        if (!plugin.getRecipesConfig().getString("shaped." + s + ".lore").equals("")) {
            im.setLore(Arrays.asList(plugin.getRecipesConfig().getString("shaped." + s + ".lore").split("\n")));
        }
        is.setItemMeta(im);
        ShapedRecipe r = new ShapedRecipe(is);
        // get shape
        String difficulty = plugin.getConfig().getString("preferences.difficulty");
        try {
            String[] shape_tmp = plugin.getRecipesConfig().getString("shaped." + s + "." + difficulty + "_shape").split(",");
            String[] shape = new String[3];
            for (int i = 0; i < 3; i++) {
                shape[i] = shape_tmp[i].replaceAll("-", " ");
            }
            r.shape(shape[0], shape[1], shape[2]);
            Set<String> ingredients = plugin.getRecipesConfig().getConfigurationSection("shaped." + s + "." + difficulty + "_ingredients").getKeys(false);
            for (String g : ingredients) {
                char c = g.charAt(0);
                String[] recipe_iddata = plugin.getRecipesConfig().getString("shaped." + s + "." + difficulty + "_ingredients." + g).split(":");
                Material m = Material.valueOf(recipe_iddata[0]);
                if (recipe_iddata.length == 2) {
                    int recipe_data = plugin.utils.parseInt(recipe_iddata[1]);
                    r.setIngredient(c, m, recipe_data);
                } else {
                    r.setIngredient(c, m);
                }
            }
        } catch (IllegalArgumentException e) {
            plugin.console.sendMessage(plugin.pluginName + ChatColor.RED + s + " recipe failed! " + ChatColor.RESET + "Check the recipe config file!");
        }
        shapedRecipes.put(s, r);
        return r;
    }

    public HashMap<String, ShapedRecipe> getShapedRecipes() {
        return shapedRecipes;
    }
}
