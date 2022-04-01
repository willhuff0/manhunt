package com.icecoldmoon.manhunt;

import com.icecoldmoon.manhunt.endgame.FireworksManager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MHCommandExecutor implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FireworksManager.PlayFireworks(true, ((Player) sender).getLocation());
        return true;
    }
}
