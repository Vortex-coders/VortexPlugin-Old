package org.ru.vortex.utils;

import arc.struct.ObjectMap;
import arc.util.Time;
import mindustry.gen.Player;

public class Timeouts {

    private static final Long defaultTime = 90000L;
    private static final ObjectMap<String, ObjectMap<String, Long>> timeouts = new ObjectMap<>();

    public static boolean hasTimeout(Player player, String command) {
        var map = timeouts.get(player.uuid());
        return map != null && map.containsKey(command);
    }

    public static boolean isRunnable(Player player, String command) {
        if (player.admin || !timeouts.containsKey(player.uuid()) || !timeouts.get(player.uuid()).containsKey(command)) return true;
        return timeouts.get(player.uuid()).get(command) <= Time.millis();
    }

    public static void timeout(Player player, String command) {
        if (player.admin) return;
        timeouts.get(player.uuid(), ObjectMap::new).put(command, Time.millis() + defaultTime);
    }
}
