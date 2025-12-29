package com.tradesystem;

import com.tradesystem.command.TradeCommand;
import com.tradesystem.config.TradeConfig;
import com.tradesystem.listener.InventoryListener;
import com.tradesystem.trade.TradeSessionManager;
import org.bukkit.plugin.java.JavaPlugin;

public class TradeSystemPlugin extends JavaPlugin {

    private static TradeSystemPlugin instance;
    private TradeConfig tradeConfig;

    @Override
    public void onEnable() {
        instance = this;
        
        this.tradeConfig = new TradeConfig(this);
        
        TradeSessionManager.initialize(this);
        
        getCommand("trade").setExecutor(new TradeCommand(this));
        
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        
        getLogger().info("TradeSystem v2.0.0 enabled!");
    }

    @Override
    public void onDisable() {
        // Проверяем был ли TradeSessionManager инициализирован
        try {
            TradeSessionManager manager = TradeSessionManager.getInstance();
            if (manager != null) {
                manager.cleanupAll();
            }
        } catch (IllegalStateException e) {
            getLogger().warning("TradeSessionManager не был инициализирован, пропуск очистки");
        }
        
        getLogger().info("TradeSystem v2.0.0 disabled!");
    }

    public static TradeSystemPlugin getInstance() {
        return instance;
    }

    public TradeConfig getTradeConfig() {
        return tradeConfig;
    }
}
