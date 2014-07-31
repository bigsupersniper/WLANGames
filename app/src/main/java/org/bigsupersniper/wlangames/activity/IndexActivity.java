package org.bigsupersniper.wlangames.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.bigsupersniper.wlangames.R;
import org.bigsupersniper.wlangames.common.FragmentTags;
import org.bigsupersniper.wlangames.common.SendWhats;
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
    }

    public void hideAllFragments(FragmentTransaction transaction ){
        findViewById(R.id.layoutIndex).setVisibility(View.INVISIBLE);
        transaction.hide(gameServerFragment);
        transaction.hide(bluffDiceFragment);
        transaction.hide(cPokerFragment);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getFragmentManager();
        if (gameServerFragment == null){
            gameServerFragment = (GameServerFragment)fragmentManager.findFragmentByTag(FragmentTags.GameServer);
        }
        if (bluffDiceFragment == null){
            bluffDiceFragment = (BluffDiceFragment)fragmentManager.findFragmentByTag(FragmentTags.BluffDice);
        }
        if (cPokerFragment == null){
            cPokerFragment = (CPokerFragment)fragmentManager.findFragmentByTag(FragmentTags.CPoker);
        }

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        this.hideAllFragments(transaction);
        switch (position) {
            case 0:
                transaction.show(gameServerFragment);
                break;
            case 1:
                transaction.show(bluffDiceFragment);
                break;
            case 2:
                transaction.show(cPokerFragment);
                break;
            default:
                break;
        }
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
        }

        return true;
    }

    private SocketServer socketServer;
    public void setSocketServer(SocketServer socketServer){
        this.socketServer = socketServer;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.action_status:
                if (socketServer != null && socketServer.isStarted()){
                    String[] ips = socketServer.getList();
                    String[] items = new String[ips.length + 1];
                    if (ips.length > 0){
                        new AlertDialog.Builder(this).setTitle("在线客户端列表").setItems(ips, null)
                                .setNegativeButton("确定", null).show();
                    }else {
                        Toast.makeText(this , "没有已连接的客户端！", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.action_next:
                if (socketServer != null){
                    if (bluffDiceFragment.isVisible()){
                        socketServer.broadcast(SendWhats.Broadcast_BluffDice);
                    }else if(cPokerFragment.isVisible()){
                        socketServer.broadcast(SendWhats.Broadcast_CPoker);
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
}
