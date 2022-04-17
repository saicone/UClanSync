package com.saicone.uclansync.core;

import com.saicone.uclansync.UClanSync;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

public class PlayerUpdateTask {

    private final ClanUpdater updater;
    private final Map<String, String> connected = new HashMap<>();

    private boolean onUpdate = false;
    private int task = 0;

    public PlayerUpdateTask(ClanUpdater updater) {
        this.updater = updater;
    }

    public String getPlayersAsJson() {
        // No need to use JsonObject class, just simple formatting with StringBuilder
        StringBuilder builder = new StringBuilder();
        new HashMap<>(connected).forEach((player, uuid) -> builder.append(", \"").append(player).append("\": \"").append(uuid).append("\""));
        return "{" + (builder.length() > 0 ? builder.substring(2) : "") + "}";
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(UClanSync.getClans(), () -> {
            if (onUpdate) {
                return;
            }
            onUpdate = true;

            // Avoid any error who can stop the task
            try {
                connected.clear();
                updater.players();
                Bukkit.getScheduler().runTaskLaterAsynchronously(UClanSync.getClans(), () -> {
                    try {
                        appendOnlinePlayers();
                        UClanSync.getClans().getClanAPI().setProxieds(getPlayersAsJson());
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                    onUpdate = false;
                }, UClanSync.SETTINGS.getInt("Addon.ClanUpdater.Player-Update", 80));
            } catch (Throwable t) {
                t.printStackTrace();
                onUpdate = false;
            }
        }, 1L, 10L).getTaskId();
    }

    public void stop() {
        if (task > 0) {
            Bukkit.getScheduler().cancelTask(task);
        }
    }

    public void append(String s) {
        String[] split = s.split(",", 2);
        if (split.length > 1) {
            append(split[0], split[1]);
        }
    }

    public void append(String player, String uuid) {
        if (!connected.containsKey(player)) {
            connected.put(player, uuid);
        }
    }

    public void appendOnlinePlayers() {
        Bukkit.getOnlinePlayers().forEach(player -> append(player.getName(), player.getUniqueId().toString()));
    }
}
