/*
 * Copyright (C) 2012 eccentric_nz
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
package me.eccentric_nz.TARDIS.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import me.eccentric_nz.TARDIS.TARDIS;
import me.eccentric_nz.TARDIS.database.ResultSetPlayerPrefs;
import me.eccentric_nz.TARDIS.database.ResultSetTravellers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.SpoutManager;

/**
 * The distinctive TARDIS sound effect - a cyclic wheezing, groaning noise - was
 * originally created in the BBC Radiophonic Workshop by Brian Hodgson. He
 * produced the effect by dragging a set of house keys along the strings of an
 * old, gutted piano. The resulting sound was recorded and electronically
 * processed with echo and reverb.
 *
 * @author eccentric_nz
 */
public class TARDISSounds {

    private static Random rand = new Random();

    /**
     * Plays a random TARDIS sound to players who are inside the TARDIS and
     * don't have SFX set to false.
     */
    public static void randomTARDISSound() {
        if (TARDIS.plugin.getConfig().getBoolean("sfx") == true) {
            ResultSetTravellers rs = new ResultSetTravellers(TARDIS.plugin, null, true);
            if (rs.resultSet()) {
                ArrayList<HashMap<String, String>> data = rs.getData();
                for (HashMap<String, String> map : data) {
                    String playerNameStr = map.get("player");
                    HashMap<String, Object> where = new HashMap<String, Object>();
                    where.put("player", playerNameStr);
                    ResultSetPlayerPrefs rsp = new ResultSetPlayerPrefs(TARDIS.plugin, where);
                    boolean userSFX;
                    if (rsp.resultSet()) {
                        userSFX = rsp.isSFX_on();
                    } else {
                        userSFX = true;
                    }
                    final Player player = Bukkit.getServer().getPlayer(playerNameStr);
                    if (player != null) {
                        if (SpoutManager.getPlayer(player).isSpoutCraftEnabled() && userSFX) {
                            int i = rand.nextInt(28);
                            final String sfx = "https://dl.dropbox.com/u/53758864/soundeffects/drwho" + i + ".mp3";
                            final Location location = player.getLocation();
                            SpoutManager.getSoundManager().playCustomSoundEffect(TARDIS.plugin, SpoutManager.getPlayer(player), sfx, false, location, 9, 75);
                        }
                    }
                }
            }
        }
    }
}