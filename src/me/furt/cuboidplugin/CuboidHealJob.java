package me.furt.cuboidplugin;

import java.util.TimerTask;

import org.bukkit.entity.Player;

public class CuboidHealJob extends TimerTask {
	String playerName;
	CuboidC cuboid;
	private Main plugin;

	public CuboidHealJob(String playerName, CuboidC cuboid) {
		this.playerName = playerName;
		this.cuboid = cuboid;
	}

	public void run() {
		Player player = plugin.playerMatch(this.playerName);
		if (player != null
				&& this.cuboid.contains((int) player.getLocation().getX(),
						(int) player.getLocation().getY(), (int) player
								.getLocation().getZ())) {
			if (player.getHealth() > 0) {
				player.setHealth(player.getHealth() + CuboidAreas.healPower);
			}
			if (player.getHealth() < 20) {
				CuboidAreas.healTimer.schedule(new CuboidHealJob(this.playerName, this.cuboid), CuboidAreas.healDelay);
			}
		}

	}
}