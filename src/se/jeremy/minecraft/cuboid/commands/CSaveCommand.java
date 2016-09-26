package se.jeremy.minecraft.cuboid.commands;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import se.jeremy.minecraft.cuboid.Cuboid;
import se.jeremy.minecraft.cuboid.CuboidAction;
import se.jeremy.minecraft.cuboid.CuboidAreas;
import se.jeremy.minecraft.cuboid.CuboidC;

public class CSaveCommand implements CommandExecutor {
	private Cuboid plugin;

	public CSaveCommand(Cuboid instance) {
		this.plugin = instance;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (!(sender instanceof Player)) {
			return true;
		}
		
		Player player = (Player) sender;
		UUID playerId = player.getUniqueId();
		
		CuboidC playersArea = CuboidAreas.findCuboidArea(player.getLocation());
		
		if (playersArea != null && !playersArea.isAllowed(cmd) && !playersArea.isOwner(player) && !player.hasPermission(cmd.getPermission())) {
			player.sendMessage(ChatColor.RED + "This command is disallowed in this area");
			return true;
		}

		if (args.length > 0) {
			String cuboidName = args[0].toLowerCase();
			
			if (!plugin.cuboidExists(playerId, cuboidName) || args.length == 2 && args[1].startsWith("over")) {
				if (CuboidAction.isReady(playerId, true)) {
					int returnCode = CuboidAction.saveCuboid(playerId, cuboidName);
					
					if (returnCode == 0) {
						player.sendMessage(ChatColor.GREEN + "Selected cuboid is saved with the name " + cuboidName);
					} else if (returnCode == 1) {
						player.sendMessage(ChatColor.RED + "Could not create the target folder.");
					} else if (returnCode == 2) {
						player.sendMessage(ChatColor.RED + "Error while writing the file.");
					}
				} else {
					player.sendMessage(ChatColor.RED + "No cuboid has been selected");
				}
			} else {
				player.sendMessage(ChatColor.RED + "This cuboid name is already taken.");
			}
		} else {
			player.sendMessage(ChatColor.RED + "Usage : /csave <cuboid name>");
		}

		return false;
	}
}
