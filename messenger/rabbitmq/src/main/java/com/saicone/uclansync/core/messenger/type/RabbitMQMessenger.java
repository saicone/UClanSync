package com.saicone.uclansync.core.messenger.type;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.saicone.uclansync.UClanSync;
import com.saicone.uclansync.core.messenger.Messenger;
import com.saicone.uclansync.module.Locale;
import com.rabbitmq.client.*;

import java.net.URI;
import java.util.function.Consumer;

public class RabbitMQMessenger extends Messenger implements DeliverCallback {

    static {
        Messenger.PROVIDERS.put("rabbitmq", RabbitMQMessenger.class);
    }

    private String exchange = UClanSync.SETTINGS.getString("Messenger.RabbitMQ.exchange", "uclansync");
    private ConnectionFactory factory = null;
    private Connection connection = null;
    private Channel cChannel = null;
    private boolean enabled = false;

    private boolean reconnected = false;
    private String url = "";

    public RabbitMQMessenger(Consumer<String> consumer) {
        super(consumer);
    }

    private String getUrl() {
        String url = UClanSync.SETTINGS.getString("Messenger.RabbitMQ.url");
        if (url == null) {
            throw new NullPointerException("RabbitMQ URL can't be null");
        }
        return url;
    }

    private void close(AutoCloseable... closeables) {
        try {
            for (AutoCloseable closeable : closeables) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void newConnection(String url) {
        this.url = url;
        factory = new ConnectionFactory();
        try {
            factory.setUri(new URI(this.url));
        } catch (Throwable t) {
            t.printStackTrace();
        }
        try {
            connection = factory.newConnection();
        } catch (Throwable t) {
            cChannel = null;
            t.printStackTrace();
            return;
        }
        newChannel();
    }

    private void newChannel() {
        new Thread(() -> {
            try {
                cChannel = connection.createChannel();

                String queue = cChannel.queueDeclare("", false, true, true, null).getQueue();
                cChannel.exchangeDeclare(exchange, BuiltinExchangeType.TOPIC, false, true, null);
                cChannel.queueBind(queue, exchange, getChannel());
                cChannel.basicConsume(queue, true, this, __ -> {});
                if (reconnected) {
                    Locale.log(3, "RabbitMQ connection is alive again");
                }
                enabled = true;
            } catch (Throwable t) {
                t.printStackTrace();
                return;
            }
            alive();
        }).start();
    }

    @Override
    public void onEnable() {
        newConnection(getUrl());
    }

    @Override
    public void onDisable() {
        enabled = false;
        reconnected = false;
        close(cChannel, connection);
        cChannel = null;
        connection = null;
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
        String exchange = UClanSync.SETTINGS.getString("Messenger.RabbitMQ.exchange", "uclansync");
        if (cChannel != null) {
            if (!this.url.equals(url)) {
                onDisable();
            } else {
                if (!getChannel().equals(channel) || !this.exchange.equals(exchange)) {
                    enabled = false;
                    reconnected = false;
                    close(cChannel);
                    setChannel(channel);
                    this.exchange = exchange;
                    newChannel();
                }
                return;
            }
        }
        setChannel(channel);
        this.exchange = exchange;
        newConnection(url);
    }

    @Override
    public void sendMessage(String msg) {
        if (cChannel == null) {
            return;
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(msg);

        byte[] data = out.toByteArray();
        try {
            cChannel.basicPublish(exchange, getChannel(), new AMQP.BasicProperties.Builder().build(), data);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void handle(String consumerTag, Delivery message) {
        byte[] data = message.getBody();
        ByteArrayDataInput in = ByteStreams.newDataInput(data);
        onReceive(in.readUTF());
    }

    @SuppressWarnings("all")
    private void alive() {
        while (enabled && !Thread.interrupted()) {
            if (connection != null && connection.isOpen() && cChannel != null && cChannel.isOpen()) {
                try {
                    Thread.sleep(30_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                Locale.log(2, "RabbitMQ connection dropped, automatic reconnection every 8 seconds...");
                onDisable();

                reconnected = true;
                newConnection(url);

                if (!enabled) {
                    enabled = true;
                    try {
                        Thread.sleep(8000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }
}
