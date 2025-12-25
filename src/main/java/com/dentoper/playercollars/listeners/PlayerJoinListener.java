package com.dentoper.playercollars.listeners;

import com.dentoper.playercollars.PlayerCollarsPlugin;
import com.dentoper.playercollars.config.ConfigManager;
import com.dentoper.playercollars.data.PlayerCollarData;
import com.dentoper.playercollars.utils.CollarItemUtil;
import com.dentoper.playercollars.utils.ColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final PlayerCollarsPlugin plugin;

    public PlayerJoinListener(PlayerCollarsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerCollarData data = plugin.getPlayerCollarData();

        String collarId = data.getCurrentCollar(player.getUniqueId());
        if (collarId != null) {
            ConfigManager.CollarData collarData = plugin.getConfigManager().getCollar(collarId);
            if (collarData != null) {
                player.getInventory().setHelmet(CollarItemUtil.create(plugin, collarData));
            }
        }

        if (data.isLeashed(player.getUniqueId())) {
            player.setCustomName(ColorUtil.color("&c[ПРИВЯЗАН] &f" + player.getName()));
            player.setCustomNameVisible(true);
        }
    }
}
