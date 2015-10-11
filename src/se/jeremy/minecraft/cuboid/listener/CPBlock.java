package se.jeremy.minecraft.cuboid.listener;

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

import se.jeremy.minecraft.cuboid.Cuboid;
import se.jeremy.minecraft.cuboid.CuboidAction;
import se.jeremy.minecraft.cuboid.CuboidAreas;
import se.jeremy.minecraft.cuboid.CuboidC;

public class CPBlock implements Listener {

	private Cuboid plugin;

	public CPBlock(Cuboid instance) {
		this.plugin = instance;
	}

	// TODO needs moved to proper place
	public void onItemUse(Player player, Block blockPlaced, Block blockClicked,
			ItemStack item) {
		if (blockClicked != null && Cuboid.protectionSytem
				&& !player.hasPermission("/ignoresOwnership")
				&& plugin.isCreatorItem(item)) {
			CuboidC cuboid = CuboidAreas.findCuboidArea(blockClicked.getWorld()
					.getName(), blockClicked.getX(), blockClicked.getY(),
					blockClicked.getZ());
			if (cuboid != null && cuboid.protection) {
				boolean allowed = cuboid.isAllowed(player);
				if (!allowed && Cuboid.protectionWarn) {
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
			if (Cuboid.chestProtection
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
			if (Cuboid.chestProtection
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
		if (Cuboid.protectionSytem
				&& !player.hasPermission("cuboidplugin.ignoresownership")) {
			CuboidC cuboid = CuboidAreas.findCuboidArea(blockPlaced.getWorld()
					.getName(), blockPlaced.getX(), blockPlaced.getY(),
					blockPlaced.getZ());
			if (cuboid != null && cuboid.protection) {
				boolean allowed = cuboid.isAllowed(player);
				if (!allowed && Cuboid.protectionWarn) {
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
		if (Cuboid.protectionSytem
				&& !player.hasPermission("cuboidplugin.ignoresownership")) {
			CuboidC cuboid = CuboidAreas.findCuboidArea(block.getWorld()
					.getName(), block.getX(), block.getY(), block.getZ());
			if (cuboid != null && cuboid.protection) {
				boolean allowed = cuboid.isAllowed(player);
				if (!allowed) {
					if (Cuboid.protectionWarn)
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
					.equals(Material.getMaterial(Cuboid.mainToolID))
					&& (player.hasPermission("cuboidplugin.protect") || player
							.hasPermission("cuboidplugin.cuboid"))) {
				boolean whichPoint = CuboidAction.setPoint(player.getName(),
						blockClicked.getX(), blockClicked.getY(),
						blockClicked.getZ());
				player.sendMessage(ChatColor.BLUE
						+ ((whichPoint) ? "First" : "Second")
						+ " point is set.");
			} else if (player.getItemInHand().getType()
					.equals(Material.getMaterial(Cuboid.checkToolID))) {
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
