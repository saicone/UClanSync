package com.saicone.uclansync.module;

import com.saicone.uclansync.UClanSync;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import com.velocitypowered.api.proxy.server.ServerInfo;

import java.util.HashMap;
import java.util.Map;

public class VelocityListener {

    private final UClanSync plugin;
    private Map<String, LegacyChannelIdentifier> channels;

    public VelocityListener(UClanSync plugin) {
        this.plugin = plugin;
        channels = loadChannels();
    }

    private Map<String, LegacyChannelIdentifier> loadChannels() {
        Map<String, LegacyChannelIdentifier> channels = new HashMap<>();
        for (String channel : UClanSync.getSettings().getStringList("Messenger.Channels")) {
            channels.put(channel, new LegacyChannelIdentifier(channel));
        }
        return channels;
    }

    public void onEnable() {
        channels.forEach((id, channel) -> plugin.getProxy().getChannelRegistrar().register(channel));
        plugin.getProxy().getEventManager().register(plugin, this);
    }

    public void onDisable() {
        channels.forEach((id, channel) -> plugin.getProxy().getChannelRegistrar().unregister(channel));
        plugin.getProxy().getEventManager().unregisterListener(plugin, this);
    }

    public void onReload() {
        Map<String, LegacyChannelIdentifier> channels = loadChannels();
        this.channels.forEach((id, channel) -> {
            if (!channels.containsKey(id)) {
                plugin.getProxy().getChannelRegistrar().unregister(channel);
            }
        });

        channels.forEach((id, channel) -> {
            if (!this.channels.containsKey(id)) {
                plugin.getProxy().getChannelRegistrar().register(channel);
            }
        });
        this.channels = channels;
    }

    @Subscribe
    public void onMessage(PluginMessageEvent e) {
        if (!channels.containsKey(e.getIdentifier().getId())) {
            return;
        }

        e.setResult(PluginMessageEvent.ForwardResult.handled());

        if (e.getSource() instanceof Player) {
            return;
        }

        ServerInfo info = ((ServerConnection) e.getSource()).getServerInfo();
        byte[] data = e.getData();
        plugin.getProxy().getScheduler().buildTask(plugin, () -> plugin.getProxy().getAllServers().forEach(server -> {
            if (!server.getServerInfo().equals(info)) {
                server.sendPluginMessage(e.getIdentifier(), data);
            }
        })).schedule();
    }
}
