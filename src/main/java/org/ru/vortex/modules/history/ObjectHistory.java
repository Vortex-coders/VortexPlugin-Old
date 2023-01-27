package org.ru.vortex.modules.history;

import mindustry.gen.Player;

import java.util.concurrent.TimeUnit;

public interface ObjectHistory
{
    String getMessage(Player player);

    long getLastAccessTime(TimeUnit unit);
}
