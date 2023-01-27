package org.ru.vortex.modules.history.entry;

import mindustry.gen.Player;
import mindustry.world.Block;

import static mindustry.game.EventType.BlockBuildEndEvent;

public class BlockEntry implements HistoryEntry
{

    public Player player;
    public Block block;
    public boolean breaking;

    public BlockEntry(BlockBuildEndEvent event)
    {
        this.player = event.unit.getPlayer();
        this.block = event.tile.block();
        this.breaking = event.breaking;
    }

    @Override
    public String getMessage(boolean isAdmin)
    {
        String id;

        if (isAdmin) id = "[white] (UUID: [scarlet]" + player.uuid() + "[white])";
        else id =
                "[white] (ID: [scarlet]" + player.id + "[white])";

        if (breaking) return "[red]- [white]" + player.name + id + " broke this tile";
        else return (
                "[green]+ [white]" + player.name + id + " placed [purple]" + block + "[white]"
        );
    }
}
