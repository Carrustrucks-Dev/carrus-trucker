package com.carrustruckerapp.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.carrustruckerapp.R;
import com.carrustruckerapp.interfaces.AppConstants;
import com.carrustruckerapp.interfaces.WebServices;
import com.carrustruckerapp.services.MyService;
import com.carrustruckerapp.utils.CommonUtils;
import com.carrustruckerapp.utils.GMapV2GetRouteDirection;
import com.carrustruckerapp.utils.GPSTracker;
import com.carrustruckerapp.utils.GlobalClass;
import com.carrustruckerapp.utils.Log;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Saurbhv on 10/28/15.
 */
public class CurrentShipmentFragment extends android.support.v4.app.Fragment implements View.OnClickListener,AppConstants {

    private GoogleMap googleMap;
    private GPSTracker gpsTracker;
    public WebServices googleWebServices,webServices;
    public GlobalClass globalClass;
    private GMapV2GetRouteDirection v2GetRouteDirection;
    private Bundle bundle;
    private RelativeLayout noBookingLayout, bookingDetailsLayout;
    private TextView tvName, tvDate, tvMonth, tvShipingJourney,tvBookingStatus,tvTimeSlot,tvTruckName;
    private ImageView callShipper;
    private String shipperNumber;
    public SharedPreferences sharedPreferences;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_current_shipment, container, false);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("custom-event-name"));
        globalClass = (GlobalClass) getActivity().getApplicationContext();
        googleWebServices = globalClass.getGoogleWebServices();
        webServices=globalClass.getWebServices();
        gpsTracker = new GPSTracker(getActivity());
        googleMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();
        googleMap.setMyLocationEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setTiltGesturesEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        noBookingLayout = (RelativeLayout) v.findViewById(R.id.noBookingLayout);
        bookingDetailsLayout = (RelativeLayout) v.findViewById(R.id.bookingDetailsLayout);
        tvName = (TextView) v.findViewById(R.id.name);
        tvDate = (TextView) v.findViewById(R.id.date);
        tvMonth = (TextView) v.findViewById(R.id.month);
        tvShipingJourney = (TextView) v.findViewById(R.id.shipingJourney);
        tvBookingStatus=(TextView) v.findViewById(R.id.status);
        callShipper=(ImageView) v.findViewById(R.id.callShipperButton);
        tvTimeSlot=(TextView)v.findViewById(R.id.timeSlot);
        tvTruckName=(TextView)v.findViewById(R.id.truckName);
        callShipper.setOnClickListener(this);
        if (gpsTracker.canGetLocation()) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude()), 14));
//            getDriectionToDestination(new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude()), gpsTracker.getLatitude() + ", " + gpsTracker.getLongitude(), "11.723512, 78.466287", GMapV2GetRouteDirection.MODE_DRIVING);
        } else {
//            gpsTracker.showSettingsAlert();
        }


        return v;
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            tvBookingStatus.setText(intent.getStringExtra("bookingStatus").replace("_", " "));


        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        getCurrentBookings();
        v2GetRouteDirection = new GMapV2GetRouteDirection();
        bundle = getArguments();
