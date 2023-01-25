package org.ru.vortex.modules.discord;

import static arc.util.Log.infoTag;
import static arc.util.Strings.format;
import static arc.util.Strings.stripColors;
import static mindustry.Vars.state;
import static net.dv8tion.jda.api.JDA.Status.CONNECTED;
import static net.dv8tion.jda.api.Permission.ADMINISTRATOR;
import static net.dv8tion.jda.api.entities.Activity.watching;
import static net.dv8tion.jda.api.entities.Message.MentionType.CHANNEL;
import static net.dv8tion.jda.api.entities.Message.MentionType.EMOJI;
import static net.dv8tion.jda.api.requests.GatewayIntent.*;
import static net.dv8tion.jda.api.utils.MemberCachePolicy.OWNER;
import static net.dv8tion.jda.api.utils.MemberCachePolicy.VOICE;
import static org.ru.vortex.PluginVars.config;

import arc.util.Log;
import arc.util.Strings;
import java.awt.*;
import java.util.EnumSet;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.messages.MessageRequest;
import org.ru.vortex.modules.database.models.BanData;

public class Bot {

    public static Role adminRole;
    public static MessageChannel botChannel, adminChannel, bansChannel;
    private static JDA jda;

    public static void init() {
        try {
            jda =
                JDABuilder
                    .createLight(config.token)
                    .disableCache(CacheFlag.ACTIVITY)
                    .setMemberCachePolicy(VOICE.or(OWNER))
                    .setChunkingFilter(ChunkingFilter.NONE)
                    .disableIntents(GUILD_MESSAGE_TYPING, GUILD_PRESENCES)
                    .setLargeThreshold(50)
                    .enableIntents(MESSAGE_CONTENT, GUILD_MEMBERS)
                    .addEventListeners(new Listener())
                    .build()
                    .awaitReady();

            adminRole = jda.getRoleById(config.adminRoleId);
            botChannel = jda.getThreadChannelById(config.channelId);
            adminChannel = jda.getTextChannelById(config.adminChannelId);
            bansChannel = jda.getTextChannelById(config.bansChannelId);

            MessageRequest.setDefaultMentions(EnumSet.of(CHANNEL, EMOJI));

            jda
                .getGuildCache()
                .stream()
                .findFirst()
                .ifPresent(guild ->
                    guild.getSelfMember().modifyNickname(format("[@] @", config.prefix, jda.getSelfUser().getName())).queue()
                );

            infoTag("Discord", format("Bot connected in as @", jda.getSelfUser().getAsTag()));
        } catch (InterruptedException | IllegalArgumentException e) {
            Log.errTag("Discord", format("Cannot connect to discord: @", e));
        } catch (InvalidTokenException e) {
            Log.errTag("Discord", format("Invalid discord bot token: @", e));
        }
    }

    public static boolean isAdmin(Member member) {
        return member != null && (member.getRoles().contains(adminRole) || member.hasPermission(ADMINISTRATOR));
    }

    public static boolean isConnected() {
        return jda != null && jda.getStatus() == CONNECTED;
    }

    public static void disconnect() {
        if (isConnected()) jda.shutdownNow();
    }

    public static void sendMessage(MessageChannel channel, String content) {
        if (channel != null && channel.canTalk()) channel.sendMessage(content).queue();
    }

    public static void sendMessage(MessageChannel channel, String content, Object... values) {
        if (channel != null && channel.canTalk()) channel.sendMessage(Strings.format(content, values)).queue();
    }

    public static void sendEmbed(MessageChannel channel, String title) {
        if (channel != null && channel.canTalk()) channel.sendMessageEmbeds(new EmbedBuilder().setTitle(title).build()).queue();
    }

    public static void sendEmbed(MessageChannel channel, String title, Object... values) {
        if (channel != null && channel.canTalk()) channel
            .sendMessageEmbeds(new EmbedBuilder().setTitle(Strings.format(title, values)).build())
            .queue();
    }

    public static void updateStatus() {
        if (isConnected()) jda
            .getPresence()
            .setActivity(watching(format("at @ players on @", Groups.player.size(), stripColors(state.map.name()))));
    }

    public static void sendMessageToGame(Member member, Message message) {
        var nickname = member.getUser().getAsTag();
        var rawContent = message.getContentRaw();

        infoTag("Discord", Strings.format("@: @", nickname, rawContent));
        Call.sendMessage(Strings.format("[blue][Discord] [white]@[blue]: [white]@", nickname, rawContent));
    }

    public static void sendBanMessage(BanData ban) {
        if (bansChannel == null || !bansChannel.canTalk()) return;

        bansChannel
            .sendMessageEmbeds(
                new EmbedBuilder()
                    .setTitle(ban.name + " banned")
                    .addField("Administrator", ban.adminName, false)
                    .addField("Reason", ban.reason, false)
                    .addField("Unban date", TimeFormat.DATE_LONG.format(ban.unbanDate), false)
                    .setColor(Color.red)
                    .build()
            )
            .queue();
    }
}
