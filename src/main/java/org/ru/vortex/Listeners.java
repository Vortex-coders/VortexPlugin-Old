package org.ru.vortex;

import arc.Events;
import arc.util.Time;
import mindustry.gen.Groups;
import org.ru.vortex.modules.discord.Bot;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

import static arc.Core.app;
import static arc.util.Log.info;
import static mindustry.Vars.netServer;
import static mindustry.Vars.player;
import static mindustry.game.EventType.*;
import static org.ru.vortex.PluginVars.brokenBlocksCache;
import static org.ru.vortex.PluginVars.placedBlocksCache;
import static org.ru.vortex.modules.Bundler.sendLocalized;
import static org.ru.vortex.modules.Bundler.sendLocalizedAll;
import static org.ru.vortex.modules.database.Database.*;
import static org.ru.vortex.modules.discord.Bot.*;
import static org.ru.vortex.utils.Utils.kickLocalized;

public class Listeners
{

    public static void init()
    {
        Events.on(ServerLoadEvent.class, event -> sendEmbed(botChannel, "Server launched"));

        Events.on(
                PlayerJoin.class,
                event ->
                {
                    info("@ joined", player.name);
                    sendLocalizedAll("events.player-joined", event.player.name);
                    sendLocalized(event.player, "events.welcome", event.player.name, PluginVars.serverLink);
                    sendEmbed(botChannel, "@ joined", event.player.plainName());
                    app.post(Bot::updateStatus);
                }
        );

        Events.on(
                PlayerLeave.class,
                event ->
                {
                    getPlayerData(event.player)
                            .subscribe(data ->
                            {
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
                event ->
                {
                    getPlayersData(Groups.player)
                            .publishOn(Schedulers.boundedElastic())
                            .doOnNext(data ->
                            {
                                data.gamesPlayed += 1;
                                setPlayerData(data).subscribe();
                            })
                            .subscribe();

                    sendEmbed(botChannel, "Game over!", event.winner.name);
                    app.post(Bot::updateStatus);
                }
        );

        Events.on(
                ConnectPacketEvent.class,
                event ->
                {
                    var con = event.connection;

                    getBan(con.uuid, con.address)
                            .subscribe(data ->
                            {
                                if (data.uuid == null) return;
                                if (Time.millis() > data.unbanDate)
                                {
                                    unBan(data).subscribe();
                                }
                                else
                                {
                                    Duration remain = Duration.ofMillis(data.unbanDate - Time.millis());

                                    kickLocalized(
                                            con,
                                            event.packet.locale,
                                            "kick.temporary-ban",
                                            data.reason,
                                            remain.toDays(),
                                            remain.toHoursPart(),
                                            remain.toMinutesPart()
                                    );
                                }
                            });
                }
        );

        netServer.admins.addChatFilter((author, text) ->
        {
            info("&fi@: @", "&lc" + author.plainName(), "&lw" + text);
            author.sendMessage(netServer.chatFormatter.format(author, text), author, text);
            sendMessage(botChannel, "**`@:`** @", author.plainName(), text);
            return null;
        });
    }
}
