package us.talabrek.ultimateskyblock.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.*;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

/**
 * Responsible for holding out-standing invites, and carrying out a transfer of invitation.
 */
@SuppressWarnings("deprecation")
public class InviteHandler {
    private final Map<UUID, Invite> inviteMap = new HashMap<>();
    private final Map<String, Set<UUID>> waitingInvites = new HashMap<>();
    private final uSkyBlock plugin;

    public InviteHandler(uSkyBlock plugin) {
        this.plugin = plugin;
    }

    public synchronized boolean invite(Player player, final IslandInfo island, Player otherPlayer) {
        PlayerInfo oPi = plugin.getPlayerInfo(otherPlayer);
        Set<UUID> invites = waitingInvites.get(island.getName());
        if (invites == null) {
            invites = new HashSet<>();
        }
        if (island.getPartySize() + invites.size() >= island.getMaxPartySize()) {
            player.sendMessage(tr("\u00a74Le nombre total de membres autorisé est atteint pour votre île, ou vous avez trop de invitations en attentes. Vous ne pouvez plus inviter d'autres joueurs pour l'instant."));
            return false;
        }
        if (oPi.getHasIsland()) {
            IslandInfo oIsland = plugin.getIslandInfo(oPi);
            if (oIsland.isParty() && oIsland.isLeader(otherPlayer)) {
                player.sendMessage(tr("\u00a74Ce joueur est déjà d'une autre île."));
                otherPlayer.sendMessage(tr("\u00a7e{0}\u00a7e essaye de vous inviter, mais vous êtes déjà dans sur un groupe d'île.", player.getDisplayName()));
                return false;
            }
        }
        final UUID uniqueId = otherPlayer.getUniqueId();
        invites.add(uniqueId);
        final Invite invite = new Invite(island.getName(), uniqueId, player.getDisplayName());
        inviteMap.put(uniqueId, invite);
        waitingInvites.put(island.getName(), invites);
        player.sendMessage("\u00a7aInvitation envoyé à " + otherPlayer.getDisplayName());
        otherPlayer.sendMessage(new String[]{
                tr("{0}\u00a7e vous a invité à rejoindre son île!", player.getDisplayName()),
                tr("\u00a7f/island [accept/reject]\u00a7e pour accepter ou refuser l'invitation."),
                tr("\u00a74ATTENTION: Vous allez perdre votre île actuelle si vous acceptez!")
        });
        player.getDisplayName();
        int timeout = plugin.getConfig().getInt("options.party.invite-timeout", 100);
        BukkitTask timeoutTask = plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                uninvite(island, uniqueId);
            }
        }, timeout);
        invite.setTimeoutTask(timeoutTask);
        return true;
    }

    public synchronized boolean reject(Player player) {
        Invite invite = inviteMap.remove(player.getUniqueId());
        if (invite != null) {
            if (invite.getTimeoutTask() != null) {
                invite.getTimeoutTask().cancel();
            }
            IslandInfo island = plugin.getIslandInfo(invite.getIslandName());
            if (island != null) {
                island.sendMessageToIslandGroup(tr("{0}\u00a7e a rejeté l'invitation.", player.getDisplayName()));
            }
            if (waitingInvites.containsKey(invite.getIslandName())) {
                waitingInvites.get(invite.getIslandName()).remove(player.getUniqueId());
            }
            return true;
        }
        return false;
    }

    public synchronized boolean accept(final Player player) {
        UUID uuid = player.getUniqueId();
        IslandInfo oldIsland = plugin.getIslandInfo(player);
        if (oldIsland != null && oldIsland.isParty()) {
            player.sendMessage(tr("\u00a74Vous ne pouvez pas utiliser cette commande. Quittez le groupe votre île avant!"));
            return false;
        }
        Invite invite = inviteMap.remove(uuid);
        if (invite != null) {
            if (invite.getTimeoutTask() != null) {
                invite.getTimeoutTask().cancel();
            }
            PlayerInfo pi = plugin.getPlayerInfo(player);
            final IslandInfo island = plugin.getIslandInfo(invite.getIslandName());
            boolean deleteOldIsland = false;
            if (pi.getHasIsland() && pi.getIslandLocation() != null) {
                String islandName = WorldGuardHandler.getIslandNameAt(pi.getIslandLocation());
                deleteOldIsland = !island.getName().equals(islandName);
            }
            Set<UUID> uuids = waitingInvites.get(invite.getIslandName());
            uuids.remove(uuid);
            Runnable joinIsland = new Runnable() {
                @Override
                public void run() {
                    player.sendMessage(tr("\u00a7aVous avez rejoint un groupe d'île! Tapez /is voir les autres membres."));
                    // TODO: 29/12/2014 - R4zorax: Perhaps these steps should belong somewhere else?
                    addPlayerToParty(player, island);
                    plugin.setRestartCooldown(player);
                    plugin.homeTeleport(player);
                    plugin.clearPlayerInventory(player);
                    WorldGuardHandler.addPlayerToOldRegion(island.getName(), player.getName());
                }
            };
            if (deleteOldIsland) {
                plugin.deletePlayerIsland(player.getName(), joinIsland);
            } else {
                joinIsland.run();
            }
            return true;
        }
        return false;
    }

    public synchronized Set<UUID> getPendingInvites(IslandInfo island) {
        return waitingInvites.get(island.getName());
    }

    public boolean addPlayerToParty(final Player player, final IslandInfo island) {
        PlayerInfo playerInfo = plugin.getPlayerInfo(player);
        PlayerInfo leaderInfo = plugin.getPlayerInfo(island.getLeader());
        playerInfo.setJoinParty(leaderInfo.getIslandLocation());
        if (playerInfo != leaderInfo) { // Caching is done in sky, this should be safe...
            if (leaderInfo.getHomeLocation() != null) {
                playerInfo.setHomeLocation(leaderInfo.getHomeLocation());
            } else {
                playerInfo.setHomeLocation(leaderInfo.getIslandLocation());
            }
            island.setupPartyMember(player.getName());
        }
        playerInfo.save();
        island.sendMessageToIslandGroup(tr("\u00a7b{0}\u00a7d a rejoint votre groupe d'île.", player.getDisplayName()));
        return true;
    }

    public synchronized boolean uninvite(IslandInfo islandInfo, String playerName) {
        OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(playerName);
        if (offlinePlayer != null) {
            UUID uuid = offlinePlayer.getUniqueId();
            return uninvite(islandInfo, uuid);
        }
        return false;
    }

    private synchronized boolean uninvite(IslandInfo islandInfo, UUID uuid) {
        Set<UUID> invites = waitingInvites.get(islandInfo.getName());
        if (invites != null && invites.contains(uuid)) {
            Invite invite = inviteMap.remove(uuid);
            invites.remove(uuid);
            if (invite != null && invite.getTimeoutTask() != null) {
                invite.getTimeoutTask().cancel();
            }
            String msg = tr("\u00a7eInvitation pour \u00a7a{0}\u00a7e a expiré ou a été annulé.", invite.getDisplayName());
            islandInfo.sendMessageToIslandGroup(msg);
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(tr("\u00a7eInvitation pour \u00a7a{0}\u00a7e a expiré ou a été annulé.", islandInfo.getLeader()));
            }
            return true;
        }
        return false;
    }

    private static class Invite {
        private final String islandName;
        private final String displayName;
        private BukkitTask timeoutTask;

        public Invite(String islandName, UUID uniqueId, String displayName) {
            this.islandName = islandName;
            this.displayName = displayName;
            System.currentTimeMillis();
        }

        public String getIslandName() {
            return islandName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public BukkitTask getTimeoutTask() {
            return timeoutTask;
        }

        public void setTimeoutTask(BukkitTask timeoutTask) {
            this.timeoutTask = timeoutTask;
        }
    }
}
