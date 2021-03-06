package se.jeremy.minecraft.cuboid.listener;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import se.jeremy.minecraft.cuboid.Cuboid;
import se.jeremy.minecraft.cuboid.CuboidAreas;
import se.jeremy.minecraft.cuboid.CuboidC;

public class CPPlayer implements Listener {

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Location to = event.getTo();
		
		
		if (Cuboid.onMoveFeatures) {
			CuboidC arrival = CuboidAreas.findCuboidArea(to);
			
			/*
			 * IF:    
			 * - the cuboid exists
			 * - and is restricted
			 * - and if the player does not have the permission "cuboid.ignoresOwnership"
			 * - and the player is not allowed in the cuboid
			 * THEN:
			 * -
			 */
			if (arrival != null && arrival.restricted && !player.hasPermission("cuboid.ignoresOwnership") && !arrival.isAllowed(player)) {
				if (arrival.warning != null) {
					player.sendMessage(ChatColor.RED + arrival.warning);
				}
				
				Cuboid.notTeleport.add(player.getName());
				player.teleport(event.getFrom());
				
				return;
			}
			CuboidAreas.movement(player, event.getTo());
		}
	}

	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		Location to = event.getTo();
		
		if (Cuboid.onMoveFeatures) {
			CuboidC arrival = CuboidAreas.findCuboidArea(to);
			
			if (arrival != null && arrival.restricted && !player.hasPermission("/ignoresOwnership") && !arrival.isAllowed(player)) {
				if (arrival.warning != null) {
					player.sendMessage(ChatColor.RED + arrival.warning);
				}
				
				return;
			}

			// if he was teleported out of a restricted area
			if (Cuboid.notTeleport.contains(player.getName())) {
				Cuboid.notTeleport.remove(player.getName());
			} else {
				CuboidAreas.movement(player, to);
			}
		}
	}

	@EventHandler
	public void onDisconnect(PlayerQuitEvent event) {
		CuboidAreas.leaveAll(event.getPlayer());
	}
}
