package se.jeremy.minecraft.cuboid.commands;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import se.jeremy.minecraft.cuboid.Cuboid;
import se.jeremy.minecraft.cuboid.CuboidAction;
import se.jeremy.minecraft.cuboid.CuboidAreas;
import se.jeremy.minecraft.cuboid.CuboidBackup;
import se.jeremy.minecraft.cuboid.CuboidC;

public class CModCommand implements CommandExecutor {
	private Cuboid plugin;

	public CModCommand(Cuboid instance) {
		this.plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		Player player = (Player) sender;
		String playerName = player.getName();

		if (args.length == 0) {
			plugin.printCuboidHelp(player);
			return true;
		}

		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("list")) {
				player.sendMessage(ChatColor.YELLOW + "Cuboid areas"
						+ ChatColor.YELLOW + " : "
						+ CuboidAreas.displayCuboidsList());
			} else if (args[0].equalsIgnoreCase("who")) {
				if (!Cuboid.onMoveFeatures) {
					player.sendMessage(ChatColor.YELLOW
							+ "onMove functions are disabled. So are area playerlists");
				} else {
					CuboidC cuboidArea = CuboidAreas.findCuboidArea(player.getLocation());
					
					if (cuboidArea != null) {
						cuboidArea.printPresentPlayers(player);
					} else {
						player.sendMessage("You are not in a cuboid area");
					}
				}
			} else if (args[0].startsWith("own")) {
				CuboidAreas.displayOwnedList(player);
			} else if (args[0].startsWith("global")) {
				String message = "";
				
				for (String group : Cuboid.restrictedGroups) {
					message += " " + group;
				}
				
				if (!message.equalsIgnoreCase("")) {
					player.sendMessage(ChatColor.YELLOW + "Restricted group(s) :" + ChatColor.YELLOW + message);
				} else {
					player.sendMessage(ChatColor.YELLOW + "No restricted group");
				}
				if (Cuboid.globalDisablePvP) {
					player.sendMessage(ChatColor.YELLOW + "PvP : " + ChatColor.YELLOW + "disabled");
				} else {
					player.sendMessage(ChatColor.YELLOW + "PvP : " + ChatColor.YELLOW + "allowed");
				}
				if (Cuboid.globalCreeperProt) {
					player.sendMessage(ChatColor.YELLOW + "Creeper explosions :" + ChatColor.YELLOW + " disabled");
				} else {
					player.sendMessage(ChatColor.YELLOW + "Creeper explosions :" + ChatColor.YELLOW + " enabled");
				}
				if (Cuboid.globalSanctuary) {
					player.sendMessage(ChatColor.YELLOW + "Monsters :" + ChatColor.YELLOW + " harmless");
				} else {
					player.sendMessage(ChatColor.YELLOW + "Monsters :"
							+ ChatColor.YELLOW + " dangerous");
				}
				player.sendMessage(ChatColor.RED
						+ "Local setting overwrite global ones.");
			} else if (args[0].equalsIgnoreCase("reload")) {
				if (!player.hasPermission("cuboidplugin.cuboid")) {
					player.sendMessage(ChatColor.RED
							+ "You are not allowed to use this command.");
					return true;
				}
				plugin.loadProperties();
				player.sendMessage(ChatColor.GREEN
						+ "CuboidPlugin properties reloaded");
				plugin.getLogger().log(Level.INFO, "properties reloaded");
			} else if (args[0].equalsIgnoreCase("write")) {
				if (!player.hasPermission("cuboidplugin.cuboid")) {
					player.sendMessage(ChatColor.RED
							+ "You are not allowed to use this command.");
					return true;
				}
				CuboidAreas.writeCuboidAreas();
				player.sendMessage(ChatColor.GREEN
						+ "CuboidPlugin data written to hard drive.");
			} else {
				plugin.printCuboidHelp(player);
			}
			return true;
		}
		if (args.length >= 2) {
			CuboidC playersArea = CuboidAreas.findCuboidArea(player.getLocation());
			if (playersArea != null && !playersArea.isAllowed(args[0]) && !playersArea.isOwner(player) && !player.hasPermission("cuboidplugin.ignoreownership")) {
				player.sendMessage(ChatColor.RED
						+ "This command is disallowed in this area");
				return true;
			}
			
			if (args[0].equalsIgnoreCase("globaltoggle") && player.isOp()) {
				if (args[1].equalsIgnoreCase("pvp")) {
					Cuboid.globalDisablePvP = !Cuboid.globalDisablePvP;
					if (Cuboid.globalDisablePvP) {
						plugin.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE
								+ "PvP is now allowed only in specific areas");
					} else {
						plugin.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE
								+ "PvP is now allowed anywhere");
					}

				} else if (args[1].equalsIgnoreCase("creeper")
						|| args[1].equalsIgnoreCase("creepers")) {
					Cuboid.globalCreeperProt = !Cuboid.globalCreeperProt;
					if (Cuboid.globalCreeperProt) {
						plugin.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE
								+ "Creepers now explode only in specific areas");
					} else {
						plugin.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE
								+ "Creepers now explode anywhere");
					}
				} else if (args[1].equalsIgnoreCase("sanctuary")
						|| args[1].equalsIgnoreCase("sanctuaries")) {
					Cuboid.globalSanctuary = !Cuboid.globalSanctuary;
					if (Cuboid.globalSanctuary) {
						plugin.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE
								+ "Monsters now hurt only in specific areas");
					} else {
						plugin.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE
								+ "Monsters now hurt anywhere");
					}
				} else {
					player.sendMessage(ChatColor.RED
							+ "Usage : /cmod globaltoggle <pvp | creepers | sanctuary>");
				}
				return true;
			}

			if (args[1].equalsIgnoreCase("create")
					|| args[1].equalsIgnoreCase("add")) {
				if (!player.hasPermission("cuboidplugin.protect")) {
					player.sendMessage(ChatColor.RED
							+ "You are not allowed to use this command.");
					return true;
				}
				if (CuboidAction.isReady(playerName, true))
					CuboidAreas.createCuboidArea(player, args[0]);
				else
					player.sendMessage(ChatColor.RED
							+ "No cuboid has been selected");
				return true;
			}

			CuboidC cuboidArea = CuboidAreas.findCuboidArea(args[0]);
			if (cuboidArea == null) {
				player.sendMessage(ChatColor.RED + "Area not found : "
						+ args[0]);
				return true;
			}

			if (args[1].equalsIgnoreCase("allow")) {
				if (args.length < 3) {
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
						|| player.hasPermission("cuboidplugin.protect")) {
					String[] parameters = new String[args.length - 2];
					for (int i = 2; i < args.length; i++) {
						parameters[i - 3] = args[i];
					}
					CuboidAreas.treatAllowance(player, parameters, cuboidArea);
				} else {
					player.sendMessage(ChatColor.RED
							+ "You are not an owner of this cuboid area.");
					return true;
				}
			} else if (args[1].equalsIgnoreCase("disallow")
					|| args[1].equalsIgnoreCase("revoke")) {
				if (args.length < 3) {
					player.sendMessage(ChatColor.RED
							+ "Usage : disallow <player> and/or </command>");
					return true;
				}
				if (cuboidArea.isOwner(player)
						|| player.hasPermission("cuboidplugin.protect")) {
					String[] parameters = new String[args.length - 2];
					for (int i = 2; i < args.length; i++) {
						parameters[i - 3] = args[i];
					}
					CuboidAreas.treatDisallowance(player, parameters,
							cuboidArea);
				} else {
					player.sendMessage(ChatColor.RED
							+ "You are not an owner of this cuboid area.");
					return true;
				}
			} else if (args[1].startsWith("info")) {
				cuboidArea.printInfos(player, true, true);
			} else if (args[1].startsWith("select")) {
				if (!player.hasPermission("cuboidplugin.cuboid")) {
					player.sendMessage(ChatColor.RED
							+ "You are not allowed to use this command.");
					return true;
				}
				CuboidAction.setBothPoint(playerName, cuboidArea.coords);
				player.sendMessage(ChatColor.GREEN + "Area selected : "
						+ cuboidArea.name);
			} else if (args[1].equalsIgnoreCase("move")) {
				if (!player.hasPermission("cuboidplugin.protect")) {
					player.sendMessage(ChatColor.RED
							+ "You are not allowed to use this command.");
					return true;
				}
				if (CuboidAction.isReady(playerName, true))
					CuboidAreas.moveCuboidArea(player, cuboidArea);
				else
					player.sendMessage(ChatColor.RED
							+ "No cuboid has been selected");
			} else if (args[1].equalsIgnoreCase("delete")
					|| args[1].equalsIgnoreCase("remove")) {
				if (!player.hasPermission("cuboidplugin.protect")) {
					player.sendMessage(ChatColor.RED
							+ "You are not allowed to use this command.");
					return true;
				}
				CuboidAreas.removeCuboidArea(player, cuboidArea);
			} else if (args[1].equalsIgnoreCase("toggle")) {
				if (cuboidArea.isOwner(player)
						|| player.hasPermission("cuboidplugin.protect")
						|| player.hasPermission("cuboidplugin.cuboidareas")) {
					if (args.length < 3) {
						player.sendMessage(ChatColor.RED
								+ "Usage : toggle"
								+ "<protection/restriction/pvp/heal/sanctuary/ceeper>");
						return true;
					}
					if (args[2].startsWith("prot")) {
						cuboidArea.protection = !cuboidArea.protection;
						/*
						 * CuboidAction.updateChestsState(playerName,
						 * cuboidArea.coords[0], cuboidArea.coords[1],
						 * cuboidArea.coords[2], cuboidArea.coords[3],
						 * cuboidArea.coords[4], cuboidArea.coords[5]);
						 */
						player.sendMessage(ChatColor.GREEN
								+ "Protection : "
								+ (cuboidArea.protection ? "enabled"
										: "disabled"));
					} else if (args[2].startsWith("restric")) {
						if (!player.hasPermission("/cuboidAreas")
								&& !Cuboid.allowRestrictedZones) {
							player.sendMessage(ChatColor.YELLOW
									+ "Restricted areas switching is disabled");
							return true;
						}
						cuboidArea.restricted = !cuboidArea.restricted;
						player.sendMessage(ChatColor.GREEN
								+ "Restricted access : "
								+ (cuboidArea.restricted ? "enabled"
										: "disabled"));
					} else if (args[2].equalsIgnoreCase("pvp")) {
						if (!player.hasPermission("/cuboidAreas")
								&& !Cuboid.allowNoPvpZones) {
							player.sendMessage(ChatColor.YELLOW
									+ "No-PvP areas switching is disabled");
							return true;
						}
						cuboidArea.PvP = !cuboidArea.PvP;
						player.sendMessage(ChatColor.GREEN
								+ "No PvP allowed : "
								+ (!cuboidArea.PvP ? "enabled" : "disabled"));
					} else if (args[2].equalsIgnoreCase("heal")) {
						if (!player.hasPermission("/cuboidAreas")
								&& CuboidAreas.healPower == 0) {
							player.sendMessage(ChatColor.YELLOW
									+ "Healing areas switching is disabled");
							return true;
						}
						cuboidArea.heal = !cuboidArea.heal;
						player.sendMessage(ChatColor.GREEN + "Healing : "
								+ (cuboidArea.heal ? "enabled" : "disabled"));
					} else if (args[2].equalsIgnoreCase("sanctuary")) {
						if (!player.hasPermission("/cuboidAreas")
								&& !Cuboid.allowSanctuaries) {
							player.sendMessage(ChatColor.YELLOW
									+ "Sanctuaries switching is disabled");
							return true;
						}
						cuboidArea.sanctuary = !cuboidArea.sanctuary;
						player.sendMessage(ChatColor.GREEN
								+ "Sanctuary : "
								+ (cuboidArea.sanctuary ? "enabled"
										: "disabled"));
					} else if (args[2].equalsIgnoreCase("creeper")) {
						if (!player.hasPermission("/cuboidAreas")
								&& !Cuboid.allowNoCreeperZones) {
							player.sendMessage(ChatColor.YELLOW
									+ "No-Creeper areas switching is disabled");
							return true;
						}
						cuboidArea.creeper = !cuboidArea.creeper;
						player.sendMessage(ChatColor.GREEN
								+ "No creeper explosion : "
								+ (!cuboidArea.creeper ? "enabled" : "disabled"));
					} else {
						player.sendMessage(ChatColor.RED
								+ "Usage : toggle <protection/restriction/pvp/heal/sanctuary/creeper>");
					}
				} else {
					player.sendMessage(ChatColor.RED
							+ "You are not an owner of this cuboid area.");
					return true;
				}
			} else if (args[1].equalsIgnoreCase("backup")) {
				if (cuboidArea.isOwner(player)
						|| player.hasPermission("cuboidplugin.protect")) {
					byte returnCode = new CuboidBackup(cuboidArea, true)
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
			} else if (args[1].equalsIgnoreCase("restore")) {
				if ((Cuboid.allowOwnersToBackup && cuboidArea.isOwner(player))
						|| player.hasPermission("cuboidplugin.protect")) {
					byte returnCode = new CuboidBackup(cuboidArea, false)
							.loadFromDisc(playerName);
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
			} else if (args[1].equalsIgnoreCase("welcome")) {
				if (cuboidArea.isOwner(player)
						|| player.hasPermission("cuboidplugin.protect")) {
					if (args.length == 2) {
						cuboidArea.welcomeMessage = null;
						player.sendMessage(ChatColor.GREEN
								+ "Welcome message disabled.");
						return true;
					}
					String message = "";
					for (int i = 2; i < args.length; i++) {
						message += " " + args[i];
					}
					cuboidArea.welcomeMessage = message.trim();
					player.sendMessage(ChatColor.GREEN
							+ "Welcome message successfuly changed.");
					return true;
				} else {
					player.sendMessage(ChatColor.RED
							+ "You are not an owner of this cuboid area.");
					return true;
				}
			} else if (args[1].equalsIgnoreCase("farewell")) {
				if (cuboidArea.isOwner(player)
						|| player.hasPermission("cuboidplugin.protect")) {
					if (args.length == 2) {
						cuboidArea.farewellMessage = null;
						player.sendMessage(ChatColor.GREEN
								+ "Farewell message disabled.");
						return true;
					}
					String message = "";
					for (int i = 2; i < args.length; i++) {
						message += " " + args[i];
					}
					cuboidArea.farewellMessage = message.trim();
					player.sendMessage(ChatColor.GREEN
							+ "Farewell message successfuly changed.");
					return true;
				} else {
					player.sendMessage(ChatColor.RED
							+ "You are not an owner of this cuboid area.");
					return true;
				}
			} else if (args[1].equalsIgnoreCase("warning")) {
				if (cuboidArea.isOwner(player)
						|| player.hasPermission("cuboidplugin.protect")) {
					if (args.length == 2) {
						cuboidArea.warning = null;
						player.sendMessage(ChatColor.GREEN
								+ "Restricted area message disabled.");
						return true;
					}
					String message = "";
					for (int i = 2; i < args.length; i++) {
						message += " " + args[i];
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
		}
		return false;
	}
}
