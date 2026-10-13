package com.nokopi.logstairsslabsandwalls.datagen;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

import com.nokopi.logstairsslabsandwalls.LogStairsSlabsAndWalls;
import com.nokopi.logstairsslabsandwalls.LogWood;
import com.nokopi.logstairsslabsandwalls.ModBlocks;
import com.nokopi.logstairsslabsandwalls.ModItems;
import com.nokopi.logstairsslabsandwalls.block.StrippedFace;
import com.nokopi.logstairsslabsandwalls.client.StrippedFaceModel;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.neoforged.neoforge.client.model.generators.blockstate.CustomBlockStateModelBuilder;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.level.block.state.properties.SlabType;

public class ModModelProvider extends ModelProvider {
    // assets/log_stairs_slabs_and_walls/models/block/template_log_panel_*.json（手書き）を型にする。
    // バニラの cube_column から 1 面だけを取り出したもの。横置きの上面だけは cube_column_horizontal と同じく UV を 180° 回す
    private static final Map<Direction, ModelTemplate> PANEL_FACES = new EnumMap<>(Direction.class);
    private static final ModelTemplate PANEL_HORIZONTAL_UP = template("template_log_panel_horizontal_up", "_horizontal_up");
    // バニラの slab / slab_top から 1 面だけを取り出したもの。2 枚重ねは下置きの下面・側面と上置きの上面・側面を組み合わせる
    private static final Map<Direction, ModelTemplate> SLAB_BOTTOM_FACES = new EnumMap<>(Direction.class);
    private static final Map<Direction, ModelTemplate> SLAB_TOP_FACES = new EnumMap<>(Direction.class);
    // バニラの stairs / inner_stairs / outer_stairs を、外周の面（向きごと）と段の内側の面（踏み面・蹴上げ）に分けたもの。
    // キーは向きの名前と "tread" / "riser"
    private static final List<String> STAIRS_PARTS = List.of("down", "up", "north", "south", "west", "east", "tread", "riser");
    private static final Map<String, ModelTemplate> STAIRS_STRAIGHT_PARTS = stairsTemplates("stairs", "");
    private static final Map<String, ModelTemplate> STAIRS_INNER_PARTS = stairsTemplates("inner_stairs", "_inner");
    private static final Map<String, ModelTemplate> STAIRS_OUTER_PARTS = stairsTemplates("outer_stairs", "_outer");
    // バニラの template_wall_post / _side / _side_tall から 1 面だけを取り出したもの（壁の南の面は元のモデルにもない）
    private static final Map<Direction, ModelTemplate> WALL_POST_FACES = new EnumMap<>(Direction.class);
    private static final Map<Direction, ModelTemplate> WALL_LOW_SIDE_FACES = new EnumMap<>(Direction.class);
    private static final Map<Direction, ModelTemplate> WALL_TALL_SIDE_FACES = new EnumMap<>(Direction.class);
    private static final ModelTemplate WALL_INVENTORY = template("template_log_wall_inventory", "_inventory");

    static {
        for (Direction face : Direction.values()) {
            String name = face.getSerializedName();
            PANEL_FACES.put(face, template("template_log_panel_" + name, "_" + name));
            SLAB_BOTTOM_FACES.put(face, template("template_log_slab_" + name, "_" + name));
            SLAB_TOP_FACES.put(face, template("template_log_slab_top_" + name, "_top_" + name));
            WALL_POST_FACES.put(face, template("template_log_wall_post_" + name, "_post_" + name));
            if (face != Direction.SOUTH) {
                WALL_LOW_SIDE_FACES.put(face, template("template_log_wall_side_" + name, "_side_" + name));
                WALL_TALL_SIDE_FACES.put(face, template("template_log_wall_side_tall_" + name, "_side_tall_" + name));
            }
        }
    }

    private static final String STRIPPED_SUFFIX = "_stripped";

    public ModModelProvider(PackOutput output) {
        super(output, LogStairsSlabsAndWalls.MODID);
    }

    private static Map<String, ModelTemplate> stairsTemplates(String shape, String suffix) {
        Map<String, ModelTemplate> templates = new HashMap<>();
        for (String part : STAIRS_PARTS) {
            templates.put(part, template("template_log_" + shape + "_" + part, suffix + "_" + part));
        }
        return templates;
    }

