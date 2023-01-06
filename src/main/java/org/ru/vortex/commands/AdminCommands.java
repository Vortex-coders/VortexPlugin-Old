package org.ru.vortex.commands;

import static arc.util.Strings.format;
import static arc.util.Strings.parseInt;
import static org.ru.vortex.PluginVars.clientCommands;
import static org.ru.vortex.modules.Bundler.getLocalized;
import static org.ru.vortex.modules.Bundler.sendLocalized;
import static org.ru.vortex.utils.Utils.temporaryBan;

import arc.util.CommandHandler;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Packets;

public class AdminCommands {

    public static void init() {
        register(
            "ban",
            (args, player) -> {
                var other = Groups.player.find(target -> target.id == parseInt(args[0]));

                if (other == null) {
                    sendLocalized(player, "player-not-found");
                    return;
                }

                other.kick(Packets.KickReason.banned);
                temporaryBan(other, args[2], parseInt(args[1]));
                sendLocalized(player, "commands.ban.player-banned", other.name);
            }
        );
    }

    private static void register(String name, CommandHandler.CommandRunner<Player> runner) {
        clientCommands.<Player>register(
            name,
            getLocalized(format("commands.@.parameters", name)),
            getLocalized(format("commands.@.description", name)),
            (args, player) -> {
                if (!player.admin) return;
                runner.accept(args, player);
            }
        );
    }
}
