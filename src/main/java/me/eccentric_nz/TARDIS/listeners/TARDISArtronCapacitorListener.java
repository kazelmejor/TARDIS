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
package me.eccentric_nz.TARDIS.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import me.eccentric_nz.TARDIS.TARDIS;
import me.eccentric_nz.TARDIS.artron.TARDISArtronIndicator;
import me.eccentric_nz.TARDIS.database.QueryFactory;
import me.eccentric_nz.TARDIS.database.ResultSetControls;
import me.eccentric_nz.TARDIS.database.ResultSetPlayerPrefs;
import me.eccentric_nz.TARDIS.database.ResultSetTardis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * The Ninth Doctor used the Cardiff rift to "re-charge" his TARDIS. The process
 * took 2 days.
 *
 * @author eccentric_nz
 */
public class TARDISArtronCapacitorListener implements Listener {

    private final TARDIS plugin;
    List<Material> validBlocks = new ArrayList<Material>();

    public TARDISArtronCapacitorListener(TARDIS plugin) {
        this.plugin = plugin;
        validBlocks.add(Material.WOOD_BUTTON);
        validBlocks.add(Material.REDSTONE_COMPARATOR_OFF);
        validBlocks.add(Material.REDSTONE_COMPARATOR_ON);
        validBlocks.add(Material.STONE_BUTTON);
        validBlocks.add(Material.LEVER);
    }

