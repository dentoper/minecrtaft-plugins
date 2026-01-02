package com.example.mineskills.listeners;

import com.example.mineskills.managers.PlayerDataManager;
import com.example.mineskills.managers.SkillApplier;
import com.example.mineskills.managers.ActionTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Слушатель событий отключения игроков
 * Обрабатывает выход игроков с сервера
 */
public class PlayerQuitListener implements Listener {
    private final PlayerDataManager playerDataManager;
    private final SkillApplier skillApplier;
    private final ActionTracker actionTracker;

    public PlayerQuitListener(PlayerDataManager playerDataManager, SkillApplier skillApplier, ActionTracker actionTracker) {
        this.playerDataManager = playerDataManager;
        this.skillApplier = skillApplier;
        this.actionTracker = actionTracker;
    }

    /**
     * Обработка отключения игрока
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Сохраняем данные игрока
        playerDataManager.savePlayerData(player.getUniqueId());
        
        // Удаляем все активные эффекты скиллов
        skillApplier.removeAllSkillEffects(player);
        
        // Удаляем данные отслеживания действий
        actionTracker.removePlayer(player.getUniqueId());
        
        // Удаляем GUI данные
        // guiManager.removePlayer(player); // Раскомментировать если нужен GuiManager в конструкторе
    }
}