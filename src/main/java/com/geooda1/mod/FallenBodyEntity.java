package com.geooda1.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** El cuerpo caido de un jugador: invulnerable, se puede empujar y muestra el contador arriba. */
public class FallenBodyEntity extends LivingEntity {
    private static final EntityDataAccessor<Optional<UUID>> OWNER =
            SynchedEntityData.defineId(FallenBodyEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private String ownerName = "?";

    public FallenBodyEntity(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(OWNER, Optional.empty());
    }

    public void setOwner(UUID id, String name) {
        this.entityData.set(OWNER, Optional.of(id));
        this.ownerName = name;
    }

    public UUID getOwner() {
        return this.entityData.get(OWNER).orElse(null);
    }

    public void setTimer(long ms) {
        long total = Math.max(0, ms) / 1000;
        String t = String.format("%02d:%02d", total / 60, total % 60);
        setCustomName(Component.literal("§f" + ownerName + " §c" + t));
        setCustomNameVisible(true);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (isOnFire()) clearFire();
        if (getY() < level().getMinBuildHeight() - 8) {
            BlockPos s = ((ServerLevel) level()).getSharedSpawnPos();
            teleportTo(s.getX() + 0.5, s.getY() + 1, s.getZ() + 0.5);
            setDeltaMovement(Vec3.ZERO);
        }
        if (tickCount % 40 == 0 && level().getServer() != null) {
            UUID owner = getOwner();
            LivesData d = LivesData.get(level().getServer());
            LivesData.Rec r = owner == null ? null : d.map.get(owner);
            if (r == null || r.status != LivesData.Status.DOWNED || !getUUID().equals(r.bodyId)) discard();
        }
    }

    /** Nadie puede matarlo, pero los golpes lo empujan de lugar. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker != null && !level().isClientSide) {
            knockback(0.5, attacker.getX() - getX(), attacker.getZ() - getZ());
            this.hurtMarked = true;
        }
        return false;
    }

    /** Click derecho: lo empujas. */
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide) {
            knockback(0.6, player.getX() - getX(), player.getZ() - getZ());
            this.hurtMarked = true;
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    public float getNameTagOffsetY() {
        return 1.1F;
    }

    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        UUID o = getOwner();
        if (o != null) tag.putUUID("Owner", o);
        tag.putString("OwnerName", ownerName);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) this.entityData.set(OWNER, Optional.of(tag.getUUID("Owner")));
        this.ownerName = tag.getString("OwnerName");
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return List.of();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }
}
