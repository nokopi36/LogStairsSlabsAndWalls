package com.nokopi.logstairsslabsandwalls.block;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * 面ごとの樹皮剥ぎ（SPEC 3.2）。パネル・階段・ハーフ・塀で共通。
 * 剥ぎ状態はブロックエンティティ（StrippedFacesBlockEntity）に持つ（SPEC 3.3.1）。
 * 方角はワールドの向きで、クリックした面の向きを向いた面がまとめて剥がれる。
 */
public final class FaceStripping {
    // 1 面剥ぐごとの斧の耐久消費（バニラの原木と同じ）
    private static final int AXE_DAMAGE_PER_STRIP = 1;

    private FaceStripping() {
    }

    // バニラの斧と同じく、斧の BlockTransformer を持つアイテムを斧とみなす（Item.Properties#axe で付く）
    public static boolean isAxe(ItemStack stack) {
        Holder<BlockTransformer> transformer = stack.get(DataComponents.BLOCK_TRANSFORMER);
        return transformer != null && transformer.is(BlockTransformers.AXE);
    }

    /**
     * 斧でクリックした面の向きの樹皮を剥ぐ。斧でなければ null（呼び出し元で通常の処理に回す）。
     */
    public static @Nullable InteractionResult stripWithAxe(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hitResult) {
        if (!isAxe(stack)) {
            return null;
        }
        return strip(StrippedFace.of(hitResult.getDirection()), state, level, pos, player, stack, hand);
    }

    /**
     * face の面を剥いで、バニラの原木と同じ音・ゲームイベント・斧の耐久消費を起こす。
     * 剥がれ済み、またはブロックエンティティがなければ何もせず、通常の右クリックに回す。
     */
    public static InteractionResult strip(StrippedFace face, BlockState state, Level level, BlockPos pos, Player player,
            ItemStack stack, InteractionHand hand) {
        if (!(level.getBlockEntity(pos) instanceof StrippedFacesBlockEntity blockEntity) || blockEntity.isStripped(face)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        blockEntity.strip(face);
        level.playSound(player, pos, SoundEvents.AXE_STRIP.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state));
        stack.hurtAndBreak(AXE_DAMAGE_PER_STRIP, player, hand.asEquipmentSlot());
        return InteractionResult.SUCCESS;
    }
}
