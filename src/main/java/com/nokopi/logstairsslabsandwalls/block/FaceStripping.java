package com.nokopi.logstairsslabsandwalls.block;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockTransformers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * 面ごとの樹皮剥ぎ（SPEC 3.2）。パネル・階段・ハーフ・塀で共通。
 * 方角はワールドの向きで、クリックした面の向きを向いた面がまとめて剥がれる。
 */
public final class FaceStripping {
    private static final Map<Direction, BooleanProperty> STRIPPED = new EnumMap<>(Direction.class);

    static {
        for (Direction direction : Direction.values()) {
            STRIPPED.put(direction, BooleanProperty.create(direction.getSerializedName() + "_stripped"));
        }
    }

    // 1 面剥ぐごとの斧の耐久消費（バニラの原木と同じ）
    private static final int AXE_DAMAGE_PER_STRIP = 1;

    private FaceStripping() {
    }

    public static BooleanProperty strippedProperty(Direction direction) {
        return STRIPPED.get(direction);
    }

    public static void addProperties(StateDefinition.Builder<Block, BlockState> builder) {
        for (Direction direction : Direction.values()) {
            builder.add(strippedProperty(direction));
        }
    }

    public static BlockState withAllBark(BlockState state) {
        for (Direction direction : Direction.values()) {
            state = state.setValue(strippedProperty(direction), false);
        }
        return state;
    }

    public static boolean isStripped(BlockState state, Direction direction) {
        return state.getValue(strippedProperty(direction));
    }

    // バニラの斧と同じく、斧の BlockTransformer を持つアイテムを斧とみなす（Item.Properties#axe で付く）
    public static boolean isAxe(ItemStack stack) {
        Holder<BlockTransformer> transformer = stack.get(DataComponents.BLOCK_TRANSFORMER);
        return transformer != null && transformer.is(BlockTransformers.AXE);
    }

    /**
     * 斧でクリックした面の樹皮を剥ぐ。斧でなければ null（呼び出し元で通常の処理に回す）。
     */
    public static @Nullable InteractionResult stripWithAxe(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hitResult) {
        if (!isAxe(stack)) {
            return null;
        }
        return strip(state, strippedProperty(hitResult.getDirection()), level, pos, player, stack, hand);
    }

    /**
     * 剥ぎ状態 property の面を剥いで、バニラの原木と同じ音・ゲームイベント・斧の耐久消費を起こす。
     * 剥がれ済みなら何もせず、通常の右クリックに回す。
     */
    public static InteractionResult strip(BlockState state, BooleanProperty property, Level level, BlockPos pos, Player player,
            ItemStack stack, InteractionHand hand) {
        if (state.getValue(property)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        BlockState stripped = state.setValue(property, true);
        level.setBlock(pos, stripped, Block.UPDATE_ALL_IMMEDIATE);
        playStripEffects(level, pos, player, stack, hand, stripped);
        return InteractionResult.SUCCESS;
    }

    /**
     * ブロックエンティティに剥ぎ状態を持つブロック用（SPEC 3.3.1）。face の面を剥いで、同じ音・ゲームイベント・耐久消費を起こす。
     * 剥がれ済み、またはブロックエンティティがなければ何もせず、通常の右クリックに回す。
     */
    public static InteractionResult strip(StrippedFace face, BlockState state, Level level, BlockPos pos, Player player,
            ItemStack stack, InteractionHand hand) {
        if (!(level.getBlockEntity(pos) instanceof StrippedFacesBlockEntity blockEntity) || blockEntity.isStripped(face)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        blockEntity.strip(face);
        playStripEffects(level, pos, player, stack, hand, state);
        return InteractionResult.SUCCESS;
    }

    // バニラの原木を剥ぐときと同じ音・ゲームイベント・斧の耐久消費
    private static void playStripEffects(Level level, BlockPos pos, Player player, ItemStack stack, InteractionHand hand, BlockState state) {
        level.playSound(player, pos, SoundEvents.AXE_STRIP.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state));
        stack.hurtAndBreak(AXE_DAMAGE_PER_STRIP, player, hand.asEquipmentSlot());
    }

    // ストラクチャーなどでブロックを回転・反転したとき、剥いだ面も一緒に動かす
    public static BlockState rotate(BlockState original, BlockState rotated, Rotation rotation) {
        for (Direction direction : Direction.values()) {
            rotated = rotated.setValue(strippedProperty(rotation.rotate(direction)), isStripped(original, direction));
        }
        return rotated;
    }

    public static BlockState mirror(BlockState original, BlockState mirrored, Mirror mirror) {
        for (Direction direction : Direction.values()) {
            mirrored = mirrored.setValue(strippedProperty(mirror.mirror(direction)), isStripped(original, direction));
        }
        return mirrored;
    }
}
