package io.animal.mouse;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import io.animal.mouse.settings.SettingsActivity;
import io.animal.mouse.views.PlayPauseView;
import io.animal.mouse.views.ProgressPieView;
import io.animal.mouse.views.SeekCircle;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private SharedPreferences pref;

    private ImageView alarmVibration;

    private ProgressPieView stopWatchPie;
    private SeekCircle stopWatch;
    private PlayPauseView playPauseController;

    private Chronometer miniStopWatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);

        // get resources
        initialize();

        initializeAdMob();

        initializeAlarmVibration();

        initializeSettingMenu();

        stopWatchPie = findViewById(R.id.my_progress);

        stopWatch = findViewById(R.id.my_seekbar);
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
        miniStopWatch = findViewById(R.id.stop_watch);
        miniStopWatch.setText("45:00");
        miniStopWatch.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                stopWatchPie.setPercent(stopWatchPie.getPercent() + 1);
            }
        });

        playPauseController = findViewById(R.id.play_pause_view);
//        long startTime = pref.getLong("startTime", 0);
//        if (startTime == 0) {
////            playPauseController.toggle();
//        }

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


        // start & stop controller
//        AppCompatImageView controller = findViewById(R.id.controller);
//        controller.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final AppCompatImageView imageView = (AppCompatImageView) v;
//                Drawable drawable = imageView.getDrawable();
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    if (drawable instanceof AnimatedVectorDrawable) {
//                        AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) drawable;
//                        animatedVectorDrawable.registerAnimationCallback(new Animatable2.AnimationCallback() {
//                            @Override
//                            public void onAnimationEnd(Drawable drawable) {
//                                super.onAnimationEnd(drawable);
//                                if (status == 1) {
//                                    imageView.setImageResource(R.drawable.avd_pause_play2);
//                                    status = 0;
//                                }
//                            }
//                        });
//                        status = 1;
//                        animatedVectorDrawable.start();
//                    }
//                } else {
//                    if (drawable instanceof AnimatedVectorDrawableCompat) {
//                        AnimatedVectorDrawableCompat animatedVectorDrawableCompat = (AnimatedVectorDrawableCompat) drawable;
//                        animatedVectorDrawableCompat.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
//                            @Override
//                            public void onAnimationEnd(Drawable drawable) {
//                                super.onAnimationEnd(drawable);
//                                if (status == 1) {
//                                    imageView.setImageResource(R.drawable.avd_pause_play2);
//                                    status = 0;
//                                }
//                            }
//                        });
////                        status = 1;
//                        animatedVectorDrawableCompat.start();
//                    }
//                }
//            }
//        });

    }

    @Override
    protected void onStop() {
        super.onStop();

    }


    private void initialize() {
        alarmVibration = findViewById(R.id.alarm_vibration);
    }

    /**
     * Alarm & Vibration Button
     */
    private void initializeAlarmVibration() {
        boolean isAlarm = pref.getBoolean("alarm", true);
        if (isAlarm) {
            alarmVibration.setImageResource(R.drawable.ic_notifications_24px);
        } else {
            alarmVibration.setImageResource(R.drawable.ic_notifications_off_24px);
        }

        alarmVibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isAlarm = pref.getBoolean("alarm", true);
                if (isAlarm) {
                    alarmVibration.setImageResource(R.drawable.ic_notifications_off_24px);
                } else {
                    alarmVibration.setImageResource(R.drawable.ic_notifications_24px);
                }

                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("alarm", !isAlarm);
                editor.commit();
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
     * Setting Activity.
     */
    private void initializeSettingMenu() {
        // show setting activity.
        ImageView menu = findViewById(R.id.more_menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });
    }
}
