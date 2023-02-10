package org.ru.vortex;

import arc.ApplicationListener;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import mindustry.core.Version;
import mindustry.mod.Plugin;
import org.ru.vortex.commands.AdminCommands;
import org.ru.vortex.commands.ClientCommands;
import org.ru.vortex.commands.ServerCommands;
import org.ru.vortex.modules.Config;
import org.ru.vortex.modules.Webhook;
import org.ru.vortex.modules.database.Database;

import static arc.Core.app;
import static mindustry.Vars.netServer;
import static mindustry.net.Packets.KickReason.serverRestarting;
import static org.ru.vortex.PluginVars.clientCommands;
import static org.ru.vortex.PluginVars.serverCommands;

@SuppressWarnings("unused")
public class Vortex extends Plugin
{

    public Vortex()
    {
        Log.infoTag("Vortex", "Loading");

        app.addListener(
                new ApplicationListener()
                {
                    @Override
                    public void dispose()
                    {
                        Log.infoTag("Shutdown", "The server will now be shut down!");

                        netServer.kickAll(serverRestarting);
                        app.post(Webhook::disconnect);
                    }
                }
        );
    }

    @Override
    public void init()
    {
        Log.infoTag("Vortex", "Starting");
        Time.mark();

        Config.init();
        Database.init();
        Listeners.init();
        Webhook.init();

        Version.build = -1;

        Log.infoTag("Vortex", Strings.format("Loaded in @ ms", Time.elapsed()));
    }

    @Override
    public void registerClientCommands(CommandHandler handler)
    {
        clientCommands = handler;

        ClientCommands.init();
        AdminCommands.init();
    }

    @Override
    public void registerServerCommands(CommandHandler handler)
    {
        serverCommands = handler;

        ServerCommands.init();
    }
}
