package com.dentoper.playercollars;

import com.dentoper.playercollars.commands.CollarCommand;
import com.dentoper.playercollars.config.ConfigManager;
import com.dentoper.playercollars.data.PlayerCollarData;
import com.dentoper.playercollars.gui.CollarGUI;
import com.dentoper.playercollars.listeners.ArmorEquipListener;
import com.dentoper.playercollars.listeners.CollarItemListener;
import com.dentoper.playercollars.listeners.PlayerJoinListener;
import com.dentoper.playercollars.utils.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerCollarsPlugin extends JavaPlugin implements Listener {
    
    private static final String COLLAR_KEY = "playercollar";
    
    private ConfigManager configManager;
    private PlayerCollarData playerData;
    private Map<UUID, String> activeCollars;
    
    @Override
    public void onEnable() {
        // Initialize managers
        configManager = new ConfigManager(this);
        playerData = new PlayerCollarData(this);
        activeCollars = new HashMap<>();
        
        // Register command
        getCommand("collar").setExecutor(new CollarCommand(this));
        
        // Register events
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ArmorEquipListener(), this);
        getServer().getPluginManager().registerEvents(new CollarItemListener(), this);
        getServer().getPluginManager().registerEvents(this, this);
        
        // Load data for online players
        getServer().getOnlinePlayers().forEach(player -> {
            String collarName = playerData.getPlayerCollar(player.getUniqueId());
            if (collarName != null) {
                activeCollars.put(player.getUniqueId(), collarName);
                equipCollar(player, collarName, false);
            }
        });
        
        getLogger().info("PlayerCollars plugin enabled!");
    }
    
    @Override
    public void onDisable() {
        // Clean up all collars on disable
        activeCollars.keySet().forEach(uuid -> {
            Player player = getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                removeCollarVisual(player);
            }
        });
        
        getLogger().info("PlayerCollars plugin disabled!");
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // Remove from active collars but don't remove the visual - they'll see it when they rejoin
        activeCollars.remove(player.getUniqueId());
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public PlayerCollarData getPlayerData() {
        return playerData;
    }
    
    public Map<String, ConfigManager.CollarConfig> getAvailableCollars() {
        return configManager.getCollarConfigs();
    }
    
    public CollarGUI createCollarGUI(Player player) {
        return new CollarGUI(this, player);
    }
    
    public boolean equipCollar(Player player, String collarName) {
        return equipCollar(player, collarName, true);
    }
    
    private boolean equipCollar(Player player, String collarName, boolean sendMessage) {
        ConfigManager.CollarConfig config = getAvailableCollars().get(collarName);
        if (config == null) {
            if (sendMessage) {
                player.sendMessage(ColorUtil.colorize(configManager.getMessage("collar-not-found")));
            }
            return false;
        }
        
        // Remove existing collar
        if (activeCollars.containsKey(player.getUniqueId())) {
            removeCollarVisual(player);
        }
        
        // Add new collar
        activeCollars.put(player.getUniqueId(), collarName);
        applyCollarVisual(player, config);
        playerData.setPlayerCollar(player.getUniqueId(), collarName);
        
        if (sendMessage) {
            String message = configManager.getMessage("collar-equipped")
                    .replace("{collar_name}", config.getDisplayName());
            player.sendMessage(ColorUtil.colorize(message));
        }
        
        return true;
    }
    
    public boolean removeCollar(Player player) {
        if (!activeCollars.containsKey(player.getUniqueId())) {
            player.sendMessage(ColorUtil.colorize(configManager.getMessage("already-wearing")));
            return false;
        }
        
        removeCollarVisual(player);
        activeCollars.remove(player.getUniqueId());
        playerData.setPlayerCollar(player.getUniqueId(), null);
        
        player.sendMessage(ColorUtil.colorize(configManager.getMessage("collar-removed")));
        return true;
    }
    
    public String getActiveCollar(Player player) {
        return activeCollars.get(player.getUniqueId());
    }
    
    private void applyCollarVisual(Player player, ConfigManager.CollarConfig config) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }
                
                ItemStack helmet = player.getInventory().getHelmet();
                if (helmet != null && helmet.getType() != Material.AIR) {
                    ItemMeta meta = helmet.getItemMeta();
                    if (meta != null) {
                        PersistentDataContainer container = meta.getPersistentDataContainer();
                        container.set(getCollarKey(), PersistentDataType.STRING, "collar_" + config.getModelData());
                        helmet.setItemMeta(meta);
                    }
                }
            }
        }.runTaskTimer(this, 20L, 20L);
    }
    
    private void removeCollarVisual(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet != null && helmet.getType() != Material.AIR) {
            ItemMeta meta = helmet.getItemMeta();
            if (meta != null) {
                PersistentDataContainer container = meta.getPersistentDataContainer();
                container.remove(getCollarKey());
                helmet.setItemMeta(meta);
            }
        }
    }
    
    public org.bukkit.NamespacedKey getCollarKey() {
        return new org.bukkit.NamespacedKey(this, COLLAR_KEY);
    }
}