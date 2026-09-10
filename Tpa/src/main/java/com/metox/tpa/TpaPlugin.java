package com.metox.tpa;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TpaPlugin extends JavaPlugin implements Listener, TabExecutor {

    private Warmups warmups;

    private final Map<UUID, Req> requests = new HashMap<UUID, Req>();
    private final Map<UUID, Long> cooldownUntil = new HashMap<UUID, Long>();

    private static class Req {
        UUID from;
        UUID target;
        long expireAt;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        warmups = new Warmups(this, "tpa.bypass");

        String[] cmds = {"tpa", "tpaccept", "tpdeny", "tpacancel"};
        for (String cmd : cmds) {
            if (getCommand(cmd) != null) {
                getCommand(cmd).setExecutor(this);
                getCommand(cmd).setTabCompleter(this);
            }
        }
        getServer().getPluginManager().registerEvents(this, this);

        getServer().getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                expireOld();
            }
        }, 20L, 20L);

        getLogger().info("Tpa aktif - algilanan surum 1." + Compat.MINOR);
        // coslant
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName().toLowerCase();

        if (name.equals("tpa")) return request(sender, args);
        if (name.equals("tpaccept")) return accept(sender, args);
        if (name.equals("tpdeny")) return deny(sender, args);
        if (name.equals("tpacancel")) return cancel(sender);
        return true;
    }

    // coslant
    private boolean request(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            send(sender, "players-only");
            return true;
        }
        Player p = (Player) sender;
        if (!p.hasPermission("tpa.use")) {
            send(p, "no-permission");
            return true;
        }
        if (args.length < 1) {
            send(p, "usage-tpa");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            send(p, "player-not-found");
            return true;
        }
        if (target.getUniqueId().equals(p.getUniqueId())) {
            send(p, "self");
            return true;
        }

        long now = System.currentTimeMillis();

        Req existing = requests.get(p.getUniqueId());
        if (existing != null && now < existing.expireAt) {
            send(p, "already-pending");
            return true;
        }

        Long cd = cooldownUntil.get(p.getUniqueId());
        if (cd != null && now < cd) {
            long left = (cd - now + 999) / 1000;
            send(p, "cooldown", "%time%", String.valueOf(left));
            return true;
        }

        int timeout = getConfig().getInt("request-timeout", 60);
        Req r = new Req();
        r.from = p.getUniqueId();
        r.target = target.getUniqueId();
        r.expireAt = now + timeout * 1000L;
        requests.put(p.getUniqueId(), r);

        int cooldown = getConfig().getInt("cooldown", 0);
        if (cooldown > 0) cooldownUntil.put(p.getUniqueId(), now + cooldown * 1000L);

        send(p, "request-sent", "%player%", target.getName());
        send(target, "request-received", "%player%", p.getName());
        return true;
    }

    private boolean accept(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            send(sender, "players-only");
            return true;
        }
        Player me = (Player) sender;

        List<Req> mine = pendingTo(me.getUniqueId());
        if (mine.isEmpty()) {
            send(me, "no-request");
            return true;
        }

        Req chosen = pick(mine, args);
        if (chosen == null) {
            send(me, mine.size() > 1 ? "multiple-requests" : "no-request");
            return true;
        }

        requests.remove(chosen.from);

        Player reqP = Bukkit.getPlayer(chosen.from);
        Player tgtP = Bukkit.getPlayer(chosen.target);
        if (reqP == null || tgtP == null) {
            send(me, "offline");
            return true;
        }

        Player mover = reqP;
        Location dest = tgtP.getLocation();

        send(me, "accepted-target");
        send(reqP, "accepted-sender", "%player%", tgtP.getName());
        warmups.start(mover, dest);
        return true;
    }

    private boolean deny(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            send(sender, "players-only");
            return true;
        }
        Player me = (Player) sender;

        List<Req> mine = pendingTo(me.getUniqueId());
        if (mine.isEmpty()) {
            send(me, "no-request");
            return true;
        }

        Req chosen = pick(mine, args);
        if (chosen == null) {
            send(me, mine.size() > 1 ? "multiple-requests" : "no-request");
            return true;
        }

        requests.remove(chosen.from);
        send(me, "denied-target");

        Player reqP = Bukkit.getPlayer(chosen.from);
        if (reqP != null) send(reqP, "denied-sender", "%player%", me.getName());
        return true;
    }

    private boolean cancel(CommandSender sender) {
        if (!(sender instanceof Player)) {
            send(sender, "players-only");
            return true;
        }
        Player p = (Player) sender;

        Req r = requests.remove(p.getUniqueId());
        if (r == null) {
            send(p, "no-outgoing");
            return true;
        }
        Player tgt = Bukkit.getPlayer(r.target);
        if (tgt != null) send(tgt, "cancelled-target", "%player%", p.getName());
        send(p, "cancelled-own");
        return true;
    }


    private List<Req> pendingTo(UUID target) {
        long now = System.currentTimeMillis();
        List<Req> out = new ArrayList<Req>();
        for (Req r : requests.values()) {
            if (r.target.equals(target) && now < r.expireAt) out.add(r);
        }
        return out;
    }

    private Req pick(List<Req> list, String[] args) {
        if (args.length >= 1) {
            for (Req r : list) {
                Player f = Bukkit.getPlayer(r.from);
                if (f != null && f.getName().equalsIgnoreCase(args[0])) return r;
            }
            return null;
        }
        if (list.size() == 1) return list.get(0);
        return null;
    }

    private void expireOld() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, Req>> it = requests.entrySet().iterator();
        while (it.hasNext()) {
            Req r = it.next().getValue();
            if (now >= r.expireAt) {
                it.remove();
                Player reqP = Bukkit.getPlayer(r.from);
                Player tgtP = Bukkit.getPlayer(r.target);
                if (reqP != null) send(reqP, "expired-sender", "%player%", tgtP != null ? tgtP.getName() : "?");
                if (tgtP != null) send(tgtP, "expired-target", "%player%", reqP != null ? reqP.getName() : "?");
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getTo() == null) return;
        warmups.onMove(e.getPlayer(), e.getFrom(), e.getTo());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        warmups.cancelSilent(p);

        UUID id = p.getUniqueId();
        requests.remove(id);

        Iterator<Map.Entry<UUID, Req>> it = requests.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().target.equals(id)) it.remove();
        }
    }

    // coslant
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<String>();
        if (args.length != 1) return out;

        String name = command.getName().toLowerCase();
        String pref = args[0].toLowerCase();

        if (name.equals("tpa")) {
            for (Player p : Compat.online()) {
                if (p.getName().toLowerCase().startsWith(pref)) out.add(p.getName());
            }
            return out;
        }

        if ((name.equals("tpaccept") || name.equals("tpdeny")) && sender instanceof Player) {
            for (Req r : pendingTo(((Player) sender).getUniqueId())) {
                Player f = Bukkit.getPlayer(r.from);
                if (f != null && f.getName().toLowerCase().startsWith(pref)) out.add(f.getName());
            }
        }
        return out;
    }

    public void send(CommandSender sender, String key, String... repl) {
        String m = getConfig().getString("messages." + key, "");
        if (m == null || m.isEmpty()) return;
        for (int i = 0; i + 1 < repl.length; i += 2) {
            m = m.replace(repl[i], repl[i + 1]);
        }
        sender.sendMessage(Compat.color(m));
    }
}
