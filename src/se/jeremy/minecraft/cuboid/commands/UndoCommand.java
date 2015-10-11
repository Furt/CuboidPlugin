package se.jeremy.minecraft.cuboid.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import se.jeremy.minecraft.cuboid.CuboidAction;
import se.jeremy.minecraft.cuboid.CuboidAreas;
import se.jeremy.minecraft.cuboid.CuboidC;

public class UndoCommand implements CommandExecutor {

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

		if (CuboidAction.isUndoAble(playerName)) {
			CuboidAction.undo(playerName);
			player.sendMessage(ChatColor.GREEN
					+ "Your last action has been undone !");
		} else {
			player.sendMessage(ChatColor.RED
					+ "Your last action is non-reversible.");
		}

		return false;
	}
}
