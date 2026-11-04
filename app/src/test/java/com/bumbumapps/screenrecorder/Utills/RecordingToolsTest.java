package com.bumbumapps.screenrecorder.Utills;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class RecordingToolsTest {

    private static RecordingTools.Item item(String n, long size, long modified, long dur) {
        return new RecordingTools.Item(n, size, modified, dur);
    }

    @Test
    public void formatsDuration() {
        assertEquals("00:00", RecordingTools.formatDuration(-5));
        assertEquals("00:09", RecordingTools.formatDuration(9999));
        assertEquals("01:05", RecordingTools.formatDuration(65_000));
        assertEquals("1:01:01", RecordingTools.formatDuration(3_661_000));
    }

    @Test
    public void clampsRange() {
        assertArrayEquals(new long[]{1000, 5000}, RecordingTools.clampRange(1000, 5000, 10_000));
        assertArrayEquals(new long[]{0, 10_000}, RecordingTools.clampRange(-5, 99_999, 10_000));
        assertArrayEquals(new long[]{2000, 4000}, RecordingTools.clampRange(4000, 2000, 10_000));
        assertArrayEquals(new long[]{3000, 3500}, RecordingTools.clampRange(3000, 3100, 10_000));
        assertArrayEquals(new long[]{9500, 10_000}, RecordingTools.clampRange(9900, 10_000, 10_000));
        assertArrayEquals(new long[]{0, 300}, RecordingTools.clampRange(0, 300, 300));
    }

    @Test
    public void sanitizesFileNames() {
        assertEquals("My clip.mp4", RecordingTools.sanitizeFileName("  My clip  ", "mp4"));
        assertEquals("a_b_c.mp4", RecordingTools.sanitizeFileName("a/b\\c", "mp4"));
        assertEquals("x.mp4", RecordingTools.sanitizeFileName("x.MP4", "mp4"));
        assertEquals("hidden.mp4", RecordingTools.sanitizeFileName("..hidden", "mp4"));
        assertNull(RecordingTools.sanitizeFileName("   ", "mp4"));
        assertNull(RecordingTools.sanitizeFileName("...", "mp4"));
        assertNull(RecordingTools.sanitizeFileName(null, "mp4"));
        assertEquals(84, RecordingTools.sanitizeFileName(new String(new char[200]).replace('\0', 'a'), "mp4").length());
    }

    @Test
    public void makesNamesUnique() {
        HashSet<String> taken = new HashSet<>(Arrays.asList("a.mp4", "a (2).mp4"));
        assertEquals("b.mp4", RecordingTools.uniqueName("b.mp4", taken));
        assertEquals("A (3).mp4", RecordingTools.uniqueName("A.mp4", taken));
    }

    @Test
    public void filtersAndSorts() {
        List<RecordingTools.Item> all = new ArrayList<>(Arrays.asList(
                item("Game.mp4", 300, 10, 5000),
                item("demo.mp4", 100, 30, 9000),
                item("Zoom call.mp4", 200, 20, 1000)));
        assertEquals("demo.mp4", RecordingTools.filterAndSort(all, "", RecordingTools.SORT_NEWEST).get(0).name);
        assertEquals("Game.mp4", RecordingTools.filterAndSort(all, null, RecordingTools.SORT_OLDEST).get(0).name);
        assertEquals("Game.mp4", RecordingTools.filterAndSort(all, "", RecordingTools.SORT_LARGEST).get(0).name);
        assertEquals("demo.mp4", RecordingTools.filterAndSort(all, "", RecordingTools.SORT_LONGEST).get(0).name);
        assertEquals("demo.mp4", RecordingTools.filterAndSort(all, "", RecordingTools.SORT_NAME).get(0).name);
        List<RecordingTools.Item> found = RecordingTools.filterAndSort(all, " ZOOM ", RecordingTools.SORT_NAME);
        assertEquals(1, found.size());
        assertEquals("Zoom call.mp4", found.get(0).name);
        assertTrue(RecordingTools.filterAndSort(all, "nothing", RecordingTools.SORT_NAME).isEmpty());
    }

    @Test
    public void cleanupRules() {
        long now = 100 * RecordingTools.DAY_MS;
        RecordingTools.Item old = item("old", 10, now - 40 * RecordingTools.DAY_MS, 1);
        RecordingTools.Item fresh = item("fresh", 10, now - RecordingTools.DAY_MS, 1);
        RecordingTools.Item big = item("big", 200L * 1024 * 1024, now, 1);
        List<RecordingTools.Item> all = Arrays.asList(old, fresh, big);
        assertEquals(1, RecordingTools.cleanupCandidates(all, RecordingTools.CLEAN_OLDER_7, now).size());
        assertEquals(1, RecordingTools.cleanupCandidates(all, RecordingTools.CLEAN_OLDER_30, now).size());
        assertEquals(0, RecordingTools.cleanupCandidates(all, RecordingTools.CLEAN_OLDER_90, now).size());
        List<RecordingTools.Item> large = RecordingTools.cleanupCandidates(all, RecordingTools.CLEAN_LARGER_100MB, now);
        assertEquals(1, large.size());
        assertEquals("big", large.get(0).name);
        assertFalse(RecordingTools.selectedForCleanup(fresh, 99, now));
    }

    @Test
    public void totalsAndSizes() {
        List<RecordingTools.Item> all = Arrays.asList(item("a", 1024, 0, 1000), item("b", 2048, 0, 2000));
        assertEquals(3072, RecordingTools.totalSize(all));
        assertEquals(3000, RecordingTools.totalDuration(all));
        assertEquals("0 B", RecordingTools.formatSize(0));
        assertEquals("512 B", RecordingTools.formatSize(512));
        assertEquals("1.5 KB", RecordingTools.formatSize(1536));
        assertEquals("2.0 MB", RecordingTools.formatSize(2L * 1024 * 1024));
    }

    @Test
    public void countdownFallsBackToOff() {
        assertEquals(5, RecordingTools.countdownOrOff(5));
        assertEquals(0, RecordingTools.countdownOrOff(7));
    }
}
