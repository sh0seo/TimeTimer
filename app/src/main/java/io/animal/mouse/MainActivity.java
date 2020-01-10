package io.animal.mouse;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.content.Intent;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeAdMob();


        final ProgressPieView progressView = findViewById(R.id.my_progress);

        SeekCircle seeker = findViewById(R.id.my_seekbar);
        seeker.setOnSeekCircleChangeListener(new SeekCircle.OnSeekCircleChangeListener() {
            @Override
            public void onProgressChanged(SeekCircle seekCircle, int progress, boolean fromUser) {
                progressView.setPercent(seekCircle.getProgress() + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekCircle seekCircle) {
            }

            @Override
            public void onStopTrackingTouch(SeekCircle seekCircle) {
            }
        });

        // show setting activity.
        ImageView menu = findViewById(R.id.more_menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });


        // temp imple stopwatch
        final Chronometer stopWatch = findViewById(R.id.stop_watch);
        stopWatch.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                progressView.setPercent(progressView.getPercent() + 1);
            }
        });

        final PlayPauseView view = findViewById(R.id.play_pause_view);
        view.toggle();

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopWatch.setBase(45);
//                stopWatch.setCountDown(true);
                stopWatch.start();
                view.toggle();
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

//    private int status = 0;

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
}
