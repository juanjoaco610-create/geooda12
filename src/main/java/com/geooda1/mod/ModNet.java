package com.geooda1.mod;

import com.geooda1.mod.client.ClientHooks;
import com.geooda1.mod.client.ClientState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class ModNet {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Geooda1.MODID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, Sync.class, Sync::encode, Sync::decode, Sync::handle);
        CHANNEL.registerMessage(id++, OpenScreen.class, OpenScreen::encode, OpenScreen::decode, OpenScreen::handle);
        CHANNEL.registerMessage(id++, Revive.class, Revive::encode, Revive::decode, Revive::handle);
    }

    public static void toPlayer(ServerPlayer p, Object msg) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), msg);
    }

    public static void toAll(Object msg) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), msg);
    }

    /** Lista de jugadores y sus estados (servidor -> cliente). */
    public static class Sync {
        public final List<PlayerEntry> list;

        public Sync(List<PlayerEntry> list) { this.list = list; }

        static void encode(Sync m, FriendlyByteBuf b) {
            b.writeVarInt(m.list.size());
            for (PlayerEntry e : m.list) {
                b.writeUUID(e.id());
                b.writeUtf(e.name(), 32);
                b.writeByte(e.lives());
                b.writeByte(e.status());
                b.writeLong(e.remainingMs());
            }
        }

        static Sync decode(FriendlyByteBuf b) {
            int n = b.readVarInt();
            List<PlayerEntry> l = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                l.add(new PlayerEntry(b.readUUID(), b.readUtf(32), b.readByte(), b.readByte(), b.readLong()));
            }
            return new Sync(l);
        }

        static void handle(Sync m, Supplier<NetworkEvent.Context> c) {
            c.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientState.set(m.list)));
            c.get().setPacketHandled(true);
        }
    }

    /** Pide al cliente abrir una pantalla. type: 0 caido, 1 muerto definitivo, 2 frasco de revivir. */
    public static class OpenScreen {
        public final int type;
        public final BlockPos pos;

        public OpenScreen(int type, BlockPos pos) { this.type = type; this.pos = pos; }

        static void encode(OpenScreen m, FriendlyByteBuf b) { b.writeByte(m.type); b.writeBlockPos(m.pos); }

        static OpenScreen decode(FriendlyByteBuf b) { return new OpenScreen(b.readByte(), b.readBlockPos()); }

        static void handle(OpenScreen m, Supplier<NetworkEvent.Context> c) {
            c.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.open(m.type, m.pos)));
            c.get().setPacketHandled(true);
        }
    }

    /** El jugador elige a quien revivir en el frasco (cliente -> servidor). */
    public static class Revive {
        public final UUID target;
        public final BlockPos jar;

        public Revive(UUID target, BlockPos jar) { this.target = target; this.jar = jar; }

        static void encode(Revive m, FriendlyByteBuf b) { b.writeUUID(m.target); b.writeBlockPos(m.jar); }

        static Revive decode(FriendlyByteBuf b) { return new Revive(b.readUUID(), b.readBlockPos()); }

        static void handle(Revive m, Supplier<NetworkEvent.Context> c) {
            ServerPlayer sender = c.get().getSender();
            c.get().enqueueWork(() -> {
                if (sender != null) ServerEvents.handleRevive(sender, m.target, m.jar);
            });
            c.get().setPacketHandled(true);
        }
    }
}
