package com.geooda1.mod.client;

import com.geooda1.mod.PlayerEntry;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class ClientState {
    private static List<PlayerEntry> entries = new ArrayList<>();
    private static long receivedAt = System.currentTimeMillis();

    public static void set(List<PlayerEntry> list) {
        entries = list;
        receivedAt = System.currentTimeMillis();
    }

    public static List<PlayerEntry> all() {
        return entries;
    }

    public static PlayerEntry me() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return null;
        for (PlayerEntry e : entries) if (e.id().equals(mc.player.getUUID())) return e;
        return null;
    }

    /** Tiempo restante, descontando lo que paso desde que llego el paquete. */
    public static long remaining(PlayerEntry e) {
        return Math.max(0, e.remainingMs() - (System.currentTimeMillis() - receivedAt));
    }

    public static String fmt(long ms) {
        long s = ms / 1000;
        return String.format("%02d:%02d", s / 60, s % 60);
    }
}
