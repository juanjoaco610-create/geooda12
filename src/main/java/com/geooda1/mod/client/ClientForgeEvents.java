package com.geooda1.mod.client;

import com.geooda1.mod.Geooda1;
import com.geooda1.mod.client.screen.PlayersScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Geooda1.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {
    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        while (ClientSetup.PLAYERS_KEY.consumeClick()) {
            if (mc.player != null && mc.screen == null) mc.setScreen(new PlayersScreen(null));
        }
    }
}
