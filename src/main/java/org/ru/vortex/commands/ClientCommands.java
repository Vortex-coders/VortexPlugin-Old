package org.ru.vortex.commands;

import static arc.util.Strings.parseInt;
import static mindustry.gen.Call.openURI;
import static org.ru.vortex.PluginVars.*;
import static org.ru.vortex.modules.history.History.enabledHistory;

import arc.Events;
import arc.util.CommandHandler.CommandRunner;
import arc.util.Strings;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Packets;
import org.ru.vortex.modules.Bundler;
import org.ru.vortex.utils.Timeouts;
import org.ru.vortex.utils.Utils;

public class ClientCommands {

    public static void init() {
        register("discord", (args, player) -> openURI(player.con, serverLink));

        register(
            "rtv",
            (args, player) -> {
                if (player.admin()) {
                    rtvEnabled = args.length != 1 || !args[0].equals("off");
                }

                if (!rtvEnabled) {
                    Bundler.sendLocalized(player, "commands.rtv.disabled");
                    return;
                }

                if (rtvVotes.contains(player.uuid())) {
                    Bundler.sendLocalized(player, "commands.rtv.already-voted");
                    return;
                }

                rtvVotes.add(player.uuid());

                int cur = rtvVotes.size();
                int req = (int) Math.ceil(rtvRatio * Groups.player.size());

                Bundler.sendLocalizedAll("commands.rtv.change-map", player.name, cur, req);

                if (cur < req) return;
                rtvVotes.clear();

                Bundler.sendLocalizedAll("commands.rtv.vote-passed");
                Events.fire(new EventType.GameOverEvent(Team.crux));
            }
        );

        register(
            "history",
            (args, player) -> {
                if (enabledHistory.contains(player)) {
                    enabledHistory.remove(player);
                    return;
                }

                enabledHistory.add(player);
            }
        );

        register(
            "login",
            (args, player) -> {
                if (player.admin) return;

                Bundler.sendLocalized(player, "commands.login.wait");
            }
        );

        register(
            "ban",
            (args, player) -> {
                var other = Groups.player.find(p -> p.id == parseInt(args[0]));

                if (other == null) {
                    Bundler.sendLocalized(player, "player-not-found");
                    return;
                }

                other.kick(Packets.KickReason.banned);

                Utils.temporaryBan(other, args[2], parseInt(args[1]));

                Bundler.sendLocalized(player, "commands.ban.player-banned", other.name);
            }
        );
    }

    private static void register(String name, CommandRunner<Player> runner) {
        clientCommands.<Player>register(
            name,
            Bundler.getLocalized(Strings.format("commands.@.parameters", name)),
            Bundler.getLocalized(Strings.format("commands.@.description", name)),
            (args, player) -> {
                if (Timeouts.hasTimeout(player, name)) return;
                runner.accept(args, player);
                Timeouts.timeout(player, name);
            }
        );
    }
}
