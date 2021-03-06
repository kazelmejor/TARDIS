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
package me.eccentric_nz.TARDIS.commands.preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import me.eccentric_nz.TARDIS.TARDIS;
import static me.eccentric_nz.TARDIS.commands.preferences.TARDISPrefsCommands.ucfirst;
import me.eccentric_nz.TARDIS.database.QueryFactory;
import org.bukkit.entity.Player;

/**
 *
 * @author eccentric_nz
 */
public class TARDISFloorCommand {

    private final TARDIS plugin;

    public TARDISFloorCommand(TARDIS plugin) {
        this.plugin = plugin;
    }

    public boolean setFloorOrWallBlock(Player player, String[] args, QueryFactory qf) {
        String pref = args[0];
        if (args.length < 2) {
            player.sendMessage(plugin.pluginName + "You need to specify a " + pref + " material!");
            return false;
        }
        String wall_mat;
        if (args.length > 2) {
            int count = args.length;
            StringBuilder buf = new StringBuilder();
            for (int i = 1; i < count; i++) {
                buf.append(args[i]).append("_");
            }
            String tmp = buf.toString();
            String t = tmp.substring(0, tmp.length() - 1);
            wall_mat = t.toUpperCase(Locale.ENGLISH);
        } else {
            wall_mat = args[1].toUpperCase(Locale.ENGLISH);
        }
        if (!plugin.tw.blocks.containsKey(wall_mat)) {
            String message = (wall_mat.equals("HELP")) ? "Here is a list of valid " + pref + " materials:" : "That is not a valid " + pref + " material! Try:";
            player.sendMessage(plugin.pluginName + message);
            List<String> sortedKeys = new ArrayList(plugin.tw.blocks.keySet());
            Collections.sort(sortedKeys);
            for (String w : sortedKeys) {
                player.sendMessage(w);
            }
            return true;
        }
        HashMap<String, Object> setw = new HashMap<String, Object>();
        setw.put(pref, wall_mat);
        HashMap<String, Object> where = new HashMap<String, Object>();
        where.put("player", player.getName());
        qf.doUpdate("player_prefs", setw, where);
        player.sendMessage(plugin.pluginName + ucfirst(pref) + " material saved.");
        return true;
    }
}
