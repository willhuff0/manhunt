package com.icecoldmoon.manhunt.midgame;

import java.util.List;

import com.icecoldmoon.manhunt.GameOptions;
import com.icecoldmoon.manhunt.TeamManager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class HunterPlayer {
    public Player player;
    public ItemStack compass;
    
    HunterPlayer(Player player) {
        this.player = player;
    }

    private boolean trackingHunters;

    public void Start() {
        compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        meta.setDisplayName("Tracker");
        meta.setLore(List.of("Right click to toggle tracked team"));
        compass.setItemMeta(meta);
        player.getInventory().setItem(8, compass);

        if (GameOptions.HEADSTART > 0)  {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 9999999, 255));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 9999999, 255));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 9999999, 255));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 9999999, 255));
        }
    }

    public void EndHeadstart() {
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.SLOW);
        player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
        player.removePotionEffect(PotionEffectType.JUMP);
    }

    public void Tick() {
        Location trackedLocation;
        if (trackingHunters) {
            Player trackingPlayer = Midgame.GetClosestHunter(player);
            if (trackingPlayer != null) trackedLocation = trackingPlayer.getLocation();
            else {
                trackingHunters = false;
                trackedLocation = Midgame.GetClosestRunner(player).getLocation();
            }
        }
        else trackedLocation = Midgame.GetClosestRunner(player).getLocation();
        
        Environment environment = player.getWorld().getEnvironment();
        if (environment == Environment.NORMAL) { player.setCompassTarget(trackedLocation); } 
        else if (environment == Environment.NETHER && GameOptions.NETHER_TRACKING) {
            trackedLocation.setY(0);
            trackedLocation.getBlock().setType(Material.LODESTONE);

            CompassMeta meta = (CompassMeta) compass.getItemMeta();
            meta.setLodestone(trackedLocation);
            compass.setItemMeta(meta);
        }
    }

    public void OnClickCompass() { 
        if (trackingHunters) {
            trackingHunters = false;
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Tracking runners"));
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 1.0f, 1.0f);
        }
        else if (TeamManager.hunters.size() > 1) {
            trackingHunters = true;
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Tracking hunters"));
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 1.0f, 1.0f);
        }
        else {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("No other hunters. Tracking runners"));
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 1.0f, 1.0f);
        }
        Tick();
     }

    public void OnTravelToNether() {
        CompassMeta meta = (CompassMeta) compass.getItemMeta();
        meta.setLodestoneTracked(true);
        compass.setItemMeta(meta);
    }
    public void OnTravelFromNether() {
        CompassMeta meta = (CompassMeta) compass.getItemMeta();
        meta.setLodestoneTracked(false);
        compass.setItemMeta(meta);
    }
}
