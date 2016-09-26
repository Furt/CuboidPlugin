package me.furt.cuboidplugin.listener;

import me.furt.cuboidplugin.CuboidAction;
import me.furt.cuboidplugin.CuboidAreas;
import me.furt.cuboidplugin.CuboidC;
import me.furt.cuboidplugin.Main;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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
	public void onItemUse(Player player, Block blockPlaced, Block blockClicked,
			ItemStack item) {
		if (blockClicked != null && Main.protectionSytem
				&& !player.hasPermission("/ignoresOwnership")
				&& plugin.isCreatorItem(item)) {
			CuboidC cuboid = CuboidAreas.findCuboidArea(blockClicked.getWorld()
					.getName(), blockClicked.getX(), blockClicked.getY(),
					blockClicked.getZ());
			if (cuboid != null && cuboid.protection) {
				boolean allowed = cuboid.isAllowed(player);
				if (!allowed && Main.protectionWarn) {
					player.sendMessage(ChatColor.RED
							+ "This block is protected !");
				}
				// return !allowed;
			} else {
				// return plugin.isGloballyRestricted(player);
			}
		}
		// return false;
	}

	public void onComplexBlockChange(Player player, Block block) {
		if (block instanceof Chest) {
			if (Main.chestProtection
					&& !player.hasPermission("/ignoresOwnership")) {
				CuboidC cuboid = CuboidAreas.findCuboidArea(block.getWorld()
						.getName(), block.getX(), block.getY(), block.getZ());
				if (cuboid != null && cuboid.protection) {
					// return !cuboid.isAllowed(player);
				}
			}
			// return plugin.isGloballyRestricted(player);
		}
		// return false;
	}

	public void onSendComplexBlock(Player player, Block block) {
		if (block instanceof Chest) {
			if (Main.chestProtection
					&& !player.hasPermission("/ignoresOwnership")) {
				CuboidC cuboid = CuboidAreas.findCuboidArea(block.getWorld()
						.getName(), block.getX(), block.getY(), block.getZ());
				if (cuboid != null && cuboid.protection) {
					// return !cuboid.isAllowed(player);
				}
			}
			// return plugin.isGloballyRestricted(player);
		}
		// return false;
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		Block blockPlaced = event.getBlockPlaced();
		if (Main.protectionSytem
				&& !player.hasPermission("cuboidplugin.ignoresownership")) {
			CuboidC cuboid = CuboidAreas.findCuboidArea(blockPlaced.getWorld()
					.getName(), blockPlaced.getX(), blockPlaced.getY(),
					blockPlaced.getZ());
			if (cuboid != null && cuboid.protection) {
				boolean allowed = cuboid.isAllowed(player);
				if (!allowed && Main.protectionWarn) {
					player.sendMessage(ChatColor.RED
							+ "This block is protected !");
					event.setCancelled(true);
				}

			} else if (plugin.isGloballyRestricted(player)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		if (Main.protectionSytem
				&& !player.hasPermission("cuboidplugin.ignoresownership")) {
			CuboidC cuboid = CuboidAreas.findCuboidArea(block.getWorld()
					.getName(), block.getX(), block.getY(), block.getZ());
			if (cuboid != null && cuboid.protection) {
				boolean allowed = cuboid.isAllowed(player);
				if (!allowed) {
					if (Main.protectionWarn)
						player.sendMessage(ChatColor.RED
								+ "This block is protected !");
					event.setCancelled(true);
				}

			} else if (plugin.isGloballyRestricted(player)) {
				event.setCancelled(true);
			} else {
				player.sendMessage("well this isnt working.");
			}
		}
	}

	@EventHandler
	public void onBlockRightClicked(PlayerInteractEvent event) {
		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)
				|| event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			Player player = event.getPlayer();
			Block blockClicked = event.getClickedBlock();
			if (player.getItemInHand().getType()
					.equals(Material.getMaterial(Main.mainToolID))
					&& (player.hasPermission("cuboidplugin.protect") || player
							.hasPermission("cuboidplugin.cuboid"))) {
				boolean whichPoint = CuboidAction.setPoint(player.getName(),
						blockClicked.getX(), blockClicked.getY(),
						blockClicked.getZ());
				player.sendMessage(ChatColor.BLUE
						+ ((whichPoint) ? "First" : "Second")
						+ " point is set.");
			} else if (player.getItemInHand().getType()
					.equals(Material.getMaterial(Main.checkToolID))) {
				CuboidC cuboid = CuboidAreas.findCuboidArea(blockClicked
						.getWorld().getName(), blockClicked.getX(),
						blockClicked.getY(), blockClicked.getZ());
				if (cuboid == null)
					player.sendMessage(ChatColor.YELLOW + "Not a cuboid area");
				else
					cuboid.printInfos(player, false, false);
			}
		}
	}
}
