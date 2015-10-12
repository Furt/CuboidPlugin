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

public class CFacesCommand implements CommandExecutor {
	private Cuboid plugin;

	public CFacesCommand(Cuboid instance) {
		this.plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
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

		if (CuboidAction.isReady(playerName, true)) {
			int blockID = 4;
			if (args.length > 1) {
				try {
					blockID = Integer.parseInt(args[1]);
				} catch (NumberFormatException n) {
					blockID = Material.getMaterial(args[1]).getId();
					// blockID = etc.getDataSource().getItem(args[1]);
				}

				if (!plugin.isValidBlockID(blockID)) {
					player.sendMessage(ChatColor.RED + args[1] + " is not a valid block ID.");
					return true;
				}

				CuboidAction.buildCuboidFaces(playerName, blockID, true);
				player.sendMessage(ChatColor.GREEN
						+ "The faces of the cuboid have been built");
			} else {
				player.sendMessage(ChatColor.RED
						+ "Usage : /cfaces <block id|name>");
			}
		} else {
			player.sendMessage(ChatColor.RED + "No cuboid has been selected");
		}

		return false;
	}

}
