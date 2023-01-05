package org.ru.vortex;

import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_DASHES;

import arc.struct.IntIntMap;
import arc.struct.OrderedMap;
import arc.util.CommandHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.HashSet;
import mindustry.net.Administration;
import net.dv8tion.jda.api.entities.Message;
import org.ru.vortex.modules.Config;

public class PluginVars {

    public static final IntIntMap placedBlocksCache = new IntIntMap(), brokenBlocksCache = new IntIntMap();
    public static final String translationApiUrl = "";
    public static final String serverLink = "https://discord.gg/pTtQTUQM68";
    public static final String discordAuthString =
        "https://discord.com/api/oauth2/authorize?client_id=1058095954097610794&redirect_uri=http%3A%2F%2Flocalhost%3A3000%2Fredirect&response_type=code&scope=identify&state=";

    public static final Gson gson = new GsonBuilder()
        .setFieldNamingPolicy(LOWER_CASE_WITH_DASHES)
        .setPrettyPrinting()
        .serializeNulls()
        .disableHtmlEscaping()
        .create();

    public static final OrderedMap<Message, Administration.PlayerInfo> loginWaiting = new OrderedMap<>();
    public static final String outlinePassword = "hentai";
    public static final double rtvRatio = 0.6;
    public static final HashSet<String> rtvVotes = new HashSet<>();
    public static boolean rtvEnabled = true;
    public static Config config;
    public static CommandHandler clientCommands, serverCommands, outlineCommands;
}
