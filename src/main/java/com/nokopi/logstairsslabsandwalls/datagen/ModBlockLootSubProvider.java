package com.nokopi.logstairsslabsandwalls.datagen;

import java.util.Set;

import com.nokopi.logstairsslabsandwalls.LogWood;
import com.nokopi.logstairsslabsandwalls.ModBlocks;

import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public class ModBlockLootSubProvider extends BlockLootSubProvider {
    public ModBlockLootSubProvider(LootTableSubProvider.Context context) {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, context);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(e -> (Block) e.value()).toList();
    }

    @Override
    protected void generate() {
        for (LogWood wood : ModBlocks.WOODS) {
            this.dropSelf(ModBlocks.logPanel(wood).get());
            this.dropSelf(ModBlocks.logStairs(wood).get());
            this.add(ModBlocks.logSlab(wood).get(), this.createSlabItemTable(ModBlocks.logSlab(wood).get()));
        }
    }
}
