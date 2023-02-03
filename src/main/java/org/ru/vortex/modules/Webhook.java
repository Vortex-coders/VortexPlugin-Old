package org.ru.vortex.modules;

import arc.util.Strings;
import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import mindustry.gen.Player;

import static arc.util.Log.err;
import static club.minnced.discord.webhook.send.WebhookEmbed.EmbedTitle;
import static org.ru.vortex.PluginVars.config;

public class Webhook
{
    private static WebhookClient client;

    public static void init()
    {
        try
        {
            client = new WebhookClientBuilder(config.webhookUrl)
                    .setThreadId(config.webhookThread)
                    .setAllowedMentions(AllowedMentions.none())
                    .setThreadFactory(job ->
                    {
                        Thread thread = new Thread(job);
                        thread.setName(config.gamemode + " - webhook");
                        thread.setDaemon(true);
                        return thread;
                    })
                    .setWait(true)
                    .build();
        }
        catch (IllegalArgumentException | NullPointerException exception)
        {
            err("The webhook url isn't valid: @", exception);
        }
    }

    public static void disconnect()
    {
        if (!client.isShutdown() && !client.isWait())
            client.close();
    }

    public static void sendFrom(Player player, String message)
    {
        client.send(new WebhookMessageBuilder().setContent(Strings.format("**`@:`** @", player.plainName(), message)).build());
    }

    public static void sendInfo(String content, Object... objects)
    {
        client.send(new WebhookEmbedBuilder().setTitle(new EmbedTitle(Strings.format(content, objects), null)).build());
    }
}
