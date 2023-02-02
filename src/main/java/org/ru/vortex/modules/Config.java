package org.ru.vortex.modules;

import arc.files.Fi;
import arc.util.Log;
import arc.util.Strings;

import static arc.Core.app;
import static mindustry.Vars.dataDirectory;
import static mindustry.net.Administration.Config.*;
import static org.ru.vortex.PluginVars.config;
import static org.ru.vortex.PluginVars.gson;
import static org.ru.vortex.modules.Config.Gamemode.hub;
import static org.ru.vortex.modules.Config.Gamemode.survival;

public class Config
{

    public final String token = "";
    public final String prefix = "";
    public final String mongoUrl = "";
    public final String name = "";
    public final String description = "";
    public final String channelId = "";
    public final String adminRoleId = "";
    public final String adminChannelId = "";
    public final String bansChannelId = "";
    public final Gamemode gamemode = survival;

    public static void init()
    {
        Fi file = dataDirectory.child("config.json");

        if (file.exists())
        {
            config = gson.fromJson(file.reader(), Config.class);
        }
        else
        {
            file.writeString(gson.toJson(config = new Config()));
            Log.infoTag("Config", Strings.format("Config generated in @", file.absolutePath()));
            app.exit();
        }

        autoPause.set(config.gamemode.isDefault());
        enableVotekick.set(config.gamemode != hub);

        serverName.set(config.name);
        desc.set(config.description);

        motd.set("off");
        showConnectMessages.set(false);
        logging.set(true);
        strict.set(true);
        antiSpam.set(true);

        interactRateWindow.set(1);
        interactRateLimit.set(15);
        messageRateLimit.set(1);
        packetSpamLimit.set(250);

        interactRateKick.set(15);
        messageSpamKick.set(5);

        snapshotInterval.set(250);
    }

    public enum Gamemode
    {
        attack,
        hub,
        pvp,
        sandbox,
        survival;

        public boolean isDefault()
        {
            return (this == attack || this == pvp || this == sandbox || this == survival);
        }
    }
}
