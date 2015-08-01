package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LocationUtil;

import java.util.Map;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

public class CreateCommand extends RequirePlayerCommand {
    private final uSkyBlock plugin;

    public CreateCommand(uSkyBlock plugin) {
        super("create|c", "usb.island.create", "cr�er une �le");
        this.plugin = plugin;
    }

    @Override
    protected boolean doExecute(String alias, Player player, Map<String, Object> data, String... args) {
        PlayerInfo pi = plugin.getPlayerInfo(player);
        if (LocationUtil.isEmptyLocation(pi.getIslandLocation()) && !plugin.onRestartCooldown(player)) {
            plugin.createIsland(player, pi);
        } else if (pi.getHasIsland()) {
            IslandInfo island = plugin.getIslandInfo(pi);
            if (island.isLeader(player)) {
                player.sendMessage(tr("\u00a74�le trouv�!" +
                        "\u00a7e Vous avez d�j� une �le. Si vous voulez restart, Tapez" +
                        "\u00a7b /is restart\u00a7e pour avoir une nouvelle �le."));
            } else {
                player.sendMessage(tr("\u00a74Island found!" +
                        "\u00a7e Vous �tes d�j� membre d'une �le. Pour cr�e votre propre �le, " +
                        "\u00a7equittez d'abord avec \u00a7b /is leave"));
            }
        } else {
            player.sendMessage(tr("\u00a7eVous pouvez cr�er une nouvelle �le {0,number,#} seconds.", plugin.getRestartCooldownTime(player) / 1000L));
        }
        return true;
    }
}
