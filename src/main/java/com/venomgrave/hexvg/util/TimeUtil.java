package com.venomgrave.hexvg.util;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TimeUtil {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    // Kolejność ważna — od największej do najmniejszej
    private static final Map<String, Long> UNITS = new LinkedHashMap<>();

    static {
        UNITS.put("y",  365 * 24 * 60 * 60L);  // rok
        UNITS.put("mo", 30  * 24 * 60 * 60L);  // miesiąc
        UNITS.put("w",  7   * 24 * 60 * 60L);  // tydzień
        UNITS.put("d",  24  * 60 * 60L);        // dzień
        UNITS.put("h",  60  * 60L);             // godzina
        UNITS.put("m",  60L);                   // minuta
        UNITS.put("s",  1L);                    // sekunda
    }

    // Polskie nazwy jednostek
    private static final Map<String, String[]> PL_UNITS = new LinkedHashMap<>();

    static {
        // [singular, plural2-4, plural5+]
        PL_UNITS.put("y",  new String[]{"rok",     "lata",    "lat"});
        PL_UNITS.put("mo", new String[]{"miesiąc", "miesiące","miesięcy"});
        PL_UNITS.put("w",  new String[]{"tydzień", "tygodnie","tygodni"});
        PL_UNITS.put("d",  new String[]{"dzień",   "dni",     "dni"});
        PL_UNITS.put("h",  new String[]{"godzinę", "godziny", "godzin"});
        PL_UNITS.put("m",  new String[]{"minutę",  "minuty",  "minut"});
        PL_UNITS.put("s",  new String[]{"sekundę", "sekundy", "sekund"});
    }

    private TimeUtil() {}

    // ── Parsowanie ────────────────────────────────────────────────────────

    /**
     * Parsuje string czasu → sekundy.
     * Obsługuje formaty: "5s", "10m", "2h", "7d", "2w", "1mo", "1y"
     * Można łączyć: "1h30m", "2d12h"
     *
     * @throws IllegalArgumentException jeśli format jest nieprawidłowy
     */
    public static long parseSeconds(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Pusty string czasu.");
        }

        String s = input.trim().toLowerCase();
        long totalSeconds = 0;
        int i = 0;

        while (i < s.length()) {
            // Odczytaj liczbę
            int numStart = i;
            while (i < s.length() && Character.isDigit(s.charAt(i))) i++;

            if (i == numStart) {
                throw new IllegalArgumentException(
                        "Oczekiwano liczby w: '" + input + "' na pozycji " + i);
            }

            long value;
            try {
                value = Long.parseLong(s.substring(numStart, i));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Nieprawidłowa liczba w: '" + input + "'");
            }

            // Odczytaj jednostkę (max 2 znaki: "mo")
            int unitStart = i;
            while (i < s.length() && Character.isLetter(s.charAt(i))) i++;

            String unit = s.substring(unitStart, i);
            Long multiplier = UNITS.get(unit);

            if (multiplier == null) {
                throw new IllegalArgumentException(
                        "Nieznana jednostka: '" + unit + "'. "
                                + "Dozwolone: s, m, h, d, w, mo, y");
            }

            totalSeconds += value * multiplier;
        }

        if (totalSeconds <= 0) {
            throw new IllegalArgumentException("Czas musi być większy od 0.");
        }

        return totalSeconds;
    }

    /**
     * Parsuje bezpiecznie — zwraca -1 zamiast rzucać wyjątek.
     */
    public static long parseSecondsSafe(String input) {
        try {
            return parseSeconds(input);
        } catch (IllegalArgumentException e) {
            return -1;
        }
    }

    // ── Formatowanie ──────────────────────────────────────────────────────

    /**
     * Formatuje sekundy → czytelny string w języku polskim.
     * Przykład: 3661 → "1 godzinę 1 minutę 1 sekundę"
     *
     * @param maxUnits ile maksymalnie jednostek wyświetlić (1-7)
     */
    public static String formatPl(long seconds, int maxUnits) {
        if (seconds <= 0) return "0 sekund";

        StringBuilder sb = new StringBuilder();
        int units = 0;
        long remaining = seconds;

        for (Map.Entry<String, Long> entry : UNITS.entrySet()) {
            if (units >= maxUnits) break;

            String key  = entry.getKey();
            long   unit = entry.getValue();
            long   val  = remaining / unit;

            if (val > 0) {
                remaining -= val * unit;
                if (sb.length() > 0) sb.append(" ");
                sb.append(val).append(" ").append(pluralPl(key, val));
                units++;
            }
        }

        return sb.length() > 0 ? sb.toString() : "0 sekund";
    }

    /**
     * Formatuje sekundy → short string.
     * Przykład: 3661 → "1h 1m 1s"
     */
    public static String formatShort(long seconds) {
        if (seconds <= 0) return "0s";

        StringBuilder sb = new StringBuilder();
        long remaining = seconds;

        for (Map.Entry<String, Long> entry : UNITS.entrySet()) {
            long val = remaining / entry.getValue();
            if (val > 0) {
                remaining -= val * entry.getValue();
                if (sb.length() > 0) sb.append(" ");
                sb.append(val).append(entry.getKey());
            }
        }

        return sb.length() > 0 ? sb.toString() : "0s";
    }

    /**
     * Skrót — formatuje z max 2 jednostkami.
     * Przykład: 3661 → "1 godzinę 1 minutę"
     */
    public static String formatPl(long seconds) {
        return formatPl(seconds, 2);
    }

    /**
     * Formatuje Instant → string "dd.MM.yyyy HH:mm:ss" w lokalnej strefie.
     */
    public static String formatDate(Instant instant) {
        if (instant == null) return "nigdy";
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return ldt.format(FORMATTER);
    }

    /**
     * Formatuje Instant jako "X czasu temu".
     */
    public static String timeAgo(Instant instant) {
        if (instant == null) return "nigdy";
        long seconds = Duration.between(instant, Instant.now()).getSeconds();
        if (seconds < 60) return "przed chwilą";
        return formatPl(seconds, 1) + " temu";
    }

    /**
     * Zwraca Instant za podaną liczbę sekund od teraz.
     */
    public static Instant fromNow(long seconds) {
        return Instant.now().plusSeconds(seconds);
    }

    /**
     * Sprawdza czy Instant już minął.
     */
    public static boolean hasPassed(Instant instant) {
        return instant != null && Instant.now().isAfter(instant);
    }

    /**
     * Sprawdza czy Instant jeszcze nie minął (kara aktywna).
     */
    public static boolean isActive(Instant expiresAt) {
        return expiresAt == null || !hasPassed(expiresAt);
    }

    // ── Prywatne ─────────────────────────────────────────────────────────

    private static String pluralPl(String unit, long value) {
        String[] forms = PL_UNITS.get(unit);
        if (forms == null) return unit;

        long abs = Math.abs(value);
        if (abs == 1) return forms[0];
        if (abs % 10 >= 2 && abs % 10 <= 4
                && (abs % 100 < 10 || abs % 100 >= 20)) return forms[1];
        return forms[2];
    }
}