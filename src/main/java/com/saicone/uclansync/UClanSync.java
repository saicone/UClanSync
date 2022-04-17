package com.saicone.uclansync;

import com.saicone.uclansync.core.ClanUpdater;
import com.saicone.uclansync.core.messenger.Messenger;
import com.saicone.uclansync.core.messenger.type.RabbitMQMessenger;
import com.saicone.uclansync.core.messenger.type.RedisMessenger;
import com.saicone.uclansync.module.Locale;
import com.saicone.uclansync.module.SettingsFile;
import com.saicone.uclansync.module.command.CommandLoader;
import com.saicone.uclansync.module.LibraryLoader;
import com.saicone.uclansync.module.listener.BukkitListener;
import com.saicone.uclansync.util.Proxy;
import me.ulrich.clans.Clans;
import me.ulrich.clans.data.Addon;
import me.ulrich.clans.data.ClanEnum;
import org.bukkit.Bukkit;

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
            libraryLoader = new LibraryLoader();
            libraryLoader.load();
            try {
                Messenger.PROVIDERS.put("redis", RedisMessenger.class);
            } catch (Throwable t) {
                Messenger.PROVIDERS.remove("redis");
            }
            try {
                Messenger.PROVIDERS.put("rabbitmq", RabbitMQMessenger.class);
            } catch (Throwable t) {
                Messenger.PROVIDERS.remove("rabbitmq");
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
        getClans().setMultiMode(ClanEnum.MultiserverMode.BUNGEECORD);
        getClans().getClanAPI().setProxieds("{}");
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
}
