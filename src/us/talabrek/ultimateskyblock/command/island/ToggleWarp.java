package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

public class ToggleWarp extends RequireIslandCommand {
    public ToggleWarp(uSkyBlock plugin) {
        super(plugin, "togglewarp|tw", "usb.extra.addwarp", "enable/disable interdire ou autorisé les joueurs de visiter le warp de votre île.");
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (island.hasPerm(player, "canToggleWarp")) {
            if (!island.hasWarp()) {
                if (island.isLocked()) {
                    player.sendMessage(tr("\u00a74Votre île est verrouillé. Vous devez la déverrouiller avant d'activer le warp."));
                    return true;
                }
                island.sendMessageToIslandGroup("\u00a7b" +player.getName() + "\u00a7d a activé le warp de son île.");
                island.setWarpActive(true);
            } else {
                island.sendMessageToIslandGroup("\u00a7b" +player.getName() + "\u00a7d a désactivé le warp de son île.");
                island.setWarpActive(false);
            }
        } else {
            player.sendMessage(tr("\u00a7cVous n'avez pas la permission pour activé/disactivé le warp de votre île!"));
        }
        return true;
    }
}
