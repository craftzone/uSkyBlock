package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;
import java.util.logging.Level;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

@SuppressWarnings("deprecation")
public class KickCommand extends RequireIslandCommand {
    public KickCommand(uSkyBlock plugin) {
        super(plugin, "kick|remove", "usb.party.kick", "player", "supprimer un membre de votre île.");
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (args.length == 1) {
            if (island == null || !island.hasPerm(player, "canKickOthers")) {
                player.sendMessage(tr("\u00a74Vous n'avez pas la permission de kick les autres de cette île!"));
                return true;
            }
            String playerName = args[0];
            Player otherPlayer = Bukkit.getPlayer(playerName);
            if (otherPlayer == null && Bukkit.getOfflinePlayer(playerName) == null) {
                player.sendMessage(tr("\u00a74le joueur n'existe pas!."));
                return true;
            }
            if (island.isLeader(playerName)) {
                player.sendMessage(tr("\u00a74Vous ne pouvez pas supprimer le leader de l'île!"));
                return true;
            }
            if (player.getName().equalsIgnoreCase(playerName)) {
                player.sendMessage(tr("\u00a74Stop kickin' yourself!"));
                return true;
            }
            if (island.getMembers().contains(playerName)) {
                if (otherPlayer != null) {
                    plugin.clearPlayerInventory(otherPlayer);
                    otherPlayer.sendMessage(tr("\u00a74" + player.getName() + " vous a kick de son île!"));
                    plugin.spawnTeleport(otherPlayer);
                }
                if (Bukkit.getPlayer(island.getLeader()) != null) {
                    Bukkit.getPlayer(island.getLeader()).sendMessage(tr("\u00a74{0} a été kick de l'île..", playerName));
                }
                island.removeMember(plugin.getPlayerInfo(playerName));
                uSkyBlock.log(Level.INFO, "Removing from " + island.getLeader() + "'s Island");
            } else if (otherPlayer != null && plugin.locationIsOnIsland(player, otherPlayer.getLocation())) {
                plugin.spawnTeleport(otherPlayer);
                otherPlayer.sendMessage(tr("\u00a74" + player.getName() + " vous a kick de son île!"));
                player.sendMessage(tr("\u00a74{0} a été kick de l'île.", playerName));
            } else {
                player.sendMessage(tr("\u00a74Ce joueur ne fait pas partie de votre groupe d'îles!"));
            }
            return true;
        }
        return false;
    }
}
