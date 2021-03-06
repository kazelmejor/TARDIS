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
package me.eccentric_nz.TARDIS.commands.admin;

import java.io.File;
import java.io.IOException;
import me.eccentric_nz.TARDIS.TARDIS;
import me.eccentric_nz.TARDIS.enumeration.MESSAGE;
import me.eccentric_nz.TARDIS.travel.TARDISTerminalInventory;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author eccentric_nz
 */
public class TARDISSetIntegerCommand {

    private final TARDIS plugin;

    public TARDISSetIntegerCommand(TARDIS plugin) {
        this.plugin = plugin;
    }

    public boolean setConfigInt(CommandSender sender, String[] args, String section) {
        String first = (section.isEmpty()) ? args[0].toLowerCase() : section + "." + args[0].toLowerCase();
        String a = args[1];
        int val;
        try {
            val = Integer.parseInt(a);
        } catch (NumberFormatException nfe) {
            // not a number
            sender.sendMessage(plugin.pluginName + ChatColor.RED + " The last argument must be a number!");
            return false;
        }
        plugin.getConfig().set(first, val);
        if (first.equals("terminal_step")) {
            // reset the terminal inventory
            plugin.buttonListener.items = new TARDISTerminalInventory().getTerminal();
        }
        plugin.saveConfig();
        sender.sendMessage(plugin.pluginName + MESSAGE.CONFIG_UPDATED.getText());
        return true;
    }

    public boolean setConfigInt(CommandSender sender, String[] args) {
        String first = args[0];
        String a = args[1];
        int val;
        try {
            val = Integer.parseInt(a);
        } catch (NumberFormatException nfe) {
            // not a number
            sender.sendMessage(plugin.pluginName + ChatColor.RED + " The last argument must be a number!");
            return false;
        }
        plugin.getArtronConfig().set(first, val);
        try {
            plugin.getArtronConfig().save(new File(plugin.getDataFolder(), "artron.yml"));
        } catch (IOException io) {
            plugin.debug("Could not save artron.yml, " + io);
        }
        sender.sendMessage(plugin.pluginName + MESSAGE.CONFIG_UPDATED.getText());
        return true;
    }
}
