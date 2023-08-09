package com.saicone.uclansync;

import com.google.inject.Inject;
import com.saicone.ezlib.Ezlib;
import com.saicone.uclansync.module.MainCommand;
import com.saicone.uclansync.module.Settings;
import com.saicone.uclansync.module.VelocityListener;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Plugin(id = "UClanSync", name = "Ultimate Clans Sync", version = "${version}", authors = "Rubenicos")
public class UClanSync {

    private static UClanSync instance;
    private static Settings settings;

    public static UClanSync get() {
        return instance;
    }

    public static Settings getSettings() {
        return settings;
    }

    private Ezlib ezlib;
    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDirectory;
    private final VelocityListener listener;

    private MainCommand command = null;

    @Inject
    public UClanSync(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        instance = this;
        Map<String, String> relocations = new HashMap<>();
        relocations.put(new StringBuilder("com").append(".osiris.dyml").toString(), "com.saicone.uclansync.lib.dyml");
        ezlib = new Ezlib(new File(dataDirectory.toFile(), "libs"));
        ezlib.dependency("com.github.Osiris-Team:Dyml:9.4.0", "https://jitpack.io").relocations(relocations).parent(true).load();
        settings = new Settings(dataDirectory.toFile(), "settings.yml", "${version}", true);
        this.proxy = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        listener = new VelocityListener(this);
    }

    public ProxyServer getProxy() {
        return proxy;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    @Subscribe
    public void onEnable(ProxyInitializeEvent event) {
        try {
            settings.load("settings.yml", true);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        listener.onEnable();
        reloadCommand();
    }

    @Subscribe
    public void onDisable(ProxyShutdownEvent event) {
        listener.onDisable();
        settings.clear();
    }

    public void onReload() {
        settings.reload();
        listener.onReload();
        reloadCommand();
    }

    private void reloadCommand() {
        String name = settings.getString("Command.name", "uclansyncvelocity");
        String permission = settings.getString("Command.permission", "uclansync.use");
        List<String> list = settings.getStringList("Command.aliases");
        String[] aliases = list.toArray(new String[0]);
        if (command != null) {
            if (!command.equals(name, list, permission)) {
                command.getMeta().getAliases().forEach(alias -> proxy.getCommandManager().unregister(alias));
            } else {
                return;
            }
        }
        command = new MainCommand(proxy.getCommandManager().metaBuilder(name).aliases(aliases).build(), permission);
        proxy.getCommandManager().register(command.getMeta(), command);
    }
}
