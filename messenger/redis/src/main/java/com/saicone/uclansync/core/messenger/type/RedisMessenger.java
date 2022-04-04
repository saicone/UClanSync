package com.saicone.uclansync.core.messenger.type;

import com.saicone.uclansync.UClanSync;
import com.saicone.uclansync.core.messenger.Messenger;
import com.saicone.uclansync.module.Locale;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.net.URI;
import java.util.function.Consumer;

public class RedisMessenger extends Messenger {

    static {
        Messenger.PROVIDERS.put("redis", RedisMessenger.class);
    }

    private final Bridge bridge;
    private JedisPool pool = null;
    private boolean enabled = false;

    private String url = "";

    public RedisMessenger(Consumer<String> consumer) {
        super(consumer);
        this.bridge = new Bridge();
    }

    private String getUrl() {
        String url = UClanSync.SETTINGS.getString("Messenger.Redis.url");
        if (url == null) {
            throw new NullPointerException("Redis URL can't be null");
        }
        return url;
    }

    private void newPool(String url) {
        this.url = url;
        try {
            pool = new JedisPool(new URI(this.url));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        register();
    }

    private void register() {
        enabled = true;
        new Thread(() -> {
            try (Jedis jedis = pool.getResource()) {
                jedis.subscribe(bridge, getChannel());
                bridge.alive();
            }
        }).start();
    }

    @Override
    public void onEnable() {
        newPool(getUrl());
    }

    @Override
    public void onDisable() {
        enabled = false;
        bridge.unsubscribe();
        if (pool != null) {
            pool.destroy();
            pool = null;
        }
    }

    @Override
    public void onReload() {
        String url;
        try {
            url = getUrl();
        } catch (NullPointerException e) {
            onDisable();
            throw e;
        }

        String channel = UClanSync.SETTINGS.getString("Messenger.Channel", "uclansync:channel");
        if (pool != null) {
            if (!this.url.equals(url)) {
                enabled = false;
                bridge.unsubscribe();
                pool.destroy();
            } else {
                if (!getChannel().equals(channel)) {
                    enabled = false;
                    bridge.unsubscribe();
                    setChannel(channel);
                    register();
                }
                return;
            }
        }
        setChannel(channel);
        newPool(url);
    }

    @Override
    public void sendMessage(String msg) {
        if (pool == null) {
            return;
        }
        try (Jedis jedis = pool.getResource()) {
            jedis.publish(getChannel(), msg);
        }
    }

    private class Bridge extends JedisPubSub {

        @SuppressWarnings("all")
        public void alive() {
            while (enabled && !Thread.interrupted() && pool != null && !pool.isClosed()) {
                try (Jedis jedis = pool.getResource()) {
                    Locale.log(3, "Redis connection is alive again");
                    jedis.subscribe(this, getChannel());
                } catch (Throwable t) {
                    if (enabled) {
                        Locale.log(2, "Redis connection dropped, automatic reconnection in 8 seconds...", t.getMessage());
                        try {
                            unsubscribe();
                        } catch (Throwable ignored) { }

                        try {
                            Thread.sleep(8000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        return;
                    }
                }
            }
        }

        @Override
        public void onMessage(String channel, String message) {
            if (getChannel().equals(channel)) {
                onReceive(message);
            }
        }

        @Override
        public void onSubscribe(String channel, int subscribedChannels) {
            Locale.log(3, "Redis subscribed to channel '" + channel + "'");
        }

        @Override
        public void onUnsubscribe(String channel, int subscribedChannels) {
            Locale.log(3, "Redis unsubscribed from channel '" + channel + "'");
        }
    }
}
