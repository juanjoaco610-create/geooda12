package com.geooda1.mod.client;

import com.geooda1.mod.Geooda1;
import com.geooda1.mod.PlayerEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.ForgeGui;

public class HudOverlay {
    public static void render(ForgeGui gui, GuiGraphics g, float partialTick, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        PlayerEntry me = ClientState.me();
        if (me == null) return;

        // Contador de vidas (arriba a la izquierda)
        StringBuilder hearts = new StringBuilder();
        for (int i = 0; i < Geooda1.MAX_LIVES; i++) {
            hearts.append(i < me.lives() ? "§c♥ " : "§8♥ ");
        }
        g.drawString(mc.font, Component.literal("§fVidas: " + hearts), 8, 8, 0xFFFFFF, true);

        // Contador de 1 hora (arriba al centro) mientras estas caido
        if (me.status() == 1) {
            String t = "Te pueden revivir durante: §c" + ClientState.fmt(ClientState.remaining(me));
            g.drawCenteredString(mc.font, Component.literal("§6" + t), w / 2, 12, 0xFFFFFF);
        }
    }
}
