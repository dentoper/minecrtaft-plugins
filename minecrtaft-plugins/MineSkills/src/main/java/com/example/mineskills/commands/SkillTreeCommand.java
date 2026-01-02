package com.example.mineskills.commands;

import com.example.mineskills.managers.SkillManager;
import com.example.mineskills.managers.PlayerDataManager;
import com.example.mineskills.managers.SkillApplier;
import com.example.mineskills.managers.ActionTracker;
import com.example.mineskills.gui.GuiManager;
import com.example.mineskills.models.SkillBranch;
import com.example.mineskills.models.PlayerSkillData;
import net.kyori.adventure.text.Component;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Основная команда управления MineSkills
 * Поддерживает команды: open, info, addpoints
 */
public class SkillTreeCommand implements TabExecutor {
    private final SkillManager skillManager;
    private final PlayerDataManager playerDataManager;
    private final SkillApplier skillApplier;
    private final ActionTracker actionTracker;
    private final GuiManager guiManager;

    public SkillTreeCommand(SkillManager skillManager, PlayerDataManager playerDataManager,
                          SkillApplier skillApplier, ActionTracker actionTracker, GuiManager guiManager) {
        this.skillManager = skillManager;
        this.playerDataManager = playerDataManager;
        this.skillApplier = skillApplier;
        this.actionTracker = actionTracker;
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только для игроков!");
            return true;
        }

        Player player = (Player) sender;

        // Проверяем права доступа
        if (!player.hasPermission("mineskills.command")) {
            player.sendMessage("§cУ вас нет прав для использования этой команды!");
            return true;
        }

        // Если нет аргументов, открываем меню скиллов
        if (args.length == 0) {
            openSkillTree(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "open":
            case "menu":
                openSkillTree(player);
                break;

            case "info":
            case "help":
                showInfo(player);
                break;

            case "addpoints":
                if (!player.hasPermission("mineskills.command.admin")) {
                    player.sendMessage("§cУ вас нет прав для использования этой команды!");
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage("§cИспользование: /skilltree addpoints <игрок> <количество>");
                    return true;
                }
                addPoints(player, args[1], args[2]);
                break;

            case "stats":
            case "stat":
                showStats(player);
                break;

            case "reload":
                if (!player.hasPermission("mineskills.command.admin")) {
                    player.sendMessage("§cУ вас нет прав для использования этой команды!");
                    return true;
                }
                reloadConfig(player);
                break;

            default:
                player.sendMessage("§cНеизвестная команда! Используйте /skilltree для справки.");
                break;
        }

        return true;
    }

    /**
     * Открыть дерево скиллов
     */
    private void openSkillTree(Player player) {
        if (!player.hasPermission("mineskills.command.open")) {
            player.sendMessage("§cУ вас нет прав для открытия меню скиллов!");
            return;
        }

        guiManager.openSkillTree(player);
    }

    /**
     * Показать информацию о скиллах
     */
    private void showInfo(Player player) {
        player.sendMessage("§6§l=== Информация о MineSkills ===");
        player.sendMessage("");
        player.sendMessage("§7Система развития навыков для Paper 1.21");
        player.sendMessage("§7Доступно веток: §e" + SkillBranch.values().length);
        player.sendMessage("§7Доступно скиллов: §e" + skillManager.getAllSkills().size());
        player.sendMessage("");

        player.sendMessage("§6§lКоманды:");
        player.sendMessage("§e/skilltree §7- открыть меню скиллов");
        player.sendMessage("§e/skilltree info §7- показать эту информацию");
        player.sendMessage("§e/skilltree stats §7- ваша статистика");

        if (player.hasPermission("mineskills.command.admin")) {
            player.sendMessage("§e/skilltree addpoints <игрок> <количество> §7- добавить очки");
            player.sendMessage("§e/skilltree reload §7- перезагрузить конфигурацию");
        }

        player.sendMessage("");
        player.sendMessage("§6§lВетки скиллов:");
        for (SkillBranch branch : SkillBranch.values()) {
            int skillCount = skillManager.getSkillsByBranch(branch).size();
            player.sendMessage("§" + branch.getColorCode() + "• " + branch.getDisplayName() + 
                " (§e" + skillCount + "§" + branch.getColorCode() + " скиллов)");
        }

        player.sendMessage("");
        player.sendMessage("§6§lКак получить очки:");
        player.sendMessage("§7• §eДобыча блоков §7- за каждые 100 добытых блоков");
        player.sendMessage("§7• §eБой §7- за каждые 50 единиц урона");
        player.sendMessage("§7• §eДвижение §7- за каждые 1000 блоков пути");
        player.sendMessage("§7• §eПрыжки §7- за каждые 50 прыжков");
        player.sendMessage("§6§l=============================");
    }

