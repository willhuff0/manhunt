package com.icecoldmoon.manhunt.input;

import com.icecoldmoon.manhunt.TeamManager;
import com.icecoldmoon.manhunt.pregame.Pregame;

import org.bukkit.entity.Player;

public class InputHandler {
    public static void Start() { Pregame.StartPregame(); }

    public static void Settings() {

    }

    public static void JoinRunners(Player player) { TeamManager.JoinRunners(player); }
    public static void JoinHunters(Player player) { TeamManager.JoinHunters(player); }

    public static void VoteStay(Player player) { Pregame.VoteStay(player); }
    public static void VoteSkip(Player player) { Pregame.VoteSkip(player); }
}
