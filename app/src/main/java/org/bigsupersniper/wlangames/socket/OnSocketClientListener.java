package org.bigsupersniper.wlangames.socket;

/**
 * Created by sniper on 2014/7/28.
 */
public interface OnSocketClientListener {

    public void onDisconnected(SocketClient client);

    public void onMessage(String message);

    public void onRead(SocketClient client, SocketMessage msg);

    public void onSend(SocketClient client, SocketMessage msg);
}
