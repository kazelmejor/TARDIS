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

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import me.eccentric_nz.TARDIS.TARDIS;
import me.eccentric_nz.TARDIS.TARDISConstants;
import me.eccentric_nz.TARDIS.database.QueryFactory;
import me.eccentric_nz.TARDIS.database.ResultSetDoors;
import me.eccentric_nz.TARDIS.database.ResultSetPlayerPrefs;
import me.eccentric_nz.TARDIS.database.ResultSetTardis;
import me.eccentric_nz.TARDIS.thirdparty.Version;
import me.eccentric_nz.TARDIS.travel.TARDISDoorLocation;
import me.eccentric_nz.TARDIS.travel.TARDISFarmer;
import me.eccentric_nz.TARDIS.travel.TARDISMob;
import me.eccentric_nz.TARDIS.utility.TARDISItemRenamer;
import me.eccentric_nz.TARDIS.utility.TARDISTexturePackChanger;
import multiworld.MultiWorldPlugin;
import multiworld.api.MultiWorldAPI;
import multiworld.api.MultiWorldWorldData;
import multiworld.api.flag.FlagName;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Door;
import org.getspout.spoutapi.SpoutManager;

/**
 * During TARDIS operation, a distinctive grinding and whirring sound is usually
 * heard. River Song once demonstrated a TARDIS was capable of materialising
 * silently, teasing the Doctor that the noise was actually caused by him
 * leaving the brakes on.
 *
 * @author eccentric_nz
 */
public class TARDISDoorListener implements Listener {

    private TARDIS plugin;
    public float[][] adjustYaw = new float[4][4];
    Version bukkitversion;
    Version preIMversion = new Version("1.4.5");
    Version SUBversion;
    Version preSUBversion = new Version("1.0");
    Random r = new Random();

    public TARDISDoorListener(TARDIS plugin) {
        this.plugin = plugin;
        // yaw adjustments if inner and outer door directions are different
        adjustYaw[0][0] = 0;
        adjustYaw[0][1] = -90;
        adjustYaw[0][2] = 180;
        adjustYaw[0][3] = 90;
        adjustYaw[1][0] = 90;
        adjustYaw[1][1] = 0;
        adjustYaw[1][2] = -90;
        adjustYaw[1][3] = 180;
        adjustYaw[2][0] = 180;
        adjustYaw[2][1] = 90;
        adjustYaw[2][2] = 0;
        adjustYaw[2][3] = -90;
        adjustYaw[3][0] = -90;
        adjustYaw[3][1] = 180;
        adjustYaw[3][2] = 90;
        adjustYaw[3][3] = 0;

        String[] v = Bukkit.getServer().getBukkitVersion().split("-");
        bukkitversion = (!v[0].equalsIgnoreCase("unknown")) ? new Version(v[0]) : new Version("1.4.7");
        SUBversion = (!v[0].equalsIgnoreCase("unknown")) ? new Version(v[1].substring(1, v[1].length())) : new Version("4.7");
    }

