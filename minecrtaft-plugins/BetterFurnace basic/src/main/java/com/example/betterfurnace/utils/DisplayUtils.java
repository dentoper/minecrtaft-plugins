package com.example.betterfurnace.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class DisplayUtils {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    public static String colorize(String message) {
        if (message == null) {
            return "";
        }
        return message.replace("&", "§");
    }

    public static Component legacyToComponent(String message) {
        if (message == null) {
            return Component.empty();
        }
        return LEGACY_SERIALIZER.deserialize(colorize(message));
    }

    public static String componentToLegacy(Component component) {
        if (component == null) {
            return "";
        }
        return LEGACY_SERIALIZER.serialize(component);
    }
}
