package com.example.mineskill.gui;

import com.example.mineskill.MineSkillPlugin;
import com.example.mineskill.models.Skill;
import com.example.mineskill.models.SkillBranch;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class GuiManager {
    private final MineSkillPlugin plugin;
    private final Map<Player, SkillTreeGui> openGuis;

    public GuiManager(MineSkillPlugin plugin) {
        this.plugin = plugin;
        this.openGuis = new HashMap<>();
    }

    public void openGui(Player player) {
        SkillTreeGui gui = new SkillTreeGui(plugin, player);
        Inventory inventory = gui.createInventory();
        player.openInventory(inventory);
        openGuis.put(player, gui);
    }

    public void closeGui(Player player) {
        openGuis.remove(player);
    }

    public SkillTreeGui getOpenGui(Player player) {
        return openGuis.get(player);
    }

    public boolean hasOpenGui(Player player) {
        return openGuis.containsKey(player);
    }

    public void updateGui(Player player) {
        SkillTreeGui gui = openGuis.get(player);
        if (gui == null) return;

        Inventory inventory = gui.createInventory();
        player.openInventory(inventory);
    }

    public void switchTab(Player player, SkillBranch branch) {
        SkillTreeGui gui = openGuis.get(player);
        if (gui == null) return;

        gui.setBranch(branch);
        updateGui(player);
    }

    public void handleSkillClick(Player player, int slot) {
        SkillTreeGui gui = openGuis.get(player);
        if (gui == null) return;

        int[] skillSlots = SkillTreeGui.getSkillDisplaySlots();
        boolean isSkillSlot = false;
        int skillIndex = -1;

        for (int i = 0; i < skillSlots.length; i++) {
            if (skillSlots[i] == slot) {
                isSkillSlot = true;
                skillIndex = i;
                break;
            }
        }

        if (!isSkillSlot) return;

        java.util.List<Skill> skills = plugin.getSkillManager()
            .getSkillsByBranch(gui.getCurrentBranch());
        
        if (skillIndex >= skills.size()) return;

        Skill skill = skills.get(skillIndex);
        var playerData = plugin.getPlayerDataManager().getPlayerData(player);

        if (!plugin.getSkillManager().canPurchase(playerData, skill)) {
            String reason;
            if (playerData.getSkillLevel(skill.getId()) >= skill.getMaxLevel()) {
                reason = plugin.getConfig().getString("gui.messages.max-level-reached", 
                    "Максимальный уровень!");
            } else if (playerData.getSkillPoints() < skill.getCost()) {
                reason = plugin.getConfig().getString("gui.messages.not-enough-points", 
                    "Недостаточно очков!");
            } else {
                String missing = plugin.getSkillManager().getMissingRequirement(playerData, skill);
                reason = plugin.getConfig().getString("gui.messages.requirement-not-met", 
                    "Не выполнено требование: " + missing).replace("%s", missing);
            }
            player.sendMessage(parseColor(reason));
            return;
        }

        playerData.setSkillPoints(playerData.getSkillPoints() - skill.getCost());
        playerData.increaseSkillLevel(skill.getId());
        
        plugin.getPlayerDataManager().savePlayerData(player);
        plugin.getSkillApplier().applySkills(player);

        String message = plugin.getConfig().getString("gui.messages.skill-purchased", 
            "Скилл %s успешно куплен! Уровень: %d/%d")
            .replace("%s", skill.getName())
            .replace("%d", String.valueOf(playerData.getSkillLevel(skill.getId())))
            .replace("%d", String.valueOf(skill.getMaxLevel()));
        
        player.sendMessage(parseColor(message));
        updateGui(player);
    }

    private String parseColor(String text) {
        return text.replace("&", "§");
    }
}
