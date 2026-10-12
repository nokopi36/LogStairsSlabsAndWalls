package com.nokopi.logstairsslabsandwalls.datagen;

import com.nokopi.logstairsslabsandwalls.LogStairsSlabsAndWalls;
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
            this.add(ModCreativeTabs.TAB_TITLE, "Log Stairs, Slabs and Walls");
        }
    }

    public static final class Japanese extends LanguageProvider {
        public Japanese(PackOutput output) {
            super(output, LogStairsSlabsAndWalls.MODID, "ja_jp");
        }

        @Override
        protected void addTranslations() {
            this.add(ModCreativeTabs.TAB_TITLE, "Log Stairs, Slabs and Walls");
        }
    }
}
