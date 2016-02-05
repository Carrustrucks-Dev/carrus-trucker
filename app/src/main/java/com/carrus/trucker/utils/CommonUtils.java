package com.carrus.trucker.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.carrus.trucker.R;
import com.carrus.trucker.activities.LoginActivity;
import com.carrus.trucker.interfaces.AppConstants;

import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import retrofit.RetrofitError;
import retrofit.mime.TypedByteArray;

/**
 * Created by Saurbhv on 10/21/15.
 */
public class CommonUtils implements AppConstants {

    public static Dialog progressDial;
    public static Dialog dialog;
    public static String APP_VERSION = "0";
    public static AlertDialog.Builder alertDialog=null;
    public static int pastHitCount=0;
    public static int upComingHitCount=0;

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


    public static void showLoadingDialog(final Activity activity, String msg) {
        dismissLoadingDialog();

        progressDial = new Dialog(activity,
                R.style.Theme_AppCompat_Translucent);
        progressDial.setCancelable(false);
        //progressDial.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        WindowManager.LayoutParams layoutParams = progressDial.getWindow()
                .getAttributes();
        progressDial.setContentView(R.layout.loading_box);
        ProgressBar progressBar = (ProgressBar) progressDial.findViewById(R.id.progressBar1);
        progressBar.getIndeterminateDrawable().setColorFilter(0xFF2362C0, android.graphics.PorterDuff.Mode.MULTIPLY);
        @SuppressWarnings("unused")
        TextView dialogTxt = (TextView) progressDial.findViewById(R.id.tv101);
        progressDial.show();
        //dialogTxt.setText(msg);

    }

