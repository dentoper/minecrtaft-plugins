package com.example.mineskills.commands;

import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Tab Completer для команды MineSkills
 * Обеспечивает автодополнение для всех подкоманд
 */
public class CommandTabCompleter implements TabCompleter {
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!(sender instanceof Player)) {
            return completions;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 1) {
            // Основные подкоманды
            completions.add("open");
            completions.add("info");
            completions.add("stats");
            
            // Админ команды
            if (player.hasPermission("mineskills.command.admin")) {
                completions.add("addpoints");
                completions.add("reload");
            }
            
        } else if (args.length == 2) {
            // Второй аргумент для админ команд
            if (args[0].equalsIgnoreCase("addpoints") && player.hasPermission("mineskills.command.admin")) {
                // Имена всех игроков
                for (Player onlinePlayer : player.getServer().getOnlinePlayers()) {
                    completions.add(onlinePlayer.getName());
                }
            }
            
        } else if (args.length == 3 && args[0].equalsIgnoreCase("addpoints")) {
            // Количество очков
            completions.add("1");
            completions.add("5");
            completions.add("10");
            completions.add("25");
            completions.add("50");
            completions.add("100");
        }
        
        // Фильтруем результаты по введенному тексту
        String lastArg = args[args.length - 1].toLowerCase();
        completions.removeIf(comp -> !comp.toLowerCase().startsWith(lastArg));
        
        return completions;
    }
}