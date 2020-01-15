package io.animal.mouse;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import io.animal.mouse.alarm.AlarmUtil;
import io.animal.mouse.service.CountDownService;
import io.animal.mouse.service.CountDownServiceBinder;
import io.animal.mouse.service.IRemoteServiceCallback;
import io.animal.mouse.settings.SettingsActivity;
import io.animal.mouse.views.PlayPauseView;
import io.animal.mouse.views.ProgressPieView;
import io.animal.mouse.views.SeekCircle;

public class MainActivity extends AppCompatActivity implements IRemoteServiceCallback {

    private static final String TAG = "MainActivity";

    private final long MAX_TIMER_MILLISECODNS = 3600 * 1000;

    private SharedPreferences pref;

    // UI Components
    private ImageView alarmVibration;
    private ProgressPieView stopWatchPie;
    private SeekCircle stopWatch;
    private PlayPauseView playPauseController;
    private ImageButton settingsMenu;

    // StopWatch Service
    private ServiceConnection serviceConnection;
    private CountDownService countDownService;
    private Intent serviceIntent;

    private Chronometer miniStopWatch;

    private AlarmUtil alarmUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);

        // findViewBy ~
        stopWatchPie = findViewById(R.id.my_progress);
        stopWatch = findViewById(R.id.my_seekbar);
        playPauseController = findViewById(R.id.play_pause_view);

        alarmVibration = findViewById(R.id.alarm_vibration);
        miniStopWatch = findViewById(R.id.stop_watch);

        settingsMenu = findViewById(R.id.more_menu);


        initializeAdMob();

        initializeAlarmVibration();

        initializeSettingMenu();

        // Alarm & Vibrate Utility.
        alarmUtil = new AlarmUtil();

        stopWatch.setOnSeekCircleChangeListener(new SeekCircle.OnSeekCircleChangeListener() {
            @Override
            public void onProgressChanged(SeekCircle seekCircle, int progress, boolean fromUser) {
                Log.d(TAG,"[onProgressChanged()] Progress: " + progress);
                if (countDownService == null) {
                    return;
                }

                if (countDownService.getState() == TimerStatus.START) {
                    return;
                }

                updateUIStopWatchPie(MAX_TIMER_MILLISECODNS - (progress * 1000));
                updateUIMiniStopWatch(MAX_TIMER_MILLISECODNS - (progress * 1000));
            }

            @Override
            public void onStartTrackingTouch(SeekCircle seekCircle) {
            }

            @Override
            public void onStopTrackingTouch(SeekCircle seekCircle) {
            }
        });

        playPauseController.setOnClickListener(v -> {
            float stopWatchPiePercent = stopWatchPie.getPercent();
            int stopWatchProgress = stopWatch.getProgress();

            long startTime = (3600 - stopWatchProgress) * 1000;

            Log.d(TAG, "StartTime: " + startTime + " ms PiePercent: " + stopWatchPiePercent + " Progress: " + stopWatchProgress);
            pref.edit().putLong("startTime", startTime).apply();

            if (isServiceBound()) {
                if (countDownService.getState() == TimerStatus.STOP) {
                    countDownService.startCountdown(startTime);
                } else if (countDownService.getState() == TimerStatus.START) {
                    countDownService.stopCountdown();
                }

                playPauseController.toggle();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");

        if (countDownService == null) {
            serviceIntent = new Intent(this, CountDownService.class);

            Log.d(TAG, "initializing service connection");

            startCountDownService();
            initServiceConnection();

            Log.d(TAG, "about to call  : bindService");
            bindService(serviceIntent, serviceConnection, 0);
            Log.d(TAG, "has been called: bindService");

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");

        unBindCountdownService();
    }

    /**
     * Alarm & Vibration Button
     */
    private void initializeAlarmVibration() {
        boolean isAlarm = pref.getBoolean("alarm_type", true);
        if (isAlarm) {
            alarmVibration.setImageResource(R.drawable.ic_notifications_24px);
        } else {
            alarmVibration.setImageResource(R.drawable.ic_notifications_off_24px);
        }

        alarmVibration.setOnClickListener(v -> {
            boolean bAlarm = pref.getBoolean("alarm_type", true);
            if (bAlarm) {
                alarmVibration.setImageResource(R.drawable.ic_notifications_off_24px);
                    // alert vibrate
                alarmUtil.playVibrate(getApplicationContext());
            } else {
                alarmVibration.setImageResource(R.drawable.ic_notifications_24px);
                    // alert ringtone
                alarmUtil.playRingtone(getApplicationContext());
            }

            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("alarm_type", !bAlarm);
            editor.apply();
        });
    }

    /**
     * AdMob 초기화.
     */
    private void initializeAdMob() {
        MobileAds.initialize(this, s -> {});

        // AdMob View.
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        });
    }

    /**
     * Setting Menu Event
     */
    private void initializeSettingMenu() {
        settingsMenu.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void onServiceConnectedBinder(CountDownServiceBinder binder) {
        Log.d(TAG, "onServiceConnected(CountdownServiceBinder)");

        countDownService = binder.getCountdownService();
        countDownService.registerCallback(this);
    }

    private void onServiceDisConnected() {
        Log.d(TAG, "onServiceDisConnected()");

        countDownService.unregisterCallback(this);
        countDownService = null;
        serviceConnection = null;
    }

    protected void unBindCountdownService() {
        Log.d(TAG, "unBindCountdownService()");

        if (isServiceBound()) {
            Log.d(TAG, "about to call  : super.unbindService");
            unbindService(serviceConnection);
            Log.d(TAG, "has been called: super.unbindService");

            countDownService = null;
            serviceConnection = null;
        } else {
            Log.d(TAG, "service not bound in unbindCountdownService()");
        }
    }

    private boolean isServiceBound() {
        return countDownService != null;
    }

    private CountDownService getCountdownService() {
        if (isServiceBound()) {
            return countDownService;
        } else {
            throw new IllegalStateException("CountdownService is not bound to activity");
        }
    }

    private void startCountDownService() {
        Log.d(TAG, "startCountdownService()");

        Log.d(TAG, "about to call  : startService");
        ComponentName result = startService(serviceIntent);
        Log.d(TAG, "has been called: startService - result: " + result);
    }

    private void initServiceConnection() {
        Log.d(TAG, "initServiceConnection()");

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                Log.d(TAG, "ServiceConnection#onServiceConnected()");

                CountDownServiceBinder binder = (CountDownServiceBinder) service;
                onServiceConnectedBinder(binder);

                // Update UI.
                updateUIMiniStopWatch(countDownService.getRemainMilliseconds());
                updateUIStopWatchPie(countDownService.getRemainMilliseconds());
                if (countDownService.getState() == TimerStatus.START) {
                    if (playPauseController.isPlaying()) {
                        playPauseController.toggle();
                    }
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG, "ServiceConnection#onServiceDisconnected()");

                onServiceDisConnected();
            }
        };
    }

    @Override
    public void onTick(final long milliseconds) {
        Log.d(TAG, "onTick(" + milliseconds + ")");

        updateUIMiniStopWatch(milliseconds);

        updateUIStopWatchPie(milliseconds);
    }

    @Override
    public void onFinish() {
        Log.d(TAG, "onFinish()");

        updateUIMiniStopWatch(0);

        updateUIStopWatchPie(0);

        playPauseController.toggle();
    }

    private void updateUIMiniStopWatch(long milliseconds) {
        Log.d(TAG, "updateUIMiniStopWatch(" + milliseconds + ")");

        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;

        seconds = seconds % 60;
        minutes = minutes % 60;

        String secondsD = String.valueOf(seconds);
        String minutesD = String.valueOf(minutes);

        if (seconds < 10) secondsD = "0" + seconds;
        if (minutes < 10) minutesD = "0" + minutes;

        miniStopWatch.setText(String.format("%s:%s", minutesD, secondsD));
    }

    private void updateUIStopWatchPie(long milliseconds) {
        Log.d(TAG, "updateUIStopWatchPie(" + milliseconds + ")");

        float t = MAX_TIMER_MILLISECODNS - milliseconds;
        float temp = t / MAX_TIMER_MILLISECODNS * 100;
        stopWatchPie.setPercent(temp);
    }
}
