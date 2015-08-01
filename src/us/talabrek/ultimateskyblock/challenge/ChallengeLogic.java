package us.talabrek.ultimateskyblock.challenge;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.handler.VaultHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.*;
import java.util.logging.Level;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

/**
 * The home of challenge business logic.
 */
public class ChallengeLogic {
    //private static final Pattern ITEM_AMOUNT_PATTERN = Pattern.compile("(?<id>[0-9]+)(:(?<data>[0-9]+))?:(?<amount>[0-9]+)");
    public static final int MS_MIN = 60*1000;
    public static final int MS_HOUR = 60*MS_MIN;
    public static final long MS_DAY = 24*MS_HOUR;

    private final FileConfiguration config;
    private final uSkyBlock skyBlock;
    private final Map<String, Rank> ranks;

    public final ChallengeDefaults defaults;

    public ChallengeLogic(FileConfiguration config, uSkyBlock skyBlock) {
        this.config = config;
        this.skyBlock = skyBlock;
        this.defaults = ChallengeFactory.createDefaults(config.getRoot());
        load();
        ranks = ChallengeFactory.createRankMap(config.getConfigurationSection("ranks"), defaults);
    }

    public boolean isEnabled() {
        return config.getBoolean("allowChallenges", true);
    }
    private void load() {
        Arrays.asList(config.getString("ranks", "").split(" "));
    }

    public List<Rank> getRanks() {
        return Collections.unmodifiableList(new ArrayList<>(ranks.values()));
    }

    public List<String> getAvailableChallengeNames(PlayerInfo playerInfo) {
        List<String> list = new ArrayList<>();
        for (Rank rank : ranks.values()) {
            if (rank.isAvailable(playerInfo)) {
                for (Challenge challenge : rank.getChallenges()) {
                    list.add(challenge.getName());
                }
            } else {
                break;
            }
        }
        return list;
    }

    public List<String> getAllChallengeNames() {
        List<String> list = new ArrayList<>();
        for (Rank rank : ranks.values()) {
            for (Challenge challenge : rank.getChallenges()) {
                list.add(challenge.getName());
            }
        }
        return list;
    }

    public List<Challenge> getChallengesForRank(String rank) {
        return ranks.get(rank).getChallenges();
    }

    public boolean completeChallenge(final Player player, final String challengeName) {
        final PlayerInfo pi = skyBlock.getPlayerInfo(player);
        Challenge challenge = getChallenge(challengeName);
        ChallengeCompletion completion = pi.getChallenge(challengeName);
        if (!challenge.getRank().isAvailable(pi)) {
            player.sendMessage(tr("§4Vous n'avez pas encore débloqué ce challenge!!"));
            return false;
        }
        if (!pi.challengeExists(challengeName)) {
            player.sendMessage(tr("§4Nom de challenge inconnu (vérifiez l'orthographe)!"));
            return false;
        }
        if (completion.getTimesCompleted() > 0 && (!challenge.isRepeatable() || challenge.getType() == Challenge.Type.ISLAND)) {
            player.sendMessage(tr("§4Le challenge " + challengeName + " n'est pas répétable!"));
            return false;
        }
        if (challenge.getType() == Challenge.Type.PLAYER) {
            if (!tryComplete(player, challengeName, "onPlayer")) {
                player.sendMessage("§4" + challenge.getDescription());
                player.sendMessage(tr("§4Vous n'avez pas les élément(s) requis!"));
                return false;
            }
            return true;
        } else if (challenge.getType() == Challenge.Type.ISLAND) {
            if (!skyBlock.playerIsOnIsland(player)) {
                player.sendMessage(tr("§4Vous devez être sur votre île pour faire ça!"));
                return false;
            }
            if (!tryComplete(player, challengeName, "onIsland")) {
                player.sendMessage("§4" + challenge.getDescription());
                player.sendMessage(tr("§4Vous devez être proche de " + challenge.getRadius() + " blocs de tous les éléments nécessaires."));
                return false;
            }
            return true;
        } else if (challenge.getType() == Challenge.Type.ISLAND_LEVEL) {
            if (!tryCompleteIslandLevel(player, challenge)) {
                player.sendMessage(tr("§4Votre île doit avoir un level de " + challenge.getRequiredLevel() + " pour completer ce challenge!"));
            }
            return true;
        }
        return false;
    }

    public Challenge getChallenge(String challengeName) {
        for (Rank rank : ranks.values()) {
            for (Challenge challenge : rank.getChallenges()) {
                if (challenge.getName().equalsIgnoreCase(challengeName)) {
                    return challenge;
                }
            }
        }
        return null;
    }

