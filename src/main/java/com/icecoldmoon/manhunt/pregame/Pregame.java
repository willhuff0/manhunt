package com.icecoldmoon.manhunt.pregame;

import java.util.HashSet;
import java.util.Set;

import com.icecoldmoon.manhunt.GameState;
import com.icecoldmoon.manhunt.Manhunt;
import com.icecoldmoon.manhunt.input.Hotbars;
import com.icecoldmoon.manhunt.input.InputManager;
import com.icecoldmoon.manhunt.midgame.Midgame;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Pregame {
    private static Manhunt plugin;

    public static void Setup(Manhunt plugin) {
        Pregame.plugin = plugin;
    }

    private static Set<Player> stayVotes;
    private static Set<Player> skipVotes;

    private static int taskId;
    private static boolean firstVote;

    private static BossBar bossBar = Bukkit.createBossBar("", BarColor.PURPLE, BarStyle.SEGMENTED_10);

    public static void StartVoting() {
        stayVotes = new HashSet<Player>();
        skipVotes = new HashSet<Player>();
        firstVote = true;
        UpdateUI();
        bossBar.setTitle("Seed: " + Long.toString(WorldManager.seed));
        InputManager.SetAll((isOp) -> Hotbars.GetVotingHotbar(isOp));
    }
    private static void CheckVotingDone() {
        if (firstVote) {
            firstVote = false;
            remainingTicks = 200;
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() { @Override public void run() { Tick(); } }, 0, 1);
        }
        int stayCount = stayVotes.size();
        int skipCount = skipVotes.size();
        if (stayCount + skipCount >= Bukkit.getOnlinePlayers().size()) EndVoting(stayCount, skipCount);
    }

    private static int remainingTicks;
    private static void Tick() {
        bossBar.setProgress(remainingTicks / 200);
        remainingTicks--;
        if (remainingTicks <= 0) EndVoting();
    }

    private static void EndVoting() { EndVoting(stayVotes.size(), skipVotes.size()); }
    private static void EndVoting(int stayCount, int skipCount) {
        if (stayCount == 0 && skipCount == 0) return;
        Bukkit.getScheduler().cancelTask(taskId);
        if (stayCount > skipCount) EndPregame();
        else Regenerate();
    }

    public static void VoteStay(Player player) {
        UpdateUI();
        if (skipVotes.contains(player)) skipVotes.remove(player);
        stayVotes.add(player);
        CheckVotingDone();
    }
    public static void VoteSkip(Player player) {
        UpdateUI();
        if (stayVotes.contains(player)) stayVotes.remove(player);
        skipVotes.add(player);
        CheckVotingDone();
    }

    private static void UpdateUI() {
        TextComponent stayVotesComponent = new TextComponent(Integer.toString(stayVotes.size()));
        stayVotesComponent.setColor(ChatColor.GREEN);

        TextComponent skipVotesComponent = new TextComponent(Integer.toString(skipVotes.size()));
        skipVotesComponent.setColor(ChatColor.RED);

        TextComponent[] components = new TextComponent[] {
            stayVotesComponent,
            new TextComponent(" | "),
            skipVotesComponent,
        };

        Bukkit.getOnlinePlayers().forEach((player) -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR, components));
    }

    public static void StartPregame() {
        Manhunt.state = GameState.pregame;
        Bukkit.getOnlinePlayers().forEach((player) -> bossBar.addPlayer(player));
        InputManager.active = true;
        Regenerate();
    }

    public static void EndPregame() {
        bossBar.setProgress(1.0);
        bossBar.setTitle("World selected. Preparing game...");
        InputManager.active = false;
        InputManager.ClearAll();
        WorldManager.RegenerateMHNether();
        WorldManager.RegenerateMHEnd();
        Midgame.StartMidgame();
    }

    public static void Regenerate() {
        bossBar.setProgress(1.0);
        bossBar.setTitle("Generating world preview...");
        WorldManager.ToLimbo();
        WorldManager.RegenerateSeed();
        WorldManager.RegenerateMHWorld();
        WorldManager.ExitLimbo(WorldManager.mhWorld);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { @Override public void run() { StartVoting(); } }, 20);
    }
}
