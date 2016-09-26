package me.furt.cuboidplugin.listener;

import me.furt.cuboidplugin.CuboidAreas;
import me.furt.cuboidplugin.CuboidC;
import me.furt.cuboidplugin.Main;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class CPPlayer implements Listener {

	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Location to = event.getTo();
		if (Main.onMoveFeatures) {
			CuboidC arrival = CuboidAreas.findCuboidArea(to.getWorld()
					.getName(), (int) to.getX(), (int) to.getY(), (int) to
					.getZ());
			if (arrival != null && arrival.restricted
					&& !player.hasPermission("/ignoresOwnership")
					&& !arrival.isAllowed(player)) {
				if (arrival.warning != null) {
					player.sendMessage(ChatColor.RED + arrival.warning);
				}
				Main.notTeleport.add(player.getName());
				player.teleport(event.getFrom());
				return;
			}
			CuboidAreas.movement(player, event.getTo());
		}
	}

	public void onTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		Location to = event.getTo();
		if (Main.onMoveFeatures) {
			CuboidC arrival = CuboidAreas.findCuboidArea(to.getWorld()
					.getName(), (int) to.getX(), (int) to.getY(), (int) to
					.getZ());
			if (arrival != null && arrival.restricted
					&& !player.hasPermission("/ignoresOwnership")
					&& !arrival.isAllowed(player)) {
				if (arrival.warning != null) {
					player.sendMessage(ChatColor.RED + arrival.warning);
				}
				return;
			}

			// if he was teleported out of a restricted area
			if (Main.notTeleport.contains(player.getName())) {
				Main.notTeleport.remove(player.getName());
			} else {
				CuboidAreas.movement(player, to);
			}
		}
	}

	public void onDisconnect(PlayerQuitEvent event) {
		CuboidAreas.leaveAll(event.getPlayer());
	}
}