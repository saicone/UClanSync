package com.saicone.uclansync.module;

import com.saicone.uclansync.UClanSync;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;

public class MainCommand extends Command {

    public MainCommand(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length >= 1) {
            if (args[0].equals("reload")) {
                long time = System.currentTimeMillis();
                UClanSync.get().onReload();
                sender.sendMessage(new ComponentBuilder("Plugin reloaded successfully ").color(ChatColor.GREEN)
                        .append((System.currentTimeMillis() - time) + "ms").color(ChatColor.WHITE).create());
                return;
            }
        }
        sender.sendMessage(new ComponentBuilder("/" + getName() + " [help/reload]").color(ChatColor.YELLOW).create());
    }
}
