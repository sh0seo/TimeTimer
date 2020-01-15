package io.animal.mouse.service;

import java.io.IOException;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import io.animal.mouse.R;

public class AlarmPlayer {

	private static final String TAG = "AlarmPlayer";

	private MediaPlayer mediaPlayer;

	void playAlarmSound(Context context) {
		Log.d(TAG, "playAlarmSound");

		if (mediaPlayer == null) {
			mediaPlayer = MediaPlayer.create(context, R.raw.beep);

			if (prepareSoundAndCheckPreconditions(context, mediaPlayer)) {
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				mediaPlayer.setLooping(true);

				try {
					mediaPlayer.prepare();
				} catch (IllegalStateException e) {
					Log.e(TAG, "Could not prepare media player for playback.", e);
				} catch (IOException e) {
					Log.e(TAG, "Could not prepare media player for playback.", e);
				}
			}
		}

		if (!mediaPlayer.isPlaying()) {
			mediaPlayer.start();
		}
	}

	void stopAlarm() {
		Log.d(TAG, "stopAlarm");
		if (mediaPlayer != null) {
			mediaPlayer.stop();
		}
		mediaPlayer = null;
	}

	private boolean prepareSoundAndCheckPreconditions(Context context, MediaPlayer mediaPlayer) {
		Log.d(TAG, "prepareSoundAndCheckPreconditions");

		final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		if (audioManager == null) {
			Log.e(TAG, "Could not get audio manager, sound will not be played.");
			return false;
		}

		if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) == 0) {
			Log.w(TAG, "Volume set to 0, alarm sound will not be played.");
			return false;
		}

		if (mediaPlayer == null) {
			Log.e(TAG, "Media player is not initialized, sound will not be played.");
			return false;
		}
//
//		if (!findAndLoadAlarmSound(context, mediaPlayer)) {
//			Log.e(TAG, "Could not find any alarm sound or could not set the alarm sound as data source in media player, alarm sound will not be played.");
//			return false;
//		}

		return true;
	}

	private boolean findAndLoadAlarmSound(Context context, MediaPlayer mediaPlayer) {
		Log.d(TAG, "findAndLoadAlarmSound");

		Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		if (!setDataSourceInMediaPlayer(context, mediaPlayer, alarmUri)) {
			// alert is null or not readable, use notification sound as backup
			alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			// I don't think this can ever being null (a default notification
			// should always be present ) but just in case
			if (!setDataSourceInMediaPlayer(context, mediaPlayer, alarmUri)) {
				// notification sound is null or not readable, use ringtone as
				// last resort
				alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
				if (!setDataSourceInMediaPlayer(context, mediaPlayer, alarmUri)) {
					return false;
				}
			}
		}

		return true;
	}

	private boolean setDataSourceInMediaPlayer(Context context, MediaPlayer mediaPlayer, Uri alarmUri) {
		if (alarmUri == null) {
			return false;
		}

		try {
			mediaPlayer.setDataSource(context, alarmUri);
			return true;
		} catch (IllegalStateException e) {
			Log.w(TAG, "Could not load the alarm sound from " + alarmUri.toString(), e);
			return false;
		} catch (IOException e) {
			Log.w(TAG, "Could not load the alarm sound from " + alarmUri.toString(), e);
			return false;
		}
	}
}