package se.jeremy.minecraft.cuboid;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.UUID;
import java.util.logging.Level;

import se.jeremy.minecraft.cuboid.Cuboid;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;


public class CuboidAreas {
	static ArrayList<CuboidC> listOfCuboids;
	static HashMap<String, ArrayList<CuboidC>> inside;

	static Timer healTimer = new Timer();
	public static int healPower = 0;
	static long healDelay = 1000;

	static String currentDataVersion = "C";
	static int addedHeight = 0;
	static boolean newestHavePriority = true;

	@SuppressWarnings("unchecked")
	public static void loadCuboidAreas() {
		listOfCuboids = new ArrayList<CuboidC>();
		
		try {
			ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(Cuboid.data, "cuboidAreas.dat"))));
			listOfCuboids = (ArrayList<CuboidC>) (ois.readObject());
			ois.close();
			Cuboid.log(Level.INFO, "CuboidPlugin: cuboidAreas.dat loaded");
		} catch (Exception e) {
			Cuboid.log(Level.SEVERE, "Cuboid plugin : Error while reading cuboidAreas.dat");
		}
	}

	public static void writeCuboidAreas() {
		Cuboid.log(Level.INFO, "CuboidPlugin : Saving data to hard drive...");
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(new File(Cuboid.data, "cuboidAreas.dat"))));
			oos.writeObject(listOfCuboids);
			oos.close();
		} catch (IOException e1) {
			Cuboid.log(Level.SEVERE, "CuboidPlugin : Error while writing data");
			Cuboid.log(Level.SEVERE, e1.getMessage());
		}
		Cuboid.log(Level.INFO, "CuboidPlugin : Done.");
	}

	// //////////////////////////
	// // DATA SENDING ////
	// //////////////////////////

	public static CuboidC findCuboidArea(Location loc) {
		
		String world = loc.getWorld().getName();
		int X = loc.getBlockX();
		int Y = loc.getBlockY(); 
		int Z = loc.getBlockZ();
		
		CuboidC lastEntry = null;
		for (CuboidC cuboid : listOfCuboids) {
			if (cuboid.contains(world, X, Y, Z)) {
				if (newestHavePriority) {
					lastEntry = cuboid;
				} else {
					return cuboid;
				}
			}
		}
		return lastEntry;
	}

	public static CuboidC findCuboidArea(String cuboidName) {
		CuboidC lastEntry = null;
		for (CuboidC cuboid : listOfCuboids) {
			if (cuboid.name.equalsIgnoreCase(cuboidName)) {
				if (newestHavePriority) {
					lastEntry = cuboid;
				} else {
					return cuboid;
				}
			}
		}
		return lastEntry;
	}

	public static void movement(Player player, Location loc) {
		if (!inside.containsKey(player.getName()))
			inside.put(player.getName(), new ArrayList<CuboidC>());
		ArrayList<CuboidC> presence = inside.get(player.getName());
		for (int i = 0; i < listOfCuboids.size(); i++) {
			CuboidC cuboid = listOfCuboids.get(i);
			if (cuboid.contains(loc) && !presence.contains(cuboid)) {
				cuboid.playerEnters(player);
				if (cuboid.heal && healPower > 0 && player.getHealth() > 0) {
					healTimer
							.schedule(
									new CuboidHealJob(player.getName(),
											cuboid), healDelay);
				}
				presence.add(cuboid);
			} else if (!cuboid.contains(loc) && presence.contains(cuboid)) {
				cuboid.playerLeaves(player);
				presence.remove(cuboid);
			}
		}
	}

	public static void leaveAll(Player player) {
		if (!inside.containsKey(player.getName()))
			return;
		ArrayList<CuboidC> presence = inside.get(player.getName());
		for (int i = listOfCuboids.size() - 1; i >= 0; i--) {
			CuboidC cuboid = listOfCuboids.get(i);
			if (presence.contains(cuboid)) {
				cuboid.playerLeaves(player);
			}
		}
		inside.remove(player.getName());
	}

	public static String displayCuboidsList() {
		if (listOfCuboids.size() == 0) {
			return "<list is empty>";
		}

		String list = "";
		for (CuboidC cuboid : listOfCuboids) {
			list += " " + cuboid.name;
		}
		return list.trim();
	}

	public static void displayOwnedList(Player player) {
		String list = "";
		for (CuboidC cuboid : listOfCuboids) {
			if (cuboid.isOwner(player))
				list += " " + cuboid.name;
		}
		if (list.equalsIgnoreCase(""))
			player.sendMessage(ChatColor.YELLOW + "Areas you own : "
					+ ChatColor.WHITE + "<list is empty>");
		else
			player.sendMessage(ChatColor.YELLOW + "Areas you own :"
					+ ChatColor.WHITE + list);
	}

	// ////////////////////////////
	// // DATA TREATMENT ////
	// ////////////////////////////

	public static boolean createCuboidArea(Player player, String cuboidName) {
		UUID playerId = player.getUniqueId();
		
		if (findCuboidArea(cuboidName) != null) {
			player.sendMessage(ChatColor.RED
					+ "There is already an area with that name");
			if (Cuboid.logging)
				Cuboid.log(
						Level.INFO,
						playerId + " failed to create a cuboid area named "
								+ cuboidName + " (aleady used)");
			return false;
		}
		int[] firstPoint = CuboidAction.getPoint(playerId, false);
		int[] secondPoint = CuboidAction.getPoint(playerId, true);

		CuboidC newCuboid = new CuboidC();
		for (short i = 0; i < 3; i++)
			newCuboid.coords[i] = firstPoint[i];
		for (short i = 0; i < 3; i++)
			newCuboid.coords[i + 3] = secondPoint[i];
		newCuboid.allowedPlayers.add("o:" + playerId);
		newCuboid.name = cuboidName;
		newCuboid.world = player.getWorld();
		if (Cuboid.protectionOnDefault) {
			newCuboid.protection = true;
		}
		if (Cuboid.restrictedOnDefault) {
			newCuboid.restricted = true;
		}
		if (Cuboid.sanctuaryOnDefault) {
			newCuboid.sanctuary = true;
		}
		if (Cuboid.creeperDisabledOnDefault) {
			newCuboid.creeper = false;
		}
		if (Cuboid.pvpDisabledOnDefault) {
			newCuboid.PvP = false;
		}
		if (Cuboid.healOnDefault) {
			newCuboid.heal = true;
		}
		listOfCuboids.add(newCuboid);

		player.sendMessage(ChatColor.GREEN + "Cuboid area successfuly created");
		if (Cuboid.logging)
			Cuboid.log(
					Level.INFO,
					playerId + " created a new cuboid area named "
							+ cuboidName);
		return true;
	}

	public static boolean protectCuboidArea(Player player,
			ArrayList<String> ownersList, String cuboidName, boolean highProtect) {
		UUID playerId = player.getUniqueId();
		if (findCuboidArea(cuboidName) != null) {
			player.sendMessage(ChatColor.RED
					+ "There is already an area with that name");
			if (Cuboid.logging)
				Cuboid.log(
						Level.INFO,
						playerId
								+ " failed to create a protected area named "
								+ cuboidName + " (aleady used)");
			return false;
		}

		// Getting the corners' coordinates and correcting them if necessary
		int[] firstPoint = CuboidAction.getPoint(playerId, false);
		int[] secondPoint = CuboidAction.getPoint(playerId, true);
		if (highProtect) {
			firstPoint[1] = 0;
			// increased height for newest mc
			secondPoint[1] = 255;
		} else if (firstPoint[1] == secondPoint[1]) {
			firstPoint[1] -= addedHeight;
			secondPoint[1] += addedHeight;
		}

		CuboidC newCuboid = new CuboidC();
		for (short i = 0; i < 3; i++)
			newCuboid.coords[i] = firstPoint[i];
		for (short i = 0; i < 3; i++)
			newCuboid.coords[i + 3] = secondPoint[i];
		newCuboid.allowedPlayers = ownersList;
		newCuboid.name = cuboidName;
		newCuboid.world = player.getWorld();
		newCuboid.protection = true;

		listOfCuboids.add(newCuboid);
		/*CuboidAction.updateChestsState(playerName, firstPoint[0],
				firstPoint[1], firstPoint[2], secondPoint[0], secondPoint[1],
				secondPoint[2]);*/

		player.sendMessage(ChatColor.GREEN
				+ "Protected area successfuly created");
		if (Cuboid.logging)
			Cuboid.log(
					Level.INFO,
					playerId + " created a new protected area named "
							+ cuboidName);

		return true;
	}

	public static void moveCuboidArea(Player player, CuboidC cuboid) {
		UUID playerId = player.getUniqueId();
		int[] firstPoint = CuboidAction.getPoint(playerId, false);
		int[] secondPoint = CuboidAction.getPoint(playerId, true);

		/*if (cuboid.protection)
			CuboidAction.updateChestsState(playerName, cuboid.coords[0],
					cuboid.coords[1], cuboid.coords[2], cuboid.coords[3],
					cuboid.coords[4], cuboid.coords[5]);*/

		cuboid.coords[0] = firstPoint[0];
		cuboid.coords[1] = firstPoint[1];
		cuboid.coords[2] = firstPoint[2];
		cuboid.coords[3] = secondPoint[0];
		cuboid.coords[4] = secondPoint[1];
		cuboid.coords[5] = secondPoint[2];

		/*if (cuboid.protection)
			CuboidAction.updateChestsState(playerName, firstPoint[0],
					firstPoint[1], firstPoint[2], secondPoint[0],
					secondPoint[1], secondPoint[2]);*/

		byte returnCode = new CuboidBackup(cuboid, true).writeToDisk();
		player.sendMessage(ChatColor.GREEN + "return code of backup : "
				+ returnCode);

		player.sendMessage(ChatColor.GREEN + "Cuboid area successfuly moved");
		if (Cuboid.logging)
			Cuboid.log(Level.INFO,
					playerId + " moved a cuboid area named " + cuboid.name);
	}

	public static void removeCuboidArea(Player player, CuboidC cuboid) {
		String playerName = player.getName();
		listOfCuboids.remove(cuboid);
		/*if (cuboid.protection)
			CuboidAction.updateChestsState(playerName, cuboid.coords[0],
					cuboid.coords[1], cuboid.coords[2], cuboid.coords[3],
					cuboid.coords[4], cuboid.coords[5]);*/

		if (new CuboidBackup(cuboid, false).deleteFromDisc()) {
			player.sendMessage(ChatColor.GREEN
					+ "Cuboid area successfuly removed");
			if (Cuboid.logging)
				Cuboid.log(
						Level.INFO,
						playerName + " removed a cuboid area named "
								+ cuboid.name);
		} else {
			player.sendMessage(ChatColor.YELLOW
					+ "Cuboid area removed, but unable to remove the backup file.");
			Cuboid.log(
					Level.INFO,
					"Unable to delete a backup file when removing a cuboid area ("
							+ cuboid.name + ")");
		}
	}

	public static void treatAllowance(Player player, String[] list,
			CuboidC cuboid) {
		for (String data : list) {
			if (data.charAt(0) == '/')
				cuboid.allowCommand(data);
			else
				cuboid.allowPlayer(data);
		}
		player.sendMessage(ChatColor.GREEN + "Area's whitelist updated");
	}

	public static void treatDisallowance(Player player, String[] list,
			CuboidC cuboid) {
		for (String data : list) {
			if (data.charAt(0) == '/')
				cuboid.disallowCommand(data);
			else
				cuboid.disallowPlayer(data);
		}
		player.sendMessage(ChatColor.GREEN + "Area's whitelist updated");
	}
}
