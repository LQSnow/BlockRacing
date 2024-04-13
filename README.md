[English](./docs/en/README-en.md) | [简体中文](./docs/zh/README-zh.md)

[Translation Tutorial](https://github.com/LQSnow/BlockRacing/blob/3.0/docs/en/TranslationTutorial-en.md) | [翻译教程](https://github.com/LQSnow/BlockRacing/blob/3.0/docs/zh/TranslationTutorial-zh.md)

# 方块竞速BlockRacing

这是一个Minecraft多人竞速小游戏，分为两个队伍，先收集完指定方块的队伍获胜。

版本：Java 1.20.2

# 特色功能

1. 队伍箱子：每个队伍都有3个队伍箱子，箱子里的物品对同队伍成员共享。

2. 奖励机制：每收集一个方块，对方队伍将会获得一组该方块（存放在队伍箱子里）。

3. 队伍TP：同队伍之间可以自由TP。

4. 轮换方块：当前所需方块太难获取时，可以轮换掉，每局仅限三次。

5. 定位：玩家可以花费队伍积分购买locate指令，用于定位群系或结构。

6. 记录点：每个队伍有3个记录点，可以自由保存、传送、删除。

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

1. 准备一个Paper服务器（也可以是Spigot或Purpur）（如果不会，可以去看我的博客里的相关文章，网址lqsnow.top）

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
zh_cn.json 翻译文件
en_us.json 翻译文件
```

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

# 更新日志

### 2024.4.13 - BlockRacing 3.1

- 修复了使用指令进行随机传送时不扣积分的Bug
- 游戏结束时会显示方块收集排行榜
- 添加了语言文件与配置文件的版本检查
- 添加了block指令，如果记分板的方块名显示不全，可以使用该指令查询完整方块名
- 移除了EasyBlocks中的GRASS
- 更新游戏版本至1.20.4

### 2024.1.30 - BlockRacing 3.0

- 重构了所有代码和执行逻辑
- 增加了语言文件与配置文件
- 修改了locate指令执行方式，拆分为两个指令
- 服务器开启时会自动读取配置文件，加载上一局的配置
- 不同难度方块在游戏的不同时期，生成权重将不一样，更有助于游戏推进
- 现在轮换方块改为队伍内所有**在线**玩家申请即可轮换
- 游戏开始前的准备可以取消了
- 玩家发言拥有队伍前缀
- 删除记录点需要在聊天框确认

### 2023.9.28 - BlockRacing 2.2.1

- 修复了极速模式下急迫效果等级不正确的Bug
- 修复了随机传送世界错误的Bug
- /locate指令追加1.20生物群系
- 极速模式下初始道具中鞘翅的RepairCost改为15
- 修改了部分物品描述错误
- /menu指令追加了更多功能
- 优化了部分代码和执行逻辑

### 2023.9.25 - BlockRacing 2.2

- 轮换方块次数改为3次，并且不再只能Roll到简单方块（[@BlockyDeer](https://github.com/BlockyDeer)）
- 添加极速模式，开局有额外物资和效果（[@xiaojiuwo233](https://github.com/xiaojiuwo233)）
- 更换了新的准备菜单GUI
- 游戏开始前限制世界边界防止提前探图
- 添加了指令/menu，可以通过指令打开各种菜单
- 修复了方块数量无法正常修改的Bug
- 修复了选队后退出游戏重进会被判定为旁观者的Bug
- 修复了死亡复活后药水效果丢失的Bug
- 优化了部分代码和执行逻辑
- 更新游戏版本至1.20.2

### 2023.9.18 - BlockRacing 2.1

- 玩家进入游戏将获得无限夜视效果
- 全队玩家全部申请轮换方块之后才会替换本队方块
- 添加了Debug命令（需要OP权限）
- 修复了玩家重进游戏能再次免费随机传送的Bug
- 修复了下界疣显示为Null的Bug
- 修复了部分显示错误
- 优化了部分代码和执行逻辑
- 更新游戏版本至1.19.4

# 意见反馈

游戏反馈：https://docs.qq.com/form/page/DU0Fvc0xtUmZWRUJN

联系方式：

>  邮箱：lq_snow@outlook.com
> 
>  QQ：2784628010
> 
>  博客：lqsnow.top

# 版权说明

该项目签署 [**GNU Affero General Public License v3.0**](https://github.com/LQSnow/BlockRacing/blob/main/LICENSE) 授权许可

The project is licensed under the [**GNU Affero General Public License v3.0**](https://github.com/LQSnow/BlockRacing/blob/main/LICENSE)
