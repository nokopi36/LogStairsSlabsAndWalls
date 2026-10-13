package com.nokopi.logstairsslabsandwalls.item;

import java.util.Map;

import com.nokopi.logstairsslabsandwalls.block.StrippedFacesBlockEntity;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
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
     * ハーフを重ねて 2 枚重ねにするときは、LogSlabBlock#setPlacedBy が新しいハーフの面だけ剥がれた状態にする。
     */
    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState placementState) {
        if (!super.placeBlock(context, placementState)) {
            return false;
        }
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof StrippedFacesBlockEntity blockEntity
                && !blockEntity.isStackingSlab()) {
            blockEntity.setStrippedMask(StrippedFacesBlockEntity.allFacesMask(placementState));
        }
        return true;
    }

    // ブロック → アイテムの対応は普通の版のまま（上書きすると普通の版の asItem などが壊れる）
    @Override
    public void registerBlocks(Map<Block, Item> map, Item item) {
    }
}
