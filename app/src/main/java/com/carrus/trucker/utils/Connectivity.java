package com.carrus.trucker.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.carrus.trucker.R;

/**
 * Created by Saurbhv on 10/26/15.
 */
public class Connectivity {



    private Activity context;

    public Connectivity(Activity context){
        this.context = context;
    }

    public boolean isConnectingToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {

                        return true;
                    }

        }
        internetSettingPopup();
        return false;
    }

    public void internetSettingPopup() {
        try {
            MaterialDesignAnimations.fadeIn(context, context.findViewById(R.id.errorLayout), context.getResources().getString(R.string.internetConnectionError), 0);
//
//            final Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
//            dialog.setContentView(R.layout.internet_setting_dialog);
//            WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
//            lp.dimAmount = 0.7f; // Dim level. 0.0 - no dim, 1.0 - completely opaque
//            dialog.getWindow().setAttributes(lp);
//            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//            dialog.setCancelable(false);
//            dialog.setCanceledOnTouchOutside(false);
//            Button btnOk = (Button) dialog.findViewById(R.id.btnOk);
//            //Button btnNo = (Button) dialog.findViewById(R.id.btnNo);
//            btnOk.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    dialog.dismiss();
////                    context.startActivity(new Intent(
////                            Settings.ACTION_AIRPLANE_MODE_SETTINGS));
//
//                }
//
//            });
////            btnNo.setOnClickListener(new View.OnClickListener() {
////                @Override
////                public void onClick(View view) {
////                    dialog.dismiss();
////                    //Toast.makeText(context, "No internet connection.", Toast.LENGTH_LONG).show();
////                    context.finish();
////                }
////
////            });
//            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

