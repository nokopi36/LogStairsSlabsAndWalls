package com.nokopi.logstairsslabsandwalls;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LogStairsSlabsAndWalls.MODID);

    private static final Map<LogWood, DeferredItem<BlockItem>> LOG_PANELS = new EnumMap<>(LogWood.class);
    private static final Map<LogWood, DeferredItem<BlockItem>> LOG_STAIRS = new EnumMap<>(LogWood.class);
    private static final Map<LogWood, DeferredItem<BlockItem>> LOG_SLABS = new EnumMap<>(LogWood.class);
    private static final Map<LogWood, DeferredItem<BlockItem>> LOG_WALLS = new EnumMap<>(LogWood.class);

    static {
        for (LogWood wood : ModBlocks.WOODS) {
            LOG_PANELS.put(wood, ITEMS.registerSimpleBlockItem(wood.logPanelName(), ModBlocks.logPanel(wood), blockFuel(wood)));
            LOG_STAIRS.put(wood, ITEMS.registerSimpleBlockItem(wood.logStairsName(), ModBlocks.logStairs(wood), blockFuel(wood)));
            LOG_SLABS.put(wood, ITEMS.registerSimpleBlockItem(wood.logSlabName(), ModBlocks.logSlab(wood), slabFuel(wood)));
            LOG_WALLS.put(wood, ITEMS.registerSimpleBlockItem(wood.logWallName(), ModBlocks.logWall(wood), blockFuel(wood)));
        }
    }

    private ModItems() {
    }

    public static DeferredItem<BlockItem> logPanel(LogWood wood) {
        return LOG_PANELS.get(wood);
    }

    public static DeferredItem<BlockItem> logStairs(LogWood wood) {
        return LOG_STAIRS.get(wood);
    }

    public static DeferredItem<BlockItem> logSlab(LogWood wood) {
        return LOG_SLABS.get(wood);
    }

    public static DeferredItem<BlockItem> logWall(LogWood wood) {
        return LOG_WALLS.get(wood);
    }

    // 燃料はバニラの木の階段・フェンスなどと同じ（COOKING_TIME_WOOD_BLOCKS = 300 tick）。ネザーの木は燃料にならない
    private static UnaryOperator<Item.Properties> blockFuel(LogWood wood) {
        return wood.flammable() ? p -> p.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS) : UnaryOperator.identity();
    }

    // ハーフはバニラの木のハーフと同じ（COOKING_TIME_WOOD_SLABS = 150 tick）
    private static UnaryOperator<Item.Properties> slabFuel(LogWood wood) {
        return wood.flammable() ? p -> p.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_SLABS) : UnaryOperator.identity();
    }
}
