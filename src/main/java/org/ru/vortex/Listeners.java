package org.ru.vortex;

import static arc.Core.app;
import static arc.util.Log.info;
import static mindustry.Vars.netServer;
import static mindustry.game.EventType.*;
import static org.ru.vortex.PluginVars.brokenBlocksCache;
import static org.ru.vortex.PluginVars.placedBlocksCache;
import static org.ru.vortex.modules.Bundler.sendLocalized;
import static org.ru.vortex.modules.Bundler.sendLocalizedAll;
import static org.ru.vortex.modules.database.Database.*;
import static org.ru.vortex.modules.discord.Bot.*;

import arc.Events;
import arc.math.geom.Vec2;
import arc.util.Time;
import java.util.ArrayList;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import org.ru.vortex.modules.discord.Bot;
import org.ru.vortex.modules.history.History;
import org.ru.vortex.modules.history.components.BlockChangeType;
import org.ru.vortex.modules.history.components.FormattedEntry;
import org.ru.vortex.utils.Pipe;
import reactor.core.scheduler.Schedulers;

public class Listeners {

    public static void init() {
        Events.on(WorldLoadEvent.class, event -> History.initHistory());
        Events.on(ServerLoadEvent.class, event -> sendEmbed(botChannel, "Server launched"));

        Events.on(
            PlayerJoin.class,
            event -> {
                sendLocalizedAll("events.player-joined", event.player.plainName());
                sendLocalized(event.player, "events.welcome", event.player.coloredName(), PluginVars.serverLink);
                sendEmbed(botChannel, "@ joined", event.player.plainName());
                app.post(Bot::updateStatus);
            }
        );

        Events.on(
            PlayerLeave.class,
            event -> {
                getPlayerData(event.player)
                    .subscribe(data -> {
                        data.blocksBuilt += placedBlocksCache.get(event.player.id);
                        data.blocksBroken += brokenBlocksCache.get(event.player.id);
                        brokenBlocksCache.remove(event.player.id);
                        placedBlocksCache.remove(event.player.id);
                        setPlayerData(data).block();
                    });

                sendEmbed(botChannel, "@ left", event.player.plainName());
                sendLocalizedAll("events.player-left", event.player.plainName());

                app.post(Bot::updateStatus);
            }
        );

        Events.on(
            GameOverEvent.class,
            event -> {
                getPlayersData(Groups.player)
                    .publishOn(Schedulers.boundedElastic())
                    .doOnNext(data -> {
                        data.gamesPlayed += 1;
                        setPlayerData(data).subscribe();
                    })
                    .subscribe();

                sendEmbed(botChannel, "Game over. @ team won!", event.winner.name);
                app.post(Bot::updateStatus);
            }
        );

        Events.on(
            ConnectPacketEvent.class,
            event -> {
                var con = event.connection;

                getBan(con.uuid, con.address)
                    .subscribe(data -> {
                        if (data.uuid == null) return;
                        if (data.unbanDate - Time.millis() <= 0) {
                            netServer.admins.unbanPlayerID(con.uuid);
                            netServer.admins.unbanPlayerIP(con.address);
                            unBan(data).subscribe();
                        }
                    });
            }
        );

        Events.on(
            TapEvent.class,
            event -> {
                if (!History.enabledHistory.contains(event.player)) return;

                Player player = event.player;

                byte additionalDataSize = History.additionalDataSize();
                int xShift = additionalDataSize + History.yBites;
                long xBiteValueMask = ((long) Math.pow(2, History.xBites) - 1) << xShift;
                long yBiteValueMask = ((long) Math.pow(2, History.yBites) - 1) << (int) additionalDataSize;

                ArrayList<FormattedEntry> tileHistory = new ArrayList<>();

                for (int i = 0, length = History.history.size(); i < length; i++) {
                    long packaged = History.history.get(i);

                    if (
                        (packaged & xBiteValueMask) >> xShift == event.tile.x &&
                        (packaged & yBiteValueMask) >> (int) additionalDataSize == event.tile.y
                    ) tileHistory.add(History.decompressHistory(packaged));
                }

                tileHistory.forEach(tileEntry -> {
                    switch (tileEntry.changeType()) {
                        case Built -> sendLocalized(player, "history.block.built", tileEntry.block().localizedName, player.name);
                        case Destroyed -> sendLocalized(player, "history.block.destroyed", tileEntry.block().localizedName, player.name);
                        case PayloadDrop -> sendLocalized(
                            player,
                            "history.block.payload-drop",
                            tileEntry.block().localizedName,
                            player.name
                        );
                        case Pickup -> sendLocalized(player, "history.block.pickup", tileEntry.block().localizedName, player.name);
                    }
                });
            }
        );

        Events.on(
            PickupEvent.class,
            event -> {
                if (event.build == null || event.carrier.getPlayer() == null || event.carrier.getPlayer().getInfo() == null) return;

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
            PayloadDropEvent.class,
            event -> {
                if (event.build == null || event.carrier.getPlayer() == null || event.carrier.getPlayer().getInfo() == null) return;

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

        netServer.admins.addChatFilter((author, text) -> {
            info("&fi@: @", "&lc" + author.plainName(), "&lw" + text);
            author.sendMessage(netServer.chatFormatter.format(author, text), author, text);
            sendMessage(botChannel, "[@]: @", author.plainName(), text);
            return null;
        });

        netServer.admins.addActionFilter(action -> {
            FormattedEntry historyEntry = null;
            var playerInfo = action.player.getInfo();
            var position = new Vec2(action.tile.x, action.tile.y);

            switch (action.type) {
                case breakBlock -> {
                    brokenBlocksCache.increment(action.player.id);
                    historyEntry = BlockChangeType.Destroyed.formatEntry(playerInfo, action.block, position);
                }
                case placeBlock -> {
                    placedBlocksCache.increment(action.player.id);
                    historyEntry = BlockChangeType.Built.formatEntry(playerInfo, action.block, position);
                }
                default -> {}
            }

            if (historyEntry != null) Pipe.apply(historyEntry).pipe(History::compressHistory).pipe(History.history::add);
            return true;
        });
    }
}