    private static ModelTemplate template(String name, String suffix) {
        return new ModelTemplate(Optional.of(Identifier.fromNamespaceAndPath(LogStairsSlabsAndWalls.MODID, "block/" + name)),
                Optional.of(suffix), TextureSlot.SIDE, TextureSlot.END);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        for (LogWood wood : ModBlocks.WOODS) {
            createLogPanel(blockModels, ModBlocks.logPanel(wood).get(), wood.log(), wood.strippedLog());
            createLogStairs(blockModels, ModBlocks.logStairs(wood).get(), wood.log(), wood.strippedLog());
            createLogSlab(blockModels, ModBlocks.logSlab(wood).get(), wood.log(), wood.strippedLog());
            createLogWall(blockModels, ModBlocks.logWall(wood).get(), wood.log(), wood.strippedLog());
            createStrippedItemModels(blockModels, wood);
        }
    }

    // SPEC 3.5: 剥いだ版のアイテムは、剥いだ原木のテクスチャのインベントリモデル（形は普通の版と同じ）
    private static void createStrippedItemModels(BlockModelGenerators blockModels, LogWood wood) {
        TextureMapping stripped = logTextures(wood.strippedLog());
        blockModels.registerSimpleItemModel(ModItems.strippedLogPanel(wood).get(), ModelTemplates.CUBE_COLUMN.createWithSuffix(
                ModBlocks.logPanel(wood).get(), STRIPPED_SUFFIX, stripped, blockModels.modelOutput));
        blockModels.registerSimpleItemModel(ModItems.strippedLogStairs(wood).get(), ModelTemplates.STAIRS_STRAIGHT.createWithSuffix(
                ModBlocks.logStairs(wood).get(), STRIPPED_SUFFIX, slabAndStairsTextures(wood.strippedLog()), blockModels.modelOutput));
        blockModels.registerSimpleItemModel(ModItems.strippedLogSlab(wood).get(), ModelTemplates.SLAB_BOTTOM.createWithSuffix(
                ModBlocks.logSlab(wood).get(), STRIPPED_SUFFIX, slabAndStairsTextures(wood.strippedLog()), blockModels.modelOutput));
        blockModels.registerSimpleItemModel(ModItems.strippedLogWall(wood).get(), WALL_INVENTORY.createWithSuffix(
                ModBlocks.logWall(wood).get(), STRIPPED_SUFFIX, stripped, blockModels.modelOutput));
    }

