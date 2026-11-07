# Paper 26.3 方块库审核与分类

已完成 API 升级、方块库数据重建，并接入[五个标准难度与铜附属开关](preset-guide.md)。新库使用标准命名空间 ID，按主世界／下界／末地 × T1–T6 分为 18 份 TXT。新比赛使用按完成进度补位的生成器；旧五份 TXT 和服务器上的用户自定义文件保留，旧进行中对局按原待完成队列恢复。

## 数据来源与覆盖范围

- Paper API：`io.papermc.paper:paper-api:26.3.build.244-stable`；Java 25。
- 官方服务端：[Paper 26.3 build 244](https://fill.papermc.io/v3/projects/paper/versions/26.3/builds/244)。
- 服务端 JAR SHA-256：`9601f9a26a2a52a0f2c807cf1abf691f96ee4fb287878313b601cebc2c51eabf`。
- 审核日期：2026-11-07。
- 在真实 Paper 服务端注册表完成引导后，调用 Bukkit `Material.values()`、`getKey()`、`isBlock()`、`isItem()`、`isAir()` 和 `ItemType.hasBlockType()`；没有用旧 TXT 或英文名称推测 API 候选。
- 离线工具还检查服务端默认 feature flags，本次没有默认关闭的实验性条目。它只初始化注册表，不创建世界、不监听端口、不启动 Minecraft 对局。
- 人工分类考虑常规获取链、工具、结构／群系、合成成本和等待。配方／掉落引用取自同一官方服务端 JAR 的 `data/minecraft/`；结构例外核对 `structure/trial_chambers/*.nbt`。掉落表中出现某个 ID 只是证据，不能单独证明该方块能在生存中取得：石化橡木台阶就是有自身掉落表但缺乏正常生成来源的反例。

| 审核环节 | 数量 |
|---|---:|
| API 非 Legacy Material | 1,815 |
| `isBlock()` 方块类型 | 1,286 |
| 空气或没有可持有 Material 物品的状态 | 158 |
| `isBlock && isItem && !isAir && !isLegacy` 初筛候选 | 1,128 |
| 正常生存不能取得的方块物品 | 32 |
| 小麦：同名 Material 的物品不是方块物品 | 1 |
| 龙蛋：可取得但世界唯一，因双队公平性排除 | 1 |
| 纳入 18 类方块库 | **1,094** |
| 没有审核记录的初筛候选 | **0** |

这一轮只建立普通方块库。非方块 Material（包括小麦物品）尚未制作“全物品”生存白名单，不能把原始 API 快照直接当成全物品候选。

## 分类统计

| 主要获取维度 | T1 | T2 | T3 | T4 | T5 | T6 | 合计 |
|---|---:|---:|---:|---:|---:|---:|---:|
| 主世界 | 24 | 218 | 367 | 235 | 89 | 22 | 955 |
| 下界 | 1 | 9 | 77 | 10 | 9 | 4 | 110 |
| 末地 | 1 | 5 | 5 | 1 | 16 | 1 | 29 |
| 合计 | 26 | 232 | 449 | 246 | 114 | 27 | **1,094** |

按交接文档的范围投影，铜附属开启时休闲候选 242、简单 252、标准 931、困难 1,067、噩梦 1,094；默认关闭铜附属后分别为 242、252、857、975、988。这些是候选容量，不是抽取权重或任务配额；实际比赛在四槽补位时执行进度门槛。标准包含主世界 T1–T4 和下界 T1–T3，涵盖不同木种、花草、硫磺洞穴、普通矿材加工、海底神殿材料、树脂、蜂箱、蛋糕和染色加工；高成本／精准采集原矿、长等待等保留在高等级。

## 文件与维护入口

- [完整分类清单](classification-complete.md)：按维度 × 等级列出全部 1,094 项的中文名称、标准 ID、获取方式与平衡复核标记。
- [API 原始快照](materials-api-26.3.csv)：全部 1,815 个现代 Material 的实际 API 标志与翻译 key。
- [方块完整审核表](block-audit.csv)：全部 1,286 个方块类型，含是否可持有、是否纳入、生存标志、维度、等级、原因及证据。
- [排除清单](excluded-blocks.csv)：192 项，含 158 个非收集状态和 34 个候选排除项。
- [排除清单中文核对版](excluded-blocks.md)：按中文名称、标准 ID、排除原因逐项核对。
- [分级统计](counts.csv)。
- [五难度使用及迁移说明](preset-guide.md)、[各难度铜开关容量](preset-summary.csv)、[铜分组清单](copper-options.md)。
- [宏观标签与节奏说明](pacing-guide.md)、[标签完整中文清单](acquisition-complete.md)、[标签分布统计](acquisition-counts.csv)。
- [待调优清单](balance-review.csv)：199 项，见下一节。
- 分类资源：`src/main/resources/block-pools/{overworld,nether,end}/tier-{1..6}.txt`。
- 维度／等级与可获得性的人工维护源：`src/main/resources/block-pools/material-metadata.tsv`；主要获取标签的人工源为同目录 `acquisition-tags.tsv`。TXT／报告由脚本生成。TSV 为 UTF-8，字段不可包含制表符或换行；`flags` 与 `evidence` 内部用分号分隔。

元数据列为：`id`、`status`、`survival_obtainable`、`dimension`、`tier`、`reason`、`flags`、`evidence`。`status=excluded` 的条目不分配维度／等级。`survival_obtainable` 指对应 Material 物品能否正常持有，故龙蛋和小麦仍为 `true`，并由原因区分排除理由。`manual/vanilla_survival_rules` 表示创造专属／技术性方块的人工规则判断，其他证据路径均相对于官方 JAR 的 `data/minecraft/`。

中文核对版沿用项目本地化方式：以 API 快照中的 `translation_key` 查询 `src/main/resources/zh_cn.json`，同时兼容 `block.minecraft.*` 与 `item.minecraft.*`，不从枚举名拼接翻译 key。本次仅补入原语言包缺少的 35 个 key，译名来自 [Minecraft 26.3 官方简体中文资源](https://resources.download.minecraft.net/bd/bd3b3df108c1eaf78b6b8904c9f0842a7b110d5c)，资源 SHA-1 为 `bd3b3df108c1eaf78b6b8904c9f0842a7b110d5c`。全部 1,286 个方块类型的实际 API 翻译 key 均有中文名称；已有译名保持原语言包内容。

## 获取路径中的重要例外

- 蜂箱 `beehive` 为主世界 T4，天然蜂巢 `bee_nest` 为主世界 T5；后者需要精准采集。
- 磁石在此版本使用铁锭与錾制石砖合成，归主世界 T3，不沿用旧的下界合金获取链。
- 小型垂滴叶在本版本的掉落表允许剪刀采集，归主世界 T3；不沿用“只能流浪商人购买”的旧结论。
- 大型蕨和高草方块物品依赖村庄箱子，归 T5。剪刀剪自然双层植株掉落较小植株，不能据此判为常见。
- 小／中／大型紫晶芽和紫水晶簇都可精准采集；紫水晶母岩不可取得。
- 试炼密室中的氧化铜基材可以直接采集，再刮除氧化或涂蜡获得多个中间状态。这些通常列入主世界 T4；没有该结构捷径的铜灯、铜栏杆、铜链、铜箱、铜灯笼、避雷针氧化／涂蜡状态仍按等待分入 T4–T6。铜傀儡雕像涉及傀儡氧化，列 T6。
- 铜灯有主世界试炼密室来源，所以不因烈焰棒合成配方而强制归下界。观察者、比较器、阳光探测器和红石灯以常规石英／荧石获取链归下界 T3，并记录主世界替代来源。
- 岩浆块在主世界海底峡谷也可采集；哭泣黑曜石可从主世界废弃传送门取得。不是所有名称含下界风格的材料都必须归下界。
- 潜影盒为末地 T4，16 色潜影盒为末地 T5，龙首为末地 T6；龙蛋独立排除。
- 不可获得的虫蚀方块、刷怪笼、试炼刷怪笼、宝库、强化深板岩、可疑沙／沙砾等都不会进入库。小麦还额外通过 `hasBlockType()` 排除，避免单凭 `isBlock && isItem` 的同名映射误入。

## 仍需人工确认的平衡判断

获取合法性与推荐 T 级是不同问题。全部候选已有纳入或排除决定；[待调优清单](balance-review.csv) 中的 199 项已给出初版 T 级，并标记为需要实际对局确认，不代表未知材料自动进入正式库：

1. 杨木及新植物，以及羊毛／混凝土台阶和楼梯：取得路径明确；确认群系搜索成本、染料差异及大量外观变体对抽取体验的影响。
2. 硫磺／朱砂系列：确认硫磺洞穴的实际寻找时间与加工物 T3／强效硫磺 T4 的相对投入。强效硫磺的自然来源见 `worldgen/feature/sulfur_pool.json`。
3. 试炼密室铜基材及可刮除氧化的衍生状态：确认把结构探索成本归 T4 的合理性。
4. 没有直接结构捷径的氧化／涂蜡铜，以及铜傀儡雕像：确认平均等待与 T5／T6 分界。

本轮没有进行 50 目标真实多人平衡实测，不能据候选数推断平均局长。后续预设的维度／等级权重应继续遵守交接文档，避免把候选数量直接当作概率。

## 重新导出、检查和维护

需要 Java 25（含源码启动器）与 PowerShell。下载对应官方 Paper JAR 后执行：

```powershell
./tools/material-audit/Export-Materials.ps1 `
  -PaperJar /absolute/path/paper-26.3-244.jar `
  -Java /absolute/path/java.exe `
  -Baseline ./docs/block-library/materials-api-26.3.csv `
  -ValidateLibrary
```

工具在 `.runtime/material-audit` 解包 Paper 所需依赖，首次可能下载 Mojang 服务端；按 Paper JAR 自带的 classpath 清单加载依赖，避免混用多个版本。默认输出 `materials-current.csv`；与基线对比显示新增、失效或 API 标志变化。`-ValidateLibrary` 使用真实 API 检查新候选、非法项和 18 类数据一致性，不把新材料自动分类。更新 API 后若需审核新增清单，可先省略 `-ValidateLibrary` 导出，再逐项补充元数据；更名没有静默别名替换。

```powershell
# 修改人工维护 TSV 后重建 TXT 和报告
./tools/material-audit/Rebuild-Pools.ps1

# 只检查，不写入文件
./tools/material-audit/Rebuild-Pools.ps1 -Check

# 用新版快照检查未审核／失效候选
./tools/material-audit/Rebuild-Pools.ps1 -Check -ApiSnapshot /absolute/path/materials-current.csv

mvn test
```

新增候选没有审核记录、旧候选失效、ID 非规范、重复、非方块物品或实验性物品都会使检查失败。当前 `Materials.require` 同时接受 `minecraft:oak_log` 和旧 TXT 的 `OAK_LOG` 格式；未知 ID、未知命名空间和 Legacy 条目明确报错。

## 验证记录

- API 升级后项目编译成功。
- 自动化测试包含原有玩法、全 API 快照覆盖、1,128 项审核覆盖、18 类精确对应、重复检查、非法目标、五难度范围和补位／重抽／恢复场景。
- 在真实 Paper 26.3 build 244 注册表重新导出并执行 `-ValidateLibrary` 成功；与保存快照没有差异。
- `Rebuild-Pools.ps1 -Check` 通过；生成的数据与人工源一致。
- 在真实 Paper 注册表模拟 20 组完整比赛，并检查旧 v1／兼容 v2 队列迁移和生产适配层恢复；详见[预设说明](preset-guide.md)。
- 插件版本为 26.3.0，API 声明为 26.3，打包产物位于 `target/BlockRacing-26.3.0.jar`。
