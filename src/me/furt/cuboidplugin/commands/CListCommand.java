package me.furt.cuboidplugin.commands;

import me.furt.cuboidplugin.CuboidAreas;
import me.furt.cuboidplugin.CuboidC;
import me.furt.cuboidplugin.Main;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CListCommand implements CommandExecutor {
	private Main plugin;

	public CListCommand(Main instance) {
		this.plugin = instance;
	}

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
		
		if (args.length == 1) {
			String list = plugin.listPersonalCuboids(playerName);
			if (list != null) {
				player.sendMessage(ChatColor.GREEN
						+ "Your saved cuboids :" + ChatColor.WHITE
						+ list);
			} else {
				player.sendMessage(ChatColor.RED
						+ "You have no saved cuboid");
			}
		} else if (args.length == 2 && player.isOp()) {
			String list = plugin.listPersonalCuboids(args[1]);
			if (list != null) {
				player.sendMessage(ChatColor.GREEN + args[1]
						+ "'s saved cuboids :" + ChatColor.WHITE
						+ list);
			} else {
				player.sendMessage(ChatColor.RED + args[1]
						+ " has no saved cuboid");
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
