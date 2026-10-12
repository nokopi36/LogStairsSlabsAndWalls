package com.nokopi.logstairsslabsandwalls;

import net.minecraft.tags.BlockItemTagId;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.WoodType;

/**
 * 原木パネル・階段・ハーフ・塀を作る木の種類（SPEC 2 章）。Log Fences and Gates と同じ 13 種・同じ並び。並び順はクリエイティブタブの並び順。
 * 表示名はバニラの原木・幹・竹ブロックの名前に合わせる。
 */
public enum LogWood {
    OAK("oak_log", WoodType.OAK, true, "Oak Log", "オークの原木", "樹皮", 4),
    SPRUCE("spruce_log", WoodType.SPRUCE, true, "Spruce Log", "トウヒの原木", "樹皮", 4),
    BIRCH("birch_log", WoodType.BIRCH, true, "Birch Log", "シラカバの原木", "樹皮", 4),
    JUNGLE("jungle_log", WoodType.JUNGLE, true, "Jungle Log", "ジャングルの原木", "樹皮", 4),
    ACACIA("acacia_log", WoodType.ACACIA, true, "Acacia Log", "アカシアの原木", "樹皮", 4),
    DARK_OAK("dark_oak_log", WoodType.DARK_OAK, true, "Dark Oak Log", "ダークオークの原木", "樹皮", 4),
    MANGROVE("mangrove_log", WoodType.MANGROVE, true, "Mangrove Log", "マングローブの原木", "樹皮", 4),
    CHERRY("cherry_log", WoodType.CHERRY, true, "Cherry Log", "サクラの原木", "樹皮", 4),
    PALE_OAK("pale_oak_log", WoodType.PALE_OAK, true, "Pale Oak Log", "ペールオークの原木", "樹皮", 4),
    POPLAR("poplar_log", WoodType.POPLAR, true, "Poplar Log", "ポプラの原木", "樹皮", 4),
    BAMBOO("bamboo_block", WoodType.BAMBOO, true, "Bamboo Block", "竹ブロック", "表皮", 2),
    CRIMSON("crimson_stem", WoodType.CRIMSON, false, "Crimson Stem", "真紅の幹", "表皮", 4),
    WARPED("warped_stem", WoodType.WARPED, false, "Warped Stem", "歪んだ幹", "表皮", 4);

    private final String logName;
    private final WoodType woodType;
    private final boolean flammable;
    private final String englishName;
    private final String japaneseName;
    private final String strippedJapanesePrefix;
    private final int planksPerLog;

    LogWood(String logName, WoodType woodType, boolean flammable, String englishName, String japaneseName,
            String strippedJapanesePrefix, int planksPerLog) {
        this.logName = logName;
        this.woodType = woodType;
        this.flammable = flammable;
        this.englishName = englishName;
        this.japaneseName = japaneseName;
        this.strippedJapanesePrefix = strippedJapanesePrefix;
        this.planksPerLog = planksPerLog;
    }

    public String logPanelName() {
        return this.logName + "_panel";
    }

    public String logStairsName() {
        return this.logName + "_stairs";
    }

    public String logSlabName() {
        return this.logName + "_slab";
    }

    public String logWallName() {
        return this.logName + "_wall";
    }

    public static String strippedName(String name) {
        return "stripped_" + name;
    }

    public WoodType woodType() {
        return this.woodType;
    }

    // 燃える・燃料になる（ネザーの木は false）
    public boolean flammable() {
        return this.flammable;
    }

    public String englishName() {
        return this.englishName;
    }

    public String japaneseName() {
        return this.japaneseName;
    }

    // バニラに合わせ、原木は「樹皮を剥いだ」、ネザーの幹と竹ブロックは「表皮を剥いだ」
    public String strippedJapaneseName() {
        return this.strippedJapanesePrefix + "を剥いだ" + this.japaneseName;
    }

    // 原木 1 個をクラフトしてできる板材の枚数（レシピの個数の換算に使う）
    public int planksPerLog() {
        return this.planksPerLog;
    }

