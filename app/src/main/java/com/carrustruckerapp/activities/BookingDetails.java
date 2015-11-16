package com.carrustruckerapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.carrustruckerapp.R;
import com.carrustruckerapp.adapters.ExpandableListAdapter;
import com.carrustruckerapp.entities.ExpandableChildItem;
import com.carrustruckerapp.interfaces.AppConstants;
import com.carrustruckerapp.interfaces.WebServices;
import com.carrustruckerapp.utils.CommonUtils;
import com.carrustruckerapp.utils.GlobalClass;
import com.carrustruckerapp.utils.Log;
import com.carrustruckerapp.utils.MaterialDesignAnimations;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class BookingDetails extends BaseActivity implements View.OnClickListener, AppConstants{


    private String bookingId;
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<ExpandableChildItem>> listDataChild;
    ExpandableChildItem expandableChildItem;
    public GlobalClass globalClass;
    public WebServices webServices;
    public CommonUtils commonUtils;
    public String accessToken;
    public SharedPreferences sharedPreferences;
    public String dropOffPhoneNumber,pickUpPhoneNumber;
    ImageView pickUpCallButton,dropOffCallButton;
    private RelativeLayout orderStatusLayout;
    private TextView crnNumber,orderStatus,truckName,truckNumber,pickUpTruckerName,pickupName,
            pickUpLocation,pickUpDay,pickUpTime,dropOffTruckerName,dropOffName,dropOffLocation,dropOffDay,dropOffTime
            ,paymentMethod,totalCost;
    private Button orderStatusButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);
        init();



        // Listview Group click listener
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                setListViewHeight(parent, groupPosition);
                return false;
            }
        });

        // Listview Group expanded listener
        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {


            }
        });

        // Listview Group collasped listener
        expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {


            }
        });

        // Listview on child click listener
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                return false;
            }
        });
    }