    // バニラの stairs / slab モデル用（上下は年輪、横は樹皮）
    private static TextureMapping slabAndStairsTextures(Block log) {
        return new TextureMapping()
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(log, "_top"))
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(log, "_top"))
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(log));
    }

    private static TextureMapping logTextures(Block log) {
        return new TextureMapping()
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(log))
                .put(TextureSlot.END, TextureMapping.getBlockTexture(log, "_top"));
    }

    private static Identifier createModel(BlockModelGenerators blockModels, ModelTemplate template, Block block,
            TextureMapping textures, boolean isStripped) {
        return isStripped
                ? template.createWithSuffix(block, STRIPPED_SUFFIX, textures, blockModels.modelOutput)
                : template.create(block, textures, blockModels.modelOutput);
    }

    /**
     * 原木パネル: 1 面ずつのモデルを、その面がワールドで向く方向の剥ぎ状態（ブロックエンティティ）で切り替える。
     * 軸ごとの回転はバニラの原木の blockstate と同じ（y: なし / z: x=90 / x: x=90, y=90）。
     */
    private static void createLogPanel(BlockModelGenerators blockModels, Block panel, Block log, Block strippedLog) {
        TextureMapping bark = logTextures(log);
        TextureMapping stripped = logTextures(strippedLog);

        // 1 面ずつのモデル（型 × 樹皮あり／なし）は軸をまたいで使い回す
        Map<ModelTemplate, MultiVariant[]> models = new HashMap<>();
        for (ModelTemplate template : allPanelTemplates()) {
            models.put(template, new MultiVariant[] {
                    BlockModelGenerators.plainVariant(createModel(blockModels, template, panel, bark, false)),
                    BlockModelGenerators.plainVariant(createModel(blockModels, template, panel, stripped, true))});
        }

        MultiPartGenerator generator = MultiPartGenerator.multiPart(panel);
        for (Direction.Axis axis : Direction.Axis.values()) {
            boolean horizontal = axis != Direction.Axis.Y;
            for (Direction face : Direction.values()) {
                ModelTemplate template = horizontal && face == Direction.UP ? PANEL_HORIZONTAL_UP : PANEL_FACES.get(face);
                Direction worldFace = rotateForAxis(face, axis);
                MultiVariant[] barkAndStripped = models.get(template);
                generator.with(BlockModelGenerators.condition().term(RotatedPillarBlock.AXIS, axis),
                        strippedFace(StrippedFace.of(worldFace),
                                axisRotation(axis).apply(barkAndStripped[0]), axisRotation(axis).apply(barkAndStripped[1])));
            }
        }
        blockModels.blockStateOutput.accept(generator);

        // インベントリでは樹皮付きの縦置きの原木の見た目
        blockModels.registerSimpleItemModel(panel, ModelTemplates.CUBE_COLUMN.create(panel, bark, blockModels.modelOutput));
    }

    // 樹皮あり（[0]）・剥いだもの（[1]）の 2 つのモデル
    private static MultiVariant[] barkAndStripped(BlockModelGenerators blockModels, ModelTemplate template, Block block,
            TextureMapping bark, TextureMapping stripped) {
        return new MultiVariant[] {
                BlockModelGenerators.plainVariant(createModel(blockModels, template, block, bark, false)),
                BlockModelGenerators.plainVariant(createModel(blockModels, template, block, stripped, true))};
    }

    /**
     * 原木の階段: 縦置きの原木の見た目。回転はバニラの createStairs と同じ（上下逆は x=180、向きと形で y、uvlock）。
     * 外周の面は回した後にワールドで向く方向、段の内側の面は踏み面・蹴上げとして、ブロックエンティティの剥ぎ状態で切り替える。
     */
    private static void createLogStairs(BlockModelGenerators blockModels, Block stairs, Block log, Block strippedLog) {
        TextureMapping bark = logTextures(log);
        TextureMapping stripped = logTextures(strippedLog);

        Map<StairsShape, Map<String, MultiVariant[]>> models = new EnumMap<>(StairsShape.class);
        for (StairsShape shape : StairsShape.values()) {
            Map<String, ModelTemplate> templates = switch (shape) {
                case STRAIGHT -> STAIRS_STRAIGHT_PARTS;
                case INNER_LEFT, INNER_RIGHT -> STAIRS_INNER_PARTS;
                case OUTER_LEFT, OUTER_RIGHT -> STAIRS_OUTER_PARTS;
            };
            // 左右は同じモデルを回転だけ変えて使うので、モデルは 1 回だけ作る（enum の並びで先に来る LEFT で作る）
            StairsShape owner = switch (shape) {
                case INNER_RIGHT -> StairsShape.INNER_LEFT;
                case OUTER_RIGHT -> StairsShape.OUTER_LEFT;
                default -> shape;
            };
            if (owner != shape) {
                models.put(shape, models.get(owner));
                continue;
            }
            Map<String, MultiVariant[]> byPart = new HashMap<>();
            for (String part : STAIRS_PARTS) {
                byPart.put(part, barkAndStripped(blockModels, templates.get(part), stairs, bark, stripped));
            }
            models.put(shape, byPart);
        }

        MultiPartGenerator generator = MultiPartGenerator.multiPart(stairs);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            for (Half half : Half.values()) {
                for (StairsShape shape : StairsShape.values()) {
                    boolean upsideDown = half == Half.TOP;
                    int yRotation = stairsYRotation(facing, half, shape);
                    for (String part : STAIRS_PARTS) {
                        StrippedFace face = switch (part) {
                            case "tread" -> StrippedFace.TREAD;
                            case "riser" -> StrippedFace.RISER;
                            default -> StrippedFace.of(
                                    rotateY(upsideDown ? rotateX180(Direction.byName(part)) : Direction.byName(part), yRotation));
                        };
                        MultiVariant[] barkAndStripped = new MultiVariant[2];
                        for (int i = 0; i < 2; i++) {
                            MultiVariant model = models.get(shape).get(part)[i];
                            if (upsideDown) {
                                model = model.with(BlockModelGenerators.X_ROT_180);
                            }
                            barkAndStripped[i] = withYRotation(model, yRotation).with(BlockModelGenerators.UV_LOCK);
                        }
                        generator.with(BlockModelGenerators.condition()
                                .term(StairBlock.FACING, facing)
                                .term(StairBlock.HALF, half)
                                .term(StairBlock.SHAPE, shape),
                                strippedFace(face, barkAndStripped[0], barkAndStripped[1]));
                    }
                }
            }
        }
        blockModels.blockStateOutput.accept(generator);

        // インベントリでは樹皮付きの見た目（バニラの stairs モデル）
        blockModels.registerSimpleItemModel(stairs, ModelTemplates.STAIRS_STRAIGHT.create(stairs, slabAndStairsTextures(log), blockModels.modelOutput));
    }

    /**
     * 原木の塀: 縦置きの原木の見た目。回転はバニラの createWall と同じ（壁は北向きが基準で y、uvlock）。
     * 柱と壁の 1 面ずつのモデルを、ワールドで向く方向の剥ぎ状態（ブロックエンティティ）で切り替える。
     */
    private static void createLogWall(BlockModelGenerators blockModels, Block wall, Block log, Block strippedLog) {
        TextureMapping bark = logTextures(log);
        TextureMapping stripped = logTextures(strippedLog);

        MultiPartGenerator generator = MultiPartGenerator.multiPart(wall);
        for (Direction face : Direction.values()) {
            MultiVariant[] post = barkAndStripped(blockModels, WALL_POST_FACES.get(face), wall, bark, stripped);
            generator.with(BlockModelGenerators.condition().term(WallBlock.UP, true),
                    strippedFace(StrippedFace.of(face), post[0], post[1]));
        }
        addWallSides(blockModels, generator, wall, WallSide.LOW, WALL_LOW_SIDE_FACES, bark, stripped);
        addWallSides(blockModels, generator, wall, WallSide.TALL, WALL_TALL_SIDE_FACES, bark, stripped);
        blockModels.blockStateOutput.accept(generator);

        // インベントリではバニラの wall_inventory と同じ形の、樹皮付きの見た目
        blockModels.registerSimpleItemModel(wall, WALL_INVENTORY.create(wall, bark, blockModels.modelOutput));
    }

    private static void addWallSides(BlockModelGenerators blockModels, MultiPartGenerator generator, Block wall, WallSide height,
            Map<Direction, ModelTemplate> templates, TextureMapping bark, TextureMapping stripped) {
        for (Map.Entry<Direction, ModelTemplate> entry : templates.entrySet()) {
            MultiVariant[] models = barkAndStripped(blockModels, entry.getValue(), wall, bark, stripped);
            for (Direction side : Direction.Plane.HORIZONTAL) {
                // 北向きの壁を side の向きに回す角度
                int yRotation = switch (side) {
                    case EAST -> 90;
                    case SOUTH -> 180;
                    case WEST -> 270;
                    default -> 0;
                };
                Direction worldFace = rotateY(entry.getKey(), yRotation);
                generator.with(BlockModelGenerators.condition().term(WallBlock.PROPERTY_BY_DIRECTION.get(side), height),
                        strippedFace(StrippedFace.of(worldFace),
                                withYRotation(models[0], yRotation).with(BlockModelGenerators.UV_LOCK),
                                withYRotation(models[1], yRotation).with(BlockModelGenerators.UV_LOCK)));
            }
        }
    }

    // ブロックエンティティの剥ぎ状態で bark / stripped を切り替えるモデル（SPEC 3.3.1、client/StrippedFaceModel）
    private static MultiVariant strippedFace(StrippedFace face, MultiVariant bark, MultiVariant stripped) {
        return MultiVariant.of(new CustomBlockStateModelBuilder.Simple(
                new StrippedFaceModel.Unbaked(face, bark.toUnbaked(), stripped.toUnbaked())));
    }

    // BlockModelGenerators#createStairs の y 回転（度）。モデルは東向きが基準
    private static int stairsYRotation(Direction facing, Half half, StairsShape shape) {
        int base = switch (facing) {
            case SOUTH -> 90;
            case WEST -> 180;
            case NORTH -> 270;
            default -> 0;
        };
        boolean left = shape == StairsShape.INNER_LEFT || shape == StairsShape.OUTER_LEFT;
        boolean right = shape == StairsShape.INNER_RIGHT || shape == StairsShape.OUTER_RIGHT;
        int offset = half == Half.BOTTOM ? (left ? 270 : 0) : (right ? 90 : 0);
        return (base + offset) % 360;
    }

    private static MultiVariant withYRotation(MultiVariant model, int degrees) {
        return switch (degrees) {
            case 90 -> model.with(BlockModelGenerators.Y_ROT_90);
            case 180 -> model.with(BlockModelGenerators.Y_ROT_180);
            case 270 -> model.with(BlockModelGenerators.Y_ROT_270);
            default -> model;
        };
    }

    // blockstate の x=180: 上と下、北と南が入れ替わる
    private static Direction rotateX180(Direction face) {
        return face.getAxis() == Direction.Axis.X ? face : face.getOpposite();
    }

    private static Direction rotateY(Direction face, int degrees) {
        for (int i = 0; i < degrees / 90; i++) {
            face = rotateY90(face);
        }
        return face;
    }

    /**
     * 原木のハーフ: 縦置きの原木の見た目。下置き・上置き・2 枚重ねのそれぞれで、1 面ずつのモデルを組み合わせる（回転なし）。
     * 上のハーフ（上置き・2 枚重ねの上半分）の側面は upper_* の剥ぎ状態で切り替える（SPEC 3.2）。
     * 2 枚重ねは、下置きの下面・側面と上置きの上面・側面（中の 2 面は見えないので使わない）。
     */
    private static void createLogSlab(BlockModelGenerators blockModels, Block slab, Block log, Block strippedLog) {
        TextureMapping bark = logTextures(log);
        TextureMapping stripped = logTextures(strippedLog);

        Map<Direction, MultiVariant[]> bottom = new EnumMap<>(Direction.class);
        Map<Direction, MultiVariant[]> top = new EnumMap<>(Direction.class);
        for (Direction face : Direction.values()) {
            bottom.put(face, barkAndStripped(blockModels, SLAB_BOTTOM_FACES.get(face), slab, bark, stripped));
            top.put(face, barkAndStripped(blockModels, SLAB_TOP_FACES.get(face), slab, bark, stripped));
        }

        MultiPartGenerator generator = MultiPartGenerator.multiPart(slab);
        for (Direction face : Direction.values()) {
            StrippedFace upperFace = face.getAxis().isHorizontal() ? StrippedFace.upper(face) : StrippedFace.of(face);
            addSlabFace(generator, SlabType.BOTTOM, StrippedFace.of(face), bottom.get(face));
            addSlabFace(generator, SlabType.TOP, upperFace, top.get(face));
            if (face != Direction.UP) {
                addSlabFace(generator, SlabType.DOUBLE, StrippedFace.of(face), bottom.get(face));
            }
            if (face != Direction.DOWN) {
                addSlabFace(generator, SlabType.DOUBLE, upperFace, top.get(face));
            }
        }
        blockModels.blockStateOutput.accept(generator);

        // インベントリでは樹皮付きの下置きの見た目（バニラの slab モデル）
        blockModels.registerSimpleItemModel(slab, ModelTemplates.SLAB_BOTTOM.create(slab, slabAndStairsTextures(log), blockModels.modelOutput));
    }

    private static void addSlabFace(MultiPartGenerator generator, SlabType type, StrippedFace face, MultiVariant[] barkAndStripped) {
        generator.with(BlockModelGenerators.condition().term(SlabBlock.TYPE, type),
                strippedFace(face, barkAndStripped[0], barkAndStripped[1]));
    }

    private static List<ModelTemplate> allPanelTemplates() {
        List<ModelTemplate> templates = new ArrayList<>(PANEL_FACES.values());
        templates.add(PANEL_HORIZONTAL_UP);
        return templates;
    }

    private static UnaryOperator<MultiVariant> axisRotation(Direction.Axis axis) {
        return switch (axis) {
            case X -> model -> model.with(BlockModelGenerators.X_ROT_90).with(BlockModelGenerators.Y_ROT_90);
            case Y -> UnaryOperator.identity();
            case Z -> model -> model.with(BlockModelGenerators.X_ROT_90);
        };
    }

    // モデルの面が、axisRotation の回転の後にワールドで向く方向
    static Direction rotateForAxis(Direction face, Direction.Axis axis) {
        return switch (axis) {
            case X -> rotateY90(rotateX90(face));
            case Y -> face;
            case Z -> rotateX90(face);
        };
    }

    // blockstate の x=90: 上 → 北 → 下 → 南 → 上（end_rod の facing=north が x=90 なのと同じ）
    private static Direction rotateX90(Direction face) {
        return switch (face) {
            case UP -> Direction.NORTH;
            case NORTH -> Direction.DOWN;
            case DOWN -> Direction.SOUTH;
            case SOUTH -> Direction.UP;
            default -> face;
        };
    }

    // blockstate の y=90: 北 → 東 → 南 → 西 → 北
    private static Direction rotateY90(Direction face) {
        return face.getAxis() == Direction.Axis.Y ? face : face.getClockWise();
    }
}
