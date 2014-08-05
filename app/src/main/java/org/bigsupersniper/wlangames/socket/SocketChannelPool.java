package org.bigsupersniper.wlangames.socket;

import java.util.ArrayList;
import java.util.List;

public class SocketChannelPool{
    private List<SocketClient> pool = new ArrayList<SocketClient>();

    public synchronized void add(SocketClient client){
        if (!pool.contains(client)){
            pool.add(client);
        }
    }

    public synchronized void remove(SocketClient client){
        if (pool.contains(client)){
            pool.remove(client);
        }
    }

    public synchronized int size(){
        return this.pool.size();
    }

    public synchronized List<SocketClient> getList(){
        return this.pool;
    }

}
