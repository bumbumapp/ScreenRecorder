package com.bumbumapps.screenrecorder.Utills;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * Cuts a recording without re-encoding. Video can only start on a key frame, so the cut starts at the
 * key frame at or before the chosen start; audio follows the same start time.
 */
public final class VideoCutter {

    private VideoCutter() {
    }

    public static class CutException extends Exception {
        public CutException(String message) {
            super(message);
        }

        public CutException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /** Writes [startUs, endUs] of in to out (mp4). All tracks are kept. */
    public static void trim(File in, File out, long startUs, long endUs) throws CutException {
        copy(in, out, startUs, endUs, false);
    }

    /** Writes the audio track of in to out (m4a). Fails with CutException when there is none. */
    public static void extractAudio(File in, File out) throws CutException {
        copy(in, out, 0, Long.MAX_VALUE, true);
    }

    public static boolean hasAudio(File in) {
        MediaExtractor ex = new MediaExtractor();
        try {
            ex.setDataSource(in.getAbsolutePath());
            for (int i = 0; i < ex.getTrackCount(); i++) {
                String mime = ex.getTrackFormat(i).getString(MediaFormat.KEY_MIME);
                if (mime != null && mime.startsWith("audio/")) return true;
            }
            return false;
        } catch (IOException | RuntimeException e) {
            return false;
        } finally {
            ex.release();
        }
    }

    private static void copy(File in, File out, long startUs, long endUs, boolean audioOnly) throws CutException {
        MediaExtractor extractor = new MediaExtractor();
        MediaMuxer muxer = null;
        boolean started = false;
        try {
            extractor.setDataSource(in.getAbsolutePath());
            muxer = new MediaMuxer(out.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            int rotation = 0;
            MediaMetadataRetriever r = new MediaMetadataRetriever();
            try {
                r.setDataSource(in.getAbsolutePath());
                String rot = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
                if (rot != null) rotation = Integer.parseInt(rot);
            } catch (RuntimeException ignored) {
            } finally {
                try {
                    r.release();
                } catch (Exception ignored) {
                }
            }
            if (!audioOnly && rotation != 0) muxer.setOrientationHint(rotation);

            HashMap<Integer, Integer> map = new HashMap<>();
            int bufferSize = 256 * 1024;
            int videoTrack = -1;
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat f = extractor.getTrackFormat(i);
                String mime = f.getString(MediaFormat.KEY_MIME);
                if (mime == null) continue;
                boolean isAudio = mime.startsWith("audio/");
                boolean isVideo = mime.startsWith("video/");
                if (audioOnly && !isAudio) continue;
                if (!isAudio && !isVideo) continue;
                if (audioOnly && !map.isEmpty()) continue;
                map.put(i, muxer.addTrack(f));
                if (isVideo) videoTrack = i;
                if (f.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                    bufferSize = Math.max(bufferSize, f.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE));
                }
            }
            if (map.isEmpty()) {
                throw new CutException(audioOnly ? "no audio" : "no tracks");
            }
            for (int t : map.keySet()) extractor.selectTrack(t);

            // Start at the key frame of the video track; audio and video share one origin.
            long originUs = startUs;
            if (videoTrack >= 0 && startUs > 0) {
                extractor.seekTo(startUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
                long t = extractor.getSampleTime();
                if (t >= 0) originUs = Math.min(t, startUs);
            }
            extractor.seekTo(originUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);

            muxer.start();
            started = true;
            ByteBuffer buf = ByteBuffer.allocate(bufferSize);
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            long written = 0;
            while (true) {
                int track = extractor.getSampleTrackIndex();
                if (track < 0) break;
                long time = extractor.getSampleTime();
                if (time > endUs) break;
                Integer outTrack = map.get(track);
                if (outTrack != null && time >= originUs) {
                    buf.clear();
                    int size = extractor.readSampleData(buf, 0);
                    if (size < 0) break;
                    info.offset = 0;
                    info.size = size;
                    info.presentationTimeUs = time - originUs;
                    info.flags = (extractor.getSampleFlags() & MediaExtractor.SAMPLE_FLAG_SYNC) != 0
                            ? MediaCodec.BUFFER_FLAG_KEY_FRAME : 0;
                    muxer.writeSampleData(outTrack, buf, info);
                    written++;
                }
                if (!extractor.advance()) break;
            }
            if (written == 0) throw new CutException("nothing written");
        } catch (CutException e) {
            cleanup(out);
            throw e;
        } catch (IOException | RuntimeException e) {
            cleanup(out);
            throw new CutException("cut failed", e);
        } finally {
            extractor.release();
            if (muxer != null) {
                try {
                    if (started) muxer.stop();
                } catch (RuntimeException ignored) {
                }
                try {
                    muxer.release();
                } catch (RuntimeException ignored) {
                }
            }
        }
    }

    private static void cleanup(File f) {
        //noinspection ResultOfMethodCallIgnored
        f.delete();
    }
}
