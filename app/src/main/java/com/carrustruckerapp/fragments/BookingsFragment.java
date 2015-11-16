package com.carrustruckerapp.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.carrustruckerapp.R;
import com.carrustruckerapp.interfaces.AppConstants;

/**
 * Created by Saurbhv on 10/29/15.
 */
public class BookingsFragment extends Fragment implements AppConstants, View.OnClickListener {
    Button upcomingBookingButton, pastBookingButton;
    Fragment upcomingOrderFragment,pastOrderFragment;
    FragmentManager fragmentManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_bookings, container, false);
        init(v);
        fragmentManager = getActivity().getSupportFragmentManager();
        return v;
    }

    private void init(View v) {
        upcomingBookingButton = (Button) v.findViewById(R.id.upcoming_button);
        pastBookingButton = (Button) v.findViewById(R.id.past_button);
        upcomingBookingButton.setOnClickListener(this);
        pastBookingButton.setOnClickListener(this);
        upcomingOrderFragment=new UpcomingOrdersFragment();
        pastOrderFragment=new PastOrdersFragment();
    }





    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                getData();
//                fragmentManager.beginTransaction()
//                        .replace(R.id.booking_frame_container, upcomingOrderFragment).commit();
                setFragment(upcomingOrderFragment);
            }
        }, 100);


    }

    @Override
    public void onClick(View v) {
//        GetOrders getOrders;
        switch (v.getId()) {
            case R.id.upcoming_button:
                pastBookingButton.setBackgroundResource(R.drawable.round_corner_button_background);
                upcomingBookingButton.setBackgroundResource(R.drawable.left_round_button_filled_background);
                pastBookingButton.setTextColor(Color.parseColor("#F89715"));
                upcomingBookingButton.setTextColor(Color.parseColor("#FFFFFF"));
                setFragment(upcomingOrderFragment);
//                fragmentManager.beginTransaction()
//                        .replace(R.id.booking_frame_container, upcomingOrderFragment).commit();
//                upcomingOrders = true;
////                listView.setVisibility(View.GONE);
//                noOrderPlaceholder.setVisibility(View.GONE);
//                if (bookingsArrayList.size() == 0) {
//                    getData();
//                } else {
//                    listView.setVisibility(View.VISIBLE);
//                    bookingAdapter = new BookingAdapter(getActivity(), bookingsArrayList);
//                    listView.setAdapter(bookingAdapter);
//                }


                break;
            case R.id.past_button:
                upcomingBookingButton.setBackgroundResource(R.drawable.left_round_corner_button_background);
                pastBookingButton.setBackgroundResource(R.drawable.right_round_button_filled_background);
                pastBookingButton.setTextColor(Color.parseColor("#FFFFFF"));
                upcomingBookingButton.setTextColor(Color.parseColor("#F89715"));
                setFragment(pastOrderFragment);
//                fragmentManager.beginTransaction()
//                        .replace(R.id.booking_frame_container, pastOrderFragment).commit();
//                upcomingOrders = false;
////                listView.setVisibility(View.GONE);
//                noOrderPlaceholder.setVisibility(View.GONE);
//                if (pastBookingArrayList.size() == 0) {
//                    getData();
//                } else {
//                    listView.setVisibility(View.VISIBLE);
//                    bookingAdapter = new BookingAdapter(getActivity(), pastBookingArrayList);
//                    listView.setAdapter(bookingAdapter);
//                }
                break;
        }


    }
    private void setFragment(Fragment fragment){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // If fragment doesn't exist yet, create one
        if (fragment.isAdded())
        {
            if(fragment instanceof UpcomingOrdersFragment) {
                fragmentTransaction.hide(pastOrderFragment);
            }else if(fragment instanceof PastOrdersFragment){
                fragmentTransaction.hide(upcomingOrderFragment);
            }
            fragmentTransaction.show(fragment);
        }

        else { // re-use the old fragment
            if(fragment instanceof PastOrdersFragment){
                fragmentTransaction.hide(upcomingOrderFragment);
            }
            fragmentTransaction.add(R.id.booking_frame_container, fragment);
        }

        fragmentTransaction.commit();

    }

//    private void getData(){
//        getOrdersFunction();
//    }
//
//    public class GetOrders extends AsyncTask<Void, Void, ArrayList<Booking>> {
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            homeCallback.getCommonUtils().showLoadingDialog(getActivity(), getResources().getString(R.string.loading));
//
//        }
//
//        @Override
//        protected ArrayList<Booking> doInBackground(Void... params) {
//            return getOrdersFunction();
//
//        }
//
//        @Override
//        protected void onPostExecute(ArrayList<Booking> list) {
//            bookingAdapter = new BookingAdapter(getActivity(), list);
//            listView.setAdapter(bookingAdapter);
//            homeCallback.getCommonUtils().dismissLoadingDialog();
//        }
//    }


}
