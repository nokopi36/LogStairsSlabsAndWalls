package com.nokopi.logstairsslabsandwalls.datagen;

import java.util.concurrent.CompletableFuture;

import com.nokopi.logstairsslabsandwalls.LogStairsSlabsAndWalls;
import com.nokopi.logstairsslabsandwalls.LogWood;
import com.nokopi.logstairsslabsandwalls.ModBlocks;
import com.nokopi.logstairsslabsandwalls.ModItems;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
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
        for (LogWood wood : ModBlocks.WOODS) {
            this.copy(wood.logsTag().block(), wood.logsTag().item());
            // バニラと同じく、ネザーの木は燃えない木材として扱う
            if (!wood.flammable()) {
                this.tag(ItemTags.NON_FLAMMABLE_WOOD).add(ModItems.logPanel(wood).getKey());
            }
        }
    }
}
