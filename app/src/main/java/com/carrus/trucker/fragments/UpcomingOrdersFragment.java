package com.carrus.trucker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.carrus.trucker.R;
import com.carrus.trucker.activities.BookingDetailsActivity;
import com.carrus.trucker.adapters.BookingAdapter;
import com.carrus.trucker.entities.Booking;
import com.carrus.trucker.interfaces.AppConstants;
import com.carrus.trucker.interfaces.HomeCallback;
import com.carrus.trucker.utils.CommonUtils;
import com.carrus.trucker.utils.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Saurbhv on 11/16/15.
 */
public class UpcomingOrdersFragment extends Fragment implements AppConstants, SwipeRefreshLayout.OnRefreshListener {

    ListView listView;
    ArrayList<Booking> bookingsArrayList, pastBookingArrayList;
    BookingAdapter bookingAdapter, pastBookinAdapter;
    HomeCallback homeCallback;
    boolean isRefreshView=false;
    TextView noOrderPlaceholder;
    private SwipeRefreshLayout swipeRefreshLayout;
    public UpcomingOrdersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_bookingview, container, false);
        init(v);
        getData();

        return v;
    }

    private void init(View v){
        homeCallback = (HomeCallback) getActivity();
        if (homeCallback == null)
            throw new IllegalArgumentException(" implement home callback in Activity");
        listView = (ListView) v.findViewById(R.id.orders_listview);
        pastBookingArrayList = new ArrayList<Booking>();
        bookingsArrayList = new ArrayList<Booking>();
        noOrderPlaceholder = (TextView) v.findViewById(R.id.no_bookings_placeholder);
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_layout);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout.setOnRefreshListener((SwipeRefreshLayout.OnRefreshListener) this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("I m clicked", "");
                Intent intent = new Intent(getActivity(), BookingDetailsActivity.class);
                intent.putExtra("bookingId", bookingsArrayList.get(position).getBooking_id());
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
            }
        });
    }

    @Override
    public void onRefresh() {
        isRefreshView=true;
        swipeRefreshLayout.setRefreshing(true);
        getData();
    }

    @Override
    public void onResume() {
        super.onResume();
        onRefresh();
    }

    private void getData() {
            if (!isRefreshView) {
                homeCallback.getCommonUtils().showLoadingDialog(getActivity(), getResources().getString(R.string.loading));
            }


            Log.i("bookingsArrayList", bookingsArrayList.size() + "");
            homeCallback.getWebServices().getUpComingOrders(homeCallback.getSharedPreference().getString(ACCESS_TOKEN, ""),"ASC",
                    new Callback<String>() {
                        @Override
                        public void success(String serverResponse, Response response) {
                            try {
                                bookingsArrayList.clear();
                                JSONObject jsonObjectServerResponse = new JSONObject(serverResponse);
                                if (!jsonObjectServerResponse.isNull("data")) {
                                    JSONArray jsonDataArray = new JSONArray(jsonObjectServerResponse.getString("data"));
                                    for (int i = 0; i < jsonDataArray.length(); i++) {
                                        Booking booking = new Booking();
                                        JSONObject jsonObject = jsonDataArray.getJSONObject(i);
                                        booking.setBooking_id(jsonObject.getString("_id"));
                                        booking.setBookingTime(jsonObject.getJSONObject("pickUp").getString("date"));
                                        booking.setName(jsonObject.getJSONObject("shipper").getString("firstName") + " " + jsonObject.getJSONObject("shipper").getString("lastName"));
                                        booking.setShipingJourney(CommonUtils.toCamelCase(jsonObject.getJSONObject("pickUp").getString("city")) + " to " + CommonUtils.toCamelCase(jsonObject.getJSONObject("dropOff").getString("city")));
                                        booking.setStatus(jsonObject.getString("bookingStatus"));
                                        booking.setTimeSlot(jsonObject.getJSONObject("pickUp").getString("time"));
                                        booking.setTruckName(jsonObject.getJSONObject("truck").getJSONObject("truckType").getString("typeTruckName")
                                        +" "+jsonObject.getJSONObject("assignTruck").getString("truckNumber"));
                                        bookingsArrayList.add(booking);
                                    }
                                }

                                if (bookingsArrayList.size() == 0) {
                                    Log.i("bookingsArrayList1", bookingsArrayList.size() + "");
                                    noOrderPlaceholder.setText("No Upcoming Orders");
                                    noOrderPlaceholder.setVisibility(View.VISIBLE);
                                    //  listView.setVisibility(View.GONE);
                                } else {
                                    Log.i("bookingsArrayList2", bookingsArrayList.size() + "");
                                    noOrderPlaceholder.setVisibility(View.GONE);
                                    listView.setVisibility(View.VISIBLE);
                                    bookingAdapter = new BookingAdapter(getActivity(), bookingsArrayList);
                                    listView.setAdapter(bookingAdapter);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            homeCallback.getCommonUtils().dismissLoadingDialog();
                            swipeRefreshLayout.setRefreshing(false);
                            isRefreshView = false;
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            homeCallback.getCommonUtils().showRetrofitError(getActivity(), retrofitError);
                            homeCallback.getCommonUtils().dismissLoadingDialog();
                            swipeRefreshLayout.setRefreshing(false);
                            isRefreshView = false;
                        }
                    });
//        } else {
//            if (bookingsArrayList.isEmpty() && pastBookingArrayList.isEmpty()&& !isRefreshView) {
//                homeCallback.getCommonUtils().showLoadingDialog(getActivity(), getResources().getString(R.string.loading));
//            }
//
//            pastBookingArrayList.clear();
//            homeCallback.getWebServices().getPastOrders(homeCallback.getSharedPreference().getString(ACCESS_TOKEN, ""),
//                    new Callback<String>() {
//                        @Override
//                        public void success(String serverResponse, Response response) {
//                            try {
//                                JSONObject jsonObjectServerResponse = new JSONObject(serverResponse);
//                                if (!jsonObjectServerResponse.isNull("data")) {
//                                    JSONArray jsonDataArray = new JSONArray(jsonObjectServerResponse.getString("data"));
//                                    for (int i = 0; i < jsonDataArray.length(); i++) {
//                                        Booking booking = new Booking();
//                                        JSONObject jsonObject = jsonDataArray.getJSONObject(i);
//                                        booking.setBooking_id(jsonObject.getString("_id"));
//                                        booking.setBookingTime(jsonObject.getString("bookingCreatedAt"));
//                                        booking.setName(jsonObject.getJSONObject("shipper").getString("firstName") + " " + jsonObject.getJSONObject("shipper").getString("lastName"));
//                                        booking.setShipingJourney(jsonObject.getJSONObject("pickUp").getString("city") + " to " + jsonObject.getJSONObject("pickUp").getString("city"));
//                                        booking.setStatus(jsonObject.getString("bookingStatus"));
//                                        booking.setTimeSlot(jsonObject.getJSONObject("dropOff").getString("time"));
//                                        booking.setTruckName(jsonObject.getJSONObject("truck").getJSONObject("truckType").getString("typeTruckName"));
//                                        pastBookingArrayList.add(booking);
//                                    }
//                                }
//
//                                if (pastBookingArrayList.size() == 0) {
//                                    noOrderPlaceholder.setText("No Past Orders");
//                                    noOrderPlaceholder.setVisibility(View.VISIBLE);
//                                    // listView.setVisibility(View.GONE);
//                                } else {
//                                    noOrderPlaceholder.setVisibility(View.GONE);
//                                    listView.setVisibility(View.VISIBLE);
//                                    bookingAdapter = new BookingAdapter(getActivity(), pastBookingArrayList);
//                                    listView.setAdapter(bookingAdapter);
//                                }
//
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                            homeCallback.getCommonUtils().dismissLoadingDialog();
//                            swipeRefreshLayout.setRefreshing(false);
//                            isRefreshView=false;
//                        }
//
//                        @Override
//                        public void failure(RetrofitError retrofitError) {
//                            homeCallback.getCommonUtils().showRetrofitError(getActivity(), retrofitError);
//                            homeCallback.getCommonUtils().dismissLoadingDialog();
//                            swipeRefreshLayout.setRefreshing(false);
//                            isRefreshView=false;
//                        }
//                    });
//        }

        }

}
