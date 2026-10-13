package com.printxpress.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Pure Java & Android SDK Image Loader.
 * Zero third-party dependencies.
 * Features:
 * - In-memory LRU caching
 * - Subsampled decoding to prevent OutOfMemory errors
 * - Background multi-threaded execution
 * - Safe view tagging for RecyclerView recycling
 * - Circular cropping for avatar views
 */
public final class ImageLoader {

    private static final String TAG = "ImageLoader";
    private static final int MAX_REQ_WIDTH = 1024;
    private static final int MAX_REQ_HEIGHT = 1024;

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    // In-memory memory cache (1/8th of available runtime memory)
    private static final LruCache<String, Bitmap> MEMORY_CACHE;

    static {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        MEMORY_CACHE = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    private static final ImageLoader INSTANCE = new ImageLoader();

    public static ImageLoader getInstance() {
        return INSTANCE;
    }

    private ImageLoader() {
        // Utility class
    }

    public void load(String pathOrUri, ImageView imageView) {
        if (imageView != null) {
            load(imageView.getContext(), pathOrUri, imageView, 0);
        }
    }

    public void loadCircleCrop(String pathOrUri, ImageView imageView) {
        if (imageView != null) {
            loadCircle(imageView.getContext(), pathOrUri, imageView, 0);
        }
    }

    /**
     * Load an image resource directly into an ImageView.
     */
    public static void loadRes(ImageView imageView, @DrawableRes int resId) {
        if (imageView == null) return;
        imageView.setTag(null);
        if (resId != 0) {
            imageView.setImageResource(resId);
        }
    }

    /**
     * Load an image from file path or content URI with a placeholder.
     */
    public static void load(Context context, @Nullable String pathOrUri, ImageView imageView, @DrawableRes int placeholderRes) {
        loadInternal(context, pathOrUri, imageView, placeholderRes, false);
    }

    /**
     * Load an image from file path or content URI with circular crop.
     */
    public static void loadCircle(Context context, @Nullable String pathOrUri, ImageView imageView, @DrawableRes int placeholderRes) {
        loadInternal(context, pathOrUri, imageView, placeholderRes, true);
    }

    private static void loadInternal(Context context, @Nullable String pathOrUri, ImageView imageView, @DrawableRes int placeholderRes, boolean circleCrop) {
        if (imageView == null) return;

        if (TextUtils.isEmpty(pathOrUri)) {
            imageView.setTag(null);
            if (placeholderRes != 0) {
                imageView.setImageResource(placeholderRes);
            }
            return;
        }

        final String cacheKey = pathOrUri + (circleCrop ? "_circle" : "");
        Bitmap cached = MEMORY_CACHE.get(cacheKey);
        if (cached != null && !cached.isRecycled()) {
            imageView.setTag(cacheKey);
            imageView.setImageBitmap(cached);
            return;
        }

        // Set placeholder while loading
        if (placeholderRes != 0) {
            imageView.setImageResource(placeholderRes);
        }
        imageView.setTag(cacheKey);

        final Context appContext = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            Bitmap decoded = decodeBitmap(appContext, pathOrUri);
            if (decoded != null && circleCrop) {
                decoded = createCircleBitmap(decoded);
            }

            if (decoded != null) {
                MEMORY_CACHE.put(cacheKey, decoded);
            }

            final Bitmap finalBitmap = decoded;
            MAIN_HANDLER.post(() -> {
                Object currentTag = imageView.getTag();
                if (cacheKey.equals(currentTag)) {
                    if (finalBitmap != null) {
                        imageView.setImageBitmap(finalBitmap);
                    } else if (placeholderRes != 0) {
                        imageView.setImageResource(placeholderRes);
                    }
                }
            });
        });
    }

    @Nullable
    private static Bitmap decodeBitmap(Context context, String pathOrUri) {
        try {
            // First pass: inspect dimensions
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            InputStream isForBounds = openInputStream(context, pathOrUri);
            if (isForBounds == null) return null;
            BitmapFactory.decodeStream(isForBounds, null, options);
            try { isForBounds.close(); } catch (Exception ignored) {}

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                return null;
            }

            // Second pass: subsample and decode
            options.inSampleSize = calculateInSampleSize(options, MAX_REQ_WIDTH, MAX_REQ_HEIGHT);
            options.inJustDecodeBounds = false;

            InputStream isForData = openInputStream(context, pathOrUri);
            if (isForData == null) return null;
            Bitmap bitmap = BitmapFactory.decodeStream(isForData, null, options);
            try { isForData.close(); } catch (Exception ignored) {}

            return bitmap;
        } catch (Throwable t) {
            Log.e(TAG, "Error decoding bitmap from: " + pathOrUri, t);
            return null;
        }
    }

    @Nullable
    private static InputStream openInputStream(Context context, String pathOrUri) {
        try {
            if (pathOrUri.startsWith("content://") || pathOrUri.startsWith("file://")) {
                return context.getContentResolver().openInputStream(Uri.parse(pathOrUri));
            } else {
                File file = new File(pathOrUri);
                if (file.exists()) {
                    return new FileInputStream(file);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not open stream for: " + pathOrUri, e);
        }
        return null;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap createCircleBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;
        try {
            int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
            Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);

            final Rect rect = new Rect(
                    (bitmap.getWidth() - size) / 2,
                    (bitmap.getHeight() - size) / 2,
                    (bitmap.getWidth() + size) / 2,
                    (bitmap.getHeight() + size) / 2
            );
            final Rect dstRect = new Rect(0, 0, size, size);

            canvas.drawARGB(0, 0, 0, 0);
            canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, dstRect, paint);

            return output;
        } catch (Throwable t) {
            Log.e(TAG, "Error creating circular bitmap", t);
            return bitmap;
        }
    }
}
