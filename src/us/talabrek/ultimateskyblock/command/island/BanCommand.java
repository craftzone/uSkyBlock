package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

public class BanCommand extends RequireIslandCommand {
    public BanCommand(uSkyBlock plugin) {
        super(plugin, "ban|unban", "usb.island.ban", "player", "ban/unban un joueur de warp sur votre île.");
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (args.length == 0) {
            player.sendMessage(tr("\u00a7eLes joueurs suivants sont interdits de se warp sur votre île:"));
            player.sendMessage(tr("\u00a74{0}", island.getBans()));
            player.sendMessage(tr("\u00a7epour interdir/autoriser de warp sur votre île, utiliser /island ban <joueur>"));
            return true;
        } else if (args.length == 1) {
            String name = args[0];
            if (island.getMembers().contains(name)) {
                player.sendMessage(tr("\u00a74Vous ne pouvez pas interdire des membres. Retirez-les du groupe d'abord!"));
                return true;
            }
            if (!island.hasPerm(player.getName(), "canKickOthers")) {
                player.sendMessage(tr("\u00a74Vous n'avez pas la permission de kick/ban les joueurs de cette île!"));
                return true;
            }
            if (!island.isBanned(name)) {
                island.banPlayer(name);
                player.sendMessage(tr("\u00a7eVous avez interdit \u00a74{0}\u00a7e de warp sur votre île.", name));
            } else {
                island.unbanPlayer(name);
                player.sendMessage(tr("\u00a7eVous avez autoriser \u00a7a{0}\u00a7e de warp sur votre île.", name));
            }
            return true;
        }
        return false;
    }
}
