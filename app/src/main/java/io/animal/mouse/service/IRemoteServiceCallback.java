package io.animal.mouse.service;

/**
 * Service call activity for callback interface.
 */
public interface IRemoteServiceCallback {

    void onTick(long milliseconds);

    void onFinish();
}
