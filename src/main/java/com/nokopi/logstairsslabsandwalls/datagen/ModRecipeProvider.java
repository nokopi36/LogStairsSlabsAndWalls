package com.nokopi.logstairsslabsandwalls.datagen;

import com.nokopi.logstairsslabsandwalls.LogStairsSlabsAndWalls;
import com.nokopi.logstairsslabsandwalls.LogWood;
import com.nokopi.logstairsslabsandwalls.ModBlocks;
import com.nokopi.logstairsslabsandwalls.ModItems;

import net.minecraft.advancements.Advancement;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

// 26.x ではレシピはリロード可能なレジストリとして生成する（DataGenerators で RecipeProvider.asBootstrap に渡す）。
// 材料はタグでなくアイテムそのもので指定する（SPEC 4 章: タグにすると、パネル 2 個に「作る」「戻す」の両方が当たり、
// 剥いだ原木（原木のタグに入っている）から普通の版ができてしまう）
public class ModRecipeProvider extends RecipeProvider {
    // SPEC 4 章: 原木を縦に 2 個 → パネル 2 個、パネルを縦に 2 個 → 原木 2 個
    public static final int LOG_PANELS_PER_RECIPE = 2;
    public static final int LOGS_PER_PANEL_RECIPE = 2;
    // バニラの「板材 3 → ハーフ 6 個」を、原木 1 個 = 板材 LogWood#planksPerLog 枚で換算する（原木 24 個、竹ブロック 12 個）
    private static final int VANILLA_SLABS_PER_PLANK_RECIPE = 6;
    // バニラの「板材 6 → 階段 4 個」を同じく換算する（原木 16 個、竹ブロック 8 個）
    private static final int VANILLA_STAIRS_PER_PLANK_RECIPE = 4;
    // バニラに木の塀はないので、石の塀（ブロック 6 → 6 個）を板材に置き換えて換算する（原木 24 個、竹ブロック 12 個）
    private static final int VANILLA_WALLS_PER_PLANK_RECIPE = 6;

    public ModRecipeProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
        super(recipeOutput, advancementOutput);
    }

    @Override
    protected void buildRecipes() {
        for (LogWood wood : ModBlocks.WOODS) {
            this.logRecipes(wood, wood.log(), ModItems.logPanel(wood).get(), ModItems.logStairs(wood).get(),
                    ModItems.logSlab(wood).get(), ModItems.logWall(wood).get(), "");
            // SPEC 3.5: 剥いだ原木（と剥いだ原木パネル）から剥いだ版
            this.logRecipes(wood, wood.strippedLog(), ModItems.strippedLogPanel(wood).get(), ModItems.strippedLogStairs(wood).get(),
                    ModItems.strippedLogSlab(wood).get(), ModItems.strippedLogWall(wood).get(), "stripped_");
        }
    }

    // log からパネル（と逆）、log かパネルから階段・ハーフ・塀。group は剥いだ版と普通の版で分ける
    private void logRecipes(LogWood wood, ItemLike log, ItemLike panel, ItemLike stairs, ItemLike slab, ItemLike wall, String groupPrefix) {
        this.shaped(RecipeCategory.BUILDING_BLOCKS, panel, LOG_PANELS_PER_RECIPE)
                .define('#', log)
                .pattern("#")
                .pattern("#")
                .group(groupPrefix + "log_panel")
                .unlockedBy(getHasName(log), this.has(log))
                .save(this.output);
        this.shaped(RecipeCategory.BUILDING_BLOCKS, log, LOGS_PER_PANEL_RECIPE)
                .define('#', panel)
                .pattern("#")
                .pattern("#")
                .group(groupPrefix + "log_from_log_panel")
                .unlockedBy(getHasName(panel), this.has(panel))
                .save(this.output, LogStairsSlabsAndWalls.MODID + ":" + getConversionRecipeName(log, panel));
        // 階段・ハーフ・塀は原木と原木パネルのどちらでも作れる（混ぜてもよい。パネル 1 個 = 原木 1 個）
        Ingredient logOrPanel = Ingredient.of(log, panel);
        this.shaped(RecipeCategory.BUILDING_BLOCKS, stairs, stairsCount(wood))
                .define('#', logOrPanel)
                .pattern("#  ")
                .pattern("## ")
                .pattern("###")
                .group(groupPrefix + "log_stairs")
                .unlockedBy(getHasName(log), this.has(log))
                .save(this.output);
        this.shaped(RecipeCategory.BUILDING_BLOCKS, slab, slabCount(wood))
                .define('#', logOrPanel)
                .pattern("###")
                .group(groupPrefix + "log_slab")
                .unlockedBy(getHasName(log), this.has(log))
                .save(this.output);
        this.shaped(RecipeCategory.DECORATIONS, wall, wallCount(wood))
                .define('#', logOrPanel)
                .pattern("###")
                .pattern("###")
                .group(groupPrefix + "log_wall")
                .unlockedBy(getHasName(log), this.has(log))
                .save(this.output);
    }

    // 階段のレシピは原木 6 個 = 板材 6 * planksPerLog 枚分で、バニラのレシピ planksPerLog 回分
    public static int stairsCount(LogWood wood) {
        return VANILLA_STAIRS_PER_PLANK_RECIPE * wood.planksPerLog();
    }

    // 塀のレシピは原木 6 個 = 板材 6 * planksPerLog 枚分で、換算した石の塀のレシピ planksPerLog 回分
    public static int wallCount(LogWood wood) {
        return VANILLA_WALLS_PER_PLANK_RECIPE * wood.planksPerLog();
    }

    // ハーフのレシピは原木 3 個 = 板材 3 * planksPerLog 枚分で、バニラのレシピ planksPerLog 回分
    public static int slabCount(LogWood wood) {
        return VANILLA_SLABS_PER_PLANK_RECIPE * wood.planksPerLog();
    }
}
