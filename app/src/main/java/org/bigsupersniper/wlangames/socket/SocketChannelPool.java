package org.bigsupersniper.wlangames.socket;

import java.util.ArrayList;
import java.util.List;

public class SocketChannelPool{
    private List<SocketClient> pool = new ArrayList<SocketClient>();

    public synchronized boolean add(SocketClient client){
        if (!pool.contains(client)){
            return pool.add(client);
        }
        return false;
    }

    public synchronized boolean remove(SocketClient client){
        if (pool.contains(client)){
            return pool.remove(client);
        }
        return false;
    }

    public synchronized int size(){
        return this.pool.size();
    }

    public synchronized List<SocketClient> getList(){
        return this.pool;
    }

}
