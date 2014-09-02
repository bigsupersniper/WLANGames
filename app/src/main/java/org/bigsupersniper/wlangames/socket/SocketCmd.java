package org.bigsupersniper.wlangames.socket;

/**
 * Created by linfeng on 2014/7/28.
 */
public class SocketCmd {

    //服务端相关 0x1 ~ 0x800
    public final static int Server_Closed = 0x1;

    //客户端相关 0x801 ~ 0x1000
    public final static int Client_Connected = 0x801;
    public final static int Client_Disconnected = 0x802;
    public final static int Client_Bind = 0x803;
    public final static int Client_Bind_Resp = 0x804;

    //骰子相关 0x10001 ~ 0x10800
    public final static int BluffDice_Send = 0x10001;
    public final static int BluffDice_Send_Resp = 0x10002;
    public final static int BluffDice_Open = 0x10003;
    public final static int BluffDice_Open_Resp = 0x10004;

}
