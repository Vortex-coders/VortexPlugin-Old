package org.ru.vortex.commands;

import static arc.util.Strings.format;
import static mindustry.gen.Call.openURI;
import static org.ru.vortex.PluginVars.*;
import static org.ru.vortex.modules.Bundler.*;
import static org.ru.vortex.modules.GameOAuth.sendAdminRequest;
import static org.ru.vortex.modules.history.History.enabledHistory;
import static org.ru.vortex.utils.Checks.timeoutCheck;
import static org.ru.vortex.utils.Oauth.getAuthLink;
import static org.ru.vortex.utils.Oauth.isAuthorized;
import static org.ru.vortex.utils.Timeouts.timeout;

import arc.Events;
import arc.util.CommandHandler.CommandRunner;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;

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
                    sendLocalized(player, "commands.rtv.disabled");
                    return;
                }

                if (rtvVotes.contains(player.uuid())) {
                    sendLocalized(player, "commands.rtv.already-voted");
                    return;
                }

                rtvVotes.add(player.uuid());

                int cur = rtvVotes.size();
                int req = (int) Math.ceil(rtvRatio * Groups.player.size());

                sendLocalizedAll("commands.rtv.change-map", player.name, cur, req);

                if (cur < req) return;
                rtvVotes.clear();

                sendLocalizedAll("commands.rtv.vote-passed");
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
                if (player.admin) {
                    sendLocalized(player, "already-admin");
                    return;
                }

                sendAdminRequest(player);
                sendLocalized(player, "commands.login.wait");
            }
        );

        register(
            "register",
            (args, player) -> {
                if (isAuthorized(player)) return;

                openURI(player.con, getAuthLink(player));
            }
        );
    }

    private static void register(String name, CommandRunner<Player> runner) {
        clientCommands.<Player>register(
            name,
            getLocalized(format("commands.@.parameters", name)),
            getLocalized(format("commands.@.description", name)),
            (args, player) -> {
                if (timeoutCheck(player, name)) return;
                runner.accept(args, player);
                timeout(player, name);
            }
        );
    }
}
