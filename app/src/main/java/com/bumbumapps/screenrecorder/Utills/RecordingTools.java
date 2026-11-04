package com.bumbumapps.screenrecorder.Utills;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Pure helpers for the recordings library and the video editor. No Android classes, unit tested. */
public final class RecordingTools {

    private RecordingTools() {
    }

    public static final long DAY_MS = 24L * 60 * 60 * 1000;
    /** Shortest clip a trim may produce. */
    public static final long MIN_CLIP_MS = 500;

    public static final int SORT_NEWEST = 0;
    public static final int SORT_OLDEST = 1;
    public static final int SORT_LARGEST = 2;
    public static final int SORT_LONGEST = 3;
    public static final int SORT_NAME = 4;

    public static final int CLEAN_OLDER_7 = 0;
    public static final int CLEAN_OLDER_30 = 1;
    public static final int CLEAN_OLDER_90 = 2;
    public static final int CLEAN_LARGER_100MB = 3;

    public static final int[] COUNTDOWN_OPTIONS = {0, 3, 5, 10};

    /** Light description of one recording; the UI fills it from the file and MediaMetadataRetriever. */
    public static final class Item {
        public final String name;
        public final long size;
        public final long modified;
        public final long durationMs;

        public Item(String name, long size, long modified, long durationMs) {
            this.name = name;
            this.size = size;
            this.modified = modified;
            this.durationMs = durationMs;
        }
    }

    public static String formatDuration(long ms) {
        long total = Math.max(0, ms) / 1000;
        long h = total / 3600;
        long m = (total % 3600) / 60;
        long s = total % 60;
        if (h > 0) {
            return String.format(Locale.US, "%d:%02d:%02d", h, m, s);
        }
        return String.format(Locale.US, "%02d:%02d", m, s);
    }

    /** Keeps start and end inside the video and at least MIN_CLIP_MS apart. Returns {start, end}. */
    public static long[] clampRange(long start, long end, long durationMs) {
        long dur = Math.max(durationMs, 0);
        long s = Math.min(Math.max(start, 0), dur);
        long e = Math.min(Math.max(end, 0), dur);
        if (s > e) {
            long t = s;
            s = e;
            e = t;
        }
        if (e - s < MIN_CLIP_MS) {
            if (dur <= MIN_CLIP_MS) {
                return new long[]{0, dur};
            }
            if (s + MIN_CLIP_MS <= dur) {
                e = s + MIN_CLIP_MS;
            } else {
                e = dur;
                s = dur - MIN_CLIP_MS;
            }
        }
        return new long[]{s, e};
    }

    /** Turns text typed by the user into a safe .mp4 file name, or null when nothing usable is left. */
    public static String sanitizeFileName(String input, String extension) {
        if (input == null) return null;
        String base = input.trim();
        String ext = "." + extension;
        if (base.toLowerCase(Locale.ROOT).endsWith(ext)) {
            base = base.substring(0, base.length() - ext.length());
        }
        base = base.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").trim();
        while (base.startsWith(".")) {
            base = base.substring(1);
        }
        base = base.trim();
        if (base.isEmpty()) return null;
        if (base.length() > 80) base = base.substring(0, 80).trim();
        return base + ext;
    }

    /** name.ext, or name (2).ext when taken. Comparison ignores case. */
    public static String uniqueName(String wanted, Set<String> existingLowerCase) {
        if (!existingLowerCase.contains(wanted.toLowerCase(Locale.ROOT))) return wanted;
        int dot = wanted.lastIndexOf('.');
        String base = dot > 0 ? wanted.substring(0, dot) : wanted;
        String ext = dot > 0 ? wanted.substring(dot) : "";
        for (int i = 2; i < 10000; i++) {
            String candidate = base + " (" + i + ")" + ext;
            if (!existingLowerCase.contains(candidate.toLowerCase(Locale.ROOT))) return candidate;
        }
        return wanted;
    }

    public static boolean matches(Item item, String query) {
        if (query == null) return true;
        String q = query.trim().toLowerCase(Locale.ROOT);
        return q.isEmpty() || item.name.toLowerCase(Locale.ROOT).contains(q);
    }

    public static Comparator<Item> comparator(int sort) {
        switch (sort) {
            case SORT_OLDEST:
                return (a, b) -> Long.compare(a.modified, b.modified);
            case SORT_LARGEST:
                return (a, b) -> Long.compare(b.size, a.size);
            case SORT_LONGEST:
                return (a, b) -> Long.compare(b.durationMs, a.durationMs);
            case SORT_NAME:
                return (a, b) -> a.name.compareToIgnoreCase(b.name);
            case SORT_NEWEST:
            default:
                return (a, b) -> Long.compare(b.modified, a.modified);
        }
    }

    public static List<Item> filterAndSort(List<Item> all, String query, int sort) {
        List<Item> out = new ArrayList<>();
        for (Item i : all) {
            if (matches(i, query)) out.add(i);
        }
        Collections.sort(out, comparator(sort));
        return out;
    }

    public static boolean selectedForCleanup(Item item, int rule, long nowMs) {
        switch (rule) {
            case CLEAN_OLDER_7:
                return nowMs - item.modified > 7 * DAY_MS;
            case CLEAN_OLDER_30:
                return nowMs - item.modified > 30 * DAY_MS;
            case CLEAN_OLDER_90:
                return nowMs - item.modified > 90 * DAY_MS;
            case CLEAN_LARGER_100MB:
                return item.size > 100L * 1024 * 1024;
            default:
                return false;
        }
    }

    public static List<Item> cleanupCandidates(List<Item> all, int rule, long nowMs) {
        List<Item> out = new ArrayList<>();
        for (Item i : all) {
            if (selectedForCleanup(i, rule, nowMs)) out.add(i);
        }
        return out;
    }

    public static long totalSize(List<Item> items) {
        long sum = 0;
        for (Item i : items) sum += i.size;
        return sum;
    }

    public static long totalDuration(List<Item> items) {
        long sum = 0;
        for (Item i : items) sum += i.durationMs;
        return sum;
    }

    public static String formatSize(long bytes) {
        if (bytes <= 0) return "0 B";
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int g = (int) (Math.log10(bytes) / Math.log10(1024));
        g = Math.min(g, units.length - 1);
        double v = bytes / Math.pow(1024, g);
        return (g == 0 ? String.format(Locale.US, "%d", bytes) : String.format(Locale.US, "%.1f", v)) + " " + units[g];
    }

    /** Position of a stored countdown value in COUNTDOWN_OPTIONS; unknown values fall back to Off. */
    public static int countdownOrOff(int seconds) {
        for (int o : COUNTDOWN_OPTIONS) {
            if (o == seconds) return o;
        }
        return 0;
    }
}