    /**
     * Listens for player interaction with TARDIS doors. If the door is
     * right-clicked with the TARDIS key (configurable) it will teleport the
     * player either into or out of the TARDIS.
     *
     * @param event a player clicking a block
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDoorInteract(PlayerInteractEvent event) {
        QueryFactory qf = new QueryFactory(plugin);
        final Player player = event.getPlayer();
        final String playerNameStr = player.getName();
        int cx = 0;
        int cy = 0;
        int cz = 0;
        Block block = event.getClickedBlock();
        if (block != null) {
            Material blockType = block.getType();
            Action action = event.getAction();
            // only proceed if they are clicking an iron door with a TARDIS key!
            if (blockType == Material.IRON_DOOR_BLOCK) {
                if (player.hasPermission("tardis.enter")) {
                    World playerWorld = player.getLocation().getWorld();
                    Location block_loc = block.getLocation();
                    byte doorData = block.getData();
                    String bw = block_loc.getWorld().getName();
                    int bx = block_loc.getBlockX();
                    int by = block_loc.getBlockY();
                    int bz = block_loc.getBlockZ();
                    if (doorData >= 8) {
                        by = (by - 1);
                    }
                    String doorloc = bw + ":" + bx + ":" + by + ":" + bz;
                    ItemStack stack = player.getItemInHand();
                    Material material = stack.getType();
                    // get key material
                    HashMap<String, Object> wherepp = new HashMap<String, Object>();
                    wherepp.put("player", playerNameStr);
                    ResultSetPlayerPrefs rsp = new ResultSetPlayerPrefs(plugin, wherepp);
                    String key;
                    boolean hasPrefs = false;
                    if (rsp.resultSet()) {
                        hasPrefs = true;
                        key = (!rsp.getKey().isEmpty()) ? rsp.getKey() : plugin.getConfig().getString("key");
                    } else {
                        key = plugin.getConfig().getString("key");
                    }
                    Material m = Material.getMaterial(key);
                    HashMap<String, Object> where = new HashMap<String, Object>();
                    where.put("door_location", doorloc);
                    ResultSetDoors rsd = new ResultSetDoors(plugin, where, false);
                    if (rsd.resultSet()) {
                        if (material.equals(m)) {
                            TARDISConstants.COMPASS dd = rsd.getDoor_direction();
                            int doortype = rsd.getDoor_type();
                            if (action == Action.LEFT_CLICK_BLOCK) {
                                // must be the owner
                                int id = rsd.getTardis_id();
                                HashMap<String, Object> oid = new HashMap<String, Object>();
                                oid.put("owner", player.getName());
                                ResultSetTardis rs = new ResultSetTardis(plugin, oid, "", false);
                                if (rs.resultSet()) {
                                    if (rs.getTardis_id() != id) {
                                        player.sendMessage(plugin.pluginName + "You can only lock or unlock your own door!");
                                        return;
                                    }
                                    int locked = (rsd.isLocked()) ? 0 : 1;
                                    String message = (rsd.isLocked()) ? "unlocked" : "locked";
                                    HashMap<String, Object> setl = new HashMap<String, Object>();
                                    setl.put("locked", locked);
                                    HashMap<String, Object> wherel = new HashMap<String, Object>();
                                    wherel.put("tardis_id", rsd.getTardis_id());
                                    wherel.put("door_type", rsd.getDoor_type());
                                    qf.doUpdate("doors", setl, wherel);
                                    player.sendMessage(plugin.pluginName + "The door was " + message);
                                }
                            }
                            if (action == Action.RIGHT_CLICK_BLOCK && player.isSneaking()) {
                                if (!rsd.isLocked()) {
                                    // toogle the door open/closed
                                    Block door_bottom;
                                    Door door = (Door) block.getState().getData();
                                    door_bottom = (door.isTopHalf()) ? block.getRelative(BlockFace.DOWN) : block;
                                    byte door_data = door_bottom.getData();
                                    switch (dd) {
                                        case NORTH:
                                            if (door_data == 3) {
                                                door_bottom.setData((byte) 7, false);
                                            } else {
                                                door_bottom.setData((byte) 3, false);
                                            }
                                            break;
                                        case WEST:
                                            if (door_data == 2) {
                                                door_bottom.setData((byte) 6, false);
                                            } else {
                                                door_bottom.setData((byte) 2, false);
                                            }
                                            break;
                                        case SOUTH:
                                            if (door_data == 1) {
                                                door_bottom.setData((byte) 5, false);
                                            } else {
                                                door_bottom.setData((byte) 1, false);
                                            }
                                            break;
                                        default:
                                            if (door_data == 0) {
                                                door_bottom.setData((byte) 4, false);
                                            } else {
                                                door_bottom.setData((byte) 0, false);
                                            }
                                            break;
                                    }
                                    playDoorSound(player, playerWorld, block_loc);
                                } else {
                                    player.sendMessage(plugin.pluginName + "You need to unlock the door!");
                                }
                            } else if (action == Action.RIGHT_CLICK_BLOCK) {
                                if (rsd.isLocked()) {
                                    player.sendMessage(plugin.pluginName + "The door is locked!");
                                    return;
                                }
                                int id = rsd.getTardis_id();
                                HashMap<String, Object> tid = new HashMap<String, Object>();
                                tid.put("tardis_id", id);
                                ResultSetTardis rs = new ResultSetTardis(plugin, tid, "", false);
                                if (rs.resultSet()) {
                                    int artron = rs.getArtron_level();
                                    int required = plugin.getArtronConfig().getInt("backdoor");
                                    TARDISConstants.COMPASS d = rs.getDirection();
                                    String tl = rs.getOwner();
                                    String current = rs.getCurrent();
                                    float yaw = player.getLocation().getYaw();
                                    float pitch = player.getLocation().getPitch();
                                    String companions = rs.getCompanions();
                                    // get quotes player prefs
                                    boolean userQuotes;
                                    boolean userTP;
                                    if (hasPrefs) {
                                        userQuotes = rsp.isQuotes_on();
                                        userTP = rsp.isTexture_on();
                                    } else {
                                        userQuotes = true;
                                        userTP = false;
                                    }
                                    List<TARDISMob> pets = null;
                                    switch (doortype) {
                                        case 1:
                                        case 4:
                                            // player is in the TARDIS - always exit to current location
                                            Location exitLoc = plugin.utils.getLocationFromDB(current, yaw, pitch);
                                            if (rs.isHandbrake_on()) {
                                                // change the yaw if the door directions are different
                                                if (!dd.equals(d)) {
                                                    yaw += adjustYaw(dd, d);
                                                }
                                                exitLoc.setYaw(yaw);
                                                // get location from database
                                                final Location exitTardis = exitLoc;
                                                // make location safe ie. outside of the bluebox
                                                double ex = exitTardis.getX();
                                                double ez = exitTardis.getZ();
                                                switch (d) {
                                                    case NORTH:
                                                        exitTardis.setX(ex + 0.5);
                                                        exitTardis.setZ(ez + 2.5);
                                                        break;
                                                    case EAST:
                                                        exitTardis.setX(ex - 1.5);
                                                        exitTardis.setZ(ez + 0.5);
                                                        break;
                                                    case SOUTH:
                                                        exitTardis.setX(ex + 0.5);
                                                        exitTardis.setZ(ez - 1.5);
                                                        break;
                                                    case WEST:
                                                        exitTardis.setX(ex + 2.5);
                                                        exitTardis.setZ(ez + 0.5);
                                                        break;
                                                }
                                                // exit TARDIS!
                                                playDoorSound(player, playerWorld, block_loc);
                                                movePlayer(player, exitTardis, true, playerWorld, userQuotes);
                                                if (plugin.getConfig().getBoolean("allow_mob_farming") && player.hasPermission("tardis.farm")) {
                                                    TARDISFarmer tf = new TARDISFarmer(plugin);
                                                    pets = tf.exitPets(player);
                                                    if (pets != null && pets.size() > 0) {
                                                        movePets(pets, exitTardis, player);
                                                    }
                                                }
                                                if (plugin.getConfig().getBoolean("allow_tp_switch") && userTP) {
                                                    new TARDISTexturePackChanger(plugin).changeTP(player, rsp.getTexture_out());
                                                }
                                                // remove player from traveller table
                                                HashMap<String, Object> wherd = new HashMap<String, Object>();
                                                wherd.put("player", playerNameStr);
                                                qf.doDelete("travellers", wherd);
                                            } else {
                                                player.sendMessage(plugin.pluginName + "The TARDIS is still travelling... you would get lost in the time vortex!");
                                            }
                                            break;
                                        case 0:
                                            // is the TARDIS materialising?
                                            if (plugin.tardisMaterialising.contains(id) || plugin.tardisDematerialising.contains(id)) {
                                                player.sendMessage(plugin.pluginName + "The TARDIS is still travelling... you would get lost in the time vortex!");
                                                return;
                                            }
                                            boolean chkCompanion = false;
                                            if (!playerNameStr.equals(tl)) {
                                                if (companions != null && !companions.isEmpty()) {
                                                    // is the player in the comapnion list
                                                    String[] companionData = companions.split(":");
                                                    for (String c : companionData) {
                                                        if (c.equalsIgnoreCase(playerNameStr)) {
                                                            chkCompanion = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                            if (playerNameStr.equals(tl) || chkCompanion == true || player.hasPermission("tardis.skeletonkey")) {
                                                // get INNER TARDIS location
                                                TARDISDoorLocation idl = getDoor(1, id);
                                                Location tmp_loc = idl.getL();
                                                World cw = idl.getW();
                                                TARDISConstants.COMPASS innerD = idl.getD();
                                                // check for entities in the police box
                                                if (plugin.getConfig().getBoolean("allow_mob_farming") && player.hasPermission("tardis.farm")) {
                                                    TARDISFarmer tf = new TARDISFarmer(plugin);
                                                    pets = tf.farmAnimals(block_loc, d, id, player);
                                                }
                                                // enter TARDIS!
                                                playDoorSound(player, playerWorld, block_loc);
                                                cw.getChunkAt(tmp_loc).load();
                                                tmp_loc.setPitch(pitch);
                                                // get inner door direction so we can adjust yaw if necessary
                                                if (!innerD.equals(d)) {
                                                    yaw += adjustYaw(d, innerD);
                                                }
                                                tmp_loc.setYaw(yaw);
                                                final Location tardis_loc = tmp_loc;
                                                movePlayer(player, tardis_loc, false, playerWorld, userQuotes);
                                                if (pets != null && pets.size() > 0) {
                                                    movePets(pets, tardis_loc, player);
                                                }
                                                if (plugin.getConfig().getBoolean("allow_tp_switch") && userTP) {
                                                    if (!rsp.getTexture_in().isEmpty()) {
                                                        new TARDISTexturePackChanger(plugin).changeTP(player, rsp.getTexture_in());
                                                    }
                                                }
                                                // put player into travellers table
                                                HashMap<String, Object> set = new HashMap<String, Object>();
                                                set.put("tardis_id", id);
                                                set.put("player", playerNameStr);
                                                qf.doInsert("travellers", set);
                                                if (plugin.pm.getPlugin("Spout") != null && SpoutManager.getPlayer(player).isSpoutCraftEnabled()) {
                                                    SpoutManager.getSoundManager().playCustomSoundEffect(plugin, SpoutManager.getPlayer(player), "https://dl.dropboxusercontent.com/u/53758864/tardis_hum.mp3", false, tardis_loc, 9, 25);
                                                }
                                            }
                                            break;
                                        case 2:
                                            if (artron < required) {
                                                player.sendMessage(plugin.pluginName + "You don't have enough Artron Energy to use the back door!");
                                                return;
                                            }
                                            // always enter by the back door
                                            TARDISDoorLocation ibdl = getDoor(3, id);
                                            Location ibd_loc = ibdl.getL();
                                            if (ibd_loc == null) {
                                                player.sendMessage(plugin.pluginName + "You need to add a back door inside the TARDIS!");
                                                return;
                                            }
                                            TARDISConstants.COMPASS ibdd = ibdl.getD();
                                            TARDISConstants.COMPASS ipd = TARDISConstants.COMPASS.valueOf(plugin.utils.getPlayersDirection(player, false));
                                            if (!ibdd.equals(ipd)) {
                                                yaw += adjustYaw(ipd, ibdd);
                                            }
                                            ibd_loc.setYaw(yaw);
                                            ibd_loc.setPitch(pitch);
                                            final Location inner_loc = ibd_loc;
                                            playDoorSound(player, playerWorld, block_loc);
                                            movePlayer(player, inner_loc, false, playerWorld, userQuotes);
                                            if (!rsp.getTexture_in().isEmpty()) {
                                                new TARDISTexturePackChanger(plugin).changeTP(player, rsp.getTexture_in());
                                            }
                                            // put player into travellers table
                                            HashMap<String, Object> set = new HashMap<String, Object>();
                                            set.put("tardis_id", id);
                                            set.put("player", playerNameStr);
                                            qf.doInsert("travellers", set);
                                            if (plugin.pm.getPlugin("Spout") != null && SpoutManager.getPlayer(player).isSpoutCraftEnabled()) {
                                                SpoutManager.getSoundManager().playCustomSoundEffect(plugin, SpoutManager.getPlayer(player), "https://dl.dropboxusercontent.com/u/53758864/tardis_hum.mp3", false, inner_loc, 9, 25);
                                            }
                                            HashMap<String, Object> wheree = new HashMap<String, Object>();
                                            wheree.put("tardis_id", id);
                                            int cost = (0 - plugin.getArtronConfig().getInt("backdoor"));
                                            qf.alterEnergyLevel("tardis", cost, wheree, player);
                                            break;
                                        case 3:
                                            if (artron < required) {
                                                player.sendMessage(plugin.pluginName + "You don't have enough Artron Energy to use the back door!");
                                                return;
                                            }
                                            // always exit to outer back door
                                            TARDISDoorLocation obdl = getDoor(2, id);
                                            Location obd_loc = obdl.getL();
                                            if (obd_loc == null) {
                                                player.sendMessage(plugin.pluginName + "You need to add a back door outside the TARDIS!");
                                                return;
                                            }
                                            TARDISConstants.COMPASS obdd = obdl.getD();
                                            TARDISConstants.COMPASS opd = TARDISConstants.COMPASS.valueOf(plugin.utils.getPlayersDirection(player, false));
                                            if (!obdd.equals(opd)) {
                                                yaw += adjustYaw(opd, obdd);
                                            }
                                            obd_loc.setYaw(yaw);
                                            obd_loc.setPitch(pitch);
                                            final Location outer_loc = obd_loc;
                                            playDoorSound(player, playerWorld, block_loc);
                                            movePlayer(player, outer_loc, false, playerWorld, userQuotes);
                                            if (plugin.getConfig().getBoolean("allow_tp_switch") && userTP) {
                                                new TARDISTexturePackChanger(plugin).changeTP(player, rsp.getTexture_out());
                                            }
                                            // remove player from traveller table
                                            HashMap<String, Object> wherd = new HashMap<String, Object>();
                                            wherd.put("player", playerNameStr);
                                            qf.doDelete("travellers", wherd);
                                            // take energy
                                            HashMap<String, Object> wherea = new HashMap<String, Object>();
                                            wherea.put("tardis_id", id);
                                            int costa = (0 - plugin.getArtronConfig().getInt("backdoor"));
                                            qf.alterEnergyLevel("tardis", costa, wherea, player);
                                            break;
                                        default:
                                            // do nothing
                                            break;
                                    }
                                }
                            }
                        } else {
                            String grammar;
                            if (!material.equals(Material.AIR)) {
                                grammar = (TARDISConstants.vowels.contains(material.toString().substring(0, 1))) ? "an " + material : "a " + material;
                            } else {
                                grammar = "nothing";
                            }
                            player.sendMessage(plugin.pluginName + "The TARDIS key is a " + key + ". You have " + grammar + " in your hand!");
                        }
                    }
                }
            }
        }
    }

    /**
     * A method to teleport the player into and out of the TARDIS.
     *
     * @param p the player to teleport
     * @param l the location to teleport to
     * @param exit whether the player is entering or exiting the TARDIS, if true
     * they are exiting
     * @param from the world they are teleporting from
     * @param q whether the player will receive a TARDIS quote message
     */
    public void movePlayer(final Player p, Location l, final boolean exit, final World from, boolean q) {

        final int i = r.nextInt(plugin.quotelen);
        final Location theLocation = l;
        final World to = theLocation.getWorld();
        final boolean allowFlight = p.getAllowFlight();
        final boolean crossWorlds = from != to;
        final boolean quotes = q;
        final String name = p.getName();
        // try loading chunk
        World world = l.getWorld();
        final boolean isSurvival = checkSurvival(world);

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                p.teleport(theLocation);
            }
        }, 5L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            @SuppressWarnings("deprecation")
            public void run() {
                p.teleport(theLocation);
                if (p.getGameMode() == GameMode.CREATIVE || (allowFlight && crossWorlds && !isSurvival)) {
                    p.setAllowFlight(true);
                }
                if (quotes) {
                    p.sendMessage(plugin.pluginName + plugin.quote.get(i));
                }
                if (exit) {
                    // give some artron energy
                    QueryFactory qf = new QueryFactory(plugin);
                    // add energy to player
                    HashMap<String, Object> where = new HashMap<String, Object>();
                    where.put("player", name);
                    int player_artron = (plugin.getConfig().getBoolean("create_worlds")) ? plugin.getArtronConfig().getInt("player") : plugin.getArtronConfig().getInt("player") * 10;
                    qf.alterEnergyLevel("player_prefs", player_artron, where, p);
                }
                // give a key
                giveKey(p);
            }
        }, 10L);
    }

    /**
     * Checks if the world the player is teleporting to is a SURVIVAL world.
     *
     * @param w the world to check
     * @return true if the world is a SURVIVAL world, otherwise false
     */
    private boolean checkSurvival(World w) {
        boolean bool = false;
        if (plugin.pm.isPluginEnabled("Multiverse-Core")) {
            MultiverseCore mv = (MultiverseCore) plugin.pm.getPlugin("Multiverse-Core");
            MultiverseWorld mvw = mv.getCore().getMVWorldManager().getMVWorld(w);
            GameMode gm = mvw.getGameMode();
            if (gm.equals(GameMode.SURVIVAL)) {
                bool = true;
            }
        }
        if (plugin.pm.isPluginEnabled("MultiWorld")) {
            MultiWorldAPI mw = ((MultiWorldPlugin) plugin.pm.getPlugin("MultiWorld")).getApi();
            MultiWorldWorldData mww = mw.getWorld(w.getName());
            if (!mww.isOptionSet(FlagName.CREATIVEWORLD)) {
                bool = true;
            }
        }
        return bool;
    }

    /**
     * A method to transport player pets (tamed mobs) into and out of the
     * TARDIS.
     *
     * @param p a list of the player's pets found nearby
     * @param l the location to teleport pets to
     * @param player the player who owns the pets
     */
    private void movePets(List<TARDISMob> p, Location l, Player player) {
        Location pl = l.clone();
        World w = l.getWorld();
        // will need to adjust this depending on direction Police Box is facing
        pl.setX(l.getX() + 1);
        pl.setZ(l.getZ() + 1);
        for (TARDISMob pet : p) {
            plugin.myspawn = true;
            LivingEntity ent = (LivingEntity) w.spawnEntity(pl, pet.getType());
            ent.setTicksLived(pet.getAge());
            String pet_name = pet.getName();
            if (pet_name != null && !pet_name.isEmpty()) {
                ent.setCustomName(pet.getName());
                ent.setCustomNameVisible(true);
            }
            ent.setHealth(pet.getHealth());
            ((Tameable) ent).setTamed(true);
            ((Tameable) ent).setOwner(player);
            if (pet.getType().equals(EntityType.WOLF)) {
                Wolf wolf = (Wolf) ent;
                wolf.setCollarColor(pet.getColour());
                wolf.setSitting(pet.getSitting());
                if (pet.isBaby()) {
                    wolf.setBaby();
                }
            } else {
                Ocelot cat = (Ocelot) ent;
                cat.setCatType(pet.getCatType());
                cat.setSitting(pet.getSitting());
                if (pet.isBaby()) {
                    cat.setBaby();
                }
            }
        }
        p.clear();
    }

    /**
     * A method to give the TARDIS key to a player if the server is using a
     * multi-inventory plugin.
     *
     * @param p the player to give the key to
     */
    @SuppressWarnings("deprecation")
    private void giveKey(Player p) {
        String key;
        HashMap<String, Object> where = new HashMap<String, Object>();
        where.put("player", p.getName());
        ResultSetPlayerPrefs rsp = new ResultSetPlayerPrefs(plugin, where);
        if (rsp.resultSet()) {
            key = (!rsp.getKey().isEmpty()) ? rsp.getKey() : plugin.getConfig().getString("key");
        } else {
            key = plugin.getConfig().getString("key");
        }
        if (plugin.getConfig().getBoolean("give_key") && (bukkitversion.compareTo(preIMversion) > 0 || (bukkitversion.compareTo(preIMversion) == 0 && SUBversion.compareTo(preSUBversion) >= 0)) && !key.equals("AIR")) {
            Inventory inv = p.getInventory();
            Material m = Material.valueOf(key);
            if (!inv.contains(m)) {
                ItemStack is = new ItemStack(m, 1);
                TARDISItemRenamer ir = new TARDISItemRenamer(is);
                ir.setName("Sonic Screwdriver", true);
                inv.addItem(is);
                p.updateInventory();
                p.sendMessage(plugin.pluginName + "Don't forget your TARDIS key!");
            }
        }
    }

    /**
     * Adjusts the direction the player is facing after a teleport.
     *
     * @param d1 the direction the first door is facing
     * @param d2 the direction the second door is facing
     * @return the angle needed to correct the yaw
     */
    private float adjustYaw(TARDISConstants.COMPASS d1, TARDISConstants.COMPASS d2) {
        switch (d1) {
            case NORTH:
                return adjustYaw[0][d2.ordinal()];
            case WEST:
                return adjustYaw[1][d2.ordinal()];
            case SOUTH:
                return adjustYaw[2][d2.ordinal()];
            default:
                return adjustYaw[3][d2.ordinal()];
        }
    }

    /**
     * Get door location data for teleport entry and exit of the TARDIS.
     *
     * @param doortype a reference to the door_type field in the doors table
     * @param id the unique TARDIS identifier i the database
     * @return an instance of the TARDISDoorLocation data class
     */
    public TARDISDoorLocation getDoor(int doortype, int id) {
        TARDISDoorLocation tdl = new TARDISDoorLocation();
        // get door location
        HashMap<String, Object> wherei = new HashMap<String, Object>();
        wherei.put("door_type", doortype);
        wherei.put("tardis_id", id);
        ResultSetDoors rsd = new ResultSetDoors(plugin, wherei, false);
        if (rsd.resultSet()) {
            TARDISConstants.COMPASS d = rsd.getDoor_direction();
            tdl.setD(d);
            String doorLocStr = rsd.getDoor_location();
            String[] split = doorLocStr.split(":");
            World cw = plugin.getServer().getWorld(split[0]);
            tdl.setW(cw);
            int cx = plugin.utils.parseNum(split[1]);
            int cy = plugin.utils.parseNum(split[2]);
            int cz = plugin.utils.parseNum(split[3]);
            Location tmp_loc = cw.getBlockAt(cx, cy, cz).getLocation();
            int getx = tmp_loc.getBlockX();
            int getz = tmp_loc.getBlockZ();
            switch (d) {
                case NORTH:
                    // z -ve
                    tmp_loc.setX(getx + 0.5);
                    tmp_loc.setZ(getz - 0.5);
                    break;
                case EAST:
                    // x +ve
                    tmp_loc.setX(getx + 1.5);
                    tmp_loc.setZ(getz + 0.5);
                    break;
                case SOUTH:
                    // z +ve
                    tmp_loc.setX(getx + 0.5);
                    tmp_loc.setZ(getz + 1.5);
                    break;
                case WEST:
                    // x -ve
                    tmp_loc.setX(getx - 0.5);
                    tmp_loc.setZ(getz + 0.5);
                    break;
            }
            tdl.setL(tmp_loc);
        }
        return tdl;
    }

    /**
     * Plays a door sound when the iron door is clicked.
     *
     * @param p a player to play the sound for
     * @param w a world to play the sound in
     * @param l a location to play the sound at
     */
    private void playDoorSound(Player p, World w, Location l) {
        try {
            Class.forName("org.bukkit.Sound");
            p.playSound(p.getLocation(), Sound.DOOR_OPEN, 1, 1);
        } catch (ClassNotFoundException e) {
            w.playEffect(l, Effect.DOOR_TOGGLE, 0);
        }
    }
}
