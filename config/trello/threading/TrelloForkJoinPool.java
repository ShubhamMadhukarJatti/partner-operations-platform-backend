package com.sharkdom.config.trello.threading;

import java.util.concurrent.ForkJoinPool;

public final class TrelloForkJoinPool extends ForkJoinPool {

    public TrelloForkJoinPool(final int parallelism,
                              final ForkJoinWorkerThreadFactory factory,
                              final Thread.UncaughtExceptionHandler handler,
                              final boolean asyncMode) {
        super(parallelism, factory, handler, asyncMode);
    }
}
