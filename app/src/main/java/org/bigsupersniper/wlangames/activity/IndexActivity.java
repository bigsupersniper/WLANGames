package org.bigsupersniper.wlangames.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.bigsupersniper.wlangames.R;
import org.bigsupersniper.wlangames.router.ServerRouter;
import org.bigsupersniper.wlangames.socket.SocketClient;
import org.bigsupersniper.wlangames.socket.SocketCmd;
import org.bigsupersniper.wlangames.socket.SocketMessage;
import org.bigsupersniper.wlangames.socket.SocketServer;

import java.util.Locale;

public class IndexActivity extends Activity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    GamePagerAdapter mGamePagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    ActionBar actionBar;
    private GameServerFragment gameServerFragment;
    private BluffDiceFragment bluffDiceFragment;
    private CPokerFragment cPokerFragment;
    private SocketServer socketServer;
    private ServerRouter serverRouter;
    private SocketClient socketClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        // Set up the action bar.
        actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mGamePagerAdapter = new GamePagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mGamePagerAdapter);
        //必须设置，否则加载的page状态不保存
        mViewPager.setOffscreenPageLimit(mGamePagerAdapter.getCount());

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mGamePagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(actionBar.newTab().setText(mGamePagerAdapter.getPageTitle(i)).setTabListener(this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.index, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (socketServer != null && socketServer.isStarted()) {
            menu.getItem(0).setVisible(true);
            menu.getItem(1).setVisible(mViewPager.getCurrentItem() != 0);
        } else {
            menu.getItem(0).setVisible(false);
            menu.getItem(1).setVisible(false);
        }

        if (socketClient != null) {
            menu.getItem(2).setVisible(true);
        } else {
            menu.getItem(2).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_server_status:
                if (socketServer != null && socketServer.isStarted()) {
                    String[] ips = socketServer.getIPList();
                    if (ips.length > 0) {
                        new AlertDialog.Builder(this).setTitle("在线客户端列表").setItems(ips, null)
                                .setNegativeButton("确定", null).show();
                    } else {
                        Toast.makeText(this, "没有已连接的客户端！", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.action_client_status:
                if (socketClient != null) {
                    if (socketClient.isConnected()) {
                        String[] infos = new String[2];
                        infos[0] = "本机标识：" + socketClient.getId();
                        infos[1] = "本机地址：" + socketClient.getLocalIP();
                        new AlertDialog.Builder(this).setTitle("本机连接状态").setItems(infos, null)
                                .setNegativeButton("确定", null).show();
                    } else {
                        Toast.makeText(this, "未连接服务器！", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.action_next:
                if (socketServer != null) {
                    switch (mViewPager.getCurrentItem()) {
                        case 1:
                            serverRouter.broadcast(SocketCmd.BluffDice_Send);
                            break;
                        case 2:
                            //serverRouter.broadcast(SocketCmd.CPoker_Send);
                            Toast.makeText(this, "未完全实现", Toast.LENGTH_LONG).show();
                            break;
                    }
                } else {
                    Toast.makeText(this, "服务未启动！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_exit:
                this.finish();
                System.exit(0);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public void register(SocketServer socketServer) {
        this.socketServer = socketServer;
    }

    public void register(ServerRouter serverRouter) {
        this.serverRouter = serverRouter;
    }

    public void register(SocketClient socketClient) {
        this.socketClient = socketClient;
    }

    public void sendCmd(int cmd) {
        if (socketClient != null) {
            SocketMessage msg = new SocketMessage();
            msg.setFrom(socketClient.getLocalIP());
            msg.setTo(socketClient.getRemoteIP());
            msg.setCmd(cmd);
            socketClient.send(msg);
        }
    }

    public void router(SocketMessage msg) {
        switch (msg.getCmd()) {
            case SocketCmd.BluffDice_Send:
            case SocketCmd.BluffDice_Open_Resp:
                mViewPager.setCurrentItem(1);
                bluffDiceFragment.router(msg);
                break;
            case SocketCmd.CPoker_Send:
                mViewPager.setCurrentItem(2);
                cPokerFragment.router(msg);
            default:
                break;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class GamePagerAdapter extends FragmentPagerAdapter {

        public GamePagerAdapter(FragmentManager fm) {
            super(fm);

            gameServerFragment = new GameServerFragment();
            bluffDiceFragment = new BluffDiceFragment();
            cPokerFragment = new CPokerFragment();
        }

        @Override
        public Fragment getItem(int position) {
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
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

}
