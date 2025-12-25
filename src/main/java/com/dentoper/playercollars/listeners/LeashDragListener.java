package com.dentoper.playercollars.listeners;

import com.dentoper.playercollars.PlayerCollarsPlugin;
import com.dentoper.playercollars.commands.CollarCommand;
import com.dentoper.playercollars.config.ConfigManager;
import com.dentoper.playercollars.data.PlayerCollarData;
import com.dentoper.playercollars.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LeashDragListener implements Listener {
    private final PlayerCollarsPlugin plugin;
    private final CollarCommand collarCommand;

    private final Map<UUID, Long> leashWarnAtMs = new HashMap<>();

    public LeashDragListener(PlayerCollarsPlugin plugin, CollarCommand collarCommand) {
        this.plugin = plugin;
        this.collarCommand = collarCommand;
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player pet)) return;
        Player owner = event.getPlayer();

        if (!plugin.getConfigManager().isLeashSystemEnabled()) return;
        if (!owner.hasPermission("playercollars.leash")) return;

        ItemStack hand = owner.getInventory().getItemInMainHand();
        if (hand.getType() != Material.LEAD) return;

        UUID ownerUuid = owner.getUniqueId();
        UUID petUuid = pet.getUniqueId();

        UUID actualOwner = plugin.getPlayerCollarData().getOwnerUuid(petUuid);
        if (plugin.getConfigManager().isOwnerSystemEnabled() && (actualOwner == null || !actualOwner.equals(ownerUuid))) {
            return;
        }

        UUID currentPet = collarCommand.getDraggedPet(ownerUuid);
        if (petUuid.equals(currentPet)) {
            collarCommand.clearDraggedPet(ownerUuid);
        } else {
            collarCommand.setDraggedPet(ownerUuid, petUuid);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player mover = event.getPlayer();

        if (plugin.getConfigManager().isLeashSystemEnabled()) {
            tickFenceLeash(mover);
            tickDrag(mover);
        }
    }

    private void tickFenceLeash(Player pet) {
        PlayerCollarData data = plugin.getPlayerCollarData();
        if (!data.isLeashed(pet.getUniqueId())) return;

        PlayerCollarData.StoredLocation stored = data.getLeashLocation(pet.getUniqueId());
        if (stored == null) return;

        Location anchor = stored.toLocation(Bukkit.getServer());
        if (anchor == null || anchor.getWorld() == null || pet.getWorld() != anchor.getWorld()) {
            data.setLeashed(pet.getUniqueId(), false, null);
            pet.setCustomName(null);
            pet.setCustomNameVisible(false);
            return;
        }

        ConfigManager.LeashSettings settings = plugin.getConfigManager().getLeashSettings();

        double maxDist = settings.maxDistance();
        double distSq = pet.getLocation().distanceSquared(anchor);

        if (distSq <= maxDist * maxDist) {
            return;
        }

        if (settings.escapeIfOwnerFar()) {
            UUID ownerUuid = data.getOwnerUuid(pet.getUniqueId());
            if (ownerUuid != null) {
                Player owner = Bukkit.getPlayer(ownerUuid);
                if (owner == null || owner.getWorld() != pet.getWorld()) {
                    data.setLeashed(pet.getUniqueId(), false, null);
                    pet.setCustomName(null);
                    pet.setCustomNameVisible(false);
                    return;
                }

                if (owner.getLocation().distance(pet.getLocation()) > settings.escapeDistance()) {
                    data.setLeashed(pet.getUniqueId(), false, null);
                    pet.setCustomName(null);
                    pet.setCustomNameVisible(false);
                    return;
                }
            }
        }

        Location tp = anchor.clone();
        tp.setYaw(pet.getLocation().getYaw());
        tp.setPitch(pet.getLocation().getPitch());
        pet.teleport(tp);

        long now = System.currentTimeMillis();
        long last = leashWarnAtMs.getOrDefault(pet.getUniqueId(), 0L);
        if (now - last > 3000L) {
            leashWarnAtMs.put(pet.getUniqueId(), now);
            pet.sendMessage(plugin.getConfigManager().getMessage("leashed"));
        }

        pet.setCustomName(ColorUtil.color("&c[ПРИВЯЗАН] &f" + pet.getName()));
        pet.setCustomNameVisible(true);
    }

    private void tickDrag(Player owner) {
        UUID petUuid = collarCommand.getDraggedPet(owner.getUniqueId());
        if (petUuid == null) return;

        Player pet = Bukkit.getPlayer(petUuid);
        if (pet == null) {
            collarCommand.clearDraggedPet(owner.getUniqueId());
            return;
        }

        if (plugin.getPlayerCollarData().isLeashed(petUuid)) return;

        if (!isHoldingLead(owner)) {
            collarCommand.clearDraggedPet(owner.getUniqueId());
            return;
        }

        collarCommand.dragPetTowardsOwner(owner, pet);
    }

    private boolean isHoldingLead(Player player) {
        return player.getInventory().getItemInMainHand().getType() == Material.LEAD
                || player.getInventory().getItemInOffHand().getType() == Material.LEAD;
    }
}
