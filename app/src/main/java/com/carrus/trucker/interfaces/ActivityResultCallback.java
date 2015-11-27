package com.carrus.trucker.interfaces;

import android.content.Intent;

/**
 * Created by Saurbhv on 11/17/15.
 */
public interface ActivityResultCallback {

    void startActivityResult(Intent intent,int requestCode,int resultCode);
    void getOrderDetails();
}
