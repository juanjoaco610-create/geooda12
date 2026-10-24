package com.geooda1.mod.client.screen;

import com.geooda1.mod.ModNet;
import com.geooda1.mod.PlayerEntry;
import com.geooda1.mod.client.ClientState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** Frasco de corazon: muestra a los caidos que aun estan a tiempo y permite revivir a uno. */
public class ReviveScreen extends Screen {
    private static final int PER_PAGE = 6;
    private final BlockPos jar;
    private final List<PlayerEntry> candidates = new ArrayList<>();
    private int page = 0;

    public ReviveScreen(BlockPos jar) {
        super(Component.literal("Frasco de Corazón"));
        this.jar = jar;
        for (PlayerEntry e : ClientState.all()) {
            if (e.status() == 1 && ClientState.remaining(e) > 0) candidates.add(e);
        }
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = 50;
        int start = page * PER_PAGE;
        for (int i = start; i < Math.min(candidates.size(), start + PER_PAGE); i++) {
            PlayerEntry e = candidates.get(i);
            addRenderableWidget(Button.builder(Component.literal("Revivir a " + e.name()), b -> {
                ModNet.CHANNEL.sendToServer(new ModNet.Revive(e.id(), jar));
                onClose();
            }).bounds(cx - 100, y, 200, 20).build());
            y += 24;
        }
        if (candidates.size() > PER_PAGE) {
            addRenderableWidget(Button.builder(Component.literal("<"), b -> { page = Math.max(0, page - 1); rebuildWidgets(); })
                    .bounds(cx - 100, this.height - 52, 40, 20).build());
            addRenderableWidget(Button.builder(Component.literal(">"), b -> {
                if ((page + 1) * PER_PAGE < candidates.size()) page++;
                rebuildWidgets();
            }).bounds(cx + 60, this.height - 52, 40, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Cerrar"), b -> onClose())
                .bounds(cx - 50, this.height - 28, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        g.drawCenteredString(this.font, Component.literal("§d§lFrasco de Corazón"), this.width / 2, 14, 0xFFFFFF);
        if (candidates.isEmpty()) {
            g.drawCenteredString(this.font, Component.literal("§7No hay nadie caído al que se pueda revivir."), this.width / 2, 60, 0xFFFFFF);
        } else {
            g.drawCenteredString(this.font, Component.literal("§7Elige a quién revivir (se gasta 1 Corazón Dorado)"), this.width / 2, 30, 0xFFFFFF);
        }
        super.render(g, mx, my, pt);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
