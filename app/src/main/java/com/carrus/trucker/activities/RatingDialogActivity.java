package com.carrus.trucker.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.carrus.trucker.R;
import com.carrus.trucker.retrofit.RestClient;
import com.carrus.trucker.utils.ApiResponseFlags;
import com.carrus.trucker.utils.CommonUtils;
import com.carrus.trucker.utils.Log;
import com.carrus.trucker.utils.MaterialDesignAnimations;
import com.flurry.android.FlurryAgent;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class RatingDialogActivity extends BaseActivity implements View.OnClickListener {

    private EditText etComment;
    private RatingBar ratingBar;
    private ImageView ivCloseButton;
    private String bookingId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_rating);
        setupUI(getWindow().getDecorView().getRootView());
        init();

    }

    /**
     * Method to initialize all the {@link View}s inside the Layout of this
     * {@link Activity}
     */
    private void init() {
        //Get value from intent
        bookingId = getIntent().getStringExtra("bookingId");

        //Get Resource ID from XML
        etComment = (EditText) findViewById(R.id.etComment);
        ratingBar = (RatingBar) findViewById(R.id.ratingStars);
        ivCloseButton = (ImageView) findViewById(R.id.ivCloseButton);

        //Set LIsteners
        ivCloseButton.setOnClickListener(this);
        findViewById(R.id.btnSubmit).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSubmit:
                if (validate(etComment.getText().toString().trim(),ratingBar.getRating())) {
                    sendRating();
                }
                break;

            case R.id.ivCloseButton:
                setMessage(false, "");
                break;
        }
    }

    private void sendRating() {
        CommonUtils.showLoadingDialog(RatingDialogActivity.this, getString(R.string.sending));
        RestClient.getWebServices().addRating(accessToken, bookingId, String.valueOf(ratingBar.getRating()), etComment.getText().toString(), new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                FlurryAgent.onEvent("Shipper Rating mode");
                CommonUtils.dismissLoadingDialog();
                try {
                    setMessage(true, new JSONObject(s).getString("message"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                CommonUtils.dismissLoadingDialog();
                try {
                    Log.e("request succesfull", "RetrofitError = " + retrofitError.toString());
                    if (((RetrofitError) retrofitError).getKind() == RetrofitError.Kind.NETWORK) {
                        CommonUtils.alertDialog=null;
                        CommonUtils.showDialog(RatingDialogActivity.this, getString(R.string.internetConnectionError));
                        //MaterialDesignAnimations.fadeIn(activity, activity.findViewById(R.id.errorLayout), activity.getResources().getString(R.string.internetConnectionError), 0);
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
                                MaterialDesignAnimations.fadeIn(RatingDialogActivity.this, findViewById(R.id.errorLayout), jsonObject.get("message").toString(), 0);
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
        setMessage(false, "");
    }

    private void setMessage(boolean ratingDone, String message) {
        Intent intent = new Intent();
        intent.putExtra("ratingDone", ratingDone);
        intent.putExtra("message", message);
        setResult(RESULT_OK, intent);
        finish();
    }

    private boolean validate(String comment, float rating){
//        if(comment.isEmpty()){
//            MaterialDesignAnimations.fadeIn(this, findViewById(R.id.errorLayout), getString(R.string.enter_your_feedback), 0);
//            return false;
//        }
        if(rating==0.0){
            MaterialDesignAnimations.fadeIn(this, findViewById(R.id.errorLayout), getString(R.string.give_rating_msg), 0);
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, MY_FLURRY_APIKEY);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }
}
