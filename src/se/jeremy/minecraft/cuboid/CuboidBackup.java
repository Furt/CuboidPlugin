package se.jeremy.minecraft.cuboid;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;

/*
 * Serializing the content of a cuboid area
 * Needs a lot of work (a chest-content management, for instance)
 */

@SuppressWarnings("serial")
public class CuboidBackup implements Serializable {

	private String name;
	private Material[][][] cuboidData;
	private int[] coords;
	private Cuboid plugin;
	private World world;

	public CuboidBackup(CuboidC cuboid, boolean store) {
		this.name = cuboid.name;
		this.world = cuboid.world;
		this.coords = cuboid.coords;
		if (store) {
			storeCuboidData(cuboid.world);
		}
			
	}

	public Material[][][] getData() {
		return this.cuboidData;
	}

	private void storeCuboidData(World world) {
		int Xsize = this.coords[3] - this.coords[0] + 1;
		int Ysize = this.coords[4] - this.coords[1] + 1;
		int Zsize = this.coords[5] - this.coords[2] + 1;
		this.cuboidData = new Material[Xsize][][];
		for (int i = 0; i < Xsize; i++) {
			this.cuboidData[i] = new Material[Ysize][];
			for (int j = 0; j < Ysize; ++j) {
				this.cuboidData[i][j] = new Material[Zsize];
				for (int k = 0; k < Zsize; ++k) {
					this.cuboidData[i][j][k] = world.getBlockAt(this.coords[0] + i, this.coords[1] + j, this.coords[2] + k).getType();
				}
			}
		}
	}

	private void restoreCuboidData(UUID playerId) {
		World world = Bukkit.getPlayer(playerId).getWorld();
		int Xsize = cuboidData.length;
		int Ysize = cuboidData[0].length;
		int Zsize = cuboidData[0][0].length;
		
		for (int i = 0; i < Xsize; i++) {
			for (int j = 0; j < Ysize; ++j) {
				for (int k = 0; k < Zsize; ++k) {
					world.getBlockAt(this.coords[0] + i, this.coords[1] + j, this.coords[2] + k).setType(this.cuboidData[i][j][k]);
				}
			}
		}
	}

	public byte writeToDisk() {
		// checking folders
		File cuboidFolder = new File(plugin.getDataFolder() + File.separator
				+ "backups");
		try {
			if (!cuboidFolder.exists()) {
				cuboidFolder.mkdir();
			}
			File subFolder = new File(plugin.getDataFolder() + File.separator
					+ "backups" + File.separator + world);
			try {
				if (!subFolder.exists()) {
					subFolder.mkdir();
				}
			} catch (Exception e) {
				e.printStackTrace();
				return 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}

		// writing
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					new BufferedOutputStream(new FileOutputStream(new File(
							plugin.getDataFolder() + File.separator + "backups"
									+ File.separator + world, this.name
									+ ".cuboid"))));
			oos.writeObject(this.cuboidData);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
			return 2;
		}
		if (Cuboid.logging)
			plugin.getLogger().log(
					Level.INFO,
					"New cuboidArea backup : " + this.name + " on world: "
							+ world);
		return 0;
	}

	@SuppressWarnings("resource")
	public byte loadFromDisc(UUID playerId) {
		try {
			ObjectInputStream ois = new ObjectInputStream(
					new BufferedInputStream(new FileInputStream(new File(
							plugin.getDataFolder() + File.separator + "backups"
									+ File.separator + world, this.name
									+ ".cuboid"))));
			try {
				this.cuboidData = (Material[][][]) (ois.readObject());
			} catch (Exception e) {
				e.printStackTrace();
				return 3;
			}
			ois.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return 1;
		} catch (IOException e) {
			e.printStackTrace();
			return 2;
		}

		restoreCuboidData(playerId);

		if (Cuboid.logging) {
			plugin.getLogger().log(Level.INFO, "Loaded cuboidArea backup : " + this.name + " on world: " + world);
		}
			
		return 0;
	}

	public boolean deleteFromDisc() {
		File fileToDelete = new File(plugin.getDataFolder() + File.separator + "backups" + File.separator + world, this.name + ".cuboid");
		
		if (fileToDelete.exists()) {
			return fileToDelete.delete();
		}
		
		return true;
	}
}
