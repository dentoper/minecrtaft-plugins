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

/**
 * Менеджер активных сессий торговли.
 * Управляет созданием, хранением и очисткой сессий.
 * 
 * Совместимость: Paper 1.21.x (1.21.0 - 1.21.8+)
 */
public class TradeSessionManager {

    /** Единственный экземпляр менеджера (singleton) */
    private static TradeSessionManager instance;

    /** Экземпляр плагина */
    private final TradeSystemPlugin plugin;
    
    /** Карта активных сессий по UUID игрока */
    private final Map<UUID, TradeSession> activeByPlayer = new HashMap<>();
    
    /** Карта ожидающих запросов по UUID целевого игрока */
    private final Map<UUID, PendingRequest> pendingRequestsByTarget = new HashMap<>();

    /**
     * Создает новый экземпляр менеджера.
     * 
     * @param plugin экземпляр плагина
     */
    private TradeSessionManager(TradeSystemPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Инициализирует менеджер (вызывается при включении плагина).
     * 
     * @param plugin экземпляр плагина
     */
    public static void initialize(TradeSystemPlugin plugin) {
        if (instance == null) {
            instance = new TradeSessionManager(plugin);
        }
    }

    /**
     * Возвращает экземпляр менеджера.
     * 
     * @return экземпляр TradeSessionManager
     * @throws IllegalStateException если менеджер не инициализирован
     */
    public static TradeSessionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("TradeSessionManager не инициализирован. Вызовите initialize() сначала.");
        }
        return instance;
    }

    /**
     * Возвращает экземпляр плагина.
     */
    public TradeSystemPlugin getPlugin() {
        return plugin;
    }

    /**
     * Получает активную сессию для указанного игрока.
     * 
     * @param player игрок для поиска
     * @return сессия торговли или null если игрок не в трейде
     */
    public TradeSession getSession(Player player) {
        return activeByPlayer.get(player.getUniqueId());
    }

    /**
     * Проверяет, находится ли игрок в активном трейде.
     * 
     * @param player игрок для проверки
     * @return true если игрок в трейде
     */
    public boolean isPlayerInTrade(Player player) {
        return getSession(player) != null;
    }

    /**
     * Добавляет новую сессию в менеджер.
     * 
     * @param session сессия для добавления
     */
    public void addSession(TradeSession session) {
        activeByPlayer.put(session.getPlayer1Id(), session);
        activeByPlayer.put(session.getPlayer2Id(), session);
    }

    /**
     * Удаляет сессию из менеджера.
     * 
     * @param session сессия для удаления
     */
    public void removeSession(TradeSession session) {
        activeByPlayer.remove(session.getPlayer1Id());
        activeByPlayer.remove(session.getPlayer2Id());
    }

    /**
     * Возвращает все активные сессии торговли.
     * 
     * @return набор уникальных активных сессий
     */
    public Set<TradeSession> getActiveSessions() {
        return new HashSet<>(activeByPlayer.values());
    }

    /**
     * Очищает все активные сессии и ожидающие запросы.
     * Вызывается при отключении плагина.
     */
    public void cleanupAll() {
        Set<TradeSession> unique = new HashSet<>(activeByPlayer.values());
        for (TradeSession session : unique) {
            session.forceCancel("§c[✗] Трейд отменён - плагин отключен");
        }
        activeByPlayer.clear();
        pendingRequestsByTarget.clear();
    }

    /**
     * Закрывает все активные сессии с указанным сообщением.
     * Используется при перезагрузке конфигурации.
     * 
     * @param message сообщение для отправки игрокам
     */
    public void closeAllSessions(String message) {
        Set<TradeSession> unique = new HashSet<>(activeByPlayer.values());
        for (TradeSession session : unique) {
            session.forceCancel(message);
        }
        activeByPlayer.clear();
    }

    /**
     * Создает или заменяет запрос на трейд.
     * 
     * @param initiator игрок, инициирующий запрос
     * @param target целевой игрок
     */
    public void createOrReplaceRequest(Player initiator, Player target) {
        cleanupExpiredRequests();
        pendingRequestsByTarget.put(target.getUniqueId(), 
            new PendingRequest(initiator.getUniqueId(), System.currentTimeMillis()));
    }

    /**
     * Пытается принять запрос на трейд.
     * Удаляет запрос после успешного потребления.
     * 
     * @param initiator предполагаемый инициатор
     * @param target игрок, принимающий запрос
     * @return true если запрос найден и принят
     */
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

    /**
     * Удаляет просроченные запросы (старше 60 секунд).
     */
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

    /**
     * Внутренний класс для хранения информации о запросе на трейд.
     */
    private static final class PendingRequest {
        private final UUID initiatorId;
        private final long createdAtMs;

        private PendingRequest(UUID initiatorId, long createdAtMs) {
            this.initiatorId = initiatorId;
            this.createdAtMs = createdAtMs;
        }
    }
}
