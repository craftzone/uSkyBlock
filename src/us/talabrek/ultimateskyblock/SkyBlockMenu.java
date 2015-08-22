package us.talabrek.ultimateskyblock;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import us.talabrek.ultimateskyblock.challenge.ChallengeLogic;
import us.talabrek.ultimateskyblock.handler.VaultHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: Move all the texts to resource-files (translatable).
/**
 * The UI menu of uSkyBlock (using the inventory UI).
 */
public class SkyBlockMenu {
    private static final Pattern PERM_VALUE_PATTERN = Pattern.compile("(\\[(?<perm>(?<not>[!])?[^\\]]+)\\])?(?<value>.*)");
    private static final Pattern CHALLENGE_PAGE_HEADER = Pattern.compile("Challenge Menu \\((?<p>[0-9]+)/(?<max>[0-9]+)\\)");
    private uSkyBlock skyBlock;
    private final ChallengeLogic challengeLogic;
    ItemStack pHead;
    ItemStack sign;
    ItemStack biome;
    ItemStack lock;
    ItemStack warpset;
    ItemStack warptoggle;
    ItemStack invite;
    ItemStack kick;

    @SuppressWarnings("deprecation")
	public SkyBlockMenu(uSkyBlock skyBlock, ChallengeLogic challengeLogic) {
        this.skyBlock = skyBlock;
        this.challengeLogic = challengeLogic;
        pHead = new ItemStack(397, 1, (short) 3);
        sign = new ItemStack(323, 1);
        biome = new ItemStack(6, 1, (short) 3);
        lock = new ItemStack(101, 1);
        warpset = new ItemStack(90, 1);
        warptoggle = new ItemStack(69, 1);
        invite = new ItemStack(398, 1);
        kick = new ItemStack(301, 1);
    }

    @SuppressWarnings("deprecation")
	public Inventory displayPartyPlayerGUI(final Player player, final String pname) {
        List<String> lores = new ArrayList<>();
        Inventory menu = Bukkit.createInventory(null, 9, pname + " <Permissions>");
        final ItemStack pHead = new ItemStack(397, 1, (short) 3);
        final SkullMeta meta3 = (SkullMeta) pHead.getItemMeta();
        ItemMeta meta2 = sign.getItemMeta();
        meta2.setDisplayName("§hPlayer Permissions");
        lores.add("§eCliquez ici pour retourner");
        lores.add("§eà la liste des membres.");
        meta2.setLore(lores);
        sign.setItemMeta(meta2);
        menu.addItem(new ItemStack[]{sign});
        lores.clear();
        meta3.setDisplayName(pname + "'s Permissions");
        lores.add("§eSurvolez une icône pour afficher");
        lores.add("§eune permission. Changez la");
        lores.add("§epermission en cliquant dessus.");
        meta3.setLore(lores);
        pHead.setItemMeta(meta3);
        menu.addItem(new ItemStack[]{pHead});
        lores.clear();
        meta2 = biome.getItemMeta();
        if (skyBlock.getIslandInfo(player).hasPerm(pname, "canChangeBiome")) {
            meta2.setDisplayName("§aChanger le Biome");
            lores.add("§fCe joueur §apeut§f changer");
            lores.add("§fle biome de votre île.");
            lores.add("§fCliquez ici pour lui retirer cette permission.");
        } else {
            meta2.setDisplayName("§cChanger le Biome");
            lores.add("§fCe joueur §cne peut pas§f changer");
            lores.add("§fle biome de votre île.");
            lores.add("§fCliquez ici pour lui ajouter cette permission.");
        }
        meta2.setLore(lores);
        biome.setItemMeta(meta2);
        menu.addItem(new ItemStack[]{biome});
        lores.clear();
        meta2 = lock.getItemMeta();
        if (skyBlock.getIslandInfo(player).hasPerm(pname, "canToggleLock")) {
            meta2.setDisplayName("§aVerrouiller/déverrouiller l'île");
            lores.add("§fCe joueur §apeut§f verrouiller");
            lores.add("§fou déverrouiller l'île.");
            lores.add("§fCliquez ici pour lui retirer cette permission.");
        } else {
            meta2.setDisplayName("§cVerrouiller/déverrouiller l'île");
            lores.add("§fCe joueur §cne peut pas§f verrouiller");
            lores.add("§fou déverrouiller l'île.");
            lores.add("§fCliquez ici pour lui ajouter cette permission");
        }
        meta2.setLore(lores);
        lock.setItemMeta(meta2);
        menu.addItem(new ItemStack[]{lock});
        lores.clear();
        meta2 = warpset.getItemMeta();
        if (skyBlock.getIslandInfo(player).hasPerm(pname, "canChangeWarp")) {
            meta2.setDisplayName("§aDéfinir le warp de l'île.");
            lores.add("§fCe joueur §apeut§f définir");
            lores.add("§fle warp de l'île");
            lores.add("§fCliquez ici pour lui retirer cette permission.");
        } else {
            meta2.setDisplayName("§cDéfinir le warp de l'île.");
            lores.add("§fCe joueur §cne peut pas§f définir");
            lores.add("§fle warp de l'île.");
            lores.add("§fCliquez ici pour lui ajouter cette permission.");
        }
        meta2.setLore(lores);
        warpset.setItemMeta(meta2);
        menu.addItem(new ItemStack[]{warpset});
        lores.clear();
        meta2 = warptoggle.getItemMeta();
        if (skyBlock.getIslandInfo(player).hasPerm(pname, "canToggleWarp")) {
            meta2.setDisplayName("§aActiver/désactiver le warp de l'île.");
            lores.add("§fCe joueur §apeut§f activer");
            lores.add("§fou désactiver le warp de l'île.");
            lores.add("§fCliquez ici pour lui retirer cette permission.");
        } else {
            meta2.setDisplayName("§cActiver/désactiver le warp de l'île.");
            lores.add("§fCe joueur §cne peut pas§f activer");
            lores.add("§fou désactiver le warp de l'île.");
            lores.add("§fCliquez ici pour lui ajouter cette permission.");
        }
        meta2.setLore(lores);
        warptoggle.setItemMeta(meta2);
        menu.addItem(new ItemStack[]{warptoggle});
        lores.clear();
        meta2 = invite.getItemMeta();
        if (skyBlock.getIslandInfo(player).hasPerm(pname, "canInviteOthers")) {
            meta2.setDisplayName("§aInviter d'autres joueurs à l'île.");
            lores.add("§fCe joueur §apeut§f inviter");
            lores.add("§fd'autres joueurs à l'île.");
            lores.add("§fCliquez ici pour lui retirer cette permission.");
        } else {
            meta2.setDisplayName("§cInviter d'autres joueurs à l'île.");
            lores.add("§fCe joueur §cne peut pas§f inviter");
            lores.add("§fd'autres joueurs à l'île.");
            lores.add("§fCliquez ici pour lui ajouter cette permission.");
        }
        meta2.setLore(lores);
        invite.setItemMeta(meta2);
        menu.addItem(new ItemStack[]{invite});
        lores.clear();
        meta2 = kick.getItemMeta();
        if (skyBlock.getIslandInfo(player).hasPerm(pname, "canKickOthers")) {
            meta2.setDisplayName("§aKick les autres membres de l'île");
            lores.add("§fCe joueur §apeut§f kick");
            lores.add("§fles autres membres du groupe de l'île.");
            lores.add("§fexcepté le chef de l'île");
            lores.add("§fCliquez ici pour lui retirer cette permission.");
        } else {
            meta2.setDisplayName("§cKick les autres membres de l'île");
            lores.add("§fCe joueur §cne peut pas§f kick");
            lores.add("§fles autres membres du groupe de l'île.");
            lores.add("§fCliquez ici pour lui ajouter cette permission.");
        }
        meta2.setLore(lores);
        kick.setItemMeta(meta2);
        menu.addItem(new ItemStack[]{kick});
        lores.clear();
        return menu;
    }

    public Inventory displayPartyGUI(final Player player) {
        List<String> lores = new ArrayList<>();
        Inventory menu = Bukkit.createInventory(null, 18, "§9Island Group Members");
        IslandInfo islandInfo = skyBlock.getIslandInfo(player);
        final Set<String> memberList = islandInfo.getMembers();
        final SkullMeta meta3 = (SkullMeta) pHead.getItemMeta();
        final ItemMeta meta2 = sign.getItemMeta();
        meta2.setDisplayName("§aGroup Info");
        lores.add("Group Members: §2" + islandInfo.getPartySize() + "§7/§e" + islandInfo.getMaxPartySize());
        if (islandInfo.getPartySize() < islandInfo.getMaxPartySize()) {
        	lores.add("§aD'autres joueurs peuvent être invités à cette île.");
        } else {
        	lores.add("§cVous avez atteint le nombre maximum de membres.");
        }
	    lores.add("§eSurvolez l'icône d'un joueur");
	    lores.add("§epour voir ses permissions");
	    lores.add("§eLe chef peut changer les permissions");
	    lores.add("§een cliquant sur l'icône d'un joueur.");
        meta2.setLore(lores);
        sign.setItemMeta(meta2);
        menu.addItem(new ItemStack[]{sign});
        lores.clear();
        for (String temp : memberList) {
            if (temp.equalsIgnoreCase(islandInfo.getLeader())) {
                meta3.setDisplayName("§f" + temp);
    	        lores.add("§a§lChef");
    	        lores.add("§aPeut §fchanger le biome de l'île.");
    	        lores.add("§aPeut §fverrouiller/déverrouiller l'île.");
    	        lores.add("§aPeut §fdéfinir le warp de l'île.");
    	        lores.add("§aPeut §factiver/désactiver le warp de l'île.");
    	        lores.add("§aPeut §finviter d'autres joueurs à l'île.");
    	        lores.add("§aPeut §fkick les autres membres de l'île.");
                meta3.setLore(lores);
                lores.clear();
            } else {
                meta3.setDisplayName("§f" + temp);
    	        lores.add("§e§lMembre");
                if (islandInfo.hasPerm(temp, "canChangeBiome")) {
      	          lores.add("§aPeut §fchanger le biome de l'île.");
                } else {
      	          lores.add("§cNe peut pas §fchanger le biome de l'île.");
                }
                if (islandInfo.hasPerm(temp, "canToggleLock")) {
      	          lores.add("§aPeut §fverrouiller/déverrouiller l'île.");
                } else {
      	          lores.add("§cNe peut pas §fverrouiller/déverrouiller l'île.");
                }
                if (islandInfo.hasPerm(temp, "canChangeWarp")) {
      	          lores.add("§aPeut §fdéfinir le warp de l'île.");
                } else {
      	          lores.add("§cNe peut pas §fdéfinir le warp de l'île.");
                }
                if (islandInfo.hasPerm(temp, "canToggleWarp")) {
      	          lores.add("§aPeut §factiver/désactiver le warp de l'île.");
                } else {
      	          lores.add("§cNe peut pas §factiver/désactiver le warp de l'île.");
                }
                if (islandInfo.hasPerm(temp, "canInviteOthers")) {
      	          lores.add("§aPeut §finviter d'autres joueurs à l'île.");
                } else {
      	          lores.add("§cNe peut pas §finviter d'autres joueurs à l'île.");
                }
                if (islandInfo.hasPerm(temp, "canKickOthers")) {
      	          lores.add("§aPeut §fkick les autres membres de l'île.");
                } else {
      	          lores.add("§cNe peut pas §fkick les autres membres de l'île.");
                }
                if (player.getName().equalsIgnoreCase(islandInfo.getLeader())) {
      	          lores.add("§e<Cliquez pour changer les permissions de ce joueur>");
                }
                meta3.setLore(lores);
                lores.clear();
            }
            meta3.setOwner(temp);
            pHead.setItemMeta(meta3);
            menu.addItem(new ItemStack[]{pHead});
        }
        return menu;
    }

