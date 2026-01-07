package com.tradesystem.command;

import com.tradesystem.TradeSystemPlugin;
import com.tradesystem.config.TradeConfig;
import com.tradesystem.inventory.TradeEditorInventory;
import com.tradesystem.trade.TradeSession;
import com.tradesystem.trade.TradeSessionManager;
import com.tradesystem.util.TradeValidator;
import com.tradesystem.inventory.TradeInventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TradeCommand implements CommandExecutor, TabCompleter {

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
            player.sendMessage("§cИспользование: /trade <игрок> или /trade editgui");
            return true;
        }

        // Команда reload - перезагрузка плагина
        if (args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("tradesystem.admin")) {
                player.sendMessage("§cУ вас нет прав на использование этой команды!");
                return true;
            }
            return handleReloadCommand(player);
        }

        // Визуальный редактор GUI (/trade editgui | /trade edit gui)
        if (args[0].equalsIgnoreCase("editgui") || (args[0].equalsIgnoreCase("edit") && args.length >= 2 && args[1].equalsIgnoreCase("gui"))) {
            if (!player.hasPermission("tradesystem.admin")) {
                player.sendMessage("§cУ вас нет прав на использование этой команды!");
                return true;
            }
            return handleEditGuiCommand(player, args);
        }

        // Текстовый редактор конфига (/trade edit ...)
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

        // Проверяем расстояние между игроками используя значение из конфига
        int maxDistance = plugin.getTradeConfig().getMaxDistance();
        if (!TradeValidator.isWithinDistance(player, target, maxDistance)) {
            // Получаем сообщение об ошибке из конфига и заменяем плейсхолдер {distance}
            String errorMessage = plugin.getTradeConfig().getErrorTooFar();
            errorMessage = errorMessage.replace("{distance}", String.valueOf(maxDistance));
            player.sendMessage(errorMessage);
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
     * Обрабатывает команды визуального редактора GUI.
     * 
     * @param player администратор, выполняющий команду
     * @param args аргументы команды
     * @return true если команда обработана
     */
    private boolean handleEditGuiCommand(Player player, String[] args) {
        // Проверяем наличие подкоманд
        String subCommand = "";
        
        // /trade editgui [subcommand]
        if (args[0].equalsIgnoreCase("editgui") && args.length > 1) {
            subCommand = args[1].toLowerCase();
        }
        // /trade edit gui [subcommand]
        else if (args[0].equalsIgnoreCase("edit") && args.length > 2) {
            subCommand = args[2].toLowerCase();
        }
        
        switch (subCommand) {
            case "help" -> {
                player.sendMessage("§6=== Визуальный редактор GUI TradeSystem ===");
                player.sendMessage("§e/trade editgui §7- открыть визуальный редактор");
                player.sendMessage("§e/trade editgui reset §7- сбросить на стандартные значения");
                player.sendMessage("§e/trade editgui help §7- эта справка");
                player.sendMessage("");
                player.sendMessage("§7В редакторе:");
                player.sendMessage("  §eЛКМ §7на слот - изменить элемент");
                player.sendMessage("  §eПКМ §7на слот - очистить слот");
                player.sendMessage("  §a✓ СОХРАНИТЬ §7- применить изменения");
                player.sendMessage("  §c✗ ОТМЕНА §7- закрыть без сохранения");
                return true;
            }
            case "reset" -> {
                TradeConfig config = plugin.getTradeConfig();
                
                // Сбрасываем на дефолтные значения
                config.set("loc.agree1", 47);
                config.set("loc.agree2", 51);
                config.set("loc.exit", 45);
                config.set("loc.player1", 3);
                config.set("loc.player2", 5);
                config.set("loc.clock", 22);
                config.set("loc.offer_p1", Arrays.asList(10, 11, 12, 19, 20, 21, 28, 29, 30, 37, 38, 39));
                config.set("loc.offer_p2", Arrays.asList(14, 15, 16, 23, 24, 25, 32, 33, 34, 41, 42, 43));
                
                player.sendMessage("§a[✓] Настройки GUI сброшены на стандартные значения!");
                player.sendMessage("§7Изменения будут применены при следующем открытии трейда.");
                
                // Обновляем все активные торговые окна
                refreshAllInventories();
                return true;
            }
            case "" -> {
                // Открываем визуальный редактор
                TradeEditorInventory editor = plugin.getEditorInventory();
                if (editor != null) {
                    editor.openEditor(player);
                    player.sendMessage("§a[✓] Визуальный редактор GUI открыт!");
                } else {
                    player.sendMessage("§c[✗] Ошибка: редактор не инициализирован!");
                }
                return true;
            }
            default -> {
                player.sendMessage("§cНеизвестная подкоманда: " + subCommand);
                player.sendMessage("§7Используйте §e/trade editgui help §7для справки");
                return true;
            }
        }
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
            player.sendMessage("§cИспользование: /trade edit <name|loc|trade|view> ...");
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
            case "trade" -> {
                if (args.length < 3) {
                    player.sendMessage("§cИспользование: /trade edit trade <max_distance|error_too_far> <значение>");
                    return true;
                }
                String element = args[2].toLowerCase();

                if (element.equals("max_distance")) {
                    if (args.length < 4) {
                        player.sendMessage("§cИспользование: /trade edit trade max_distance <расстояние>");
                        return true;
                    }
                    int distance;
                    try {
                        distance = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        player.sendMessage("§c[✗] Расстояние должно быть числом!");
                        return true;
                    }

                    if (distance < 1) {
                        player.sendMessage("§c[✗] Расстояние должно быть положительным числом!");
                        return true;
                    }

                    config.set("trade.max_distance", distance);
                    player.sendMessage("§a[✓] Максимальное расстояние изменено на: " + distance + " блоков");
                    player.sendMessage("§7Изменения применятся при следующей попытке открыть трейд.");
                } else if (element.equals("error_too_far")) {
                    if (args.length < 4) {
                        player.sendMessage("§cИспользование: /trade edit trade error_too_far <сообщение>");
                        player.sendMessage("§7Используйте {distance} для подстановки максимального расстояния");
                        return true;
                    }

                    String newMessage = String.join(" ", Arrays.copyOfRange(args, 3, args.length));

                    // Убираем кавычки если они есть
                    if (newMessage.startsWith("\"") && newMessage.endsWith("\"") && newMessage.length() > 1) {
                        newMessage = newMessage.substring(1, newMessage.length() - 1);
                    }

                    config.set("trade.error_too_far", newMessage);
                    player.sendMessage("§a[✓] Сообщение об ошибке изменено на: " + TradeConfig.color(newMessage));
                    player.sendMessage("§7Изменения применятся при следующей попытке открыть трейд.");
                } else {
                    player.sendMessage("§c[✗] Неизвестный параметр: " + element);
                    player.sendMessage("§7Доступные параметры: max_distance, error_too_far");
                }
            }
            case "view" -> {
                player.sendMessage("§6=== Текущие настройки TradeSystem ===");
                player.sendMessage("§eНастройки трейда:");
                player.sendMessage("  §7max_distance: " + config.getMaxDistance());
                player.sendMessage("  §7error_too_far: " + config.getErrorTooFar());
                
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
     * Обрабатывает команду перезагрузки плагина.
     * 
     * @param player администратор, выполняющий команду
     * @return true если команда обработана
     */
    private boolean handleReloadCommand(Player player) {
        plugin.getLogger().info("Перезагрузка TradeSystem инициирована игроком " + player.getName());
        
        // Закрываем все активные торговые сессии
        TradeSessionManager manager = TradeSessionManager.getInstance();
        int closedSessions = manager.getActiveSessions().size();
        manager.closeAllSessions("§e[⚠] Конфигурация перезагружена, трейд прерван");
        
        // Перезагружаем конфигурацию
        plugin.getTradeConfig().reload();
        
        // Сообщаем администратору
        player.sendMessage("§a[✓] TradeSystem успешно перезагружена!");
        if (closedSessions > 0) {
            player.sendMessage("§7Закрыто активных сессий: " + closedSessions);
        }
        
        plugin.getLogger().info("TradeSystem перезагружена. Закрыто сессий: " + closedSessions);
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!(sender instanceof Player)) {
            return completions;
        }
        
        Player player = (Player) sender;
        
        // /trade <TAB>
        if (args.length == 1) {
            if (player.hasPermission("tradesystem.admin")) {
                completions.add("editgui");
                completions.add("edit");
                completions.add("reload");
            }
            
            // Добавляем список игроков онлайн
            if (player.hasPermission("tradesystem.trade")) {
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> !name.equals(player.getName()))
                        .collect(Collectors.toList()));
            }
            
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        }
        
        // /trade editgui <TAB>
        if (args.length == 2 && args[0].equalsIgnoreCase("editgui")) {
            if (player.hasPermission("tradesystem.admin")) {
                completions.add("help");
                completions.add("reset");
            }
            
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        }
        
        // /trade edit <TAB>
        if (args.length == 2 && args[0].equalsIgnoreCase("edit")) {
            if (player.hasPermission("tradesystem.admin")) {
                completions.add("name");
                completions.add("loc");
                completions.add("trade");
            }

            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        }
        
        // /trade edit name <TAB>
        if (args.length == 3 && args[0].equalsIgnoreCase("edit") && args[1].equalsIgnoreCase("name")) {
            if (player.hasPermission("tradesystem.admin")) {
                completions.addAll(Arrays.asList("title", "agree", "decline", "waiting", "exit", 
                        "player1", "player2", "you1", "you2", "clock", "clock_active"));
            }
            
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        }
        
        // /trade edit loc <TAB>
        if (args.length == 3 && args[0].equalsIgnoreCase("edit") && args[1].equalsIgnoreCase("loc")) {
            if (player.hasPermission("tradesystem.admin")) {
                completions.addAll(Arrays.asList("agree1", "agree2", "exit", "player1", "player2", "clock"));
            }

            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        }

        // /trade edit trade <TAB>
        if (args.length == 3 && args[0].equalsIgnoreCase("edit") && args[1].equalsIgnoreCase("trade")) {
            if (player.hasPermission("tradesystem.admin")) {
                completions.add("max_distance");
                completions.add("error_too_far");
            }

            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        }

        // /trade edit name <element> <TAB> - показываем подсказку на формат цветовых кодов
        if (args.length == 4 && args[0].equalsIgnoreCase("edit") && args[1].equalsIgnoreCase("name")) {
            if (player.hasPermission("tradesystem.admin")) {
                completions.add("\"&a<текст>\"");
                completions.add("\"&#FF5733<текст>\"");
            }
            return completions;
        }
        
        return completions;
    }
}
