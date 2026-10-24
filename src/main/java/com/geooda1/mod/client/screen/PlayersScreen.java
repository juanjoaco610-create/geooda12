package com.geooda1.mod.client.screen;

import com.geooda1.mod.PlayerEntry;
import com.geooda1.mod.client.ClientState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** Lista de jugadores: vivos, caidos (con contador) y muertos definitivos. */
public class PlayersScreen extends Screen {
    private final Screen parent;
    private int scroll = 0;

    public PlayersScreen(Screen parent) {
        super(Component.literal("Jugadores"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Cerrar"), b -> onClose())
                .bounds(this.width / 2 - 50, this.height - 28, 100, 20).build());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    private List<String> lines() {
        List<PlayerEntry> alive = new ArrayList<>(), downed = new ArrayList<>(), dead = new ArrayList<>();
        for (PlayerEntry e : ClientState.all()) {
            (e.status() == 0 ? alive : e.status() == 1 ? downed : dead).add(e);
        }
        List<String> l = new ArrayList<>();
        l.add("§a§lVivos (" + alive.size() + ")");
        for (PlayerEntry e : alive) l.add("  §f" + e.name() + "  " + hearts(e.lives()));
        l.add("");
        l.add("§6§lCaídos (" + downed.size() + ")");
        for (PlayerEntry e : downed) l.add("  §f" + e.name() + "  §6⏳ " + ClientState.fmt(ClientState.remaining(e)));
        l.add("");
        l.add("§4§lMuertos definitivamente (" + dead.size() + ")");
        for (PlayerEntry e : dead) l.add("  §7" + e.name() + "  §4✖");
        return l;
    }

    private static String hearts(int lives) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < com.geooda1.mod.Geooda1.MAX_LIVES; i++) sb.append(i < lives ? "§c♥ " : "§8♥ ");
        return sb.toString();
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        scroll = Math.max(0, scroll - (int) Math.signum(delta));
        return true;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        g.drawCenteredString(this.font, Component.literal("§e§lJugadores"), this.width / 2, 12, 0xFFFFFF);
        List<String> l = lines();
        int visible = (this.height - 80) / 11;
        scroll = Math.min(scroll, Math.max(0, l.size() - visible));
        int x = this.width / 2 - 110;
        int y = 36;
        for (int i = scroll; i < Math.min(l.size(), scroll + visible); i++) {
            g.drawString(this.font, Component.literal(l.get(i)), x, y, 0xFFFFFF, true);
            y += 11;
        }
        super.render(g, mx, my, pt);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