    public static int calcAmount(int amount, char op, int inc, int timesCompleted) {
        switch (op) {
            case '+':
                return amount + inc * timesCompleted;
            case '-':
                return amount - inc * timesCompleted; // Why?
            case '*':
                return amount * inc * timesCompleted; // Oh, my god! Just do the time m8!
            case '/':
                return amount / (inc * timesCompleted); // Yay! Free stuff!!!
        }
        return amount;
    }

    public boolean tryComplete(final Player player, final String challenge, final String type) {
        if (type.equalsIgnoreCase("onPlayer")) {
            return tryCompleteOnPlayer(player, challenge);
        } else if (type.equalsIgnoreCase("onIsland")) {
            return tryCompleteOnIsland(player, challenge);
        } else {
            player.sendMessage("§4Unknown type of challenge: " + type);
        }
        return true;
    }

    private boolean tryCompleteIslandLevel(Player player, Challenge challenge) {
        if (skyBlock.getIslandInfo(player).getLevel() >= challenge.getRequiredLevel()) {
            giveReward(player, challenge.getName());
            return true;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
	private boolean islandContains(Player player, List<ItemStack> itemStacks, int radius) {
        final Location l = player.getLocation();
        final int px = l.getBlockX();
        final int py = l.getBlockY();
        final int pz = l.getBlockZ();
        World world = l.getWorld();
        int[] blockCount = new int[0xffffff];
        int[] baseBlocks = new int[0xffff];
        for (int x = px - radius; x <= px + radius; x++) {
            for (int y = py - radius; y <= py + radius; y++) {
                for (int z = pz - radius; z <= pz + radius; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    blockCount[(block.getTypeId() << 8) + (block.getData() & 0xff)]++;
                    baseBlocks[block.getTypeId()]++;
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        boolean hasAll = true;
        for (ItemStack item : itemStacks) {
            int diffSpecific = item.getAmount() - blockCount[(item.getTypeId() << 8) + (item.getDurability() & 0xff)];
            int diffGeneral = item.getAmount() - baseBlocks[item.getTypeId()];
            if (item.getDurability() != 0 && diffSpecific > 0) {
                sb.append(" §4" + diffSpecific
                        + " §b" + VaultHandler.getItemName(item));
                hasAll = false;
            } if (diffGeneral > 0) {
                sb.append(" §4" + diffGeneral
                        + " §b" + VaultHandler.getItemName(item));
                hasAll = false;
            }
        }
        if (!hasAll) {
            player.sendMessage("§eVous n'avez pas les élément(s) requis:" + sb.toString());
        }
        return hasAll;
    }

    private boolean tryCompleteOnIsland(Player player, String challengeName) {
        Challenge challenge = getChallenge(challengeName);
        List<ItemStack> requiredItems = challenge.getRequiredItems(0);
        int radius = challenge.getRadius();
        if (islandContains(player, requiredItems, radius) && hasEntitiesNear(player, challenge.getRequiredEntities(), radius)) {
            giveReward(player, challengeName);
            return true;
        }
        return false;
    }

    private boolean hasEntitiesNear(Player player, List<EntityMatch> requiredEntities, int radius) {
        Map<EntityMatch, Integer> countMap = new LinkedHashMap<>();
        Map<EntityType, Set<EntityMatch>> matchMap = new HashMap<>();
        for (EntityMatch match : requiredEntities) {
            countMap.put(match, match.getCount());
            Set<EntityMatch> set = matchMap.get(match.getType());
            if (set == null) {
                set = new HashSet<>();
            }
            set.add(match);
            matchMap.put(match.getType(), set);
        }
        List<Entity> nearbyEntities = player.getNearbyEntities(radius, radius, radius);
        for (Entity entity : nearbyEntities) {
            if (matchMap.containsKey(entity.getType())) {
                for (Iterator<EntityMatch> it = matchMap.get(entity.getType()).iterator(); it.hasNext();) {
                    EntityMatch match = it.next();
                    if (match.matches(entity)) {
                        int newCount = countMap.get(match) - 1;
                        if (newCount <= 0) {
                            countMap.remove(match);
                            it.remove();
                        } else {
                            countMap.put(match, newCount);
                        }
                    }
                }
            }
        }
        if (!countMap.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<EntityMatch,Integer> entry : countMap.entrySet()) {
                sb.append("§e - ");
                sb.append(" §4" + entry.getValue() + " §ex");
                sb.append(" §b" + entry.getKey() + "\n");
            }
            player.sendMessage(("§eVous n'avez pas les élément(s) requis:\n" + sb.toString()).split("\n"));
        }
        return countMap.isEmpty();
    }

    private boolean tryCompleteOnPlayer(Player player, String challengeName) {
        Challenge challenge = getChallenge(challengeName);
        PlayerInfo playerInfo = skyBlock.getPlayerInfo(player);
        ChallengeCompletion completion = playerInfo.getChallenge(challengeName);
        if (challenge != null && completion != null) {
            StringBuilder sb = new StringBuilder();
            boolean hasAll = true;
            List<ItemStack> requiredItems = challenge.getRequiredItems(completion.getTimesCompletedSinceTimer());
            for (ItemStack required : requiredItems) {
                required.setItemMeta(null);
                if (!player.getInventory().containsAtLeast(required, required.getAmount())) {
                    sb.append(" §4" + (required.getAmount() - getCountOf(player.getInventory(), required))
                            + " §b" + VaultHandler.getItemName(required));
                    hasAll = false;
                }
            }
            if (hasAll) {
                if (challenge.isTakeItems()) {
                    player.getInventory().removeItem(requiredItems.toArray(new ItemStack[requiredItems.size()]));
                }
                giveReward(player, challenge);
                return true;
            } else {
                player.sendMessage("§eVous n'avez pas les élément(s) requis:" + sb.toString());
            }
        }
        return true;
    }

    private int getCountOf(PlayerInventory inventory, ItemStack required) {
        int count = 0;
        for (ItemStack invItem : inventory.all(required.getType()).values()) {
            if (invItem.getDurability() == required.getDurability()) {
                count += invItem.getAmount();
            }
        }
        return count;
    }

    public boolean giveReward(final Player player, final String challengeName) {
        return giveReward(player, getChallenge(challengeName));
    }

    private boolean giveReward(Player player, Challenge challenge) {
        String challengeName = challenge.getName();
        World skyWorld = skyBlock.getWorld();
        player.sendMessage(ChatColor.GREEN + "\u2714 Félicitation vous avez terminé le challenge: " + challengeName + "!");
        PlayerInfo playerInfo = skyBlock.getPlayerInfo(player);
        Reward reward;
        boolean isFirstCompletion = playerInfo.checkChallenge(challengeName) == 0;
        if (isFirstCompletion) {
            reward = challenge.getReward();
        } else {
        	ChallengeCompletion completion = playerInfo.getChallenge(challengeName);
        	if (completion.isOnCooldown()) reward = challenge.getRepeatReward();
        	else reward = challenge.getReward();
            
        }
        float rewBonus = 1;
        if (defaults.enableEconomyPlugin && VaultHandler.hasEcon()) {
            // TODO: 10/12/2014 - R4zorax: Move this to some config file
            if (VaultHandler.checkPerk(player.getName(), "group.memberplus", skyWorld)) {
                rewBonus += 0.05;
            }
            if (VaultHandler.checkPerk(player.getName(), "usb.donor.all", skyWorld)) {
                rewBonus += 0.05;
            }
            if (VaultHandler.checkPerk(player.getName(), "usb.donor.25", skyWorld)) {
                rewBonus += 0.05;
            }
            if (VaultHandler.checkPerk(player.getName(), "usb.donor.50", skyWorld)) {
                rewBonus += 0.05;
            }
            if (VaultHandler.checkPerk(player.getName(), "usb.donor.75", skyWorld)) {
                rewBonus += 0.1;
            }
            if (VaultHandler.checkPerk(player.getName(), "usb.donor.100", skyWorld)) {
                rewBonus += 0.2;
            }
            VaultHandler.depositPlayer(player.getName(), reward.getCurrencyReward() * rewBonus);
        }
        player.giveExp(reward.getXpReward());
        if (defaults.broadcastCompletion && isFirstCompletion) {
            Bukkit.getServer().broadcastMessage("§6\u27A4 §f" + player.getName() + " §6a terminé le challenge §a" + challengeName + "!");
        }
        player.sendMessage(String.format("§a\u2714 §eRécompense §7(Items): §f%s", reward.getRewardText()));
        player.sendMessage(String.format("§a\u2714 §eRécompense §7(XP): §f%d", reward.getXpReward()));
        if (defaults.enableEconomyPlugin && VaultHandler.hasEcon()) {
            player.sendMessage(String.format("§a\u2714 §eRécompense §7(Pièce d'or): §f%5.2f %s §a (%4.2f%%)", reward.getCurrencyReward() * rewBonus, VaultHandler.getEcon().currencyNamePlural(), (rewBonus - 1.0) * 100.0));
        }
        if (reward.getPermissionReward() != null) {
            for (String perm : reward.getPermissionReward().split(" ")) {
                if (!VaultHandler.checkPerm(player, perm, player.getWorld())) {
                    VaultHandler.addPerk(player, perm);
                }
            }
        }
        for (String cmd : reward.getCommands()) {
            String command = cmd.replaceAll("\\{challenge\\}", challengeName);
            skyBlock.execCommand(player, command);
        }
        HashMap<Integer, ItemStack> leftOvers = player.getInventory().addItem(reward.getItemReward().toArray(new ItemStack[0]));
        for (ItemStack item : leftOvers.values()) {
            player.getWorld().dropItem(player.getLocation(), item);
        }
        if (!leftOvers.isEmpty()) {
            player.sendMessage(tr("§eVotre inventaire est §4plein u00a7e.Les items seront déposés sur terre!"));
        }
        playerInfo.completeChallenge(challengeName);
        return true;
    }

    public long getResetInMillis(String challenge) {
        return getChallenge(challenge).getResetInHours() * MS_HOUR;
    }

    public ItemStack getItemStack(PlayerInfo playerInfo, String challengeName) {
        Challenge challenge = getChallenge(challengeName);
        ChallengeCompletion completion = playerInfo.getChallenge(challengeName);
        ItemStack currentChallengeItem = challenge.getDisplayItem(completion, defaults.enableEconomyPlugin);
        if (completion.getTimesCompleted() == 0) {
            currentChallengeItem.setType(Material.STAINED_GLASS_PANE);
            currentChallengeItem.setDurability((short) 4);
        } else if (!challenge.isRepeatable()) {
            currentChallengeItem.setType(Material.STAINED_GLASS_PANE);
            currentChallengeItem.setDurability((short) 13);
        }
        ItemMeta meta = currentChallengeItem.getItemMeta();
        List<String> lores = meta.getLore();
        if (challenge.isRepeatable() || completion.getTimesCompleted() == 0) {
            lores.add("§c\u27A1 §e§lCliquez pour terminer ce challenge");
        } else {
            lores.add("§4§l\u2717 Vous ne pouvez pas répéter ce challenge.");
        }
        meta.setLore(lores);
        currentChallengeItem.setItemMeta(meta);
        return currentChallengeItem;
    }

    public static List<String> wordWrap(String s, int firstSegment, int lineSize) {
        List<String> words = new ArrayList<>();
        int ix = firstSegment;
        int jx = 0;
        while (ix < s.length()) {
            ix = s.indexOf(' ', ix);
            if (ix != -1) {
                String subString = s.substring(jx, ix).trim();
                if (!subString.isEmpty()) {
                    words.add(subString);
                }
            } else {
                break;
            }
            jx = ix + 1;
            ix += lineSize;
        }
        words.add(s.substring(jx));
        return words;
    }
    public void populateChallenges(Map<String, ChallengeCompletion> challengeMap) {
        for (Rank rank : ranks.values()) {
            for (Challenge challenge : rank.getChallenges()) {
                String key = challenge.getName().toLowerCase();
                if (!challengeMap.containsKey(key)) {
                    challengeMap.put(key, new ChallengeCompletion(key, 0L, 0, 0));
                }
            }
        }
    }

    public void populateChallengeRank(Inventory menu, Player player, PlayerInfo pi, int page) {
        List<Rank> ranksOnPage = new ArrayList<>(ranks.values());
        // page 1 = 0-4, 2 = 5-8, ...
        if (page > 0) {
            ranksOnPage = ranksOnPage.subList(((page-1)*4), Math.min(page*4, ranksOnPage.size()));
        }
        int location = 0;
        for (Rank rank : ranksOnPage) {
            populateChallengeRank(menu, player, rank, location, pi);
            location += 9;
        }
    }

    public void populateChallengeRank(Inventory menu, final Player player, final Rank rank, int location, final PlayerInfo playerInfo) {
        List<String> lores = new ArrayList<>();
        ItemStack currentChallengeItem = rank.getDisplayItem();
        ItemMeta meta4 = currentChallengeItem.getItemMeta();
        meta4.setDisplayName("§e§l\u2720 Rank: §3§l" + rank.getName());
        lores.add("§fCompléter la plupart des challenges dans");
        lores.add("§fce niveau pour débloquer le niveau suivant.");
        meta4.setLore(lores);
        currentChallengeItem.setItemMeta(meta4);
        menu.setItem(location, currentChallengeItem);
        List<String> missingRequirements = rank.getMissingRequirements(playerInfo);
        for (Challenge challenge : rank.getChallenges()) {
            lores.clear();
            String challengeName = challenge.getName();
            try {
                if (!missingRequirements.isEmpty()) {
                    currentChallengeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
                    meta4 = currentChallengeItem.getItemMeta();
                    meta4.setDisplayName("§4§l\u2716 Challenge verrouillé \u2716");
                    lores.addAll(missingRequirements);
                    meta4.setLore(lores);
                    currentChallengeItem.setItemMeta(meta4);
                    menu.setItem(++location, currentChallengeItem);
                } else {
                    currentChallengeItem = getItemStack(playerInfo, challengeName);
                    menu.setItem(++location, currentChallengeItem);
                }
                

                
            } catch (Exception e) {
                skyBlock.getLogger().log(Level.SEVERE, "Invalid challenge " + challenge, e);
            }
        }
    }

    public boolean isResetOnCreate() {
        return config.getBoolean("resetChallengesOnCreate", true);
    }
}
