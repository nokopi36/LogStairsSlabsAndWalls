package com.nokopi.logstairsslabsandwalls.block;

import com.nokopi.logstairsslabsandwalls.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;
import org.jspecify.annotations.Nullable;

/**
 * 剥ぎ状態をブロックごとに持つブロックエンティティ（SPEC 3.3.1）。
 * blockstate に持たせると状態の数が 64〜256 倍になるため、ビットの集まり（StrippedFace）として保存し、
 * クライアントでは ModelData で描画に渡す。
 */
public class StrippedFacesBlockEntity extends BlockEntity {
    // 描画に渡す剥ぎ状態（StrippedFace のビットの集まり）
    public static final ModelProperty<Integer> STRIPPED_MASK = new ModelProperty<>();
    private static final String TAG_STRIPPED = "stripped_faces";

    private int strippedMask;
    // ハーフを重ねている途中なら、新しく重ねるのが上のハーフか（保存しない。LogSlabBlock が置く前に付け、置いた後に読む）
    private @Nullable Boolean stackingUpperSlab;

    public StrippedFacesBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STRIPPED_FACES.get(), pos, state);
    }

    public int strippedMask() {
        return this.strippedMask;
    }

    public boolean isStripped(StrippedFace face) {
        return face.isStrippedIn(this.strippedMask);
    }

    // すべての面が剥がれているか（剥いだ版のドロップ・ピックブロック。SPEC 3.5）
    public boolean isAllStripped() {
        int all = allFacesMask(this.getBlockState());
        return (this.strippedMask & all) == all;
    }

    // そのブロックが持つ面のすべて: 階段は 8 面（段の内側の 2 面を含む）、ハーフは置き方による（2 枚重ねは 10 面）、ほかは外周の 6 面
    public static int allFacesMask(BlockState state) {
        if (state.getBlock() instanceof LogStairBlock) {
            return outerFacesMask() | StrippedFace.TREAD.bit() | StrippedFace.RISER.bit();
        }
        if (state.getBlock() instanceof SlabBlock) {
            SlabType type = state.getValue(SlabBlock.TYPE);
            return (type != SlabType.TOP ? slabHalfMask(state, false) : 0) | (type != SlabType.BOTTOM ? slabHalfMask(state, true) : 0);
        }
        return outerFacesMask();
    }

    /**
     * ハーフの上下どちらかのハーフの面（SPEC 3.2）。下のハーフは下面と下半分の側面、上のハーフは上面と上半分の側面。
     * 1 枚だけのときは、もう一方の向きの面（下置きの上面・上置きの下面）も含む。
     */
    public static int slabHalfMask(BlockState state, boolean upper) {
        SlabType type = state.getValue(SlabBlock.TYPE);
        if (upper) {
            return StrippedFace.UP.bit() | StrippedFace.UPPER_SIDES | (type == SlabType.TOP ? StrippedFace.DOWN.bit() : 0);
        }
        return StrippedFace.DOWN.bit() | StrippedFace.LOWER_SIDES | (type == SlabType.BOTTOM ? StrippedFace.UP.bit() : 0);
    }

    // そのハーフの面がすべて剥がれているか（2 枚重ねのドロップ）
    public boolean isSlabHalfStripped(boolean upper) {
        int half = slabHalfMask(this.getBlockState(), upper);
        return (this.strippedMask & half) == half;
    }

    private static int outerFacesMask() {
        int mask = 0;
        for (Direction direction : Direction.values()) {
            mask |= StrippedFace.of(direction).bit();
        }
        return mask;
    }

    public void markStackingSlab(boolean upper) {
        this.stackingUpperSlab = upper;
    }

    public boolean isStackingSlab() {
        return this.stackingUpperSlab != null;
    }

    // 重ねている途中の印を読んで消す（重ねていなければ null）
    public @Nullable Boolean consumeStackingSlab() {
        Boolean upper = this.stackingUpperSlab;
        this.stackingUpperSlab = null;
        return upper;
    }

    // 面を剥いで保存し、クライアントへ送る。クライアント側でも同じ処理を呼んで、すぐ見た目を変える
    public void strip(StrippedFace face) {
        this.setStrippedMask(this.strippedMask | face.bit());
    }

    public void setStrippedMask(int mask) {
        this.strippedMask = mask;
        this.setChanged();
        if (this.level != null) {
            BlockState state = this.getBlockState();
            if (this.level.isClientSide()) {
                this.requestModelDataUpdate();
            }
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(TAG_STRIPPED, this.strippedMask);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.strippedMask = input.getIntOr(TAG_STRIPPED, 0);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    // サーバーから剥ぎ状態が届いたら、描画用のデータを更新して描き直す
    @Override
    public void onDataPacket(Connection connection, ValueInput input) {
        super.onDataPacket(connection, input);
        if (this.level != null) {
            this.requestModelDataUpdate();
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_IMMEDIATE);
        }
    }

    /**
     * ストラクチャーで回転・反転して置かれたとき、剥いだ面もブロックと一緒に回す（方角はワールドの向きなので。SPEC 3.2）。
     * ワールド生成の別スレッドから呼ばれることがあるので、自分の値だけを書き換える（level には触らない）。
     */
    @Override
    public void applyStructureRotation(Mirror mirror, Rotation rotation) {
        this.strippedMask = StrippedFace.transform(this.strippedMask, mirror, rotation);
    }

    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(STRIPPED_MASK, this.strippedMask).build();
    }
}
