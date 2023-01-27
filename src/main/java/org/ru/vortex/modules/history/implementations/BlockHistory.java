package org.ru.vortex.modules.history.implementations;

import arc.util.Time;
import mindustry.gen.Player;
import mindustry.world.Block;
import org.ru.vortex.modules.history.ObjectHistory;

import java.util.concurrent.TimeUnit;

import static mindustry.game.EventType.BlockBuildEndEvent;

public class BlockHistory implements ObjectHistory
{
    private final Block block;
    private final int rotation;
    private final String target;
    private final boolean breaking;
    private final long lastAccessTime = Time.millis();

    public BlockHistory(BlockBuildEndEvent event)
    {
        this.block = event.tile.block();
        this.rotation = event.tile.build.rotation;
        this.target = event.unit.isPlayer() ? event.unit.getPlayer().name : event.unit.controller() instanceof Player ? event.unit.getPlayer().name : null;
        this.breaking = event.breaking;
    }

    @Override
    public String getMessage(Player player)
    {
        StringBuilder builder;

        if (breaking)
        {
            return null;
        }

        return null;
    }

    @Override
    public long getLastAccessTime(TimeUnit unit)
    {
        return unit.convert(Time.timeSinceMillis(lastAccessTime), TimeUnit.MILLISECONDS);
    }
}
