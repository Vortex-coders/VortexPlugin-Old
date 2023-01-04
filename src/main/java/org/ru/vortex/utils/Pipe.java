package org.ru.vortex.utils;

import lombok.SneakyThrows;

public class Pipe<A> {

    private A object;
    private Throwable error;

    private Pipe(A object) {
        this.object = object;
    }

    private Pipe(Throwable error) {
        this.error = error;
    }

    public static <A> Pipe<A> apply(A object) {
        return new Pipe<>(object);
    }

    public static <A> Pipe<A> apply(ErrorableSupplier<A> consumer) {
        try {
            return new Pipe<>(consumer.get());
        } catch (Throwable e) {
            return new Pipe<>(e);
        }
    }

    public <R> Pipe<R> pipe(ErrorableFunction<A, R> func) {
        throwIfNotCatched();

        try {
            return new Pipe<>(func.apply(object));
        } catch (Throwable e) {
            return new Pipe<>(e);
        }
    }

    public <R, B> Pipe<R> pipe(ErrorableBiFunction<A, B, R> func, B argObject1) {
        throwIfNotCatched();

        try {
            return new Pipe<>(func.apply(object, argObject1));
        } catch (Throwable e) {
            return new Pipe<>(e);
        }
    }

    public <R, B, C> Pipe<R> pipe(
            ErrorableTriFunction<A, B, C, R> func,
            B argObject1,
            C argObject2
    ) {
        throwIfNotCatched();

        try {
            return new Pipe<>(func.apply(object, argObject1, argObject2));
        } catch (Throwable e) {
            return new Pipe<>(e);
        }
    }

    public <R, B, C, D> Pipe<R> pipe(
            ErrorableQuadriFunction<A, B, C, D, R> func,
            B argObject1,
            C argObject2,
            D argObject3
    ) {
        throwIfNotCatched();

        try {
            return new Pipe<>(func.apply(object, argObject1, argObject2, argObject3));
        } catch (Throwable e) {
            return new Pipe<>(e);
        }
    }

    public Pipe<A> consume(ErrorableConsumer<A> consumer) {
        throwIfNotCatched();

        try {
            consumer.accept(object);

            return this;
        } catch (Throwable e) {
            return new Pipe<>(e);
        }
    }

    public Pipe<A> run(ErrorableRunnable runnable) {
        try {
            runnable.run();
            return this;
        } catch (Throwable e) {
            return new Pipe<>(e);
        }
    }

    public Pipe<A> catchError(ErrorableFunction<Throwable, A> func) {
        try {
            if (error != null) return new Pipe<>(func.apply(error));
            return this;
        } catch (Throwable e) {
            return new Pipe<>(e);
        }
    }

    public Pipe<A> catchError(ErrorableConsumer<Throwable> func) {
        try {
            if (error != null) func.accept(error);
            return new Pipe<>(object);
        } catch (Throwable e) {
            return new Pipe<>(e);
        }
    }

    public A result() {
        throwIfNotCatched();

        return object;
    }

    @SneakyThrows
    private void throwIfNotCatched() {
        if (error != null) throw error;
    }

    @FunctionalInterface
    public interface ErrorableFunction<A, R> {
        R apply(A a) throws Throwable;
    }

    @FunctionalInterface
    public interface ErrorableBiFunction<A, B, R> {
        R apply(A a, B b) throws Throwable;
    }

    @FunctionalInterface
    public interface ErrorableTriFunction<A, B, C, R> {
        R apply(A a, B b, C c) throws Throwable;
    }

    @FunctionalInterface
    public interface ErrorableQuadriFunction<A, B, C, D, R> {
        R apply(A a, B b, C c, D d) throws Throwable;
    }

    @FunctionalInterface
    public interface ErrorableConsumer<A> {
        void accept(A a) throws Throwable;
    }

    @FunctionalInterface
    public interface ErrorableSupplier<A> {
        public A get() throws Throwable;
    }

    public interface ErrorableRunnable {
        public void run() throws Throwable;
    }
}
