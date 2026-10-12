package com.nokopi.logstairsslabsandwalls.datagen;

import java.util.concurrent.CompletableFuture;

import com.nokopi.logstairsslabsandwalls.LogStairsSlabsAndWalls;
import com.nokopi.logstairsslabsandwalls.LogWood;
import com.nokopi.logstairsslabsandwalls.ModBlocks;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, LogStairsSlabsAndWalls.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        for (LogWood wood : ModBlocks.WOODS) {
            // 原木パネルはその木の原木として扱う（minecraft:logs などにも含まれる）
            this.tag(wood.logsTag().block()).add(ModBlocks.logPanel(wood).getKey());
            this.tag(BlockItemTags.WOODEN_STAIRS.block()).add(ModBlocks.logStairs(wood).getKey());
            this.tag(BlockItemTags.WOODEN_SLABS.block()).add(ModBlocks.logSlab(wood).getKey());
            this.tag(BlockTags.MINEABLE_WITH_AXE)
                    .add(ModBlocks.logPanel(wood).getKey())
                    .add(ModBlocks.logStairs(wood).getKey())
                    .add(ModBlocks.logSlab(wood).getKey());
        }
    }
}
