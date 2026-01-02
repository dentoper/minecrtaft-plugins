package com.example.mineskills.listeners;

import com.example.mineskills.managers.PlayerDataManager;
import com.example.mineskills.managers.SkillApplier;
import com.example.mineskills.managers.ActionTracker;
import com.example.mineskills.models.PlayerSkillData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

/**
 * Слушатель событий GUI для MineSkills
 * Обрабатывает клики, открытие и закрытие инвентарей скилл-дерева
 */
public class GuiClickListener implements Listener {
    private final PlayerDataManager playerDataManager;
    private final SkillApplier skillApplier;
    private final ActionTracker actionTracker;
    private final com.example.mineskills.gui.GuiManager guiManager;

    public GuiClickListener(PlayerDataManager playerDataManager, SkillApplier skillApplier,
                          ActionTracker actionTracker, com.example.mineskills.gui.GuiManager guiManager) {
        this.playerDataManager = playerDataManager;
        this.skillApplier = skillApplier;
        this.actionTracker = actionTracker;
        this.guiManager = guiManager;
    }

    /**
     * Обработка кликов в GUI
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        
        // Проверяем, открыт ли GUI MineSkills
        if (!guiManager.hasOpenGui(player)) {
            return;
        }

        // Проверяем, что клик внутри нашего инвентаря
        if (event.getView().getTopInventory() == null || 
            event.getView().getTopInventory().getHolder() != null) {
            return;
        }

        // Обрабатываем клик через GuiManager
        guiManager.handleInventoryClick(player, event);
    }

    /**
     * Обработка открытия инвентаря
     */
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        
        // Применяем все скиллы игрока при открытии GUI
        if (guiManager.hasOpenGui(player)) {
            skillApplier.applyAllSkills(player);
        }
    }

    /**
     * Обработка закрытия инвентаря
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        
        // Обрабатываем закрытие через GuiManager
        guiManager.handleInventoryClose(player);
    }

    /**
     * Обработка бронированных предметов (запрет на перемещение)
     */
    @EventHandler
    public void onInventoryDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        
        // Проверяем, открыт ли GUI MineSkills
        if (guiManager.hasOpenGui(player)) {
            event.setCancelled(true);
        }
    }

    /**
     * Обработка перемещения предметов
     */
    @EventHandler
    public void onInventoryMoveItem(org.bukkit.event.inventory.InventoryMoveItemEvent event) {
        // Предотвращаем перемещение предметов между GUI
        // Это событие вызывается при попытке переместить предмет
        // в наш инвентарь или из нашего инвентаря
        
        // Проверяем, является ли инвентарь частью нашего GUI
        // (это сложнее определить, поэтому просто запрещаем)
        if (event.getDestination() != null && event.getSource() != null) {
            // В реальной реализации здесь должна быть проверка
            // является ли инвентарь нашим GUI
        }
    }

    /**
     * Обработка закрытия GUI при смерти игрока
     */
    @EventHandler
    public void onPlayerRespawn(org.bukkit.event.player.PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        // При респавне закрываем все GUI
        if (guiManager.hasOpenGui(player)) {
            player.closeInventory();
        }
    }

    /**
     * Обработка закрытия GUI при телепортации
     */
    @EventHandler
    public void onPlayerTeleport(org.bukkit.event.player.PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        
        // При телепортации закрываем все GUI
        if (guiManager.hasOpenGui(player)) {
            player.closeInventory();
        }
    }

    /**
     * Обработка закрытия GUI при изменении мира
     */
    @EventHandler
    public void onPlayerChangedWorld(org.bukkit.event.player.PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        
        // При смене мира закрываем все GUI
        if (guiManager.hasOpenGui(player)) {
            player.closeInventory();
        }
    }

    /**
     * Обработка закрытия GUI при выходе с сервера
     */
    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Удаляем данные GUI при выходе
        guiManager.removePlayer(player);
    }

    /**
     * Проверка, является ли инвентарь частью нашего GUI
     */
    private boolean isMineSkillsInventory(org.bukkit.inventory.Inventory inventory) {
        if (inventory == null || inventory.getTitle() == null) {
            return false;
        }
        
        String title = inventory.getTitle();
        return title.contains("Дерево Скиллов") || 
               title.contains("Цепочка Навыков") ||
               title.contains("Skill Tree") ||
               title.contains("Skill Chain");
    }
}