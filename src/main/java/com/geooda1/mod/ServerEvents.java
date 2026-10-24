package com.geooda1.mod;

import com.geooda1.mod.LivesData.Rec;
import com.geooda1.mod.LivesData.Status;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerEvents {

    // ------------------------------------------------------------------ utilidades

    public static void chat(MinecraftServer s, String msg) {
        s.getPlayerList().broadcastSystemMessage(Component.literal("§6[geooda1] §f" + msg), false);
    }

    public static List<PlayerEntry> entries(MinecraftServer s) {
        LivesData d = LivesData.get(s);
        long now = System.currentTimeMillis();
        List<PlayerEntry> list = new ArrayList<>();
        for (Rec r : d.map.values()) {
            int st = r.status == Status.ALIVE ? 0 : r.status == Status.DOWNED ? 1 : 2;
            long rem = st == 1 ? Math.max(0, r.expireAt - now) : 0;
            list.add(new PlayerEntry(r.id, r.name, r.lives, st, rem));
        }
        return list;
    }

    public static void sync(ServerPlayer p) {
        ModNet.toPlayer(p, new ModNet.Sync(entries(p.getServer())));
    }

    public static void syncAll(MinecraftServer s) {
        ModNet.toAll(new ModNet.Sync(entries(s)));
    }

    private static Entity findBody(MinecraftServer s, Rec r) {
        if (r.bodyId == null || r.bodyDim.isEmpty()) return null;
        try {
            ServerLevel l = s.getLevel(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(r.bodyDim)));
            return l == null ? null : l.getEntity(r.bodyId);
        } catch (Exception ex) {
            return null;
        }
    }

    private static Vec3 pickSpot(ServerLevel l, BlockPos jar) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos p = jar.relative(dir);
            if (l.getBlockState(p).isAir() && l.getBlockState(p.above()).isAir()) return Vec3.atBottomCenterOf(p);
        }
        return Vec3.atBottomCenterOf(jar.above());
    }

    // ------------------------------------------------------------------ muertes

    @SubscribeEvent
    public void onDeath(LivingDeathEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        MinecraftServer server = p.getServer();
        if (server == null) return;
        LivesData d = LivesData.get(server);
        Rec r = d.rec(p);
        if (r.status != Status.ALIVE) return;

        r.lives--;
        d.setDirty();

        if (r.lives > 0) {
            chat(server, "§c" + r.name + "§f perdió una vida. Le queda §c" + r.lives + "§f.");
            syncAll(server);
            return;
        }

        // Perdio la ultima vida: queda caido (la muerte real se cancela)
        e.setCanceled(true);
        ServerLevel level = p.serverLevel();
        if (p.getY() < level.getMinBuildHeight()) {
            BlockPos sp = level.getSharedSpawnPos();
            p.teleportTo(level, sp.getX() + 0.5, sp.getY() + 1, sp.getZ() + 0.5, p.getYRot(), p.getXRot());
        }
        p.getInventory().dropAll();
        p.setHealth(p.getMaxHealth());
        p.getFoodData().setFoodLevel(20);
        p.removeAllEffects();
        p.clearFire();
        p.fallDistance = 0;
        p.setAirSupply(p.getMaxAirSupply());

        FallenBodyEntity body = new FallenBodyEntity(Geooda1.FALLEN_BODY.get(), level);
        body.setOwner(p.getUUID(), r.name);
        body.moveTo(p.getX(), p.getY(), p.getZ(), p.getYRot(), 0);
        body.setTimer(Geooda1.DOWNED_MILLIS);
        level.addFreshEntity(body);

        r.status = Status.DOWNED;
        r.expireAt = System.currentTimeMillis() + Geooda1.DOWNED_MILLIS;
        r.bodyDim = level.dimension().location().toString();
        r.bodyId = body.getUUID();
        d.setDirty();

        p.setGameMode(GameType.SPECTATOR);
        p.setCamera(body);
        chat(server, "§c" + r.name + "§f perdió su última vida y quedó caído. Tiene §e1 hora§f para que lo revivan.");
        syncAll(server);
        ModNet.toPlayer(p, new ModNet.OpenScreen(0, BlockPos.ZERO));
    }

    private static void finalDeath(MinecraftServer s, LivesData d, Rec r, ServerPlayer p, Entity body) {
        r.status = Status.DEAD;
        r.lives = 0;
        r.bodyId = null;
        d.setDirty();
        if (body != null) {
            ServerLevel l = (ServerLevel) body.level();
            l.explode(null, body.getX(), body.getY() + 0.3, body.getZ(), 3.0F, Level.ExplosionInteraction.MOB);
            body.discard();
        }
        chat(s, "§4" + r.name + "§c murió definitivamente§f.");
        if (p != null) {
            p.setCamera(p);
            p.setGameMode(GameType.SPECTATOR);
            ModNet.toPlayer(p, new ModNet.OpenScreen(1, BlockPos.ZERO));
        }
        syncAll(s);
    }

    // ------------------------------------------------------------------ contador

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        MinecraftServer s = e.getServer();
        if (s.getTickCount() % 20 != 0) return;
        LivesData d = LivesData.get(s);
        long now = System.currentTimeMillis();
        boolean anyDowned = false;
        for (Rec r : new ArrayList<>(d.map.values())) {
            if (r.status != Status.DOWNED) continue;
            long rem = r.expireAt - now;
            Entity body = findBody(s, r);
            ServerPlayer p = s.getPlayerList().getPlayer(r.id);
            if (rem <= 0) {
                finalDeath(s, d, r, p, body);
                continue;
            }
            anyDowned = true;
            if (body instanceof FallenBodyEntity fb) fb.setTimer(rem);
            if (p != null && body != null
                    && (p.level() != body.level() || p.distanceToSqr(body) > 400)) {
                p.teleportTo((ServerLevel) body.level(), body.getX(), body.getY() + 1, body.getZ(), p.getYRot(), p.getXRot());
                p.setCamera(body);
            }
        }
        if (anyDowned && s.getTickCount() % 200 == 0) syncAll(s);
    }

    // ------------------------------------------------------------------ conexion

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        MinecraftServer s = p.getServer();
        if (s == null) return;
        LivesData d = LivesData.get(s);
        Rec r = d.rec(p);
        if (r.status == Status.ALIVE && r.pending) {
            applyRevive(s, p, r);
        } else if (r.status == Status.DOWNED) {
            p.setGameMode(GameType.SPECTATOR);
            Entity body = findBody(s, r);
            if (body != null) {
                p.teleportTo((ServerLevel) body.level(), body.getX(), body.getY() + 1, body.getZ(), p.getYRot(), p.getXRot());
                p.setCamera(body);
            }
            ModNet.toPlayer(p, new ModNet.OpenScreen(0, BlockPos.ZERO));
        } else if (r.status == Status.DEAD) {
            p.setGameMode(GameType.SPECTATOR);
            ModNet.toPlayer(p, new ModNet.OpenScreen(1, BlockPos.ZERO));
        }
        syncAll(s);
    }

    // ------------------------------------------------------------------ revivir

    public static void applyRevive(MinecraftServer s, ServerPlayer p, Rec r) {
        ServerLevel lvl = null;
        try {
            lvl = s.getLevel(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(r.pendDim)));
        } catch (Exception ignored) {
        }
        if (lvl == null) lvl = s.overworld();
        Vec3 spot = pickSpot(lvl, r.pendPos);
        p.setCamera(p);
        p.setGameMode(GameType.SURVIVAL);
        p.teleportTo(lvl, spot.x, spot.y, spot.z, p.getYRot(), 0);
        p.setHealth(p.getMaxHealth());
        p.getFoodData().setFoodLevel(20);
        r.pending = false;
        LivesData.get(s).setDirty();
    }

    public static void handleRevive(ServerPlayer reviver, UUID target, BlockPos jar) {
        MinecraftServer s = reviver.getServer();
        if (s == null) return;
        ServerLevel level = reviver.serverLevel();
        if (!level.getBlockState(jar).is(Geooda1.HEART_JAR.get())
                || reviver.distanceToSqr(Vec3.atCenterOf(jar)) > 64) return;

        InteractionHand hand = reviver.getMainHandItem().is(Geooda1.GOLDEN_HEART.get()) ? InteractionHand.MAIN_HAND
                : reviver.getOffhandItem().is(Geooda1.GOLDEN_HEART.get()) ? InteractionHand.OFF_HAND : null;
        if (hand == null) {
            reviver.displayClientMessage(Component.literal("§cNecesitas un Corazón Dorado en la mano."), true);
            return;
        }

        LivesData d = LivesData.get(s);
        Rec r = d.map.get(target);
        if (r == null || r.status != Status.DOWNED || r.expireAt <= System.currentTimeMillis()) {
            reviver.displayClientMessage(Component.literal("§cEse jugador ya no se puede revivir."), true);
            sync(reviver);
            return;
        }

        if (!reviver.getAbilities().instabuild) reviver.getItemInHand(hand).shrink(1);

        Entity body = findBody(s, r);
        if (body != null) body.discard();

        r.status = Status.ALIVE;
        r.lives = Geooda1.MAX_LIVES;
        r.expireAt = 0;
        r.bodyId = null;
        r.pending = true;
        r.pendDim = level.dimension().location().toString();
        r.pendPos = jar;
        d.setDirty();

        // Rayo + columna de luz tipo beacon
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(Vec3.atBottomCenterOf(jar));
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }
        level.sendParticles(ParticleTypes.END_ROD, jar.getX() + 0.5, jar.getY() + 6, jar.getZ() + 0.5,
                120, 0.15, 6, 0.15, 0.01);

        ServerPlayer revived = s.getPlayerList().getPlayer(target);
        if (revived != null) applyRevive(s, revived, r);

        chat(s, "§a" + r.name + "§f fue revivido por §e" + reviver.getGameProfile().getName() + "§f.");
        syncAll(s);
    }

    // ------------------------------------------------------------------ Wither

    @SubscribeEvent
    public void onDrops(LivingDropsEvent e) {
        if (e.getEntity() instanceof WitherBoss w) {
            e.getDrops().add(new ItemEntity(w.level(), w.getX(), w.getY() + 1, w.getZ(),
                    new ItemStack(Geooda1.GOLDEN_HEART.get())));
        }
    }
}
