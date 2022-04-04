package com.saicone.uclansync.module.command;

import com.saicone.uclansync.UClanSync;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class CommandLoader {

    private static final CommandMap commandMap;
    private static final MethodHandle getCommands;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        CommandMap map = null;
        MethodHandle m1 = null;
        try {
            Field mapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            mapField.setAccessible(true);

            map = (CommandMap) mapField.get(Bukkit.getServer());
            Class<?> mapClass = map.getClass();
            if (mapClass.getSimpleName().equals("CraftCommandMap")) {
                mapClass = mapClass.getSuperclass();
            }

            Field commands = mapClass.getDeclaredField("knownCommands");
            commands.setAccessible(true);
            m1 = lookup.unreflectGetter(commands);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        commandMap = map;
        getCommands = m1;
    }

    public static CommandMap getCommandMap() {
        return commandMap;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Command> getCommands() {
        try {
            return (Map<String, Command>) getCommands.invoke(commandMap);
        } catch (Throwable t) {
            throw new NullPointerException(t.getMessage());
        }
    }

    private final MainCommand command;

    public CommandLoader() {
        command = new MainCommand();
    }

    public String getName() {
        return UClanSync.SETTINGS.getString("Command.Name", "uclansync");
    }

    public String getPermission() {
        return UClanSync.SETTINGS.getString("Command.Permission", "uclansync.use");
    }

    public List<String> getAliases() {
        return UClanSync.SETTINGS.getStringList("Command.Aliases");
    }

    private void loadChanges() {
        loadChanges(getName(), getPermission(), getAliases());
    }

    private void loadChanges(String name, String permission, List<String> aliases) {
        command.setName(name);
        command.setPermission(permission);
        command.setAliases(aliases);
    }

    public void onEnable() {
        loadChanges();
        Map<String, Command> commands = getCommands();
        commands.put(command.getName(), command);
        for (String alias : command.getAliases()) {
            commands.put(alias, command);
        }
        command.register(getCommandMap());
    }

    public void onDisable() {
        if (command.isRegistered()) {
            Map<String, Command> commands = getCommands();
            commands.remove(command.getName());
            for (String alias : command.getAliases()) {
                if (commands.containsKey(alias) && commands.get(alias).getName().contentEquals(command.getName())) {
                    commands.remove(alias);
                }
            }
            command.unregister(getCommandMap());
        }
    }

    public void onReload() {
        Map<String, Command> commands = getCommands();

        String name = getName();
        if (!command.getName().equalsIgnoreCase(name)) {
            commands.remove(command.getName());
            command.setName(name);
            commands.put(name, command);
        }

        command.setPermission(getPermission());

        List<String> aliases = getAliases();
        for (String alias : command.getAliases()) {
            if (!aliases.contains(alias) && commands.get(alias).getName().contentEquals(command.getName())) {
                commands.remove(alias);
            }
        }

        for (String alias : aliases) {
            if (!command.getAliases().contains(alias)) {
                commands.put(alias, command);
            }
        }
        command.setAliases(aliases);
    }
}
