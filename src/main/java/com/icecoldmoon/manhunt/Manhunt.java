package com.icecoldmoon.manhunt;

import com.icecoldmoon.manhunt.endgame.FireworksManager;
import com.icecoldmoon.manhunt.input.Hotbars;
import com.icecoldmoon.manhunt.input.InputManager;
import com.icecoldmoon.manhunt.midgame.Midgame;
import com.icecoldmoon.manhunt.pregame.Pregame;
import com.icecoldmoon.manhunt.pregame.WorldManager;

import org.bukkit.plugin.java.JavaPlugin;

public class Manhunt extends JavaPlugin
{
    public static GameState state;

    @Override
    public void onEnable() {
        state = GameState.stopped;

        WorldManager.Setup(this);
        Pregame.Setup(this);
        Midgame.Setup(this);
        FireworksManager.Setup(this);
        getServer().getPluginManager().registerEvents(new MHListener(this), this);
        getCommand("mh").setExecutor(new MHCommandExecutor());

        WorldManager.ToLimbo();
        InputManager.active = true;
        InputManager.SetAll((isOp) -> Hotbars.GetPregameHotbar(isOp));
    }
}
