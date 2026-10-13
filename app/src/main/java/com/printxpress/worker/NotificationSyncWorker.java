package com.printxpress.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.printxpress.data.db.AppDatabase;
import com.printxpress.data.entity.Notification;
import com.printxpress.util.NotificationHelper;
import com.printxpress.util.SessionManager;
import java.util.List;

/**
 * Syncs un-pushed Room notifications to Android system tray.
 */
public class NotificationSyncWorker extends Worker {

    public NotificationSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SessionManager session = new SessionManager(context);
        long userId = session.getUserId();
        if (userId <= 0) {
            return Result.success();
        }

        try {
            AppDatabase db = AppDatabase.getDatabase(context);
            List<Notification> undelivered = db.commonDao().getUndeliveredNotificationsSync(userId);
            if (undelivered != null && !undelivered.isEmpty()) {
                for (Notification n : undelivered) {
                    NotificationHelper.showOrderNotification(context, n.id, n.title, n.message);
                }
                db.commonDao().markNotificationsDeliveredSync(userId);
            }
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}
