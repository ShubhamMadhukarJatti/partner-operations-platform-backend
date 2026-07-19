package com.sharkdom.config.trello.threading;


import org.jetbrains.annotations.NotNull;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public class TrelloExecutor implements Executor {

    private final ExecutorService executorService;

    public TrelloExecutor(final ExecutorService executorService) {
        this.executorService = new DelegatingSecurityContextExecutorService(executorService);
    }

    @Override
    public void execute(@NotNull Runnable command) {
        executorService.execute(command);
    }
}
