package org.ru.vortex.commands;

import org.ru.vortex.modules.database.Database;
import reactor.core.publisher.Mono;

import java.time.*;

import static arc.util.Log.err;
import static arc.util.Log.info;
import static java.lang.Long.parseLong;
import static mindustry.Vars.netServer;
import static mindustry.gen.Groups.player;
import static mindustry.net.Packets.KickReason;
import static org.ru.vortex.PluginVars.serverCommands;
import static org.ru.vortex.utils.Utils.temporaryBan;

public class ServerCommands
{

    public static void init()
    {
        serverCommands.register(
                "tempban",
                "<uuid> <days-of-ban> <reason...>",
                "Temporary ban player.",
                args ->
                {
                    var target = netServer.admins.getInfoOptional(args[0]);

                    if (target == null)
                    {
                        err("Player not found.");
                        return;
                    }

                    long days;
                    try
                    {
                        days = parseLong(args[1]);
                    }
                    catch (NumberFormatException ignored)
                    {
                        err("Ban days must be a number.");
                        return;
                    }

                    if (days <= 0)
                    {
                        err("Ban days must be a positive number.");
                        return;
                    }

                    player.each(p -> p.uuid().equals(target.id) || p.ip().equals(target.lastIP), p -> p.kick(KickReason.banned));

                    temporaryBan(target.id, target.lastIP, target.lastName, "console", args[2], days);
                }
        );

        serverCommands.register(
                "tempbans",
                "List all temporary banned players.",
                args ->
                {
                    info("Temporary banned players:");
                    Database
                            .getBanned()
                            .doOnNext(ban ->
                            {
                                var info = netServer.admins.getInfoOptional(ban.uuid);

                                var date = LocalDateTime.ofInstant(Instant.ofEpochMilli(ban.unbanDate), ZoneId.systemDefault()).toString();

                                if (info != null)
                                {
                                    info(
                                            "  '@' / Last known name: '@' / IP: '@' / Unban date: @ / Reason: '@'",
                                            ban.uuid,
                                            info.plainLastName(),
                                            ban.ip,
                                            date,
                                            ban.reason
                                    );
                                }
                                else
                                {
                                    info("  '@' / IP: '@' / Unban date: @ / Reason: '@'", ban.uuid, ban.ip, date, ban.reason);
                                }
                            })
                            .subscribe();
                }
        );

        serverCommands.register(
                "tempunban",
                "<uuid/ip>",
                "Unban a temporary banned player.",
                args ->
                {
                    Database
                            .unBan(args[0], "")
                            .flatMap(result ->
                            {
                                if (result.getDeletedCount() == 0)
                                {
                                    return Database.unBan("", args[0]);
                                }
                                return Mono.empty();
                            })
                            .subscribe();

                    info("Unbanned.");
                }
        );
    }
}
