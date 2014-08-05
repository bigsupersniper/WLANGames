package org.bigsupersniper.wlangames.socket;

/**
 * Created by linfeng on 2014/7/28.
 */
public class SocketCmd {
    //客户端相关
    public final static String Connected = "connected";
    public final static String Disconnected = "disconnected";
    public final static String SetClientId = "setclientid";
    //骰子相关
    public final static String BluffDice = "bluffdice";
    public final static String BluffDice_Open = "bluffdice_open";
    public final static String BluffDice_Open_Resp = "bluffdice_open_resp";
    //十三水相关
    public final static String CPoker = "cpoker";

}
