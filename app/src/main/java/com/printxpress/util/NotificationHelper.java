package com.printxpress.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.printxpress.MainActivity;
import com.printxpress.R;

/**
 * Native Notification Manager for PrintXpress.
 * Zero third-party dependencies.
 */
public final class NotificationHelper {

    public static final String CHANNEL_ORDERS = "channel_orders";
    public static final String CHANNEL_PROMOS = "channel_promos";
    public static final String CHANNEL_SUPPORT = "channel_support";

    private NotificationHelper() {}

    /**
     * Creates system notification channels required for Android 8.0+ (API 26+)
     */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            if (nm == null) return;

            NotificationChannel ordersChannel = new NotificationChannel(
                    CHANNEL_ORDERS,
                    "Order Updates",
                    NotificationManager.IMPORTANCE_HIGH
            );
            ordersChannel.setDescription("Notifications about your print order lifecycle and delivery status");

            NotificationChannel promosChannel = new NotificationChannel(
                    CHANNEL_PROMOS,
                    "Promotions & Discounts",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            promosChannel.setDescription("Special discounts, seasonal offers, and coupon alerts");

            NotificationChannel supportChannel = new NotificationChannel(
                    CHANNEL_SUPPORT,
                    "Support Messages",
                    NotificationManager.IMPORTANCE_HIGH
            );
            supportChannel.setDescription("Replies from customer service and print specialists");

            nm.createNotificationChannel(ordersChannel);
            nm.createNotificationChannel(promosChannel);
            nm.createNotificationChannel(supportChannel);
        }
    }

    /**
     * Dispatch an Order notification to the system tray.
     */
    public static void showOrderNotification(Context context, long orderId, String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("navigateTo", "order");
        intent.putExtra("orderId", orderId);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) (orderId % Integer.MAX_VALUE),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ORDERS)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        try {
            NotificationManagerCompat.from(context).notify((int) (orderId % 100000), builder.build());
        } catch (SecurityException ignored) {
            // Android 13+ permission may not have been granted yet
        }
    }

    /**
     * Dispatch a promotional notification to the system tray.
     */
    public static void showPromoNotification(Context context, int notificationId, String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("navigateTo", "catalog");
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_PROMOS)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build());
        } catch (SecurityException ignored) {}
    }
}
