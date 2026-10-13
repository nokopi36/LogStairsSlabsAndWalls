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
import net.minecraft.world.level.storage.loot.entries.UniformContainerBase;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
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

    // ハーフは、バニラの createSlabItemTable と同じく 2 枚重ねなら 2 個（剥いだ版・普通の版とも）
    private LootTable.Builder createStrippableSlabTable(Block slab, Item strippedItem) {
        return LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.exactly(1))
                .add(this.slabEntry(slab, strippedItem)
                        .when(AllFacesStrippedCondition.allFacesStripped())
                        .otherwise(this.slabEntry(slab, slab.asItem()))));
    }

    private UniformContainerBase.Builder<?> slabEntry(Block slab, Item item) {
        return this.applyExplosionDecay(slab, LootItem.lootTableItem(item)
                .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(2))
                        .when(MatchBlock.blockMatches(this.blocks, slab,
                                StatePropertiesPredicate.Builder.properties().hasProperty(SlabBlock.TYPE, SlabType.DOUBLE)))));
    }
}
