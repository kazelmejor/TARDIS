/*
 * Copyright (C) 2013 eccentric_nz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package me.eccentric_nz.TARDIS.travel;

import java.util.Arrays;
import me.eccentric_nz.TARDIS.TARDIS;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A
 * The Stattenheim remote control is a remote control used by Time Lords to
 * control their TARDISes. The Rani and the Second Doctor each have used a
 * Stattenheim remote control for their TARDISes, which allowed them 'call' the
 * TARDIS to their location.
 *
 * @author eccentric_nz
 */
public class TARDISStattenheimRemote {

    private final TARDIS plugin;
    Material mat;

    public TARDISStattenheimRemote(TARDIS plugin) {
        this.plugin = plugin;
        mat = Material.valueOf(plugin.getConfig().getString("stattenheim"));
    }

    public ShapedRecipe stattenheim() {
        ItemStack is = new ItemStack(Material.FLINT, 1);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName("Stattenheim Remote");
        im.setLore(Arrays.asList(new String[]{"Right-click block", "to call TARDIS"}));
        is.setItemMeta(im);
        ShapedRecipe recipe = new ShapedRecipe(is);
        recipe.shape("OBO", "OLO", "RRR");
        recipe.setIngredient('O', Material.OBSIDIAN);
        recipe.setIngredient('B', Material.STONE_BUTTON);
        recipe.setIngredient('L', Material.INK_SACK, 4);
        recipe.setIngredient('R', Material.REDSTONE);
        plugin.getServer().addRecipe(recipe);
        return recipe;
    }
}