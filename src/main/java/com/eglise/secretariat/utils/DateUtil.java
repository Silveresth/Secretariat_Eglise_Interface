package com.eglise.secretariat.utils;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtil {

    public static final DateTimeFormatter FRENCH_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);
    public static final DateTimeFormatter FRENCH_SHORT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH);

    public static String formatFrench(LocalDate date) {
        if (date == null) return "-";
        return date.format(FRENCH_DATE_FORMAT);
    }

    public static String formatShort(LocalDate date) {
        if (date == null) return "-";
        return date.format(FRENCH_SHORT_DATE_FORMAT);
    }

    public static String formatWithAge(LocalDate birthDate) {
        if (birthDate == null) return "-";
        int age = calculateAge(birthDate);
        return formatFrench(birthDate) + " (" + age + " ans)";
    }

    public static int calculateAge(LocalDate birthDate) {
        if (birthDate == null) return 0;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}