    /**
     * Show the circular progress bar as loading with the message
     *
     * @param activity on which it is to be displayed
     * @param message  that is to be shown
     */
    /*public static void showLoadingDialog(final Activity activity, String message) {
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
    }*/
    public static void dismissLoadingDialog() {
        try {
            if (progressDial != null && progressDial.isShowing()) {
                progressDial.dismiss();
                progressDial = null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

       /* if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }*/
    }

    public static void showSingleButtonPopup(Context context, String msg) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setCancelable(false);
        alertDialog.setMessage(msg);
        alertDialog.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        alertDialog.show();
    }

    /**
     * public static void showSingleButtonPopup(Context context, String msg) {
     * final Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
     * <p/>
     * //setting custom layout to dialog
     * dialog.setContentView(R.layout.single_button_custom_dialog);
     * WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
     * lp.dimAmount = 0.7f; // Dim level. 0.0 - no dim, 1.0 - completely opaque
     * dialog.getWindow().setAttributes(lp);
     * dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
     * dialog.setCancelable(true);
     * dialog.setCanceledOnTouchOutside(false);
     * <p/>
     * //adding text dynamically
     * TextView txt = (TextView) dialog.findViewById(R.id.textMessage);
     * txt.setText(msg);
     * <p/>
     * //adding button click event
     * Button dismissButton = (Button) dialog.findViewById(R.id.button);
     * dismissButton.setOnClickListener(new View.OnClickListener() {
     *
     * @Override public void onClick(View v) {
     * dialog.dismiss();
     * }
     * });
     * dialog.show();
     * }
     */

    public static void showRetrofitError(Activity activity, RetrofitError retrofitError) {
        try {
            Log.e("request succesfull", "RetrofitError = " + retrofitError.toString());
            if (((RetrofitError) retrofitError).getKind() == RetrofitError.Kind.NETWORK) {
                showDialog(activity, activity.getResources().getString(R.string.internetConnectionError));
                //MaterialDesignAnimations.fadeIn(activity, activity.findViewById(R.id.errorLayout), activity.getResources().getString(R.string.internetConnectionError), 0);
            } else {
                try {
                    String json = new String(((TypedByteArray) retrofitError.getResponse()
                            .getBody()).getBytes());
                    JSONObject jsonObject = new JSONObject(json);
                    int statusCode = retrofitError.getResponse().getStatus();
                    if (statusCode == ApiResponseFlags.Unauthorized.getOrdinal()) {
                        Intent intent = new Intent(activity, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                        Toast.makeText(activity, activity.getString(R.string.session_expired), Toast.LENGTH_LONG).show();
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

    public static void showDialog(final Context context, String msg) {
        try {
            if (alertDialog == null) {
                alertDialog = new AlertDialog.Builder(context);
                alertDialog.setCancelable(false);
                alertDialog.setMessage(msg);
                alertDialog.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        alertDialog = null;

                    }
                });
                alertDialog.setNegativeButton(context.getString(R.string.call_carrus), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        try {
                            Intent call = new Intent(Intent.ACTION_DIAL);
                            call.setData(Uri.parse("tel:" + "+91" + CONTACT_CARRUS));
                            context.startActivity(call);
                        } catch (Exception e) {
                            CommonUtils.showSingleButtonPopup(context, "Unable to perform action.");
                        }
                        alertDialog = null;

                    }
                });

                alertDialog.show();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * @param ctx
     * @param v
     */
    public static void slide_down(Context ctx, View v) {

        Animation a = AnimationUtils.loadAnimation(ctx, R.anim.slide_down);
        if (a != null) {
            a.reset();
            if (v != null) {
                v.clearAnimation();
                v.startAnimation(a);
                v.setVisibility(View.VISIBLE);

            }
        }
    }

    public static void slide_up(Context ctx, View v) {

        Animation a = AnimationUtils.loadAnimation(ctx, R.anim.slide_up);
        if (a != null) {
            a.reset();
            if (v != null) {
                v.clearAnimation();
                v.startAnimation(a);
                v.setVisibility(View.GONE);
            }
        }
    }

    public static String getDateSuffix(int i) {
        switch (i) {
            case 1:
            case 21:
            case 31:
                return ("st");

            case 2:
            case 22:
                return ("nd");

            case 3:
            case 23:
                return ("rd");

            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
                return ("th");
            default:
                return ("");
        }
    }

    public static boolean isGPSEnabled(Context context) {
        GPSTracker gpsTracker = new GPSTracker(context);
        if (gpsTracker.canGetLocation()) {
            return true;
        } else {
            gpsTracker.showSettingsAlert();
            return false;
        }
    }

    public static int getCameraPhotoOrientation(String imagePath) {
        int rotate = 0;
        try {
            //context.getContentResolver().notifyChange(imageUri, null);
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(
                    imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }


            android.util.Log.v("Exif orientation: ", "" + orientation);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static void hideSoftKeyboard(Activity activity) {
        if (activity != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static String getMonthNameFromUTC(String date) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        DateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        f.setTimeZone(TimeZone.getTimeZone("ISO"));
        try {
            Date d = f.parse(date);
            DateFormat month = new SimpleDateFormat("MMMM");
            return month.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static int getDayNumberFromUTC(String date) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        DateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        f.setTimeZone(TimeZone.getTimeZone("ISO"));
        try {
            Date d = f.parse(date);
            DateFormat dayNumber = new SimpleDateFormat("d");
            return Integer.parseInt(dayNumber.format(d));
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getDayNameNumberFromUTC(String date) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        DateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        f.setTimeZone(TimeZone.getTimeZone("ISO"));
        try {
            Date d = f.parse(date);
            DateFormat day = new SimpleDateFormat("EEEE, d");
            return day.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getDateFromUTC(String date) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        DateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        f.setTimeZone(TimeZone.getTimeZone("ISO"));
        try {
            Date d = f.parse(date);
            DateFormat day = new SimpleDateFormat("dd");
            return day.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getDayNameFromUTC(String date) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        DateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        f.setTimeZone(TimeZone.getTimeZone("ISO"));
        try {
            Date d = f.parse(date);
            DateFormat day = new SimpleDateFormat("EEEE");
            return day.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getShortDayNameFromUTC(String date) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        DateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        f.setTimeZone(TimeZone.getTimeZone("ISO"));
        try {
            Date d = f.parse(date);
            DateFormat day = new SimpleDateFormat("EEE");
            return day.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getShortMonthNameFromUTC(String date) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        DateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        f.setTimeZone(TimeZone.getTimeZone("ISO"));
        try {
            Date d = f.parse(date);
            DateFormat day = new SimpleDateFormat("MMM");
            return day.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void phoneCall(Context context, String phoneNumber) {
        try {
            Intent call = new Intent(Intent.ACTION_DIAL);
            call.setData(Uri.parse("tel:" + "+91" + phoneNumber));
            context.startActivity(call);
        } catch (Exception e) {
            CommonUtils.showSingleButtonPopup(context, "Unable to perform action.");
        }
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

}
