package com.nokopi.logstairsslabsandwalls.block;

import com.nokopi.logstairsslabsandwalls.item.StrippedLogBlockItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

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
     * 2 枚重ねにするとき（同じブロックのまま状態だけ変わるので、ブロックエンティティは残る）、どちらのハーフを重ねるかを印に残す。
     * 置く処理はクライアントとサーバーの両方で動くので、手元でもすぐ正しい見た目になる（onPlace はサーバーでしか呼ばれない）。
     */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState existing = context.getLevel().getBlockState(context.getClickedPos());
        BlockState result = super.getStateForPlacement(context);
        if (result != null && existing.is(this) && result.getValue(TYPE) == SlabType.DOUBLE
                && context.getLevel().getBlockEntity(context.getClickedPos()) instanceof StrippedFacesBlockEntity blockEntity) {
            // 先に置いたのが下置きなら、新しいのは上のハーフ
            blockEntity.markStackingSlab(existing.getValue(TYPE) == SlabType.BOTTOM);
        }
        return result;
    }

    /**
     * 2 枚重ねにしたとき、先に置いたハーフの面はそのまま。新しいハーフの面（側面 4 と、表に出る上面か下面）は重ねたアイテムに合わせる:
     * 普通の版を重ねたら樹皮付き、剥いだ版を重ねたら剥がれた状態。
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack itemStack) {
        super.setPlacedBy(level, pos, state, by, itemStack);
        if (level.getBlockEntity(pos) instanceof StrippedFacesBlockEntity blockEntity) {
            Boolean upper = blockEntity.consumeStackingSlab();
            if (upper != null && state.getValue(TYPE) == SlabType.DOUBLE) {
                int newHalf = StrippedFacesBlockEntity.slabHalfMask(state, upper);
                blockEntity.setStrippedMask(itemStack.getItem() instanceof StrippedLogBlockItem
                        ? blockEntity.strippedMask() | newHalf
                        : blockEntity.strippedMask() & ~newHalf);
            }
        }
    }

    /**
     * 剥いだ版のハーフでも 2 枚重ねにできる（バニラは同じアイテムのときだけ）。判定はバニラの SlabBlock#canBeReplaced と同じ。
     */
    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        ItemStack stack = context.getItemInHand();
        if (!(stack.getItem() instanceof StrippedLogBlockItem item) || item.getBlock() != this) {
            return super.canBeReplaced(state, context);
        }
        SlabType type = state.getValue(TYPE);
        if (type == SlabType.DOUBLE) {
            return false;
        } else if (context.replacingClickedOnBlock()) {
            boolean above = context.getClickLocation().y - context.getClickedPos().getY() > 0.5;
            Direction clickedFace = context.getClickedFace();
            return type == SlabType.BOTTOM
                    ? clickedFace == Direction.UP || above && clickedFace.getAxis().isHorizontal()
                    : clickedFace == Direction.DOWN || !above && clickedFace.getAxis().isHorizontal();
        } else {
            return true;
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hitResult) {
        if (!FaceStripping.isAxe(stack)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }
        double localY = hitResult.getLocation().y - pos.getY();
        return FaceStripping.strip(strippedFaceAt(state, hitResult.getDirection(), localY), state, level, pos, player, stack, hand);
    }

    /**
     * クリックした面。側面は、上置きと 2 枚重ねの上半分（クリックした高さが半分より上）なら上のハーフの側面。
     */
    public static StrippedFace strippedFaceAt(BlockState state, Direction face, double localY) {
        if (face.getAxis().isVertical()) {
            return StrippedFace.of(face);
        }
        SlabType type = state.getValue(TYPE);
        boolean upper = type == SlabType.TOP || type == SlabType.DOUBLE && localY > 0.5;
        return upper ? StrippedFace.upper(face) : StrippedFace.of(face);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        return FaceStripping.cloneItemStack(super.getCloneItemStack(level, pos, state, includeData, player), level, pos, state);
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
