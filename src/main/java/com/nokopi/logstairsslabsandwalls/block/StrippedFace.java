package com.nokopi.logstairsslabsandwalls.block;

import com.mojang.serialization.Codec;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

/**
 * ブロックエンティティに保存する剥ぎ状態の面（SPEC 3.2 / 3.3.1）。ordinal がビットの位置。
 * 最初の 6 つは外周の面で、並びは Direction と同じ（ワールドの向き）。階段だけ段の内側の 2 面を使う。
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
    RISER("riser");

    public static final Codec<StrippedFace> CODEC = StringRepresentable.fromEnum(StrippedFace::values);

    private final String name;

    StrippedFace(String name) {
        this.name = name;
    }

    public static StrippedFace of(Direction direction) {
        return values()[direction.ordinal()];
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
