package com.geooda1.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HeartJarBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 14, 13);

    public HeartJarBlock() {
        super(BlockBehaviour.Properties.of().strength(0.5F).sound(SoundType.GLASS).noOcclusion().lightLevel(s -> 8));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(Geooda1.GOLDEN_HEART.get())) {
            player.displayClientMessage(Component.literal("§eNecesitas un Corazón Dorado en la mano para usar el frasco."), true);
            return InteractionResult.CONSUME;
        }
        if (player instanceof ServerPlayer sp) {
            ServerEvents.sync(sp);
            ModNet.toPlayer(sp, new ModNet.OpenScreen(2, pos));
        }
        return InteractionResult.CONSUME;
    }
}
