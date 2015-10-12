package se.jeremy.minecraft.cuboid.commands;

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

public class CListCommand implements CommandExecutor {
	
	private Cuboid plugin;

	public CListCommand(Cuboid instance) {
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

		if (args.length == 1) {
			String list = plugin.listPersonalCuboids(playerId);
			if (list != null) {
				player.sendMessage(ChatColor.GREEN + "Your saved cuboids :"
						+ ChatColor.WHITE + list);
			} else {
				player.sendMessage(ChatColor.RED + "You have no saved cuboid");
			}
		} else if (args.length == 2 && player.isOp()) {
			Player target = Bukkit.getPlayer(args[0]);
			String list = plugin.listPersonalCuboids(target.getUniqueId());
			
			if (list != null) {
				player.sendMessage(ChatColor.GREEN + target.getDisplayName() + "'s saved cuboids :" + ChatColor.WHITE + list);
			} else {
				player.sendMessage(ChatColor.RED + target.getDisplayName() + " has no saved cuboid");
			}
		} else {
			if (player.isOp())
				player.sendMessage(ChatColor.RED
						+ "Usage : /clist <player name>");
			player.sendMessage(ChatColor.RED + "Usage : /clist");
		}

		return false;
	}

}
