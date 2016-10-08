package org.bigsupersniper.wlangames.socket;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
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
    private String remoteIP;
    private String id;
    private long lastAliableTime = System.currentTimeMillis();
    private Timer timer;
    private OnSocketClientListener onSocketClientListener;

    public SocketClient() {

    }

    public SocketClient(SocketChannel channel) {
        this.channel = channel;
        this.init();
        this.localIP = channel.socket().getRemoteSocketAddress().toString().replace("/", "");
        this.remoteIP = channel.socket().getLocalSocketAddress().toString().replace("/", "");
    }

    public void setOnSocketClientListener(OnSocketClientListener onSocketClientListener) {
        this.onSocketClientListener = onSocketClientListener;
    }

    private void init() {
        try {
            sendPoolExecutor = new ThreadPoolExecutor(10, 20, 1, TimeUnit.HOURS, new LinkedBlockingQueue());
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

    public void connect(String ip, int port) throws IOException{
        try {
            if (channel == null) {
                channel = SocketChannel.open();
                channel.connect(new InetSocketAddress(ip, port));
                this.init();
                this.openRead();
                this.localIP = channel.socket().getLocalSocketAddress().toString().replace("/", "");
                this.remoteIP = channel.socket().getRemoteSocketAddress().toString().replace("/", "");
            }
        } catch (IOException e) {
            throw e;
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
                                    sb.append(new String(buffer, 0, len - 1, SocketUtils.MessageCharset));
                                    break;
                                } else {
                                    sb.append(new String(buffer, 0, len, SocketUtils.MessageCharset));
                                }
                                readBuffer.clear();
                            }

                            if (sb.length() > 0) {
                                try {
                                    SocketMessage msg = new Gson().fromJson(sb.toString(), SocketMessage.class);
                                    onSocketClientListener.onRead(SocketClient.this, msg);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    break;
                                }
                            } else {
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

    public void openRead() {
        synchronized (lock) {
            if (this.connected && !this.readStarted) {
                onSocketClientListener.onConnected(this);
                new Thread(readRunnable).start();
                this.readStarted = true;
            }
        }
    }

    public void send(final SocketMessage msg) {
        sendPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String json = new Gson().toJson(msg) + SocketUtils.EndChar;
                    sendBuffer.put(json.getBytes(SocketUtils.MessageCharset));
                    sendBuffer.flip();
                    channel.write(sendBuffer);
                    sendBuffer.clear();
                    onSocketClientListener.onSend(SocketClient.this, msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void send(int cmd, String body) {
        SocketMessage msg = new SocketMessage();
        msg.setFrom(this.localIP);
        msg.setTo(this.remoteIP);
        msg.setCmd(cmd);
        msg.setBody(body);

        this.send(msg);
    }

    public boolean isConnected() {
        return this.connected;
    }

    public String getLocalIP() {
        return this.localIP;
    }

    public String getRemoteIP() {
        return this.remoteIP;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getLastAliableTime(){
        return this.lastAliableTime;
    }

    public void setLastAliableTime(){
        this.lastAliableTime = System.currentTimeMillis();
    }

    public void startTimer(){
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (isConnected()){
                    send(SocketCmd.Client_Heartbeat , "");
                }
            }
        };
        timer = new Timer();
        timer.schedule(task , 5000 , 45 * 1000);
    }

    public void stopTimer(){
        if (this.timer != null){
            this.timer.cancel();
        }
    }

    public void disconnect() {
        synchronized (lock) {
            if (this.connected) {
                this.connected = false;
                this.onSocketClientListener.onDisconnected(this);
                try {
                    if (channel.isConnected()) {
                        channel.socket().shutdownInput();
                        channel.socket().shutdownOutput();
                        channel.close();
                    }
                    if (selector.isOpen()) {
                        selector.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
