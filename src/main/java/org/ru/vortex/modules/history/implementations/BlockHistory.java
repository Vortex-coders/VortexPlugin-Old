package org.ru.vortex.modules.history.implementations;

import arc.util.Time;
import mindustry.gen.Player;
import mindustry.world.blocks.ConstructBlock;
import org.ru.vortex.modules.Bundler;
import org.ru.vortex.modules.history.ObjectHistory;

import static mindustry.Vars.content;
import static mindustry.game.EventType.BlockBuildEndEvent;

public class BlockHistory implements ObjectHistory
{
    public final String name;
    public final short blockID;
    public final byte rotation;
    public final boolean breaking;
    public final long time;

    public BlockHistory(BlockBuildEndEvent event)
    {
        this.name = event.unit.getPlayer().coloredName();
        this.blockID = event.tile.build instanceof ConstructBlock.ConstructBuild build ? build.current.id : event.tile.blockID();
        this.rotation = (byte) event.tile.build.rotation;
        this.breaking = event.breaking;
        this.time = Time.millis();
    }

    @Override
    public String getMessage(Player player)
    {
        var block = content.block(blockID);
        return Bundler.getLocalized(player, breaking ? "history.deconstruct" : block.rotate ? "history.construct.rotate" : "history.construct", name /* formatShortDate(time), sides[rotation] */);
    }
}
