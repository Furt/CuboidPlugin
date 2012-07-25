package me.furt.cuboidplugin.commands;

import java.util.ArrayList;

import me.furt.cuboidplugin.CuboidAction;
import me.furt.cuboidplugin.CuboidAreas;
import me.furt.cuboidplugin.CuboidC;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HighProtectCommand implements CommandExecutor {

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

		if (CuboidAction.isReady(playerName, true)) {
			ArrayList<String> ownersList = new ArrayList<String>();
			int paramSize = args.length;
			if (paramSize > 2) {
				for (short i = 1; i < paramSize - 1; i++) {
					ownersList.add(args[i]);
				}
				String cuboidName = args[paramSize - 1].trim().toLowerCase();

				// TODO
				CuboidAreas.protectCuboidArea(player, ownersList, cuboidName,
						true);
			} else {
				player.sendMessage(ChatColor.YELLOW
						+ "You need to specify at least one player or group, and a name.");
			}
		} else {
			player.sendMessage(ChatColor.RED + "No cuboid has been selected");
		}
		return false;
	}

}
