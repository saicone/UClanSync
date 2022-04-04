package com.saicone.uclansync.core.messenger;

import com.saicone.uclansync.UClanSync;
import com.saicone.uclansync.core.messenger.type.PluginMessenger;
import com.saicone.uclansync.core.messenger.type.ProxyMessenger;
import com.saicone.uclansync.util.Strings;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class Messenger {

    public static final Map<String, Class<? extends Messenger>> PROVIDERS = new HashMap<>();

    static {
        PROVIDERS.put("proxy", ProxyMessenger.class);
        PROVIDERS.put("plugin", PluginMessenger.class);
    }

    public static Messenger of(String s, Consumer<String> consumer) {
        for (Map.Entry<String, Class<? extends Messenger>> entry : PROVIDERS.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(s)) {
                try {
                    return entry.getValue().getDeclaredConstructor(Consumer.class).newInstance(consumer);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
        return null;
    }

    private final List<String> cached = new ArrayList<>();
    private final Consumer<String> consumer;
    private String channel = UClanSync.SETTINGS.getString("Messenger.Channel", "uclansync:channel");

    public Messenger(Consumer<String> consumer) {
        this.consumer = consumer;
    }

    public Consumer<String> getConsumer() {
        return consumer;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    private void addID(String id) {
        cached.add(id);
        Bukkit.getScheduler().runTaskLaterAsynchronously(UClanSync.getClans(), () -> cached.remove(id), 80L);
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onReload() {
        channel = UClanSync.SETTINGS.getString("Messenger.Channel", "");
    }

    public void onReceive(String msg) {
        // Avoid message duplication
        String[] split = msg.split(" ", 2);
        if (split.length < 2 || cached.contains(split[0])) {
            return;
        }
        String id = split[0];
        addID(id);

        consumer.accept(split[1]);
    }

    public void send(String msg) {
        String id = Strings.genID(6);
        addID(id);
        sendMessage(id + " " + msg);
    }

    public abstract void sendMessage(String msg);
}
