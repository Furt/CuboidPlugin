package me.furt.cuboidplugin.commands;

import me.furt.cuboidplugin.CuboidAction;
import me.furt.cuboidplugin.CuboidAreas;
import me.furt.cuboidplugin.CuboidC;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMoveCommand implements CommandExecutor {

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
			if (args.length < 3) {
				player.sendMessage(ChatColor.RED
						+ "Usage : /cmove <direction> <distance>");
				player.sendMessage(ChatColor.RED
						+ "Direction : Up/Down/North/East/West/South");
				return true;
			}

			int howFar = 0;
			try {
				howFar = Integer.parseInt(args[2]);
				if (howFar < 0) {
					player.sendMessage(ChatColor.RED + "Distance must be > 0 !");
					return true;
				}
			} catch (NumberFormatException n) {
				player.sendMessage(ChatColor.RED + args[2]
						+ " is not a valid distance.");
				return true;
			}

			CuboidAction.moveCuboidContent(player, args[1], howFar);

		} else {
			player.sendMessage(ChatColor.RED + "No cuboid has been selected");
		}

		return false;
	}
}
