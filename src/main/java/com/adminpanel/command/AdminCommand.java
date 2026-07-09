package com.adminpanel.command;

import com.adminpanel.AdminPanelMod;
import com.adminpanel.logger.EventLogger;
import com.adminpanel.stats.StatsManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Map;

public class AdminCommand {

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("admin")
                .requires(source -> source.hasPermissionLevel(3))
                .then(CommandManager.literal("logs")
                    .executes(AdminCommand::showRecentLogs)
                    .then(CommandManager.argument("count", IntegerArgumentType.integer(1, 500))
                        .executes(ctx -> showRecentLogs(ctx, IntegerArgumentType.getInteger(ctx, "count"))))
                    .then(CommandManager.literal("player")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                            .executes(ctx -> showPlayerLogs(ctx, StringArgumentType.getString(ctx, "name")))))
                    .then(CommandManager.literal("type")
                        .then(CommandManager.argument("type", StringArgumentType.word())
                            .executes(ctx -> showTypeLogs(ctx, StringArgumentType.getString(ctx, "type"))))))
                .then(CommandManager.literal("stats")
                    .executes(AdminCommand::showServerStats)
                    .then(CommandManager.argument("player", StringArgumentType.word())
                        .executes(ctx -> showPlayerStats(ctx, StringArgumentType.getString(ctx, "player")))))
                .then(CommandManager.literal("top")
                    .then(CommandManager.argument("field", StringArgumentType.word())
                        .executes(ctx -> showTop(ctx, StringArgumentType.getString(ctx, "field")))))
        );
    }

    private static int showRecentLogs(CommandContext<ServerCommandSource> ctx) {
        return showRecentLogs(ctx, 10);
    }

    private static int showRecentLogs(CommandContext<ServerCommandSource> ctx, int count) {
        List<EventLogger.LogEntry> logs = AdminPanelMod.eventLogger.getRecent(count);
        if (logs.isEmpty()) {
            ctx.getSource().sendFeedback(() -> Text.literal("No recent events.").formatted(Formatting.YELLOW), false);
            return 1;
        }
        ctx.getSource().sendFeedback(() -> Text.literal("=== Recent Events (last " + logs.size() + ") ===").formatted(Formatting.GOLD), false);
        for (EventLogger.LogEntry entry : logs) {
            ctx.getSource().sendFeedback(() -> Text.literal(
                "[" + entry.time() + "] " + entry.type() + " | " + entry.player() + " | " + entry.details()
            ).formatted(Formatting.GRAY), false);
        }
        return 1;
    }

    private static int showPlayerLogs(CommandContext<ServerCommandSource> ctx, String name) {
        List<EventLogger.LogEntry> logs = AdminPanelMod.eventLogger.getByPlayer(name);
        ctx.getSource().sendFeedback(() -> Text.literal(
            "=== Events for " + name + " (" + logs.size() + ") ==="
        ).formatted(Formatting.GOLD), false);
        for (EventLogger.LogEntry entry : logs) {
            ctx.getSource().sendFeedback(() -> Text.literal(
                "[" + entry.time() + "] " + entry.type() + " | " + entry.details()
            ).formatted(Formatting.GRAY), false);
        }
        return 1;
    }

    private static int showTypeLogs(CommandContext<ServerCommandSource> ctx, String type) {
        List<EventLogger.LogEntry> logs = AdminPanelMod.eventLogger.getByType(type);
        ctx.getSource().sendFeedback(() -> Text.literal(
            "=== Events of type '" + type + "' (" + logs.size() + ") ==="
        ).formatted(Formatting.GOLD), false);
        for (EventLogger.LogEntry entry : logs) {
            ctx.getSource().sendFeedback(() -> Text.literal(
                "[" + entry.time() + "] " + entry.player() + " | " + entry.details()
            ).formatted(Formatting.GRAY), false);
        }
        return 1;
    }

    private static int showServerStats(CommandContext<ServerCommandSource> ctx) {
        Map<String, StatsManager.PlayerStats> all = AdminPanelMod.statsManager.getAllStats();
        ctx.getSource().sendFeedback(() -> Text.literal("=== Server Statistics ===").formatted(Formatting.GOLD), false);
        ctx.getSource().sendFeedback(() -> Text.literal("Tracked players: " + all.size()).formatted(Formatting.GREEN), false);

        long totalPlaytime = all.values().stream().mapToLong(s -> s.playtimeMinutes).sum();
        long totalBroken = all.values().stream().mapToLong(s -> s.blocksBroken).sum();
        long totalDeaths = all.values().stream().mapToLong(s -> s.deaths).sum();

        ctx.getSource().sendFeedback(() -> Text.literal("Total playtime: " + totalPlaytime + " min").formatted(Formatting.GREEN), false);
        ctx.getSource().sendFeedback(() -> Text.literal("Total blocks broken: " + totalBroken).formatted(Formatting.GREEN), false);
        ctx.getSource().sendFeedback(() -> Text.literal("Total deaths: " + totalDeaths).formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int showPlayerStats(CommandContext<ServerCommandSource> ctx, String player) {
        StatsManager.PlayerStats stats = AdminPanelMod.statsManager.getOrCreate(player);
        ctx.getSource().sendFeedback(() -> Text.literal("=== Stats: " + player + " ===").formatted(Formatting.GOLD), false);
        ctx.getSource().sendFeedback(() -> Text.literal("Playtime: " + stats.playtimeMinutes + " min").formatted(Formatting.GREEN), false);
        ctx.getSource().sendFeedback(() -> Text.literal("Blocks broken: " + stats.blocksBroken).formatted(Formatting.GREEN), false);
        ctx.getSource().sendFeedback(() -> Text.literal("Blocks placed: " + stats.blocksPlaced).formatted(Formatting.GREEN), false);
        ctx.getSource().sendFeedback(() -> Text.literal("Mobs killed: " + stats.mobsKilled).formatted(Formatting.GREEN), false);
        ctx.getSource().sendFeedback(() -> Text.literal("Deaths: " + stats.deaths).formatted(Formatting.GREEN), false);
        ctx.getSource().sendFeedback(() -> Text.literal("Distance: " + stats.distanceWalked + " blocks").formatted(Formatting.GREEN), false);
        ctx.getSource().sendFeedback(() -> Text.literal("Commands used: " + stats.commandsUsed).formatted(Formatting.GREEN), false);
        ctx.getSource().sendFeedback(() -> Text.literal("Joins: " + stats.joinCount).formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int showTop(CommandContext<ServerCommandSource> ctx, String field) {
        List<StatsManager.PlayerStats> top = AdminPanelMod.statsManager.getTopPlayers(field, 10);
        ctx.getSource().sendFeedback(() -> Text.literal("=== Top 10 by " + field + " ===").formatted(Formatting.GOLD), false);
        int rank = 1;
        for (StatsManager.PlayerStats s : top) {
            final int r = rank++;
            final String name = s.playerName;
            final long val = switch (field.toLowerCase()) {
                case "playtime" -> s.playtimeMinutes;
                case "blocks_broken" -> s.blocksBroken;
                case "blocks_placed" -> s.blocksPlaced;
                case "mobs_killed" -> s.mobsKilled;
                case "deaths" -> s.deaths;
                case "distance" -> s.distanceWalked;
                case "commands" -> s.commandsUsed;
                case "joins" -> s.joinCount;
                default -> 0;
            };
            ctx.getSource().sendFeedback(() -> Text.literal(
                r + ". " + name + " — " + val
            ).formatted(Formatting.YELLOW), false);
        }
        return 1;
    }
}