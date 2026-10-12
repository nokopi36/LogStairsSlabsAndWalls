package com.nokopi.logstairsslabsandwalls;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LogStairsSlabsAndWalls.MODID);

    public static final String TAB_TITLE = "itemGroup." + LogStairsSlabsAndWalls.MODID;

    // 専用タブ: 木の種類ごとにパネル → 階段 → ハーフ → 塀（剥いだ版はその後ろ）の順で並べる
    // アイコンはオークの原木パネルができるまでバニラの原木
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> LOG_STAIRS_TAB = CREATIVE_MODE_TABS.register(LogStairsSlabsAndWalls.MODID, () -> CreativeModeTab.builder()
            .title(Component.translatable(TAB_TITLE))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .icon(() -> new ItemStack(Items.OAK_LOG))
            .displayItems((parameters, output) -> {
            })
            .build());

    private ModCreativeTabs() {
    }

    // 建築ブロックタブにも、各木のハーフの後ろに並べる
    static void addToVanillaTabs(BuildCreativeModeTabContentsEvent event) {
    }
}
