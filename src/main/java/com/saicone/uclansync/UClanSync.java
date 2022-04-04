package com.saicone.uclansync;

import com.saicone.uclansync.core.ClanUpdater;
import com.saicone.uclansync.core.messenger.Messenger;
import com.saicone.uclansync.module.Locale;
import com.saicone.uclansync.module.SettingsFile;
import com.saicone.uclansync.module.command.CommandLoader;
import com.saicone.uclansync.module.library.LibraryLoader;
import com.saicone.uclansync.module.listener.BukkitListener;
import com.saicone.uclansync.util.Proxy;
import me.ulrich.clans.Clans;
import me.ulrich.clans.data.Addon;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class UClanSync extends Addon {

    private static UClanSync instance;
    private static Clans clans;
    public static final SettingsFile SETTINGS = new SettingsFile("settings.yml");

    public static UClanSync get() {
        return instance;
    }

    public static Clans getClans() {
        return clans;
    }

    private LibraryLoader libraryLoader = null;
    private ClanUpdater clanUpdater = null;
    private BukkitListener listener = null;
    private CommandLoader commandLoader = null;

    public UClanSync() {
        instance = this;
    }

    public void onLoad() {
        setEnabled(true);
        Bukkit.getConsoleSender().sendMessage("[UClanSync] Loading UClanSync addon...");
        SETTINGS.load(this).onReload();
        Locale.INSTANCE.load(this);
        if (isEnabled()) {
            libraryLoader = new LibraryLoader(
                    new File(getAddonDataFolder(), "libs"),
                    getMessengers("rabbitmq", "redis"),
                    getClassLoader());
            if (libraryLoader.load(SETTINGS.getConfigurationSection("Libraries.Redis"))) {
                libraryLoader.init("com.minelatino.uclansync.core.messenger.type.RedisMessenger");
            } else {
                Locale.log(2, "Redis messenger will not be loaded");
            }
            if (libraryLoader.load(SETTINGS.getConfigurationSection("Libraries.RabbitMQ"))) {
                libraryLoader.init("com.minelatino.uclansync.core.messenger.type.RabbitMQMessenger");
            } else {
                Locale.log(2, "RabbitMQ messenger will not be loaded");
            }
        } else {
            return;
        }
        Locale.log(3, "Successfully loaded " + Messenger.PROVIDERS.size() + " messenger types: " + String.join(", ", Messenger.PROVIDERS.keySet()));
    }

    @Override
    public void onEnable() {
        onLoad();
        if (!isEnabled()) {
            onDisable();
            return;
        }
        clans = (Clans) getInstance();
        Bukkit.getConsoleSender().sendMessage("[UClanSync] Enabling UClanSync addon...");
        getClans().setMultiServer(true);
        getClans().setMultiName(SETTINGS.getString("Server.Name", "null"));
        Proxy.init();
        clanUpdater = new ClanUpdater();
        clanUpdater.onEnable();
        listener = new BukkitListener();
        listener.onEnable();
        commandLoader = new CommandLoader();
        commandLoader.onEnable();
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("[UClanSync] Disabling UClanSync addon...");
        if (commandLoader != null) {
            commandLoader.onDisable();
        }
        if (listener != null) {
            listener.onDisable();
        }
        if (clanUpdater != null) {
            clanUpdater.onDisable();
        }
        if (libraryLoader != null) {
            libraryLoader.close();
        }
    }

    public void onReload() {
        Bukkit.getConsoleSender().sendMessage("[UClanSync] Reloading UClanSync addon...");
        SETTINGS.onReload();
        Locale.INSTANCE.onReload();
        ((Clans) getInstance()).setMultiName(SETTINGS.getString("Server.Name", "null"));
        clanUpdater.onReload();
        commandLoader.onReload();
    }

    public ClanUpdater getClanUpdater() {
        return clanUpdater;
    }

    private URL[] getMessengers(String... names) {
        List<URL> urls = new ArrayList<>();
        for (String name : names) {
            URL url = getMessenger(name);
            if (url != null) {
                urls.add(url);
            }
        }
        return urls.toArray(new URL[0]);
    }

    private URL getMessenger(String name) {
        URL url = getClassLoader().getResource(name + "-messenger.compiled");
        if (url == null) {
            return null;
        }

        Path path;
        try {
            path = Files.createTempFile("uclansync-" + name + "-messenger", ".jar.tmp");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        path.toFile().deleteOnExit();

        try (InputStream in = url.openStream()) {
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        try {
            return path.toUri().toURL();
        } catch (Exception e) {
            return null;
        }
    }
}
