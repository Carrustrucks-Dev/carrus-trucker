package com.carrustruckerapp.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
public class ShowImageActivity extends BaseActivity  implements View.OnClickListener{

    public ImageView closeButton;
    private Button uploadNewButton;
    String orderId, documentName, imagePath;
    public Map<String, TypedFile> images;
    private String url;
    private ImageView imageView;
    private WebView webView;
    private boolean uploadFlag=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show_image);
        setupUI(getWindow().getDecorView().getRootView());
        images = new HashMap<String, TypedFile>();
        closeButton = (ImageView) findViewById(R.id.imageView_close);
        uploadNewButton = (Button) findViewById(R.id.upload_new_document);
        url = getIntent().getStringExtra("url");
        orderId = getIntent().getStringExtra("orderId");
        documentName = getIntent().getStringExtra("documentName");
        imageView = (ImageView) findViewById(R.id.image);
        webView = (WebView) findViewById(R.id.webView);
        showDocument(url);
        findViewById(R.id.imageView_close).setOnClickListener(this);
        uploadNewButton.setOnClickListener(this);

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
                    } catch (Exception ex) {
                        Toast.makeText(ShowImageActivity.this, getResources().getString(R.string.unable_to_perform_action), Toast.LENGTH_LONG).show();
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
                filenameAndUpload();
            } catch (Exception ne) {
                Toast.makeText(ShowImageActivity.this, getResources().getString(R.string.unable_to_perform_action), Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == LOAD_PDF_FILE && resultCode == -1 && data != null) {
            try {
                imagePath = CommonUtils.getPath(this, data.getData());
                mat.postRotate(CommonUtils.getCameraPhotoOrientation(imagePath));
                Log.e("Imagepath", "" + imagePath);
                filenameAndUpload();
            } catch (Exception ne) {
                Toast.makeText(ShowImageActivity.this, getResources().getString(R.string.unable_to_perform_action), Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent=new Intent();
        intent.putExtra("uploadFlag", uploadFlag);
        setResult(RESULT_OK, intent);
        overridePendingTransition(0, 0);
    }

    private void showDocument(String url) {
        String extension = url.substring(url.lastIndexOf("."));
        if (extension.equalsIgnoreCase(".pdf")) {
            imageView.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//            startWebView("https://docs.google.com/gview?embedded=true&url=" + url);
//            CommonUtils.showLoadingDialog(ShowImageActivity.this, "Loading...");
            webView.setWebChromeClient(new WebChromeClient());
            webView.loadUrl("https://docs.google.com/gview?embedded=true&url=" + url);


        } else {
            imageView.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
            Picasso.with(this).
                    load(url).
                    placeholder(R.drawable.loading_placeholder)
                    .into(imageView);
        }
    }

    private void filenameAndUpload() {
        switch (documentName.toUpperCase()) {
            case "POD":
                images.put("pod", new TypedFile("*/*", new File(imagePath)));
                break;
            case "Invoice":
                images.put("invoice", new TypedFile("*/*", new File(imagePath)));
                break;
            case "Consignment Notes":
                images.put("consigmentNote", new TypedFile("*/*", new File(imagePath)));
                break;
        }

        uploadDocumentApi();

    }

    private void uploadDocumentApi() {
        CommonUtils.showLoadingDialog(ShowImageActivity.this, "Uploading...");
        RestClient.getWebServices().uploadDocument(accessToken, new TypedString(orderId), images, new Callback<String>() {
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
                    showDocument(url);
                    uploadFlag=true;
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
    }


    private void startWebView(String url) {


        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            public void onLoadResource(WebView view, String url) {
//                CommonUtils.showLoadingDialog(ShowImageActivity.this, "Loading...");
            }

            public void onPageFinished(WebView view, String url) {
                CommonUtils.dismissLoadingDialog();
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView_close:
                Intent intent=new Intent();
                intent.putExtra("uploadFlag",uploadFlag);
                setResult(RESULT_OK, intent);
                finish();
                overridePendingTransition(0, 0);
                break;
            case R.id.upload_new_document:
                selectImage();
                break;
        }
    }
}