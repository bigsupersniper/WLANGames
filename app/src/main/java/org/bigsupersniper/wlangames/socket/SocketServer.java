package org.bigsupersniper.wlangames.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

public class SocketServer {

    private ServerSocketChannel channel = null;
    private Thread acceptThread = null;
    private String localIP = null;
    private boolean started = false;
    private SocketChannelPool channelPool = null;

    public SocketServer(){
        try {
            channel = ServerSocketChannel.open();
            channelPool = new SocketChannelPool();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     *
     * @param ip
     * @param port 端口必须大于1024
     * @param backlog
     */
    public void bind(String ip , int port , int backlog){
        try {
            channel.socket().bind(new InetSocketAddress(ip , port) , backlog);
            started = true;
            localIP = ip + ":" + port;
            acceptThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (started){
                        try {
                            SocketClient client = new SocketClient(channel.accept());
                            SocketMessage msg = new SocketMessage();
                            msg.setFrom(localIP);
                            msg.setTo(client.getRemoteIP());
                            msg.setCmd("connect");
                            msg.setBody("欢迎 :" + msg.getTo());
                            client.send(msg);
                            channelPool.add(client);
                            client.beginRead();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            acceptThread.start();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean isStarted(){
        return this.started;
    }

    public void stop(){
        if (this.started){
            this.started = false;
            if (acceptThread.isAlive()){
                acceptThread.interrupt();
            }
            if(channel.isOpen()){
                try {
                    channel.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public void broadcast(int whatMsg){
        if (this.started){
            SocketMessage msg = new SocketMessage();
            msg.setFrom(this.localIP);
            this.channelPool.foreach(whatMsg , msg);
        }
    }
}
