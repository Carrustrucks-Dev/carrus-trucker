package com.carrustruckerapp.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.carrustruckerapp.R;
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
public class CurrentShipmentFragment extends android.support.v4.app.Fragment implements View.OnClickListener {

    private GoogleMap googleMap;
    private GPSTracker gpsTracker;
    public WebServices googleWebServices;
    public GlobalClass globalClass;
    private GMapV2GetRouteDirection v2GetRouteDirection;
    private Bundle bundle;
    private RelativeLayout noBookingLayout, bookingDetailsLayout;
    private TextView tvName, tvDate, tvMonth, tvShipingJourney,tvBookingStatus,tvTimeSlot,tvTruckName;
    private ImageView callShipper;
    private String shipperNumber;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_current_shipment, container, false);
        globalClass = (GlobalClass) getActivity().getApplicationContext();
        googleWebServices = globalClass.getGoogleWebServices();
        gpsTracker = new GPSTracker(getActivity());
        googleMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();
        googleMap.setMyLocationEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
//        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setTiltGesturesEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        v2GetRouteDirection = new GMapV2GetRouteDirection();
        bundle = getArguments();
        shipperNumber=bundle.getString("shipperPhoneNumber");
        Log.e("isbooking", "" + bundle.getBoolean("isBooking"));
        if (bundle.getBoolean("isBooking")) {
            bookingDetailsLayout.setVisibility(View.VISIBLE);
            Calendar cal = Calendar.getInstance();
            TimeZone tz = cal.getTimeZone();
            DateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            f.setTimeZone(TimeZone.getTimeZone("ISO"));
            try {
                Date d = f.parse(bundle.getString("bookingCreatedAt"));
                DateFormat date = new SimpleDateFormat("dd");
                DateFormat month = new SimpleDateFormat("MMM");
                DateFormat dayName = new SimpleDateFormat("EEE");
                tvDate.setText(date.format(d));
                tvMonth.setText(month.format(d));
                tvTimeSlot.setText(dayName.format(d)+", "+bundle.getString("timeSlot"));
                tvTruckName.setText(bundle.getString("truckNameNumber"));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            tvShipingJourney.setText(bundle.getString("shippingJourney"));
            tvName.setText(CommonUtils.toCamelCase(bundle.getString("shipperName")));
            tvBookingStatus.setText(bundle.getString("bookingStatus").replace("_", " "));

            getDriectionToDestination(new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude()), bundle.getString("pickUpLat") + ", " + bundle.getString("pickUpLong"), bundle.getString("dropOffLat") + ", " + bundle.getString("dropOffLong"), GMapV2GetRouteDirection.MODE_DRIVING);
            if (bundle.getString("tracking").equalsIgnoreCase("YES")) {
                Intent intent = new Intent(getActivity(), MyService.class);
                intent.putExtra("bookingId", bundle.getString("bookingId"));

                getActivity().startService(intent);
            }
        } else {
            noBookingLayout.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

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
                    PolylineOptions rectLine = new PolylineOptions().width(6).color(Color.GREEN);

                    for (int i = 0; i < directionPoint.size(); i++) {
                        rectLine.add(directionPoint.get(i));
                    }
                    // Adding route on the map
                    googleMap.addPolyline(rectLine);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(currentposition);
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
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
                try {
                    Intent call = new Intent(Intent.ACTION_DIAL);
                    call.setData(Uri.parse("tel:" + "+91" + shipperNumber));
                    startActivity(call);
                } catch (Exception e) {
                    CommonUtils.showSingleButtonPopup(getActivity(),"Unable to perform action.");
                }
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
