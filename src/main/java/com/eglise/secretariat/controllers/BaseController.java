package com.eglise.secretariat.controllers;

import com.eglise.secretariat.utils.AsyncHelper;
import com.eglise.secretariat.utils.DialogUtil;
import com.eglise.secretariat.utils.NavigationService;
import com.eglise.secretariat.utils.NotificationUtil;
import javafx.scene.Node;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class BaseController {

    protected final NavigationService navigationService = NavigationService.getInstance();

    protected <T> void executeAsync(
            CompletableFuture<T> future,
            Node loadingIndicator,
            Consumer<T> onSuccess
    ) {
        AsyncHelper.execute(future, loadingIndicator, onSuccess, throwable -> {
            NotificationUtil.showError("Erreur d'opération", throwable.getMessage());
        });
    }

    protected <T> void executeAsync(
            CompletableFuture<T> future,
            Node loadingIndicator,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError
    ) {
        AsyncHelper.execute(future, loadingIndicator, onSuccess, onError);
    }
}
