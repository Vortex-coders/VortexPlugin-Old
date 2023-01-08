package org.ru.vortex.utils;

import static org.ru.vortex.PluginVars.config;
import static org.ru.vortex.modules.Bundler.getLocalized;

import arc.util.Strings;
import arc.util.Time;
import mindustry.gen.Player;
import mindustry.net.NetConnection;
import org.ru.vortex.modules.database.Database;
import org.ru.vortex.modules.database.models.BanData;
import org.ru.vortex.modules.discord.Bot;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static void temporaryBan(Player player, String adminName, String reason, long days) {
        temporaryBan(player.uuid(), player.ip(), player.plainName(), adminName, reason, days);
    }

    public static void temporaryBan(String uuid, String ip, String name, String adminName, String reason, long days) {
        BanData ban = new BanData(data -> {
            data.server = config.gamemode.name();
            data.uuid = uuid;
            data.ip = ip;
            data.name = name;
            data.adminName = adminName;
            data.reason = reason;
            data.unbanDate = Time.millis() + TimeUnit.DAYS.toMillis(days);
        });

        Database.setBan(ban).subscribe();
        Bot.sendBanMessage(ban);
    }

    public static void kickLocalized(Player player, String key, Object... objects) {
        kickLocalized(player.con, player.locale, key, objects);
    }
    public static void kickLocalized(NetConnection con, String locale, String key, Object... objects) {
        con.kick(Strings.format(getLocalized(new Locale(locale), key), objects));
    }
}
