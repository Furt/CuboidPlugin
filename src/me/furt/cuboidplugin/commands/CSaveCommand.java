package me.furt.cuboidplugin.commands;

import me.furt.cuboidplugin.CuboidAction;
import me.furt.cuboidplugin.CuboidAreas;
import me.furt.cuboidplugin.CuboidC;
import me.furt.cuboidplugin.Main;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CSaveCommand implements CommandExecutor {
	private Main plugin;

	public CSaveCommand(Main instance) {
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

		if (args.length > 1) {
			String cuboidName = args[1].toLowerCase();
			if (!plugin.cuboidExists(playerName, cuboidName)
					|| args.length == 3 && args[2].startsWith("over")) {
				if (CuboidAction.isReady(playerName, true)) {
					byte returnCode = CuboidAction.saveCuboid(playerName,
							cuboidName);
					if (returnCode == 0) {
						player.sendMessage(ChatColor.GREEN
								+ "Selected cuboid is saved with the name "
								+ cuboidName);
					} else if (returnCode == 1) {
						player.sendMessage(ChatColor.RED
								+ "Could not create the target folder.");
					} else if (returnCode == 2) {
						player.sendMessage(ChatColor.RED
								+ "Error while writing the file.");
					}
				} else {
					player.sendMessage(ChatColor.RED
							+ "No cuboid has been selected");
				}
			} else {
				player.sendMessage(ChatColor.RED
						+ "This cuboid name is already taken.");
			}
		} else {
			player.sendMessage(ChatColor.RED + "Usage : /csave <cuboid name>");
		}

		return false;
	}
}
