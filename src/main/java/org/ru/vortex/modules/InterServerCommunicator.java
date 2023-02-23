package org.ru.vortex.modules;

import arc.Events;
import arc.util.Log;
import fr.xpdustry.javelin.*;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration;

import static org.ru.vortex.utils.Utils.temporaryBan;

public class InterServerCommunicator
{
    JavelinSocket socket;

    public InterServerCommunicator()
    {
        socket = JavelinPlugin.getJavelinSocket();

        Events.on(EventType.ServerLoadEvent.class, e ->
        {
            socket = JavelinPlugin.getJavelinSocket();

            socket.subscribe(IncomeMessageEvent.class, incomeMessageEvent ->
            {
                if (incomeMessageEvent.port() != Administration.Config.port.num())
                    Log.debug(incomeMessageEvent.authorName() + " |>>>| " + incomeMessageEvent.message());
            });

            socket.subscribe(AdminResEvent.class, adminResEvent ->
            {
                if (adminResEvent.port != Administration.Config.port.num()) return;

                Player p = Groups.player.find(player -> player.uuid().equals(adminResEvent.uuid));
                if (p == null) return;

                if (adminResEvent.confirm)
                {
                    p.admin(true);
                }
                else
                {
                    // TODO: 23.02.2023 переделать текст
                    temporaryBan(p, "Intentional server designer Skyfire", "Illegal admin request", 3L);
                }
            });
        });
    }

    public void sendConnect(Player p)
    {
        safeSend(new GatesEvent(p.plainName(), true));
    }

    public void sendDisconnect(Player p)
    {
        safeSend(new GatesEvent(p.plainName(), false));
    }

    public void sendMessage(Player p, String message)
    {
        safeSend(new MessageSendEvent(p.plainName(), message));
    }

    public void sendAdminReq(Player p)
    {
        safeSend(new AdminReqEvent(p.plainName(), p.uuid()));
    }

    public void safeSend(JavelinEvent event)
    {
        if (socket.getStatus() == JavelinSocket.Status.OPEN)
        {
            socket.sendEvent(event);
        }
        else
        {
            Log.debug("|>| Socket error |<| Status " + socket.getStatus().name());
            socket = JavelinPlugin.getJavelinSocket();
            Log.debug("|>| Socket recon |<| Status " + socket.getStatus().name());
            socket.sendEvent(event);
        }
    }

    public record IncomeMessageEvent(String authorName, String message, int port) implements JavelinEvent
    {

    }

    public record AdminResEvent(String uuid, boolean confirm, int port) implements JavelinEvent
    {

    }

    public record MessageSendEvent(String authorName, String message, int port) implements JavelinEvent
    {
        public MessageSendEvent(String author, String msg)
        {
            this(author, msg, Administration.Config.port.num());
        }
    }

    public record GatesEvent(String user, boolean join, int port) implements JavelinEvent
    {
        public GatesEvent(String author, boolean join)
        {
            this(author, join, Administration.Config.port.num());
        }
    }

    public record AdminReqEvent(String user, String uuid, int port) implements JavelinEvent
    {
        public AdminReqEvent(String author, String uuid)
        {
            this(author, uuid, Administration.Config.port.num());
        }
    }
}
