package com.nokopi.logstairsslabsandwalls.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 原木のハーフブロック（SPEC 3.1）。縦置きの原木の見た目で、斧でクリックした面だけ樹皮を剥げる（SPEC 3.2）。
 * 剥ぎ状態はブロックエンティティに持つ（SPEC 3.3.1）。
 */
public class LogSlabBlock extends SlabBlock implements EntityBlock {
    private final boolean flammable;

    public LogSlabBlock(Properties properties, boolean flammable) {
        super(properties);
        this.flammable = flammable;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StrippedFacesBlockEntity(pos, state);
    }

    /**
     * 2 枚重ねにしたとき（同じブロックのまま状態だけ変わるので、ブロックエンティティは残る）、側面の剥ぎ状態は元のハーフから引き継ぐ。
     * 新しいハーフで隠れる面（下置きなら上面、上置きなら下面）は、表に出る新しいハーフの面に合わせて樹皮付きに戻す。
     */
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (oldState.is(this) && oldState.getValue(TYPE) != SlabType.DOUBLE && state.getValue(TYPE) == SlabType.DOUBLE
                && level.getBlockEntity(pos) instanceof StrippedFacesBlockEntity blockEntity) {
            StrippedFace covered = oldState.getValue(TYPE) == SlabType.BOTTOM ? StrippedFace.UP : StrippedFace.DOWN;
            blockEntity.setStrippedMask(blockEntity.strippedMask() & ~covered.bit());
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hitResult) {
        InteractionResult result = FaceStripping.stripWithAxe(stack, state, level, pos, player, hand, hitResult);
        return result != null ? result : super.useItemOn(stack, state, level, pos, player, hand, hitResult);
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
