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

public class CSphereCommand implements CommandExecutor {
	private Main plugin;

	public CSphereCommand(Main instance) {
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
		CuboidC playersArea = CuboidAreas.findCuboidArea((int) player
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
			boolean ball = (args[0].equalsIgnoreCase("/cball")) ? true
					: false;
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
					// TODO blockID = etc.getDataSource().getItem(args[2]);
				}
				if (!plugin.isValidBlockID(blockID)) {
					player.sendMessage(ChatColor.RED + args[2]
							+ " is not a valid block ID.");
					return true;
				}

				if (ball) {
					CuboidAction.buildShpere(playerName, radius,
							blockID, true);
					player.sendMessage(ChatColor.GREEN
							+ "The ball has been built");
				} else {
					CuboidAction.buildShpere(playerName, radius,
							blockID, false);
					player.sendMessage(ChatColor.GREEN
							+ "The sphere has been built");
				}

			} else {
				if (ball) {
					player.sendMessage(ChatColor.RED
							+ "Usage : /cball <radius> <block id|name>");
				} else {
					player.sendMessage(ChatColor.RED
							+ "Usage : /csphere <radius> <block id|name>");
				}
			}
		} else {
			player.sendMessage(ChatColor.RED
					+ "No point has been selected");
		}

		return false;
	}

}
