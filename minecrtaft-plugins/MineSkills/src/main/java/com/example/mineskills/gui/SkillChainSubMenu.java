package com.example.mineskills.gui;

import com.example.mineskills.managers.SkillManager;
import com.example.mineskills.managers.PlayerDataManager;
import com.example.mineskills.models.Skill;
import com.example.mineskills.models.SkillChain;
import com.example.mineskills.models.PlayerSkillData;
import com.example.mineskills.utils.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Подменю для отображения цепочки навыков (5 уровней)
 * Показывает иконки и описания для каждого уровня скилла
 */
public class SkillChainSubMenu {
    private final SkillManager skillManager;
    private final PlayerDataManager playerDataManager;
    private final SkillApplier skillApplier;
    private final Player player;
    private final String skillId;
    private final Inventory inventory;

    public SkillChainSubMenu(SkillManager skillManager, PlayerDataManager playerDataManager,
                           SkillApplier skillApplier, Player player, String skillId) {
        this.skillManager = skillManager;
        this.playerDataManager = playerDataManager;
        this.skillApplier = skillApplier;
        this.player = player;
        this.skillId = skillId;
        
        // Создаем инвентарь 54 слота
        this.inventory = Bukkit.createInventory(null, 54, 
            Component.text("§5§lЦепочка Навыков"));
        
        initializeInventory();
    }

