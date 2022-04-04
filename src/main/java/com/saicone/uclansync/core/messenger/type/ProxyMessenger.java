package com.saicone.uclansync.core.messenger.type;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.saicone.uclansync.UClanSync;
import com.saicone.uclansync.util.ServerInstance;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ProxyMessenger extends PluginMessenger {

    public static final String PROXY_CHANNEL = ServerInstance.isLegacy ? "BungeeCord" : "bungeecord:main";

    public ProxyMessenger(Consumer<String> consumer) {
        super(consumer);
    }

    @Override
    public void unregister(String channel) {
        Bukkit.getServer().getMessenger().unregisterIncomingPluginChannel(UClanSync.getClans(), channel, this);
    }

    @Override
    public void onEnable() {
        register(PROXY_CHANNEL);
    }

    @Override
    public void onDisable() {
        unregister(PROXY_CHANNEL);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String s, @NotNull Player player, byte[] message) {
        if (s.equals("BungeeCord") || s.equals("bungeecord:main")) {
            ByteArrayDataInput input = ByteStreams.newDataInput(message);
            String subChannel = input.readUTF();
            if ("Forward".equals(subChannel)) {
                String channel = input.readUTF();
                if (getChannel().equals(channel)) {
                    byte[] bytes = new byte[input.readShort()];
                    input.readFully(bytes);
                    onReceive(ByteStreams.newDataInput(bytes).readUTF());
                }
            }
        }
    }

    @Override
    public void sendMessage(String msg) {
        // Configure data to send
        ByteArrayDataOutput data = ByteStreams.newDataOutput();
        data.writeUTF("Forward");
        data.writeUTF("ALL");
        data.writeUTF(getChannel());

        // Insert message into data
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(msg); // Avoid message duplication
        data.writeShort(out.toByteArray().length);
        data.write(out.toByteArray());

        // Send data
        sendData(data.toByteArray(), PROXY_CHANNEL);
    }
}
