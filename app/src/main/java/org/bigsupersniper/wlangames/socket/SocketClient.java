package org.bigsupersniper.wlangames.socket;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SocketClient {

    private Object lock = new Object();
    private SocketChannel channel;
    private boolean connected = false;
    private boolean readStarted = false;
    private ThreadPoolExecutor sendPoolExecutor;
    private Selector selector;
    private ByteBuffer readBuffer;
    private ByteBuffer sendBuffer;
    private String localIP;
    private String id ;

    private OnSocketClientListener onSocketClientListener= new OnSocketClientListener() {
        @Override
        public void onConnected() {

        }

        @Override
        public void onDisconnected(SocketClient client) {

        }

        @Override
        public void onMessage(String message) {

        }

        @Override
        public void onRead(SocketClient client , SocketMessage msg) {

        }

        @Override
        public void onSend(SocketClient client , SocketMessage msg) {

        }
    };

    public SocketClient(){

    }

    public SocketClient(SocketChannel channel){
        this.channel = channel;
        this.init();
        this.localIP = channel.socket().getRemoteSocketAddress().toString().replace("/","");
    }

    public void setOnSocketClientListener(OnSocketClientListener onSocketClientListener){
        this.onSocketClientListener = onSocketClientListener;
    }

    private void init(){
        try {
            sendPoolExecutor = new ThreadPoolExecutor(10 , 20 , 1 , TimeUnit.HOURS , new LinkedBlockingQueue());
            readBuffer = ByteBuffer.allocate(SocketUtils.BufferSize);
            sendBuffer = ByteBuffer.allocate(SocketUtils.BufferSize);
            selector = Selector.open();
            this.connected = true;
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect(String ip , int port){
        try {
            if (channel == null){
                channel = SocketChannel.open();
                channel.connect(new InetSocketAddress(ip, port));
                this.init();
                this.openRead();
                this.localIP = channel.socket().getLocalSocketAddress().toString().replace("/","");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Runnable readRunnable = new Runnable() {
        @Override
        public void run() {
            while (channel.isConnected()) {
                try {
                    if (selector.select() == 0) continue;

                    SocketChannel sc;
                    StringBuilder sb = new StringBuilder();
                    for (SelectionKey key : selector.selectedKeys()) {
                        selector.selectedKeys().remove(key);
                        if (key.isReadable()) {
                            sc = (SocketChannel) key.channel();
                            readBuffer.clear();
                            int len = 0;
                            while ((len = sc.read(readBuffer)) > 0) {
                                byte[] buffer = readBuffer.array();
                                if (buffer[len - 1] == SocketUtils.EndByte) {
                                    sb.append(new String(buffer, 0,len - 1, SocketUtils.MessageCharset));
                                    break;
                                } else {
                                    sb.append(new String(buffer, 0, len, SocketUtils.MessageCharset));
                                }
                                readBuffer.clear();
                            }

                            if (sb.length() > 0) {
                                SocketMessage msg = new Gson().fromJson(sb.toString(), SocketMessage.class);
                                if (msg.getCmd().equals(SocketCmd.Connected)){
                                    onSocketClientListener.onConnected();
                                }else{
                                    onSocketClientListener.onRead(SocketClient.this , msg);
                                }
                            }else{
                                disconnect();
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void openRead(){
        synchronized (lock){
            if (this.connected && !this.readStarted){
                new Thread(readRunnable).start();
                this.readStarted = true;
            }
        }
    }

    public void send(final SocketMessage msg){
        sendPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String json = new Gson().toJson(msg) + SocketUtils.EndChar;
                    sendBuffer.put(json.getBytes(SocketUtils.MessageCharset));
                    sendBuffer.flip();
                    channel.write(sendBuffer);
                    sendBuffer.clear();
                    onSocketClientListener.onSend(SocketClient.this , msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public boolean isConnected(){
        return this.connected;
    }

    public String getLocalIP(){
        return this.localIP;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getId(){
        return this.id;
    }

    public void disconnect(){
        synchronized (lock){
            if (this.connected){
                this.connected = false;
                this.onSocketClientListener.onDisconnected(this);
                try {
                    if (channel.isConnected()){
                        channel.socket().shutdownInput();
                        channel.socket().shutdownOutput();
                        channel.close();
                    }
                    if (selector.isOpen()){
                        selector.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
