package org.bigsupersniper.wlangames.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SocketServer {

    private Object lock = new Object();
    private String localIP;
    private boolean isStarted;
    private ServerSocketChannel channel;
    private SocketChannelPool channelPool;
    private int poolSize;
    private Selector selector;
    private ByteBuffer readBuffer;
    private OnSocketServerListener onSocketServerListener;
    private OnSocketClientListener onSocketClientListener;
    private Runnable acceptRunnable = new Runnable() {
        @Override
        public void run() {
            while (isStarted) {
                try {
                    SocketClient client = new SocketClient(channel.accept());
                    if (getCurrentPoolSize() <= 0) {
                        client.send(SocketCmd.Client_Disconnected, "服务器连接池已满");
                    } else {
                        client.send(SocketCmd.Client_Connected, "欢迎 : " + client.getLocalIP());
                        client.setOnSocketClientListener(onSocketClientListener);
                        if (channelPool.add(client)) {
                            client.openRead();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public SocketServer() {
        readBuffer = ByteBuffer.allocate(SocketUtils.BufferSize);
        channelPool = new SocketChannelPool();
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setOnSocketServerListener(OnSocketServerListener onSocketServerListener) {
        this.onSocketServerListener = onSocketServerListener;
    }

    public void setOnPoolSocketClientListener(OnSocketClientListener onSocketClientListener) {
        this.onSocketClientListener = onSocketClientListener;
    }

    /**
     * @param ip
     * @param port    端口必须大于1024
     * @param backlog
     */
    public void bind(String ip, int port, int backlog) {
        try {
            if (!isStarted) {
                channel = ServerSocketChannel.open();
                channel.socket().bind(new InetSocketAddress(ip, port), backlog);
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

    public boolean isStarted() {
        return this.isStarted;
    }

    public void stop() {
        synchronized (lock) {
            if (this.isStarted) {
                this.isStarted = false;
                if (channel.isOpen()) {
                    onSocketServerListener.onStop();
                    try {
                        selector.close();
                        channel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public String getLocalIP() {
        return localIP;
    }

    public int getCurrentPoolSize() {
        return poolSize - channelPool.size();
    }

    public List<SocketClient> getList() {
        return channelPool.getList();
    }

    public boolean add(SocketClient client) {
        if (isStarted) {
            return channelPool.add(client);
        }
        return false;
    }

    public boolean remove(SocketClient client) {
        if (isStarted) {
            return channelPool.remove(client);
        }
        return false;
    }

    public String[] getIPList() {
        List<String> ips = new ArrayList<String>();
        if (channelPool.size() > 0) {
            Iterator<SocketClient> iterator = channelPool.getList().iterator();
            while (iterator.hasNext()) {
                SocketClient client = iterator.next();
                String ip = client.getLocalIP();
                ips.add(client.getId() + " ( " + ip + " )");
            }
        }

        return ips.toArray(new String[0]);
    }

}
