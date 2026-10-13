package com.nokopi.logstairsslabsandwalls.gametest;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.nokopi.logstairsslabsandwalls.LogStairsSlabsAndWalls;
import com.nokopi.logstairsslabsandwalls.LogWood;
import com.nokopi.logstairsslabsandwalls.ModBlocks;
import com.nokopi.logstairsslabsandwalls.ModItems;
import com.nokopi.logstairsslabsandwalls.block.FaceStripping;
import com.nokopi.logstairsslabsandwalls.block.StrippedFace;
import com.nokopi.logstairsslabsandwalls.block.StrippedFacesBlockEntity;
import com.nokopi.logstairsslabsandwalls.datagen.ModRecipeProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestEnvironments;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModGameTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS = DeferredRegister.create(Registries.TEST_FUNCTION, LogStairsSlabsAndWalls.MODID);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LOG_PANEL_RECIPES =
            TEST_FUNCTIONS.register("log_panel_recipes", () -> ModGameTests::logPanelRecipes);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STRIP_EACH_FACE =
            TEST_FUNCTIONS.register("strip_each_face", () -> ModGameTests::stripEachFace);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STRIP_STRIPPED_FACE =
            TEST_FUNCTIONS.register("strip_stripped_face", () -> ModGameTests::stripStrippedFace);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LOG_SLAB_RECIPE =
            TEST_FUNCTIONS.register("log_slab_recipe", () -> ModGameTests::logSlabRecipe);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STRIP_SLAB_FACES =
            TEST_FUNCTIONS.register("strip_slab_faces", () -> ModGameTests::stripSlabFaces);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LOG_STAIRS_RECIPE =
            TEST_FUNCTIONS.register("log_stairs_recipe", () -> ModGameTests::logStairsRecipe);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STRIP_STAIRS_FACES =
            TEST_FUNCTIONS.register("strip_stairs_faces", () -> ModGameTests::stripStairsFaces);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STRIPPED_FACES_SAVE_LOAD =
            TEST_FUNCTIONS.register("stripped_faces_save_load", () -> ModGameTests::strippedFacesSaveLoad);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LOG_WALL_RECIPE =
            TEST_FUNCTIONS.register("log_wall_recipe", () -> ModGameTests::logWallRecipe);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STRIP_WALL_FACES =
            TEST_FUNCTIONS.register("strip_wall_faces", () -> ModGameTests::stripWallFaces);

    // data/log_stairs_slabs_and_walls/structure/test_area.nbt（空の 3x3x3）
    private static final Identifier TEST_AREA = Identifier.fromNamespaceAndPath(LogStairsSlabsAndWalls.MODID, "test_area");
    private static final int MAX_TICKS = 20;

    private ModGameTests() {
    }

    // datagen から呼ぶ: 登録済みのテスト関数ごとにテストインスタンスを作る
    public static void bootstrapInstances(BootstrapContext<GameTestInstance> context) {
        HolderGetter<TestEnvironmentDefinition<?>> environments = context.lookup(Registries.TEST_ENVIRONMENT);
        for (DeferredHolder<Consumer<GameTestHelper>, ? extends Consumer<GameTestHelper>> function : TEST_FUNCTIONS.getEntries()) {
            ResourceKey<GameTestInstance> key = ResourceKey.create(Registries.TEST_INSTANCE, function.getId());
            context.register(key, new FunctionGameTestInstance(function.getKey(),
                    new TestData<>(environments.getOrThrow(GameTestEnvironments.DEFAULT_KEY), TEST_AREA, MAX_TICKS, 0, true)));
        }
    }

    // SPEC 4 章: 縦 2 個の原木 → パネル、縦 2 個のパネル → 原木。どちらも当たるレシピは 1 つだけ。
    // パネル 1 個はバニラの「原木 → 板材」（タグ）に当たる
    private static void logPanelRecipes(GameTestHelper helper) {
        for (LogWood wood : ModBlocks.WOODS) {
            Item panel = ModItems.logPanel(wood).get();
            assertOnlyRecipe(helper, CraftingInput.of(1, 2, Collections.nCopies(2, new ItemStack(wood.log()))),
                    panel, ModRecipeProvider.LOG_PANELS_PER_RECIPE);
            assertOnlyRecipe(helper, CraftingInput.of(1, 2, Collections.nCopies(2, new ItemStack(panel))),
                    wood.log(), ModRecipeProvider.LOGS_PER_PANEL_RECIPE);
        }
        assertOnlyRecipe(helper, CraftingInput.of(1, 1, List.of(new ItemStack(ModItems.logPanel(LogWood.OAK).get()))), Items.OAK_PLANKS, 4);
        helper.succeed();
    }

    // SPEC 4 章: 原木または原木パネルを横一列に 3 個 → ハーフ 24 個（竹ブロック 12 個）
    private static void logSlabRecipe(GameTestHelper helper) {
        for (LogWood wood : ModBlocks.WOODS) {
            assertOnlyRecipe(helper, CraftingInput.of(3, 1, Collections.nCopies(3, new ItemStack(wood.log()))),
                    ModItems.logSlab(wood).get(), ModRecipeProvider.slabCount(wood));
            // 原木パネルでも、原木と混ぜても同じ個数
            ItemStack panel = new ItemStack(ModItems.logPanel(wood).get());
            assertOnlyRecipe(helper, CraftingInput.of(3, 1, Collections.nCopies(3, panel)),
                    ModItems.logSlab(wood).get(), ModRecipeProvider.slabCount(wood));
            assertOnlyRecipe(helper, CraftingInput.of(3, 1, List.of(new ItemStack(wood.log()), panel, new ItemStack(wood.log()))),
                    ModItems.logSlab(wood).get(), ModRecipeProvider.slabCount(wood));
        }
        helper.assertTrue(ModRecipeProvider.slabCount(LogWood.OAK) == 24 && ModRecipeProvider.slabCount(LogWood.BAMBOO) == 12,
                "slab counts should be 24 and 12");
        helper.succeed();
    }

    // SPEC 4 章: 階段の形に原木または原木パネル 6 個 → 階段 16 個（竹ブロック 8 個）
    private static void logStairsRecipe(GameTestHelper helper) {
        for (LogWood wood : ModBlocks.WOODS) {
            ItemStack log = new ItemStack(wood.log());
            ItemStack panel = new ItemStack(ModItems.logPanel(wood).get());
            for (ItemStack mixed : List.of(log, panel)) {
                assertOnlyRecipe(helper, CraftingInput.of(3, 3, List.of(
                        log, ItemStack.EMPTY, ItemStack.EMPTY,
                        mixed, panel, ItemStack.EMPTY,
                        log, mixed, panel)), ModItems.logStairs(wood).get(), ModRecipeProvider.stairsCount(wood));
            }
        }
        helper.assertTrue(ModRecipeProvider.stairsCount(LogWood.OAK) == 16 && ModRecipeProvider.stairsCount(LogWood.BAMBOO) == 8,
                "stairs counts should be 16 and 8");
        helper.succeed();
    }

    // 階段の上下・すべての形で、外周の 6 面と段の内側の踏み面・蹴上げがそれぞれ単独で剥げる
    private static void stripStairsFaces(GameTestHelper helper) {
        for (Half half : Half.values()) {
            for (StairsShape shape : StairsShape.values()) {
                BlockState stairs = ModBlocks.logStairs(LogWood.OAK).get().defaultBlockState()
                        .setValue(StairBlock.HALF, half).setValue(StairBlock.SHAPE, shape);
                for (Direction face : Direction.values()) {
                    Vec3 local = new Vec3(0.5, 0.5, 0.5).add(Vec3.atLowerCornerOf(face.getUnitVec3i()).scale(0.5));
                    assertBlockEntityStripsOnly(helper, stairs, face, local, StrippedFace.of(face));
                }
                // 段の内側の面（ブロックの中央の高さ・位置）: 踏み面は上下逆なら下向き、蹴上げは横向き
                Direction treadFace = half == Half.BOTTOM ? Direction.UP : Direction.DOWN;
                assertBlockEntityStripsOnly(helper, stairs, treadFace, new Vec3(0.25, 0.5, 0.5), StrippedFace.TREAD);
                for (Direction riserFace : Direction.Plane.HORIZONTAL) {
                    Vec3 local = new Vec3(0.5, half == Half.BOTTOM ? 0.75 : 0.25, 0.5);
                    assertBlockEntityStripsOnly(helper, stairs, riserFace, local, StrippedFace.RISER);
                }
            }
        }
        helper.succeed();
    }

    // SPEC 4 章: 原木または原木パネル 6 個（3 × 2）→ 塀 24 個（竹ブロック 12 個）
    private static void logWallRecipe(GameTestHelper helper) {
        for (LogWood wood : ModBlocks.WOODS) {
            ItemStack log = new ItemStack(wood.log());
            ItemStack panel = new ItemStack(ModItems.logPanel(wood).get());
            assertOnlyRecipe(helper, CraftingInput.of(3, 2, Collections.nCopies(6, log)), ModItems.logWall(wood).get(), ModRecipeProvider.wallCount(wood));
            assertOnlyRecipe(helper, CraftingInput.of(3, 2, List.of(log, panel, log, panel, log, panel)),
                    ModItems.logWall(wood).get(), ModRecipeProvider.wallCount(wood));
        }
        helper.assertTrue(ModRecipeProvider.wallCount(LogWood.OAK) == 24 && ModRecipeProvider.wallCount(LogWood.BAMBOO) == 12,
                "wall counts should be 24 and 12");
        helper.succeed();
    }

    // 塀の柱あり・なし、低い壁・高い壁で、6 面それぞれが剥げる
    private static void stripWallFaces(GameTestHelper helper) {
        BlockState wall = ModBlocks.logWall(LogWood.OAK).get().defaultBlockState();
        assertStripEachFace(helper, wall.setValue(WallBlock.UP, true));
        assertStripEachFace(helper, wall.setValue(WallBlock.UP, false)
                .setValue(WallBlock.NORTH, WallSide.LOW).setValue(WallBlock.SOUTH, WallSide.LOW));
        assertStripEachFace(helper, wall.setValue(WallBlock.UP, true)
                .setValue(WallBlock.EAST, WallSide.TALL).setValue(WallBlock.WEST, WallSide.LOW));
        helper.succeed();
    }

    // SPEC 3.2: 斧でクリックした面の向きだけ剥がれ、斧の耐久が 1 減る
    private static void stripEachFace(GameTestHelper helper) {
        assertStripEachFace(helper, ModBlocks.logPanel(LogWood.OAK).get().defaultBlockState());
        helper.succeed();
    }

    // ハーフの下置き・上置き・2 枚重ねで、6 面それぞれが剥げる
    private static void stripSlabFaces(GameTestHelper helper) {
        for (SlabType type : SlabType.values()) {
            assertStripEachFace(helper, ModBlocks.logSlab(LogWood.OAK).get().defaultBlockState().setValue(SlabBlock.TYPE, type));
        }
        helper.succeed();
    }

    // 外周の 6 面それぞれの中央をクリックすると、その向きの状態だけ剥がれる
    private static void assertStripEachFace(GameTestHelper helper, BlockState initial) {
        for (Direction face : Direction.values()) {
            Vec3 local = new Vec3(0.5, 0.5, 0.5).add(Vec3.atLowerCornerOf(face.getUnitVec3i()).scale(0.5));
            assertStripsOnly(helper, initial, face, local, FaceStripping.strippedProperty(face));
        }
    }

    // ブロックエンティティに剥ぎ状態を持つブロック（SPEC 3.3.1）: local の位置を face 向きにクリックすると、expected の面だけ剥がれ、斧の耐久が 1 減る。
    // 同じ面をもう一度クリックしても変わらない
    private static void assertBlockEntityStripsOnly(GameTestHelper helper, BlockState initial, Direction face, Vec3 local, StrippedFace expected) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.AIR);
        helper.setBlock(pos, initial);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
        click(helper, player, pos, local, face);
        click(helper, player, pos, local, face);
        StrippedFacesBlockEntity blockEntity = helper.getBlockEntity(pos, StrippedFacesBlockEntity.class);
        helper.assertTrue(blockEntity.strippedMask() == expected.bit(),
                initial + ": clicked " + face + " at " + local + " expected only " + expected + " but mask=" + Integer.toBinaryString(blockEntity.strippedMask()));
        helper.assertTrue(player.getMainHandItem().getDamageValue() == 1, "axe damage should be 1 after stripping " + expected + " of " + initial);
    }

    // SPEC 3.3.1: 剥ぎ状態はブロックエンティティとして保存・読み込みされる
    private static void strippedFacesSaveLoad(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.logStairs(LogWood.OAK).get());
        StrippedFacesBlockEntity blockEntity = helper.getBlockEntity(pos, StrippedFacesBlockEntity.class);
        int mask = StrippedFace.NORTH.bit() | StrippedFace.TREAD.bit() | StrippedFace.RISER.bit();
        blockEntity.setStrippedMask(mask);
        HolderLookup.Provider registries = helper.getLevel().registryAccess();
        CompoundTag tag = blockEntity.saveCustomOnly(registries);
        StrippedFacesBlockEntity loaded = new StrippedFacesBlockEntity(blockEntity.getBlockPos(), blockEntity.getBlockState());
        loaded.loadCustomOnly(TagValueInput.create(ProblemReporter.DISCARDING, registries, tag));
        helper.assertTrue(loaded.strippedMask() == mask, "loaded mask " + loaded.strippedMask() + " != " + mask);
        helper.succeed();
    }

    // local の位置を face 向きにクリックすると、剥ぎ状態（*_stripped）のうち expected だけ剥がれ、斧の耐久が 1 減る
    private static void assertStripsOnly(GameTestHelper helper, BlockState initial, Direction face, Vec3 local, BooleanProperty expected) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, initial);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
        click(helper, player, pos, local, face);
        BlockState state = helper.getBlockState(pos);
        for (Property<?> property : state.getProperties()) {
            if (property instanceof BooleanProperty bool && bool.getName().endsWith("_stripped")) {
                helper.assertTrue(state.getValue(bool) == (bool == expected),
                        initial + ": clicked " + face + " at " + local + " but " + bool.getName() + "=" + state.getValue(bool));
            }
        }
        helper.assertTrue(player.getMainHandItem().getDamageValue() == 1, "axe damage should be 1 after stripping " + expected.getName() + " of " + initial);
    }

    // SPEC 3.2: 剥がれ済みの面をクリックしても何も起きない（斧も消費しない）
    private static void stripStrippedFace(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos pos = new BlockPos(1, 1, 1);
        BlockState stripped = ModBlocks.logPanel(LogWood.OAK).get().defaultBlockState()
                .setValue(FaceStripping.strippedProperty(Direction.NORTH), true);
        helper.setBlock(pos, stripped);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
        clickFace(helper, player, pos, Direction.NORTH);
        helper.assertTrue(helper.getBlockState(pos) == stripped, "state changed: " + helper.getBlockState(pos));
        helper.assertTrue(player.getMainHandItem().getDamageValue() == 0, "axe was damaged");
        helper.succeed();
    }

    // ブロックの face 側の面の中央をクリックする
    private static void clickFace(GameTestHelper helper, Player player, BlockPos relativePos, Direction face) {
        click(helper, player, relativePos, new Vec3(0.5, 0.5, 0.5).add(Vec3.atLowerCornerOf(face.getUnitVec3i()).scale(0.5)), face);
    }

    // ブロック内の位置 local（0〜1）を face 向きにクリックする
    private static void click(GameTestHelper helper, Player player, BlockPos relativePos, Vec3 local, Direction face) {
        BlockPos absolutePos = helper.absolutePos(relativePos);
        helper.useBlock(relativePos, player, new BlockHitResult(Vec3.atLowerCornerOf(absolutePos).add(local), face, absolutePos, false));
    }

    private static void assertOnlyRecipe(GameTestHelper helper, CraftingInput input, ItemLike expected, int count) {
        List<RecipeHolder<CraftingRecipe>> recipes = helper.getLevel().getServer().getRecipeManager().recipeMap()
                .getRecipesFor(RecipeType.CRAFTING, input, helper.getLevel()).toList();
        helper.assertTrue(recipes.size() == 1, input.items() + " should match one recipe but matched " + recipes.stream().map(RecipeHolder::id).toList());
        ItemStack result = recipes.get(0).value().assemble(input);
        helper.assertTrue(result.is(expected.asItem()) && result.getCount() == count,
                input.items() + " should make " + count + " " + expected + " but makes " + result);
    }
}
