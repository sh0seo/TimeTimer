package io.animal.mouse.alarm;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import io.animal.mouse.R;

public class AlarmUtil {

    // vibrate pattern.
    private final long VIBRATE_PATTERN[] = {100, 100, 100, 100, 100};

    // -1 is no repeat.
    private final int VIBRATE_REPEATS = 3;

    private SharedPreferences pref;

    private Vibrator vibrator;
    private MediaPlayer audioPlayer ;

    public void playAlarm(Context context) {
        pref = context.getSharedPreferences("pref", Activity.MODE_PRIVATE);

        if (pref.getBoolean("alarm_type", false)) {
            playRingtone(context);
        } else {
            playVibrate(context);
        }
    }

    public void stopAlarm() {
        if (pref.getBoolean("alarm_type", false)) {
            audioPlayer.stop();
        } else {
            vibrator.cancel();
        }
    }

    public void playVibrate(Context context) {
        if (context == null) {
            throw new NullPointerException();
        }

        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) {
            throw new NullPointerException();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(1500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(VIBRATE_PATTERN, VIBRATE_REPEATS);
        }
    }

    public void playRingtone(Context context) {
        if (context == null) {
            throw new NullPointerException();
        }

        audioPlayer = MediaPlayer.create(context, R.raw.beep);
        audioPlayer.start();
    }
}
