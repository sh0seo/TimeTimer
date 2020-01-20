package io.animal.mouse.service;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.greenrobot.eventbus.EventBus;

import io.animal.mouse.MainActivity;
import io.animal.mouse.R;
import io.animal.mouse.CountdownStatus;
import io.animal.mouse.alarm.AlarmUtil;
import io.animal.mouse.events.CountdownFinishEvent;
import io.animal.mouse.events.CountdownTickEvent;
import io.animal.mouse.notification.NotificationHelper;

public class CountDownService extends Service {

    private final String TAG = "CountDownService";

    private final int MAX_SECONDS = 3600;
    private final int ONE_SECONDS = 1000;
    private final int DEFAULT_MILLI_SECONDS = 2700 * ONE_SECONDS;

    private final static long VIBRATION_PATTERN[] = {-1};
    private final static int COUNTDOWN_TICK_INTERVAL = 500;

    private final static int NOTI_START_ID1 = 1101;
    private final static int NOTI_START_ID2 = 1102;
    private final static int NOTI_END_ID1 = 1201;
    private final static int NOTI_END_ID2 = 1202;

    private CountDownTimer countDownTimer;
    private IBinder countDownServiceBinder;

    private CountdownStatus countdownStatus;
    private long remainMilliseconds;

    private SharedPreferences pref;

    private NotificationHelper notificationHelper;
    private AlarmUtil alarmUtil;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notificationHelper = new NotificationHelper(this);

        startForegroundService();

        remainMilliseconds = getPreferences().getLong("remain_time", DEFAULT_MILLI_SECONDS);

        int timerType = getPreferences().getInt("timer_status", CountdownStatus.STOP.getType());
        if (timerType == CountdownStatus.STOP.getType()) {
            countdownStatus = CountdownStatus.STOP;
        } else if (timerType == CountdownStatus.START.getType()) {
            countdownStatus = CountdownStatus.START;
        }

        countDownServiceBinder = new CountDownServiceBinder(this);

        alarmUtil = new AlarmUtil(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind(intent)");

        remainMilliseconds = getPreferences().getLong("remain_time", DEFAULT_MILLI_SECONDS);

        return countDownServiceBinder;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();

        getPreferences().edit().putLong("remain_time", remainMilliseconds).apply();
        getPreferences().edit().putInt("timer_status", countdownStatus.getType()).apply();
    }

    public void startCountdown(final long millis) {
        Log.d(TAG, "startCountdown(" + millis + ")");

        countdownStatus = CountdownStatus.START;

        sendFinishNotification("New Start Countdown");

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

                countdownStatus = CountdownStatus.STOP;
                try {
                    countDownTimer.cancel();
                } catch (NullPointerException e) {
                    Log.e(TAG, "" + e.getLocalizedMessage());
                }

                EventBus.getDefault().post(new CountdownFinishEvent());

//                sendFinishNotification("End Countdown");
            }
        }.start();
    }

    public void stopCountdown() {
        Log.d(TAG, "stopCountdown()");

        countdownStatus = CountdownStatus.STOP;

        try {
            countDownTimer.cancel();
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public CountdownStatus getState() {
        return countdownStatus;
    }

    public long getRemainMilliseconds() {
        return remainMilliseconds;
    }

    public void setRemainMilliseconds(long milliseconds) {
        this.remainMilliseconds = milliseconds;
    }

    private void startForegroundService() {
        final int CHANNEL_ID = 1001;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            NotificationCompat.Builder builder = notificationHelper.getForegroundNotification();

            startForeground(CHANNEL_ID, builder.build());
        } else {
            startForeground(CHANNEL_ID, new Notification());
        }
    }

    private void sendStartNotification(String text) {
        Log.d(TAG, "sendNotification(" + text + ")");

        NotificationCompat.Builder builder = notificationHelper.getStartNotification(
                getResources().getString(R.string.app_name), text, getMainIntent());
        notificationHelper.notify(NOTI_START_ID1, builder);

        alarmUtil.playAlarm();
    }

    private void sendFinishNotification(String text) {
        Log.d(TAG, "sendNotification(" + text + ")");

        NotificationCompat.Builder builder = notificationHelper.getEndNotification(
                getResources().getString(R.string.app_name), text, getMainIntent(), getStopIntent());
        notificationHelper.notify(NOTI_END_ID1, builder);

        alarmUtil.playAlarm();
    }

    private boolean isAlarm() {
        return pref.getBoolean("alarm_type", false);
    }

    private SharedPreferences getPreferences() {
        if (pref == null) {
            pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        }
        return pref;
    }

    private PendingIntent getMainIntent() {
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra("event_alarm","end");

        int requestID = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), requestID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }

    private PendingIntent getStopIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("event_alarm","end");

        int requestID = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), requestID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }
}
