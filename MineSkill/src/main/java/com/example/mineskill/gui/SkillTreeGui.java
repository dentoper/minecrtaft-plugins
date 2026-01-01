package com.example.mineskill.gui;

import com.example.mineskill.MineSkillPlugin;
import com.example.mineskill.models.PlayerSkillData;
import com.example.mineskill.models.Skill;
import com.example.mineskill.models.SkillBranch;
import com.example.mineskill.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SkillTreeGui {
    private final MineSkillPlugin plugin;
    private final Player player;
    private final PlayerSkillData playerData;
    private SkillBranch currentBranch;

    private static final int[] CORNER_SLOTS = {
        0, 8, 9, 17, 36, 44, 45, 53
    };

    private static final int[] INFO_PANEL_SLOTS = {
        1, 2, 3, 4, 5, 6, 7, 10, 11, 12, 13, 14, 15, 16
    };

    private static final int[] TAB_SLOTS = {18, 26, 36, 44};
    private static final int EXIT_SLOT = 45;
    private static final int POINTS_SLOT = 49;

    private static final int[] SKILL_DISPLAY_SLOTS = {
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };

    public SkillTreeGui(MineSkillPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.playerData = plugin.getPlayerDataManager().getPlayerData(player);
        this.currentBranch = SkillBranch.STRENGTH;
    }

    public Inventory createInventory() {
        String title = plugin.getConfig().getString("gui.title", "Дерево Скиллов");
        Inventory inventory = Bukkit.createInventory(player, 54, title);

        fillCorners(inventory);
        fillInfoPanel(inventory);
        fillTabs(inventory);
        fillExitButton(inventory);
        fillPointsDisplay(inventory);
        fillSkills(inventory);

        return inventory;
    }

    private void fillCorners(Inventory inventory) {
        ItemStack glass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
            .name(" ")
            .build();
        
        for (int slot : CORNER_SLOTS) {
            inventory.setItem(slot, glass);
        }
    }

    private void fillInfoPanel(Inventory inventory) {
        ItemStack info = new ItemBuilder(Material.BOOK)
            .name("§eИнформация о скиллах")
            .lore(
                "",
                "§7Ветка: §f" + currentBranch.getDisplayName(),
                "§7Скиллов в ветке: §f" + 
                    plugin.getSkillManager().getSkillsByBranch(currentBranch).size(),
                "",
                "§aЛКМ - улучшить скилл",
                "§7ПКМ - информация",
                "",
                "§eКликайте на вкладки для перехода"
            )
            .build();

        inventory.setItem(INFO_PANEL_SLOTS[0], info);

        ItemStack info2 = new ItemBuilder(Material.PAPER)
            .name("§7Статистика")
            .lore(
                "",
                "§7Всего очков получено: §e" + playerData.getTotalPoints(),
                "§7Очков потрачено: §c" + (playerData.getTotalPoints() - playerData.getSkillPoints()),
                "",
                "§7Скиллов куплено: §e" + playerData.getSkills().size()
            )
            .build();

        inventory.setItem(INFO_PANEL_SLOTS[1], info2);
    }

    private void fillTabs(Inventory inventory) {
        SkillBranch[] branches = SkillBranch.values();
        
        for (int i = 0; i < branches.length && i < TAB_SLOTS.length; i++) {
            SkillBranch branch = branches[i];
            int slot = TAB_SLOTS[i];
            
            boolean isActive = branch == currentBranch;
            String status = isActive ? "§a[Активно]" : "§7[Вкладка]";
            
            ItemStack tab = new ItemBuilder(branch.getIcon())
                .name(status + " " + branch.getColorCode() + branch.getDisplayName())
                .enchanted(isActive)
                .build();
            
            inventory.setItem(slot, tab);
        }
    }

    private void fillExitButton(Inventory inventory) {
        ItemStack exit = new ItemBuilder(Material.BARRIER)
            .name("§cЗакрыть")
            .lore("", "§7Нажмите, чтобы закрыть меню")
            .build();
        
        inventory.setItem(EXIT_SLOT, exit);
    }

    private void fillPointsDisplay(Inventory inventory) {
        ItemStack points = new ItemBuilder(Material.EXPERIENCE_BOTTLE)
            .name("§eДоступные очки")
            .lore(
                "",
                "§6" + playerData.getSkillPoints() + " §7очков",
                "",
                "§7Используйте очки для покупки скиллов"
            )
            .build();
        
        inventory.setItem(POINTS_SLOT, points);
    }

    private void fillSkills(Inventory inventory) {
        List<Skill> skills = plugin.getSkillManager().getSkillsByBranch(currentBranch);
        
        for (int i = 0; i < SKILL_DISPLAY_SLOTS.length && i < skills.size(); i++) {
            int slot = SKILL_DISPLAY_SLOTS[i];
            Skill skill = skills.get(i);
            int currentLevel = playerData.getSkillLevel(skill.getId());
            
            ItemStack skillIcon = skill.createIcon(currentLevel, playerData.getSkillPoints());
            inventory.setItem(slot, skillIcon);
        }
    }

    public void setBranch(SkillBranch branch) {
        this.currentBranch = branch;
    }

    public SkillBranch getCurrentBranch() {
        return currentBranch;
    }

    public static int[] getTabSlots() {
        return TAB_SLOTS.clone();
    }

    public static int[] getSkillDisplaySlots() {
        return SKILL_DISPLAY_SLOTS.clone();
    }

    public static int getExitSlot() {
        return EXIT_SLOT;
    }

    public static int getPointsSlot() {
        return POINTS_SLOT;
    }

    public static int[] getInfoPanelSlots() {
        return INFO_PANEL_SLOTS.clone();
    }

    public static int[] getCornerSlots() {
        return CORNER_SLOTS.clone();
    }
}
