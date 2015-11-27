package com.carrus.trucker.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.carrus.trucker.R;
import com.carrus.trucker.retrofit.RestClient;
import com.carrus.trucker.utils.ApiResponseFlags;
import com.carrus.trucker.utils.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class RatingDialogActivity extends BaseActivity implements View.OnClickListener {

    private EditText etComment;
    private RatingBar ratingBar;
    private ImageView crossButton;
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
        bookingId = getIntent().getStringExtra("bookingId");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ratingSubmitBtn:
                if (etComment.getText().toString().trim().isEmpty()) {
                    CommonUtils.showSingleButtonPopup(RatingDialogActivity.this, "Enter your feedback");
                } else {
                    sendRating();
                }
                break;

            case R.id.crossIV:
                Intent intent=new Intent();
                intent.putExtra("ratingDone",false);
                intent.putExtra("message","");
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }

    public void sendRating() {
        CommonUtils.showLoadingDialog(RatingDialogActivity.this,"Sending...");
        RestClient.getWebServices().addRating(sharedPreferences.getString(ACCESS_TOKEN, ""), bookingId, String.valueOf(ratingBar.getRating()), etComment.getText().toString(), new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                try {
                    CommonUtils.dismissLoadingDialog();
                    JSONObject jsonObject = new JSONObject(s);
//                    CommonUtils.showSingleButtonPopup(RatingDialogActivity.this, jsonObject.getString("message"));
                    Intent intent=new Intent();
                    intent.putExtra("ratingDone",true);
                    intent.putExtra("message", jsonObject.getString("message"));
                    setResult(RESULT_OK, intent);
//                    setResult(RESULT_OK, new Intent());
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                    CommonUtils.dismissLoadingDialog();
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                CommonUtils.dismissLoadingDialog();
                try {

                    if (((RetrofitError) retrofitError).getKind() == RetrofitError.Kind.NETWORK) {
                        CommonUtils.showSingleButtonPopup(RatingDialogActivity.this, getString(R.string.internetConnectionError));
                    } else {
                        try {
                            String json = new String(((TypedByteArray) retrofitError.getResponse()
                                    .getBody()).getBytes());
                            JSONObject jsonObject = new JSONObject(json);
                            int statusCode = retrofitError.getResponse().getStatus();
                            if (statusCode == ApiResponseFlags.Unauthorized.getOrdinal()) {
                                Intent intent = new Intent(RatingDialogActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                        Intent.FLAG_ACTIVITY_NEW_TASK);
                                Toast.makeText(RatingDialogActivity.this, getString(R.string.session_expired), Toast.LENGTH_LONG).show();
                                startActivity(intent);
                                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                            } else {
                                CommonUtils.showSingleButtonPopup(RatingDialogActivity.this, jsonObject.get("message").toString());

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Intent intent=new Intent();
        intent.putExtra("ratingDone",false);
        intent.putExtra("message","");
        setResult(RESULT_OK, intent);
        finish();
    }
}
