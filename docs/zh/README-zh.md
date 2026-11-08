[English](../../README.md) | [简体中文](./README-zh.md)

[Translation Tutorial](../en/TranslationTutorial-en.md) | [翻译教程](../../TranslationTutorial.md)

# 方块竞速BlockRacing

这是一个Minecraft多人竞速小游戏，分为两个队伍，先收集完指定方块的队伍获胜。

插件版本：26.3.0

版本号以正式发布为更迭基准；26.3.0 尚未发布期间的开发改动统一使用 26.3.0，正式发布新版本时再递增。

运行要求：Minecraft Java 版 26.3、Paper 26.3、Java 25。插件直接使用 Paper API，不支持 Spigot。

新的“维度 × 等级”方块库与审核报告见[方块库说明](../block-library/README.md)。新比赛已接入[五个标准难度与独立铜附属开关](../block-library/preset-guide.md)，默认标准难度、每队 50 个目标、铜附属关闭。旧五份方块库保留用于恢复旧进行中对局。

## 升级须知

仅替换插件 JAR 不会自动更新已有配置文件。旧版 `config.yml` 和 `lang.yml` 的版本号如果与插件不一致，游戏内会显示警告，并且新设置或新文本可能无法正常工作。

更新插件时务必重新生成配置文件夹：

1. 先停服并完整备份 `plugins/BlockRacing`，包括对局存档、本地自定义库、预设及玩家语言偏好。需要恢复对局时同时保留匹配的世界。
2. **删除 `plugins/BlockRacing` 配置文件夹**，替换插件 JAR 后重启，让插件生成新版本的配置、菜单文字及分类文件。
3. 对照备份将自定义内容按新格式合并回去。不要整份覆盖回旧 `config.yml` 或旧语言文件，也不要只改版本号。确需恢复进行中对局时，从备份中单独恢复存档，并保留该局匹配的世界和原有规则。

配置或任一语言文件版本不匹配时，控制台和玩家登录提示都会提醒备份、删除配置文件夹并重新生成。插件不会自动删除数据。

当前版本的默认配置和资源文件可在 [`src/main/resources` 目录](../../src/main/resources) 查看。

更新检查会在开服后及每 6 小时后台读取本仓库最新的 GitHub 正式 Release，仅当插件版本更新时提示控制台和管理员（`blockracing.update-notify` 权限，默认 OP）。没有更新或请求失败时静默；`check-updates: false` 可关闭，不自动下载插件。

本机离线测试服的启动、停服和管理员授权见[测试服说明](../../tools/test-server/README.md)。

“难度 / 方块库”界面提供五个标准难度与“自定义”六项单选，直接显示当前启用项，旁边单独提供[详细设置](../block-library/custom-guide.md)。修改自动保存，各 Tier 显示实际方块数和代表性示例；详细界面可切换铜附属。选择本地文件模式后，方块与各维度 `unlock-percent` 均读取 `plugins/BlockRacing/custom/blocks.yml`，不受界面过滤或铜开关影响。

# 特色功能

所有维度的目标使用统一的展示标签与积分分组。展示分组不改变方块原有 T 级，实际抽取遵循下文的阶段节奏：

| 标签 | 原等级 | 普通得分 | 极速模式得分 |
|---|---|---:|---:|
| 简单 | T1–T2 | 1 | 3 |
| 中等 | T3 | 2 | 4 |
| 困难 | T4–T5 | 3 | 5 |
| 噩梦 | T6 | 4 | 6 |

普通与竞速模式按同一表计分；普通模式还会给对方队伍一组方块。游戏内规则书和极速模式说明已同步。

游戏主菜单右下角可切换“菜单位置保留”，默认关闭，按玩家独立设置。开启后 Shift+F 与 `/menu` 都会继续上次使用的界面，包括具体的队伍箱子、记录点、队友选择和语言菜单；返回箭头回上一级，队伍箱子返回箱子选择页。`/menu main` 始终进入主菜单。开关保存在 `menu-preferences.yml`，新一局或服务器重启后重新记录位置；队伍变化或箱子编号失效时回到主菜单。

新比赛使用[完整的阶段节奏规则](../block-library/pacing-guide.md)：开局只开放 T1–T2，完成进度达到 15%／35%／60%／80% 后依次开放 T3／T4／T5／T6，同时调整等级概率；预设范围、维度门槛仍取交集，重抽不能绕过阶段。现有对局保留存档原规则，新一局使用新节奏。本地文件模式可在自己的 `tier-progression` 段单独决定是否启用。

队伍箱子右下角保留一个受保护的返回按钮，提供 53 个储物格。旧存档该格的物品会保留在存档中，出现空位后自动移回储物格，等待期间仍参与目标检测，不会被返回箭头覆盖。

