package com.dentoper.smartharvest;

import com.dentoper.smartharvest.commands.SmartHarvestCommand;
import com.dentoper.smartharvest.config.ConfigManager;
import com.dentoper.smartharvest.gui.GuiManager;
import com.dentoper.smartharvest.listeners.BlockBreakListener;
import com.dentoper.smartharvest.listeners.InventoryClickListener;
import com.dentoper.smartharvest.listeners.PlayerJoinListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class SmartHarvestPlus extends JavaPlugin {

    private static SmartHarvestPlus instance;
    private ConfigManager configManager;
    private GuiManager guiManager;

    @Override
    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);
        this.guiManager = new GuiManager(this);

        SmartHarvestCommand command = new SmartHarvestCommand(this);
        if (getCommand("smartharvest") != null) {
            getCommand("smartharvest").setExecutor(command);
            getCommand("smartharvest").setTabCompleter(command);
        }
        if (getCommand("sh") != null) {
            getCommand("sh").setExecutor(command);
            getCommand("sh").setTabCompleter(command);
        }

        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        Logger logger = getLogger();
        logger.info("SmartHarvestPlus v" + getDescription().getVersion() + " has been enabled!");
        logger.info("Author: dentoper");
    }

    @Override
    public void onDisable() {
        if (configManager != null) {
            configManager.savePlayerData();
        }
        Logger logger = getLogger();
        logger.info("SmartHarvestPlus has been disabled!");
    }

    public static SmartHarvestPlus getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }
}
