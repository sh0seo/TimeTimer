package io.animal.mouse;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import io.animal.mouse.alarm.AlarmUtil;
import io.animal.mouse.service.CountDownService;
import io.animal.mouse.service.CountDownServiceBinder;
import io.animal.mouse.settings.SettingsActivity;
import io.animal.mouse.views.PlayPauseView;
import io.animal.mouse.views.ProgressPieView;
import io.animal.mouse.views.SeekCircle;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

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

    private NotificationManagerCompat notificationManagerCompat;

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

        this.serviceIntent = new Intent(this, CountDownService.class);

        initializeAlarmVibration();

        initializeSettingMenu();

        // Alarm & Vibrate Utility.
        alarmUtil = new AlarmUtil();

//        notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());


        stopWatch.setOnSeekCircleChangeListener(new SeekCircle.OnSeekCircleChangeListener() {
            @Override
            public void onProgressChanged(SeekCircle seekCircle, int progress, boolean fromUser) {
                float temp = progress * 100 / 3600;
                Log.d(TAG,"[onProgressChanged]" + progress + ": " + temp);

                stopWatchPie.setPercent(temp);
            }

            @Override
            public void onStartTrackingTouch(SeekCircle seekCircle) {
            }

            @Override
            public void onStopTrackingTouch(SeekCircle seekCircle) {
            }
        });

        // temp imple stopwatch

        miniStopWatch.setText("45:00");
        miniStopWatch.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                stopWatchPie.setPercent(stopWatchPie.getPercent() + 1);
            }
        });

        playPauseController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long startTime = SystemClock.elapsedRealtime() + 1000 * 60 * 45;

                pref.edit().putLong("startTime", startTime).commit();

                miniStopWatch.setBase(startTime);
//                stopWatch.setCountDown(true);
                miniStopWatch.start();
                playPauseController.toggle();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        if (countDownService == null) {
            Log.d(TAG, "initializing service connection");
            startCountDownService();
            initServiceConnection();

            Log.d(TAG, "about to call  : bindService");
            super.bindService(this.serviceIntent, this.serviceConnection, 0);
            Log.d(TAG, "has been called: bindService");
        } else {
            Log.d(TAG, "service already bound in bindService(), will not bind again");

            onAfterServiceConnected(getCountdownService().getState());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
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

        alarmVibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isAlarm = pref.getBoolean("alarm_type", true);
                if (isAlarm) {
                    alarmVibration.setImageResource(R.drawable.ic_notifications_off_24px);
                    // alert vibrate
                    alarmUtil.playVibrate(getApplicationContext());
                } else {
                    alarmVibration.setImageResource(R.drawable.ic_notifications_24px);
                    // alert ringtone
                    alarmUtil.playRingtone(getApplicationContext());
                }

                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("alarm_type", !isAlarm);
                editor.commit();

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

//                notifManager.notify(0, builder.build());
            }
        });
    }

    /**
     * AdMob 초기화.
     */
    private void initializeAdMob() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

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

        countDownService = binder.getCountDownService();
        onAfterServiceConnected(getCountdownService().getState());
    }

    private void onServiceDisConnected() {
        Log.d(TAG, "onServiceDisConnected()");

        countDownService = null;
        serviceConnection = null;
    }

    protected void unbindCountdownService() {
        Log.d(TAG, "unbindCountdownService()");

        if (isServiceBound()) {
            Log.d(TAG, "about to call  : super.unbindService");
            unbindService(this.serviceConnection);
            Log.d(TAG, "has been called: super.unbindService");

            countDownService = null;
            this.serviceConnection = null;
        } else {
            Log.d(TAG, "service not bound in unbindCountdownService()");
        }
    }

    private final boolean isServiceBound() {
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
        ComponentName result = super.startService(this.serviceIntent);
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
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG, "ServiceConnection#onServiceDisconnected()");

                onServiceDisConnected();
            }
        };
    }

    private final void onAfterServiceConnected(TimerStatus serviceState) {
        Log.d(TAG, "onAfterServiceConnected(" + serviceState + ")");

//        TimerStatus[] handledStates = getHandledServiceStates();
//        TimerStatus[] finishingStates = getFinishingServiceStates();

//        if (Arrays.binarySearch(handledStates, serviceState) >= 0) {
//            Log.d(TAG, "Current activity will handle state " + serviceState + ".");
//            handleState(serviceState);
//        } else if (Arrays.binarySearch(finishingStates, serviceState) >= 0) {
//            Log.d(TAG, "Current activity will finish because of state " + serviceState + ".");
//            onBeforeFinish();
//            finish();
//        } else {
//            Log.d(TAG, "Current activity will execute navigation from state "  + serviceState + ".");
//        }
    }
}
