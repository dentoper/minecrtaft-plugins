package com.dentoper.playercollars.listeners;

import com.dentoper.playercollars.PlayerCollarsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final PlayerCollarsPlugin plugin;

    public PlayerQuitListener(PlayerCollarsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getPlayerCollarData().save();
    }
}