//        shipperNumber=bundle.getString("shipperPhoneNumber");
        Log.e("isbooking", "" + bundle.getBoolean("isBooking"));


    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void getCurrentBookings(){
        CommonUtils.showLoadingDialog(getActivity(),"loading...");
        webServices.getCurrentBooking(sharedPreferences.getString(ACCESS_TOKEN, ""), new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                try {
                    JSONObject serverResponse=new JSONObject(s);
                    JSONObject data=serverResponse.getJSONObject("data");
                    if(data.isNull("bookingData")){
                        noBookingLayout.setVisibility(View.VISIBLE);
                    }else{
                        bookingDetailsLayout.setVisibility(View.VISIBLE);
                        JSONObject bookingData=data.getJSONObject("bookingData");
                        Calendar cal = Calendar.getInstance();
                        TimeZone tz = cal.getTimeZone();
                        DateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        f.setTimeZone(TimeZone.getTimeZone("ISO"));
                        try {
                            Date d = f.parse(bookingData.getJSONObject("pickUp").getString("date"));
                            DateFormat date = new SimpleDateFormat("dd");
                            DateFormat month = new SimpleDateFormat("MMM");
                            DateFormat dayName = new SimpleDateFormat("EEE");
                            tvDate.setText(date.format(d));
                            tvMonth.setText(month.format(d));
                            tvTimeSlot.setText(dayName.format(d)+", "+bookingData.getJSONObject("pickUp").getString("time"));
                            tvTruckName.setText(bookingData.getJSONObject("truck").getJSONObject("truckType").getString("typeTruckName")
                            +" "+bookingData.getJSONObject("assignTruck").getString("truckNumber"));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        tvShipingJourney.setText(CommonUtils.toCamelCase(bookingData.getJSONObject("pickUp").getString("city"))+" to "+
                                CommonUtils.toCamelCase(bookingData.getJSONObject("dropOff").getString("city")));
                        tvName.setText(CommonUtils.toCamelCase(bookingData.getJSONObject("shipper").getString("firstName") + " " +
                                bookingData.getJSONObject("shipper").getString("lastName")));
                        tvBookingStatus.setText(bookingData.getString("bookingStatus").replace("_", " "));

                        shipperNumber=bookingData.getJSONObject("shipper").getString("phoneNumber");

                        getDriectionToDestination(new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude()),
                                bookingData.getJSONObject("pickUp").getJSONObject("coordinates").getString("pickUpLong") + ", " + bookingData.getJSONObject("pickUp").getJSONObject("coordinates").getString("pickUpLat"),
                                bookingData.getJSONObject("dropOff").getJSONObject("coordinates").getString("dropOffLong") + ", " + bookingData.getJSONObject("dropOff").getJSONObject("coordinates").getString("dropOffLat"),
                                GMapV2GetRouteDirection.MODE_DRIVING);


                        if (bookingData.getString("tracking").equalsIgnoreCase("YES")) {
                            Intent intent = new Intent(getActivity(), MyService.class);
                            intent.putExtra("bookingId", bookingData.getString("_id"));
                            getActivity().startService(intent);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    CommonUtils.dismissLoadingDialog();
                }

                CommonUtils.dismissLoadingDialog();
                Log.d("Response",s);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("failure",""+error);
                CommonUtils.dismissLoadingDialog();
            }
        });
    }

    //Path Direction Call
    private void getDriectionToDestination(final LatLng currentposition, String start, String end, String mode) {
        googleWebServices.getDriections(start, end, "false", "metric", mode, new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                googleMap.clear();
                // convert String into InputStream
                InputStream in = new ByteArrayInputStream(s.getBytes());
                DocumentBuilder builder = null;
                try {
                    builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    Document doc = builder.parse(in);
                    ArrayList<LatLng> directionPoint = v2GetRouteDirection.getDirection(doc);
                    PolylineOptions rectLine = new PolylineOptions().width(6).color(Color.BLUE);

                    for (int i = 0; i < directionPoint.size(); i++) {
                        rectLine.add(directionPoint.get(i));
                    }
                    // Adding route on the map
                    googleMap.addPolyline(rectLine);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(currentposition);
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_van));
                    googleMap.addMarker(markerOptions);

                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("error", "" + error);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.callShipperButton:

                CommonUtils.phoneCall(getActivity(),shipperNumber);
//                try {
//                    Intent call = new Intent(Intent.ACTION_DIAL);
//                    call.setData(Uri.parse("tel:" + "+91" + shipperNumber));
//                    startActivity(call);
//                } catch (Exception e) {
//                    CommonUtils.showSingleButtonPopup(getActivity(),"Unable to perform action.");
//                }
                break;
        }
    }

    //    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        FragmentManager fm = getActivity().getFragmentManager();
//        Fragment fragment = (fm.findFragmentById(R.id.map));
//        FragmentTransaction ft = fm.beginTransaction();
//        ft.remove(fragment);
//        ft.commit();
//    }
}
