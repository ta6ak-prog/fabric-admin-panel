# Fabric Admin Panel

Server-side Fabric mod for Minecraft 1.21.x with event logging, player statistics, and **Telegram notifications**.

## Features

- **Telegram Notifications** — при входе/выходе игрока бот отправляет уведомление с ником, привилегией и статистикой
- **Event Logging** — логирует join/leave/chat/death/command в JSON-файлы по дням
- **Player Statistics** — playtime, блоки, мобы, смерти, дистанция, команды
- **Leaderboards** — `/admin top <field>` для топ-10 по любому полю
- **JSON Storage** — логи в `adminpanel/logs/`, статистика в `adminpanel/stats.json`

## Telegram Setup

1. Создай бота через [@BotFather](https://t.me/BotFather) → получи токен
2. Напиши боту `/start`
3. Открой `https://api.telegram.org/bot<ТВОЙ_ТОКЕН>/getUpdates` → найди `"chat":{"id":123456...}`
4. Заполни `adminpanel/config.json`:

```json
{
  "bot_token": "123456:ABC-DEF...",
  "chat_id": 123456789,
  "default_privilege": "Игрок"
}
```

### Пример уведомления:

```
🟢 Игрок зашёл на сервер

👤 Ник: Steve
⭐ Привилегия: Игрок

📊 Последняя сессия:
  ⏱ Наиграл: 20 мин
  ⛏ Блоков сломано: 142
  🗡 Мобов убито: 3
  🕐 Последний вход: 2026-07-08 18:30:00
```

## Commands

| Command | Description |
|---------|-------------|
| `/admin logs [count]` | Последние события (по умолчанию 10) |
| `/admin logs player <name>` | Лог по игроку |
| `/admin logs type <type>` | Фильтр по типу (join/leave/chat/death/command) |
| `/admin stats` | Общая статистика сервера |
| `/admin stats <player>` | Статистика игрока |
| `/admin top <field>` | Топ-10 (playtime/blocks_broken/mobs_killed/deaths/distance) |

Все команды требуют permission level 3 (оператор).

## Privilege Detection

По умолчанию привилегия определяется по тегам в display name:
- `[Admin]` / `[Админ]` → Админ
- `[VIP]` / `[Вип]` → VIP
- `[Mod]` / `[Модер]` → Модератор
- Всё остальное → значение из `default_privilege` в конфиге

Для интеграции с LuckPerms или другими плагинами прав — расширь метод `AdminPanelMod.getPlayerPrivilege()`.

## Requirements

- Minecraft 1.21.1
- Fabric Loader 0.16.0+
- Fabric API
- Java 21

## Build

```bash
./gradlew build
```

Output: `build/libs/fabric-admin-panel-1.0.0.jar`

## Installation

1. Положи `.jar` в `mods/` сервера
2. Установи Fabric API
3. Заполни `adminpanel/config.json` (токен бота и chat_id)
4. Запусти сервер

## License

MIT