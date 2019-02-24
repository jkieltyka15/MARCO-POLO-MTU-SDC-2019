package edu.mtu.polofirstresponder;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.karan.churi.PermissionManager.PermissionManager;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private PermissionManager permissionManager;            //used for checking permissions
    private FirebaseAuth mAuth;                             //Firebase authenticator
    private FirebaseAuth.AuthStateListener mAuthListener;   //Firebase authentication state listener
    private static final String TAG = "Navigation";         //tag for logfile

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        permissionManager = new PermissionManager() {};
        permissionManager.checkAndRequestPermissions(this);

        //if the user is currently not signed in, go to the sign in page
        if ( FirebaseAuth.getInstance().getCurrentUser() == null ) {
            this.startActivity(new Intent( NavigationActivity.this, LoginActivity.class ));
            this.finish();
        }

        //Initialize Firebase and check to see the user's status
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //current user is signed in
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                }

                //user is currently not signed in
                else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    mAuth.signOut();                                                                        //sign out the Firebase user
                    startActivity(new Intent(NavigationActivity.this, LoginActivity.class));   //start the login activity
                }
            }
        };

        //default view is map
        FragmentManager fragmentManager = getSupportFragmentManager();     //used to determine what fragment should be displayed
        setTitle("Map");                                                   //change the title to map
        fragmentManager.beginTransaction()                                 //change view to map
                .replace(R.id.navBackground, new MapFragment())
                .commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionManager.checkResult(requestCode,permissions,grantResults);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        FragmentManager fragmentManager = getSupportFragmentManager(); //used to determine what fragment should be displayed

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //go to the active shooter map fragment
        if (id == R.id.nav_map) {
            setTitle("Map");                                                                        //change the title to map
            fragmentManager.beginTransaction()                                                      //change view to map
                    .replace(R.id.navBackground, new MapFragment())
                    .commit();
        }

        //go to settings fragment
        else if (id == R.id.nav_settings) {
            setTitle("Settings");                                                                   //change the title to settings
            fragmentManager.beginTransaction()                                                      //change view to settings
                    .replace(R.id.navBackground, new SettingsFragment())
                    .commit();
        }

        //logout the current user
        else if (id == R.id.nav_logout) {
            setTitle("Log Out");                                                                    //change the title to log out
            mAuth.signOut();                                                                        //sign out the Firebase user
            startActivity(new Intent(NavigationActivity.this, LoginActivity.class));   //start the login activity
            this.finish();                                                                          //end the current activity
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
