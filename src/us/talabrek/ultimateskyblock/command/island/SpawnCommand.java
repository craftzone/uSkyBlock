package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

/**
 * Convernience to get to spawn.
 */
public class SpawnCommand extends RequirePlayerCommand {
    private final uSkyBlock plugin;

    public SpawnCommand(uSkyBlock plugin) {
        super("spawn", null, "se teleporter au spawn");
        this.plugin = plugin;
    }

    @Override
    protected boolean doExecute(String alias, Player player, Map<String, Object> data, String... args) {
        plugin.spawnTeleport(player);
        return true;
    }
}
