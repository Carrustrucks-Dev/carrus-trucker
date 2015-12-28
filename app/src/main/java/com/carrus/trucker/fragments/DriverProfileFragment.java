package com.carrus.trucker.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.carrus.trucker.R;
import com.carrus.trucker.interfaces.AppConstants;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class DriverProfileFragment extends Fragment implements AppConstants {

    private SharedPreferences sharedPreferences;
    private CircleImageView driverImage;
    private TextView driverName;
    private RatingBar driverRating;
    private TextView driverId, drivingLicense, licenseState, expiresOn, mobileNumber;


    public DriverProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_driver_profile, container, false);
        init(v);
        setDriverData();
        return v;
    }


    private void init(View v) {
        driverImage = (CircleImageView) v.findViewById(R.id.driverImage);
        driverName = (TextView) v.findViewById(R.id.driverName);
        driverRating = (RatingBar) v.findViewById(R.id.driverRating);
        driverId = (TextView) v.findViewById(R.id.tvDriverId);
        drivingLicense = (TextView) v.findViewById(R.id.tvDrivingLicense);
        licenseState = (TextView) v.findViewById(R.id.tvLicenseState);
        expiresOn = (TextView) v.findViewById(R.id.tvExpiresOn);
        mobileNumber = (TextView) v.findViewById(R.id.tvMobileNumber);
        sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    private void setDriverData() {
        driverName.setText(sharedPreferences.getString(DRIVAR_NAME, ""));
        driverId.setText(sharedPreferences.getString(DRIVER_ID, ""));
        drivingLicense.setText(sharedPreferences.getString(DRIVING_LICENSE, ""));
        licenseState.setText(sharedPreferences.getString(DL_STATE, ""));
        expiresOn.setText(sharedPreferences.getString(VALIDITY, ""));
        mobileNumber.setText(sharedPreferences.getString(DRIVER_PHONENO, ""));
        driverRating.setRating(Float.valueOf(sharedPreferences.getFloat(RATING, 0)));
        try {
            Calendar cal = Calendar.getInstance();
            TimeZone tz = cal.getTimeZone();
            Log.i("Time zone: ", tz.getDisplayName());
            DateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            f.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date d = f.parse(sharedPreferences.getString(VALIDITY, ""));
            DateFormat date = new SimpleDateFormat("MM/yyyy");
            expiresOn.setText(date.format(d));
            String path=sharedPreferences.getString(DRIVER_IMAGE, "");
            if(!path.equals("")){
                Picasso.with(getActivity())
                        .load(path).fit()
                        .centerCrop().memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).skipMemoryCache()
                        .placeholder(R.mipmap.icon_placeholder)// optional
                        .into(driverImage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
