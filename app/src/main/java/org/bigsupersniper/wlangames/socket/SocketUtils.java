package org.bigsupersniper.wlangames.socket;

import java.nio.charset.Charset;

/**
 * Created by linfeng on 2014/7/26.
 */
public class SocketUtils {
    public final static String EndChar = "\0";
    public final static int EndByte = 0;
    public final static int BufferSize = 1024;
    public final static Charset MessageCharset = Charset.forName("utf-8");
    public final static int SocketPoolSize = 20;

    public final static String Cmd_BluffDice = "bluffdice";
    public final static String Cmd_CPoker = "cpoker";
}
