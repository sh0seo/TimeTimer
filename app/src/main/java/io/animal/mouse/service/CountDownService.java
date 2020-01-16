package io.animal.mouse.service;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompatExtras;

import org.greenrobot.eventbus.EventBus;

import io.animal.mouse.MainActivity;
import io.animal.mouse.R;
import io.animal.mouse.TimerStatus;
import io.animal.mouse.events.CountdownFinishEvent;
import io.animal.mouse.events.CountdownTickEvent;

public class CountDownService extends Service {

    private final String TAG = "CountDownService";

    private final int MAX_SECONDS = 3600;
    private final int ONE_SECONDS = 1000;
    private final int DEFAULT_MILLI_SECONDS = 2700 * ONE_SECONDS;

    private static final int COUNTDOWN_TICK_INTERVAL = 500;

    private CountDownTimer countDownTimer;
    private IBinder countDownServiceBinder;

    private TimerStatus timerStatus;
    private long remainMilliseconds;

    private SharedPreferences pref;

    private AlarmPlayer alarmPlayer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        startForegroundService();

        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);

        remainMilliseconds = pref.getLong("remain_time", DEFAULT_MILLI_SECONDS);

        int timerType = pref.getInt("timer_status", TimerStatus.STOP.getType());
        if (timerType == TimerStatus.STOP.getType()) {
            timerStatus = TimerStatus.STOP;
        } else if (timerType == TimerStatus.START.getType()) {
            timerStatus = TimerStatus.START;
        }

        countDownServiceBinder = new CountDownServiceBinder(this);

        alarmPlayer = new AlarmPlayer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind(intent)");

        remainMilliseconds = pref.getLong("remain_time", DEFAULT_MILLI_SECONDS);

        return countDownServiceBinder;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();

        pref.edit().putLong("remain_time", remainMilliseconds).apply();
        pref.edit().putInt("timer_status", timerStatus.getType()).apply();
    }

    public void startCountdown(final long millis) {
        Log.d(TAG, "startCountdown(" + millis + ")");

        timerStatus = TimerStatus.START;

        sendStartNotification("Start Countdown");

        countDownTimer = new CountDownTimer(millis, COUNTDOWN_TICK_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(TAG, "onTick(" + millisUntilFinished + ")");

                remainMilliseconds = millisUntilFinished;
                EventBus.getDefault().post(new CountdownTickEvent(remainMilliseconds));
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "onFinish()");

                timerStatus = TimerStatus.STOP;
                countDownTimer.cancel();
                EventBus.getDefault().post(new CountdownFinishEvent());

                sendFinishNotification("End Countdown");
                alarmPlayer.playAlarmSound(getApplicationContext());
            }
        }.start();
    }

    public void stopCountdown() {
        Log.d(TAG, "stopCountdown()");

        timerStatus = TimerStatus.STOP;

        try {
            countDownTimer.cancel();
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public TimerStatus getState() {
        return timerStatus;
    }

    public long getRemainMilliseconds() {
        return remainMilliseconds;
    }

    public void setRemainMilliseconds(long milliseconds) {
        this.remainMilliseconds = milliseconds;
    }

    private void startForegroundService() {
//        Intent notificationIntent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
//
////        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_service);
//
//        NotificationCompat.Builder builder;
//        if (Build.VERSION.SDK_INT >= 26) {
//            String CHANNEL_ID = "io.animal.mouse";
//            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
//                    "io.animal.mouse Service Channel", NotificationManager.IMPORTANCE_DEFAULT);
//
//            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
//
//            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
//        } else {
//            builder = new NotificationCompat.Builder(this);
//        }
//
//        builder.setSmallIcon(R.drawable.ic_notification)
//                .setContent(null)
//                .setContentIntent(pendingIntent);

        startForeground(1, new Notification());
    }

    private void sendStartNotification(String text) {
        Log.d(TAG, "sendNotification(" + text + ")");

        String channelId = "io.animal";
        String channelName = "mouse";
        int notifyId = 0;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
//            channel.setSound(null, null); // no sound
            channel.setShowBadge(true);
            notificationManager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int requestID = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), requestID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setContentTitle("TimeTimer") // required
                .setContentText(text)  // required
//                .setDefaults(Notification.BADGE_ICON_SMALL) // 알림, 사운드 진동 설정
                .setPriority(NotificationCompat.PRIORITY_LOW) // not display in heads-up .
//                .setSound(null)
                .setSmallIcon(R.drawable.ic_notification)
//                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification))
                .setContentIntent(pendingIntent);

        notificationManager.notify(notifyId, builder.build());
    }

    private void sendFinishNotification(String text) {
        Log.d(TAG, "sendNotification(" + text + ")");

        String channelId = "io.animal";
        String channelName = "mouse";
        int notifyId = 0;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
//            channel.enableVibration(true);
//            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationManager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int requestID = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), requestID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setContentTitle("TimeTimer") // required
                .setContentText(text)  // required
//                .setDefaults(Notification.DEFAULT_ALL) // 알림, 사운드 진동 설정
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

                .setSmallIcon(R.drawable.ic_notification)
//                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground))
                .setContentIntent(pendingIntent)
                .setChannelId(channelId);

        notificationManager.notify(notifyId, builder.build());
    }
}
