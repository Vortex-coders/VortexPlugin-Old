package org.ru.vortex.commands;

import static arc.util.Strings.format;
import static arc.util.Strings.parseInt;
import static org.ru.vortex.PluginVars.clientCommands;
import static org.ru.vortex.modules.Bundler.getLocalized;
import static org.ru.vortex.modules.Bundler.sendLocalized;
import static org.ru.vortex.utils.Utils.temporaryBan;

import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Packets;
import org.ru.vortex.modules.Bundler;

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

                long days;
                try {
                    days = Long.parseLong(args[1]);
                } catch (NumberFormatException ignored) {
                    Bundler.sendLocalized(player, "commands.ban.days-number");
                    return;
                }

                if (days <= 0) {
                    Bundler.sendLocalized(player, "commands.ban.days-positive");
                    return;
                }

                other.kick(Packets.KickReason.banned);

                Groups.player.each(p -> p.ip().equals(other.ip()), p -> p.kick(Packets.KickReason.banned));

                temporaryBan(other, args[2], days);
                sendLocalized(player, "commands.ban.player-banned");
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
