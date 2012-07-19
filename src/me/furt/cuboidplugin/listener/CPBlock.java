package me.furt.cuboidplugin.listener;

import me.furt.cuboidplugin.CuboidAction;
import me.furt.cuboidplugin.CuboidAreas;
import me.furt.cuboidplugin.CuboidC;
import me.furt.cuboidplugin.Main;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class CPBlock implements Listener {

	private Main plugin;

	public CPBlock(Main instance) {
		this.plugin = instance;
	}

	// TODO needs moved to proper place
	public void onItemUse(Player player, Block blockPlaced,
			Block blockClicked, ItemStack item) {
		if (blockClicked != null && Main.protectionSytem
				&& !player.hasPermission("/ignoresOwnership")
				&& plugin.isCreatorItem(item)) {
			CuboidC cuboid = CuboidAreas.findCuboidArea(blockClicked.getX(),
					blockClicked.getY(), blockClicked.getZ());
			if (cuboid != null && cuboid.protection) {
				boolean allowed = cuboid.isAllowed(player);
				if (!allowed && Main.protectionWarn) {
					player.sendMessage(ChatColor.RED
							+ "This block is protected !");
				}
				//return !allowed;
			} else {
				//return plugin.isGloballyRestricted(player);
			}
		}
		//return false;
	}

	public boolean onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		Block blockPlaced = event.getBlockPlaced();
		if (Main.protectionSytem && !player.hasPermission("/ignoresOwnership")) {
			CuboidC cuboid = CuboidAreas.findCuboidArea(blockPlaced.getX(),
					blockPlaced.getY(), blockPlaced.getZ());
			if (cuboid != null && cuboid.protection) {
				boolean allowed = cuboid.isAllowed(player);
				if (!allowed && Main.protectionWarn) {
					player.sendMessage(ChatColor.RED
							+ "This block is protected !");
				}
				return !allowed;
			} else {
				return plugin.isGloballyRestricted(player);
			}
		}
		return false;
	}

	public boolean onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		if (Main.protectionSytem && !player.hasPermission("/ignoresOwnership")) {
			CuboidC cuboid = CuboidAreas.findCuboidArea(block.getX(),
					block.getY(), block.getZ());
			if (cuboid != null && cuboid.protection) {
				boolean allowed = cuboid.isAllowed(player);
				if (!allowed && Main.protectionWarn) {
					player.sendMessage(ChatColor.RED
							+ "This block is protected !");
				}
				return !allowed;
			} else {
				return plugin.isGloballyRestricted(player);
			}
		}
		return false;
	}

	public void onBlockRightClicked(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Block blockClicked = event.getClickedBlock();
		if (player.getItemInHand().getTypeId() == Main.mainToolID
				&& (player.hasPermission("/protect") || player
						.hasPermission("/cuboid"))) {
			boolean whichPoint = CuboidAction.setPoint(player.getName(),
					blockClicked.getX(), blockClicked.getY(),
					blockClicked.getZ());
			player.sendMessage(ChatColor.BLUE
					+ ((whichPoint) ? "First" : "Second") + " point is set.");
		} else if (player.getItemInHand().getTypeId() == Main.checkToolID) {
			CuboidC cuboid = CuboidAreas.findCuboidArea(blockClicked.getX(),
					blockClicked.getY(), blockClicked.getZ());
			if (cuboid == null)
				player.sendMessage(ChatColor.YELLOW + "Not a cuboid area");
			else
				cuboid.printInfos(player, false, false);
		}
	}
}
