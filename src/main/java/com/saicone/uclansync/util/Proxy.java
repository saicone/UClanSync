package com.saicone.uclansync.util;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.saicone.uclansync.UClanSync;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Proxy {

    Proxy() {
    }

    public static void init() {
        if (!Bukkit.getServer().getMessenger().isOutgoingChannelRegistered(UClanSync.getClans(), "BungeeCord")) {
            Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(UClanSync.getClans(), "BungeeCord");
        }
    }

    public static void sendPlayer(Player player, String server) {
        ByteArrayDataOutput data = ByteStreams.newDataOutput();
        data.writeUTF("Connect");
        data.writeUTF(server);
        player.sendPluginMessage(UClanSync.getClans(), "BungeeCord", data.toByteArray());
    }
}
