package se.jeremy.minecraft.cuboid.listener;

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

import se.jeremy.minecraft.cuboid.Cuboid;
import se.jeremy.minecraft.cuboid.CuboidAreas;
import se.jeremy.minecraft.cuboid.CuboidC;

public class CPEntity implements Listener {

	@EventHandler
	public void onMobSpawn(CreatureSpawnEvent event) {
		Entity mob = event.getEntity();
		Location loc = mob.getLocation();
		CuboidC cuboid = CuboidAreas.findCuboidArea(loc);
		
		if (cuboid != null && cuboid.sanctuary) {
			event.setCancelled(true);
		} else if (Cuboid.globalSanctuary) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onExplode(EntityExplodeEvent event) {
		CuboidC cuboid = CuboidAreas.findCuboidArea(event.getLocation());
		
		if (cuboid != null && !cuboid.creeper) {
			event.setCancelled(true);
		} else if (Cuboid.globalCreeperProt) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		Entity defender = event.getEntity();
		if (defender instanceof Player) {
			EntityDamageEvent ede = defender.getLastDamageCause();
			
			if (ede instanceof EntityDamageByEntityEvent || event instanceof LivingEntity) {
				EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) ede;
				Entity attacker = subEvent.getDamager();
				
				if (attacker instanceof Player) {
					Player target = (Player) attacker;
					CuboidC cuboid = CuboidAreas.findCuboidArea(target.getLocation());
					
					if (cuboid != null && !cuboid.PvP) {
						event.setCancelled(true);
					} else if (Cuboid.globalDisablePvP) {
						event.setCancelled(true);
					}
				} else {
					CuboidC cuboid = CuboidAreas.findCuboidArea(attacker.getLocation());
					
					if (cuboid != null && cuboid.sanctuary) {
						event.setCancelled(true);
					} else if (Cuboid.globalSanctuary) {
						event.setCancelled(true);
					}
				}

			}
		}
	}
}
