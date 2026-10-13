package com.nokopi.logstairsslabsandwalls.block;

import com.nokopi.logstairsslabsandwalls.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;

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
    // 剥いだ版のハーフを重ねている途中か（保存しない）。NeoForge はアイテムで置くとき onPlace を置いた後に呼ぶので、
    // アイテムからハーフの onPlace へ「表に出る面を剥がれた状態にする」ことを伝える
    private boolean stackingStrippedSlab;

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

    // そのブロックが持つ面のすべて: 階段は 8 面（段の内側の 2 面を含む）、ほかは外周の 6 面
    public static int allFacesMask(BlockState state) {
        int mask = 0;
        for (StrippedFace face : StrippedFace.values()) {
            if (face.isOuter() || state.getBlock() instanceof LogStairBlock) {
                mask |= face.bit();
            }
        }
        return mask;
    }

    public void markStackingStrippedSlab() {
        this.stackingStrippedSlab = true;
    }

    // 重ねている途中の印を読んで消す
    public boolean consumeStackingStrippedSlab() {
        boolean stacking = this.stackingStrippedSlab;
        this.stackingStrippedSlab = false;
        return stacking;
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

    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(STRIPPED_MASK, this.strippedMask).build();
    }
}