    /**
     * Инициализация инвентаря с цепочкой навыков
     */
    private void initializeInventory() {
        // Очищаем инвентарь
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, ItemBuilder.createEmptySlot());
        }

        // Добавляем информацию о скилле
        addSkillInfo();
        
        // Добавляем уровни цепочки
        addSkillLevels();
        
        // Добавляем кнопку назад
        addBackButton();
        
        // Добавляем информационную панель
        addInfoPanel();
    }

    /**
     * Добавление информации о скилле
     */
    private void addSkillInfo() {
        Skill skill = skillManager.getSkill(skillId);
        if (skill == null) return;
        
        PlayerSkillData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        int currentLevel = playerData != null ? playerData.getSkillLevel(skillId) : 0;
        int maxLevel = skill.getMaxLevel();
        
        // Заголовок скилла
        String displayName = "§6§l" + skill.getName();
        if (currentLevel > 0) {
            displayName += " §7(§e" + currentLevel + "§7/§e" + maxLevel + "§7)";
        }
        
        Component[] skillInfoLore = {
            Component.text("§7" + skill.getDescription()).color(NamedTextColor.GRAY),
            Component.empty(),
            Component.text("§7Ветка: §e" + skill.getBranch().getDisplayName()).color(NamedTextColor.YELLOW),
            Component.text("§7Максимальный уровень: §e" + maxLevel).color(NamedTextColor.YELLOW),
            Component.text("§7Текущий уровень: §e" + currentLevel).color(NamedTextColor.YELLOW),
            Component.empty(),
            Component.text("§7Каждый уровень открывает").color(NamedTextColor.GRAY),
            Component.text("§7новые возможности и улучшения").color(NamedTextColor.GRAY)
        };
        
        ItemStack skillInfoItem = ItemBuilder.createGuiButton(
            skill.getIcon(),
            displayName,
            skillInfoLore
        );
        inventory.setItem(4, skillInfoItem); // Центр верхнего ряда
    }

    /**
     * Добавление уровней цепочки
     */
    private void addSkillLevels() {
        SkillChain chain = skillManager.getSkillChain(skillId);
        if (chain == null) return;
        
        PlayerSkillData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        int currentLevel = playerData != null ? playerData.getSkillLevel(skillId) : 0;
        
        // Позиции для уровней (2-й ряд, от слота 10 до 16)
        int[] levelSlots = {10, 12, 14, 16, 18};
        
        for (int i = 0; i < chain.getLevels().size() && i < levelSlots.length; i++) {
            SkillChain.SkillChainLevel level = chain.getLevels().get(i);
            int levelNumber = level.getLevel();
            boolean isUnlocked = currentLevel >= levelNumber;
            boolean isCurrent = currentLevel == levelNumber - 1;
            
            ItemStack levelItem = createLevelItem(level, isUnlocked, isCurrent);
            inventory.setItem(levelSlots[i], levelItem);
        }
        
        // Добавляем бонус за полное прохождение (последний уровень)
        addChainBonus(chain, currentLevel);
    }

    /**
     * Создание предмета для уровня цепочки
     */
    private ItemStack createLevelItem(SkillChain.SkillChainLevel level, boolean isUnlocked, boolean isCurrent) {
        String levelTitle = "Уровень " + level.getLevel();
        Material material;
        
        if (isUnlocked) {
            material = level.isMaxLevel() ? Material.GOLD_BLOCK : Material.DIAMOND_BLOCK;
            levelTitle = "§a§l" + levelTitle;
        } else if (isCurrent) {
            material = Material.EMERALD_BLOCK;
            levelTitle = "§e§l" + levelTitle;
        } else {
            material = Material.RED_STAINED_GLASS_PANE;
            levelTitle = "§c§l" + levelTitle;
        }
        
        Component[] levelLore = {
            Component.text("§7" + level.getTitle()).color(isUnlocked ? NamedTextColor.GREEN : NamedTextColor.GRAY),
            Component.empty(),
            Component.text("§7Эффект:").color(NamedTextColor.GRAY),
            Component.text("§7" + level.getDescription()).color(NamedTextColor.GRAY),
            Component.empty(),
        };
        
        if (isUnlocked) {
            levelLore = java.util.Arrays.copyOf(levelLore, levelLore.length + 1);
            levelLore[levelLore.length - 1] = Component.text("§a§l✓ РАЗБЛОКИРОВАН").color(NamedTextColor.GREEN);
        } else if (isCurrent) {
            levelLore = java.util.Arrays.copyOf(levelLore, levelLore.length + 1);
            levelLore[levelLore.length - 1] = Component.text("§e§l→ СЛЕДУЮЩИЙ").color(NamedTextColor.YELLOW);
        } else {
            levelLore = java.util.Arrays.copyOf(levelLore, levelLore.length + 1);
            levelLore[levelLore.length - 1] = Component.text("§c§l✗ ЗАБЛОКИРОВАН").color(NamedTextColor.RED);
        }
        
        return ItemBuilder.createGuiButton(material, levelTitle, levelLore);
    }

    /**
     * Добавление бонуса за полное прохождение цепочки
     */
    private void addChainBonus(SkillChain chain, int currentLevel) {
        if (currentLevel < chain.getMaxLevel()) {
            // Цепочка не завершена
            int remaining = chain.getMaxLevel() - currentLevel;
            
            Component[] bonusLore = {
                Component.text("§7Завершите все уровни").color(NamedTextColor.GRAY),
                Component.text("§7цепочки для получения").color(NamedTextColor.GRAY),
                Component.text("§7специального бонуса!").color(NamedTextColor.GRAY),
                Component.empty(),
                Component.text("§7Осталось уровней: §e" + remaining).color(NamedTextColor.YELLOW)
            };
            
            ItemStack lockedBonusItem = ItemBuilder.createGuiButton(
                Material.REDSTONE_BLOCK,
                "§c§lЗаблокированный Бонус",
                bonusLore
            );
            inventory.setItem(31, lockedBonusItem); // Центр нижней части
            
        } else {
            // Цепочка завершена, показываем бонус
            String bonusName = "§a§l" + chain.getTitle() + " - МАСТЕР";
            Component[] bonusLore = {
                Component.text("§a§lПОЗДРАВЛЯЕМ!").color(NamedTextColor.GREEN),
                Component.text("§7Вы стали мастером этого").color(NamedTextColor.GRAY),
                Component.text("§7навыка и получили").color(NamedTextColor.GRAY),
                Component.text("§7специальный бонус!").color(NamedTextColor.GRAY),
                Component.empty(),
                Component.text("§7Максимальные возможности").color(NamedTextColor.GRAY),
                Component.text("§7скилла активны").color(NamedTextColor.GRAY),
                Component.empty(),
                Component.text("§a§l✓ ЦЕПОЧКА ЗАВЕРШЕНА").color(NamedTextColor.GREEN)
            };
            
            ItemStack bonusItem = ItemBuilder.createGuiButton(
                Material.NETHER_STAR,
                bonusName,
                bonusLore
            );
            inventory.setItem(31, bonusItem); // Центр нижней части
        }
    }

    /**
     * Добавление кнопки назад
     */
    private void addBackButton() {
        Component[] backLore = {
            Component.text("§7Вернуться к").color(NamedTextColor.GRAY),
            Component.text("§7дереву скиллов").color(NamedTextColor.GRAY),
            Component.empty(),
            Component.text("§cВнимание: Изменения").color(NamedTextColor.RED),
            Component.text("§cуже сохранены!").color(NamedTextColor.RED)
        };
        
        ItemStack backItem = ItemBuilder.createGuiButton(
            Material.ARROW,
            "§4§lНазад к Скиллам",
            backLore
        );
        inventory.setItem(49, backItem); // Центр нижнего ряда
    }

    /**
     * Добавление информационной панели
     */
    private void addInfoPanel() {
        PlayerSkillData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        if (playerData == null) return;
        
        int skillPoints = playerData.getSkillPoints();
        
        Component[] infoLore = {
            Component.text("§7Информация о цепочке:").color(NamedTextColor.GRAY),
            Component.empty(),
            Component.text("§7• 5 уникальных уровней").color(NamedTextColor.GRAY),
            Component.text("§7• Каждый уровень имеет").color(NamedTextColor.GRAY),
            Component.text("§7  специальную иконку").color(NamedTextColor.GRAY),
            Component.text("§7• Прогрессивные улучшения").color(NamedTextColor.GRAY),
            Component.text("§7• Специальный бонус").color(NamedTextColor.GRAY),
            Component.text("§7  за завершение").color(NamedTextColor.GRAY),
            Component.empty(),
            Component.text("§7Ваши очки: §e" + skillPoints).color(NamedTextColor.YELLOW)
        };
        
        ItemStack infoItem = ItemBuilder.createGuiButton(
            Material.BOOKSHELF,
            "§6§lИнформация",
            infoLore
        );
        inventory.setItem(53, infoItem); // Правый нижний угол
    }

    /**
     * Обработка клика в подменю
     */
    public void handleClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        
        event.setCancelled(true); // Запрещаем перемещение предметов
        
        int slot = event.getSlot();
        Player clickedPlayer = (Player) event.getWhoClicked();
        
        // Кнопка назад
        if (slot == 49) {
            openSkillTree();
            return;
        }
        
        // Остальные слоты только для отображения
        if (slot >= 0 && slot < 54) {
            // Просто играем звук при клике
            clickedPlayer.playSound(clickedPlayer.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f);
        }
    }

    /**
     * Открыть дерево скиллов
     */
    private void openSkillTree() {
        SkillTreeGui skillTree = new SkillTreeGui(skillManager, playerDataManager, skillApplier, player);
        skillTree.open();
    }

    /**
     * Открыть подменю для игрока
     */
    public void open() {
        player.openInventory(inventory);
    }

    /**
     * Получить инвентарь
     */
    public Inventory getInventory() {
        return inventory;
    }
}