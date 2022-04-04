package com.saicone.uclansync.core.messenger.type;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.saicone.uclansync.UClanSync;
import com.saicone.uclansync.core.messenger.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Consumer;

public class PluginMessenger extends Messenger implements PluginMessageListener {

    public PluginMessenger(Consumer<String> consumer) {
        super(consumer);
    }

    public void register(String channel) {
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(UClanSync.getClans(), channel, this);
        if (!Bukkit.getServer().getMessenger().isOutgoingChannelRegistered(UClanSync.getClans(), channel)) {
            Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(UClanSync.getClans(), channel);
        }
    }

    public void unregister(String channel) {
        Bukkit.getServer().getMessenger().unregisterIncomingPluginChannel(UClanSync.getClans(), channel, this);
        Bukkit.getServer().getMessenger().unregisterOutgoingPluginChannel(UClanSync.getClans(), channel);
    }

    @Override
    public void onEnable() {
        register(getChannel());
    }

    @Override
    public void onDisable() {
        unregister(getChannel());
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if (getChannel().equals(channel)) {
            ByteArrayDataInput input = ByteStreams.newDataInput(message);
            onReceive(input.readUTF());
        }
    }

    @Override
    public void sendMessage(String msg) {
        ByteArrayDataOutput data = ByteStreams.newDataOutput();
        data.writeUTF(msg);
        sendData(data.toByteArray(), getChannel());
    }

    public void sendData(byte[] message, String channel) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Collection<? extends Player> players = Bukkit.getOnlinePlayers();
                Player player = Iterables.getFirst(players, null);
                if (player == null) {
                    return;
                }

                player.sendPluginMessage(UClanSync.getClans(), channel, message);
                cancel();
            }
        }.runTaskTimer(UClanSync.getClans(), 1L, 80L);
    }
}
