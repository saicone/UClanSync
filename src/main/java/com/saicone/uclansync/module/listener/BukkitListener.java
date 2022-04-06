package com.saicone.uclansync.module.listener;

import com.saicone.uclansync.UClanSync;
import com.saicone.uclansync.util.Proxy;
import me.ulrich.clans.events.ChestSortEvent;
import me.ulrich.clans.events.ClanAddonEvent;
import me.ulrich.clans.events.ClanAllyAddEvent;
import me.ulrich.clans.events.ClanAllyInviteEvent;
import me.ulrich.clans.events.ClanAllyRemoveEvent;
import me.ulrich.clans.events.ClanBankChangeEvent;
import me.ulrich.clans.events.ClanBannerChangeEvent;
import me.ulrich.clans.events.ClanChatEvent;
import me.ulrich.clans.events.ClanChestChangeEvent;
import me.ulrich.clans.events.ClanCreateEvent;
import me.ulrich.clans.events.ClanDeleteEvent;
import me.ulrich.clans.events.ClanExtraChestEvent;
import me.ulrich.clans.events.ClanFFChangeEvent;
import me.ulrich.clans.events.ClanGlobalEvent;
import me.ulrich.clans.events.ClanGlobalFriendlyFire;
import me.ulrich.clans.events.ClanHomeCreateEvent;
import me.ulrich.clans.events.ClanHomeDeleteEvent;
import me.ulrich.clans.events.ClanKDRChangeEvent;
import me.ulrich.clans.events.ClanLeaderChangeEvent;
import me.ulrich.clans.events.ClanLevelEvent;
import me.ulrich.clans.events.ClanModDescEvent;
import me.ulrich.clans.events.ClanModTagEvent;
import me.ulrich.clans.events.ClanPlayerInvitedEvent;
import me.ulrich.clans.events.ClanPlayerJoinEvent;
import me.ulrich.clans.events.ClanPlayerKDRChangeEvent;
import me.ulrich.clans.events.ClanPlayerLeaveEvent;
import me.ulrich.clans.events.ClanPlayerLoginEvent;
import me.ulrich.clans.events.ClanPlayerRoleChangeEvent;
import me.ulrich.clans.events.ClanPlayerTeleportEvent;
import me.ulrich.clans.events.ClanPointChangeEvent;
import me.ulrich.clans.events.ClanQuestChangeEvent;
import me.ulrich.clans.events.ClanRivalAddEvent;
import me.ulrich.clans.events.ClanRivalInviteEvent;
import me.ulrich.clans.events.ClanRivalRemoveEvent;
import me.ulrich.clans.events.ClanSettingsChangeEvent;
import me.ulrich.clans.events.ClanVerifyChangeEvent;
import me.ulrich.clans.events.ClanWarEndEvent;
import me.ulrich.clans.events.ClanWarStartEvent;
import me.ulrich.clans.events.ClanWarWinEvent;
import me.ulrich.clans.events.PlayerHologramEvent;
import me.ulrich.clans.events.PlayerHologramHideEvent;
import me.ulrich.clans.events.PlayerHologramShowEvent;
import me.ulrich.clans.events.StructurePaintEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class BukkitListener implements Listener {

    private void registerUpdater(UpdateExecutor<?>... executors) {
        for (UpdateExecutor<?> executor : executors) {
            executor.register(this);
        }
    }

    public void onEnable() {
        UClanSync.getClans().getServer().getPluginManager().registerEvents(this, UClanSync.getClans());
        registerUpdater(
                new UpdateExecutor<ClanAllyAddEvent>(ClanAllyAddEvent.getHandlerList(), (event) -> event.getClan2().toString()),
                new UpdateExecutor<ClanAllyRemoveEvent>(ClanAllyRemoveEvent.getHandlerList(), (event) -> event.getClan2().toString()),
                new UpdateExecutor<ClanBankChangeEvent>(ClanBankChangeEvent.getHandlerList(), (event) -> event.getClanID().toString()),
                new UpdateExecutor<ClanBannerChangeEvent>(ClanBannerChangeEvent.getHandlerList(), (event) -> event.getClanID().toString()),
                new UpdateExecutor<ClanDeleteEvent>(ClanDeleteEvent.getHandlerList(), (event) -> event.getClanID().toString()),
                new UpdateExecutor<ClanHomeCreateEvent>(ClanHomeCreateEvent.getHandlerList(), (event) -> event.getClanID().toString()),
                new UpdateExecutor<ClanHomeDeleteEvent>(ClanHomeDeleteEvent.getHandlerList(), (event) -> event.getClanID().toString()),
                new UpdateExecutor<ClanKDRChangeEvent>(ClanKDRChangeEvent.getHandlerList(), (event) -> event.getClanID().toString()),
                new UpdateExecutor<ClanLeaderChangeEvent>(ClanLeaderChangeEvent.getHandlerList(), (event) -> event.getClanID().toString()),
                new UpdateExecutor<ClanModDescEvent>(ClanModDescEvent.getHandlerList(), (event) -> event.getClanID().toString()),
                new UpdateExecutor<ClanModTagEvent>(ClanModTagEvent.getHandlerList(), (event) -> event.getClanID().toString()),
                new UpdateExecutor<ClanPlayerJoinEvent>(ClanPlayerJoinEvent.getHandlerList(), (event) -> event.getClanID().toString()),
                new UpdateExecutor<ClanPlayerKDRChangeEvent>(ClanPlayerKDRChangeEvent.getHandlerList(), (event) -> event.getClanID().toString()),
                new UpdateExecutor<ClanPlayerLeaveEvent>(ClanPlayerLeaveEvent.getHandlerList(), (event) -> event.getClanID().toString()),
                new UpdateExecutor<ClanPlayerRoleChangeEvent>(ClanPlayerRoleChangeEvent.getHandlerList(), (event) -> event.getClanID().toString()),
                new UpdateExecutor<ClanPointChangeEvent>(ClanPointChangeEvent.getHandlerList(), (event) -> event.getClanID().toString()),
                new UpdateExecutor<ClanRivalAddEvent>(ClanRivalAddEvent.getHandlerList(), (event) -> event.getClan2().toString()),
                new UpdateExecutor<ClanRivalRemoveEvent>(ClanRivalRemoveEvent.getHandlerList(), (event) -> event.getClan2().toString()),
                new UpdateExecutor<ClanSettingsChangeEvent>(ClanSettingsChangeEvent.getHandlerList(), (event) -> event.getClanID().toString()),
                new UpdateExecutor<ClanVerifyChangeEvent>(ClanVerifyChangeEvent.getHandlerList(), (event) -> event.getClanID().toString())
        );
    }

    public void onDisable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onAllyInvite(ClanAllyInviteEvent e) {
        UClanSync.get().getClanUpdater().inviteClan(e.getClan1().toString(), e.getClan2().toString(), true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(ClanChatEvent e) {
        if (UClanSync.SETTINGS.getBoolean("Addon.Feature.Chat", true)) {
            UClanSync.get().getClanUpdater().chat(e.getSender().toString(), e.getClanID().toString(), e.getMessage());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChestChange(ClanChestChangeEvent e) {
        UClanSync.get().getClanUpdater().updateChest(e.getClanID().toString());
    }

    @EventHandler(ignoreCancelled = true)
    public void onClanCreate(ClanCreateEvent e) {
        UClanSync.get().getClanUpdater().updateClan(e.getClanID().toString(), true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInvited(ClanPlayerInvitedEvent e) {
        UClanSync.get().getClanUpdater().invitePlayer(e.getPlayer().toString(), e.getClanID().toString(), e.getExpires());
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(ClanPlayerTeleportEvent e) {
        if (UClanSync.SETTINGS.getBoolean("Addon.Feature.Homes", true)) {
            Player player = Bukkit.getPlayer(e.getPlayerUUID());
            if (player != null) {
                Proxy.sendPlayer(player, e.getServer());
                UClanSync.get().getClanUpdater().teleport(e.getPlayerUUID().toString(), e.getServer(), e.getLocationEncoded());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onRivalInvite(ClanRivalInviteEvent e) {
        UClanSync.get().getClanUpdater().inviteClan(e.getClanInvite().toString(), e.getClanReceive().toString(), false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        Location loc = UClanSync.get().getClanUpdater().getTeleport(player.getUniqueId().toString());
        if (loc != null) {
            Bukkit.getScheduler().runTaskLater(UClanSync.get().getInstance(), () -> player.teleport(loc), 36L);
        }
    }

    public static class UpdateExecutor<T extends Event> implements EventExecutor {

        private final HandlerList handlerList;
        private final Function<T, String> function;

        public UpdateExecutor(HandlerList handlerList, Function<T, String> function) {
            this.handlerList = handlerList;
            this.function = function;
        }

        public void register(Listener listener) {
            handlerList.register(new RegisteredListener(listener, this, EventPriority.MONITOR, UClanSync.getClans(), true));
        }

        @SuppressWarnings("unchecked")
        @Override
        public void execute(@NotNull Listener listener, @NotNull Event event) {
            try {
                UClanSync.get().getClanUpdater().updateClan(function.apply((T) event));
            } catch (ClassCastException ignored) { }
        }
    }
}
