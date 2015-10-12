package se.jeremy.minecraft.cuboid;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/*
 * Here happens all the raw 'treatment' of selected areas, such as filling, empty-ing, replacing etc...
 */

public class CuboidAction {
	static HashMap<UUID, CuboidSelection> playerSelection = new HashMap<UUID, CuboidSelection>();
	static Object lock = new Object();
	//static Material[] blocksToBeQueued = { 37, 38, 39, 40, 50, 55, 63, 66, 69, 75, 76, 81, 83 };
	static Material[] blocksToBeQueued = {};
	public static Cuboid plugin;

	public CuboidAction(Cuboid instance) {
		CuboidAction.plugin = instance;
	}

	public static CuboidSelection getPlayerSelection(UUID playerId) {
		if (!playerSelection.containsKey(playerId)) {
			playerSelection.put(playerId, new CuboidSelection());
		}
		return playerSelection.get(playerId);
	}

	public static boolean setPoint(UUID playerId, int X, int Y, int Z) {
		return getPlayerSelection(playerId).selectCorner(X, Y, Z);
	}

	public static void setBothPoint(UUID playerId, int[] coords) {
		CuboidSelection selection = getPlayerSelection(playerId);
		selection.firstCorner[0] = coords[0];
		selection.firstCorner[1] = coords[1];
		selection.firstCorner[2] = coords[2];
		selection.secondCorner[0] = coords[3];
		selection.secondCorner[1] = coords[4];
		selection.secondCorner[2] = coords[5];
		selection.status = false;
	}

	public static int[] getPoint(UUID playerId, boolean secondPoint) {
		if (secondPoint) {
			return getPlayerSelection(playerId).secondCorner;
		} else {
			return getPlayerSelection(playerId).firstCorner;
		}
	}

	public static boolean isUndoAble(UUID playerId) {
		return getPlayerSelection(playerId).undoable;
	}

	public static boolean isReady(UUID playerId, boolean deuxPoints) {
		CuboidSelection selection = getPlayerSelection(playerId);
		
		if (deuxPoints && !selection.status && selection.firstCorner != null) {
			return true;
		} else if (!deuxPoints && selection.status) {
			return true;
		}
		
		return false;
	}

	public static void copyCuboid(UUID playerId, boolean manual) {
		copyCuboid(playerId, getPlayerSelection(playerId), manual);
	}

	private static void copyCuboid(UUID playerId, CuboidSelection selection, boolean manual) {
		copyCuboid(playerId, selection, selection.firstCorner[0], selection.secondCorner[0], selection.firstCorner[1], selection.secondCorner[1], selection.firstCorner[2], selection.secondCorner[2]);

		if (!manual) {
			selection.undoable = true;
		}
	}

	private static void copyCuboid(UUID playerId, CuboidSelection selection, int Xmin, int Xmax, int Ymin, int Ymax, int Zmin, int Zmax) {
		World world = Bukkit.getServer().getPlayer(playerId).getWorld();
		int Xsize = Xmax - Xmin + 1;
		int Ysize = Ymax - Ymin + 1;
		int Zsize = Zmax - Zmin + 1;

		Material[][][] tableaux = new Material[Xsize][][];
		for (int i = 0; i < Xsize; i++) {
			tableaux[i] = new Material[Ysize][];
			for (int j = 0; j < Ysize; ++j) {
				tableaux[i][j] = new Material[Zsize];
				for (int k = 0; k < Zsize; ++k) {
					tableaux[i][j][k] = world.getBlockAt(Xmin + i, Ymin + j, Zmin + k).getType();
				}
			}
		}

		selection.lastCopiedCuboid = tableaux;
		selection.pastePoint = new int[] { Xmin, Ymin, Zmin };
		selection.undoable = true;
	}

	private static boolean shoudBeQueued(Material lastCopiedCuboid) {
		for (Material shoudBeQueued : blocksToBeQueued) {
			if (lastCopiedCuboid == shoudBeQueued)
				return true;
		}
		return false;
	}

