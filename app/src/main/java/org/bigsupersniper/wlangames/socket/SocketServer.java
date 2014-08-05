package org.bigsupersniper.wlangames.socket;

import org.bigsupersniper.wlangames.common.SendWhats;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SocketServer {

    private Object lock = new Object();
    private String localIP ;
    private boolean isStarted ;
    private ServerSocketChannel channel ;
    private SocketChannelPool channelPool ;
    private int poolSize;
    private OnSocketServerListener onSocketServerListener;
    private OnSocketClientListener onSocketClientListener;

    public SocketServer(){
        channelPool = new SocketChannelPool();
        this.onSocketServerListener = new OnSocketServerListener() {
            @Override
            public void onBind(boolean success) {

            }

            @Override
            public void onMessage(String message) {

            }

            @Override
            public void onBroadcast(List<SocketClient> clients , int what) {

            }

            @Override
            public void onStop(List<SocketClient> clients) {

            }
        };
        this.onSocketClientListener = new OnSocketClientListener() {
            @Override
            public void onConnected() {

            }

            @Override
            public void onDisconnected(SocketClient client) {
                if (isStarted){
                    channelPool.remove(client);
                    onSocketServerListener.onMessage(client.getLocalIP() + " 已断开连接！");
                }
            }

            @Override
            public void onMessage(String message) {

            }

            @Override
            public void onRead(SocketClient client , SocketMessage msg) {
                if (msg.getCmd().equals(SocketCmd.SetClientId)){
                    client.setId(msg.getBody());
                }else if (msg.getCmd().equals(SocketCmd.BluffDice_Open)){
                    broadcast(SendWhats.Broadcast_BluffDice_Result);
                }
            }

            @Override
            public void onSend(SocketClient client , SocketMessage msg) {
                if(msg.getCmd().equals(SocketCmd.Disconnected)){
                    client.disconnect();
                }
            }
        };
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

                    if (channelPool.size() >= poolSize){
                        msg.setCmd(SocketCmd.Disconnected);
                        msg.setBody("服务器已达最大连接数");
                        client.send(msg);
                    }else{
                        msg.setCmd(SocketCmd.Connected);
                        msg.setBody("welcome :" + msg.getTo());
                        client.send(msg);
                        client.setOnSocketClientListener(onSocketClientListener);
                        channelPool.add(client);
                        client.openRead();
                    }
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
            if (!isStarted){
                channel = ServerSocketChannel.open();
                channel.socket().bind(new InetSocketAddress(ip , port) , backlog);
                isStarted = true;
                poolSize = backlog;
                localIP = ip + ":" + port;
                new Thread(acceptRunnable).start();
            }
            onSocketServerListener.onBind(isStarted);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isStarted(){
        return this.isStarted;
    }

    public int getPoolSize(){
        return this.poolSize;
    }

    public void stop(){
        synchronized (lock){
            if (this.isStarted){
                this.isStarted = false;
                if(channel.isOpen()){
                    onSocketServerListener.onStop(channelPool.getList());
                    try {
                        channel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public String getLocalIP(){
        return localIP;
    }

    public void broadcast(int what){
        if (this.isStarted){
            onSocketServerListener.onBroadcast(channelPool.getList() , what);
        }
    }

    public String[] getList(){
        List<String> ips = new ArrayList<String>();
        if (channelPool.size() > 0){
            Iterator<SocketClient> iterator = channelPool.getList().iterator();
            while (iterator.hasNext()){
                SocketClient client = iterator.next();
                String ip = client.getLocalIP();
                ips.add(client.getId() + " ( " + ip + " )");
            }
        }

        return ips.toArray(new String[0]);
    }

}
