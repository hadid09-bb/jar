package id.goodsmp.setrank;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class GoodSMPSetrank extends JavaPlugin implements Listener {

    private static GoodSMPSetrank instance;

    // UUID -> pesan yang akan dikirim saat login
    private final Map<String, String> pendingMessages = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        getCommand("setrank").setExecutor(new SetrankCommand(this));
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("GoodSMPSetrank enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("GoodSMPSetrank disabled!");
    }

    public static GoodSMPSetrank getInstance() {
        return instance;
    }

    /** Simpan pesan pending untuk dikirim saat player login */
    public void addPendingMessage(String uuid, String message) {
        pendingMessages.put(uuid, message);
    }

    /** Kirim pending message saat player join */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        if (pendingMessages.containsKey(uuid)) {
            String msg = pendingMessages.remove(uuid);
            // Kirim sedikit delay agar UI player sudah siap
            getServer().getScheduler().runTaskLater(this, () -> {
                for (String line : msg.split("\n")) {
                    player.sendMessage(line);
                }
            }, 20L); // 1 detik delay
        }
    }
}