    // バニラのブロック・アイテムは登録後に参照するため、メソッドで取り出す
    public Block log() {
        return switch (this) {
            case OAK -> Blocks.OAK_LOG;
            case SPRUCE -> Blocks.SPRUCE_LOG;
            case BIRCH -> Blocks.BIRCH_LOG;
            case JUNGLE -> Blocks.JUNGLE_LOG;
            case ACACIA -> Blocks.ACACIA_LOG;
            case DARK_OAK -> Blocks.DARK_OAK_LOG;
            case MANGROVE -> Blocks.MANGROVE_LOG;
            case CHERRY -> Blocks.CHERRY_LOG;
            case PALE_OAK -> Blocks.PALE_OAK_LOG;
            case POPLAR -> Blocks.POPLAR_LOG;
            case BAMBOO -> Blocks.BAMBOO_BLOCK;
            case CRIMSON -> Blocks.CRIMSON_STEM;
            case WARPED -> Blocks.WARPED_STEM;
        };
    }

    public Block strippedLog() {
        return switch (this) {
            case OAK -> Blocks.STRIPPED_OAK_LOG;
            case SPRUCE -> Blocks.STRIPPED_SPRUCE_LOG;
            case BIRCH -> Blocks.STRIPPED_BIRCH_LOG;
            case JUNGLE -> Blocks.STRIPPED_JUNGLE_LOG;
            case ACACIA -> Blocks.STRIPPED_ACACIA_LOG;
            case DARK_OAK -> Blocks.STRIPPED_DARK_OAK_LOG;
            case MANGROVE -> Blocks.STRIPPED_MANGROVE_LOG;
            case CHERRY -> Blocks.STRIPPED_CHERRY_LOG;
            case PALE_OAK -> Blocks.STRIPPED_PALE_OAK_LOG;
            case POPLAR -> Blocks.STRIPPED_POPLAR_LOG;
            case BAMBOO -> Blocks.STRIPPED_BAMBOO_BLOCK;
            case CRIMSON -> Blocks.STRIPPED_CRIMSON_STEM;
            case WARPED -> Blocks.STRIPPED_WARPED_STEM;
        };
    }

    // この木の原木のタグ（ブロック・アイテム共通）。原木パネルを入れて、板材・木炭などの材料にする
    public BlockItemTagId logsTag() {
        return switch (this) {
            case OAK -> BlockItemTags.OAK_LOGS;
            case SPRUCE -> BlockItemTags.SPRUCE_LOGS;
            case BIRCH -> BlockItemTags.BIRCH_LOGS;
            case JUNGLE -> BlockItemTags.JUNGLE_LOGS;
            case ACACIA -> BlockItemTags.ACACIA_LOGS;
            case DARK_OAK -> BlockItemTags.DARK_OAK_LOGS;
            case MANGROVE -> BlockItemTags.MANGROVE_LOGS;
            case CHERRY -> BlockItemTags.CHERRY_LOGS;
            case PALE_OAK -> BlockItemTags.PALE_OAK_LOGS;
            case POPLAR -> BlockItemTags.POPLAR_LOGS;
            case BAMBOO -> BlockItemTags.BAMBOO_BLOCKS;
            case CRIMSON -> BlockItemTags.CRIMSON_STEMS;
            case WARPED -> BlockItemTags.WARPED_STEMS;
        };
    }

    // クリエイティブの建築ブロックタブで、このアイテム（バニラの木のハーフ）の後ろに並べる
    public Item vanillaSlab() {
        return switch (this) {
            case OAK -> Items.OAK_SLAB;
            case SPRUCE -> Items.SPRUCE_SLAB;
            case BIRCH -> Items.BIRCH_SLAB;
            case JUNGLE -> Items.JUNGLE_SLAB;
            case ACACIA -> Items.ACACIA_SLAB;
            case DARK_OAK -> Items.DARK_OAK_SLAB;
            case MANGROVE -> Items.MANGROVE_SLAB;
            case CHERRY -> Items.CHERRY_SLAB;
            case PALE_OAK -> Items.PALE_OAK_SLAB;
            case POPLAR -> Items.POPLAR_SLAB;
            case BAMBOO -> Items.BAMBOO_SLAB;
            case CRIMSON -> Items.CRIMSON_SLAB;
            case WARPED -> Items.WARPED_SLAB;
        };
    }
}
