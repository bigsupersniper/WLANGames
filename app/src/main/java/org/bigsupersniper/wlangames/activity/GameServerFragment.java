package org.bigsupersniper.wlangames.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import com.google.gson.Gson;

import org.bigsupersniper.wlangames.R;
import org.bigsupersniper.wlangames.common.BluffDice;
import org.bigsupersniper.wlangames.common.CPoker;
import org.bigsupersniper.wlangames.common.FragmentTags;
import org.bigsupersniper.wlangames.common.SendWhats;
import org.bigsupersniper.wlangames.common.StringUtils;
import org.bigsupersniper.wlangames.common.WifiUtils;
import org.bigsupersniper.wlangames.socket.OnSocketClientListener;
import org.bigsupersniper.wlangames.socket.OnSocketServerListener;
import org.bigsupersniper.wlangames.socket.SocketClient;
import org.bigsupersniper.wlangames.socket.SocketCmd;
import org.bigsupersniper.wlangames.socket.SocketMessage;
import org.bigsupersniper.wlangames.socket.SocketServer;
import org.bigsupersniper.wlangames.socket.SocketUtils;

import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


public class GameServerFragment extends Fragment{

    //server
    private SocketServer socketServer;
    private Switch swServer;
    private TextView tvIP;
    private EditText etPort;
    private EditText etCount;

    //client
    private SocketClient socketClient;
    private Switch swClient;
    private EditText etServerIp;
    private EditText etServerPort;
    private EditText etClientId;

