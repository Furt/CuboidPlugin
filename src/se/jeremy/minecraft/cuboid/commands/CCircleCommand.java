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

public class CCircleCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		
		Player player = (Player) sender;
		UUID playerId = player.getUniqueId();
		CuboidC playersArea = CuboidAreas.findCuboidArea(player.getLocation());
		
		if (playersArea != null && !playersArea.isAllowed(cmd) && !playersArea.isOwner(player) && !player.hasPermission("cuboidplugin.ignoreownership")) {
			player.sendMessage(ChatColor.RED+ "This command is disallowed in this area");
			return true;
		}

		if (CuboidAction.isReady(playerId, false)) {
			boolean disc = cmd.getName().equalsIgnoreCase("/cdisc") ? true : false;
			
			Material blockType = Material.COBBLESTONE;
			
			int radius = 0;
			int height = 0;
			
			if (args.length > 1) {
				try {
					radius = Integer.parseInt(args[0]);
				} catch (NumberFormatException n) {
					player.sendMessage(ChatColor.RED + args[0]
							+ " is not a valid radius.");
					return true;
				}
				if (radius < 1) {
					player.sendMessage(ChatColor.RED + args[0]
							+ " is not a valid radius.");
					return true;
				}

				blockType = Material.getMaterial(args[1]);

				if (blockType == null) {
					player.sendMessage(ChatColor.RED + args[1]+ " is not a valid block ID.");
					return true;
				}

				if (args.length == 3) {
					try {
						height = Integer.parseInt(args[2]);
					} catch (NumberFormatException n) {
						player.sendMessage(ChatColor.RED + args[2]
								+ " is not a valid height.");
						return true;
					}
					if (height > 0) {
						height--;
					} else if (height < 0) {
						height++;
					}
				}

				if (disc) {
					CuboidAction.buildCircle(playerId, radius, blockType, height, true);
					player.sendMessage(ChatColor.GREEN + "The " + ((height == 0) ? "disc" : "cylinder") + " has been build");
				} else {
					CuboidAction.buildCircle(playerId, radius, blockType, height, false);
					player.sendMessage(ChatColor.GREEN + "The " + ((height == 0) ? "circle" : "cylinder") + " has been build");
				}

			} else {
				if (disc) {
					player.sendMessage(ChatColor.RED
							+ "Usage : /cdisc <radius> <block id|name> [height]");
				} else {
					player.sendMessage(ChatColor.RED
							+ "Usage : /ccircle <radius> <block id|name> [height]");
				}
			}
		} else {
			player.sendMessage(ChatColor.RED + "No point has been selected");
		}

		return false;
	}

}