    public Inventory displayLogGUI(final Player player) {
        List<String> lores = new ArrayList<>();
        Inventory menu = Bukkit.createInventory(null, 9, "§9Island Log");
        ItemMeta meta4 = sign.getItemMeta();
        meta4.setDisplayName("§lIsland Log");
        lores.add("§eCliquez ici pour retourner");
        lores.add("§eau menu principal.");
        meta4.setLore(lores);
        sign.setItemMeta(meta4);
        menu.addItem(new ItemStack[]{sign});
        lores.clear();
        ItemStack menuItem = new ItemStack(Material.BOOK_AND_QUILL, 1);
        meta4 = menuItem.getItemMeta();
        meta4.setDisplayName("§e§lIsland Log");
        for (String log : skyBlock.getIslandInfo(player).getLog()) {
            lores.add(log);
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.setItem(8, menuItem);
        lores.clear();
        return menu;
    }

    public Inventory displayBiomeGUI(final Player player) {
        List<String> lores = new ArrayList<>();
        Inventory menu = Bukkit.createInventory(null, 18, "§9Island Biome");
        ItemMeta meta4 = sign.getItemMeta();
        meta4.setDisplayName("§hIsland Biome");
        lores.add("§eCliquez ici pour retourner");
        lores.add("§eau menu principal.");
        meta4.setLore(lores);
        sign.setItemMeta(meta4);
        menu.addItem(new ItemStack[]{sign});
        lores.clear();
        ItemStack menuItem = new ItemStack(Material.RAW_FISH, 1, (short) 2);
        meta4 = menuItem.getItemMeta();
        String currentBiome = skyBlock.getCurrentBiome(player);
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.ocean", player.getWorld())) {
            meta4.setDisplayName("§aBiome: Océan");
            lores.add("§fLe biome océan est le biome");
            lores.add("§fbasique par défaut pour toutes les îles.");
            lores.add("§fLes animaux et les monstres");
            lores.add("§fspawn normalement.");
            if ("OCEAN".equals(currentBiome)) {
                lores.add("§2§lCeci est votre biome actuel.");
            } else {
                lores.add("§c\u27A1 §e§lCliquez pour changer en biome océan.");
            }
        } else {
            meta4.setDisplayName("§8Biome: Océan");
            lores.add("§cGrade Spécialiste et +.");
            lores.add("§fLe biome océan est le biome");
            lores.add("§fbasique par défaut pour toutes les îles.");
            lores.add("§fLes animaux et les monstres");
            lores.add("§fspawn normalement.");
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();
        menuItem = new ItemStack(Material.SAPLING, 1, (short) 1);
        meta4 = menuItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.forest", player.getWorld())) {
            meta4.setDisplayName("§aBiome: Forêt");
            lores.add("§fLe biome forêt:");
            lores.add("§fLes animaux et les monstres");
            lores.add("§fspawn normalement.");
            lores.add("§fLes loups spawn désormais également.");
            if ("FOREST".equals(currentBiome)) {
          	  lores.add("§2§lCeci est votre biome actuel.");
            } else {
                lores.add("§c\u27A1 §e§lCliquez pour changer en biome forêt.");
            }
        } else {
            meta4.setDisplayName("§8Biome: Forêt");
            lores.add("§cGrade Spécialiste et +.");
            lores.add("§fLe biome forêt:");
            lores.add("§fLes animaux et les monstres");
            lores.add("§fspawn normalement.");
            lores.add("§fLes loups spawn désormais également.");
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();
        menuItem = new ItemStack(Material.SAND, 1);
        meta4 = menuItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.desert", player.getWorld())) {
            meta4.setDisplayName("§aBiome: Désert");
            lores.add("§fLe biome désert:");
            lores.add("§fIl n'y a pas de pluie ni de neige");
            lores.add("§fLes animaux ne spawn pas");
            lores.add("§fLes monstres spawn normalement.");
            if ("DESERT".equals(currentBiome)) {
          	  lores.add("§2§lCeci est votre biome actuel.");
            } else {
                lores.add("§c\u27A1 §e§lCliquez pour changer en biome désert.");
            }
        } else {
            meta4.setDisplayName("§8Biome: Désert");
            lores.add("§cGrade Spécialiste et +.");
            lores.add("§fLe biome désert:");
            lores.add("§fIl n'y a pas de pluie ni de neige");
            lores.add("§fLes animaux ne spawn pas");
            lores.add("§fLes monstres spawn normalement.");
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();
        menuItem = new ItemStack(Material.SAPLING, 1, (short) 3);
        meta4 = menuItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.jungle", player.getWorld())) {
            meta4.setDisplayName("§aBiome: Jungle");
            lores.add("§fLe biome jungle:");
            lores.add("§fBiome lumineux et coloré.");
            lores.add("§fLes animaux et les monstres");
            lores.add("§fspawn normalement.");
            lores.add("§fLes ocelots spawn désormais également.");
            if ("JUNGLE".equals(currentBiome)) {
            	lores.add("§2§lCeci est votre biome actuel.");
            } else {
            	lores.add("§c\u27A1 §e§lCliquez pour changer en biome jungle.");
            }
        } else {
            meta4.setDisplayName("§8Biome: Jungle");
            lores.add("§cGrade Conseiller et +.");
            lores.add("§fLe biome jungle:");
            lores.add("§fBiome lumineux et coloré.");
            lores.add("§fLes animaux et les monstres");
            lores.add("§fspawn normalement.");
            lores.add("§fLes ocelots spawn désormais également.");
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();
        menuItem = new ItemStack(Material.WATER_LILY, 1);
        meta4 = menuItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.swampland", player.getWorld())) {
            meta4.setDisplayName("§aBiome: Marais");
            lores.add("§fLe biome marais:");
            lores.add("§fBiome sombre et humide.");
            lores.add("§fLes animaux et les monstres");
            lores.add("§fspawn normalement.");
            lores.add("§fLes slimes spawn désormais également");
            lores.add("§fselon la postion de la lune");
            if ("SWAMPLAND".equals(currentBiome)) {
            	lores.add("§2§lCeci est votre biome actuel.");
            } else {
            	lores.add("§c\u27A1 §e§lCliquez pour changer en biome marais.");
            }
        } else {
            meta4.setDisplayName("§8Biome: Marais");
            lores.add("§cGrade Conseiller et +.");
            lores.add("§fLe biome marais:");
            lores.add("§fBiome sombre et humide.");
            lores.add("§fLes animaux et les monstres");
            lores.add("§fspawn normalement.");
            lores.add("§fLes slimes spawn désormais également");
            lores.add("§fselon la position de la lune.");
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();
        menuItem = new ItemStack(Material.SNOW, 1);
        meta4 = menuItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.taiga", player.getWorld())) {
            meta4.setDisplayName("§aBiome: Taïga");
            lores.add("§fLe biome taïga:");
            lores.add("§fBiome avec de fortes chances de neige.");
            lores.add("§fLes animaux et les monstres");
            lores.add("§fspawn normalement.");
            lores.add("§fLes loups spawn désormais également.");
            if ("TAIGA".equals(currentBiome)) {
            	lores.add("§2§lCeci est votre biome actuel.");
            } else {
            	lores.add("§c\u27A1 §e§lCliquez pour changer en biome taïga.");
            }
        } else {
            meta4.setDisplayName("§8Biome: Taïga");
            lores.add("§cGrade Conseiller et +.");
            lores.add("§fLe biome taïga:");
            lores.add("§fBiome avec de fortes chances de neige.");
            lores.add("§fLes animaux et les monstres");
            lores.add("§fspawn normalement.");
            lores.add("§fLes loups spawn désormais également.");
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();
        menuItem = new ItemStack(Material.RED_MUSHROOM, 1);
        meta4 = menuItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.mushroom", player.getWorld())) {
            meta4.setDisplayName("§aBiome: Champignon");
            lores.add("§fLe biome champignon:");
            lores.add("§fBiome lumineux et coloré.");
            lores.add("§fLes mooshrooms sont les seuls");
            lores.add("§fanimaux qui spawn.");
            lores.add("§fLes autres animaux et monstres");
            lores.add("§fne spawn plus!");
            if ("MUSHROOM".equals(currentBiome)) {
            	lores.add("§2§lCeci est votre biome actuel.");
            } else {
            	lores.add("§c\u27A1 §e§lCliquez pour changer en biome champignon.");
            }
        } else {
            meta4.setDisplayName("§8Biome: Champignon");
            lores.add("§cGrade Expert et +.");
            lores.add("§fLe biome champignon:");
            lores.add("§fBiome lumineux et coloré.");
            lores.add("§fLes mooshrooms sont les seuls");
            lores.add("§fanimaux qui spawn.");
            lores.add("§fLes autres animaux et monstres");
            lores.add("§fne spawn plus!");
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();
        menuItem = new ItemStack(Material.NETHER_BRICK, 1);
        meta4 = menuItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.hell", player.getWorld())) {
            meta4.setDisplayName("§aBiome: Nether");
            lores.add("§fLe biome nether:");
            lores.add("§fbiome sombre et mort.");
            lores.add("§fIl n'y a pas de pluie ni de neige.");
            lores.add("§fLes monstres du nether spawn");
            lores.add("§fsauf les ghasts et les blazes.");
            if ("HELL".equals(currentBiome)) {
            	lores.add("§2§lCeci est votre biome actuel.");
            } else {
            	lores.add("§c\u27A1 §e§lCliquez pour changer en biome nether.");
            }
        } else {
            meta4.setDisplayName("§8Biome: Nether");
            lores.add("§cGrade Expert et +.");
            lores.add("§fLe biome nether:");
            lores.add("§fbiome sombre et mort.");
            lores.add("§fIl n'y a pas de pluie ni de neige.");
            lores.add("§fLes monstres du nether spawn");
            lores.add("§fsauf les ghasts et les blazes.");
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();
        menuItem = new ItemStack(Material.EYE_OF_ENDER, 1);
        meta4 = menuItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.sky", player.getWorld())) {
            meta4.setDisplayName("§aBiome: End");
            lores.add("§fLe biome End:");
            lores.add("§fbiome avec un ciel sombre.");
            lores.add("§fSeul les endermen spawn");
            lores.add("§fdans ce biome.");
            if ("SKY".equals(currentBiome)) {
            	lores.add("§2§lCeci est votre biome actuel.");
            } else {
            	lores.add("§c\u27A1 §e§lCliquez pour changer en biome End.");
            }
        } else {
            meta4.setDisplayName("§8Biome: End");
            lores.add("§cGrade Expert et +.");
            lores.add("§fLe biome End:");
            lores.add("§fbiome avec un ciel sombre.");
            lores.add("§fSeul les endermen spawn");
            lores.add("§fdans ce biome.");
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();
       /* menuItem = new ItemStack(Material.LONG_GRASS, 1, (byte)1);
        meta4 = menuItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.plains", player.getWorld())) {
            meta4.setDisplayName("§aBiome: Plains");
            lores.add("§fThe plains biome has rain");
            lores.add("§finstead of snow. Passive");
            lores.add("§fmobs will spawn normally");
            lores.add("§f(including horses) and");
            lores.add("§fhostile mobs will spawn.");
            if ("PLAINS".equals(currentBiome)) {
                lores.add("§2§lThis is your current biome.");
            } else {
                lores.add("§e§lClick to change to this biome.");
            }
        } else {
            meta4.setDisplayName("§8Biome: Plains");
            lores.add("§cYou cannot use this biome.");
            lores.add("§7The plains biome has rain");
            lores.add("§7instead of snow. Passive");
            lores.add("§7mobs will spawn normally");
            lores.add("§7(including horses) and");
            lores.add("§7hostile mobs will spawn.");
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();
        menuItem = new ItemStack(Material.EMERALD_ORE, 1);
        meta4 = menuItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.extreme_hills", player.getWorld())) {
            meta4.setDisplayName("§aBiome: Extreme Hills");
            lores.add("§fThe extreme hills biome.");
            lores.add("§fPassive mobs will spawn ");
            lores.add("§fnormally and hostile");
            lores.add("§fmobs will spawn.");
            if ("EXTREME_HILLS".equals(currentBiome)) {
                lores.add("§2§lThis is your current biome.");
            } else {
                lores.add("§e§lClick to change to this biome.");
            }
        } else {
            meta4.setDisplayName("§8Biome: Extreme Hills");
            lores.add("§cYou cannot use this biome.");
            lores.add("§7The extreme hills biome.");
            lores.add("§7Passive mobs will spawn ");
            lores.add("§7normally and hostile");
            lores.add("§7mobs will spawn.");
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear(); */
        return menu;
    }
    
    
    
    
    
    
    
    
    

   /* private void addExtraMenus(Player player, Inventory menu) {
        ConfigurationSection extras = skyBlock.getConfig().getConfigurationSection("options.extra-menus");
        if (extras == null) {
            return;
        }
        World world = uSkyBlock.getSkyBlockWorld();
        for (String sIndex : extras.getKeys(false)) {
            ConfigurationSection menuSection = extras.getConfigurationSection(sIndex);
            if (menuSection == null) {
                continue;
            }
            try {
                int index = Integer.parseInt(sIndex, 10);
                String title = menuSection.getString("title", "\u00a9Unknown");
                String icon = menuSection.getString("displayItem", "CHEST");
                List<String> lores = new ArrayList<>();
                for (String l : menuSection.getStringList("lore")) {
                    Matcher matcher = PERM_VALUE_PATTERN.matcher(l);
                    if (matcher.matches()) {
                        String perm = matcher.group("perm");
                        String lore = matcher.group("value");
                        boolean not = matcher.group("not") != null;
                        if (perm != null) {
                            boolean hasPerm = VaultHandler.checkPerm(player, perm, world);
                            if ((hasPerm && !not) || (!hasPerm && not)) {
                                lores.add(lore);
                            }
                        } else {
                            lores.add(lore);
                        }
                    }
                }
                // Only SIMPLE icons supported...
                ItemStack item = new ItemStack(Material.matchMaterial(icon), 1);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(title);
                meta.setLore(lores);
                item.setItemMeta(meta);
                menu.setItem(index, item);
            } catch (Exception e) {
                uSkyBlock.log(Level.INFO, "§9[uSkyBlock]§r Unable to add extra-menu " + sIndex + ": " + e);
            }
        }
    } */

    private boolean isExtraMenuAction(Player player, ItemStack currentItem) {
        ConfigurationSection extras = skyBlock.getConfig().getConfigurationSection("options.extra-menus");
        if (extras == null || currentItem == null || currentItem.getItemMeta() == null) {
            return false;
        }
        Material itemType = currentItem.getType();
        String itemTitle = currentItem.getItemMeta().getDisplayName();
        World world = uSkyBlock.getSkyBlockWorld();
        for (String sIndex : extras.getKeys(false)) {
            ConfigurationSection menuSection = extras.getConfigurationSection(sIndex);
            if (menuSection == null) {
                continue;
            }
            try {
                String title = menuSection.getString("title", "\u00a9Unknown");
                String icon = menuSection.getString("displayItem", "CHEST");
                Material material = Material.matchMaterial(icon);
                if (title.equals(itemTitle) && material == itemType) {
                    for (String command : menuSection.getStringList("commands")) {
                        Matcher matcher = PERM_VALUE_PATTERN.matcher(command);
                        if (matcher.matches()) {
                            String perm = matcher.group("perm");
                            String cmd = matcher.group("value");
                            boolean not = matcher.group("not") != null;
                            if (perm != null) {
                                boolean hasPerm = VaultHandler.checkPerm(player, perm, world);
                                if ((hasPerm && !not) || (!hasPerm && not)) {
                                    skyBlock.execCommand(player, cmd);
                                }
                            } else {
                                skyBlock.execCommand(player, cmd);
                            }
                        } else {
                            uSkyBlock.log(Level.INFO, "§a[uSkyBlock] Malformed menu " + title + ", invalid command : " + command);
                        }
                    }
                    return true;
                }
            } catch (Exception e) {
                uSkyBlock.log(Level.INFO, "§9[uSkyBlock]§r Unable to execute commands for extra-menu " + sIndex + ": " + e);
            }
        }
        return false;
    }

    public Inventory displayChallengeGUI(final Player player, int page) {
        Inventory menu = Bukkit.createInventory(null, 45, "§9Challenge Menu (" + page + "/" + ((challengeLogic.getRanks().size()/4)+1) + ")");
        final PlayerInfo pi = skyBlock.getPlayerInfo(player);
        challengeLogic.populateChallengeRank(menu, player, pi, page);
        if(page == 1) {
        ItemStack currentChallengeItem = new ItemStack(Material.ARROW);
        ItemMeta metaarrw = currentChallengeItem.getItemMeta();
        metaarrw.setDisplayName("§a§l\u21E8 Page suivante");
        currentChallengeItem.setItemMeta(metaarrw);
        menu.setItem(44, currentChallengeItem); 
        
        ItemStack currentChallengeItem2 = new ItemStack(Material.BARRIER);
        ItemMeta metaarrw2 = currentChallengeItem2.getItemMeta();
        metaarrw2.setDisplayName("§c§l\u2716 Menu principal");
        currentChallengeItem2.setItemMeta(metaarrw2);
        menu.setItem(36, currentChallengeItem2); 
        }
        else if(page == 2) {
        ItemStack currentChallengeItem = new ItemStack(Material.ARROW);
        ItemMeta metaarrw = currentChallengeItem.getItemMeta();
        metaarrw.setDisplayName("§a§l\u21E6 Page précédente");
        currentChallengeItem.setItemMeta(metaarrw);
        menu.setItem(36, currentChallengeItem);
        
        ItemStack currentChallengeItem2 = new ItemStack(Material.BARRIER);
        ItemMeta metaarrw2 = currentChallengeItem2.getItemMeta();
        metaarrw2.setDisplayName("§c§l\u2716 Menu principal");
        currentChallengeItem2.setItemMeta(metaarrw2);
        menu.setItem(44, currentChallengeItem2); 
        }
        
        return menu;
       
    }

    public Inventory displayIslandGUI(final Player player) {
        Inventory menu = null;
        if (skyBlock.hasIsland(player.getName())) {
            menu = Bukkit.createInventory(null, 45, "§9Island Menu");
            addMainMenu(menu, player);
        } else {
            menu = Bukkit.createInventory(null, 9, "§9Island Create Menu");
            addInitMenu(menu);
        }
        return menu;
    }

    private void addInitMenu(Inventory menu) {
        List<String> lores = new ArrayList<>();
        ItemStack menuItem = new ItemStack(Material.GRASS, 1);
        ItemMeta meta4 = menuItem.getItemMeta();
	      meta4.setDisplayName("§a§lCréer votre île");
	      lores.add("§fCommencez votre aventure skyblock");
	      lores.add("§fen créant votre propre île.");
	      lores.add("§fRemplissez des challenges pour gagner");
	      lores.add("§fdes items, des avantages et bien plus!");
	      lores.add("§fVous pouvez inviter d'autres joueurs");
	      lores.add("§fpour partager avec vous la");
	      lores.add("§fconstruction de votre Skyworld!");
	      lores.add("§c\u27A1 §e§lCliquez ici pour commencer!");
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();
        menuItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        final SkullMeta meta2 = (SkullMeta) menuItem.getItemMeta();
        meta2.setDisplayName("§a§lRejoindre une île");
	      lores.add("§fEnvie de rejoindre l'île d'autres joueurs?");
	      lores.add("§e/island accept §fpour rejoindre.");
	      lores.add("§e§l(Vous devez être invité en premier)");
        meta2.setLore(lores);
        menuItem.setItemMeta(meta2);
        menu.setItem(4, menuItem);
        lores.clear();
        menuItem = new ItemStack(Material.SIGN, 1);
        meta4 = menuItem.getItemMeta();
        meta4.setDisplayName("§a§lIsland Aide");
	      lores.add("§fBesoin d'aide avec les concepts");
	      lores.add("§fdu Skyblock ou des commandes?");
	      lores.add("§fTapez /island help ou");
	      lores.add("§e§lCliquez ici pour l'aide!");
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.setItem(8, menuItem);
        lores.clear();
    }

    private void addMainMenu(Inventory menu, Player player) {
        List<String> lores = new ArrayList<>();
        ItemStack menuItem = new ItemStack(Material.GRASS, 1);
        ItemMeta meta4 = menuItem.getItemMeta();
	      meta4.setDisplayName("§6\u2739 §a§lIsland Home §6\u2739");
	      lores.add("§fSe téléporter à votre île.");
	      lores.add("§fVous pouvez changer l'emplacement");
	      lores.add("§fdu home de votre île");
	      lores.add("§fen utilisant §b/island sethome");
	      lores.add("§fou en utilisant le lit au dessous.");
	      lores.add("§c\u27A1 §e§lCliquez ici pour aller au home.");
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.setItem(0, menuItem);
        lores.clear();

        IslandInfo islandInfo = skyBlock.getIslandInfo(player);

        menuItem = new ItemStack(Material.DIAMOND_ORE, 1);
        meta4 = menuItem.getItemMeta();
	      meta4.setDisplayName("§d\u2764 §a§lChallenges §d\u2764");
	      lores.add("§fAfficher la liste des challenges que");
	      lores.add("§fvous pouvez compléter sur votre île");
	      lores.add("§fpour gagner des items, avantages et bien plus!");
        if (skyBlock.getChallengeLogic().isEnabled()) {
        	lores.add("§c\u27A1 §e§lCliquez ici pour voir les challenges.");
        } else {
            lores.add("§4§lChallenges disabled.");
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.setItem(1, menuItem);

        lores.clear();
        menuItem = new ItemStack(Material.EXP_BOTTLE, 1);
        meta4 = menuItem.getItemMeta();
	      meta4.setDisplayName("§b\u2714 §a§lIsland Level §b\u2714");
	      lores.add("§eNiveau(level) actuel: §a" + islandInfo.getLevel());
	      lores.add("§fGagner des levels en agrandissant");
	      lores.add("§fvotre île, en remplissant");
	      lores.add("§fcertains challenges et en");
	      lores.add("§frajoutant des blocs plus rares.");
	      lores.add("§c\u27A1 §e§lCliquez ici pour actualiser");
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.setItem(2, menuItem);
        lores.clear();

        menuItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        final SkullMeta meta2 = (SkullMeta) menuItem.getItemMeta();
        meta2.setDisplayName("§5\u2720 §a§lIsland Group §5\u2720");
        lores.add("§eMembres: §2" + islandInfo.getPartySize() + "/" + islandInfo.getMaxPartySize());
        
        
	      if (!VaultHandler.checkPerk(player.getName(), "usb.party.create", player.getWorld()))
	      {
	        lores.add("§cVous devez avoir le grade");
	        lores.add("§cTravailleur ou + pour pouvoir inviter");
	        lores.add("§cd'autres joueurs sur votre île");
	      }
        
	      lores.add("§fVoir les membres du groupe de");
	      lores.add("§fvotre île et leurs permissions.");
	      lores.add("§fSi vous êtes le chef de l'île, vous pouvez");
	      lores.add("§fmodifier les permissions des membres.");
	      lores.add("§f/island invite <joueur> pour inviter quelqu'un.");
	      lores.add("§c\u27A1 §e§lCliquez ici pour consulter ou modifier.");
	      
        meta2.setLore(lores);
        menuItem.setItemMeta(meta2);
        menu.setItem(3, menuItem);
        lores.clear();

        menuItem = new ItemStack(Material.SAPLING, 1, (short) 3);
        meta4 = menuItem.getItemMeta();
        meta4.setDisplayName("§1\u25CF §a§lIsland Biome §1\u25CF");
	      lores.add("§eBiome Actuel: §b" + islandInfo.getBiome());
	      lores.add("§fLe biome de l'île affecte les items");
	      lores.add("§fcomme la couleur de l'herbe et le spawn");
	      lores.add("§fdes animaux et des monstres...");
        if (islandInfo.hasPerm(player, "canChangeBiome")) {
        	lores.add("§c\u27A1 §e§lCliquez ici pour changer le biome.");
        } else {
        	lores.add("§c§lVous ne pouvez pas changer le biome.");
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.setItem(4, menuItem);
        lores.clear();

        menuItem = new ItemStack(Material.IRON_FENCE, 1);
        meta4 = menuItem.getItemMeta();
        meta4.setDisplayName("§4\u2588 §a§lIsland Lock §4\u2588");
        if (skyBlock.getIslandInfo(player).isLocked()) {
	        lores.add("§eVerouillage(Lock) Status: §aActif");
	        lores.add("§fVotre île est actuellement §7verouillée.");
	        lores.add("§fLes joueurs qui ne sont pas de votre groupe");
	        lores.add("§fsont interdits d'entrer/tp dans votre île.");
            if (islandInfo.hasPerm(player, "canToggleLock")) {
            	lores.add("§c\u27A1 §e§lCliquez ici pour déverouiller votre île.");
            } else {
            	lores.add("§c§lVous ne pouvez pas changer le verouillage.");
            }
        } else {
	        lores.add("§eVerouillage(Lock) Status: §8Inactif");
	        lores.add("§fVotre île est actuellement §adéverouillée.");
	        lores.add("§fLes joueurs qui ne sont pas de votre groupe");
	        lores.add("§fpeuvent entrer/tp dans votre île mais seulement");
	        lores.add("§fvous et votre groupe êtes autorisés à build.");
            if (islandInfo.hasPerm(player, "canToggleLock")) {
  	          lores.add("§c\u27A1 §e§lCliquez ici pour verouiller votre île.");
            } else {
  	          lores.add("§c§lVous ne pouvez pas changer le verouillage.");
            }
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.setItem(5, menuItem);
        lores.clear();

        if (skyBlock.getIslandInfo(player).hasWarp()) {
            menuItem = new ItemStack(Material.ENDER_PORTAL_FRAME, 1);
            meta4 = menuItem.getItemMeta();
	        meta4.setDisplayName("§e\u2739 §a§lIsland Warp §e\u2739");
	        lores.add("§eWarp Status: §aActif");
	        lores.add("§fD'autres joueurs peuvent visiter");
	        lores.add("§fvotre île avec le warp que vous");
	        lores.add("§favez défini à l'aide de §d/island setwarp.");
            if (islandInfo.hasPerm(player, "canToggleWarp") && VaultHandler.checkPerk(player.getName(), "usb.extra.addwarp", uSkyBlock.getSkyBlockWorld())) {
  	          lores.add("§c\u27A1 §e§lCliquez ici pour le désactiver.");
            } else {
  	          lores.add("§c§lVous ne pouvez pas changer le warp.");
            }
        } else {
            menuItem = new ItemStack(Material.ENDER_STONE, 1);
            meta4 = menuItem.getItemMeta();
	        meta4.setDisplayName("§e\u2739 §a§lIsland Warp §e\u2739");
	        lores.add("§eWarp Status: §8Inactif");
	        lores.add("§fD'autres joueurs ne peuvent pas visiter");
	        lores.add("§fvotre île à avec le warp parce que vous");
	        lores.add("§fn'avez pas encore défini le warp");
	        lores.add("§fà l'aide de §d/island setwarp.");
	        lores.add("§fou parce que vous l'avez désactiver.");
            if (islandInfo.hasPerm(player, "canToggleWarp") && VaultHandler.checkPerk(player.getName(), "usb.extra.addwarp", uSkyBlock.getSkyBlockWorld())) {
	        	lores.add("§c\u27A1 §e§lCliquez ici pour Activer.");
            } else {
  	          lores.add("§c§lVous ne pouvez pas changer le warp.");
            }
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.setItem(6, menuItem);
        lores.clear();

        menuItem = new ItemStack(Material.BOOK_AND_QUILL, 1);
        meta4 = menuItem.getItemMeta();
	      meta4.setDisplayName("§4\u2714 §a§lIsland Log §4\u2714");
	      lores.add("§fAfficher les log des événements de");
	      lores.add("§fvotre île comme les changements");
	      lores.add("§fde biome, de warp ou autre.");
	      lores.add("§c\u27A1 §e§lCliquez ici pour afficher les logs.");
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.setItem(8, menuItem); // Last item, first line
        lores.clear();

        menuItem = new ItemStack(Material.BED, 1);
        meta4 = menuItem.getItemMeta();
	      meta4.setDisplayName("§a§lChanger la position de votre home");
	      lores.add("§fLorsque vous vous téléportez à votre");
	      lores.add("§fîle, vous serez amenés à");
	      lores.add("§fcet emplacement.");
	      lores.add("§c\u27A1 §e§lCliquez ici pour le changer.");
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.setItem(9, menuItem); // First item, 2nd line
        lores.clear();

        menuItem = new ItemStack(Material.HOPPER, 1);
        meta4 = menuItem.getItemMeta();
	      meta4.setDisplayName("§a§lChanger la position de votre warp");
	      lores.add("§fLorsque votre warp est activé,");
	      lores.add("§fles autres joueurs auront la possibilité");
	      lores.add("§fde se téléporter à ce point");
	      lores.add("§fde votre île.");
	      lores.add("§c\u27A1 §e§lCliquez ici pour le changer.");
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.setItem(10, menuItem);
        lores.clear();
        
        
        
        
	      menuItem = new ItemStack(Material.NETHERRACK, 1);
	      meta4 = menuItem.getItemMeta();
	      meta4.setDisplayName("§6Nether");
	      lores.add("Aller au nether.");
	      meta4.setLore(lores);
	      menuItem.setItemMeta(meta4);
	      menu.setItem(23, menuItem);
	      lores.clear();
	      
	      
	      menuItem = new ItemStack(Material.EYE_OF_ENDER, 1);
	      meta4 = menuItem.getItemMeta();
	      meta4.setDisplayName("§6End");
	      lores.add("Aller à l'end.");
	      meta4.setLore(lores);
	      menuItem.setItemMeta(meta4);
	      menu.setItem(21, menuItem);
	      lores.clear();
        
        
        
        
	      menuItem = new ItemStack(Material.PUMPKIN, 1);
	      meta4 = menuItem.getItemMeta();
	      meta4.setDisplayName("§6§lDéguisement");
	      lores.add("Se déguiser en mob,");
	      lores.add("block ou joueurs! :D");
	      meta4.setLore(lores);
	      menuItem.setItemMeta(meta4);
	      menu.setItem(30, menuItem);
	      lores.clear();
        
        
        
	      menuItem = new ItemStack(Material.REDSTONE, 1);
	      meta4 = menuItem.getItemMeta();
	      meta4.setDisplayName("§5§lWiEffect");
	      lores.add("§fFaire apparaître des");
	      lores.add("§fparticules autour de vous!");
	      meta4.setLore(lores);
	      menuItem.setItemMeta(meta4);
	      menu.setItem(31, menuItem);
	      lores.clear();
        
        

	      menuItem = new ItemStack(Material.BONE, 1);
	      meta4 = menuItem.getItemMeta();
	      meta4.setDisplayName("§6§lPet");
	      lores.add("§c\u27A1 §e§lCliquez ici pour");
	      lores.add("    §e§lappeler votre");
	      lores.add("    §e§lanimal de compagnie");
	      meta4.setLore(lores);
	      menuItem.setItemMeta(meta4);
	      menu.setItem(32, menuItem);
	      lores.clear();
	      
	      
	      
	      
	      
	      menuItem = new ItemStack(Material.EMERALD, 1);
	      meta4 = menuItem.getItemMeta();
	      meta4.setDisplayName("§a§lGrades");
	      lores.add("§fDisponibles au spawn");
	      meta4.setLore(lores);
	      menuItem.setItemMeta(meta4);
	      menu.setItem(36, menuItem);
	      lores.clear(); 
        
        
	      
	      
	      menuItem = new ItemStack(Material.WOOD_DOOR, 1);
	      meta4 = menuItem.getItemMeta();
	      meta4.setDisplayName("§6§lRetour au spawn");
	      lores.add("§c\u27A1 §e§lCliquez ici pour");
	      lores.add("    §e§laller au spawn!");
	      meta4.setLore(lores);
	      menuItem.setItemMeta(meta4);
	      menu.setItem(43, menuItem);
	      lores.clear();
        
        
	      menuItem = new ItemStack(Material.BLAZE_ROD, 1);
	      meta4 = menuItem.getItemMeta();
	      meta4.setDisplayName("§6§lRetour au lobby");
	      lores.add("§c\u27A1 §e§lCliquez ici pour");
	      lores.add("    §e§lretourner au lobby!");
	      meta4.setLore(lores);
	      menuItem.setItemMeta(meta4);
	      menu.setItem(44, menuItem);
	      lores.clear();
	      

	      

        
        //addExtraMenus(player, menu);
    }

    @SuppressWarnings("deprecation")
	public void onClick(InventoryClickEvent event) {
        if (event == null || event.getCurrentItem() == null || event.getWhoClicked() == null || event.getSlotType() != InventoryType.SlotType.CONTAINER) {
            return; // Bail out, nothing we can do anyway
        }
        Player p = (Player) event.getWhoClicked();
        ItemMeta meta = event.getCurrentItem().getItemMeta();
        SkullMeta skull = meta instanceof SkullMeta ? (SkullMeta) meta : null;
        if (event.getInventory().getName().equalsIgnoreCase("§9Island Group Members")) {
            event.setCancelled(true);
            if (event.getSlot() < 0 || event.getSlot() > 35) {
                return;
            }
            if (meta == null || event.getCurrentItem().getType() == Material.SIGN) {
                p.closeInventory();
                p.openInventory(displayIslandGUI(p));
            } else if (meta.getLore().contains("§a§lLeader")) {
                p.closeInventory();
                p.openInventory(displayPartyGUI(p));
            } else if (!uSkyBlock.getInstance().isPartyLeader(p)) {
                p.closeInventory();
                p.openInventory(displayPartyGUI(p));
            } else if (skull != null) {
                p.closeInventory();
                p.openInventory(displayPartyPlayerGUI(p, skull.getOwner()));
            }
        } else if (event.getInventory().getName().contains("Permissions")) {
            event.setCancelled(true);
            if (event.getSlot() < 0 || event.getSlot() > 35) {
                return;
            }
            String[] playerPerm = event.getInventory().getName().split(" ");
            IslandInfo islandInfo = skyBlock.getIslandInfo(p);
            // TODO: 19/12/2014 - R4zorax: Make this more robust!
            if (event.getCurrentItem().getTypeId() == 6) {
                p.closeInventory();
                islandInfo.togglePerm(playerPerm[0], "canChangeBiome");
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            } else if (event.getCurrentItem().getTypeId() == 101) {
                p.closeInventory();
                islandInfo.togglePerm(playerPerm[0], "canToggleLock");
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            } else if (event.getCurrentItem().getTypeId() == 90) {
                p.closeInventory();
                islandInfo.togglePerm(playerPerm[0], "canChangeWarp");
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            } else if (event.getCurrentItem().getTypeId() == 69) {
                p.closeInventory();
                islandInfo.togglePerm(playerPerm[0], "canToggleWarp");
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            } else if (event.getCurrentItem().getTypeId() == 398) {
                p.closeInventory();
                islandInfo.togglePerm(playerPerm[0], "canInviteOthers");
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            } else if (event.getCurrentItem().getTypeId() == 301) {
                p.closeInventory();
                islandInfo.togglePerm(playerPerm[0], "canKickOthers");
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            } else if (event.getCurrentItem().getTypeId() == 323) {
                p.closeInventory();
                p.openInventory(displayPartyGUI(p));
            } else {
                p.closeInventory();
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            }
        } else if (event.getInventory().getName().contains("Island Biome")) {
            event.setCancelled(true);
            if (event.getSlot() < 0 || event.getSlot() > 35) {
                return;
            }
            if (event.getCurrentItem().getType() == Material.SAPLING && event.getCurrentItem().getDurability() == 3) {
                p.closeInventory();
                p.performCommand("island biome jungle");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.SAPLING && event.getCurrentItem().getDurability() == 1) {
                p.closeInventory();
                p.performCommand("island biome forest");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.SAND) {
                p.closeInventory();
                p.performCommand("island biome desert");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.SNOW) {
                p.closeInventory();
                p.performCommand("island biome taiga");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.EYE_OF_ENDER) {
                p.closeInventory();
                p.performCommand("island biome sky");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.WATER_LILY) {
                p.closeInventory();
                p.performCommand("island biome swampland");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.NETHER_BRICK) {
                p.closeInventory();
                p.performCommand("island biome hell");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.RED_MUSHROOM) {
                p.closeInventory();
                p.performCommand("island biome mushroom");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.LONG_GRASS) {
                p.closeInventory();
                p.performCommand("island biome plains");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.EMERALD_ORE) {
                p.closeInventory();
                p.performCommand("island biome extreme_hills");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.RAW_FISH) {
                p.closeInventory();
                p.performCommand("island biome ocean");
                p.openInventory(displayIslandGUI(p));
            } else {
                p.closeInventory();
                p.openInventory(displayIslandGUI(p));
            }
        } else if (event.getInventory().getName().contains("Challenge Menu (")) {
            event.setCancelled(true);
            Matcher m = CHALLENGE_PAGE_HEADER.matcher(event.getInventory().getName());
            int page = 1;
            int max = 1;
            if (m.find()) {
                page = Integer.parseInt(m.group("p"));
                max = Integer.parseInt(m.group("max"));
            }
            if (event.getSlot() < 0 || event.getSlot() > 54) {
                return;
            }
            
            if (event.getCurrentItem().hasItemMeta()) {
            
            if (event.getCurrentItem().getItemMeta().getDisplayName().equals("§a§l\u21E8 Page suivante")) {
            	 p.closeInventory();
            	 p.openInventory(displayChallengeGUI(p, 2));
            	 return;
            }
            
            else if (event.getCurrentItem().getItemMeta().getDisplayName().equals("§a§l\u21E6 Page précédente")) {
           	 p.closeInventory();
           	 p.openInventory(displayChallengeGUI(p, 1));
           	 return;
           }
            
            else if (event.getCurrentItem().getItemMeta().getDisplayName().equals("§c§l\u2716 Menu principal")) {
              	 p.closeInventory();
              	p.openInventory(displayIslandGUI(p));
              	 return;
              }

            
        }
            
            if ((event.getSlot() % 9) > 0) { // 0,9... are the rank-headers...
                p.closeInventory();
                if (event.getCurrentItem().getItemMeta() != null) {
                    String challenge = event.getCurrentItem().getItemMeta().getDisplayName();
                    String challengeName = uSkyBlock.stripFormatting(challenge);
                    p.performCommand("c c " + challengeName);
                }
                p.openInventory(displayChallengeGUI(p, page));
            } else {
                p.closeInventory();
                if (event.getSlot() < 18) { // Upper half
                    if (page > 1) {
                        p.openInventory(displayChallengeGUI(p, page - 1));
                    } else {
                        p.openInventory(displayIslandGUI(p));
                    }
                } else if (page < max) {
                    p.openInventory(displayChallengeGUI(p, page+1));
                } else {
                    p.openInventory(displayIslandGUI(p));
                }
            }
        } else if (event.getInventory().getName().contains("Island Log")) {
            event.setCancelled(true);
            if (event.getSlot() < 0 || event.getSlot() > 35) {
                return;
            }
            p.closeInventory();
            p.openInventory(displayIslandGUI(p));
        } else if (event.getInventory().getName().contains("Island Menu")) {
            event.setCancelled(true);
            if (event.getSlot() < 0 || event.getSlot() > 35) {
                return;
            }
            PlayerInfo playerInfo = skyBlock.getPlayerInfo(p);
            IslandInfo islandInfo = skyBlock.getIslandInfo(playerInfo);
            if (event.getCurrentItem().getType() == Material.SAPLING && event.getCurrentItem().getDurability() == 3) {
                p.closeInventory();
                p.performCommand("island biome");
            } else if (event.getCurrentItem().getType() == Material.SKULL_ITEM) {
                p.closeInventory();
                p.performCommand("island party");
            } else if (event.getCurrentItem().getType() == Material.BED) {
                p.closeInventory();
                p.performCommand("island sethome");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.HOPPER) {
                p.closeInventory();
                p.performCommand("island setwarp");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.BOOK_AND_QUILL) {
                p.closeInventory();
                p.performCommand("island log");
            } else if (event.getCurrentItem().getType() == Material.GRASS) {
                p.closeInventory();
                p.performCommand("island home");
            } else if (event.getCurrentItem().getType() == Material.EXP_BOTTLE) {
                p.closeInventory();
                p.performCommand("island level");
            } else if (event.getCurrentItem().getType() == Material.DIAMOND_ORE) {
                p.closeInventory();
                p.performCommand("c");
            } else if (event.getCurrentItem().getType() == Material.ENDER_STONE || event.getCurrentItem().getType() == Material.ENDER_PORTAL_FRAME) {
                p.closeInventory();
                p.performCommand("island togglewarp");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.IRON_FENCE && islandInfo.isLocked()) {
                p.closeInventory();
                p.performCommand("island unlock");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.IRON_FENCE && !islandInfo.isLocked()) {
                p.closeInventory();
                p.performCommand("island lock");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.SIGN) {
                p.performCommand("chestcommands open island_help");
            } else if (event.getCurrentItem().getType() == Material.WOODEN_DOOR) {
            	 p.closeInventory();
            	 p.performCommand("spawn");
            } else if (event.getCurrentItem().getType() == Material.BLAZE_ROD) {
          	   p.closeInventory();
          	   ByteArrayOutputStream b = new ByteArrayOutputStream();
          	   DataOutputStream out = new DataOutputStream(b);
          	   try {
          	   out.writeUTF("Connect");
          	   out.writeUTF("lobby");
          	   } catch (IOException ex) {}
   		     p.sendPluginMessage(uSkyBlock.getInstance(), "BungeeCord", b.toByteArray());
   		     
            } else if (event.getCurrentItem().getType() == Material.BONE) {
           	 p.closeInventory();
           	 p.performCommand("pet select");
            } else if (event.getCurrentItem().getType() == Material.REDSTONE) {
              	 p.closeInventory();
              	p.openInventory(displayeffectGUI(p));
            } else if (event.getCurrentItem().getType() == Material.PUMPKIN) {
             	 p.closeInventory();
             	 p.openInventory(displaydisgGUI(p));
            } else if (event.getCurrentItem().getType() == Material.EMERALD) {
            	 p.closeInventory();
            	 p.performCommand("warp grades");
            } else if (event.getCurrentItem().getType() == Material.NETHERRACK) {
           	 p.closeInventory();
           	 if(playerInfo.checkChallenge("enfer") > 0) {
           		World nether = Bukkit.getWorld("world_nether");
           		p.teleport(nether.getSpawnLocation());
           	 } else p.sendMessage(ChatColor.RED + "Vous devez avoir réussi le challenge 'Enfer' pour pouvoir aller dans le Nether");
            } else if (event.getCurrentItem().getType() == Material.EYE_OF_ENDER) {
            	p.closeInventory();
              	 if(playerInfo.checkChallenge("theend") > 0) {
                		World end = Bukkit.getWorld("world_the_end");
                		p.teleport(end.getSpawnLocation());
                	 } else p.sendMessage(ChatColor.RED + "Vous devez avoir réussi le challenge 'The End' pour pouvoir aller dans l'End");
            } else {
                if (!isExtraMenuAction(p, event.getCurrentItem())) {
                    p.closeInventory();
                    p.openInventory(displayIslandGUI(p));
                }
            }
        } else if (event.getInventory().getName().contains("Island Create Menu")) {
            event.setCancelled(true);
            if (event.getSlot() == 0) {
                p.closeInventory();
                p.performCommand("island create");
            } else if (event.getCurrentItem().getType() == Material.SKULL_ITEM) {
                p.closeInventory();
                p.performCommand("island accept");
            } else if (event.getCurrentItem().getType() == Material.SIGN) {
                p.closeInventory();
                p.sendMessage("§a==>http://craftzone.fr/forum/");
            }
        }
    }

    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getName().equalsIgnoreCase("§9SB Island Group Members")) {
            event.setCancelled(true);
            SkullMeta meta = (SkullMeta) event.getCursor().getItemMeta();
            Player p = (Player) event.getWhoClicked();
            p.updateInventory();
            p.closeInventory();
            if (meta.getOwner() == null) {
                p.openInventory(displayPartyGUI(p));
            } else {
                p.openInventory(displayPartyPlayerGUI(p, meta.getOwner()));
            }
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    ItemStack currenteffectItem = null;
    ItemStack currentdisgItem = null;
    public Inventory GUIeffect = null;
    public Inventory GUIdisg = null;
    List<String> lores = new ArrayList<>();
    
 
    public Inventory displayeffectGUI(Player player)
    {
      GUIeffect = Bukkit.createInventory(null, 18, "§6WiEffect");
      
      currenteffectItem = new ItemStack(Material.ENDER_PORTAL_FRAME, 1);
      ItemMeta meta1 = currenteffectItem.getItemMeta();
      meta1.setDisplayName("§3\u2739 §e§lSmoke §3\u2739");
      lores.add("§fEffect fumée");
      lores.add("§2Travailleur et +");
      meta1.setLore(lores);
      currenteffectItem.setItemMeta(meta1);
      GUIeffect.addItem(new ItemStack[] { currenteffectItem });
      lores.clear();
     
      //specialiste 4
      
      currenteffectItem = new ItemStack(Material.QUARTZ, 1);
      ItemMeta meta2 = currenteffectItem.getItemMeta();
      meta2.setDisplayName("§3\u2739 §e§lStars §3\u2739");
      lores.add("§fEffect étoiles 1");
      lores.add("§3Spécialiste et +");
      meta2.setLore(lores);
      currenteffectItem.setItemMeta(meta2);
      GUIeffect.addItem(new ItemStack[] { currenteffectItem });
      lores.clear();
      

      currenteffectItem = new ItemStack(Material.FLINT, 1);
      ItemMeta meta3 = currenteffectItem.getItemMeta();
      meta3.setDisplayName("§3\u2739 §e§lCrit §3\u2739");
      lores.add("§fEffect étoiles 2");
      lores.add("§3Spécialiste et +");
      meta3.setLore(lores);
      currenteffectItem.setItemMeta(meta3);
      GUIeffect.addItem(new ItemStack[] { currenteffectItem });
      lores.clear();
      
      
      currenteffectItem = new ItemStack(Material.NETHER_STAR, 1);
      ItemMeta meta4 = currenteffectItem.getItemMeta();
      meta4.setDisplayName("§3\u2739 §e§lSparks §3\u2739");
      lores.add("§fEffect étoiles 3");
      lores.add("§3Spécialiste et +");
      meta4.setLore(lores);
      currenteffectItem.setItemMeta(meta4);
      GUIeffect.addItem(new ItemStack[] { currenteffectItem });
      lores.clear();
      
      
      currenteffectItem = new ItemStack(Material.ENDER_PEARL, 1);
      ItemMeta meta5 = currenteffectItem.getItemMeta();
      meta5.setDisplayName("§3\u2739 §e§lEnder §3\u2739");
      lores.add("§fEffect ender");
      lores.add("§3Spécialiste et +");
      meta5.setLore(lores);
      currenteffectItem.setItemMeta(meta5);
      GUIeffect.addItem(new ItemStack[] { currenteffectItem });
      lores.clear();
      
      //conseiller 4
      
      currenteffectItem = new ItemStack(Material.LAVA_BUCKET, 1);
      ItemMeta metac1 = currenteffectItem.getItemMeta();
      metac1.setDisplayName("§3\u2739 §e§lFire §3\u2739");
      lores.add("§fEffect feu");
      lores.add("§1Conseiller et +");
      metac1.setLore(lores);
      currenteffectItem.setItemMeta(metac1);
      GUIeffect.addItem(new ItemStack[] { currenteffectItem });
      lores.clear();
      
      
      currenteffectItem = new ItemStack(Material.REDSTONE, 1);
      ItemMeta metac2 = currenteffectItem.getItemMeta();
      metac2.setDisplayName("§3\u2739 §e§lBlood §3\u2739");
      lores.add("§fEffect redstone");
      lores.add("§1Conseiller et +");
      metac2.setLore(lores);
      currenteffectItem.setItemMeta(metac2);
      GUIeffect.addItem(new ItemStack[] { currenteffectItem });
      lores.clear();
      
      currenteffectItem = new ItemStack(Material.WATER_BUCKET, 1);
      ItemMeta metac3 = currenteffectItem.getItemMeta();
      metac3.setDisplayName("§3\u2739 §e§lSweat §3\u2739");
      lores.add("§fEffect eau");
      lores.add("§1Conseiller et +");
      metac3.setLore(lores);
      currenteffectItem.setItemMeta(metac3);
      GUIeffect.addItem(new ItemStack[] { currenteffectItem });
      lores.clear();
      
      
      currenteffectItem = new ItemStack(Material.SNOW_BALL, 1);
      ItemMeta metac4 = currenteffectItem.getItemMeta();
      metac4.setDisplayName("§3\u2739 §e§lSnow §3\u2739");
      lores.add("§fEffect boules de neige");
      lores.add("§1Conseiller et +");
      metac4.setLore(lores);
      currenteffectItem.setItemMeta(metac4);
      GUIeffect.addItem(new ItemStack[] { currenteffectItem });
      lores.clear();
      
      
    //cEpert 6
      
      
      currenteffectItem = new ItemStack(Material.MAGMA_CREAM, 1);
      ItemMeta metae1 = currenteffectItem.getItemMeta();
      metae1.setDisplayName("§3\u2739 §e§lMagma §3\u2739");
      lores.add("§fEffect magma");
      lores.add("§dExpert et +");
      metae1.setLore(lores);
      currenteffectItem.setItemMeta(metae1);
      GUIeffect.addItem(new ItemStack[] { currenteffectItem });
      lores.clear();
      
      currenteffectItem = new ItemStack(Material.REDSTONE_BLOCK, 1);
      ItemMeta metae2 = currenteffectItem.getItemMeta();
      metae2.setDisplayName("§3\u2739 §e§lHearts §3\u2739");
      lores.add("§fEffect coeurs");
      lores.add("§dExpert et +");
      metae2.setLore(lores);
      currenteffectItem.setItemMeta(metae2);
      GUIeffect.addItem(new ItemStack[] { currenteffectItem });
      lores.clear();
      
      
      currenteffectItem = new ItemStack(Material.JUKEBOX, 1);
      ItemMeta metae3 = currenteffectItem.getItemMeta();
      metae3.setDisplayName("§3\u2739 §e§lMusic §3\u2739");
      lores.add("§fEffect musique");
      lores.add("§dExpert et +");
      metae3.setLore(lores);
      currenteffectItem.setItemMeta(metae3);
      GUIeffect.addItem(new ItemStack[] { currenteffectItem });
      lores.clear();
      
      
      currenteffectItem = new ItemStack(Material.ENCHANTMENT_TABLE, 1);
      ItemMeta metae4 = currenteffectItem.getItemMeta();
      metae4.setDisplayName("§3\u2739 §e§lMagic §3\u2739");
      lores.add("§fEffect magique");
      lores.add("§dExpert et +");
      metae4.setLore(lores);
      currenteffectItem.setItemMeta(metae4);
      GUIeffect.addItem(new ItemStack[] { currenteffectItem });
      lores.clear();
      
      currenteffectItem = new ItemStack(Material.EXP_BOTTLE, 1);
      ItemMeta metae5 = currenteffectItem.getItemMeta();
      metae5.setDisplayName("§3\u2739 §e§lHappy §3\u2739");
      lores.add("§fEffect happy");
      lores.add("§dExpert et +");
      metae5.setLore(lores);
      currenteffectItem.setItemMeta(metae5);
      GUIeffect.addItem(new ItemStack[] { currenteffectItem });
      lores.clear();
      
      
      currenteffectItem = new ItemStack(Material.REDSTONE_LAMP_ON, 1);
      ItemMeta metae6 = currenteffectItem.getItemMeta();
      metae6.setDisplayName("§3\u2739 §e§lDisco §3\u2739");
      lores.add("§fEffect disco");
      lores.add("§dExpert et +");
      metae6.setLore(lores);
      currenteffectItem.setItemMeta(metae6);
      GUIeffect.addItem(new ItemStack[] { currenteffectItem });
      lores.clear();
      
      
      currenteffectItem = new ItemStack(Material.SIGN, 1);
      ItemMeta metar = currenteffectItem.getItemMeta();
      metar.setDisplayName("§hWiEffect");
      lores.add("§eCliquez ici pour retourner");
      lores.add("§eau menu principal.");
      metar.setLore(lores);
      currenteffectItem.setItemMeta(metar);
      GUIeffect.setItem(17, currenteffectItem);
      lores.clear();
      
      
      return GUIeffect;
    } 
    

    
    
    
    public Inventory displaydisgGUI(Player player)
    {
      GUIdisg = Bukkit.createInventory(null, 45, "§6Déguisements");
      
      currentdisgItem = new ItemStack(Material.MONSTER_EGG, 1, (short)65);
      ItemMeta meta1 = currentdisgItem.getItemMeta();
      meta1.setDisplayName("Chauve-souris");
      lores.add("§dExpert et +");
      meta1.setLore(lores);
      currentdisgItem.setItemMeta(meta1);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
     
      
      currentdisgItem = new ItemStack(Material.MONSTER_EGG, 1, (short)61);
      ItemMeta meta2 = currentdisgItem.getItemMeta();
      meta2.setDisplayName("Blaze");
      lores.add("§dExpert et +");
      meta2.setLore(lores);
      currentdisgItem.setItemMeta(meta2);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      currentdisgItem = new ItemStack(Material.MONSTER_EGG, 1, (short)59);
      ItemMeta meta3 = currentdisgItem.getItemMeta();
      meta3.setDisplayName("Araignée bleue");
      lores.add("§1Conseiller et +");
      meta3.setLore(lores);
      currentdisgItem.setItemMeta(meta3);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      currentdisgItem = new ItemStack(Material.MONSTER_EGG, 1, (short)93);
      ItemMeta meta4 = currentdisgItem.getItemMeta();
      meta4.setDisplayName("Poulet");
      lores.add("§3Spécialiste et +");
      meta4.setLore(lores);
      currentdisgItem.setItemMeta(meta4);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      currentdisgItem = new ItemStack(Material.MONSTER_EGG, 1, (short)92);
      ItemMeta meta5 = currentdisgItem.getItemMeta();
      meta5.setDisplayName("Vache");
      lores.add("§3Spécialiste et +");
      meta5.setLore(lores);
      currentdisgItem.setItemMeta(meta5);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      currentdisgItem = new ItemStack(Material.MONSTER_EGG, 1, (short)50);
      ItemMeta meta27 = currentdisgItem.getItemMeta();
      meta27.setDisplayName("Creeper");
      lores.add("§1Conseiller et +");
      meta27.setLore(lores);
      currentdisgItem.setItemMeta(meta27);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      currentdisgItem = new ItemStack(Material.DRAGON_EGG, 1);
      ItemMeta meta6 = currentdisgItem.getItemMeta();
      meta6.setDisplayName("EnderDragon");
      lores.add("NP");
      meta6.setLore(lores);
      currentdisgItem.setItemMeta(meta6);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      currentdisgItem = new ItemStack(Material.MONSTER_EGG, 1, (short)58);
      ItemMeta meta7 = currentdisgItem.getItemMeta();
      meta7.setDisplayName("Enderman");
      lores.add("§dExpert et +");
      meta7.setLore(lores);
      currentdisgItem.setItemMeta(meta7);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      currentdisgItem = new ItemStack(Material.MONSTER_EGG, 1, (short)56);
      ItemMeta meta8 = currentdisgItem.getItemMeta();
      meta8.setDisplayName("Ghast");
      lores.add("NP");
      meta8.setLore(lores);
      currentdisgItem.setItemMeta(meta8);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      currentdisgItem = new ItemStack(Material.MONSTER_EGG, 1, (short)100);
      ItemMeta meta9 = currentdisgItem.getItemMeta();
      meta9.setDisplayName("Cheval");
      lores.add("§3Spécialiste et +");
      meta9.setLore(lores);
      currentdisgItem.setItemMeta(meta9);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      currentdisgItem = new ItemStack(Material.IRON_BLOCK, 1);
      ItemMeta meta10 = currentdisgItem.getItemMeta();
      meta10.setDisplayName("Golem de fer");
      lores.add("§dExpert et +");
      meta10.setLore(lores);
      currentdisgItem.setItemMeta(meta10);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      currentdisgItem = new ItemStack(Material.MONSTER_EGG, 1, (short)62);
      ItemMeta meta11 = currentdisgItem.getItemMeta();
      meta11.setDisplayName("Magma Cube");
      lores.add("§1Conseiller et +");
      meta11.setLore(lores);
      currentdisgItem.setItemMeta(meta11);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      currentdisgItem = new ItemStack(Material.MONSTER_EGG, 1, (short)96);
      ItemMeta meta12 = currentdisgItem.getItemMeta();
      meta12.setDisplayName("Mooshroom");
      lores.add("§1Conseiller et +");
      meta12.setLore(lores);
      currentdisgItem.setItemMeta(meta12);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      currentdisgItem = new ItemStack(Material.MONSTER_EGG, 1, (short)98);
      ItemMeta meta13 = currentdisgItem.getItemMeta();
      meta13.setDisplayName("Ocelot");
      lores.add("§3Spécialiste et +");
      meta13.setLore(lores);
      currentdisgItem.setItemMeta(meta13);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      currentdisgItem = new ItemStack(Material.MONSTER_EGG, 1, (short)90);
      ItemMeta meta14 = currentdisgItem.getItemMeta();
      meta14.setDisplayName("Cochon");
      lores.add("§3Spécialiste et +");
      meta14.setLore(lores);
      currentdisgItem.setItemMeta(meta14);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      currentdisgItem = new ItemStack(Material.MONSTER_EGG, 1, (short)57);
      ItemMeta meta15 = currentdisgItem.getItemMeta();
      meta15.setDisplayName("Zombie Pigman");
      lores.add("§1Conseiller et +");
      meta15.setLore(lores);
      currentdisgItem.setItemMeta(meta15);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      currentdisgItem = new ItemStack(Material.MONSTER_EGG, 1, (short)91);
      ItemMeta meta16 = currentdisgItem.getItemMeta();
      meta16.setDisplayName("Mouton");
      lores.add("§1Conseiller et +");
      meta16.setLore(lores);
      currentdisgItem.setItemMeta(meta16);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      currentdisgItem = new ItemStack(Material.MONSTER_EGG, 1, (short)60);
      ItemMeta meta17 = currentdisgItem.getItemMeta();
      meta17.setDisplayName("Silverfish");
      lores.add("§dExpert et +");
      meta17.setLore(lores);
      currentdisgItem.setItemMeta(meta17);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      currentdisgItem = new ItemStack(Material.MONSTER_EGG, 1, (short)51);
      ItemMeta meta18 = currentdisgItem.getItemMeta();
      meta18.setDisplayName("Squelette");
      lores.add("§1Conseiller et +");
      meta18.setLore(lores);
      currentdisgItem.setItemMeta(meta18);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      currentdisgItem = new ItemStack(Material.MONSTER_EGG, 1, (short)55);
      ItemMeta meta19 = currentdisgItem.getItemMeta();
      meta19.setDisplayName("Slime");
      lores.add("§1Conseiller et +");
      meta19.setLore(lores);
      currentdisgItem.setItemMeta(meta19);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      currentdisgItem = new ItemStack(Material.MONSTER_EGG, 1, (short)52);
      ItemMeta meta20 = currentdisgItem.getItemMeta();
      meta20.setDisplayName("Araignée");
      lores.add("§3Spécialiste et +");
      meta20.setLore(lores);
      currentdisgItem.setItemMeta(meta20);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      currentdisgItem = new ItemStack(Material.MONSTER_EGG, 1, (short)94);
      ItemMeta meta21 = currentdisgItem.getItemMeta();
      meta21.setDisplayName("Poulpe");
      lores.add("§dExpert et +");
      meta21.setLore(lores);
      currentdisgItem.setItemMeta(meta21);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      currentdisgItem = new ItemStack(Material.MONSTER_EGG, 1, (short)120);
      ItemMeta meta22 = currentdisgItem.getItemMeta();
      meta22.setDisplayName("Villageois");
      lores.add("§3Spécialiste et +");
      meta22.setLore(lores);
      currentdisgItem.setItemMeta(meta22);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      currentdisgItem = new ItemStack(Material.MONSTER_EGG, 1, (short)66);
      ItemMeta meta23 = currentdisgItem.getItemMeta();
      meta23.setDisplayName("Sorcière");
      lores.add("§dExpert et +");
      meta23.setLore(lores);
      currentdisgItem.setItemMeta(meta23);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      currentdisgItem = new ItemStack(Material.NETHER_STAR, 1);
      ItemMeta meta24 = currentdisgItem.getItemMeta();
      meta24.setDisplayName("Wither");
      lores.add("NP");
      meta24.setLore(lores);
      currentdisgItem.setItemMeta(meta24);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      currentdisgItem = new ItemStack(Material.MONSTER_EGG, 1, (short)95);
      ItemMeta meta25 = currentdisgItem.getItemMeta();
      meta25.setDisplayName("Loup");
      lores.add("§1Conseiller et +");
      meta25.setLore(lores);
      currentdisgItem.setItemMeta(meta25);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      currentdisgItem = new ItemStack(Material.MONSTER_EGG, 1, (short)54);
      ItemMeta meta26 = currentdisgItem.getItemMeta();
      meta26.setDisplayName("Zombie");
      lores.add("§3Spécialiste et +");
      meta26.setLore(lores);
      currentdisgItem.setItemMeta(meta26);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      
      
      currentdisgItem = new ItemStack(Material.STONE,1);
      ItemMeta metab1 = currentdisgItem.getItemMeta();
      metab1.setDisplayName("Stone");
      lores.add("§1Conseiller et +");
      metab1.setLore(lores);
      currentdisgItem.setItemMeta(metab1);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      
      currentdisgItem = new ItemStack(Material.GRASS,1);
      ItemMeta metab2 = currentdisgItem.getItemMeta();
      metab2.setDisplayName("Grass");
      lores.add("§dExpert et +");
      metab2.setLore(lores);
      currentdisgItem.setItemMeta(metab2);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      
      currentdisgItem = new ItemStack(Material.BEDROCK,1);
      ItemMeta metab3 = currentdisgItem.getItemMeta();
      metab3.setDisplayName("Bedrock");
      lores.add("§dExpert et +");
      metab3.setLore(lores);
      currentdisgItem.setItemMeta(metab3);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      
      currentdisgItem = new ItemStack(Material.SPONGE,1);
      ItemMeta metab4 = currentdisgItem.getItemMeta();
      metab4.setDisplayName("Eponge");
      lores.add("§dExpert et +");
      metab4.setLore(lores);
      currentdisgItem.setItemMeta(metab4);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      
      
      currentdisgItem = new ItemStack(Material.NETHERRACK,1);
      ItemMeta metab5 = currentdisgItem.getItemMeta();
      metab5.setDisplayName("Roche du nether");
      lores.add("§dExpert et +");
      metab5.setLore(lores);
      currentdisgItem.setItemMeta(metab5);
      GUIdisg.addItem(new ItemStack[] { currentdisgItem });
      lores.clear();
      
      


      currentdisgItem = new ItemStack(Material.STONE_SWORD, 1);
      ItemMeta metae = currentdisgItem.getItemMeta();
      metae.setDisplayName("Supprimer le déguisement");
      lores.add("");
      metae.setLore(lores);
      currentdisgItem.setItemMeta(metae);
      GUIdisg.setItem(43, currentdisgItem);
      lores.clear();
      
      
      currentdisgItem = new ItemStack(Material.SIGN, 1);
      ItemMeta metar = currentdisgItem.getItemMeta();
      metar.setDisplayName("§bDéguisement");
      lores.add("§eCliquez ici pour retourner");
      lores.add("§eau menu principal.");
      metar.setLore(lores);
      currentdisgItem.setItemMeta(metar);
      GUIdisg.setItem(44, currentdisgItem);
      lores.clear();

    
    
      return GUIdisg;
    } 
    
    

	
	  public void guiGadget(InventoryClickEvent event) {
		
		  Player p = ((Player)event.getWhoClicked());

		   if (event.getInventory().getName().contains("Déguisement"))
			   
		      {
		    	  event.setCancelled(true);
		          if ((event.getSlot() < 0) || (event.getSlot() > 62))
		              return;
		    	  
		          
		          //if (event.getCurrentItem().getType() == Material.PORTAL)
		        if ((event.getCurrentItem().getType() == Material.MONSTER_EGG) && (event.getCurrentItem().getDurability() == 65))
		          {

		            p.closeInventory();
		            p.performCommand("d bat");
		          }
		    
		        
		        else if ((event.getCurrentItem().getType() == Material.MONSTER_EGG) && (event.getCurrentItem().getDurability() == 61))
		        {

		          p.closeInventory();
		          p.performCommand("d blaze");
		        }
		        
		        
		        else if ((event.getCurrentItem().getType() == Material.MONSTER_EGG) && (event.getCurrentItem().getDurability() == 59))
		        {

		          p.closeInventory();
		          p.performCommand("d cavespider");
		        }
		        
		        
		        else if ((event.getCurrentItem().getType() == Material.MONSTER_EGG) && (event.getCurrentItem().getDurability() == 93))
		        {

		          p.closeInventory();
		          p.performCommand("d chicken");
		        }
		        
		        
		        else if ((event.getCurrentItem().getType() == Material.MONSTER_EGG) && (event.getCurrentItem().getDurability() == 92))
		        {

		          p.closeInventory();
		          p.performCommand("d cow");
		        }
		        
		        
		        else if ((event.getCurrentItem().getType() == Material.MONSTER_EGG) && (event.getCurrentItem().getDurability() == 50))
		        {

		          p.closeInventory();
		          p.performCommand("d creeper");
		        }
		        
		        
		        else if (event.getCurrentItem().getType() == Material.DRAGON_EGG)
		        {

		          p.closeInventory();
		          p.performCommand("d enderdragon");
		        }
		        
		        
		        else if ((event.getCurrentItem().getType() == Material.MONSTER_EGG) && (event.getCurrentItem().getDurability() == 58))
		        {

		          p.closeInventory();
		          p.performCommand("d enderman");
		        }
		        
		        else if ((event.getCurrentItem().getType() == Material.MONSTER_EGG) && (event.getCurrentItem().getDurability() == 56))
		        {

		          p.closeInventory();
		          p.performCommand("d ghast");
		        }
		        
		        
		        else if ((event.getCurrentItem().getType() == Material.MONSTER_EGG) && (event.getCurrentItem().getDurability() == 100))
		        {

		          p.closeInventory();
		          p.performCommand("d horse");
		        }
		        
		        
		        else if (event.getCurrentItem().getType() == Material.IRON_BLOCK)
		        {

		          p.closeInventory();
		          p.performCommand("d irongolem");
		        }
		        
		        
		        else if ((event.getCurrentItem().getType() == Material.MONSTER_EGG) && (event.getCurrentItem().getDurability() == 62))
		        {

		          p.closeInventory();
		          p.performCommand("d magmacube");
		        }
		        
		        
		        else if ((event.getCurrentItem().getType() == Material.MONSTER_EGG) && (event.getCurrentItem().getDurability() == 96))
		        {

		          p.closeInventory();
		          p.performCommand("d mushroomcow");
		        }
		        
		        
		        else if ((event.getCurrentItem().getType() == Material.MONSTER_EGG) && (event.getCurrentItem().getDurability() == 98))
		        {

		          p.closeInventory();
		          p.performCommand("d ocelot");
		        }
		        
		        
		        else if ((event.getCurrentItem().getType() == Material.MONSTER_EGG) && (event.getCurrentItem().getDurability() == 90))
		        {

		          p.closeInventory();
		          p.performCommand("d pig");
		        }
		        
		        
		        else if ((event.getCurrentItem().getType() == Material.MONSTER_EGG) && (event.getCurrentItem().getDurability() == 57))
		        {

		          p.closeInventory();
		          p.performCommand("d pigzombie");
		        }
		        
		        
		        else if ((event.getCurrentItem().getType() == Material.MONSTER_EGG) && (event.getCurrentItem().getDurability() == 91))
		        {

		          p.closeInventory();
		          p.performCommand("d sheep");
		        }
		        
		        
		        else if ((event.getCurrentItem().getType() == Material.MONSTER_EGG) && (event.getCurrentItem().getDurability() == 60))
		        {

		          p.closeInventory();
		          p.performCommand("d silverfish");
		        }
		        
		        
		        else if ((event.getCurrentItem().getType() == Material.MONSTER_EGG) && (event.getCurrentItem().getDurability() == 51))
		        {

		          p.closeInventory();
		          p.performCommand("d skeleton");
		        }
		        
		        
		        else if ((event.getCurrentItem().getType() == Material.MONSTER_EGG) && (event.getCurrentItem().getDurability() == 55))
		        {

		          p.closeInventory();
		          p.performCommand("d slime");
		        }
		        
		        
		        else if ((event.getCurrentItem().getType() == Material.MONSTER_EGG) && (event.getCurrentItem().getDurability() == 52))
		        {

		          p.closeInventory();
		          p.performCommand("d spider");
		        }
		        
		        
		        else if ((event.getCurrentItem().getType() == Material.MONSTER_EGG) && (event.getCurrentItem().getDurability() == 94))
		        {

		          p.closeInventory();
		          p.performCommand("d squid");
		        }
		        
		        
		        
		        else if ((event.getCurrentItem().getType() == Material.MONSTER_EGG) && (event.getCurrentItem().getDurability() == 120))
		        {

		          p.closeInventory();
		          p.performCommand("d villager");
		        }
		        
		        
		        else if ((event.getCurrentItem().getType() == Material.MONSTER_EGG) && (event.getCurrentItem().getDurability() == 66))
		        {

		          p.closeInventory();
		          p.performCommand("d witch");
		        }
		        
		        
		        
		        else if (event.getCurrentItem().getType() == Material.NETHER_STAR)
		        {

		          p.closeInventory();
		          p.performCommand("d wither");
		        }
		        
		        else if ((event.getCurrentItem().getType() == Material.MONSTER_EGG) && (event.getCurrentItem().getDurability() == 95))
		        {

		          p.closeInventory();
		          p.performCommand("d wolf");
		        }
		        
		        
		        
		        else if ((event.getCurrentItem().getType() == Material.MONSTER_EGG) && (event.getCurrentItem().getDurability() == 54))
		        {

		          p.closeInventory();
		          p.performCommand("d zombie");
		        }
		        
		       //block
		        
		        else if  (event.getCurrentItem().getType() == Material.STONE)
		        {

		          p.closeInventory();
		          p.performCommand("d falling_block 1");
		        }
		        
		        else if  (event.getCurrentItem().getType() == Material.GRASS)
		        {

		          p.closeInventory();
		          p.performCommand("d falling_block 2");
		        }
		        
		        else if  (event.getCurrentItem().getType() == Material.BEDROCK)
		        {

		          p.closeInventory();
		          p.performCommand("d falling_block 7");
		        }
		        
		        else if  (event.getCurrentItem().getType() == Material.SPONGE)
		        {

		          p.closeInventory();
		          p.performCommand("d falling_block 19");
		        }
		        
		        
		        else if  (event.getCurrentItem().getType() == Material.NETHERRACK)
		        {

		          p.closeInventory();
		          p.performCommand("d falling_block 87");
		        }
		        
		        
		        else if  (event.getCurrentItem().getType() == Material.STONE_SWORD)
		        {
		          p.closeInventory();
		          p.performCommand("u");
		        }
		        
		        else if ((event.getCurrentItem().getType() == Material.SIGN))
		        {
		          p.closeInventory();
		          p.performCommand("is");
		        }
		        
		 }	        
		       
		   
		   
		      else if (event.getInventory().getName().contains("WiEffect"))
		      {
		    	  event.setCancelled(true);
		          if ((event.getSlot() < 0) || (event.getSlot() > 35))
		              return;
		    	  
		    	  
		          
		          p = ((Player)event.getWhoClicked());
		          
		          if (event.getCurrentItem().getType() == Material.ENDER_PORTAL_FRAME)
		          {

		            p.closeInventory();
		            p.performCommand("trail smoke");
		          }
		          
		        //specialiste 4
		          
		          else if (event.getCurrentItem().getType() == Material.QUARTZ)
		          {

		            p.closeInventory();
		            p.performCommand("trail stars");
		          }
		          
		          else if (event.getCurrentItem().getType() == Material.FLINT)
		          {

		            p.closeInventory();
		            p.performCommand("trail crit");
		          }
		          
		          
		          else if (event.getCurrentItem().getType() == Material.NETHER_STAR)
		          {

		            p.closeInventory();
		            p.performCommand("trail sparks");
		          }
		          
		          else if (event.getCurrentItem().getType() == Material.ENDER_PEARL)
		          {

		            p.closeInventory();
		            p.performCommand("trail ender");
		          }
		          
		          
		        //conseiller
		          
		          else if (event.getCurrentItem().getType() == Material.LAVA_BUCKET)
		          {

		            p.closeInventory();
		            p.performCommand("trail fire");
		          }
		          
		          else if (event.getCurrentItem().getType() == Material.REDSTONE)
		          {

		            p.closeInventory();
		            p.performCommand("trail blood");
		          }
		          
		          else if (event.getCurrentItem().getType() == Material.WATER_BUCKET)
		          {

		            p.closeInventory();
		            p.performCommand("trail sweat");
		          }
		          
		          else if (event.getCurrentItem().getType() == Material.SNOW_BALL)
		          {

		            p.closeInventory();
		            p.performCommand("trail clouds");
		          }
		          
		          
		          //expert 6 
		          
		          else if (event.getCurrentItem().getType() == Material.MAGMA_CREAM)
		          {

		            p.closeInventory();
		            p.performCommand("trail magma");
		          }
		          
		          
		          else if (event.getCurrentItem().getType() == Material.REDSTONE_BLOCK)
		          {

		            p.closeInventory();
		            p.performCommand("trail hearts");
		          }
		          
		          
		          else if (event.getCurrentItem().getType() == Material.JUKEBOX)
		          {

		            p.closeInventory();
		            p.performCommand("trail music");
		          }
		          
		          
		          else if (event.getCurrentItem().getType() == Material.ENCHANTMENT_TABLE)
		          {

		            p.closeInventory();
		            p.performCommand("trail magic");
		          }
		          
		          
		          else if (event.getCurrentItem().getType() == Material.EXP_BOTTLE)
		          {

		            p.closeInventory();
		            p.performCommand("trail happy");
		          }
		          
		          
		          else if (event.getCurrentItem().getType() == Material.REDSTONE_LAMP_ON)
		          {

		            p.closeInventory();
		            p.performCommand("trail disco");
		          }
		          
		          
		          else if ((event.getCurrentItem().getType() == Material.SIGN))
		          {

		            p.closeInventory();
		            p.performCommand("is");
		          }
		          
		     
		    	  
		      
		    }
		    
		   
		   
		
	}
    
    
}
