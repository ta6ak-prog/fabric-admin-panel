package com.adminpanel.telegram;

import com.adminpanel.AdminPanelMod;
import com.adminpanel.stats.StatsManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Sends notifications to a Telegram chat via Bot API.
 * Notifies on player join/leave with nickname, privilege, and last session stats.
 */
public class TelegramNotifier {

    private static final Gson GSON = new Gson();
    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    private String botToken;
    private long chatId;
    private boolean enabled;

    public void init(String botToken, long chatId) {
        this.botToken = botToken;
        this.chatId = chatId;
        this.enabled = botToken != null && !botToken.isEmpty() && chatId != 0;
        if (enabled) {
            AdminPanelMod.LOGGER.info("Telegram notifications enabled for chat " + chatId);
        } else {
            AdminPanelMod.LOGGER.warn("Telegram notifications disabled — set bot_token and chat_id in config");
        }
    }

    public void onPlayerJoin(String playerName, String privilege, StatsManager.PlayerStats stats) {
        if (!enabled) return;

        StringBuilder msg = new StringBuilder();
        msg.append("🟢 <b>Игрок зашёл на сервер</b>\n\n");
        msg.append("👤 Ник: <code>").append(escapeHtml(playerName)).append("</code>\n");
        msg.append("⭐ Привилегия: <b>").append(escapeHtml(privilege)).append("</b>\n");

        if (stats != null) {
            msg.append("\n📊 <b>Последняя сессия:</b>\n");
            if (stats.playtimeMinutes > 0) {
                msg.append("  ⏱ Наиграл: ").append(formatTime(stats.playtimeMinutes)).append("\n");
            }
            if (stats.blocksBroken > 0) {
                msg.append("  ⛏ Блоков сломано: ").append(stats.blocksBroken).append("\n");
            }
            if (stats.mobsKilled > 0) {
                msg.append("  🗡 Мобов убито: ").append(stats.mobsKilled).append("\n");
            }
            if (stats.deaths > 0) {
                msg.append("  💀 Смертей: ").append(stats.deaths).append("\n");
            }
            if (!stats.lastJoin.isEmpty()) {
                msg.append("  🕐 Последний вход: ").append(stats.lastJoin).append("\n");
            }
        }

        sendMessage(msg.toString());
    }

    public void onPlayerLeave(String playerName, String privilege, StatsManager.PlayerStats stats) {
        if (!enabled) return;

        StringBuilder msg = new StringBuilder();
        msg.append("🔴 <b>Игрок вышел с сервера</b>\n\n");
        msg.append("👤 Ник: <code>").append(escapeHtml(playerName)).append("</code>\n");
        msg.append("⭐ Привилегия: <b>").append(escapeHtml(privilege)).append("</b>\n");

        if (stats != null) {
            msg.append("\n📊 <b>Эта сессия:</b>\n");
            msg.append("  ⏱ Наиграл: ").append(formatTime(stats.playtimeMinutes)).append("\n");
            if (stats.blocksBroken > 0) {
                msg.append("  ⛏ Блоков сломано: ").append(stats.blocksBroken).append("\n");
            }
            if (stats.mobsKilled > 0) {
                msg.append("  🗡 Мобов убито: ").append(stats.mobsKilled).append("\n");
            }
            if (stats.deaths > 0) {
                msg.append("  💀 Смертей: ").append(stats.deaths).append("\n");
            }
        }

        sendMessage(msg.toString());
    }

    private void sendMessage(String text) {
        try {
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            JsonObject body = new JsonObject();
            body.addProperty("chat_id", chatId);
            body.addProperty("text", text);
            body.addProperty("parse_mode", "HTML");
            body.addProperty("disable_web_page_preview", true);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)))
                .timeout(Duration.ofSeconds(15))
                .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() != 200) {
                        AdminPanelMod.LOGGER.warn("Telegram API error: " + response.statusCode() + " — " + response.body());
                    }
                })
                .exceptionally(ex -> {
                    AdminPanelMod.LOGGER.error("Failed to send Telegram message: " + ex.getMessage());
                    return null;
                });

        } catch (Exception e) {
            AdminPanelMod.LOGGER.error("Telegram send error: " + e.getMessage());
        }
    }

    private static String formatTime(long minutes) {
        if (minutes < 60) return minutes + " мин";
        long hours = minutes / 60;
        long mins = minutes % 60;
        if (mins == 0) return hours + " ч";
        return hours + " ч " + mins + " мин";
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}