package com.printxpress.ui.upload;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

public class ArtworkValidator {
    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024; // 20 MB
    private static final long WARN_FILE_SIZE = 10L * 1024 * 1024; // 10 MB

    public static class ValidationResult {
        public boolean isValid;
        public String message;
        public boolean isWarning;

        public ValidationResult(boolean isValid, String message, boolean isWarning) {
            this.isValid = isValid;
            this.message = message;
            this.isWarning = isWarning;
        }
    }

    public static ValidationResult validateFile(Context context, Uri uri) {
        if (uri == null) {
            return new ValidationResult(false, "No file selected", false);
        }

        String mimeType = context.getContentResolver().getType(uri);
        String path = uri.getPath();
        if (path == null) path = "";

        boolean isAllowed = (mimeType != null && (mimeType.startsWith("image/") || mimeType.equals("application/pdf")))
                || path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png") || path.endsWith(".pdf") || path.endsWith(".webp");

        if (!isAllowed) {
            return new ValidationResult(false, "Invalid file format. Please upload JPG, PNG, WEBP or PDF.", false);
        }

        long size = getFileSize(context, uri);
        if (size > MAX_FILE_SIZE) {
            return new ValidationResult(false, "File is too large. Maximum size is 20 MB.", false);
        }
        if (size > WARN_FILE_SIZE) {
            return new ValidationResult(true, "Warning: File size exceeds 10 MB, upload may take longer.", true);
        }

        return new ValidationResult(true, "File is valid and ready.", false);
    }

    private static long getFileSize(Context context, Uri uri) {
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    return cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception ignored) {}
        return 0;
    }
}
