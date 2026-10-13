package com.nokopi.logstairsslabsandwalls.datagen;

import java.util.Set;

import com.nokopi.logstairsslabsandwalls.LogWood;
import com.nokopi.logstairsslabsandwalls.ModBlocks;
import com.nokopi.logstairsslabsandwalls.ModItems;
import com.nokopi.logstairsslabsandwalls.loot.AllFacesStrippedCondition;

import net.minecraft.advancements.predicates.StatePropertiesPredicate;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.MatchBlock;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

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
            this.add(ModBlocks.logPanel(wood).get(), this.createStrippableTable(ModBlocks.logPanel(wood).get(), ModItems.strippedLogPanel(wood).get()));
            this.add(ModBlocks.logStairs(wood).get(), this.createStrippableTable(ModBlocks.logStairs(wood).get(), ModItems.strippedLogStairs(wood).get()));
            this.add(ModBlocks.logWall(wood).get(), this.createStrippableTable(ModBlocks.logWall(wood).get(), ModItems.strippedLogWall(wood).get()));
            this.add(ModBlocks.logSlab(wood).get(), this.createStrippableSlabTable(ModBlocks.logSlab(wood).get(), ModItems.strippedLogSlab(wood).get()));
        }
    }

    // SPEC 3.5: 全面が剥がれていれば剥いだ版、そうでなければ普通の版を落とす
    private LootTable.Builder createStrippableTable(Block block, Item strippedItem) {
        return LootTable.lootTable().withPool(this.applyExplosionCondition(block, LootPool.lootPool()
                .setRolls(ContextIntProviders.exactly(1))
                .add(LootItem.lootTableItem(strippedItem)
                        .when(AllFacesStrippedCondition.allFacesStripped())
                        .otherwise(LootItem.lootTableItem(block)))));
    }

    // ハーフは上下のハーフごとに 1 個ずつ（2 枚重ねなら 2 個）。そのハーフの面がすべて剥がれていれば剥いだ版（SPEC 3.5）
    private LootTable.Builder createStrippableSlabTable(Block slab, Item strippedItem) {
        return LootTable.lootTable()
                .withPool(this.slabHalfPool(slab, strippedItem, false, SlabType.TOP))
                .withPool(this.slabHalfPool(slab, strippedItem, true, SlabType.BOTTOM));
    }

    // そのハーフがあるとき（absentType でないとき）に 1 個落とすプール
    private LootPool.Builder slabHalfPool(Block slab, Item strippedItem, boolean upper, SlabType absentType) {
        return this.applyExplosionCondition(slab, LootPool.lootPool()
                .setRolls(ContextIntProviders.exactly(1))
                .when(MatchBlock.blockMatches(this.blocks, slab,
                        StatePropertiesPredicate.Builder.properties().hasProperty(SlabBlock.TYPE, absentType)).invert())
                .add(LootItem.lootTableItem(strippedItem)
                        .when(AllFacesStrippedCondition.slabHalfStripped(upper))
                        .otherwise(LootItem.lootTableItem(slab))));
    }
}
