package org.ru.vortex.modules;

import arc.util.Strings;
import mindustry.gen.Groups;
import mindustry.gen.Player;

import java.util.*;

public class Bundler
{

    private static final Locale defaultLocale = new Locale("en");

    public static void sendLocalized(Player player, String key)
    {
        player.sendMessage(getLocalized(player, key));
    }

    public static void sendLocalized(Player player, String key, Object... objects)
    {
        player.sendMessage(getLocalized(player, key, objects));
    }

    public static void sendLocalizedAll(String key)
    {
        Groups.player.each(player -> sendLocalized(player, key));
    }

    public static void sendLocalizedAll(String key, Object... objects)
    {
        Groups.player.each(player -> sendLocalized(player, key, objects));
    }

    public static String getLocalized(Player player, String key, Object... objects)
    {
        return getLocalized(new Locale(player.locale), key, objects);
    }

    public static String getLocalized(Locale locale, String key, Object... objects)
    {
        return Strings.format(getLocalized(locale, key), objects);
    }

    public static String getLocalized(String key)
    {
        return getLocalized(defaultLocale, key);
    }

    public static String getLocalized(Locale locale, String key)
    {
        try
        {
            return ResourceBundle.getBundle("bundle", locale).getString(key);
        }
        catch (MissingResourceException ignored)
        {
            return ResourceBundle.getBundle("bundle", defaultLocale).getString(key);
        }
    }
}
