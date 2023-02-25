package org.ru.vortex.modules;

import arc.func.Cons;
import arc.struct.StringMap;
import arc.util.*;
import arc.util.serialization.JsonReader;
import mindustry.gen.Groups;
import mindustry.gen.Player;

import static mindustry.Vars.netServer;
import static org.ru.vortex.PluginVars.cachedPlayerData;
import static org.ru.vortex.PluginVars.translatorLanguages;

public class Translator
{
    public static void init()
    {
        translatorLanguages.putAll(
                "ca", "Català",
                "id", "Indonesian",
                "da", "Dansk",
                "de", "Deutsch",
                "et", "Eesti",
                "en", "English",
                "es", "Español",
                "eu", "Euskara",
                "fil", "Filipino",
                "fr", "Français",
                "it", "Italiano",
                "lt", "Lietuvių",
                "hu", "Magyar",
                "nl", "Nederlands",
                "pl", "Polski",
                "pt", "Português",
                "ro", "Română",
                "fi", "Suomi",
                "sv", "Svenska",
                "vi", "Tiếng Việt",
                "tk", "Türkmen dili",
                "tr", "Türkçe",
                "cs", "Čeština",
                "be", "Беларуская",
                "bg", "Български",
                "ru", "Русский",
                "sr", "Српски",
                "uk_UA", "Українська",
                "th", "ไทย",
                "zh", "简体中文",
                "ja", "日本語",
                "ko", "한국어"
        );
    }

    public static void translate(String text, String from, String to, Cons<String> result, Cons<Throwable> error)
    {
        Http
                .post("https://clients5.google.com/translate_a/t?client=dict-chrome-ex&dt=t", "tl=" + to + "&sl=" + from + "&q=" + Strings.encode(text))
                .error(error::get)
                .submit(response -> result.get(new JsonReader().parse(response.getResultAsString()).get(0).get(0).asString()));
    }

    public static void translate(Player author, String text)
    {
        var cache = new StringMap();
        var message = netServer.chatFormatter.format(author, text);

        cachedPlayerData.each((uuid, data) ->
        {
            var player = Groups.player.find(p -> p.uuid().equals(data.uuid));

            if (player == author) return;

            if (data.translatorLanguage.equals("off"))
            {
                player.sendMessage(message, author, text);
                return;
            }

            if (cache.containsKey(data.translatorLanguage))
            {
                player.sendMessage(cache.get(data.translatorLanguage), author, text);
            }
            else translate(
                    text,
                    "auto",
                    data.translatorLanguage,
                    result ->
                    {
                        cache.put(data.translatorLanguage, message + " [white]([lightgray]" + result + "[])");
                        player.sendMessage(cache.get(data.translatorLanguage), author, text);
                    },
                    (throwable) ->
                    {
                        player.sendMessage(message, author, text);
                        Log.err(throwable);
                    }
            );
        });
    }
}
