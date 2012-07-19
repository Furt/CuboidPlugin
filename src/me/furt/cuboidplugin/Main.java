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
	static final Logger log = Logger.getLogger("Minecraft");
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
	static String[] restrictedGroups;
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
	static boolean allowOwnersToBackup = false;
	static boolean allowRestrictedZones = false;
	static boolean allowNoPvpZones = true;
	static boolean allowNoCreeperZones = true;
	static boolean allowSanctuaries = false;
	// List of players denied entry to a restricted cuboid, that are to not
	// trigger the teleport functions
	public static ArrayList<String> notTeleport;
	// Temporaty fix for wrinting to disk...
	static long writeDelay = 1800000;

	static Timer writeTimer = new Timer();

	@Override
	public void onEnable() {
		onPluginReload();
		log.info("CuboidPlugin : initializing v17.9 for hMod 131+");
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
				log.severe("CuboidPlugin : could not create the cuboids folder");
				return false;
			}
		}
		return true;
	}

	private void loadProperties() {
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
				log.log(Level.SEVERE,
						"Could not create cuboidPlugin.properties file inside 'cuboids' folder.",
						e);
			} finally {
				try {
					if (writer != null) {
						writer.close();
					}
				} catch (IOException e) {
					log.log(Level.SEVERE,
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

	private boolean isValidBlockID(int blocID) {
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

	private boolean cuboidExists(String playerName, String cuboidName) {
		return new File("cuboids/" + playerName + "/" + cuboidName + ".cuboid")
				.exists();
	}

	private String listPersonalCuboids(String owner) {
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

	private void printCuboidHelp(Player player) {
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
				log.severe("CuboidPlugin : Error while writing the state of global features");
			}
		} else {
			File globalFile = new File("cuboids/globalFeatues.dat");
			if (globalFile.exists()) {
				globalFile.delete();
			}
		}

		log.info("CuboidPlugin : shutting down");
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

	private void broadcast(String message) {
		for (Player p : this.getServer().getOnlinePlayers()) {
			p.sendMessage(message);
		}
	}

	public class CuboidListener implements Listener {

		public boolean onCommand(Player player, String[] split) {
			String playerName = player.getName();
			CuboidC playersArea = CuboidAreas.findCuboidArea((int) player
					.getLocation().getX(), (int) player.getLocation().getY(),
					(int) player.getLocation().getZ());
			if (playersArea != null && !playersArea.isAllowed(split[0])
					&& !playersArea.isOwner(player)
					&& !player.hasPermission("/ignoresOwnership")) {
				player.sendMessage(ChatColor.RED
						+ "This command is disallowed in this area");
				return true;
			}

			if (split[0].equalsIgnoreCase("/cmod")) {
				if (split.length == 1) {
					printCuboidHelp(player);
					return true;
				}
				if (split.length == 2) {
					if (split[1].equalsIgnoreCase("list")) {
						player.sendMessage(ChatColor.YELLOW + "Cuboid areas"
								+ ChatColor.YELLOW + " : "
								+ CuboidAreas.displayCuboidsList());
					} else if (split[1].equalsIgnoreCase("who")) {
						if (!onMoveFeatures) {
							player.sendMessage(ChatColor.YELLOW
									+ "onMove functions are disabled. So are area playerlists");
						} else {
							CuboidC cuboidArea = CuboidAreas.findCuboidArea(
									(int) player.getLocation().getX(),
									(int) player.getLocation().getY(),
									(int) player.getLocation().getZ());
							if (cuboidArea != null)
								cuboidArea.printPresentPlayers(player);
							else
								player.sendMessage("You are not in a cuboid area");
						}
					} else if (split[1].startsWith("own")) {
						CuboidAreas.displayOwnedList(player);
					} else if (split[1].startsWith("global")) {
						String message = "";
						for (String group : restrictedGroups) {
							message += " " + group;
						}
						if (!message.equalsIgnoreCase("")) {
							player.sendMessage(ChatColor.YELLOW
									+ "Restricted group(s) :"
									+ ChatColor.YELLOW + message);
						} else {
							player.sendMessage(ChatColor.YELLOW
									+ "No restricted group");
						}
						if (globalDisablePvP) {
							player.sendMessage(ChatColor.YELLOW + "PvP : "
									+ ChatColor.YELLOW + "disabled");
						} else {
							player.sendMessage(ChatColor.YELLOW + "PvP : "
									+ ChatColor.YELLOW + "allowed");
						}
						if (globalCreeperProt) {
							player.sendMessage(ChatColor.YELLOW
									+ "Creeper explosions :" + ChatColor.YELLOW
									+ " disabled");
						} else {
							player.sendMessage(ChatColor.YELLOW
									+ "Creeper explosions :" + ChatColor.YELLOW
									+ " enabled");
						}
						if (globalSanctuary) {
							player.sendMessage(ChatColor.YELLOW + "Monsters :"
									+ ChatColor.YELLOW + " harmless");
						} else {
							player.sendMessage(ChatColor.YELLOW + "Monsters :"
									+ ChatColor.YELLOW + " dangerous");
						}
						player.sendMessage(ChatColor.RED
								+ "Local setting overwrite global ones.");
					} else if (split[1].equalsIgnoreCase("reload")) {
						if (!player.hasPermission("/cuboid")) {
							player.sendMessage(ChatColor.RED
									+ "You are not allowed to use this command.");
							return true;
						}
						loadProperties();
						player.sendMessage(ChatColor.GREEN
								+ "CuboidPlugin properties reloaded");
						log.info("CuboidPlugin : properties reloaded");
					} else if (split[1].equalsIgnoreCase("write")) {
						if (!player.hasPermission("/cuboid")) {
							player.sendMessage(ChatColor.RED
									+ "You are not allowed to use this command.");
							return true;
						}
						CuboidAreas.writeCuboidAreas();
						player.sendMessage(ChatColor.GREEN
								+ "CuboidPlugin data written to hard drive.");
					} else {
						printCuboidHelp(player);
					}
					return true;
				}

				if (split[1].equalsIgnoreCase("globaltoggle") && player.isOp()) {
					if (split[2].equalsIgnoreCase("pvp")) {
						globalDisablePvP = !globalDisablePvP;
						if (globalDisablePvP) {
							broadcast(ChatColor.LIGHT_PURPLE
									+ "PvP is now allowed only in specific areas");
						} else {
							broadcast(ChatColor.LIGHT_PURPLE
									+ "PvP is now allowed anywhere");
						}

					} else if (split[2].equalsIgnoreCase("creeper")
							|| split[2].equalsIgnoreCase("creepers")) {
						globalCreeperProt = !globalCreeperProt;
						if (globalCreeperProt) {
							broadcast(ChatColor.LIGHT_PURPLE
									+ "Creepers now explode only in specific areas");
						} else {
							broadcast(ChatColor.LIGHT_PURPLE
									+ "Creepers now explode anywhere");
						}
					} else if (split[2].equalsIgnoreCase("sanctuary")
							|| split[2].equalsIgnoreCase("sanctuaries")) {
						globalSanctuary = !globalSanctuary;
						if (globalSanctuary) {
							broadcast(ChatColor.LIGHT_PURPLE
									+ "Monsters now hurt only in specific areas");
						} else {
							broadcast(ChatColor.LIGHT_PURPLE
									+ "Monsters now hurt anywhere");
						}
					} else {
						player.sendMessage(ChatColor.RED
								+ "Usage : /cmod globaltoggle <pvp | creepers | sanctuary>");
					}
					return true;
				}

				if (split[2].equalsIgnoreCase("create")
						|| split[2].equalsIgnoreCase("add")) {
					if (!player.hasPermission("/protect")) {
						player.sendMessage(ChatColor.RED
								+ "You are not allowed to use this command.");
						return true;
					}
					if (CuboidAction.isReady(playerName, true))
						CuboidAreas.createCuboidArea(player, split[1]);
					else
						player.sendMessage(ChatColor.RED
								+ "No cuboid has been selected");
					return true;
				}

				CuboidC cuboidArea = CuboidAreas.findCuboidArea(split[1]);
				if (cuboidArea == null) {
					player.sendMessage(ChatColor.RED + "Area not found : "
							+ split[1]);
					return true;
				}

				if (split[2].equalsIgnoreCase("allow")) {
					if (split.length < 4) {
						player.sendMessage(ChatColor.RED
								+ "Usage : allow <player> and/or </command>");
						player.sendMessage(ChatColor.YELLOW
								+ "Use <playername> to simply allow a player");
						player.sendMessage(ChatColor.YELLOW
								+ "Use o:<playername> to set a new owner");
						player.sendMessage(ChatColor.YELLOW
								+ "Use g:<groupname> to allow an entire group");
						return true;
					}
					if (cuboidArea.isOwner(player)
							|| player.hasPermission("/protect")) {
						String[] parameters = new String[split.length - 3];
						for (int i = 3; i < split.length; i++) {
							parameters[i - 3] = split[i];
						}
						CuboidAreas.treatAllowance(player, parameters,
								cuboidArea);
					} else {
						player.sendMessage(ChatColor.RED
								+ "You are not an owner of this cuboid area.");
						return true;
					}
				} else if (split[2].equalsIgnoreCase("disallow")
						|| split[2].equalsIgnoreCase("revoke")) {
					if (split.length < 4) {
						player.sendMessage(ChatColor.RED
								+ "Usage : disallow <player> and/or </command>");
						return true;
					}
					if (cuboidArea.isOwner(player)
							|| player.hasPermission("/protect")) {
						String[] parameters = new String[split.length - 3];
						for (int i = 3; i < split.length; i++) {
							parameters[i - 3] = split[i];
						}
						CuboidAreas.treatDisallowance(player, parameters,
								cuboidArea);
					} else {
						player.sendMessage(ChatColor.RED
								+ "You are not an owner of this cuboid area.");
						return true;
					}
				} else if (split[2].startsWith("info")) {
					cuboidArea.printInfos(player, true, true);
				} else if (split[2].startsWith("select")) {
					if (!player.hasPermission("/cuboid")) {
						player.sendMessage(ChatColor.RED
								+ "You are not allowed to use this command.");
						return true;
					}
					CuboidAction.setBothPoint(playerName, cuboidArea.coords);
					player.sendMessage(ChatColor.GREEN + "Area selected : "
							+ cuboidArea.name);
				} else if (split[2].equalsIgnoreCase("move")) {
					if (!player.hasPermission("/protect")) {
						player.sendMessage(ChatColor.RED
								+ "You are not allowed to use this command.");
						return true;
					}
					if (CuboidAction.isReady(playerName, true))
						CuboidAreas.moveCuboidArea(player, cuboidArea);
					else
						player.sendMessage(ChatColor.RED
								+ "No cuboid has been selected");
				} else if (split[2].equalsIgnoreCase("delete")
						|| split[2].equalsIgnoreCase("remove")) {
					if (!player.hasPermission("/protect")) {
						player.sendMessage(ChatColor.RED
								+ "You are not allowed to use this command.");
						return true;
					}
					CuboidAreas.removeCuboidArea(player, cuboidArea);
				} else if (split[2].equalsIgnoreCase("toggle")) {
					if (cuboidArea.isOwner(player)
							|| player.hasPermission("/protect")
							|| player.hasPermission("/cuboidAreas")) {
						if (split.length < 4) {
							player.sendMessage(ChatColor.RED
									+ "Usage : toggle"
									+ "<protection/restriction/pvp/heal/sanctuary/ceeper>");
							return true;
						}
						if (split[3].startsWith("prot")) {
							cuboidArea.protection = !cuboidArea.protection;
							CuboidAction.updateChestsState(
									cuboidArea.coords[0], cuboidArea.coords[1],
									cuboidArea.coords[2], cuboidArea.coords[3],
									cuboidArea.coords[4], cuboidArea.coords[5]);
							player.sendMessage(ChatColor.GREEN
									+ "Protection : "
									+ (cuboidArea.protection ? "enabled"
											: "disabled"));
						} else if (split[3].startsWith("restric")) {
							if (!player.hasPermission("/cuboidAreas")
									&& !allowRestrictedZones) {
								player.sendMessage(ChatColor.YELLOW
										+ "Restricted areas switching is disabled");
								return true;
							}
							cuboidArea.restricted = !cuboidArea.restricted;
							player.sendMessage(ChatColor.GREEN
									+ "Restricted access : "
									+ (cuboidArea.restricted ? "enabled"
											: "disabled"));
						} else if (split[3].equalsIgnoreCase("pvp")) {
							if (!player.hasPermission("/cuboidAreas")
									&& !allowNoPvpZones) {
								player.sendMessage(ChatColor.YELLOW
										+ "No-PvP areas switching is disabled");
								return true;
							}
							cuboidArea.PvP = !cuboidArea.PvP;
							player.sendMessage(ChatColor.GREEN
									+ "No PvP allowed : "
									+ (!cuboidArea.PvP ? "enabled" : "disabled"));
						} else if (split[3].equalsIgnoreCase("heal")) {
							if (!player.hasPermission("/cuboidAreas")
									&& CuboidAreas.healPower == 0) {
								player.sendMessage(ChatColor.YELLOW
										+ "Healing areas switching is disabled");
								return true;
							}
							cuboidArea.heal = !cuboidArea.heal;
							player.sendMessage(ChatColor.GREEN
									+ "Healing : "
									+ (cuboidArea.heal ? "enabled" : "disabled"));
						} else if (split[3].equalsIgnoreCase("sanctuary")) {
							if (!player.hasPermission("/cuboidAreas")
									&& !allowSanctuaries) {
								player.sendMessage(ChatColor.YELLOW
										+ "Sanctuaries switching is disabled");
								return true;
							}
							cuboidArea.sanctuary = !cuboidArea.sanctuary;
							player.sendMessage(ChatColor.GREEN
									+ "Sanctuary : "
									+ (cuboidArea.sanctuary ? "enabled"
											: "disabled"));
						} else if (split[3].equalsIgnoreCase("creeper")) {
							if (!player.hasPermission("/cuboidAreas")
									&& !allowNoCreeperZones) {
								player.sendMessage(ChatColor.YELLOW
										+ "No-Creeper areas switching is disabled");
								return true;
							}
							cuboidArea.creeper = !cuboidArea.creeper;
							player.sendMessage(ChatColor.GREEN
									+ "No creeper explosion : "
									+ (!cuboidArea.creeper ? "enabled"
											: "disabled"));
						} else {
							player.sendMessage(ChatColor.RED
									+ "Usage : toggle <protection/restriction/pvp/heal/sanctuary/creeper>");
						}
					} else {
						player.sendMessage(ChatColor.RED
								+ "You are not an owner of this cuboid area.");
						return true;
					}
				} else if (split[2].equalsIgnoreCase("backup")) {
					if (cuboidArea.isOwner(player)
							|| player.hasPermission("/protect")) {
						byte returnCode = new CuboidBackup(null, cuboidArea, true)
								.writeToDisk();
						if (returnCode == 0) {
							player.sendMessage(ChatColor.GREEN
									+ "Cuboid area successfuly backed up");
						} else if (returnCode == 1) {
							player.sendMessage(ChatColor.RED
									+ "Error when creating necessary folders");
						} else {
							player.sendMessage(ChatColor.RED
									+ "Error when writing the cuboid file");
						}
					} else {
						player.sendMessage(ChatColor.RED
								+ "You are not an owner of this cuboid area.");
						return true;
					}
				} else if (split[2].equalsIgnoreCase("restore")) {
					if ((allowOwnersToBackup && cuboidArea.isOwner(player))
							|| player.hasPermission("/protect")) {
						byte returnCode = new CuboidBackup(null, cuboidArea, false)
								.loadFromDisc();
						if (returnCode == 0) {
							player.sendMessage(ChatColor.GREEN
									+ "Cuboid area successfuly restored");
						} else if (returnCode == 1) {
							player.sendMessage(ChatColor.RED
									+ "Found no backup of this cuboid Area");
						} else if (returnCode == 2) {
							player.sendMessage(ChatColor.RED
									+ "Not allowed to access the backup file");
						} else if (returnCode == 3) {
							player.sendMessage(ChatColor.RED
									+ "Error while reading the backup file");
						}
					} else {
						player.sendMessage(ChatColor.RED
								+ "You are not an owner of this cuboid area.");
						return true;
					}
				} else if (split[2].equalsIgnoreCase("welcome")) {
					if (cuboidArea.isOwner(player)
							|| player.hasPermission("/protect")) {
						if (split.length == 3) {
							cuboidArea.welcomeMessage = null;
							player.sendMessage(ChatColor.GREEN
									+ "Welcome message disabled.");
							return true;
						}
						String message = "";
						for (int i = 3; i < split.length; i++) {
							message += " " + split[i];
						}
						cuboidArea.welcomeMessage = message.trim();
						player.sendMessage(ChatColor.GREEN
								+ "Welcome message successfuly changed.");
					} else {
						player.sendMessage(ChatColor.RED
								+ "You are not an owner of this cuboid area.");
						return true;
					}
				} else if (split[2].equalsIgnoreCase("farewell")) {
					if (cuboidArea.isOwner(player)
							|| player.hasPermission("/protect")) {
						if (split.length == 3) {
							cuboidArea.farewellMessage = null;
							player.sendMessage(ChatColor.GREEN
									+ "Farewell message disabled.");
							return true;
						}
						String message = "";
						for (int i = 3; i < split.length; i++) {
							message += " " + split[i];
						}
						cuboidArea.farewellMessage = message.trim();
						player.sendMessage(ChatColor.GREEN
								+ "Farewell message successfuly changed.");
					} else {
						player.sendMessage(ChatColor.RED
								+ "You are not an owner of this cuboid area.");
						return true;
					}
				} else if (split[2].equalsIgnoreCase("warning")) {
					if (cuboidArea.isOwner(player)
							|| player.hasPermission("/protect")) {
						if (split.length == 3) {
							cuboidArea.warning = null;
							player.sendMessage(ChatColor.GREEN
									+ "Restricted area message disabled.");
							return true;
						}
						String message = "";
						for (int i = 3; i < split.length; i++) {
							message += " " + split[i];
						}
						cuboidArea.warning = message.trim();
						player.sendMessage(ChatColor.GREEN
								+ "Restricted area message successfuly changed.");
					} else {
						player.sendMessage(ChatColor.RED
								+ "You are not an owner of this cuboid area.");
						return true;
					}
				}
				return true;
			}

			// /////////////////////////////////
			// // PROTECTION COMMANDS ////
			// /////////////////////////////////

			else if (player.hasPermission("/protect")) {
				boolean highProtect = split[0].equalsIgnoreCase("/highprotect");
				if (split[0].equalsIgnoreCase("/protect") || highProtect) {
					if (CuboidAction.isReady(playerName, true)) {
						ArrayList<String> ownersList = new ArrayList<String>();
						int paramSize = split.length;
						if (paramSize > 2) {
							for (short i = 1; i < paramSize - 1; i++) {
								ownersList.add(split[i]);
							}
							String cuboidName = split[paramSize - 1].trim()
									.toLowerCase();
							CuboidAreas.protectCuboidArea(player, ownersList,
									cuboidName, highProtect);
						} else {
							player.sendMessage(ChatColor.YELLOW
									+ "You need to specify at least one player or group, and a name.");
						}
					} else {
						player.sendMessage(ChatColor.RED
								+ "No cuboid has been selected");
					}
					return true;
				}
			}

			// /////////////////////////////
			// // CUBOID COMMANDS ////
			// /////////////////////////////

			if (player.hasPermission("/cuboid")) {

				// ///////////////////////////
				// // CORE COMMANDS ////
				// ///////////////////////////

				if (split[0].equalsIgnoreCase("/csize")) {
					if (CuboidAction.isReady(playerName, true)) {
						player.sendMessage(ChatColor.GREEN
								+ "The selected cuboid size is : "
								+ CuboidAction.blocksCount(playerName)
								+ " blocks");
					} else {
						player.sendMessage(ChatColor.RED
								+ "No cuboid has been selected");
					}
					return true;
				}

				else if (split[0].equalsIgnoreCase("/cdel")) {
					if (CuboidAction.isReady(playerName, true)) {
						CuboidAction.emptyCuboid(playerName);
						player.sendMessage(ChatColor.GREEN
								+ "The cuboid is now empty");
					} else {
						player.sendMessage(ChatColor.RED
								+ "No cuboid has been selected");
					}
					return true;
				}

				else if (split[0].equalsIgnoreCase("/cfill")) {
					if (CuboidAction.isReady(playerName, true)) {
						if (split.length > 1) {
							int blocID = 0;
							try {
								blocID = Integer.parseInt(split[1]);
							} catch (NumberFormatException n) {
								blocID = Material.getMaterial(split[1]).getId();
								//blocID = etc.getDataSource().getItem(split[1]);
							}
							if (isValidBlockID(blocID)) {
								CuboidAction.fillCuboid(playerName, blocID);
								player.sendMessage(ChatColor.GREEN
										+ "The cuboid has been filled");
							} else {
								player.sendMessage(ChatColor.RED +""+ blocID
										+ " is not a valid block ID.");
							}
						} else {
							player.sendMessage(ChatColor.RED
									+ "Usage : /cfill <block id|name>");
						}
					} else {
						player.sendMessage(ChatColor.RED
								+ "No cuboid has been selected");
					}
					return true;
				}

				else if (split[0].equalsIgnoreCase("/creplace")) {
					if (CuboidAction.isReady(playerName, true)) {

						int paramSize = split.length - 1;
						if (paramSize > 1) {
							int[] replaceParams = new int[paramSize];
							for (int i = 0; i < paramSize; i++) {
								try {
									replaceParams[i] = Integer
											.parseInt(split[i + 1]);
								} catch (NumberFormatException n) {
									replaceParams[i] = Material.getMaterial(split[i + 1]).getId();
									//replaceParams[i] = etc.getDataSource().getItem(split[i + 1]);
									if (replaceParams[i] == 0) {
										player.sendMessage(ChatColor.RED
												+ split[i + 1]
												+ " is not a valid block name.");
										return true;
									}
								}
								if (!isValidBlockID(replaceParams[i])) {
									player.sendMessage(ChatColor.RED
											+""+ replaceParams[i]
											+ " is not a valid block ID.");
									return true;
								}
							}

							int blockID = replaceParams[replaceParams.length - 1];
							CuboidAction.replaceBlocks(playerName,
									replaceParams);
							player.sendMessage(ChatColor.GREEN
									+ "The blocks have been replaced");
						} else {
							player.sendMessage(ChatColor.RED
									+ "Usage : /creplace <block id|name> <block id|name>");
						}
					} else {
						player.sendMessage(ChatColor.RED
								+ "No cuboid has been selected");
					}
					return true;
				}

				// //////////////////////////////
				// // MOVEMENT COMMANDS ////
				// //////////////////////////////

				else if (split[0].equalsIgnoreCase("/cmove")) {
					if (CuboidAction.isReady(playerName, true)) {
						if (split.length < 3) {
							player.sendMessage(ChatColor.RED
									+ "Usage : /cmove <direction> <distance>");
							player.sendMessage(ChatColor.RED
									+ "Direction : Up/Down/North/East/West/South");
							return true;
						}

						int howFar = 0;
						try {
							howFar = Integer.parseInt(split[2]);
							if (howFar < 0) {
								player.sendMessage(ChatColor.RED
										+ "Distance must be > 0 !");
								return true;
							}
						} catch (NumberFormatException n) {
							player.sendMessage(ChatColor.RED + split[2]
									+ " is not a valid distance.");
							return true;
						}

						CuboidAction
								.moveCuboidContent(player, split[1], howFar);

					} else {
						player.sendMessage(ChatColor.RED
								+ "No cuboid has been selected");
					}
					return true;
				}

				// /////////////////////////////////////////////
				// // CIRCLE/SPHERE BUILDING COMMANDS ////
				// /////////////////////////////////////////////

				else if (split[0].equalsIgnoreCase("/ccircle")
						|| split[0].equalsIgnoreCase("/cdisc")) {
					if (CuboidAction.isReady(playerName, false)) {
						boolean disc = split[0].equalsIgnoreCase("/cdisc") ? true
								: false;
						int radius = 0;
						int blockID = 4;
						int height = 0;
						if (split.length > 2) {
							try {
								radius = Integer.parseInt(split[1]);
							} catch (NumberFormatException n) {
								player.sendMessage(ChatColor.RED + split[1]
										+ " is not a valid radius.");
								return true;
							}
							if (radius < 1) {
								player.sendMessage(ChatColor.RED + split[1]
										+ " is not a valid radius.");
								return true;
							}

							try {
								blockID = Integer.parseInt(split[2]);
							} catch (NumberFormatException n) {
								blockID = Material.getMaterial(split[2]).getId();
								//blockID = etc.getDataSource().getItem(split[2]);
							}

							if (!isValidBlockID(blockID)) {
								player.sendMessage(ChatColor.RED + split[2]
										+ " is not a valid block ID.");
								return true;
							}

							if (split.length == 4) {
								try {
									height = Integer.parseInt(split[3]);
								} catch (NumberFormatException n) {
									player.sendMessage(ChatColor.RED + split[3]
											+ " is not a valid height.");
									return true;
								}
								if (height > 0) {
									height--;
								} else if (height < 0) {
									height++;
								}
							}

							if (disc) {
								CuboidAction.buildCircle(playerName, radius,
										blockID, height, true);
								player.sendMessage(ChatColor.GREEN + "The "
										+ ((height == 0) ? "disc" : "cylinder")
										+ " has been build");
							} else {
								CuboidAction.buildCircle(playerName, radius,
										blockID, height, false);
								player.sendMessage(ChatColor.GREEN
										+ "The "
										+ ((height == 0) ? "circle"
												: "cylinder")
										+ " has been build");
							}

						} else {
							if (disc) {
								player.sendMessage(ChatColor.RED
										+ "Usage : /cdisc <radius> <block id|name> [height]");
							} else {
								player.sendMessage(ChatColor.RED
										+ "Usage : /ccircle <radius> <block id|name> [height]");
							}
						}
					} else {
						player.sendMessage(ChatColor.RED
								+ "No point has been selected");
					}
					return true;
				}

				else if (split[0].equalsIgnoreCase("/csphere")
						|| split[0].equalsIgnoreCase("/cball")) {
					if (CuboidAction.isReady(playerName, false)) {
						boolean ball = (split[0].equalsIgnoreCase("/cball")) ? true
								: false;
						int radius = 0;
						int blockID = 4;
						if (split.length > 2) {
							try {
								radius = Integer.parseInt(split[1]);
							} catch (NumberFormatException n) {
								player.sendMessage(ChatColor.RED + split[1]
										+ " is not a valid radius.");
								return true;
							}
							if (radius < 2) {
								player.sendMessage(ChatColor.RED
										+ "The radius has to be greater than 1");
								return true;
							}

							try {
								blockID = Integer.parseInt(split[2]);
							} catch (NumberFormatException n) {
								blockID = Material.getMaterial(split[2]).getId();
								// TODO blockID = etc.getDataSource().getItem(split[2]);
							}
							if (!isValidBlockID(blockID)) {
								player.sendMessage(ChatColor.RED + split[2]
										+ " is not a valid block ID.");
								return true;
							}

							if (ball) {
								CuboidAction.buildShpere(playerName, radius,
										blockID, true);
								player.sendMessage(ChatColor.GREEN
										+ "The ball has been built");
							} else {
								CuboidAction.buildShpere(playerName, radius,
										blockID, false);
								player.sendMessage(ChatColor.GREEN
										+ "The sphere has been built");
							}

						} else {
							if (ball) {
								player.sendMessage(ChatColor.RED
										+ "Usage : /cball <radius> <block id|name>");
							} else {
								player.sendMessage(ChatColor.RED
										+ "Usage : /csphere <radius> <block id|name>");
							}
						}
					} else {
						player.sendMessage(ChatColor.RED
								+ "No point has been selected");
					}
					return true;
				}

				// /////////////////////////////////////
				// // CUBOIDS BUILDING COMMANDS ///
				// /////////////////////////////////////

				else if (split[0].equalsIgnoreCase("/cfaces")) {
					if (CuboidAction.isReady(playerName, true)) {
						int blockID = 4;
						if (split.length > 1) {
							try {
								blockID = Integer.parseInt(split[1]);
							} catch (NumberFormatException n) {
								blockID = Material.getMaterial(split[1]).getId();
								//blockID = etc.getDataSource().getItem(split[1]);
							}

							if (!isValidBlockID(blockID)) {
								player.sendMessage(ChatColor.RED + split[1]
										+ " is not a valid block ID.");
								return true;
							}

							CuboidAction.buildCuboidFaces(playerName, blockID,
									true);
							player.sendMessage(ChatColor.GREEN
									+ "The faces of the cuboid have been built");
						} else {
							player.sendMessage(ChatColor.RED
									+ "Usage : /cfaces <block id|name>");
						}
					} else {
						player.sendMessage(ChatColor.RED
								+ "No cuboid has been selected");
					}
					return true;
				}

				else if (split[0].equalsIgnoreCase("/cwalls")) {
					if (CuboidAction.isReady(playerName, true)) {
						int blockID = 4;
						if (split.length > 1) {
							try {
								blockID = Integer.parseInt(split[1]);
							} catch (NumberFormatException n) {
								blockID = Material.getMaterial(split[1]).getId();
								//blockID = etc.getDataSource().getItem(split[1]);
							}

							if (!isValidBlockID(blockID)) {
								player.sendMessage(ChatColor.RED + split[1]
										+ " is not a valid block ID.");
								return true;
							}

							CuboidAction.buildCuboidFaces(playerName, blockID,
									false);
							player.sendMessage(ChatColor.GREEN
									+ "The walls have been built");
						} else {
							player.sendMessage(ChatColor.RED
									+ "Usage : /cwalls <block id|name>");
						}
					} else {
						player.sendMessage(ChatColor.RED
								+ "No cuboid has been selected");
					}
					return true;
				}

				// /////////////////////////////////////
				// // OTHER BUILDING COMMANDS ////
				// /////////////////////////////////////

				else if (split[0].equalsIgnoreCase("/cpyramid")) {
					if (CuboidAction.isReady(playerName, false)) {
						int radius = 0;
						int blockID = 4;
						if (split.length > 2) {
							try {
								radius = Integer.parseInt(split[1]);
							} catch (NumberFormatException n) {
								player.sendMessage(ChatColor.RED + split[1]
										+ " is not a valid radius.");
								return true;
							}
							if (radius < 2) {
								player.sendMessage(ChatColor.RED
										+ "The radius has to be greater than 1");
								return true;
							}

							try {
								blockID = Integer.parseInt(split[2]);
							} catch (NumberFormatException n) {
								blockID = Material.getMaterial(split[2]).getId();
								//blockID = etc.getDataSource().getItem(split[2]);
							}
							if (!isValidBlockID(blockID)) {
								player.sendMessage(ChatColor.RED + split[2]
										+ " is not a valid block ID.");
								return true;
							}

							boolean filled = true;
							if (split.length == 4
									&& split[3].equalsIgnoreCase("empty")) {
								filled = false;
							}

							CuboidAction.buildPyramid(playerName, radius,
									blockID, filled);
							player.sendMessage(ChatColor.GREEN
									+ "The pyramid has been built");
						} else {
							player.sendMessage(ChatColor.RED
									+ "Usage : /cpyramid <radius> <block id|name>");
						}
					} else {
						player.sendMessage(ChatColor.RED
								+ "No point has been selected");
					}
					return true;
				}

				// ///////////////////////////////
				// // COPY/PASTE SYSTEM ////
				// ///////////////////////////////

				else if (split[0].equalsIgnoreCase("/undo")) {
					if (CuboidAction.isUndoAble(playerName)) {
						CuboidAction.undo(playerName);
						player.sendMessage(ChatColor.GREEN
								+ "Your last action has been undone !");
					} else {
						player.sendMessage(ChatColor.RED
								+ "Your last action is non-reversible.");
					}
					return true;
				}

				else if (split[0].equalsIgnoreCase("/ccopy")) {
					if (CuboidAction.isReady(playerName, true)) {
						CuboidAction.copyCuboid(playerName, true);
						player.sendMessage(ChatColor.GREEN
								+ "Selected cuboid is copied. Ready to paste !");
					} else {
						player.sendMessage(ChatColor.RED
								+ "No cuboid has been selected");
					}
					return true;
				}

				else if (split[0].equalsIgnoreCase("/cpaste")) {
					if (CuboidAction.isReady(playerName, false)) {
						byte returnCode = CuboidAction.paste(playerName);
						if (returnCode == 0) {
							player.sendMessage(ChatColor.GREEN
									+ "The cuboid has been placed.");
						} else if (returnCode == 1) {
							player.sendMessage(ChatColor.RED
									+ "Nothing to paste !");
						}
					} else {
						player.sendMessage(ChatColor.RED
								+ "No point has been selected");
					}
					return true;
				}

				// /////////////////////////////
				// // SAVE/LOAD SYSTEM ///
				// /////////////////////////////

				else if (split[0].equalsIgnoreCase("/csave")) {
					if (split.length > 1) {
						String cuboidName = split[1].toLowerCase();
						if (!cuboidExists(playerName, cuboidName)
								|| split.length == 3
								&& split[2].startsWith("over")) {
							if (CuboidAction.isReady(playerName, true)) {
								byte returnCode = CuboidAction.saveCuboid(
										playerName, cuboidName);
								if (returnCode == 0) {
									player.sendMessage(ChatColor.GREEN
											+ "Selected cuboid is saved with the name "
											+ cuboidName);
								} else if (returnCode == 1) {
									player.sendMessage(ChatColor.RED
											+ "Could not create the target folder.");
								} else if (returnCode == 2) {
									player.sendMessage(ChatColor.RED
											+ "Error while writing the file.");
								}
							} else {
								player.sendMessage(ChatColor.RED
										+ "No cuboid has been selected");
							}
						} else {
							player.sendMessage(ChatColor.RED
									+ "This cuboid name is already taken.");
						}
					} else {
						player.sendMessage(ChatColor.RED
								+ "Usage : /csave <cuboid name>");
					}
					return true;
				}

				else if (split[0].equalsIgnoreCase("/cload")) {
					if (split.length > 1) {
						String cuboidName = split[1].toLowerCase();
						if (cuboidExists(playerName, cuboidName)) {
							if (CuboidAction.isReady(playerName, false)) {
								byte returnCode = CuboidAction.loadCuboid(
										playerName, cuboidName);
								if (returnCode == 0) {
									player.sendMessage(ChatColor.GREEN
											+ "The cuboid has been loaded.");
								} else if (returnCode == 1) {
									player.sendMessage(ChatColor.RED
											+ "Could not find the file.");
								} else if (returnCode == 2) {
									player.sendMessage(ChatColor.RED
											+ "Reading error while accessing the file.");
								} else if (returnCode == 3) {
									player.sendMessage(ChatColor.RED
											+ "The file seems to be corrupted");
								}
							} else {
								player.sendMessage(ChatColor.RED
										+ "No point has been selected");
							}
						} else {
							player.sendMessage(ChatColor.RED
									+ "This cuboid does not exist.");
						}
					} else {
						player.sendMessage(ChatColor.RED
								+ "Usage : /cload <cuboid name>");
					}
					return true;
				}

				else if (split[0].equalsIgnoreCase("/clist")) {
					if (split.length == 1) {
						String list = listPersonalCuboids(playerName);
						if (list != null) {
							player.sendMessage(ChatColor.GREEN
									+ "Your saved cuboids :" + ChatColor.WHITE
									+ list);
						} else {
							player.sendMessage(ChatColor.RED
									+ "You have no saved cuboid");
						}
					} else if (split.length == 2 && player.isOp()) {
						String list = listPersonalCuboids(split[1]);
						if (list != null) {
							player.sendMessage(ChatColor.GREEN + split[1]
									+ "'s saved cuboids :" + ChatColor.WHITE
									+ list);
						} else {
							player.sendMessage(ChatColor.RED + split[1]
									+ " has no saved cuboid");
						}
					} else {
						if (player.isOp())
							player.sendMessage(ChatColor.RED
									+ "Usage : /clist <player name>");
						player.sendMessage(ChatColor.RED + "Usage : /clist");
					}
					return true;
				}

				else if (split[0].equalsIgnoreCase("/cremove")) {
					if (split.length > 1) {
						String cuboidName = split[1].toLowerCase();
						if (cuboidExists(playerName, cuboidName)) {
							File toDelete = new File("cuboids/" + playerName
									+ "/" + cuboidName + ".cuboid");
							if (toDelete.delete()) {
								player.sendMessage(ChatColor.GREEN
										+ "Cuboid sucessfuly deleted");
							} else {
								player.sendMessage(ChatColor.RED
										+ "Error while deleting the cuboid file");
							}
						} else {
							player.sendMessage(ChatColor.RED
									+ "This cuboid does not exist.");
						}
					} else {
						player.sendMessage(ChatColor.RED
								+ "Usage : /cremove <cuboid name>");
					}
					return true;
				}

				else if (split[0].equalsIgnoreCase("/cshare")) {
					if (split.length > 2) {
						String cuboidName = split[1].toLowerCase();
						String targetPlayerName = "";
						Player targetPlayer = etc.getServer().matchPlayer(split[2]);

						if (targetPlayer != null) {
							targetPlayerName = targetPlayer.getName();
						} else {
							player.sendMessage(ChatColor.RED + "Player "
									+ split[2] + " seems to be offline");
							return true;
						}

						if (cuboidExists(playerName, cuboidName)) {
							if (!cuboidExists(targetPlayerName, cuboidName)) {

								File ownerFolder = new File("cuboids/"
										+ targetPlayerName);
								try {
									if (!ownerFolder.exists()) {
										ownerFolder.mkdir();
									}
								} catch (Exception e) {
									player.sendMessage(ChatColor.RED
											+ "Error while creating targer folder");
									return true;
								}

								if (CuboidContent.copyFile(new File("cuboids/"
										+ playerName + "/" + cuboidName
										+ ".cuboid"), new File("cuboids/"
										+ targetPlayerName + "/" + cuboidName
										+ ".cuboid"))) {
									player.sendMessage(ChatColor.GREEN
											+ "You shared " + cuboidName
											+ " with " + targetPlayerName);
									for (Player p : etc.getServer().getPlayerList()) {
										if (p.getName()
												.equals(targetPlayerName)) {
											p.sendMessage(ChatColor.GREEN
													+ playerName + " shared "
													+ cuboidName
													+ ".cuboid with you");
										}
									}
								} else {
									player.sendMessage(ChatColor.RED
											+ "Error while copying the the cuboid file");
								}
							} else {
								player.sendMessage(ChatColor.RED
										+ targetPlayerName
										+ " already has a cuboid named "
										+ cuboidName);
							}
						} else {
							player.sendMessage(ChatColor.RED
									+ "This cuboid does not exist.");
						}
					} else {
						player.sendMessage(ChatColor.RED
								+ "Usage : /cshare <cuboid name> <player name>");
					}
					return true;
				}

				else if (split[0].equalsIgnoreCase("/#stop")) {
					onPluginStop();
					return false;
				}

			}
			return false;
		}

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

		
	} // end cuboidListener

	public class WriteJob extends TimerTask {
		public void run() {
			CuboidAreas.writeCuboidAreas();
			writeTimer.schedule(new WriteJob(), writeDelay);
		}
	}

}