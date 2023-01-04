package org.ru.vortex.modules.history.components;

import arc.math.geom.Position;
import mindustry.net.Administration.PlayerInfo;
import mindustry.world.Block;

public record FormattedEntry(
        BlockChangeType changeType,
        PlayerInfo participantInfo,
        Block block,
        Position position,
        long additionalData
) {
    public void applyAdditionalData() {
        changeType.applyAdditionalData.get(block, additionalData);
    }
}
