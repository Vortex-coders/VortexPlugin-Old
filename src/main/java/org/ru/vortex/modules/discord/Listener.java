package org.ru.vortex.modules.discord;

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import static arc.Core.app;
import static org.ru.vortex.PluginVars.loginWaiting;
import static org.ru.vortex.modules.GameOAuth.*;
import static org.ru.vortex.modules.discord.Bot.*;

public class Listener extends ListenerAdapter
{

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event)
    {
        if (event.getChannel() == botChannel && !event.getAuthor().isBot() && event.getMember() != null) app.post(() ->
                sendMessageToGame(event.getMember(), event.getMessage())
        );
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event)
    {
        if (!loginWaiting.containsKey(event.getMessage()) || !isAdmin(event.getMember())) event
                .reply("Missing permissions.")
                .setEphemeral(true)
                .queue();

        if (event.getComponentId().equals("oauth")) switch (event.getValues().get(0))
        {
        case "oauth.confirm" -> confirm(event);
        case "oauth.reject" -> deny(event);
        case "oauth.information" -> information(event);
        }
    }
}
