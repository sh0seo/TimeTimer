package io.animal.mouse;

/**
 * Timer의 상태를 정의.
 */
public enum CountdownStatus {
    STOP("STOP", 0),
    START("START", 1);

    private String value;

    private int type;

    CountdownStatus(String value, int type) {
        this.value = value;
        this.type = type;
    }

    public String getValue() {
        return this.value;
    }

    public int getType() {
        return this.type;
    }
}