    /**
     * Listens for player interaction with the button on the Artron Energy
     * Capacitor. If the button is right-clicked, then the Artron levels are
     * updated. Clicking with a Nether Star puts the capacitor to maximum,
     * clicking with the TARDIS key initialises the capacitor by spawning a
     * charged creeper inside it and sets the level to 50%. Clicking while
     * sneaking transfers player Artron Energy into the capacitor.
     *
     * If the button is just right-clicked, it displays the current capacitor
     * level as percentage of full.
     *
     * @param event the player clicking a block
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onCapacitorInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block != null) {
            Material blockType = block.getType();
            Action action = event.getAction();
            if (action == Action.RIGHT_CLICK_BLOCK) {
                // only proceed if they are clicking a button!
                if (validBlocks.contains(blockType)) {
                    // we need to get this block's location and then get the tardis_id from it
                    String buttonloc = block.getLocation().toString();
                    HashMap<String, Object> where = new HashMap<String, Object>();
                    where.put("type", 6);
                    where.put("location", buttonloc);
                    ResultSetControls rsc = new ResultSetControls(plugin, where, false);
                    if (rsc.resultSet()) {
                        // get tardis data
                        int id = rsc.getTardis_id();
                        HashMap<String, Object> wheret = new HashMap<String, Object>();
                        wheret.put("tardis_id", id);
                        ResultSetTardis rs = new ResultSetTardis(plugin, wheret, "", false);
                        if (rs.resultSet()) {
                            HashMap<String, Object> whereid = new HashMap<String, Object>();
                            whereid.put("tardis_id", id);
                            int current_level = rs.getArtron_level();
                            boolean init = rs.isTardis_init();
                            int fc = plugin.getArtronConfig().getInt("full_charge");
                            Material item = player.getItemInHand().getType();
                            Material full = Material.valueOf(plugin.getArtronConfig().getString("full_charge_item"));
                            Material cell = Material.valueOf(plugin.getRecipesConfig().getString("shaped.Artron Storage Cell.result"));
                            QueryFactory qf = new QueryFactory(plugin);
                            // determine key item
                            HashMap<String, Object> wherek = new HashMap<String, Object>();
                            wherek.put("player", player.getName());
                            ResultSetPlayerPrefs rsp = new ResultSetPlayerPrefs(plugin, wherek);
                            String key;
                            boolean hasPrefs = false;
                            if (rsp.resultSet()) {
                                hasPrefs = true;
                                key = (!rsp.getKey().isEmpty()) ? rsp.getKey() : plugin.getConfig().getString("preferences.key");
                            } else {
                                key = plugin.getConfig().getString("preferences.key");
                            }
                            if (item.equals(full) || item.equals(cell)) {
                                if (!init) {
                                    player.sendMessage(plugin.pluginName + "You haven't initialised the Artron Energy capacitor yet!");
                                    return;
                                }
                                int amount = fc;
                                if (item.equals(full)) {
                                    // remove the NETHER_STAR!
                                    int a = player.getInventory().getItemInHand().getAmount();
                                    int a2 = Integer.valueOf(a) - 1;
                                    if (a2 > 0) {
                                        player.getInventory().getItemInHand().setAmount(a2);
                                    } else {
                                        player.getInventory().removeItem(new ItemStack(full, 1));
                                    }
                                    player.sendMessage(plugin.pluginName + "Artron Energy Levels at maximum!");
                                } else {
                                    ItemStack is = player.getItemInHand();
                                    if (is.hasItemMeta()) {
                                        ItemMeta im = is.getItemMeta();
                                        String name = im.getDisplayName();
                                        if (!name.equals("Artron Storage Cell")) {
                                            player.sendMessage(plugin.pluginName + "That's not an Artron Storage Cell!");
                                            return;
                                        }
                                        List<String> lore = im.getLore();
                                        int charge = plugin.utils.parseInt(lore.get(1));
                                        if (charge <= 0) {
                                            player.sendMessage(plugin.pluginName + "The Artron Storage Cell is not charged!");
                                            return;
                                        }
                                        amount = current_level + charge;
                                        lore.set(1, "0");
                                        im.setLore(lore);
                                        is.setItemMeta(im);
                                        for (Enchantment e : is.getEnchantments().keySet()) {
                                            is.removeEnchantment(e);
                                        }
                                        player.sendMessage(plugin.pluginName + "Energy transfered from Artron Storage Cell!");
                                    }
                                }
                                // update charge
                                HashMap<String, Object> set = new HashMap<String, Object>();
                                set.put("artron_level", amount);
                                qf.doUpdate("tardis", set, whereid);
                            } else if (item.equals(Material.getMaterial(key))) {
                                // kickstart the TARDIS Artron Energy Capacitor
                                // has the TARDIS been initialised?
                                if (!init) {
                                    // get locations from database
                                    String creeper = rs.getCreeper();
                                    String beacon = rs.getBeacon();
                                    if (!creeper.isEmpty() && !creeper.equals(":")) {
                                        String[] creeperData = creeper.split(":");
                                        String[] beaconData = beacon.split(":");
                                        World w = block.getLocation().getWorld();
                                        int bx = 0, by = 0, bz = 0;
                                        float cx = plugin.utils.parseFloat(creeperData[1]);
                                        float cy = plugin.utils.parseFloat(creeperData[2]) + 1;
                                        float cz = plugin.utils.parseFloat(creeperData[3]);
                                        if (beaconData.length > 2) {
                                            bx = plugin.utils.parseInt(beaconData[1]);
                                            by = plugin.utils.parseInt(beaconData[2]);
                                            bz = plugin.utils.parseInt(beaconData[3]);
                                        }
                                        Location cl = new Location(w, cx, cy, cz);
                                        plugin.myspawn = true;
                                        Entity e = w.spawnEntity(cl, EntityType.CREEPER);
                                        Creeper c = (Creeper) e;
                                        c.setPowered(true);
                                        if (beaconData.length > 2) {
                                            Location bl = new Location(w, bx, by, bz);
                                            bl.getBlock().setTypeId(20);
                                        }
                                    }
                                    // set the capacitor to 50% charge
                                    HashMap<String, Object> set = new HashMap<String, Object>();
                                    int half = Math.round(fc / 2.0F);
                                    set.put("artron_level", half);
                                    set.put("tardis_init", 1);
                                    qf.doUpdate("tardis", set, whereid);
                                    player.sendMessage(plugin.pluginName + "Artron Energy Capacitor activated! Levels at 50%");
                                } else {
                                    player.sendMessage(plugin.pluginName + "You can only kick-start the Artron Energy Capacitor once!");
                                }
                            } else if (player.isSneaking()) {
                                if (!init) {
                                    player.sendMessage(plugin.pluginName + "You haven't initialised the Artron Energy capacitor yet!");
                                    return;
                                }
                                // transfer player artron energy into the capacitor
                                int ten_percent = Math.round(fc * 0.1F);
                                if (current_level >= ten_percent && plugin.getConfig().getBoolean("creation.create_worlds")) {
                                    player.sendMessage(plugin.pluginName + "You can only transfer Timelord Artron Energy when the capacitor is below 10%");
                                    return;
                                }
                                HashMap<String, Object> wherep = new HashMap<String, Object>();
                                wherep.put("player", player.getName());
                                if (hasPrefs) {
                                    int level = rsp.getArtronLevel();
                                    if (level < 1) {
                                        player.sendMessage(plugin.pluginName + "You don't have any Artron Energy to give the TARDIS");
                                        return;
                                    }
                                    int new_level = current_level + level;
                                    // set player level to 0
                                    HashMap<String, Object> set = new HashMap<String, Object>();
                                    set.put("artron_level", 0);
                                    HashMap<String, Object> wherel = new HashMap<String, Object>();
                                    wherel.put("player", player.getName());
                                    qf.doUpdate("player_prefs", set, wherel);
                                    // add player level to TARDIS level
                                    HashMap<String, Object> sett = new HashMap<String, Object>();
                                    sett.put("artron_level", new_level);
                                    qf.doUpdate("tardis", sett, whereid);
                                    int percent = Math.round((new_level * 100F) / fc);
                                    player.sendMessage(plugin.pluginName + "You charged the Artron Energy Capacitor to " + percent + "%");
                                } else {
                                    player.sendMessage(plugin.pluginName + "You don't have any Artron Energy to give the TARDIS");
                                }
                            } else {
                                // just tell us how much energy we have
                                new TARDISArtronIndicator(plugin).showArtronLevel(player, id, true, 0);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Listens for entity spawn events. If WorldGuard is enabled it blocks
     * mob-spawning inside the TARDIS, so this checks to see if we are doing the
     * spawning and un-cancels WorldGuard's setCancelled(true).
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        boolean isTardisWorldBreeding = (event.getLocation().getWorld().getName().contains("TARDIS") && (event.getSpawnReason().equals(SpawnReason.BREEDING)));
        if (isTardisWorldBreeding || plugin.myspawn) {
            event.setCancelled(false);
            if (plugin.myspawn == true) {
                plugin.myspawn = false;
            }
        }
    }
}
