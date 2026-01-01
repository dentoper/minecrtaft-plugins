package com.example.mineskill.listeners;

import com.example.mineskill.MineSkillPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final MineSkillPlugin plugin;

    public PlayerJoinListener(MineSkillPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        plugin.getPlayerDataManager().getPlayerData(player);
        plugin.getPlayerDataManager().giveInitialPoints(player);
        plugin.getSkillApplier().applySkills(player);

        plugin.getLogger().info("Загружены данные скиллов для игрока: " + player.getName());
    }
}
