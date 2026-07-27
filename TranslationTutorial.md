# 翻译与语言教程

[English](./docs/en/TranslationTutorial-en.md) | [简体中文](./TranslationTutorial.md)

BlockRacing 26.2.1 内置简体中文和英文，并允许每位玩家独立选择语言。

## 玩家切换语言

- 输入 `/language` 打开语言菜单。
- `/language auto`：跟随 Minecraft 客户端语言。中文客户端使用简体中文，其他客户端使用英文。
- `/language zh_cn`：固定使用简体中文。
- `/language en_us`：固定使用英文。

玩家选择保存在 `plugins/BlockRacing/language-preferences.yml`，不需要修改
`config.yml`。首次进入服务器时，聊天框也会显示可点击的语言菜单提示。

## 修改现有翻译

服务器首次启动后会生成：

- `lang.yml`：简体中文界面文字。
- `languages/en_us/lang.yml`：英文界面文字。
- `zh_cn.json`：简体中文方块名称。
- `en_us.json`：英文方块名称。

可以直接修改这些文件，然后执行 `/debug reload` 或重启服务器。请保留 YAML
层级、颜色代码（例如 `&a`）和占位符（例如 `%player%`）。

升级插件时不会覆盖已经存在的翻译文件。请对照仓库中的
[`src/main/resources`](./src/main/resources) 和 [`en-us`](./en-us)
手动合并新增文本。

## 增加其他语言

当前游戏内菜单正式支持简体中文和英文。若要增加第三种语言，需要同时扩展
`LanguageManager` 的语言选项、语言菜单、界面 YAML 和方块名称 JSON；仅添加一个
JSON 文件不会自动出现新的语言选项。
