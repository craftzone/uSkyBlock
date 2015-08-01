package us.talabrek.ultimateskyblock.challenge;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import us.talabrek.ultimateskyblock.handler.VaultHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static us.talabrek.ultimateskyblock.challenge.ChallengeLogic.*;

/**
 * The data-object for a challenge
 */
public class Challenge {
    public static final Pattern REQ_PATTERN = Pattern.compile("(?<type>[0-9]+)(:(?<subtype>[0-9]+))?:(?<amount>[0-9]+)(;(?<op>[+\\-*])(?<inc>[0-9]+))?");

    enum Type { PLAYER, ISLAND, ISLAND_LEVEL;
        static Type from(String s) {
            if (s == null || s.trim().isEmpty() || s.trim().toLowerCase().equals("onplayer")) {
                return PLAYER;
            } else if (s != null && s.equalsIgnoreCase("islandlevel")) {
                return ISLAND_LEVEL;
            }
            return ISLAND;
        }
    }
    private final String name;
    private final String description;
    private final Type type;
    private final String requiredItems;
    private final List<EntityMatch> requiredEntities;
    private final Rank rank;
    private final int resetInHours;
    private final ItemStack displayItem;
    private final boolean takeItems;
    private final int radius;
    private final Reward reward;
    private final Reward repeatReward;

    public Challenge(String name, String description, Type type, String requiredItems, List<EntityMatch> requiredEntities, Rank rank, int resetInHours, ItemStack displayItem, boolean takeItems, int radius, Reward reward, Reward repeatReward) {
        this.name = name;
        this.type = type;
        this.requiredItems = requiredItems;
        this.requiredEntities = requiredEntities;
        this.rank = rank;
        this.resetInHours = resetInHours;
        this.displayItem = displayItem;
        this.takeItems = takeItems;
        this.radius = radius;
        this.reward = reward;
        this.repeatReward = repeatReward;
        this.description = description;
    }

