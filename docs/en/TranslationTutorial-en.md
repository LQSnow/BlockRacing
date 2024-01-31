# Translation Tutorial

All language texts presented to players in the game are divided into two parts:

The first part, the block names displayed on the scoreboard;

The second part, other prompt texts excluding block names (excluding the /debug command).

The game currently comes with Simplified Chinese and English versions.

If you need to switch to another language, follow this tutorial:

## 1. Language File Translation

First, place `BlockRacing.jar` into the plugins folder, start the server once, and a `BlockRacing` folder will be created in the plugins folder, storing all the game configurations for BlockRacing. Among them, lang.yml is the language file, which corresponds to the second part mentioned above.

If the language is not what you need:

1. The game comes with language files for Simplified Chinese and English, which can be downloaded from Github:

   English version: [https://github.com/LQSnow/BlockRacing/tree/3.0/en-us](https://github.com/LQSnow/BlockRacing/tree/3.0/en-us)

   Simplified Chinese version: [https://github.com/LQSnow/BlockRacing/tree/3.0/zh-cn](https://github.com/LQSnow/BlockRacing/tree/3.0/zh-cn)

   Replace the two files directly in the `BlockRacing` folder and remember to restart the server.

2. If you need another language, you can download the English version first, then copy its content to ChatGPT for translation. It will automatically recognize color codes and placeholders.

Note! Be sure to change the following lines in `config.yml` and `lang.yml` to your language code:

```
lang: en_us
```


The game will read the block translation file based on this content!

## 2. Block Translation

The block translation file is files like `zh_cn.json` and `en_us.json` in the `BlockRacing` folder. This file is actually the game's built-in translation file, and you can obtain it directly from the game.

1. First, in the client game directory, find `.minecraft\assets\indexes` and open `12.json`. This file is the index file for version 1.20.x. Search for your language code, such as `zh_cn`, and you will find a key-value pair like this:

    ```
    "minecraft/lang/zh_cn.json": {"hash": "773e22552e5569ff4512a881c5afb7d5f6d7a091", "size": 421000}
    ```

    The value of `"hash"`, here is `773e22552e5569ff4512a881c5afb7d5f6d7a091`, is the filename of the language translation file in the game. Copy it.

2. In the `.minecraft\assets\objects` folder, search for this hash value, and you will get the translation file for your language. Copy it to the `BlockRacing` folder, change the suffix to `.json`, and name it with your language code.

**Notice! The file name must be consistent with the value of `lang` in the above language file and configuration file! Otherwise the game will not read properly!**

## 3. Restart the Server

When all the configurations in the `BlockRacing` folder are ready, restart the server, and the game will automatically load the corresponding language.
