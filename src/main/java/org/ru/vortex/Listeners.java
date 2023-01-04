package org.ru.vortex;

import arc.Events;
import arc.math.geom.Vec2;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import org.ru.vortex.modules.Bundler;
import org.ru.vortex.modules.database.Database;
import org.ru.vortex.modules.discord.Bot;
import org.ru.vortex.modules.history.History;
import org.ru.vortex.modules.history.components.BlockChangeType;
import org.ru.vortex.modules.history.components.FormattedEntry;
import org.ru.vortex.utils.Pipe;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;

public class Listeners {

    public static void init() {
        Events.on(EventType.WorldLoadEvent.class, event -> History.initHistory());
        Events.on(
                EventType.PlayerLeave.class,
                event -> {
                    Bot.updateStatus();

                    Database
                            .getPlayerData(event.player)
                            .subscribe(data -> {
                                data.blocksBuilt +=
                                        PluginVars.placedBlocksCache.get(event.player.id);
                                data.blocksBroken +=
                                        PluginVars.brokenBlocksCache.get(event.player.id);
                                PluginVars.brokenBlocksCache.remove(event.player.id);
                                PluginVars.placedBlocksCache.remove(event.player.id);
                                Database.setPlayerData(data).block();
                            });
                }
        );
        Events.on(
                EventType.GameOverEvent.class,
                event -> {
                    Bot.updateStatus();

                    Database
                            .getPlayersData(Groups.player)
                            .publishOn(Schedulers.boundedElastic())
                            .doOnNext(data -> {
                                data.gamesPlayed += 1;
                                Database.setPlayerData(data).subscribe();
                            })
                            .subscribe();
                }
        );

        Events.on(
                EventType.TapEvent.class,
                event -> {
                    if (!History.enabledHistory.contains(event.player)) return;

                    Player player = event.player;

                    byte additionalDataSize = History.additionalDataSize();
                    int xShift = additionalDataSize + History.yBites;
                    long xBiteValueMask =
                            ((long) Math.pow(2, History.xBites) - 1) << xShift;
                    long yBiteValueMask =
                            ((long) Math.pow(2, History.yBites) - 1) << (int) additionalDataSize;

                    ArrayList<FormattedEntry> tileHistory = new ArrayList<>();

                    for (int i = 0, length = History.history.size(); i < length; i++) {
                        long packaged = History.history.get(i);

                        if (
                                (packaged & xBiteValueMask) >> xShift == event.tile.x &&
                                        (packaged & yBiteValueMask) >> (int) additionalDataSize ==
                                                event.tile.y
                        ) tileHistory.add(History.decompressHistory(packaged));
                    }

                    tileHistory.forEach(tileEntry -> {
                        switch (tileEntry.changeType()) {
                            case Built -> Bundler.sendLocalized(
                                    player,
                                    "history.block.built",
                                    tileEntry.block().localizedName,
                                    player.name
                            );
                            case Destroyed -> Bundler.sendLocalized(
                                    player,
                                    "history.block.destroyed",
                                    tileEntry.block().localizedName,
                                    player.name
                            );
                            case PayloadDrop -> Bundler.sendLocalized(
                                    player,
                                    "history.block.payloaddrop",
                                    tileEntry.block().localizedName,
                                    player.name
                            );
                            case Pickup -> Bundler.sendLocalized(
                                    player,
                                    "history.block.pickup",
                                    tileEntry.block().localizedName,
                                    player.name
                            );
                        }
                    });
                }
        );

        Events.on(
                EventType.PickupEvent.class,
                event -> {
                    if (
                            event.build == null ||
                                    event.carrier.getPlayer() == null ||
                                    event.carrier.getPlayer().getInfo() == null
                    ) return;

                    Pipe
                            .apply(
                                    BlockChangeType.Pickup.formatEntry(
                                            event.carrier.getPlayer().getInfo(),
                                            event.build.block,
                                            new Vec2(event.build.tileX(), event.build.tileY())
                                    )
                            )
                            .pipe(History::compressHistory)
                            .pipe(History.history::add);
                }
        );

        Events.on(
                EventType.PayloadDropEvent.class,
                event -> {
                    if (
                            event.build == null ||
                                    event.carrier.getPlayer() == null ||
                                    event.carrier.getPlayer().getInfo() == null
                    ) return;

                    Pipe
                            .apply(
                                    BlockChangeType.PayloadDrop.formatEntry(
                                            event.carrier.getPlayer().getInfo(),
                                            event.build.block,
                                            new Vec2(event.build.tileX(), event.build.tileY())
                                    )
                            )
                            .pipe(History::compressHistory)
                            .pipe(History.history::add);
                }
        );

        Vars.netServer.admins.addActionFilter(action -> {
            FormattedEntry historyEntry = null;
            var playerInfo = action.player.getInfo();
            var position = new Vec2(action.tile.x, action.tile.y);

            switch (action.type) {
                case breakBlock -> {
                    PluginVars.brokenBlocksCache.increment(action.player.id);
                    historyEntry =
                            BlockChangeType.Destroyed.formatEntry(
                                    playerInfo,
                                    action.block,
                                    position
                            );
                }
                case placeBlock -> {
                    PluginVars.placedBlocksCache.increment(action.player.id);
                    historyEntry =
                            BlockChangeType.Built.formatEntry(
                                    playerInfo,
                                    action.block,
                                    position
                            );
                }
                default -> {
                }
            }

            if (historyEntry != null) Pipe
                    .apply(historyEntry)
                    .pipe(History::compressHistory)
                    .pipe(History.history::add);
            return true;
        });
    }
}
