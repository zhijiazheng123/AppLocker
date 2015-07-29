package org.twinone.locker.ui;

import android.app.Activity;
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
import org.twinone.locker.util.PrefUtils;
import org.twinone.util.DialogSequencer;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private static final String EXTRA_UNLOCKED = "com.twinone.locker.unlocked";

    private DialogSequencer mSequencer;
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
                    Dialogs.getChangePasswordDialog(this).show();
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
                toggleService();
                break;
        }
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

        mSequencer = new DialogSequencer();
        showDialogs();
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
        mSequencer.stop();

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

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.global, menu);
        return true;
    }

    /**
     * Provide a way back to {@link MainActivity} without having to provide a
     * password again. It finishes the calling {@link Activity}
     *
     * @param context
     */
    public static void showWithoutPassword(Context context) {
        Intent i = new Intent(context, MainActivity.class);
        i.putExtra(EXTRA_UNLOCKED, true);
        if (!(context instanceof Activity)) {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(i);
    }


    /**
     * @return True if the service is allowed to start
     */
    private boolean showDialogs() {
        boolean deny = false;

        // Recovery code
        mSequencer.addDialog(Dialogs.getRecoveryCodeDialog(this));

        // Empty password
        deny = Dialogs.addEmptyPasswordDialog(this, mSequencer);

        mSequencer.start();
        return !deny;
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


    private void toggleService() {
        boolean on = false;
        if (AppLockService.isRunning(this)) {
            Log.d("", "toggleService() Service is running, now stopping");
            AppLockService.stop(this);
        } else if (Dialogs.addEmptyPasswordDialog(this, mSequencer)) {
            mSequencer.start();
        } else {
            on = true;
            AppLockService.start(this);
        }
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
