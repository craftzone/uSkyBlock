package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

public class ToggleWarp extends RequireIslandCommand {
    public ToggleWarp(uSkyBlock plugin) {
        super(plugin, "togglewarp|tw", "usb.extra.addwarp", "enable/disable interdire ou autoris� les joueurs de visiter le warp de votre �le.");
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (island.hasPerm(player, "canToggleWarp")) {
            if (!island.hasWarp()) {
                if (island.isLocked()) {
                    player.sendMessage(tr("\u00a74Votre �le est verrouill�. Vous devez la d�verrouiller avant d'activer le warp."));
                    return true;
                }
                island.sendMessageToIslandGroup("\u00a7b" +player.getName() + "\u00a7d a activ� le warp de son �le.");
                island.setWarpActive(true);
            } else {
                island.sendMessageToIslandGroup("\u00a7b" +player.getName() + "\u00a7d a d�sactiv� le warp de son �le.");
                island.setWarpActive(false);
            }
        } else {
            player.sendMessage(tr("\u00a7cVous n'avez pas la permission pour activ�/disactiv� le warp de votre �le!"));
        }
        return true;
    }
}
