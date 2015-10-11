package se.jeremy.minecraft.cuboid.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import se.jeremy.minecraft.cuboid.CuboidAction;
import se.jeremy.minecraft.cuboid.CuboidAreas;
import se.jeremy.minecraft.cuboid.CuboidC;
import se.jeremy.minecraft.cuboid.Main;

public class CPyramidCommand implements CommandExecutor {
	private Main plugin;

	public CPyramidCommand(Main instance) {
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
				.getWorld().getName(), (int) player.getLocation().getX(),
				(int) player.getLocation().getY(), (int) player.getLocation()
						.getZ());
		if (playersArea != null && !playersArea.isAllowed(args[0])
				&& !playersArea.isOwner(player)
				&& !player.hasPermission("cuboidplugin.ignoreownership")) {
			player.sendMessage(ChatColor.RED
					+ "This command is disallowed in this area");
			return true;
		}

		if (CuboidAction.isReady(playerName, false)) {
			int radius = 0;
			int blockID = 4;
			if (args.length > 2) {
				try {
					radius = Integer.parseInt(args[1]);
				} catch (NumberFormatException n) {
					player.sendMessage(ChatColor.RED + args[1]
							+ " is not a valid radius.");
					return true;
				}
				if (radius < 2) {
					player.sendMessage(ChatColor.RED
							+ "The radius has to be greater than 1");
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

				boolean filled = true;
				if (args.length == 4 && args[3].equalsIgnoreCase("empty")) {
					filled = false;
				}

				CuboidAction.buildPyramid(playerName, radius, blockID, filled);
				player.sendMessage(ChatColor.GREEN
						+ "The pyramid has been built");
			} else {
				player.sendMessage(ChatColor.RED
						+ "Usage : /cpyramid <radius> <block id|name>");
			}
		} else {
			player.sendMessage(ChatColor.RED + "No point has been selected");
		}

		return false;
	}

}
