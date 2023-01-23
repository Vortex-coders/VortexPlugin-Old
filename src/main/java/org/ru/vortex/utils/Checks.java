package org.ru.vortex.utils;

import static org.ru.vortex.modules.Bundler.sendLocalized;
import static org.ru.vortex.utils.Timeouts.*;

import mindustry.gen.Player;

public class Checks {

    public static boolean timeoutCheck(Player player, String name) {
        if (hasTimeout(player, name)) {
            sendLocalized(player, "has-timeout");
            return false;
        } else return true;
    }

    public static boolean adminCheck(Player player) {
        if (!player.admin) {
            sendLocalized(player, "not-admin");
            return false;
        } else return true;
    }
}
