package org.twinone.locker.ui;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.twinone.locker.R;

import org.twinone.locker.lock.AppLockService;
import org.twinone.locker.lock.LockService;
import org.twinone.locker.ui.dialogs.ChoosePasswordDialog;
import org.twinone.locker.util.PrefUtils;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private static final String EXTRA_UNLOCKED = "com.twinone.locker.unlocked";

    private Fragment mCurrentFragment;
//	/**
//	 * Fragment managing the behaviors, interactions and presentation of the
//	 * navigation drawer.
//	 */
//	private NavigationFragment mNavFragment;

    /**
     * Used to store the last screen title. For use in
     * .
     */
    private CharSequence mTitle;

    private Toolbar mToolbar;
    private BroadcastReceiver mReceiver;
    private IntentFilter mFilter;
    private NavigationView mNavView;
    private DrawerLayout mDrawerLayout;
    private ImageView mLockStateImage;
    private TextView mLockStateDesc;
    private TextView mLockStateInfoMessage;
    private int mNavSelected = R.id.nav_apps; // 0 is an invalid resId

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id != mNavSelected) {
            switch (id) {
                case R.id.nav_apps:
                    navigateToFragment(new AppsFragment());
                    break;
                case R.id.nav_change:
                    Log.d("ShowPass", "from navigation");
                    new ChoosePasswordDialog().show(getSupportFragmentManager(), "show_password_dialog");
                    // Don't close drawer here, and don't select the menu item
                    return false;
                case R.id.nav_settings:
                    navigateToFragment(new SettingsFragment());
                    break;

            }
        }

        mNavSelected = id;
        mDrawerLayout.closeDrawers();
        item.setChecked(true);
        return true;
    }

    private void navigateToFragment(Fragment f) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, f).commit();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lock_state_image:
                if (toggleService()) {
                    dismissLockInfoMessage();
                }
                break;
        }
    }

    void dismissLockInfoMessage() {
        mLockStateInfoMessage.setVisibility(View.GONE);
        getPreferences(MODE_PRIVATE).edit().putBoolean("main_nav_info_onetime_learned", true).apply();
    }

    private class ServiceStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateLayout();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handleIntent();

        mReceiver = new ServiceStateReceiver();
        mFilter = new IntentFilter();
        mFilter.addCategory(AppLockService.CATEGORY_STATE_EVENTS);
        mFilter.addAction(AppLockService.BROADCAST_SERVICE_STARTED);
        mFilter.addAction(AppLockService.BROADCAST_SERVICE_STOPPED);

        mTitle = getTitle();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mNavView = (NavigationView) findViewById(R.id.navView);
        mNavView.setNavigationItemSelectedListener(this);
        mLockStateImage = (ImageView) mNavView.findViewById(R.id.lock_state_image);
        mLockStateDesc = (TextView) mNavView.findViewById(R.id.lock_state_desc);

        mLockStateInfoMessage = (TextView) mNavView.findViewById(R.id.lock_state_info_message);
        if (getPreferences(MODE_PRIVATE).getBoolean("main_nav_info_onetime_learned", false)) {
            mLockStateInfoMessage.setVisibility(View.GONE);
        }
        mLockStateImage.setOnClickListener(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle dt = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar,
                0, 0
        );
        mDrawerLayout.setDrawerListener(dt);
        dt.syncState();

        mCurrentFragment = new AppsFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, mCurrentFragment).commit();


        setupPassword();
        showLockerIfNotUnlocked(false);

    }

    @Override
    protected void onResume() {
        super.onResume();
        showLockerIfNotUnlocked(true);
        registerReceiver(mReceiver, mFilter);
        updateLayout();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LockService.hide(this);
        unregisterReceiver(mReceiver);

        // We have to finish here or the system will assign a lower priority to
        // the app (since 4.4?)
        finish();
    }

    @Override
    protected void onDestroy() {
        Log.v("Main", "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("", "onNewIntent");
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent();
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        mTitle = title;
        getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.global, menu);
        return true;
    }


    private void showLockerIfNotUnlocked(boolean relock) {
        boolean unlocked = getIntent().getBooleanExtra(EXTRA_UNLOCKED, false);
        if (new PrefUtils(this).isCurrentPasswordEmpty()) {
            unlocked = true;
        }
        if (!unlocked) {
            LockService.showCompare(this, getPackageName());
        }
        getIntent().putExtra(EXTRA_UNLOCKED, !relock);
    }

    private void updateLayout() {
        boolean on = AppLockService.isRunning(this);
        mLockStateImage.setImageDrawable(getResources().getDrawable(on
                ? R.drawable.lock_hole
                : R.drawable.lock_open_hole));
        mLockStateDesc.setText(getString(on ? R.string.main_nav_locked : R.string.main_nav_unlocked));
    }

    /**
     * Handle this Intent for searching...
     */
    private void handleIntent() {
        if (getIntent() != null && getIntent().getAction() != null) {
            if (getIntent().getAction().equals(Intent.ACTION_SEARCH) && mCurrentFragment instanceof AppsFragment) {
                final String query = getIntent().getStringExtra(
                        SearchManager.QUERY);
                if (query != null) {
                    ((AppsFragment) mCurrentFragment).onSearch(query);
                }
            }
        }

    }

    private boolean toggleService() {
        if (AppLockService.isRunning(this)) {
            AppLockService.stop(this);
            return false;
        } else if (setupPassword()) {
            AppLockService.start(this);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Ask the user to setup his password
     *
     * @return True if there's already a password setup, false if the password is about to be created
     */
    boolean setupPassword() {
        if (!new PrefUtils(this).isCurrentPasswordEmpty())
            return true;
        try {
            throw new RuntimeException("");
        } catch(Exception e) {
            Log.d("ShowPass", "from setup: ", e);
        }

        new ChoosePasswordDialog().show(getSupportFragmentManager(), "show_password_dialog");
        return false;
    }


    private void toGooglePlay() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + getPackageName()));
        if (getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY).size() >= 1) {
            startActivity(intent);
        }
    }
}
