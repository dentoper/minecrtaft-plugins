package com.dentoper.playercollars.data;

import com.dentoper.playercollars.PlayerCollarsPlugin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerCollarData {
    private final File file;
    private final Gson gson;
    private Map<String, String> equippedCollars = new HashMap<>();

    public PlayerCollarData(PlayerCollarsPlugin plugin) {
        this.file = new File(plugin.getDataFolder(), "player_data.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        load();
    }

    public void load() {
        if (!file.exists()) return;
        try (Reader reader = new FileReader(file)) {
            equippedCollars = gson.fromJson(reader, new TypeToken<Map<String, String>>() {}.getType());
            if (equippedCollars == null) equippedCollars = new HashMap<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(equippedCollars, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCollar(UUID uuid, String collarId) {
        if (collarId == null) {
            equippedCollars.remove(uuid.toString());
        } else {
            equippedCollars.put(uuid.toString(), collarId);
        }
        save();
    }

    public String getCollar(UUID uuid) {
        return equippedCollars.get(uuid.toString());
    }
}
