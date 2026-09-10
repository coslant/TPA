package com.metox.tpa;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Warmups {

    private final JavaPlugin plugin;
    private final String bypassPerm;

    private final Map<UUID, Task> active = new HashMap<UUID, Task>();

    public Warmups(JavaPlugin plugin, String bypassPerm) {
        this.plugin = plugin;
        this.bypassPerm = bypassPerm;
    }

    private class Task {
        Location dest;
        Location start;
        int left;
        BukkitTask task;
    }

    // coslant
    public void start(final Player p, Location dest) {
        FileConfiguration c = plugin.getConfig();
        int warmup = c.getInt("warmup", 5);

        if (warmup <= 0 || p.hasPermission(bypassPerm)) {
            p.teleport(dest);
            msg(p, "teleported");
            return;
        }

        cancelSilent(p);

        final Task t = new Task();
        t.dest = dest;
        t.start = p.getLocation();
        t.left = warmup;
        active.put(p.getUniqueId(), t);

        msg(p, "warmup-start", "%time%", String.valueOf(warmup));
        show(p, warmup);

        t.task = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                tick(p);
            }
        }, 20L, 20L);
    }

    private void tick(Player p) {
        Task t = active.get(p.getUniqueId());
        if (t == null) return;

        t.left--;
        if (t.left <= 0) {
            finish(p, t);
        } else {
            show(p, t.left);
        }
    }

    private void finish(Player p, Task t) {
        active.remove(p.getUniqueId());
        if (t.task != null) t.task.cancel();

        if (p.isOnline() && t.dest != null) {
            p.teleport(t.dest);
            clearBar(p);
            msg(p, "teleported");
        }
    }

    public void onMove(Player p, Location from, Location to) {
        if (!plugin.getConfig().getBoolean("cancel-on-move", true)) return;
        Task t = active.get(p.getUniqueId());
        if (t == null) return;

        if (from.getWorld() != to.getWorld()
                || from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ()) {
            cancel(p, "cancelled-move");
        }
    }

    public void cancel(Player p, String key) {
        Task t = active.remove(p.getUniqueId());
        if (t == null) return;
        if (t.task != null) t.task.cancel();
        clearBar(p);
        if (key != null) msg(p, key);
    }

    public void cancelSilent(Player p) {
        Task t = active.remove(p.getUniqueId());
        if (t != null && t.task != null) t.task.cancel();
    }


    private void show(Player p, int seconds) {
        FileConfiguration c = plugin.getConfig();
        String type = c.getString("countdown.type", "ACTIONBAR");

        if (type != null && type.equalsIgnoreCase("TITLE")) {
            String title = c.getString("countdown.title", "&b&l%time%").replace("%time%", String.valueOf(seconds));
            String sub = c.getString("countdown.subtitle", "&7Isinlaniyor").replace("%time%", String.valueOf(seconds));
            Compat.title(p, title, sub, 0, 30, 5);
        } else {
            String msg = c.getString("countdown.message", "&eIsinlaniyor &f&l%time%").replace("%time%", String.valueOf(seconds));
            Compat.actionBar(p, msg);
        }
    }

    private void clearBar(Player p) {
        String type = plugin.getConfig().getString("countdown.type", "ACTIONBAR");
        if (type != null && type.equalsIgnoreCase("TITLE")) {
            Compat.title(p, " ", " ", 0, 1, 0);
        } else {
            Compat.actionBar(p, " ");
        }
    }

    private void msg(Player p, String key, String... repl) {
        String m = plugin.getConfig().getString("messages." + key, "");
        if (m == null || m.isEmpty()) return;
        for (int i = 0; i + 1 < repl.length; i += 2) {
            m = m.replace(repl[i], repl[i + 1]);
        }
        p.sendMessage(Compat.color(m));
    }
}
