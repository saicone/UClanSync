package com.saicone.uclansync.module;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class SettingsFile {

    private final String path;
    private Plugin plugin;
    private Configuration config;

    public SettingsFile(String path) {
        this.path = path;
    }

    public SettingsFile load(Plugin plugin) {
        this.plugin = plugin;
        return this;
    }

    public void onReload() {
        File file = getFile(path);
        if (!file.exists()) {
            saveResource(path, false);
        }
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Configuration getConfig() {
        return config;
    }

    public void saveResource(String path, boolean replace) {
        saveResource(plugin, path, replace);
    }

    public static void saveResource(Plugin plugin, String path, boolean replace) {
        InputStream input = plugin.getClass().getClassLoader().getResourceAsStream(path);
        if (input != null) {
            File file = getFile(plugin.getDataFolder(), path);
            if (file.exists() && !replace) {
                return;
            }
            if (!file.getParentFile().exists() || !file.getParentFile().isDirectory()) {
                file.getParentFile().mkdirs();
            }
            try {
                Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public File getFile(String path) {
        return getFile(plugin.getDataFolder(), path);
    }

    public static File getFile(File folder, String path) {
        File file = folder;
        for (String s : path.split("/")) {
            file = new File(file, s);
        }
        return file;
    }
}