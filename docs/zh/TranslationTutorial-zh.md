# 翻译教程

游戏内所有呈现给玩家的语言文字一共分为两部分：

第一部分，在记分板上显示的方块名；

第二部分，除去方块名以外的其他提示文字（不包括/debug命令）。

游戏目前自带简体中文和英文版本。

如果你需要切换成其他语言，请跟随教程：

## 1. 语言文件翻译

首先，将`BlockRacing.jar`放入plugins文件夹中，启动一遍服务器，plugins文件夹内会多出一个`BlockRacing`文件夹，其中存储着所有方块竞速的游戏配置。其中，lang.yml就是语言文件，也就是上面提到的第二部分。

如果其中的语言并不是你需要的：

1. 游戏自带简体中文和英语的语言文件，可前往Github下载：

   英文版本：https://github.com/LQSnow/BlockRacing/tree/3.0/en-us

   简体中文版本：https://github.com/LQSnow/BlockRacing/tree/3.0/zh-cn

   将两个文件直接替换到`BlockRacing`文件夹中即可，记得重启服务器。

2. 如果你需要其他语言，可以先下载英文版本的文件，然后将其内容复制给ChatGPT，让他翻译即可，他会自动识别颜色代码和占位符。

注意！请一定将`config.yml`和`lang.yml`中的

```
lang: en_us
```

改成你的语言名！游戏会根据这项内容自动读取方块翻译文件！

## 2. 方块翻译

方块翻译文件就是`BlockRacing`文件夹中的`zh_cn.json`、`en_us.json`这样的文件，这个文件其实是游戏自带的翻译文件，可以直接从游戏里获取。

1. 首先，在客户端游戏目录里找到`.minecraft\assets\indexes`，打开`12.json`，该文件是1.20.x版本的索引文件，搜索你的语言代码，比如`zh_cn`，你就会找到这样一个键值对：

   ```
   "minecraft/lang/zh_cn.json": {"hash": "773e22552e5569ff4512a881c5afb7d5f6d7a091", "size": 421000}
   ```

   其中，`"hash"`的值，在这里是`773e22552e5569ff4512a881c5afb7d5f6d7a091`，就是游戏内该语言翻译文件的文件名，将其复制。

2. 在`.minecraft\assets\objects`文件夹中，搜索这个hash值，你就拿到了你的语言的翻译文件。将其复制到`BlockRacing`文件夹中，后缀改为`.json`，名字改为你的语言代码。

   **注意！文件名必须和上面语言文件以及配置文件中的`lang`的值是一致的！否则游戏将无法正常读取！**

## 3. 重启服务器

当`BlockRacing`文件夹中的配置全部就绪后，重启服务器，游戏就能自动加载相应的语言了。