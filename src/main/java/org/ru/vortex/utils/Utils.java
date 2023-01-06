package org.ru.vortex.utils;

import static org.ru.vortex.PluginVars.config;

import arc.util.Time;
import mindustry.Vars;
import mindustry.gen.Player;
import org.ru.vortex.modules.database.Database;
import org.ru.vortex.modules.database.models.BanData;

import java.util.concurrent.TimeUnit;

public class Utils {

    public static void temporaryBan(Player player, String reason, long days) {
        temporaryBan(player.uuid(), player.ip(), reason, days);
    }

    public static void temporaryBan(String uuid, String ip, String reason, long days) {
        Vars.netServer.admins.banPlayer(uuid);
        Vars.netServer.admins.banPlayerIP(ip);

        Database
            .setBan(
                new BanData(data -> {
                    data.server = config.gamemode.name();
                    data.uuid = uuid;
                    data.ip = ip;
                    data.reason = reason;
                    data.unbanDate = Time.millis() + TimeUnit.DAYS.toMillis(days);
                })
            )
            .subscribe();
    }
}
