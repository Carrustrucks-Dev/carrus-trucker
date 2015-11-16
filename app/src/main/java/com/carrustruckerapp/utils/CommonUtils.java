package com.carrustruckerapp.utils;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.carrustruckerapp.R;
import com.carrustruckerapp.activities.LoginActivity;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.RetrofitError;
import retrofit.mime.TypedByteArray;

/**
 * Created by Saurbhv on 10/21/15.
 */
public class CommonUtils {

    public static ProgressDialog progressDial;
    public static Dialog dialog;

    public static String toCamelCase(String inputString) {
        String result = "";
        if (inputString.length() == 0) {
            return result;
        }
        char firstChar = inputString.charAt(0);
        char firstCharToUpperCase = Character.toUpperCase(firstChar);
        result = result + firstCharToUpperCase;
        for (int i = 1; i < inputString.length(); i++) {
            char currentChar = inputString.charAt(i);
            char previousChar = inputString.charAt(i - 1);
            if (previousChar == ' ') {
                char currentCharToUpperCase = Character.toUpperCase(currentChar);
                result = result + currentCharToUpperCase;
            } else {
                char currentCharToLowerCase = Character.toLowerCase(currentChar);
                result = result + currentCharToLowerCase;
            }
        }
        return result;
    }


    public static Boolean validateEmail(final String email) {
        final String EMAIL_PATTERN = Patterns.EMAIL_ADDRESS.toString();
//"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

//    public void showLoadingDialog(Context context, String msg) {
//        dismissLoadingDialog();
//
//        progressDial = new ProgressDialog(context);
//        progressDial.setCancelable(false);
//        progressDial.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//        progressDial.show();
//        WindowManager.LayoutParams layoutParams = progressDial.getWindow()
//                .getAttributes();
//        progressDial.setContentView(R.layout.loading_box);
//        @SuppressWarnings("unused")
//                TextView dialogTxt = (TextView) progressDial.findViewById(R.id.tv101);
//        dialogTxt.setText(msg);
//
//    }

    /**
     * Show the circular progress bar as loading with the message
     *
     * @param activity on which it is to be displayed
     * @param message  that is to be shown
     */
    public static void showLoadingDialog(final Activity activity, String message) {
        try {
            dismissLoadingDialog();
            dialog = new Dialog(activity,
                    R.style.Theme_AppCompat_Translucent);

            // Configure dialog box
            dialog.setContentView(R.layout.progress_layout);
            WindowManager.LayoutParams layoutParams = dialog.getWindow()
                    .getAttributes();
            layoutParams.dimAmount = 0.6f;
            dialog.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);

            // set progress drawable
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                ProgressBar progressBar = (ProgressBar) dialog.findViewById(R.id.circular_progress);
                progressBar.setIndeterminateDrawable(activity.getResources().getDrawable(R.drawable.progress));
            }

            // set the message
            if (!message.isEmpty()) {
                final TextView dialogMsg = (TextView) dialog.findViewById(R.id.progress_msg);
                dialogMsg.setText(message);
            }

            try {
                dialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void dismissLoadingDialog() {
//        if (progressDial != null) {
//            progressDial.dismiss();
//            progressDial = null;
//        }

        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    public static void showSingleButtonPopup(Context context, String msg){
        final Dialog dialog = new Dialog(context,android.R.style.Theme_Translucent_NoTitleBar);

        //setting custom layout to dialog
        dialog.setContentView(R.layout.single_button_custom_dialog);
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.dimAmount = 0.7f; // Dim level. 0.0 - no dim, 1.0 - completely opaque
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        //adding text dynamically
        TextView txt = (TextView) dialog.findViewById(R.id.textMessage);
        txt.setText(msg);

        //adding button click event
        Button dismissButton = (Button) dialog.findViewById(R.id.button);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public static void showRetrofitError(Activity activity,RetrofitError retrofitError){
        try {
            Log.e("request succesfull", "RetrofitError = " + retrofitError.toString());
            if (((RetrofitError) retrofitError).getKind() == RetrofitError.Kind.NETWORK) {
                MaterialDesignAnimations.fadeIn(activity, activity.findViewById(R.id.errorLayout), activity.getResources().getString(R.string.internetConnectionError), 0);
            } else {
                try {
                    String json = new String(((TypedByteArray) retrofitError.getResponse()
                            .getBody()).getBytes());
                    JSONObject jsonObject = new JSONObject(json);
                    int statusCode = retrofitError.getResponse().getStatus();
                    if (statusCode == 401) {
                        Intent intent = new Intent(activity, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                        activity.startActivity(intent);
                        activity.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                    } else {
                        MaterialDesignAnimations.fadeIn(activity, activity.findViewById(R.id.errorLayout), jsonObject.get("message").toString(), 0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param ctx
     * @param v
     */
    public static void slide_down(Context ctx, View v){

        Animation a = AnimationUtils.loadAnimation(ctx, R.anim.slide_down);
        if(a != null){
            a.reset();
            if(v != null){
                v.clearAnimation();
                v.startAnimation(a);
                v.setVisibility(View.VISIBLE);

            }
        }
    }

    public static void slide_up(Context ctx, View v){

        Animation a = AnimationUtils.loadAnimation(ctx, R.anim.slide_up);
        if(a != null){
            a.reset();
            if(v != null){
                v.clearAnimation();
                v.startAnimation(a);
                v.setVisibility(View.GONE);
            }
        }
    }

    public  String getDateSuffix( int i) {
        switch (i) {
            case 1: case 21: case 31:
                return ("st");

            case 2: case 22:
                return ("nd");

            case 3: case 23:
                return ("rd");

            case 4: case 5:
            case 6: case 7:
            case 8: case 9:
            case 10: case 11:
            case 12: case 13:
            case 14: case 15:
            case 16: case 17:
            case 18: case 19:
            case 20: case 24:
            case 25: case 26:
            case 27: case 28:
            case 29: case 30:
                return ("th");
            default:
                return ("");
        }
    }

    public static boolean isGPSEnabled(Context context){
        GPSTracker gpsTracker=new GPSTracker(context);
        if(gpsTracker.canGetLocation()){
            return true;
        }else{
            gpsTracker.showSettingsAlert();
            return false;
        }
    }
}