//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//
//        Drawable drawable_groupIndicator =
//                getResources().getDrawable(R.drawable.expandablelistview_indicator_selector);
//        int drawable_width = drawable_groupIndicator.getMinimumWidth();
//
//        if (android.os.Build.VERSION.SDK_INT <
//                android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
//            expListView.setIndicatorBounds(
//                    expListView.getWidth() - drawable_width,
//                    expListView.getWidth());
//        } else {
//            expListView.setIndicatorBoundsRelative(
//                    expListView.getWidth() - drawable_width,
//                    expListView.getWidth());
//        }
//    }

    private void init() {
        bookingId = getIntent().getStringExtra("bookingId");
//        setListViewHeight(expListView);
        findViewById(R.id.back_button).setOnClickListener(this);
        sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        accessToken = sharedPreferences.getString(ACCESS_TOKEN, "");
        globalClass = (GlobalClass) getApplicationContext();
        webServices = globalClass.getWebServices();
        commonUtils = new CommonUtils();
        crnNumber=(TextView) findViewById(R.id.crn_number);
        orderStatus=(TextView) findViewById(R.id.order_status);
        truckName=(TextView) findViewById(R.id.truckName);
        truckNumber=(TextView) findViewById(R.id.truckNumber);
        pickUpTruckerName=(TextView) findViewById(R.id.pickup_trucker_name);
        pickupName=(TextView) findViewById(R.id.pickup_name);
        pickUpLocation=(TextView) findViewById(R.id.pickup_location);
        pickUpDay=(TextView) findViewById(R.id.pickup_day);
        pickUpTime=(TextView) findViewById(R.id.pickup_time);
        dropOffTruckerName=(TextView) findViewById(R.id.dropup_trucker_name);
        dropOffName=(TextView) findViewById(R.id.dropup_name);
        dropOffLocation=(TextView) findViewById(R.id.dropup_location);
        dropOffDay=(TextView) findViewById(R.id.dropup_day);
        dropOffTime=(TextView) findViewById(R.id.dropup_time);
        pickUpCallButton=(ImageView) findViewById(R.id.pickup_call);
        pickUpCallButton.setOnClickListener(this);
        dropOffCallButton=(ImageView) findViewById(R.id.dropoff_call);
        dropOffCallButton.setOnClickListener(this);
        orderStatusLayout=(RelativeLayout)findViewById(R.id.order_status_layout);
        paymentMethod=(TextView)findViewById(R.id.payment_method);
        totalCost=(TextView)findViewById(R.id.total_amount);
        orderStatusButton=(Button)findViewById(R.id.status_button);
        orderStatusButton.setOnClickListener(this);
        findViewById(R.id.collect_cash_button).setOnClickListener(this);
        findViewById(R.id.retry_button).setOnClickListener(this);
        expListView = (ExpandableListView) findViewById(R.id.exListView);
        getOrderDetails();
    }


    void getOrderDetails(){
        commonUtils.showLoadingDialog(BookingDetails.this, getResources().getString(R.string.loading));
            webServices.getBookingDetails(accessToken,bookingId,
                    new Callback<String>() {
                        @Override
                        public void success(String serverResponse, Response response) {
                            findViewById(R.id.scrollView).setVisibility(View.VISIBLE);
                            try {
                                JSONObject jsonObject=new JSONObject(serverResponse);
                                Calendar cal = Calendar.getInstance();
                                TimeZone tz = cal.getTimeZone();
                                DateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                f.setTimeZone(TimeZone.getTimeZone("ISO"));
                                Date d = f.parse(String.valueOf(jsonObject.getJSONObject("data").getJSONObject("pickUp").getString("date")));
                                Date d1 = f.parse(String.valueOf(jsonObject.getJSONObject("data").getJSONObject("dropOff").getString("date")));
                                DateFormat date = new SimpleDateFormat("EEEE, d");
                                DateFormat dayNumber = new SimpleDateFormat("d");
                                int day = Integer.parseInt(dayNumber.format(d));
                                int day1 = Integer.parseInt(dayNumber.format(d1));
                                DateFormat month = new SimpleDateFormat("MMMM");
                                crnNumber.setText("CRN - " + jsonObject.getJSONObject("data").getString("crn").toUpperCase());
                                if(jsonObject.getJSONObject("data").getString("bookingStatus").equals("REACHED_DESTINATION")||
                                        jsonObject.getJSONObject("data").getString("bookingStatus").equals("REACHED_PICKUP_LOCATION")) {
                                    orderStatusLayout.setBackgroundColor(getResources().getColor(R.color.orange));
                                }
                                if(jsonObject.getJSONObject("data").getString("bookingStatus").equals("ON_GOING")||
                                        jsonObject.getJSONObject("data").getString("bookingStatus").equals("UP_GOING")) {
                                    orderStatusLayout.setBackgroundColor(getResources().getColor(R.color.dark_blue));
                                }
                                if(jsonObject.getJSONObject("data").getString("bookingStatus").equals("CONFIRMED")) {
                                    orderStatusLayout.setBackgroundColor(getResources().getColor(R.color.green));
                                }
                                if(jsonObject.getJSONObject("data").getString("bookingStatus").equals("HALT")||
                                        jsonObject.getJSONObject("data").getString("bookingStatus").equals("COMPLETED")) {
                                    orderStatusLayout.setBackgroundColor(getResources().getColor(R.color.dark_gery));
                                }
                                if(jsonObject.getJSONObject("data").getString("bookingStatus").equals("CANCELED")) {
                                    orderStatusLayout.setBackgroundColor(getResources().getColor(R.color.red));
                                }
                                if(jsonObject.getJSONObject("data").getString("bookingStatus").equals("ON_GOING")||
                                        jsonObject.getJSONObject("data").getString("bookingStatus").equals("HALT")||
                                        jsonObject.getJSONObject("data").getString("bookingStatus").equals("REACHED_DESTINATION")){
                                    orderStatusButton.setVisibility(View.VISIBLE);
                                    orderStatusButton.setText("END TRIP");

                                }
                                if(jsonObject.getJSONObject("data").getString("bookingStatus").equals("ON_THE_WAY")||
                                        jsonObject.getJSONObject("data").getString("bookingStatus").equals("REACHED_PICKUP_LOCATION")
                                        ){
                                    orderStatusButton.setVisibility(View.VISIBLE);
                                    orderStatusButton.setText("START TRIP");

                                }
                                if(jsonObject.getJSONObject("data").getString("bookingStatus").equals("CONFIRMED")){
                                    orderStatusButton.setVisibility(View.VISIBLE);
                                    orderStatusButton.setText("ON THE WAY");

                                }

                                orderStatus.setText(jsonObject.getJSONObject("data").getString("bookingStatus").toUpperCase().replace("_", " "));
                                truckName.setText(jsonObject.getJSONObject("data").getJSONObject("truck").getJSONObject("truckType").getString("typeTruckName").toUpperCase());
                                truckNumber.setText(jsonObject.getJSONObject("data").getJSONObject("assignTruck").getString("truckNumber").toUpperCase());
                                pickUpTruckerName.setText(jsonObject.getJSONObject("data").getJSONObject("pickUp").getString("companyName"));
                                pickupName.setText(jsonObject.getJSONObject("data").getJSONObject("pickUp").getString("name"));
                                pickUpLocation.setText(jsonObject.getJSONObject("data").getJSONObject("pickUp").getString("address")+", "+
                                        jsonObject.getJSONObject("data").getJSONObject("pickUp").getString("city")+", "+
                                        jsonObject.getJSONObject("data").getJSONObject("pickUp").getString("state")+" - "+
                                        jsonObject.getJSONObject("data").getJSONObject("pickUp").getString("zipCode"));
                                pickUpPhoneNumber=jsonObject.getJSONObject("data").getJSONObject("pickUp").getString("contactNumber");
                                pickUpDay.setText(date.format(d)+commonUtils.getDateSuffix(day)+" "+month.format(d));
                                pickUpTime.setText(jsonObject.getJSONObject("data").getJSONObject("pickUp").getString("time").toUpperCase());
                                dropOffTruckerName.setText(jsonObject.getJSONObject("data").getJSONObject("dropOff").getString("companyName"));
                                dropOffName.setText(jsonObject.getJSONObject("data").getJSONObject("dropOff").getString("name"));
                                dropOffLocation.setText(jsonObject.getJSONObject("data").getJSONObject("dropOff").getString("address")+", "+
                                        jsonObject.getJSONObject("data").getJSONObject("dropOff").getString("city")+", "+
                                        jsonObject.getJSONObject("data").getJSONObject("dropOff").getString("state")+" - "+
                                        jsonObject.getJSONObject("data").getJSONObject("dropOff").getString("zipCode"));
                                dropOffPhoneNumber=jsonObject.getJSONObject("data").getJSONObject("dropOff").getString("contactNumber");
                                dropOffDay.setText(date.format(d1)+commonUtils.getDateSuffix(day1)+" "+month.format(d1));
                                dropOffTime.setText(jsonObject.getJSONObject("data").getJSONObject("dropOff").getString("time").toUpperCase());
                                paymentMethod.setText(jsonObject.getJSONObject("data").getString("paymentMode").toUpperCase());
                                totalCost.setText(getResources().getString(R.string.indian_rupee_symbol)+" "+jsonObject.getJSONObject("data").getJSONObject("bid").getString("acceptPrice"));
                                if(jsonObject.getJSONObject("data").getString("paymentStatus").equalsIgnoreCase("Pending")
                                        &&jsonObject.getJSONObject("data").getString("paymentMode").equalsIgnoreCase("Cash"))
                                    findViewById(R.id.collect_cash_button).setVisibility(View.VISIBLE);

//                                prepareListData();
                                listDataHeader = new ArrayList<String>();
                                listDataChild = new HashMap<String, List<ExpandableChildItem>>();

                                // Adding child data
                                listDataHeader.add(getResources().getString(R.string.cargo_details));
                                listDataHeader.add(getResources().getString(R.string.documents));
                                listDataHeader.add(getResources().getString(R.string.advisory_checkList));
                                listDataHeader.add(getResources().getString(R.string.notes));
                                listDataHeader.add(getResources().getString(R.string.my_notes));


                                // Adding child data
                                ArrayList<ExpandableChildItem> cargoDetails = new ArrayList<ExpandableChildItem>();
                                cargoDetails.add(new ExpandableChildItem("Type of Cargo", CommonUtils.toCamelCase(jsonObject.getJSONObject("data").getJSONObject("cargo").getJSONObject("cargoType").getString("typeCargoName"))));
                                cargoDetails.add(new ExpandableChildItem("Weight",jsonObject.getJSONObject("data").getJSONObject("cargo").getString("weight")+" Ton"));

                                ArrayList<ExpandableChildItem> documents = new ArrayList<ExpandableChildItem>();
                                documents.add(new ExpandableChildItem(getString(R.string.pod),""));
                                documents.add(new ExpandableChildItem(getString(R.string.invoice),""));
                                documents.add(new ExpandableChildItem(getString(R.string.consignment),""));


                                ArrayList<ExpandableChildItem> checklist = new ArrayList<ExpandableChildItem>();
                                checklist.add(new ExpandableChildItem("Checklist 1", "Driving License"));
                                checklist.add(new ExpandableChildItem("Checklist 2", "PAN"));
                                checklist.add(new ExpandableChildItem("Checklist 3", "SIN"));

                                ArrayList<ExpandableChildItem> notes = new ArrayList<ExpandableChildItem>();
                                notes.add(new ExpandableChildItem(jsonObject.getJSONObject("data").getString("jobNote"),""));

                                ArrayList<ExpandableChildItem> myNotes = new ArrayList<ExpandableChildItem>();
                                if(jsonObject.getJSONObject("data").has("truckerNote")){
                                    myNotes.add(new ExpandableChildItem(jsonObject.getJSONObject("data").getString("truckerNote"), ""));
                                }else {
                                    myNotes.add(new ExpandableChildItem("", ""));
                                }

                                listDataChild.put(listDataHeader.get(0), cargoDetails); // Header, Child data
                                listDataChild.put(listDataHeader.get(1), documents);
                                listDataChild.put(listDataHeader.get(2), checklist);
                                listDataChild.put(listDataHeader.get(3), notes);
                                listDataChild.put(listDataHeader.get(4), myNotes);
                                listAdapter = new ExpandableListAdapter(BookingDetails.this,bookingId, listDataHeader, listDataChild);
                                expListView.setAdapter(listAdapter);
                                setListViewHeight(expListView);
                                final ScrollView scrollview = (ScrollView)findViewById(R.id.scrollView);

                                scrollview.post(new Runnable() {
                                    public void run() {
                                        scrollview.scrollTo(0, 0);
                                    }
                                });

                            } catch (Exception e) {
                                e.printStackTrace();
                                commonUtils.dismissLoadingDialog();
                            }


                            commonUtils.dismissLoadingDialog();
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            if (((RetrofitError) retrofitError).getKind() == RetrofitError.Kind.NETWORK) {
                                MaterialDesignAnimations.fadeIn(getApplicationContext(), findViewById(R.id.errorLayout), getResources().getString(R.string.internetConnectionError), 0);
                                findViewById(R.id.scrollView).setVisibility(View.GONE);
                                findViewById(R.id.retry_button).setVisibility(View.VISIBLE);
                                commonUtils.dismissLoadingDialog();
                            } else {
                                commonUtils.dismissLoadingDialog();
                                commonUtils.showRetrofitError(BookingDetails.this, retrofitError);
                            }
                        }
                    });
    }

    private void prepareListData() {

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.collect_cash_button:
                commonUtils.showLoadingDialog(BookingDetails.this, getResources().getString(R.string.loading));
                webServices.collectCash(accessToken, bookingId, "PAID", new Callback<String>() {
                    @Override
                    public void success(String serverResponse, Response response) {
                        Log.i("Success", "" + serverResponse);
                        findViewById(R.id.collect_cash_button).setVisibility(View.GONE);
                        try {
                            JSONObject jsonObject=new JSONObject(serverResponse);
                            CommonUtils.showSingleButtonPopup(BookingDetails.this,jsonObject.getString("message"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        commonUtils.dismissLoadingDialog();

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.i("Failure", "" + error);
                        commonUtils.dismissLoadingDialog();
                    }
                });
                break;
            case R.id.retry_button:

                findViewById(R.id.retry_button).setVisibility(View.GONE);
                getOrderDetails();
                break;


            case R.id.back_button:
                finish();
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
                break;

            case R.id.pickup_call:
                try {
                    Intent call = new Intent(Intent.ACTION_DIAL);
                    call.setData(Uri.parse("tel:" + "+91"+pickUpPhoneNumber));
                    startActivity(call);
                } catch (Exception e) {
                    commonUtils.showSingleButtonPopup(BookingDetails.this,"Unable to perform action.");
                }
                break;

            case R.id.dropoff_call:
                try {
                    Intent call = new Intent(Intent.ACTION_DIAL);
                    call.setData(Uri.parse("tel:" + "+91"+dropOffPhoneNumber));
                    startActivity(call);
                } catch (Exception e) {
                    commonUtils.showSingleButtonPopup(BookingDetails.this,"Unable to perform action.");
                }
                break;

            case R.id.status_button:
                if(orderStatusButton.getText().equals("END TRIP")){
                    commonUtils.showLoadingDialog(BookingDetails.this, getResources().getString(R.string.loading));
                    webServices.completeOrder(accessToken, bookingId, "COMPLETED", new Callback<String>() {
                        @Override
                        public void success(String s, Response response) {
                            Log.i("Success", "" + s);
                            orderStatusButton.setVisibility(View.GONE);
                            commonUtils.dismissLoadingDialog();
                            getOrderDetails();
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.i("Failure", "" + error);
                            commonUtils.dismissLoadingDialog();
                        }
                    });
                }else if(orderStatusButton.getText().equals("ON THE WAY")){
                    commonUtils.showLoadingDialog(BookingDetails.this, getResources().getString(R.string.loading));
                    webServices.changeOrderStatus(accessToken, bookingId, "ON_THE_WAY", new Callback<String>() {
                        @Override
                        public void success(String s, Response response) {
                            Log.i("Success", "" + s);
                            orderStatusButton.setVisibility(View.GONE);
                            commonUtils.dismissLoadingDialog();
                            getOrderDetails();
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.i("Failure", "" + error);
                            commonUtils.dismissLoadingDialog();
                        }
                    });
                }else if(orderStatusButton.getText().equals("START TRIP")){
                    commonUtils.showLoadingDialog(BookingDetails.this, getResources().getString(R.string.loading));
                    webServices.changeOrderStatus(accessToken, bookingId, "ON_GOING", new Callback<String>() {
                        @Override
                        public void success(String s, Response response) {
                            Log.i("Success",""+s);
                            orderStatusButton.setVisibility(View.GONE);
                            commonUtils.dismissLoadingDialog();
                            getOrderDetails();
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.i("Failure",""+error);
                            commonUtils.dismissLoadingDialog();
                        }
                    });
                }
                break;

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    private void setListViewHeight(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }


    private void setListViewHeight(ExpandableListView listView,
                                   int group) {
        ExpandableListAdapter listAdapter = (ExpandableListAdapter) listView.getExpandableListAdapter();
        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(),
                View.MeasureSpec.EXACTLY);
        for (int i = 0; i < listAdapter.getGroupCount(); i++) {
            View groupItem = listAdapter.getGroupView(i, false, null, listView);
            groupItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);

            totalHeight += groupItem.getMeasuredHeight();

            if (((listView.isGroupExpanded(i)) && (i != group))
                    || ((!listView.isGroupExpanded(i)) && (i == group))) {
                for (int j = 0; j < listAdapter.getChildrenCount(i); j++) {
                    View listItem = listAdapter.getChildView(i, j, false, null,
                            listView);
                    listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);

                    totalHeight += listItem.getMeasuredHeight();

                }
            }
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        int height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getGroupCount() - 1));
        if (height < 10)
            height = 200;
        params.height = height;
        listView.setLayoutParams(params);
        listView.requestLayout();

    }


}
