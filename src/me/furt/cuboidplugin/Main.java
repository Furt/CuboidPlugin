package me.furt.cuboidplugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

	public String name = "CuboidPlugin";
	static boolean logging = false;
	static boolean SQLstorage = false; // TODO : look for // SQL

	static ArrayList<Integer> operableItems;
	static boolean allowBlacklistedBlocks = false;
	static boolean chestProtection = true;
	public static int mainToolID = 269;
	public static int checkToolID = 268;

	public static boolean protectionSytem = true;
	public static boolean protectionWarn = false;
	// worldwide area features
	public static String[] restrictedGroups;
	public static boolean globalDisablePvP = false;
	public static boolean globalCreeperProt = false;
	public static boolean globalSanctuary = false;
	// local area features default values
	static boolean protectionOnDefault = false;
	static boolean restrictedOnDefault = false;
	static boolean sanctuaryOnDefault = false;
	static boolean creeperDisabledOnDefault = false;
	static boolean pvpDisabledOnDefault = false;
	static boolean healOnDefault = false;
	// local area features allowance for owners/and protect-allowed
	public static boolean onMoveFeatures = true;
	public static boolean allowOwnersToBackup = false;
	public static boolean allowRestrictedZones = false;
	public static boolean allowNoPvpZones = true;
	public static boolean allowNoCreeperZones = true;
	public static boolean allowSanctuaries = false;
	// List of players denied entry to a restricted cuboid, that are to not
	// trigger the teleport functions
	public static ArrayList<String> notTeleport;
	// Temporaty fix for wrinting to disk...
	static long writeDelay = 1800000;

	static Timer writeTimer = new Timer();

	@Override
	public void onEnable() {
		onPluginReload();
		getLogger().log(Level.INFO, "initializing v17.9 for hMod 131+");
	}

	@Override
	public void onDisable() {
		onPluginStop();
		writeTimer = new Timer();
		CuboidAreas.healTimer = new Timer();
	}

	/*
	 * Ensures the existence of the full arborescence
	 */
	private boolean checkFolder() {
		File folder = new File("cuboids");
		if (!folder.exists()) {
			if (!folder.mkdir()) {
				getLogger().log(Level.SEVERE, "could not create the cuboids folder");
				return false;
			}
		}
		return true;
	}

	public void loadProperties() {
		if (!new File("cuboids/cuboidPlugin.properties").exists()) {
			FileWriter writer = null;
			try {
				writer = new FileWriter("cuboids/cuboidPlugin.properties");
				// general
				writer.write("#The selection tool, default : 269 = wooden shovel\r\n");
				writer.write("mainToolID=269\r\n");
				writer.write("#The information tool, default : 268 = wooden sword\r\n");
				writer.write("checkToolID=268\r\n");
				// cuboid
				writer.write("#Should players be able to spawn blacklisted blocks with cuboid ?\r\n");
				writer.write("allowBlacklistedBlocks=false\r\n");
				writer.write("#Should every cuboid action be logged ?\r\n");
				writer.write("fullLogging=false\r\n");
				writer.write("# Delay betweed two auto-save of the cuboids to the hard drive.\r\n");
				writer.write("autoSaveDelay=30\r\n");
				// Priorities
				writer.write("#Which cuboid areas have priority ? Newest or oldest ?\r\n");
				writer.write("newestHavePriority=true\r\n");
				// Protection
				writer.write("#Do you want to allow areas to be protected ?\r\n");
				writer.write("protectionSytem=true\r\n");
				writer.write("#Do you want the chests protected too ?\r\n");
				writer.write("chestProtection=true\r\n");
				writer.write("#List of block id that are activable in protected areas\r\n");
				writer.write("operableItemIDs=64,69,77,84\r\n");
				writer.write("#Display a warning when touching a protected block ?\r\n");
				writer.write("protectionWarning=false\r\n");
				// Worldwide features
				writer.write("#List of groups that are forbiden to build on the entire world.\r\n");
				writer.write("#Delimiter is a coma. Leave blank if none\r\n");
				writer.write("restrictedGroups=\r\n");
				// Area features : default values
				writer.write("# Is protection by default in a newly created area ?\r\n");
				writer.write("protectionOnDefault=false\r\n");
				writer.write("# Is restricted access by default in a newly created area ?\r\n");
				writer.write("restrictedOnDefault=false\r\n");
				writer.write("# Is mob damage protection by default in a newly created area ?\r\n");
				writer.write("sanctuaryOnDefault=false\r\n");
				writer.write("# Is creeper explosion disabled by default in a newly created area ?\r\n");
				writer.write("creeperDisabledOnDefault=false\r\n");
				writer.write("# Is pvp disabled by default in a newly created area ?\r\n");
				writer.write("pvpDisabledOnDefault=false\r\n");
				writer.write("# Is healing by default in a newly created area ?\r\n");
				writer.write("healOnDefault=false\r\n");
				// Area features : switching allowance
				writer.write("# All the below allowances will define if an owner is able to switch features on/off by himself\r\n");
				writer.write("# Do you want owners to be able to prevent PvP in their area ?\r\n");
				writer.write("allowNoPvpZones=true\r\n");
				writer.write("# Do you want owners to be able to prevent Creeper explosions in their area ?\r\n");
				writer.write("allowNoCreeperZones=true\r\n");
				writer.write("# Do you want owners to be able to restrict the access to their area ?\r\n");
				writer.write("allowRestrictedZones=false\r\n");
				writer.write("# Do you want owners to be able to mob spawn & mob damage in their area ?\r\n");
				writer.write("allowSanctuaries=true\r\n");
				writer.write("# How much are the player healed by tick in the healing areas ?\r\n");
				writer.write("# 0 -> disable feature; 1 -> minimum; Max health is 20 for players\r\n");
				writer.write("healPower=1\r\n");
				writer.write("# How often are players healed in a healing area ? (seconds, minimum 1)\r\n");
				writer.write("healDelay=1\r\n");
				writer.write("#Do you want to allow areas to be restricted, have welcome, farwell messages & heal ?\r\n");
				writer.write("onMoveFeatures=true\r\n");
				writer.write("#Do you want players of a zone to be able to backup/restore areas they own ? (beware :"
						+ " duplication possible) \r\n");
				writer.write("allowOwnersToBackup=false\r\n");
				writer.write("#Height and depth added to cuboid areas\r\n");
				writer.write("#(only when a flat area is selected to be protected)\r\n");
				writer.write("minProtectedHeight=0\r\n");
				// SQL
			} catch (Exception e) {
				getLogger().log(Level.SEVERE, 
						"Could not create cuboidPlugin.properties file inside 'cuboids' folder.",
						e);
			} finally {
				try {
					if (writer != null) {
						writer.close();
					}
				} catch (IOException e) {
					getLogger().log(Level.SEVERE, 
							"Exception while closing writer for cuboidPlugin.properties",
							e);
				}
			}
		}
		// TODO update config
		/*try {
			// Protection properties
			CuboidAreas.addedHeight = properties
					.getInt("minProtectedHeight", 0);
			protectionWarn = properties.getBoolean("protectionWarning", false);
			CuboidAreas.newestHavePriority = properties.getBoolean(
					"newestHavePriority", true);
			// Worldwide features toggle
			restrictedGroups = properties.getString("restrictedGroups", "")
					.split(",");
			for (int i = 0; i < restrictedGroups.length; i++) {
				restrictedGroups[i] = restrictedGroups[i].trim();
			}
			// general cuboid properties
			logging = properties.getBoolean("fullLogging", false);
			allowBlacklistedBlocks = properties.getBoolean(
					"allowBlacklistedBlocks", false);
			chestProtection = properties.getBoolean("chestProtection", true);
			mainToolID = properties.getInt("mainToolID", 269);
			checkToolID = properties.getInt("checkToolID", 268);
			writeDelay = (long) (60000 * properties.getInt("autoSaveDelay", 30));
			onMoveFeatures = properties.getBoolean("onMoveFeatures", true);
			protectionSytem = properties.getBoolean("protectionSytem", true);
			protectionOnDefault = properties.getBoolean("protectionOnDefault",
					false);
			restrictedOnDefault = properties.getBoolean("restrictedOnDefault",
					false);
			sanctuaryOnDefault = properties.getBoolean("sanctuaryOnDefault",
					false);
			creeperDisabledOnDefault = properties.getBoolean(
					"creeperDisabledOnDefault", false);
			pvpDisabledOnDefault = properties.getBoolean(
					"pvpDisabledOnDefault", false);
			healOnDefault = properties.getBoolean("healOnDefault", false);
			allowRestrictedZones = properties.getBoolean(
					"allowRestrictedZones", false);
			allowNoPvpZones = properties.getBoolean("allowNoPvpZones", true);
			allowNoCreeperZones = properties.getBoolean("allowNoCreeperZones",
					true);
			allowSanctuaries = properties.getBoolean("allowSanctuaries", false);
			int healPower = (int) Math.ceil(properties.getInt("healPower", 0));
			if (healPower < 0) {
				healPower = 0;
			}
			CuboidAreas.healPower = healPower;
			long healDelay = (long) Math
					.ceil(properties.getInt("healDelay", 1));
			if (healDelay < 1) {
				healDelay = 1;
			}
			CuboidAreas.healDelay = healDelay * 1000;

			// generating list of operable items within protected areas
			operableItems = new ArrayList<Integer>();
			String[] operableString = properties.getString("operableItemIDs",
					"").split(",");
			for (String operableItem : operableString) {
				if (operableItem == null || operableItem.equalsIgnoreCase("")) {
					continue;
				}
				try {
					int operableItemID = Integer.parseInt(operableItem);
					operableItems.add(operableItemID);
				} catch (NumberFormatException e) {
					log.info("CuboidPlugin : invalid item ID skipped : "
							+ operableItem);
				}
			}

			// reading state of global features if needed
			File globalFile = new File("cuboids/globalFeatues.dat");
			if (globalFile.exists()) {
				try {
					ObjectInputStream ois = new ObjectInputStream(
							new FileInputStream(globalFile));
					globalDisablePvP = (Boolean) ois.readObject();
					globalCreeperProt = (Boolean) ois.readObject();
					globalSanctuary = (Boolean) ois.readObject();
					ois.close();
				} catch (Exception e) {
					log.severe("CuboidPlugin : Error while reading the state of global features");
				}
			}

			log.info("CuboidPlugin : properties loaded");
		} catch (Exception e) {
			log.log(Level.SEVERE,
					"Exception while reading from server.properties", e);
		}*/

	}

	// //////////////////////
	// // FUNCTIONS ////
	// //////////////////////
	
	public Player playerMatch(String name) {
		if (getServer().getOnlinePlayers().length < 1) {
			return null;
		}

		Player[] online = getServer().getOnlinePlayers();
		Player lastPlayer = null;

		for (Player player : online) {
			String playerName = player.getName();
			String playerDisplayName = player.getDisplayName();

			if (playerName.equalsIgnoreCase(name)) {
				lastPlayer = player;
				break;
			} else if (playerDisplayName.equalsIgnoreCase(name)) {
				lastPlayer = player;
				break;
			}

			if (playerName.toLowerCase().indexOf(name.toLowerCase()) != -1) {
				if (lastPlayer != null) {
					return null;
				}

				lastPlayer = player;
			} else if (playerDisplayName.toLowerCase().indexOf(
					name.toLowerCase()) != -1) {
				if (lastPlayer != null) {
					return null;
				}

				lastPlayer = player;
			}
		}

		return lastPlayer;
	}

	public boolean isValidBlockID(int blocID) {
		if (blocID >= 0 && blocID <= 91) {
			if ((blocID > 20 && blocID < 35) || blocID == 36) {
				return false;
			} else {
				return true;
			}
		} else
			return false;
	}

	public boolean isCreatorItem(ItemStack itemStack) {
		if (itemStack.getTypeId() == 259 || itemStack.getTypeId() == 290 || itemStack.getTypeId() == 291 || itemStack.getTypeId() == 292
				|| itemStack.getTypeId() == 293 || itemStack.getTypeId() == 294 || itemStack.getTypeId() == 295 || itemStack.getTypeId() == 323
				|| itemStack.getTypeId() == 324 || itemStack.getTypeId() == 325 || itemStack.getTypeId() == 326 || itemStack.getTypeId() == 327
				|| itemStack.getTypeId() == 330 || itemStack.getTypeId() == 331 || itemStack.getTypeId() == 338) {
			return true;
		}
		return false;
	}

	public boolean cuboidExists(String playerName, String cuboidName) {
		return new File("cuboids/" + playerName + "/" + cuboidName + ".cuboid")
				.exists();
	}

	public String listPersonalCuboids(String owner) {
		if (!new File("cuboids").exists()
				|| !new File("cuboids/" + owner).exists()) {
			return null;
		}
		String[] fileList = new File("cuboids/" + owner).list();
		String result = (fileList.length > 0) ? "" : null;

		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].endsWith(".cuboid") == true) {
				result += " "
						+ fileList[i].substring(0, fileList[i].length() - 7);
			}
		}

		return result;
	}

	public void printCuboidHelp(Player player) {
		player.sendMessage("/cmod list - prints a list of cuboid areas");
		player.sendMessage("/cmod who - prints a list of players in this area");
		player.sendMessage("/cmod <name> info - prints info about the area");
		player.sendMessage("/cmod <name> allow <list> - allow players/commands");
		player.sendMessage("/cmod <name> disallow <list> - disallow players/commands");
		player.sendMessage("/cmod <name> toggle <option> - toggles the option");
		player.sendMessage("/cmod <name> welcome <text> - sets welcome message");
		player.sendMessage("/cmod <name> farewell <text> - sets farewell message");
		player.sendMessage("/cmod <name> warning <text> - sets 'restricted' message");
		player.sendMessage("/cmod <name> backup - backs up the cuboidArea");
		player.sendMessage("/cmod <name> restore - restores the cuboidArea");
		if (player.hasPermission("/cuboid")) {
			player.sendMessage("/cmod reload - reloads CuboidPlugin properties");
		}
		if (player.hasPermission("/protect")) {
			player.sendMessage("/cmod <name> create - creates a new cuboidArea");
			player.sendMessage("/cmod <name> delete - deletes the cuboidArea");
			player.sendMessage("/cmod <name> move - moves the cuboidArea to selection");

		}
	}

	private void onPluginStop() {
		for (Player p : this.getServer().getOnlinePlayers()) {
			CuboidAreas.leaveAll(p);
		}
		CuboidAreas.writeCuboidAreas();

		if (globalCreeperProt || globalDisablePvP || globalSanctuary) {
			try {
				ObjectOutputStream oos = new ObjectOutputStream(
						new FileOutputStream(new File(
								"cuboids/globalFeatues.dat")));
				oos.writeObject(globalDisablePvP);
				oos.writeObject(globalCreeperProt);
				oos.writeObject(globalSanctuary);
				oos.close();
			} catch (Exception e) {
				getLogger().log(Level.SEVERE, "Error while writing the state of global features");
			}
		} else {
			File globalFile = new File("cuboids/globalFeatues.dat");
			if (globalFile.exists()) {
				globalFile.delete();
			}
		}

		getLogger().log(Level.INFO, "shutting down");
	}

	private void onPluginReload() {
		checkFolder();
		CuboidAreas.loadCuboidAreas();
		loadProperties();
		notTeleport = new ArrayList<String>();
		if (writeDelay > 0) {
			writeTimer.schedule(new WriteJob(), writeDelay);
		}

		CuboidAreas.inside = new HashMap<String, ArrayList<CuboidC>>();

		for (Player p : this.getServer().getOnlinePlayers()) {
			CuboidAreas.movement(p, p.getLocation());
		}
	}

	public boolean isGloballyRestricted(Player player) {
		if (player.hasPermission("cuboidplugin.globalretrict"))
			return true;

		return false;
	}

	public void broadcast(String message) {
		for (Player p : this.getServer().getOnlinePlayers()) {
			p.sendMessage(message);
		}
	}

	
	// TODO finish removal of all events from main
	public class CuboidListener implements Listener {
		
		public boolean onComplexBlockChange(Player player, ComplexBlock block) {
			if (block instanceof Chest) {
				if (chestProtection
						&& !player.hasPermission("/ignoresOwnership")) {
					CuboidC cuboid = CuboidAreas.findCuboidArea(block.getX(),
							block.getY(), block.getZ());
					if (cuboid != null && cuboid.protection) {
						return !cuboid.isAllowed(player);
					}
				}
				return isGloballyRestricted(player);
			}
			return false;
		}

		public boolean onSendComplexBlock(Player player, ComplexBlock block) {
			if (block instanceof Chest) {
				if (chestProtection
						&& !player.hasPermission("/ignoresOwnership")) {
					CuboidC cuboid = CuboidAreas.findCuboidArea(block.getX(),
							block.getY(), block.getZ());
					if (cuboid != null && cuboid.protection) {
						return !cuboid.isAllowed(player);
					}
				}
				return isGloballyRestricted(player);
			}
			return false;
		}

		public boolean onConsoleCommand(String[] split) {
			if (split[0].equalsIgnoreCase("stop")) {
				onPluginStop();
			}
			return false;
		}

		
	}
	// TODO end of old events

	public class WriteJob extends TimerTask {
		public void run() {
			CuboidAreas.writeCuboidAreas();
			writeTimer.schedule(new WriteJob(), writeDelay);
		}
	}

}