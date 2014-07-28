package org.bigsupersniper.wlangames.socket;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class SocketClient {

    private SocketChannel channel;
    private boolean connected = false;
    private Selector selector;
    private ByteBuffer readBuffer;
    private ByteBuffer sendBuffer;
    private Thread readThread ;
    private String localIP;
    private OnSocketClientListener onSocketListener;

    public SocketClient(){

    }

    public SocketClient(SocketChannel channel){
        this.channel = channel;
        this.init();
    }

    public void setOnSocketListener(OnSocketClientListener onSocketListener){
        this.onSocketListener = onSocketListener;
    }

    private void init(){
        try {
            readBuffer = ByteBuffer.allocate(SocketUtils.BufferSize);
            sendBuffer = ByteBuffer.allocate(SocketUtils.BufferSize);
            selector = Selector.open();
            this.connected = true;
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
            this.localIP = channel.socket().getRemoteSocketAddress().toString().replace("/","");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect(String ip , int port){
        try {
            channel = SocketChannel.open();
            channel.connect(new InetSocketAddress(ip, port));
            this.init();
            this.openRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openRead(){
        if (this.connected){
            if (readThread == null){
                readThread = new Thread(new Runnable() {
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

                                        if (onSocketListener != null){
                                            SocketMessage msg = new Gson().fromJson(sb.toString(), SocketMessage.class);
                                            onSocketListener.onRead(msg);
                                        }
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                break;
                            }
                        }
                    }
                });
            }
            readThread.start();
        }
    }

    public void send(final SocketMessage msg){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String json = new Gson().toJson(msg) + SocketUtils.EndChar;
                    sendBuffer.put(json.getBytes(SocketUtils.MessageCharset));
                    sendBuffer.flip();
                    channel.write(sendBuffer);
                    sendBuffer.clear();
                    //System.out.println("send : " + json.replace(SocketUtils.EndChar, ""));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public boolean isConnected(){
        return this.connected;
    }

    public String getLocalIP(){
        return this.localIP;
    }

    public void disconnect(){
        if (this.connected){
            this.connected = false;
            if (readThread.isAlive()){
                readThread.interrupt();
            }

            try {
                if (selector.isOpen()){
                    selector.close();
                }
                if (channel.isConnected()){
                    channel.socket().shutdownInput();
                    channel.socket().shutdownOutput();
                    channel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
