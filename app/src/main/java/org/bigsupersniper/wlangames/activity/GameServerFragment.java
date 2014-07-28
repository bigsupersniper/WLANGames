package org.bigsupersniper.wlangames.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Looper;
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

import org.bigsupersniper.wlangames.R;
import org.bigsupersniper.wlangames.socket.HandlerWhats;
import org.bigsupersniper.wlangames.socket.OnSocketClientListener;
import org.bigsupersniper.wlangames.socket.OnSocketServerListener;
import org.bigsupersniper.wlangames.socket.SocketClient;
import org.bigsupersniper.wlangames.socket.SocketCmd;
import org.bigsupersniper.wlangames.socket.SocketMessage;
import org.bigsupersniper.wlangames.socket.SocketServer;
import org.bigsupersniper.wlangames.socket.SocketUtils;


public class GameServerFragment extends Fragment{

    private Context context;
    //server
    private SocketServer socketServer;
    private Switch swServer;
    private TextView tvIP;
    private EditText etPort;

    //client
    private SocketClient socketClient;
    private Switch swClient;
    private EditText etServerIp;
    private EditText etServerPort;

    public static String getIPAdress(Context mContext) {

        //获取wifi服务
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int i = wifiInfo.getIpAddress();
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
    }

    private OnSocketServerListener onSocketServerListener = new OnSocketServerListener() {
        @Override
        public void onBind() {

        }

        @Override
        public void onAccept(SocketClient client) {

        }

        @Override
        public void onBroadcast() {

        }
    };

    private void initServerView(View view){
        context = view.getContext();
        //ip
        tvIP = (TextView) view.findViewById(R.id.tvIP);
        tvIP.setText(getIPAdress(context));
        //port
        etPort = (EditText) view.findViewById(R.id.etPort);
        //open or close service
        swServer = (Switch)view.findViewById(R.id.swServer);
        swServer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    socketServer = new SocketServer();
                    String ip = tvIP.getText().toString();
                    int port = Integer.parseInt(etPort.getText().toString());
                    if (port < 1024) {
                        Toast.makeText(context, "监听端口必须大于1024！", Toast.LENGTH_SHORT).show();
                        swServer.setChecked(false);
                        return;
                    }
                    socketServer.bind(ip, port, SocketUtils.SocketPoolSize);
                    Toast.makeText(context, "启动服务成功,最大连接数" + SocketUtils.SocketPoolSize + "个！", Toast.LENGTH_SHORT).show();
                    etPort.setEnabled(false);
                    getIndexActivity().setSocketServer(socketServer);
                } else {
                    socketServer.stop();
                    etPort.setEnabled(false);
                    Toast.makeText(context, "停止服务成功！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private OnSocketClientListener onSocketClientListener = new OnSocketClientListener() {
        @Override
        public void onConnected() {

        }

        @Override
        public void onDisconnected() {

        }

        @Override
        public void onRead(SocketMessage msg) {
            System.out.println("onRead : " + msg.getCmd() + " : " + msg.getBody());
            if (msg.getCmd().equals(SocketCmd.BluffDice)) {
                Message message = new Message();
                message.what = HandlerWhats.ReadMessage;
                message.obj = msg;
                handler.sendMessage(message);
            }
        }

        @Override
        public void onSend(SocketMessage msg) {

        }
    };

    private void initClientView(View view){
        etServerIp = (EditText) view.findViewById(R.id.etServerIP);
        etServerPort = (EditText) view.findViewById(R.id.etServerPort);
        //设置服务器ip
        etServerIp.setText(tvIP.getText().toString());

        swClient = (Switch)view.findViewById(R.id.swClient);
        swClient.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    new Thread(new Runnable(){
                        @Override
                        public void run() {
                            try {
                                socketClient = new SocketClient();
                                socketClient.setOnSocketListener(onSocketClientListener);

                                String ip = etServerIp.getText().toString();
                                int port = Integer.parseInt(etServerPort.getText().toString());
                                if (port < 1024) {
                                    showMessage("服务端口必须大于1024！");
                                    swClient.setChecked(false);
                                    return ;
                                }
                                if (!socketClient.isConnected()) {
                                    socketClient.connect(ip, port);
                                    showMessage("连接服务器成功！");
                                    etServerIp.setEnabled(false);
                                    etServerPort.setEnabled(false);
                                }
                            }catch (Exception e){
                                System.out.println(e.getMessage());
                            }
                        }
                    }).start();
                } else {
                    socketClient.disconnect();
                    etServerIp.setEnabled(true);
                    etServerPort.setEnabled(true);
                    showMessage("断开服务器连接成功！");
                }
            }
        });
    }

    /**
     * 子线程直接调用Toast会报错，必须如下实现
     * @param message
     */
    public void showMessage(String message){
        Looper.prepare();
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        Looper.loop();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HandlerWhats.ReadMessage) {
                SocketMessage smg = (SocketMessage)msg.obj;
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                Fragment fragment = null;
                if (smg.getCmd().equals(SocketCmd.BluffDice)){
                    fragment = getFragmentManager().findFragmentByTag("BluffDiceFragment");
                    TextView textView = (TextView)fragment.getView().findViewById(R.id.textView);
                    textView.setText(smg.getBody());
                }
                if (fragment != null){
                    IndexActivity indexActivity = (IndexActivity)getActivity();
                    indexActivity.hideAllFragments(transaction);
                    transaction.show(fragment);
                    transaction.commit();
                }
            }
        }
    };

    private IndexActivity getIndexActivity(){
        return (IndexActivity)getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_server, container, false);

        this.initServerView(view);
        this.initClientView(view);

        return view;
    }


}