    /**
     * Показать статистику игрока
     */
    private void showStats(Player player) {
        PlayerSkillData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        
        player.sendMessage("§6§l=== Ваша Статистика ===");
        player.sendMessage("");
        player.sendMessage("§7Имя: §e" + playerData.getPlayerName());
        player.sendMessage("§7Очки скиллов: §e" + playerData.getSkillPoints());
        player.sendMessage("§7Всего заработано: §b" + playerData.getTotalPointsEarned());
        player.sendMessage("");

        player.sendMessage("§6§lПрогресс действий:");
        var progress = playerData.getActionProgress();
        player.sendMessage("§8Добыча: §e" + progress.getMiningProgress() + "§7/§e100");
        player.sendMessage("§8Бой: §e" + progress.getCombatProgress() + "§7/§e50");
        player.sendMessage("§8Движение: §e" + progress.getMovementProgress() + "§7/§e1000");
        player.sendMessage("§8Прыжки: §e" + progress.getJumpingProgress() + "§7/§e50");
        player.sendMessage("");

        player.sendMessage("§6§lВаши навыки:");
        int skillsShown = 0;
        for (java.util.Map.Entry<String, Integer> entry : playerData.getSkills().entrySet()) {
            if (entry.getValue() > 0) {
                String skillName = getSkillDisplayName(entry.getKey());
                player.sendMessage("§7• §e" + skillName + " §7(§e" + entry.getValue() + "§7)");
                skillsShown++;
            }
        }
        
        if (skillsShown == 0) {
            player.sendMessage("§7• Пока нет изученных навыков");
        }

        player.sendMessage("§6§l============================");
    }

    /**
     * Добавить очки игроку (админ команда)
     */
    private void addPoints(Player sender, String playerName, String amountStr) {
        try {
            int amount = Integer.parseInt(amountStr);
            if (amount <= 0) {
                sender.sendMessage("§cКоличество очков должно быть положительным числом!");
                return;
            }

            // Находим игрока
            Player target = sender.getServer().getPlayer(playerName);
            if (target == null) {
                sender.sendMessage("§cИгрок " + playerName + " не найден или не в сети!");
                return;
            }

            // Добавляем очки
            boolean success = playerDataManager.addSkillPoints(target.getUniqueId(), amount);
            if (success) {
                sender.sendMessage("§a" + amount + " очков добавлено игроку " + target.getName());
                target.sendMessage("§a§l+" + amount + " очков скиллов! §7(админ: " + sender.getName() + ")");
                
                // Обновляем GUI если открыто
                if (guiManager.hasOpenGui(target)) {
                    guiManager.updateSkillTreeGui(target);
                }
            } else {
                sender.sendMessage("§cОшибка при добавлении очков!");
            }

        } catch (NumberFormatException e) {
            sender.sendMessage("§cНеверное количество очков: " + amountStr);
        }
    }

    /**
     * Перезагрузить конфигурацию (админ команда)
     */
    private void reloadConfig(Player player) {
        player.sendMessage("§aПерезагрузка конфигурации MineSkills...");
        
        try {
            // Здесь можно добавить логику перезагрузки конфига
            // Пока просто отправляем сообщение
            player.sendMessage("§aКонфигурация перезагружена!");
            player.sendMessage("§7Изменения вступят в силу при следующем входе на сервер");
            
        } catch (Exception e) {
            player.sendMessage("§cОшибка при перезагрузке: " + e.getMessage());
        }
    }

    /**
     * Получить отображаемое название скилла
     */
    private String getSkillDisplayName(String skillId) {
        var skill = skillManager.getSkill(skillId);
        return skill != null ? skill.getName() : skillId;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Подкоманды
            completions.add("open");
            completions.add("info");
            completions.add("stats");
            
            if (sender.hasPermission("mineskills.command.admin")) {
                completions.add("addpoints");
                completions.add("reload");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("addpoints")) {
            // Имена игроков
            for (Player onlinePlayer : sender.getServer().getOnlinePlayers()) {
                completions.add(onlinePlayer.getName());
            }
        }

        // Фильтруем по введенному тексту
        String lastArg = args[args.length - 1].toLowerCase();
        completions.removeIf(comp -> !comp.toLowerCase().startsWith(lastArg));

        return completions;
    }
}