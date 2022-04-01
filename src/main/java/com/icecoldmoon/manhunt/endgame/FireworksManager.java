package com.icecoldmoon.manhunt.endgame;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;

public class FireworksManager {
    private static Plugin plugin;

    public static void Setup(Plugin plugin) {
        FireworksManager.plugin = plugin;
    }

    private static List<Color> runnerColors = List.of(
        Color.fromRGB(255, 255, 255),
        Color.fromRGB(220, 220, 220),
        Color.fromRGB(200, 200, 200),
        
        Color.fromRGB(0, 255, 0),
        Color.fromRGB(0, 200, 0),
        Color.fromRGB(0, 150, 0),
        Color.fromRGB(0, 255, 0),
        Color.fromRGB(0, 200, 0),
        Color.fromRGB(0, 150, 0),
        Color.fromRGB(0, 255, 0),
        Color.fromRGB(0, 200, 0),
        Color.fromRGB(0, 150, 0)
    );
    private static List<Color> hunterColors = List.of(
        Color.fromRGB(255, 255, 255),
        Color.fromRGB(220, 220, 220),
        Color.fromRGB(200, 200, 200),

        Color.fromRGB(255, 0, 0),
        Color.fromRGB(200, 0, 0),
        Color.fromRGB(150, 0, 0),
        Color.fromRGB(255, 0, 0),
        Color.fromRGB(200, 0, 0),
        Color.fromRGB(150, 0, 0),
        Color.fromRGB(255, 0, 0),
        Color.fromRGB(200, 0, 0),
        Color.fromRGB(150, 0, 0)
    );

    private static Random random = new Random();

    private static double avgNum;

    public static void PlayFireworks(boolean runnersWon, Location location) {
        World world = location.getWorld();
        List<Color> colors = runnersWon ? runnerColors : hunterColors;
        int colorsSize = colors.size();

        avgNum = 0;
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() { @Override public void run() { 
            int number = (int) (random.nextInt(6) - avgNum);
            if (avgNum < 4) avgNum += 0.1;
            for (int i = 0; i < number; i++) {
                Location randomLocation = GetRandomLocation(location);
                Firework firework = (Firework) world.spawnEntity(randomLocation, EntityType.FIREWORK);
                FireworkMeta meta = firework.getFireworkMeta();
                meta.addEffect(FireworkEffect.builder().flicker(random.nextBoolean()).trail(random.nextBoolean()).withColor(colors.get(random.nextInt(colorsSize))).withFade(colors.get(random.nextInt(colorsSize))).with(GetRandomType()).build());
                meta.setPower(random.nextInt(3));
                firework.setFireworkMeta(meta);
            }
         } }, 0, 5);

         Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { @Override public void run() { Bukkit.getScheduler().cancelTask(taskId); } }, 15 * 20);
    }

    public static Location GetRandomLocation(Location from) { return from.clone().add(random.nextDouble(-20, 20), 0, random.nextDouble(-20, 20)); }

    public static FireworkEffect.Type GetRandomType() {
        switch(random.nextInt(4)) {
            case 0:
                return FireworkEffect.Type.BALL;
            case 1:
                return FireworkEffect.Type.BALL_LARGE;
            case 2:
                return FireworkEffect.Type.BURST;
            case 3:
                return FireworkEffect.Type.STAR;
        }
        return null;
    }
}
