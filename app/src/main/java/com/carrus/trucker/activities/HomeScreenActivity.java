package com.carrus.trucker.activities;


import android.app.Dialog;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.carrus.trucker.R;
import com.carrus.trucker.adapters.NavDrawerListAdapter;
import com.carrus.trucker.models.NavDrawerItem;
import com.carrus.trucker.fragments.BookingsFragment;
import com.carrus.trucker.fragments.CurrentShipmentFragment;
import com.carrus.trucker.fragments.DriverProfileFragment;
import com.carrus.trucker.retrofit.RestClient;
import com.carrus.trucker.services.MyService;
import com.carrus.trucker.utils.CommonUtils;
import com.carrus.trucker.utils.InternetConnectionStatus;
import com.carrus.trucker.utils.Prefs;
import com.carrus.trucker.utils.Transactions;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class HomeScreenActivity extends BaseActivity implements View.OnClickListener {

    private DrawerLayout mDrawerLayout;
    private ListView lvNavMenuList;
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;
    private CircleImageView ivProfilePicture;
    private TextView tvDriverName, tvHeaderTitle;
    private int lastSelectedScreen = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        setupUI(getWindow().getDecorView().getRootView());
        init();
        setDriverData();
        if (savedInstanceState == null) {
            displayView(0);
        }
    }

    /**
     * Method for set driver profile in sliding menu
     * */
    private void setDriverData() {
        tvDriverName.setText(Prefs.with(this).getString(DRIVAR_NAME, "Test"));
        String path = Prefs.with(this).getString(DRIVER_IMAGE, "");
        if (!path.equals("")) {
            Picasso.with(this)
                    .load(Prefs.with(this).getString(DRIVER_IMAGE, "")).fit()
                    .centerCrop().memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).skipMemoryCache()
                    .placeholder(R.mipmap.icon_placeholder)// optional
                    .into(ivProfilePicture);
        }
    }

    /**
     * Method to initialize all the {@link View}s inside the Layout of this
     * {@link Activity}
     */
    private void init() {
        //Get Resource ID from XML
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        lvNavMenuList = (ListView) findViewById(R.id.lvNavMenuList);
        ivProfilePicture = (CircleImageView) findViewById(R.id.ivProfilePicture);
        tvDriverName = (TextView) findViewById(R.id.tvDriverName);
        tvHeaderTitle = (TextView) findViewById(R.id.tvHeaderTitle);

        //Initialize variables
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
        navDrawerItems = new ArrayList<NavDrawerItem>();
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
        navMenuIcons.recycle();
        adapter = new NavDrawerListAdapter(HomeScreenActivity.this, navDrawerItems);

        //Set Adapter
        lvNavMenuList.setAdapter(adapter);

        //Set Listeners
        findViewById(R.id.ivNavigationDrawerButton).setOnClickListener(this);
        findViewById(R.id.rlDriverProfile).setOnClickListener(this);
        lvNavMenuList.setOnItemClickListener(new SlideMenuClickListener());

        //Set Listview height dynamically
        CommonUtils.setListViewHeightBasedOnChildren(lvNavMenuList);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivNavigationDrawerButton:
                mDrawerLayout.openDrawer(Gravity.LEFT);
                break;

            case R.id.rlDriverProfile:
                if (lastSelectedScreen != 5) {
                    lastSelectedScreen = 5;
                    tvHeaderTitle.setText(getString(R.string.my_profile));
                    Fragment fragment = new DriverProfileFragment();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frame_container, fragment).commit();
                }
                mDrawerLayout.closeDrawer(GravityCompat.START);

                break;


        }
    }

    private class SlideMenuClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            if (lastSelectedScreen != position)
                displayView(position);
            else
                mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    /**
     * Method to display view of fragment
     * */
    private void displayView(int position) {
        lastSelectedScreen = position;
        mDrawerLayout.closeDrawer(GravityCompat.START);
        Fragment fragment = null;
        switch (position) {
            case 0:
                tvHeaderTitle.setText(getResources().getString(R.string.current_shipment));
                fragment = new CurrentShipmentFragment();
                break;
            case 1:
                tvHeaderTitle.setText(getResources().getString(R.string.my_schedule));
                fragment = new BookingsFragment();
                break;
            case 2:
                lastSelectedScreen = 6;
                CommonUtils.phoneCall(HomeScreenActivity.this, Prefs.with(this).getString(FLEET_OWNER_NO, ""));
                break;
            case 3:
                logoutPopup();
                break;
            default:
                break;
        }
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragment).commit();
        }
    }

    /**
     * Method for logout API
     */
    public void logout() {

        if (InternetConnectionStatus.getInstance(this).isOnline()) {
            CommonUtils.showLoadingDialog(HomeScreenActivity.this, getString(R.string.please_wait));
            RestClient.getWebServices().logoutDriver(accessToken,/*accessToken,*/
                    new Callback<String>() {
                        @Override
                        public void success(String serverResponse, Response response) {
                            try {
                                JSONObject jsonObject = new JSONObject(serverResponse);
                                Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                        Intent.FLAG_ACTIVITY_NEW_TASK);
                                Prefs.with(HomeScreenActivity.this).removeAll();
                                CommonUtils.dismissLoadingDialog();
                                startActivity(intent);
                                Transactions.showPreviousAnimation(HomeScreenActivity.this);
                                finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                                CommonUtils.dismissLoadingDialog();
                            }
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            CommonUtils.dismissLoadingDialog();
                            CommonUtils.showRetrofitError(HomeScreenActivity.this, retrofitError);
                        }
                    });
        }

    }

    @Override
    public void onBackPressed() {
        if (this.mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            showExitPopup();
        }
    }

    /**
     * Method for show exit popup
     */
    public void showExitPopup() {
        final Dialog dialog = new Dialog(HomeScreenActivity.this, android.R.style.Theme_Translucent_NoTitleBar);

        //setting custom layout to dialog
        dialog.setContentView(R.layout.two_button_custom_layout);
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.dimAmount = 0.7f; // Dim level. 0.0 - no dim, 1.0 - completely opaque
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        //adding text dynamically
        TextView txt = (TextView) dialog.findViewById(R.id.textMessage);
        txt.setText("Do you want to exit application?");

        //adding button click event
        Button yesButton = (Button) dialog.findViewById(R.id.yes_button);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
            }
        });
        Button noButton = (Button) dialog.findViewById(R.id.no_button);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    /**
     * Method to show logout popup
     * */
    public void logoutPopup() {

        final Dialog dialog = new Dialog(HomeScreenActivity.this, android.R.style.Theme_Translucent_NoTitleBar);

        //setting custom layout to dialog
        dialog.setContentView(R.layout.two_button_custom_layout);
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.dimAmount = 0.7f; // Dim level. 0.0 - no dim, 1.0 - completely opaque
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        //adding text dynamically
        TextView txt = (TextView) dialog.findViewById(R.id.textMessage);
        txt.setText("Do you want to logout?");

        //adding button click event
        Button yesButton = (Button) dialog.findViewById(R.id.yes_button);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastSelectedScreen = 0;
                dialog.dismiss();
                logout();
//                finish();
//                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
            }
        });
        Button noButton = (Button) dialog.findViewById(R.id.no_button);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                lastSelectedScreen = 0;
            }
        });
        dialog.show();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(HomeScreenActivity.this, MyService.class));
    }
}
