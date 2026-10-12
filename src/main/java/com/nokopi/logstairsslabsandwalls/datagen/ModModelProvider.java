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
import com.nokopi.logstairsslabsandwalls.block.FaceStripping;

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
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.block.state.properties.SlabType;

public class ModModelProvider extends ModelProvider {
    // assets/log_stairs_slabs_and_walls/models/block/template_log_panel_*.json（手書き）を型にする。
    // バニラの cube_column から 1 面だけを取り出したもの。横置きの上面だけは cube_column_horizontal と同じく UV を 180° 回す
    private static final Map<Direction, ModelTemplate> PANEL_FACES = new EnumMap<>(Direction.class);
    private static final ModelTemplate PANEL_HORIZONTAL_UP = template("template_log_panel_horizontal_up", "_horizontal_up");
    // バニラの slab / slab_top から 1 面だけを取り出したもの。2 枚重ねはパネルの縦置きの型を使う
    private static final Map<Direction, ModelTemplate> SLAB_BOTTOM_FACES = new EnumMap<>(Direction.class);
    private static final Map<Direction, ModelTemplate> SLAB_TOP_FACES = new EnumMap<>(Direction.class);
    private static final Map<Direction, ModelTemplate> SLAB_DOUBLE_FACES = new EnumMap<>(Direction.class);
    // バニラの stairs / inner_stairs / outer_stairs を面の向きごとに分けたもの（その向きの面をすべて含む）
    private static final Map<Direction, ModelTemplate> STAIRS_STRAIGHT_FACES = new EnumMap<>(Direction.class);
    private static final Map<Direction, ModelTemplate> STAIRS_INNER_FACES = new EnumMap<>(Direction.class);
    private static final Map<Direction, ModelTemplate> STAIRS_OUTER_FACES = new EnumMap<>(Direction.class);

    static {
        for (Direction face : Direction.values()) {
            String name = face.getSerializedName();
            PANEL_FACES.put(face, template("template_log_panel_" + name, "_" + name));
            SLAB_BOTTOM_FACES.put(face, template("template_log_slab_" + name, "_" + name));
            SLAB_TOP_FACES.put(face, template("template_log_slab_top_" + name, "_top_" + name));
            SLAB_DOUBLE_FACES.put(face, template("template_log_panel_" + name, "_double_" + name));
            STAIRS_STRAIGHT_FACES.put(face, template("template_log_stairs_" + name, "_" + name));
            STAIRS_INNER_FACES.put(face, template("template_log_inner_stairs_" + name, "_inner_" + name));
            STAIRS_OUTER_FACES.put(face, template("template_log_outer_stairs_" + name, "_outer_" + name));
        }
    }

    private static final String STRIPPED_SUFFIX = "_stripped";

    public ModModelProvider(PackOutput output) {
        super(output, LogStairsSlabsAndWalls.MODID);
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
        }
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
     * 原木パネル: 1 面ずつのモデルを、その面がワールドで向く方向の _stripped を条件に組み合わせる。
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
                for (boolean isStripped : new boolean[] {false, true}) {
                    MultiVariant model = models.get(template)[isStripped ? 1 : 0];
                    generator.with(BlockModelGenerators.condition()
                            .term(RotatedPillarBlock.AXIS, axis)
                            .term(FaceStripping.strippedProperty(worldFace), isStripped), axisRotation(axis).apply(model));
                }
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
     * 1 面ずつのモデルを、回した後にワールドで向く方向の _stripped を条件に組み合わせる。
     */
    private static void createLogStairs(BlockModelGenerators blockModels, Block stairs, Block log, Block strippedLog) {
        TextureMapping bark = logTextures(log);
        TextureMapping stripped = logTextures(strippedLog);

        Map<StairsShape, Map<Direction, MultiVariant[]>> models = new EnumMap<>(StairsShape.class);
        for (StairsShape shape : StairsShape.values()) {
            Map<Direction, ModelTemplate> templates = switch (shape) {
                case STRAIGHT -> STAIRS_STRAIGHT_FACES;
                case INNER_LEFT, INNER_RIGHT -> STAIRS_INNER_FACES;
                case OUTER_LEFT, OUTER_RIGHT -> STAIRS_OUTER_FACES;
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
            Map<Direction, MultiVariant[]> byFace = new EnumMap<>(Direction.class);
            for (Direction face : Direction.values()) {
                byFace.put(face, barkAndStripped(blockModels, templates.get(face), stairs, bark, stripped));
            }
            models.put(shape, byFace);
        }

        MultiPartGenerator generator = MultiPartGenerator.multiPart(stairs);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            for (Half half : Half.values()) {
                for (StairsShape shape : StairsShape.values()) {
                    boolean upsideDown = half == Half.TOP;
                    int yRotation = stairsYRotation(facing, half, shape);
                    for (Direction face : Direction.values()) {
                        Direction worldFace = rotateY(upsideDown ? rotateX180(face) : face, yRotation);
                        for (boolean isStripped : new boolean[] {false, true}) {
                            MultiVariant model = models.get(shape).get(face)[isStripped ? 1 : 0];
                            if (upsideDown) {
                                model = model.with(BlockModelGenerators.X_ROT_180);
                            }
                            generator.with(BlockModelGenerators.condition()
                                    .term(StairBlock.FACING, facing)
                                    .term(StairBlock.HALF, half)
                                    .term(StairBlock.SHAPE, shape)
                                    .term(FaceStripping.strippedProperty(worldFace), isStripped),
                                    withYRotation(model, yRotation).with(BlockModelGenerators.UV_LOCK));
                        }
                    }
                }
            }
        }
        blockModels.blockStateOutput.accept(generator);

        // インベントリでは樹皮付きの見た目（バニラの stairs モデル）
        blockModels.registerSimpleItemModel(stairs, ModelTemplates.STAIRS_STRAIGHT.create(stairs, new TextureMapping()
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(log, "_top"))
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(log, "_top"))
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(log)), blockModels.modelOutput));
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
     */
    private static void createLogSlab(BlockModelGenerators blockModels, Block slab, Block log, Block strippedLog) {
        TextureMapping bark = logTextures(log);
        TextureMapping stripped = logTextures(strippedLog);

        MultiPartGenerator generator = MultiPartGenerator.multiPart(slab);
        for (SlabType type : SlabType.values()) {
            Map<Direction, ModelTemplate> faces = switch (type) {
                case BOTTOM -> SLAB_BOTTOM_FACES;
                case TOP -> SLAB_TOP_FACES;
                case DOUBLE -> SLAB_DOUBLE_FACES;
            };
            for (Direction face : Direction.values()) {
                MultiVariant[] models = barkAndStripped(blockModels, faces.get(face), slab, bark, stripped);
                for (boolean isStripped : new boolean[] {false, true}) {
                    generator.with(BlockModelGenerators.condition()
                            .term(SlabBlock.TYPE, type)
                            .term(FaceStripping.strippedProperty(face), isStripped), models[isStripped ? 1 : 0]);
                }
            }
        }
        blockModels.blockStateOutput.accept(generator);

        // インベントリでは樹皮付きの下置きの見た目（バニラの slab モデル）
        blockModels.registerSimpleItemModel(slab, ModelTemplates.SLAB_BOTTOM.create(slab, new TextureMapping()
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(log, "_top"))
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(log, "_top"))
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(log)), blockModels.modelOutput));
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
