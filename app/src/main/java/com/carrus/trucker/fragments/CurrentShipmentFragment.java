package com.carrus.trucker.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.carrus.trucker.R;
import com.carrus.trucker.activities.BookingDetails;
import com.carrus.trucker.interfaces.AppConstants;
import com.carrus.trucker.retrofit.RestClient;
import com.carrus.trucker.services.MyService;
import com.carrus.trucker.utils.CommonUtils;
import com.carrus.trucker.utils.GMapV2GetRouteDirection;
import com.carrus.trucker.utils.GPSTracker;
import com.carrus.trucker.utils.Log;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Saurbhv on 10/28/15.
 */
public class CurrentShipmentFragment extends android.support.v4.app.Fragment implements View.OnClickListener, AppConstants {

    private GoogleMap googleMap;
    private GPSTracker gpsTracker;
    private GMapV2GetRouteDirection v2GetRouteDirection;
    private Bundle bundle;
    private RelativeLayout noBookingLayout, bookingDetailsLayout;
    private TextView tvName, tvDate, tvMonth, tvShipingJourney, tvBookingStatus, tvTimeSlot, tvTruckName;
    private ImageView callShipper;
    private String shipperNumber;
    public SharedPreferences sharedPreferences;
    private double[] latitude = new double[2];
    private double[] longitude = new double[2];
    public String name[] = new String[2];
    private ArrayList<Marker> mMarkerArray = new ArrayList<Marker>();
    private Marker currentMarker = null;
    private String bookingId;

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
        tvBookingStatus = (TextView) v.findViewById(R.id.status);
        callShipper = (ImageView) v.findViewById(R.id.callShipperButton);
        tvTimeSlot = (TextView) v.findViewById(R.id.timeSlot);
        tvTruckName = (TextView) v.findViewById(R.id.truckName);
        callShipper.setOnClickListener(this);
        v.findViewById(R.id.bookingDetailsLayout).setOnClickListener(this);
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
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(intent.getDoubleExtra("latitude", 0.0), intent.getDoubleExtra("longitude", 0.0)));
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_van));
            if (currentMarker != null)
                currentMarker.remove();

            currentMarker = googleMap.addMarker(markerOptions);

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

    private void getCurrentBookings() {
        CommonUtils.showLoadingDialog(getActivity(), "loading...");
        RestClient.getWebServices().getCurrentBooking(sharedPreferences.getString(ACCESS_TOKEN, ""), new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                try {
                    JSONObject serverResponse = new JSONObject(s);
                    JSONObject data = serverResponse.getJSONObject("data");
                    if (data.isNull("bookingData")) {
                        noBookingLayout.setVisibility(View.VISIBLE);
                        googleMap.getUiSettings().setZoomControlsEnabled(false);
                    } else {
                        bookingDetailsLayout.setVisibility(View.VISIBLE);
                        JSONObject bookingData = data.getJSONObject("bookingData");
                        bookingId=bookingData.getString("_id");

                        tvDate.setText(CommonUtils.getDateFromUTC(bookingData.getJSONObject("pickUp").getString("date")));
                        tvMonth.setText(CommonUtils.getShortMonthNameFromUTC(bookingData.getJSONObject("pickUp").getString("date")));
                        tvTimeSlot.setText(CommonUtils.getDayNameFromUTC(bookingData.getJSONObject("pickUp").getString("date")) + ", " + bookingData.getJSONObject("pickUp").getString("time"));
                        tvTruckName.setText(bookingData.getJSONObject("truck").getJSONObject("truckType").getString("typeTruckName")
                                + " " + bookingData.getJSONObject("assignTruck").getString("truckNumber"));

                        tvShipingJourney.setText(CommonUtils.toCamelCase(bookingData.getJSONObject("pickUp").getString("city")) + " to " +
                                CommonUtils.toCamelCase(bookingData.getJSONObject("dropOff").getString("city")));
                        tvName.setText(CommonUtils.toCamelCase(bookingData.getJSONObject("shipper").getString("firstName") + " " +
                                bookingData.getJSONObject("shipper").getString("lastName")));
                        tvBookingStatus.setText(bookingData.getString("bookingStatus").replace("_", " "));

                        shipperNumber = bookingData.getJSONObject("shipper").getString("phoneNumber");

                        getDriectionToDestination(new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude()),
                                bookingData.getJSONObject("pickUp").getJSONObject("coordinates").getString("pickUpLat") + ", " + bookingData.getJSONObject("pickUp").getJSONObject("coordinates").getString("pickUpLong"),
                                bookingData.getJSONObject("dropOff").getJSONObject("coordinates").getString("dropOffLat") + ", " + bookingData.getJSONObject("dropOff").getJSONObject("coordinates").getString("dropOffLong"),
                                GMapV2GetRouteDirection.MODE_DRIVING);


                        if (bookingData.getString("tracking").equalsIgnoreCase("YES")) {
                            if (!bookingData.getString("_id").isEmpty()) {
                                Intent intent = new Intent(getActivity(), MyService.class);
                                intent.putExtra("bookingId", bookingData.getString("_id"));
                                getActivity().startService(intent);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    CommonUtils.dismissLoadingDialog();
                }

                CommonUtils.dismissLoadingDialog();
                Log.d("Response", s);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("failure", "" + error);
                CommonUtils.dismissLoadingDialog();
                if (((RetrofitError) error).getKind() == RetrofitError.Kind.NETWORK) {
                    showRetryPopup(getActivity().getString(R.string.no_internet_access));
                } else {
                    showRetryPopup(getActivity().getString(R.string.some_ereor_ocurred));
                }
            }
        });
    }

    //Path Direction Call
    private void getDriectionToDestination(final LatLng currentposition, final String start, final String end, String mode) {
        RestClient.getGoogleApiService().getDriections(start, end, "false", "metric", mode, new Callback<String>() {
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
                    currentMarker = googleMap.addMarker(markerOptions);
                    String source[] = start.split(",");
                    try {
                        longitude[0] = Double.valueOf(source[1]);
                        latitude[0] = Double.valueOf(source[0]);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }


//                    MarkerOptions sourceMarker = new MarkerOptions();
//                    sourceMarker.position(new LatLng(Double.valueOf(source[0]), Double.valueOf(source[1])));
//                    sourceMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
//                    googleMap.addMarker(sourceMarker);

                    String destination[] = end.split(",");
                    try {
                        longitude[1] = Double.valueOf(destination[1]);
                        latitude[1] = Double.valueOf(destination[0]);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        longitude[1] = 0;
                        latitude[1] = 0;
                    }

//                    MarkerOptions destinationMarker = new MarkerOptions();
//                    destinationMarker.position(new LatLng(Double.valueOf(destination[0]), Double.valueOf(destination[1])));
//                    destinationMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
//                    googleMap.addMarker(destinationMarker);

                    addmarkers();
                    LatLngBounds.Builder builder1 = new LatLngBounds.Builder();
                    for (Marker marker : mMarkerArray) {
                        builder1.include(marker.getPosition());
                    }
                    LatLngBounds bounds = builder1.build();

                    int padding = 150; // offset from edges of the map in pixels
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    googleMap.animateCamera(cu);


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


    public void addmarkers() {

        for (int i = 0; i < latitude.length; i++) {

            LatLng location = new LatLng(latitude[i], longitude[i]);

            Marker marker = googleMap.addMarker(new MarkerOptions().position(location)
                            .title(name[i])
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            );

            mMarkerArray.add(marker);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.callShipperButton:
                CommonUtils.phoneCall(getActivity(), shipperNumber);
                break;

            case R.id.bookingDetailsLayout:
                Intent intent = new Intent(getActivity(), BookingDetails.class);
                intent.putExtra("bookingId",bookingId);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                break;
        }
    }

    private void showRetryPopup(String msg) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setMessage(msg);
        alertDialog.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                getCurrentBookings();
            }
        });

        alertDialog.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });
        alertDialog.show();
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