	public static byte paste(UUID playerId) {
		// Paste will occur from North-East to South-West

		CuboidSelection selection = getPlayerSelection(playerId);
		World world = Bukkit.getServer().getPlayer(playerId).getWorld();

		int Xsize = selection.lastCopiedCuboid.length;
		if (Xsize == 0) {
			return 1;
		}
		int Ysize = selection.lastCopiedCuboid[0].length;
		int Zsize = selection.lastCopiedCuboid[0][0].length;

		selection.lastSelectedCuboid = new Material[Xsize][][];

		int curX, curY, curZ;
		HashMap<int[], Material> queuedBlocks = new HashMap<int[], Material>();

		synchronized (lock) {
			for (int i = 0; i < Xsize; i++) {
				selection.lastSelectedCuboid[i] = new Material[Ysize][];
				
				for (int j = 0; j < Ysize; ++j) {
					selection.lastSelectedCuboid[i][j] = new Material[Zsize];
					
					for (int k = 0; k < Zsize; ++k) {
						curX = selection.pastePoint[0] + i;
						curY = selection.pastePoint[1] + j;
						curZ = selection.pastePoint[2] + k;
						selection.lastSelectedCuboid[i][j][k] = world.getBlockAt(curX, curY, curZ).getType();
						
						if (shoudBeQueued(selection.lastCopiedCuboid[i][j][k])) {
							queuedBlocks.put(new int[] { curX, curY, curZ }, selection.lastCopiedCuboid[i][j][k]);
						} else {
							world.getBlockAt(curX, curY,curZ).setType(selection.lastCopiedCuboid[i][j][k]);
						}
					}
				}
			}

			for (Entry<int[], Material> queuedBlock : queuedBlocks.entrySet()) {
				world.getBlockAt(queuedBlock.getKey()[0], queuedBlock.getKey()[1], queuedBlock.getKey()[2]).setType(queuedBlock.getValue());
			}
		}

		selection.undoable = true;
		return 0;
	}

	public static byte undo(UUID playerId) {
		CuboidSelection selection = getPlayerSelection(playerId);
		World world = Bukkit.getServer().getPlayer(playerId).getWorld();

		Material[][][] toPaste;
		if (selection.lastSelectedCuboid != null) {
			toPaste = selection.lastSelectedCuboid;
		} else {
			toPaste = selection.lastCopiedCuboid;
		}

		int Xsize = toPaste.length;
		if (Xsize == 0) {
			return 1;
		}
		int Ysize = toPaste[0].length;
		int Zsize = toPaste[0][0].length;

		synchronized (lock) {
			for (int i = 0; i < Xsize; i++) {
				for (int j = 0; j < Ysize; ++j) {
					for (int k = 0; k < Zsize; ++k) {
						world.getBlockAt(selection.pastePoint[0] + i,selection.pastePoint[1] + j,selection.pastePoint[2] + k).setType(toPaste[i][j][k]);
					}
				}
			}
		}

		selection.lastSelectedCuboid = null;
		selection.undoable = false;

		return 0;
	}

	public static byte saveCuboid(UUID playerId, String cuboidName) {
		CuboidSelection selection = getPlayerSelection(playerId);
		
		World world = Bukkit.getPlayer(playerId).getWorld();
		int Xsize = selection.secondCorner[0] - selection.firstCorner[0] + 1;
		int Ysize = selection.secondCorner[1] - selection.firstCorner[1] + 1;
		int Zsize = selection.secondCorner[2] - selection.firstCorner[2] + 1;

		Material[][][] tableaux = new Material[Xsize][][];
		for (int i = 0; i < Xsize; i++) {
			tableaux[i] = new Material[Ysize][];
			
			for (int j = 0; j < Ysize; ++j) {
				tableaux[i][j] = new Material[Zsize];
				
				for (int k = 0; k < Zsize; ++k) {
					tableaux[i][j][k] = world.getBlockAt(selection.firstCorner[0] + i, selection.firstCorner[1] + j, selection.firstCorner[2] + k).getType();
				}
			}
		}

		return new CuboidContent(plugin, playerId, cuboidName, tableaux).save();
	}

