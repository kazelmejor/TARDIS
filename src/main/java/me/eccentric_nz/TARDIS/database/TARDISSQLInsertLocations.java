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
package me.eccentric_nz.TARDIS.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import me.eccentric_nz.TARDIS.TARDIS;

/**
 *
 * @author eccentric_nz
 */
public class TARDISSQLInsertLocations implements Runnable {

    private final TARDIS plugin;
    private final TARDISDatabaseConnection service = TARDISDatabaseConnection.getInstance();
    private final Connection connection = service.getConnection();
    private final HashMap<String, Object> data;

    /**
     * Inserts data into an SQLite database table. This method builds a prepared
     * SQL statement from the parameters supplied and then executes the insert.
     *
     * @param plugin an instance of the main plugin class
     * @param data a HashMap<String, Object> of table fields and values to
     * insert.
     */
    public TARDISSQLInsertLocations(TARDIS plugin, HashMap<String, Object> data) {
        this.plugin = plugin;
        this.data = data;
    }

    @Override
    public void run() {
        String[] tables = {"homes", "current", "next", "back"};
        PreparedStatement ps = null;
        String fields;
        String questions;
        StringBuilder sbf = new StringBuilder();
        StringBuilder sbq = new StringBuilder();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            sbf.append(entry.getKey()).append(",");
            sbq.append("?,");
        }
        fields = sbf.toString().substring(0, sbf.length() - 1);
        questions = sbq.toString().substring(0, sbq.length() - 1);
        try {
            service.testConnection(connection);
            for (String s : tables) {
                ps = connection.prepareStatement("INSERT INTO " + s + " (" + fields + ") VALUES (" + questions + ")");
                int i = 1;
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    if (entry.getValue().getClass().equals(String.class)) {
                        ps.setString(i, entry.getValue().toString());
                    } else {
                        ps.setInt(i, plugin.utils.parseInt(entry.getValue().toString()));
                    }
                    i++;
                }
                ps.executeUpdate();
            }
            data.clear();
        } catch (SQLException e) {
            plugin.debug("Insert error for starting locations! " + e.getMessage());
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException e) {
                plugin.debug("Error closing location tables! " + e.getMessage());
            }
        }
    }
}
