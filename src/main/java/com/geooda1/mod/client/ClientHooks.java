package com.geooda1.mod.client;

import com.geooda1.mod.client.screen.DeadScreen;
import com.geooda1.mod.client.screen.DownedScreen;
import com.geooda1.mod.client.screen.ReviveScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class ClientHooks {
    public static void open(int type, BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        switch (type) {
            case 0 -> mc.setScreen(new DownedScreen());
            case 1 -> mc.setScreen(new DeadScreen());
            case 2 -> mc.setScreen(new ReviveScreen(pos));
            default -> { }
        }
    }

    /** Volver al menu principal (igual que el boton del menu de pausa). */
    public static void disconnect() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        boolean local = mc.isLocalServer();
        mc.level.disconnect();
        if (local) {
            mc.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
        } else {
            mc.clearLevel();
        }
        TitleScreen title = new TitleScreen();
        if (local) mc.setScreen(title);
        else mc.setScreen(new JoinMultiplayerScreen(title));
    }
}
