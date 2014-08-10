package org.bigsupersniper.wlangames.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.bigsupersniper.wlangames.R;
import org.bigsupersniper.wlangames.common.SendWhats;
import org.bigsupersniper.wlangames.common.StringUtils;
import org.bigsupersniper.wlangames.common.WifiUtils;
import org.bigsupersniper.wlangames.router.ServerRouter;
import org.bigsupersniper.wlangames.socket.OnSocketClientListener;
import org.bigsupersniper.wlangames.socket.OnSocketServerListener;
import org.bigsupersniper.wlangames.socket.SocketClient;
import org.bigsupersniper.wlangames.socket.SocketCmd;
import org.bigsupersniper.wlangames.socket.SocketMessage;
import org.bigsupersniper.wlangames.socket.SocketServer;


public class GameServerFragment extends Fragment{

    //server
    private SocketServer socketServer;
    private Switch swServer;
    private TextView tvIP;
    private EditText etPort;
    private EditText etCount;
    private ServerRouter serverRouter;

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
                getParent().register(socketServer);
                getParent().register(serverRouter);
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
        public void onStop() {
            serverRouter.broadcast(SocketCmd.Server_Closed , "服务已停止");
        }
    };

    private OnSocketClientListener onPoolSocketClientListener = new OnSocketClientListener() {

        @Override
        public void onDisconnected(SocketClient client) {
            if (socketServer.remove(client)){
                sendMessage(SendWhats.Toast_ShowMessage , client.getLocalIP() + " 已断开连接！");
            }
        }

        @Override
        public void onMessage(String message) { }

        @Override
        public void onRead(SocketClient client , SocketMessage msg) {
            serverRouter.router(client , msg);
        }

        @Override
        public void onSend(SocketClient client , SocketMessage msg) {
            switch (msg.getCmd()){
                case SocketCmd.Client_Disconnected:
                    client.disconnect();
                    break;
                default:
                    break;
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
                    serverRouter = new ServerRouter(socketServer);
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
                    socketServer.setOnPoolSocketClientListener(onPoolSocketClientListener);
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
        public void onDisconnected(SocketClient client) {
            sendMessage(SendWhats.Client_Disconnected, null);
        }

        @Override
        public void onMessage(String message) {}

        @Override
        public void onRead(SocketClient client , SocketMessage msg) {
            switch (msg.getCmd()){
                case SocketCmd.Client_Connected:
                    sendMessage(SendWhats.Client_Connected, null);
                    break;
                case SocketCmd.Client_Disconnected:
                case SocketCmd.Server_Closed:
                    sendMessage(SendWhats.Client_Disconnected, null);
                    break;
                default:
                    sendMessage(SendWhats.Client_ReadMessage, msg);
                    break;
            }
        }

        @Override
        public void onSend(SocketClient client , SocketMessage msg) {}
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

    private IndexActivity getParent(){
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
                    etClientId.setEnabled(false);
                    etServerIp.setEnabled(false);
                    etServerPort.setEnabled(false);
                    //设置首页客户端引用
                    getParent().register(socketClient);
                    //提交客户端Id
                    socketClient.send(SocketCmd.Client_Bind , socketClient.getId());
                    Toast.makeText(getActivity() , "连接服务器成功！" , Toast.LENGTH_SHORT).show();
                    break;
                case SendWhats.Client_Disconnected:
                    etClientId.setEnabled(true);
                    etServerIp.setEnabled(true);
                    etServerPort.setEnabled(true);
                    swClient.setChecked(false);
                    Toast.makeText(getActivity() , "已从服务器断开连接！" , Toast.LENGTH_SHORT).show();
                    break;
                case SendWhats.Client_ReadMessage:
                    if (msg.obj != null) {
                        getParent().router((SocketMessage) msg.obj);
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
