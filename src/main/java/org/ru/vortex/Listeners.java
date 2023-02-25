package org.ru.vortex;

import arc.Events;
import arc.util.Log;
import arc.util.Time;
import org.ru.vortex.modules.Translator;
import org.ru.vortex.modules.database.models.PlayerData;

import java.time.Duration;

import static arc.util.Log.info;
import static mindustry.Vars.netServer;
import static mindustry.game.EventType.*;
import static org.ru.vortex.PluginVars.cachedPlayerData;
import static org.ru.vortex.modules.Bundler.sendLocalized;
import static org.ru.vortex.modules.Bundler.sendLocalizedAll;
import static org.ru.vortex.modules.Webhook.sendFrom;
import static org.ru.vortex.modules.Webhook.sendInfo;
import static org.ru.vortex.modules.database.Database.*;
import static org.ru.vortex.utils.Utils.kickLocalized;

public class Listeners
{

    public static void init()
    {
        Events.on(ServerLoadEvent.class, event -> sendInfo("Server launched"));

        Events.on(
                PlayerJoin.class,
                event ->
                {
                    getPlayerData(event.player).subscribe(data -> cachedPlayerData.put(event.player.uuid(), data));
                    info("@ joined", event.player.name);
                    sendLocalizedAll("events.player-joined", event.player.name);
                    sendLocalized(event.player, "events.welcome", event.player.name, PluginVars.serverLink);
                    sendInfo("@ joined", event.player.plainName());
                }
        );

        Events.on(
                PlayerLeave.class,
                event ->
                {
                    setPlayerData(cachedPlayerData.remove(event.player.uuid())).subscribe();

                    info("@ left", event.player.name);
                    sendLocalizedAll("events.player-left", event.player.plainName());
                    sendInfo("@ left", event.player.plainName());
                }
        );

        Events.on(
                GameOverEvent.class,
                event ->
                {
                    cachedPlayerData.each((uuid, data) -> cachedPlayerData.put(uuid, data.setGamesPlayed(data.gamesPlayed++)));

                    sendInfo("Game over!", event.winner.name);
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

        Events.on(
                BlockBuildEndEvent.class,
                event ->
                {
                    if (!event.unit.isPlayer()) return;

                    var player = event.unit.getPlayer();

                    PlayerData data = cachedPlayerData.get(player.uuid());

                    if (event.breaking)
                    {
                        data.blocksBroken++;
                    }
                    else
                    {
                        data.blocksBuilt++;
                    }

                    cachedPlayerData.put(player.uuid(), data);
                }
        );

        netServer.admins.addChatFilter((author, text) ->
        {
            Log.info("&fi@: @", "&lc" + author.plainName(), "&lw" + text);

            author.sendMessage(netServer.chatFormatter.format(author, text), author, text);
            Translator.translate(author, text);

            sendFrom(author, text);
            return null;
        });
    }
}
