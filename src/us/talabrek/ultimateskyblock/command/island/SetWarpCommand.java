package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

public class SetWarpCommand extends RequireIslandCommand {
    public SetWarpCommand(uSkyBlock plugin) {
        super(plugin, "setwarp|warpset", "usb.extra.addwarp", "définir la position du warp de votre île.");
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (island.hasPerm(player, "canChangeWarp")) {
            island.setWarpLocation(player.getLocation());
            island.sendMessageToIslandGroup(tr("\u00a7b"+ player.getName()  +"\u00a7d a changé la position du warp de l'île.", player.getName()));
        } else {
            player.sendMessage(tr("\u00a7cVous n'avez pas la permission de mettre le warp de votre île!"));
        }
        return true;
    }
}
