package se.jeremy.minecraft.cuboid.commands;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import se.jeremy.minecraft.cuboid.Cuboid;
import se.jeremy.minecraft.cuboid.CuboidAction;
import se.jeremy.minecraft.cuboid.CuboidAreas;
import se.jeremy.minecraft.cuboid.CuboidC;

public class CWallsCommand implements CommandExecutor {
	private Cuboid plugin;

	public CWallsCommand(Cuboid instance) {
		this.plugin = instance;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Bukkit.getLogger().info("Command /cwalls initiated with " + args.length + " arguments.");
		
		if (!(sender instanceof Player)) {
			// Do nothing is the command initiator is not a Player
			sender.sendMessage("This command is only for players.");
			return true;
		}
		
		Player player = (Player) sender;
		String playerName = player.getName();
		CuboidC playersArea = CuboidAreas.findCuboidArea(player.getLocation());
		
		if (playersArea != null && !playersArea.isAllowed(args[0]) && !playersArea.isOwner(player) && !player.hasPermission("cuboidplugin.ignoreownership")) {
			player.sendMessage(ChatColor.RED + "This command is disallowed in this area");
			return true;
		}

		if (CuboidAction.isReady(playerName, true)) {
			Material block = Material.COBBLESTONE;
			
			if (args.length >= 1) {
				player.sendMessage(ChatColor.YELLOW + "So, you want to build walls with " + args[0] + "?");
				block = Material.getMaterial(args[0]);

				if (block == null) {
					player.sendMessage(ChatColor.RED + args[0] + " is not a valid block.");
					return true;
				}

				CuboidAction.buildCuboidFaces(playerName, block, false);
				player.sendMessage(ChatColor.GREEN + "The walls have been built");
			} else {
				player.sendMessage(ChatColor.RED + "Not enough arguments");
				return true;
			}
		} else {
			player.sendMessage(ChatColor.RED + "No cuboid has been selected");
		}

		player.sendMessage("Got to the end...");
		return true;
	}

}
