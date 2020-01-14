package io.animal.mouse.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import io.animal.mouse.MainActivity;
import io.animal.mouse.R;
import io.animal.mouse.TimerStatus;

public class CountDownService extends Service {

    private final String TAG = "CountDownService";

    private final int MAX_MILLI_SECONDS = 3600 * 1000;
    private CountDownTimer countDownTimer;
    private IBinder countDownServiceBinder;

    private TimerStatus timerStatus;
    private long remainMilliseconds;

    private static final int COUNTDOWN_TICK_INTERVALL = 300;
//    static final int DELAY_TIME = COUNTDOWN_TICK_INTERVALL / 2;
//    public static final int GUI_UPDATE_INTERVALL = COUNTDOWN_TICK_INTERVALL / 4;


    public CountDownService() {
        Log.d(TAG, "CountdownService instance created");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        timerStatus = TimerStatus.STOP;
        remainMilliseconds = MAX_MILLI_SECONDS;

        countDownServiceBinder = new CountDownServiceBinder(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind(intent)");
        return countDownServiceBinder;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();

        stopCountDownTimer();
    }

    private void stopCountDownTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    public void startCountdown(final long millis) {
        Log.d(TAG, "startCountdown(" + millis + ")");

        timerStatus = TimerStatus.START;

        // TODO test notification
        String channelId = "channel";
        String channelName = "Channel Name";

        NotificationManager notifManager = (NotificationManager) getSystemService  (Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            notifManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int requestID = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                requestID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentTitle("TimeTimer") // required
                .setContentText("Content")  // required
//                        .setDefaults(Notification.DEFAULT_ALL) // 알림, 사운드 진동 설정
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground))
                .setContentIntent(pendingIntent);

        notifManager.notify(0, builder.build());

//        countDownTimer = new CountDownTimer(millis, COUNTDOWN_TICK_INTERVALL) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                onCountdownTimerTick(millisUntilFinished);
//            }
//
//            @Override
//            public void onFinish() {
//                onCountdownFinish();
//            }
//        };
    }

    public void stopCountdown() {
    }

    private void onCountdownTimerTick(long remainMilliseconds) {
        this.remainMilliseconds = remainMilliseconds;
    }

    private void onCountdownFinish() {
        countDownTimer.cancel();
    }

    public TimerStatus getState() {
        return timerStatus;
    }


}
