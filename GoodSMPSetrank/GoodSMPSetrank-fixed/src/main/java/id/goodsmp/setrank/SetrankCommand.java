package id.goodsmp.setrank;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

@SuppressWarnings("deprecation")
public class SetrankCommand implements CommandExecutor {

    private final GoodSMPSetrank plugin;

    public SetrankCommand(GoodSMPSetrank plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("goodsmp.setrank")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /setrank <player> <group>");
            return true;
        }

        String targetName = args[0];
        String group = args[1];
        String staffName = sender instanceof Player ? sender.getName() : "CONSOLE";

        // Cek apakah player online dulu
        Player onlineTarget = Bukkit.getPlayer(targetName);

        if (onlineTarget != null) {
            // Player ONLINE
            String uuid = onlineTarget.getUniqueId().toString();
            final String displayName = onlineTarget.getName();

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + uuid + " parent set " + group);

                String border = "§8------------------------------------";
                Bukkit.broadcastMessage(border);
                Bukkit.broadcastMessage("§b[SETRANK] §f" + displayName);
                Bukkit.broadcastMessage("§7Set by: §f" + staffName);
                Bukkit.broadcastMessage("§7Username: §f" + displayName);
                Bukkit.broadcastMessage("§7New Rank: §f" + group);
                Bukkit.broadcastMessage(border);

                onlineTarget.sendMessage("§b§lRank kamu telah diubah oleh §f" + staffName + "§b§l!");
                onlineTarget.sendMessage("§7Rank baru: §f" + group);

                DiscordWebhook.sendSetrank(staffName, displayName, group);

                sender.sendMessage("§aBerhasil set rank §f" + displayName + " §ake §f" + group);
            });

        } else {
            // Player OFFLINE — cari dari riwayat server
            // getOfflinePlayer(name) bisa blocking, jalankan async lalu set sync
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                OfflinePlayer offlineTarget = findOfflinePlayer(targetName);

                if (offlineTarget == null || !offlineTarget.hasPlayedBefore()) {
                    sender.sendMessage("§cPlayer §f" + targetName + " §ctidak ditemukan. Pastikan nama benar dan player pernah join server.");
                    return;
                }

                String uuid = offlineTarget.getUniqueId().toString();
                // getName() bisa null pada beberapa versi Spigot jika belum pernah join
                String displayName = offlineTarget.getName() != null ? offlineTarget.getName() : targetName;

                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + uuid + " parent set " + group);

                    // Broadcast ke server
                    String border = "§8------------------------------------";
                    Bukkit.broadcastMessage(border);
                    Bukkit.broadcastMessage("§b[SETRANK] §f" + displayName + " §7(offline)");
                    Bukkit.broadcastMessage("§7Set by: §f" + staffName);
                    Bukkit.broadcastMessage("§7Username: §f" + displayName);
                    Bukkit.broadcastMessage("§7New Rank: §f" + group);
                    Bukkit.broadcastMessage(border);

                    // Simpan pending message, akan dikirim saat player login
                    plugin.addPendingMessage(uuid,
                            "§b§lRank kamu telah diubah oleh §f" + staffName + "§b§l!\n" +
                            "§7Rank baru: §f" + group);

                    DiscordWebhook.sendSetrank(staffName, displayName, group);

                    sender.sendMessage("§aBerhasil set rank §f" + displayName + " §ake §f" + group + " §7(player sedang offline, akan diberi tahu saat login).");
                });
            });
        }

        return true;
    }

    /**
     * Mencari OfflinePlayer berdasarkan nama (case-insensitive).
     * Iterasi semua OfflinePlayers agar cocok meskipun ada perbedaan kapitalisasi.
     */
    private OfflinePlayer findOfflinePlayer(String name) {
        // Cek exact match dulu (ini juga handles online player yg sudah kita lewati)
        OfflinePlayer direct = Bukkit.getOfflinePlayer(name);
        if (direct.hasPlayedBefore()) return direct;

        // Fallback: iterasi semua offline players (case-insensitive)
        return Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
