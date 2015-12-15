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
import com.carrus.trucker.models.Booking;
import com.carrus.trucker.interfaces.AppConstants;
import com.carrus.trucker.retrofit.RestClient;
import com.carrus.trucker.utils.CommonUtils;
import com.carrus.trucker.utils.Prefs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Saurbhv on 11/16/15.
 */
public class PastOrdersFragment extends Fragment implements AppConstants, SwipeRefreshLayout.OnRefreshListener  {

    private ListView listView;
    private ArrayList<Booking> pastBookingArrayList;
    private BookingAdapter bookingAdapter;
    private boolean isRefreshView=false;
    private TextView noOrderPlaceholder;
    private SwipeRefreshLayout swipeRefreshLayout;
    public PastOrdersFragment() {
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
        listView = (ListView) v.findViewById(R.id.orders_listview);
        pastBookingArrayList = new ArrayList<Booking>();
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
                Intent intent = new Intent(getActivity(), BookingDetailsActivity.class);
                intent.putExtra("bookingId", pastBookingArrayList.get(position).getBooking_id());
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
            if ( !isRefreshView) {
                CommonUtils.showLoadingDialog(getActivity(), getResources().getString(R.string.loading));
            }
            RestClient.getWebServices().getPastOrders(Prefs.with(getActivity()).getString(ACCESS_TOKEN, ""),"DESC",
                    new Callback<String>() {
                        @Override
                        public void success(String serverResponse, Response response) {
                            try {
                                pastBookingArrayList.clear();
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
                                        pastBookingArrayList.add(booking);
                                    }
                                }

                                if (pastBookingArrayList.size() == 0) {
                                    noOrderPlaceholder.setText(getString(R.string.no_past_orders));
                                    noOrderPlaceholder.setVisibility(View.VISIBLE);
                                    // listView.setVisibility(View.GONE);
                                } else {
                                    noOrderPlaceholder.setVisibility(View.GONE);
                                    listView.setVisibility(View.VISIBLE);
                                    bookingAdapter = new BookingAdapter(getActivity(), pastBookingArrayList);
                                    listView.setAdapter(bookingAdapter);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            CommonUtils.dismissLoadingDialog();
                            swipeRefreshLayout.setRefreshing(false);
                            isRefreshView=false;
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            if(retrofitError.getResponse().getStatus()==405){
                                noOrderPlaceholder.setText(getString(R.string.no_past_orders));
                                noOrderPlaceholder.setVisibility(View.VISIBLE);
                            }else {
                                CommonUtils.showRetrofitError(getActivity(), retrofitError);
                            }
                            CommonUtils.dismissLoadingDialog();
                            swipeRefreshLayout.setRefreshing(false);
                            isRefreshView = false;
                        }
                    });
        }


}
