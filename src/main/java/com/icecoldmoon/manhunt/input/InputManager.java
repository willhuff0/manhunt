package com.icecoldmoon.manhunt.input;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public class InputManager {
    public static boolean active;

    private static Map<Player, HotbarAction[]> data = new HashMap<Player, HotbarAction[]>();

    public static void Click(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        HotbarAction[] playerHotbar = data.get(player);
        if (playerHotbar == null) return;
        HotbarAction action = playerHotbar[player.getInventory().getHeldItemSlot()];
        if (action == null) return;
        action.OnClick.accept(player);
    }

    public static void SetSlotAll(int slot, HotbarAction action) { Bukkit.getOnlinePlayers().forEach((player) -> SetSlot(player, slot, action)); }
    public static void SetSlot(Player player, int slot, HotbarAction action) {
        data.putIfAbsent(player, new HotbarAction[9]);
        data.get(player)[slot] = action;
        player.getInventory().setItem(slot, action != null ? action.item : null);
    }

    public static void SetAll(HotbarAction[] actions) { Bukkit.getOnlinePlayers().forEach((player) -> Set(player, actions)); }
    public static void SetAll(Function<Boolean, HotbarAction[]> function) { Bukkit.getOnlinePlayers().forEach((player) -> Set(player, function.apply(player.isOp()))); }
    public static void Set(Player player, HotbarAction[] actions) { for (int i = 0; i < 9; i++) SetSlot(player, i, actions[i]); }

    public static void Wipe() { data = new HashMap<Player, HotbarAction[]>(); }
    public static void ClearAll() { Bukkit.getOnlinePlayers().forEach((player) -> Clear(player)); }
    public static void Clear(Player player) { 
        data.put(player, new HotbarAction[9]);
        player.getInventory().clear();
     }
}