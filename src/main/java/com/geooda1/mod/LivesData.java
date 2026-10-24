package com.geooda1.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class LivesData extends SavedData {
    public enum Status { ALIVE, DOWNED, DEAD }

    public static class Rec {
        public final UUID id;
        public String name;
        public int lives = Geooda1.MAX_LIVES;
        public Status status = Status.ALIVE;
        public long expireAt;
        public String bodyDim = "";
        public UUID bodyId;
        public boolean pending;          // revivido mientras estaba desconectado
        public String pendDim = "";
        public BlockPos pendPos = BlockPos.ZERO;

        public Rec(UUID id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public final Map<UUID, Rec> map = new LinkedHashMap<>();

    public static LivesData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(LivesData::load, LivesData::new, "geooda1_lives");
    }

    public Rec rec(ServerPlayer p) {
        String name = p.getGameProfile().getName();
        Rec r = map.get(p.getUUID());
        if (r == null) {
            r = new Rec(p.getUUID(), name);
            map.put(r.id, r);
            setDirty();
        } else if (!r.name.equals(name)) {
            r.name = name;
            setDirty();
        }
        return r;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Rec r : map.values()) {
            CompoundTag t = new CompoundTag();
            t.putUUID("id", r.id);
            t.putString("name", r.name);
            t.putInt("lives", r.lives);
            t.putString("status", r.status.name());
            t.putLong("expireAt", r.expireAt);
            t.putString("bodyDim", r.bodyDim);
            if (r.bodyId != null) t.putUUID("bodyId", r.bodyId);
            t.putBoolean("pending", r.pending);
            t.putString("pendDim", r.pendDim);
            t.putLong("pendPos", r.pendPos.asLong());
            list.add(t);
        }
        tag.put("players", list);
        return tag;
    }

    public static LivesData load(CompoundTag tag) {
        LivesData d = new LivesData();
        ListTag list = tag.getList("players", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag t = list.getCompound(i);
            Rec r = new Rec(t.getUUID("id"), t.getString("name"));
            r.lives = t.getInt("lives");
            try { r.status = Status.valueOf(t.getString("status")); } catch (IllegalArgumentException ignored) { }
            r.expireAt = t.getLong("expireAt");
            r.bodyDim = t.getString("bodyDim");
            if (t.hasUUID("bodyId")) r.bodyId = t.getUUID("bodyId");
            r.pending = t.getBoolean("pending");
            r.pendDim = t.getString("pendDim");
            r.pendPos = BlockPos.of(t.getLong("pendPos"));
            d.map.put(r.id, r);
        }
        return d;
    }
}
