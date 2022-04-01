package com.icecoldmoon.manhunt.input;

import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HotbarAction {
    public ItemStack item;
    public Consumer<Player> OnClick;

    HotbarAction(ItemStack item, Consumer<Player> OnClick) {
        this.item = item;
        this.OnClick = OnClick;
    }

    HotbarAction(Material material, Consumer<Player> OnClick) {
        item = new ItemStack(material);
        this.OnClick = OnClick;
    }
    
    HotbarAction(Material material, String name, Consumer<Player> OnClick) {
        item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        this.OnClick = OnClick;
    }

    HotbarAction(Material material, int amount, Consumer<Player> OnClick) {
        item = new ItemStack(material, amount);
        this.OnClick = OnClick;
    }

    HotbarAction(Material material, String name, int amount, Consumer<Player> OnClick) {
        item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        this.OnClick = OnClick;
    }
}
