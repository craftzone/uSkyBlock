package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

public class RestartCommand extends RequireIslandCommand {
    public RestartCommand(uSkyBlock plugin) {
        super(plugin, "restart|reset", "supprimer votre �le et commencez une nouvelle.");
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (island.getPartySize() > 1) {
            if (!island.isLeader(player)) {
                player.sendMessage(tr("\u00a74Seul le propri�taire peut restart cette �le. quittez cette �le afin de pouvoir cr�e la votre (/is leave)."));
            } else {
                player.sendMessage(tr("\u00a7eVous devez supprimer tous les joueurs de votre �le avant de pouvoir restart (/island kick <player>). pour voir la liste des joueurs actuellement partie de votre �le. /is party."));
            }
            return true;
        }
        if (!plugin.onRestartCooldown(player) || Settings.general_cooldownRestart == 0) {
            return plugin.restartPlayerIsland(player, pi.getIslandLocation());
        }
        player.sendMessage(tr("\u00a7eVous pouvez restart votre �le dans " + plugin.getRestartCooldownTime(player) / 1000L + " secondes."));
        return true;

    }
}
