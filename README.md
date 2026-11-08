[English](./README.md) | [简体中文](./docs/zh/README-zh.md)

[Translation Tutorial](./docs/en/TranslationTutorial-en.md) | [翻译教程](./TranslationTutorial.md)

# BlockRacing

This is a multiplayer racing mini-game in Minecraft, divided into two teams, where the team that collects the specified blocks first wins.

Plugin version: 26.3.0

Version numbers advance with official releases. Until 26.3.0 is published, all development changes remain under 26.3.0.

Requirements: Minecraft Java Edition 26.3, Paper 26.3, and Java 25. BlockRacing uses Paper APIs directly and does not support Spigot.

The dimension × tier catalogue and audit reports are documented in [block library](./docs/block-library/README.md). Fresh games use [five difficulty presets and an independent copper-variant switch](./docs/block-library/preset-guide.md), with Standard + 50 targets and copper variants off by default. Legacy pools are retained for unfinished older matches.

## Upgrade Notice

Replacing only the plugin JAR does not update existing configuration files. If the versions in an old `config.yml` or `lang.yml` do not match the plugin, an in-game warning is displayed and new settings or messages may not work correctly.

When upgrading, regenerate the configuration folder:

1. Stop the server and back up the entire `plugins/BlockRacing` folder, including saved matches, local custom pools, presets and player language preferences. Keep the matching worlds if restoring a match.
2. **Delete `plugins/BlockRacing`**, replace the plugin JAR, and restart to generate the new configuration and language files.
3. Review and merge custom values into the new files. Do not copy old `config.yml` or language files back wholesale, or merely change their version markers. If restoring an unfinished match, preserve its saved rules and matching worlds; restore the saved match deliberately from the backup.

The plugin warns in the console and on player login if the configuration or either language file is outdated. It does not automatically delete your data.

See the current default configuration and resource files in the [`src/main/resources` directory](./src/main/resources).

Version checks read this repository's latest official GitHub Release in the background at startup and every six hours. Only newer stable versions notify the console and administrators (permission `blockracing.update-notify`, default OP). Failed requests and up-to-date results are silent. Disable with `check-updates: false`; no automatic downloads occur.

For a local offline test server, see [test server setup](./tools/test-server/README.md).

The difficulty menu presents six mutually exclusive choices: the five standard presets and Custom, with a separate settings button beside Custom. Changes save automatically. Each T1–T6 selector shows its available block count and representative examples. The custom settings screen also has a copper-variant toggle for tier mode. Local-file mode reads both blocks and per-dimension `unlock-percent` from `plugins/BlockRacing/custom/blocks.yml`, independently of all GUI filters and the copper toggle. See the [custom pool guide](./docs/block-library/custom-guide.md) for file layout and validation.

# Features

Target labels and collection points share these display groups across all dimensions. The display groups do not change block classifications; generation follows the progressive rules below:

| Label | Original tiers | Points | Speed Mode points |
|---|---|---:|---:|
| Easy | T1–T2 | 1 | 3 |
| Medium | T3 | 2 | 4 |
| Hard | T4–T5 | 3 | 5 |
| Nightmare | T6 | 4 | 6 |

The in-game rule book and Speed Mode description include this scoring table. Both Normal and Racing use the same points; Normal also gives the opposing team a stack of the collected block.

The bottom-right button in the game menu toggles **Remember menu**, independently for each player (off by default). Shift+F and `/menu` both resume the last game menu, including a specific team chest, waypoints, teammate selection or language settings. Back arrows return to the parent menu; a team chest returns to the chest selector. `/menu main` always opens the main menu. The enabled preference is stored in `menu-preferences.yml`; remembered destinations reset on a new match or server restart. Invalid chest/team references fall back to the main menu.

New matches use [progressive difficulty](./docs/block-library/pacing-guide.md): T1–T2 at the opening, T3 at 15% completed, T4 at 35%, T5 at 60%, and T6 at 80%, always intersected with the chosen preset and dimension gates. Tier weights shift from development to exploration and later challenges; rerolls obey the same gates. The reroll button shows remaining uses for your team and refreshes after a successful reroll. Existing matches keep their saved rules. Local-file mode controls this separately through its own optional `tier-progression` section.

Team chests reserve their bottom-right slot for a protected back arrow, leaving 53 storage slots. A legacy item saved in that slot is retained through saves and restores, and moves into storage when a slot becomes free; it still counts for target detection while waiting.

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
- Open Difficulty / Block library to choose Casual, Easy, Standard, Hard or Nightmare; toggle copper variants independently.
- Open Custom block settings to select dimensions and tiers or use a local custom pool. Nightmare remains a block-only mode; tools and food are not targets.
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

1. Set up a Paper 26.3 server running Java 25.

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

# Legacy Block Libraries (Saved-Match Recovery)

New matches use the reviewed dimension / tier pools and preset files described in the [block library guide](./docs/block-library/README.md). The five files below are retained for legacy matches; editing them does not change the five standard presets.

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

6. Legacy switches apply only to legacy matches. Choose new-match presets through Difficulty / Block library.

# Legacy Generation Weights

These weights apply to legacy saved matches only. New-match generation uses [dimension thresholds and tier weights](./docs/block-library/preset-guide.md) together with [acquisition phase weights](./docs/block-library/pacing-guide.md).

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
