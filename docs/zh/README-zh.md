[English](../../README.md) | [简体中文](./README-zh.md)

[Translation Tutorial](../en/TranslationTutorial-en.md) | [翻译教程](../../TranslationTutorial.md)

# 方块竞速BlockRacing

这是一个Minecraft多人竞速小游戏，分为两个队伍，先收集完指定方块的队伍获胜。

版本：Java 26.2（需要 Java 25）

运行要求：Paper 26.2、Java 25。26.2.1 起直接使用 Paper API，不再支持 Spigot。

## 升级须知

仅替换插件 JAR 不会自动更新已有配置文件。旧版 `config.yml` 和 `lang.yml` 的版本号如果与插件不一致，游戏内会显示警告，并且新设置或新文本可能无法正常工作。

升级前请备份 `plugins/BlockRacing`，然后选择一种方式：

1. 没有自定义配置：删除旧的 `config.yml`、`lang.yml`、五个方块库和两个翻译 JSON，重启服务器自动生成当前版本文件。
2. 有自定义配置：将自定义内容手动合并到当前版本的默认文件中，不要只修改文件末尾的版本号。

当前版本的默认配置和资源文件可在 [`src/main/resources` 目录](../../src/main/resources) 查看。

# 特色功能

1. 队伍箱子：每个队伍都有3个队伍箱子，箱子里的物品对同队伍成员共享。

2. 奖励机制：每收集一个方块，对方队伍将会获得一组该方块（存放在队伍箱子里）。

3. 队伍TP：同队伍之间可以自由TP。

4. 轮换方块：当前所需方块太难获取时，可以轮换掉，每局仅限三次。

5. 定位：玩家可以花费队伍积分购买locate指令，用于定位群系或结构。

6. 记录点：每个队伍有3个记录点，可以自由保存、传送、删除。

7. 玩家独立语言：聊天提示、标题、菜单、队伍前缀、方块名称和计分板均可跟随 Minecraft 客户端语言，也可以手动切换英文或简体中文。

# 玩法说明

## 准备阶段

- 进入游戏后，按Shift+F打开菜单。
- 在菜单进行选队。
- 菜单中可以设置目标方块库，可以选择性开启中等难度方块、困难难度方块、染色方块和末地方块。
- 菜单中可以设置目标方块数量。
- 菜单中可以切换模式（普通模式或竞速模式，可选开启极速模式）
- 所有玩家准备后，即可在菜单点击钻石开始游戏。
- 所有玩家输入/restartgame可以关闭服务器（通过后续设置可以实现自动重启）。

## 游戏阶段

- 随机传送后，按照记分板上的内容，开始收集方块吧。
  
- Shift+F可以打开菜单，在菜单里可以使用队伍箱子、Roll、定位、记录点、随机传送功能。
  
- 购买定位权限后，可以使用/locatestrcture或/locatebiome进行定位。
  
- 输入/tp \<teammates>可以TP队友。


# 安装教程

1. 准备一个运行 Java 25 的 Paper 26.2 服务器（如果不会，可以去看我的博客里的相关文章，网址lqsnow.top）

2. 下载插件，将插件放到服务器目录下的`plugins`文件夹中

3. （**推荐**）将`server.properties`中`spawn-protection`的值改为0（避免出生点无法破坏方块）

4. （**推荐**）在`server.properties`文件中，更改如下设置：

   ```
   pvp=false
   seed=
   ```

   推荐关闭PVP，让玩家沉浸于方块收集。

   推荐将种子留空，玩完一局后将`world` `world_nether` `world_the_end`三个文件夹删除，起到重置种子的作用。

   你也可以更改服务器启动文件（start.bat）以自动重启、自动重置种子（seed留空就是随机种子）：

   ```
   :start
   java -Xmx4G -Xms4G -jar server.jar nogui
   rd /s /q world
   rd /s /q world_nether
   rd /s /q world_the_end
   timeout /nobreak /t 5
   goto start
   ```

   记得修改server.jar为你的服务器核心文件名，并按实际情况分配内存。

# 目标方块库修改

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
   
6. 游戏内除了简单方块库必选以外，剩下的都可以选择性开启，如非特殊需要不要动这5个文件

# 不同难度方块的生成权重变化

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
- `chest [1|2|3]`: 打开队伍箱子菜单或指定队伍箱子。
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
- 在所有玩家确认重启之后，关闭服务器并重新启动。

### /block - 获取方块信息

`/block <red|blue> <index>`
- `<red|blue>`: 队伍颜色。
- `<index>`: 方块索引（1, 2, 3 或 4）。

### /waypoint - 管理路径点

`/waypoint remove <index>`
- `<index>`: 要删除的路径点索引（1, 2 或 3）。

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

游戏反馈：lq_snow@outlook.com

联系方式：

>  邮箱：lq_snow@outlook.com
> 
>  QQ：2784628010

# 版权说明

该项目签署 [**GNU Affero General Public License v3.0**](../../LICENSE) 授权许可

The project is licensed under the [**GNU Affero General Public License v3.0**](../../LICENSE)
