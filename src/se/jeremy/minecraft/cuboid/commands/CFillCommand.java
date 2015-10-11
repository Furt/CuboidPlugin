package se.jeremy.minecraft.cuboid.commands;

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

public class CFillCommand implements CommandExecutor {
	private Cuboid plugin;

	public CFillCommand(Cuboid instance) {
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
			if (args.length > 1) {
				int blocID = 0;
				try {
					blocID = Integer.parseInt(args[1]);
				} catch (NumberFormatException n) {
					blocID = Material.getMaterial(args[1]).getId();
					// blocID = etc.getDataSource().getItem(args[1]);
				}
				if (plugin.isValidBlockID(blocID)) {
					CuboidAction.fillCuboid(playerName, blocID);
					player.sendMessage(ChatColor.GREEN
							+ "The cuboid has been filled");
				} else {
					player.sendMessage(ChatColor.RED + "" + blocID
							+ " is not a valid block ID.");
				}
			} else {
				player.sendMessage(ChatColor.RED
						+ "Usage : /cfill <block id|name>");
			}
		} else {
			player.sendMessage(ChatColor.RED + "No cuboid has been selected");
		}

		return false;
	}

}
