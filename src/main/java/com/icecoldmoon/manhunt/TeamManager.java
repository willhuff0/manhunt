package com.icecoldmoon.manhunt;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

public class TeamManager {
    public static Set<Player> runners = new HashSet<Player>();
    public static Set<Player> hunters = new HashSet<Player>();
   
    public static void JoinRunners(Player player) { 
        hunters.remove(player); 
        runners.add(player); 

        player.sendMessage("Joined runners");
    }
    public static void JoinHunters(Player player) { 
        runners.remove(player); 
        hunters.add(player); 

        player.sendMessage("Joined hunters");
    } 

    public static boolean IsRunner(Player player) { return runners.contains(player); }
    public static boolean IsHunter(Player player) { return hunters.contains(player); }
}
