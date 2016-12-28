package com.github.ematiyuk.expensetracer.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;

import com.github.ematiyuk.expensetracer.fragments.CategoryFragment;
import com.github.ematiyuk.expensetracer.R;
import com.github.ematiyuk.expensetracer.fragments.ReportFragment;
import com.github.ematiyuk.expensetracer.fragments.TodayFragment;

public class MainActivity extends BaseFragmentActivity {
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavDrawer;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavDrawer = (NavigationView) findViewById(R.id.nav_drawer);
        mDrawerToggle = setupDrawerToggle();

        // Tie DrawerLayout events to the ActionBarToggle
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        // Setup drawer view
        setupDrawerContent(mNavDrawer);

        // Select TodayFragment on app start by default
        loadTodayFragment();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeNavigationDrawer();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggle
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (!closeNavigationDrawer()) {
            Fragment currentFragment = getSupportFragmentManager()
                    .findFragmentById(R.id.content_frame);
            if (!(currentFragment instanceof TodayFragment)) {
                loadTodayFragment();
            } else {
                // If current fragment is TodayFragment then exit
                super.onBackPressed();
            }
        }
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.drawer_open,  R.string.drawer_close);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    private void selectDrawerItem(MenuItem menuItem) {
        closeNavigationDrawer();
        switch(menuItem.getItemId()) {
            case R.id.nav_today:
                loadFragment(TodayFragment.class, menuItem.getItemId(), menuItem.getTitle());
                break;
            case R.id.nav_report:
                loadFragment(ReportFragment.class, menuItem.getItemId(), menuItem.getTitle());
                break;
            case R.id.nav_categories:
                loadFragment(CategoryFragment.class, menuItem.getItemId(), menuItem.getTitle());
                break;
            case R.id.nav_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            default:
                loadFragment(TodayFragment.class, menuItem.getItemId(), menuItem.getTitle());
        }
    }

    private boolean closeNavigationDrawer() {
        boolean drawerIsOpen = mDrawerLayout.isDrawerOpen(GravityCompat.START);
        if (drawerIsOpen) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        return drawerIsOpen;
    }

    public void hideNavigationBar() {
        closeNavigationDrawer();
    }

    private void loadFragment(Class fragmentClass, @IdRes int navDrawerCheckedItemId,
                              CharSequence toolbarTitle) {
        Fragment fragment = null;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        insertFragment(fragment);

        // Highlight the selected item
        mNavDrawer.setCheckedItem(navDrawerCheckedItemId);
        // Set action bar title
        setTitle(toolbarTitle);
    }

    private void loadTodayFragment() {
        loadFragment(TodayFragment.class, R.id.nav_today,
                getResources().getString(R.string.nav_today));
    }
}

