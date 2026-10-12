package com.nokopi.logstairsslabsandwalls.datagen;

import com.nokopi.logstairsslabsandwalls.LogStairsSlabsAndWalls;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

// MDK の clientData 設定に合わせ、すべてのプロバイダーを GatherDataEvent.Client で登録する
@EventBusSubscriber(modid = LogStairsSlabsAndWalls.MODID)
public final class DataGenerators {
    private DataGenerators() {
    }

    @SubscribeEvent
    static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(output -> new ModLanguageProvider.English(output));
        event.createProvider(output -> new ModLanguageProvider.Japanese(output));
    }
}
