package org.bigsupersniper.wlangames.socket;

/**
 * Created by sniper on 2014/7/28.
 */
public interface OnSocketServerListener {

    public void onBind();

    public void onAccept(SocketClient client);

    public void onBroadcast();
}
