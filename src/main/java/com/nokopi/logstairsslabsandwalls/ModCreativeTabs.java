package com.nokopi.logstairsslabsandwalls;

import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LogStairsSlabsAndWalls.MODID);

    public static final String TAB_TITLE = "itemGroup." + LogStairsSlabsAndWalls.MODID;

    // 専用タブ: 木の種類ごとにパネル → 階段 → ハーフ → 塀（剥いだ版はその後ろ）の順で並べる
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> LOG_STAIRS_TAB = CREATIVE_MODE_TABS.register(LogStairsSlabsAndWalls.MODID, () -> CreativeModeTab.builder()
            .title(Component.translatable(TAB_TITLE))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .icon(() -> new ItemStack(ModItems.logPanel(LogWood.OAK).get()))
            .displayItems((parameters, output) -> {
                for (LogWood wood : ModBlocks.WOODS) {
                    for (ItemStack stack : items(wood)) {
                        output.accept(stack);
                    }
                }
            })
            .build());

    private ModCreativeTabs() {
    }

    // 建築ブロックタブにも、各木のハーフの後ろに並べる
    static void addToVanillaTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            for (LogWood wood : ModBlocks.WOODS) {
                ItemStack previous = new ItemStack(wood.vanillaSlab());
                for (ItemStack stack : items(wood)) {
                    event.insertAfter(previous, stack, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
                    previous = stack;
                }
            }
        }
    }

    // 1 種類の木のアイテムを、タブに並べる順で返す
    private static List<ItemStack> items(LogWood wood) {
        return List.of(new ItemStack(ModItems.logPanel(wood).get()), new ItemStack(ModItems.logStairs(wood).get()),
                new ItemStack(ModItems.logSlab(wood).get()), new ItemStack(ModItems.logWall(wood).get()));
    }
}
