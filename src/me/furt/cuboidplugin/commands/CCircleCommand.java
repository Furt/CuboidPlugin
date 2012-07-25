package me.furt.cuboidplugin.commands;

import me.furt.cuboidplugin.CuboidAction;
import me.furt.cuboidplugin.CuboidAreas;
import me.furt.cuboidplugin.CuboidC;
import me.furt.cuboidplugin.Main;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CCircleCommand implements CommandExecutor {
	private Main plugin;

	public CCircleCommand(Main instance) {
		this.plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		Player player = (Player) sender;
		String playerName = player.getName();
		CuboidC playersArea = CuboidAreas.findCuboidArea(player.getLocation()
				.getWorld().getName(), (int) player
				.getLocation().getX(), (int) player.getLocation().getY(),
				(int) player.getLocation().getZ());
		if (playersArea != null && !playersArea.isAllowed(args[0])
				&& !playersArea.isOwner(player)
				&& !player.hasPermission("cuboidplugin.ignoreownership")) {
			player.sendMessage(ChatColor.RED
					+ "This command is disallowed in this area");
			return true;
		}

		if (CuboidAction.isReady(playerName, false)) {
			boolean disc = args[0].equalsIgnoreCase("/cdisc") ? true : false;
			int radius = 0;
			int blockID = 4;
			int height = 0;
			if (args.length > 2) {
				try {
					radius = Integer.parseInt(args[1]);
				} catch (NumberFormatException n) {
					player.sendMessage(ChatColor.RED + args[1]
							+ " is not a valid radius.");
					return true;
				}
				if (radius < 1) {
					player.sendMessage(ChatColor.RED + args[1]
							+ " is not a valid radius.");
					return true;
				}

				try {
					blockID = Integer.parseInt(args[2]);
				} catch (NumberFormatException n) {
					blockID = Material.getMaterial(args[2]).getId();
					// blockID = etc.getDataSource().getItem(args[2]);
				}

				if (!plugin.isValidBlockID(blockID)) {
					player.sendMessage(ChatColor.RED + args[2]
							+ " is not a valid block ID.");
					return true;
				}

				if (args.length == 4) {
					try {
						height = Integer.parseInt(args[3]);
					} catch (NumberFormatException n) {
						player.sendMessage(ChatColor.RED + args[3]
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
					CuboidAction.buildCircle(playerName, radius, blockID,
							height, true);
					player.sendMessage(ChatColor.GREEN + "The "
							+ ((height == 0) ? "disc" : "cylinder")
							+ " has been build");
				} else {
					CuboidAction.buildCircle(playerName, radius, blockID,
							height, false);
					player.sendMessage(ChatColor.GREEN + "The "
							+ ((height == 0) ? "circle" : "cylinder")
							+ " has been build");
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
