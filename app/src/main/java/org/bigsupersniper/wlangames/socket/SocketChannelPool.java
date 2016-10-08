package org.bigsupersniper.wlangames.socket;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SocketChannelPool {
    private Object lock = new Object();
    private List<SocketClient> pool = new ArrayList<SocketClient>();

    public SocketChannelPool(){

    }

    private void startTimer(){
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                synchronized (lock){
                    Iterator<SocketClient> iterator = pool.iterator();
                    List<SocketClient> removePool = new ArrayList<SocketClient>();
                    while (iterator.hasNext()){
                        SocketClient client = iterator.next();
                        //over 60 seconds no response then add to removepool
                        if (System.currentTimeMillis() - client.getLastAliableTime() > 60 * 1000){
                            client.disconnect();
                            removePool.add(client);
                        }
                    }
                    //remove timeout clients
                    if (removePool.size() > 0) {
                        pool.removeAll(removePool);
                    }
                }
            }
        };
        Timer timer = new Timer();
        timer.schedule(task , 5000 , 60 * 1000);
    }

    public boolean add(SocketClient client) {
        synchronized (lock){
            if (!pool.contains(client)) {
                return pool.add(client);
            }
            return false;
        }
    }

    public boolean remove(SocketClient client) {
        synchronized (lock) {
            if (pool.contains(client)) {
                return pool.remove(client);
            }
            return false;
        }
    }

    public int size() {
        synchronized (lock) {
            return this.pool.size();
        }
    }

    public List<SocketClient> getList() {
        synchronized (lock) {
            return this.pool;
        }
    }
}
