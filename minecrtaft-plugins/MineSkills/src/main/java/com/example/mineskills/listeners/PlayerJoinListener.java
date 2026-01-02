package com.example.mineskills.listeners;

import com.example.mineskills.managers.PlayerDataManager;
import com.example.mineskills.managers.SkillApplier;
import com.example.mineskills.managers.SkillManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Слушатель событий подключения игроков
 * Обрабатывает вход игроков на сервер
 */
public class PlayerJoinListener implements Listener {
    private final PlayerDataManager playerDataManager;
    private final SkillApplier skillApplier;
    private final SkillManager skillManager;

    public PlayerJoinListener(PlayerDataManager playerDataManager, SkillApplier skillApplier, SkillManager skillManager) {
        this.playerDataManager = playerDataManager;
        this.skillApplier = skillApplier;
        this.skillManager = skillManager;
    }

    /**
     * Обработка подключения игрока
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Загружаем данные игрока
        var playerData = playerDataManager.getPlayerData(player.getUniqueId(), player.getName());
        
        // Применяем все скиллы игрока
        skillApplier.applyAllSkills(player);
        
        // Отправляем приветственное сообщение с информацией о скиллах
        sendWelcomeMessage(player, playerData);
    }

    /**
     * Отправка приветственного сообщения
     */
    private void sendWelcomeMessage(Player player, com.example.mineskills.models.PlayerSkillData playerData) {
        player.sendMessage("§6§l=== MineSkills ===");
        player.sendMessage("§7Добро пожаловать на сервер!");
        player.sendMessage("");
        player.sendMessage("§7Ваши скилл-очки: §e" + playerData.getSkillPoints());
        player.sendMessage("§7Всего заработано: §b" + playerData.getTotalPointsEarned());
        player.sendMessage("");
        player.sendMessage("§7Команды:");
        player.sendMessage("§e/skilltree §7- открыть меню скиллов");
        player.sendMessage("§e/skilltree info §7- информация о скиллах");
        
        // Проверяем, новый ли это игрок
        if (playerData.getTotalPointsEarned() <= 10) {
            player.sendMessage("");
            player.sendMessage("§a§lНовичок!");
            player.sendMessage("§7Вы получили §e10 §7очков для начала.");
            player.sendMessage("§7Начните развивать свои навыки!");
        }
        
        player.sendMessage("§6§l================");
    }
}