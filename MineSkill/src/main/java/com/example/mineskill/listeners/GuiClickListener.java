package com.example.mineskill.listeners;

import com.example.mineskill.MineSkillPlugin;
import com.example.mineskill.gui.SkillTreeGui;
import com.example.mineskill.models.SkillBranch;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class GuiClickListener implements Listener {
    private final MineSkillPlugin plugin;

    public GuiClickListener(MineSkillPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        String expectedTitle = plugin.getConfig().getString("gui.title", "Дерево Скиллов");
        
        if (!title.equals(expectedTitle)) return;

        event.setCancelled(true);

        int slot = event.getSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) return;

        int[] tabSlots = SkillTreeGui.getTabSlots();
        for (int i = 0; i < tabSlots.length; i++) {
            if (tabSlots[i] == slot) {
                SkillBranch[] branches = SkillBranch.values();
                if (i < branches.length) {
                    plugin.getGuiManager().switchTab(player, branches[i]);
                }
                return;
            }
        }

        if (slot == SkillTreeGui.getExitSlot()) {
            player.closeInventory();
            return;
        }

        plugin.getGuiManager().handleSkillClick(player, slot);
    }
}
