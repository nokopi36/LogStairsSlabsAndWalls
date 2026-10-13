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
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 原木の階段（SPEC 3.1）。縦置きの原木の見た目で、斧で面ごとに樹皮を剥げる（SPEC 3.2）。
 * ブロックの外周の面はワールドの 6 方向、段の内側の面は踏み面と蹴上げの 2 つで、合わせて 8 つの剥ぎ状態を持つ。
 * 剥ぎ状態は blockstate ではなくブロックエンティティに持つ（SPEC 3.3.1 の試作）。
 */
public class LogStairBlock extends StairBlock implements EntityBlock {
    // 段の内側の面はブロックの中央（0.5）にある。クリック位置が外周からこれ以上離れていれば内側の面とみなす
    private static final double INNER_FACE_MARGIN = 0.25;

    private final boolean flammable;

    public LogStairBlock(BlockState baseState, Properties properties, boolean flammable) {
        super(baseState, properties);
        this.flammable = flammable;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StrippedFacesBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hitResult) {
        if (!FaceStripping.isAxe(stack)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }
        Vec3 local = hitResult.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
        return FaceStripping.strip(strippedFaceAt(hitResult.getDirection(), local), state, level, pos, player, stack, hand);
    }

    /**
     * クリックした面。face はクリックした面の向き、local はブロック内のクリック位置（0〜1）。
     * 外周の面ならその向き、段の内側の面なら上下向きが踏み面・横向きが蹴上げ。
     */
    public static StrippedFace strippedFaceAt(Direction face, Vec3 local) {
        double position = local.get(face.getAxis());
        boolean onBoundary = face.getAxisDirection() == Direction.AxisDirection.POSITIVE
                ? position > 1.0 - INNER_FACE_MARGIN
                : position < INNER_FACE_MARGIN;
        if (onBoundary) {
            return StrippedFace.of(face);
        }
        return face.getAxis() == Direction.Axis.Y ? StrippedFace.TREAD : StrippedFace.RISER;
    }

    // TODO(SPEC 3.3.1): ストラクチャーで回転・反転したとき、ブロックエンティティの剥いだ面は一緒に回らない（試作では後回し）

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.flammable ? LogBlockProperties.FLAMMABILITY : 0;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.flammable ? LogBlockProperties.FIRE_SPREAD_SPEED : 0;
    }
}
