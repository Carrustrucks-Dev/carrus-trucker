package com.carrus.trucker.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.carrus.trucker.R;

/**
 * Created by Saurbhv on 10/21/15.
 */
public  class MaterialDesignAnimations {
    /***material design anims***/
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void appllyLoadInAnimation(View myView)
    {

        int cx = (myView.getLeft() + myView.getRight()) / 2;
        int cy = (myView.getTop() + myView.getBottom()) / 2;

// get the final radius for the clipping circle
        int finalRadius = Math.max(myView.getWidth(), myView.getHeight());

// create the animator for this view (the start radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);

// make the view visible and start the animation
        myView.setVisibility(View.VISIBLE);
        anim.start();
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void applyHideAnimation(final View view)
    {
        // previously visible view


// get the center for the clipping circle
        int cx = (view.getLeft() + view.getRight()) / 2;
        int cy = (view.getTop() + view.getBottom()) / 2;

// get the initial radius for the clipping circle
        int initialRadius = view.getWidth();

// create the animation (the final radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, 0);

// make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.INVISIBLE);
            }
        });

// start the animation
        anim.start();
    }

    /*
    Fade in animation
    success 0 for green 1 for red
     */
    public static void fadeIn(final Context context, final View view,String message,int success) {
        LinearLayout layouErrorMessage= null;
        try {
            layouErrorMessage = (LinearLayout)view.findViewById(R.id.layouErrorMessage);
            if(success==0)
                layouErrorMessage.setBackgroundColor(Color.parseColor("#ff5500"));
            else
                layouErrorMessage.setBackgroundColor(Color.parseColor("#22C064"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        Animation animationFadeIn = AnimationUtils.loadAnimation(context, R.anim.fadein);
        view.setAnimation(animationFadeIn);
        TextView textView=(TextView)view.findViewById(R.id.errorMessage);
        textView.setText(message);
        view.setVisibility(View.VISIBLE);

        Handler handler = new Handler();

        handler.postDelayed(new Runnable()
        {

            @Override
            public void run()

            {
                fadeOut(context,view);

            }
        }, 2000);
    }

    /*
Fade in animation
 */
    private static void fadeOut(Context context,View view) {
        final Animation animationFadeOut = AnimationUtils.loadAnimation(context, R.anim.fadeout);
        view.clearAnimation();
        view.setAnimation(animationFadeOut);
        view.setVisibility(View.INVISIBLE);

    }


}


