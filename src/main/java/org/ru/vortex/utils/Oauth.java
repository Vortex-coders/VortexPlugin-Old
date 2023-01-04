package org.ru.vortex.utils;

import mindustry.gen.Player;
import org.ru.vortex.PluginVars;
import org.ru.vortex.modules.database.Database;

import static org.ru.vortex.PluginVars.discordAuthString;

/**
 * Class contains static OauthMethods
 */
public class Oauth {

    /**
     * Generate auth link using player uuid
     *
     * @param p player who need auth url
     * @return http string ready-to-use
     * @see PluginVars#discordAuthString
     */
    public static String getAuthLink(Player p) {
        return discordAuthString + p.uuid();
    }

    /**
     * Check player's authorization
     *
     * @param p target player
     * @return boolean value of player's authorization
     */
    public static boolean isAuthorized(Player p) {
        return getDiscordID(p) != -1;
    }

    /**
     * Finding discord id for player
     *
     * @param p target player
     * @return ID of this player, -1 if not authorized
     * @see #getDiscordID(String)
     */
    public static Long getDiscordID(Player p) {
        return getDiscordID(p.uuid());
    }

    /**
     * Find discord id for uuid string
     *
     * @param uuid string(player uuid)
     * @return ID associated with this UUID, -1 if not authorized
     */
    public static Long getDiscordID(String uuid) {
        return Pipe.apply(Database.getPlayerData(uuid).block()).result().discord;
    }
}
