package com.carrus.trucker.activities;

import android.content.Intent;
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

import com.carrus.trucker.R;
import com.carrus.trucker.adapters.ExpandableListAdapter;
import com.carrus.trucker.interfaces.ActivityResultCallback;
import com.carrus.trucker.models.ExpandableChildItem;
import com.carrus.trucker.retrofit.RestClient;
import com.carrus.trucker.utils.ApiResponseFlags;
import com.carrus.trucker.utils.CommonUtils;
import com.carrus.trucker.utils.Log;
import com.carrus.trucker.utils.MaterialDesignAnimations;
import com.carrus.trucker.utils.Transactions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class BookingDetailsActivity extends BaseActivity implements View.OnClickListener, ActivityResultCallback, ExpandableListView.OnChildClickListener {


    private String bookingId;
    private ExpandableListAdapter listAdapter;
    private ExpandableListView expListView;
    private List<String> listDataHeader;
    private HashMap<String, List<ExpandableChildItem>> listDataChild;
    private String dropOffPhoneNumber, pickUpPhoneNumber;
    private ImageView ivPickupCall, ivDropoffCall;
    private RelativeLayout orderStatusLayout, paymentCollectedLayout;
    private TextView tvCrnNumber, tvOrderStatus, tvTruckName, tvTruckNumber, tvPickupTruckerName, tvPickupName,
            tvPickupLocation, tvPickupDay, tvPickupTime, tvDropoffTruckerName, tvDropoffName, tvDropoffLocation, tvDropoffDay, tvDropoffTime,
            tvPaymentMethod, tvTotalAmount,
            tvPaymentStatus;
    private Button btnStatus;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);

        //Set up touch listener for non-text box views to hide keyboard.
        setupUI(getWindow().getDecorView().getRootView());

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

        getOrderDetails();
    }

    /**
     * Method to initialize all the {@link View}s inside the Layout of this
     * {@link Activity}
     */
    private void init() {
        //Get values from intent
        bookingId = getIntent().getStringExtra("bookingId");

        //Get Resource ID from XML
        tvCrnNumber = (TextView) findViewById(R.id.tvCrnNumber);
        tvOrderStatus = (TextView) findViewById(R.id.tvOrderStatus);
        tvTruckName = (TextView) findViewById(R.id.tvTruckName);
        tvTruckNumber = (TextView) findViewById(R.id.tvTruckNumber);
        tvPickupTruckerName = (TextView) findViewById(R.id.tvPickupTruckerName);
        tvPickupName = (TextView) findViewById(R.id.tvPickupName);
        tvPickupLocation = (TextView) findViewById(R.id.tvPickupLocation);
        tvPickupDay = (TextView) findViewById(R.id.tvPickupDay);
        tvPickupTime = (TextView) findViewById(R.id.tvPickupTime);
        tvDropoffTruckerName = (TextView) findViewById(R.id.tvDropoffTruckerName);
        tvDropoffName = (TextView) findViewById(R.id.tvDropoffName);
        tvDropoffLocation = (TextView) findViewById(R.id.tvDropoffLocation);
        tvDropoffDay = (TextView) findViewById(R.id.tvDropoffDay);
        tvDropoffTime = (TextView) findViewById(R.id.tvDropoffTime);
        ivPickupCall = (ImageView) findViewById(R.id.ivPickupCall);
        ivDropoffCall = (ImageView) findViewById(R.id.ivDropoffCall);
        tvPaymentMethod = (TextView) findViewById(R.id.tvPaymentMethod);
        tvTotalAmount = (TextView) findViewById(R.id.tvTotalAmount);
        tvPaymentStatus = (TextView) findViewById(R.id.tvPaymentStatus);
        btnStatus = (Button) findViewById(R.id.btnStatus);
        expListView = (ExpandableListView) findViewById(R.id.exListView);
        orderStatusLayout = (RelativeLayout) findViewById(R.id.order_status_layout);
        paymentCollectedLayout = (RelativeLayout) findViewById(R.id.payment_collected_layout);

        //Set Listeners
        ivPickupCall.setOnClickListener(this);
        ivDropoffCall.setOnClickListener(this);
        btnStatus.setOnClickListener(this);
        findViewById(R.id.btnCollectCash).setOnClickListener(this);
        findViewById(R.id.retry_button).setOnClickListener(this);
        findViewById(R.id.ivBackButton).setOnClickListener(this);
        expListView.setOnChildClickListener(this);

        //Initialize expendable listView headers
        listDataHeader = new ArrayList<String>();
        listDataHeader.add(getString(R.string.cargo_details));
        listDataHeader.add(getString(R.string.documents));
        listDataHeader.add(getString(R.string.advisory_checkList));
        listDataHeader.add(getString(R.string.notes));
        listDataHeader.add(getString(R.string.my_notes));

    }

    /**
     * Method for API call of get details for particular order
     */
    public void getOrderDetails() {
        CommonUtils.showLoadingDialog(BookingDetailsActivity.this, getResources().getString(R.string.loading));
        RestClient.getWebServices().getBookingDetails(accessToken, bookingId,
                new Callback<String>() {
                    @Override
                    public void success(String serverResponse, Response response) {
                        findViewById(R.id.scrollView).setVisibility(View.VISIBLE);
                        try {
                            JSONObject jsonObject = new JSONObject(serverResponse);
                            JSONObject data = jsonObject.getJSONObject("data");
                            tvCrnNumber.setText(getString(R.string.crn) + " - " + data.getString("crn").toUpperCase());
                            orderStatusLayout.setBackgroundColor(statusBarColor(data.getString("bookingStatus").toUpperCase()));
                            btnStatus.setText(textOnButton(data.getString("bookingStatus").toUpperCase()));
                            if (data.getString("paymentStatus").equalsIgnoreCase("PENDING")) {
                                tvPaymentStatus.setText(getString(R.string.no));
                            } else {
                                tvPaymentStatus.setText(getString(R.string.yes));
                            }

                            JSONObject pickupJson = data.getJSONObject("pickUp");
                            tvOrderStatus.setText(CommonUtils.toCamelCase(data.getString("bookingStatus").toUpperCase().replace("_", " ")));
                            tvTruckName.setText(data.getJSONObject("truck").getJSONObject("truckType").getString("typeTruckName").toUpperCase());
                            tvTruckNumber.setText(data.getJSONObject("assignTruck").getString("truckNumber").toUpperCase());
                            tvPickupTruckerName.setText(pickupJson.getString("companyName"));
                            tvPickupName.setText(pickupJson.getString("name"));
                            tvPickupLocation.setText(pickupJson.getString("address") + ", " +
                                    pickupJson.getString("city") + ", " +
                                    pickupJson.getString("state") + " - " +
                                    pickupJson.getString("zipCode"));
                            pickUpPhoneNumber = pickupJson.getString("contactNumber");
                            tvPickupDay.setText(CommonUtils.getDayNameNumberFromUTC(pickupJson.getString("date"))
                                    + CommonUtils.getDateSuffix(CommonUtils.getDayNumberFromUTC(pickupJson.getString("date")))
                                    + " " + CommonUtils.getMonthNameFromUTC(pickupJson.getString("date")));
                            tvPickupTime.setText(pickupJson.getString("time").toUpperCase());
                            JSONObject dropoffJson = data.getJSONObject("dropOff");
                            tvDropoffTruckerName.setText(dropoffJson.getString("companyName"));
                            tvDropoffName.setText(dropoffJson.getString("name"));
                            tvDropoffLocation.setText(dropoffJson.getString("address") + ", " +
                                    dropoffJson.getString("city") + ", " +
                                    dropoffJson.getString("state") + " - " +
                                    dropoffJson.getString("zipCode"));
                            dropOffPhoneNumber = dropoffJson.getString("contactNumber");
                            tvDropoffDay.setText(CommonUtils.getDayNameNumberFromUTC(dropoffJson.getString("date"))
                                    + CommonUtils.getDateSuffix(CommonUtils.getDayNumberFromUTC(dropoffJson.getString("date")))
                                    + " " + CommonUtils.getMonthNameFromUTC(dropoffJson.getString("date")));


                            tvDropoffTime.setText(dropoffJson.getString("time").toUpperCase());
                            tvPaymentMethod.setText(data.getString("paymentMode").toUpperCase());
                            tvTotalAmount.setText(getString(R.string.indian_rupee_symbol) + " " + data.getJSONObject("bid").getString("acceptPrice"));
                            if (data.getString("paymentStatus").equalsIgnoreCase("Pending")
                                    && data.getString("paymentMode").equalsIgnoreCase("Cash"))
                                findViewById(R.id.btnCollectCash).setVisibility(View.VISIBLE);
                            listDataChild = new HashMap<String, List<ExpandableChildItem>>();

                            // Adding child data
                            ArrayList<ExpandableChildItem> cargoDetails = new ArrayList<ExpandableChildItem>();
                            cargoDetails.add(new ExpandableChildItem(getString(R.string.type_of_cargo), CommonUtils.toCamelCase(data.getJSONObject("cargo").getJSONObject("cargoType").getString("typeCargoName"))));
                            cargoDetails.add(new ExpandableChildItem(getString(R.string.weight), data.getJSONObject("cargo").getString("weight") + " Ton"));

                            ArrayList<ExpandableChildItem> documents = new ArrayList<ExpandableChildItem>();
                            if (jsonObject.getJSONObject("data").has("doc")) {
                                JSONObject docJson = data.getJSONObject("doc");
                                documents.add(new ExpandableChildItem(getString(R.string.pod), docJson.has("pod") ? docJson.getString("pod") : null));
                                documents.add(new ExpandableChildItem(getString(R.string.invoice), docJson.has("invoice") ? docJson.getString("invoice") : null));
                                documents.add(new ExpandableChildItem(getString(R.string.consignment), docJson.has("consigmentNote") ? docJson.getString("consigmentNote") : null));
                            } else {
                                documents.add(new ExpandableChildItem(getString(R.string.pod), null));
                                documents.add(new ExpandableChildItem(getString(R.string.invoice), null));
                                documents.add(new ExpandableChildItem(getString(R.string.consignment), null));
                            }


                            ArrayList<ExpandableChildItem> checklist = new ArrayList<ExpandableChildItem>();
                            checklist.add(new ExpandableChildItem("Checklist 1", "Driving License"));
                            checklist.add(new ExpandableChildItem("Checklist 2", "PAN"));
                            checklist.add(new ExpandableChildItem("Checklist 3", "SIN"));

                            ArrayList<ExpandableChildItem> notes = new ArrayList<ExpandableChildItem>();
                            notes.add(new ExpandableChildItem(data.getString("jobNote"), ""));

                            ArrayList<ExpandableChildItem> myNotes = new ArrayList<ExpandableChildItem>();
                            if (data.has("truckerNote") && !data.isNull("truckerNote")) {

                                myNotes.add(new ExpandableChildItem(data.getString("truckerNote"), ""));
                            } else {
                                myNotes.add(new ExpandableChildItem("", ""));
                            }

                            listDataChild.put(listDataHeader.get(0), cargoDetails); // Header, Child data
                            listDataChild.put(listDataHeader.get(1), documents);
                            listDataChild.put(listDataHeader.get(2), checklist);
                            listDataChild.put(listDataHeader.get(3), notes);
                            listDataChild.put(listDataHeader.get(4), myNotes);
                            listAdapter = new ExpandableListAdapter(BookingDetailsActivity.this, bookingId, listDataHeader, listDataChild);
                            expListView.setAdapter(listAdapter);
                            setListViewHeight(expListView);  //CommonUtils.setListViewHeightBasedOnChildren(expListView);
                            final ScrollView scrollview = (ScrollView) findViewById(R.id.scrollView);
                            expListView.setOnChildClickListener(BookingDetailsActivity.this);
                            scrollview.post(new Runnable() {
                                public void run() {
                                    scrollview.scrollTo(0, 0);
                                }
                            });


                        } catch (Exception e) {
                            e.printStackTrace();
                            CommonUtils.dismissLoadingDialog();
                        }


                        CommonUtils.dismissLoadingDialog();
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        if (((RetrofitError) retrofitError).getKind() == RetrofitError.Kind.NETWORK) {
                            MaterialDesignAnimations.fadeIn(getApplicationContext(), findViewById(R.id.errorLayout), getResources().getString(R.string.internetConnectionError), 0);
                            findViewById(R.id.scrollView).setVisibility(View.GONE);
                            findViewById(R.id.retry_button).setVisibility(View.VISIBLE);
                            CommonUtils.dismissLoadingDialog();
                        } else {
                            int statusCode = retrofitError.getResponse().getStatus();
                            if (ApiResponseFlags.Not_Found.getOrdinal() == statusCode) {
                                finish();
                            } else {
                                CommonUtils.dismissLoadingDialog();
                                CommonUtils.showRetrofitError(BookingDetailsActivity.this, retrofitError);
                            }
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCollectCash:
                CommonUtils.showLoadingDialog(BookingDetailsActivity.this, getResources().getString(R.string.loading));
                RestClient.getWebServices().collectCash(accessToken, bookingId, "PAID", new Callback<String>() {
                    @Override
                    public void success(String serverResponse, Response response) {
                        findViewById(R.id.btnCollectCash).setVisibility(View.GONE);
                        try {
                            JSONObject jsonObject = new JSONObject(serverResponse);
                            CommonUtils.showSingleButtonPopup(BookingDetailsActivity.this, jsonObject.getString("message"));
                            getOrderDetails();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        CommonUtils.dismissLoadingDialog();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        CommonUtils.dismissLoadingDialog();
                        CommonUtils.showRetrofitError(BookingDetailsActivity.this, error);
                    }
                });
                break;
            case R.id.retry_button:
                findViewById(R.id.retry_button).setVisibility(View.GONE);
                getOrderDetails();
                break;


            case R.id.ivBackButton:
                finish();
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
                break;

            case R.id.ivPickupCall:
                CommonUtils.phoneCall(BookingDetailsActivity.this, pickUpPhoneNumber);
                break;

            case R.id.ivDropoffCall:
                CommonUtils.phoneCall(BookingDetailsActivity.this, dropOffPhoneNumber);
                break;

            case R.id.btnStatus:
                switch (btnStatus.getText().toString()) {
                    case "END TRIP":
                        CommonUtils.showLoadingDialog(BookingDetailsActivity.this, getResources().getString(R.string.loading));
                        RestClient.getWebServices().completeOrder(accessToken, bookingId, "COMPLETED", new Callback<String>() {
                            @Override
                            public void success(String s, Response response) {
                                Log.i("Success", "" + s);
                                btnStatus.setVisibility(View.GONE);
                                CommonUtils.dismissLoadingDialog();
                                Intent intent = new Intent(BookingDetailsActivity.this, RatingDialogActivity.class);
                                intent.putExtra("bookingId", bookingId);
                                startActivityForResult(intent, FIVE_REQUEST_CODE);
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Log.i("Failure", "" + error);
                                CommonUtils.showRetrofitError(BookingDetailsActivity.this, error);
                                CommonUtils.dismissLoadingDialog();
                            }
                        });
                        break;
                    case "ON THE WAY":
                        changeStatus("ON_THE_WAY");
                        break;

                    case "START TRIP":
                        changeStatus("ON_GOING");
                        break;
                }

//                if (btnStatus.getText().equals("END TRIP")) {
//                    CommonUtils.showLoadingDialog(BookingDetailsActivity.this, getResources().getString(R.string.loading));
//                    RestClient.getWebServices().completeOrder(accessToken, bookingId, "COMPLETED", new Callback<String>() {
//                        @Override
//                        public void success(String s, Response response) {
//                            Log.i("Success", "" + s);
//                            btnStatus.setVisibility(View.GONE);
//                            CommonUtils.dismissLoadingDialog();
//                            Intent intent = new Intent(BookingDetailsActivity.this, RatingDialogActivity.class);
//                            intent.putExtra("bookingId", bookingId);
//                            startActivityForResult(intent, FIVE_REQUEST_CODE);
//                        }
//
//                        @Override
//                        public void failure(RetrofitError error) {
//                            Log.i("Failure", "" + error);
//                            CommonUtils.showRetrofitError(BookingDetailsActivity.this, error);
//                            CommonUtils.dismissLoadingDialog();
//                        }
//                    });
//                } else if (btnStatus.getText().equals("ON THE WAY")) {
//                    CommonUtils.showLoadingDialog(BookingDetailsActivity.this, getResources().getString(R.string.loading));
//                    RestClient.getWebServices().changeOrderStatus(accessToken, bookingId, "ON_THE_WAY", new Callback<String>() {
//                        @Override
//                        public void success(String s, Response response) {
//                            Log.i("Success", "" + s);
//                            btnStatus.setVisibility(View.GONE);
//                            CommonUtils.dismissLoadingDialog();
//                            getOrderDetails();
//                        }
//
//                        @Override
//                        public void failure(RetrofitError error) {
//                            Log.i("Failure", "" + error);
//                            CommonUtils.showRetrofitError(BookingDetailsActivity.this, error);
//                            CommonUtils.dismissLoadingDialog();
//                        }
//                    });
//                } else if (btnStatus.getText().equals("START TRIP")) {
//                    CommonUtils.showLoadingDialog(BookingDetailsActivity.this, getResources().getString(R.string.loading));
//                    RestClient.getWebServices().changeOrderStatus(accessToken, bookingId, "ON_GOING", new Callback<String>() {
//                        @Override
//                        public void success(String s, Response response) {
//                            Log.i("Success", "" + s);
//                            btnStatus.setVisibility(View.GONE);
//                            CommonUtils.dismissLoadingDialog();
//                            getOrderDetails();
//                        }
//
//                        @Override
//                        public void failure(RetrofitError error) {
//                            Log.i("Failure", "" + error);
//                            CommonUtils.showRetrofitError(BookingDetailsActivity.this, error);
//                            CommonUtils.dismissLoadingDialog();
//                        }
//                    });
//                }
                break;


        }
    }

    @Override
    public void onBackPressed() {
        finish();
        Transactions.showPreviousAnimation(BookingDetailsActivity.this);
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



//    private void setListViewHeight(ListView listView) {
//        ListAdapter listAdapter = listView.getAdapter();
//        int totalHeight = 0;
//        for (int i = 0; i < listAdapter.getCount(); i++) {
//            View listItem = listAdapter.getView(i, null, listView);
//            try {
//                listItem.measure(0, 0);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            totalHeight += listItem.getMeasuredHeight();
//        }
//        ViewGroup.LayoutParams params = listView.getLayoutParams();
//        params.height = totalHeight
//                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
//        listView.setLayoutParams(params);
//        listView.requestLayout();
//
//    }

    /**
     * Method to set height expandable listview
     */
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

    @Override
    public void startActivityResult(Intent intent, int requestCode, int resultCode) {
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TEN_RESULT_CODE) {
            Log.e("callback", "");
            if (data != null) {
                if (data.getBooleanExtra("uploadFlag", true)) {
                    getOrderDetails();
                    expListView.setOnChildClickListener(this);
                }
            }
        } else if (requestCode == FIVE_REQUEST_CODE) {
            getOrderDetails();
            expListView.setOnChildClickListener(this);
            if (data != null) {
                if (data.getBooleanExtra("ratingDone", false)) {
                    MaterialDesignAnimations.fadeIn(this, findViewById(R.id.errorLayout), data.getStringExtra("message"), 1);
                }
            }

        } else {
            if (listAdapter != null)
                listAdapter.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        if (listAdapter != null)
            listAdapter.onChildClick(parent, v, groupPosition, childPosition, id);
        return false;
    }

    /**
     * Method to set color of status bar
     */
    private int statusBarColor(String status) {
        switch (status) {
            case "REACHED_PICKUP_LOCATION":
            case "ON_THE_WAY":
                return getResources().getColor(R.color.orange);

            case "ON_GOING":
            case "UP_GOING":
                return getResources().getColor(R.color.dark_blue);

            case "CONFIRMED":
            case "COMPLETED":
            case "REACHED_DESTINATION":
                return getResources().getColor(R.color.green);

            case "HALT":
                return getResources().getColor(R.color.dark_gery);

            case "CANCELED":
                return getResources().getColor(R.color.red);
            default:
                return 0;
        }
    }

    /**
     * Method for set text on button of status
     */
    private String textOnButton(String status) {
        btnStatus.setBackgroundResource(R.drawable.button_background_selector);
        switch (status) {
            case "ON_GOING":
            case "HALT":
            case "REACHED_DESTINATION":
                paymentCollectedLayout.setVisibility(View.VISIBLE);
                btnStatus.setVisibility(View.VISIBLE);
                return "END TRIP";

            case "ON_THE_WAY":
            case "REACHED_PICKUP_LOCATION":
                paymentCollectedLayout.setVisibility(View.VISIBLE);
                btnStatus.setVisibility(View.VISIBLE);
                return "START TRIP";

            case "CONFIRMED":
                paymentCollectedLayout.setVisibility(View.VISIBLE);
                btnStatus.setVisibility(View.VISIBLE);
                btnStatus.setBackgroundResource(R.drawable.orange_button_background_selector);
                return "ON THE WAY";

            case "COMPLETED":
                paymentCollectedLayout.setVisibility(View.VISIBLE);
            default:
                btnStatus.setVisibility(View.GONE);
                return "";
        }

    }

    /**
     * @param String Method for API call to change status of order
     */
    private void changeStatus(String orderStatus) {
        CommonUtils.showLoadingDialog(BookingDetailsActivity.this, getString(R.string.loading));
        RestClient.getWebServices().changeOrderStatus(accessToken, bookingId, orderStatus, new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                btnStatus.setVisibility(View.GONE);
                CommonUtils.dismissLoadingDialog();
                getOrderDetails();
            }

            @Override
            public void failure(RetrofitError error) {
                CommonUtils.showRetrofitError(BookingDetailsActivity.this, error);
                CommonUtils.dismissLoadingDialog();
            }
        });
    }
}


