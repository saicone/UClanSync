package com.saicone.uclansync.module;

import com.saicone.uclansync.UClanSync;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;
import java.util.Map;

public class BungeeListener implements Listener {

    private final UClanSync plugin;
    private List<String> channels;

    public BungeeListener(UClanSync plugin) {
        this.plugin = plugin;
        channels = UClanSync.SETTINGS.getConfig().getStringList("Messenger.Channels");
    }

    public void onEnable() {
        for (String channel : channels) {
            plugin.getProxy().registerChannel(channel);
        }
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    public void onDisable() {
        for (String channel : channels) {
            plugin.getProxy().unregisterChannel(channel);
        }
    }

    public void onReload() {
        List<String> channels = UClanSync.SETTINGS.getConfig().getStringList("Messenger.Channels");
        for (String channel : this.channels) {
            if (!channels.contains(channel)) {
                plugin.getProxy().unregisterChannel(channel);
            }
        }

        for (String channel : channels) {
            if (!this.channels.contains(channel)) {
                plugin.getProxy().registerChannel(channel);
            }
        }
        this.channels = channels;
    }

    @EventHandler
    public void onMessage(PluginMessageEvent e) {
        if (e.isCancelled() || !channels.contains(e.getTag())) {
            return;
        }
        e.setCancelled(true);

        if (e.getSender() instanceof ProxiedPlayer) {
            return;
        }

        String name = ((Server) e.getSender()).getInfo().getName();
        byte[] data = e.getData();
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            for (Map.Entry<String, ServerInfo> entry : plugin.getProxy().getServers().entrySet()) {
                if (!entry.getKey().equals(name)) {
                    entry.getValue().sendData(e.getTag(), data, false);
                }
            }
        });
    }
}
