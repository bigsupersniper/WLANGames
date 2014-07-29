package org.bigsupersniper.wlangames.socket;

import com.google.gson.Gson;

import org.bigsupersniper.wlangames.common.BluffDice;

import java.util.ArrayList;
import java.util.Iterator;
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
