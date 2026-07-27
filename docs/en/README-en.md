[English](../en/README-en.md) | [简体中文](../../README.md)

[Translation Tutorial](../en/TranslationTutorial-en.md) | [翻译教程](../../TranslationTutorial)

# BlockRacing

This is a multiplayer racing mini-game in Minecraft, divided into two teams, where the team that collects the specified blocks first wins.

Version: Java 26.2 (requires Java 25)

Requirements: Paper 26.2 and Java 25. Starting with 26.2.1, BlockRacing uses Paper APIs directly and no longer supports Spigot.

## Upgrade Notice

Replacing only the plugin JAR does not update existing configuration files. If the versions in an old `config.yml` or `lang.yml` do not match the plugin, an in-game warning is displayed and new settings or messages may not work correctly.

Back up `plugins/BlockRacing` before upgrading, then choose one method:

1. No customizations: delete the old `config.yml`, `lang.yml`, five block pools, and two translation JSON files, then restart the server to generate current files.
2. Customized files: manually merge your changes into the current default files. Do not change only the version number at the end of a file.

# Features

1. Team Chests: Each team has 3 team chests, and the items inside are shared among team members.

2. Reward Mechanism: Collecting a block grants the opposing team 64 blocks of that block (stored in the team chests).

3. Team Teleportation: Players within the same team can freely teleport to each other.

4. Roll Blocks: When the current required blocks are too difficult to obtain, they can be rotated out, limited to three times per game.

5. Locate: Players can spend team points to purchase the locate command for finding biomes or structures.

6. Waypoints: Each team has 3 waypoints for free saving, teleporting, and deleting.

# Gameplay Instructions

## Preparation Phase

- After entering the game, open the menu with Shift+F.
- Choose a team in the menu.
- Set the target block library in the menu, optionally enabling medium difficulty blocks, hard difficulty blocks, dyed blocks, and End dimension blocks.
- Set the target block quantity in the menu.
- Switch between modes (normal mode or racing mode, with an option to enable extreme speed mode).
- Once all players are ready, click the diamond in the menu to start the game.
- When all players enter /restartgame, the server will automatically restart and start a new game.

## Game Phase

- After random teleportation, follow the scoreboard and start collecting blocks.

- Shift+F opens the menu, where players can use team chests, roll, locate, waypoints, and random teleportation.

- After purchasing locate permissions, use /locatestructure or /locatebiome for positioning.

- Use /tp \<teammates> to teleport to teammates.

# Installation Guide

1. Set up a Paper 26.2 server running Java 25.

2. Download the plugin and place it in the `plugins` folder in the server directory.

3. (**Recommended**) Set the `spawn-protection` value to 0 in `server.properties` (to allow breaking blocks at the spawn point).

4. (**Recommended**) In the `server.properties` file, make the following changes:

```
pvp=false
seed=
```

It's recommended to disable PVP for a block-collecting immersion.

It's recommended to leave the seed empty, delete the `world`, `world_nether`, and `world_the_end` folders after each game to reset the seed.

You can also modify the server startup file (`start.bat`) for automatic restart and seed reset:

```
:start
java -Xmx4G -Xms4G -jar server.jar nogui
rd /s /q world
rd /s /q world_nether
rd /s /q world_the_end
timeout /nobreak /t 5
goto start
```

Remember to change `server.jar` to your server core file name and adjust memory allocation accordingly.

# Modifying Target Block Libraries

In the `plugins/BlockRacing` directory under the server folder, there are the following files:

```
EasyBlocks.txt - Simple block library
MediumBlocks.txt - Medium block library
HardBlocks.txt - Difficult block library
DyedBlocks.txt - Dyed block library
EndBlocks.txt - End dimension block library
config.yml - Configuration file
lang.yml - Language file
zh_cn.json - Translation file (Simplified Chinese)
en_us.json - Translation file (English)
```

You can freely modify the 5 block library files, but please note:

1. Only one block name per line.

2. Use the uppercase block namespace for block names.

3. Avoid duplicate blocks in the 5 block libraries.

4. No empty lines or spaces.

5. Do not modify file names or delete files.

6. Except for the required easy block library, the rest can be selectively enabled. Avoid modifying these 5 files unless necessary.

# Changes in Generation Weight for Blocks of Different Difficulties

Easy: Game progress from 0% to 100%, weight decreases from 100 to 20.

Medium: Game progress from 0% to 40%, weight increases from 20 to 60; game progress from 40% to 100%, weight stays at 60.

Hard: Game progress from 0% to 50%, weight increases from 1 to 20; game progress from 50% to 100%, weight increases from 20 to 60.

Dyed: Game progress from 0% to 100%, weight stays at 10.

End: When game progress exceeds the percentage of non-End dimension blocks in total blocks, weight is fixed at 60. If game progress is below the percentage of non-End dimension blocks in total blocks: game progress from 0% to 80%, weight is 0; game progress from 80% to 100%, weight increases from 0 to 60. (Reference data: Default block library, if all block difficulties are enabled, End dimension block percentage is about 2%, non-End dimension block percentage is about 98%).

# Command Reference

### /tp - Teleport

`/tp <player>`
- `<player>`: The name of the player to teleport to. Can only teleport to players on the same team.

**Note: The /tp command has been modified by the plugin. To use the original tp command, please enter /teleport or /minecraft:tp**

### /menu - Open Menu

`/menu [main|chest|waypoints|roll|locate|randomTP]`
- `main`: Open the main menu.
- `chest [1|2|3]`: Open the team chest menu or a specific team's chest.
- `waypoints [use <index>]`: Open the waypoints menu or use a specific waypoint.
- `roll`: Execute a roll operation.
- `locate`: Buy locate command permission.
- `randomTP`: Perform a random teleportation.

### /locatebiome - Locate Biome

`/locatebiome <biome>`
- `<biome>`: The name of the biome to locate.

### /locatestructure - Locate Structure

`/locatestructure <structure>`
- `<structure>`: The name of the structure to locate.

### /restart - Restart Server

`/restart`
- Shutdown the server and restart it after all players confirm the restart.

### /getblock - Get Block Information

`/getblock <red|blue> <index>`
- `<red|blue>`: The team color.
- `<index>`: The block index (1, 2, 3, or 4).

### /waypoint - Manage Waypoints

`/waypoint remove <index>`
- `<index>`: The index of the waypoint to remove (1, 2, or 3).

### /debug - Debug Commands (Admin Only)

`/debug reload`
- Reload game messages and block information, and reload blocks during gameplay.

`/debug skip <team> [block number|all]`
- Skip specific block tasks for the specified team, or skip all block tasks.

`/debug setscore <team> <score>`
- Set the score for a specified team.

`/debug getblock <team> <type>`
- Retrieve block information for a specified team, including remaining and all blocks.

`/debug gettranslation <team> <block number>`
- Get translation information and Minecraft key for a specified block in the specified team.

`/debug getteam`
- Get the list of players in the current red and blue teams.

`/debug setteam <team> <add|remove> <player>`
- Add or remove a specified player from the specified team.

# Feedback

Game feedback: lq_snow@outlook.com

Contact:

> Email: lq_snow@outlook.com
>
> QQ: 2784628010

# License

This project is licensed under the [**GNU Affero General Public License v3.0**](https://github.com/LQSnow/BlockRacing/blob/main/LICENSE).
