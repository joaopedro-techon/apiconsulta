package br.com.consultas.application.concurrent;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utilitário de fanout com semântica fail-fast.
 * - Executa tasks em paralelo usando o {@link ExecutorService} fornecido (recomendado: Virtual Threads).
 * - Ao detectar a primeira falha, cancela as demais tasks.
 * - Aguarda por sucesso total ou timeout.
 */
public final class VirtualThreadFanout {

    private VirtualThreadFanout() {
    }

    public static <T> List<T> failFast(
            List<Callable<T>> tasks,
            ExecutorService executor,
            Duration timeout
    ) {
        if (tasks == null || tasks.isEmpty()) {
            return List.of();
        }

        List<CompletableFuture<T>> futures = tasks.stream()
                .map(task -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return task.call();
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                }, executor))
                .toList();

        CompletableFuture<Void> all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        AtomicReference<Throwable> firstFailure = new AtomicReference<>(null);
        CompletableFuture<Void> firstFailureSignal = new CompletableFuture<>();

        for (CompletableFuture<T> f : futures) {
            f.whenComplete((r, ex) -> {
                if (ex != null && firstFailureSignal.complete(null)) {
                    Throwable cause = ex instanceof CompletionException ce ? ce.getCause() : ex;
                    firstFailure.set(cause);
                    // Fail-fast: cancela o restante.
                    futures.forEach(other -> other.cancel(true));
                }
            });
        }

        try {
            CompletableFuture.anyOf(all, firstFailureSignal).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            futures.forEach(f -> f.cancel(true));
            throw new RuntimeException("Timeout no fanout", e);
        } catch (Exception e) {
            // ignore: a causa real já está em firstFailure (se falhou)
        }

        Throwable failure = firstFailure.get();
        if (failure != null) {
            if (failure instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(failure);
        }

        return futures.stream().map(CompletableFuture::join).toList();
    }
}

