package org.ru.vortex.modules.sockets;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.extensions.permessage_deflate.PerMessageDeflateExtension;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.protocols.Protocol;
import org.ru.vortex.modules.sockets.modules.AbstractSocket;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static arc.util.Log.err;
import static arc.util.Log.info;

public class ClientSocket extends AbstractSocket
{
    private final ClientWebSocket socket;
    private final ExecutorService executor;
    private final AtomicBoolean connecting = new AtomicBoolean();

    public ClientSocket(final URI uri, final int workers)
    {
        this.executor = Executors.newFixedThreadPool(workers);
        this.socket = new ClientWebSocket(uri);
    }

    @Override
    public CompletableFuture<Void> open()
    {
        if (getStatus() != Status.UNUSABLE && getStatus() == Status.CLOSED && connecting.compareAndSet(false, true))
        {
            final var future = new CompletableFuture<Void>();
            ForkJoinPool.commonPool().execute(() ->
            {
                try
                {
                    if (!socket.connectBlocking())
                    {
                        future.completeExceptionally(new IOException("Failed to connect."));
                    }
                    else
                    {
                        future.complete(null);
                    }
                }
                catch (final InterruptedException e)
                {
                    future.cancel(true);
                }
                finally
                {
                    connecting.set(false);
                }
            });
            return future;
        }

        return CompletableFuture.failedFuture(
                new IllegalStateException("The client socket can't be started in it's current state."));
    }

    @Override
    public CompletableFuture<Void> restart()
    {
        if (getStatus() != Status.UNUSABLE && getStatus() != Status.CLOSING && connecting.compareAndSet(false, true))
        {
            final var future = new CompletableFuture<Void>();
            ForkJoinPool.commonPool().execute(() ->
            {
                try
                {
                    if (!socket.reconnectBlocking())
                    {
                        future.completeExceptionally(new IOException("Failed to connect."));
                    }
                    else
                    {
                        future.complete(null);
                    }
                }
                catch (final InterruptedException e)
                {
                    future.cancel(true);
                }
                finally
                {
                    connecting.set(false);
                }
            });
            return future;
        }

        return CompletableFuture.failedFuture(
                new IllegalStateException("The client socket can't be restarted in it's current state."));
    }

    @Override
    public CompletableFuture<Void> close()
    {
        if (getStatus() == Status.OPEN)
        {
            final var future = new CompletableFuture<Void>();
            ForkJoinPool.commonPool().execute(() ->
            {
                try
                {
                    socket.closeBlocking();
                    executor.shutdown();
                    future.complete(null);
                }
                catch (final InterruptedException e)
                {
                    future.cancel(true);
                }
            });
            return future;
        }
        else if (!executor.isShutdown())
        {
            return CompletableFuture.runAsync(executor::shutdown);
        }

        return CompletableFuture.failedFuture(
                new IllegalStateException("The client socket can't be closed in it's current state."));
    }

    @Override
    protected void onEventSend(final ByteBuffer buffer)
    {
        socket.send(buffer);
    }

    @Override
    public Status getStatus()
    {
        if (connecting.get())
        {
            return Status.OPENING;
        }

        return switch (socket.getReadyState())
                {
                    case OPEN -> Status.OPEN;
                    case CLOSING -> Status.CLOSING;
                    default -> executor.isShutdown() ? Status.UNUSABLE : Status.CLOSED;
                };
    }

    private final class ClientWebSocket extends WebSocketClient
    {
        private ClientWebSocket(final URI uri)
        {
            super(uri, new Draft_6455(Collections.singletonList(new PerMessageDeflateExtension()), List.of(new Protocol(""), new Protocol("ocpp2.0"))));

            this.setConnectionLostTimeout(60);
        }

        @Override
        public void onOpen(final ServerHandshake data)
        {
            info("The connection has been successfully established with the server: @", data.getHttpStatusMessage());
        }

        @Override
        public void onMessage(final String message)
        {
            info("Received message (ignoring): @", message);
        }

        @Override
        public void onMessage(final ByteBuffer bytes)
        {
            executor.execute(() -> onEventReceive(bytes));
        }

        @Override
        public void onClose(final int code, final String reason, final boolean remote)
        {
            switch (code)
            {
            case CloseFrame.NORMAL -> info("The connection has been closed.");
            case CloseFrame.GOING_AWAY -> info("The connection has been closed by the server.");
            default -> err(
                    "The connection has been unexpectedly closed (@, @).", code, reason);
            }
        }

        @Override
        public void onError(final Exception e)
        {
            err("An exception occurred in the websocket client: @", e);
        }
    }
}
