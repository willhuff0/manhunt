package com.icecoldmoon.manhunt.midgame;

import java.util.HashSet;
import java.util.Set;

import com.github.yannicklamprecht.worldborder.api.IWorldBorder;
import com.github.yannicklamprecht.worldborder.api.Position;
import com.github.yannicklamprecht.worldborder.api.WorldBorderAction;
import com.github.yannicklamprecht.worldborder.api.WorldBorderApi;
import com.icecoldmoon.manhunt.GameOptions;
import com.icecoldmoon.manhunt.GameState;
import com.icecoldmoon.manhunt.Manhunt;
import com.icecoldmoon.manhunt.TeamManager;
import com.icecoldmoon.manhunt.endgame.FireworksManager;
import com.icecoldmoon.manhunt.pregame.WorldManager;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Midgame {
    private static Manhunt plugin;
    private static WorldBorderApi worldBorderApi;

    private static Set<HunterPlayer> hunters;

    public static boolean isInHeadstart;
    
    public static void Setup(Manhunt plugin) {
        Midgame.plugin = plugin;
        worldBorderApi = Bukkit.getServer().getServicesManager().getRegistration(WorldBorderApi.class).getProvider();
    }

    public static void StartMidgame() {
        Manhunt.state = GameState.midgame;
        Bukkit.getLogger().info("started midgame");
        isInHeadstart = false;
        returnedElytra = false;
        hunters = new HashSet<HunterPlayer>();
        Bukkit.getOnlinePlayers().forEach((player) -> { player.setGameMode(GameMode.SURVIVAL); player.teleport(WorldManager.mhWorld.getSpawnLocation()); } );
        TeamManager.hunters.forEach((hunter) -> hunters.add(new HunterPlayer(hunter)));
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { @Override public void run() { AfterStartCountdown(); }; }, 3 * 20);
    }
    private static void AfterStartCountdown() {
        if (GameOptions.HEADSTART > 0) {
            isInHeadstart = true;
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { @Override public void run() { AfterHeadstart(); }; }, GameOptions.HEADSTART * 20);
        }
        WorldManager.UnfreezeWorld(WorldManager.mhWorld);
        hunters.forEach((hunter) -> hunter.Start());
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() { @Override public void run() { Tick(); } }, 0, 10);
    }
    private static void AfterHeadstart() {
        isInHeadstart = false;
        hunters.forEach((hunter) -> hunter.EndHeadstart());
    }

    private static void Tick() {
        if (!isInHeadstart) hunters.forEach((hunter) -> hunter.Tick());
        TeamManager.runners.forEach((runner) -> {
            if (GameOptions.RUNNER_SENSE) {
                World world = runner.getWorld();
                Set<Player> huntersInWorld = new HashSet<>();
                TeamManager.hunters.forEach((hunter) -> { if (hunter.getWorld() == world) huntersInWorld.add(hunter); });

                final Location runnerLocation = runner.getLocation();
                double closestDistance = Double.MAX_VALUE;
                for (Player hunter : huntersInWorld) {
                    double distance = runnerLocation.distanceSquared(hunter.getLocation());
                    if (distance < closestDistance) closestDistance = distance;
                }
                final double intensity = Math.max(Math.min(1 - closestDistance * 0.001, 1), 0);

                final IWorldBorder worldBorder = worldBorderApi.getWorldBorder(runner);
                worldBorder.setCenter(Position.of(runnerLocation));
                worldBorder.setWarningDistanceInBlocks((int)(worldBorder.getSize() - (1 - intensity) * 10));
                worldBorder.send(runner, WorldBorderAction.SET_WARNING_BLOCKS);
            }
        });
    }

    public static void CheckHuntersWon(Player killingPlayer) {
        boolean won = true;
        for (Player runner : TeamManager.runners) { if(runner.getGameMode() != GameMode.SPECTATOR) won = false; }
        if (won) EndMidgame(false, killingPlayer);
    }

    public static boolean returnedElytra;
    public static Location fallbackWinLocation;
    public static void EndMidgame(boolean runnersWon, Player winningPlayer) { // winningPlayer can be null
        Manhunt.state = GameState.ended;
        
        Location location = winningPlayer == null ? fallbackWinLocation : winningPlayer.getLocation();

        if (runnersWon) Bukkit.broadcastMessage("Game over! Runners win.");
        else Bukkit.broadcastMessage("Game over! Hunters win.");

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { @Override public void run() { 
            Bukkit.getOnlinePlayers().forEach((player) -> player.teleport(location));
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { @Override public void run() { 
                FireworksManager.PlayFireworks(runnersWon, location);
             } }, 1 * 20);
         } }, 3 * 20);
    }

    public static Player GetClosestHunter(Player runner) {
        World world = runner.getWorld();
        Set<Player> huntersInWorld = new HashSet<>();
        TeamManager.hunters.forEach((hunter) -> { if (hunter.getWorld() == world) huntersInWorld.add(hunter); });
        final Location runnerLocation = runner.getLocation();

        Player closestHunter = null;
        double closestDistance = Double.MAX_VALUE;
        for (Player hunter : huntersInWorld) {
            if (hunter.getUniqueId() == runner.getUniqueId()) continue;
            double distance = runnerLocation.distanceSquared(hunter.getLocation());
            if (distance < closestDistance) {
                closestHunter = hunter;
                closestDistance = distance;
            };
        }

        return closestHunter;
    }
    public static Player GetClosestRunner(Player hunter) {
        World world = hunter.getWorld();
        Set<Player> runnersInWorld = new HashSet<>();
        TeamManager.runners.forEach((runner) -> { if (runner.getWorld() == world) runnersInWorld.add(runner); });
        final Location hunterLocation = hunter.getLocation();

        Player closestRunner = null;
        double closestDistance = Double.MAX_VALUE;
        for (Player runner : runnersInWorld) {
            if (runner.getUniqueId() == hunter.getUniqueId()) continue;
            double distance = hunterLocation.distanceSquared(runner.getLocation());
            if (distance < closestDistance) {
                closestRunner = runner;
                closestDistance = distance;
            };
        }

        return closestRunner;
    }

    public static HunterPlayer GetHunterPlayer(Player player) { 
        HunterPlayer matchingHunterPlayer = null;
        for (HunterPlayer hunter : hunters) {
            if (hunter.player == player) {
                matchingHunterPlayer = hunter;
                break;
            }
        }
        return matchingHunterPlayer;
    }
}
