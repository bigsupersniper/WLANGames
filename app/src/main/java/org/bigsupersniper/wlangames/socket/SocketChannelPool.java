package org.bigsupersniper.wlangames.socket;

import com.google.gson.Gson;

import org.bigsupersniper.wlangames.common.BluffDice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SocketChannelPool{
    private Object lock = new Object();
    private List<SocketClient> pool = new ArrayList<SocketClient>();

    public void add(SocketClient client){
        synchronized (lock){
            if (!pool.contains(client)){
                pool.add(client);
            }
        }
    }

    public void remove(SocketClient client){
        synchronized (lock){
            if (pool.contains(client)){
                pool.remove(client);
            }
        }
    }

    public Object getLock(){
        return this.lock;
    }

    public List<SocketClient> getList(){
        return this.pool;
    }

}
