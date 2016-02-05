package com.carrus.trucker.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.carrus.trucker.R;
import com.carrus.trucker.adapters.PagerAdapter;
import com.carrus.trucker.interfaces.AppConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Saurbhv on 10/29/15.
 */
public class BookingsFragment extends Fragment implements AppConstants, View.OnClickListener {
    Button upcomingBookingButton, pastBookingButton;
    Fragment upcomingOrderFragment,pastOrderFragment;
    FragmentManager fragmentManager;
    private PagerAdapter mPagerAdapter;
    private ViewPager pager;


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
        upcomingOrderFragment=new UpcomingOrderFragment();
        pastOrderFragment=new PastOrderFragment();


        List<Fragment> fragments = getFragments();
        mPagerAdapter = new PagerAdapter(getActivity().getSupportFragmentManager(), fragments);
        pager = (ViewPager) v.findViewById(R.id.viewpager);
        pager.setOffscreenPageLimit(2);
        pager.setAdapter(mPagerAdapter);
        pager.setCurrentItem(0);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        selectButton(0);
                        break;

                    case 1:
                        selectButton(1);
                        break;

                    default:
                        selectButton(0);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void selectButton(int position){
        if(position==0){  //upcoming tab
            pastBookingButton.setBackgroundResource(R.drawable.round_corner_button_background);
            upcomingBookingButton.setBackgroundResource(R.drawable.left_round_button_filled_background);
            pastBookingButton.setTextColor(Color.parseColor("#F89715"));
            upcomingBookingButton.setTextColor(Color.parseColor("#FFFFFF"));
        }else if(position==1){  //past tab
            upcomingBookingButton.setBackgroundResource(R.drawable.left_round_corner_button_background);
            pastBookingButton.setBackgroundResource(R.drawable.right_round_button_filled_background);
            pastBookingButton.setTextColor(Color.parseColor("#FFFFFF"));
            upcomingBookingButton.setTextColor(Color.parseColor("#F89715"));

        }

    }





    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                getData();
//                fragmentManager.beginTransaction()
//                        .replace(R.id.booking_frame_container, upcomingOrderFragment).commit();
//                setFragment(upcomingOrderFragment);
//            }
//        }, 100);


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
                pager.setCurrentItem(0);
                //setFragment(upcomingOrderFragment);
                break;
            case R.id.past_button:
                upcomingBookingButton.setBackgroundResource(R.drawable.left_round_corner_button_background);
                pastBookingButton.setBackgroundResource(R.drawable.right_round_button_filled_background);
                pastBookingButton.setTextColor(Color.parseColor("#FFFFFF"));
                upcomingBookingButton.setTextColor(Color.parseColor("#F89715"));
                pager.setCurrentItem(1);
               // setFragment(pastOrderFragment);
                break;
        }


    }
   /**
    private void setFragment(Fragment fragment){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // If fragment doesn't exist yet, create one
        if (fragment.isAdded())
        {
            if(fragment instanceof UpcomingOrderFragment) {
                fragmentTransaction.hide(pastOrderFragment);
            }else if(fragment instanceof PastOrderFragment){
                fragmentTransaction.hide(upcomingOrderFragment);
            }
            fragmentTransaction.show(fragment);
        }

        else { // re-use the old fragment
            if(fragment instanceof PastOrderFragment){
                fragmentTransaction.hide(upcomingOrderFragment);
            }
            fragmentTransaction.add(R.id.booking_frame_container, fragment);
        }

        fragmentTransaction.commit();

    }*/

    private List<Fragment> getFragments() {
        List<Fragment> fList = new ArrayList<Fragment>();
        fList.add(new UpcomingOrderFragment());
        fList.add(new PastOrderFragment());
        return fList;

    }

}
