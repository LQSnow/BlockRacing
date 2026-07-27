# Translation and Language Guide

[English](./TranslationTutorial-en.md) | [简体中文](../../TranslationTutorial.md)

BlockRacing 26.2.1 includes English and Simplified Chinese, with an independent
language preference for every player.

## Changing a Player's Language

- Run `/language` to open the language menu.
- `/language auto`: Follow the Minecraft client language. Chinese clients use
  Simplified Chinese; all other clients use English.
- `/language zh_cn`: Always use Simplified Chinese.
- `/language en_us`: Always use English.

Preferences are stored in `plugins/BlockRacing/language-preferences.yml`.
`config.yml` does not control player language. First-time players also receive a
clickable language-menu prompt in chat.

## Editing the Built-in Translations

The first server start creates:

- `lang.yml`: Simplified Chinese interface text.
- `languages/en_us/lang.yml`: English interface text.
- `zh_cn.json`: Simplified Chinese block names.
- `en_us.json`: English block names.

Edit these files directly, then run `/debug reload` or restart the server. Keep
the YAML structure, color codes such as `&a`, and placeholders such as
`%player%`.

Plugin upgrades do not overwrite existing translation files. Compare and merge
new entries from [`src/main/resources`](../../src/main/resources) and
[`en-us`](../../en-us).

## Adding Another Language

The in-game selector currently supports English and Simplified Chinese. Adding a
third language requires extending `LanguageManager`, the language menu, the
interface YAML, and the block-name JSON. Adding only a JSON file does not create
a new language option.
