package org.bigsupersniper.wlangames.activity;

import android.app.Activity;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

import org.bigsupersniper.wlangames.R;


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
            gameServerFragment = (GameServerFragment)fragmentManager.findFragmentByTag("GameServerFragment");
        }
        if (bluffDiceFragment == null){
            bluffDiceFragment = (BluffDiceFragment)fragmentManager.findFragmentByTag("BluffDiceFragment");
        }
        if (cPokerFragment == null){
            cPokerFragment = (CPokerFragment)fragmentManager.findFragmentByTag("CPokerFragment");
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
