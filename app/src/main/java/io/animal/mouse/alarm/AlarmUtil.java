package io.animal.mouse.alarm;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;

public class AlarmUtil {

    // vibrate pattern.
    private final long VIBRATE_PATTERN[] = {100, 100};

    // -1 is no repeat.
    private final int VIBRATE_REPEATS = -1;

    public void playVibrate(Context context) {
        if (context == null) {
            throw new NullPointerException();
        }

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) {
            throw new NullPointerException();
        }

        vibrator.vibrate(VIBRATE_PATTERN, VIBRATE_REPEATS);
    }

    public void playRingtone(Context context) {
        if (context == null) {
            throw new NullPointerException();
        }

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
        ringtone.play();
    }
}
