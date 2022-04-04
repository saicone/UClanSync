package com.saicone.uclansync.module;

import com.saicone.uclansync.UClanSync;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Map;

public class BungeeListener implements Listener {

    private final UClanSync plugin;
    private String channel;

    public BungeeListener(UClanSync plugin) {
        this.plugin = plugin;
        channel = UClanSync.SETTINGS.getConfig().getString("Messenger.Channel", "uclansync:channel");
    }

    public void onEnable() {
        plugin.getProxy().registerChannel(channel);
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    public void onDisable() {
        plugin.getProxy().unregisterChannel(channel);
    }

    public void onReload() {
        String channel = UClanSync.SETTINGS.getConfig().getString("Messenger.Channel", "uclansync:channel");
        if (!this.channel.equals(channel)) {
            plugin.getProxy().unregisterChannel(this.channel);
            plugin.getProxy().registerChannel(channel);
            this.channel = channel;
        }
    }

    @EventHandler
    public void onMessage(PluginMessageEvent e) {
        if (e.isCancelled() || !e.getTag().equals(channel)) {
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
                    entry.getValue().sendData(channel, data, false);
                }
            }
        });
    }
}
