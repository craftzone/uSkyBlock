package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.command.InviteHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

public class InviteCommand extends RequireIslandCommand {
    private final InviteHandler inviteHandler;

    public InviteCommand(uSkyBlock plugin, InviteHandler inviteHandler) {
        super(plugin, "invite", "usb.party.create", "oplayer", "inviter un joueur à votre île.");
        this.inviteHandler = inviteHandler;
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (args.length == 0) {
            player.sendMessage(tr("\u00a7eTapez\u00a7f /island invite <joueur>\u00a7e pour inviter un joueur à votre île."));
            if (!island.isParty()) {
                return true;
            }
            if (!island.isLeader(player) || !island.hasPerm(player, "canInviteOthers")) {
                player.sendMessage(tr("\u00a74Seul le propriétaire de l'île peut inviter!"));
                return true;
            }
            int diff = island.getMaxPartySize() - island.getPartySize();
            if (diff > 0) {
                player.sendMessage(tr("\u00a7aVous pouvez inviter encore {0} autres joueurs.", diff));
            } else {
                player.sendMessage(tr("\u00a74Vous ne pouvez plus inviter d'autres joueurs."));
            }
        }
        if (args.length == 1) {
            Player otherPlayer = Bukkit.getPlayer(args[0]);
            if (!island.hasPerm(player, "canInviteOthers")) {
                player.sendMessage(tr("\u00a74Vous n'avez pas la permission d'inviter d'autres à cette île!"));
                return true;
            }
            if (otherPlayer == null || !otherPlayer.isOnline()) {
                player.sendMessage(tr("\u00a74Ce joueur est déconnecté ou ne existe pas."));
                return true;
            }
            if (player.getName().equalsIgnoreCase(otherPlayer.getName())) {
                player.sendMessage(tr("\u00a74You can't invite yourself!"));
                return true;
            }
            if (island.isLeader(otherPlayer)) {
                player.sendMessage(tr("\u00a74Ce joueur est le leader de votre île!"));
                return true;
            }
            if (inviteHandler.invite(player, island, otherPlayer)) {
                island.sendMessageToIslandGroup(tr("{0}\u00a7d a invité {1}", player.getDisplayName(), otherPlayer.getDisplayName()));
            }
        }
        return true;
    }
}
