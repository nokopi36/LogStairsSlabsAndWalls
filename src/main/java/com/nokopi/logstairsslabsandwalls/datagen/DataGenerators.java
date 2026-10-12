package com.nokopi.logstairsslabsandwalls.datagen;

import java.util.List;
import java.util.Set;

import com.nokopi.logstairsslabsandwalls.LogStairsSlabsAndWalls;
import com.nokopi.logstairsslabsandwalls.gametest.ModGameTests;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
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
        // 26.x ではルートテーブルとレシピはリロード可能なレジストリとして生成する（NeoForge 本体の ClientNeoForgeMod と同じ形）
        event.createReloadableRegistryObjects(new RegistrySetBuilder()
                .add(Registries.LOOT_TABLE, new LootTableProvider(Set.of(),
                        List.of(new LootTableProvider.SubProviderEntry(ModBlockLootSubProvider::new, LootContextParamSets.BLOCK))))
                .add(RecipeProvider.asBootstrap(ModRecipeProvider::new)));

        event.createWorldRegistryObjects(new RegistrySetBuilder()
                .add(Registries.TEST_INSTANCE, ModGameTests::bootstrapInstances));

        event.createBlockAndItemTags(ModBlockTagsProvider::new, ModItemTagsProvider::new);
        event.createProvider(ModModelProvider::new);
        event.createProvider(output -> new ModLanguageProvider.English(output));
        event.createProvider(output -> new ModLanguageProvider.Japanese(output));
    }
}
