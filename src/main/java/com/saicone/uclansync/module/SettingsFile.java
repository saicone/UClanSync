package com.saicone.uclansync.module;

import me.ulrich.clans.data.Addon;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SettingsFile extends YamlConfiguration {

    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private final String path;
    private Addon addon;

    public SettingsFile(String path) {
        this.path = path;
    }

    public SettingsFile load(Addon addon) {
        this.addon = addon;
        return this;
    }

    public void onReload() {
        cache.clear();
        File file = getFile(path);
        if (!file.exists()) {
            saveResource(path, false);
        }
        try {
            load(file);
        } catch (Exception e) {
            e.printStackTrace();
            addon.setEnabled(false);
        }
    }

    @Override
    public Object get(@NotNull String path, Object def) {
        if (!cache.containsKey(path)) {
            cache.put(path, super.get(path, def));
        }
        return cache.get(path);
    }

    public void saveResource(String path, boolean replace) {
        saveResource(addon, path, replace);
    }

    public File getFile(String path) {
        return getFile(addon.getAddonDataFolder(), path);
    }

    public static void saveResource(Addon addon, String path, boolean replace) {
        InputStream input = addon.getClass().getClassLoader().getResourceAsStream(path);
        if (input != null) {
            if (!addon.getAddonDataFolder().exists() || !addon.getAddonDataFolder().isDirectory()) {
                addon.getAddonDataFolder().mkdirs();
            }
            File file = getFile(addon.getAddonDataFolder(), path);
            if (file.exists() && !replace) {
                return;
            }
            try {
                Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static File getFile(File folder, String path) {
        File file = folder;
        for (String s : path.split("/")) {
            file = new File(file, s);
        }
        return file;
    }
}
