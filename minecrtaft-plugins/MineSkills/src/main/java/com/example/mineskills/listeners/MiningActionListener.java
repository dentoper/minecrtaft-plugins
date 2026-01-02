package com.example.mineskills.listeners;

import com.example.mineskills.managers.PlayerDataManager;
import com.example.mineskills.managers.SkillApplier;
import com.example.mineskills.managers.ActionTracker;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

/**
 * Слушатель событий добычи для MineSkills
 * Отслеживает добычу блоков и выдает очки за Mining навык
 */
public class MiningActionListener implements Listener {
    private final PlayerDataManager playerDataManager;
    private final SkillApplier skillApplier;
    private final ActionTracker actionTracker;
    private final boolean miningTrackingEnabled;

    public MiningActionListener(PlayerDataManager playerDataManager, SkillApplier skillApplier,
                              ActionTracker actionTracker, boolean miningTrackingEnabled) {
        this.playerDataManager = playerDataManager;
        this.skillApplier = skillApplier;
        this.actionTracker = actionTracker;
        this.miningTrackingEnabled = miningTrackingEnabled;
    }

    /**
     * Обработка добычи блоков
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!miningTrackingEnabled) return;
        if (event.isCancelled()) return;
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();
        
        // Отслеживаем только руду и ценные блоки
        if (isValuableBlock(blockType)) {
            // Отслеживаем прогресс добычи
            actionTracker.trackBlockBreak(player.getUniqueId(), blockType);
            
            // Применяем бонус опыта если есть Mining навык
            double originalExp = event.getExpToDrop();
            double bonusExp = skillApplier.applyExperienceBonus(player, originalExp);
            
            // Устанавливаем новый опыт за добычу
            event.setExpToDrop((int) Math.round(bonusExp));
            
            // Специальная логика для Ore Finder
            if (skillApplier.canSeeOre(player, blockType)) {
                handleOreFinder(player, blockType);
            }
        }
    }

    /**
     * Обработка размещения блоков (может использоваться для отслеживания строительства)
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!miningTrackingEnabled) return;
        if (event.isCancelled()) return;
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = event.getPlayer();
        
        // Можно добавить логику отслеживания размещения блоков
        // для специальных навыков в будущем
    }

    /**
     * Обработка смены инструмента
     */
    @EventHandler
    public void onPlayerItemHeldChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        
        // Можно добавить логику изменения характеристик
        // в зависимости от инструмента
    }

    /**
     * Проверка, является ли блок ценным
     */
    private boolean isValuableBlock(Material material) {
        switch (material) {
            // Руды
            case COAL_ORE:
            case DEEPSLATE_COAL_ORE:
            case IRON_ORE:
            case DEEPSLATE_IRON_ORE:
            case COPPER_ORE:
            case DEEPSLATE_COPPER_ORE:
            case GOLD_ORE:
            case DEEPSLATE_GOLD_ORE:
            case LAPIS_ORE:
            case DEEPSLATE_LAPIS_ORE:
            case REDSTONE_ORE:
            case DEEPSLATE_REDSTONE_ORE:
            case DIAMOND_ORE:
            case DEEPSLATE_DIAMOND_ORE:
            case EMERALD_ORE:
            case DEEPSLATE_EMERALD_ORE:
            
            // Обработанные руды
            case COAL:
            case IRON_INGOT:
            case COPPER_INGOT:
            case GOLD_INGOT:
            case LAPIS_LAZULI:
            case REDSTONE:
            case DIAMOND:
            case EMERALD:
            
            // Другие ценные блоки
            case QUARTZ:
            case NETHER_QUARTZ_ORE:
            case ANCIENT_DEBRIS:
            case AMETHYST_SHARD:
            case GLOWSTONE:
            
                return true;
            
            default:
                return false;
        }
    }

    /**
     * Обработка скилла Ore Finder
     */
    private void handleOreFinder(Player player, Material blockType) {
        int oreFinderLevel = playerDataManager.getSkillLevel(player.getUniqueId(), "ORE_FINDER");
        
        if (oreFinderLevel <= 0) return;
        
        // Подсвечиваем найденную руду
        switch (oreFinderLevel) {
            case 1:
                // Базовый уровень - просто информация
                player.sendMessage("§aНайдена руда: " + getBlockDisplayName(blockType));
                break;
                
            case 2:
                // Улучшенный уровень - больше информации
                player.sendMessage("§aНайдена руда: " + getBlockDisplayName(blockType) + 
                    " §7(качество: хорошее)");
                break;
                
            case 3:
                // Превосходный уровень - детальная информация
                player.sendMessage("§aНайдена руда: " + getBlockDisplayName(blockType) + 
                    " §7(качество: отличное)");
                break;
                
            case 4:
            case 5:
                // Максимальный уровень - максимальная информация
                player.sendMessage("§aНайдена редкая руда: " + getBlockDisplayName(blockType) + 
                    " §7(качество: превосходное)");
                break;
        }
    }

    /**
     * Получить отображаемое название блока
     */
    private String getBlockDisplayName(Material material) {
        switch (material) {
            case COAL_ORE:
            case DEEPSLATE_COAL_ORE:
                return "§8Уголь";
            case IRON_ORE:
            case DEEPSLATE_IRON_ORE:
                return "§7Железо";
            case COPPER_ORE:
            case DEEPSLATE_COPPER_ORE:
                return "§6Медь";
            case GOLD_ORE:
            case DEEPSLATE_GOLD_ORE:
                return "§eЗолото";
            case LAPIS_ORE:
            case DEEPSLATE_LAPIS_ORE:
                return "§9Лазурит";
            case REDSTONE_ORE:
            case DEEPSLATE_REDSTONE_ORE:
                return "§cКрасная пыль";
            case DIAMOND_ORE:
            case DEEPSLATE_DIAMOND_ORE:
                return "§bАлмаз";
            case EMERALD_ORE:
            case DEEPSLATE_EMERALD_ORE:
                return "§aИзумруд";
            case ANCIENT_DEBRIS:
                return "§5Древние обломки";
            default:
                return material.toString();
        }
    }

    /**
     * Проверка, может ли игрок добыть блок быстрее (Fast Mining)
     */
    public double getMiningSpeedMultiplier(Player player, Material tool) {
        int fastMiningLevel = playerDataManager.getSkillLevel(player.getUniqueId(), "FAST_MINING");
        
        if (fastMiningLevel <= 0) return 1.0;
        
        // Бонус к скорости добычи
        double bonus = 1.0 + (0.1 * fastMiningLevel); // +10% за уровень
        
        // Проверяем, подходит ли инструмент
        if (isProperTool(tool)) {
            return bonus;
        }
        
        return 1.0; // Без бонуса если неправильный инструмент
    }

    /**
     * Проверка, является ли инструмент подходящим для добычи
     */
    private boolean isProperTool(Material material) {
        switch (material) {
            case DIAMOND_PICKAXE:
            case GOLDEN_PICKAXE:
            case IRON_PICKAXE:
            case STONE_PICKAXE:
            case WOODEN_PICKAXE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Получить отслеживание добычи (для тестирования)
     */
    public boolean isMiningTrackingEnabled() {
        return miningTrackingEnabled;
    }

    /**
     * Включить/выключить отслеживание добычи
     */
    public void setMiningTrackingEnabled(boolean enabled) {
        // Этот метод может использоваться для динамического включения/выключения
        // отслеживания добычи в зависимости от настроек сервера
    }
}