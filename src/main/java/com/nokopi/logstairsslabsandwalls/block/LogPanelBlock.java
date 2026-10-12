package com.nokopi.logstairsslabsandwalls.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 原木パネル（SPEC 3.1）。バニラの原木と同じく axis を持ち、置いた面に合わせて縦・横に置ける。
 */
public class LogPanelBlock extends RotatedPillarBlock {
    private final boolean flammable;

    public LogPanelBlock(Properties properties, boolean flammable) {
        super(properties);
        this.flammable = flammable;
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
