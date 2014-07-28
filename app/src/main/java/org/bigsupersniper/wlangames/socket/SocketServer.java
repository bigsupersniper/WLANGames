package org.bigsupersniper.wlangames.socket;

import com.google.gson.Gson;

import org.bigsupersniper.wlangames.common.BluffDice;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.List;

public class SocketServer {

    private String localIP ;
    private boolean isStarted ;
    private ServerSocketChannel channel ;
    private SocketChannelPool channelPool ;
    private OnSocketServerListener onSocketServerListener;

    public SocketServer(){
        try {
            channel = ServerSocketChannel.open();
            channelPool = new SocketChannelPool();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void setOnSocketServerListener(OnSocketServerListener onSocketServerListener){
        this.onSocketServerListener = onSocketServerListener;
    }

    private Runnable acceptRunnable = new Runnable() {
        @Override
        public void run() {
            while (isStarted){
                try {
                    SocketClient client = new SocketClient(channel.accept());
                    SocketMessage msg = new SocketMessage();
                    msg.setFrom(localIP);
                    msg.setTo(client.getLocalIP());
                    msg.setCmd(SocketCmd.Connected);
                    msg.setBody("欢迎 :" + msg.getTo());
                    client.send(msg);
                    channelPool.add(client);
                    client.openRead();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    /**
     *
     * @param ip
     * @param port 端口必须大于1024
     * @param backlog
     */
    public void bind(String ip , int port , int backlog){
        try {
            channel.socket().bind(new InetSocketAddress(ip , port) , backlog);
            isStarted = true;
            localIP = ip + ":" + port;
            new Thread(acceptRunnable).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean isStarted(){
        return this.isStarted;
    }

    public synchronized void stop(){
        if (this.isStarted){
            this.isStarted = false;
            if(channel.isOpen()){
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void broadcast(int whatMsg){
        if (this.isStarted){
            Object lock = channelPool.getLock();
            synchronized (lock){
                List<SocketClient> pool = channelPool.getList();
                if (pool.size() > 0){
                    SocketMessage msg = new SocketMessage();
                    msg.setFrom(this.localIP);
                    Iterator<SocketClient> iterator = pool.iterator();
                    while (iterator.hasNext()){
                        SocketClient client = iterator.next();
                        msg.setTo(client.getLocalIP());
                        if(whatMsg == HandlerWhats.Broadcast_BluffDice){
                            msg.setCmd(SocketCmd.BluffDice);
                            msg.setBody(new Gson().toJson(BluffDice.shake()));
                        }else if (whatMsg == HandlerWhats.Broadcast_CPoker){
                            msg.setCmd(SocketCmd.CPoker);
                            //msg.setBody(new Gson().toJson(BluffDice.shake()));
                        }

                        client.send(msg);
                    }
                }
            }

            this.onSocketServerListener.onBind();
        }
    }

}
