package com.dentoper.playercollars.commands;

import com.dentoper.playercollars.PlayerCollarsPlugin;
import com.dentoper.playercollars.config.ConfigManager;
import com.dentoper.playercollars.data.PlayerCollarData;
import com.dentoper.playercollars.gui.CollarGUI;
import com.dentoper.playercollars.utils.CollarItemUtil;
import com.dentoper.playercollars.utils.ColorUtil;
import com.dentoper.playercollars.utils.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class CollarCommand implements CommandExecutor, TabCompleter {
    private final PlayerCollarsPlugin plugin;
    private final CollarGUI collarGUI;

    private final Map<UUID, CollarEquipRequest> collarRequests = new HashMap<>();
    private final Map<UUID, SummonRequest> summonRequests = new HashMap<>();

    private final Map<UUID, UUID> activeDrags = new HashMap<>();
    private final Map<UUID, Long> lastSummonAtMs = new HashMap<>();

    public CollarCommand(PlayerCollarsPlugin plugin) {
        this.plugin = plugin;
        this.collarGUI = new CollarGUI(plugin);
        plugin.getServer().getPluginManager().registerEvents(collarGUI, plugin);
    }

    public UUID getDraggedPet(UUID ownerUuid) {
        return activeDrags.get(ownerUuid);
    }

    public boolean isPetBeingDragged(UUID petUuid) {
        return activeDrags.containsValue(petUuid);
    }

    public void setDraggedPet(UUID ownerUuid, UUID petUuid) {
        if (ownerUuid == null || petUuid == null) return;
        activeDrags.put(ownerUuid, petUuid);
    }

    public void clearDraggedPet(UUID ownerUuid) {
        if (ownerUuid == null) return;
        activeDrags.remove(ownerUuid);
    }

    public void clearDraggedPetByPet(UUID petUuid) {
        if (petUuid == null) return;
        activeDrags.entrySet().removeIf(e -> e.getValue().equals(petUuid));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(player);
            return true;
        }

        return switch (args[0].toLowerCase()) {
            case "gui" -> {
                collarGUI.open(player);
                yield true;
            }
            case "list" -> {
                player.sendMessage(ColorUtil.color("&6Available collars:"));
                for (String id : plugin.getConfigManager().getCollars().keySet()) {
                    player.sendMessage(ColorUtil.color(" - &e" + id));
                }
                yield true;
            }
            case "wear" -> {
                if (args.length < 2) {
                    player.sendMessage(ColorUtil.color("&cUsage: /collar wear <name>"));
                    yield true;
                }
                String collarId = args[1];
                ConfigManager.CollarData collarData = plugin.getConfigManager().getCollar(collarId);
                if (collarData == null) {
                    player.sendMessage(plugin.getConfigManager().getMessage("collar-not-found"));
                    yield true;
                }
                if (!PermissionUtil.has(player, collarData.getPermission())) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    yield true;
                }
                equipCollar(player, player, collarData, false);
                yield true;
            }
            case "remove" -> {
                removeCollar(player);
                yield true;
            }
            case "give" -> {
                if (!player.hasPermission("playercollars.give")) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    yield true;
                }
                if (args.length < 3) {
                    player.sendMessage(ColorUtil.color("&cUsage: /collar give <player> <collar>"));
                    yield true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    player.sendMessage(ColorUtil.color("&cPlayer not found"));
                    yield true;
                }
                ConfigManager.CollarData collarData = plugin.getConfigManager().getCollar(args[2]);
                if (collarData == null) {
                    player.sendMessage(plugin.getConfigManager().getMessage("collar-not-found"));
                    yield true;
                }
                if (!PermissionUtil.has(target, collarData.getPermission()) && !PermissionUtil.isAdmin(player)) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    yield true;
                }
                equipCollar(player, target, collarData, true);
                yield true;
            }
            case "request" -> {
                if (!player.hasPermission("playercollars.give")) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    yield true;
                }
                if (args.length < 3) {
                    player.sendMessage(ColorUtil.color("&cUsage: /collar request <player> <collar>"));
                    yield true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    player.sendMessage(ColorUtil.color("&cPlayer not found"));
                    yield true;
                }
                ConfigManager.CollarData collarData = plugin.getConfigManager().getCollar(args[2]);
                if (collarData == null) {
                    player.sendMessage(plugin.getConfigManager().getMessage("collar-not-found"));
                    yield true;
                }
                if (!PermissionUtil.has(target, collarData.getPermission()) && !PermissionUtil.isAdmin(player)) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    yield true;
                }
                collarRequests.put(target.getUniqueId(), new CollarEquipRequest(player.getUniqueId(), collarData.getId()));
                player.sendMessage(plugin.getConfigManager().getMessage("request-sent").replace("{player}", target.getName()));
                target.sendMessage(plugin.getConfigManager().getMessage("request-received").replace("{player}", player.getName()));
                yield true;
            }
            case "accept" -> {
                if (tryAcceptCollarRequest(player)) {
                    yield true;
                }
                if (tryAcceptSummonRequest(player)) {
                    yield true;
                }
                player.sendMessage(ColorUtil.color("&cNo pending requests."));
                yield true;
            }
            case "deny" -> {
                CollarEquipRequest collarReq = collarRequests.remove(player.getUniqueId());
                if (collarReq != null) {
                    player.sendMessage(plugin.getConfigManager().getMessage("request-denied"));
                    Player requester = Bukkit.getPlayer(collarReq.requesterUuid());
                    if (requester != null) {
                        requester.sendMessage(plugin.getConfigManager().getMessage("prefix")
                                + ColorUtil.color("&cИгрок &e" + player.getName() + " &cотклонил запрос."));
                    }
                    yield true;
                }
                if (summonRequests.remove(player.getUniqueId()) != null) {
                    player.sendMessage(plugin.getConfigManager().getMessage("request-denied"));
                    yield true;
                }
                player.sendMessage(ColorUtil.color("&cNo pending requests."));
                yield true;
            }
            case "leash" -> {
                if (!plugin.getConfigManager().isLeashSystemEnabled()) {
                    player.sendMessage(ColorUtil.color("&cLeash system is disabled."));
                    yield true;
                }
                if (!player.hasPermission("playercollars.leash")) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    yield true;
                }
                if (args.length < 2) {
                    player.sendMessage(ColorUtil.color("&cUsage: /collar leash <player>"));
                    yield true;
                }
                Player pet = Bukkit.getPlayerExact(args[1]);
                if (pet == null) {
                    player.sendMessage(ColorUtil.color("&cPlayer not found"));
                    yield true;
                }
                if (!isOwnerOf(player, pet)) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    yield true;
                }
                if (plugin.getPlayerCollarData().getCurrentCollar(pet.getUniqueId()) == null) {
                    player.sendMessage(ColorUtil.color("&cPlayer is not wearing a collar."));
                    yield true;
                }

                Location anchor = findNearestLeashAnchor(pet.getLocation(), 4);
                if (anchor == null) {
                    player.sendMessage(ColorUtil.color("&cNo fence/wall nearby."));
                    yield true;
                }

                plugin.getPlayerCollarData().setLeashed(pet.getUniqueId(), true, PlayerCollarData.StoredLocation.from(anchor));
                pet.setCustomName(ColorUtil.color("&c[ПРИВЯЗАН] &f" + pet.getName()));
                pet.setCustomNameVisible(true);

                player.sendMessage(plugin.getConfigManager().getMessage("leash-attached").replace("{player}", pet.getName()));
                pet.sendMessage(plugin.getConfigManager().getMessage("leashed"));
                yield true;
            }
            case "unleash" -> {
                if (!plugin.getConfigManager().isLeashSystemEnabled()) {
                    player.sendMessage(ColorUtil.color("&cLeash system is disabled."));
                    yield true;
                }
                if (!player.hasPermission("playercollars.leash")) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    yield true;
                }
                Player pet = findLatestOnlinePet(player);
                if (pet == null) {
                    player.sendMessage(ColorUtil.color("&cNo pet found."));
                    yield true;
                }
                if (!isOwnerOf(player, pet)) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    yield true;
                }

                plugin.getPlayerCollarData().setLeashed(pet.getUniqueId(), false, null);
                pet.setCustomName(null);
                pet.setCustomNameVisible(false);

                player.sendMessage(plugin.getConfigManager().getMessage("leash-detached").replace("{player}", pet.getName()));
                yield true;
            }
            case "call" -> {
                if (!plugin.getConfigManager().isSummonSystemEnabled()) {
                    player.sendMessage(ColorUtil.color("&cSummon system is disabled."));
                    yield true;
                }
                if (!player.hasPermission("playercollars.call")) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    yield true;
                }
                UUID ownerUuid = plugin.getPlayerCollarData().getOwnerUuid(player.getUniqueId());
                if (ownerUuid == null) {
                    player.sendMessage(ColorUtil.color("&cYou have no owner."));
                    yield true;
                }
                Player owner = Bukkit.getPlayer(ownerUuid);
                if (owner == null) {
                    player.sendMessage(ColorUtil.color("&cOwner is offline."));
                    yield true;
                }
                summonRequests.put(ownerUuid, new SummonRequest(player.getUniqueId()));
                player.sendMessage(plugin.getConfigManager().getMessage("summon-sent"));
                owner.sendMessage(plugin.getConfigManager().getMessage("summon-received").replace("{player}", player.getName()));
                yield true;
            }
            case "summon" -> {
                if (!plugin.getConfigManager().isSummonSystemEnabled()) {
                    player.sendMessage(ColorUtil.color("&cSummon system is disabled."));
                    yield true;
                }
                if (!player.hasPermission("playercollars.summon")) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission-summon"));
                    yield true;
                }
                Player pet = findLatestOnlinePet(player);
                if (pet == null) {
                    player.sendMessage(ColorUtil.color("&cNo pet found."));
                    yield true;
                }
                if (!isOwnerOf(player, pet)) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    yield true;
                }

                summonPet(player, pet);
                yield true;
            }
            case "owner" -> {
                Player target = player;
                if (args.length >= 2) {
                    if (!PermissionUtil.isAdmin(player) && !player.getName().equalsIgnoreCase(args[1])) {
                        player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                        yield true;
                    }
                    Player online = Bukkit.getPlayerExact(args[1]);
                    if (online != null) {
                        target = online;
                    } else {
                        player.sendMessage(ColorUtil.color("&cPlayer not found (must be online)."));
                        yield true;
                    }
                }

                showOwnerInfo(player, target);
                yield true;
            }
            case "reload" -> {
                if (!PermissionUtil.isAdmin(player)) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    yield true;
                }
                plugin.getConfigManager().loadConfig();
                player.sendMessage(ColorUtil.color("&aConfiguration reloaded!"));
                yield true;
            }
            default -> {
                sendHelp(player);
                yield true;
            }
        };
    }

    private boolean tryAcceptCollarRequest(Player target) {
        CollarEquipRequest req = collarRequests.remove(target.getUniqueId());
        if (req == null) return false;

        Player requester = Bukkit.getPlayer(req.requesterUuid());
        if (requester == null) {
            target.sendMessage(ColorUtil.color("&cRequester is offline."));
            return true;
        }

        ConfigManager.CollarData collarData = plugin.getConfigManager().getCollar(req.collarId());
        if (collarData == null) {
            target.sendMessage(plugin.getConfigManager().getMessage("collar-not-found"));
            return true;
        }

        equipCollar(requester, target, collarData, true);
        target.sendMessage(plugin.getConfigManager().getMessage("request-accepted"));
        return true;
    }

    private boolean tryAcceptSummonRequest(Player owner) {
        SummonRequest req = summonRequests.remove(owner.getUniqueId());
        if (req == null) return false;

        if (!owner.hasPermission("playercollars.summon")) {
            owner.sendMessage(plugin.getConfigManager().getMessage("no-permission-summon"));
            return true;
        }

        Player pet = Bukkit.getPlayer(req.petUuid());
        if (pet == null) {
            owner.sendMessage(ColorUtil.color("&cPet is offline."));
            return true;
        }

        if (!isOwnerOf(owner, pet)) {
            owner.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        summonPet(owner, pet);
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(plugin.getConfigManager().getMessage("help-header"));
        player.sendMessage(plugin.getConfigManager().getMessage("help-wear"));
        player.sendMessage(plugin.getConfigManager().getMessage("help-give"));
        player.sendMessage(plugin.getConfigManager().getMessage("help-request"));
        player.sendMessage(plugin.getConfigManager().getMessage("help-accept"));
        player.sendMessage(plugin.getConfigManager().getMessage("help-deny"));
        player.sendMessage(plugin.getConfigManager().getMessage("help-leash"));
        player.sendMessage(plugin.getConfigManager().getMessage("help-unleash"));
        player.sendMessage(plugin.getConfigManager().getMessage("help-call"));
        player.sendMessage(plugin.getConfigManager().getMessage("help-summon"));
    }

    private void showOwnerInfo(Player viewer, Player target) {
        PlayerCollarData data = plugin.getPlayerCollarData();
        PlayerCollarData.PlayerState state = data.getState(target.getUniqueId());

        viewer.sendMessage(ColorUtil.color("&6========== Owner Info =========="));
        if (state == null || state.current_collar == null) {
            viewer.sendMessage(ColorUtil.color("&e" + target.getName() + "&f: &cno collar"));
            return;
        }

        viewer.sendMessage(ColorUtil.color("&ePlayer: &f" + target.getName()));
        viewer.sendMessage(ColorUtil.color("&eCollar: &f" + state.current_collar));
        viewer.sendMessage(ColorUtil.color("&eOwner: &f" + (state.owner_name == null ? "-" : state.owner_name)));
        viewer.sendMessage(ColorUtil.color("&eLeashed: &f" + (state.leashed ? "yes" : "no")));
    }

    private boolean isOwnerOf(Player owner, Player pet) {
        if (!plugin.getConfigManager().isOwnerSystemEnabled()) return true;
        UUID ownerUuid = plugin.getPlayerCollarData().getOwnerUuid(pet.getUniqueId());
        return ownerUuid != null && ownerUuid.equals(owner.getUniqueId());
    }

    private Player findLatestOnlinePet(Player owner) {
        UUID petUuid = plugin.getPlayerCollarData().getLatestPet(owner.getUniqueId());
        if (petUuid == null) return null;
        return Bukkit.getPlayer(petUuid);
    }

    private void equipCollar(Player actor, Player target, ConfigManager.CollarData collarData, boolean setOwner) {
        PlayerCollarData data = plugin.getPlayerCollarData();

        if (data.getCurrentCollar(target.getUniqueId()) != null) {
            target.sendMessage(plugin.getConfigManager().getMessage("already-wearing"));
            return;
        }

        if (plugin.getConfigManager().isHelmetBackupEnabled()) {
            ItemStack currentHelmet = target.getInventory().getHelmet();
            if (!data.hasOriginalHelmet(target.getUniqueId()) && currentHelmet != null && currentHelmet.getType() != Material.AIR) {
                if (!CollarItemUtil.isCollarItem(plugin, currentHelmet)) {
                    data.saveOriginalHelmet(target.getUniqueId(), currentHelmet);
                    target.sendMessage(plugin.getConfigManager().getMessage("helmet-saved"));
                }
            }
        }

        target.getInventory().setHelmet(CollarItemUtil.create(plugin, collarData));
        data.setCurrentCollar(target.getUniqueId(), collarData.getId());

        if (setOwner && actor != null && !actor.getUniqueId().equals(target.getUniqueId())) {
            if (plugin.getConfigManager().isOwnerSystemEnabled()) {
                data.setOwner(target.getUniqueId(), actor.getUniqueId(), actor.getName());
            } else {
                data.setOwner(target.getUniqueId(), null, null);
            }
            actor.sendMessage(plugin.getConfigManager().getMessage("collar-given").replace("{player}", target.getName()));
            target.sendMessage(plugin.getConfigManager().getMessage("collar-received").replace("{owner}", actor.getName()));
        } else {
            data.setOwner(target.getUniqueId(), null, null);
            target.sendMessage(plugin.getConfigManager().getMessage("collar-equipped")
                    .replace("{collar_name}", ColorUtil.color(collarData.getDisplayName())));
        }
    }

    private void removeCollar(Player player) {
        PlayerCollarData data = plugin.getPlayerCollarData();
        if (data.getCurrentCollar(player.getUniqueId()) == null) {
            player.sendMessage(ColorUtil.color("&cYou are not wearing a collar."));
            return;
        }

        data.setCurrentCollar(player.getUniqueId(), null);

        if (plugin.getConfigManager().isHelmetBackupEnabled()) {
            ItemStack original = data.loadOriginalHelmet(player.getUniqueId());
            if (original != null) {
                player.getInventory().setHelmet(original);
                player.sendMessage(plugin.getConfigManager().getMessage("helmet-restored"));
            } else {
                player.getInventory().setHelmet(null);
            }
        } else {
            player.getInventory().setHelmet(null);
        }

        data.clearOriginalHelmet(player.getUniqueId());

        player.setCustomName(null);
        player.setCustomNameVisible(false);
        clearDraggedPetByPet(player.getUniqueId());

        player.sendMessage(plugin.getConfigManager().getMessage("collar-removed"));
    }

    private Location findNearestLeashAnchor(Location origin, int radius) {
        if (origin.getWorld() == null) return null;

        Block best = null;
        double bestDist = Double.MAX_VALUE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    Block b = origin.getBlock().getRelative(dx, dy, dz);
                    Material type = b.getType();
                    if (!Tag.FENCES.isTagged(type) && !Tag.WALLS.isTagged(type)) continue;
                    double dist = b.getLocation().distanceSquared(origin);
                    if (dist < bestDist) {
                        bestDist = dist;
                        best = b;
                    }
                }
            }
        }

        return best == null ? null : best.getLocation();
    }

    public void dragPetTowardsOwner(Player owner, Player pet) {
        if (owner == null || pet == null) return;
        if (owner.getWorld() != pet.getWorld()) return;

        ConfigManager.LeashSettings settings = plugin.getConfigManager().getLeashSettings();

        double dist = owner.getLocation().distance(pet.getLocation());
        if (dist <= settings.dragDistance()) {
            return;
        }

        Vector direction = owner.getLocation().toVector().subtract(pet.getLocation().toVector());
        direction.setY(0);
        if (direction.lengthSquared() == 0) return;

        Vector vel = direction.normalize().multiply(settings.dragSpeed());
        if (owner.getVelocity().getY() > 0.2) {
            vel.setY(0.42);
        }

        pet.setVelocity(vel);
    }

    private void summonPet(Player owner, Player pet) {
        ConfigManager.SummonSettings settings = plugin.getConfigManager().getSummonSettings();

        if (settings.sameWorldOnly() && owner.getWorld() != pet.getWorld()) {
            owner.sendMessage(ColorUtil.color("&cPet is in another world."));
            return;
        }

        if (owner.getWorld() == pet.getWorld()) {
            double distance = owner.getLocation().distance(pet.getLocation());
            if (distance > settings.maxDistance()) {
                owner.sendMessage(ColorUtil.color("&cToo far to summon (" + (int) distance + "/" + settings.maxDistance() + ")."));
                return;
            }
        }

        long now = System.currentTimeMillis();
        Long last = lastSummonAtMs.get(owner.getUniqueId());
        if (last != null) {
            long cdMs = settings.cooldownSeconds() * 1000L;
            long remaining = cdMs - (now - last);
            if (remaining > 0) {
                owner.sendMessage(ColorUtil.color("&cCooldown: " + (remaining / 1000L) + "s"));
                return;
            }
        }
        lastSummonAtMs.put(owner.getUniqueId(), now);

        Location from = pet.getLocation().clone();
        Location to = owner.getLocation().clone();

        if (settings.teleportEffects()) {
            from.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_NORMAL, from, 30, 0.5, 0.5, 0.5, 0.02);
            to.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_NORMAL, to, 30, 0.5, 0.5, 0.5, 0.02);
            to.getWorld().playSound(to, org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            to.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, to, 20, 0.5, 0.8, 0.5, 0.01);
        }

        plugin.getPlayerCollarData().setLeashed(pet.getUniqueId(), false, null);
        pet.setCustomName(null);
        pet.setCustomNameVisible(false);
        clearDraggedPetByPet(pet.getUniqueId());

        pet.teleport(to);
        pet.sendMessage(plugin.getConfigManager().getMessage("summoned").replace("{player}", owner.getName()));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>(List.of(
                    "gui", "list", "wear", "give", "request", "accept", "deny", "leash", "unleash", "call", "summon", "owner", "help"
            ));
            if (sender.hasPermission("playercollars.admin")) {
                subcommands.add("reload");
                subcommands.add("remove");
            }
            return subcommands.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }

        if (args.length == 2) {
            return switch (args[0].toLowerCase()) {
                case "wear" -> plugin.getConfigManager().getCollars().keySet().stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .sorted()
                        .collect(Collectors.toList());
                case "give", "request", "leash", "owner" -> Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                        .sorted()
                        .collect(Collectors.toList());
                default -> List.of();
            };
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("request"))) {
            return plugin.getConfigManager().getCollars().keySet().stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    private record CollarEquipRequest(UUID requesterUuid, String collarId) {
    }

    private record SummonRequest(UUID petUuid) {
    }
}
