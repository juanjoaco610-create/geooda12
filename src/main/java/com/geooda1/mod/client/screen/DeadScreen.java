package com.geooda1.mod.client.screen;

import com.geooda1.mod.client.ClientHooks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Muerte definitiva: menu principal o ver jugadores. */
public class DeadScreen extends Screen {
    public DeadScreen() {
        super(Component.literal("Muerte definitiva"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        addRenderableWidget(Button.builder(Component.literal("Ver jugadores"), b -> this.minecraft.setScreen(new PlayersScreen(this)))
                .bounds(cx - 100, this.height / 2, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Menú principal"), b -> ClientHooks.disconnect())
                .bounds(cx - 100, this.height / 2 + 26, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        g.drawCenteredString(this.font, Component.literal("§4§lHas muerto definitivamente"), this.width / 2, this.height / 2 - 50, 0xFFFFFF);
        g.drawCenteredString(this.font, Component.literal("§7Ya no puedes ser revivido."), this.width / 2, this.height / 2 - 30, 0xFFFFFF);
        super.render(g, mx, my, pt);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
