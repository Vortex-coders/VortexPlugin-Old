package org.ru.vortex.modules.database;

import arc.util.Strings;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.*;
import mindustry.gen.Player;
import org.bson.conversions.Bson;
import org.ru.vortex.modules.database.models.BanData;
import org.ru.vortex.modules.database.models.PlayerData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static arc.util.Log.errTag;
import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.*;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.codecs.pojo.PojoCodecProvider.builder;
import static org.ru.vortex.PluginVars.config;

public class Database
{

    public static MongoClient client;
    public static MongoDatabase database;

    public static MongoCollection<PlayerData> playersCollection;
    public static MongoCollection<BanData> bansCollection;

    public static void init()
    {
        try
        {
            client = MongoClients.create(config.mongoUrl);
            database =
                    client
                            .getDatabase("vortex")
                            .withCodecRegistry(fromRegistries(getDefaultCodecRegistry(), fromProviders(builder().automatic(true).build())));

            playersCollection = database.getCollection("players", PlayerData.class);
            bansCollection = database.getCollection("bans", BanData.class);
        }
        catch (Exception e)
        {
            errTag("Database", Strings.format("Unable connect to database: @", e));
        }
    }

    public static Mono<PlayerData> getPlayerData(Player player)
    {
        return getPlayerData(player.uuid());
    }

    public static Mono<PlayerData> getPlayerData(String uuid)
    {
        return Mono.from(playersCollection.find(eq("uuid", uuid))).defaultIfEmpty(new PlayerData(uuid));
    }

    public static Flux<PlayerData> getPlayersData(Iterable<Player> players)
    {
        return Flux.fromIterable(players).flatMap(Database::getPlayerData);
    }

    public static Mono<UpdateResult> setPlayerData(PlayerData data)
    {
        return Mono.from(playersCollection.replaceOne(eq("uuid", data.uuid), data, new ReplaceOptions().upsert(true)));
    }

    public static Mono<BanData> getBan(String uuid, String ip)
    {
        return Mono.from(bansCollection.find(getBanFilter(uuid, ip))).defaultIfEmpty(new BanData());
    }

    public static Mono<UpdateResult> setBan(BanData data)
    {
        return Mono.from(bansCollection.replaceOne(getBanFilter(data.uuid, data.ip), data, new ReplaceOptions().upsert(true)));
    }

    public static Mono<DeleteResult> unBan(BanData data)
    {
        return unBan(data.uuid, data.ip);
    }

    public static Mono<DeleteResult> unBan(String uuid, String ip)
    {
        return Mono.from(bansCollection.deleteMany(getBanFilter(uuid, ip)));
    }

    private static Bson getBanFilter(String uuid, String ip)
    {
        return or(and(eq("uuid", uuid), eq("server", config.gamemode.name())), and(eq("ip", ip), eq("server", config.gamemode.name())));
    }

    public static Mono<BanData> getBanned()
    {
        return Mono.from(bansCollection.find(eq("server", config.gamemode.name())));
    }
}
