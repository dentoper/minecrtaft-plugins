package com.dentoper.smartharvest.listeners;

import com.dentoper.smartharvest.SmartHarvestPlus;
import com.dentoper.smartharvest.config.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final SmartHarvestPlus plugin;

    public PlayerJoinListener(SmartHarvestPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ConfigManager configManager = plugin.getConfigManager();

        configManager.getPlayerSettings(player.getUniqueId());
    }
}
