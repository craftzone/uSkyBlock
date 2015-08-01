package us.talabrek.ultimateskyblock.event;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.projectiles.ProjectileSource;

import us.talabrek.ultimateskyblock.*;
import us.talabrek.ultimateskyblock.handler.VaultHandler;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;

import java.util.*;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

public class PlayerEvents implements Listener {
    private static final Set<EntityDamageEvent.DamageCause> FIRE_TRAP = new HashSet<>(
            Arrays.asList(EntityDamageEvent.DamageCause.LAVA, EntityDamageEvent.DamageCause.FIRE, EntityDamageEvent.DamageCause.FIRE_TICK));
    private static final Random RANDOM = new Random();
    private static final int OBSIDIAN_SPAM = 10000; // Max once every 10 seconds.
    
    private final uSkyBlock plugin;
    private final boolean visitorFallProtected;
    private final boolean visitorFireProtected;
    private final Map<UUID, Long> obsidianClick = new WeakHashMap<>();

    public PlayerEvents(uSkyBlock plugin) {
        this.plugin = plugin;
        visitorFallProtected = plugin.getConfig().getBoolean("options.protection.visitors.fall", true);
        visitorFireProtected = plugin.getConfig().getBoolean("options.protection.visitors.fire-damage", true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.isSkyWorld(player.getWorld())) {
            PlayerInfo playerInfo = plugin.loadPlayerData(player);
            if (playerInfo != null && !playerInfo.getHasIsland() && player.isFlying()) {
                uSkyBlock.getInstance().spawnTeleport(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        plugin.unloadPlayerData(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerFoodChange(final FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player && plugin.isSkyWorld(event.getEntity().getWorld())) {
            Player hungerman = (Player) event.getEntity();
            float randomNum = RANDOM.nextFloat();
            if (plugin.isSkyWorld(hungerman.getWorld()) && hungerman.getFoodLevel() > event.getFoodLevel() && plugin.playerIsOnIsland(hungerman)) {
                if (VaultHandler.checkPerk(hungerman.getName(), "usb.extra.hunger4", hungerman.getWorld())) {
                    event.setCancelled(true);
                    return;
                }
                if (VaultHandler.checkPerk(hungerman.getName(), "usb.extra.hunger3", hungerman.getWorld())) {
                    if (randomNum <= 0.75f) {
                        event.setCancelled(true);
                        return;
                    }
                }
                if (VaultHandler.checkPerk(hungerman.getName(), "usb.extra.hunger2", hungerman.getWorld())) {
                    if (randomNum <= 0.50f) {
                        event.setCancelled(true);
                        return;
                    }
                }
                if (VaultHandler.checkPerk(hungerman.getName(), "usb.extra.hunger", hungerman.getWorld())) {
                    if (randomNum <= 0.25f) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onClickOnObsidian(final PlayerInteractEvent event) {
        if (!plugin.isSkyWorld(event.getPlayer().getWorld())) {
            return;
        }
        long now = System.currentTimeMillis();
        Player player = event.getPlayer();
        PlayerInventory inventory = player != null ? player.getInventory() : null;
        Block block = event.getClickedBlock();
        Long lastClick = obsidianClick.get(player.getUniqueId());
        if (lastClick != null && (lastClick + OBSIDIAN_SPAM) >= now) {
            plugin.notifyPlayer(player, "\u00a74You can only convert obsidian once every 10 seconds");
            return;
        }
        if (Settings.extras_obsidianToLava && plugin.playerIsOnIsland(player)
                && plugin.isSkyWorld(player.getWorld())
                && event.getAction() == Action.RIGHT_CLICK_BLOCK
                && player.getItemInHand().getType() == Material.BUCKET
                && block != null
                && block.getType() == Material.OBSIDIAN
                && !plugin.testForObsidian(block)) {
            obsidianClick.put(player.getUniqueId(), now);
            player.sendMessage(tr("\u00a7eChanging your obsidian back into lava. Be careful!"));
            inventory.setItem(inventory.getHeldItemSlot(), new ItemStack(Material.LAVA_BUCKET, 1));
            player.updateInventory();
            block.setType(Material.AIR);
            event.setCancelled(true); // Don't execute the click anymore (since that would re-place the lava).
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVisitorDamage(final EntityDamageEvent event) {
        if (!plugin.isSkyWorld(event.getEntity().getWorld())) {
            return;
        }
        // Only protect visitors against damage, if pvp is disabled
        if (!Settings.island_allowPvP
                && ((visitorFireProtected && FIRE_TRAP.contains(event.getCause()))
                  || (visitorFallProtected && (event.getCause() == EntityDamageEvent.DamageCause.FALL)))
                && event.getEntity() instanceof Player
                && !plugin.playerIsOnIsland((Player)event.getEntity())) {
            event.setDamage(-event.getDamage());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMemberDamage(final EntityDamageByEntityEvent event) {
        if (!plugin.isSkyWorld(event.getEntity().getWorld())) {
            return;
        }
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player p2 = (Player) event.getEntity();
        if (event.getDamager() instanceof Player) {
            Player p1 = (Player) event.getDamager();
            cancelMemberDamage(p1, p2, event);
        } else if (event.getDamager() instanceof Projectile
                && !(event.getDamager() instanceof EnderPearl)) {
            ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
            if (shooter instanceof Player) {
                Player p1 = (Player) shooter;
                cancelMemberDamage(p1, p2, event);
            }
        }
    }

    private void cancelMemberDamage(Player p1, Player p2, EntityDamageByEntityEvent event) {
        IslandInfo is1 = plugin.getIslandInfo(p1);
        IslandInfo is2 = plugin.getIslandInfo(p2);
        if (is1 != null && is2 != null && is1.getName().equals(is2.getName())) {
            plugin.notifyPlayer(p1, "\u00a7eLe PVP est désactivé sur le skyblock!");
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (Settings.extras_sendToSpawn) {
            return;
        }
        if (plugin.isSkyWorld(event.getPlayer().getWorld())) {
            event.setRespawnLocation(uSkyBlock.getSkyBlockWorld().getSpawnLocation());
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (!plugin.isSkyWorld(event.getTo().getWorld())) {
            return;
        }
        Player player = event.getPlayer();
        IslandInfo islandInfo = uSkyBlock.getInstance().getIslandInfo(WorldGuardHandler.getIslandNameAt(event.getTo()));
        if (islandInfo != null && islandInfo.isBanned(player.getName())) {
            event.setCancelled(true);
            player.sendMessage(tr("\u00a74Ce joueur vous a interdit de se téléporter sur son île."));
        }
    }
    
    
	@EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
	public void onBeaconClick(PlayerInteractEvent event) {

		if ((event.getAction() == Action.RIGHT_CLICK_BLOCK)
				&& (event.getClickedBlock().getType() == Material.ENDER_CHEST)
				&& (!event.getPlayer().hasPermission("enderchest.use"))) {
			event.getPlayer().sendMessage(
					ChatColor.AQUA + "Vous n'avez pas la permission");
			event.setCancelled(true);
			return;
		}

	}
    
    
	@SuppressWarnings("deprecation")
	@EventHandler
	public void ondead(PlayerDeathEvent e) {
		Player pkilled = e.getEntity().getPlayer();
		String killed = e.getEntity().getPlayer().getName();
		VaultHandler.getEcon().withdrawPlayer(killed, 5);
		pkilled.sendMessage(ChatColor.RED + "-5 PO");
	}
	  
    
    
	@SuppressWarnings("deprecation")
	@EventHandler
	public void signuse(PlayerInteractEvent e) {

		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			final Player p = e.getPlayer();
			Block block = e.getClickedBlock();
			BlockState state = block.getState();

			if (state instanceof Sign) {
				Sign sign = (Sign) state;
				double balance = VaultHandler.getEcon().getBalance(p.getName());

				if (sign.getLine(0).equalsIgnoreCase(
						ChatColor.DARK_GREEN + "[Travailleur]")) {

					if (balance >= 500) {

						if (p.hasPermission("g.debutant")) {
							Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),"perm player " + p.getName()+ " setgroup Travailleur");
							Bukkit.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "Félicitation "+ ChatColor.DARK_GREEN+ p.getName()+ ChatColor.LIGHT_PURPLE+ " est maintenant Travailleur");
							VaultHandler.getEcon().withdrawPlayer(p.getName(), 500);
						} else {
							p.sendMessage(ChatColor.RED
									+ "Vous devez avoir le grade Débutant pour pouvoir acheter ce grade");
						} // fin balace >0

					} else {
						p.sendMessage(ChatColor.RED
								+ "Vous n'avez pas assez de PO ");
						p.sendMessage(ChatColor.AQUA + "==>Vistez http://www.craftzone.fr  puis shop si vous ne voulez pas farmer les PO et avoir le grade de suite");
					} // fin balace >0

				} // fin travailleur

				if (sign.getLine(0).equalsIgnoreCase(
						ChatColor.AQUA + "[Spécialiste]")) {

					if (balance >= 5000) {

						if (p.hasPermission("g.travailleur")) {
							Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),"perm player " + p.getName()+ " setgroup Specialiste");
							Bukkit.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "Félicitation "+ ChatColor.AQUA + p.getName()+ ChatColor.LIGHT_PURPLE+ " est maintenant Spécialiste");
							VaultHandler.getEcon().withdrawPlayer(p.getName(), 5000);
						} else {
							p.sendMessage(ChatColor.RED + "Vous devez avoir le grade Travailleur pour pouvoir acheter ce grade");
						} // fin balace >0

					} else {
						p.sendMessage(ChatColor.RED + "Vous n'avez pas assez de PO ");
						p.sendMessage(ChatColor.AQUA + "==>Vistez http://www.craftzone.fr  puis shop si vous ne voulez pas farmer les PO et avoir le grade de suite");
					} // fin balace >0

				} // fin specialiste

				if (sign.getLine(0).equalsIgnoreCase(
						ChatColor.DARK_BLUE + "[Conseiller]")) {

					if (balance >= 20000) {

						if (p.hasPermission("g.specialiste")) {
							Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "perm player " + p.getName() + " setgroup Conseiller");
							Bukkit.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "Félicitation "+ ChatColor.DARK_BLUE + p.getName() + ChatColor.LIGHT_PURPLE + " est maintenant Conseiller");
							VaultHandler.getEcon().withdrawPlayer(p.getName(), 20000);
						} else {
							p.sendMessage(ChatColor.RED + "Vous devez avoir le grade Spécialiste pour pouvoir acheter ce grade");
						} // fin balace >0

					} else {
						p.sendMessage(ChatColor.RED + "Vous n'avez pas assez de PO ");
						p.sendMessage(ChatColor.AQUA + "==>Vistez http://www.craftzone.fr puis shop si vous ne voulez pas farmer 500 PO et avoir le grade de suite");
					} // fin balace >0

				} // fin conseiller

				if (sign.getLine(0).equalsIgnoreCase(
						ChatColor.LIGHT_PURPLE + "[Expert]")) {

					if (balance >= 40000) {

						if (p.hasPermission("g.conseiller")) {
							Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),"perm player " + p.getName() + " setgroup Expert");
							Bukkit.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "Félicitation " + ChatColor.DARK_PURPLE + p.getName() + ChatColor.LIGHT_PURPLE + " est maintenant Expert");
							VaultHandler.getEcon().withdrawPlayer(p.getName(), 40000);
						} else {
							p.sendMessage(ChatColor.RED+ "Vous devez avoir le grade Conseiller pour pouvoir acheter ce grade");
						} // fin balace >0

					} else {
						p.sendMessage(ChatColor.RED+ "Vous n'avez pas assez de PO ");
						p.sendMessage(ChatColor.AQUA+ "==>Vistez http://www.craftzone.fr  puis shop si vous ne voulez pas farmer les PO et avoir le grade de suite");
					} // fin balace >0

				} // fin specialiste

			}

		}

	}
	  
	@EventHandler
	public void PortalEvent(PlayerPortalEvent e) {
		Player p = e.getPlayer();
		World nether = Bukkit.getWorld("world_nether");
		PlayerInfo playerInfo = uSkyBlock.getInstance().getPlayerInfo(p);
		
		if(playerInfo.checkChallenge("enfer") > 0) {
			p.teleport(nether.getSpawnLocation());
		} else {
			p.sendMessage(ChatColor.RED + "Vous devez avoir le challenge ENFER pour pouvoir aller dans le nether");
			return;
		}
		
	}
    
    
}
