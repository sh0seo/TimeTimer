package io.animal.mouse.service;

public interface IRemoteService {

    boolean registerCallback(IRemoteServiceCallback callback);
    boolean unregisterCallback(IRemoteServiceCallback callback);

}
