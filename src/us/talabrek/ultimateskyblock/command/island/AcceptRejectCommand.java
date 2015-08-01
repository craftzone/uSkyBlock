package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.command.InviteHandler;

import java.util.Map;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

public class AcceptRejectCommand extends RequirePlayerCommand {
    private final InviteHandler inviteHandler;

    public AcceptRejectCommand(InviteHandler inviteHandler) {
        super("accept|reject", "usb.party.join", "accept/reject une invitation.");
        this.inviteHandler = inviteHandler;
    }

    @Override
    protected boolean doExecute(String alias, Player player, Map<String, Object> data, String... args) {
        if (alias.equalsIgnoreCase("reject")) {
            if (inviteHandler.reject(player)) {
                player.sendMessage(tr("\u00a7eVous avez rejeté l'invitation à rejoindre un groupe d'île."));
            } else {
                player.sendMessage(tr("\u00a74Vous n'avez pas d'invitation en attente!"));
            }
        } else if (alias.equalsIgnoreCase("accept")) {
            if (inviteHandler.accept(player)) {
                player.sendMessage(tr("\u00a7eVous avez accepté l'invitation pour rejoindre un groupe d'île."));
            } else {
                player.sendMessage(tr("\u00a74Vous n'avez pas d'invitation en attente!"));
            }
        }
        return true;
    }
}
