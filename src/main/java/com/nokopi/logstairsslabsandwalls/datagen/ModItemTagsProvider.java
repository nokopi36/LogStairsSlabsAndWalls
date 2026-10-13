package com.nokopi.logstairsslabsandwalls.datagen;

import java.util.concurrent.CompletableFuture;

import com.nokopi.logstairsslabsandwalls.LogStairsSlabsAndWalls;
import com.nokopi.logstairsslabsandwalls.LogWood;
import com.nokopi.logstairsslabsandwalls.ModBlocks;
import com.nokopi.logstairsslabsandwalls.ModItems;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagCopyingItemTagProvider;

public class ModItemTagsProvider extends BlockTagCopyingItemTagProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
            CompletableFuture<TagsProvider.TagLookup<Block>> blockTags) {
        super(output, lookupProvider, blockTags, LogStairsSlabsAndWalls.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        this.copy(BlockItemTags.WOODEN_STAIRS.block(), BlockItemTags.WOODEN_STAIRS.item());
        this.copy(BlockItemTags.WOODEN_SLABS.block(), BlockItemTags.WOODEN_SLABS.item());
        this.copy(BlockItemTags.WALLS.block(), BlockItemTags.WALLS.item());
        for (LogWood wood : ModBlocks.WOODS) {
            this.copy(wood.logsTag().block(), wood.logsTag().item());
            // 剥いだ版はブロックを持たないので、アイテムのタグに直接入れる。剥いだパネルもバニラの剥いだ原木と同じく原木のタグに入る
            this.tag(wood.logsTag().item()).add(ModItems.strippedLogPanel(wood).getKey());
            this.tag(BlockItemTags.WOODEN_STAIRS.item()).add(ModItems.strippedLogStairs(wood).getKey());
            this.tag(BlockItemTags.WOODEN_SLABS.item()).add(ModItems.strippedLogSlab(wood).getKey());
            this.tag(BlockItemTags.WALLS.item()).add(ModItems.strippedLogWall(wood).getKey());
            // バニラと同じく、ネザーの木は燃えない木材として扱う
            if (!wood.flammable()) {
                this.tag(ItemTags.NON_FLAMMABLE_WOOD)
                        .add(ModItems.logPanel(wood).getKey())
                        .add(ModItems.logStairs(wood).getKey())
                        .add(ModItems.logSlab(wood).getKey())
                        .add(ModItems.logWall(wood).getKey())
                        .add(ModItems.strippedLogPanel(wood).getKey())
                        .add(ModItems.strippedLogStairs(wood).getKey())
                        .add(ModItems.strippedLogSlab(wood).getKey())
                        .add(ModItems.strippedLogWall(wood).getKey());
            }
        }
    }
}
