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

public class CLoadCommand implements CommandExecutor {
	private Cuboid plugin;

	public CLoadCommand(Cuboid instance) {
		this.plugin = instance;
	}

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

		if (args.length > 1) {
			String cuboidName = args[0].toLowerCase();
			if (plugin.cuboidExists(playerId, cuboidName)) {
				if (CuboidAction.isReady(playerId, false)) {
					byte returnCode = CuboidAction.loadCuboid(playerId, cuboidName);
					if (returnCode == 0) {
						player.sendMessage(ChatColor.GREEN + "The cuboid has been loaded.");
					} else if (returnCode == 1) {
						player.sendMessage(ChatColor.RED + "Could not find the file.");
					} else if (returnCode == 2) {
						player.sendMessage(ChatColor.RED + "Reading error while accessing the file.");
					} else if (returnCode == 3) {
						player.sendMessage(ChatColor.RED + "The file seems to be corrupted");
					}
				} else {
					player.sendMessage(ChatColor.RED + "No point has been selected");
					return true;
				}
			} else {
				player.sendMessage(ChatColor.RED + "This cuboid does not exist.");
				return true;
			}
		} else {
			return false;
		}

		return false;
	}

}
