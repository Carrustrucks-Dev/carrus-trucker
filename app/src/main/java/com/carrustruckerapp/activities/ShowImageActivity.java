package com.carrustruckerapp.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.carrustruckerapp.R;
import com.carrustruckerapp.retrofit.RestClient;
import com.carrustruckerapp.utils.CommonUtils;
import com.carrustruckerapp.utils.Log;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

/**
 * Created by Saurbhv on 11/17/15.
 */
public class ShowImageActivity extends BaseActivity  {

    public ImageView closeButton;
    private   Button uploadNewButton;
    String orderId,documentName,imagePath;
    public SharedPreferences sharedPreferences;
    public Map<String, TypedFile> images;
    private String url;
    private ImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show_image);
        images = new HashMap<String, TypedFile>();
        closeButton=(ImageView)findViewById(R.id.imageView_close);
        uploadNewButton=(Button)findViewById(R.id.upload_new_document);
        sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        Intent intent=getIntent();
        url=intent.getStringExtra("url");
        orderId=intent.getStringExtra("orderId");
        documentName=intent.getStringExtra("documentName");
        imageView=(ImageView) findViewById(R.id.image);
        Picasso.with(this).
                load(url).
                placeholder(R.drawable.loading_placeholder)
                .into(imageView);

        View.OnClickListener handler = new View.OnClickListener() {
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.imageView_close:
                        finish();
                        overridePendingTransition(0,0);
                        break;
                    case R.id.upload_new_document:
                        selectImage();
                        break;
                }
            }
        };

        findViewById(R.id.imageView_close).setOnClickListener(handler);
        uploadNewButton.setOnClickListener(handler);

    }

    void selectImage() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getResources().getString(R.string.upload_documents));
        dialog.setItems(R.array.image_menu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    try {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent, LOAD_IMAGE_RESULTS);
//                        startActivityForResult(Intent.createChooser(intent, _context.getString(R.string.select_picture)), LOAD_IMAGE_RESULTS);
                    } catch (Exception ex) {
                        Toast.makeText(ShowImageActivity.this,getResources().getString(R.string.unable_to_perform_action), Toast.LENGTH_LONG).show();
                    }

                } else if (item == 1) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/pdf");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, LOAD_PDF_FILE);

                } else {
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Matrix mat = new Matrix();
        if (requestCode == LOAD_IMAGE_RESULTS && resultCode == -1 && data != null) {

            try {
                imagePath = CommonUtils.getPath(this, data.getData());
                mat.postRotate(CommonUtils.getCameraPhotoOrientation(imagePath));
                Log.e("Imagepath", "" + imagePath);
                if (documentName.equalsIgnoreCase(getResources().getString(R.string.pod))) {
                    images.put("pod", new TypedFile("image/*", new File(imagePath)));
                } else if (documentName.equalsIgnoreCase(getResources().getString(R.string.invoice))) {
                    images.put("invoice", new TypedFile("image/*", new File(imagePath)));
                } else if(documentName.equalsIgnoreCase(getResources().getString(R.string.consignment))){
                    images.put("consigmentNote", new TypedFile("image/*", new File(imagePath)));
                }
                CommonUtils.showLoadingDialog(ShowImageActivity.this, "Uploading...");
                RestClient.getWebServices().uploadDocument(sharedPreferences.getString(ACCESS_TOKEN, ""), new TypedString(orderId), images, new Callback<String>() {
                    @Override
                    public void success(String s, Response response) {
                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            CommonUtils.dismissLoadingDialog();
                            CommonUtils.showSingleButtonPopup(ShowImageActivity.this, jsonObject.getString("message"));
                            switch (documentName) {
                                case "POD":
                                    url = jsonObject.getJSONObject("data").getString("podUpload");
                                    break;
                                case "Invoice":
                                    url = jsonObject.getJSONObject("data").getString("invoiceUpdate");
                                    break;
                                case "Consignment Notes":
                                    url = jsonObject.getJSONObject("data").getString("consigmentNoteUpdate");
                                    break;
                            }
                            Picasso.with(ShowImageActivity.this).
                                    load(url).
                                    placeholder(R.drawable.loading_placeholder)
                                    .into(imageView);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            CommonUtils.dismissLoadingDialog();
                        }

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        CommonUtils.showRetrofitError(ShowImageActivity.this, error);
                        CommonUtils.showSingleButtonPopup(ShowImageActivity.this, "Oops!! Some error occurred. Please try again. ");
                        CommonUtils.dismissLoadingDialog();
                    }
                });
            } catch (Exception ne) {
                Toast.makeText(ShowImageActivity.this, getResources().getString(R.string.unable_to_perform_action), Toast.LENGTH_LONG).show();
            }
        }
        else if(requestCode == LOAD_PDF_FILE && resultCode == -1 && data != null){
            try {
                imagePath = CommonUtils.getPath(this, data.getData());
                mat.postRotate(CommonUtils.getCameraPhotoOrientation(imagePath));
                Log.e("Imagepath", "" + imagePath);
                if (documentName.equalsIgnoreCase(getResources().getString(R.string.pod))) {
                    images.put("pod", new TypedFile("*/*", new File(imagePath)));
                } else if (documentName.equalsIgnoreCase(getResources().getString(R.string.invoice))) {
                    images.put("invoice", new TypedFile("*/*", new File(imagePath)));
                } else if(documentName.equalsIgnoreCase(getResources().getString(R.string.consignment))){
                    images.put("consigmentNote", new TypedFile("*/*", new File(imagePath)));
                }
                CommonUtils.showLoadingDialog(ShowImageActivity.this, "Uploading...");
                RestClient.getWebServices().uploadDocument(sharedPreferences.getString(ACCESS_TOKEN, ""), new TypedString(orderId), images, new Callback<String>() {
                    @Override
                    public void success(String s, Response response) {
                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            CommonUtils.dismissLoadingDialog();
                            CommonUtils.showSingleButtonPopup(ShowImageActivity.this, jsonObject.getString("message"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            CommonUtils.dismissLoadingDialog();
                        }

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        CommonUtils.showRetrofitError(ShowImageActivity.this, error);
                        CommonUtils.showSingleButtonPopup(ShowImageActivity.this, "Oops!! Some error occurred. Please try again. ");
                        CommonUtils.dismissLoadingDialog();
                    }
                });
            } catch (Exception ne) {
                Toast.makeText(ShowImageActivity.this, getResources().getString(R.string.unable_to_perform_action), Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }
}