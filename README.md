# Fabric Admin Panel

Server-side Fabric mod for Minecraft 1.21.x with event logging and player statistics.

## Features

- **Event Logging** — tracks player joins, leaves, chat messages, deaths, and command usage
- **Player Statistics** — playtime, blocks broken/placed, mobs killed, deaths, distance walked
- **Leaderboards** — `/admin top <field>` to see top 10 players by any stat
- **JSON Storage** — logs saved daily to `adminpanel/logs/`, stats to `adminpanel/stats.json`

## Commands

| Command | Description |
|---------|-------------|
| `/admin logs [count]` | Show recent events (default: 10) |
| `/admin logs player <name>` | Events by specific player |
| `/admin logs type <type>` | Events by type (join/leave/chat/death/command) |
| `/admin stats` | Server-wide statistics overview |
| `/admin stats <player>` | Individual player statistics |
| `/admin top <field>` | Top 10 leaderboard (playtime/blocks_broken/mobs_killed/deaths/distance) |

All commands require permission level 3 (operator).

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

1. Place the `.jar` file in your server's `mods/` folder
2. Ensure Fabric API is also installed
3. Start the server — logs will appear in `adminpanel/logs/`

## License

MIT