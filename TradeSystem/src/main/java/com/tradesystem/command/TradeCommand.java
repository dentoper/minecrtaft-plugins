package com.tradesystem.command;

import com.tradesystem.TradeSystemPlugin;
import com.tradesystem.config.TradeConfig;
import com.tradesystem.trade.TradeSession;
import com.tradesystem.trade.TradeSessionManager;
import com.tradesystem.util.TradeValidator;
import com.tradesystem.inventory.TradeInventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Set;

public class TradeCommand implements CommandExecutor {

    private final TradeSystemPlugin plugin;

    public TradeCommand(TradeSystemPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cТолько игроки могут использовать эту команду!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§cИспользование: /trade <игрок> или /trade edit ...");
            return true;
        }

        // Обработка команды редактирования для администраторов
        if (args[0].equalsIgnoreCase("edit")) {
            if (!player.hasPermission("tradesystem.admin")) {
                player.sendMessage("§cУ вас нет прав на использование этой команды!");
                return true;
            }
            return handleEditCommand(player, args);
        }

        // Обычная логика трейда
        if (!player.hasPermission("tradesystem.trade")) {
            player.sendMessage("§cУ вас нет прав на использование этой команды!");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("§cИспользование: /trade <игрок>");
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null || !target.isOnline()) {
            player.sendMessage("§c[✗] Игрок не найден или не онлайн!");
            return true;
        }

        if (player.getUniqueId().equals(target.getUniqueId())) {
            player.sendMessage("§c[✗] Вы не можете торговать с самим собой!");
            return true;
        }

        TradeSessionManager manager = TradeSessionManager.getInstance();

        if (manager.isPlayerInTrade(player)) {
            player.sendMessage("§c[✗] Вы уже находитесь в трейде!");
            return true;
        }

        if (manager.isPlayerInTrade(target)) {
            player.sendMessage("§c[✗] Этот игрок уже находится в трейде!");
            return true;
        }

        if (!TradeValidator.isWithinDistance(player, target, 8.0)) {
            player.sendMessage("§c[✗] Игрок находится на расстоянии более 8 блоков!");
            return true;
        }

        if (manager.consumeRequest(target, player)) {
            TradeSession session = new TradeSession(plugin, target, player);
            manager.addSession(session);
            session.start();
        } else {
            manager.createOrReplaceRequest(player, target);
            player.sendMessage("§a[✓] Вы отправили запрос на трейд к " + target.getName() + ". Ждите ответа...");
            target.sendMessage("§6[⚠] Игрок " + player.getName() + " предложил вам трейд! Введите /trade " + player.getName() + " для принятия.");
        }

        return true;
    }

    /**
     * Обрабатывает подкоманды редактирования конфига.
     * 
     * @param player администратор, выполняющий команду
     * @param args аргументы команды
     * @return true если команда обработана
     */
    private boolean handleEditCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cИспользование: /trade edit <name|loc|view> ...");
            return true;
        }

        TradeConfig config = plugin.getTradeConfig();
        String sub = args[1].toLowerCase();

        switch (sub) {
            case "name" -> {
                if (args.length < 4) {
                    player.sendMessage("§cИспользование: /trade edit name <элемент> <новое_название>");
                    player.sendMessage("§7Элементы: title, agree, decline, waiting, exit, player1, player2, you1, you2, clock, clock_active");
                    return true;
                }
                String element = args[2].toLowerCase();
                String newName = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                
                // Убираем кавычки если они есть
                if (newName.startsWith("\"") && newName.endsWith("\"") && newName.length() > 1) {
                    newName = newName.substring(1, newName.length() - 1);
                }

                // Проверка HEX кодов
                if (newName.contains("&#")) {
                    int index = newName.indexOf("&#");
                    while (index != -1) {
                        if (index + 7 >= newName.length() || !newName.substring(index + 2, index + 8).matches("[A-Fa-f0-9]{6}")) {
                            player.sendMessage("§c[⚠] Предупреждение: обнаружен некорректный HEX-код рядом с индексом " + index);
                            player.sendMessage("§7Формат: &#RRGGBB (например, &#FF5733)");
                        }
                        index = newName.indexOf("&#", index + 1);
                    }
                }

                config.set("names." + element, newName);
                player.sendMessage("§a[✓] Название для '" + element + "' изменено на: " + TradeConfig.color(newName));
                
                // Применяем изменения к открытым инвентарям
                refreshAllInventories();
            }
            case "loc" -> {
                if (args.length < 4) {
                    player.sendMessage("§cИспользование: /trade edit loc <элемент> <номер_слота>");
                    player.sendMessage("§7Элементы: agree1, agree2, exit, player1, player2, clock");
                    return true;
                }
                String element = args[2].toLowerCase();
                int slot;
                try {
                    slot = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§c[✗] Номер слота должен быть числом!");
                    return true;
                }

                if (slot < 0 || slot > 53) {
                    player.sendMessage("§c[✗] Номер слота должен быть от 0 до 53!");
                    return true;
                }

                config.set("loc." + element, slot);
                player.sendMessage("§a[✓] Позиция для '" + element + "' изменена на слот: " + slot);
                player.sendMessage("§7Изменения применятся при следующем открытии трейда.");
            }
            case "view" -> {
                player.sendMessage("§6=== Текущие настройки TradeSystem ===");
                player.sendMessage("§eНазвания:");
                player.sendMessage("  §7title: " + config.getString("names.title", "&5&lТрейд"));
                player.sendMessage("  §7agree: " + config.getString("names.agree", "&a&l✓ СОГЛАСИЕ"));
                player.sendMessage("  §7decline: " + config.getString("names.decline", "&7&l✗ ОТМЕНЕНО"));
                player.sendMessage("  §7waiting: " + config.getString("names.waiting", "&7&l? ОЖИДАНИЕ"));
                player.sendMessage("  §7exit: " + config.getString("names.exit", "&4&l✖ ВЫХОД"));
                player.sendMessage("  §7player1: " + config.getString("names.player1", "&3&l◄ %player%"));
                player.sendMessage("  §7player2: " + config.getString("names.player2", "&3&l► %player%"));
                player.sendMessage("  §7you1: " + config.getString("names.you1", "&2&l◄ ВЫ"));
                player.sendMessage("  §7you2: " + config.getString("names.you2", "&2&l► ВЫ"));
                player.sendMessage("  §7clock: " + config.getString("names.clock", "&7&lТАЙМЕР"));
                player.sendMessage("  §7clock_active: " + config.getString("names.clock_active", "&6&lТАЙМЕР"));
                
                player.sendMessage("§eПозиции:");
                player.sendMessage("  §7agree1 (P1): " + config.getInt("loc.agree1", 47));
                player.sendMessage("  §7agree2 (P2): " + config.getInt("loc.agree2", 51));
                player.sendMessage("  §7exit: " + config.getInt("loc.exit", 45));
                player.sendMessage("  §7clock: " + config.getInt("loc.clock", 22));
                player.sendMessage("  §7player1 label: " + config.getInt("loc.player1", 3));
                player.sendMessage("  §7player2 label: " + config.getInt("loc.player2", 5));
            }
            default -> player.sendMessage("§cНеизвестная подкоманда: " + sub);
        }

        return true;
    }

    /**
     * Перерисовывает все активные торговые инвентари.
     */
    private void refreshAllInventories() {
        Set<TradeSession> sessions = TradeSessionManager.getInstance().getActiveSessions();
        for (TradeSession session : sessions) {
            session.getGuiManager().updateInventories();
        }
    }
}
