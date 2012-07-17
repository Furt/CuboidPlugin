package me.furt.cuboidplugin.listener;

import me.furt.cuboidplugin.CuboidAreas;
import me.furt.cuboidplugin.CuboidC;
import me.furt.cuboidplugin.Main;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CPEntity implements Listener {
	private Main plugin;

	public CPEntity(Main instance) {
		this.plugin = instance;
	}
	
	public boolean onMobSpawn(CreatureSpawnEvent event) { // Has never worked right ><
		Entity mob = event.getEntity();
		Location loc = mob.getLocation();
		CuboidC cuboid = CuboidAreas.findCuboidArea((int) loc.getX(),
				(int) loc.getY(), (int) loc.getZ());
		if (cuboid != null) {
			return cuboid.sanctuary;
		}
		return Main.globalSanctuary;
	}

	public boolean onExplode(Block block) {
		if (block.getStatus() == 2) {
			CuboidC cuboid = CuboidAreas.findCuboidArea(block.getX(),
					block.getY(), block.getZ());
			if (cuboid != null) {
				return !cuboid.creeper;
			}
			return Main.globalCreeperProt;
		}
		return false;
	}
}
