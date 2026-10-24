package com.geooda1.mod.client.screen;

import com.geooda1.mod.PlayerEntry;
import com.geooda1.mod.client.ClientHooks;
import com.geooda1.mod.client.ClientState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Pantalla al perder la ultima vida: esperar que te revivan o volver al menu principal. */
public class DownedScreen extends Screen {
    public DownedScreen() {
        super(Component.literal("Estás caído"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        addRenderableWidget(Button.builder(Component.literal("Esperar que me revivan"), b -> onClose())
                .bounds(cx - 100, this.height / 2, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Menú principal"), b -> ClientHooks.disconnect())
                .bounds(cx - 100, this.height / 2 + 26, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        g.drawCenteredString(this.font, Component.literal("§cHas perdido tu última vida"), this.width / 2, this.height / 2 - 60, 0xFFFFFF);
        PlayerEntry me = ClientState.me();
        if (me != null && me.status() == 1) {
            g.drawCenteredString(this.font, Component.literal("§7Tus amigos tienen §e" + ClientState.fmt(ClientState.remaining(me)) + "§7 para revivirte"),
                    this.width / 2, this.height / 2 - 40, 0xFFFFFF);
        }
        g.drawCenteredString(this.font, Component.literal("§7Si el contador llega a 0, tu cuerpo explota y mueres definitivamente."),
                this.width / 2, this.height / 2 - 24, 0xFFFFFF);
        super.render(g, mx, my, pt);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
