package io.animal.mouse.service;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

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
                onCountdownTimerTick(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                onCountdownFinish();
            }
        };
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

    private final boolean isServiceBound() {
        return countDownService != null;
    }

    private CountDownService getCountdownService() {
        if (isServiceBound()) {
            return countDownService;
        } else {
            throw new IllegalStateException(
                    "CountdownService is not bound to activity");
        }
    }
}
