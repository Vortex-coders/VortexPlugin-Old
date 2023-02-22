package org.ru.vortex.modules;

import arc.Events;
import arc.util.Log;
import fr.xpdustry.javelin.JavelinEvent;
import fr.xpdustry.javelin.JavelinPlugin;
import fr.xpdustry.javelin.JavelinSocket;
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
                if (incomeMessageEvent.getPort() != Administration.Config.port.num())
                {
                    Log.debug(incomeMessageEvent.getAuthorName() + " |>>>| " + incomeMessageEvent.getMessage());
                    //TODO Для люсни и его кода ибо я ня лазить по переводчику
                }
            });

            socket.subscribe(AdminResEvent.class, adminResEvent ->
            {
                if (adminResEvent.port != Administration.Config.port.num())
                {
                    return;
                }

                Player p = Groups.player.find(player -> player.uuid().equals(adminResEvent.uuid));
                if (p == null)
                {
                    return;
                }
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

    public static final class MessageSendEvent implements JavelinEvent
    {
        private final String authorName;
        private final String message;
        private final int port;

        public MessageSendEvent(String author, String msg)
        {
            this.authorName = author;
            this.message = msg;
            this.port = Administration.Config.port.num();
        }
    }

    public static final class IncomeMessageEvent implements JavelinEvent
    {
        private final String authorName;
        private final String message;
        private final int port;

        public IncomeMessageEvent(String author, String msg, int port)
        {
            this.authorName = author;
            this.message = msg;
            this.port = port;
        }

        public int getPort()
        {
            return this.port;
        }

        public String getAuthorName()
        {
            return authorName;
        }

        public String getMessage()
        {
            return message;
        }
    }


    public static final class GatesEvent implements JavelinEvent
    {
        private final String user;
        private final boolean join;
        private final int port;

        public GatesEvent(String author, boolean join)
        {
            this.user = author;
            this.join = join;
            this.port = Administration.Config.port.num();
        }
    }

    public static final class AdminReqEvent implements JavelinEvent
    {
        private final String user;
        private final String uuid;
        private final int port;

        public AdminReqEvent(String author, String uuid)
        {
            this.user = author;
            this.uuid = uuid;
            this.port = Administration.Config.port.num();
        }
    }

    public static final class AdminResEvent implements JavelinEvent
    {
        private final String uuid;
        private final boolean confirm;
        private final int port;

        public AdminResEvent(String uuid, boolean confirm, int port)
        {
            this.uuid = uuid;
            this.confirm = confirm;
            this.port = port;
        }
    }
}
