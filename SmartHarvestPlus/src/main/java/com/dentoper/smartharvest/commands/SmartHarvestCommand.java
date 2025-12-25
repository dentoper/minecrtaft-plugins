package com.dentoper.smartharvest.commands;

import com.dentoper.smartharvest.SmartHarvestPlus;
import com.dentoper.smartharvest.config.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SmartHarvestCommand implements CommandExecutor, TabCompleter {

    private final SmartHarvestPlus plugin;

    public SmartHarvestCommand(SmartHarvestPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                plugin.getGuiManager().openMainMenu(player);
            } else {
                sender.sendMessage(plugin.getConfigManager().getPlayerOnlyMessage());
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
            case "rl":
                return handleReload(sender);
            case "help":
            case "?":
                return handleHelp(sender);
            default:
                sender.sendMessage(plugin.getConfigManager().getUnknownCommandMessage());
                return true;
        }
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("smartharvest.admin")) {
            sender.sendMessage(plugin.getConfigManager().getNoPermissionMessage());
            return true;
        }

        ConfigManager configManager = plugin.getConfigManager();
        configManager.reload();
        sender.sendMessage(configManager.getReloadSuccessMessage());
        return true;
    }

    private boolean handleHelp(CommandSender sender) {
        ConfigManager configManager = plugin.getConfigManager();
        sender.sendMessage(ColorUtil.color("&#D0D0D0=== &3SmartHarvest &7Помощь ==="));
        sender.sendMessage(ColorUtil.color("&#D0D0D0/sh &7- открыть меню настроек"));
        sender.sendMessage(ColorUtil.color("&#D0D0D0/sh reload &7- перезагрузить конфигурацию"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("reload");
            completions.add("help");
            String partial = args[0].toLowerCase();
            return completions.stream().filter(s -> s.startsWith(partial)).toList();
        }
        return new ArrayList<>();
    }

    private static class ColorUtil {
        static String color(String message) {
            if (message == null) return null;
            return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', message);
        }
    }
}
