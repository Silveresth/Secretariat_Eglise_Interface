package com.eglise.secretariat.config;

public class AppConfig {
    public static final String APP_TITLE = "Secrétariat Église - Assemblées de Dieu";
    public static final String APP_VERSION = "1.0.0";
    
    // Default Backend Spring Boot API URL
    private static String apiBaseUrl = "http://localhost:8081";
    
    // HTTP Timeouts in seconds
    public static final int CONNECT_TIMEOUT_SECONDS = 10;
    public static final int REQUEST_TIMEOUT_SECONDS = 30;

    // Official Church Info (Assemblées de Dieu du Togo)
    private static String churchName = "EGLISE DES ASSEMBLEES DE DIEU DU TOGO";
    private static String templeName = "TEMPLE DIEU NE CHANGE PAS";
    private static String churchAddress = "04 BP: 27 Lomé 04-Togo";
    private static String churchPhone = "0028 90 14 68 87";
    private static String churchEmail = "contact@ad-dieunechangepas.tg";
    private static String churchRegNumber = "TG-LOM-04-AD";

    public static String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public static void setApiBaseUrl(String url) {
        if (url != null && !url.trim().isEmpty()) {
            apiBaseUrl = url.trim().replaceAll("/+$", "");
        }
    }

    public static String getChurchName() {
        return churchName;
    }

    public static void setChurchName(String name) {
        churchName = name;
    }

    public static String getTempleName() {
        return templeName;
    }

    public static void setTempleName(String name) {
        templeName = name;
    }

    public static String getChurchAddress() {
        return churchAddress;
    }

    public static void setChurchAddress(String address) {
        churchAddress = address;
    }

    public static String getChurchPhone() {
        return churchPhone;
    }

    public static void setChurchPhone(String phone) {
        churchPhone = phone;
    }

    public static String getChurchEmail() {
        return churchEmail;
    }

    public static void setChurchEmail(String email) {
        churchEmail = email;
    }

    public static String getChurchRegNumber() {
        return churchRegNumber;
    }

    public static void setChurchRegNumber(String regNumber) {
        churchRegNumber = regNumber;
    }
}
