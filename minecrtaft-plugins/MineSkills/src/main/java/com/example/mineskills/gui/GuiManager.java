package com.example.mineskills.gui;

import com.example.mineskills.managers.SkillManager;
import com.example.mineskills.managers.PlayerDataManager;
import com.example.mineskills.managers.SkillApplier;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Менеджер GUI для управления всеми окнами интерфейса MineSkills
 * Отслеживает открытые окна и предотвращает конфликты
 */
public class GuiManager {
    private final SkillManager skillManager;
    private final PlayerDataManager playerDataManager;
    private final SkillApplier skillApplier;
    
    // Открытые GUI окна
    private final Map<UUID, SkillTreeGui> skillTreeGuis;
    private final Map<UUID, SkillChainSubMenu> skillChainGuis;
    
    // Открытые инвентари для обработки событий
    private final Map<UUID, String> openInventories; // uuid -> gui type

    public GuiManager(SkillManager skillManager, PlayerDataManager playerDataManager, SkillApplier skillApplier) {
        this.skillManager = skillManager;
        this.playerDataManager = playerDataManager;
        this.skillApplier = skillApplier;
        this.skillTreeGuis = new HashMap<>();
        this.skillChainGuis = new HashMap<>();
        this.openInventories = new HashMap<>();
    }

    /**
     * Открыть дерево скиллов для игрока
     */
    public void openSkillTree(Player player) {
        closeAllGuis(player);
        
        SkillTreeGui skillTreeGui = new SkillTreeGui(skillManager, playerDataManager, skillApplier, player);
        skillTreeGuis.put(player.getUniqueId(), skillTreeGui);
        openInventories.put(player.getUniqueId(), "skill_tree");
        
        skillTreeGui.open();
    }

    /**
     * Открыть цепочку навыков для игрока
     */
    public void openSkillChain(Player player, String skillId) {
        closeAllGuis(player);
        
        SkillChainSubMenu chainGui = new SkillChainSubMenu(skillManager, playerDataManager, skillApplier, player, skillId);
        skillChainGuis.put(player.getUniqueId(), chainGui);
        openInventories.put(player.getUniqueId(), "skill_chain");
        
        chainGui.open();
    }

    /**
     * Обработать клик в GUI
     */
    public void handleInventoryClick(Player player, org.bukkit.event.inventory.InventoryClickEvent event) {
        UUID uuid = player.getUniqueId();
        
        if (!openInventories.containsKey(uuid)) {
            return; // Не наше GUI
        }
        
        String guiType = openInventories.get(uuid);
        
        switch (guiType) {
            case "skill_tree":
                SkillTreeGui skillTreeGui = skillTreeGuis.get(uuid);
                if (skillTreeGui != null) {
                    skillTreeGui.handleClick(event);
                }
                break;
                
            case "skill_chain":
                SkillChainSubMenu chainGui = skillChainGuis.get(uuid);
                if (chainGui != null) {
                    chainGui.handleClick(event);
                }
                break;
        }
    }

    /**
     * Обработать закрытие инвентаря
     */
    public void handleInventoryClose(Player player) {
        UUID uuid = player.getUniqueId();
        closeAllGuis(player);
    }

    /**
     * Закрыть все GUI для игрока
     */
    private void closeAllGuis(Player player) {
        UUID uuid = player.getUniqueId();
        
        skillTreeGuis.remove(uuid);
        skillChainGuis.remove(uuid);
        openInventories.remove(uuid);
    }

    /**
     * Проверить, открыто ли GUI для игрока
     */
    public boolean hasOpenGui(Player player) {
        return openInventories.containsKey(player.getUniqueId());
    }

    /**
     * Получить тип открытого GUI для игрока
     */
    public String getOpenGuiType(Player player) {
        return openInventories.get(player.getUniqueId());
    }

    /**
     * Обновить отображение GUI
     */
    public void updateSkillTreeGui(Player player) {
        UUID uuid = player.getUniqueId();
        SkillTreeGui skillTreeGui = skillTreeGuis.get(uuid);
        
        if (skillTreeGui != null) {
            skillTreeGui.update();
        }
    }

    /**
     * Закрыть GUI при отключении игрока
     */
    public void removePlayer(Player player) {
        closeAllGuis(player);
    }

    /**
     * Закрыть GUI для всех игроков (при перезагрузке)
     */
    public void closeAllGuis() {
        for (UUID uuid : openInventories.keySet()) {
            Player player = org.bukkit.Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.closeInventory();
            }
        }
        
        skillTreeGuis.clear();
        skillChainGuis.clear();
        openInventories.clear();
    }

    /**
     * Получить статистику открытых GUI
     */
    public Map<String, Integer> getGuiStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("skill_tree_guis", skillTreeGuis.size());
        stats.put("skill_chain_guis", skillChainGuis.size());
        stats.put("total_open_guis", openInventories.size());
        return stats;
    }

    /**
     * Принудительно закрыть GUI определенного типа для игрока
     */
    public void forceCloseGui(Player player, String guiType) {
        UUID uuid = player.getUniqueId();
        
        if (openInventories.get(uuid) == null) {
            return; // GUI не открыто
        }
        
        if (openInventories.get(uuid).equals(guiType)) {
            player.closeInventory();
            closeAllGuis(player);
        }
    }

    /**
     * Получить активное GUI дерева скиллов игрока
     */
    public SkillTreeGui getSkillTreeGui(Player player) {
        return skillTreeGuis.get(player.getUniqueId());
    }

    /**
     * Получить активное GUI цепочки навыков игрока
     */
    public SkillChainSubMenu getSkillChainGui(Player player) {
        return skillChainGuis.get(player.getUniqueId());
    }

    /**
     * Проверить, может ли игрок открыть новое GUI
     */
    public boolean canOpenNewGui(Player player) {
        return !hasOpenGui(player);
    }

    /**
     * Открыть случайное уведомление в GUI (для тестирования)
     */
    public void showNotification(Player player, String message) {
        if (hasOpenGui(player)) {
            player.sendMessage("§e" + message);
        }
    }
}