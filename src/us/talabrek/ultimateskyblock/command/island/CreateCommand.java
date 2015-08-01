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
        super("create|c", "usb.island.create", "créer une île");
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
                player.sendMessage(tr("\u00a74Île trouvé!" +
                        "\u00a7e Vous avez déjà une île. Si vous voulez restart, Tapez" +
                        "\u00a7b /is restart\u00a7e pour avoir une nouvelle île."));
            } else {
                player.sendMessage(tr("\u00a74Island found!" +
                        "\u00a7e Vous êtes déjà membre d'une île. Pour crée votre propre île, " +
                        "\u00a7equittez d'abord avec \u00a7b /is leave"));
            }
        } else {
            player.sendMessage(tr("\u00a7eVous pouvez créer une nouvelle île {0,number,#} seconds.", plugin.getRestartCooldownTime(player) / 1000L));
        }
        return true;
    }
}
