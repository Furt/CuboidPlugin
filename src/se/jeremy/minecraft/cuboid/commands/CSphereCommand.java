package se.jeremy.minecraft.cuboid.commands;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import se.jeremy.minecraft.cuboid.CuboidAction;
import se.jeremy.minecraft.cuboid.CuboidAreas;
import se.jeremy.minecraft.cuboid.CuboidC;

public class CSphereCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (!(sender instanceof Player)) {
			return true;
		}
		
		Player player = (Player) sender;
		UUID playerId = player.getUniqueId();
		
		CuboidC playersArea = CuboidAreas.findCuboidArea(player.getLocation());
		
		if (playersArea != null && !playersArea.isAllowed(cmd) && !playersArea.isOwner(player) && !player.hasPermission("cuboidplugin.ignoreownership")) {
			player.sendMessage(ChatColor.RED + "This command is disallowed in this area");
			return true;
		}

		if (CuboidAction.isReady(playerId, false)) {
			boolean ball = (args[0].equalsIgnoreCase("/cball")) ? true : false;
			int radius = 0;
			int blockID = 4;
			Material blockType = Material.COBBLESTONE;
			
			if (args.length > 1) {
				try {
					radius = Integer.parseInt(args[0]);
				} catch (NumberFormatException n) {
					player.sendMessage(ChatColor.RED + args[0]
							+ " is not a valid radius.");
					return true;
				}
				if (radius < 2) {
					player.sendMessage(ChatColor.RED + "The radius has to be greater than 1");
					return true;
				}

				blockType = Material.matchMaterial(args[1]);
				

				if (blockType == null) {
					player.sendMessage(ChatColor.RED + args[2] + " is not a valid block ID.");
					return true;
				}

				if (ball) {
					CuboidAction.buildShpere(playerId, radius, blockID, true);
					player.sendMessage(ChatColor.GREEN + "The ball has been built");
				} else {
					CuboidAction.buildShpere(playerId, radius, blockID, false);
					player.sendMessage(ChatColor.GREEN + "The sphere has been built");
				}

			} else {
				if (ball) {
					player.sendMessage(ChatColor.RED + "Usage : /cball <radius> <block id|name>");
				} else {
					player.sendMessage(ChatColor.RED + "Usage : /csphere <radius> <block id|name>");
				}
			}
		} else {
			player.sendMessage(ChatColor.RED + "No point has been selected");
		}

		return false;
	}

}
