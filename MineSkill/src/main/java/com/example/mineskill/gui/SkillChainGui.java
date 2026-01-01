package com.example.mineskill.gui;

import com.example.mineskill.MineSkillPlugin;
import com.example.mineskill.models.*;
import com.example.mineskill.utils.ColorUtil;
import com.example.mineskill.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SkillChainGui {
    private final MineSkillPlugin plugin;
    private final Player player;
    private final Skill skill;
    private final PlayerSkillData playerData;
    private static final int BACK_SLOT = 45;

    public SkillChainGui(MineSkillPlugin plugin, Player player, Skill skill) {
        this.plugin = plugin;
        this.player = player;
        this.skill = skill;
        this.playerData = plugin.getPlayerDataManager().getPlayerData(player);
    }

    public Inventory createInventory() {
        String title = ColorUtil.color("&eЦепочка: " + skill.getName());
        Inventory inventory = Bukkit.createInventory(null, 54, title);

        fillBackground(inventory);
        fillChainBonuses(inventory);
        fillBackButton(inventory);
        fillInfo(inventory);

        return inventory;
    }

    private void fillBackground(Inventory inventory) {
        ItemStack glass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
            .name(" ")
            .build();

        for (int i = 0; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, glass);
            }
        }
    }

    private void fillChainBonuses(Inventory inventory) {
        SkillChain chain = skill.getSkillChain();
        int currentLevel = playerData.getSkillLevel(skill.getId());
        List<SkillChainBonus> bonuses = chain.getAllBonuses();

        int[] slots = {10, 12, 14, 16, 28, 30, 32, 34};

        for (int i = 0; i < bonuses.size() && i < slots.length; i++) {
            SkillChainBonus bonus = bonuses.get(i);
            int level = bonus.getLevel();
            boolean unlocked = level <= currentLevel;
            boolean isNext = level == currentLevel + 1;

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ColorUtil.color("§7Уровень: §f" + level));
            lore.add("");
            lore.add(ColorUtil.color("§7" + bonus.getDescription()));
            lore.add("");

            if (unlocked) {
                lore.add(ColorUtil.color("§a✔ Разблокировано!"));
            } else if (isNext) {
                lore.add(ColorUtil.color("§eСледующий уровень"));
                lore.add(ColorUtil.color("§7Стоимость: §6" + skill.getCost() + " очков"));
            } else {
                lore.add(ColorUtil.color("§7Заблокировано"));
            }

            Material icon = bonus.getIcon();
            ItemBuilder builder = new ItemBuilder(icon)
                .name((unlocked ? "&a" : (isNext ? "&e" : "&7")) + "Уровень " + level)
                .lore(lore);

            if (unlocked) {
                builder.enchanted(true);
            }

            inventory.setItem(slots[i], builder.build());
        }
    }

    private void fillBackButton(Inventory inventory) {
        ItemStack back = new ItemBuilder(Material.ARROW)
            .name("&cВернуться назад")
            .lore("", "&7Нажмите, чтобы вернуться")
            .build();

        inventory.setItem(BACK_SLOT, back);
    }

    private void fillInfo(Inventory inventory) {
        int currentLevel = playerData.getSkillLevel(skill.getId());
        SkillChain chain = skill.getSkillChain();

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ColorUtil.color("§7Текущий уровень: §e" + currentLevel + "§7/" + skill.getMaxLevel()));
        lore.add(ColorUtil.color("§7Доступно очков: §6" + playerData.getSkillPoints()));
        lore.add("");
        lore.add(ColorUtil.color("§eЦепочка бонусов:"));

        for (SkillChainBonus bonus : chain.getAllBonuses()) {
            String prefix = bonus.getLevel() <= currentLevel ? "§a✔" : "§7○";
            lore.add(ColorUtil.color(prefix + " §7Ур." + bonus.getLevel() + ": §f" + bonus.getDescription()));
        }

        ItemStack info = new ItemBuilder(skill.getIcon())
            .name(skill.getBranch().getColorCode() + skill.getName())
            .lore(lore)
            .build();

        inventory.setItem(4, info);
    }

    public static int getBackSlot() {
        return BACK_SLOT;
    }
}
