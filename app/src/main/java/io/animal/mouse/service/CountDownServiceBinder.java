package io.animal.mouse.service;

import android.os.Binder;

public class CountDownServiceBinder extends Binder {

    private CountDownService service;

    public CountDownServiceBinder(CountDownService service) {
        this.service = service;
    }

    public CountDownService getCountdownService() {
        return service;
    }
}
