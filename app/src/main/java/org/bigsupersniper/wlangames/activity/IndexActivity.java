package org.bigsupersniper.wlangames.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.bigsupersniper.wlangames.R;
import org.bigsupersniper.wlangames.router.ServerRouter;
import org.bigsupersniper.wlangames.socket.SocketClient;
import org.bigsupersniper.wlangames.socket.SocketCmd;
import org.bigsupersniper.wlangames.socket.SocketMessage;
import org.bigsupersniper.wlangames.socket.SocketServer;


public class IndexActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private GameServerFragment gameServerFragment;
    private BluffDiceFragment bluffDiceFragment;
    private CPokerFragment cPokerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        //初始化页面
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        gameServerFragment = new GameServerFragment();
        bluffDiceFragment = new BluffDiceFragment();
        cPokerFragment = new CPokerFragment();
        transaction.add(R.id.container , gameServerFragment);
        transaction.add(R.id.container , bluffDiceFragment);
        transaction.add(R.id.container , cPokerFragment);
        hideAllFragment(transaction);
        transaction.show(gameServerFragment);
        transaction.commit();
    }

    private void hideAllFragment(FragmentTransaction transaction){
        transaction.hide(gameServerFragment);
        transaction.hide(bluffDiceFragment);
        transaction.hide(cPokerFragment);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment fragment = null;

        switch (position) {
            case 0:
                fragment = gameServerFragment;
                break;
            case 1:
                fragment = bluffDiceFragment;
                break;
            case 2:
                fragment = cPokerFragment;
                break;
            default:
                break;
        }
        hideAllFragment(transaction);
        transaction.show(fragment);
        transaction.commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.index, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            if (socketServer != null && socketServer.isStarted()) {
                menu.getItem(0).setVisible(true);
                menu.getItem(1).setVisible(!gameServerFragment.isVisible());
            } else {
                menu.getItem(0).setVisible(false);
                menu.getItem(1).setVisible(false);
            }

            if(socketClient != null){
                menu.getItem(2).setVisible(true);
            }else{
                menu.getItem(2).setVisible(false);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.action_server_status:
                if (socketServer != null && socketServer.isStarted()){
                    String[] ips = socketServer.getIPList();
                    if (ips.length > 0){
                        new AlertDialog.Builder(this).setTitle("在线客户端列表").setItems(ips, null)
                                .setNegativeButton("确定", null).show();
                    }else {
                        Toast.makeText(this , "没有已连接的客户端！", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.action_client_status:
                if (socketClient != null){
                    if (socketClient.isConnected()){
                        String[] infos = new String[2];
                        infos[0] = "本机标识：" + socketClient.getId();
                        infos[1] = "本机地址：" + socketClient.getLocalIP();
                        new AlertDialog.Builder(this).setTitle("本机连接状态").setItems(infos, null)
                                .setNegativeButton("确定", null).show();
                    }else {
                        Toast.makeText(this , "未连接服务器！", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.action_next:
                if (socketServer != null){
                    if (bluffDiceFragment.isVisible()){
                        serverRouter.broadcast(SocketCmd.BluffDice_Send);
                    }else if(cPokerFragment.isVisible()){
                        serverRouter.broadcast(SocketCmd.CPoker_Send);
                    }
                }else{
                    Toast.makeText(this , "服务未启动！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_exit:
                this.finish();
                System.exit(0);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private SocketServer socketServer;
    private ServerRouter serverRouter;
    private SocketClient socketClient;

    public void register(SocketServer socketServer){
        this.socketServer = socketServer;
    }

    public void register(ServerRouter serverRouter){
        this.serverRouter = serverRouter;
    }

    public void register(SocketClient socketClient){
        this.socketClient = socketClient;
    }

    public void sendCmd(int cmd){
        if (socketClient != null){
            SocketMessage msg = new SocketMessage();
            msg.setFrom(socketClient.getLocalIP());
            msg.setTo(socketClient.getRemoteIP());
            msg.setCmd(cmd);
            socketClient.send(msg);
        }
    }

    public void router(SocketMessage msg){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        switch (msg.getCmd()){
            case SocketCmd.BluffDice_Send:
            case SocketCmd.BluffDice_Open_Resp:
                hideAllFragment(transaction);
                transaction.show(bluffDiceFragment);
                bluffDiceFragment.router(msg);
                break;
            case SocketCmd.CPoker_Send:
                hideAllFragment(transaction);
                transaction.show(cPokerFragment);
                cPokerFragment.router(msg);
                break;
            default:
                break;
        }
        transaction.commit();
    }

}
