package com.carrus.trucker.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.carrus.trucker.R;
import com.carrus.trucker.activities.BookingDetailsActivity;
import com.carrus.trucker.interfaces.AppConstants;
import com.carrus.trucker.retrofit.RestClient;
import com.carrus.trucker.services.MyService;
import com.carrus.trucker.utils.CommonUtils;
import com.carrus.trucker.utils.GMapV2GetRouteDirection;
import com.carrus.trucker.utils.GPSTracker;
import com.carrus.trucker.utils.Log;
import com.carrus.trucker.utils.Transactions;
import com.flurry.android.FlurryAgent;
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
 * Developer: Saurbhv
 * Dated: 10/28/15.
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
    private Location location;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    public SharedPreferences sharedPreferences;
    private double[] latitude = new double[2];
    private double[] longitude = new double[2];
    private String name[] = new String[2];
    private String address[] = new String[2];
    private ArrayList<Marker> mMarkerArray = new ArrayList<Marker>();
    private Marker currentMarker = null;
    private String bookingId;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    LocationManager locationManager;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_current_shipment, container, false);

        noBookingLayout = (RelativeLayout) v.findViewById(R.id.noBookingLayout);
        bookingDetailsLayout = (RelativeLayout) v.findViewById(R.id.bookingDetailsLayout);
        tvName = (TextView) v.findViewById(R.id.name);
        tvDate = (TextView) v.findViewById(R.id.date);
        tvMonth = (TextView) v.findViewById(R.id.month);
        tvShipingJourney = (TextView) v.findViewById(R.id.shipingJourney);
        tvBookingStatus = (TextView) v.findViewById(R.id.tvStatus);
        callShipper = (ImageView) v.findViewById(R.id.callShipperButton);
        tvTimeSlot = (TextView) v.findViewById(R.id.timeSlot);
        tvTruckName = (TextView) v.findViewById(R.id.truckName);

        callShipper.setOnClickListener(this);
        v.findViewById(R.id.bookingDetailsLayout).setOnClickListener(this);


        googleMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();
        if(googleMap!=null) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.getUiSettings().setZoomGesturesEnabled(true);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setTiltGesturesEnabled(false);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
            v2GetRouteDirection = new GMapV2GetRouteDirection();
            checkLocationPermissionNSetLocation();
        }else{
            Toast.makeText(getActivity(),"Unable to load google map.",Toast.LENGTH_LONG).show();
        }
        return v;
    }

    private void checkLocationPermissionNSetLocation() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
            //gpsTracker = new GPSTracker(getActivity());
            locationManager = (LocationManager) getActivity().getSystemService(Activity.LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            googleMap.setMyLocationEnabled(true);
            if (isNetworkEnabled) {
                if (locationManager != null)
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if(location==null)
                if (isGPSEnabled) {
                    if (locationManager != null)
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }


            if (location!=null) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));
            }

            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                    new IntentFilter("custom-event-name"));
            getCurrentBookings();

        } else {
            //gpsTracker = new GPSTracker(getActivity());
            locationManager = (LocationManager) getActivity().getSystemService(Activity.LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            googleMap.setMyLocationEnabled(true);

            if (isNetworkEnabled) {
                if (locationManager != null)
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if(location==null)
                if (isGPSEnabled) {
                    if (locationManager != null)
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }

            if (location!=null) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));
            }

            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                    new IntentFilter("custom-event-name"));
            getCurrentBookings();
        }

    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            if (intent != null) {
                tvBookingStatus.setText(CommonUtils.toCamelCase(intent.getStringExtra("bookingStatus").replace("_", " ")));
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(intent.getDoubleExtra("latitude", 0.0), intent.getDoubleExtra("longitude", 0.0)));
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_van));
                if (currentMarker != null)
                    currentMarker.remove();

                currentMarker = googleMap.addMarker(markerOptions);
            }

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkLocationPermissionNSetLocation();
                } else {
                    // Permission Denied
                    Toast.makeText(getActivity(), "Location permission denied.", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    /**
     * Method for GetCurrentBooking details and set corresponding data
     */
    private void getCurrentBookings() {
        CommonUtils.showLoadingDialog(getActivity(), "loading...");
        RestClient.getWebServices().getCurrentBooking(sharedPreferences.getString(ACCESS_TOKEN, ""), new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                if(getActivity()!=null) {
                    try {
                        FlurryAgent.onEvent("Current Booking mode");
                        JSONObject serverResponse = new JSONObject(s);
                        JSONObject data = serverResponse.getJSONObject("data");
                        if (data.isNull("bookingData")) {
                            noBookingLayout.setVisibility(View.VISIBLE);
                            googleMap.getUiSettings().setZoomControlsEnabled(false);
                        } else {
                            bookingDetailsLayout.setVisibility(View.VISIBLE);

                            JSONObject bookingData = data.getJSONObject("bookingData");
                            bookingId = bookingData.getString("_id");

                            tvDate.setText(CommonUtils.getDateFromUTC(bookingData.getJSONObject("pickUp").getString("date")));
                            tvMonth.setText(CommonUtils.getShortMonthNameFromUTC(bookingData.getJSONObject("pickUp").getString("date")));
                            tvTimeSlot.setText(CommonUtils.getDayNameFromUTC(bookingData.getJSONObject("pickUp").getString("date")) + ", " + bookingData.getJSONObject("pickUp").getString("time"));
                            tvTruckName.setText(bookingData.getJSONObject("truck").getJSONObject("truckType").getString("typeTruckName")
                                    + " " + bookingData.getJSONObject("assignTruck").getString("truckNumber"));

                            tvShipingJourney.setText(CommonUtils.toCamelCase(bookingData.getJSONObject("pickUp").getString("city")) + " to " +
                                    CommonUtils.toCamelCase(bookingData.getJSONObject("dropOff").getString("city")));
                            tvName.setText(CommonUtils.toCamelCase(bookingData.getJSONObject("shipper").getString("firstName") + " " +
                                    bookingData.getJSONObject("shipper").getString("lastName")));
                            tvBookingStatus.setText(CommonUtils.toCamelCase(bookingData.getString("bookingStatus").replace("_", " ")));

                            shipperNumber = bookingData.getJSONObject("shipper").getString("phoneNumber");
                            name[0] = bookingData.getJSONObject("pickUp").getString("companyName");
                            name[1] = bookingData.getJSONObject("dropOff").getString("companyName");
                            address[0] = bookingData.getJSONObject("pickUp").getString("address") + ", " +
                                    bookingData.getJSONObject("pickUp").getString("city") + ", " +
                                    bookingData.getJSONObject("pickUp").getString("state");

                            address[1] = bookingData.getJSONObject("dropOff").getString("address") + ", " +
                                    bookingData.getJSONObject("dropOff").getString("city") + ", " +
                                    bookingData.getJSONObject("dropOff").getString("state");

                            if (location != null)
                                getDriectionToDestination(new LatLng(location.getLatitude(), location.getLongitude()),
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
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if(getActivity()!=null) {
                    CommonUtils.dismissLoadingDialog();
                    if (((RetrofitError) error).getKind() == RetrofitError.Kind.NETWORK) {
                        showRetryPopup(getActivity().getString(R.string.no_internet_access));
                    } else {
                        showRetryPopup(getActivity().getString(R.string.some_ereor_ocurred));
                    }
                }
            }
        });
    }

    /**
     * Method for google API call of get path between two latlng points
     */
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
                    markerOptions.title(getActivity().getString(R.string.current_position));
                    currentMarker = googleMap.addMarker(markerOptions);
                    String source[] = start.split(",");
                    try {
                        longitude[0] = Double.valueOf(source[1]);
                        latitude[0] = Double.valueOf(source[0]);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }

                    String destination[] = end.split(",");
                    try {
                        longitude[1] = Double.valueOf(destination[1]);
                        latitude[1] = Double.valueOf(destination[0]);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        longitude[1] = 0;
                        latitude[1] = 0;
                    }

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
                            .title(CommonUtils.toCamelCase(name[i]))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            .snippet(CommonUtils.toCamelCase(address[i]))

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
                Intent intent = new Intent(getActivity(), BookingDetailsActivity.class);
                intent.putExtra("bookingId", bookingId);
                startActivity(intent);
                Transactions.showNextAnimation(getActivity());
                break;
        }
    }

    /**
     * Method to retry when getCurrentBooking Api call fails
     */
    private void showRetryPopup(String msg) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setMessage(msg);
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                getCurrentBookings();
            }
        });

        alertDialog.setNegativeButton(getString(R.string.call_carrus), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                try {
                    Intent call = new Intent(Intent.ACTION_DIAL);
                    call.setData(Uri.parse("tel:" + "+91" + CONTACT_CARRUS));
                    startActivity(call);
                } catch (Exception e) {
                    CommonUtils.showSingleButtonPopup(getActivity(), "Unable to perform action.");
                }
            }
        });

        alertDialog.setNeutralButton("Exit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                getActivity().finish();
                Transactions.showPreviousAnimation(getActivity());
            }
        });


        alertDialog.show();
    }
}
