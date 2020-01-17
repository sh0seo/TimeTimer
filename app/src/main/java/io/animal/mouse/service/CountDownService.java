package io.animal.mouse.service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

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

    private final static long VIBRATION_PATTERN[] = {100, 100};
    private final static int COUNTDOWN_TICK_INTERVAL = 500;

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

        remainMilliseconds = 5000;

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

//        sendStartNotification("Start Countdown");

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
                try {
                    countDownTimer.cancel();
                } catch (NullPointerException e) {
                    Log.e(TAG, "" + e.getLocalizedMessage());
                }

                EventBus.getDefault().post(new CountdownFinishEvent());

                sendFinishNotification("End Countdown");
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
        startForeground(1, new Notification());
    }

    private void sendStartNotification(String text) {
        Log.d(TAG, "sendNotification(" + text + ")");

        String channelId = "10001";
        String channelName = "io.animal.mouse.play";
        int notifyId = 0;

        boolean isAlarm = isAlarm();

        NotificationManager notificationManager = getNotificationManager(channelId, channelName, false, isAlarm, true);

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int requestID = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), requestID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setContentTitle("TimeTimer") // required
                .setContentText(text)  // required
                .setDefaults(Notification.BADGE_ICON_SMALL) // 알림, 사운드 진동 설정
                .setPriority(NotificationCompat.PRIORITY_LOW) // not display in heads-up .
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent);

        notificationManager.notify(notifyId, builder.build());
    }

    private void sendFinishNotification(String text) {
        Log.d(TAG, "sendNotification(" + text + ")");

        final String channelId = "10002";
        final String channelName = "io.animal.mouse.stop";
        final int notifyId = 0;

        NotificationManager notificationManager;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager = getSystemService(NotificationManager.class);
            NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
            if (channel == null) {
                channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);

//                if (!isAlarm()) {
//                    channel.enableVibration(true);
//                    channel.setVibrationPattern(VIBRATION_PATTERN);
//                } else {
//                    Uri soundUri = Uri.parse("android.resource://io.animal.mouse/" + R.raw.beep);
//                    AudioAttributes audioAttributes = new AudioAttributes.Builder()
//                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
//                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
//                            .build();
//                    channel.setSound(soundUri, audioAttributes);
//                    channel.setShowBadge(true);
//                }

                notificationManager.createNotificationChannel(channel);
            } else {
               notificationManager.deleteNotificationChannel(channelId);

//                if (!isAlarm()) {
//                    channel.enableVibration(true);
//                    channel.setVibrationPattern(VIBRATION_PATTERN);
//                } else {
//                    Uri soundUri = Uri.parse("android.resource://io.animal.mouse/" + R.raw.beep);
//                    AudioAttributes audioAttributes = new AudioAttributes.Builder()
//                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
//                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
//                            .build();
//                    channel.setSound(soundUri, audioAttributes);
//                    channel.setShowBadge(true);
//                }

                notificationManager.createNotificationChannel(channel);
            }
        } else {
             notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int requestID = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), requestID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setContentTitle(getResources().getString(R.string.app_name)) // required
                .setContentText(text)  // required
                .setDefaults(Notification.DEFAULT_ALL) // 알림, 사운드 진동 설정
                .setVibrate(VIBRATION_PATTERN)
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // not display in heads-up .
                .setContentIntent(pendingIntent);

        notificationManager.notify(notifyId, builder.build());
    }

    private NotificationManager getNotificationManager(String channelId, String channelName,
                                                       boolean useAlarm, boolean useRingtone, boolean useBadge) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            if (!useAlarm) {
                importance = NotificationManager.IMPORTANCE_HIGH;
            }

            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            if (useRingtone) {
                channel.enableVibration(true);
                channel.setVibrationPattern(VIBRATION_PATTERN);
            } else {
                Uri soundUri = Uri.parse("android.resource://io.animal.mouse/" + R.raw.beep);
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
                channel.setSound(soundUri, audioAttributes);
            }

            channel.setShowBadge(useBadge);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            return notificationManager;
        } else {
            NotificationManager notificationManager =
                    (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            return notificationManager;
        }
    }

    private boolean isAlarm() {
        return pref.getBoolean("alarm_type", false);
    }
}
