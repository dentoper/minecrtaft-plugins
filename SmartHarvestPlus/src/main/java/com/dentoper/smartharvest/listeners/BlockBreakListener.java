package com.dentoper.smartharvest.listeners;

import com.dentoper.smartharvest.SmartHarvestPlus;
import com.dentoper.smartharvest.config.ConfigManager;
import com.dentoper.smartharvest.config.PlayerSettings;
import com.dentoper.smartharvest.utils.ColorUtil;
import com.dentoper.smartharvest.utils.HarvestUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BlockBreakListener implements Listener {

    private final SmartHarvestPlus plugin;

    public BlockBreakListener(SmartHarvestPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        ConfigManager configManager = plugin.getConfigManager();

        PlayerSettings settings = configManager.getPlayerSettings(player.getUniqueId());

        if (!settings.isEnabled()) return;

        Block block = event.getBlock();
        Material blockType = block.getType();

        if (!HarvestUtils.isCrop(blockType)) return;
        if (!configManager.getAffectedCrops().contains(blockType.name())) return;

        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (!HarvestUtils.isHoe(handItem.getType())) {
            if (configManager.isRequireHoeForAutoReplant()) return;
        }

        if (!HarvestUtils.isFullyGrown(block)) return;

        event.setCancelled(true);

        int radius = settings.getAoeRadius();
        List<Block> blocksToHarvest = new ArrayList<>();

        if (radius == 1) {
            blocksToHarvest.add(block);
        } else {
            blocksToHarvest = HarvestUtils.getRadiusBlocks(block, radius);
        }

        harvestBlocks(player, blocksToHarvest, settings);
    }

    private void harvestBlocks(Player player, List<Block> blocks, PlayerSettings settings) {
        ConfigManager configManager = plugin.getConfigManager();

        for (Block cropBlock : blocks) {
            if (!HarvestUtils.isFullyGrown(cropBlock)) continue;

            Material cropType = cropBlock.getType();
            Material seedMaterial = configManager.getSeedForCrop(cropType);

            ItemStack seed = seedMaterial != null ? new ItemStack(seedMaterial, 1) : null;

            boolean hasSeeds = seed != null && player.getInventory().containsAtLeast(seed, 1);

            ItemStack drop = new ItemStack(cropType, 1);
            if (cropType == Material.WHEAT) {
                drop = new ItemStack(Material.WHEAT, 1);
            } else if (cropType == Material.CARROTS) {
                drop = new ItemStack(Material.CARROT, 1);
            } else if (cropType == Material.POTATOES) {
                drop = new ItemStack(Material.POTATO, 1);
            } else if (cropType == Material.BEETROOTS) {
                drop = new ItemStack(Material.BEETROOT, 1);
            } else if (cropType == Material.NETHER_WART) {
                drop = new ItemStack(Material.NETHER_WART, 1);
            }

            java.util.HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(drop);
            if (!remaining.isEmpty()) {
                player.getWorld().dropItemNaturally(cropBlock.getLocation(), drop);
            }

            if (hasSeeds) {
                player.getInventory().removeItem(seed);
                cropBlock.setType(cropType);
            } else {
                cropBlock.setType(Material.AIR);
                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    player.sendMessage(ColorUtil.color("&cНет семян для пересадки!"));
                }
            }
        }

        playSoundAndParticle(player, settings);
    }

    private void playSoundAndParticle(Player player, PlayerSettings settings) {
        try {
            Sound sound = Sound.valueOf(settings.getSound());
            player.getWorld().playSound(player.getLocation(), sound, SoundCategory.PLAYERS, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().warning("Invalid sound: " + settings.getSound());
            }
        }

        try {
            org.bukkit.Particle particle = org.bukkit.Particle.valueOf(settings.getParticle());
            player.spawnParticle(particle, player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1);
        } catch (IllegalArgumentException e) {
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().warning("Invalid particle: " + settings.getParticle());
            }
        }
    }
}
