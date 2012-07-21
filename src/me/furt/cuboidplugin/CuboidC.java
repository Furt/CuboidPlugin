package me.furt.cuboidplugin;

import java.io.Serializable;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@SuppressWarnings("serial")
public class CuboidC implements Serializable {
	public String name = "noname";
	public int[] coords = new int[6];
	public boolean protection = false;
	public boolean restricted = false;
	boolean trespassing = false;
	public boolean PvP = true;
	public boolean heal = false;
	public boolean creeper = true;
	public boolean sanctuary = false;
	ArrayList<String> allowedPlayers = new ArrayList<String>();
	public String welcomeMessage = null;
	public String farewellMessage = null;
	public String warning = null;
	ArrayList<String> disallowedCommands = new ArrayList<String>();
	private Main plugin;

	public CuboidC(Main instance) {
		this.plugin = instance;
	}

	public boolean contains(int X, int Y, int Z) {
		if (X >= coords[0] && X <= coords[3] && Z >= coords[2]
				&& Z <= coords[5] && Y >= coords[1] && Y <= coords[4])
			return true;
		return false;
	}

	public boolean contains(Location l) {
		return contains((int) l.getX(), (int) l.getY(), (int) l.getZ());
	}

	// TODO come back to later
	public boolean isAllowed(Player player) {
		return true;
	}

	public boolean isAllowed(String command) {
		for (String disallowed : disallowedCommands) {
			if (command.equals(disallowed))
				return false;
		}
		return true;
	}

	public boolean isOwner(Player player) {
		String playerName = "o:" + player.getName();
		for (String allowedPlayer : allowedPlayers) {
			if (allowedPlayer.equalsIgnoreCase(playerName)) {
				return true;
			}
		}
		return false;
	}

	public void allowPlayer(String playerName) {
		boolean done = false;
		boolean newIsOwner = false;
		if (playerName.startsWith("o:")) {
			playerName = playerName.substring(2);
			newIsOwner = true;
		}

		for (int j = 0; j < this.allowedPlayers.size() && !done; j++) {
			String allowedPlayer = this.allowedPlayers.get(j);

			// if the new player already is in the list as simple allowed
			if (allowedPlayer.equalsIgnoreCase(playerName)) {
				// we switch him to owner if needed
				if (newIsOwner) {
					this.allowedPlayers.set(j, "o:" + playerName);
				}
				// we've found the guy, no need to add him again.
				done = true;
			}

			// if the new player already is an owner, no need to go further
			if (allowedPlayer.equalsIgnoreCase("o:" + playerName)) {
				done = true;
			}
		}

		// If the player wasn't found, we add him.
		if (!done) {
			this.allowedPlayers.add(((newIsOwner) ? "o:" : "") + playerName);
		}
	}

	public void disallowPlayer(String playerName) {
		this.allowedPlayers.remove(playerName);
	}

	public void disallowCommand(String command) {
		if (!disallowedCommands.contains(command))
			disallowedCommands.add(command);
	}

	public void allowCommand(String command) {
		disallowedCommands.remove(command);
	}

	public void playerEnters(Player player) {
		if (this.welcomeMessage != null)
			player.sendMessage(ChatColor.YELLOW + this.welcomeMessage);
	}

	public void playerLeaves(Player player) {
		if (this.farewellMessage != null)
			player.sendMessage(ChatColor.YELLOW + this.farewellMessage);
	}

	public void printInfos(Player player, boolean players, boolean commands) {
		player.sendMessage(ChatColor.YELLOW + "----    " + this.name
				+ "    ----");
		String flags = "";
		boolean noflag = true;
		if (this.protection) {
			flags += " protection";
			noflag = false;
		}
		if (this.restricted) {
			flags += " restricted";
			noflag = false;
		}
		if (!this.PvP) {
			flags += " no-PvP";
			noflag = false;
		}
		if (this.heal) {
			flags += " heal";
			noflag = false;
		}
		if (!this.creeper) {
			flags += " creeper-free";
			noflag = false;
		}
		if (this.sanctuary) {
			flags += " sanctuary";
			noflag = false;
		}
		if (this.welcomeMessage != null) {
			flags += " welcome";
			noflag = false;
		}
		if (this.farewellMessage != null) {
			flags += " farewell";
			noflag = false;
		}
		if (noflag) {
			flags += " <none>";
		}

		player.sendMessage(ChatColor.YELLOW + "Flags :" + ChatColor.WHITE
				+ flags);
		printAllowedPlayers(player);

		if (players) {
			printPresentPlayers(player);
		}
		if (commands) {
			printDisallowedCommands(player);
		}
	}

	public void printAllowedPlayers(Player player) {
		if (this.allowedPlayers.size() == 0) {
			player.sendMessage(ChatColor.YELLOW + "Allowed players : "
					+ ChatColor.WHITE + "<list is empty>");
			return;
		}
		String list = "";
		for (String playerName : this.allowedPlayers) {
			list += " " + playerName;
		}
		player.sendMessage(ChatColor.YELLOW + "Allowed players :"
				+ ChatColor.WHITE + list);
	}

	public void printPresentPlayers(Player player) {
		String list = "";
		for (Player p : plugin.getServer().getOnlinePlayers()) {
			if (this.contains((int) p.getLocation().getX(), (int) p
					.getLocation().getY(), (int) p.getLocation().getZ())) {
				list += " " + p.getName();
			}
		}
		if (list.length() < 2) {
			player.sendMessage(ChatColor.YELLOW + "Present players : "
					+ ChatColor.WHITE + "<list is empty>");
		} else {
			player.sendMessage(ChatColor.YELLOW + "Present players :"
					+ ChatColor.WHITE + list);
		}
	}

	public void printDisallowedCommands(Player player) {
		if (this.disallowedCommands.size() == 0) {
			player.sendMessage(ChatColor.YELLOW + "Disallowed commands : "
					+ ChatColor.WHITE + "<list is empty>");
			return;
		}
		String list = "";
		for (String command : this.disallowedCommands) {
			list += " " + command;
		}
		player.sendMessage(ChatColor.YELLOW + "Disallowed commands :"
				+ ChatColor.WHITE + list);
	}
}