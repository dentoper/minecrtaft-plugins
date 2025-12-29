package com.tradesystem;

import com.tradesystem.command.TradeCommand;
import com.tradesystem.listener.InventoryListener;
import com.tradesystem.trade.TradeSessionManager;
import org.bukkit.plugin.java.JavaPlugin;

public class TradeSystemPlugin extends JavaPlugin {

    private static TradeSystemPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize TradeSessionManager
        TradeSessionManager.initialize(this);
        
        // Register commands
        getCommand("trade").setExecutor(new TradeCommand(this));
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        
        getLogger().info("TradeSystem plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("TradeSystem plugin disabled!");
    }

    public static TradeSystemPlugin getInstance() {
        return instance;
    }
}