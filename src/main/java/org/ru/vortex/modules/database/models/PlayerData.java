package org.ru.vortex.modules.database.models;

public class PlayerData
{

    public String translatorLanguage = "off";
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

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    public PlayerData setDiscord(long discord)
    {
        this.discord = discord;
        return this;
    }

    public PlayerData setBlocksBuilt(int blocksBuilt)
    {
        this.blocksBuilt = blocksBuilt;
        return this;
    }

    public PlayerData setBlocksBroken(int blocksBroken)
    {
        this.blocksBroken = blocksBroken;
        return this;
    }

    public PlayerData setGamesPlayed(int gamesPlayed)
    {
        this.gamesPlayed = gamesPlayed;
        return this;
    }
}
