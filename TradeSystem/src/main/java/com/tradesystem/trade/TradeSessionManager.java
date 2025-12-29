package com.tradesystem.trade;

import com.tradesystem.TradeSystemPlugin;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TradeSessionManager {

    private static TradeSessionManager instance;
    private final List<TradeSession> activeSessions;
    private final TradeSystemPlugin plugin;

    public TradeSessionManager(TradeSystemPlugin plugin) {
        this.plugin = plugin;
        this.activeSessions = new ArrayList<>();
        instance = this;
    }

    public static TradeSessionManager getInstance() {
        return instance;
    }

    public static void initialize(TradeSystemPlugin plugin) {
        if (instance == null) {
            new TradeSessionManager(plugin);
        }
    }

    public void addTradeSession(TradeSession session) {
        activeSessions.add(session);
    }

    public void removeTradeSession(TradeSession session) {
        activeSessions.remove(session);
        session.cleanup();
    }

    public TradeSession getTradeSession(Player player) {
        UUID playerId = player.getUniqueId();
        
        for (TradeSession session : activeSessions) {
            if (session.getInitiatorId().equals(playerId) || session.getTargetId().equals(playerId)) {
                return session;
            }
        }
        
        return null;
    }

    public boolean isPlayerInTrade(Player player) {
        return getTradeSession(player) != null;
    }

    public void cleanupAll() {
        for (TradeSession session : new ArrayList<>(activeSessions)) {
            removeTradeSession(session);
        }
    }
}