	public static byte loadCuboid(UUID playerId, String cuboidName) {
		CuboidSelection selection = getPlayerSelection(playerId);
		CuboidContent data = new CuboidContent(playerId, cuboidName);
		World world = Bukkit.getPlayer(playerId).getWorld();

		if (data.loadReturnCode == 0) {

			Material[][][] tableau = data.getData();
			int Xsize = tableau.length;
			int Ysize = tableau[0].length;
			int Zsize = tableau[0][0].length;

			copyCuboid(playerId, selection, selection.firstCorner[0],
					selection.firstCorner[0] + Xsize, selection.firstCorner[1],
					selection.firstCorner[1] + Ysize, selection.firstCorner[2],
					selection.firstCorner[2] + Zsize);

			synchronized (lock) {
				for (int i = 0; i < Xsize; i++) {
					for (int j = 0; j < Ysize; ++j) {
						for (int k = 0; k < Zsize; ++k) {
							world.getBlockAt(selection.firstCorner[0] + i, selection.firstCorner[1] + j, selection.firstCorner[2] + k).setType(tableau[i][j][k]);
						}
					}
				}
			}
		}

		return data.loadReturnCode;
	}

	public static int blocksCount(UUID playerId) {
		CuboidSelection selection = getPlayerSelection(playerId);
		int Xsize = selection.secondCorner[0] - selection.firstCorner[0] + 1;
		int Ysize = selection.secondCorner[1] - selection.firstCorner[1] + 1;
		int Zsize = selection.secondCorner[2] - selection.firstCorner[2] + 1;
		return Xsize * Ysize * Zsize;
	}

	public static void emptyCuboid(UUID playerId) {
		CuboidSelection selection = getPlayerSelection(playerId);
		copyCuboid(playerId, selection, false);
		
		World world = Bukkit.getPlayer(playerId).getWorld();

		synchronized (lock) {
			for (int i = selection.firstCorner[0]; i <= selection.secondCorner[0]; i++) {
				for (int j = selection.firstCorner[1]; j <= selection.secondCorner[1]; j++) {
					for (int k = selection.firstCorner[2]; k <= selection.secondCorner[2]; k++) {
						world.getBlockAt(i, j, k).setType(Material.AIR);
					}
				}
			}
		}

		if (Cuboid.logging)
			Bukkit.getLogger().log(Level.INFO, Bukkit.getPlayer(playerId).getDisplayName() + " emptied a cuboid");
	}

	public static void fillCuboid(UUID playerId, Material blockType) {
		CuboidSelection selection = getPlayerSelection(playerId);
		copyCuboid(playerId, selection, false);
		
		World world = Bukkit.getPlayer(playerId).getWorld();

		synchronized (lock) {
			for (int i = selection.firstCorner[0]; i <= selection.secondCorner[0]; i++) {
				for (int j = selection.firstCorner[1]; j <= selection.secondCorner[1]; j++) {
					for (int k = selection.firstCorner[2]; k <= selection.secondCorner[2]; k++) {
						world.getBlockAt(i, j, k).setType(blockType);
					}
				}
			}
		}

		if (Cuboid.logging)
			plugin.getLogger().log(Level.INFO, Bukkit.getPlayer(playerId).getDisplayName() + " filled a cuboid");
	}

	public static void replaceBlocks(UUID playerId, Material[] replaceParams) {
		CuboidSelection selection = getPlayerSelection(playerId);
		copyCuboid(playerId, selection, false);
		
		World world = Bukkit.getPlayer(playerId).getWorld();

		synchronized (lock) {
			int targetBlockIndex = replaceParams.length - 1;
			for (int i = selection.firstCorner[0]; i <= selection.secondCorner[0]; i++) {
				for (int j = selection.firstCorner[1]; j <= selection.secondCorner[1]; j++) {
					for (int k = selection.firstCorner[2]; k <= selection.secondCorner[2]; k++) {
						for (int l = 0; l < targetBlockIndex; l++) {
							if (world.getBlockAt(i, j, k).getType() == replaceParams[l]) {
								world.getBlockAt(i, j, k).setType(replaceParams[targetBlockIndex]);
							}
						}
					}
				}
			}
			if (Cuboid.logging)
				Bukkit.getLogger().log(Level.INFO, Bukkit.getPlayer(playerId).getDisplayName() + " replaced blocks inside a cuboid");
		}
	}

