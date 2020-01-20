package io.animal.mouse.alarm;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import io.animal.mouse.R;

public class AlarmUtil extends ContextWrapper {

    // vibrate pattern.
    private final long VIBRATE_PATTERN[] = {100, 100};

    // -1 is no repeat.
    private final int VIBRATE_REPEATS = 3;

    private SharedPreferences pref;

    private Vibrator vibrator;
    private MediaPlayer audioPlayer ;

    public AlarmUtil(Context base) {
        super(base);
    }

    public void playAlarm() {
        if (getPref().getBoolean("alarm_type", false)) {
            playRingtone();
        } else {
            playVibrate();
        }
    }

    public void stopAlarm() {
        if (getPref().getBoolean("alarm_type", false)) {
            getMediaPlayer().stop();
        } else {
            getVibrator().cancel();
        }
    }

    public void pingVibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getVibrator().vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            getVibrator().vibrate(VIBRATE_PATTERN, VIBRATE_REPEATS);
        }
    }

    public void pingRingtone() {
        getMediaPlayer().start();
    }

    public void playVibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getVibrator().vibrate(VibrationEffect.createOneShot(1500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            getVibrator().vibrate(VIBRATE_PATTERN, VIBRATE_REPEATS);
        }
    }

    public void playRingtone() {
        getMediaPlayer().start();
    }

    /**
     * Get Shared Preference.
     *
     * @return preference of private mode.
     */
    private SharedPreferences getPref() {
        if (pref == null) {
            pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        }
        return pref;
    }

    /**
     * Get vibrator service.
     *
     * @return vibrator service.
     */
    private Vibrator getVibrator() {
        if (vibrator == null) {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }
        return vibrator;
    }

    private MediaPlayer getMediaPlayer() {
        if (audioPlayer == null) {
            audioPlayer = MediaPlayer.create(getApplicationContext(), R.raw.beep);
        }
        return audioPlayer;
    }
}
