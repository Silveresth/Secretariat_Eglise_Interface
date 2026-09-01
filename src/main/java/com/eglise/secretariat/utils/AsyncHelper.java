package com.eglise.secretariat.utils;

import javafx.application.Platform;
import javafx.scene.Node;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AsyncHelper {

    /**
     * Executes a background task and manages UI loader/spinner visibility safely on the FX Thread.
     */
    public static <T> void execute(
            CompletableFuture<T> future,
            Node loadingIndicator,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError
    ) {
        if (loadingIndicator != null) {
            Platform.runLater(() -> loadingIndicator.setVisible(true));
        }

        future.whenComplete((result, throwable) -> {
            Platform.runLater(() -> {
                if (loadingIndicator != null) {
                    loadingIndicator.setVisible(false);
                }

                if (throwable != null) {
                    Throwable cause = throwable.getCause() != null ? throwable.getCause() : throwable;
                    if (onError != null) {
                        onError.accept(cause);
                    } else {
                        NotificationUtil.showError("Erreur", cause.getMessage());
                    }
                } else {
                    if (onSuccess != null) {
                        onSuccess.accept(result);
                    }
                }
            });
        });
    }
}
