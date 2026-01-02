package com.example.mineskills.gui;

import com.example.mineskills.managers.SkillManager;
import com.example.mineskills.managers.PlayerDataManager;
import com.example.mineskills.managers.SkillApplier;
import com.example.mineskills.models.Skill;
import com.example.mineskills.models.SkillBranch;
import com.example.mineskills.models.PlayerSkillData;
import com.example.mineskills.utils.ItemBuilder;
import com.example.mineskills.utils.ProgressBar;
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

import java.util.HashMap;
import java.util.Map;

/**
 * Главное GUI дерева скиллов с 54 слотами
 * Использует Component API для отображения цветных текстов
 */
public class SkillTreeGui {
    private final SkillManager skillManager;
    private final PlayerDataManager playerDataManager;
    private final SkillApplier skillApplier;
    private final Player player;
    private final Inventory inventory;
    private SkillBranch currentBranch;

    public SkillTreeGui(SkillManager skillManager, PlayerDataManager playerDataManager,
                       SkillApplier skillApplier, Player player) {
        this.skillManager = skillManager;
        this.playerDataManager = playerDataManager;
        this.skillApplier = skillApplier;
        this.player = player;
        this.currentBranch = SkillBranch.STRENGTH; // По умолчанию показываем Strength
        
        // Создаем инвентарь 54 слота (6 рядов x 9 слотов)
        this.inventory = Bukkit.createInventory(null, 54, 
            Component.text("§6§lДерево Скиллов - " + currentBranch.getDisplayName()));
        
        initializeInventory();
    }

