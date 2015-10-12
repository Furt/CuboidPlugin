package se.jeremy.minecraft.cuboid.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import se.jeremy.minecraft.cuboid.CuboidAction;
import se.jeremy.minecraft.cuboid.CuboidAreas;
import se.jeremy.minecraft.cuboid.CuboidC;

public class CPasteCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		Player player = (Player) sender;
		String playerName = player.getName();
		CuboidC playersArea = CuboidAreas.findCuboidArea(player.getLocation());
		if (playersArea != null && !playersArea.isAllowed(args[0])
				&& !playersArea.isOwner(player)
				&& !player.hasPermission("cuboidplugin.ignoreownership")) {
			player.sendMessage(ChatColor.RED
					+ "This command is disallowed in this area");
			return true;
		}

		if (CuboidAction.isReady(playerName, false)) {
			byte returnCode = CuboidAction.paste(playerName);
			if (returnCode == 0) {
				player.sendMessage(ChatColor.GREEN
						+ "The cuboid has been placed.");
			} else if (returnCode == 1) {
				player.sendMessage(ChatColor.RED + "Nothing to paste !");
			}
		} else {
			player.sendMessage(ChatColor.RED + "No point has been selected");
		}
		return false;
	}
}
