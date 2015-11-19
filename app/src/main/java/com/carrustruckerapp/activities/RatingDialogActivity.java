package com.carrustruckerapp.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;

import com.carrustruckerapp.R;
import com.carrustruckerapp.interfaces.WebServices;
import com.carrustruckerapp.utils.CommonUtils;
import com.carrustruckerapp.utils.GlobalClass;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RatingDialogActivity extends BaseActivity implements View.OnClickListener {

    private EditText etComment;
    private RatingBar ratingBar;
    private ImageView crossButton;
    public GlobalClass globalClass;
    public WebServices webServices;
    public SharedPreferences sharedPreferences;
    private String bookingId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_rating);
        setupUI(getWindow().getDecorView().getRootView());
        init();

    }

    private void init() {
        etComment = (EditText) findViewById(R.id.feedbackEdtxt);
        ratingBar = (RatingBar) findViewById(R.id.ratingStars);
        crossButton = (ImageView) findViewById(R.id.crossIV);
        crossButton.setOnClickListener(this);
        findViewById(R.id.ratingSubmitBtn).setOnClickListener(this);
        sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        globalClass = (GlobalClass) getApplicationContext();
        webServices = globalClass.getWebServices();
        bookingId = getIntent().getStringExtra("bookingId");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ratingSubmitBtn:
                if (etComment.getText().toString().trim().isEmpty()) {
                    CommonUtils.showSingleButtonPopup(RatingDialogActivity.this, "Enter your comment");
                } else {
                    sendRating();
                }
                break;

            case R.id.crossIV:
                finish();
                break;
        }
    }

    public void sendRating() {
        CommonUtils.showSingleButtonPopup(RatingDialogActivity.this,"Sending...");
        webServices.addRating(sharedPreferences.getString(ACCESS_TOKEN, ""), bookingId, String.valueOf(ratingBar.getRating()), etComment.getText().toString(), new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                try {
                    CommonUtils.dismissLoadingDialog();
                    JSONObject jsonObject = new JSONObject(s);
                    CommonUtils.showSingleButtonPopup(RatingDialogActivity.this, jsonObject.getString("message"));
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                    CommonUtils.dismissLoadingDialog();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                CommonUtils.dismissLoadingDialog();
                CommonUtils.showSingleButtonPopup(RatingDialogActivity.this, "Please try again.");
            }
        });
    }
}