    /**
     * Инициализация инвентаря с скиллами
     */
    private void initializeInventory() {
        // Очищаем инвентарь
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, ItemBuilder.createEmptySlot());
        }

        // Заполняем кнопки веток в верхнем ряду
        fillBranchButtons();
        
        // Заполняем скиллы выбранной ветки
        fillSkillTree();
        
        // Добавляем информационные панели
        fillInfoPanels();
        
        // Добавляем кнопку выхода
        addExitButton();
    }

    /**
     * Заполнение кнопок веток в верхнем ряду
     */
    private void fillBranchButtons() {
        // Позиции кнопок веток: 0, 9, 18, 27, 36
        int[] branchSlots = {0, 9, 18, 27, 36};
        SkillBranch[] branches = SkillBranch.values();
        
        for (int i = 0; i < branches.length && i < branchSlots.length; i++) {
            SkillBranch branch = branches[i];
            Material icon = getBranchIcon(branch);
            String displayName = branch.getColoredName() + " §l" + branch.getDisplayName();
            
            Component[] lore = {
                Component.text("§7Нажмите для просмотра").color(NamedTextColor.GRAY),
                Component.text("§7скиллов ветки").color(NamedTextColor.GRAY),
                Component.empty(),
                Component.text("§aДоступно скиллов: §e" + 
                    skillManager.getSkillsByBranch(branch).size()).color(NamedTextColor.GREEN)
            };
            
            ItemStack item = ItemBuilder.createGuiButton(icon, displayName, lore);
            inventory.setItem(branchSlots[i], item);
        }
    }

    /**
     * Заполнение скиллами выбранной ветки
     */
    private void fillSkillTree() {
        if (currentBranch == null) return;
        
        var skills = skillManager.getSkillsByBranch(currentBranch);
        int slotIndex = 0;
        
        // Размещаем скиллы в сетке 3x2 (максимум 6 скиллов на ветку)
        int[] slots = {2, 3, 4, 11, 12, 13}; // Центральные позиции для скиллов
        
        for (int i = 0; i < skills.size() && i < slots.length; i++) {
            Skill skill = skills.get(i);
            ItemStack skillItem = createSkillItem(skill);
            inventory.setItem(slots[i], skillItem);
            slotIndex++;
        }
    }

    /**
     * Создание предмета для скилла
     */
    private ItemStack createSkillItem(Skill skill) {
        PlayerSkillData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        if (playerData == null) return ItemBuilder.createEmptySlot();
        
        int currentLevel = playerData.getSkillLevel(skill.getId());
        int maxLevel = skill.getMaxLevel();
        boolean hasRequirements = skillManager.hasRequirements(skill, playerData);
        boolean canLevelUp = skill.canLevelUp(currentLevel);
        int cost = skillManager.getNextLevelCost(skill, currentLevel);
        int playerPoints = playerData.getSkillPoints();
        
        // Определяем цвет и материал
        Material material = currentLevel >= maxLevel ? Material.LIME_STAINED_GLASS_PANE :
                          canLevelUp && hasRequirements && playerPoints >= cost ? 
                          Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        
        // Создаем имя скилла
        String displayName = currentBranch.getMinecraftColor() + "§l" + skill.getName();
        if (currentLevel > 0) {
            displayName += " §7(§e" + currentLevel + "§7/§e" + maxLevel + "§7)";
        }
        
        // Создаем описание
        java.util.List<Component> loreList = new java.util.ArrayList<>();
        
        // Описание скилла
        loreList.add(Component.text("§7" + skill.getDescription()).color(NamedTextColor.GRAY));
        loreList.add(Component.text(" "));
        
        // Текущие уровни и их эффекты
        if (currentLevel > 0) {
            loreList.add(Component.text("§aТекущий эффект:").color(NamedTextColor.GREEN));
            for (int i = 0; i < currentLevel && i < skill.getLevelDescriptions().size(); i++) {
                String effect = skill.getLevelDescriptions().get(i);
                loreList.add(Component.text("§7  • §a" + effect).color(NamedTextColor.GRAY));
            }
            loreList.add(Component.text(" "));
        }
        
        // Следующий уровень
        if (canLevelUp) {
            if (currentLevel < skill.getLevelDescriptions().size()) {
                String nextEffect = skill.getLevelDescriptions().get(currentLevel);
                loreList.add(Component.text("§eСледующий уровень:").color(NamedTextColor.YELLOW));
                loreList.add(Component.text("§7  • §e" + nextEffect).color(NamedTextColor.GRAY));
                loreList.add(Component.text(" "));
            }
            
            // Стоимость
            if (hasRequirements) {
                if (playerPoints >= cost) {
                    loreList.add(Component.text("§aСтоимость: §e" + cost + " §aочков").color(NamedTextColor.GREEN));
                } else {
                    loreList.add(Component.text("§cНедостаточно очков: §e" + cost).color(NamedTextColor.RED));
                }
            } else {
                loreList.add(Component.text("§cТребования не выполнены").color(NamedTextColor.RED));
            }
        } else {
            loreList.add(Component.text("§6§lМаксимальный уровень достигнут").color(NamedTextColor.GOLD));
        }
        
        // Требования
        if (skill.getRequiredSkill() != null && !hasRequirements) {
            loreList.add(Component.text(" "));
            loreList.add(Component.text("§cТребуется: " + skill.getRequiredSkill() + 
                " §eуровень " + skill.getRequiredLevel()).color(NamedTextColor.RED));
        }
        
        // Действие
        loreList.add(Component.text(" "));
        if (currentLevel >= maxLevel) {
            loreList.add(Component.text("§6ЛКМ: Просмотр цепочки").color(NamedTextColor.GOLD));
        } else if (canLevelUp && hasRequirements && playerPoints >= cost) {
            loreList.add(Component.text("§aЛКМ: Улучшить скилл").color(NamedTextColor.GREEN));
        } else if (!hasRequirements) {
            loreList.add(Component.text("§cТребования не выполнены").color(NamedTextColor.RED));
        } else if (playerPoints < cost) {
            loreList.add(Component.text("§cНедостаточно очков").color(NamedTextColor.RED));
        } else {
            loreList.add(Component.text("§cНедоступно").color(NamedTextColor.RED));
        }
        
        return ItemBuilder.createGuiButton(material, displayName, loreList.toArray(new Component[0]));
    }

    /**
     * Заполнение информационных панелей
     */
    private void fillInfoPanels() {
        PlayerSkillData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        if (playerData == null) return;
        
        // Панель очков скиллов (правый верхний угол)
        int skillPoints = playerData.getSkillPoints();
        int totalPoints = playerData.getTotalPointsEarned();
        
        Component[] pointsLore = {
            Component.text("§7Ваши очки скиллов:").color(NamedTextColor.GRAY),
            Component.text("§eТекущие: §a" + skillPoints).color(NamedTextColor.YELLOW),
            Component.text("§7Всего заработано: §b" + totalPoints).color(NamedTextColor.AQUA),
            Component.empty(),
            Component.text("§7Очки даются за:").color(NamedTextColor.GRAY),
            Component.text("§7• Добычу блоков").color(NamedTextColor.GRAY),
            Component.text("§7• Бой и урон").color(NamedTextColor.GRAY),
            Component.text("§7• Движение").color(NamedTextColor.GRAY),
            Component.text("§7• Прыжки").color(NamedTextColor.GRAY)
        };
        
        ItemStack pointsItem = ItemBuilder.createGuiButton(
            Material.EXPERIENCE_BOTTLE,
            "§6§lОчки Скиллов",
            pointsLore
        );
        inventory.setItem(8, pointsItem);
        
        // Панель прогресса действий (нижний ряд)
        var progress = playerData.getActionProgress();
        Component[] progressLore = {
            Component.text("§7Прогресс к следующим очкам:").color(NamedTextColor.GRAY),
            Component.empty(),
            Component.text("§8Добыча:").color(NamedTextColor.GRAY),
            ProgressBar.createBar(progress.getMiningProgress(), 100, 15, 
                NamedTextColor.GREEN, NamedTextColor.DARK_GRAY),
            Component.text("§8Бой:").color(NamedTextColor.GRAY),
            ProgressBar.createBar(progress.getCombatProgress(), 50, 15, 
                NamedTextColor.RED, NamedTextColor.DARK_GRAY),
            Component.text("§8Движение:").color(NamedTextColor.GRAY),
            ProgressBar.createBar(progress.getMovementProgress(), 1000, 15, 
                NamedTextColor.BLUE, NamedTextColor.DARK_GRAY),
            Component.text("§8Прыжки:").color(NamedTextColor.GRAY),
            ProgressBar.createBar(progress.getJumpingProgress(), 50, 15, 
                NamedTextColor.YELLOW, NamedTextColor.DARK_GRAY)
        };
        
        ItemStack progressItem = ItemBuilder.createGuiButton(
            Material.BOOK,
            "§5§lПрогресс Действий",
            progressLore
        );
        inventory.setItem(53, progressItem);
    }

    /**
     * Добавление кнопки выхода
     */
    private void addExitButton() {
        Component[] exitLore = {
            Component.text("§7Закрыть меню скиллов").color(NamedTextColor.GRAY),
            Component.empty(),
            Component.text("§cВнимание: Изменения сохранятся!").color(NamedTextColor.RED)
        };
        
        ItemStack exitItem = ItemBuilder.createGuiButton(
            Material.BARRIER,
            "§4§lВыход",
            exitLore
        );
        inventory.setItem(49, exitItem); // Центр нижнего ряда
    }

    /**
     * Получить иконку ветки
     */
    private Material getBranchIcon(SkillBranch branch) {
        switch (branch) {
            case STRENGTH: return Material.DIAMOND_SWORD;
            case AGILITY: return Material.FEATHER;
            case ENDURANCE: return Material.SHIELD;
            case MINING: return Material.DIAMOND_PICKAXE;
            case WISDOM: return Material.AMETHYST_SHARD;
            default: return Material.STONE;
        }
    }

    /**
     * Обработка клика в GUI
     */
    public void handleClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        
        event.setCancelled(true); // Запрещаем перемещение предметов
        
        int slot = event.getSlot();
        Player clickedPlayer = (Player) event.getWhoClicked();
        
        // Кнопки веток (верхний ряд)
        if (isBranchButton(slot)) {
            handleBranchClick(slot);
            return;
        }
        
        // Кнопка выхода
        if (slot == 49) {
            clickedPlayer.closeInventory();
            return;
        }
        
        // Скиллы (центральная область)
        if (isSkillSlot(slot)) {
            handleSkillClick(slot, event.isLeftClick());
            return;
        }
    }

    /**
     * Проверка, является ли слот кнопкой ветки
     */
    private boolean isBranchButton(int slot) {
        return slot == 0 || slot == 9 || slot == 18 || slot == 27 || slot == 36;
    }

    /**
     * Проверка, является ли слот скиллом
     */
    private boolean isSkillSlot(int slot) {
        return slot >= 2 && slot <= 4 || slot >= 11 && slot <= 13;
    }

    /**
     * Обработка клика по кнопке ветки
     */
    private void handleBranchClick(int slot) {
        SkillBranch newBranch = getBranchBySlot(slot);
        if (newBranch != null && newBranch != currentBranch) {
            currentBranch = newBranch;
            // Обновляем заголовок
            inventory.title(Component.text("§6§lДерево Скиллов - " + currentBranch.getDisplayName()));
            // Перезаполняем скиллы
            initializeInventory();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        }
    }

    /**
     * Получить ветку по слоту
     */
    private SkillBranch getBranchBySlot(int slot) {
        int[] branchSlots = {0, 9, 18, 27, 36};
        for (int i = 0; i < branchSlots.length; i++) {
            if (slot == branchSlots[i]) {
                SkillBranch[] branches = SkillBranch.values();
                return i < branches.length ? branches[i] : null;
            }
        }
        return null;
    }

    /**
     * Обработка клика по скиллу
     */
    private void handleSkillClick(int slot, boolean isLeftClick) {
        // Получаем скилл для этого слота
        var skills = skillManager.getSkillsByBranch(currentBranch);
        int skillIndex = getSkillIndexBySlot(slot);
        
        if (skillIndex < 0 || skillIndex >= skills.size()) return;
        
        Skill skill = skills.get(skillIndex);
        PlayerSkillData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        
        if (playerData == null) return;
        
        int currentLevel = playerData.getSkillLevel(skill.getId());
        boolean hasRequirements = skillManager.hasRequirements(skill, playerData);
        boolean canLevelUp = skill.canLevelUp(currentLevel);
        int cost = skillManager.getNextLevelCost(skill, currentLevel);
        int playerPoints = playerData.getSkillPoints();
        
        if (isLeftClick) {
            // ЛКМ: улучшить скилл или открыть цепочку
            if (currentLevel >= skill.getMaxLevel()) {
                // Открываем цепочку скиллов
                openSkillChain(skill.getId());
            } else if (canLevelUp && hasRequirements && playerPoints >= cost) {
                // Покупаем улучшение
                purchaseSkillUpgrade(skill, cost);
            } else {
                // Недостаточно очков или требований
                String message = !hasRequirements ? "§cТребования не выполнены!" :
                               playerPoints < cost ? "§cНедостаточно очков! (нужно " + cost + ")" :
                               "§cСкилл недоступен!";
                player.sendMessage(message);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 0.5f);
            }
        }
        // ПКМ можно использовать для других действий в будущем
    }

    /**
     * Получить индекс скилла по слоту
     */
    private int getSkillIndexBySlot(int slot) {
        int[] skillSlots = {2, 3, 4, 11, 12, 13};
        for (int i = 0; i < skillSlots.length; i++) {
            if (slot == skillSlots[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Покупка улучшения скилла
     */
    private void purchaseSkillUpgrade(Skill skill, int cost) {
        PlayerSkillData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        if (playerData == null) return;
        
        int currentLevel = playerData.getSkillLevel(skill.getId());
        
        if (playerData.spendPoints(cost)) {
            playerData.setSkillLevel(skill.getId(), currentLevel + 1);
            playerDataManager.savePlayerData(player.getUniqueId());
            
            // Применяем скилл к игроку
            skillApplier.updateSkill(player, skill.getId(), currentLevel + 1);
            
            // Обновляем GUI
            initializeInventory();
            
            // Сообщения и звуки
            player.sendMessage("§aСкилл " + skill.getName() + " улучшен до уровня " + (currentLevel + 1) + "!");
            player.sendMessage("§7Потрачено: §e" + cost + " §7очков");
            player.sendMessage("§7Осталось: §e" + playerData.getSkillPoints() + " §7очков");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }

    /**
     * Открытие цепочки скиллов
     */
    private void openSkillChain(String skillId) {
        SkillChainSubMenu chainMenu = new SkillChainSubMenu(
            skillManager, playerDataManager, skillApplier, player, skillId
        );
        chainMenu.open();
    }

    /**
     * Открыть GUI для игрока
     */
    public void open() {
        player.openInventory(inventory);
    }

    /**
     * Обновить отображение GUI
     */
    public void update() {
        initializeInventory();
    }

    /**
     * Получить инвентарь
     */
    public Inventory getInventory() {
        return inventory;
    }
}