package com.example.mineskill.listeners;

import com.example.mineskill.MineSkillPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final MineSkillPlugin plugin;

    public PlayerQuitListener(MineSkillPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        plugin.getPlayerDataManager().savePlayerData(player);
        plugin.getGuiManager().closeGui(player);

        plugin.getLogger().info("Сохранены данные скиллов для игрока: " + player.getName());
    }
}
