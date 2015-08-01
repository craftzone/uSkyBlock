package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.handler.VaultHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

public class LockUnlockCommand extends RequireIslandCommand {
    public LockUnlockCommand(uSkyBlock plugin) {
        super(plugin, "lock|unlock", "usb.lock", "verrouill�/d�verrouill� votre �le");
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (alias.equalsIgnoreCase("lock") && pi.getHasIsland()) {
            if (Settings.island_allowIslandLock && VaultHandler.checkPerk(player.getName(), "usb.lock", player.getWorld())) {
                if (island.hasPerm(player, "canToggleLock")) {
                    island.lock(player);
                } else {
                    player.sendMessage(tr("\u00a74Vous n'avez pas la permission de verrouill� votre �le!"));
                }
            } else {
                player.sendMessage(tr("\u00a74Vous n'avez pas acc�s � cette commande!"));
            }
            return true;
        }
        if (alias.equalsIgnoreCase("unlock") && pi.getHasIsland()) {
            if (Settings.island_allowIslandLock && VaultHandler.checkPerk(player.getName(), "usb.lock", player.getWorld())) {
                if (island.hasPerm(player, "canToggleLock")) {
                    island.unlock(player);
                } else {
                    player.sendMessage(tr("\u00a74Vous n'avez pas la permission de d�verrouill� votre �le!"));
                }
            } else {
                player.sendMessage(tr("\u00a74Vous n'avez pas acc�s � cette commande!"));
            }
            return true;
        }
        return false;
    }
}
