/*
 * Copyright (C) 2014 eccentric_nz
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
package me.eccentric_nz.TARDIS.commands;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import me.eccentric_nz.TARDIS.TARDIS;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author eccentric_nz
 */
public class TARDISRecipeLister {

    private final TARDIS plugin;
    private final CommandSender sender;
    private final LinkedHashMap<String, List<String>> options;

    public TARDISRecipeLister(TARDIS plugin, CommandSender sender) {
        this.plugin = plugin;
        this.sender = sender;
        this.options = createRecipeOptions();
    }

    public void list() {
        sender.sendMessage(plugin.pluginName + "You can view the following recipes:");
        sender.sendMessage(ChatColor.GRAY + "    Command argument" + ChatColor.RESET + " - " + ChatColor.DARK_GRAY + "Recipe Result");
        for (Map.Entry<String, List<String>> map : options.entrySet()) {
            sender.sendMessage(map.getKey());
            for (String s : map.getValue()) {
                sender.sendMessage("    " + s);
            }
        }
    }

    private LinkedHashMap<String, List<String>> createRecipeOptions() {
        LinkedHashMap<String, List<String>> recipe_options = new LinkedHashMap<String, List<String>>();
        List<String> items = new ArrayList<String>();
        items.add(ChatColor.GREEN + "key" + ChatColor.RESET + " - " + ChatColor.DARK_GREEN + "TARDIS Key");
        items.add(ChatColor.GREEN + "locator" + ChatColor.RESET + " - " + ChatColor.DARK_GREEN + "TARDIS Locator");
        items.add(ChatColor.GREEN + "cell" + ChatColor.RESET + " - " + ChatColor.DARK_GREEN + "Artron Energy Cell");
        items.add(ChatColor.GREEN + "filter" + ChatColor.RESET + " - " + ChatColor.DARK_GREEN + "Perception Filter");
        items.add(ChatColor.GREEN + "sonic" + ChatColor.RESET + " - " + ChatColor.DARK_GREEN + "Sonic Screwdriver");
        items.add(ChatColor.GREEN + "remote" + ChatColor.RESET + " - " + ChatColor.DARK_GREEN + "Stattenheim Remote");
        recipe_options.put("TARDIS Items", items);
        List<String> item_circuits = new ArrayList<String>();
        item_circuits.add(ChatColor.RED + "l-circuit" + ChatColor.RESET + " - " + ChatColor.DARK_RED + "Locator Circuit");
        item_circuits.add(ChatColor.RED + "m-circuit" + ChatColor.RESET + " - " + ChatColor.DARK_RED + "Materialisation Circuit");
        item_circuits.add(ChatColor.RED + "oscillator" + ChatColor.RESET + " - " + ChatColor.DARK_RED + "Sonic Oscillator");
        item_circuits.add(ChatColor.RED + "p-circuit" + ChatColor.RESET + " - " + ChatColor.DARK_RED + "Perception Circuit");
        item_circuits.add(ChatColor.RED + "s-circuit" + ChatColor.RESET + " - " + ChatColor.DARK_RED + "Stattenheim Circuit");
        recipe_options.put("Item Circuits", item_circuits);
        List<String> sonic_circuits = new ArrayList<String>();
        sonic_circuits.add(ChatColor.BLUE + "a-circuit" + ChatColor.RESET + " - " + ChatColor.DARK_BLUE + "Admin Circuit");
        sonic_circuits.add(ChatColor.BLUE + "bio-circuit" + ChatColor.RESET + " - " + ChatColor.DARK_BLUE + "Bio-scanner Circuit");
        sonic_circuits.add(ChatColor.BLUE + "d-circuit" + ChatColor.RESET + " - " + ChatColor.DARK_BLUE + "Diamond Circuit");
        sonic_circuits.add(ChatColor.BLUE + "e-circuit" + ChatColor.RESET + " - " + ChatColor.DARK_BLUE + "Emerald Circuit");
        sonic_circuits.add(ChatColor.BLUE + "r-circuit" + ChatColor.RESET + " - " + ChatColor.DARK_BLUE + "Redstone Circuit");
        recipe_options.put("Sonic Circuits", sonic_circuits);
        List<String> console_circuits = new ArrayList<String>();
        console_circuits.add(ChatColor.LIGHT_PURPLE + "ars-circuit" + ChatColor.RESET + " - " + ChatColor.DARK_PURPLE + "ARS Circuit");
        console_circuits.add(ChatColor.LIGHT_PURPLE + "c-circuit" + ChatColor.RESET + " - " + ChatColor.DARK_PURPLE + "Chameleon Circuit");
        console_circuits.add(ChatColor.LIGHT_PURPLE + "i-circuit" + ChatColor.RESET + " - " + ChatColor.DARK_PURPLE + "Input Circuit");
        console_circuits.add(ChatColor.LIGHT_PURPLE + "m-circuit" + ChatColor.RESET + " - " + ChatColor.DARK_PURPLE + "Materialisation Circuit");
        console_circuits.add(ChatColor.LIGHT_PURPLE + "memory-circuit" + ChatColor.RESET + " - " + ChatColor.DARK_PURPLE + "Memory Circuit");
        console_circuits.add(ChatColor.LIGHT_PURPLE + "scanner-circuit" + ChatColor.RESET + " - " + ChatColor.DARK_PURPLE + "Scanner Circuit");
        console_circuits.add(ChatColor.LIGHT_PURPLE + "t-circuit" + ChatColor.RESET + " - " + ChatColor.DARK_PURPLE + "Temporal Circuit");
        recipe_options.put("Advanced Console Circuits", console_circuits);
        List<String> disks = new ArrayList<String>();
        disks.add(ChatColor.AQUA + "blank" + ChatColor.RESET + " - " + ChatColor.DARK_AQUA + "Blank Storage Disk");
        disks.add(ChatColor.AQUA + "biome-disk" + ChatColor.RESET + " - " + ChatColor.DARK_AQUA + "Biome Storage Disk");
        disks.add(ChatColor.AQUA + "sender-disk" + ChatColor.RESET + " - " + ChatColor.DARK_AQUA + "Player Storage Disk");
        disks.add(ChatColor.AQUA + "preset-disk" + ChatColor.RESET + " - " + ChatColor.DARK_AQUA + "Preset Storage Disk");
        disks.add(ChatColor.AQUA + "save-disk" + ChatColor.RESET + " - " + ChatColor.DARK_AQUA + "Save Storage Disk");
        recipe_options.put("Storage Disks", disks);
        return recipe_options;
    }
}
