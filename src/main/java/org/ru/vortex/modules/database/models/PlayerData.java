package org.ru.vortex.modules.database.models;

public class PlayerData
{

    public final String translatorLanguage = "off";
    public String uuid;
    public long discord = 0L;
    public int blocksBuilt = 0;
    public int blocksBroken = 0;
    public int gamesPlayed = 0;

    @SuppressWarnings("unused")
    public PlayerData()
    {
    }

    public PlayerData(String uuid)
    {
        this.uuid = uuid;
        this.discord = -1;
    }

    public PlayerData(String uuid, long discord)
    {
        this.uuid = uuid;
        this.discord = discord;
    }
}