    public boolean isRepeatable() {
        return repeatReward != null;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public int getRadius() {
        return radius;
    }

    public int getRequiredLevel() {
        if (type == Type.ISLAND_LEVEL) {
            return Integer.parseInt(requiredItems, 10);
        }
        return 0;
    }

    @SuppressWarnings("deprecation")
	public List<ItemStack> getRequiredItems(int timesCompleted) {
        List<ItemStack> items = new ArrayList<>();
        for (String item : requiredItems.split(" ")) {
            Matcher m = REQ_PATTERN.matcher(item);
            if (m.matches()) {
                int reqItem = Integer.parseInt(m.group("type"), 10);
                int subType = m.group("subtype") != null ? Integer.parseInt(m.group("subtype"), 10) : 0;
                int amount = Integer.parseInt(m.group("amount"), 10);
                char op = m.group("op") != null ? m.group("op").charAt(0) : 0;
                int inc = m.group("inc") != null ? Integer.parseInt(m.group("inc"), 10) : 0;
                amount = calcAmount(amount, op, inc, timesCompleted);
                ItemStack mat = new ItemStack(reqItem, amount, (short) subType);
                ItemMeta meta = mat.getItemMeta();
                meta.setDisplayName("§f" + amount + " " + fitemee(VaultHandler.getItemName(mat)));
                mat.setItemMeta(meta);
                items.add(mat);
            }
        }
        return items;
    }

    public List<EntityMatch> getRequiredEntities() {
        return requiredEntities;
    }

    public Rank getRank() {
        return rank;
    }

    public int getResetInHours() {
        return resetInHours;
    }

    public ItemStack getDisplayItem(ChallengeCompletion completion, boolean withCurrency) {
        ItemStack currentChallengeItem = getDisplayItem();
        ItemMeta meta = currentChallengeItem.getItemMeta();
        List<String> lores = new ArrayList<>();
        lores.add("§7" + getDescription());
        int timesCompleted = completion.getTimesCompletedSinceTimer();
        Reward reward = getReward();
        if (completion.getTimesCompleted() > 0 && isRepeatable()) {
            if (completion.isOnCooldown()) {
                long cooldown = completion.getCooldownInMillis();
                if (cooldown >= MS_DAY) {
                    final int days = (int) (cooldown / MS_DAY);
                    lores.add("§4Réinitialisation dans " + days + " jours.");
                } else if (cooldown >= MS_HOUR) {
                    final int hours = (int) cooldown / MS_HOUR;
                    lores.add("§4Réinitialisation dans " + hours + " heures.");
                } else {
                    final int minutes = Math.round(cooldown / MS_MIN);
                    lores.add("§4Réinitialisation dans " + minutes + " minutes.");
                }
                
                reward = getRepeatReward();
            }
            
        }
        lores.add("§b\u21D4 Ce challenge nécessite:");
        for (ItemStack item : getRequiredItems(timesCompleted)) {
            lores.add(item.getItemMeta().getDisplayName());
        }
        List<String> lines = wordWrap(reward.getRewardText(), 20, 30);
        if (completion.isOnCooldown()) lores.add("§2\u2714 §cRécompense \u21B9 §7(Items): §a" + lines.get(0));
        else lores.add("§2\u2714 §6Récompense §7(Items): §a" + lines.get(0));
        for (String line : lines.subList(1, lines.size())) {
            lores.add("§a" + line);
        }
        if (withCurrency) {
        	if (completion.isOnCooldown()) lores.add("§2\u2714 §cRécompense \u21B9 §7(Pièce d'or): §a" + reward.getCurrencyReward());
        	else lores.add("§2\u2714 §6Récompense §7(Pièce d'or): §a" + reward.getCurrencyReward());
        }
        if (completion.isOnCooldown()) lores.add("§2\u2714 §cRécompense \u21B9 §7(XP): §a" + reward.getXpReward());
        else lores.add("§2\u2714 §6Récompense §7(XP): §a" + reward.getXpReward());
        lores.add("§d\u21BB Nombre de fois complété: §f" + completion.getTimesCompleted());
        meta.setLore(lores);
        currentChallengeItem.setItemMeta(meta);
        return currentChallengeItem;
    }

    public ItemStack getDisplayItem() {
        // TODO: 10/12/2014 - R4zorax: Incorporate all the other goodies here...
        return new ItemStack(displayItem); // Copy
    }

    public boolean isTakeItems() {
        return takeItems;
    }

    public Reward getReward() {
        return reward;
    }

    public Reward getRepeatReward() {
        return repeatReward;
    }

    @Override
    public String toString() {
        return "Challenge{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", requiredItems='" + requiredItems + '\'' +
                ", rank='" + rank + '\'' +
                ", resetInHours=" + resetInHours +
                ", displayItem=" + displayItem +
                ", takeItems=" + takeItems +
                ", reward=" + reward +
                ", repeatReward=" + repeatReward +
                '}';
    }
    
    
    public String fitemee(String itemee) {
  	  String materials = itemee;
  	  
  	  //NINEAU1
  	  if (itemee.equals("Cobblestone")) materials ="Cobblestone";
  	  if (itemee.equals("Apple")) materials ="Pomme";
  	  if (itemee.equals("Wheat")) materials ="Blé";
  	  if (itemee.equals("Cactus")) materials ="Cactus";
  	  if (itemee.equals("Sugar Cane")) materials ="Canne a sucre";
  	  if (itemee.equals("Melon Slice")) materials ="Pastèque";
  	  if (itemee.equals("Pumpkin")) materials ="Citrouille";
  	  
  	//NINEAU2
  	  if (itemee.equals("Brown Mushroom")) materials ="Champignon marron";
  	  if (itemee.equals("Red Mushroom")) materials ="Champignon rouge";
  	  if (itemee.equals("Rotten Flesh")) materials ="Chair putréfiée";
  	  if (itemee.equals("String")) materials ="Ficelle";
  	  if (itemee.equals("Gunpowder")) materials ="Poudre à canon";
  	  if (itemee.equals("Arrow")) materials ="Flèche";
  	  if (itemee.equals("Bone")) materials ="Os";
  	  if (itemee.equals("Spider Eye")) materials ="Oeuil d'araignée";
  	  
  	  if (itemee.equals("Sugar")) materials ="Sucre";
  	  if (itemee.equals("Potato")) materials ="Pomme de terre";
  	  if (itemee.equals("Carrot")) materials ="Carotte";
  	  
  	  if (itemee.equals("Raw Fish")) materials ="Poisson cru";
  	  if (itemee.equals("Raw Salmon")) materials ="Saumon cru";
  	  if (itemee.equals("Pufferfish")) materials ="Poisson-clown";
  	  if (itemee.equals("Clownfish")) materials ="Poisson-globe";
  	
  	  if (itemee.equals("Oak Log")) materials ="Bois de chêne";
  	  if (itemee.equals("Spruce Log")) materials ="Bois de sapin";
  	  if (itemee.equals("Birch Log")) materials ="Bois de bouleau";
  	  if (itemee.equals("Jungle Log")) materials ="Bois d'acajou";
  	  if (itemee.equals("Acacia Log")) materials ="Bois d'acacia";
  	  if (itemee.equals("Dark Oak Log")) materials ="Bois de chêne noir";
  	  
  	  if (itemee.equals("Milk Bucket")) materials ="Seau de lait";
  	  if (itemee.equals("Cookie")) materials ="Cookie";
  	  
  	  if (itemee.equals("Bed Block")) materials ="Lit";
  	  if (itemee.equals("Crafting Table")) materials ="Table de craft";
  	  if (itemee.equals("Glass")) materials ="Verre";
  	  if (itemee.equals("WOODEN_DOOR")) materials ="Porte en bois de chêne";
  	  if (itemee.equals("Furnace")) materials ="Fourneau";
  	  if (itemee.equals("Bookshelf")) materials ="Bibliothèque";
  	  if (itemee.equals("Torch")) materials ="Torche";
  	  
  	//NINEAU3
  	  if (itemee.equals("Obsidian")) materials ="Obsidienne";
  	  if (itemee.equals("Portal")) materials ="Portail";
  	  
  	  if (itemee.equals("Ender Pearl")) materials ="Perle du néant";
  	  if (itemee.equals("Slimeball")) materials ="boule de slime";
  	  
  	  if (itemee.equals("Cake")) materials ="Gâteau";
  	  if (itemee.equals("Pumpkin Pie")) materials ="Tarte à la citrouille";
  	  
  	  if (itemee.equals("Netherrack")) materials ="Roche du nether";
  	  if (itemee.equals("Soul Sand")) materials ="Sable des âmes";
  	  if (itemee.equals("gast Tear")) materials ="Larme de ghast";
  	  if (itemee.equals("Empty Map")) materials ="Carte vierge";
  	  if (itemee.equals("Compass")) materials ="Boussole";
  	  if (itemee.equals("Clock")) materials ="Montre";
  	  if (itemee.equals("378:0")) materials ="Crème de magma";
  	  
  	  
  	  
  	if (itemee.equals("Redstone Dust")) materials ="Redstone";
  	if (itemee.equals("Redstone Torch")) materials ="Torche de redstone";
  	if (itemee.equals("Redstone Repeater")) materials ="Répéteur";
  	if (itemee.equals("Redstone Comparator")) materials ="Comparateur";
  	if (itemee.equals("Sticky Piston")) materials ="Piston collant";
  	if (itemee.equals("Lever")) materials ="Levier";
  	if (itemee.equals("Stone Button")) materials ="Bouton";
  	  
  	//NINEAU4
  	if (itemee.equals("White Wool")) materials ="Laine blanche";
  	if (itemee.equals("Orange Wool")) materials ="Laine orange";
  	if (itemee.equals("Magenta Wool")) materials ="Laine magenta";
  	if (itemee.equals("Light Blue Wool")) materials ="Laine bleu clair";
  	if (itemee.equals("Yellow Wool")) materials ="Laine jaune";
  	if (itemee.equals("Light Green Wool")) materials ="Laine vert clair";
  	if (itemee.equals("Pink Wool")) materials ="Laine rose";
  	if (itemee.equals("Gray Wool")) materials ="Laine grise";
  	if (itemee.equals("Light Gray Wool")) materials ="Laine gris clair";
  	if (itemee.equals("Cyan Wool")) materials ="Laine cyan";
  	if (itemee.equals("Purple Wool")) materials ="Laine violette";
  	if (itemee.equals("Blue Wool")) materials ="Laine bleue";
  	if (itemee.equals("Brown Wool")) materials ="Laine marron";
  	if (itemee.equals("Dark Green Wool")) materials ="Laine verte";
  	if (itemee.equals("Red Wool")) materials ="Laine rouge";
  	if (itemee.equals("Black Wool")) materials ="Laine noire";
  	
  	
  	
  	if (itemee.equals("White Stained Glass Pane")) materials ="Vitre blanche";
  	if (itemee.equals("Orange Stained Glass Pane")) materials ="Vitre orange";
  	if (itemee.equals("Magenta Stained Glass Pane")) materials ="Vitre magenta";
  	if (itemee.equals("Light Blue Stained Glass Pane")) materials ="Vitre bleu clair";
  	if (itemee.equals("Yellow Stained Glass Pane")) materials ="Vitre jaune";
  	if (itemee.equals("Light Green Stained Glass Pane")) materials ="Vitre vert clair";
  	if (itemee.equals("Pink Stained Glass Pane")) materials ="Vitre rose";
  	if (itemee.equals("Gray Stained Glass Pane")) materials ="Vitre grise";
  	if (itemee.equals("Light Gray Stained Glass Pane")) materials ="Vitre gris clair";
  	if (itemee.equals("Cyan Stained Glass Pane")) materials ="Vitre cyan";
  	if (itemee.equals("Purple Stained Glass Pane")) materials ="Vitre violette";
  	if (itemee.equals("Blue Stained Glass Pane")) materials ="Vitre bleue";
  	if (itemee.equals("Brown Stained Glass Pane")) materials ="Vitre marron";
  	if (itemee.equals("Dark Green Stained Glass Pane")) materials ="Vitre verte";
  	if (itemee.equals("Red Stained Glass Pane")) materials ="Vitre rouge";
  	if (itemee.equals("Black Stained Glass Pane")) materials ="Vitre noire";

  	
  	if (itemee.equals("Golden Carrot")) materials ="Carotte dorée";
  	if (itemee.equals("Golden Apple")) materials ="Pomme dorée";
  	
  	if (itemee.equals("Emerald")) materials ="Emeraude";
  	
  	if (itemee.equals("Baked Potato")) materials ="Pomme de terre cuite";
  	if (itemee.equals("Bread")) materials ="Pain";
  	if (itemee.equals("Cooked Chicken")) materials ="Poulet rôti";
  	if (itemee.equals("Cooked Fish")) materials ="Poisson cuit";
  	if (itemee.equals("Cooked Salmon")) materials ="Saumon cuit";
  	if (itemee.equals("Cooked Porkchop")) materials ="Côtlette de porc cuite";
  	if (itemee.equals("282:0")) materials ="Pastèque scintillante";
  	
  	  return materials; 
  	   
    }
}
