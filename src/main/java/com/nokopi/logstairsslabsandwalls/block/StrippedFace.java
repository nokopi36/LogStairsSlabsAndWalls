package com.nokopi.logstairsslabsandwalls.block;

import com.mojang.serialization.Codec;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import org.jspecify.annotations.Nullable;

/**
 * ブロックエンティティに保存する剥ぎ状態の面（SPEC 3.2 / 3.3.1）。ordinal がビットの位置。
 * 最初の 6 つは外周の面で、並びは Direction と同じ（ワールドの向き）。階段だけ段の内側の 2 面、
 * ハーフだけ上のハーフの側面 4 面を使う。
 */
public enum StrippedFace implements StringRepresentable {
    DOWN("down"),
    UP("up"),
    NORTH("north"),
    SOUTH("south"),
    WEST("west"),
    EAST("east"),
    // 下の段の踏み面（上下逆の階段では下の段の裏側）
    TREAD("tread"),
    // 上の段の蹴上げ（内角・外角では 2 面）
    RISER("riser"),
    // ハーフの上のハーフ（上置き・2 枚重ねの上半分）の側面
    UPPER_NORTH("upper_north"),
    UPPER_SOUTH("upper_south"),
    UPPER_WEST("upper_west"),
    UPPER_EAST("upper_east");

    // 下置きのハーフ・2 枚重ねの下半分の側面と、上のハーフの側面
    public static final int LOWER_SIDES = NORTH.bit() | SOUTH.bit() | WEST.bit() | EAST.bit();
    public static final int UPPER_SIDES = UPPER_NORTH.bit() | UPPER_SOUTH.bit() | UPPER_WEST.bit() | UPPER_EAST.bit();

    public static final Codec<StrippedFace> CODEC = StringRepresentable.fromEnum(StrippedFace::values);

    private final String name;

    StrippedFace(String name) {
        this.name = name;
    }

    public static StrippedFace of(Direction direction) {
        return values()[direction.ordinal()];
    }

    // 上のハーフの側面（direction は横向き）
    public static StrippedFace upper(Direction direction) {
        return switch (direction) {
            case NORTH -> UPPER_NORTH;
            case SOUTH -> UPPER_SOUTH;
            case WEST -> UPPER_WEST;
            case EAST -> UPPER_EAST;
            default -> throw new IllegalArgumentException("not a horizontal direction: " + direction);
        };
    }

    /**
     * ストラクチャーで反転・回転したとき、この面が移る先（ブロックの向きと同じく、反転してから回転）。
     * 横向きの面と上半分の側面だけ向きが変わり、上面・下面・踏み面・蹴上げはそのまま。
     */
    public StrippedFace transform(Mirror mirror, Rotation rotation) {
        Direction direction = this.horizontalDirection();
        if (direction == null) {
            return this;
        }
        Direction moved = rotation.rotate(mirror.mirror(direction));
        return this.isUpperSide() ? upper(moved) : of(moved);
    }

    // 剥ぎ状態のビットの集まり全体を transform で移す
    public static int transform(int mask, Mirror mirror, Rotation rotation) {
        int result = 0;
        for (StrippedFace face : values()) {
            if (face.isStrippedIn(mask)) {
                result |= face.transform(mirror, rotation).bit();
            }
        }
        return result;
    }

    private boolean isUpperSide() {
        return (UPPER_SIDES & this.bit()) != 0;
    }

    private @Nullable Direction horizontalDirection() {
        return switch (this) {
            case NORTH, UPPER_NORTH -> Direction.NORTH;
            case SOUTH, UPPER_SOUTH -> Direction.SOUTH;
            case WEST, UPPER_WEST -> Direction.WEST;
            case EAST, UPPER_EAST -> Direction.EAST;
            default -> null;
        };
    }

    public int bit() {
        return 1 << this.ordinal();
    }

    public boolean isStrippedIn(int mask) {
        return (mask & this.bit()) != 0;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
