package com.saicone.uclansync.module;

import com.saicone.uclansync.UClanSync;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import com.velocitypowered.api.proxy.server.ServerInfo;

public class VelocityListener {

    private final UClanSync plugin;
    private LegacyChannelIdentifier channel;

    public VelocityListener(UClanSync plugin) {
        this.plugin = plugin;
        channel = new LegacyChannelIdentifier(UClanSync.getSettings().getString("Messenger.Channel", "uclansync:channel"));
    }

    public void onEnable() {
        plugin.getProxy().getChannelRegistrar().register(channel);
        plugin.getProxy().getEventManager().register(plugin, this);
    }

    public void onDisable() {
        plugin.getProxy().getChannelRegistrar().unregister(channel);
        plugin.getProxy().getEventManager().unregisterListener(plugin, this);
    }

    public void onReload() {
        String channel = UClanSync.getSettings().getString("Messenger.Channel", "uclansync:channel");
        if (!this.channel.getId().equals(channel)) {
            plugin.getProxy().getChannelRegistrar().unregister(this.channel);
            this.channel = new LegacyChannelIdentifier(channel);
            plugin.getProxy().getChannelRegistrar().register(this.channel);
        }
    }

    @Subscribe
    public void onMessage(PluginMessageEvent e) {
        if (!e.getIdentifier().getId().equals(channel.getId())) {
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
                server.sendPluginMessage(channel, data);
            }
        })).schedule();
    }
}
