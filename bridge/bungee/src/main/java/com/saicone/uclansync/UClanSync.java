package com.saicone.uclansync;

import com.saicone.uclansync.module.BungeeListener;
import com.saicone.uclansync.module.MainCommand;
import com.saicone.uclansync.module.SettingsFile;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.Arrays;

public class UClanSync extends Plugin {

    private static UClanSync instance;
    public static final SettingsFile SETTINGS = new SettingsFile("settings.yml");

    public static UClanSync get() {
        return instance;
    }

    private final BungeeListener listener;
    private MainCommand command = null;

    public UClanSync() {
        instance = this;
        listener = new BungeeListener(this);
    }

    @Override
    public void onLoad() {
        SETTINGS.load(this).onReload();
    }

    @Override
    public void onEnable() {
        listener.onEnable();
        reloadCommand();
    }

    @Override
    public void onDisable() {
        listener.onDisable();
    }

    public void onReload() {
        SETTINGS.onReload();
        listener.onReload();
        reloadCommand();
    }

    private void reloadCommand() {
        String name = SETTINGS.getConfig().getString("Command.name", "uclansyncbungee");
        String permission = SETTINGS.getConfig().getString("Command.permission", "uclansync.use");
        String[] aliases = SETTINGS.getConfig().getStringList("Command.aliases").toArray(new String[0]);
        if (command != null) {
            if (!command.getName().equalsIgnoreCase(name) || !command.getPermission().equalsIgnoreCase(permission) || Arrays.equals(command.getAliases(), aliases)) {
                getProxy().getPluginManager().unregisterCommand(command);
            }
        }
        command = new MainCommand(name, permission, aliases);
        getProxy().getPluginManager().registerCommand(this, command);
    }
}
