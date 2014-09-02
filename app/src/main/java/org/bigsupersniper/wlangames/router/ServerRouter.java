package org.bigsupersniper.wlangames.router;

import com.google.gson.Gson;

import org.bigsupersniper.wlangames.common.BluffDice;
import org.bigsupersniper.wlangames.common.BluffDiceHistory;
import org.bigsupersniper.wlangames.socket.SocketClient;
import org.bigsupersniper.wlangames.socket.SocketCmd;
import org.bigsupersniper.wlangames.socket.SocketMessage;
import org.bigsupersniper.wlangames.socket.SocketServer;

import java.util.Iterator;
import java.util.List;

/**
 * Created by linfeng on 2014/8/9.
 */
public class ServerRouter {

    private SocketServer server;

    public ServerRouter(SocketServer server) {
        this.server = server;
    }

    public void broadcast(int cmd) {
        List<SocketClient> clients = server.getList();
        if (clients.size() > 0) {
            Iterator<SocketClient> iterator = clients.iterator();
            switch (cmd) {
                case SocketCmd.BluffDice_Send:
                    //clear history
                    BluffDiceHistory.getInstance().reset();
                    while (iterator.hasNext()) {
                        SocketClient client = iterator.next();
                        int[] dices = BluffDice.shake();
                        String body = new Gson().toJson(dices);
                        String ip = client.getLocalIP();
                        String key = client.getId() + "( " + ip.substring(ip.lastIndexOf("."), ip.lastIndexOf(":")) + " )";
                        BluffDiceHistory.getInstance().add(key, dices);
                        client.send(SocketCmd.BluffDice_Send, body);
                    }
                    break;
            }
        }
    }

    public void broadcast(int cmd, String body) {
        List<SocketClient> clients = server.getList();
        if (clients.size() > 0) {
            Iterator<SocketClient> iterator = clients.iterator();
            while (iterator.hasNext()) {
                iterator.next().send(cmd, body);
            }
        }
    }

    public void router(SocketClient client, SocketMessage msg) {
        int cmd = msg.getCmd();
        switch (cmd) {
            case SocketCmd.Client_Bind:
                client.setId(msg.getBody());
                break;
            case SocketCmd.BluffDice_Open:
                broadcast(SocketCmd.BluffDice_Open_Resp, new Gson().toJson(BluffDiceHistory.getInstance().getAll()));
                break;
            default:
                break;
        }
    }

}
