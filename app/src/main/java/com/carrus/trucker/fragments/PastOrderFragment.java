package com.carrus.trucker.fragments;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.carrus.trucker.R;
import com.carrus.trucker.adapters.BookingRecyclerAdapter;
import com.carrus.trucker.adapters.DividerItemDecoration;
import com.carrus.trucker.interfaces.AppConstants;
import com.carrus.trucker.interfaces.OnLoadMoreListener;
import com.carrus.trucker.models.Booking;
import com.carrus.trucker.retrofit.RestClient;
import com.carrus.trucker.utils.ApiResponseFlags;
import com.carrus.trucker.utils.CommonUtils;
import com.carrus.trucker.utils.InternetConnectionStatus;
import com.carrus.trucker.utils.MaterialDesignAnimations;
import com.carrus.trucker.utils.Prefs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

/**
 * Developer: Saurbhv
 * Dated: 1/13/16.
 */
public class PastOrderFragment extends android.support.v4.app.Fragment implements  SwipeRefreshLayout.OnRefreshListener, AppConstants {
    private RecyclerView mRecyclerView;
    private BookingRecyclerAdapter bookingAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<Booking> bookingList;
    private int skip = 0;
    private boolean isRefreshView = false;
    private boolean isUpdate=false;
    private TextView tvNoBookingText;
    private LinearLayout llNoBookingPlaceholder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_booking_listview, container, false);
        init(v);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(InternetConnectionStatus.getInstance(getActivity()).isOnline()){
            apiCallForPastOrders();
        }
    }

    private void init(View v){
        mRecyclerView = (RecyclerView) v.findViewById(R.id.relvBookingList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(
                new DividerItemDecoration(getActivity()));
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.blue, R.color.red, R.color.orange, R.color.green);
        mRecyclerView.setHasFixedSize(true);
        tvNoBookingText = (TextView) v.findViewById(R.id.tvNoBookingText);
        llNoBookingPlaceholder = (LinearLayout) v.findViewById(R.id.llNoBookingPlaceholder);
        tvNoBookingText.setText(getString(R.string.no_past_orders));
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isUpdate){
            isRefreshView = true;
            apiCallForPastOrders();
        }else{
            isUpdate=true;
        }
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        isRefreshView = true;
        apiCallForPastOrders();
    }

    private void apiCallForPastOrders(){

        if (isRefreshView) {
            swipeRefreshLayout.setRefreshing(true);
            bookingList=null;
            skip=0;
        } else {
            if(bookingList==null || bookingList.size()==0)
                CommonUtils.showLoadingDialog(getActivity(), getString(R.string.loading));
        }

        RestClient.getWebServices().getPastOrders(Prefs.with(getActivity()).getString(ACCESS_TOKEN, ""), LIMIT, skip, "DESC", new Callback<String>() {
            @Override
            public void success(String serverResponse, Response response) {
                if (getActivity() != null) {
                    try {
                        JSONObject mObject = new JSONObject(serverResponse);
                        int status = mObject.getInt("statusCode");
                        if (ApiResponseFlags.OK.getOrdinal() == status) {
                            if (bookingList == null) {
                                bookingList = new ArrayList<>();
                                ArrayList<Booking> temBookingArrayList = new ArrayList<Booking>();
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
                                                + " " + jsonObject.getJSONObject("assignTruck").getString("truckNumber"));
                                        temBookingArrayList.add(booking);
                                    }
                                }

                                bookingList.addAll(temBookingArrayList);
                                if (bookingList.size() == 0) {
                                    llNoBookingPlaceholder.setVisibility(View.VISIBLE);
                                } else {
                                    llNoBookingPlaceholder.setVisibility(View.GONE);
                                }
                                bookingAdapter = new BookingRecyclerAdapter(getActivity(), bookingList, mRecyclerView, true);
                                mRecyclerView.setAdapter(bookingAdapter);
                                if (bookingList.size() == LIMIT)
                                    setonScrollListener();

                                skip = skip + temBookingArrayList.size();
                            } else {
                                if (bookingList.size() > 0) {
                                    bookingList.remove(bookingList.size() - 1);
                                    bookingAdapter.notifyItemRemoved(bookingList.size());
                                }

                                ArrayList<Booking> temBookingArrayList = new ArrayList<Booking>();
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
                                                + " " + jsonObject.getJSONObject("assignTruck").getString("truckNumber"));
                                        temBookingArrayList.add(booking);
                                    }
                                }


                                //add items one by one
                                int start = bookingList.size();
                                int end = start + temBookingArrayList.size();
                                int j = 0;
                                for (int i = start + 1; i <= end; i++) {
                                    bookingList.add(temBookingArrayList.get(j));
                                    bookingAdapter.notifyItemInserted(bookingList.size());
                                    j++;
                                }
                                bookingAdapter.setLoaded();
                                skip = skip + temBookingArrayList.size();
                            }
                            //skip = skip + myPackagesSuccessResponse.getData().size();
                        } else {
                            if (ApiResponseFlags.Not_Found.getOrdinal() == status) {
                                bookingList.remove(bookingList.size() - 1);
                                bookingAdapter.notifyItemRemoved(bookingList.size());
                            } else {
                                Toast.makeText(getActivity(), mObject.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                            Toast.makeText(getActivity(), mObject.getString("message"), Toast.LENGTH_SHORT).show();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    CommonUtils.dismissLoadingDialog();
                    isRefreshView = false;
                    swipeRefreshLayout.setRefreshing(false);

                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (getActivity() != null) {
                    CommonUtils.dismissLoadingDialog();
                    try {
                        int statusCode = error.getResponse().getStatus();
                        String json = new String(((TypedByteArray) error.getResponse().getBody()).getBytes());
                        JSONObject jsonObject = new JSONObject(json);
                        if (statusCode == 404) {
                            if (bookingAdapter != null) {
                                bookingAdapter.clearAll();
                            }
                            llNoBookingPlaceholder.setVisibility(View.VISIBLE);
                        } else if (statusCode == 405) {
                            if ((bookingList != null) && bookingList.get(bookingList.size() - 1) == null) {
                                bookingList.remove(bookingList.size() - 1);
                                bookingAdapter.notifyItemRemoved(bookingList.size());
                            }
                            MaterialDesignAnimations.fadeIn(getActivity(), getActivity().findViewById(R.id.errorLayout), jsonObject.get("message").toString(), 0);
                        } else {
                            CommonUtils.showRetrofitError(getActivity(), error);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if ((bookingList != null) && bookingList.get(bookingList.size() - 1) == null) {
                            bookingList.remove(bookingList.size() - 1);
                            bookingAdapter.notifyItemRemoved(bookingList.size());
                        }
                        MaterialDesignAnimations.fadeIn(getActivity(), getActivity().findViewById(R.id.errorLayout), getActivity().getString(R.string.internetConnectionError), 0);
                    }
                    isRefreshView = false;
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    private void setonScrollListener() {

        bookingAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                //add null , so the adapter will check view_type and show progress bar at bottom
                try {
                    if(bookingList.get(bookingList.size()-1)!=null) {
                        bookingList.add(null);
                        bookingAdapter.notifyItemInserted(bookingList.size() - 1);
                    }
                    apiCallForPastOrders();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });
    }
}