	public static void buildCuboidFaces(UUID playerId, Material blockType, boolean sixFaces) {
		CuboidSelection selection = getPlayerSelection(playerId);
		copyCuboid(playerId, selection, false);
		
		Server server = Bukkit.getServer();
		World world = server.getPlayer(playerId).getWorld();

		synchronized (lock) {
			for (int i = selection.firstCorner[0]; i <= selection.secondCorner[0]; i++) {
				for (int j = selection.firstCorner[1]; j <= selection.secondCorner[1]; j++) {
					Location firstCorner = new Location(world, i, j, selection.firstCorner[2]);
					Location secondCorner = new Location(world, i, j, selection.secondCorner[2]);
					
					firstCorner.getBlock().setType(blockType);
					secondCorner.getBlock().setType(blockType);
				}
			}
			for (int i = selection.firstCorner[1]; i <= selection.secondCorner[1]; i++) {
				for (int j = selection.firstCorner[2]; j <= selection.secondCorner[2]; j++) {
					Location firstCorner = new Location(world, selection.firstCorner[0], i, j);
					Location secondCorner = new Location(world, selection.secondCorner[0], i, j);
					
					firstCorner.getBlock().setType(blockType);
					secondCorner.getBlock().setType(blockType);
				}
			}
			if (sixFaces) {
				for (int i = selection.firstCorner[0]; i <= selection.secondCorner[0]; i++) {
					for (int j = selection.firstCorner[2]; j <= selection.secondCorner[2]; j++) {
						Location firstCorner = new Location(world, i, selection.firstCorner[1], j);
						Location secondCorner = new Location(world, i, selection.secondCorner[1], j);
						
						firstCorner.getBlock().setType(blockType);
						secondCorner.getBlock().setType(blockType);
					}
				}
			}
			
			if (Cuboid.logging) {
				plugin.getLogger().log( Level.INFO, Bukkit.getPlayer(playerId).getDisplayName() + " built the " + ((sixFaces) ? "faces" : "walls") + " of a cuboid");
			}
		}
	}

	public static void rotateCuboidContent(UUID playerId, int rotationType) {
		CuboidSelection selection = getPlayerSelection(playerId);
		copyCuboid(playerId, selection, false);

		synchronized (lock) {
			if (rotationType == 0) { // 90, clockwise
			}
			if (rotationType == 1) { // 90, counter-clockwise

			}
			if (rotationType == 2) { // 180

			}
			if (rotationType == 3) { // upside-down

			}
		}

		if (Cuboid.logging)
			plugin.getLogger().log(Level.INFO, Bukkit.getPlayer(playerId).getDisplayName() + " rotated a cuboid.");
	}

