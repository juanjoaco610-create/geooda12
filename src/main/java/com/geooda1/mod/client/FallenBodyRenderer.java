package com.geooda1.mod.client;

import com.geooda1.mod.FallenBodyEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** Dibuja al jugador caido tirado en el suelo, con su propio skin. */
public class FallenBodyRenderer extends LivingEntityRenderer<FallenBodyEntity, PlayerModel<FallenBodyEntity>> {
    public FallenBodyRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(FallenBodyEntity entity) {
        UUID id = entity.getOwner();
        if (id == null) return DefaultPlayerSkin.getDefaultSkin();
        var conn = Minecraft.getInstance().getConnection();
        PlayerInfo info = conn == null ? null : conn.getPlayerInfo(id);
        return info != null ? info.getSkinLocation() : DefaultPlayerSkin.getDefaultSkin(id);
    }

    @Override
    protected void setupRotations(FallenBodyEntity entity, PoseStack ps, float bob, float yaw, float partialTick) {
        super.setupRotations(entity, ps, bob, yaw, partialTick);
        // misma postura "acostado" que usa el jugador al nadar
        ps.mulPose(Axis.XP.rotationDegrees(-90.0F));
        ps.translate(0.0D, -1.0D, 0.3D);
    }
}
