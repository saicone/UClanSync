package com.saicone.uclansync.module;

import com.saicone.uclansync.UClanSync;
import com.saicone.uclansync.util.Strings;
import me.ulrich.clans.data.Addon;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.List;

public class Locale {

    public static final Locale INSTANCE = new Locale();

    public static void log(int level, String... msg) {
        if (INSTANCE.getLogLevel() >= level) {
            switch (level) {
                case 1:
                    Bukkit.getConsoleSender().sendMessage(Strings.concatColor("&4[UClanSync] ", msg));
                    return;
                case 2:
                    Bukkit.getConsoleSender().sendMessage(Strings.concatColor("&6[UClanSync] ", msg));
                    return;
                case 3:
                case 4:
                    Bukkit.getConsoleSender().sendMessage(Strings.concatColor("[UClanSync] ", msg));
            }
        }
    }

    public static void sendTo(CommandSender sender, String path, Object... args) {
        INSTANCE.send(sender, path, args);
    }

    private final SettingsFile lang = new SettingsFile("lang.yml");
    private int logLevel = 3;

    private Locale() {
    }

    public void load(Addon addon) {
        lang.load(addon);
        onReload();
    }

    public int getLogLevel() {
        return logLevel;
    }

    public void onReload() {
        logLevel = UClanSync.SETTINGS.getInt("Addon.LogLevel", 3);
        lang.onReload();
    }

    public void send(CommandSender sender, String path, Object... args) {
        List<String> list = lang.getStringList(path);
        if (list.isEmpty()) {
            String s = lang.getString(path);
            if (s != null) {
                list.add(s);
            } else {
                log(2, "Locale path '" + path + "' is empty");
                return;
            }
        }
        sender.sendMessage(Strings.replaceArgs(list, true, args).toArray(new String[0]));
    }
}
