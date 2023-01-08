package org.ru.vortex.modules.discord;

import static arc.Core.app;
import static org.ru.vortex.modules.discord.Bot.botChannel;
import static org.ru.vortex.modules.discord.Bot.sendMessageToGame;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class Listener extends ListenerAdapter {

  @Override
  public void onMessageReceived(@NotNull MessageReceivedEvent event) {
    if (
      event.getChannel() == botChannel && event.getMember() != null
    ) app.post(() -> sendMessageToGame(event.getMember(), event.getMessage()));
  }
}
