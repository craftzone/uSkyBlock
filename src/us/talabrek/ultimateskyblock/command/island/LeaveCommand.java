package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

public class LeaveCommand extends RequireIslandCommand {
    public LeaveCommand(uSkyBlock plugin) {
        super(plugin, "leave", "usb.party.join", "quittez le groupe d'île");
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (player.getWorld().getName().equalsIgnoreCase(uSkyBlock.getSkyBlockWorld().getName())) {
            if (!island.isParty()) {
                player.sendMessage(tr("\u00a74Vous ne pouvez pas quitter votre île si vous êtes la seule personne. tapez /island restart si vous voulez une nouvelle île!"));
                return true;
            }
            if (island.isLeader(player)) {
                player.sendMessage(tr("\u00a7eVous possédez cette île, tapez plutôt /island remove <joueur>"));
                return true;
            }
            player.getInventory().clear();
            player.getEquipment().clear();
            plugin.spawnTeleport(player);
            island.removeMember(pi);
            player.sendMessage(tr("\u00a7eVous avez quitté l'île et tp au spawn."));
            if (Bukkit.getPlayer(island.getLeader()) != null) {
                Bukkit.getPlayer(island.getLeader()).sendMessage(tr("\u00a74{0} a quitter votre île!", player.getName()));
            }
            return true;
        } else {
            player.sendMessage(tr("\u00a74Vous devez être dans le monde skyblock de quitter votre groupe (party)!"));
            return true;
        }
    }
}
