package org.ru.vortex.modules;

import static mindustry.Vars.netServer;
import static net.dv8tion.jda.api.entities.emoji.Emoji.fromFormatted;
import static net.dv8tion.jda.api.interactions.components.ActionRow.of;
import static org.ru.vortex.PluginVars.loginWaiting;
import static org.ru.vortex.modules.discord.Bot.adminChannel;

import arc.func.Func2;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

public class GameOAuth {

  public static final SelectMenu menu = StringSelectMenu
    .create("oauth")
    .addOption(
      "Confirm",
      "oauth.confirm",
      "Confirm request.",
      fromFormatted("✅")
    )
    .addOption("Reject", "oauth.reject", "Deny request.", fromFormatted("❌"))
    .addOption(
      "Information",
      "oauth.information",
      "Look up all information about the player.",
      fromFormatted("🔎")
    )
    .setPlaceholder("Select an action...")
    .build();

  public static void sendAdminRequest(Player player) {
    if (adminChannel == null || !adminChannel.canTalk()) return;

    adminChannel
      .sendMessageEmbeds(
        new EmbedBuilder()
          .setTitle("Admin request")
          .addField("Nickname:", player.plainName(), true)
          .addField("UUID:", player.uuid(), true)
          .setFooter(
            "Select the desired option to confirm or deny the request. Confirm only your requests!"
          )
          .build()
      )
      .setComponents(of(menu))
      .queue(message -> loginWaiting.put(message, player.getInfo()));
  }

  public static void confirm(StringSelectInteractionEvent event) {
    remove(
      event,
      (info, player) -> {
        netServer.admins.adminPlayer(info.id, info.adminUsid);

        if (player != null) {
          player.admin(true);
          Bundler.sendLocalized(player, "commands.login.successful");
        }

        return new EmbedBuilder().setTitle("Request Confirmed");
      }
    );
  }

  public static void deny(StringSelectInteractionEvent event) {
    remove(
      event,
      (info, player) -> {
        if (player != null) Bundler.getLocalized(
          player,
          "commands.login.rejected"
        );
        return new EmbedBuilder().setTitle("Request Denied");
      }
    );
  }

  public static void information(StringSelectInteractionEvent event) {
    var playerInfo = loginWaiting.get(event.getMessage());

    var embed = new EmbedBuilder()
      .setTitle(":mag: Player Info")
      .addField("Nickname:", playerInfo.plainLastName(), true)
      .addField("UUID:", playerInfo.id, true)
      .addField("IP:", playerInfo.lastIP, true)
      .addField("Times joined:", String.valueOf(playerInfo.timesJoined), true)
      .addField("Times kicked:", String.valueOf(playerInfo.timesKicked), true)
      .addField("All nicknames:", playerInfo.names.toString(), true)
      .addField("All IPs", playerInfo.ips.toString(), true);

    event.replyEmbeds(embed.build()).setEphemeral(true).queue();
  }

  private static void remove(
    StringSelectInteractionEvent event,
    Func2<Administration.PlayerInfo, Player, EmbedBuilder> func
  ) {
    var info = loginWaiting.remove(event.getMessage());
    var player = Groups.player.find(target -> target.uuid().equals(info.id));

    event
      .getChannel()
      .sendMessageEmbeds(
        func
          .get(info, player)
          .addField("Administrator:", event.getUser().getAsMention(), true)
          .addField("Player:", info.plainLastName(), true)
          .build()
      )
      .queue();

    event.getMessage().delete().queue();
  }
}
