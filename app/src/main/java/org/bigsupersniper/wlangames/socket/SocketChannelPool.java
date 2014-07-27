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

    public void foreach(int whatMsg , SocketMessage msg){
        synchronized (lock){
            if (pool.size() > 0){
                Iterator<SocketClient> iterator = pool.iterator();
                while (iterator.hasNext()){
                    SocketClient client = iterator.next();
                    msg.setTo(client.getRemoteIP());
                    if(whatMsg == HandlerWhats.Broadcast_BluffDice){
                        msg.setCmd(SocketUtils.Cmd_BluffDice);
                        msg.setBody(new Gson().toJson(BluffDice.shake()));
                    }else if (whatMsg == HandlerWhats.Broadcast_CPoker){
                        msg.setCmd(SocketUtils.Cmd_CPoker);
                        //msg.setBody(new Gson().toJson(BluffDice.shake()));
                    }

                    client.send(msg);
                }
            }
        }
    }

}