1. 队伍箱子：每个队伍拥有共享存储，数量可通过 `max-team-chest-num` 配置。

2. 奖励机制：每收集一个方块，对方队伍将会获得一组该方块（存放在队伍箱子里）。

3. 队伍TP：同队伍之间可以自由TP。

4. 轮换方块：队伍可投票替换当前难以获取的目标，菜单直接显示本队剩余次数；成功后扣次数，失败不扣，每局上限可通过 `max-roll-count` 配置。

5. 定位：玩家可以花费队伍积分购买locate指令，用于定位群系或结构。

6. 记录点：每个队伍可共享保存、传送和删除记录点，数量可通过 `max-team-waypoint-num` 配置。

7. 玩家独立语言：聊天提示、标题、菜单、队伍前缀、方块名称和计分板均可跟随 Minecraft 客户端语言，也可以手动切换英文或简体中文。

8. 队伍聊天：游戏期间默认仅队友可见，可使用 `/shout`、`@` 或 `!` 发送全服消息。

9. 崩服恢复：异常停服后可恢复未结束比赛的分数、目标、队伍箱子和记录点。

10. 队伍工具：菜单提供随机平均分队、队友传送和极速模式团队补给。

# 玩法说明

## 准备阶段

- 进入游戏后，按Shift+F打开菜单。
- 在菜单进行选队。
- 菜单中打开“难度 / 方块库”，选择休闲、简单、标准、困难或噩梦；可独立开关铜附属方块。
- 自定义入口支持各维度独立启停、任意等级组合及本地配置文件。噩梦仍只包含生存方块，不加入工具、食物等普通物品。
- 菜单中可以设置目标方块数量。
- 菜单中可以切换模式（普通模式或竞速模式，可选开启极速模式）
- 所有玩家准备后，即可在菜单点击钻石开始游戏。
- 上一局结束后，全体玩家可确认 `/restartgame`，在服务器下次启动时重置世界。
- 游戏进行期间默认使用队伍聊天；使用 `/shout <消息>`，或以 `@`、`!` 开头可发送全服消息。
- 未结束的比赛会保存到 `game-progress.yml`，服务器异常退出后会自动恢复。

## 游戏阶段

- 随机传送后，按照记分板上的内容，开始收集方块吧。
  
- Shift+F可以打开菜单，在菜单里使用队伍箱子、轮换、定位、记录点、队友传送、随机传送和极速模式补给。
  
- 购买定位权限后，可以使用 `/locatestructure` 或 `/locatebiome` 进行定位。
  
- 输入/tp \<teammates>可以TP队友。


# 安装教程

1. 准备一个运行 Java 25 的 Paper 26.3 服务器（如果不会，可以去看我的博客里的相关文章，网址lqsnow.top）

2. 下载插件，将插件放到服务器目录下的`plugins`文件夹中

3. （**推荐**）将`server.properties`中`spawn-protection`的值改为0（避免出生点无法破坏方块）

4. （**推荐**）在`server.properties`文件中，更改如下设置：

   ```
   pvp=false
   level-seed=
   ```

   推荐关闭PVP，让玩家沉浸于方块收集。

   如果希望每局使用随机世界，推荐将种子留空。

   全员确认 `/restartgame` 后，插件会安全关闭 Paper 并记录这是一次主动重置。
   下次启动时，插件会在默认世界打开前把旧世界移动到带时间的
   `world-backups` 目录，再由 Paper 生成新世界。崩服不会产生重置请求，
   因此会原样保留世界并恢复比赛进度。

   `start.bat` 不再需要任何删除世界的命令。如果面板本身会自动拉起已停止或
   崩溃的服务器，无需修改脚本。本地服务器只需要普通的自动重启循环：

   ```bat
   :start
   java -Xmx4G -Xms4G -jar server.jar nogui
   timeout /nobreak /t 5
   goto start
   ```

   记得修改server.jar为你的服务器核心文件名，并按实际情况分配内存。

# 旧方块库（用于旧对局恢复）

新对局使用[方块库说明](../block-library/README.md)中的维度 × 等级目录和预设文件。以下五份 TXT 保留用于旧对局，修改它们不会改变五个标准难度。

在服务器文件夹下的plugins\BlockRacing目录中，存在下面这几个文件：

```
EasyBlocks.txt 简单方块库
MediumBlocks.txt 中等方块库
HardBlocks.txt 困难方块库
DyedBlocks.txt 染色方块库
EndBlocks.txt 末地方块库
config.yml 配置文件
lang.yml 语言文件
languages/en_us/lang.yml 英文语言文件
language-preferences.yml 玩家语言偏好
zh_cn.json 翻译文件
en_us.json 翻译文件
```

默认 `config.yml` 使用英文注释，且不再控制玩家语言。玩家请在游戏中使用
`/language`；自定义语言文字可编辑 `lang.yml` 和 `languages/en_us/lang.yml`。

