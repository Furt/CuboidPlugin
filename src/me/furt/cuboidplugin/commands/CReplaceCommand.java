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

public class CReplaceCommand implements CommandExecutor {
	private Main plugin;

	public CReplaceCommand(Main instance) {
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
		
		if (CuboidAction.isReady(playerName, true)) {

			int paramSize = args.length - 1;
			if (paramSize > 1) {
				int[] replaceParams = new int[paramSize];
				for (int i = 0; i < paramSize; i++) {
					try {
						replaceParams[i] = Integer
								.parseInt(args[i + 1]);
					} catch (NumberFormatException n) {
						replaceParams[i] = Material.getMaterial(args[i + 1]).getId();
						//replaceParams[i] = etc.getDataSource().getItem(args[i + 1]);
						if (replaceParams[i] == 0) {
							player.sendMessage(ChatColor.RED
									+ args[i + 1]
									+ " is not a valid block name.");
							return true;
						}
					}
					if (!plugin.isValidBlockID(replaceParams[i])) {
						player.sendMessage(ChatColor.RED
								+""+ replaceParams[i]
								+ " is not a valid block ID.");
						return true;
					}
				}

				int blockID = replaceParams[replaceParams.length - 1];
				CuboidAction.replaceBlocks(playerName,
						replaceParams);
				player.sendMessage(ChatColor.GREEN
						+ "The blocks have been replaced");
			} else {
				player.sendMessage(ChatColor.RED
						+ "Usage : /creplace <block id|name> <block id|name>");
			}
		} else {
			player.sendMessage(ChatColor.RED
					+ "No cuboid has been selected");
		}

		return false;
	}
}
