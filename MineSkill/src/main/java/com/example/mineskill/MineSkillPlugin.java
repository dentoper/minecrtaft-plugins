package com.example.mineskill;

import com.example.mineskill.commands.SkillTreeCommand;
import com.example.mineskill.gui.GuiManager;
import com.example.mineskill.listeners.DamageListener;
import com.example.mineskill.listeners.GuiClickListener;
import com.example.mineskill.listeners.PlayerJoinListener;
import com.example.mineskill.listeners.PlayerQuitListener;
import com.example.mineskill.managers.PlayerDataManager;
import com.example.mineskill.managers.SkillApplier;
import com.example.mineskill.managers.SkillManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MineSkillPlugin extends JavaPlugin {
    private SkillManager skillManager;
    private PlayerDataManager playerDataManager;
    private SkillApplier skillApplier;
    private GuiManager guiManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        skillManager = new SkillManager(this);
        playerDataManager = new PlayerDataManager(this);
        skillApplier = new SkillApplier(this);
        guiManager = new GuiManager(this);

        registerListeners();
        registerCommands();

        getLogger().info("MineSkill успешно загружен!");
        getLogger().info("Загружено скиллов: " + skillManager.getAllSkills().size());
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) {
            playerDataManager.saveAllPlayers();
        }

        getLogger().info("MineSkill успешно отключен!");
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new GuiClickListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DamageListener(this), this);
    }

    private void registerCommands() {
        getCommand("skilltree").setExecutor(new SkillTreeCommand(this));
        getCommand("skilltree").setTabCompleter(new SkillTreeCommand(this));
    }

    public SkillManager getSkillManager() {
        return skillManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public SkillApplier getSkillApplier() {
        return skillApplier;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }
}
