package se.jeremy.minecraft.cuboid.commands;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import se.jeremy.minecraft.cuboid.CuboidAction;
import se.jeremy.minecraft.cuboid.CuboidAreas;
import se.jeremy.minecraft.cuboid.CuboidC;

public class CReplaceCommand implements CommandExecutor {

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

			int paramSize = args.length - 1;
			if (paramSize > 1) {
				Material[] replaceParams = new Material[paramSize];
				for (int i = 0; i < paramSize; i++) {
					try {
						replaceParams[i] = Material.getMaterial(args[i + 1]);
					} catch (NumberFormatException n) {
						replaceParams[i] = Material.getMaterial(args[i + 1]);
						
						if (replaceParams[i] == Material.AIR) {
							player.sendMessage(ChatColor.RED + args[i + 1] + " is not a valid block name.");
							return true;
						}
					}
					if (replaceParams[i] == null) {
						player.sendMessage(ChatColor.RED + ""+ replaceParams[i] + " is not a valid block ID.");
						return true;
					}
				}

				CuboidAction.replaceBlocks(playerId, replaceParams);
				player.sendMessage(ChatColor.GREEN + "The blocks have been replaced");
			} else {
				player.sendMessage(ChatColor.RED + "Usage : /creplace <block id|name> <block id|name>");
			}
		} else {
			player.sendMessage(ChatColor.RED + "No cuboid has been selected");
		}

		return false;
	}
}
