package org.ru.vortex.modules.database.models;

import arc.func.Cons;

public class BanData {

    public String uuid;
    public String ip;

    public String name = "<unknown>";
    public String adminName;
    public String reason;
    public String server;
    public long unbanDate;

    @SuppressWarnings("unused")
    public BanData() {}

    public BanData(Cons<BanData> consumer) {
        consumer.get(this);
    }
}
