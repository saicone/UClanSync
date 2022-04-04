package com.saicone.uclansync.module.command;

import com.saicone.uclansync.UClanSync;
import com.saicone.uclansync.module.Locale;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class MainCommand extends Command {

    private static final List<String> TAB = Arrays.asList("help", "ping", "reload");

    MainCommand() {
        super("uclansync");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String cmd, @NotNull String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                long time = System.currentTimeMillis();
                UClanSync.get().onReload();
                Locale.sendTo(sender, "Command.Reload", (System.currentTimeMillis() - time));
                return true;
            } else if (args[0].equalsIgnoreCase("ping")) {
                UClanSync.get().getClanUpdater().ping();
                return true;
            }
        }
        Locale.sendTo(sender, "Command.Help", cmd);
        return true;
    }

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args, @Nullable Location location) throws IllegalArgumentException {
        return TAB;
    }

    @Override
    public boolean testPermission(@NotNull CommandSender target) {
        if (testPermissionSilent(target)) {
            return true;
        } else {
            Locale.sendTo(target, "Command.NoPerm");
            return false;
        }
    }
}