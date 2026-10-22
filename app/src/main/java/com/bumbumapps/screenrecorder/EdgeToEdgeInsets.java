package com.bumbumapps.screenrecorder;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Pads the activity content so it is not drawn under the system bars (edge-to-edge is enforced on
 * API 35+) and paints the status bar strip with the theme's statusBarColor, which API 35+ ignores.
 */
public final class EdgeToEdgeInsets {
    private EdgeToEdgeInsets() {
    }

    public static void apply(Activity activity) {
        View content = activity.findViewById(android.R.id.content);
        if (content == null) {
            return;
        }
        // API 35+ forces the window's status bar transparent, so read the colour from the theme.
        android.content.res.TypedArray a = activity.getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.statusBarColor});
        final int statusColor = a.getColor(0, 0);
        a.recycle();
        ViewCompat.setOnApplyWindowInsetsListener(content, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            if (android.os.Build.VERSION.SDK_INT >= 35 && (statusColor >>> 24) != 0
                    && (v.getBackground() == null || v.getBackground() instanceof TopBand)) {
                v.setBackground(new TopBand(statusColor, bars.top));
            }
            return WindowInsetsCompat.CONSUMED;
        });
    }

    /** Transparent drawable that only fills the top {@code height} pixels. */
    static final class TopBand extends Drawable {
        private final Paint paint = new Paint();
        private final int height;

        TopBand(int color, int height) {
            paint.setColor(color);
            this.height = height;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            canvas.drawRect(0, 0, getBounds().width(), height, paint);
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }
}
