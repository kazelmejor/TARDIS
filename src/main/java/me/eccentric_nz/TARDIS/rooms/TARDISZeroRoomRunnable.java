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
package me.eccentric_nz.TARDIS.rooms;

import java.util.List;
import me.eccentric_nz.TARDIS.TARDIS;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 *
 * @author eccentric_nz
 */
public class TARDISZeroRoomRunnable implements Runnable {

    private final TARDIS plugin;
    private final World zero_world;

    public TARDISZeroRoomRunnable(TARDIS plugin) {
        this.plugin = plugin;
        this.zero_world = plugin.getServer().getWorld("TARDIS_Zero_Room");
    }

    @Override
    public void run() {
        if (zero_world == null) {
            return;
        }
        List<Player> inZeroRoom = zero_world.getPlayers();
        if (inZeroRoom.size() < 1) {
            return;
        }
        for (Player p : inZeroRoom) {
            if (p.isOnline() && p.getHealth() < p.getMaxHealth()) {
                p.setHealth(p.getHealth() + 0.5);
            }
        }
    }
}
