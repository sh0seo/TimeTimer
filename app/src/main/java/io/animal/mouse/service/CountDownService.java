package io.animal.mouse.service;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import io.animal.mouse.TimerStatus;

public class CountDownService extends Service implements IRemoteService {

    private final String TAG = "CountDownService";

    private final int MAX_SECONDS = 3600;
    private final int ONE_SECONDS = 1000;
    private final int MAX_MILLI_SECONDS = MAX_SECONDS * ONE_SECONDS;

    private CountDownTimer countDownTimer;
    private IBinder countDownServiceBinder;

    private TimerStatus timerStatus;
    private long remainMilliseconds;

    private static final int COUNTDOWN_TICK_INTERVALL = 500;
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
        return this.countDownServiceBinder;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }

    public void startCountdown(final long millis) {
        Log.d(TAG, "startCountdown(" + millis + ")");

        timerStatus = TimerStatus.START;

        countDownTimer = new CountDownTimer(millis, COUNTDOWN_TICK_INTERVALL) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(TAG, "onTick(" + millisUntilFinished + ")");
                onCountdownTimerTick(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "onFinish()");
                onCountdownFinish();
            }
        }.start();
    }

    public void stopCountdown() {
        Log.d(TAG, "stopCountdown()");

        timerStatus = TimerStatus.STOP;

        countDownTimer.cancel();
    }

    public TimerStatus getState() {
        return timerStatus;
    }

    private void onCountdownTimerTick(long remainMilliseconds) {
        this.remainMilliseconds = remainMilliseconds;

        if (callback != null) {
            callback.onTick(remainMilliseconds);
        }
    }

    private void onCountdownFinish() {
        countDownTimer.cancel();
    }

    private IRemoteServiceCallback callback;

    @Override
    public boolean registerCallback(IRemoteServiceCallback callback) {
        this.callback = callback;
        return true;
    }

    @Override
    public boolean unregisterCallback(IRemoteServiceCallback callback) {
        this.callback = null;
        return true;
    }


//    // TODO test notification
//    String channelId = "channel";
//    String channelName = "Channel Name";
//
//    NotificationManager notifManager = (NotificationManager) getSystemService  (Context.NOTIFICATION_SERVICE);
//
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//        int importance = NotificationManager.IMPORTANCE_HIGH;
//        NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
//        notifManager.createNotificationChannel(mChannel);
//    }
//
//    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
//    Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
//                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//    int requestID = (int) System.currentTimeMillis();
//    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
//            requestID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//                builder.setContentTitle("TimeTimer") // required
//                        .setContentText("Content")  // required
////                        .setDefaults(Notification.DEFAULT_ALL) // 알림, 사운드 진동 설정
//                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
//            .setSmallIcon(R.drawable.ic_notification)
//                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground))
//            .setContentIntent(pendingIntent);
//
////                notifManager.notify(0, builder.build());
}
