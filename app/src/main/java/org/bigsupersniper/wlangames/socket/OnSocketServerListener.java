package org.bigsupersniper.wlangames.socket;

import java.util.List;

/**
 * Created by sniper on 2014/7/28.
 */
public interface OnSocketServerListener {

    public void onBind(boolean success);

    public void onMessage(String message);

    public void onBroadcast(List<SocketClient> clients , int what);
}
