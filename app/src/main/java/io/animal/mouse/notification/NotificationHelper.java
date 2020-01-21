package io.animal.mouse.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import io.animal.mouse.R;

/**
 * Notification Helper Class.
 */
public class NotificationHelper extends ContextWrapper {

    public static final String ROOT_CHANNEL_ID = "io.animal.mouse.root";
    public static final String ROOT_CHANNEL = "io.animal.mouse.root";

    public static final String START_CHANNEL_ID = "io.animal.mouse.start";
    public static final String START_CHANNEL = "io.animal.mouse.start";

    public static final String END_CHANNEL_ID = "io.animal.mouse.end";
    public static final String END_CHANNEL = "io.animal.mouse.end";

    private NotificationManager manager;

    public NotificationHelper(Context ctx) {
        super(ctx);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan0 = new NotificationChannel(ROOT_CHANNEL_ID,
                    ROOT_CHANNEL, NotificationManager.IMPORTANCE_LOW);
            getManager().createNotificationChannel(chan0);

            NotificationChannel chan1 = new NotificationChannel(START_CHANNEL_ID,
                    START_CHANNEL, NotificationManager.IMPORTANCE_DEFAULT);
            chan1.setShowBadge(true);
            chan1.setSound(null, null);
            getManager().createNotificationChannel(chan1);

            NotificationChannel chan2 = new NotificationChannel(END_CHANNEL_ID,
                    END_CHANNEL, NotificationManager.IMPORTANCE_HIGH);
            chan2.setSound(null, null);
            getManager().createNotificationChannel(chan2);
        }
    }

    /**
     * Get a notification for foreground
     *
     * @return the builder
     */
    public NotificationCompat.Builder getForegroundNotification() {
        return new NotificationCompat.Builder(getApplicationContext(), ROOT_CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("");
    }

    /**
     * Get a notification of type countdown start.
     *
     * Provide the builder rather than the notification it's self as useful for making notification
     * changes.
     *
     * @param title the title of the notification
     * @param body the body text for the notification
     * @return the builder as it keeps a reference to the notification (since API 24)
     */
    public NotificationCompat.Builder getStartNotification(String title, String body, PendingIntent intent) {
        return new NotificationCompat.Builder(getApplicationContext(), START_CHANNEL)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(getSmallIcon())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(intent);
    }

    /**
     * Build notification for countdown end.
     *
     * @param title Title for notification.
     * @param body Message for notification.
     * @return A Notification.Builder configured with the selected channel and details
     */
    public NotificationCompat.Builder getEndNotification(String title, String body, PendingIntent intent, PendingIntent stopIntent) {
        return new NotificationCompat.Builder(getApplicationContext(), END_CHANNEL)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(getSmallIcon())
                .setContentIntent((intent))
                .addAction(R.drawable.ic_alarm_active_24px, getString(R.string.end_notification_name), stopIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
    }

    /**
     * Send a notification.
     *
     * @param id The ID of the notification
     * @param notification The notification object
     */
    public void notify(int id, NotificationCompat.Builder notification) {
        getManager().notify(id, notification.build());
    }

    /**
     * Request Canceling notification.
     *
     * @param id Notification Id for cancel
     */
    public void cancel(int id) {
        getManager().cancel(id);
    }

    /**
     * Get the small icon for this app
     *
     * @return The small icon resource id
     */
    private int getSmallIcon() {
        return R.drawable.ic_notification;
    }

    /**
     * Get the notification manager.
     *
     * Utility method as this helper works with it a lot.
     *
     * @return The system service NotificationManager
     */
    private NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }
}
