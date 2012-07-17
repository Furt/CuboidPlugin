package me.furt.cuboidplugin;

import java.util.TimerTask;

import org.bukkit.entity.Player;

public class CuboidHealJob extends TimerTask {
	String playerName;
	CuboidC cuboid;
	private Main plugin;

	public CuboidHealJob(Main instance, String playerName, CuboidC cuboid) {
		this.playerName = playerName;
		this.cuboid = cuboid;
		this.plugin = instance;
	}
	
	public Player playerMatch(String name) {
		if (plugin.getServer().getOnlinePlayers().length < 1) {
			return null;
		}

		Player[] online = plugin.getServer().getOnlinePlayers();
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

	public void run() {
		Player player = playerMatch(this.playerName);
		if (player != null
				&& this.cuboid.contains((int) player.getLocation().getX(),
						(int) player.getLocation().getY(), (int) player
								.getLocation().getZ())) {
			if (player.getHealth() > 0) {
				player.setHealth(player.getHealth() + CuboidAreas.healPower);
			}
			if (player.getHealth() < 20) {
				CuboidAreas.healTimer.schedule(new CuboidHealJob(plugin,
						this.playerName, this.cuboid), CuboidAreas.healDelay);
			}
		}

	}
}