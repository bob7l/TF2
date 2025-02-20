package me.chaseoes.tf2.listeners;

import me.chaseoes.tf2.ClassUtilities;
import me.chaseoes.tf2.DataConfiguration;
import me.chaseoes.tf2.GameUtilities;
import me.chaseoes.tf2.MapConfiguration;
import me.chaseoes.tf2.MapUtilities;
import me.chaseoes.tf2.Queue;
import me.chaseoes.tf2.utilities.DataChecker;
import me.chaseoes.tf2.utilities.GeneralUtilities;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        try {
            Player player = event.getPlayer();

            if (GameUtilities.getUtilities().isIngame(player)) {
                if (player.getItemInHand().getType() == Material.getMaterial(373)) {
                    if (GameUtilities.getUtilities().justspawned.contains(player.getName())) {
                        event.setCancelled(true);
                        player.updateInventory();
                    }
                    if (!GameUtilities.getUtilities().getGameStatus(GameUtilities.getUtilities().getCurrentMap(player)).equalsIgnoreCase("in-game")) {
                        event.setCancelled(true);
                        player.updateInventory();
                    }
                }
            }

            if (event.getPlayer().isSneaking()) {
                return;
            }

            if (event.hasBlock() && (event.getClickedBlock().getType() == Material.WALL_SIGN || event.getClickedBlock().getType() == Material.SIGN_POST)) {
                Sign s = (Sign) event.getClickedBlock().getState();
                String map = ChatColor.stripColor(s.getLine(3));
                String team = GameUtilities.getUtilities().decideTeam(map);
                

                if (s.getLine(0).equalsIgnoreCase("Team Fortress 2") && s.getLine(2).equalsIgnoreCase("to join:")) {
                    DataChecker dc = new DataChecker(map);
                    if (!dc.allGood()) {
                        player.sendMessage("�e[TF2] This map has not yet been setup.");
                        if (player.hasPermission("tf2.create")) {
                            player.sendMessage("�e[TF2] Type �6/tf2 checkdata " + map + " �eto see what else needs to be done.");
                        }
                        return;
                    }
                    if (!player.hasPermission("tf2.play")) {
                        event.getPlayer().sendMessage("�e[TF2] You do not have permission.");
                        return;
                    }
                    
                    if (GameUtilities.getUtilities().isIngame(player)) {
                        event.getPlayer().sendMessage("�e[TF2] You are already playing on a map!");
                        return;
                    }

                    if (DataConfiguration.getData().getDataFile().getStringList("disabled-maps").contains(map)) {
                        player.sendMessage("�e[TF2] That map is disabled.");
                        return;
                    }

                    Queue q = GameUtilities.getUtilities().plugin.getQueue(map);
                    if (!player.hasPermission("tf2.create")) {
                        if (q.contains(player)) {
                            player.sendMessage("�e[TF2] You are #" + q.getPosition(player.getName()) + " in line for this map.");
                            return;
                        }
                        q.add(player);
                        Integer position = q.getPosition(player.getName());

                        if (GameUtilities.getUtilities().getIngameList(map).size() + 1 <= MapConfiguration.getMaps().getMap(map).getInt("playerlimit")) {
                            q.remove(position);
                            GameUtilities.getUtilities().joinGame(player, map, team);
                            player.sendMessage("�e[TF2] You joined the " + map + " �r�emap!");
                        } else {
                            player.sendMessage("�e[TF2] You are #" + position + " in line for this map.");
                        }
                    } else {
                        GameUtilities.getUtilities().joinGame(player, map, team);
                    }

                    event.setCancelled(true);
                    player.sendMessage("�e[TF2] You joined the " + map + " �r�emap!");
                }
            }

            if (event.hasBlock() && event.getClickedBlock().getType() == Material.STONE_BUTTON) {
                if (GameUtilities.getUtilities().isIngame(player)) {
                    for (String s : DataConfiguration.getData().getDataFile().getStringList("classbuttons")) {
                        if (ClassUtilities.getUtilities().loadClassButtonLocation(s).toString().equalsIgnoreCase(event.getClickedBlock().getLocation().toString())) {
                            if (player.hasPermission("tf2.button." + ClassUtilities.getUtilities().loadClassButtonTypeFromLocation(s))) {
                                ClassUtilities.getUtilities().changeClass(player, ClassUtilities.getUtilities().loadClassFromLocation(s));
                                return;
                            }
                            event.getPlayer().sendMessage("�e[TF2] " + GeneralUtilities.colorize(GameUtilities.getUtilities().plugin.getConfig().getString("donor-button-noperm")));
                        }
                    }

                    for (String s : DataConfiguration.getData().getDataFile().getStringList("changeclassbuttons")) {
                        if (ClassUtilities.getUtilities().loadClassButtonLocation(s).toString().equalsIgnoreCase(event.getClickedBlock().getLocation().toString())) {
                            GameUtilities.getUtilities().usingchangeclassbutton.add(event.getPlayer().getName());
                            event.getPlayer().teleport(MapUtilities.getUtilities().loadTeamLobby(GameUtilities.getUtilities().ingame.get(event.getPlayer().getName()), GameUtilities.getUtilities().teams.get(event.getPlayer().getName())));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
