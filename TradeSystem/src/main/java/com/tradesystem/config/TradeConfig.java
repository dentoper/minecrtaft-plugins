package com.tradesystem.config;

import com.tradesystem.TradeSystemPlugin;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс для управления конфигурацией плагина.
 * Обеспечивает загрузку, сохранение и получение настроек интерфейса.
 */
public class TradeConfig {

    private final TradeSystemPlugin plugin;
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public TradeConfig(TradeSystemPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        loadDefaults();
    }

    /**
     * Заполняет стандартные значения в конфиге, если они отсутствуют.
     */
    private void loadDefaults() {
        FileConfiguration config = plugin.getConfig();
        
        // Названия элементов
        config.addDefault("names.title", "&5&lТрейд");
        config.addDefault("names.agree", "&a&l✓ СОГЛАСИЕ");
        config.addDefault("names.decline", "&7&l✗ ОТМЕНЕНО");
        config.addDefault("names.waiting", "&7&l? ОЖИДАНИЕ");
        config.addDefault("names.exit", "&4&l✖ ВЫХОД");
        config.addDefault("names.player1", "&3&l◄ %player%");
        config.addDefault("names.player2", "&3&l► %player%");
        config.addDefault("names.you1", "&2&l◄ ВЫ");
        config.addDefault("names.you2", "&2&l► ВЫ");
        config.addDefault("names.clock", "&7&lТАЙМЕР");
        config.addDefault("names.clock_active", "&6&lТАЙМЕР");

        // Позиции элементов (слоты 0-53)
        config.addDefault("loc.agree1", 47); // Изменено с 39 для избежания конфликта
        config.addDefault("loc.agree2", 51); // Изменено с 43 для избежания конфликта
        config.addDefault("loc.exit", 45);
        config.addDefault("loc.player1", 3);
        config.addDefault("loc.player2", 5);
        config.addDefault("loc.clock", 22);

        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    public void reload() {
        plugin.reloadConfig();
    }

    public String getRawString(String path, String def) {
        return plugin.getConfig().getString(path, def);
    }

    public String getString(String path, String def) {
        return color(plugin.getConfig().getString(path, def));
    }

    public int getInt(String path, int def) {
        return plugin.getConfig().getInt(path, def);
    }

    public void set(String path, Object value) {
        plugin.getConfig().set(path, value);
        plugin.saveConfig();
    }

    /**
     * Преобразует цветовые коды (включая HEX) в формат Minecraft.
     * 
     * @param message сообщение с кодами & или &#RRGGBB
     * @return отформатированная строка
     */
    public static String color(String message) {
        if (message == null) return null;

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder();

        while (matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
        }
        
        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }
}
