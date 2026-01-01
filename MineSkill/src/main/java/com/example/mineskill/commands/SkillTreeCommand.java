package com.example.mineskill.commands;

import com.example.mineskill.MineSkillPlugin;
import com.example.mineskill.models.PlayerSkillData;
import com.example.mineskill.models.Skill;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SkillTreeCommand implements TabExecutor {
    private final MineSkillPlugin plugin;

    public SkillTreeCommand(MineSkillPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(parseColor("&cЭта команда доступна только игрокам!"));
                return true;
            }

            if (!sender.hasPermission("skilltree.use")) {
                sender.sendMessage(parseColor(plugin.getConfig().getString("gui.messages.no-permission", 
                    "У вас нет прав на эту команду!")));
                return true;
            }

            plugin.getGuiManager().openGui(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "open":
                return handleOpen(sender, args);
            case "info":
                return handleInfo(sender, args);
            case "reset":
                return handleReset(sender, args);
            case "addpoints":
                return handleAddPoints(sender, args);
            case "reload":
                return handleReload(sender);
            default:
                sender.sendMessage(parseColor("&cНеизвестная подкоманда. Используйте: /skilltree <open|info|reset|addpoints|reload>"));
                return true;
        }
    }

    private boolean handleOpen(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(parseColor("&cЭта команда доступна только игрокам!"));
            return true;
        }

        if (!sender.hasPermission("skilltree.use")) {
            sender.sendMessage(parseColor(plugin.getConfig().getString("gui.messages.no-permission")));
            return true;
        }

        plugin.getGuiManager().openGui(player);
        return true;
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(parseColor("&cЭта команда доступна только игрокам!"));
            return true;
        }

        if (!sender.hasPermission("skilltree.use")) {
            sender.sendMessage(parseColor(plugin.getConfig().getString("gui.messages.no-permission")));
            return true;
        }

        PlayerSkillData data = plugin.getPlayerDataManager().getPlayerData(player);
        
        player.sendMessage("");
        player.sendMessage(parseColor("&6=== &eМои Скиллы &6==="));
        player.sendMessage(parseColor("&7Очков доступно: &e" + data.getSkillPoints()));
        player.sendMessage(parseColor("&7Всего получено: &e" + data.getTotalPoints()));
        player.sendMessage("");
        player.sendMessage(parseColor("&7Купленные скиллы:"));

        Map<String, Skill> allSkills = plugin.getSkillManager().getAllSkills();
        for (Map.Entry<String, Integer> entry : data.getSkills().entrySet()) {
            Skill skill = allSkills.get(entry.getKey());
            if (skill != null) {
                int level = entry.getValue();
                int maxLevel = skill.getMaxLevel();
                String status = level >= maxLevel ? "§a§lMAX" : "§e" + level + "/" + maxLevel;
                player.sendMessage(parseColor("  " + skill.getBranch().getColorCode() + skill.getName() + " §7- " + status));
            }
        }

        player.sendMessage("");
        return true;
    }

    private boolean handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("skilltree.admin")) {
            sender.sendMessage(parseColor(plugin.getConfig().getString("gui.messages.no-permission")));
            return true;
        }

        Player target;
        if (args.length < 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(parseColor("&cУкажите игрока: /skilltree reset <игрок>"));
                return true;
            }
            target = (Player) sender;
        } else {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(parseColor(plugin.getConfig().getString("gui.messages.player-not-found")));
                return true;
            }
        }

        plugin.getPlayerDataManager().resetPlayerSkills(target.getUniqueId());
        plugin.getSkillApplier().applySkills(target);

        String message = plugin.getConfig().getString("gui.messages.skill-reset", 
            "Все скиллы сброшены! Очки возвращены.");
        target.sendMessage(parseColor(message));
        sender.sendMessage(parseColor("&aСкиллы игрока " + target.getName() + " успешно сброшены."));

        return true;
    }

    private boolean handleAddPoints(CommandSender sender, String[] args) {
        if (!sender.hasPermission("skilltree.admin")) {
            sender.sendMessage(parseColor(plugin.getConfig().getString("gui.messages.no-permission")));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(parseColor("&cИспользование: /skilltree addpoints <игрок> <количество>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(parseColor(plugin.getConfig().getString("gui.messages.player-not-found")));
            return true;
        }

        try {
            int amount = Integer.parseInt(args[2]);
            if (amount <= 0) {
                sender.sendMessage(parseColor("&cКоличество должно быть положительным числом!"));
                return true;
            }

            plugin.getPlayerDataManager().addSkillPoints(target.getUniqueId(), amount);

            String message = plugin.getConfig().getString("gui.messages.points-added", 
                "Игроку %s добавлено %d очков.")
                .replace("%s", target.getName())
                .replace("%d", String.valueOf(amount));
            
            sender.sendMessage(parseColor(message));
            target.sendMessage(parseColor("&e+ " + amount + " очков скиллов!"));

            return true;
        } catch (NumberFormatException e) {
            sender.sendMessage(parseColor("&cНеверное количество! Используйте число."));
            return true;
        }
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("skilltree.admin")) {
            sender.sendMessage(parseColor(plugin.getConfig().getString("gui.messages.no-permission")));
            return true;
        }

        plugin.reloadConfig();
        plugin.getLogger().info("Конфигурация перезагружена " + sender.getName());

        String message = plugin.getConfig().getString("gui.messages.reload", "Конфигурация перезагружена!");
        sender.sendMessage(parseColor(message));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("open", "info", "reset", "addpoints", "reload");
            completions.addAll(subcommands.stream()
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList()));
        } else if (args.length == 2) {
            String subcommand = args[0].toLowerCase();
            if (subcommand.equals("reset") || subcommand.equals("addpoints")) {
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList()));
            }
        }

        return completions;
    }

    private String parseColor(String text) {
        return text.replace("&", "§");
    }
}
