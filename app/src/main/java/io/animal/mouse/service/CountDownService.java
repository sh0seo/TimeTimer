package io.animal.mouse.service;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

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
    public void onCreate() {
        super.onCreate();

        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);

        int timerType = pref.getInt("timer_status", TimerStatus.STOP.getType());
        if (timerType == 0) {
            timerStatus = TimerStatus.STOP;
        } else if (timerType == 1) {
            timerStatus = TimerStatus.START;
        }

        remainMilliseconds = pref.getLong("remain_time", DEFAULT_MILLI_SECONDS);

        countDownServiceBinder = new CountDownServiceBinder(this);

        alarmPlayer = new AlarmPlayer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind(intent)");

        remainMilliseconds = pref.getLong("remain_time", DEFAULT_MILLI_SECONDS);

        return this.countDownServiceBinder;
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

                alarmPlayer.playAlarmSound(getApplicationContext());
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

    public long getRemainMilliseconds() {
        return remainMilliseconds;
    }

    public void setRemainMilliseconds(long milliseconds) {
        this.remainMilliseconds = milliseconds;
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
