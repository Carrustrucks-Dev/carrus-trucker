package com.carrus.trucker.activities;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.carrus.trucker.R;
import com.carrus.trucker.adapters.NavDrawerListAdapter;
import com.carrus.trucker.entities.NavDrawerItem;
import com.carrus.trucker.fragments.BookingsFragment;
import com.carrus.trucker.fragments.CurrentShipmentFragment;
import com.carrus.trucker.fragments.DriverProfile;
import com.carrus.trucker.interfaces.HomeCallback;
import com.carrus.trucker.interfaces.WebServices;
import com.carrus.trucker.retrofit.RestClient;
import com.carrus.trucker.services.MyService;
import com.carrus.trucker.utils.CommonUtils;
import com.carrus.trucker.utils.Connectivity;
import com.carrus.trucker.utils.Log;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class HomeScreen extends BaseActivity implements HomeCallback {

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;
    private CircleImageView profileImage;
    private SharedPreferences sharedPreferences;
    private TextView driverName;
    private TextView headerTitle;
    public Connectivity connectivity;
    public CommonUtils commonUtils;
    public String accessToken;
    public LinearLayout errorLayout;
    private int lastSelectedScreen=0;
    private Bundle bundle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        setupUI(getWindow().getDecorView().getRootView());
        init();
        View.OnClickListener handler = new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent;
                switch (v.getId()) {

                    case R.id.navigation_drawer_button:
                        mDrawerLayout.openDrawer(Gravity.LEFT);
                        break;

                    case R.id.driverProfile:
                        if(lastSelectedScreen!=5) {
                            lastSelectedScreen=5;
                            headerTitle.setText(getResources().getString(R.string.my_profile));
                            Fragment fragment = new DriverProfile();
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.frame_container, fragment).commit();
                            mDrawerLayout.closeDrawer(GravityCompat.START);
                        }
                        else{
                            mDrawerLayout.closeDrawer(GravityCompat.START);
                        }

                }
            }
        };
        driverName.setText(sharedPreferences.getString(DRIVAR_NAME, "Test"));
        try{
            String path=sharedPreferences.getString(DRIVER_IMAGE, null);
            if(path.equals(null)){

            }else{
                Picasso.with(this)
                        .load(sharedPreferences.getString(DRIVER_IMAGE, "")).fit()
                        .centerCrop().memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).skipMemoryCache()
                        .placeholder(R.mipmap.icon_placeholder)// optional
                        .into(profileImage);
            }
        }catch(Exception e){

        }

        if (savedInstanceState == null) {
            displayView(0);
        }
        findViewById(R.id.navigation_drawer_button).setOnClickListener(handler);
        findViewById(R.id.driverProfile).setOnClickListener(handler);
        setListViewHeightBasedOnChildren(mDrawerList);
    }

    private void init() {

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navMenuList);
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);
        navDrawerItems = new ArrayList<NavDrawerItem>();
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
        navMenuIcons.recycle();
        adapter = new NavDrawerListAdapter(HomeScreen.this, navDrawerItems);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
        profileImage = (CircleImageView) findViewById(R.id.profile_picture);
        driverName = (TextView) findViewById(R.id.driverName);
        sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        accessToken = sharedPreferences.getString(ACCESS_TOKEN, "");
        headerTitle=(TextView) findViewById(R.id.headerTitle);
        connectivity=new Connectivity(HomeScreen.this);
        commonUtils = new CommonUtils();
        errorLayout = (LinearLayout) findViewById(R.id.errorLayout);
        bundle=getIntent().getExtras();
    }

//    @Override
//    public GlobalClass getGlobalClass() {
//        return globalClass;
//    }

    @Override
    public WebServices getWebServices() {
        return RestClient.getWebServices();
    }

    @Override
    public CommonUtils getCommonUtils() {
        return commonUtils;
    }

    @Override
    public SharedPreferences getSharedPreference() {
        return sharedPreferences;
    }

    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            if(lastSelectedScreen!=position)
            displayView(position);
            else
                mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void displayView(int position) {
        lastSelectedScreen=position;
        mDrawerLayout.closeDrawer(GravityCompat.START);
        Fragment fragment = null;
        switch (position) {
            case 0:
                headerTitle.setText(getResources().getString(R.string.current_shipment));
                fragment = new CurrentShipmentFragment();
                fragment.setArguments(bundle);
                break;
            case 1:
                headerTitle.setText(getResources().getString(R.string.my_schedule));
                fragment = new BookingsFragment();
                break;
            case 2:
                lastSelectedScreen=6;
                CommonUtils.phoneCall(HomeScreen.this,sharedPreferences.getString(FLEET_OWNER_NO,""));
//                try {
//                    Intent call = new Intent(Intent.ACTION_DIAL);
//                    call.setData(Uri.parse("tel:" + "+91"+sharedPreferences.getString(FLEET_OWNER_NO,"")));
//                    startActivity(call);
//                } catch (Exception e) {
//                    commonUtils.showSingleButtonPopup(HomeScreen.this,"Unable to perform action.");
//                }
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
        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    public void logout() {

        if (connectivity.isConnectingToInternet()) {
            commonUtils.showLoadingDialog(HomeScreen.this, "Please wait...");
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
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.clear();
                                editor.commit();
                                commonUtils.dismissLoadingDialog();
                                startActivity(intent);
                                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);

                                finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                                commonUtils.dismissLoadingDialog();
                            }
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            if (retrofitError.getResponse() != null) {
                                String json = new String(((TypedByteArray) retrofitError.getResponse()
                                        .getBody()).getBytes());
                                int statusCode = retrofitError.getResponse().getStatus();
                                if (statusCode == 401) {
                                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                            Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
                                } else {
                                    try {
                                        JSONObject jsonObject = new JSONObject(json);
                                        if (jsonObject.getString("message").equalsIgnoreCase("access denied")) {
                                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                                    Intent.FLAG_ACTIVITY_NEW_TASK);
                                            commonUtils.dismissLoadingDialog();
                                            startActivity(intent);
                                            overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
                                        } else {
                                            Toast.makeText(HomeScreen.this, jsonObject.get("message").toString(), Toast.LENGTH_SHORT).show();
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        commonUtils.dismissLoadingDialog();
                                    }
                                }
                            }
                            commonUtils.dismissLoadingDialog();
                        }
                    });
        }

    }


    public class Logout extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            commonUtils.showLoadingDialog(HomeScreen.this, "Please wait...");
            logout();

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(500);
            } catch (Exception ae) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    @Override
    public void onBackPressed() {
        if (this.mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            showExitPopup();
        }
    }

    public void showExitPopup(){
        final Dialog dialog = new Dialog(HomeScreen.this,android.R.style.Theme_Translucent_NoTitleBar);

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
        Button noButton =(Button) dialog.findViewById(R.id.no_button);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }


    public void logoutPopup(){

        final Dialog dialog = new Dialog(HomeScreen.this,android.R.style.Theme_Translucent_NoTitleBar);

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
                lastSelectedScreen=0;
                dialog.dismiss();
                logout();
//                finish();
//                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
            }
        });
        Button noButton =(Button) dialog.findViewById(R.id.no_button);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                lastSelectedScreen=0;
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
        stopService(new Intent(HomeScreen.this, MyService.class));
    }
}
