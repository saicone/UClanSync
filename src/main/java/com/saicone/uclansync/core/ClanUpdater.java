package com.saicone.uclansync.core;

import com.google.gson.JsonObject;
import com.saicone.uclansync.UClanSync;
import com.saicone.uclansync.core.messenger.Messenger;
import com.saicone.uclansync.module.Locale;
import me.ulrich.clans.data.ClanData;
import me.ulrich.clans.events.ClanAddonEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class ClanUpdater {

    private Messenger messenger = null;
    private final PlayerUpdateTask playerUpdate;

    private final List<UUID> toUpdate = new ArrayList<>();
    private final Map<String, Location> locations = new HashMap<>();
    private String type = "PROXY";

    public ClanUpdater() {
        playerUpdate = new PlayerUpdateTask(this);
    }

    private void newMessenger(String type) {
        this.type = type;
        messenger = Messenger.of(type, (msg) -> Bukkit.getScheduler().runTaskAsynchronously(UClanSync.getClans(), () -> getMessage(msg)));
        if (messenger != null) {
            messenger.onEnable();
        }
    }

    public void onEnable() {
        newMessenger(UClanSync.SETTINGS.getString("Messenger.Type", "PROXY"));
        playerUpdate.start();
    }

    public void onDisable() {
        if (messenger != null) {
            messenger.onDisable();
            messenger = null;
        }
        playerUpdate.stop();
    }

    public void onReload() {
        String type = UClanSync.SETTINGS.getString("Messenger.Type", "PROXY");
        if (messenger != null) {
            if (!this.type.equals(type)) {
                messenger.onDisable();
            } else {
                messenger.onReload();
                return;
            }
        }
        newMessenger(type);
    }

    public void inviteClan(String sender, String receiver, boolean ally) {
        sendMessage("INVITECLAN", sender, receiver, String.valueOf(ally));
    }

    public void invitePlayer(String player, String clan, long expires) {
        sendMessage("INVITEPLAYER", player, clan, String.valueOf(expires));
    }

    public void updateClan(String id) {
        updateClan(id, false);
    }

    public void updateClan(String id, boolean newClan) {
        sendMessage("UPDATECLAN", id, String.valueOf(newClan));
    }

    public void updateChest(String clan) {
        sendMessage("UPDATECHEST", clan);
    }

    public void teleport(String player, String server, String location) {
        sendMessage("TELEPORT", player, server, location);
    }

    public void chat(String player, String clan, String message) {
        sendMessage("CHAT", player, clan, message);
    }

    public void players() {
        StringBuilder builder = new StringBuilder();
        for (Player player : Bukkit.getOnlinePlayers()) {
            builder.append(";").append(player.getName()).append(",").append(player.getUniqueId());
        }
        if (builder.length() > 0) {
            sendMessage("PLAYERS", builder.substring(1));
        }
    }

    public void ping() {
        sendMessage("PING", UClanSync.getClans().getMultiName());
    }

    public void sendMessage(String id, String... args) {
        sendMessage(id + (args.length > 1 ? ":" + args.length : "") + "=" + String.join("_|||_", args));
    }

    public void sendMessage(String msg) {
        if (messenger != null) {
            Locale.log(4, "Sent message: " + msg);
            Bukkit.getScheduler().runTaskAsynchronously(UClanSync.getClans(), () -> messenger.send(msg));
        }
    }

    public Location getTeleport(String player) {
        return locations.get(player);
    }

    public void getMessage(String msg) {
        Locale.log(4, "Received message: " + msg);
        String[] split = msg.split("=", 2);
        if (split.length < 2) {
            return;
        }
        String[] type = split[0].split(":");
        if (type.length < 1) {
            return;
        }
        String id = type[0];
        int length = type.length > 1 ? Integer.parseInt(type[1]) : 1;

        String[] args;
        if (length <= 1) {
            args = new String[] {split[1]};
        } else {
            args = split[1].split("_\\|\\|\\|_", length);
            if (args.length != length) {
                return;
            }
        }
        process(id, args);
    }

    private void process(String id, String[] args) {
        switch (id.toUpperCase()) {
            case "INVITECLAN":
                processInviteClan(UUID.fromString(args[0]), UUID.fromString(args[1]), args[2].equalsIgnoreCase("true"));
                return;
            case "INVITEPLAYER":
                processInvitePlayer(UUID.fromString(args[0]), UUID.fromString(args[1]), Long.parseLong(args[2]));
                return;
            case "UPDATECLAN":
                processUpdateClan(UUID.fromString(args[0]), args[1].equalsIgnoreCase("true"));
                return;
            case "UPDATECHEST":
                processUpdateChest(UUID.fromString(args[0]));
                return;
            case "TELEPORT":
                processTeleport(args[0], args[1], args[2]);
                return;
            case "CHAT":
                processChat(UUID.fromString(args[0]), UUID.fromString(args[1]), args[2]);
                return;
            case "PLAYERS":
                // Avoid blank array
                if (args[0].contains(";")) {
                    for (String s : args[0].split(";")) {
                        playerUpdate.append(s);
                    }
                } else {
                    playerUpdate.append(args[0]);
                }
                return;
            case "PING":
                Bukkit.getConsoleSender().sendMessage("Ping from: " + args[0]);
                sendMessage("PONG", args[0], UClanSync.getClans().getMultiName());
                return;
            case "PONG":
                if (UClanSync.getClans().getMultiName().equals(args[0])) {
                    Bukkit.getConsoleSender().sendMessage("Pong from: " + args[1]);
                }
                return;
            default:
                Locale.log(2, "Unknown action ID received from messenger: " + id);
        }
    }

    private void processInviteClan(UUID sender, UUID receiver, boolean ally) {
        if (ally) {
            UClanSync.getClans().getClanAPI().allySend(sender, receiver, false);
        } else {
            UClanSync.getClans().getClanAPI().rivalRemoveSend(sender, receiver, false);
        }
    }

    private void processInvitePlayer(UUID player, UUID clanID, long expires) {
        if (Bukkit.getPlayer(player) != null) {
            if (UClanSync.getClans().getClanAPI().clanExists(clanID)) {
                ClanData clan = UClanSync.getClans().getClanAPI().getClan(clanID);
                // Invite can't be async
                Bukkit.getScheduler().runTask(UClanSync.getClans(), () -> UClanSync.getClans().getPlayerAPI().inviteToClan(clan.getLeader(), player));
            }
        }
    }

    private void processUpdateClan(UUID clanID, boolean newClan) {
        if (toUpdate.contains(clanID)) {
            return;
        }
        toUpdate.add(clanID);
        Bukkit.getScheduler().runTaskLaterAsynchronously(UClanSync.getClans(), () -> {
            toUpdate.remove(clanID);
            // Avoid bugs about clan creation
            if (newClan) {
                UClanSync.getClans().getClanAPI().reloadClanData(clanID);
                processUpdateMembers(clanID);
            } else {
                processUpdateMembers(clanID);
                UClanSync.getClans().getClanAPI().reloadClanData(clanID);
            }
        }, UClanSync.SETTINGS.getInt("Addon.ClanUpdater.Update-Delay", 40));
    }

    private void processUpdateMembers(UUID clanID) {
        if (UClanSync.getClans().getClanAPI().clanExists(clanID)) {
            ClanData clan = UClanSync.getClans().getClanAPI().getClan(clanID);
            clan.getMembers().iterator().forEachRemaining((player) -> UClanSync.getClans().getPlayerAPI().loadPlayerData(player));
        }
    }

    private void processUpdateChest(UUID clanID) {
        JsonObject json = new JsonObject();
        json.addProperty("addon", "CLANCHEST");
        json.addProperty("action", "UPDATE");
        json.addProperty("uuid", clanID.toString());
        ClanAddonEvent event = new ClanAddonEvent(clanID, json.getAsString());
        Bukkit.getScheduler().runTask(UClanSync.getClans(), () -> Bukkit.getPluginManager().callEvent(event));
    }

    private void processTeleport(String playerID, String server, String location) {
        if (!UClanSync.SETTINGS.getBoolean("Addon.Feature.Homes", true)) {
            return;
        }
        if (server.equals(UClanSync.getClans().getMultiName())) {
            String[] split = location.split("~!~");
            World world = Bukkit.getWorld(split[0]);
            if (world != null) {
                double x = Double.parseDouble(split[1]);
                double y = Double.parseDouble(split[2]);
                double z = Double.parseDouble(split[3]);
                float yaw = Float.parseFloat(split[4]);
                float pitch = Float.parseFloat(split[5]);
                Location loc = new Location(world, x, y, z, yaw, pitch);
                Player player = Bukkit.getPlayer(UUID.fromString(playerID));
                if (player != null && player.isOnline()) {
                    player.teleport(loc);
                } else {
                    locations.put(playerID, loc);
                    Bukkit.getScheduler().runTaskLaterAsynchronously(UClanSync.getClans(), () -> locations.remove(playerID),
                            UClanSync.SETTINGS.getInt("Addon.ClanUpdater.Teleport-Cache", 200));
                }
            }
        }
    }

    private void processChat(UUID player, UUID clan, String message) {
        if (UClanSync.SETTINGS.getBoolean("Addon.Feature.Chat", true)) {
            if (UClanSync.getClans().getClanAPI().clanExists(clan)) {
                UClanSync.getClans().getClanAPI().clanChatSendOffline(player, message);
            }
        }
    }
}
