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


        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
    }

    public void playAlarm() {
        if (pref.getBoolean("alarm_type", false)) {
            playRingtone();
        } else {
            playVibrate();
        }
    }

    public void stopAlarm() {
        if (pref.getBoolean("alarm_type", false)) {
            audioPlayer.stop();
        } else {
            vibrator.cancel();
        }
    }

    public void pingVibrate() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) {
            throw new NullPointerException();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(VIBRATE_PATTERN, VIBRATE_REPEATS);
        }
    }

    public void pingRingtone() {
        audioPlayer = MediaPlayer.create(getApplicationContext(), R.raw.beep);
        audioPlayer.start();
    }

    public void playVibrate() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) {
            throw new NullPointerException();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(1500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(VIBRATE_PATTERN, VIBRATE_REPEATS);
        }
    }

    public void playRingtone() {
        audioPlayer = MediaPlayer.create(getApplicationContext(), R.raw.beep);
        audioPlayer.start();
    }
}
