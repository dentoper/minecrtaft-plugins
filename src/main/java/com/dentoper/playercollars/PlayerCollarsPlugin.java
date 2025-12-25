package com.dentoper.playercollars;

import com.dentoper.playercollars.commands.CollarCommand;
import com.dentoper.playercollars.config.ConfigManager;
import com.dentoper.playercollars.data.PlayerCollarData;
import com.dentoper.playercollars.listeners.ArmorEquipListener;
import com.dentoper.playercollars.listeners.PlayerJoinListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class PlayerCollarsPlugin extends JavaPlugin {
    private static PlayerCollarsPlugin instance;
    private ConfigManager configManager;
    private PlayerCollarData playerCollarData;

    @Override
    public void onEnable() {
        instance = this;
        
        this.configManager = new ConfigManager(this);
        this.playerCollarData = new PlayerCollarData(this);

        getCommand("collar").setExecutor(new CollarCommand(this));
        
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ArmorEquipListener(this), this);

        Logger logger = getLogger();
        logger.info("PlayerCollars has been enabled!");
    }

    @Override
    public void onDisable() {
        if (playerCollarData != null) {
            playerCollarData.save();
        }
        Logger logger = getLogger();
        logger.info("PlayerCollars has been disabled!");
    }

    public static PlayerCollarsPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public PlayerCollarData getPlayerCollarData() {
        return playerCollarData;
    }
}
