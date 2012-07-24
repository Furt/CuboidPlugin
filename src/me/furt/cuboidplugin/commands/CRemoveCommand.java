package me.furt.cuboidplugin.commands;

import java.io.File;

import me.furt.cuboidplugin.CuboidAreas;
import me.furt.cuboidplugin.CuboidC;
import me.furt.cuboidplugin.Main;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CRemoveCommand implements CommandExecutor {
	private Main plugin;

	public CRemoveCommand(Main instance) {
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
			if (plugin.cuboidExists(playerName, cuboidName)) {
				File toDelete = new File("cuboids/" + playerName + "/"
						+ cuboidName + ".cuboid");
				if (toDelete.delete()) {
					player.sendMessage(ChatColor.GREEN
							+ "Cuboid sucessfuly deleted");
				} else {
					player.sendMessage(ChatColor.RED
							+ "Error while deleting the cuboid file");
				}
			} else {
				player.sendMessage(ChatColor.RED
						+ "This cuboid does not exist.");
			}
		} else {
			player.sendMessage(ChatColor.RED + "Usage : /cremove <cuboid name>");
		}

		return false;
	}

}
