package com.tradesystem.trade;

import com.tradesystem.TradeSystemPlugin;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class TradeSessionManager {

    private static TradeSessionManager instance;

    private final TradeSystemPlugin plugin;
    private final Map<UUID, TradeSession> activeByPlayer = new HashMap<>();
    private final Map<UUID, PendingRequest> pendingRequestsByTarget = new HashMap<>();

    private TradeSessionManager(TradeSystemPlugin plugin) {
        this.plugin = plugin;
    }

    public static void initialize(TradeSystemPlugin plugin) {
        if (instance == null) {
            instance = new TradeSessionManager(plugin);
        }
    }

    public static TradeSessionManager getInstance() {
        return instance;
    }

    public TradeSystemPlugin getPlugin() {
        return plugin;
    }

    public TradeSession getSession(Player player) {
        return activeByPlayer.get(player.getUniqueId());
    }

    public boolean isPlayerInTrade(Player player) {
        return getSession(player) != null;
    }

    public void addSession(TradeSession session) {
        activeByPlayer.put(session.getPlayer1Id(), session);
        activeByPlayer.put(session.getPlayer2Id(), session);
    }

    public void removeSession(TradeSession session) {
        activeByPlayer.remove(session.getPlayer1Id());
        activeByPlayer.remove(session.getPlayer2Id());
    }

    public void cleanupAll() {
        Set<TradeSession> unique = new HashSet<>(activeByPlayer.values());
        for (TradeSession session : unique) {
            session.forceCancel("§c[✗] Трейд отменён - игроки слишком далеко друг от друга или один вышел онлайн");
        }
        activeByPlayer.clear();
        pendingRequestsByTarget.clear();
    }

    public void createOrReplaceRequest(Player initiator, Player target) {
        cleanupExpiredRequests();
        pendingRequestsByTarget.put(target.getUniqueId(), new PendingRequest(initiator.getUniqueId(), System.currentTimeMillis()));
    }

    public boolean consumeRequest(Player initiator, Player target) {
        cleanupExpiredRequests();
        PendingRequest req = pendingRequestsByTarget.get(target.getUniqueId());
        if (req == null) {
            return false;
        }
        if (!Objects.equals(req.initiatorId, initiator.getUniqueId())) {
            return false;
        }
        pendingRequestsByTarget.remove(target.getUniqueId());
        return true;
    }

    private void cleanupExpiredRequests() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, PendingRequest>> it = pendingRequestsByTarget.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, PendingRequest> entry = it.next();
            if (now - entry.getValue().createdAtMs > 60_000L) {
                it.remove();
            }
        }
    }

    private static final class PendingRequest {
        private final UUID initiatorId;
        private final long createdAtMs;

        private PendingRequest(UUID initiatorId, long createdAtMs) {
            this.initiatorId = initiatorId;
            this.createdAtMs = createdAtMs;
        }
    }
}
