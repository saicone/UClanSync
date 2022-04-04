package com.saicone.uclansync.module;

import com.saicone.uclansync.UClanSync;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

public class MainCommand implements SimpleCommand {

    private final CommandMeta meta;
    private final String permission;

    public MainCommand(CommandMeta meta, String permission) {
        this.meta = meta;
        this.permission = permission;
    }

    public CommandMeta getMeta() {
        return meta;
    }

    public String getPermission() {
        return permission;
    }

    public boolean equals(String name, List<String> aliases, String permission) {
        if (this.permission.equals(permission)) {
            if (meta.getAliases().size() == (aliases.size() + 1)) {
                if (meta.getAliases().contains(name)) {
                    return meta.getAliases().containsAll(aliases) && aliases.containsAll(meta.getAliases());
                }
            }
        }
        return false;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                long time = System.currentTimeMillis();
                UClanSync.get().onReload();
                source.sendMessage(Component.text("Plugin reloaded successfully ").color(NamedTextColor.GREEN)
                        .append(Component.text((System.currentTimeMillis() - time) + "ms").color(NamedTextColor.WHITE)));
                return;
            }
        }
        source.sendMessage(Component.text("/" + invocation.alias() + " [help/reload]").color(NamedTextColor.YELLOW));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(getPermission());
    }
}
