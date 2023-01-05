package org.ru.vortex.modules.history.components;

import arc.func.Cons2;
import arc.math.geom.Position;
import java.util.function.Function;
import mindustry.net.Administration.PlayerInfo;
import mindustry.world.Block;

public enum BlockChangeType {
    Destroyed(),
    Built(),
    PayloadDrop(),
    Pickup();

    public final byte id;
    public final Function<Block, Long> packageData;
    public final Cons2<Block, Long> applyAdditionalData;

    BlockChangeType() {
        this(block -> 0L, (block, packagedData) -> {});
    }

    BlockChangeType(Function<Block, Long> packageData, Cons2<Block, Long> applyAdditionalData) {
        this.id = (byte) (StaticFields.lastID += 1);
        this.packageData = packageData;
        this.applyAdditionalData = applyAdditionalData;
    }

    public static BlockChangeType getByID(byte id) {
        for (BlockChangeType changeType : BlockChangeType.values()) {
            if (changeType.id == id) return changeType;
        }

        return null;
    }

    public FormattedEntry formatEntry(PlayerInfo participantInfo, Block block, Position position, long additionalData) {
        return new FormattedEntry(this, participantInfo, block, position, additionalData);
    }

    public FormattedEntry formatEntry(PlayerInfo participantInfo, Block block, Position position) {
        return formatEntry(participantInfo, block, position, packageData.apply(block));
    }

    public static final class StaticFields {

        public static int lastID = -1;
    }
}
