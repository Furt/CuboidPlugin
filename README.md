# Introduction

CuboidPlugin is a powerful tool that allows users to perform various actions on user-selected areas withing minecraft.
Here is a quick list of its possibilities :

    Edition features : fill the area with a block type, empty an area, replace certain blocks, move the areas' content, copy then paste an area, save & load an area, undo the last action performed
    Construction features : build walls, build cube faces, draw circles and discs, build spheres and balls, build pyramids
    Area-specific toggle-able features : block protection, access restriction, area-specific inventory, healing areas, sanctuaries (no mob spawn + no damage from them), no creeper explosions, no-PvP areas, local command blacklist, area-specific list of players, backup & restore by area-name

## Notice

This plugin is still in development. Commands that work right now are marked with **[√]**.


# Download and changelogs

    Required hMod version : hMod build 132 or newer

    Version 18 for hMod 132+
    Last update : 25/12 - 22h00 GMT+1
    Source files are included in the .jar
    Here is the .updatr file (Are you wondering what is Updatr ?)

    v17.9 changelog (for some reason I can't see my last post, this will have to wait ><), v17.7 changelog, v17.4 changelog, v17.2 changelog, v17 changelog, v16 changelog, v15.5 changelog, v15.2 changelog, v15 changelog, v14.7 changelog, v14.5 changelog, v14 changelog, v13 changelog

    https://github.com/Relliktsohg/CuboidPlugin, if you feel like helping / forking


# Installation and configuration

    Copy Cuboid.jar in the plugins folder
    Edit server.properties, and add "CuboidPlugin" to the plugins line (careful with CAPS)
    Edit your group file/table and add the following :
    /cuboid command clearance to whoever can use the edition / construction commands (should be reserved to admins)
    /protect command clearance to whoever can create/delete/modify cuboid areas (should be reserved to moderators)
    /ignoresOwnership command clearance to whoever can ignore areas' protection, restricted access and local command blacklists (should be moderators and admins)
    note that '/cuboid' and '/ignoresOwnership' are not an actual commands. This is just a way to check who is allowed to do what.

# Usage

## General information

Right-click when pointing to a block with a wooden sword in your hand to know if it is in a cuboid area, and have few info about it.

Output example:

    -----  Area's Name  ----
    Flags : no-PvP protection no-creeper healing
    Allowed players : God Notch o:Hey0

Cuboid areas: when created, an cuboid areas has all features disables by default. Owners will have to select which one they want to enable. (protection, access restriction, welcome/farewell messages, area-specific inventory)

**Restricted areas:**

- Only allowed players, owners, and /ignoreOwnership-allowed-players will be able to enter.
- This is toggle-able independently by area, or globally via the config file.
- A player in a restricted area won't be kicked out. Instead, he can move freely until leaving the area. Then he won't be able to come back inside, just like all the non-allowed people. The same behavior applies to players that were in the area when the 'restricted' switch was turned on.

**Command blacklists:**

- An area allows every possible command by default
- Use the disallow command to blacklist a command
- Only owners and /ignoreOwnership-allowed-players can ignore this blacklist.
- Players are still bound to the hMod rules, and this blacklist merely filters the resquests.

**Area-specific inventories:**

- Anyone who enters will have an area-bound inventory. The 'oustide' inventories are returned upon leaving the area, and the inventory is kept in memory until next visit.
- This is toggle-able independently by area, or globally via the config file.
- /!\ Beta feature ! There are several issues that still need to be fixed. Sometimes, the last-held-item (when entering) will be duplicated when the inventory is returned. I don't know yet where that comes from. Also, I didn't have enough time to prevent people from dropping their items into an area, then getting it back by walking on them.

**Sanctuary area:**

Mobs can not spawn in a sanctuary. They can still walk inside, but will then be unable to deal damage.

**No creeper-explosions:**

You can prevent creepers from exploding withing certain areas

**PvP switch:**

If the defender is in a no-PvP area, the attack will be denied. By default, PvP is allowed everywhere.

**Healing area:**

Any player that enters a healing area will get a health regen until he is at full health or leaves the area. The power and the delay between heals can be changed in the config file.

**Writing data to hard drive:**

- All cuboid areas will be written to disc at the same time, once the server is issued a 'stop' command, or the plugin is disabled
- Also, the format of the file changed, and the file itself is now named 'cuboidAreas.dat'.
Retro-compatibility is assured, and data will be read from you 'protectedCuboids.txt' the first time you launch this version.
- Alternatively, one can use the /cmod write command to force the saving to the hard drive. This requires the /cuboid command clearance.

**How to set up a guest zone:**

- allow your default group to build (flatfiles : flag 0, SQL : flag 1 - I think)
- set restrictedGroups=defaultGroupName (in your cuboidPlugin.properties, and replace defaultGroupName by the correct name)
- ingame, create a cuboid where you want the guests to be able to build, with the /protect g:defaultGroupName guestZone

## Worldwide features

Only admins can use these commands:

```/cmod globaltoggle pvp```: By default, pvp is allowed everywhere. Any cuboid can choose to overwrite the global setting, regardless of its state. This allows cuboid to be non-PvP areas in a PvP world, and it allows cuboid to be PvP areas in a non-PvP world.
PvP has to be enabled in server.properties for this to work.
Caution : cuboids have PvP allowed by default, so any cuboid will be a left this way unless specified otherwise.

```/cmod globaltoggle creepers```: By default, creepers explode. Any cuboid can choose to overwrite the global setting, regardless of its state.
Caution : cuboids have Creeper explosions allowed by default, so any cuboid will be a left this way unless specified otherwise.

```/cmod globaltoggle sanctuary```: By default, the world is a dangerous place. Any cuboid can choose to overwrite the global setting, regardless of its state.

**Caution:** cuboids have Sanctuary disabled by default, so any cuboid will be a left this way unless specified otherwise.
Also, there is a bug with the onSpawn hook, so I can't prevent spawning for now, just damage. :/

```/cmod globalstatus```: Prints the status of the global features

## Cuboid selection

When holding a wood shovel (or the tool you configured), right click on a block, then another block.
It displays messages, and you're ready to type a treatment command. Issuing a treatment command on a cuboid DOES NOT reset the selection.
On this image: [Image: Cuboid_01.png], the two points you have to set up are A and G.
- Requires the right to use /protect or /cuboid command

```/cmod <areaName> select```: This selects a cuboid Area, enabling you to perform action on it from afar. Caution : the target chunk has to be loaded first, and I haven't tried to perform action on a non-loaded area.


## Area-specific commands

```/cmod```: Prints the list of commands you can use, with a short description
- Anyone can use this

```/cmod list```: Prints a list of cuboid areas
- Anyone can use this

```/cmod owned```: Prints a list of cuboid areas the player owns
- Anyone can use this

```/cmod who```: Prints a list of players in the area you are in
- Anyone can use this

```/cmod <name> info```: Prints lots of info about the area (name, protection state, is the access restricted ?, is the area-inventory enabled ?, allowed players list, blacklisted commands, list of players in the area)
*Anyone can use this*

/cmod <name> create - creates a new cuboidArea
*Requires the /protect command clearance*

```/cmod <name> delete```: Deletes the cuboidArea
*Requires the /protect command clearance*

```/cmod <name> move```: Moves the cuboidArea to the current selection
*Requires the /protect command clearance*

```/cmod <name> allow <list>```: Allow players/groups/commands and set owners. <list> is to be replaced by a combination of player/o:owner/g:groupname strings.
*Requires the player to be owner of the area, or have the /protect command clearance*

```/cmod <name> disallow <list>```: Disallow players/groups/commands. Only /protect-allowed-players can revoke ownership.
- Requires the player to be owner of the area, or have the /protect command clearance

```/cmod <name> toggle <option>```: Toggles the option. The available options are:

- **protection** - Toggles ability for others to destroy blocks in the cuboid
- **restriction** - Toggles the restricted access of the zone (only the allowed players and the owners will be able to enter)
- **inventories** - Toggles the area-specific inventory /!\ This is a beta, lots of exploits are possible at the moment.
- **heal** - Toggles the health regeneration
- **creeper** - Toggles the creeper-explosion protection
- **sanctuary** - Toggles the mob spawning in the area (+ mobs deal no damage in these areas)
- **pvp** - Toggles the no-pvp flag (by default, pvp is allowed)

*Requires the player to be owner of the area, or have the /protect command clearance*

```/cmod <name> welcome <text>```: Sets welcome message. Spaces and special characters are allowed
*Requires the player to be owner of the area, or have the /protect command clearance*

```/cmod <name> farewell <text>```: Sets farewell message. Spaces and special characters are allowed
*Requires the player to be owner of the area, or have the /protect command clearance*

```/cmod <name> warning <text>```: Sets 'this area is restricted' message. Spaces and special characters are allowed
*Requires the player to be owner of the area, or have the /protect command clearance*

```/cmod <name> backup```: Backs up the cuboidArea.
*Requires the player to be owner of the area, or have the /protect command clearance*

```/cmod <name> restore```: restores the cuboidArea. There is a flag to prevent owners from restoring their area, to prevent duplication.
*Requires the player to be owner of the area, or have the /protect command clearance*

```/highprotect <player/o:owner/g:groupname> <name of the cuboid>```: Creates a protected area from bedrock to skytop.
*Requires the right to use /protect command


## Edition commands

```/csize```: Displays the number of blocs inside the selected cuboid
- Requires the right to use /cuboid command

```/undo```: Reverts the last action performed on a cuboid.
- Requires the right to use /cuboid command

```/cfill <bloc ID|name>```: Fills the selected cuboid with the specified blocType. accepted IDs are between 1 and 84 (the only real blocs available). This has proven VERY handy to repair griefed areas.
This beach has been emptied by glass-hungry people ? No worries ! "/cfill sand" and it is good as new.
This lake has flowing water on the surface, and is no longer flat ? Haha, here comes the /cfill stillwater !
etc...
Please do not abuse. I put that here for moderating purposes, not for you to instantly build 50-blocs-high-walls.
- Requires the right to use /cuboid command

```/creplace <bloc ID|name> <bloc ID|name>```: Replaces all specified blocTypes with the last blocType specified, within the selected cuboid.
For instance : /replace grass sand goldore will replace any grass(2) or sand(12) block by gold ore(14).
You can also use itemIDs if you like, or mix both : /replace 2 sand 14 will do the same as above.
- Requires the right to use /cuboid command

```/cdel``` **[√]**: Simply removes any block within the Cuboid. I've removed a lot of unwanted constructions with this thing.
Same as above, please do not abuse. (Though it is tempting to use this to flatten the area for you soon-to-come huge manor =D)
- Requires the right to use /cuboid command

```/cmove <Up|Down|North|East|West|South> <distance>```: Needs two corners selected. This command is also undo-able.
- Requires the right to use /cuboid command

```/ccopy``` and ```/cpaste```: select two corners of a cuboid, copy it, then paste it. Carful, the copied cuboid will be deleted once you select another cuboid. In other terms, you can copy, select a point, paste, select another point, re-paste etc... but as soon as you select two points in a row, I'll remove the copy from memory.
The selected point should be the North East lower corner of the copied cuboid. I haven't found an easy way to orientate the thing, so this will have to do, for now.
- Requires the right to use /cuboid command

```/csave <name>```: select a cuboid, type /csave <name> : it will be saved server-side, in a folder called "cuboids", and a sub-folder of the player name. Type /csave <name> overwrite will write over a previously existing file.
- Requires the right to use /cuboid command

```/cload <name>```: select a single point, type /cload <name> : this will paste the requested cuboid. You cannot - yet - undo a paste.
- Requires the right to use /cuboid command

```/clist```: prints the list of available cuboids. Admins can also type "/clist <player name>" to access the list of this player.
- Requires the right to use /cuboid command

```/cshare <cuboidname> <playername>```: shares the specified saved cuboid. The target player has to be online, and the copy won't happen if the target player already has a saved cuboid with the same name.
- Requires the right to use /cuboid command

```/cremove <cuboid name>```: removes the saved cuboid from the caller's folder.
- Requires the right to use /cuboid command


## Construction commands

```/ccircle <radius> <block ID|name> [height]``` **[√]**: You only need to select one point. It draws a circle on the ground with the specified block type. Optionally, you can specify a height to create a cylinder. (negative height is possible - carful with going through bedrock)
*Requires the right to use /cuboid command*

```/cdisc <radius> <block ID|name> [height]```: You only need to select one point. It draws a disc on the ground with the specified block type. Optionally, you can specify a height to create a filled cylinder. (negative height is possible - carful with going through bedrock)
*Requires the right to use /cuboid command*

```/csphere <radius> <block ID|name>```: You only need to select one point. It builds an empty sphere around the selected center, with the specified block type.
*Requires the right to use /cuboid command*

```/cball <radius> <block ID|name>```: You only need to select one point. It builds a filled sphere around the selected center, with the specified block type.
*Requires the right to use /cuboid command*

```/cfaces <block ID|name>```: Needs two corners selected. It builds the faces of the selected cuboid. If you're afraid of the dark, don't place yourself inside, place some toches beforhand, or build it with glass Wink
*Requires the right to use /cuboid command*

```/cwalls <block ID|name>``` **[√]**: Needs two corners selected. It builds the faces of the selected cuboid, without floor and ceiling. Also, resolves the fear of the dark, as sunlight can now go through the roof \o/
*Requires the right to use /cuboid command*

```/cpyramid <radius> <block ID|name> [empty]```: Needs a single point selected. Builds a pyramid of the selected material, with the selected point as base center. This command is also undo-able.
*Requires the right to use /cuboid command*