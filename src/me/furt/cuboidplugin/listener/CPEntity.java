package me.furt.cuboidplugin.listener;

import me.furt.cuboidplugin.CuboidAreas;
import me.furt.cuboidplugin.CuboidC;
import me.furt.cuboidplugin.Main;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class CPEntity implements Listener {

	@EventHandler
	public void onMobSpawn(CreatureSpawnEvent event) {
		Entity mob = event.getEntity();
		Location loc = mob.getLocation();
		CuboidC cuboid = CuboidAreas.findCuboidArea(loc.getWorld().getName(),
				(int) loc.getX(), (int) loc.getY(), (int) loc.getZ());
		if (cuboid != null && cuboid.sanctuary) {
			event.setCancelled(true);
		} else if (Main.globalSanctuary) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onExplode(EntityExplodeEvent event) {
		CuboidC cuboid = CuboidAreas.findCuboidArea(event.getLocation()
				.getWorld().getName(), (int) event.getLocation().getX(),
				(int) event.getLocation().getY(), (int) event.getLocation()
						.getZ());
		if (cuboid != null && !cuboid.creeper) {
			event.setCancelled(true);
		} else if (Main.globalCreeperProt) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		Entity defender = event.getEntity();
		if (defender instanceof Player) {
			EntityDamageEvent ede = defender.getLastDamageCause();
			if (ede instanceof EntityDamageByEntityEvent
					|| event instanceof LivingEntity) {
				EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) ede;
				Entity attacker = subEvent.getDamager();
				if (attacker instanceof Player) {
					Player target = (Player) attacker;
					CuboidC cuboid = CuboidAreas.findCuboidArea(target
							.getLocation().getWorld().getName(), (int) target
							.getLocation().getX(), (int) target.getLocation()
							.getY(), (int) target.getLocation().getZ());
					if (cuboid != null && !cuboid.PvP) {
						event.setCancelled(true);
					} else if (Main.globalDisablePvP) {
						event.setCancelled(true);
					}
				} else {
					CuboidC cuboid = CuboidAreas.findCuboidArea(attacker
							.getLocation().getWorld().getName(), (int) attacker
							.getLocation().getX(), (int) attacker.getLocation()
							.getY(), (int) attacker.getLocation().getZ());
					if (cuboid != null && cuboid.sanctuary) {
						event.setCancelled(true);
					} else if (Main.globalSanctuary) {
						event.setCancelled(true);
					}
				}

			}
		}
	}
}
