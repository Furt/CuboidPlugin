package me.furt.cuboidplugin.commands;

import java.io.File;

import me.furt.cuboidplugin.CuboidAreas;
import me.furt.cuboidplugin.CuboidC;
import me.furt.cuboidplugin.CuboidContent;
import me.furt.cuboidplugin.Main;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CShareCommand implements CommandExecutor {
	private Main plugin;

	public CShareCommand(Main instance) {
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
		
		if (args.length > 2) {
			String cuboidName = args[1].toLowerCase();
			String targetPlayerName = "";
			Player targetPlayer = plugin.playerMatch(args[2]);

			if (targetPlayer != null) {
				targetPlayerName = targetPlayer.getName();
			} else {
				player.sendMessage(ChatColor.RED + "Player "
						+ args[2] + " seems to be offline");
				return true;
			}

			if (plugin.cuboidExists(playerName, cuboidName)) {
				if (!plugin.cuboidExists(targetPlayerName, cuboidName)) {

					File ownerFolder = new File("cuboids/"
							+ targetPlayerName);
					try {
						if (!ownerFolder.exists()) {
							ownerFolder.mkdir();
						}
					} catch (Exception e) {
						player.sendMessage(ChatColor.RED
								+ "Error while creating targer folder");
						return true;
					}

					if (CuboidContent.copyFile(new File("cuboids/"
							+ playerName + "/" + cuboidName
							+ ".cuboid"), new File("cuboids/"
							+ targetPlayerName + "/" + cuboidName
							+ ".cuboid"))) {
						player.sendMessage(ChatColor.GREEN
								+ "You shared " + cuboidName
								+ " with " + targetPlayerName);
						for (Player p : plugin.getServer().getOnlinePlayers()) {
							if (p.getName()
									.equals(targetPlayerName)) {
								p.sendMessage(ChatColor.GREEN
										+ playerName + " shared "
										+ cuboidName
										+ ".cuboid with you");
							}
						}
					} else {
						player.sendMessage(ChatColor.RED
								+ "Error while copying the the cuboid file");
					}
				} else {
					player.sendMessage(ChatColor.RED
							+ targetPlayerName
							+ " already has a cuboid named "
							+ cuboidName);
				}
			} else {
				player.sendMessage(ChatColor.RED
						+ "This cuboid does not exist.");
			}
		} else {
			player.sendMessage(ChatColor.RED
					+ "Usage : /cshare <cuboid name> <player name>");
		}

		return false;
	}

}
