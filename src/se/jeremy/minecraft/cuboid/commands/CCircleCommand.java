package se.jeremy.minecraft.cuboid.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import se.jeremy.minecraft.cuboid.Cuboid;
import se.jeremy.minecraft.cuboid.CuboidAction;
import se.jeremy.minecraft.cuboid.CuboidAreas;
import se.jeremy.minecraft.cuboid.CuboidC;

public class CCircleCommand implements CommandExecutor {
	private Cuboid plugin;

	public CCircleCommand(Cuboid instance) {
		this.plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		
		Player player = (Player) sender;
		String playerName = player.getName();
		CuboidC playersArea = CuboidAreas.findCuboidArea(player.getLocation());
		
		if (playersArea != null && !playersArea.isAllowed(args[0]) && !playersArea.isOwner(player) && !player.hasPermission("cuboidplugin.ignoreownership")) {
			player.sendMessage(ChatColor.RED+ "This command is disallowed in this area");
			return true;
		}

		if (CuboidAction.isReady(playerName, false)) {
			boolean disc = args[0].equalsIgnoreCase("/cdisc") ? true : false;
			int radius = 0;
			//int blockID = 4;
			Material block = Material.COBBLESTONE;
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

				block = Material.getMaterial(args[2]);

				if (!block.isBlock()) {
					player.sendMessage(ChatColor.RED + args[2]+ " is not a valid block ID.");
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
					CuboidAction.buildCircle(playerName, radius, block, height, true);
					player.sendMessage(ChatColor.GREEN + "The " + ((height == 0) ? "disc" : "cylinder") + " has been build");
				} else {
					CuboidAction.buildCircle(playerName, radius, block, height, false);
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
