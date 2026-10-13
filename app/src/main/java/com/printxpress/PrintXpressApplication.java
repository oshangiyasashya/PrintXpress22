package com.printxpress;

import android.app.Application;
import android.util.Log;
import com.printxpress.util.NotificationHelper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PrintXpressApplication extends Application {

    private static final String TAG = "PrintXpressApp";

    @Override
    public void onCreate() {
        super.onCreate();

        // 1. Initialize native notification channels
        NotificationHelper.createNotificationChannels(this);

        // 2. Schedule background workers with WorkManager
        scheduleBackgroundWorkers();

        // 3. Global Uncaught Exception Handler
        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                Log.e(TAG, "Uncaught exception on thread: " + thread.getName(), throwable);
                saveCrashLog(thread, throwable);
            } catch (Throwable ignored) {
            } finally {
                if (defaultHandler != null) {
                    defaultHandler.uncaughtException(thread, throwable);
                }
            }
        });
    }

    private void scheduleBackgroundWorkers() {
        try {
            androidx.work.WorkManager wm = androidx.work.WorkManager.getInstance(this);

            androidx.work.PeriodicWorkRequest notifSync = new androidx.work.PeriodicWorkRequest.Builder(
                    com.printxpress.worker.NotificationSyncWorker.class,
                    15, java.util.concurrent.TimeUnit.MINUTES
            ).build();

            androidx.work.PeriodicWorkRequest dbBackup = new androidx.work.PeriodicWorkRequest.Builder(
                    com.printxpress.worker.DatabaseBackupWorker.class,
                    24, java.util.concurrent.TimeUnit.HOURS
            ).build();

            wm.enqueueUniquePeriodicWork("px_notif_sync", androidx.work.ExistingPeriodicWorkPolicy.KEEP, notifSync);
            wm.enqueueUniquePeriodicWork("px_db_backup", androidx.work.ExistingPeriodicWorkPolicy.KEEP, dbBackup);
        } catch (Exception e) {
            Log.w(TAG, "Failed to schedule background workers", e);
        }
    }

    private void saveCrashLog(Thread thread, Throwable throwable) {
        try {
            File crashDir = new File(getFilesDir(), "crash_reports");
            if (!crashDir.exists()) {
                crashDir.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            File crashFile = new File(crashDir, "crash_" + timestamp + ".log");

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            String stackTrace = sw.toString();

            String report = "Timestamp: " + new Date().toString() + "\n"
                    + "Thread: " + thread.getName() + " (ID: " + thread.getId() + ")\n"
                    + "Exception: " + throwable.getClass().getName() + "\n"
                    + "Message: " + throwable.getMessage() + "\n\n"
                    + "Stack Trace:\n" + stackTrace;

            try (FileOutputStream fos = new FileOutputStream(crashFile)) {
                fos.write(report.getBytes());
                fos.flush();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to save crash log to file", e);
        }
    }
}
