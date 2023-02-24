package org.ru.vortex.modules.sockets.modules;

import java.util.concurrent.CompletableFuture;

public interface Socket
{
    CompletableFuture<Void> open();

    CompletableFuture<Void> restart();

    CompletableFuture<Void> close();

    <E extends Event> CompletableFuture<Void> sendEvent(final E event);

    Status getStatus();

    enum Status
    {
        OPENING,
        OPEN,
        CLOSING,
        CLOSED,
        UNUSABLE
    }
}
