package org.ru.vortex.modules;

import arc.func.Cons;
import arc.struct.StringMap;
import arc.util.Http;
import arc.util.Strings;
import arc.util.serialization.JsonReader;
import mindustry.gen.Groups;
import mindustry.gen.Player;

import static mindustry.Vars.netServer;
import static org.ru.vortex.PluginVars.translationApiUrl;
import static org.ru.vortex.modules.database.Database.getPlayersData;

public class Translator {

    public static void translate(String text, String from, String to, Cons<String> result, Runnable error) {
        Http.post(translationApiUrl, "tl=" + to + "&sl=" + from + "&q=" + Strings.encode(text))
                .error(throwable -> error.run())
                .submit(response -> result.get(new JsonReader().parse(response.getResultAsString()).get(0).get(0).asString()));
    }

    public static void translate(Player author, String text) {
        var cache = new StringMap();
        var message = netServer.chatFormatter.format(author, text);

        getPlayersData(Groups.player).doOnNext(data -> {
            var player = Groups.player.find(p -> p.uuid().equals(data.uuid));

            if (player == author) return;

            if (data.translatorLanguage.equals("off")) {
                player.sendMessage(message, author, text);
                return;
            }

            if (cache.containsKey(data.translatorLanguage)) {
                player.sendMessage(cache.get(data.translatorLanguage), author, text);
            } else translate(text, "auto", data.translatorLanguage, result -> {
                cache.put(data.translatorLanguage, message + " [white]([lightgray]" + result + "[])");
                player.sendMessage(cache.get(data.translatorLanguage), author, text);
            }, () -> player.sendMessage(message, author, text));
        }).subscribe();
    }
}