	public static void moveCuboidContent(Player player, String movementType, int value) {
		UUID playerId = player.getUniqueId();
		CuboidSelection selection = getPlayerSelection(playerId);
		copyCuboid(playerId, selection, false);
		World world = player.getWorld();

		synchronized (lock) {
			if (movementType.equalsIgnoreCase("East")) {
				copyCuboid(playerId, selection, selection.firstCorner[0], selection.secondCorner[0], selection.firstCorner[1], selection.secondCorner[1], selection.firstCorner[2] - value, selection.secondCorner[2]);
				
				int deleteIterator = 0;
				
				for (int k = selection.secondCorner[2]; k >= selection.firstCorner[2]; k--) {
					for (int i = selection.firstCorner[0]; i <= selection.secondCorner[0]; i++) {
						for (int j = selection.firstCorner[1]; j <= selection.secondCorner[1]; j++) {
							Block b = world.getBlockAt(i, j, k - value); 
							Material m = selection.lastCopiedCuboid[i - selection.firstCorner[0]][j - selection.firstCorner[1]][k - selection.firstCorner[2]+ 1];
							b.setType(m);

							if (deleteIterator < value) {
								world.getBlockAt(i, j, k).setType(Material.AIR);
							}
						}
					}
					deleteIterator++;
				}
				
				selection.firstCorner[2] -= value;
				selection.secondCorner[2] -= value;
			} else if (movementType.equalsIgnoreCase("North")) {
				copyCuboid(playerId, selection, selection.firstCorner[0] - value, selection.secondCorner[0], selection.firstCorner[1], selection.secondCorner[1], selection.firstCorner[2], selection.secondCorner[2]);
				int deleteIterator = 0;
				
				for (int i = selection.secondCorner[0]; i >= selection.firstCorner[0]; i--) {
					for (int j = selection.firstCorner[1]; j <= selection.secondCorner[1]; j++) {
						for (int k = selection.firstCorner[2]; k <= selection.secondCorner[2]; k++) {
							Block b = world.getBlockAt(i - value, j, k);
							Material m = selection.lastCopiedCuboid[i - selection.firstCorner[0] + 1][j - selection.firstCorner[1]][k - selection.firstCorner[2]];
							b.setType(m);
							
							if (deleteIterator < value) {
								world.getBlockAt(i, j, k).setType(Material.AIR);
							}
						}
					}
					deleteIterator++;
				}
				selection.firstCorner[0] -= value;
				selection.secondCorner[0] -= value;
			} else if (movementType.equalsIgnoreCase("South")) {
				copyCuboid(playerId, selection, selection.firstCorner[0],
						selection.secondCorner[0] + value,
						selection.firstCorner[1], selection.secondCorner[1],
						selection.firstCorner[2], selection.secondCorner[2]);
				int deleteIterator = 0;
				for (int i = selection.firstCorner[0]; i <= selection.secondCorner[0]; i++) {
					for (int j = selection.firstCorner[1]; j <= selection.secondCorner[1]; j++) {
						for (int k = selection.firstCorner[2]; k <= selection.secondCorner[2]; k++) {
							Block b = world.getBlockAt(i + value, j, k);
							Material m = selection.lastCopiedCuboid[i - selection.firstCorner[0]][j - selection.firstCorner[1]][k - selection.firstCorner[2]];
							b.setType(m);

							if (deleteIterator < value) {
								world.getBlockAt(i, j, k).setType(Material.AIR);
							}
						}
					}
					deleteIterator++;
				}
				selection.firstCorner[0] += value;
				selection.secondCorner[0] += value;
			} else if (movementType.equalsIgnoreCase("West")) {
				copyCuboid(playerId, selection, selection.firstCorner[0],
						selection.secondCorner[0], selection.firstCorner[1],
						selection.secondCorner[1], selection.firstCorner[2],
						selection.secondCorner[2] + value);
				int deleteIterator = 0;
				for (int k = selection.firstCorner[2]; k <= selection.secondCorner[2]; k++) {
					for (int i = selection.firstCorner[0]; i <= selection.secondCorner[0]; i++) {
						for (int j = selection.firstCorner[1]; j <= selection.secondCorner[1]; j++) {
							Block b = world.getBlockAt(i, j, k + value);
							Material m = selection.lastCopiedCuboid[i - selection.firstCorner[0]][j - selection.firstCorner[1]][k - selection.firstCorner[2]];
							b.setType(m);

							if (deleteIterator < value) {
								world.getBlockAt(i, j, k).setType(Material.AIR);
							}
						}
					}
					deleteIterator++;
				}
				selection.firstCorner[2] += value;
				selection.secondCorner[2] += value;
			} else if (movementType.equalsIgnoreCase("Up")) {
				copyCuboid(playerId, selection, selection.firstCorner[0],
						selection.secondCorner[0], selection.firstCorner[1],
						selection.secondCorner[1] + value,
						selection.firstCorner[2], selection.secondCorner[2]);
				int deleteIterator = 0;
				for (int j = selection.firstCorner[1]; j <= selection.secondCorner[1]; j++) {
					for (int i = selection.firstCorner[0]; i <= selection.secondCorner[0]; i++) {
						for (int k = selection.firstCorner[2]; k <= selection.secondCorner[2]; k++) {
							Block b = world.getBlockAt(i, j + value, k);
							Material m = selection.lastCopiedCuboid[i - selection.firstCorner[0]][j - selection.firstCorner[1]][k - selection.firstCorner[2]];
							b.setType(m);

							if (deleteIterator < value) {
								world.getBlockAt(i, j, k).setType(Material.AIR);
							}
						}
					}
					deleteIterator++;
				}
				selection.firstCorner[1] += value;
				selection.secondCorner[1] += value;
			} else if (movementType.equalsIgnoreCase("Down")) {
				copyCuboid(playerId, selection, selection.firstCorner[0],
						selection.secondCorner[0], selection.firstCorner[1]
								- value, selection.secondCorner[1],
						selection.firstCorner[2], selection.secondCorner[2]);
				int deleteIterator = 0;
				for (int j = selection.secondCorner[1]; j >= selection.firstCorner[1]; j--) {
					for (int i = selection.firstCorner[0]; i <= selection.secondCorner[0]; i++) {
						for (int k = selection.firstCorner[2]; k <= selection.secondCorner[2]; k++) {
							Block b = world.getBlockAt(i, j - value, k);
							Material m = selection.lastCopiedCuboid[i - selection.firstCorner[0]][j - selection.firstCorner[1] + 1][k - selection.firstCorner[2]];
							b.setType(m);

							if (deleteIterator < value) {
								world.getBlockAt(i, j, k).setType(Material.AIR);
							}
						}
					}
					deleteIterator++;
				}
				selection.firstCorner[1] -= value;
				selection.secondCorner[1] -= value;
			} else {
				player.sendMessage(ChatColor.RED + "Wrong parameter : "
						+ movementType);
				return;
			}

			player.sendMessage(ChatColor.GREEN + "Cuboid successfuly moved.");
			if (Cuboid.logging)
				plugin.getLogger().log(
						Level.INFO,
						playerId + " moved a cuboid : " + value
								+ " block(s) " + movementType);
		}
	}

