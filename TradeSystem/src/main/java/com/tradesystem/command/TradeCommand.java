package com.tradesystem.command;

import com.tradesystem.TradeSystemPlugin;
import com.tradesystem.trade.TradeSession;
import com.tradesystem.trade.TradeSessionManager;
import com.tradesystem.util.TradeValidator;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
}
