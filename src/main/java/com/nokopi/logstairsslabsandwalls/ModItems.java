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

    static {
        for (LogWood wood : ModBlocks.WOODS) {
            LOG_PANELS.put(wood, ITEMS.registerSimpleBlockItem(wood.logPanelName(), ModBlocks.logPanel(wood), blockFuel(wood)));
        }
    }

    private ModItems() {
    }

    public static DeferredItem<BlockItem> logPanel(LogWood wood) {
        return LOG_PANELS.get(wood);
    }

    // 燃料はバニラの木の階段・フェンスなどと同じ（COOKING_TIME_WOOD_BLOCKS = 300 tick）。ネザーの木は燃料にならない
    private static UnaryOperator<Item.Properties> blockFuel(LogWood wood) {
        return wood.flammable() ? p -> p.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS) : UnaryOperator.identity();
    }
}
