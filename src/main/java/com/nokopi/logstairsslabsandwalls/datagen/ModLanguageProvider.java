package com.nokopi.logstairsslabsandwalls.datagen;

import com.nokopi.logstairsslabsandwalls.LogStairsSlabsAndWalls;
import com.nokopi.logstairsslabsandwalls.LogWood;
import com.nokopi.logstairsslabsandwalls.ModBlocks;
import com.nokopi.logstairsslabsandwalls.ModCreativeTabs;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public final class ModLanguageProvider {
    private ModLanguageProvider() {
    }

    public static final class English extends LanguageProvider {
        public English(PackOutput output) {
            super(output, LogStairsSlabsAndWalls.MODID, "en_us");
        }

        @Override
        protected void addTranslations() {
            for (LogWood wood : ModBlocks.WOODS) {
                this.addBlock(ModBlocks.logPanel(wood), wood.englishName() + " Panel");
                this.addBlock(ModBlocks.logStairs(wood), wood.englishName() + " Stairs");
                this.addBlock(ModBlocks.logSlab(wood), wood.englishName() + " Slab");
                this.addBlock(ModBlocks.logWall(wood), wood.englishName() + " Wall");
            }
            this.add(ModCreativeTabs.TAB_TITLE, "Log Stairs, Slabs and Walls");
        }
    }

    public static final class Japanese extends LanguageProvider {
        public Japanese(PackOutput output) {
            super(output, LogStairsSlabsAndWalls.MODID, "ja_jp");
        }

        @Override
        protected void addTranslations() {
            for (LogWood wood : ModBlocks.WOODS) {
                this.addBlock(ModBlocks.logPanel(wood), wood.japaneseName() + "パネル");
                this.addBlock(ModBlocks.logStairs(wood), wood.japaneseName() + "の階段");
                this.addBlock(ModBlocks.logSlab(wood), wood.japaneseName() + "のハーフブロック");
                this.addBlock(ModBlocks.logWall(wood), wood.japaneseName() + "の塀");
            }
            this.add(ModCreativeTabs.TAB_TITLE, "Log Stairs, Slabs and Walls");
        }
    }
}
