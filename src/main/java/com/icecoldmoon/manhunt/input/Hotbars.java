package com.icecoldmoon.manhunt.input;

import org.bukkit.Material;

public class Hotbars {
    public static HotbarAction[] GetPregameHotbar(boolean isOp) { return new HotbarAction[] {
        new HotbarAction(Material.GREEN_CONCRETE, "Join Runners", (player) -> InputHandler.JoinRunners(player)), 
        new HotbarAction(Material.RED_CONCRETE, "Join Hunters", (player) -> InputHandler.JoinHunters(player)), 
        null, 
        null, 
        isOp ? new HotbarAction(Material.SLIME_BALL, "Start Game", (player) -> InputHandler.Start()) : null, 
        null, 
        null, 
        null, 
        isOp ? new HotbarAction(Material.LEVER, "Options", (player) -> InputHandler.Settings()) : null,
    }; }

    public static HotbarAction[] GetVotingHotbar(boolean isOp) { return new HotbarAction[] {
        null,
        null,
        null,
        null,
        new HotbarAction(Material.SLIME_BALL, "Vote to Stay", (player) -> InputHandler.VoteStay(player)),
        new HotbarAction(Material.ARROW, "Vote to Skip", (player) -> InputHandler.VoteSkip(player)),
        null,
        null,
        isOp ? new HotbarAction(Material.BARRIER, "Stop", (player) -> InputHandler.VoteSkip(player)) : null,
    }; }
}
