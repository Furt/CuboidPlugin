package se.jeremy.minecraft.cuboid.commands;

import java.io.File;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import se.jeremy.minecraft.cuboid.Cuboid;
import se.jeremy.minecraft.cuboid.CuboidAreas;
import se.jeremy.minecraft.cuboid.CuboidC;
import se.jeremy.minecraft.cuboid.CuboidContent;

public class CShareCommand implements CommandExecutor {
	private Cuboid plugin;

	public CShareCommand(Cuboid instance) {
		this.plugin = instance;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (!(sender instanceof Player)) {
			return true;
		}
		
		Player player = (Player) sender;
		UUID playerId = player.getUniqueId();
		
		CuboidC playersArea = CuboidAreas.findCuboidArea(player.getLocation());
		
		if (playersArea != null && !playersArea.isAllowed(cmd) && !playersArea.isOwner(player) && !player.hasPermission("cuboid.use")) {
			player.sendMessage(ChatColor.RED + "This command is disallowed in this area");
			return true;
		}

		if (args.length > 1) {
			String cuboidName = args[0].toLowerCase();
			UUID targetPlayerId = null;
			Player targetPlayer = plugin.playerMatch(args[1]);
			String targetPlayerName = Bukkit.getPlayer(targetPlayerId).getDisplayName();

			if (targetPlayer != null) {
				targetPlayerId = targetPlayer.getUniqueId();
			} else {
				player.sendMessage(ChatColor.RED + "Player " + args[1] + " seems to be offline");
				return true;
			}

			if (plugin.cuboidExists(playerId, cuboidName)) {
				if (!plugin.cuboidExists(targetPlayerId, cuboidName)) {

					File ownerFolder = new File("cuboids/" + targetPlayerId);
					try {
						if (!ownerFolder.exists()) {
							ownerFolder.mkdir();
						}
					} catch (Exception e) {
						player.sendMessage(ChatColor.RED + "Error while creating targer folder");
						return true;
					}

					if (CuboidContent.copyFile(new File("cuboids/" + playerId + "/" + cuboidName + ".cuboid"), new File("cuboids/" + targetPlayerId + "/" + cuboidName + ".cuboid"))) {
						player.sendMessage(ChatColor.GREEN + "You shared " + cuboidName + " with " + targetPlayerName);
						
						for (Player p : plugin.getServer().getOnlinePlayers()) {
							if (p.getUniqueId().equals(targetPlayer)) {
								p.sendMessage(ChatColor.GREEN + Bukkit.getPlayer(playerId).getDisplayName() + " shared " + cuboidName + ".cuboid with you");
							}
						}
					} else {
						player.sendMessage(ChatColor.RED + "Error while copying the the cuboid file");
					}
				} else {
					player.sendMessage(ChatColor.RED + targetPlayerName + " already has a cuboid named " + cuboidName);
				}
			} else {
				player.sendMessage(ChatColor.RED + "This cuboid does not exist.");
			}
		} else {
			player.sendMessage(ChatColor.RED + "Usage : /cshare <cuboid name> <player name>");
		}

		return false;
	}

}
