[English](./README.md) | [简体中文](./docs/zh/README-zh.md)

[Translation Tutorial](./docs/en/TranslationTutorial-en.md) | [翻译教程](./TranslationTutorial.md)

# BlockRacing

This is a multiplayer racing mini-game in Minecraft, divided into two teams, where the team that collects the specified blocks first wins.

Plugin version: 26.2.1

Requirements: Minecraft Java Edition 26.2, Paper 26.2, and Java 25. BlockRacing uses Paper APIs directly and does not support Spigot.

## Upgrade Notice

Replacing only the plugin JAR does not update existing configuration files. If the versions in an old `config.yml` or `lang.yml` do not match the plugin, an in-game warning is displayed and new settings or messages may not work correctly.

Back up `plugins/BlockRacing` before upgrading, then choose one method:

1. No customizations: delete the old `config.yml`, `lang.yml`, five block pools, and two translation JSON files, then restart the server to generate current files.
2. Customized files: manually merge your changes into the current default files. Do not change only the version number at the end of a file.

See the current default configuration and resource files in the [`src/main/resources` directory](./src/main/resources).

# Features

1. Team Chests: Each team has shared storage. The chest count is configurable with `max-team-chest-num`.

2. Reward Mechanism: Collecting a block grants the opposing team 64 blocks of that block (stored in the team chests).

3. Team Teleportation: Players within the same team can freely teleport to each other.

4. Roll Blocks: A team can vote to replace difficult current targets. The per-game limit is configurable with `max-roll-count`.

5. Locate: Players can spend team points to purchase the locate command for finding biomes or structures.

6. Waypoints: Each team can save, teleport to, and delete shared waypoints. Their count is configurable with `max-team-waypoint-num`.

7. Per-player languages: Messages, titles, menus, team prefixes, block names, and scoreboards can follow the Minecraft client language or be switched manually between English and Simplified Chinese.

8. Team Chat: During a game, messages are team-only by default, with `/shout`, `@`, and `!` available for global chat.

9. Recovery: Unfinished games, scores, targets, team storage, and waypoints are restored after an unexpected shutdown.

10. Team Utilities: The menus include balanced random teams, teammate teleportation, and purchasable Speed Mode supplies.

# Gameplay Instructions

## Preparation Phase

- After entering the game, open the menu with Shift+F.
- Choose a team in the menu.
- Set the target block library in the menu, optionally enabling medium difficulty blocks, hard difficulty blocks, dyed blocks, and End dimension blocks.
- Set the target block quantity in the menu.
- Switch between modes (normal mode or racing mode, with an option to enable extreme speed mode).
- Once all players are ready, click the diamond in the menu to start the game.
- After a finished game, all players can approve `/restartgame` to reset the worlds on the next server startup.

## Game Phase

- After random teleportation, follow the scoreboard and start collecting blocks.

- Shift+F opens the menu, where players can use team chests, roll, locate,
  waypoints, teammate teleportation, random teleportation, and Speed Mode supplies.
- Team chat is used by default while a game is active. Use `/shout <message>` or
  begin a message with `@` or `!` to send it to everyone.
- Unfinished games are saved to `game-progress.yml` and restored automatically
  after an unexpected server stop.

- After purchasing locate permissions, use /locatestructure or /locatebiome for positioning.

- Use /tp \<teammates> to teleport to teammates.

# Installation Guide

1. Set up a Paper 26.2 server running Java 25.

2. Download the plugin and place it in the `plugins` folder in the server directory.

3. (**Recommended**) Set the `spawn-protection` value to 0 in `server.properties` (to allow breaking blocks at the spawn point).

4. (**Recommended**) In the `server.properties` file, make the following changes:

```
pvp=false
level-seed=
```

It's recommended to disable PVP for a block-collecting immersion.

Leave the seed empty if each new game should use a random world.

After an approved `/restartgame` vote, BlockRacing shuts Paper down and records
an intentional reset. On the next startup, the plugin runs before the default
world is opened, moves the old worlds into a dated `world-backups` folder, and
lets Paper generate fresh worlds. A crash creates no reset request, so the
existing worlds and saved game progress are restored unchanged.

No world-deletion commands are required in `start.bat`. If the hosting panel
already restarts stopped/crashed servers, no script change is needed. A simple
local auto-restart loop is enough:

```bat
:start
java -Xmx4G -Xms4G -jar server.jar nogui
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
languages/en_us/lang.yml - English language file
language-preferences.yml - Saved player language preferences
zh_cn.json - Translation file (Simplified Chinese)
en_us.json - Translation file (English)
```

The default `config.yml` comments are written in English. Player language is not
controlled by `config.yml`; use `/language` in game. Custom language text can be
edited in `lang.yml` and `languages/en_us/lang.yml`.

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

### /language - Change Language

`/language [auto|en_us|zh_cn]`
- With no argument, opens the language menu.
- `auto`: Follow the player's Minecraft client language.
- `en_us` / `zh_cn`: Always use the selected language.

### /tp - Teleport

`/tp <player>`
- `<player>`: The name of the player to teleport to. Can only teleport to players on the same team.

**Note: The /tp command has been modified by the plugin. To use the original tp command, please enter /teleport or /minecraft:tp**

### /menu - Open Menu

`/menu [main|chest|waypoints|roll|locate|randomTP]`
- `main`: Open the main menu.
- `chest [index]`: Open the team chest menu or a specific team chest.
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

### /restartgame - Restart Server

`/restartgame`
- After all players confirm, mark the worlds for reset and safely shut down the server. The hosting panel or startup loop must start Paper again.
- When a recovered game has actually ended, this vote also discards its saved progress.

### /shout - Global Chat

`/shout <message>`

- During an active game, sends a highlighted message to every player. `@message` and `!message` are shortcuts.

### /randomteam - Random Teams

`/randomteam [confirm]`

- Requests a balanced random assignment of all online players before the game.
- Confirmation can be completed with the clickable chat button or `/randomteam confirm`.

### /block - Get Block Information

`/block <red|blue> <index>`
- `<red|blue>`: The team color.
- `<index>`: The block index (1, 2, 3, or 4).

### /waypoint - Manage Waypoints

`/waypoint remove <index>`
- `<index>`: The configurable waypoint index to remove.

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

Report bugs and request features through [GitHub Issues](https://github.com/LQSnow/BlockRacing/issues).

Contact:

> Discord: `ikarion1`
>
> QQ: 2784628010

# License

This project is licensed under the [**GNU Affero General Public License v3.0**](./LICENSE).
