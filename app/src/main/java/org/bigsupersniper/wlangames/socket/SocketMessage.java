package org.bigsupersniper.wlangames.socket;

import com.google.gson.annotations.SerializedName;

/**
 * Created by linfeng on 2014/7/27.
 */
public class SocketMessage {

    @SerializedName("from")
    private String from;
    @SerializedName("to")
    private String to;
    @SerializedName("cmd")
    private String cmd;
    @SerializedName("body")
    private String body = "";

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
