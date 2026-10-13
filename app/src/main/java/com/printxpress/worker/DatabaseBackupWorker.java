package com.printxpress.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;

/**
 * Periodically backs up local SQLite database file to filesDir/backups/
 */
public class DatabaseBackupWorker extends Worker {

    public DatabaseBackupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        try {
            File dbFile = context.getDatabasePath("printxpress_database");
            if (!dbFile.exists()) {
                return Result.success();
            }

            File backupDir = new File(context.getFilesDir(), "backups");
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            File backupFile = new File(backupDir, "backup_" + System.currentTimeMillis() + ".db");
            try (FileInputStream in = new FileInputStream(dbFile);
                 FileOutputStream out = new FileOutputStream(backupFile)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
            }

            // Keep only latest 5 backups
            File[] files = backupDir.listFiles();
            if (files != null && files.length > 5) {
                Arrays.sort(files, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
                for (int i = 0; i < files.length - 5; i++) {
                    files[i].delete();
                }
            }

            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}