	public static void buildCircle(UUID playerId, int radius, Material blockType, int height, boolean fill) {
		CuboidSelection selection = getPlayerSelection(playerId);

		int Xcenter = selection.firstCorner[0];
		int Ycenter = selection.firstCorner[1];
		int Zcenter = selection.firstCorner[2];
		int Xmin = Xcenter - radius;
		int Xmax = Xcenter + radius;
		int Zmin = Zcenter - radius;
		int Zmax = Zcenter + radius;
		int Ymin = (height + Ycenter >= Ycenter) ? Ycenter : height + Ycenter;
		int Ymax = (height + Ycenter <= Ycenter) ? Ycenter : height + Ycenter;

		copyCuboid(playerId, selection, Xmin, Xmax, Ymin, Ymax, Zmin, Zmax);

		synchronized (lock) {
			for (int i = Xmin; i <= Xmax; i++) {
				for (int j = Ymin; j <= Ymax; j++) {
					for (int k = Zmin; k <= Zmax; k++) {
						double diff = Math.sqrt(Math.pow(i - Xcenter, 2.0D)
								+ Math.pow(k - Zcenter, 2.0D));
						if (diff < radius + 0.5
								&& (fill || (!fill && diff > radius - 0.5))) {
							Bukkit.getServer()
									.getPlayer(playerId)
									.getWorld()
									.getBlockAt(
											new Location(Bukkit.getServer()
													.getPlayer(playerId)
													.getWorld(), i, j, k))
									.setType(blockType);
						}
					}
				}
			}
		}

		if (Cuboid.logging)
			plugin.getLogger().log(
					Level.INFO,
					playerId + " built a "
							+ ((height != 0) ? "cylinder" : "circle"));
	}

	public static void buildShpere(UUID playerId, int radius, int blocktype, boolean fill) {
		CuboidSelection selection = getPlayerSelection(playerId);

		int Xcenter = selection.firstCorner[0];
		int Ycenter = selection.firstCorner[1];
		int Zcenter = selection.firstCorner[2];
		int Xmin = Xcenter - radius;
		int Xmax = Xcenter + radius;
		int Ymin = Ycenter - radius;
		int Ymax = Ycenter + radius;
		int Zmin = Zcenter - radius;
		int Zmax = Zcenter + radius;

		copyCuboid(playerId, selection, Xmin, Xmax, Ymin, Ymax, Zmin, Zmax);

		for (int i = Xmin; i <= Xmax; i++) {
			for (int j = Ymin; j <= Ymax; j++) {
				for (int k = Zmin; k <= Zmax; k++) {
					double diff = Math.sqrt(Math.pow(i - Xcenter, 2.0D)
							+ Math.pow(j - Ycenter, 2.0D)
							+ Math.pow(k - Zcenter, 2.0D));
					if (diff < radius + 0.5
							&& (fill || (!fill && diff > radius - 0.5))) {
						Bukkit.getServer()
								.getPlayer(playerId)
								.getWorld()
								.getBlockAt(
										new Location(Bukkit.getServer()
												.getPlayer(playerId)
												.getWorld(), i, j, k))
								.setTypeId(blocktype);
					}
				}
			}
		}
		if (Cuboid.logging)
			plugin.getLogger().log(Level.INFO, playerId + " built a " + ((fill) ? "ball" : "sphere"));
	}

