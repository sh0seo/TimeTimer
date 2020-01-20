package io.animal.mouse;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.animal.mouse.alarm.AlarmUtil;
import io.animal.mouse.events.CountdownFinishEvent;
import io.animal.mouse.events.CountdownTickEvent;
import io.animal.mouse.service.CountDownService;
import io.animal.mouse.service.CountDownServiceBinder;
import io.animal.mouse.settings.SettingsActivity;
import io.animal.mouse.views.PlayPauseView;
import io.animal.mouse.views.ProgressPieView;
import io.animal.mouse.views.SeekCircle;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private final long MAX_TIMER_MILLISECONDS = 3600 * 1000;

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

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        alarmUtil = new AlarmUtil(this);

        stopWatch.setOnSeekCircleChangeListener(new SeekCircle.OnSeekCircleChangeListener() {
            @Override
            public void onProgressChanged(SeekCircle seekCircle, int progress, boolean fromUser) {
                Log.d(TAG,"[onProgressChanged()] Progress: " + progress);
                if (countDownService == null) {
                    return;
                }

                if (countDownService.getState() == CountdownStatus.START) {
                    return;
                }

                long milliSeconds = MAX_TIMER_MILLISECONDS - (progress * 1000);

                updateUIStopWatchPie(milliSeconds);
                updateUIMiniStopWatch(milliSeconds);

                countDownService.setRemainMilliseconds(milliSeconds);
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
            getPref().edit().putLong("startTime", startTime).apply();

            if (isServiceBound()) {
                long remainMilliseconds = countDownService.getRemainMilliseconds();
                if (remainMilliseconds == 0) {
                    remainMilliseconds = startTime;
                }

                if (countDownService.getState() == CountdownStatus.STOP) {
                    countDownService.startCountdown(remainMilliseconds);

                    // if options enable.
                    enableKeepScreen();
                } else if (countDownService.getState() == CountdownStatus.START) {
                    countDownService.stopCountdown();

                    disableKeepScreen();
                }

                playPauseController.toggle();
            }
        });

        receiveExtraIntent(getIntent());
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

        if (isServiceBound()) {
            if (countDownService.getState() == CountdownStatus.START) {
                if (playPauseController.isPlaying()) {
                    playPauseController.toggleNotAnimation();
                }
            }
        }

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        if (mAdView != null) {
            mAdView.resume();
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop()");
        EventBus.getDefault().unregister(this);
        super.onStop();

        unBindCountdownService();
    }

    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent()");

        receiveExtraIntent(intent);
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    /**
     * Alarm & Vibration Button
     */
    private void initializeAlarmVibration() {
        boolean isAlarm = getPref().getBoolean("alarm_type", true);
        if (isAlarm) {
            alarmVibration.setImageResource(R.drawable.ic_notifications_24px);
        } else {
            alarmVibration.setImageResource(R.drawable.ic_notifications_off_24px);
        }

        alarmVibration.setOnClickListener(v -> {
            boolean bAlarm = getPref().getBoolean("alarm_type", true);
            if (bAlarm) {
                alarmVibration.setImageResource(R.drawable.ic_notifications_off_24px);
                    // alert vibrate
                alarmUtil.pingVibrate();
            } else {
                alarmVibration.setImageResource(R.drawable.ic_notifications_24px);
                    // alert ringtone
//                alarmUtil.pingRingtone();
            }

            SharedPreferences.Editor editor = getPref().edit();
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
        mAdView = findViewById(R.id.adView);
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
    }

    private void onServiceDisConnected() {
        Log.d(TAG, "onServiceDisConnected()");

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

                CountDownServiceBinder binder = (CountDownServiceBinder) service;
                onServiceConnectedBinder(binder);

                Log.d(TAG, "onServiceConnected() getRemainMilliseconds(): " + countDownService.getRemainMilliseconds());

                // Update UI.
                updateUIMiniStopWatch(countDownService.getRemainMilliseconds());
                updateUIStopWatchPie(countDownService.getRemainMilliseconds());
                if (countDownService.getState() == CountdownStatus.START) {
                    if (playPauseController.isPlaying()) {
                        playPauseController.toggleNotAnimation();
                    }
                }

                updateKeepLockScreen();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG, "ServiceConnection#onServiceDisconnected()");

                onServiceDisConnected();
            }
        };
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCountdownTickEvent(CountdownTickEvent e) {
        Log.d(TAG, "onCountdownTickEvent(" + e.getMilliseconds() + ")");

        final long milliseconds = e.getMilliseconds();

        updateUIMiniStopWatch(milliseconds);
        updateUIStopWatchPie(milliseconds);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCountdownFinishEvent(CountdownFinishEvent e) {
        Log.d(TAG, "onCountdownFinishEvent()");

        updateUIMiniStopWatch(0);

        updateUIStopWatchPie(0);

        playPauseController.toggle();
    }

//    @SuppressWarnings("unused")
//    @Subscribe(threadMode = ThreadMode.MAIN)
    private void updateKeepLockScreen() {
        Log.d(TAG, "updateKeepLockScreen()");

        if (isServiceBound()) {
            if (countDownService.getState() == CountdownStatus.START) {
                boolean enableLockScreen = PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean("lock_screen", false);
                if (enableLockScreen) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }

    /**
     * Enable Keep Screen, if it enable options in Settings.
     */
    private void enableKeepScreen() {
        boolean enableLockScreen = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("lock_screen", false);
        if (enableLockScreen) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    /**
     * Disable Keep Screen.
     */
    private void disableKeepScreen() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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

        float t = MAX_TIMER_MILLISECONDS - milliseconds;
        float temp = t / MAX_TIMER_MILLISECONDS * 100;
        stopWatchPie.setPercent(temp);
    }

    private void receiveExtraIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        String value = intent.getStringExtra("event_alarm");
        if (value != null) {
            Log.d(TAG, "event_alarm: "+ value);

            playPauseController.callOnClick();
        }
    }

    private SharedPreferences getPref() {
        if (pref == null) {
            pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        }
        return pref;
    }
}
