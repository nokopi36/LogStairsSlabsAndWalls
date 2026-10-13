package com.nokopi.logstairsslabsandwalls;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.nokopi.logstairsslabsandwalls.gametest.ModGameTests;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(LogStairsSlabsAndWalls.MODID)
public class LogStairsSlabsAndWalls {
    public static final String MODID = "log_stairs_slabs_and_walls";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LogStairsSlabsAndWalls(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModGameTests.TEST_FUNCTIONS.register(modEventBus);
        modEventBus.addListener(ModCreativeTabs::addToVanillaTabs);
    }
}
