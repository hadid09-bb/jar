package id.goodsmp.setrank;

import org.bukkit.Bukkit;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhook {

    public static void sendSetrank(String staffName, String targetName, String group) {
        int color = GoodSMPSetrank.getInstance().getConfig().getInt("color", 3447003);
        String json = "{\"embeds\":[{" +
                "\"title\":\"GoodSMP | Setrank\"," +
                "\"color\":" + color + "," +
                "\"fields\":[" +
                "{\"name\":\"Set by\",\"value\":\"" + escape(staffName) + "\",\"inline\":true}," +
                "{\"name\":\"Username\",\"value\":\"" + escape(targetName) + "\",\"inline\":true}," +
                "{\"name\":\"New Rank\",\"value\":\"" + escape(group) + "\",\"inline\":false}" +
                "]}]}";

        send(json);
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "");
    }

    private static void send(String json) {
        String webhookUrl = GoodSMPSetrank.getInstance().getConfig().getString("discord.webhook-url");
        if (webhookUrl == null || webhookUrl.isEmpty()) return;

        Bukkit.getScheduler().runTaskAsynchronously(GoodSMPSetrank.getInstance(), () -> {
            try {
                URL url = new URL(webhookUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(bytes);
                }
                conn.getResponseCode();
                conn.disconnect();
            } catch (Exception e) {
                GoodSMPSetrank.getInstance().getLogger().warning("Failed to send Discord webhook: " + e.getMessage());
            }
        });
    }
}
