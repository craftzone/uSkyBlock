package us.talabrek.ultimateskyblock.challenge;

import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.handler.VaultHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

public class ChallengesCommand implements CommandExecutor, TabCompleter {
    private final uSkyBlock plugin;

    public ChallengesCommand(uSkyBlock plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] split) {
        if (!plugin.isRequirementsMet(sender)) {
            return false;
        }
        if (!(sender instanceof Player)) {
            return false;
        }
        if (!plugin.getChallengeLogic().isEnabled()) {
            sender.sendMessage(tr("\u00a7eChallenges has been disabled. Contact an administrator."));
            return false;
        }
        final Player player = (Player)sender;
        if (!VaultHandler.checkPerk(player.getName(), "usb.island.challenges", player.getWorld())) {
            player.sendMessage(tr("\u00a74Vous n'avez pas accès à cette commande!"));
            return true;
        }
        if (!player.getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
            player.sendMessage(tr("\u00a74Vous ne pouvez soumettre des challenges que dans le skyblock world!"));
            return true;
        }
        PlayerInfo playerInfo = plugin.getPlayerInfo(player);
        if (!playerInfo.getHasIsland()) {
            player.sendMessage(tr("\u00a74Vous ne pouvez soumettre des challenges que dans le skyblock world! Vous n'avez pas d'île!!!"));
            return true;
        }
        ChallengeLogic challengeLogic = plugin.getChallengeLogic();
        if (split.length == 0) {
            player.openInventory(plugin.getMenu().displayChallengeGUI(player, 1));
        } else if (split.length == 1) {
            String arg = split[0].toLowerCase();
            Challenge challenge = challengeLogic.getChallenge(arg);
            if (arg.equals("help") || arg.equals("complete") || arg.equals("c")) {
                player.sendMessage(tr("\u00a7eTapez /c <nom> afficher des informations sur un challenge."));
                player.sendMessage(tr("\u00a7eTapez /c complete <nom> tenter de compléter ce challenge."));
                player.sendMessage(tr("\u00a7e==>Challenges auront des couleurs différentes en fonction de si elles sont:"));
                player.sendMessage(challengeLogic.defaults.challengeColor + "Incomplete " + challengeLogic.defaults.finishedColor + "Complete(non répétable) " + challengeLogic.defaults.repeatableColor + "Complete(répétable) ");
            } else if (challenge != null && challenge.getRank().isAvailable(playerInfo)) {
                player.sendMessage("\u00a7eNom du challenge:: " + ChatColor.WHITE + arg.toLowerCase());
                player.sendMessage("\u00a7e" + challenge.getDescription());
                if (challenge.getType() == Challenge.Type.PLAYER) {
                    if (challenge.isTakeItems()) {
                        player.sendMessage(tr("\u00a74Vous perdrez tous les éléments nécessaires lorsque vous remplissez ce challenge!"));
                    }
                } else if (challenge.getType() == Challenge.Type.ISLAND) {
                    player.sendMessage(tr("\u00a74Tous les éléments requis doivent être sur votre île avec un rayon de " + challenge.getRadius() + " de vous!"));
                }
                if (challengeLogic.getRanks().size() > 1) {
                    player.sendMessage("\u00a7eRank: " + ChatColor.WHITE + challenge.getRank());
                }
                ChallengeCompletion completion = playerInfo.getChallenge(arg);
                if (completion.getTimesCompleted() > 0 && !challenge.isRepeatable()) {
                    player.sendMessage(tr("\u00a74Ce Challenge n'est pas répétable!"));
                    return true;
                }
                ItemStack item = challenge.getDisplayItem(completion, challengeLogic.defaults.enableEconomyPlugin);
                for (String lore : item.getItemMeta().getLore()) {
                    if (lore != null && !lore.trim().isEmpty()) {
                        player.sendMessage(lore);
                    }
                }
                player.sendMessage("\u00a7ePour compléter ce challenge, tapez " + "\u00a7f/c c " + arg.toLowerCase());
            } else {
                player.sendMessage(tr("\u00a74Nom de challenge invalide! tapez /c help pour plus d'informations"));
            }
        } else if (split.length == 2 && (split[0].equalsIgnoreCase("complete") || split[0].equalsIgnoreCase("c"))) {
            challengeLogic.completeChallenge(player, split[1]);
        }
        return true;
    }

    private void filter(List<String> list, String search) {
        for (ListIterator<String> it = list.listIterator(); it.hasNext(); ) {
            String test = it.next();
            if (!test.startsWith(search)) {
                it.remove();
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            List<String> suggestions = new ArrayList<>();
            if (args.length == 1) {
                suggestions.add("help");
                suggestions.add("complete");
            }
            if (args.length >= 1) {
                PlayerInfo playerInfo = plugin.getPlayerInfo(player);
                suggestions.addAll(plugin.getChallengeLogic().getAvailableChallengeNames(playerInfo));
                filter(suggestions, args[args.length - 1]);
            }
            Collections.sort(suggestions);
            return suggestions;
        }
        return null;
    }
}
