package com.dentoper.playercollars.listeners;

import com.dentoper.playercollars.PlayerCollarsPlugin;
import com.dentoper.playercollars.config.ConfigManager;
import com.dentoper.playercollars.utils.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerJoinListener implements Listener {
    private final PlayerCollarsPlugin plugin;

    public PlayerJoinListener(PlayerCollarsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String collarId = plugin.getPlayerCollarData().getCollar(player.getUniqueId());
        
        if (collarId != null) {
            ConfigManager.CollarData collarData = plugin.getConfigManager().getCollar(collarId);
            if (collarData != null) {
                equipCollar(player, collarData);
            }
        }
    }

    private void equipCollar(Player player, ConfigManager.CollarData collarData) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.color(collarData.getDisplayName()));
            meta.setCustomModelData(collarData.getModelData());
            item.setItemMeta(meta);
        }
        player.getInventory().setHelmet(item);
    }
}
