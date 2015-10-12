package se.jeremy.minecraft.cuboid.commands;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import se.jeremy.minecraft.cuboid.CuboidAction;
import se.jeremy.minecraft.cuboid.CuboidAreas;
import se.jeremy.minecraft.cuboid.CuboidC;

public class CMoveCommand implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		
		Player player = (Player) sender;
		UUID playerId = player.getUniqueId();
		
		CuboidC playersArea = CuboidAreas.findCuboidArea(player.getLocation());
		
		if (playersArea != null && !playersArea.isAllowed(cmd) && !playersArea.isOwner(player) && !player.hasPermission("cuboidplugin.ignoreownership")) {
			player.sendMessage(ChatColor.RED + "This command is disallowed in this area");
			return true;
		}

		if (CuboidAction.isReady(playerId, true)) {
			if (args.length < 3) {
				player.sendMessage(ChatColor.RED + "Usage : /cmove <direction> <distance>");
				player.sendMessage(ChatColor.RED + "Direction : Up/Down/North/East/West/South");
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
