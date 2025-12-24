package com.dentoper.playercollars.commands;

import com.dentoper.playercollars.PlayerCollarsPlugin;
import com.dentoper.playercollars.config.ConfigManager;
import com.dentoper.playercollars.gui.CollarGUI;
import com.dentoper.playercollars.utils.ColorUtil;
import com.dentoper.playercollars.utils.PermissionUtil;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CollarCommand implements CommandExecutor, TabCompleter {
    private final PlayerCollarsPlugin plugin;

    public CollarCommand(PlayerCollarsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            player.sendMessage(plugin.getConfigManager().getMessage("help-message"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "gui" -> {
                plugin.getCollarGUI().open(player);
                return true;
            }
            case "list" -> {
                player.sendMessage(ColorUtil.color("&6Available collars:"));
                for (String id : plugin.getConfigManager().getCollars().keySet()) {
                    player.sendMessage(ColorUtil.color(" - &e" + id));
                }
                return true;
            }
            case "wear" -> {
                if (args.length < 2) {
                    player.sendMessage(ColorUtil.color("&cUsage: /collar wear <name>"));
                    return true;
                }
                String collarId = args[1];
                ConfigManager.CollarData collarData = plugin.getConfigManager().getCollar(collarId);
                if (collarData == null) {
                    player.sendMessage(plugin.getConfigManager().getMessage("collar-not-found"));
                    return true;
                }
                if (!PermissionUtil.has(player, collarData.getPermission())) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }
                equipCollar(player, collarData);
                return true;
            }
            case "remove" -> {
                removeCollar(player);
                return true;
            }
            case "reload" -> {
                if (!PermissionUtil.isAdmin(player)) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }
                plugin.getConfigManager().loadConfig();
                player.sendMessage(ColorUtil.color("&aConfiguration reloaded!"));
                return true;
            }
        }

        return true;
    }

    private void equipCollar(Player player, ConfigManager.CollarData collarData) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.color(collarData.getDisplayName()));
            meta.setCustomModelData(collarData.getModelData());
            item.setItemMeta(meta);
        }
        player.getInventory().setHelmet(item);
        plugin.getPlayerCollarData().setCollar(player.getUniqueId(), collarData.getId());
        player.sendMessage(plugin.getConfigManager().getMessage("collar-equipped").replace("{collar_name}", collarData.getDisplayName()));
    }

    private void removeCollar(Player player) {
        player.getInventory().setHelmet(null);
        plugin.getPlayerCollarData().setCollar(player.getUniqueId(), null);
        player.sendMessage(plugin.getConfigManager().getMessage("collar-removed"));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>(List.of("gui", "list", "wear", "remove", "help"));
            if (sender.hasPermission("playercollars.admin")) {
                subcommands.add("reload");
            }
            return subcommands.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("wear")) {
            return plugin.getConfigManager().getCollars().keySet().stream().filter(s -> s.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}