    private OnSocketServerListener onSocketServerListener = new OnSocketServerListener() {
        @Override
        public void onBind(boolean success) {
            String message = "";
            if (success){
                message = "启动服务成功！";
                etPort.setEnabled(false);
                etCount.setEnabled(false);
                getIndexActivity().setSocketServer(socketServer);
            }else{
                message = "启动服务失败，端口已绑定！";
            }
            Toast.makeText(getActivity(), message , Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onMessage(String message) {
            sendMessage(SendWhats.Toast_ShowMessage , message);
        }

        @Override
        public void onBroadcast(List<SocketClient> clients , int what) {
            String message = "";
            if (clients.size() > 0){
                Iterator<SocketClient> iterator = clients.iterator();
                if(what == SendWhats.Broadcast_BluffDice){
                    while (iterator.hasNext()){
                        SocketClient client = iterator.next();
                        SocketMessage msg = new SocketMessage();
                        msg.setFrom(socketServer.getLocalIP());
                        msg.setTo(client.getLocalIP());
                        msg.setCmd(SocketCmd.BluffDice);
                        msg.setBody(new Gson().toJson(BluffDice.shake()));

                        client.send(msg);
                    }
                }else if (what == SendWhats.Broadcast_CPoker){
                    String[] shuffledCards = CPoker.shuffle();
                    boolean[] gones = new boolean[4];
                    Random random = new Random(new Date().getTime());
                    int size = 4;
                    if (clients.size() < 4) {
                        size = clients.size();
                    }

                    int n = random.nextInt(4);

                    while (iterator.hasNext()){
                        SocketClient client = iterator.next();
                        while (true) {
                            if (!gones[n]) {
                                gones[n] = true;
                                SocketMessage msg = new SocketMessage();
                                msg.setFrom(socketServer.getLocalIP());
                                msg.setTo(client.getLocalIP());
                                msg.setCmd(SocketCmd.CPoker);
                                msg.setBody(new Gson().toJson(CPoker.deal(shuffledCards, n)));
                                client.send(msg);
                                size--;
                                break;
                            } else {
                                n = random.nextInt(4);
                            }
                        }
                        if(size <= 0) break;
                    }
                }

                if(what == SendWhats.Broadcast_BluffDice){
                    message = "本局 <大话骰> 开始于 : " + new SimpleDateFormat("HH:mm:ss").format(new Date());
                }else if (what == SendWhats.Broadcast_CPoker){
                    message = "本局 <十三水> 开始于 : " + new SimpleDateFormat("HH:mm:ss").format(new Date());
                }
            }else {
                message = "没有已链接的客户端！";
            }

            sendMessage(SendWhats.Toast_ShowMessage , message);
        }

        @Override
        public void onStop(List<SocketClient> clients) {
            if (clients.size() > 0) {
                Iterator<SocketClient> iterator = clients.iterator();
                while (iterator.hasNext()) {
                    SocketClient client = iterator.next();
                    SocketMessage msg = new SocketMessage();
                    msg.setFrom(socketServer.getLocalIP());
                    msg.setCmd(SocketCmd.Disconnected);
                    msg.setBody("服务器已关闭");
                    msg.setTo(client.getLocalIP());

                    client.send(msg);
                }
            }
        }
    };

    private void initServerView(View view){
        tvIP = (TextView) view.findViewById(R.id.tvIP);
        tvIP.setText(WifiUtils.getIPAdress(getActivity()));
        etPort = (EditText) view.findViewById(R.id.etPort);
        etCount = (EditText) view.findViewById(R.id.etCount);
        swServer = (Switch)view.findViewById(R.id.swServer);
        swServer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    socketServer = new SocketServer();
                    String ip = tvIP.getText().toString();
                    int port = Integer.parseInt(etPort.getText().toString());
                    int count = Integer.parseInt(etCount.getText().toString());
                    if (count <= 0){
                        swServer.setChecked(false);
                        sendMessage(SendWhats.Toast_ShowMessage, "连接数不能小于0！");
                        return;
                    }
                    if (port < 1024) {
                        swServer.setChecked(false);
                        sendMessage(SendWhats.Toast_ShowMessage, "监听端口必须大于1024！");
                        return;
                    }
                    socketServer.setOnSocketServerListener(onSocketServerListener);
                    socketServer.bind(ip, port, count);
                } else {
                    socketServer.stop();
                    etPort.setEnabled(true);
                    etCount.setEnabled(true);
                    sendMessage(SendWhats.Toast_ShowMessage, "停止服务成功！");
                }
            }
        });
    }

    private OnSocketClientListener onSocketClientListener = new OnSocketClientListener() {

        @Override
        public void onConnected() {
            sendMessage(SendWhats.Client_Connected, null);
        }

        @Override
        public void onDisconnected(SocketClient client) {
            sendMessage(SendWhats.Client_Disconnected, null);
        }

        @Override
        public void onMessage(String message) {

        }

        @Override
        public void onRead(SocketClient client , SocketMessage msg) {
            if (msg.getCmd().equals(SocketCmd.BluffDice) || msg.getCmd().equals(SocketCmd.CPoker)) {
                sendMessage(SendWhats.Client_ReadMessage, msg);
            }else if (msg.getCmd().equals(SocketCmd.Disconnected)){
                sendMessage(SendWhats.Toast_ShowMessage, msg.getBody());
                socketClient.disconnect();
            }
        }

        @Override
        public void onSend(SocketClient client , SocketMessage msg) {

        }
    };

    private void sendMessage(int what , Object obj){
        Message msg = new Message();
        msg.what = what;
        msg.obj = obj;
        handler.sendMessage(msg);
    }

    private void initClientView(View view){
        etServerIp = (EditText) view.findViewById(R.id.etServerIP);
        etServerPort = (EditText) view.findViewById(R.id.etServerPort);
        //设置服务器ip
        etServerIp.setText(tvIP.getText().toString());
        etClientId = (EditText) view.findViewById(R.id.etClientId);
        etClientId.setText(StringUtils.getRandom(5));
        swClient = (Switch)view.findViewById(R.id.swClient);
        swClient.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    final String ip = etServerIp.getText().toString();
                    final int port = Integer.parseInt(etServerPort.getText().toString());
                    final String clientId = etClientId.getText().toString();
                    if (clientId.equals("")) {
                        sendMessage(SendWhats.Toast_ShowMessage , "请输入昵称！");
                        swClient.setChecked(false);
                        return ;
                    }
                    if (port < 1024) {
                        sendMessage(SendWhats.Toast_ShowMessage , "服务端口必须大于1024！");
                        swClient.setChecked(false);
                        return ;
                    }
                    new Thread(new Runnable(){
                        @Override
                        public void run() {
                            try {
                                socketClient = new SocketClient();
                                socketClient.setId(clientId);
                                socketClient.setOnSocketClientListener(onSocketClientListener);
                                socketClient.connect(ip, port);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } else {
                    socketClient.disconnect();
                }
            }
        });
    }

    private IndexActivity getIndexActivity(){
        return (IndexActivity)getActivity();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SendWhats.Toast_ShowMessage:
                    //子线程直接调用Toast会报错
                    Toast.makeText(getActivity() , msg.obj.toString() , Toast.LENGTH_SHORT).show();
                    break;
                case SendWhats.Client_Connected:
                    etServerIp.setEnabled(false);
                    etServerPort.setEnabled(false);
                    Toast.makeText(getActivity() , "连接服务器成功！" , Toast.LENGTH_SHORT).show();
                    //提交客户端Id
                    SocketMessage sendMsg = new SocketMessage();
                    sendMsg.setFrom(socketClient.getLocalIP());
                    sendMsg.setTo(etServerIp.getText().toString());
                    sendMsg.setCmd(SocketCmd.SetClientId);
                    sendMsg.setBody(socketClient.getId());
                    socketClient.send(sendMsg);
                    break;
                case SendWhats.Client_Disconnected:
                    etServerIp.setEnabled(true);
                    etServerPort.setEnabled(true);
                    swClient.setChecked(false);
                    Toast.makeText(getActivity() , "已从服务器断开连接！" , Toast.LENGTH_SHORT).show();
                    break;
                case SendWhats.Client_ReadMessage:
                    if (msg.obj != null) {
                        SocketMessage readMsg = (SocketMessage) msg.obj;
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        getIndexActivity().hideAllFragments(transaction);
                        Fragment fragment = null;
                        if (readMsg.getCmd().equals(SocketCmd.BluffDice)) {
                            fragment = getFragmentManager().findFragmentByTag(FragmentTags.BluffDice);
                            int[] ids = new Gson().fromJson(readMsg.getBody() , int[].class);
                            ((BluffDiceFragment)fragment).refreshDices(ids);
                        } else if (readMsg.getCmd().equals(SocketCmd.CPoker)) {
                            fragment = getFragmentManager().findFragmentByTag(FragmentTags.CPoker);
                            String[] cards = new Gson().fromJson(readMsg.getBody() , String[].class);
                            ((CPokerFragment)fragment).refreshCards(cards);
                        }
                        if (fragment != null) {
                            if (fragment.isVisible()){
                                transaction.show(fragment);
                            }
                            transaction.commit();
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_server, container, false);
        this.initServerView(view);
        this.initClientView(view);

        return view;
    }


}
