package com.icecoldmoon.manhunt;

import com.icecoldmoon.manhunt.input.Hotbars;
import com.icecoldmoon.manhunt.input.InputManager;
import com.icecoldmoon.manhunt.midgame.HunterPlayer;
import com.icecoldmoon.manhunt.midgame.Midgame;
import com.icecoldmoon.manhunt.pregame.WorldManager;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World.Environment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class MHListener implements Listener {
    private Plugin plugin;

    MHListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (WorldManager.GetInLimbo()) player.teleport(WorldManager.limboWorld.getSpawnLocation());
        else if (player.getWorld() == WorldManager.limboWorld) player.teleport(WorldManager.mhWorld.getSpawnLocation());

        if (Manhunt.state == GameState.stopped) InputManager.Set(player, Hotbars.GetPregameHotbar(player.isOp()));
        Bukkit.getLogger().info(Manhunt.state.name());
    }

    @EventHandler 
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (Manhunt.state == GameState.midgame) {
            if (Midgame.isInHeadstart && TeamManager.IsHunter(event.getPlayer())) event.setCancelled(true);
            Action action = event.getAction();
            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                ItemStack item = event.getItem();
                if (item != null && item.getType() == Material.COMPASS) {
                    HunterPlayer hunter = Midgame.GetHunterPlayer(event.getPlayer());
                    if (hunter != null) hunter.OnClickCompass();
                }
            }
        }
        else {
            if (!InputManager.active) return;
            InputManager.Click(event);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (Manhunt.state == GameState.midgame) {
            if (Midgame.isInHeadstart) {
                if (TeamManager.IsHunter(event.getPlayer())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (Manhunt.state == GameState.midgame) {
            EntityType type = event.getEntityType();
            if (GameOptions.WIN_REQUIRMENT == 2 && type == EntityType.ENDER_DRAGON) { 
                LivingEntity entity = event.getEntity();
                Midgame.fallbackWinLocation = entity.getLocation();
                Midgame.EndMidgame(true, entity.getKiller());
            }
            else if (GameOptions.WIN_REQUIRMENT == 6 && Midgame.returnedElytra && type == EntityType.WITHER) {
                LivingEntity entity = event.getEntity();
                Midgame.fallbackWinLocation = entity.getLocation();
                Midgame.EndMidgame(true, entity.getKiller());
            };
        } 
    }

    @EventHandler 
    void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location location = player.getLocation();

        player.spigot().respawn();
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(location);

        if (TeamManager.IsHunter(player)) {
            player.sendMessage("You died. Respawning...");
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { @Override public void run() { 
                Location spawnLocation = player.getBedSpawnLocation();
                if (spawnLocation == null) spawnLocation = WorldManager.mhWorld.getSpawnLocation();
                player.teleport(spawnLocation);
                player.setGameMode(GameMode.SURVIVAL);
                } }, 3 * 20);
        }
        else {
            player.sendMessage("You died. Spectating...");
            Midgame.CheckHuntersWon(player.getKiller());
        }
    }

    public void onPlayerAdvancement(PlayerAdvancementDoneEvent event) {
        if (Manhunt.state == GameState.midgame && GameOptions.WIN_REQUIRMENT == 3 && event.getAdvancement().getKey() == NamespacedKey.minecraft("end/elytra")) {
            Player player = event.getPlayer();
            if (TeamManager.IsRunner(player)) Midgame.EndMidgame(true, player);
        }
    }

    @EventHandler 
    public void onPlayerPortal(PlayerPortalEvent event) {
        TeleportCause cause = event.getCause();
        Environment fromEnvironment = event.getFrom().getWorld().getEnvironment();

        if (cause == TeleportCause.NETHER_PORTAL) {
            if (fromEnvironment == Environment.NORMAL) {
                Location oldTo = event.getTo();
                Location newTo = new Location(WorldManager.mhNether, oldTo.getX(), oldTo.getY(), oldTo.getZ());
                event.setTo(newTo);
            } 
            else if (fromEnvironment == Environment.NETHER) {
                Location oldTo = event.getTo();
                Location newTo = new Location(WorldManager.mhWorld, oldTo.getX(), oldTo.getY(), oldTo.getZ());
                event.setTo(newTo);
            }
        }
        else if (cause == TeleportCause.END_PORTAL) {
            if (fromEnvironment == Environment.NORMAL) {
                Location oldTo = event.getTo();
                Location newTo = new Location(WorldManager.mhEnd, oldTo.getX(), oldTo.getY(), oldTo.getZ());
                event.setTo(newTo);
            }
        }
        // end to overworld teleport doesn't work
        // if (fromEnvironment == Environment.THE_END && toWorld.getEnvironment() == Environment.NORMAL) {
        //     Location oldTo = event.getTo();
        //         Location newTo = new Location(WorldManager.mhWorld, oldTo.getX(), oldTo.getY(), oldTo.getZ());
        //         event.setTo(newTo);
        // }

        if (Manhunt.state == GameState.midgame) {
            Player player = event.getPlayer();
            Environment toEnvironment = event.getTo().getWorld().getEnvironment();

            if (toEnvironment == Environment.NETHER) {
                if (GameOptions.WIN_REQUIRMENT == 0 && TeamManager.IsRunner(player)) Midgame.EndMidgame(true, player);
                else {
                    HunterPlayer hunter = Midgame.GetHunterPlayer(player);
                    if (player != null) hunter.OnTravelToNether();
                }
            }
            else if (toEnvironment == Environment.THE_END && GameOptions.WIN_REQUIRMENT == 1 && TeamManager.IsRunner(player)) Midgame.EndMidgame(true, player);
            else if (fromEnvironment == Environment.NETHER) {
                HunterPlayer hunter = Midgame.GetHunterPlayer(player);
                if (player != null) hunter.OnTravelFromNether();
            }
            else if (toEnvironment == Environment.NORMAL) {
                if (GameOptions.WIN_REQUIRMENT == 4) { // get elytra
                    if (TeamManager.IsRunner(player) && player.getInventory().contains(Material.ELYTRA)) Midgame.EndMidgame(true, player);
                }
                else if (GameOptions.WIN_REQUIRMENT == 6) Midgame.returnedElytra = true; // get elytra then kill wither
            }
        }
    }
}
