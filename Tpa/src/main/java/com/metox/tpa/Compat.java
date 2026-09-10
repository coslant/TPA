package com.metox.tpa;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// coslant
public final class Compat {

    public static final int MINOR;

    private static final String NMS_VER;

    static {
        int m = 8;
        try {
            String raw = Bukkit.getBukkitVersion().split("-")[0];
            String[] parts = raw.split("\\.");
            if (parts.length >= 2) m = Integer.parseInt(parts[1]);
        } catch (Throwable t) {
            m = 8;
        }
        MINOR = m;

        String ver = "";
        try {
            String cn = Bukkit.getServer().getClass().getName();
            String[] p = cn.split("\\.");
            if (p.length >= 4 && p[3].startsWith("v")) ver = p[3];
        } catch (Throwable t) {
            ver = "";
        }
        NMS_VER = ver;
    }

    private Compat() {}

    public static String color(String in) {
        if (in == null) return "";
        return ChatColor.translateAlternateColorCodes('&', in);
    }


    public static void actionBar(Player p, String text) {
        String msg = color(text);
        if (spigotActionBar(p, msg)) return;
        if (nmsActionBar(p, msg)) return;
        title(p, " ", msg, 0, 30, 0);
    }

    private static boolean spigotActionBar(Player p, String msg) {
        try {
            Class<?> cmt = Class.forName("net.md_5.bungee.api.ChatMessageType");
            Object action = null;
            for (Object e : cmt.getEnumConstants()) {
                if (((Enum<?>) e).name().equals("ACTION_BAR")) {
                    action = e;
                    break;
                }
            }
            if (action == null) return false;

            Class<?> base = Class.forName("net.md_5.bungee.api.chat.BaseComponent");
            Class<?> text = Class.forName("net.md_5.bungee.api.chat.TextComponent");
            Object comp = text.getConstructor(String.class).newInstance(msg);

            Object spigot = Player.class.getMethod("spigot").invoke(p);
            Object arr = Array.newInstance(base, 1);
            Array.set(arr, 0, comp);

            Class<?> spigotClass = Class.forName("org.bukkit.entity.Player$Spigot");
            Method send = spigotClass.getMethod("sendMessage", cmt, arr.getClass());
            send.invoke(spigot, action, arr);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean nmsActionBar(Player p, String msg) {
        if (NMS_VER == null || NMS_VER.isEmpty()) return false;
        try {
            String nms = "net.minecraft.server." + NMS_VER + ".";
            String cb = "org.bukkit.craftbukkit." + NMS_VER + ".";

            Class<?> craftPlayer = Class.forName(cb + "entity.CraftPlayer");
            Object handle = craftPlayer.getMethod("getHandle").invoke(p);
            Object conn = handle.getClass().getField("playerConnection").get(handle);

            Class<?> chatBase = Class.forName(nms + "IChatBaseComponent");
            Class<?> chatText = Class.forName(nms + "ChatComponentText");
            Class<?> packetChat = Class.forName(nms + "PacketPlayOutChat");
            Class<?> packet = Class.forName(nms + "Packet");

            Object comp = chatText.getConstructor(String.class).newInstance(msg);
            Object packetObj = packetChat.getConstructor(chatBase, byte.class).newInstance(comp, (byte) 2);

            Method send = conn.getClass().getMethod("sendPacket", packet);
            send.invoke(conn, packetObj);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }


    public static boolean title(Player p, String title, String sub, int in, int stay, int out) {
        String t = color(title);
        String s = color(sub);

        if (MINOR >= 11) {
            if (apiTitle(p, t, s, in, stay, out)) return true;
        }
        return nmsTitle(p, t, s, in, stay, out);
    }

    private static boolean apiTitle(Player p, String t, String s, int in, int stay, int out) {
        try {
            Method m = Player.class.getMethod("sendTitle", String.class, String.class, int.class, int.class, int.class);
            m.invoke(p, t, s, in, stay, out);
            return true;
        } catch (NoSuchMethodException five) {
            try {
                Method m2 = Player.class.getMethod("sendTitle", String.class, String.class);
                m2.invoke(p, t, s);
                return true;
            } catch (Throwable ignored) {
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static boolean nmsTitle(Player p, String t, String s, int in, int stay, int out) {
        if (NMS_VER == null || NMS_VER.isEmpty()) return false;
        try {
            String nms = "net.minecraft.server." + NMS_VER + ".";
            String cb = "org.bukkit.craftbukkit." + NMS_VER + ".";

            Class<?> craftPlayer = Class.forName(cb + "entity.CraftPlayer");
            Object handle = craftPlayer.getMethod("getHandle").invoke(p);
            Field connField = handle.getClass().getField("playerConnection");
            Object conn = connField.get(handle);

            Class<?> packetClass = Class.forName(nms + "Packet");
            Method sendPacket = conn.getClass().getMethod("sendPacket", packetClass);

            Class<?> chatBase = Class.forName(nms + "IChatBaseComponent");
            Class<?> chatText = Class.forName(nms + "ChatComponentText");
            Class<?> packetTitle = Class.forName(nms + "PacketPlayOutTitle");

            Class<?> enumAction;
            try {
                enumAction = Class.forName(nms + "PacketPlayOutTitle$EnumTitleAction");
            } catch (ClassNotFoundException nested) {
                enumAction = Class.forName(nms + "EnumTitleAction");
            }

            Object aTitle = enumValue(enumAction, "TITLE");
            Object aSub = enumValue(enumAction, "SUBTITLE");
            Object aTimes = enumValue(enumAction, "TIMES");

            Constructor<?> timesCon = packetTitle.getConstructor(enumAction, chatBase, int.class, int.class, int.class);
            sendPacket.invoke(conn, timesCon.newInstance(aTimes, null, in, stay, out));

            Constructor<?> partCon = packetTitle.getConstructor(enumAction, chatBase);
            Constructor<?> textCon = chatText.getConstructor(String.class);

            if (s != null && !s.isEmpty()) {
                Object subComp = textCon.newInstance(s);
                sendPacket.invoke(conn, partCon.newInstance(aSub, subComp));
            }

            Object titleComp = textCon.newInstance(t == null ? "" : t);
            sendPacket.invoke(conn, partCon.newInstance(aTitle, titleComp));
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    private static Object enumValue(Class<?> enumClass, String name) {
        for (Object o : enumClass.getEnumConstants()) {
            if (((Enum<?>) o).name().equals(name)) return o;
        }
        return null;
    }


    @SuppressWarnings("unchecked")
    public static List<Player> online() {
        List<Player> list = new ArrayList<Player>();
        try {
            Object res = Bukkit.class.getMethod("getOnlinePlayers").invoke(null);
            if (res instanceof Player[]) {
                for (Player p : (Player[]) res) list.add(p);
            } else if (res instanceof Collection) {
                for (Object o : (Collection<Object>) res) list.add((Player) o);
            }
        } catch (Throwable ignored) {
        }
        return list;
    }
}
