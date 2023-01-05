package org.ru.vortex;

import static mindustry.Vars.netServer;
import static mindustry.net.Packets.KickReason.serverRestarting;
import static org.ru.vortex.PluginVars.clientCommands;
import static org.ru.vortex.PluginVars.serverCommands;

import arc.ApplicationListener;
import arc.Core;
import arc.util.*;
import mindustry.core.Version;
import mindustry.mod.Plugin;
import org.ru.vortex.commands.ClientCommands;
import org.ru.vortex.modules.Config;
import org.ru.vortex.modules.database.Database;
import org.ru.vortex.modules.discord.Bot;
import org.ru.vortex.modules.history.History;

@SuppressWarnings("unused")
public class Vortex extends Plugin {

    public Vortex() {
        Log.infoTag("Vortex", "Loading");

        Core.app.addListener(
            new ApplicationListener() {
                @Override
                public void dispose() {
                    Bot.disconnect();
                    netServer.kickAll(serverRestarting);
                    Log.infoTag("Shutdown", "The server will now be shut down!");
                }
            }
        );
    }

    @Override
    public void init() {
        Log.infoTag("Vortex", "Starting");
        Time.mark();

        Config.init();
        History.init();
        Listeners.init();
        Database.connect();
        Bot.init();

        Version.build = -1;

        Log.infoTag("Vortex", Strings.format("Loaded in @", Time.elapsed()));
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        clientCommands = handler;
        ClientCommands.init();
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        serverCommands = handler;
    }
}
