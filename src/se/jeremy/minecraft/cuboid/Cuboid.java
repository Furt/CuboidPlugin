package se.jeremy.minecraft.cuboid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import se.jeremy.minecraft.cuboid.commands.*;
import se.jeremy.minecraft.cuboid.listener.*;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Cuboid extends JavaPlugin implements Listener {

	public String name = "CuboidPlugin";
	static boolean logging = false;
	// TODO : look for // SQL
	static boolean SQLstorage = false;

	static ArrayList<Integer> operableItems;
	static boolean allowBlacklistedBlocks = false;
	public static boolean chestProtection = true;
	
	// TODO : Remove ID in favor of Material
	public static int mainToolID = 269;
	public static Material mainTool = Material.WOOD_SPADE;
	
	// TODO : Remove ID in favor of Material
	public static int checkToolID = 268;
	public static Material checkTool = Material.WOOD_AXE;

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
	
	// Listeners
	public CPBlock cpb = new CPBlock(this);
	public CPPlayer cpp = new CPPlayer();
	public CPEntity cpe = new CPEntity();
	public static File data;
	public static Cuboid plugin;

	@Override
	public void onEnable() {
		data = this.getDataFolder();
		plugin = this;
		checkFolder();
		loadProperties();
		CuboidAreas.loadCuboidAreas();
		getServer().getPluginManager().registerEvents(cpb, this);
		getServer().getPluginManager().registerEvents(cpp, this);
		getServer().getPluginManager().registerEvents(cpe, this);
		setupCommands();

		notTeleport = new ArrayList<String>();
		if (writeDelay > 0) {
			writeTimer.schedule(new WriteJob(), writeDelay);
		}

		CuboidAreas.inside = new HashMap<String, ArrayList<CuboidC>>();

		for (Player p : getServer().getOnlinePlayers()) {
			CuboidAreas.movement(p, p.getLocation());
		}
		getLogger().log(Level.INFO, "initializing v17.9 for hMod 131+");
	}

	@Override
	public void onDisable() {
		for (Player p : getServer().getOnlinePlayers()) {
			CuboidAreas.leaveAll(p);
		}
		CuboidAreas.writeCuboidAreas();

		if (globalCreeperProt || globalDisablePvP || globalSanctuary) {
			try {
				ObjectOutputStream oos = new ObjectOutputStream(
						new FileOutputStream(new File(getDataFolder(),
								"globalFeatues.dat")));
				oos.writeObject(globalDisablePvP);
				oos.writeObject(globalCreeperProt);
				oos.writeObject(globalSanctuary);
				oos.close();
			} catch (Exception e) {
				getLogger().log(Level.SEVERE,
						"Error while writing the state of global features");
			}
		} else {
			File globalFile = new File(getDataFolder(), "globalFeatues.dat");
			if (globalFile.exists()) {
				globalFile.delete();
			}
		}

		getLogger().log(Level.INFO, "shutting down");
		writeTimer = new Timer();
		CuboidAreas.healTimer = new Timer();
	}

	public static void log(Level level, String msg) {
		plugin.getLogger().log(level, msg);
	}
	// TODO finish adding them all
	private void setupCommands() {
		// getCommand("ccircle").setExecutor(new CCircleCommand(this));
		// getCommand("ccopy").setExecutor(new CCopyCommand());
		getCommand("cmod").setExecutor(new CModCommand(this));
		getCommand("undo").setExecutor(new UndoCommand());
		getCommand("protect").setExecutor(new CProtectCommand());
		getCommand("cwalls").setExecutor(new CWallsCommand(this));
	}

	/*
	 * Ensures the existence of the full directory
	 */
	private void checkFolder() {
		if (!getDataFolder().exists())
			getDataFolder().mkdir();

		if (!new File(getDataFolder(), "config.yml").exists())
			saveDefaultConfig();
		if (!new File(getDataFolder(), "cuboidAreas.dat").exists()) {
			try {
				new File(getDataFolder(), "cuboidAreas.dat").createNewFile();
			} catch (Exception e) {

			}
		}
	}

	public void loadProperties() {
		try {
			// Protection properties
			CuboidAreas.addedHeight = getConfig().getInt("minProtectedHeight");
			protectionWarn = getConfig().getBoolean("protectionWarning");
			CuboidAreas.newestHavePriority = getConfig().getBoolean(
					"newestHavePriority");
			// Worldwide features toggle
			restrictedGroups = getConfig().getString("restrictedGroups").split(
					",");
			for (int i = 0; i < restrictedGroups.length; i++) {
				restrictedGroups[i] = restrictedGroups[i].trim();
			}
			// general cuboid properties
			logging = getConfig().getBoolean("fullLogging");
			allowBlacklistedBlocks = getConfig().getBoolean(
					"allowBlacklistedBlocks");
			chestProtection = getConfig().getBoolean("chestProtection");
			mainToolID = getConfig().getInt("mainToolID");
			checkToolID = getConfig().getInt("checkToolID");
			writeDelay = (long) (60000 * getConfig().getInt("autoSaveDelay"));
			onMoveFeatures = getConfig().getBoolean("onMoveFeatures");
			protectionSytem = getConfig().getBoolean("protectionSytem");
			protectionOnDefault = getConfig().getBoolean("protectionOnDefault");
			restrictedOnDefault = getConfig().getBoolean("restrictedOnDefault");
			sanctuaryOnDefault = getConfig().getBoolean("sanctuaryOnDefault");
			creeperDisabledOnDefault = getConfig().getBoolean(
					"creeperDisabledOnDefault");
			pvpDisabledOnDefault = getConfig().getBoolean(
					"pvpDisabledOnDefault");
			healOnDefault = getConfig().getBoolean("healOnDefault");
			allowRestrictedZones = getConfig().getBoolean(
					"allowRestrictedZones");
			allowNoPvpZones = getConfig().getBoolean("allowNoPvpZones");
			allowNoCreeperZones = getConfig().getBoolean("allowNoCreeperZones");
			allowSanctuaries = getConfig().getBoolean("allowSanctuaries");
			int healPower = (int) Math.ceil(getConfig().getInt("healPower"));
			if (healPower < 0) {
				healPower = 0;
			}
			CuboidAreas.healPower = healPower;
			long healDelay = (long) Math.ceil(getConfig().getInt("healDelay"));
			if (healDelay < 1) {
				healDelay = 1;
			}
			CuboidAreas.healDelay = healDelay * 1000;

			// generating list of operable items within protected areas
			operableItems = new ArrayList<Integer>();
			String[] operableString = getConfig().getString("operableItemIDs")
					.split(",");
			for (String operableItem : operableString) {
				if (operableItem == null || operableItem.equalsIgnoreCase("")) {
					continue;
				}
				try {
					operableItems.add(Integer.parseInt(operableItem));
				} catch (NumberFormatException e) {
					getLogger().log(Level.INFO,
							"Invalid item ID skipped : " + operableItem);
				}
			}

			// reading state of global features if needed
			File globalFile = new File(getDataFolder() + File.separator
					+ "globalFeatues.dat");
			if (globalFile.exists()) {
				try {
					ObjectInputStream ois = new ObjectInputStream(
							new FileInputStream(globalFile));
					globalDisablePvP = (Boolean) ois.readObject();
					globalCreeperProt = (Boolean) ois.readObject();
					globalSanctuary = (Boolean) ois.readObject();
					ois.close();
				} catch (Exception e) {
					getLogger().log(Level.SEVERE,
							"Error while reading the state of global features");
				}
			}

			getLogger().log(Level.INFO, "Properties loaded");
		} catch (Exception e) {
			getLogger().log(Level.SEVERE,
					"Exception while reading from config.yml", e);
		}

	}

	// //////////////////////
	// // FUNCTIONS ////
	// //////////////////////

	public Player playerMatch(String name) {
		Collection<? extends Player> online = Bukkit.getOnlinePlayers();
		
		if (online.size() < 1) {
			return null;
		}
		
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

	public boolean isCreatorItem(ItemStack itemStack) {
		if (itemStack.getTypeId() == 259 || itemStack.getTypeId() == 290
				|| itemStack.getTypeId() == 291 || itemStack.getTypeId() == 292
				|| itemStack.getTypeId() == 293 || itemStack.getTypeId() == 294
				|| itemStack.getTypeId() == 295 || itemStack.getTypeId() == 323
				|| itemStack.getTypeId() == 324 || itemStack.getTypeId() == 325
				|| itemStack.getTypeId() == 326 || itemStack.getTypeId() == 327
				|| itemStack.getTypeId() == 330 || itemStack.getTypeId() == 331
				|| itemStack.getTypeId() == 338) {
			return true;
		}
		return false;
	}

	public boolean cuboidExists(String playerName, String cuboidName) {
		return new File(getDataFolder() + File.separator + playerName,
				cuboidName + ".cuboid").exists();
	}

	public String listPersonalCuboids(String owner) {
		if (!new File(getDataFolder() + File.separator + owner).exists()) {
			return null;
		}
		String[] fileList = new File(getDataFolder() + File.separator + owner)
				.list();
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

	public boolean isGloballyRestricted(Player player) {
		if (player.hasPermission("cuboidplugin.globalrestrict"))
			return true;

		return false;
	}

	public class WriteJob extends TimerTask {
		public void run() {
			CuboidAreas.writeCuboidAreas();
			writeTimer.schedule(new WriteJob(), writeDelay);
		}
	}

}