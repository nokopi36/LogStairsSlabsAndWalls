package com.nokopi.logstairsslabsandwalls.item;

import java.util.Map;

import com.nokopi.logstairsslabsandwalls.block.StrippedFacesBlockEntity;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 樹皮を剥いだ版のパネル・階段・ハーフ・塀（SPEC 3.5）。ブロックは普通の版と共通で、置くと全面が剥がれた状態になる。
 */
public class StrippedLogBlockItem extends BlockItem {
    public StrippedLogBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    /**
     * 置いた後に全面を剥がれた状態にする（ブロックエンティティは placeBlock の中で作られる）。
     * ハーフを重ねて 2 枚重ねにするときは、側面は先に置いたハーフのまま。表に出る新しいハーフの面は LogSlabBlock#onPlace が
     * 剥がれた状態にする（NeoForge はアイテムで置くとき onPlace を置いた後に呼ぶので、ここでは印を付けるだけ）。
     */
    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState placementState) {
        boolean stackingSlab = context.getLevel().getBlockState(context.getClickedPos()).is(this.getBlock())
                && placementState.getBlock() instanceof SlabBlock;
        if (stackingSlab && context.getLevel().getBlockEntity(context.getClickedPos()) instanceof StrippedFacesBlockEntity blockEntity) {
            blockEntity.markStackingStrippedSlab();
        }
        if (!super.placeBlock(context, placementState)) {
            return false;
        }
        if (!stackingSlab && context.getLevel().getBlockEntity(context.getClickedPos()) instanceof StrippedFacesBlockEntity blockEntity) {
            blockEntity.setStrippedMask(StrippedFacesBlockEntity.allFacesMask(placementState));
        }
        return true;
    }

    // ブロック → アイテムの対応は普通の版のまま（上書きすると普通の版の asItem などが壊れる）
    @Override
    public void registerBlocks(Map<Block, Item> map, Item item) {
    }
}
