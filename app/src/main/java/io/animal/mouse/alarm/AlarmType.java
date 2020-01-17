package io.animal.mouse.alarm;

public enum AlarmType {
    RINGTONE(0, "RINGTONE"),
    VIBRATE(1, "VIBRATE");

    private int type;
    private String value;

    AlarmType(int type, String value) {
        this.type = type;
        this.value = value;
    }
}