	public static void buildPyramid(UUID playerId, int radius,
			int blockType, boolean fill) {
		CuboidSelection selection = getPlayerSelection(playerId);

		int Xcenter = selection.firstCorner[0];
		int Ycenter = selection.firstCorner[1];
		int Zcenter = selection.firstCorner[2];
		int Xmin = Xcenter - radius;
		int Xmax = Xcenter + radius;
		int Zmin = Zcenter - radius;
		int Zmax = Zcenter + radius;
		int Ymin = Ycenter;
		int Ymax = Ycenter + radius;

		copyCuboid(playerId, selection, Xmin, Xmax, Ymin, Ymax, Zmin, Zmax);

		for (int j = Ymin; j <= Ymax; j++) {
			for (int i = Xmin; i <= Xmax; i++) {
				for (int k = Zmin; k <= Zmax; k++) {
					Bukkit.getServer()
							.getPlayer(playerId)
							.getWorld()
							.getBlockAt(
									new Location(Bukkit.getServer()
											.getPlayer(playerId).getWorld(),
											i, j, k)).setTypeId(blockType);
				}
			}
			Xmin += 1;
			Xmax -= 1;
			Zmin += 1;
			Zmax -= 1;
		}

		if (!fill && radius > 2) { // easy, but destructive way
			Xmin = Xcenter - radius + 2;
			Xmax = Xcenter + radius - 2;
			Zmin = Zcenter - radius + 2;
			Zmax = Zcenter + radius - 2;
			Ymin = Ycenter + 1;
			Ymax = Ycenter + radius - 1;
			for (int j = Ymin; j <= Ymax; j++) {
				for (int i = Xmin; i <= Xmax; i++) {
					for (int k = Zmin; k <= Zmax; k++) {
						Bukkit.getServer()
								.getPlayer(playerId)
								.getWorld()
								.getBlockAt(
										new Location(Bukkit.getServer()
												.getPlayer(playerId)
												.getWorld(), i, j, k))
								.setType(Material.AIR);
					}
				}
				Xmin += 1;
				Xmax -= 1;
				Zmin += 1;
				Zmax -= 1;
			}
		}

		if (Cuboid.logging)
			plugin.getLogger().log(
					Level.INFO,
					playerId + " built a " + ((fill) ? "filled " : "")
							+ "pyramid.");
	}

	
	 /*public static void updateChestsState(UUID playerId, int firstX, int
	  firstY, int firstZ, int secondX, int secondY, int secondZ) { int startX =
	  (firstX <= secondX) ? firstX : secondX; int startY = (firstY <= secondY)
	  ? firstY : secondY; int startZ = (firstZ <= secondZ) ? firstZ : secondZ;
	  
	  int endX = (firstX <= secondX) ? secondX : firstX; int endY = (firstY <=
	  secondY) ? secondY : firstY; int endZ = (firstZ <= secondZ) ? secondZ :
	  firstZ;
	  
	  for (int i = startX; i <= endX; i++) { for (int j = startY; j <= endY;
	  j++) { for (int k = startZ; k <= endZ; k++) { if
	  (Bukkit.getServer().getPlayer(playerId).getWorld() .getBlockTypeIdAt(i,
	  j, k) == 54 && Bukkit.getServer().getWorld("world") .getComplexBlock(i,
	  j, k) != null) { Bukkit.getServer().getWorld("world") .getComplexBlock(i,
	  j, k).update(); }
	  
	  } } } }*/
	 

}
