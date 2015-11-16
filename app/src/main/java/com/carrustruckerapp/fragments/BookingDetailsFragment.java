package com.carrustruckerapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.carrustruckerapp.R;

/**
 * Created by Saurbhv on 10/30/15.
 */
public class BookingDetailsFragment extends Fragment {

    public BookingDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.activity_booking_details, container, false);
        init(v);

        return v;
    }

    private void init(View v){

    }
}
