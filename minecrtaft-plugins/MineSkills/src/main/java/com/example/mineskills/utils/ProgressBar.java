package com.example.mineskills.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Утилитарный класс для создания прогресс-баров в GUI
 */
public class ProgressBar {
    
    /**
     * Создать простой прогресс-бар
     */
    public static Component createBar(int current, int max, int length, NamedTextColor filledColor, NamedTextColor emptyColor) {
        if (max <= 0) max = 1; // Предотвращение деления на ноль
        
        double percentage = (double) current / max;
        int filled = Math.min((int) (percentage * length), length);
        
        StringBuilder bar = new StringBuilder();
        bar.append("§"); bar.append(filledColor.toString().charAt(1));
        for (int i = 0; i < filled; i++) {
            bar.append("█");
        }
        
        bar.append("§"); bar.append(emptyColor.toString().charAt(1));
        for (int i = filled; i < length; i++) {
            bar.append("█");
        }
        
        return Component.text(bar.toString() + " " + current + "/" + max);
    }
    
    /**
     * Создать прогресс-бар с процентами
     */
    public static Component createBarWithPercentage(int current, int max, int length) {
        if (max <= 0) max = 1;
        
        double percentage = (double) current / max * 100;
        int filled = (int) (percentage / 100 * length);
        
        StringBuilder bar = new StringBuilder();
        bar.append("§a"); // зеленый цвет
        for (int i = 0; i < filled; i++) {
            bar.append("█");
        }
        
        bar.append("§7"); // серый цвет
        for (int i = filled; i < length; i++) {
            bar.append("█");
        }
        
        String percentageStr = String.format("%.1f%%", percentage);
        return Component.text(bar.toString() + " " + percentageStr);
    }
    
    /**
     * Создать цветной прогресс-бар в зависимости от процента
     */
    public static Component createColoredBar(int current, int max, int length) {
        if (max <= 0) max = 1;
        
        double percentage = (double) current / max;
        int filled = (int) (percentage * length);
        
        NamedTextColor color;
        if (percentage >= 0.8) {
            color = NamedTextColor.GREEN;
        } else if (percentage >= 0.5) {
            color = NamedTextColor.YELLOW;
        } else if (percentage >= 0.2) {
            color = NamedTextColor.GOLD;
        } else {
            color = NamedTextColor.RED;
        }
        
        return createBar(current, max, length, color, NamedTextColor.GRAY);
    }
    
    /**
     * Создать прогресс-бар для скилл-поинтов
     */
    public static Component createSkillPointsBar(int currentPoints, int pointsToNext) {
        if (pointsToNext <= 0) {
            return Component.text("✓ Максимум!").color(NamedTextColor.GREEN);
        }
        
        double progress = (double) currentPoints / pointsToNext;
        return createColoredBar(currentPoints, pointsToNext, 20);
    }
}