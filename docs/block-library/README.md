# 方块库

方块库包含 1,094 个生存可获得的方块，按主要获取维度和 T1–T6 分级。普通物品、创造专用方块、无法取得的方块状态以及影响双队公平性的龙蛋不作为目标。

## 设置入口

准备阶段使用 `/menu` 或 Shift+F 打开菜单，再进入“难度 / 方块库”。休闲、简单、标准、困难、噩梦、自定义为六个单选项，自定义旁边的按钮用于编辑详细设置。

默认使用标准难度、每队 50 个目标，铜附属方块关闭。

- [难度预设与铜附属开关](preset-guide.md)
- [自定义维度、等级和本地文件](custom-guide.md)
- [阶段难度与探索节奏](pacing-guide.md)

## 分级

|等级|含义|典型获取条件|
|---|---|---|
|T1|基础|常见资源，几乎无需准备|
|T2|常见|普通采矿、基础合成或熔炼|
|T3|探索|常见群系、特色资源、适度探索|
|T4|进阶|特定结构、群系或较复杂材料链|
|T5|稀有|特殊采集条件、稀有资源或较高投入|
|T6|极限|极低概率、长等待或很高成本|

T 级决定抽取范围和阶段门槛。记分板与积分另分为简单（T1–T2）、中等（T3）、困难（T4–T5）、噩梦（T6）。维度表示方块的主要获取来源，部分方块也有其他获取途径。

## 服务器文件

```text
plugins/BlockRacing/
  config.yml
  presets/
    casual.yml
    easy.yml
    normal.yml
    hard.yml
    nightmare.yml
  block-pools/
    overworld/tier-1.txt ... tier-6.txt
    nether/tier-1.txt ... tier-6.txt
    end/tier-1.txt ... tier-6.txt
    material-metadata.tsv
    acquisition-tags.tsv
    copper-basic.txt
    copper-extras.txt
  custom/
    settings.yml
    blocks.yml
```

TXT 每行一个标准 ID，例如 `minecraft:oak_log`。分类、可获得性和获取标签来自元数据文件；普通预设只接受已经列入目录的材料。未知 ID、重复条目和不匹配的分类会被拒绝。

修改标准预设后，在准备阶段执行 `/debug reload` 或重启服务器。修改本地自定义列表后，重新点击“使用本地自定义方块配置”进行读取和校验。

## 进行中的比赛

开局会保存本局候选、权重、解锁门槛和任务序列。修改配置不会改变正在进行的比赛，恢复比赛也使用保存的规则。

旧版 `EasyBlocks.txt` 等五份 TXT 仅用于旧对局兼容，不控制新的五个标准预设。
