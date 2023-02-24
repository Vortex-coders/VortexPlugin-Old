package org.ru.vortex.modules.sockets.modules;

import arc.Events;
import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.KryoException;
import com.esotericsoftware.kryo.kryo5.io.*;
import com.esotericsoftware.kryo.kryo5.objenesis.strategy.StdInstantiatorStrategy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractSocket implements Socket
{
    private final Kryo kryo = new Kryo();

    {
        kryo.setRegistrationRequired(false);
        kryo.setAutoReset(true);
        kryo.setOptimizedGenerics(false);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    protected abstract void onEventSend(final ByteBuffer buffer);

    @Override
    public <E extends Event> CompletableFuture<Void> sendEvent(E event)
    {
        if (this.getStatus() != Status.OPEN)
        {
            return CompletableFuture.failedFuture(new IOException("Socket is closed"));
        }
        else
        {
            try (final var output = new ByteBufferOutput(ByteBuffer.allocate(8192)))
            {
                kryo.writeClass(output, event.getClass());
                kryo.writeObject(output, event);
                onEventSend(output.getByteBuffer());
                return CompletableFuture.completedFuture(null);
            }
            catch (KryoBufferOverflowException e)
            {
                return CompletableFuture.failedFuture(new IOException("Event is larger than expected"));
            }
        }
    }

    protected void onEventReceive(final ByteBuffer buffer)
    {
        try (final var input = new ByteBufferInput(buffer))
        {
            final var registration = kryo.readClass(input);
            if (registration == null) return;

            @SuppressWarnings("unchecked") final var clazz = (Class<? extends Event>) registration.getType();

            Events.fire(clazz, kryo.readObject(input, clazz));
        }
        catch (final KryoException e)
        {
            if (!(e.getCause() instanceof ClassNotFoundException))
            {
                throw e;
            }
        }
    }
}
