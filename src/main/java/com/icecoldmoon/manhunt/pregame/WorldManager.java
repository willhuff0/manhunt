package com.icecoldmoon.manhunt.pregame;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import com.icecoldmoon.manhunt.input.InputManager;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WorldManager {
    private static Plugin plugin;

    public static World limboWorld;
    public static World mhWorld;
    public static World mhNether;
    public static World mhEnd;

    public static void Setup(Plugin plugin) { 
        WorldManager.plugin = plugin; 

        limboWorld = Bukkit.getWorld("mh_limbo"); 
        if (limboWorld == null) {
            WorldCreator worldCreator = new WorldCreator("mh_limbo");
            worldCreator.type(WorldType.FLAT);
            worldCreator.generatorSettings("{\"structures\": {\"structures\": {}}, \"layers\": [{\"block\": \"glass\", \"height\": 1}], \"biome\":\"plains\"}");
            worldCreator.generateStructures(false);

            limboWorld = worldCreator.createWorld();
            limboWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            limboWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            limboWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            limboWorld.setTime(23225L);
        }
        
        mhWorld = Bukkit.getWorld("mh_world");
        mhNether = Bukkit.getWorld("mh_nether");
        mhEnd = Bukkit.getWorld("mh_end");
    };

    private static boolean inLimbo = true;
    public static boolean GetInLimbo() { return inLimbo; }
    public static void ToLimbo() {
        inLimbo = true;
        Bukkit.getOnlinePlayers().forEach((player) -> { player.teleport(limboWorld.getSpawnLocation()); ResetPlayer(player); });
    }
    public static void ExitLimbo(World toWorld) {
        inLimbo = false;
        Bukkit.getOnlinePlayers().forEach((player) -> player.teleport(toWorld.getSpawnLocation()));
    }

    private static Random random = new Random();
    public static long seed;
    public static void RegenerateSeed() { seed = random.nextLong(); Bukkit.getLogger().info(Long.toString(seed)); }

    public static void RegenerateMHWorld() { mhWorld = RegenerateWorld("mh_world", Environment.NORMAL); }
    public static void RegenerateMHNether() { mhWorld = RegenerateWorld("mh_nether", Environment.NETHER); }
    public static void RegenerateMHEnd() { mhWorld = RegenerateWorld("mh_end", Environment.THE_END); }

    private static World RegenerateWorld(String name, Environment environment) {
        World world = Bukkit.getWorld(name);
        if (world != null) {
            Bukkit.unloadWorld(world, false);
            try { FileUtils.deleteDirectory(new File(Bukkit.getWorldContainer(), name)); } catch (IOException e) { e.printStackTrace(); }
        }
        WorldCreator worldCreator = new WorldCreator(name);
        worldCreator.environment(environment);
        worldCreator.seed(seed);
        world = worldCreator.createWorld();
        FreezeWorld(world);
        return world;
    }

    public static void FreezeWorld(World world) {
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
    }

    public static void UnfreezeWorld(World world) {
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
    }

    public static void ResetPlayer(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setInvulnerable(true);
        player.setFireTicks(0);
        InputManager.Clear(player);
    }
}
