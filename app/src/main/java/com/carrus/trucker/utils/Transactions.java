package com.carrus.trucker.utils;

import android.app.Activity;

import com.carrus.trucker.R;

public class Transactions {

    public static void showPreviousAnimation(Activity activity){
        activity.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    public static void showNextAnimation(Activity activity){
        activity.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
    }
}