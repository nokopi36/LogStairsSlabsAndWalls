package com.nokopi.logstairsslabsandwalls.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 原木の塀（SPEC 3.1）。縦置きの原木の見た目で、斧でクリックした面の向きの面（柱と壁）がまとめて剥がれる（SPEC 3.2）。
 */
public class LogWallBlock extends WallBlock {
    private final boolean flammable;

    public LogWallBlock(Properties properties, boolean flammable) {
        super(properties);
        this.flammable = flammable;
        this.registerDefaultState(FaceStripping.withAllBark(this.defaultBlockState()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        FaceStripping.addProperties(builder);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hitResult) {
        InteractionResult result = FaceStripping.stripWithAxe(stack, state, level, pos, player, hand, hitResult);
        return result != null ? result : super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return FaceStripping.rotate(state, super.rotate(state, rotation), rotation);
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return FaceStripping.mirror(state, super.mirror(state, mirror), mirror);
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.flammable ? LogBlockProperties.FLAMMABILITY : 0;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.flammable ? LogBlockProperties.FIRE_SPREAD_SPEED : 0;
    }
}
