package com.tradesystem.command;

import com.tradesystem.TradeSystemPlugin;
import com.tradesystem.trade.TradeSession;
import com.tradesystem.trade.TradeSessionManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TradeCommand implements CommandExecutor {

    private final TradeSystemPlugin plugin;
    private final TradeSessionManager tradeSessionManager;

    public TradeCommand(TradeSystemPlugin plugin) {
        this.plugin = plugin;
        TradeSessionManager.initialize(plugin);
        this.tradeSessionManager = TradeSessionManager.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("tradesystem.trade")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав на использование этой команды!");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Использование: /trade [игрок]");
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "§c[✗] Игрок не найден или не онлайн!");
            return true;
        }

        if (player == target) {
            player.sendMessage(ChatColor.RED + "§c[✗] Вы не можете торговать с самим собой!");
            return true;
        }

        // Check if player is already in a trade
        if (tradeSessionManager.isPlayerInTrade(player)) {
            player.sendMessage(ChatColor.RED + "§c[✗] Вы уже находитесь в трейде!");
            return true;
        }

        // Check if target is already in a trade
        if (tradeSessionManager.isPlayerInTrade(target)) {
            player.sendMessage(ChatColor.RED + "§c[✗] Этот игрок уже находится в трейде!");
            return true;
        }

        // Check distance
        if (player.getLocation().distance(target.getLocation()) > 8) {
            player.sendMessage(ChatColor.RED + "§c[✗] Игрок находится на расстоянии более 8 блоков!");
            return true;
        }

        // Create new trade session
        TradeSession tradeSession = new TradeSession(plugin, player, target);
        tradeSessionManager.addTradeSession(tradeSession);
        
        player.sendMessage(ChatColor.GREEN + "§a[✓] Вы отправили запрос на трейд " + targetName + ". Ждите ответа...");
        target.sendMessage(ChatColor.GOLD + "§6[⚠] Игрок " + player.getName() + " предложил вам трейд! Откройте интерфейс и согласитесь или отклоните запрос.");

        return true;
    }
}