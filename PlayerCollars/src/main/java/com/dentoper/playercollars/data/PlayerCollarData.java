package com.dentoper.playercollars.data;

import com.dentoper.playercollars.PlayerCollarsPlugin;
import com.dentoper.playercollars.utils.ItemStackSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.time.Instant;
import java.util.*;

public class PlayerCollarData {
    private final File file;
    private final Gson gson;

    private DataRoot root = new DataRoot();

    public PlayerCollarData(PlayerCollarsPlugin plugin) {
        this.file = new File(plugin.getDataFolder(), "player_data.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        load();
    }

    public void load() {
        if (!file.exists()) return;
        try (Reader reader = new FileReader(file)) {
            com.google.gson.JsonElement element = com.google.gson.JsonParser.parseReader(reader);
            if (element != null && element.isJsonObject() && element.getAsJsonObject().has("players")) {
                DataRoot loaded = gson.fromJson(element, DataRoot.class);
                if (loaded != null) {
                    this.root = loaded;
                }
            } else if (element != null && element.isJsonObject()) {
                java.lang.reflect.Type mapType = new com.google.gson.reflect.TypeToken<Map<String, String>>() {
                }.getType();
                Map<String, String> old = gson.fromJson(element, mapType);
                DataRoot migrated = new DataRoot();
                if (old != null) {
                    for (Map.Entry<String, String> entry : old.entrySet()) {
                        PlayerState state = new PlayerState();
                        state.current_collar = entry.getValue();
                        migrated.players.put(entry.getKey(), state);
                    }
                }
                this.root = migrated;
                save();
            }

            if (this.root.players == null) {
                this.root.players = new HashMap<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        if (!file.getParentFile().exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
        }
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(root, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlayerState getState(UUID uuid) {
        return root.players.get(uuid.toString());
    }

    public PlayerState getOrCreateState(UUID uuid) {
        return root.players.computeIfAbsent(uuid.toString(), ignored -> new PlayerState());
    }

    public void setCurrentCollar(UUID uuid, String collarId) {
        PlayerState state = getOrCreateState(uuid);
        state.current_collar = collarId;
        if (collarId == null) {
            state.owner_uuid = null;
            state.owner_name = null;
            state.leashed = false;
            state.leash_location = null;
            state.equipped_at = 0L;
        } else {
            state.equipped_at = Instant.now().getEpochSecond();
        }
        save();
    }

    public String getCurrentCollar(UUID uuid) {
        PlayerState state = getState(uuid);
        return state == null ? null : state.current_collar;
    }

    public void setOwner(UUID petUuid, UUID ownerUuid, String ownerName) {
        PlayerState state = getOrCreateState(petUuid);
        state.owner_uuid = ownerUuid == null ? null : ownerUuid.toString();
        state.owner_name = ownerName;
        save();
    }

    public UUID getOwnerUuid(UUID petUuid) {
        PlayerState state = getState(petUuid);
        if (state == null || state.owner_uuid == null) return null;
        try {
            return UUID.fromString(state.owner_uuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String getOwnerName(UUID petUuid) {
        PlayerState state = getState(petUuid);
        return state == null ? null : state.owner_name;
    }

    public void saveOriginalHelmet(UUID uuid, org.bukkit.inventory.ItemStack helmet) {
        if (helmet == null) return;
        PlayerState state = getOrCreateState(uuid);
        state.original_helmet = helmet.getType().name();
        state.original_helmet_nbt = ItemStackSerializer.toBase64(helmet);
        save();
    }

    public org.bukkit.inventory.ItemStack loadOriginalHelmet(UUID uuid) {
        PlayerState state = getState(uuid);
        if (state == null || state.original_helmet_nbt == null) return null;
        org.bukkit.inventory.ItemStack item = ItemStackSerializer.fromBase64(state.original_helmet_nbt);
        if (item != null) return item;

        if (state.original_helmet != null) {
            try {
                org.bukkit.Material mat = org.bukkit.Material.valueOf(state.original_helmet);
                return new org.bukkit.inventory.ItemStack(mat);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
        return null;
    }

    public boolean hasOriginalHelmet(UUID uuid) {
        PlayerState state = getState(uuid);
        return state != null && state.original_helmet_nbt != null;
    }

    public void clearOriginalHelmet(UUID uuid) {
        PlayerState state = getOrCreateState(uuid);
        state.original_helmet = null;
        state.original_helmet_nbt = null;
        save();
    }

    public void setLeashed(UUID uuid, boolean leashed, StoredLocation location) {
        PlayerState state = getOrCreateState(uuid);
        state.leashed = leashed;
        state.leash_location = leashed ? location : null;
        save();
    }

    public boolean isLeashed(UUID uuid) {
        PlayerState state = getState(uuid);
        return state != null && state.leashed;
    }

    public StoredLocation getLeashLocation(UUID uuid) {
        PlayerState state = getState(uuid);
        return state == null ? null : state.leash_location;
    }

    public List<UUID> getPets(UUID ownerUuid) {
        if (ownerUuid == null) return List.of();
        List<UUID> result = new ArrayList<>();
        for (Map.Entry<String, PlayerState> entry : root.players.entrySet()) {
            PlayerState state = entry.getValue();
            if (state == null || state.owner_uuid == null) continue;
            if (!state.owner_uuid.equals(ownerUuid.toString())) continue;
            try {
                result.add(UUID.fromString(entry.getKey()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return result;
    }

    public UUID getLatestPet(UUID ownerUuid) {
        UUID best = null;
        long bestAt = -1;
        for (Map.Entry<String, PlayerState> entry : root.players.entrySet()) {
            PlayerState state = entry.getValue();
            if (state == null || state.owner_uuid == null) continue;
            if (!state.owner_uuid.equals(ownerUuid.toString())) continue;
            if (state.current_collar == null) continue;
            if (state.equipped_at > bestAt) {
                try {
                    best = UUID.fromString(entry.getKey());
                    bestAt = state.equipped_at;
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return best;
    }

    public static class DataRoot {
        public Map<String, PlayerState> players = new HashMap<>();
    }

    public static class PlayerState {
        public String current_collar;
        public String owner_uuid;
        public String owner_name;
        public String original_helmet;
        public String original_helmet_nbt;
        public boolean leashed;
        public StoredLocation leash_location;
        public long equipped_at;
    }

    public static class StoredLocation {
        public String world;
        public int x;
        public int y;
        public int z;

        public static StoredLocation from(org.bukkit.Location loc) {
            StoredLocation s = new StoredLocation();
            s.world = loc.getWorld() == null ? null : loc.getWorld().getName();
            s.x = loc.getBlockX();
            s.y = loc.getBlockY();
            s.z = loc.getBlockZ();
            return s;
        }

        public org.bukkit.Location toLocation(org.bukkit.Server server) {
            if (world == null) return null;
            org.bukkit.World w = server.getWorld(world);
            if (w == null) return null;
            return new org.bukkit.Location(w, x + 0.5, y + 0.0, z + 0.5);
        }
    }
}
