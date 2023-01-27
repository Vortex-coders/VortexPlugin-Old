package org.ru.vortex.modules.history.implementations;

import mindustry.gen.Player;
import mindustry.world.Block;
import org.ru.vortex.modules.Bundler;
import org.ru.vortex.modules.history.ObjectHistory;

import static mindustry.game.EventType.BlockBuildEndEvent;

public class BlockHistory implements ObjectHistory
{
    private final Block block;
    private final Player target;
    private final boolean breaking;

    public BlockHistory(BlockBuildEndEvent event)
    {
        this.block = event.tile.block();
        this.target = event.unit.getPlayer();
        this.breaking = event.breaking;
    }

    @Override
    public String getMessage(Player player)
    {
        if (player.admin)
        {
            if (breaking)
                return Bundler.getLocalized(player, "commands.history.destroyed.for-admin", block.name, target.name, target.uuid());
            else
                return Bundler.getLocalized(player, "commands.history.placed.for-admin", block.name, target.name, target.uuid());
        }
        else
        {
            if (breaking)
                return Bundler.getLocalized(player, "commands.history.destroyed", block.name, target.name, target.id());
            else
                return Bundler.getLocalized(player, "commands.history.placed", block.name, target.name, target.id());
        }
    }
}
