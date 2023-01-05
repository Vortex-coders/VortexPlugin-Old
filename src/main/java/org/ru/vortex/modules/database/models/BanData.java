package org.ru.vortex.modules.database.models;

import arc.func.Cons;

public class BanData {

    public String uuid;
    public String ip;
    public String reason;
    public String server;
    public long unbanDate;

    @SuppressWarnings("unused")
    public BanData() {}

    public BanData(String uuid, String ip, String reason, String server, long unbanDate) {
        this.uuid = uuid;
        this.ip = ip;
        this.reason = reason;
        this.server = server;
        this.unbanDate = unbanDate;
    }

    public BanData(Cons<BanData> consumer) {
        consumer.get(this);
    }
}
