package com.dentoper.smartharvest.gui;

import com.dentoper.smartharvest.SmartHarvestPlus;
import org.bukkit.entity.Player;

public class GuiManager {

    private final SmartHarvestPlus plugin;

    public GuiManager(SmartHarvestPlus plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        new MainMenu(plugin, player).open();
    }

    public void openAoeMenu(Player player) {
        new AoeMenu(plugin, player).open();
    }

    public void openSoundMenu(Player player) {
        new OptionsMenu(plugin, player, OptionsMenu.Type.SOUND).open();
    }

    public void openParticleMenu(Player player) {
        new OptionsMenu(plugin, player, OptionsMenu.Type.PARTICLE).open();
    }
}
