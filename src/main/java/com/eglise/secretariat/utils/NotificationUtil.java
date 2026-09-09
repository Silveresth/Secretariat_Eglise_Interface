package com.eglise.secretariat.utils;

public class NotificationUtil {

    public enum ToastType {
        SUCCESS("#10b981", "✓", "Succès"),
        INFO("#38bdf8", "ℹ", "Information"),
        WARNING("#fbbf24", "⚠", "Attention"),
        ERROR("#f87171", "✕", "Erreur");

        final String iconColor;
        final String icon;
        final String defaultTitle;

        ToastType(String iconColor, String icon, String defaultTitle) {
            this.iconColor = iconColor;
            this.icon = icon;
            this.defaultTitle = defaultTitle;
        }
    }

    public static void showSuccess(String title, String message) {
        show(ToastType.SUCCESS, title, message, 3500);
    }

    public static void showInfo(String title, String message) {
        show(ToastType.INFO, title, message, 3000);
    }

    public static void showWarning(String title, String message) {
        show(ToastType.WARNING, title, message, 4500);
    }

    public static void showError(String title, String message) {
        show(ToastType.ERROR, title, message, 5500);
    }

    private static void show(ToastType type, String title, String message, int durationMs) {
        // Floating dark popup toasts disabled completely per user request
        System.out.println("[" + type + "] " + title + ": " + message);
    }
}
