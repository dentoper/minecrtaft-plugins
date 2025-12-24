package com.dentoper.playercollars.commands;

import com.dentoper.playercollars.PlayerCollarsPlugin;
import com.dentoper.playercollars.config.ConfigManager;
import com.dentoper.playercollars.gui.CollarGUI;
import com.dentoper.playercollars.utils.ColorUtil;
import com.dentoper.playercollars.utils.PermissionUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Map;

public class CollarCommand implements CommandExecutor {
    private final PlayerCollarsPlugin plugin;
    
    public CollarCommand(PlayerCollarsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!PermissionUtil.hasUsePermission(player)) {
            player.sendMessage(ColorUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "help":
                sendHelp(player);
                break;
            case "list":
                handleList(player);
                break;
            case "wear":
                if (args.length < 2) {
                    player.sendMessage(ColorUtil.colorize("&cUsage: /collar wear <collar_name>"));
                    return true;
                }
                handleWear(player, args[1]);
                break;
            case "remove":
                handleRemove(player);
                break;
            case "gui":
                handleGUI(player);
                break;
            case "info":
                handleInfo(player);
                break;
            case "admin":
                if (args.length < 2) {
                    player.sendMessage(ColorUtil.colorize("&cUsage: /collar admin reload"));
                    return true;
                }
                handleAdmin(player, args[1]);
                break;
            default:
                player.sendMessage(ColorUtil.colorize("&cUnknown command. Use /collar help for help."));
                break;
        }
        
        return true;
    }
    
    private void sendHelp(Player player) {
        player.sendMessage(ColorUtil.colorize(plugin.getConfigManager().getMessage("help-message")));
    }
    
    private void handleList(Player player) {
        player.sendMessage(ColorUtil.colorize("&6=== Available Collars ==="));
        
        Map<String, ConfigManager.CollarConfig> collars = plugin.getAvailableCollars();
        if (collars.isEmpty()) {
            player.sendMessage(ColorUtil.colorize("&cNo collars configured!"));
            return;
        }
        
        String currentCollar = plugin.getActiveCollar(player);
        
        for (Map.Entry<String, ConfigManager.CollarConfig> entry : collars.entrySet()) {
            String collarName = entry.getKey();
            ConfigManager.CollarConfig config = entry.getValue();
            
            boolean hasPermission = PermissionUtil.hasCollarPermission(player, collarName);
            boolean isCurrent = collarName.equals(currentCollar);
            
            String prefix = isCurrent ? "&a» " : (hasPermission ? "&7" : "&c");
            String suffix = isCurrent ? " &7(current)" : (hasPermission ? "" : " &7(no permission)");
            
            String message = prefix + config.getDisplayName() + suffix;
            if (!config.getDescription().isEmpty()) {
                message += ColorUtil.colorize("\n&8&o") + config.getDescription();
            }
            
            player.sendMessage(ColorUtil.colorize(message));
        }
    }
    
    private void handleWear(Player player, String collarName) {
        if (!PermissionUtil.hasWearPermission(player)) {
            player.sendMessage(ColorUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }
        
        if (!PermissionUtil.hasCollarPermission(player, collarName)) {
            player.sendMessage(ColorUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }
        
        plugin.equipCollar(player, collarName);
    }
    
    private void handleRemove(Player player) {
        if (!PermissionUtil.hasRemovePermission(player)) {
            player.sendMessage(ColorUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }
        
        plugin.removeCollar(player);
    }
    
    private void handleGUI(Player player) {
        if (!PermissionUtil.hasGUIPermission(player)) {
            player.sendMessage(ColorUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }
        
        CollarGUI gui = plugin.createCollarGUI(player);
        gui.open();
    }
    
    private void handleInfo(Player player) {
        String activeCollar = plugin.getActiveCollar(player);
        
        if (activeCollar == null) {
            player.sendMessage(ColorUtil.colorize("&7You are not wearing any collar."));
            return;
        }
        
        ConfigManager.CollarConfig config = plugin.getAvailableCollars().get(activeCollar);
        if (config != null) {
            player.sendMessage(ColorUtil.colorize("&6Current collar: " + config.getDisplayName()));
            if (!config.getDescription().isEmpty()) {
                player.sendMessage(ColorUtil.colorize("&7" + config.getDescription()));
            }
        } else {
            player.sendMessage(ColorUtil.colorize("&7Current collar: " + activeCollar));
        }
    }
    
    private void handleAdmin(Player player, String adminCommand) {
        if (!PermissionUtil.hasAdminPermission(player)) {
            player.sendMessage(ColorUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }
        
        if (adminCommand.equalsIgnoreCase("reload")) {
            if (!PermissionUtil.hasReloadPermission(player)) {
                player.sendMessage(ColorUtil.colorize(plugin.getConfigManager().getMessage("no-permission")));
                return;
            }
            
            plugin.getConfigManager().reload();
            player.sendMessage(ColorUtil.colorize("&aConfiguration reloaded!"));
        } else {
            player.sendMessage(ColorUtil.colorize("&cUnknown admin command. Use 'reload'."));
        }
    }
}