5个方块库文件你可以自由修改，但请注意：

1. 每行只写一个方块名
   
2. 方块名使用全大写的方块命名空间
   
3. 5个方块库中不要有重复方块
   
4. 不要有空行、空格等字符
   
5. 不要修改文件名，不要删除文件
   
6. 旧版对局中的开关只影响旧库；新对局通过“难度 / 方块库”选择预设。

# 旧版生成权重

以下仅用于旧存档兼容。新对局使用[维度门槛与等级权重](../block-library/preset-guide.md)，以及[获取标签的阶段权重](../block-library/pacing-guide.md)。

简单：游戏进程从0%到100%，权重从100减小到20。

中等：游戏进程从0%到40%，权重从20增加到60；游戏进程从40%到100%，权重保持60不变。

困难：游戏进程从0%到50%，权重从1增加到20；游戏进程从50%到100%，权重从20增加到60。

染色：游戏进程从0%到100%，权重保持10不变。

末地：当游戏进程超过非末地方块在总方块的占比时，权重固定为60。当游戏进程未达到非末地方块在总方块的占比时：游戏进程从0%到80%，权重为0；游戏进程从80%到100%，权重从0增加到60。（参考数据：默认方块库，如果所有方块难度全部启用，末地方块占比约为2%，非末地方块占比约为98%）

# 指令对照表

### /language - 切换语言

`/language [auto|en_us|zh_cn]`
- 不带参数时打开语言选择菜单。
- `auto`：跟随玩家的 Minecraft 客户端语言。
- `en_us` / `zh_cn`：固定使用英文或简体中文。

### /tp - 传送

`/tp <player>`
- `<player>`: 要传送到的玩家名称。只能传送到同队玩家。

**注意，/tp命令已被插件修改，如果想使用原版tp的指令，请输入/teleport或/minecraft:tp**

### /menu - 打开菜单

`/menu [main|chest|waypoints|roll|locate|randomTP]`
- `main`: 打开主菜单。
- `chest [index]`: 打开队伍箱子菜单或指定队伍箱子。
- `waypoints [use <index>]`: 打开路径点菜单或使用指定路径点。
- `roll`: 执行轮换操作。
- `locate`: 购买定位指令使用权限。
- `randomTP`: 随机传送。

### /locatebiome - 定位生物群系

`/locatebiome <biome>`
- `<biome>`: 要定位的生物群系名称。

### /locatestructure - 定位结构

`/locatestructure <structure>`
- `<structure>`: 要定位的结构名称。

### /restartgame - 重启服务器

`/restartgame`

- 全员确认后标记世界重置并安全关闭服务器；需要由服务器面板或启动循环再次启动 Paper。
- 恢复了上一局进度但确认上一局已经结束时，全员通过该指令也会清除保存的进度。

### /shout - 全服聊天

`/shout <消息>`

- 仅在游戏进行期间使用，高亮发送给所有玩家；也可以直接用 `@消息` 或 `!消息`。

### /randomteam - 随机分队

`/randomteam [confirm]`

- 游戏开始前将全部在线玩家随机、平均分配到两个队伍。
- 可以点击聊天确认按钮，或使用 `/randomteam confirm` 完成确认。

### /block - 获取方块信息

`/block <red|blue> <index>`
- `<red|blue>`: 队伍颜色。
- `<index>`: 方块索引（1, 2, 3 或 4）。

### /waypoint - 管理路径点

`/waypoint remove <index>`
- `<index>`: 要删除的记录点编号，上限由配置文件决定。

### /debug - 调试命令（需要管理员权限）

`/debug reload`
- 重新加载游戏消息和方块信息，并在游戏进行中重载方块。

`/debug skip <team> [block number|all]`
- 跳过指定队伍的指定方块任务，或跳过全部方块任务。

`/debug setscore <team> <score>`
- 设置指定队伍的分数。

`/debug getblock <team> <type>`
- 查询指定队伍的方块信息，包括剩余方块和所有方块。

`/debug gettranslation <team> <block number>`
- 获取指定队伍的指定方块的翻译信息和 Minecraft 中的键。

`/debug getteam`
- 获取当前红蓝队伍的玩家列表。

`/debug setteam <team> <add|remove> <player>`
- 将指定玩家添加到或从指定队伍移除。

# 意见反馈

请通过 [GitHub Issues](https://github.com/LQSnow/BlockRacing/issues) 报告问题或提出功能建议。

联系方式：

>  Discord：`ikarion1`
> 
>  QQ：2784628010

# 版权说明

该项目签署 [**GNU Affero General Public License v3.0**](../../LICENSE) 授权许可

The project is licensed under the [**GNU Affero General Public License v3.0**](../../LICENSE)
